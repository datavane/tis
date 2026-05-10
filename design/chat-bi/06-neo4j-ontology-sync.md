# 06 - 本体同步到 Neo4j 图数据库

> **前置依赖**：01 文档（本体层扩展），尤其是 §2.5（Linker 信息增强：`ObjectLinkInfo` 四元组）、§2.2（Property 的 `SemanticRole`）、§2.3（`OntologyGlossary`）。
> **后续依赖**：03（GraphRAG 检索使用 Neo4j 作为唯一查询源）。

## 1. 背景与方案选型

- 本体当前以 XML 持久化，每次 GraphRAG 检索都解析 XML 太重。
- 已在 `/opt/misc/neo4j-demo` 验证 Neo4j 5.18 嵌入式 + ONNX Runtime 384 维向量 + HNSW 余弦相似度索引可行（参见 `/opt/misc/neo4j-demo/src/main/java/com/qlangtech/tis/neo4j/EmbeddedNeo4jDemo.java`）。
- **决策**：本体的所有元信息**实时同步一份副本到 Neo4j**，作为 GraphRAG 阶段的唯一查询源；不再生成 `snapshot.json`。

Neo4j 同时承担三个角色，省掉了 pgvector / 独立向量库的依赖：
1. 图存储（节点 + 关系，原生支持子图遍历）
2. 向量索引（HNSW，单库内即可做 ANN 召回）
3. Cypher 查询接口（一条语句完成"召回入口节点 + N 跳邻居展开"）

## 2. Neo4j 图模型

**节点 Label 与属性**：

| Label | 来源类 | 关键属性 | 是否带 embedding |
|---|---|---|---|
| `:OntologyDomain` | `OntologyDomain` | `name`(unique), `defaultDomain`, `updateTime` | 否 |
| `:ObjectType` | `OntologyObjectType` | `name`(unique within domain), `alias`, `description`, `dorisDs`, `physicalTable` | ✅ |
| `:Property` | `OntologyProperty` | `name`, `type`(OntologyType.literia), `endType`, `pk`, `nullable`, `role`, `agg`(可空), `alias`, `description` | ✅ |
| `:SharedProperty` | `OntologySharedProperty` | `name`(unique within domain), `alias`, `description`, `type` | ✅ |
| `:ValueType` | `OntologyValueType` | `name`(unique within domain), `description`, `ontologyType`, `constraintKind`, `constraintParams`(JSON 字符串) | 否 |
| `:Glossary` | `OntologyGlossary` | `term`(unique within domain), `synonyms`(string array), `description`, `targetKind`, `targetRef`, `metricSql`(可空) | ✅ |

> **Property 是否独立成节点**：方案选择把 Property 建为独立节点（而非内嵌在 ObjectType 上），原因：
> - 每个 Property 都需要单独 embedding，方便按列名/语义召回
> - GraphRAG 检索时可以"列级"精准命中，再回溯到 OT
> - Cypher 写起来更自然

**关系（Relationship Type）**：

| Cypher 关系 | 含义 | 携带属性 |
|---|---|---|
| `(:OntologyDomain)-[:CONTAINS]->(:ObjectType\|SharedProperty\|ValueType\|Glossary)` | Domain 持有四类实体 | - |
| `(:ObjectType)-[:HAS_PROPERTY]->(:Property)` | OT 持有 Property | `ordinal`（列序） |
| `(:Property)-[:USES_VALUE_TYPE]->(:ValueType)` | Property 使用 ValueType（DefaultPropertyTypeRef.valueType 命中时建） | - |
| `(:Property)-[:REFS_SHARED_PROPERTY]->(:SharedProperty)` | typeRef 为 SharedPropertyTypeRef 时建 | - |
| `(:ObjectType)-[:LINKED_TO]->(:ObjectType)` | **Linker 实体直接折叠为关系**（核心设计） | `linkerName`, `relType`, `cardinality`, `sourceField`, `targetField`, `viaObjectType`(可空，仅 Backing/JoinTable 用) |
| `(:Glossary)-[:TARGETS_OT]->(:ObjectType)` | Glossary 指向 OT | - |
| `(:Glossary)-[:TARGETS_PROP]->(:Property)` | Glossary 指向 Property | - |

> **Linker 折叠为关系而非节点**：
> - 优点：BFS 跳数减半（OT→OT 直达），Cypher 简短，与 GraphRAG 的"沿边扩展"语义直接对齐
> - 对 JoinTable / Backing 这种实际有中间 OT 的关系：在 `LINKED_TO` 上加 `viaObjectType` 属性即可，且 `getLinks()` 输出的两段 ObjectLinkInfo 各自落成一条 `LINKED_TO` 关系
> - 对 ObjectTypeForeignKeys：单条 `LINKED_TO` 关系搞定

## 3. Embedding 字段构造

每个带 embedding 的节点都用同一个 `OnnxEmbeddingService`（沿用 demo 的 384 维多语言 MiniLM）生成向量。每个节点的 embedding 文本拼装如下：

