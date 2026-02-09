## ADDED Requirements

### Requirement: Actor System 初始化

系统 SHALL 在启动时初始化 Akka Actor System，创建核心 Actor 并配置集群参数。

#### Scenario: 单节点部署初始化

- **WHEN** TIS 以单节点模式启动
- **THEN** 系统创建 Actor System，seed-nodes 指向自己，形成单节点集群

#### Scenario: 多节点部署初始化

- **WHEN** TIS 以多节点模式启动，环境变量 AKKA_SEED_NODES 指向第一个节点
- **THEN** 系统创建 Actor System，连接到 seed node 并自动加入集群

#### Scenario: 核心 Actor 创建

- **WHEN** Actor System 初始化完成
- **THEN** 系统创建 DAGSchedulerActor、DAGMonitorActor、ClusterManagerActor 和 NodeDispatcherActor

### Requirement: 工作流调度

系统 SHALL 通过 DAGSchedulerActor 管理工作流的生命周期，包括启动、执行、取消和完成。

#### Scenario: 启动工作流

- **WHEN** 用户触发工作流执行（手动或定时）
- **THEN** 系统发送 StartWorkflow 消息到 DAGSchedulerActor，启动工作流实例

#### Scenario: 计算初始就绪节点

- **WHEN** DAGSchedulerActor 收到 StartWorkflow 消息
- **THEN** 系统加载 DAG 定义，计算初始就绪节点，并分发任务节点

#### Scenario: 处理节点完成消息

- **WHEN** DAGSchedulerActor 收到 NodeCompleted 消息
- **THEN** 系统更新节点状态，重新计算就绪节点，继续分发或标记工作流完成

#### Scenario: 取消工作流

- **WHEN** 用户请求取消正在运行的工作流
- **THEN** 系统发送 CancelWorkflow 消息，停止所有运行中的节点，标记工作流为 STOPPED

### Requirement: 任务分发路由

系统 SHALL 通过 NodeDispatcherActor 将任务分发到 TaskWorker，支持集群路由和负载均衡。

#### Scenario: 分发任务到本地 Worker

- **WHEN** 单节点部署时，NodeDispatcherActor 收到 DispatchTask 消息
- **THEN** 系统通过 ClusterRouterPool 将任务路由到本地 TaskWorker

#### Scenario: 分发任务到集群 Worker

- **WHEN** 多节点部署时，NodeDispatcherActor 收到 DispatchTask 消息
- **THEN** 系统通过 ClusterRouterPool 将任务路由到集群中的任意 TaskWorker，实现负载均衡

#### Scenario: 记录任务执行位置

- **WHEN** 任务被分发到某个 Worker
- **THEN** 系统记录 Worker 地址到 dag_node_execution 表的 worker_address 字段

### Requirement: 任务执行隔离

系统 SHALL 通过 TaskWorkerActor 执行具体任务，每个任务在独立的 Actor 中运行，实现故障隔离。

#### Scenario: 执行任务

- **WHEN** TaskWorkerActor 收到 TaskExecutionMessage 消息
- **THEN** 系统加载任务上下文，执行 DataflowTask.run() 方法，捕获执行结果

#### Scenario: 任务执行成功

- **WHEN** 任务执行成功
- **THEN** 系统发送 NodeCompleted 消息到 DAGSchedulerActor，状态为 SUCCEED

#### Scenario: 任务执行失败

- **WHEN** 任务执行过程中抛出异常
- **THEN** 系统捕获异常，发送 NodeCompleted 消息到 DAGSchedulerActor，状态为 FAILED，包含错误信息

#### Scenario: 任务超时

- **WHEN** 任务执行时间超过配置的超时时间
- **THEN** 系统发送 NodeTimeout 消息到 DAGSchedulerActor，终止任务执行

### Requirement: 消息序列化

系统 SHALL 使用 Kryo 对 Actor 消息进行高性能序列化，支持跨节点消息传递。

#### Scenario: 序列化消息

- **WHEN** Actor 发送消息到远程节点
- **THEN** 系统使用 Kryo 将消息对象序列化为字节流

#### Scenario: 反序列化消息

- **WHEN** Actor 接收来自远程节点的消息
- **THEN** 系统使用 Kryo 将字节流反序列化为消息对象

#### Scenario: 注册自定义类

- **WHEN** 系统启动时
- **THEN** 系统注册所有消息类和 DAG 相关类到 Kryo，确保序列化兼容性

### Requirement: 工作流上下文管理

系统 SHALL 支持工作流上下文（wf_context），允许节点间共享数据。

#### Scenario: 初始化上下文

- **WHEN** 工作流启动时传入初始参数
- **THEN** 系统将初始参数存储到 workflow_build_history 表的 wf_context 字段

#### Scenario: 节点读取上下文

- **WHEN** 任务节点执行时
- **THEN** 系统加载工作流上下文，传递给 DataflowTask.run() 方法

#### Scenario: 节点更新上下文

- **WHEN** 任务节点执行完成并返回新的上下文数据
- **THEN** 系统发送 UpdateContext 消息到 DAGSchedulerActor，合并新数据到上下文

#### Scenario: 持久化上下文

- **WHEN** 上下文数据发生变化
- **THEN** 系统将更新后的上下文持久化到数据库

### Requirement: 并发控制

系统 SHALL 通过数据库行锁和 Actor 单线程模型确保工作流状态的一致性。

#### Scenario: 加载工作流实例时加锁

- **WHEN** DAGSchedulerActor 处理消息需要更新工作流状态
- **THEN** 系统执行 SELECT ... FOR UPDATE 加行锁，防止并发修改

#### Scenario: Actor 单线程处理消息

- **WHEN** 多个消息同时到达 DAGSchedulerActor
- **THEN** 系统按顺序串行处理消息，避免并发冲突

### Requirement: 故障恢复

系统 SHALL 支持 Actor 故障自动恢复，确保任务调度的高可用性。

#### Scenario: Actor 异常重启

- **WHEN** Actor 处理消息时抛出异常
- **THEN** 系统根据 Supervisor 策略重启 Actor，恢复正常服务

#### Scenario: 节点宕机后任务恢复

- **WHEN** 某个 Worker 节点宕机
- **THEN** 系统检测到节点不可达，将该节点上运行的任务标记为失败，根据失败策略决定是否重试

#### Scenario: 工作流实例恢复

- **WHEN** TIS 重启后
- **THEN** 系统扫描数据库中状态为 RUNNING 的工作流实例，根据运行时状态恢复执行
