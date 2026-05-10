# 03 - GraphRAG 检索层

> **前置依赖**：06（本体已实时同步到 Neo4j，包含节点 embedding 与 HNSW 向量索引）、02（Doris 已有真实数据，便于补充值分布）。
> **后续被依赖**：04（NL→SQL prompt 由本层生成）。

## 1. 目标

| 编号 | 目标 | 说明 |
|------|------|-----|
| R1 | 给定 NLQ，能召回最相关的本体子图（OT/Property/Linker/Glossary） | top-K 入口 + N 跳邻居 |
| R2 | 子图序列化为 prompt 片段，控制 token 预算（默认 ≤ 3000 tokens） | 多档剪枝策略 |
| R3 | 中文 NLQ 友好（业务别名、同义词命中） | 借助 Glossary + alias 字段 |
| R4 | 单次检索 P95 ≤ 200ms（不含 LLM） | 全程在 Neo4j 内一次查询完成 |

## 2. 总体架构

```
NLQ ──► [Step1 入口实体识别] ──► [Step2 子图扩展] ──► [Step3 子图剪枝/排序]
        ├── Cypher 文本精确匹配  ├── 沿 LINKED_TO 1~2 跳 ├── token 预算控制
        ├── HNSW 向量召回(并行)  ├── 反查 Glossary 关联  ├── 与 NLQ 相关度排序
        │   - ObjectType         │   (TARGETS_OT/PROP)   └── 输出 prompt 上下文
        │   - Property           └── 引入 SharedProperty
        │   - SharedProperty
        │   - Glossary
        └── 关键词回退
                │
                ▼
        Neo4j 嵌入式实例（与本体同库，06 已同步好）
```

## 3. 索引层（全部由 06 的同步流程产出）

### 3.1 索引来源
**Neo4j 即唯一查询源**。06 中 `OntologyNeo4jSyncService` 在本体保存后实时同步节点+embedding+关系到 Neo4j；GraphRAG 检索阶段**只读 Neo4j**，不再触碰 XML，也不再有 `snapshot.json`。

### 3.2 索引内容（Neo4j 已建好）

| 索引名 | 类型 | 命中节点 Label | 用途 |
|------|------|---------|------|
| `objecttype_embedding_idx` | HNSW 向量索引（cosine） | `:ObjectType` | OT 召回 |
| `property_embedding_idx`   | HNSW 向量索引（cosine） | `:Property`   | Property 召回 |
| `sharedprop_embedding_idx` | HNSW 向量索引（cosine） | `:SharedProperty` | 共享列召回 |
| `glossary_embedding_idx`   | HNSW 向量索引（cosine） | `:Glossary`   | 业务术语召回 |
| `(:Glossary).synonyms`     | 自然 Cypher 字符串数组 | `:Glossary`   | 词典精确匹配（无须额外索引，规模小直接 MATCH） |
| `(:ObjectType)-[:LINKED_TO]->(:ObjectType)` | 图关系（属性边） | - | 邻居展开（沿边 BFS） |
| `(:Property)` 的 ValueType 约束（Enum 等） | 节点属性 `constraintParams` | `:ValueType`  | 值匹配（"活跃" → status='A'，可选） |

### 3.3 向量化方案

- **Embedding 服务**：复用 `/opt/misc/neo4j-demo` 中已验证的 `OnnxEmbeddingService`（384 维多语言 MiniLM，中英双语）；后续可平替为 `bge-m3` 等更强模型，仅替换 ONNX 模型文件即可
- **写入时机**：本体节点 MERGE 时由 06 的 `OntologyNeo4jSyncService` 同步写入，**无独立索引重建步骤**
- **类型要求**：Neo4j HNSW 索引仅识别 `List<Float>`（demo 已踩过坑），写入路径必须使用 `Float` 而非 `Double`
- **维度与相似度**：固定 384 维 + cosine（与 demo 一致，避免重新训练）

### 3.4 索引存储位置

