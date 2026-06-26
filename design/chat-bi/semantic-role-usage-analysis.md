# ChatBI 中 SemanticRole (roleType) 的使用分析

## 修改日期
2026-06-26

## 问题背景

在 `DefaultOntologyProperty.java:L79` 中定义了 `roleType` 字段：

```java
@FormField(ordinal = 5, validate = {Validator.require})
public PropertyRoleType roleType;

public SemanticRole getSemanticRole() {
    return this.roleType == null ? SemanticRole.Unknown : this.roleType.kind();
}
```

这个字段在 ChatBI 的当前实现中**已经部分使用**，但还有更多优化空间。本文档分析其当前使用情况和未来增强方向。

---

## SemanticRole 枚举定义

```java
public enum SemanticRole {
    Unknown(0, "未识别的语义角色，作为默认兜底"),
    Identifier(1, "实体唯一标识列，用于定位对象实例"),
    Dimension(2, "用于分组与筛选的分类型属性"),
    TimeDimension(3, "时间属性，支持按粒度切分聚合"),
    Measure(4, "可聚合的数值度量，如金额、数量");
}
```

对应的插件实现类：
- `IdentifierRole` - 主键/唯一标识
- `DimensionRole` - 维度（分组/筛选）
- `TimeDimensionRole` - 时间维度（时间分组）
- `MeasureRole` - 度量（聚合计算，包含 aggregation 配置）
- `UnknownRole` - 兜底

---

## 当前使用路径（已实现）

### 1. 数据流路径

```
OntologyProperty.roleType (定义)
    ↓
OntologyGraphMapper.toPropertyParams() (提取 role 名称)
    ↓
Neo4j Property 节点 (p.role 属性)
    ↓
DefaultGraphRAGService.expandSubgraph() (查询返回 pRole)
    ↓
SubgraphSnapshot.PropertyNode (role 字段)
    ↓
PromptSerializer.appendProperty() (序列化为 Markdown)
    ↓
LLM Prompt 上下文 (如: Product_Price : text [Measure] agg=Sum)
    ↓
LLM 生成 SQL (理解语义角色，生成合适的 SQL 结构)
```

### 2. 代码实现位置

#### (1) Neo4j 同步层
**文件**: `OntologyGraphMapper.java:L95-103`

```java
String role = "Unknown";
String agg = null;
if (prop instanceof DefaultOntologyProperty dp) {
    role = dp.getSemanticRole().name();  // ← 使用 roleType.kind()
    if (dp.roleType instanceof MeasureRole mr && mr.getAggregation() != null) {
        agg = mr.getAggregation().kind().name();  // ← MeasureRole 的聚合函数
    }
}
p.put(ROLE, role);
p.put(AGG, agg);
```

**功能**: 将 `roleType` 转换为字符串并同步到 Neo4j 的 `Property.role` 属性。

#### (2) GraphRAG 检索层
**文件**: `DefaultGraphRAGService.java:L382`

```cypher
MATCH (ot:ObjectType {domain: $domain, name: $otName})-[hp:HAS_PROPERTY]->(p:Property)
RETURN p.name AS pName, 
       p.type AS pType, 
       p.role AS pRole,  // ← 查询 role
       p.agg AS pAgg,
       ...
```

**文件**: `SubgraphSnapshot.java`

```java
static final class PropertyNode {
    final String role;  // ← 保存 role 名称
    // ...
}
```

#### (3) Prompt 序列化层
**文件**: `PromptSerializer.java:L144`

```java
if (p.role != null && !"Unknown".equals(p.role)) 
    sb.append(" [").append(p.role).append(']');
if (StringUtils.isNotBlank(p.agg)) 
    sb.append(" agg=").append(p.agg);
```

**输出示例**:
```markdown
### toy_products (产品表)
- columns:
  - Product_ID : bigint PK [Identifier] — 产品唯一标识
  - Product_Name : text [Dimension] — 产品名称
  - Product_Price : text [Measure] agg=Sum — 产品价格
  - Sale_Date : datetime [TimeDimension] — 销售日期
```

#### (4) LLM 理解语义
**位置**: Prompt 模板中**隐式使用**

