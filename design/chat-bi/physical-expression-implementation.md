# ChatBI 物理表达式（physicalExpression）功能实现文档

## 修改日期
2026-06-02

## 修改初衷

### 问题背景

在 ChatBI 的端到端测试中，发现生成的 SQL 无法正确处理带有特殊格式的数据字段。具体表现为：

**测试场景**：
- 数据库表：`toy_products`
- 问题字段：`Product_Price` 和 `Product_Cost`
- 数据格式：文本类型，带有 `$` 前缀（如 `$6.99`, `$12.50`）
- 用户问题：筛选销售额超过 50 美元的产品类别，并计算各类别销售额占总销售额的比例

**期望 SQL**（包含数据清洗）：
```sql
CAST(REPLACE(TRIM(p.product_price), '$', '') AS DECIMAL) * s.Units
```

**实际生成的 SQL**（缺少数据清洗）：
```sql
CAST(p.Product_Price AS DECIMAL) * s.Units
```

### 根本原因

TIS Ontology 系统缺乏**字段级数据格式转换**的元数据支持：
- OntologyProperty 只描述逻辑语义（类型、角色、聚合函数）
- 没有描述物理存储格式与逻辑查询格式之间的转换关系
- LLM 无法从类型信息（`text`）推断出需要清洗 `$` 符号

### 设计目标

参考 **Palantir Foundry** 的分层理念，在 Property 层添加 `physicalExpression` 字段：
- **物理层**：原始存储格式（带 `$` 的文本）
- **逻辑层**：查询可用格式（数值型）
- **转换层**：`physicalExpression` 描述从物理到逻辑的转换

---

## 架构设计

### 数据流路径

```
OntologyProperty (physicalExpression 定义)
  ↓ (XML 持久化)
Neo4j Sync (同步到图数据库)
  ↓ (Cypher 查询)
GraphRAG Retrieval (检索相关子图)
  ↓ (Markdown 序列化)
Prompt Context (LLM 输入)
  ↓ (SQL 生成)
ChatBI SQL Output (应用物理表达式)
```

### 占位符语法

使用 `{col}` 作为列引用占位符：
- **定义时**：`REPLACE(TRIM({col}), '$', '')`
- **应用时**：`REPLACE(TRIM(p.Product_Price), '$', '')` （自动替换表别名）

---

## 修改文件清单

### 1. 核心抽象层

#### 文件：`tis-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/OntologyProperty.java`

**修改内容**：添加 `physicalExpression` 字段及相关方法

```java
public abstract class OntologyProperty implements Describable<OntologyProperty>, IdentityName, IMultiElement {
    
    // 新增字段：物理表达式（可选）
    @FormField(ordinal = 2, type = FormFieldType.TEXTAREA, validate = {})
    public String physicalExpression;
    
    // Getter
    public String getPhysicalExpression() {
        return physicalExpression;
    }
    
    // 判断是否需要物理层转换
    public boolean needsPhysicalTransform() {
        return StringUtils.isNotBlank(physicalExpression);
    }
    
    // 应用物理转换（替换 {col} 占位符）
    public String applyPhysicalTransform(String columnRef) {
        if (!needsPhysicalTransform()) {
            return columnRef;
        }
        return physicalExpression.replace("{col}", columnRef);
    }
}
```

**设计要点**：
- `ordinal = 2` 确保字段在表单中位于 `description` 之后
- `FormFieldType.TEXTAREA` 支持多行 SQL 表达式
- `validate = {}` 表示可选字段，向后兼容现有 Ontology
- 占位符 `{col}` 适配不同表别名场景

---

### 2. Neo4j 同步层

#### 文件 A：`plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/sync/OntologyGraphMapper.java`

**修改位置**：`toPropertyParams()` 方法，第 105 行附近

**修改内容**：
```java
private Map<String, Object> toPropertyParams(String domain, String otName, 
                                             OntologyProperty prop, int ordinal) {
    Map<String, Object> p = new HashMap<>();
    // ... 现有代码 ...
    p.put(ROLE, role);
    p.put(AGG, agg);
    
    // 新增：同步 physicalExpression
    p.put("physicalExpr", nullSafe(prop.getPhysicalExpression()));
    
    p.put(EMBEDDING, embeddingService.embedAsList(...));
    return p;
}
```