- 嵌入式 Neo4j 数据目录：`<TIS.dataDir>/neo4j-data/`
- ONNX 模型文件：`<tis-classpath>/models/model.onnx` + `vocab.txt`（沿用 demo 路径约定）
- 无独立向量库依赖，无外部存储

## 4. 在线检索流程

### Step 1 - 入口实体识别

输入：NLQ（如"近七天每个城市的活跃用户数"）

并行多路召回（**全部一次性在 Neo4j 内完成**）：

#### 1.1 词典精确匹配（Glossary 同义词）
```cypher
MATCH (g:Glossary {domain: $domain})
WHERE g.term IN $tokens OR ANY(s IN g.synonyms WHERE s IN $tokens)
RETURN g, 1.0 AS score
```
其中 `$tokens` 为对 NLQ 做中文分词 + n-gram 后的词条数组（用 HanLP 等本地分词器即可）。

#### 1.2 向量召回（4 路并发）
对 NLQ 整句 embedding 后，并行查 4 个 HNSW 索引：
```cypher
CALL db.index.vector.queryNodes('objecttype_embedding_idx', $topK, $queryVec)
YIELD node, score WHERE node.domain = $domain
RETURN node, score
```
（`property_embedding_idx` / `sharedprop_embedding_idx` / `glossary_embedding_idx` 同样套路，默认 `$topK = 5`）

#### 1.3 关键词回退
```cypher
MATCH (n)
WHERE (n:ObjectType OR n:Property OR n:SharedProperty)
  AND n.domain = $domain
  AND (toLower(n.name) CONTAINS toLower($keyword)
       OR toLower(coalesce(n.alias,'')) CONTAINS toLower($keyword))
RETURN n, 0.3 AS score LIMIT 20
```

合并去重得入口节点集合 `seeds`，按 score 加权排序。

### Step 2 - 子图扩展

从 `seeds` 沿 `LINKED_TO` 关系做 BFS，限定 1~2 跳，并附带 OT 的 Property 列表与 Glossary 反查：

```cypher
// 单条 Cypher 完成"种子 → 邻居 OT → 各自 Property → 关联 Glossary"
MATCH (seed)
WHERE id(seed) IN $seedIds
OPTIONAL MATCH path = (seed)-[:LINKED_TO*1..2]-(neighbor:ObjectType)
WITH collect(DISTINCT seed) + collect(DISTINCT neighbor) AS allOts
UNWIND allOts AS ot
OPTIONAL MATCH (ot)-[hp:HAS_PROPERTY]->(p:Property)
OPTIONAL MATCH (p)-[:USES_VALUE_TYPE]->(vt:ValueType)
OPTIONAL MATCH (g:Glossary)-[:TARGETS_OT|TARGETS_PROP]->(ot)
RETURN ot,
       collect(DISTINCT {prop: p, ordinal: hp.ordinal, valueType: vt}) AS properties,
       collect(DISTINCT g) AS glossaries
```

同时单独取出涉及的 `LINKED_TO` 关系的属性（`linkerName / cardinality / sourceField / targetField / viaObjectType`），用于 SQL 生成时的 JOIN 推导：

```cypher
MATCH (a:ObjectType)-[r:LINKED_TO]->(b:ObjectType)
WHERE a.name IN $otNames AND b.name IN $otNames
RETURN a.name, b.name, properties(r) AS linkInfo
```

### Step 3 - 子图剪枝与序列化

- **剪枝**：按 `score = 入口相关度 + 1/(路径跳数+1) - token 成本估算` 计算每节点去留
- **预算控制**：估算 token 数（按 4 chars ≈ 1 token），超预算则按 score 升序丢弃 OT；OT 内 Property 优先保留 `pk` / `Measure` / `TimeDimension` 角色的列
- **序列化**：输出 Markdown 风格的 prompt context（模板与下方一致）

#### 输出 prompt 模板

