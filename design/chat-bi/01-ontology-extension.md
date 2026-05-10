# 01 - 面向 ChatBI 的本体层扩展

> **前置依赖**：当前本体层架构（详见仓库 memory `project_ontology_enum_constraint.md`）。
> **后续依赖**：02（同步任务复用本体 binding 指向的 Doris 数据源）、03（GraphRAG 需要 Glossary 与 Measure/Dimension）。

## 1. 现状盘点（本体层已具备的能力）

| 能力 | 类 | ChatBI 用途 |
|------|-----|------------|
| 对象类型 | `OntologyObjectType` + 两步表单 | 图节点 |
| 属性 | `OntologyProperty` + `OntologyPropertyTypeRef` | 节点属性 |
| 共享属性 | `OntologySharedProperty` | 跨表共用列定义 |
| 关系 | `OntologyLinker` + 3 种 `RelationshipType` | 图边、JOIN 路径 |
| 值类型 | `OntologyValueType` + `ValueConstraint`（Enum/Range/Regex/UUID/RID） | 值域语义 |
| 物理绑定 | `ObjectTypeBinding`（`NoneBinding` / `DataSourceBinding`） | 已支持 MySQL 源绑定 |

## 2. 本阶段需要扩展的能力

### 2.1 [必做] 绑定语义收敛到"查询目标"

**现状与误判的澄清**：
- `DataSourceBinding.dbName` 本身就是对 `DataSourceFactory` 的泛化引用，Doris 在 TIS 中就是一种 DataSource 插件，直接填 Doris 类型的数据源名即可，**无需**另建 `DorisBinding`。
- 一个 ObjectType 只应绑一处物理存储，"同时绑源 MySQL + 目标 Doris"没有运行时意义 —— ChatBI 只对 Doris 发起查询，MySQL 只在同步链路（02 文档）中作为上游出现，不需要沉淀到本体。

**规范（约定层面）**：
> 本体中的 `ObjectTypeProfile.binding` 指向**查询目标数据源**（ChatBI 场景下即 Doris）。MySQL 等源端数据源不在本体中登记，仅作为同步任务（02）中"源端 → 目标端"映射的源侧配置。

**实际工作流是分两步走**（这是真实的业务流程，不是兼容补丁）：

```
Step 1: 用户从 MySQL 导入元数据建模
  ↓ (ExportToOntologyInDataSource，binding.dbName 暂指向 MySQL)
Step 2: 同步任务把数据落到 Doris
  ↓ (02 文档的 OntologyBackfillJob / OntologyCDCJob)
Step 3: 切换 binding.dbName → Doris
  ↓ (本文档 §2.7 新增的"切换绑定"功能)
Step 4: ChatBI 开始基于 Doris 查询
```

之所以不一步到位指向 Doris：建模时 Doris 表往往**还不存在**（DDL 由 02 同步任务依据本体生成），强制必填会形成"鸡生蛋蛋生鸡"。

**对现有代码的影响**：
- `DataSourceBinding` 类本身**无需修改**，`dbName` 字段语义直接复用。
- `ExportToOntologyInDataSource` 当前实现把 `fromSource.identityValue()` （MySQL 源）写到 `dsBinding.dbName`，**保持现状**：建模时 binding 指向 MySQL；切换时机由 §2.7 的"切换绑定"功能承担。
- 02 同步任务创建时不再回写 binding；切换由用户显式触发或在同步成功后由系统提示并一键切换。

**可选的小增强（按需）**：
- 若存在"ObjectType 逻辑名 ≠ Doris 物理表名"的场景（例如本体侧用中文语义名），可在 `DataSourceBinding` 上加一个可空的 `physicalTableName` 字段，覆盖 OT 默认同名约定。
- Doris 的分区列 / 分桶列**不放到 binding**。它们是"物理建表参数"，应在同步任务配置（02 文档）或根据 Property 的 `SemanticRole`（如 `TimeDimension` → 默认分区列）自动推导，避免污染本体语义层。

**文件清单**：
| 文件 | 改动 |
|------|------|
| `DataSourceBinding.java` | 不改（可选补 `physicalTableName` 字段） |
| `ExportToOntologyInDataSource.java` | **保持现有 binding=MySQL 行为**（建模阶段需要 MySQL 上下文） |
| 见 §2.7 | 新增独立的"切换绑定到 Doris"功能 |