#### 文件 B：`plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/sync/OntologyNeo4jSyncService.java`

**修改位置**：`syncProperty()` 方法，第 189 行附近

**修改内容**：更新 Cypher SET 子句
```java
public void syncProperty(String domain, String otName, OntologyProperty prop, int ordinal) {
    Map<String, Object> p = mapper.toPropertyParams(domain, otName, prop, ordinal);
    try (Transaction tx = db.beginTx()) {
        tx.execute("""
                MERGE (ot:ObjectType {domain: $domain, name: $otName})
                MERGE (pr:Property {domain: $domain, otName: $otName, name: $name})
                SET pr.type        = $type,
                    pr.endType     = $endType,
                    pr.pk          = $pk,
                    pr.nullable    = $nullable,
                    pr.description = $description,
                    pr.role        = $role,
                    pr.agg         = $agg,
                    pr.physicalExpr = $physicalExpr,  // 新增
                    pr.embedding   = $embedding,
                    pr.updatedAt   = timestamp()
                MERGE (ot)-[:HAS_PROPERTY {ordinal: $ordinal}]->(pr)
                """, p);
        // ...
    }
}
```

---

### 3. GraphRAG 检索层

#### 文件 A：`plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/graphrag/SubgraphSnapshot.java`

**修改位置**：`PropertyNode` 类定义，第 78 行附近

**修改内容**：
```java
static final class PropertyNode {
    final String name;
    final String type;
    final String role;
    final String agg;
    final boolean pk;
    final boolean nullable;
    final String description;
    final String physicalExpr;  // 新增字段
    final String valueTypeRef;
    final String sharedPropRef;
    final String constraintKind;
    final String constraintParams;
    final double score;

    PropertyNode(String name, String type, String role, String agg, 
                 boolean pk, boolean nullable, String description,
                 String physicalExpr,  // 新增参数（第8个位置）
                 String valueTypeRef, String sharedPropRef,
                 String constraintKind, String constraintParams, double score) {
        this.name = name;
        this.type = type;
        this.role = role;
        this.agg = agg;
        this.pk = pk;
        this.nullable = nullable;
        this.description = description;
        this.physicalExpr = physicalExpr;  // 赋值
        this.valueTypeRef = valueTypeRef;
        this.sharedPropRef = sharedPropRef;
        this.constraintKind = constraintKind;
        this.constraintParams = constraintParams;
        this.score = score;
    }
}
```

#### 文件 B：`plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/graphrag/DefaultGraphRAGService.java`

**修改位置 1**：Cypher 查询，第 383 行附近
```java
private SubgraphSnapshot expandSubgraph(...) {
    // ...
    String cypherQuery = """
        MATCH (ot:ObjectType {domain: $domain, name: $otName})-[hp:HAS_PROPERTY]->(p:Property)
        RETURN p.name AS pName, 
               p.type AS pType, 
               p.role AS pRole, 
               p.agg AS pAgg,
               p.pk AS pPk, 
               p.nullable AS pNullable, 
               p.description AS pDesc,
               p.physicalExpr AS pPhysicalExpr,  // 新增返回字段
               p.embedding AS pEmbedding,
               hp.ordinal AS ordinal
        ORDER BY ordinal
        """;
    // ...
}
```

**修改位置 2**：PropertyNode 实例化，第 402 行附近
```java
while (propResult.hasNext()) {
    Map<String, Object> row = propResult.next();
    PropertyNode pn = new PropertyNode(
        (String) row.get("pName"),
        (String) row.get("pType"),
        (String) row.get("pRole"),
        (String) row.get("pAgg"),
        Boolean.TRUE.equals(row.get("pPk")),
        Boolean.TRUE.equals(row.get("pNullable")),
        (String) row.get("pDesc"),
        (String) row.get("pPhysicalExpr"),  // 新增：传递 physicalExpr（第8个参数）
        (String) row.get("pValueTypeRef"),
        (String) row.get("pSharedPropRef"),
        (String) row.get("pConstraintKind"),
        (String) row.get("pConstraintParams"),
        propScore
    );
    // ...
}
```