| 节点 | embedding 文本拼装 |
|---|---|
| `:ObjectType` | `name + " " + alias + " " + description + " " + 前若干列名` |
| `:Property` | `ot.name + "." + name + " " + alias + " " + description + " " + role` |
| `:SharedProperty` | `name + " " + alias + " " + description` |
| `:Glossary` | `term + " " + synonyms.join(" ") + " " + description` |

ValueType 不做向量化（用户极少按值类型 NLQ 召回，且 ValueType 通过 Property 间接命中即可）。

## 4. HNSW 向量索引

每类带 embedding 的 Label 建独立索引（沿用 demo 中 `db.index.vector.queryNodes()` 的用法）：

```cypher
CREATE VECTOR INDEX objecttype_embedding_idx IF NOT EXISTS
FOR (n:ObjectType) ON (n.embedding)
OPTIONS { indexConfig: { `vector.dimensions`: 384, `vector.similarity_function`: 'cosine' } };

CREATE VECTOR INDEX property_embedding_idx     IF NOT EXISTS FOR (n:Property)       ON (n.embedding) OPTIONS {...};
CREATE VECTOR INDEX sharedprop_embedding_idx   IF NOT EXISTS FOR (n:SharedProperty) ON (n.embedding) OPTIONS {...};
CREATE VECTOR INDEX glossary_embedding_idx     IF NOT EXISTS FOR (n:Glossary)       ON (n.embedding) OPTIONS {...};
```

## 5. 同步触发与一致性

新增 `OntologyNeo4jSyncService`，替换原计划的 `OntologySnapshotService`：

| 触发点 | 行为 |
|---|---|
| `IPluginStore.afterSaved`（每次本体保存） | 异步 enqueue 一条同步任务到队列，处理对应实体的增量 MERGE |
| Domain 首次创建 / 显式重建 | 全量 dump：清空该 domain 子图后重新生成 |
| 周期性巡检（每 N 分钟） | 对照 XML 持久化扫描，剔除 Neo4j 中存在但 XML 已删除的孤立节点 |

**幂等同步语句模板**（以 ObjectType 为例）：

```cypher
MERGE (d:OntologyDomain {name: $domain})
MERGE (ot:ObjectType {domain: $domain, name: $name})
SET   ot.alias = $alias,
      ot.description = $description,
      ot.dorisDs = $dorisDs,
      ot.physicalTable = $physicalTable,
      ot.embedding = $embedding,
      ot.updatedAt = timestamp()
MERGE (d)-[:CONTAINS]->(ot)
```

Property 同步时除 MERGE 自身外，同步重建 `HAS_PROPERTY / USES_VALUE_TYPE / REFS_SHARED_PROPERTY` 关系；Linker 同步时按 `getLinks()` 输出的 ObjectLinkInfo 列表重建 `LINKED_TO` 关系（先 DELETE 旧关系再 MERGE 新关系，保证一致）。

**单实体唯一性约束**（启动时执行一次）：

```cypher
CREATE CONSTRAINT domain_name_unique     IF NOT EXISTS FOR (d:OntologyDomain)  REQUIRE d.name IS UNIQUE;
CREATE CONSTRAINT ot_unique              IF NOT EXISTS FOR (n:ObjectType)      REQUIRE (n.domain, n.name) IS UNIQUE;
CREATE CONSTRAINT shared_prop_unique     IF NOT EXISTS FOR (n:SharedProperty)  REQUIRE (n.domain, n.name) IS UNIQUE;
CREATE CONSTRAINT value_type_unique      IF NOT EXISTS FOR (n:ValueType)       REQUIRE (n.domain, n.name) IS UNIQUE;
CREATE CONSTRAINT glossary_unique        IF NOT EXISTS FOR (n:Glossary)        REQUIRE (n.domain, n.term) IS UNIQUE;
```

> Property 因为本身依附于 OT，唯一性由 `(:ObjectType)-[:HAS_PROPERTY]->(:Property)` 这条边保证 + `Property` 节点上 `(domain, otName, name)` 三元组复合主键。

## 6. 部署形态

- **嵌入式 Neo4j**：参考 demo 的 `EmbeddedNeo4jManager`，在 TIS 同一 JVM 内启动；数据落 `<TIS.dataDir>/neo4j-data/`，无外部进程
- 优点：零运维、与 TIS 生命周期同步、支持事务
- 缺点：单实例无 HA；本期 ChatBI 灰度阶段够用，规模化后可升级为外部 Neo4j 集群（驱动接口完全兼容，仅切换连接配置）

## 7. 文件清单

| 文件 | 改动 |
|------|------|
| `tis-plugin/.../ontology/sync/OntologyNeo4jSyncService.java` | 新增（替代原 `OntologySnapshotService`） |
| `tis-plugin/.../ontology/sync/OntologyGraphMapper.java` | 新增：本体实体 → Cypher 参数映射 |
| `tis-plugin/.../ontology/sync/EmbeddingTextBuilder.java` | 新增：节点 embedding 文本拼装策略 |
| `tis-plugin/.../ontology/sync/Neo4jBootstrapper.java` | 新增：启动时建立约束 + 向量索引 |
| 复用 `/opt/misc/neo4j-demo` 中的 `OnnxEmbeddingService` / `EmbeddedNeo4jManager` | 抽取为共享模块或拷贝到 tis 项目下 |

