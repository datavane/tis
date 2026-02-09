## Context

TIS 当前使用 Jenkins Reactor (org.jenkins-ci:task-reactor:1.5) 作为 DAG 任务调度引擎，存在以下限制：
- 基于内存的单机执行模型，无法横向扩展
- 缺乏现代化的 DAG 算法支持（环检测、拓扑排序等）
- 没有定时调度和可视化监控能力
- 故障恢复机制较弱

本次改造需要在保持向后兼容的前提下，引入现代化的 DAG 调度系统，实现统一集群架构。

**约束条件**：
- TIS-Console 为单实例部署，无需考虑多实例共享状态
- 需要支持从单节点到多节点的平滑扩展，无需修改配置或重启服务
- 现有工作流需要能够继续运行，支持渐进式迁移
- 数据库使用 MySQL + MyBatis

## Goals / Non-Goals

**Goals:**
- 引入 PowerJob DAG 核心类，提供完善的 DAG 算法能力
- 采用 Akka Actor System 实现统一集群架构，支持单节点到多节点的零配置扩展
- 实现工作流定时调度功能（Cron 表达式）
- 新增 DAG 执行状态可视化监控界面
- 支持节点失败处理策略（跳过或终止）
- DAG 定义采用文件存储，支持版本管理和 Git 集成

**Non-Goals:**
- 不替换现有的 DataX/Flink-CDC 数据同步引擎
- 不实现分布式事务协调
- 不支持跨数据中心的工作流调度
- 不实现工作流的可视化编排界面（本次仅实现执行监控）

## Decisions

### 决策 1: 选择 PowerJob DAG 核心类而非自研

**选择**: 直接使用 PowerJob 的 DAG 核心类（PEWorkflowDAG、WorkflowDAG、WorkflowDAGUtils）

**理由**:
- PowerJob 的 DAG 设计经过大规模生产验证，算法完善（环检测、就绪节点计算、拓扑排序）
- 双重数据模型设计优雅：点线表示法（可序列化）+ 引用表示法（运行时）
- 支持丰富的流程控制：节点禁用、失败跳过、控制节点
- 开源项目，社区活跃，可持续维护

**替代方案**:
- 自研 DAG 算法：开发成本高，需要大量测试验证，不如使用成熟方案
- 使用 Apache Airflow：过于重量级，需要独立部署，不符合 TIS 的轻量化定位

### 决策 2: 选择 Akka Actor System 作为任务执行框架

**选择**: 使用 Akka 2.6.x 的 Actor System 和 Cluster 功能

**理由**:
- **统一架构**: 单节点集群和多节点集群使用相同的代码和配置，通过 ClusterRouterPool 自动适配
- **零配置扩展**: 新节点通过环境变量 AKKA_SEED_NODES 指向 seed node 即可自动加入集群
- **容错机制**: 内置 Supervisor 策略，支持故障自动恢复
- **消息驱动**: Actor 模型天然支持异步消息处理，适合任务调度场景
- **轻量级**: 相比 Kubernetes Job 或 Spark，Akka 更轻量，适合 TIS 的部署场景

**替代方案**:
- 继续使用 Jenkins Reactor：无法解决单机限制和扩展性问题
- 使用 Spring @Async + 线程池：无法实现集群路由和故障恢复
- 使用消息队列（RabbitMQ/Kafka）：需要额外部署中间件，增加运维复杂度

### 决策 3: DAG 定义采用文件存储而非数据库存储

**选择**: DAG 定义存储在 `${TIS_HOME}/workflow/<workflow-name>/dag-spec.json`

**理由**:
- **开发友好**: 可直接用文本编辑器查看和修改，无需数据库工具
- **版本管理**: 原生支持 Git 版本控制，便于追溯历史变更
- **性能**: 读取大型 DAG 定义比数据库 TEXT 字段更快
- **架构一致**: 与 WorkFlow 现有的 gitPath 字段保持一致
- **TIS-Console 单实例**: 无需考虑多实例共享文件的问题