---

### 4. Prompt 序列化层

#### 文件 A：`plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/graphrag/PromptSerializer.java`

**修改位置**：`appendProperty()` 方法，第 144 行附近

**修改内容**：
```java
private static void appendProperty(StringBuilder sb, SubgraphSnapshot.PropertyNode p,
                                   boolean includeValueExamples, boolean withDescription) {
    sb.append("  - ").append(p.name).append(" : ").append(p.type);
    if (p.pk) sb.append(" PK");
    if (!p.nullable) sb.append(" NOT_NULL");
    if (p.role != null && !"Unknown".equals(p.role)) sb.append(" [").append(p.role).append(']');
    if (StringUtils.isNotBlank(p.agg)) sb.append(" agg=").append(p.agg);
    
    // 新增：物理表达式序列化
    if (StringUtils.isNotBlank(p.physicalExpr)) {
        sb.append(" **physical=`").append(p.physicalExpr).append("`**");
    }
    
    if (StringUtils.isNotBlank(p.sharedPropRef)) sb.append(" → SP:").append(p.sharedPropRef);
    if (withDescription && StringUtils.isNotBlank(p.description)) {
        sb.append(" — ").append(p.description);
    }
    // ...
    sb.append('\n');
}
```

**序列化示例**：
```markdown
### toy_products (产品表)
- columns:
  - Product_Price : text [Measure] agg=Sum **physical=`REPLACE(TRIM({col}), '$', '')`** — 产品价格（带$符号）
```

#### 文件 B：`plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/chatbi/prompt/PromptTemplate.java`

**修改位置**：`SYSTEM_PROMPT` 常量，第 40-47 行

**修改内容**：添加物理表达式处理规则
```java
public static final String SYSTEM_PROMPT = """
    你是一名 Apache Doris SQL 专家。请根据下方"业务上下文"，把用户问题翻译成
    一条**可在 Doris 上执行**的 SQL。仅输出 SQL 本身，不要任何解释。

    ## Doris 方言关键提示
    - 时间字段使用 DATE / DATETIME，函数用 `date_trunc('day', col)` / `date_format`
    - 分组排序使用 `GROUP BY` + `ORDER BY`，TOP-N 使用 `LIMIT`
    - 不要使用 SQLite 特有函数（如 julianday）
    - 字符串拼接使用 `concat(a, b)`
    - 不要使用未在"相关数据表"中出现的表名/列名
    - 默认对 NULL 安全：使用 `coalesce` 或 `is null`

    ## 物理表达式处理规则（重要）
    - 如果列标注了 `**physical=<expr>**`，在 SQL 中使用该列时，必须将列的完整引用（如 `p.Product_Price`）替换到 `{col}` 占位符中
    - 示例：列 `Product_Price` 标注 `**physical=`REPLACE(TRIM({col}), '$', '')`**`
      - ❌ 错误写法：`CAST(p.Product_Price AS DECIMAL)`
      - ✅ 正确写法：`CAST(REPLACE(TRIM(p.Product_Price), '$', '') AS DECIMAL)`
    - 物理表达式适用于该列在 SQL 中的所有出现位置（SELECT、WHERE、GROUP BY、HAVING、ORDER BY）
    - 多表 JOIN 时注意表别名：`{col}` 应替换为 `<alias>.<column>`（如 `p.Product_Price`）
    - 物理表达式是数据清洗的必要步骤，不能省略
    """;
```

---

## 使用示例

### 配置示例

在 Ontology 配置 XML 中（如 `toy_products.xml`）：

```xml
<com.qlangtech.tis.plugin.ontology.OntologyProperty>
  <name>Product_Price</name>
  <typeRef class="com.qlangtech.tis.plugin.ontology.impl.typeref.DefaultPropertyTypeRef">
    <type>1</type>
  </typeRef>
  <pk>false</pk>
  <nullable>true</nullable>
  <physicalExpression>REPLACE(TRIM({col}), '$', '')</physicalExpression>
</com.qlangtech.tis.plugin.ontology.OntologyProperty>
```

### 前端表单字段