## 8. 与 03 GraphRAG 的接口

03 文档的 `GraphRAGService.retrieve()` 不再读 `snapshot.json`，改为通过 Cypher 查询 Neo4j：
- 入口召回：在四个向量索引上并行 `db.index.vector.queryNodes()`
- 子图扩展：从入口节点沿 `LINKED_TO` 关系跳 1~2 跳
- 命中术语：从入口或邻居 OT 反查 `(:Glossary)-[:TARGETS_OT|TARGETS_PROP]->(...)` 

详见 03 文档 §3。

## 9. 数据契约（Neo4j 图模型示例）

下方展示一个 retail domain 的核心子图（节点 + 关系），实际持久化是 Cypher 写入 Neo4j。等价的 Cypher 表达：

```cypher
// 节点
MERGE (d:OntologyDomain {name: 'retail', defaultDomain: true});

MERGE (ot:ObjectType {domain: 'retail', name: 'orders'})
  SET ot.alias = '订单',
      ot.description = '用户在商城的订单主表',
      ot.dorisDs = 'doris_prod',
      ot.physicalTable = 'ods_orders',
      ot.embedding = $orders_embedding;

MERGE (p1:Property {domain: 'retail', otName: 'orders', name: 'order_id'})
  SET p1.type = 'STRING', p1.endType = 'DataTypeString',
      p1.pk = true, p1.nullable = false,
      p1.role = 'Identifier', p1.alias = '订单号',
      p1.description = '全局唯一', p1.embedding = $p1_embedding;

MERGE (p2:Property {domain: 'retail', otName: 'orders', name: 'amount'})
  SET p2.type = 'DECIMAL', p2.endType = 'DataTypeDecimal',
      p2.role = 'Measure', p2.agg = 'SUM',
      p2.alias = '成交额', p2.embedding = $p2_embedding;

MERGE (p3:Property {domain: 'retail', otName: 'orders', name: 'user_id'})
  SET p3.type = 'BIGINT', p3.role = 'Identifier',
      p3.embedding = $p3_embedding;

MERGE (g:Glossary {domain: 'retail', term: '活跃用户'})
  SET g.synonyms = ['DAU', 'active user'],
      g.targetKind = 'MetricExpr',
      g.metricSql = "count(distinct user_id) filter (where status='A')",
      g.description = '近 30 天有过登录或下单的用户',
      g.embedding = $g_embedding;

// 关系
MERGE (d)-[:CONTAINS]->(ot);
MERGE (ot)-[:HAS_PROPERTY {ordinal: 0}]->(p1);
MERGE (ot)-[:HAS_PROPERTY {ordinal: 1}]->(p2);
MERGE (ot)-[:HAS_PROPERTY {ordinal: 2}]->(p3);

// Linker 折叠为关系（orders.user_id → users.id）
MATCH (orders:ObjectType {domain:'retail', name:'orders'}),
      (users :ObjectType {domain:'retail', name:'users'})
MERGE (orders)-[r:LINKED_TO {linkerName: 'orders_join_users'}]->(users)
  SET r.relType = 'ObjectTypeForeignKeys',
      r.cardinality = 'ONE_MANY',
      r.sourceField = 'user_id',
      r.targetField = 'id';
```

> 注意：约束（如 `Enum4String` 的可选枚举值）通过独立的 `:ValueType` 节点存储，并以 `(:Property)-[:USES_VALUE_TYPE]->(:ValueType)` 边连接；具体写法此处略。

## 10. 任务拆解

| 任务 | 估时 | 依赖 |
|------|------|------|
| N1 嵌入式 Neo4j 启动 + 约束 + 向量索引创建 + ONNX embedding 接入 | 1.5d | 01 中 T1-T6 |
| N2 `OntologyNeo4jSyncService` 增量 MERGE / 删除 / 全量重建 | 1.5d | N1 |
| N3 `OntologyGraphMapper` + `EmbeddingTextBuilder` | 0.5d | N1 |
| N4 `IPluginStore.afterSaved` 钩子接入 + 异步队列 | 0.5d | N2 |

## 11. 验收标准

- 本体保存后 2 秒内，Neo4j 中对应子图同步完成（含 embedding），可用 Cypher `MATCH (n:ObjectType {domain:$d}) RETURN n` 验证
- 对任意 ObjectType 触发向量检索 `CALL db.index.vector.queryNodes('objecttype_embedding_idx', 5, $queryVec)` 能返回合理的 top-5
- §01-2.7 切换绑定执行后，对应 `:ObjectType {dorisDs}` 节点属性同步更新
- 删除一个本体实体后，巡检任务能在下个周期内剔除 Neo4j 中对应孤立节点