```markdown
### 相关业务术语
- 活跃用户 (DAU): count(distinct user_id) filter (where status='A')

### 相关数据表
#### users (用户)
描述: 平台注册用户主表
- id        BIGINT     [PK, Identifier] 用户ID
- name      VARCHAR    [Dimension]      用户名
- status    CHAR(1)    [Dimension]      状态; 取值={A:活跃, I:不活跃, D:已注销}
- city      VARCHAR    [Dimension]      城市
- created_at DATETIME  [TimeDimension]  注册时间

#### orders (订单)
描述: 用户在商城的订单主表
- order_id  STRING     [PK, Identifier]
- user_id   BIGINT     [Identifier]    关联 users.id
- amount    DECIMAL    [Measure: SUM]  成交额
- created_at DATETIME  [TimeDimension]

### 表间关系
- orders.user_id ─(N:1)→ users.id   [linker=orders_join_users]

### Doris 物理映射
- users  → doris_prod.ods_users
- orders → doris_prod.ods_orders
```

## 5. API 设计

```java
public interface GraphRAGService {
    RetrievalResult retrieve(String domain, String nlq, RetrievalOptions opts);
}

public record RetrievalOptions(
    int topKSeeds,        // 默认 5（每个向量索引各取 topK）
    int maxHops,          // 默认 2
    int tokenBudget,      // 默认 3000
    boolean includeValueExamples  // 是否带 enum 取值
) {}

public record RetrievalResult(
    String promptContext,         // 直接喂给 LLM 的拼好上下文
    List<String> objectTypes,     // 命中的 OT 名（用于 04 阶段 SQL 校验白名单）
    List<LinkerInfo> linkers,     // 命中的 LINKED_TO 关系（用于校验 JOIN）
    List<String> glossaryTerms    // 命中的术语
) {}

public record LinkerInfo(
    String linkerName, String relType, String cardinality,
    String source, String sourceField, String target, String targetField
) {}
```

实现位置：承载在 `tis-ontology-plugin` 插件工程下（位于 `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/`），依赖 `tis-plugin` 与共享的 Neo4j 工具类（`EmbeddedNeo4jManager` / `OnnxEmbeddingService`）。后续 tis-solr 中有关 ontology 的大部分脚本也会迁移到该插件工程中。

## 6. 一致性

- 本体保存 → Neo4j 同步是 06 的职责；GraphRAG **只消费**不写入
- 若 Neo4j 中查不到某 domain（首次启动场景），降级为提示用户"该 domain 尚未同步，请稍候"，不阻塞主流程

## 7. 任务拆解

| 任务 | 估时 | 依赖 |
|------|------|------|
| T1 复用 demo 中 `EmbeddedNeo4jManager` / `OnnxEmbeddingService` 抽到共享模块 | 0.5d | 06/N1 |
| T2 `GraphRAGService.retrieve` 主流程（含 4 路并行召回 + Cypher 子图扩展） | 3d | T1 |
| T3 子图剪枝 / token 预算控制 / Markdown 序列化 | 1.5d | T2 |
| T4 中文分词器接入（HanLP / Jieba 任选） | 0.5d | - |
| T5 单元/集成测试（手工 10 条 NLQ 验证 top-K 命中率） | 1d | T1-T4 |

## 8. 验收标准

- 对一组手工 NLQ（10 条），检查 top-3 入口实体命中率 ≥ 90%
- 检索结果 prompt 长度稳定在 1500~3000 tokens
- 单次检索（含 embedding 计算 + Cypher 查询）P95 ≤ 200ms

## 9. 风险与备选

| 风险 | 备选 |
|------|------|
| 中文 embedding 效果不稳定（MiniLM 只是基础线） | 沿 OnnxEmbeddingService 接口替换为 `bge-m3` 等更强中文模型；多召回路径并联降低单路风险 |
| 大模型 prompt 上下文超限 | 提供"简表模式"：仅给 OT 名 + 列名，不带描述，token 预算可压到 800 |
| Glossary 维护成本高 | 一期允许只填术语 + 同义词，target 留空，先做 reranker；二期再补 SQL 模板 |
| Neo4j 嵌入式单实例无 HA | 灰度阶段够用；规模化后切外部 Neo4j 集群（驱动配置切换即可，业务代码无感） |
| HNSW 写入慢于读 | 同步写在 06 的异步队列内进行，不阻塞本体保存 |