| 字段名 | 标签 | 类型 | 必填 | 示例 |
|--------|------|------|------|------|
| name | 列名 | 文本 | ✅ | Product_Price |
| description | 描述 | 多行文本 | ✅ | 产品价格（带$符号） |
| physicalExpression | 物理表达式 | 多行文本 | ❌ | REPLACE(TRIM({col}), '$', '') |
| pk | 主键 | 布尔 | ✅ | false |
| nullable | 可空 | 布尔 | ✅ | true |

### SQL 生成效果

**用户问题**：筛选销售额超过50美元的产品类别

**生成的 SQL**（带物理表达式清洗）：
```sql
SELECT 
    p.Product_Category,
    SUM(CAST(REPLACE(TRIM(p.Product_Price), '$', '') AS DECIMAL) * s.Units) AS total_sales
FROM toy_products p
JOIN toy_sales s ON p.Product_ID = s.Product_ID
GROUP BY p.Product_Category
HAVING SUM(CAST(REPLACE(TRIM(p.Product_Price), '$', '') AS DECIMAL) * s.Units) > 50
ORDER BY total_sales DESC
```

---

## 数据库方言兼容性说明

### 标准 SQL 函数兼容性

| 函数 | Doris | MySQL | PostgreSQL | ClickHouse | Hive | Oracle |
|------|-------|-------|------------|------------|------|--------|
| `TRIM()` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `REPLACE()` | ✅ | ✅ | ✅ | ❌ `replaceAll()` | ❌ `regexp_replace()` | ✅ |
| `CAST()` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `CONCAT()` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

### 使用建议

1. **优先使用标准 SQL 函数**：`TRIM`, `CAST`, `COALESCE` 等在主流数据库中语法一致
2. **方言绑定**：`physicalExpression` 应使用与 `boundDsName` 一致的数据库方言
3. **迁移注意**：切换数据源类型（如 Doris → ClickHouse）时需同步更新 `physicalExpression`

### 跨方言转换示例

| 场景 | Doris/MySQL/PostgreSQL | ClickHouse | Hive |
|------|------------------------|------------|------|
| 清洗货币符号 | `REPLACE(TRIM({col}), '$', '')` | `replaceAll(trim({col}), '$', '')` | `regexp_replace(trim({col}), '\\$', '')` |
| 清洗百分比 | `REPLACE({col}, '%', '') / 100` | `replaceAll({col}, '%', '') / 100` | `regexp_replace({col}, '%', '') / 100` |

---

## 测试验证

### 单元测试

测试 `OntologyProperty.applyPhysicalTransform()` 方法：

```java
@Test
public void testPhysicalExpressionTransform() {
    DefaultOntologyProperty prop = new DefaultOntologyProperty();
    prop.name = "Product_Price";
    prop.physicalExpression = "REPLACE(TRIM({col}), '$', '')";
    
    // 验证占位符替换
    String result = prop.applyPhysicalTransform("p.Product_Price");
    assertEquals("REPLACE(TRIM(p.Product_Price), '$', '')", result);
    
    // 验证空表达式
    prop.physicalExpression = null;
    assertEquals("p.Product_Price", prop.applyPhysicalTransform("p.Product_Price"));
}
```

### Neo4j 验证

触发全量同步后查询：
```cypher
MATCH (ot:ObjectType {name: "toy_products"})-[:HAS_PROPERTY]->(p:Property {name: "Product_Price"})
RETURN p.name, p.type, p.role, p.physicalExpr
```

预期返回：
```
Product_Price | text | Measure | REPLACE(TRIM({col}), '$', '')
```

### Prompt 验证

运行 GraphRAG 检索：
```java
RetrievalResult result = DefaultGraphRAGService.getInstance()
    .retrieve("falcon_14", "产品价格", RetrievalOptions.defaults());

String promptContext = result.promptContext();
assertTrue(promptContext.contains("physical=`REPLACE(TRIM({col}), '$', '')`"));
```

### 端到端测试

运行 `DefaultChatBIServiceITTest.testAsk()`，检查生成的 SQL：

**验证点 1**：SELECT 子句
```sql
SUM(CAST(REPLACE(TRIM(p.Product_Price), '$', '') AS DECIMAL) * s.Units)
```

