# 00 - ChatBI 总览

## 1. 背景

TIS 平台已具备：
- 基于插件 SPI 的端到端数据集成（MySQL/Doris 等 Reader/Writer）
- 本体层（Ontology）：`OntologyDomain → OntologyObjectType → OntologyProperty + OntologyLinker + OntologyValueType + OntologySharedProperty`，参照 Palantir Foundry 设计
- 完整的 `ValueConstraint` 体系（Enum/Range/Regex 等）+ 多步插件表单 + 持久化

ChatBI 应用希望让用户通过**自然语言**对 Doris MPP 数据库发起查询，由 LLM 借助本体层提供的 GraphRAG 上下文生成 Doris SQL 并执行。

## 2. 目标 (Goals)

| 编号 | 目标 | 验收口径 |
|------|------|---------|
| G1 | 自然语言 → Doris SQL 端到端可跑 | Falcon dev 集 EX@1 ≥ 60% |
| G2 | 利用本体层做 GraphRAG，避免纯 schema prompt 的幻觉 | 同基线（pure-schema prompt）相比 EX@1 提升 ≥ 8pt |
| G3 | 同步链路稳定：MySQL CDC → Doris，分钟级延迟 | 单表 100w 行端到端 P95 ≤ 2min |
| G4 | 测试自动化：可重复跑 Falcon dev/test，并产出报告 | 每次代码改动 CI 可一键回归 |

## 3. 非目标 (Non-Goals)

- 通用对话/上下文记忆（首版仅做"单轮提问 → SQL"）
- 多模态（图表生成、自然语言报表叙述）—— 后续迭代
- 联邦查询（跨 Doris/MySQL/Hive 混合 SQL）—— 仅 Doris 单库

## 4. 整体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                            用户层 (前端)                             │
│   tis-console: ChatBI 入口页 + 结果展示 + 本体管理(已有)             │
└──────────────────┬──────────────────────────────┬───────────────────┘
                   │ 自然语言 NLQ                 │ 本体维护
                   ▼                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│         ChatBI 应用层 (Java + Python，承载在 tis-ontology-plugin)    │
│  ┌────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │
│  │ NLQ Router │ │ GraphRAG 检索│ │ Prompt Build │ │ SQL 校验/执行│  │
│  └─────┬──────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘  │
│        │               │                │                │          │
│        ▼               ▼                ▼                ▼          │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │              本体层 (扩展后, 见 01-ontology-extension.md)    │    │
│  │  OntologyObjectType + Property + Linker + Glossary +        │    │
│  │  DataSourceBinding(→Doris ds) + Measure/Dimension + 向量索引 │    │
│  └─────────────────────────────────────────────────────────────┘    │
└────────────────────────┬────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       数据通路层 (TIS 插件)                          │
│   MySQL Reader/CDC ──► TIS Job ──► Doris Writer                     │
│   见 02-data-sync-mysql-to-doris.md                                 │
└─────────────────────────────────────────────────────────────────────┘
                         │
                         ▼
                ┌──────────────────┐
                │  Doris MPP (查询) │
                └──────────────────┘
```

## 5. 关键技术选型

| 维度 | 选型 | 备注 |
|------|------|------|
| 同步链路 | TIS（DataX 批 + Flink-CDC 流） | 复用现有能力 |
| 目标库 | Apache Doris | MPP，OLAP 友好 |
| LLM | 复用 TIS 现有 `LLMProvider` SPI，默认 `QWenLLMProvider`（通义千问），备选 `DeepSeekProvider` | 无需新建接入层 |
| 图存储 + 向量库 | **Neo4j 嵌入式**（5.18+，原生 HNSW 向量索引） | 同时承担图遍历 + 向量召回，已在 /opt/misc/neo4j-demo 验证 |
| Embedding | ONNX Runtime + paraphrase-multilingual-MiniLM（384 维），后续可换 bge-m3 | 中文兼容，本地推理 |
| SQL 解析 | 复用 `tis-sql-parser` 模块 + Doris 自带 parser | 校验时双轨 |
| 测试集 | Falcon (eosphoros-ai)，已克隆 `/opt/misc/Falcon` | 仅借用 dev/test 题面，目标库改 Doris |

## 6. 里程碑 (Roadmap)

| 阶段 | 时间 | 主要交付 | 关联文档 |
|------|------|----------|---------|
| M1 本体扩展 | W1-W2 | 绑定语义收敛到 Doris、Measure/Dimension 标记、Glossary 实体；本体实时同步到嵌入式 Neo4j（含向量索引） | 01 |
| M2 数据通路 | W2-W4 | MySQL → Doris 同步模板、schema 自动建表、首批 dev 库灌入 | 02 |
| M3 GraphRAG | W4-W6 | Neo4j HNSW 向量召回 + Cypher 子图扩展 + Prompt 拼装器 | 03 |
| M4 NL→SQL | W6-W8 | LLM 调用层、Doris 方言提示、AST 校验、错误回退 | 04 |
| M5 Falcon 测试 | W7-W9 | dev 集自动化跑分 + CI 回归 + 报告产出 | 05 |
| M6 上线试运行 | W10 | 内部业务 1~2 个 domain 灰度 | - |

## 7. 风险登记

| 风险 | 影响 | 缓解 |
|------|------|------|
| Falcon 原生面向 SQLite/MaxCompute 方言，迁移到 Doris 有兼容差 | 中 | 见 05 文档"方言适配"小节，必要时 SQL 改写或排除少量样本 |
| 大规模本体下 GraphRAG prompt 爆炸 | 高 | 分层检索 + 子图剪枝，限定 top-K |
| LLM 幻觉表/列名 | 高 | 强校验：只允许从子图符号集中选取 + AST 校验 + 失败重试 |
| MySQL 与 Doris 类型映射差异（DECIMAL/DATETIME/精度） | 中 | 在 02 中固化一份 type-mapping；映射写死于同步任务，不污染本体 |
| 多租户/权限缺失 | 中 | 复用 TIS 现有 AppAndRuntime + 自研列级 ACL（后续阶段） |

## 8. 参考资料

- Palantir Foundry Object/Link Type 文档：https://www.palantir.com/docs/foundry/object-link-types/
- Apache Doris 官方文档：https://doris.apache.org/
- Falcon Benchmark：https://github.com/eosphoros-ai/Falcon (论文 arXiv:2510.24762)
- 本仓库 CLAUDE.md：项目概述、构建命令
- memory: `project_ontology_enum_constraint.md`：本体层架构详情