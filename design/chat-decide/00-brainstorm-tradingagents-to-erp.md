# ChatDecide：用 TradingAgents 思路 × TIS 本体语义层，构建企业 ERP 杀手级 Agent 应用

> 本文件是**头脑风暴 + 架构定位**草案，不是编码实施方案。
> 目的：在 TIS 完成本体语义层 + ChatBI + Neo4j GraphRAG 之后，提出下一阶段的产品方向候选，并推荐其中一个作为重点。

- **作者**：百岁
- **创建日期**：2026/05/31
- **状态**：草案 / 待评审
- **关联设计**：
  - `../chat-bi/00-overview.md`（ChatBI 总览）
  - `../chat-bi/06-neo4j-ontology-sync.md`（本体同步到 Neo4j）
  - `../ontology/ontology-roadmap.md`（本体路线图）

---

## 1. Context：为什么提出这个方向

用户观察到一个本质洞见：**股票交易决策**与**企业经营决策**在结构上同构 —— 都是基于多源、异构、不完全信息，由不同角色（分析师 / 研究员 / 风险官 / 决策者）协作产出可执行的"做 / 不做 / 观望"行动。差别只在于：

| 维度 | 股票交易 | 企业 ERP 决策 |
|---|---|---|
| 频率 | 日级 / 分钟级 | 周 / 月 / 事件触发 |
| 决策空间 | 买 / 卖 / 持有 | 采购 / 定价 / 补货 / 续约 / 信贷 / 合规 / … |
| 反馈周期 | 数天可计算 alpha | 数周到数季度可结算 |
| 数据源 | 公网（行情、财报、新闻、社交） | 内网 ERP（订单、库存、应收、合同、主数据） + 公网情报 |