**替代方案**:
- 数据库 TEXT 字段：大 TEXT 字段 I/O 慢，不支持版本管理，开发不友好

### 决策 4: 使用 Kryo 进行 Actor 消息序列化

**选择**: 使用 Kryo 5.x 作为 Akka 消息序列化框架

**理由**:
- 高性能：比 Java 原生序列化快 10 倍以上
- 紧凑：序列化后的字节数更少，减少网络传输开销
- 无需实现 Serializable 接口：通过注册机制支持任意类

**替代方案**:
- Java 原生序列化：性能差，序列化后体积大
- Protobuf：需要定义 .proto 文件，开发成本高

### 决策 5: 使用 Quartz 实现定时调度

**选择**: 使用 Quartz 2.3.x 实现工作流的定时调度

**理由**:
- 成熟稳定：Quartz 是 Java 生态中最成熟的定时调度框架
- Cron 表达式支持：完整支持标准 Cron 表达式
- 持久化支持：支持将调度任务持久化到数据库，重启后自动恢复
- 集群支持：支持多节点部署，避免重复触发

**替代方案**:
- Spring @Scheduled：功能较弱，不支持动态添加/删除任务
- 自研定时器：开发成本高，不如使用成熟方案

### 决策 6: Actor 体系结构设计

**选择**: 采用四层 Actor 架构

```
ActorSystem: TIS-DAG-System
├─ DAGSchedulerActor: 工作流生命周期管理，DAG 流转控制
├─ DAGMonitorActor: 实时监控，状态查询，队列统计
├─ ClusterManagerActor: 集群成员管理，故障恢复
└─ NodeDispatcherActor: 任务分发路由，超时监控
    └─ TaskWorkerActor Pool: 实际执行任务
```

**理由**:
- **职责分离**: 每个 Actor 职责单一，便于维护和测试
- **并发控制**: DAGSchedulerActor 单线程处理消息，配合数据库行锁，避免并发冲突
- **路由灵活**: NodeDispatcherActor 使用 ClusterRouterPool，自动适配单节点和多节点场景
- **监控独立**: DAGMonitorActor 独立处理查询请求，不影响调度性能

### 决策 7: 数据库表设计

**选择**: 扩展现有表 + 新建节点执行详情表

- `workflow` 表：新增 `dag_spec_path`、`schedule_cron`、`enable_schedule` 字段
- `workflow_build_history` 表：新增 `dag_runtime`、`wf_context`、`instance_status` 字段
- 新建 `dag_node_execution` 表：记录每个节点的执行详情

**理由**:
- **最小侵入**: 尽量扩展现有表，减少对现有代码的影响
- **细粒度监控**: dag_node_execution 表记录每个节点的执行信息，支持按状态查询等待队列和执行队列
- **故障恢复**: worker_address 字段记录任务执行位置，便于节点故障后的任务恢复

## Risks / Trade-offs

### 风险 1: Akka 学习曲线

**风险**: 团队对 Akka Actor 模型不熟悉，可能导致开发效率降低

**缓解措施**:
- 提供详细的开发文档和示例代码
- 核心 Actor 由架构师实现，业务开发人员只需实现 DataflowTask
- 引入 Akka 专家进行 Code Review

### 风险 2: PowerJob 类依赖冲突

**风险**: PowerJob 的依赖可能与 TIS 现有依赖冲突

**缓解措施**:
- 只引入 PowerJob 的 DAG 核心类（约 10 个类），不引入整个 PowerJob 框架
- 使用 Maven Shade Plugin 重命名包名，避免冲突
- 充分测试依赖兼容性

### 风险 3: 文件存储的并发写入

**风险**: 虽然 TIS-Console 是单实例，但多线程可能同时写入同一个 DAG 文件

**缓解措施**:
- WorkflowDAGFileManager 使用 synchronized 方法保证线程安全
- 文件写入使用原子操作（先写临时文件，再重命名）
- 数据库 workflow 表的 dag_spec_path 字段作为权威数据源

### 风险 4: 集群脑裂

**风险**: Akka Cluster 在网络分区时可能出现脑裂

