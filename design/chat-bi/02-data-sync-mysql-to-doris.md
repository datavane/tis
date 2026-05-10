# 02 - 数据通路：MySQL → Doris

> **前置依赖**：01（本体的 `DataSourceBinding.dbName` 已指向 Doris 数据源；同步任务另行提供 MySQL 源端信息）。
> **后续被依赖**：03（GraphRAG 查询的是 Doris 物理数据）、05（测试要把 Falcon 的 SQLite 数据灌入 Doris）。

## 1. 目标

| 编号 | 目标 | 说明 |
|------|------|-----|
| D1 | 基于 TIS 现有插件链路，把 MySQL 表按本体定义同步到 Doris | 批量首次 + 增量 CDC 双模式 |
| D2 | Doris 建表由本体驱动 | 从 `OntologyObjectType + DataSourceBinding(→Doris ds) + Property + SemanticRole` 反推 DDL |
| D3 | 类型映射稳定 | MySQL ↔ Doris 一份固化映射表，跑在同步任务侧，不污染本体 |
| D4 | 延迟：单表增量 P95 ≤ 2min；全量 100w 行 ≤ 30min | 可压测验证 |
| D5 | 测试场景能把 Falcon 17 个 dev 库的 SQLite 数据迁到 Doris | 见 05 文档 |

## 2. 架构

```
              ┌────────────────────┐
              │  本体（Neo4j 副本） │  (01 §2.6 同步产出)
              │  含 OT/Property/    │
              │  binding 等节点     │
              └─────────┬──────────┘
                        │  Cypher 查询本体元信息
                        ▼
  ┌───────────────────────────────────────────────────┐
  │  DDL 生成器 (Ontology → Doris CREATE TABLE)       │
  │  tis-plugin/.../ontology/sync/DorisDDLGenerator    │
  │  分区/分桶由 SemanticRole 推导                     │
  └────────────────┬──────────────────────────────────┘
                   │  doris.sql
                   ▼
        ┌─────────────────────┐
        │  Doris 目标库       │
        └─────────────────────┘
                   ▲
                   │ 数据写入
                   │
  ┌────────────────┴──────────────────────────────────┐
  │   TIS 同步任务（已有能力复用）                     │
  │   - 批量：DataX MySQL Reader + Doris Writer        │
  │   - 增量：Flink-CDC MySQL Source + Doris Sink     │
  │   同步任务自身配置 "源 MySQL ds" + "目标 Doris ds" │
  │   不依赖 OT.binding（同步阶段 OT.binding 可能仍是  │
  │   MySQL；同步成功后由 01 §2.7 的切换功能改写）      │
  └───────────────────────────────────────────────────┘
                   ▲
                   │
         ┌─────────┴──────────┐
         │   MySQL (源)        │
         │   仅存在于同步任务  │
         │   不进入本体        │
         └─────────────────────┘
```

## 3. 类型映射（MySQL → Doris）

| MySQL 类型 | Doris 类型 | 备注 |
|------------|-----------|------|
| TINYINT | TINYINT | |
| SMALLINT | SMALLINT | |
| INT / INTEGER | INT | |
| BIGINT | BIGINT | |
| FLOAT | FLOAT | |
| DOUBLE | DOUBLE | |
| DECIMAL(p,s) | DECIMAL(p,s) | p ≤ 38，超出降级到 DECIMALV3 |
| CHAR(n) | CHAR(n) | n ≤ 255 |
| VARCHAR(n) | VARCHAR(n) | n > 65533 → STRING |
| TEXT / LONGTEXT | STRING | |
| DATE | DATE | |
| DATETIME / TIMESTAMP | DATETIME | |
| JSON | JSON | Doris 2.x 原生支持 |
| BLOB / BINARY | STRING(base64) | 不建议同步，打印 warning |
| ENUM / SET | VARCHAR | 转字面量 |

映射由 `MySqlToDorisTypeMapper` 工具类承载（新增于 `tis-plugin/.../ontology/sync/`），仅在同步任务内部使用。本体侧记录的是抽象 `OntologyType`，不参与 MySQL/Doris 物理类型的双向映射。

## 4. 表模型策略

Doris 建表支持三种模型：Duplicate / Aggregate / Unique。默认策略：

| ObjectType 特征 | Doris 模型 | 说明 |
|---|---|---|
| 存在明确 PK（`OntologyProperty.pk`） | **Unique Key** | 增量 upsert 友好 |
| 无 PK、纯事实表（全部 Measure） | **Aggregate** | 对所有 Measure 按默认 agg 聚合 |
| 其它 | **Duplicate** | 明细表 |