### 2.2 [必做] 属性的 Measure / Dimension 角色

**问题**：当前 `OntologyProperty` 只有 `pk / nullable / typeRef`，没法告诉 LLM "GMV 列是事实可聚合，类目列是维度"。

**设计**：
- 在 `OntologyProperty` 上新增 `SemanticRole role` 字段（可空，默认 Unknown），类型为新建的枚举：
  - `Dimension`（维度）
  - `Measure`（度量，可聚合）
  - `TimeDimension`（时间维，区分一般维度，便于 prompt 强调时间窗口）
  - `Identifier`（标识列，PK/FK 的语义增强）
  - `Unknown`
- 度量列额外携带"默认聚合方式"（`SUM/AVG/COUNT/MIN/MAX/COUNT_DISTINCT`），抽象为 `MeasureSpec` 子结构
- 由 `ExportToOntologyInDataSource` 导入时按启发式规则给默认值（数值型 + 非 PK → Measure 候选；DATE/DATETIME → TimeDimension），用户可手工覆盖

**文件清单**：
| 文件 | 改动 |
|------|------|
| `OntologyProperty.java` | 新增 `role`、`measureSpec` 字段 |
| `SemanticRole.java` | 新增枚举 |
| `MeasureSpec.java` | 新增类（agg 默认值 + 单位/精度） |
| `ExportToOntologyInDataSource.java` | 启发式给默认 role |

### 2.3 [必做] 业务术语 / 同义词字典 `OntologyGlossary`

**问题**：用户问"近七天活跃用户" → "活跃用户" 在 Schema 里既不是表也不是列，需要本体外的业务字典做桥接。

**设计**：
- 新增 `OntologyGlossary extends Ontology`，作为第 5 类本体实体加入 `OntologyEnum`（`typeIdentity = "ontology-glossary"`）
- 字段：
  - `String term`：标准术语（identity）
  - `List<String> synonyms`：同义词
  - `GlossaryTarget target`：指向何处（OT/Property/Metric）
    - `OTRef`：指向 ObjectType 名
    - `PropertyRef`：指向 OT.Property
    - `MetricExpr`：自定义 SQL 表达式（如 `count(distinct user_id) where active_status='A'`）
  - `String description`：自然语言描述，供 embedding
- 借用现有 `BaiscAssistStoreGetter` 落到 `<domain>/ontology-glossary/<term>.xml`

**文件清单**：
| 文件 | 改动 |
|------|------|
| `OntologyGlossary.java` | 新增 |
| `Ontology.java` | `OntologyEnum` 加 `Glossary` 项 |
| `OntologyDomain.java` | `getGlossaryDir()` 工具方法 |
| `impl/glossary/GlossaryTarget.java` + 三种实现 | 新增 |

### 2.4 [可选] 指标体 `OntologyMetric`

**问题**：业务指标（GMV、DAU）通常需要 SQL 模板 + 维度组合 + 时间粒度。

**设计**：
- 暂不在本阶段落地，作为 `OntologyGlossary.MetricExpr` 的简化版先支持（`MetricExpr` 持有 SQL 片段即可）
- 后续若需多维度交叉计算，再升级为独立的 `OntologyMetric` 多步插件
- **本文档不做实现，仅留扩展位**

### 2.5 [必做] Linker 信息增强

**现状**：
- `RelationshipTypeObjectTypeForeignKeys` 持有 `LinkReference left / right`，已经在表单层采集了两端的 FK 字段（`LinkReference.objectType` + `LinkReference.targetField`）
- `RelationshipTypeJoinTableDataset` 持有 `LinkReference left / right / join`，三方字段齐备
- `RelationshipTypeBackingObjectType.joinObjectType` 字段静态类型是 `LinkReference`，**运行时实际是 `JoinReference` 子类**，包含：
  - `objectType`：中间 OT 名（如 Flight Manifest）
  - `targetField`：中间 OT 上的第一条 FK 字段
  - `rightTargetField`：中间 OT 上的第二条 FK 字段
  - `validateAll` 强制 `targetField != rightTargetField`