[TradingAgents](https://github.com/TauricResearch/TradingAgents) 已经把"多 Agent 分工 + 辩论 + 反思记忆"这套模式沉淀成了可工程化的开源框架。TIS 本期完成的本体语义层 + Neo4j GraphRAG + ChatBI + DataX，恰好把企业 ERP 数据"语义化、图谱化、可被 LLM 理解地暴露出来" —— 这是反哺 TradingAgents 思路所需要的最后一块拼图。

本文目的：

1. 拆解 TradingAgents 的核心可复用抽象
2. 盘点 TIS 本体语义层提供的"决策基础设施"
3. 头脑风暴 8 个候选企业级场景
4. 推荐核心定位：**ChatDecide**（ChatBI 之上的决策委员会层）
5. 给出与 TIS 现有抽象的接入点和落地节奏

---

## 2. TradingAgents 核心抽象（已读源码确认）

定位文件：`/opt/misc/TradingAgents/tradingagents/`

| 抽象 | 文件 | 一句话 |
|---|---|---|
| **多 Analyst 团队** | `agents/analysts/{market,news,sentiment,fundamentals}_analyst.py` | 4 个领域分析师并行采集 + 输出领域报告 |
| **Researcher 多空辩论** | `agents/researchers/{bull,bear}_researcher.py` | 围绕分析师报告，多轮结构化辩论 |
| **Research Manager 仲裁** | `agents/managers/research_manager.py` | 给辩论盖棺定论，产出 investment_plan |
| **Trader 决策** | `agents/trader/trader.py` | 把 plan 转成具体交易计划 |
| **Risk 三方辩论** | `agents/risk_mgmt/{aggressive,conservative,neutral}_debator.py` | 激进 / 保守 / 中性三视角再辩一轮 |
| **Portfolio Manager 终裁** | `agents/managers/portfolio_manager.py` | 写入历史记忆，产出 final_trade_decision |
| **状态机编排** | `graph/trading_graph.py` + `graph/setup.py` | LangGraph 把上述节点编排为有向图 |
| **Reflection** | `graph/reflection.py` | 决策落地 N 天后回看 alpha，写一段反思 → 记忆库 |
| **Memory Log** | `agents/utils/memory.py`（`~/.tradingagents/memory/trading_memory.md`） | 跨 run 持久化决策 + 反思，下次同标的复用 |

**真正可复用到 ERP 的 5 个核心抽象**：

1. **角色专业化** —— 每个 agent 只看自己领域的数据，prompt 里只放本领域上下文
2. **多空 / 多视角辩论** —— 防止单 LLM 一次性总结导致的信息坍塌，逼模型把对立证据都摆出来
3. **Manager 仲裁** —— 辩论后必须有人拍板，否则永远开放循环
4. **Risk 委员会** —— 在执行前再用激进 / 保守 / 中性三个温度过一遍，专治 LLM 的过度自信
5. **延迟反思 + 长期记忆** —— 决策后回看真实结果，把"哪部分对、哪部分错"写回记忆，下次同场景注入

---

## 3. TIS 本体语义层提供的"决策基础设施"

定位文件：`tis-solr/tis-plugin/.../ontology/` (抽象层) + `plugins/tis-ontology-plugin/.../ontology/` (实现层 + 同步)

| 能力 | 关键文件 | 给 Agent 决策提供什么 |
|---|---|---|
| 业务对象 / 属性 / 关系 | `OntologyObjectType.java`、`OntologyProperty.java`、`OntologyLinker.java` | 让 LLM 用"客户/订单/合同"等业务概念思考，不直接看数据库表 |
| 业务术语字典 | `OntologyGlossary.java` + `impl/glossary/GlossaryTarget*.java` | 业务术语 → OT/Property/MetricExpr 的可证伪绑定 |
| 自定义指标 | `impl/glossary/GlossaryTargetMetricExpr.java` | "毛利率""DSO""GMV"等业务指标的可执行 SQL 片段 |
| 聚合算子 | `impl/aggregation/{Sum,Avg,Count,Max,Min,CountDistinct,…}Agg.java` | 统一的聚合语义，跨数据源不漂移 |
| 数据源绑定 | `impl/objtype/{ObjectTypeBinding,DataSourceBinding}.java` | OT ↔ 物理表/列的映射，DataX 把 ERP 接进来 |
| ChatBI 入口 | `EnableChatBI.java` | 自然语言 → 多表 JOIN SQL（Glossary + Linker + Metric 联动） |
| Neo4j 图谱 + 384 维向量 | `sync/{OntologyNeo4jSyncService,OntologyEmbeddingService}.java` | GraphRAG：路径检索 + 向量召回 + 邻居推理 |
| 数据集成框架 | `tis-plugin/.../datax/` (`IDataxReader`、`IDataxWriter`、`DataXJobSubmit`...) | 把 SAP / 用友 / Oracle ERP / 钉钉审批流等接进来 |

**一句话**：TIS 已经把 ERP "语义化 + 可查询 + 可被 LLM 检索" 这件事干完了 —— 这正是 TradingAgents 在金融领域用 yfinance / Alpha Vantage / Reddit 构建出来的能力，只不过 TIS 的对象是企业内部数据。

---

## 4. 头脑风暴：8 个候选场景

按"决策频率 × 数据闭环可量化度 × TIS 现有能力契合度"打分排序：

### A. 智能采购委员会 Agent（首推落地，与股票最同构）
- **角色**：行情分析师（原料公开价格） / 供应商分析师（历史质量、按期率） / 库存分析师（安全库存、动销） / 财务分析师（现金流、付款条件） → 多空辩论（现在采 vs 等等再采） → 风险委员会 → CFO 终裁
- **TIS 复用**：`Supplier / Material / PurchaseOrder / GoodsReceipt` ObjectType + `供应商-原料-订单` Linker + Glossary 里的"到货及时率""跌价损失"等指标
- **反思闭环**：决策 N 周后看实际采购成本 vs 当时预测，alpha = 决策方案 vs "维持上期"基线
- **杀手感**：制造业 / 贸易公司每天都在做的高价值决策，且决策结果可量化结算

### B. 应收账款 / 客户信用风险 Agent
- **角色**：账龄分析师 / 客户经营舆情师（公网+合同执行情况）/ 行业宏观分析师 / 法务风险师 → 三方辩论（继续放账 vs 收紧 vs 暂停发货）→ 风控委员会
- **TIS 复用**：`Customer / Invoice / Contract / Payment` 本体 + DataX 接 ERP 应收 + Neo4j 图谱发现"客户与上下游关联企业"传导风险
- **反思闭环**：决策后是否真的回款 / 出险 → alpha 极清晰
- **杀手感**：toB 企业 CFO 最痛的事，且数据闭环干净，特别适合做"反思记忆"积累的领域 Know-How

### C. 库存补货 / 滞销清仓 Agent
- **角色**：销售趋势分析师 / 季节性分析师 / 供应链分析师（前置期、MOQ）/ 财务分析师（资金占用） → 多空辩论（补 vs 不补 / 清 vs 不清）
- **TIS 复用**：`SKU / Warehouse / SalesOrder` + Linker + Aggregation
- **杀手感**：电商 / 零售刚需，决策频率高（日级），易回放训练

### D. 客户续约 / 流失预警 Agent
- **角色**：使用行为分析师 / 合同条款分析师 / 客户成功经理（历史工单、舆情）/ 竞品情报师 → 辩论（健康 vs 风险）→ 客户成功决策
- **TIS 复用**：`Customer / Contract / SupportTicket / UsageEvent` 图谱

### E. 动态定价 Agent
- **角色**：成本分析师 / 竞品情报师 / 客户弹性分析师 / 库存压力分析师 → 辩论（涨 / 平 / 降）

### F. 招聘录用 Agent
- **角色**：技能匹配 / 文化契合 / 薪酬情报 / 团队结构 → 辩论（offer / 不 offer / 加面）
- 数据闭环偏长，作为 PoC 不优先

### G. 合规 / 审计红蓝对抗 Agent
- **角色**：法务 / 财税 / 数据合规作为蓝队，红队 agent 主动找漏洞 → 仲裁
- 高价值但场景定制深，留给行业版

### H. 经营异常调查官（"福尔摩斯模式"）
- 触发：某个核心指标异常（毛利率掉、回款变慢）
- **角色**：多个领域分析师以"调查"模式跨域追因，Manager 给出"可能根因 Top-3 + 验证 SQL"
- **TIS 复用**：Neo4j 图谱遍历 + ChatBI SQL 生成 + Glossary 指标定义
- **杀手感**：把"老板拍着桌子问为什么"这件事变成 5 分钟内自动给出可解释的因果链

---

## 5. 推荐核心定位：**TIS ChatDecide —— 企业经营 Agent 委员会**

**核心主张（一句话）**：

> 把 TIS 的 ChatBI（"问数"）升级为 ChatDecide（"问决策"），用一套可配置的多 Agent 委员会框架，承载企业里所有"基于内部数据 + 公网情报 + 历史经验"的高价值经营决策。

### 5.1 为什么这是"杀手级"而不是"只是好用"

1. **同构于成熟范式**：TradingAgents 的论文 + 工程实现已验证多 Agent 委员会胜过单 LLM 总结，企业版只需把"yfinance"换成"TIS 本体 + ChatBI"
2. **TIS 独有壁垒**：本体 + Glossary + Metric + Linker 让 LLM 看到的不是"原始字段"而是"业务概念"，这是单纯接 LLM 的 BI 工具做不到的
3. **天然产品形态**：一个域 = 一个委员会，由 TIS 配置（不是写代码），契合 TIS 一贯的插件 / Describable 范式
4. **闭环可学习**：每次决策落地后，结果回写到 OT 上 → Reflector agent 自动产出反思 → 写入 Domain 级"经验记忆库"，下次同类决策注入。这是企业级 Know-How 沉淀，比"问一句答一句"的 ChatBI 有质的不同

### 5.2 架构草图（与 TIS 现有抽象的映射）

```
┌─────────────── ChatDecide 框架（建议位置：plugins/tis-decide-plugin） ───────────────┐
│                                                                                       │
│  Domain Committee Config (Describable)                                                │
│    ├─ Analysts: List<DecisionAnalyst>          ← 复用 LLMProvider + ChatBI 工具       │
│    ├─ Researchers: BullResearcher / BearResearcher                                    │
│    ├─ ResearchManager（仲裁）                                                          │
│    ├─ RiskCommittee（激进/保守/中性）                                                   │
│    ├─ DecisionMaker（终裁）                                                            │
│    └─ Reflector（延迟回看，写入记忆库）                                                 │
│                                                                                       │
│  每个 Analyst 的 ToolNode = {                                                         │
│      ChatBI.queryByGlossary(term, filter, agg),    // 业务术语级查询                 │
│      Neo4jGraphRAG.findRelated(node, hops, type), // 图谱推理（上下游、客户网络）    │
│      DataXExternalSource.fetch(...)               // 公网情报：行业新闻、竞品价格…    │
│  }                                                                                    │
│                                                                                       │
│  State Machine: 复用 LangGraph4j 或自研，OntologyDomainManipulate 触发                 │
│  Memory: ~/.tis/decide-memory/<domain>/<scenario>/log.md   ← 借鉴 TradingMemoryLog   │
└───────────────────────────────────────────────────────────────────────────────────────┘
```

### 5.3 与现有 TIS 体系的接入点

| ChatDecide 模块 | 接入 TIS 现有能力 | 关键文件 |
|---|---|---|
| Analyst 的数据工具 | ChatBI 自然语言查询 | `EnableChatBI.java` |
| 跨表 / 跨域推理 | Neo4j 图谱 + 384 维向量 | `OntologyNeo4jSyncService.java`, `OntologyEmbeddingService.java` |
| 业务术语统一 | Glossary + MetricExpr | `GlossaryTarget*.java` |
| ERP 数据源接入 | DataX | `IDataxReader / DataXJobSubmit.java` |
| LLM 配置 | `LLMProvider`（已有） | `EnableChatBI.java#llm` 字段 |
| 触发机制 | `OntologyDomainManipulate` | 与 `EnableChatBI` 同款 |
| 持久化 | PluginStore | 现有 |

### 5.4 落地节奏建议

- **PoC 阶段**：选 A（采购委员会）或 B（应收信用），单委员会跑通"分析 → 辩论 → 风险 → 终裁 → 反思"全链路
- **平台化**：抽出 `DecisionCommittee` Describable + `DecisionAnalyst` SPI，让客户在 TIS 控制台像配 Glossary 一样配自己的委员会
- **行业版**：制造业的"采购+库存+应收"三件套打包，作为 TIS 的高价值 SKU

---

## 6. 可能的反对意见与回应

| 质疑 | 回应 |
|---|---|
| LLM 决策不可信，企业不敢用 | 不让 LLM 做最终决策，让 LLM 做"委员会会议纪要 + 推荐方案 + 证据链"，人保留终裁权；多空辩论结构本身就提供可解释性 |
| 反思闭环周期长，启动期没有记忆数据 | 用历史 ERP 数据做"回放训练"：把过去 N 个月的真实决策作为"过去时点 + 当时已有信息"喂入框架，对比真实结果，冷启动出一份初始记忆 |
| 与现有 ChatBI 重叠 | ChatBI 是"问数"（一次 SQL），ChatDecide 是"问决策"（多轮多角色协作），后者底层调用 ChatBI 作为工具 —— 是上层应用，不是替代 |
| 工程量大 | TradingAgents 整个框架不到 5k 行 Python；TIS 已有 LLMProvider / Glossary / Neo4j / DataX 全部基础设施，工程主体是 prompt 工程 + 状态机编排 |

---

## 7. 验证方式（如要落地 PoC）

1. **环境**：TIS 本地起一个 Domain，DataX 接一份脱敏的 ERP 采购数据（供应商、原料、采购订单、收货）
2. **建本体**：在 TIS 控制台建 `Supplier / Material / PurchaseOrder / GoodsReceipt` ObjectType + Linker + Glossary（"按期率""跌价损失""安全库存天数"）
3. **配 ChatDecide**：4 个 Analyst（行情/供应商/库存/财务）+ 2 个 Researcher（建议采购 / 建议等待）+ 1 个 Manager + 3 个 Risk + 1 个 CFO
4. **回放**：跑过去 6 个月的真实采购决策，记录 framework 的推荐 vs 当时实际决策 vs 现在已知结果
5. **指标**：
   - 推荐与实际一致的比例（一致性）
   - 推荐优于实际的回看 alpha（采购成本节省 / 缺货损失避免）
   - 决策时间从"周级"压缩到"分钟级"

---

## 8. 后续工作

本文是头脑风暴 + 架构定位草案，**本期不实施**。如果未来推进 ChatDecide 方向，下一步应产出：

- `01-architecture.md`：ChatDecide 框架详细架构（Describable 体系、状态机选型、Memory 落盘方案）
- `02-poc-procurement-committee.md`：首个 PoC 场景（智能采购委员会）的完整设计
- `03-prompt-templates.md`：各角色 prompt 模板（中英双版）
- `04-replay-training.md`：基于历史 ERP 数据做回放训练的方法论

---

## 附录 A：相关源码定位

- TradingAgents：`/opt/misc/TradingAgents/`
- TIS 本体抽象层：`/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/`
- TIS 本体实现层 + Neo4j 同步：`/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/`
- TIS DataX 集成框架：`/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/datax/`