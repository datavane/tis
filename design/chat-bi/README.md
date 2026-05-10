# TIS ChatBI 设计文档

本目录承载 TIS 平台 ChatBI 应用的需求与设计。文档按实现先后拆为多个子文件，前缀数字越小越靠前。

## 文档索引

| 序号 | 文档 | 主题 | 阶段 |
|------|------|------|------|
| 00 | [00-overview.md](./00-overview.md) | 项目总览、范围、技术栈、里程碑 | 立项 |
| —  | [ontology.md](./ontology.md) | 本体层 UI 设计（已存在，沿用） | 已完成/迭代中 |
| 01 | [01-ontology-extension.md](./01-ontology-extension.md) | 面向 ChatBI 的本体扩展（绑定语义收敛到 Doris、Measure/Dimension、Glossary） | 阶段一 |
| 02 | [02-data-sync-mysql-to-doris.md](./02-data-sync-mysql-to-doris.md) | MySQL → Doris 数据通路（基于 TIS 流批一体） | 阶段二 |
| 03 | [03-graphrag-retrieval.md](./03-graphrag-retrieval.md) | GraphRAG 检索层（子图召回、向量索引、上下文裁剪） | 阶段三 |
| 04 | [04-nl-to-sql.md](./04-nl-to-sql.md) | NL→SQL 生成、Doris 方言、校验闭环 | 阶段四 |
| 05 | [05-falcon-test-plan.md](./05-falcon-test-plan.md) | 基于 Falcon 数据集的测试方案与回归体系 | 阶段五 |
| 06 | [06-neo4j-ontology-sync.md](./06-neo4j-ontology-sync.md) | 本体同步到 Neo4j 图数据库（图模型、Embedding、HNSW 向量索引、同步服务） | 阶段一附属 |

## 阅读顺序建议

1. 先读 00 总览理解整体架构与目标
2. 按 01 → 02 → 03 → 04 顺序了解技术栈逐层叠加
3. 05 是测试方案，可在阶段三之后并行启动（先用 Falcon 跑通基线，再回流指导各阶段调优）

## 文档约定

- 所有路径基于仓库根 `/Users/mozhenghua/j2ee_solution/project/tis-solr`（除非另有说明）
- 涉及插件代码默认放在 `tis-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/...`
- 涉及前端代码默认放在 `/Users/mozhenghua/j2ee_solution/project/tis-console/src/...`
- 大插件落 `/Users/mozhenghua/j2ee_solution/project/plugins/...`
- **ChatBI 主插件工程**：`tis-ontology-plugin`，绝对路径 `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/`。后续 tis-solr 中有关 ontology 的大部分脚本会迁移到该插件工程中
- **ChatBI 测试与评测脚本**统一放在 `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/tool/falcon-eval/`（落地时新建）
- Falcon 数据集已克隆到 `/opt/misc/Falcon`