也就是说，**三种 RelationshipType 在表单层都已经把 join 列采集齐全**，与 Palantir "Property 级 FK 一处定义、Link 内引用" 的模式语义等价。

**缺口 1：`ObjectLinkInfo` 没把字段透传**

`LinkResources.ObjectLinkInfo` record 当前只有 `(source, target)` 两个 OT 名，导致下游 GraphRAG / SQL 生成阶段拿不到 join 列。

**设计**：把 `ObjectLinkInfo` 扩展为：

```java
public record ObjectLinkInfo(
    String source, String sourceField,
    String target, String targetField,
    Cardinality cardinality   // ONE_ONE / ONE_MANY / MANY_MANY
) {
    public boolean contain(String objType) { ... }
}
```

三种 `RelationshipType` 的 `getLinks()` 改造为输出完整四元组：

```java
// RelationshipTypeObjectTypeForeignKeys
return List.of(new ObjectLinkInfo(
    left.getObjectType(),  left.targetField,
    right.getObjectType(), right.targetField,
    Cardinality.ONE_MANY));

// RelationshipTypeJoinTableDataset
return List.of(
    new ObjectLinkInfo(left.getObjectType(),  left.targetField,
                       join.getObjectType(),  join.targetField, Cardinality.ONE_MANY),
    new ObjectLinkInfo(join.getObjectType(),  join.targetField,
                       right.getObjectType(), right.targetField, Cardinality.ONE_MANY));

// RelationshipTypeBackingObjectType
JoinReference jr = (JoinReference) joinObjectType;
return List.of(
    new ObjectLinkInfo(leftObjectType,    inferPk(leftObjectType),
                       jr.getObjectType(), jr.targetField,        Cardinality.ONE_MANY),
    new ObjectLinkInfo(jr.getObjectType(), jr.rightTargetField,
                       rightObjectType,    inferPk(rightObjectType), Cardinality.ONE_MANY));
```

`inferPk(ot)`：从 OT 的 properties 里取 `pk == true` 的列名（沿用 Palantir "FK 默认指向另一端 PK" 的隐式约定）。`Cardinality` 由 `RelationshipType` 隐式确定（FK→1:N，JoinTable→M:N，Backing→M:N+attr）。

**缺口 2：`JoinReference` 的字段 ↔ 左右端是隐式约定**

`JoinReference` 中 `targetField` 对应 `leftObjectType`、`rightTargetField` 对应 `rightObjectType` 的对应关系当前**没有 UI 显式化**，仅靠 `validateAll` 防止填同一字段。用户填错可能让 §2.5 中 §"缺口 1" 的字段透传方向反掉。

**设计（最轻方案）**：保持结构不变，仅在 `JoinReference.JoinDesc` 的字段 `label` 上显式化语义：
- `targetField` 的 label：`"指向左端 OT 的 FK 字段"`
- `rightTargetField` 的 label：`"指向右端 OT 的 FK 字段"`

并在 `RelationshipTypeBackingObjectType` 的表单上显式提示"先选左/右端 OT，再在 Manifest 中分别选对应的 FK 字段"，避免用户主观乱填。

**文件清单**：

| 文件 | 改动 |
|------|------|
| `LinkResources.java`（`ObjectLinkInfo` record） | 扩展为含 sourceField/targetField/cardinality 的四元组 |
| `RelationshipTypeObjectTypeForeignKeys.java` | `getLinks()` 改用四元组 |
| `RelationshipTypeJoinTableDataset.java` | `getLinks()` 改用四元组 |
| `RelationshipTypeBackingObjectType.java` | `getLinks()` 改用四元组（含 inferPk + JoinReference 转型） |
| `Cardinality.java`（新枚举） | 新增 |
| `JoinReference.json`（资源） | `targetField` / `rightTargetField` 的 label 显式化 |

**与 Palantir 的对照**：

| Palantir | TIS（修订后） |
|---|---|
| Property Type 上声明 Foreign Key 元信息 | `LinkReference` / `JoinReference` 直接在 Linker 上声明 FK 字段（位置不同，效果等价） |
| Backing Link Type 自动从中间 OT 的 FK 属性推断 join 列 | `RelationshipTypeBackingObjectType.getLinks()` 从 `JoinReference.targetField` + `rightTargetField` 反推 |
| 一处定义、下游按名引用 | 同样按 Linker 名引用，不在 prompt/SQL 模板里重复定义 |

