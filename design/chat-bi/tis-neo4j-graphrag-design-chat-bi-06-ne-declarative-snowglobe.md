# 本体同步到 Neo4j 图数据库 —— 实现计划

## Context

TIS Ontology 层已完成开发（ObjectType / ValueType / Linker / SharedProperty / Glossary），实体持久化在 XML 中。  
GraphRAG 检索若每次解析 XML 则开销太重；已在 `/opt/misc/neo4j-demo` 验证嵌入式 Neo4j 5.18 + ONNX 384 维向量 + HNSW 可行。  
目标：将本体实时同步到嵌入式 Neo4j，作为 GraphRAG 的唯一查询源（替代原计划的 `snapshot.json`）。  
设计文档：`tis-solr/design/chat-bi/06-neo4j-ontology-sync.md`。

---

## 工程边界

- **抽象层（只读/接口）**：`tis-plugin/…/ontology/`（tis-solr 工程）  
  包含 `Ontology`、`OntologyObjectType`、`OntologyProperty`、`OntologyLinker`、`OntologySharedProperty`、`OntologyGlossary`、`OntologyDomain`。
- **实现层（新增代码放此处）**：`plugins/tis-ontology-plugin/`  
  已有全部具体实现类（`DefaultOntologyObjectType`、`RelationshipType*`、`GlossaryTarget*` 等）。

---

## 关键复用资源

| 资源 | 位置 | 说明 |
|---|---|---|
| `EmbeddedNeo4jManager` | `/opt/misc/neo4j-demo/…/EmbeddedNeo4jManager.java` | 嵌入式启动，`GraphDatabaseService` 获取入口 |
| `OnnxEmbeddingService` | `/opt/misc/neo4j-demo/…/semantic/OnnxEmbeddingService.java` | ONNX 384 维推理，`embed(text)→float[]` |
| `BertTokenizer` | 同上 | 分词配套 |
| `IPluginStore.AfterPluginSaved` | `tis-plugin/…/IPluginStore.java` | `afterSaved()` 钩子，已有多个实现参考（`DataxProcessor`、`IncrRateController`） |
| `Ontology.loadAllLinkers/loadAllGlossary/loadAllSharedProperties/loadObjectTypeDetail` | `Ontology.java` | 加载各类实体的静态方法 |
| `LinkResources.ObjectLinkInfo` / `ObjectLinkerPair` | `tis-plugin/…/impl/linker/LinkResources.java` | Linker 的 source/target/field/cardinality 四元组 |
| `DefaultOntologyProperty.getSemanticRole()` + `AggregationFunc` | `tis-ontology-plugin/…/impl/objtype/DefaultOntologyProperty.java` | Property 的 role/agg 字段 |
| `DataSourceBinding` | `tis-ontology-plugin/…/impl/objtype/DataSourceBinding.java` | dorisDs + physicalTableName 获取 |

---

## 任务拆解（对应设计文档 §10）

### N1 — 嵌入式 Neo4j 启动 + 约束 + 向量索引

**新增文件**（均在 `tis-ontology-plugin/…/ontology/sync/`）：

#### `Neo4jStoreManager.java`

从 `EmbeddedNeo4jManager` 复制并改造：
- 数据目录改为 `Config.getMetaCfgDir()/neo4j-data/`（对齐 TIS 数据路径约定）
- 做成单例（`getInstance()`），TIS 启动时调用，注册 JVM shutdown hook
- 对外暴露 `getDatabase() → GraphDatabaseService`

#### `Neo4jBootstrapper.java`

调用 `Neo4jStoreManager.getInstance().getDatabase()`，执行以下幂等 Cypher（启动时一次调用）：

```cypher
// 唯一性约束
CREATE CONSTRAINT domain_name_unique     IF NOT EXISTS FOR (d:OntologyDomain)  REQUIRE d.name IS UNIQUE;
CREATE CONSTRAINT ot_unique              IF NOT EXISTS FOR (n:ObjectType)      REQUIRE (n.domain, n.name) IS UNIQUE;
CREATE CONSTRAINT shared_prop_unique     IF NOT EXISTS FOR (n:SharedProperty)  REQUIRE (n.domain, n.name) IS UNIQUE;
CREATE CONSTRAINT value_type_unique      IF NOT EXISTS FOR (n:ValueType)       REQUIRE (n.domain, n.name) IS UNIQUE;
CREATE CONSTRAINT glossary_unique        IF NOT EXISTS FOR (n:Glossary)        REQUIRE (n.domain, n.term) IS UNIQUE;

// HNSW 向量索引（4 个 label）
CREATE VECTOR INDEX objecttype_embedding_idx    IF NOT EXISTS FOR (n:ObjectType)     ON (n.embedding) OPTIONS {indexConfig:{`vector.dimensions`:384,`vector.similarity_function`:'cosine'}};
CREATE VECTOR INDEX property_embedding_idx      IF NOT EXISTS FOR (n:Property)       ON (n.embedding) OPTIONS {...};
CREATE VECTOR INDEX sharedprop_embedding_idx    IF NOT EXISTS FOR (n:SharedProperty) ON (n.embedding) OPTIONS {...};
CREATE VECTOR INDEX glossary_embedding_idx      IF NOT EXISTS FOR (n:Glossary)       ON (n.embedding) OPTIONS {...};
```

