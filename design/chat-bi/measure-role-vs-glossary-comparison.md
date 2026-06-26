# MeasureRole vs Glossary + MetricExpr：设计对比与使用场景

## 修改日期
2026-06-26

## 问题背景

用户提出疑问：**MeasureRole (derived property)** 和 **Glossary + MetricExpr** 在功能上似乎重叠，都能实现跨表聚合的虚拟字段。为什么需要两种机制？各自的适用场景是什么？

---

## 功能对比

### MeasureRole (Derived Property)

**定义位置**: `OntologyProperty.roleType = MeasureRole`

**配置结构**:
```java
public class MeasureRole extends PropertyRoleType {
    // 1. Linked objects 链路（最多 3 跳）
    public List<OntologyLinkerRef> linkers;
    
    // 2. 聚合方式（Sum/Avg/Count/Min/Max）
    public AggregationKind aggregation;
    
    // 3. 单位和精度（可选）
    public String unit;
    public Integer precision;
}
```

**示例配置**:
```xml
<!-- Customer 表定义虚拟字段 total_order_amount -->
<OntologyProperty>
  <name>total_order_amount</name>
  <roleType class="MeasureRole">
    <linkers>
      <OntologyLinkerRef>customer_orders</OntologyLinkerRef>
    </linkers>
    <aggregation class="PropertyTargetedAgg">
      <targetProperty>order_amount</targetProperty>
      <kind>Sum</kind>
    </aggregation>
    <unit>USD</unit>
    <precision>2</precision>
  </roleType>
</OntologyProperty>
```

**在 Prompt 中的展示**:
```markdown
### Customer (客户表)
- columns:
  - customer_id : bigint PK [Identifier]
  - customer_name : text [Dimension]
  - total_order_amount : decimal [Measure] agg=Sum unit=USD — 客户总订单金额（通过 customer_orders 链路聚合）
```

**SQL 生成方式**: LLM 需要理解链路和聚合语义，自己生成 JOIN + 聚合 SQL：
```sql
-- 用户问: "查询订单总额超过 1000 美元的客户"
SELECT c.customer_id, c.customer_name, SUM(o.order_amount) AS total_order_amount
FROM Customer c
JOIN Order o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.customer_name
HAVING SUM(o.order_amount) > 1000;
```

---

### Glossary + MetricExpr

**定义位置**: 独立的 `OntologyGlossary` 实体

**配置结构**:
```java
public class OntologyGlossary {
    public String term;                    // 业务术语（如 "客户终身价值"）
    public List<String> synonyms;          // 同义词（如 "CLV", "LTV"）
    public GlossaryTarget target;          // 指向目标
    public String description;             // 自然语言描述
}

// Target 可以是:
// 1. GlossaryTargetOT - 指向 ObjectType
// 2. GlossaryTargetProperty - 指向某个表的某个字段
// 3. GlossaryTargetMetricExpr - 自定义 SQL 表达式
```

**示例配置**:
```xml
<OntologyGlossary>
  <term>客户终身价值</term>
  <synonyms>
    <string>CLV</string>
    <string>lifetime value</string>
  </synonyms>
  <target class="GlossaryTargetMetricExpr">
    <sql><![CDATA[
      SUM(o.order_amount) * 1.2 - SUM(o.refund_amount)
    ]]></sql>
  </target>
  <description>客户所有订单的总金额扣除退款，乘以 1.2 的毛利率系数</description>
</OntologyGlossary>
```

**在 Prompt 中的展示**:
```markdown
## 业务术语
- **客户终身价值** (CLV, lifetime value)
  - 定义: 客户所有订单的总金额扣除退款，乘以 1.2 的毛利率系数
  - SQL: `SUM(o.order_amount) * 1.2 - SUM(o.refund_amount)`
```

**SQL 生成方式**: LLM 识别业务术语，直接使用预定义的 SQL 表达式：
```sql
-- 用户问: "查询 CLV 超过 5000 的客户"
SELECT c.customer_id, c.customer_name,
       SUM(o.order_amount) * 1.2 - SUM(o.refund_amount) AS customer_lifetime_value
FROM Customer c
JOIN Order o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.customer_name
HAVING SUM(o.order_amount) * 1.2 - SUM(o.refund_amount) > 5000;
```

---

## 核心区别分析

### 1. 抽象层次不同

| 维度 | MeasureRole | Glossary + MetricExpr |
|------|-------------|----------------------|
| **抽象层** | **数据模型层** (Ontology Property) | **业务语义层** (Business Glossary) |
| **归属** | 是某个 ObjectType 的"列"（虽然是虚拟的） | 是独立的"业务概念"，不属于任何表 |
| **视角** | 面向数据工程师：这个实体"有哪些属性" | 面向业务分析师：这个指标"如何计算" |