**验证点 2**：HAVING 子句
```sql
HAVING SUM(CAST(REPLACE(TRIM(p.Product_Price), '$', '') AS DECIMAL) * s.Units) > 50
```

**验证点 3**：无裸列引用
```bash
# 不应包含未清洗的列引用
! grep -q "CAST(p.Product_Price AS DECIMAL)" evaluate_result.txt
```

---

## 最佳实践

### 1. 何时使用 physicalExpression

**适用场景**：
- ✅ 数据带有格式前缀/后缀（货币符号 `$`, `¥`，单位 `kg`, `%`）
- ✅ 需要去除空白字符（`TRIM`）
- ✅ 需要类型转换前的字符串清洗
- ✅ 数据存储格式与查询格式不一致

**不适用场景**：
- ❌ 复杂业务计算（应使用 Glossary + MetricExpr）
- ❌ 多表 JOIN 逻辑（应在 SQL 层实现）
- ❌ 条件分支逻辑（应使用 CASE WHEN）

### 2. 占位符使用规范

| 场景 | 定义时 | 应用时 |
|------|--------|--------|
| 单表查询 | `REPLACE(TRIM({col}), '$', '')` | `REPLACE(TRIM(Product_Price), '$', '')` |
| 多表 JOIN | `REPLACE(TRIM({col}), '$', '')` | `REPLACE(TRIM(p.Product_Price), '$', '')` |
| 子查询 | `REPLACE(TRIM({col}), '$', '')` | `REPLACE(TRIM(sub.Product_Price), '$', '')` |

**关键原则**：`{col}` 应替换为**完整的列引用**（包含表别名）

### 3. 常见模式库

| 场景 | physicalExpression 示例 |
|------|--------------------------|
| 美元货币清洗 | `REPLACE(TRIM({col}), '$', '')` |
| 人民币货币清洗 | `REPLACE(TRIM({col}), '¥', '')` |
| 百分比转小数 | `CAST(REPLACE(TRIM({col}), '%', '') AS DECIMAL) / 100` |
| 千分位去除 | `REPLACE(REPLACE({col}, ',', ''), ' ', '')` |
| 单位去除 | `REGEXP_REPLACE({col}, '[a-zA-Z]+$', '')` |

---

## 架构对比：Palantir Foundry vs TIS

| 层次 | Palantir Foundry | TIS Ontology |
|------|------------------|--------------|
| 物理层 | Pipeline Transform（ETL 清洗） | `physicalExpression`（运行时清洗） |
| 映射层 | Dataset → Ontology 映射 | `OntologyObjectType.physicalTable` |
| 语义层 | Property Type + Role | `PropertyRoleType` + `SemanticRole` |
| 业务层 | Metric（复杂聚合计算） | `Glossary` + `MetricExpr` |

**设计理念一致性**：
- ✅ 物理层与逻辑层分离
- ✅ 字段级元数据驱动
- ✅ 声明式转换规则
- ✅ 对 LLM 友好的 Prompt 序列化

---

## 未来扩展方向

### 短期（已实现）
- ✅ 字段级 physicalExpression 支持
- ✅ 占位符语法 `{col}`
- ✅ Neo4j 同步和 GraphRAG 传播
- ✅ LLM Prompt 增强

### 中期（待评估）
- ⏳ 前端可视化表单字段（TEXTAREA 配置）
- ⏳ 常见模式库（预定义清洗模板）
- ⏳ 数据采样自动检测格式问题

### 长期（待需求驱动）
- 🔮 SQL 方言自动翻译（跨数据库兼容）
- 🔮 物理表达式验证（语法检查）
- 🔮 性能优化（表达式下推到数据库）

---

## 相关文档

- [TIS Ontology 架构设计](../ontology/architecture.md)
- [GraphRAG 检索机制](../ontology/graphrag-design.md)
- [ChatBI NL-to-SQL 流程](./chatbi-pipeline.md)
- [Palantir Foundry 对比分析](./palantir-comparison.md)

---

## 版本历史

| 版本 | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-06-02 | Claude Code | 初始实现，支持字段级 physicalExpression |

---

**文档生成时间**: 2026-06-02  
**TIS 版本**: 4.3.0  
**相关 PR**: （待提交）