参考 `SemanticSearchService.ensureVectorIndex()` 的等待-ONLINE 轮询模式。

#### `OntologyEmbeddingService.java`

从 `OnnxEmbeddingService` + `BertTokenizer` 复制到 `tis-ontology-plugin`（避免跨工程依赖未发布的 demo jar）。  
ONNX 模型资源路径：`models/model.onnx`（放到 `tis-ontology-plugin/src/main/resources/models/`）。

**pom.xml 新增依赖**（`tis-ontology-plugin/pom.xml`）：

```xml
<dependency>
  <groupId>org.neo4j</groupId>
  <artifactId>neo4j</artifactId>
  <version>5.18.1</version>
</dependency>
<dependency>
  <groupId>com.microsoft.onnxruntime</groupId>
  <artifactId>onnxruntime</artifactId>
  <version>1.17.3</version>
</dependency>
```

---

### N2 — `OntologyNeo4jSyncService`（全量 + 增量 MERGE）

**新增文件**：`sync/OntologyNeo4jSyncService.java`

核心方法：

```java
// 全量重建一个 domain 的子图（先清后写）
public void fullRebuild(String domain);

// 增量 MERGE 单个实体（幂等）
public void syncObjectType(String domain, OntologyObjectType ot);
public void syncProperty(String domain, String otName, OntologyProperty prop, int ordinal);
public void syncLinker(String domain, OntologyLinker linker);
public void syncSharedProperty(String domain, OntologySharedProperty sp);
public void syncValueType(String domain, OntologyValueType vt);
public void syncGlossary(String domain, OntologyGlossary g);

// 删除孤立节点（巡检用）
public void deleteNode(String domain, String label, String nameOrTerm);
```

Cypher 模板（以 ObjectType 为例，其他同 §5 设计）：

```cypher
MERGE (d:OntologyDomain {name: $domain})
MERGE (ot:ObjectType {domain: $domain, name: $name})
SET   ot.alias=$alias, ot.description=$description,
      ot.dorisDs=$dorisDs, ot.physicalTable=$physicalTable,
      ot.embedding=$embedding, ot.updatedAt=timestamp()
MERGE (d)-[:CONTAINS]->(ot)
```

**Linker 折叠为关系**：对每个 `ObjectLinkInfo`（从 `linker.getLinkResourcesStep().getLinks().stream()` 取），执行：

```cypher
MATCH (src:ObjectType {domain:$domain, name:$source}),
      (tgt:ObjectType {domain:$domain, name:$target})
MERGE (src)-[r:LINKED_TO {linkerName:$linkerName}]->(tgt)
SET r.relType=$relType, r.cardinality=$cardinality,
    r.sourceField=$sourceField, r.targetField=$targetField,
    r.viaObjectType=$viaObjectType
```

同步前先 `MATCH (src)-[r:LINKED_TO {linkerName:$linkerName}]->(tgt) DELETE r` 清旧关系。

**Glossary 关系**：按 `target` 类型分支：
- `GlossaryTargetOT` → `MERGE (g)-[:TARGETS_OT]->(ot)`
- `GlossaryTargetProperty` → `MERGE (g)-[:TARGETS_PROP]->(p)`
- `GlossaryTargetMetricExpr` → 仅在 Glossary 节点上存 `metricSql` 属性，无额外边

---

### N3 — `OntologyGraphMapper` + `EmbeddingTextBuilder`

**新增文件**：`sync/OntologyGraphMapper.java`

将 domain 内所有实体组装为 Cypher 参数 `Map<String, Object>`，供 `syncXxx()` 方法调用。  
主要逻辑：`DataSourceBinding` → 取 `dbName`（dorisDs）、`physicalTableName`；`DefaultOntologyProperty` → 取 `getSemanticRole().name()`（role）和 `aggregation`（agg，MeasureRole 专有）。

**新增文件**：`sync/EmbeddingTextBuilder.java`

按设计文档 §3 的拼装策略：

| 节点 | embedding 文本 |
|---|---|
| ObjectType | `name + " " + alias + " " + description + " " + 前5列名` |
| Property | `otName + "." + name + " " + alias + " " + description + " " + role` |
| SharedProperty | `name + " " + alias + " " + description` |
| Glossary | `term + " " + synonyms.join(" ") + " " + description` |

调用 `OntologyEmbeddingService.embed(text)` 得到 `float[]`，转 `List<Float>` 传 Cypher（注意**必须 Float 不能 Double**，否则 HNSW 索引无法命中，参见 demo `SemanticSearchService.toFloatList()` 注释）。

---

### N4 — `IPluginStore.AfterPluginSaved` 钩子接入 + 异步队列