LLM 通过看到 `[Measure]` / `[Dimension]` / `[TimeDimension]` 标记，能够：
- 识别哪些字段适合用于 `GROUP BY`（Dimension, TimeDimension）
- 识别哪些字段适合用于 `SUM()` / `AVG()` 等聚合（Measure）
- 识别主键字段（Identifier），避免对其做聚合
- 识别时间字段，使用 `date_trunc()` / `date_format()` 等时间函数

---

## 未充分利用的场景（建议增强）

虽然 `roleType` 已经通过 Prompt 传递给 LLM，但以下场景可以**更主动地利用**语义角色：

### 1. SQL 生成后的智能验证

**问题**: 当前的 `AstValidator` 和 `ExplainValidator` 是通用校验器，不感知语义角色。

**改进方向**:
```java
// 新增：SemanticRoleValidator
public class SemanticRoleValidator {
    public ValidationResult validate(String sql, SubgraphSnapshot subgraph) {
        // 规则 1: Identifier 字段不应出现在聚合函数中
        // 规则 2: Measure 字段在 GROUP BY 查询中应使用聚合函数
        // 规则 3: TimeDimension 应使用时间函数（date_trunc, date_format）
        // 规则 4: Dimension 字段应在 GROUP BY 子句或 WHERE 子句中
    }
}
```

**示例错误捕获**:
```sql
-- ❌ 错误：对主键做聚合
SELECT SUM(Product_ID) FROM toy_products;

-- ✅ 正确：对度量做聚合
SELECT SUM(CAST(REPLACE(TRIM(Product_Price), '$', '') AS DECIMAL)) FROM toy_products;
```

### 2. Prompt 模板中的显式规则

**当前状态**: Prompt 模板中只是将 role 标记展示给 LLM，但没有明确的使用规则。

**改进建议**: 在 `PromptTemplate.BASE_SYSTEM_PROMPT` 中增加语义角色使用规则：

```java
private static final String SEMANTIC_ROLE_RULES = """

    ## 语义角色使用规则
    - **[Identifier]**: 实体主键，用于 JOIN 或唯一定位，不应出现在聚合函数中
    - **[Dimension]**: 分类属性，适合用于 GROUP BY 分组或 WHERE 筛选
    - **[TimeDimension]**: 时间属性，需使用时间函数
      - 按日聚合: `date_trunc('day', col)`
      - 按月聚合: `date_trunc('month', col)`
      - 格式化: `date_format(col, '%Y-%m-%d')`
    - **[Measure]**: 数值度量，在分组查询中应使用聚合函数
      - 如果标注了 `agg=Sum`，优先使用 `SUM(col)`
      - 如果标注了 `agg=Avg`，优先使用 `AVG(col)`
      - 如果标注了 `agg=Count`，使用 `COUNT(col)`
    """;
```

### 3. MeasureRole 的 aggregation 字段深度使用

**当前状态**: `MeasureRole` 包含 `aggregation` 字段（指定默认聚合方式），已同步到 Neo4j 的 `p.agg` 属性，并在 Prompt 中展示为 `agg=Sum`。

**当前使用**:
```markdown
Product_Price : text [Measure] agg=Sum — 产品价格
```

**深度使用建议**:

#### (a) Prompt 层强化
在用户问题包含"总销售额"/"平均价格"等聚合语义时，优先匹配到 `agg` 标注的聚合方式：

```java
// PromptTemplate 中增加提示
## 聚合函数提示
- 如果属性标注了 `agg=Sum`，且用户问题包含"总计/总额/合计"，应使用 `SUM(col)`
- 如果属性标注了 `agg=Avg`，且用户问题包含"平均/均值"，应使用 `AVG(col)`
- 如果属性标注了 `agg=Count`，且用户问题包含"数量/个数"，应使用 `COUNT(col)`
```

#### (b) 后处理层修正
如果 LLM 生成的 SQL 对 Measure 字段使用了错误的聚合函数（与 `agg` 标注不匹配），可以做智能提示或自动修正：

```java
// 伪代码
if (measure.agg == "Sum" && sqlContains("AVG(" + measure.name)) {
    warn("建议使用 SUM 而非 AVG，因为该字段配置为求和度量");
}
```

### 4. TimeDimension 的时间粒度智能推断

**当前状态**: TimeDimension 角色已标记，但没有进一步的时间粒度配置。

**增强方向**: 可以在 `TimeDimensionRole` 中扩展配置：