**缓解措施**:
- 配置 Akka Split Brain Resolver（使用 keep-majority 策略）
- 数据库行锁作为最终一致性保证
- 监控集群成员状态，及时发现异常

### 风险 5: 向后兼容性

**风险**: 新架构可能导致现有工作流无法运行

**缓解措施**:
- 保留 TISReactor 接口，现有工作流继续使用旧机制
- 新工作流通过 dag_spec_path 字段标识，使用新机制
- 提供迁移工具，将旧工作流转换为新格式

### Trade-off 1: 文件存储 vs 数据库存储

**选择**: 文件存储

**优势**: 开发友好、支持版本管理、性能更好
**劣势**: 多实例部署需要共享存储（但 TIS-Console 是单实例，无此问题）

### Trade-off 2: Akka vs 消息队列

**选择**: Akka

**优势**: 轻量级、统一架构、零配置扩展
**劣势**: 学习曲线较陡，团队需要时间适应

### Trade-off 3: 引入 PowerJob 类 vs 自研

**选择**: 引入 PowerJob 类

**优势**: 快速实现、算法成熟、经过生产验证
**劣势**: 引入外部依赖，可能存在依赖冲突

## Migration Plan

### 阶段 1: 基础设施准备（第 1-2 周）

1. 引入依赖：Akka、Kryo、Quartz、PowerJob DAG 核心类
2. 数据库变更：执行 DDL 脚本，扩展表结构
3. 创建 `${TIS_HOME}/workflow/` 目录
4. 配置 Akka：创建 application.conf

### 阶段 2: 核心功能实现（第 3-6 周）

1. 实现 WorkflowDAGFileManager（DAG 文件管理）
2. 实现 DAGSchedulerActor（工作流调度）
3. 实现 NodeDispatcherActor（任务分发）
4. 实现 TaskWorkerActor（任务执行）
5. 实现 DAGMonitorActor（监控查询）
6. 实现 ClusterManagerActor（集群管理）

### 阶段 3: 定时调度和监控（第 7-8 周）

1. 集成 Quartz，实现定时调度
2. 实现 Web 监控界面（等待队列、执行队列）
3. 实现工作流配置界面（Cron 表达式配置）

### 阶段 4: 测试和优化（第 9-10 周）

1. 单元测试：各 Actor 的消息处理逻辑
2. 集成测试：完整工作流执行流程
3. 集群测试：单节点到多节点扩展
4. 性能测试：并发工作流执行
5. 故障测试：节点宕机、网络分区

### 阶段 5: 灰度发布（第 11-12 周）

1. 选择 1-2 个非核心工作流进行灰度
2. 监控执行情况，收集反馈
3. 修复发现的问题
4. 逐步扩大灰度范围

### 回滚策略

- 保留 TISReactor 代码，随时可以切换回旧机制
- 数据库变更使用 ALTER TABLE ADD COLUMN，不删除现有字段
- 新增的表可以直接 DROP，不影响现有功能

## Open Questions

1. **是否需要支持工作流的可视化编排界面？**
   - 当前设计只实现执行监控，不包含拖拽式编排
   - 如果需要，可以在后续版本中基于 React Flow 实现

2. **是否需要支持工作流的版本回滚？**
   - 当前设计支持 Git 版本管理，但没有实现一键回滚功能
   - 如果需要，可以在 Web 界面中添加版本选择和回滚按钮

3. **是否需要支持跨数据中心的工作流调度？**
   - 当前设计假设所有节点在同一数据中心
   - 如果需要跨数据中心，需要考虑网络延迟和数据一致性问题

4. **是否需要支持工作流的优先级调度？**
   - 当前设计按照 FIFO 顺序调度工作流
   - 如果需要优先级，可以在 DAGSchedulerActor 中引入优先级队列

5. **Akka Cluster 的 seed nodes 如何配置？**
   - 单节点部署：seed-nodes 指向自己
   - 多节点部署：通过环境变量 AKKA_SEED_NODES 指向第一个节点
   - 是否需要支持动态 seed nodes 发现（如通过 Kubernetes Service）？
