## Why

TIS 当前使用的 Jenkins Reactor 任务调度机制存在技术老旧、单机限制、缺乏可视化和故障恢复能力弱等问题。需要引入现代化的 DAG 调度系统，实现统一集群架构，支持定时调度、弹性伸缩、故障容错和可视化监控，以满足企业级数据集成场景的需求。

## What Changes

- 引入 PowerJob DAG 核心类替代 Jenkins Reactor，提供完善的 DAG 算法（环检测、就绪节点计算、拓扑排序）
- 采用 Akka Actor System 作为任务执行框架，实现异步消息驱动的任务调度
- 实现统一集群架构，支持从单节点到多节点的零配置扩展
- 新增定时调度功能，支持 Cron 表达式配置
- 新增 DAG 执行状态可视化监控界面
- 扩展数据库表结构：workflow 表新增调度配置字段，workflow_build_history 表新增运行时状态字段，新建 dag_node_execution 表记录节点执行详情
- 新增 Akka、Kryo、Quartz 等依赖
- 新增 application.conf 集群配置文件
- DAG 定义采用文件存储（JSON 格式），支持版本管理和 Git 集成

## Capabilities

### New Capabilities

- `dag-algorithm`: DAG 拓扑计算和验证能力，包括环检测、就绪节点计算、拓扑排序等核心算法
- `akka-task-execution`: 基于 Akka Actor System 的任务调度和执行能力，支持异步消息驱动和集群路由
- `unified-cluster-architecture`: 统一集群架构配置和管理能力，支持单节点到多节点的零配置扩展
- `scheduled-workflow-trigger`: 工作流定时调度触发能力，支持 Cron 表达式配置
- `dag-execution-monitoring`: DAG 执行状态可视化监控能力，实时查看等待队列和执行队列
- `workflow-persistence`: 工作流定义和运行时状态持久化能力，包括 DAG 定义文件存储和数据库状态管理
- `node-failure-strategy`: DAG 节点失败处理策略能力，支持失败跳过或终止后续任务

### Modified Capabilities

<!-- 无现有能力需要修改 -->

## Impact

**核心模块**:
- `tis-dag`: 核心改造模块，替换 TISReactor 实现，引入 PowerJob DAG 和 Akka Actor System
- `tis-console`: Web 控制台，新增工作流定时调度配置和 DAG 监控界面
- `tis-sql-parser`: DAGSessionSpec 需要适配新的 DAG 模型

**数据库变更**:
- `workflow` 表：新增 `dag_spec_path`、`schedule_cron`、`enable_schedule` 字段
- `workflow_build_history` 表：新增 `dag_runtime`、`wf_context`、`instance_status` 字段
- 新建 `dag_node_execution` 表：记录节点执行详情

**依赖变更**:
- 新增 Akka (2.6.x)：Actor System 和集群管理
- 新增 Kryo (5.x)：高性能序列化
- 新增 Quartz (2.3.x)：定时调度
- 新增 Micrometer：指标采集和监控
- 保留 Jenkins Reactor 依赖以支持平滑迁移

**配置文件**:
- 新增 `application.conf`：Akka 集群配置
- 新增 `${TIS_HOME}/workflow/` 目录：存储 DAG 定义文件

**部署方式**:
- 支持单机部署（单节点集群）
- 支持 Docker 容器化部署
- 支持 Kubernetes 集群部署
- 新节点通过环境变量 `AKKA_SEED_NODES` 自动加入集群

**向后兼容性**:
- 保留现有 TISReactor 接口，支持渐进式迁移
- 现有工作流可继续使用旧机制，新工作流使用新机制