```java
public class TimeDimensionRole extends PropertyRoleType {
    @FormField(ordinal = 0, type = FormFieldType.ENUM_SELECTABLE)
    public TimeGranularity defaultGranularity;  // 默认聚合粒度: DAY, MONTH, YEAR
    
    public enum TimeGranularity {
        DAY, WEEK, MONTH, QUARTER, YEAR
    }
}
```

**使用场景**:
```markdown
Sale_Date : datetime [TimeDimension] granularity=DAY — 销售日期
```

当用户问"每月销售额"时，LLM 知道应该使用 `date_trunc('month', Sale_Date)`。

### 5. Identifier 字段的自动 JOIN 推断

**当前状态**: Identifier 角色已标记，但在 JOIN 场景中没有优先级提示。

**增强方向**: 当 GraphRAG 检索到多个 ObjectType 需要 JOIN 时，优先使用 `[Identifier]` 字段作为 JOIN 条件：

```java
// 在 PromptBuilder 中增加 JOIN 提示
## JOIN 关系提示
- 如果需要连接 toy_products 和 toy_sales 表，优先使用主键字段 Product_ID [Identifier]
- JOIN 条件: p.Product_ID = s.Product_ID
```

---

## 对比：当前 vs 增强后

| 维度 | 当前实现 | 增强后 |
|------|---------|--------|
| **数据同步** | ✅ roleType → Neo4j (p.role) | ✅ 保持不变 |
| **Prompt 展示** | ✅ 标记展示 (如 `[Measure]`) | ✅ 保持 + 增加使用规则 |
| **LLM 理解** | ✅ 隐式理解语义 | ✅ 显式规则引导 |
| **SQL 验证** | ❌ 未感知语义角色 | ✅ SemanticRoleValidator |
| **聚合函数** | ✅ 展示 `agg=Sum` | ✅ 强化匹配 + 后处理校验 |
| **时间函数** | ⚠️ 依赖 LLM 推断 | ✅ 时间粒度配置 |
| **JOIN 推断** | ⚠️ 依赖 LLM 推断 | ✅ Identifier 优先级提示 |

---

## 实现优先级建议

### 第一阶段（高优先级 - 低成本）
1. **Prompt 模板增强**: 在 `PromptTemplate` 中增加 `SEMANTIC_ROLE_RULES`，明确各角色的使用规则。
2. **聚合函数强化**: 在 Prompt 中增加"聚合函数提示"部分，引导 LLM 使用与 `agg` 标注一致的聚合方式。

### 第二阶段（中优先级 - 中等成本）
3. **SemanticRoleValidator**: 实现基于语义角色的 SQL 验证器，捕获常见错误（如对主键做聚合）。
4. **MeasureRole 后处理**: 检查生成的 SQL 中 Measure 字段的聚合函数是否与配置一致。

### 第三阶段（低优先级 - 高成本）
5. **TimeDimensionRole 扩展**: 增加时间粒度配置，支持更精细的时间聚合。
6. **JOIN 推断优化**: 基于 Identifier 角色优化多表 JOIN 的 Prompt 提示。

---

## 代码示例：增强 Prompt 模板

```java
// PromptTemplate.java
private static final String SEMANTIC_ROLE_RULES = """

    ## 语义角色使用规则
    每个字段标注的 `[角色]` 含义如下：
    
    - **[Identifier]**: 实体主键或唯一标识
      - 适用场景: JOIN 连接条件、WHERE 精确查找
      - 不应出现在聚合函数中（SUM/AVG/COUNT 等）
      
    - **[Dimension]**: 分类维度
      - 适用场景: GROUP BY 分组、WHERE 筛选
      - 示例: 按产品类别分组、筛选特定地区
      
    - **[TimeDimension]**: 时间维度
      - 必须使用时间函数:
        * 按日: `date_trunc('day', col)`
        * 按月: `date_trunc('month', col)`
        * 按年: `date_trunc('year', col)`
      - 示例: `GROUP BY date_trunc('month', Sale_Date)`
      
    - **[Measure]**: 数值度量
      - 在 GROUP BY 查询中必须使用聚合函数
      - 如果标注了 `agg=Sum`，优先使用 `SUM(col)`
      - 如果标注了 `agg=Avg`，优先使用 `AVG(col)`
      - 如果标注了 `agg=Count`，使用 `COUNT(col)`
      - 示例: `SUM(CAST(REPLACE(TRIM(p.Product_Price), '$', '') AS DECIMAL))`
    """;

public static String buildSystemPrompt(boolean hasPhysicalExpression) {
    StringBuilder prompt = new StringBuilder(BASE_SYSTEM_PROMPT);
    prompt.append(SEMANTIC_ROLE_RULES);  // ← 增加语义角色规则
    if (hasPhysicalExpression) {
        prompt.append(PHYSICAL_EXPR_RULES);
    }
    return prompt.toString();
}
```