**类比**:
- MeasureRole: 类似 ORM 中的 `@OneToMany` + `@Aggregated` 注解
- Glossary: 类似数据仓库中的"指标字典"

### 2. 配置复杂度不同

| 维度 | MeasureRole | Glossary + MetricExpr |
|------|-------------|----------------------|
| **声明式程度** | **高度声明式**（链路 + 聚合函数） | **命令式** (直接写 SQL) |
| **灵活性** | 低（只能表达简单的"沿链路聚合"） | 高（可以是任意复杂的 SQL 表达式） |
| **可维护性** | 高（结构化元数据，易于校验） | 中（SQL 字符串，需要手工验证） |

**示例对比**:

**简单场景** (客户的订单总额):
```java
// MeasureRole: 3 行配置
linkers = [customer_orders]
aggregation = Sum(order_amount)
unit = USD

// Glossary: 直接写 SQL
sql = "SUM(o.order_amount)"
```
✅ **MeasureRole 更简洁**

**复杂场景** (客户终身价值 = 订单总额 × 毛利率 - 退款):
```java
// MeasureRole: 无法表达（不支持复杂公式）
❌ 无法配置

// Glossary: 直接写 SQL
sql = "SUM(o.order_amount) * 1.2 - SUM(o.refund_amount)"
```
✅ **Glossary 更灵活**

### 3. LLM 生成 SQL 的方式不同

| 维度 | MeasureRole | Glossary + MetricExpr |
|------|-------------|----------------------|
| **LLM 的任务** | **推断** SQL（理解链路 → 生成 JOIN + 聚合） | **替换** SQL（识别术语 → 直接使用 SQL 模板） |
| **对 LLM 能力要求** | 高（需要理解 derived property 语义） | 低（只需识别术语匹配） |
| **生成稳定性** | 中（LLM 可能生成错误的 JOIN） | 高（SQL 是人工预定义的） |
| **错误调试难度** | 高（LLM 黑盒推断） | 低（直接检查 SQL 模板） |

**MeasureRole 的挑战**:
```markdown
# Prompt 中展示
- total_order_amount : decimal [Measure] agg=Sum — 通过 customer_orders 链路聚合

# LLM 需要推断:
1. customer_orders 是什么？（需要检索到 Linker 定义）
2. 如何生成 JOIN？（Customer.customer_id = Order.customer_id）
3. 如何聚合？（SUM(o.order_amount)）
```

**Glossary 的优势**:
```markdown
# Prompt 中展示
- **客户终身价值**: `SUM(o.order_amount) * 1.2 - SUM(o.refund_amount)`

# LLM 只需识别术语，直接复制 SQL
```

### 4. 适用场景不同

#### MeasureRole 适合的场景

✅ **1. 简单的"实体聚合属性"**
- 客户的订单总数（`COUNT(orders)`)
- 客户的订单总额（`SUM(order_amount)`)
- 产品的平均评分（`AVG(rating)`)

✅ **2. 属性在逻辑上"属于"某个实体**
- 这些属性在业务语义上是实体的"固有属性"
- 例如：`Customer.lifetime_order_count` 在业务上就是客户的一个"属性"

✅ **3. 聚合逻辑简单且标准化**
- 只需单一聚合函数（SUM/AVG/COUNT）
- 不涉及复杂计算公式

✅ **4. 需要跨多表传递**
- MeasureRole 支持多跳链路（最多 3 跳）
- 例如：`Customer → Order → OrderItem → Product` 四跳聚合产品销量

#### Glossary + MetricExpr 适合的场景

✅ **1. 复杂的业务指标**
- 涉及多个字段的复杂公式
- 例如：`GMV = SUM(订单金额) - SUM(退款) + SUM(运费)`

✅ **2. 独立的业务概念**
- 不属于某个特定实体，而是跨实体的业务指标
- 例如：`月活跃用户数`, `同比增长率`

✅ **3. 带有业务规则的计算**
- 包含条件筛选、时间窗口、业务系数
- 例如：`近 30 天活跃用户 = COUNT(DISTINCT user_id) WHERE last_login > NOW() - 30`

✅ **4. 需要同义词支持**
- 业务部门使用多种叫法
- 例如：`CLV = lifetime value = 客户终身价值`

✅ **5. SQL 优化需求**
- 预定义的 SQL 可以手工优化（如使用窗口函数、子查询）
- MeasureRole 生成的 SQL 依赖 LLM，可能不够优化

---

## 设计理念对比

### MeasureRole 的设计理念

**参考**: Palantir Foundry 的 **Derived Properties**