> **下一轮迭代方向**（不在本期落地）：可考虑仿 Palantir 把 FK 元信息下沉到 `OntologyProperty`（新增 `ForeignKeyRef` 字段），让 Linker 自动从 Property 元信息反推连接键，进一步贴近 Palantir 的"属性级一次定义"模式。届时 `JoinReference` 中的 `targetField` / `rightTargetField` 可以退化为可选项（用户不填则按属性级 FK 自动推断）。

### 2.6 [必做] 本体同步到 Neo4j 图数据库

> 详细设计已迁移至 [`06-neo4j-ontology-sync.md`](./06-neo4j-ontology-sync.md)。本节仅保留对 01 的影响摘要：
>
> - 本体保存事件 (`IPluginStore.afterSaved`) 触发增量同步到 Neo4j
> - §2.5 的 `ObjectLinkInfo` 四元组 → Neo4j `LINKED_TO` 关系
> - §2.2 的 `SemanticRole` / `MeasureSpec` → `:Property` 节点属性
> - §2.3 的 `OntologyGlossary` → `:Glossary` 节点 + 向量索引
> - §2.7 切换绑定后通过同一钩子刷新 `:ObjectType {dorisDs}` 属性


### 2.7 [必做] ObjectType 切换绑定数据源（MySQL → Doris）

**场景**：建模阶段 OT 的 `binding.dbName` 指向 MySQL（建模时元数据由 MySQL 提供，且 Doris 表往往尚未生成）；02 文档的同步任务把数据落到 Doris 后，需要把 binding 切换到 Doris 数据源，使 ChatBI 后续查询命中 Doris。

**触发方式**（两条入口并存）：

| 触发入口 | 行为 |
|---|---|
| **手工切换**：OT 详情页新增"切换数据源"按钮 | 弹出对话框选 Doris 类型的 DataSource，确认后写回 binding |
| **同步成功后建议**：02 同步任务执行成功，UI 顶部提示"已检测到 OT 数据已落入 Doris 数据源 X，是否切换查询绑定？" | 用户点击"是"即一键切换 |

两条入口都不**自动**切换，因为：
- 同一 OT 可能存在"双向同步演练 / 灰度阶段同时存在"，自动切换可能误伤
- 切换会立刻影响 ChatBI 查询路径，需要用户显式确认

**校验逻辑**（切换时执行）：

| 校验项 | 处理 |
|---|---|
| 目标数据源类型必须是 Doris（或其它支持 ChatBI 查询的 OLAP DS） | 通过 `DataSourceFactory.getDescriptor()` 判定其 endType |
| 目标 Doris 中存在与 OT 同名表（或 `physicalTableName` 指定的物理表） | 调用 `DataSourceFactory.getTablesInDB()` 检查 |
| 表的列集与 OT 的 `Property` 集名称对齐（数量与列名）| 列出差异列表，差异较大时阻止切换 |
| 列类型与 `OntologyType` 兼容（按 02 文档 §3 类型映射反向校验） | 不兼容则告警，但不阻止（让用户决断） |

**切换的副作用**：

- `binding.dbName` 修改后，OT 的 `manipuldateProcess()` 会触发 `IPluginStore.afterSaved` 钩子
- 06 文档的同步服务会自动捕获并刷新下游图存储（详见 06）
- 如果 02 文档的同步任务还在跑（增量 CDC 模式），切换不影响同步任务（同步任务读自己的源端/目标端配置，不依赖 OT.binding）

**接口草案**：

```java
public interface OntologyBindingSwitcher {
    /**
     * 校验是否可切换；返回校验报告（兼容差异列表）
     */
    BindingSwitchReport validate(OntologyObjectType ot, String newDsName);

    /**
     * 执行切换：把 binding.dbName 改为 newDsName，并触发持久化（图存储同步由下游钩子处理，详见 06）
     */
    void switchBinding(OntologyObjectType ot, String newDsName, IPluginContext ctx);
}

public record BindingSwitchReport(
    boolean ok,
    List<String> missingColumns,    // OT 有但 Doris 没有的列
    List<String> extraColumns,      // Doris 有但 OT 没有的列
    List<TypeMismatch> typeMismatches,
    String error                    // 阻断性错误，非空时不应执行 switchBinding
) {}
```

