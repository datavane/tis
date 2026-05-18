# TIS Ontology 层能力拓展路线图

## 定位

TIS Ontology 层是一个**语义元数据层**，用结构化、可插拔的方式描述"数据是什么"以及"数据之间的关系"。
它是大模型在大数据领域落地的关键基础设施——一旦建好，能力远不止 ChatBI。

## 已完成的基础

| 模块 | 说明 |
|------|------|
| OntologyObjectType | 本体对象类型定义 |
| OntologyProperty + PropertyRoleType | 属性 + 语义角色（Identifier/Dimension/TimeDimension/Measure/Unknown） |
| OntologyLinker + OntologyLinkerRef | 对象间关系（支持多跳链路） |
| AggregationKind（9 种） | 聚合方式抽象（Count/Sum/Avg/Min/Max/ApproxCardinality/CountDistinct/CollectList/CollectSet） |
| MeasureRole derived property | 度量角色承载 link 链路 + 聚合 + 单位/精度 |

## 可拓展的应用场景

| 场景 | 如何利用 Ontology |
|------|-------------------|
| ChatBI（自然语言查询） | 大模型根据 ontology 理解表结构和语义，生成正确的 SQL/指标 |
| 数据产品 / 数据目录 | ontology 天然就是 data catalog 的骨架，提供业务语义描述 |
| 自动化数据治理 | 基于 role + link 做血缘分析、影响评估、质量规则推导 |
| 智能 ETL 编排 | 大模型根据 ontology 推断 source→target 字段映射 |
| 跨域数据联邦 | 通过 linker 描述跨系统实体关系，实现联合查询 |
| 指标平台 | Measure + Aggregation 直接定义指标口径，消除二义性 |

---

## 待补充的短板（按优先级排序）

### 1. 查询抽象层（优先级：高）

**现状：** 有 schema 定义（ObjectType、Property、Linker），但没有统一的"根据 ontology 生成查询"的引擎。

**目标：** 提供一个中间逻辑查询层，类似 Palantir 的 Object Set + Filter + Aggregation API。

**为什么重要：**
- ChatBI 场景下大模型可以直接生成 SQL，但其他场景（数据产品、治理）需要一个与存储无关的查询抽象
- 有了这层，大模型只需生成"语义查询意图"，由引擎翻译成具体的 SQL/API 调用
- 可以统一处理 derived property 的多跳展开逻辑

**执行架构：**

查询抽象层分为两层协作——Neo4j 负责元数据图（路径解析），底层数据引擎负责实际业务数据查询：

```
用户意图 / 大模型语义查询
        │
        ▼
┌──────────────────────────────┐
│   查询抽象层 (OntologyQuery)    │
└─────────┬────────────────────┘
          │
    ┌─────┴──────┐
    ▼            ▼
 Neo4j         数据源
(ontology      (实际业务数据:
 元数据图)      StarRocks/MySQL/Hive)
    │            │
    │ 路径解析    │ SQL/查询执行
    │ link展开    │
    └─────┬──────┘
          ▼
       合并结果
```

**Neo4j 的职责：**
- 存储 ontology 图（ObjectType 为节点、OntologyLinker 为边）
- 解析多跳 link 路径（MeasureRole 的 derived property 展开）
- 回答"两个 ObjectType 之间有哪些可达路径"这类元数据查询
- 为大模型提供 schema context（prompt 构建时查图拿相关实体）

**数据引擎的职责：**
- 执行实际的业务数据查询（订单记录、用户记录等）
- Neo4j 负责"告诉你该怎么 JOIN"，数据引擎负责"执行 JOIN"

**核心逻辑：先问 Neo4j 路径怎么走，再把路径翻译成目标引擎的 JOIN 语句。**

OntologyLinker 已经描述了对象间的关系，这些关系同步到 Neo4j 就是天然的图结构。

**查询模型：**
```
OntologyQuery {
    objectType: String           // 查询的目标 ObjectType
    filters: List<Filter>        // 属性过滤条件
    aggregations: List<Agg>      // 聚合规则（复用 AggregationKind）
    links: List<LinkTraversal>   // 关联对象展开（由 Neo4j 解析路径）
    projections: List<String>    // 返回字段
}
```