---

## 代码示例：SemanticRoleValidator

```java
package com.qlangtech.tis.plugin.ontology.chatbi.validation;

import com.qlangtech.tis.plugin.ontology.SemanticRole;
import com.qlangtech.tis.plugin.ontology.graphrag.SubgraphSnapshot;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于语义角色的 SQL 验证器。
 */
public class SemanticRoleValidator {
    
    public ValidationResult validate(String sql, SubgraphSnapshot subgraph) {
        try {
            // 构建字段 → role 映射
            Map<String, SemanticRole> fieldRoles = buildFieldRoleMap(subgraph);
            
            // 解析 SQL AST
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            
            // 规则 1: Identifier 不应出现在聚合函数中
            List<String> errors = checkIdentifierInAggregate(plainSelect, fieldRoles);
            
            // 规则 2: Measure 在 GROUP BY 查询中应使用聚合函数
            errors.addAll(checkMeasureWithoutAggregate(plainSelect, fieldRoles));
            
            // 规则 3: TimeDimension 应使用时间函数
            errors.addAll(checkTimeDimensionWithoutTimeFunc(plainSelect, fieldRoles));
            
            if (errors.isEmpty()) {
                return ValidationResult.ok();
            } else {
                return ValidationResult.fail(String.join("; ", errors));
            }
        } catch (Exception e) {
            return ValidationResult.ok(); // 解析失败时不阻塞，交由其他验证器处理
        }
    }
    
    private Map<String, SemanticRole> buildFieldRoleMap(SubgraphSnapshot subgraph) {
        return subgraph.getPropertyNodes().stream()
                .collect(Collectors.toMap(
                        p -> p.name,
                        p -> SemanticRole.valueOf(p.role != null ? p.role : "Unknown")
                ));
    }
    
    private List<String> checkIdentifierInAggregate(PlainSelect select, Map<String, SemanticRole> fieldRoles) {
        // 检查 SELECT 子句中的聚合函数
        // 如果 SUM(Product_ID) 且 Product_ID 是 Identifier，报错
        // 实现略...
        return List.of();
    }
    
    private List<String> checkMeasureWithoutAggregate(PlainSelect select, Map<String, SemanticRole> fieldRoles) {
        // 如果有 GROUP BY，检查 SELECT 中的 Measure 字段是否都用了聚合函数
        // 实现略...
        return List.of();
    }
    
    private List<String> checkTimeDimensionWithoutTimeFunc(PlainSelect select, Map<String, SemanticRole> fieldRoles) {
        // 检查 TimeDimension 字段是否使用了 date_trunc/date_format 等函数
        // 实现略...
        return List.of();
    }
}
```

---

## 总结

### roleType 当前使用情况
✅ **已使用**:
1. 同步到 Neo4j (`p.role`)
2. GraphRAG 检索时传递到子图快照
3. Prompt 中展示为 `[Measure]` / `[Dimension]` 标记
4. LLM 隐式理解语义角色，生成合适的 SQL

### 建议增强方向
⭐ **高价值增强**:
1. 在 Prompt 模板中增加语义角色使用规则（显式引导 LLM）
2. 实现 `SemanticRoleValidator`（捕获语义错误）
3. 强化 `MeasureRole.aggregation` 的使用（匹配验证）

🔮 **长期扩展**:
4. `TimeDimensionRole` 增加时间粒度配置
5. `IdentifierRole` 在 JOIN 推断中优先级提升

---

## 相关文档
- [物理表达式实现](./physical-expression-implementation.md)
- [TIS Ontology 架构](../ontology/architecture.md)
- [GraphRAG 检索机制](../ontology/graphrag-design.md)

---

**文档生成时间**: 2026-06-26  
**TIS 版本**: 4.3.0  
**作者**: Claude Code