**核心思想**:
- 虚拟属性是实体的"逻辑扩展"
- 通过声明式配置（链路 + 聚合方式）自动计算
- 对用户屏蔽底层 SQL 实现细节

**优势**:
- 结构化、类型安全
- 易于可视化（前端表单配置）
- 支持多跳链路（复杂关系图）

**劣势**:
- 表达能力有限（只能简单聚合）
- 依赖 LLM 理解链路语义
- 调试困难（LLM 黑盒推断）

### Glossary 的设计理念

**参考**: 传统数据仓库的 **指标字典** + **业务术语表**

**核心思想**:
- 业务指标是独立的"业务概念"
- 通过自然语言描述 + SQL 模板定义
- 支持同义词匹配（业务语言桥接）

**优势**:
- 灵活（可以是任意复杂的 SQL）
- 稳定（预定义 SQL，不依赖 LLM 推断）
- 易调试（SQL 显式可见）

**劣势**:
- 需要手工编写 SQL（对业务人员不友好）
- 缺乏结构化约束（SQL 字符串难以校验）
- 不支持多跳链路（需要手工写 JOIN）

---

## 实际使用建议

### 场景 1: 简单聚合属性 → 使用 MeasureRole

**问题**: 为 `Customer` 表添加"订单总数"虚拟字段

**推荐方案**: MeasureRole
```xml
<OntologyProperty>
  <name>total_orders</name>
  <roleType class="MeasureRole">
    <linkers>
      <OntologyLinkerRef>customer_orders</OntologyLinkerRef>
    </linkers>
    <aggregation class="CountAggregation"/>
  </roleType>
</OntologyProperty>
```

**为什么不用 Glossary**:
- 这是 Customer 实体的"固有属性"，不是独立的业务概念
- 聚合逻辑简单（COUNT），MeasureRole 足够

### 场景 2: 复杂业务指标 → 使用 Glossary

**问题**: 定义"客户终身价值"指标

**推荐方案**: Glossary + MetricExpr
```xml
<OntologyGlossary>
  <term>客户终身价值</term>
  <synonyms>
    <string>CLV</string>
    <string>lifetime value</string>
  </synonyms>
  <target class="GlossaryTargetMetricExpr">
    <sql><![CDATA[
      SUM(o.order_amount) * 1.2 - COALESCE(SUM(r.refund_amount), 0)
    ]]></sql>
  </target>
</OntologyGlossary>
```

**为什么不用 MeasureRole**:
- 计算公式复杂（涉及毛利率系数、退款处理）
- 不是简单的单表聚合
- 是独立的业务概念，不仅属于 Customer

### 场景 3: 两者结合使用

**真实业务场景**: 电商系统

**数据模型**:
```
Customer (客户)
  ├─ customer_id (PK)
  ├─ customer_name
  └─ [MeasureRole] total_orders: COUNT(Order) via customer_orders
  └─ [MeasureRole] total_spent: SUM(Order.amount) via customer_orders

Order (订单)
  ├─ order_id (PK)
  ├─ customer_id (FK)
  └─ order_amount

Glossary (业务术语)
  - 高价值客户: total_spent > 10000 AND total_orders > 50
  - 客户流失风险: DATEDIFF(NOW(), last_order_date) > 180
```

**ChatBI 查询示例**:
```
用户问: "查询高价值客户的数量"

LLM 生成:
SELECT COUNT(*) AS high_value_customer_count
FROM (
  SELECT c.customer_id,
         COUNT(o.order_id) AS total_orders,
         SUM(o.order_amount) AS total_spent
  FROM Customer c
  JOIN Order o ON c.customer_id = o.customer_id
  GROUP BY c.customer_id
) AS customer_metrics
WHERE total_spent > 10000 AND total_orders > 50;
```

**数据来源**:
- `total_orders` / `total_spent`: 从 MeasureRole 理解如何生成聚合
- `高价值客户` 定义: 从 Glossary 获取业务规则

---

## MeasureRole 存在的意义

回到你的问题：**既然 Glossary 也能实现跨表聚合，为什么还需要 MeasureRole？**

### 核心答案

**MeasureRole 和 Glossary 解决的是不同层次的问题**:

1. **MeasureRole** 解决的是 **"数据模型完整性"** 问题
   - 让本体模型更接近业务概念模型
   - 客户"有"订单总额这个属性（逻辑上）
   - 即使物理表中没有这个字段，本体层应该能表达

2. **Glossary** 解决的是 **"业务语言映射"** 问题
   - 用户说的话 ↔ SQL 的桥接
   - "活跃用户" 这个术语对应什么 SQL
   - 不是某个表的"列"，而是独立的"概念"