---

### 2. 版本演进机制（优先级：高）

**现状：** ObjectType 加字段、改 linker 关系后，下游依赖无法感知变更。

**目标：** 支持 schema evolution，保证 ontology 变更时下游配置不会静默失效。

**为什么重要：**
- 已保存的 derived property 引用了特定的 linker 和 property，如果被删除/重命名会断裂
- ChatBI 的 prompt 模板依赖 ontology 结构，结构变了 prompt 可能生成错误 SQL
- 多人协作时需要知道"谁改了什么，影响了哪些下游"

**设计方向：**
- 每次 ontology 变更生成 migration event（类似 DB migration）
- 提供 impact analysis：变更前预览受影响的 derived property / linker / 下游配置
- 可选：ontology 版本号，下游绑定版本

---

### 3. 事件/行为语义建模（优先级：中）

**现状：** 当前 ontology 描述的是"实体和属性"（静态结构）。

**目标：** 引入 Event 类型，支持实时/时序场景。

**为什么重要：**
- "最近一小时下单量"这类查询需要理解"下单"是一个事件，有时间戳和关联实体
- 实时数据管道（Flink）的语义描述需要事件模型
- 事件 + 实体的组合才是完整的业务语义图

**设计方向：**
- 新增 ObjectType 子类型：EntityType / EventType
- EventType 自带 timestamp 属性（TimeDimension 角色）和 actor 关联（Linker）
- 支持"事件聚合到实体"的 derived property（如：用户最近 30 天订单数）

---

### 4. 约束与业务规则层（优先级：中）

**现状：** 只有 pk / nullable 这种物理约束。

**目标：** 支持业务层面的规则表达。

**为什么重要：**
- 大模型生成查询时需要知道值域范围（避免生成无意义的 WHERE 条件）
- 数据质量治理需要规则定义（如：age 必须 > 0 且 < 150）
- 枚举约束可以帮助大模型生成更精确的筛选条件

**设计方向：**
```
PropertyConstraint implements Describable<PropertyConstraint> {
    // 子类：
    RangeConstraint      { min, max }
    EnumConstraint       { allowedValues: List<String> }
    RegexConstraint      { pattern: String }
    CrossFieldConstraint { expression: String }  // 跨属性依赖
}
```
在 OntologyProperty 上增加 `List<PropertyConstraint> constraints` 字段。

---

### 5. 权限与可见性（优先级：低）

**现状：** ontology 对所有用户完全可见，无访问控制。

**目标：** 控制"谁能看到哪些 ObjectType / Property"。

**为什么重要：**
- ontology 作为多场景共享基础设施，不同团队/角色应看到不同视图
- 敏感字段（如身份证号、薪资）需要在 ontology 层就标记访问级别
- ChatBI 场景下，权限控制可以防止大模型暴露不该看到的数据

**设计方向：**
- Property 级别增加 `accessLevel` 标记（public / internal / restricted）
- ObjectType 级别增加 owner / visibility scope
- 查询抽象层在执行时根据当前用户角色过滤可见属性

---

## 建议实施顺序

```
Phase 1（当前）: ✅ 基础 ontology schema 已完成
     │
Phase 2: 查询抽象层 ──→ 让 ontology 从"描述"变成"可执行"
     │
Phase 3: 版本演进 ──→ 保证 ontology 变更不会破坏下游
     │
Phase 4: 事件建模 + 约束规则 ──→ 补全语义表达能力
     │
Phase 5: 权限可见性 ──→ 多租户/多团队共享
```

## 现有设计优势

当前的插件化设计（Describable + Descriptor + @TISExtension）提供了很好的扩展基础：
- 新增 ObjectType 子类型（Entity/Event）不需要改现有结构
- PropertyConstraint 可以作为新的 Describable 插件接入
- 查询抽象层可以复用 AggregationKind 和 OntologyLinkerRef 的设计模式
- 所有扩展都是"加法"，不需要推翻已有实现