# TIS Workshop 设计文档

本目录包含 TIS Workshop 低代码平台的完整设计文档。

## 文档结构

### [01-feasibility-analysis.md](./01-feasibility-analysis.md)
**可行性分析报告**

内容包括：
- 可行性结论
- 现有优势分析（本体模型、插件机制、ChatBI 案例、微前端架构）
- 关键差距分析（Function/Action 实体、应用定义层、可视化构建器、工作流引擎、权限审计）
- Palantir Workshop 功能对比
- 实施建议（分阶段、先配置后可视化、复用现有能力）
- 投入产出分析
- 风险和应对

**适合阅读人群**：管理层、技术决策者

### [02-architecture-design.md](./02-architecture-design.md)
**架构设计文档**

内容包括：
- 整体架构（分层架构图）
- 核心组件设计（Function Registry、Action Engine、Application Runtime、Workflow Engine）
- 数据流设计
- 接口设计（RESTful API）
- 性能优化策略（批量计算、缓存、虚拟滚动）
- 安全设计（权限控制、审计日志、数据安全）
- 扩展性设计（插件化、事件驱动）

**适合阅读人群**：架构师、高级工程师

### [workshop-mvp-design.md](workshop-mvp-design.md)
**MVP 实施方案**（在上级目录）

内容包括：
- MVP 目标和范围
- Function 和 Action 基础框架实现
- 数据质量巡检应用示例
- 前后端代码示例
- 验收标准

**适合阅读人群**：开发工程师

### [ontology-action-function-scenarios.md](ontology-action-function-scenarios.md)
**场景应用设计**（在上级目录）

内容包括：
- Function 和 Action 概念定义
- 10 大场景应用（智能任务编排、数据质量监控、数据版本管理、资源优化、成本分析、数据治理、智能运维、ChatBI 增强）
- 具体 Function 和 Action 示例代码

**适合阅读人群**：产品经理、业务分析师

## 阅读建议

### 如果你是...

**管理层/决策者**：
1. 先读 `01-feasibility-analysis.md` 了解可行性和投入产出
2. 重点关注"可行性结论"、"投入产出分析"、"实施建议"章节

**架构师/技术负责人**：
1. 先读 `01-feasibility-analysis.md` 了解整体规划
2. 再读 `02-architecture-design.md` 了解技术架构
3. 重点关注"核心组件设计"、"性能优化"、"扩展性设计"

**产品经理/业务分析师**：
1. 先读 `ontology-action-function-scenarios.md` 了解业务场景
2. 再读 `01-feasibility-analysis.md` 了解 Palantir Workshop 对比
3. 重点关注"场景应用"、"功能对比"

**开发工程师**：
1. 先读 `workshop-mvp-design.md` 了解具体实现
2. 再读 `02-architecture-design.md` 了解整体架构
3. 重点关注代码示例和接口设计

## 快速导航

| 关心的问题 | 推荐阅读 |
|-----------|---------|
| TIS 能不能做类似 Palantir Workshop？ | `01-feasibility-analysis.md` - 可行性结论 |
| 需要投入多少资源？多久能做出来？ | `01-feasibility-analysis.md` - 投入产出分析 |
| 技术架构是怎样的？ | `02-architecture-design.md` - 整体架构 |
| Function 和 Action 怎么实现？ | `02-architecture-design.md` - 核心组件设计 |
| 有哪些应用场景？ | `ontology-action-function-scenarios.md` - 场景应用 |
| 第一个版本怎么做？ | `workshop-mvp-design.md` - MVP 实施方案 |
| 如何保证性能？ | `02-architecture-design.md` - 性能优化策略 |
| 如何保证安全？ | `02-architecture-design.md` - 安全设计 |
| 如何扩展功能？ | `02-architecture-design.md` - 扩展性设计 |

## 版本历史

- **2026-06-24**: 初始版本
  - 完成可行性分析
  - 完成架构设计
  - 完成 MVP 方案

## 相关资源

- [Palantir Foundry 官方文档](https://www.palantir.com/docs/foundry/)
- [TIS 本体系统文档](../../tis-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/)
- [TIS 插件开发文档](https://tis.pub/docs/develop/)

## 联系方式

如有疑问，请联系：
- 技术负责人：百岁 (baisui@qlangtech.com)
- 项目组邮箱：tis-dev@qlangtech.com