### 类比理解

**MeasureRole** = ORM 中的 `@OneToMany` + 聚合
```java
// 在 Java ORM 中
@Entity
public class Customer {
    @Id
    private Long customerId;
    
    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
    
    // Derived property (MeasureRole)
    @Formula("SELECT SUM(o.amount) FROM orders o WHERE o.customer_id = customer_id")
    private BigDecimal totalOrderAmount;
}
```

**Glossary** = 数据字典中的"业务术语"
```yaml
# 数据字典
glossary:
  - term: "高价值客户"
    definition: "订单总额超过 10000 且订单数超过 50 的客户"
    sql: "SELECT * FROM customer_metrics WHERE total_spent > 10000 AND total_orders > 50"
```

### 为什么不能只用 Glossary？

1. **语义归属问题**:
   - `Customer.total_order_amount` 在业务语义上是客户的"属性"
   - 如果只用 Glossary，它变成了一个"游离的概念"
   - 破坏了实体-属性的语义结构

2. **可维护性问题**:
   - MeasureRole 是结构化配置，前端可以做表单校验
   - Glossary 的 SQL 是字符串，难以校验和重构

3. **复用性问题**:
   - MeasureRole 定义一次，所有涉及 Customer 的查询都能自动使用
   - Glossary 是独立术语，需要用户主动提到这个术语

### 为什么不能只用 MeasureRole？

1. **表达能力不足**:
   - 复杂公式（毛利率、同比增长）无法用链路+聚合表达
   - 需要灵活的 SQL 表达式

2. **跨实体指标**:
   - "平台整体 GMV" 不属于任何一个实体
   - 需要 Glossary 作为独立概念

3. **业务语言桥接**:
   - 同义词支持（CLV = lifetime value）
   - 业务描述（自然语言解释）

---

## 总结对比表

| 维度 | MeasureRole | Glossary + MetricExpr |
|------|-------------|----------------------|
| **层次** | 数据模型层（Ontology Property） | 业务语义层（Business Glossary） |
| **归属** | 属于某个 ObjectType | 独立的业务概念 |
| **配置方式** | 声明式（链路 + 聚合函数） | 命令式（SQL 字符串） |
| **灵活性** | 低（只能简单聚合） | 高（任意 SQL） |
| **可维护性** | 高（结构化配置） | 中（字符串难校验） |
| **LLM 任务** | 推断 SQL（理解链路） | 替换 SQL（识别术语） |
| **生成稳定性** | 中（依赖 LLM 推断） | 高（预定义 SQL） |
| **适用场景** | 简单聚合属性、实体固有属性 | 复杂指标、跨实体概念 |
| **同义词支持** | 无 | 有 |
| **多跳链路** | 支持（最多 3 跳） | 不支持（需手工写 JOIN） |

---

## 推荐的使用策略

### 阶段 1: 优先使用 MeasureRole
- 为主要实体定义常见的聚合属性
- 例如：Customer.total_orders, Product.avg_rating

### 阶段 2: 补充 Glossary
- 为复杂业务指标定义 Glossary
- 例如：CLV, GMV, DAU

### 阶段 3: 两者协同
- MeasureRole 提供基础聚合属性
- Glossary 基于这些属性定义更高层的业务指标
- 例如：Glossary "高价值客户" 引用 MeasureRole 的 total_spent

---

## 未来优化方向

### 短期（已具备基础）
- ✅ MeasureRole 同步到 Neo4j (`p.role`, `p.agg`)
- ✅ Glossary 同步到 Neo4j (`Glossary` 节点)
- ✅ GraphRAG 检索两者并传递给 LLM

### 中期（增强表达能力）
- ⏳ MeasureRole 支持"计算表达式"（不只是简单聚合）
  - 例如：`SUM(order_amount) * 1.2`（乘以毛利率）
- ⏳ Glossary 支持"参数化 SQL"
  - 例如：`活跃用户(天数) = COUNT(*) WHERE last_login > NOW() - {days}`

### 长期（智能融合）
- 🔮 LLM 自动推断：简单查询用 MeasureRole，复杂查询用 Glossary
- 🔮 自动生成：从 Glossary SQL 反推可能的 MeasureRole 配置
- 🔮 智能校验：检测 Glossary SQL 是否与 MeasureRole 定义一致

---

## 相关文档
- [语义角色使用分析](./semantic-role-usage-analysis.md)
- [本体层扩展设计](./01-ontology-extension.md)
- [GraphRAG 检索机制](../ontology/graphrag-design.md)

---

**文档生成时间**: 2026-06-26  
**TIS 版本**: 4.3.0  
**作者**: Claude Code