分区：
- 若存在 `TimeDimension` 列，按"月"分区
- 否则不分区
- 用户若需自定义，可在**同步任务**配置中覆盖（不污染本体）

Bucket：
- 默认取 PK；无 PK 则取首个 `Identifier` 角色列；都没有则 random 32 bucket
- 同样可在同步任务配置中覆盖

## 5. DDL 生成器接口草案

```java
public interface DorisDDLGenerator {
    /** 根据 ObjectType 生成 CREATE TABLE。dorisDsName 由同步任务配置提供，不强制等于 OT.binding.dbName */
    String generate(OntologyObjectType ot, String dorisDsName, DorisPhysicalHints hints);

    /** 只生成 ADD COLUMN/DROP COLUMN 的增量 DDL */
    List<String> generateDelta(OntologyObjectType ot, String dorisDsName,
                               DorisPhysicalHints hints, DorisTableMeta existing);
}

/** 物理表参数，由同步任务配置提供，不存于本体 */
public record DorisPhysicalHints(
    String physicalTableName,   // 默认 = ot.name
    String partitionColumn,     // 默认按 TimeDimension 推导
    String[] bucketColumns,     // 默认按 PK / Identifier 推导
    int buckets                 // 默认 32
) {}
```

实现位置：`tis-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/sync/`

## 6. 同步任务编排

### 6.1 同步任务配置维度
一次同步任务包含三组配置：
- **源端**：MySQL 数据源、待同步表清单（不进入本体）
- **目标端**：Doris 数据源 + `DorisPhysicalHints`（同步任务自行配置，不强制要求与 OT.binding.dbName 一致；同步成功后用户可通过 01 §2.7 的"切换绑定"功能把 OT.binding 改指向此 Doris ds）
- **本体绑定**：domain + 表清单（= OT 清单）

### 6.2 批量全量（首次/重算）
- 复用 TIS 现有 DataX 任务定义
- 新增一个 `OntologyBackfillJob`：从 Neo4j 读取所选 ObjectType 子图，按同步任务配置的目标 Doris ds + hints 生成 DataX job JSON 批量提交（**不读 OT.binding**，binding 此时通常还是 MySQL）
- 写入前先跑 DDL 生成器 `CREATE TABLE IF NOT EXISTS`
- **执行成功后回调**：UI 提示"已检测到 N 个 OT 数据落入 Doris X，是否切换查询绑定？"，调用 01 §2.7 `OntologyBindingSwitcher` 一键切换；用户选"否"则保持 binding 不变（适合演练 / 灰度场景）

### 6.3 增量 CDC
- 复用 Flink-CDC MySQL Source + Doris Sink
- `OntologyCDCJob`：同样由 Neo4j 中的本体子图驱动生成 Flink Job，按 domain 粒度启动一个 Flink 应用
- 幂等保证：Doris 侧使用 Unique Key 模型的 upsert 语义

### 6.4 Schema 变更
- MySQL 侧 `ALTER TABLE` 不会自动传导到本体，需要用户重新跑 `ExportToOntologyInDataSource`
- 本体变更（新增列/删除列）→ Neo4j 中的对应节点/关系实时更新 → `DorisDDLGenerator.generateDelta` 输出 ALTER SQL → 用户手动确认后执行（避免破坏性变更自动触发）

## 7. 落盘与观测

- 每次 backfill/CDC 启动记录到 TIS 现有任务日志
- 新增 metrics：`ontology.sync.rows_written`、`ontology.sync.lag_seconds`，接入 TIS Prometheus endpoint

## 8. 任务拆解

| 任务 | 估时 | 依赖 |
|------|------|------|
| T1 `MySqlToDorisTypeMapper` | 0.5d | - |
| T2 `DorisDDLGenerator` 全量 + 增量（基于 OT.binding + DorisPhysicalHints） | 2d | 01 / T1 |
| T3 `OntologyBackfillJob` | 1d | T2 |
| T4 `OntologyCDCJob` 模板 | 2d | T2 |
| T5 前端入口（同步任务向导：选源 MySQL + 目标 Doris + 物理 hints） | 1d | T3/T4 |
| T6 Falcon 17 库迁移脚本（SQLite → MySQL → Doris） | 1d | T3，见 05 文档 |

## 9. 验收标准

- 对一个典型 domain（5~10 张表）执行 backfill，Doris 中能看到全部表结构与数据
- MySQL 源端插入/更新/删除 1 条，Doris 侧 1min 内可见（Unique Key 模型）
- Doris 自动建表 DDL 通过 `EXPLAIN CREATE TABLE` 检查无语法错误