**修改文件**：抽象层各 Ontology 类实现 `AfterPluginSaved`，**但**实际触发委托给 `OntologyNeo4jSyncService`（依赖注入/静态单例）。

推荐做法：在 `Ontology` 基类（或在各具体 Default 实现类）中重写 `afterSaved()`，示例：

```java
// 在 DefaultOntologyObjectType 中：
@Override
public void afterSaved(IPluginContext pluginContext, Optional<Context> context) {
    OntologySyncQueue.enqueue(() ->
        OntologyNeo4jSyncService.getInstance().syncObjectType(domain, this));
}
```

**新增文件**：`sync/OntologySyncQueue.java`

使用 `LinkedBlockingQueue` + 单个后台守护线程（命名 `ontology-neo4j-sync`），任务为 `Runnable`，消费时捕获异常并记 warn 日志，不阻塞主线程。

**全量重建触发点**：在 `DefaultOntologyDomain.afterSaved()` 中（Domain 首次创建 / defaultDomain 切换时），enqueue 全量重建任务：

```java
OntologySyncQueue.enqueue(() -> OntologyNeo4jSyncService.getInstance().fullRebuild(this.name));
```

**巡检定时任务**：在 `Neo4jBootstrapper` 中，用 `ScheduledExecutorService` 每 5 分钟扫一次 XML 目录，对照 Neo4j 中的节点，`DELETE` 孤立节点。

---

## 文件清单汇总

| 文件（均在 `tis-ontology-plugin/…/ontology/`） | 操作 |
|---|---|
| `sync/Neo4jStoreManager.java` | 新增（从 EmbeddedNeo4jManager 改造） |
| `sync/OntologyEmbeddingService.java` | 新增（从 OnnxEmbeddingService + BertTokenizer 复制） |
| `sync/Neo4jBootstrapper.java` | 新增（约束 + 索引 + 巡检定时器） |
| `sync/OntologyNeo4jSyncService.java` | 新增（MERGE + 删除） |
| `sync/OntologyGraphMapper.java` | 新增（实体 → Cypher 参数） |
| `sync/EmbeddingTextBuilder.java` | 新增（embedding 文本拼装） |
| `sync/OntologySyncQueue.java` | 新增（异步队列 + 后台线程） |
| `impl/domain/DefaultOntologyDomain.java` | 修改：实现 `AfterPluginSaved`，触发全量重建 |
| `impl/objtype/DefaultOntologyObjectType.java` | 修改：实现 `AfterPluginSaved` |
| `impl/objtype/DefaultOntologyProperty.java` | 修改：实现 `AfterPluginSaved`（委托给 OT 重新同步 Property） |
| `impl/linker/DefaultOntologyLinker.java` | 修改：实现 `AfterPluginSaved` |
| `impl/sharedproperty/DefaultOntologySharedProperty.java` | 修改：实现 `AfterPluginSaved` |
| `impl/glossary/DefaultOntologyGlossary.java` | 修改：实现 `AfterPluginSaved` |
| `impl/valuetype/DefaultOntologyValueType.java` | 修改：实现 `AfterPluginSaved` |
| `pom.xml` | 修改：添加 neo4j + onnxruntime 依赖 |
| `src/main/resources/models/model.onnx` | 添加（ONNX 模型文件占位说明，实际下载后放入） |

---

## 图模型速查（节点 + 关系）

见设计文档 §2 + §9，实现时直接对照。关键提醒：
- **Property 节点唯一性**：`(domain, otName, name)` 三元组复合，无独立 UNIQUE 约束，由 OT→Property 的 HAS_PROPERTY 边保证。
- **embedding 必须 `List<Float>`**，不能 `List<Double>`，否则 HNSW 索引无法命中（`SemanticSearchService.toFloatList()` 已有正确实现）。
- **Linker 折叠为关系**：JoinTable / Backing 产生两条 `LINKED_TO`（各自的 `ObjectLinkInfo`），通过 `linker.getLinkResourcesStep().getLinks().stream()` 遍历。

---

## 验收标准

1. **保存后同步**：保存任一本体实体后 2 秒内，执行 `MATCH (n:ObjectType {domain:$d}) RETURN n` 能看到该节点已更新（含 embedding）。
2. **向量检索可用**：`CALL db.index.vector.queryNodes('objecttype_embedding_idx', 5, $queryVec)` 返回合理 top-5（embedding 维度正确，List<Float>）。
3. **绑定切换同步**：`DataSourceBinding` 切换后，`:ObjectType {dorisDs}` 属性同步更新。
4. **删除巡检**：删除 XML 实体后，下个巡检周期（5 分钟内）Neo4j 中对应孤立节点被清除。
5. **全量重建幂等**：多次调用 `fullRebuild(domain)` 不产生重复节点（MERGE 语义保证）。

---

## 执行顺序建议

N1（Manager + Embedding + Bootstrapper）→ N3（Mapper + TextBuilder）→ N2（SyncService）→ N4（钩子 + 队列），依次实现，每步可独立单元测试。