**前端入口**：

- OT 详情页 overview tab，binding 信息行右侧加"切换"按钮 → 弹窗（DataSource 下拉 + 校验报告）
- 02 同步任务执行成功页面，加"切换查询绑定到 X"快捷入口

**文件清单**：

| 文件 | 改动 |
|------|------|
| `tis-plugin/.../ontology/binding/OntologyBindingSwitcher.java` | 新增接口 + 默认实现 |
| `tis-plugin/.../ontology/binding/BindingSwitchReport.java` | 新增 record |
| `tis-plugin/.../ontology/binding/DefaultBindingSwitcher.java` | 校验 + 持久化逻辑 |
| `tis-console/...` | OT 详情页"切换"按钮 + 弹窗组件 |
| 02 同步任务结果页 | 新增"切换绑定"快捷入口（仅 UI） |

## 3. 数据契约

本文档的所有扩展（`SemanticRole` / `MeasureSpec` / `OntologyGlossary` / `ObjectLinkInfo` 四元组）仍以 XML 文件作为权威持久化层（沿用现有 `BaiscAssistStoreGetter` 与 Domain 目录约定）。下游图存储的数据契约示例（含 retail domain 子图）见 [`06-neo4j-ontology-sync.md`](./06-neo4j-ontology-sync.md) §9。

## 4. 影响面与回归

- **前端**：`tis-console` 需要为 `SemanticRole`、`OntologyGlossary`、§2.7 的"切换绑定"对话框新增表单页（见 `ontology.md` 中已有的 Detail 组件，按相同套路扩展）
- **DB Schema**：本体走文件持久化，无 DB schema 变更
- **现存本体兼容**：
  - 旧 OT 的 `DataSourceBinding.dbName` 指向 MySQL 是**预期状态**（不再视为遗留问题），可由 §2.7 的切换功能在合适时机迁移到 Doris
  - 旧 Property 缺 `role` 的，按"空值=未设置"宽松处理；下游同步任务遇空字段时跳过（详见 06）

## 5. 任务拆解

| 任务 | 估时 | 依赖 |
|------|------|------|
| T1 `ExportToOntologyInDataSource` 保持 binding=MySQL 行为，确认现状不回滚 | 0.1d | - |
| T2 `OntologyProperty` 加 `role` + `MeasureSpec` | 1d | - |
| T3 `ExportToOntologyInDataSource` 启发式默认 `role` | 0.5d | T2 |
| T4 `OntologyGlossary` + 三种 GlossaryTarget | 2d | - |
| T5 `Ontology.OntologyEnum` 注册 Glossary + 目录工具 | 0.5d | T4 |
| T6 `ObjectLinkInfo` 扩展四元组 + Cardinality | 1d | - |
| T7 `OntologyBindingSwitcher` 切换绑定功能（后端 + 校验报告） | 1d | - |
| T8 前端表单适配（含 §2.7 切换绑定对话框 + 02 同步任务结果页快捷入口） | 2.5d | T1-T6, T7 |

> 原 "Neo4j 同步服务接入" 任务已拆出至 [06 文档](./06-neo4j-ontology-sync.md) §10。

## 6. 验收标准

- 在一个新建的 domain 中，能完整建出含上述扩展字段的 ObjectType / Property / Linker / Glossary，并保存
- `ExportToOntologyInDataSource` 一键导入 MySQL 表后，自动生成的 ObjectType 含合理的默认 `role`
- §2.7 切换绑定功能：
  - 选中一个绑定 MySQL 的 OT，触发"切换"，目标 Doris 中存在同名同列表 → 切换成功，binding.dbName 已更新到 Doris
  - 目标 Doris 中表不存在 → 校验报告阻止切换，给出明确错误信息
  - 列差异 < 10% → 切换成功 + 警告；差异 ≥ 10% → 阻止切换

> 下游图存储侧的验收标准（embedding 同步、Cypher 检索）见 [06 文档](./06-neo4j-ontology-sync.md) §11。