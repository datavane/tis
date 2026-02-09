## ADDED Requirements

### Requirement: 统一集群配置

系统 SHALL 使用统一的 Akka 集群配置，支持单节点和多节点部署，无需修改配置文件。

#### Scenario: 单节点部署配置

- **WHEN** TIS 以单节点模式部署
- **THEN** 系统使用默认配置，seed-nodes 指向自己（127.0.0.1:2551），形成单节点集群

#### Scenario: 多节点部署配置

- **WHEN** TIS 以多节点模式部署
- **THEN** 系统读取环境变量 AKKA_SEED_NODES，连接到指定的 seed node

#### Scenario: 配置文件加载

- **WHEN** 系统启动时
- **THEN** 系统加载 application.conf 配置文件，初始化 Actor System

### Requirement: 零配置节点扩展

系统 SHALL 支持新节点零配置加入集群，无需重启现有节点。

#### Scenario: 新节点自动加入

- **WHEN** 启动新的 TIS 节点，环境变量 AKKA_SEED_NODES 指向第一个节点
- **THEN** 新节点自动连接到 seed node，加入集群，无需重启现有节点

#### Scenario: 集群成员发现

- **WHEN** 新节点加入集群
- **THEN** 系统通过 Akka Cluster 的成员发现机制，自动感知新节点

#### Scenario: 任务自动路由到新节点

- **WHEN** 新节点加入集群后
- **THEN** ClusterRouterPool 自动将新节点纳入路由范围，开始分发任务到新节点

### Requirement: 集群成员管理

系统 SHALL 通过 ClusterManagerActor 监听集群成员事件，管理节点上下线。

#### Scenario: 监听节点上线事件

- **WHEN** 新节点加入集群
- **THEN** ClusterManagerActor 收到 MemberUp 事件，记录节点信息，触发任务重新平衡

#### Scenario: 监听节点下线事件

- **WHEN** 某个节点正常关闭
- **THEN** ClusterManagerActor 收到 MemberRemoved 事件，从集群成员列表中移除该节点

#### Scenario: 监听节点不可达事件

- **WHEN** 某个节点网络故障或宕机
- **THEN** ClusterManagerActor 收到 UnreachableMember 事件，标记该节点为不可达，触发故障恢复

### Requirement: 任务负载均衡

系统 SHALL 通过 ClusterRouterPool 实现任务的负载均衡，支持多种路由策略。

#### Scenario: 轮询路由

- **WHEN** 使用 RoundRobinPool 路由策略
- **THEN** 系统按轮询方式将任务分发到集群中的各个 Worker

#### Scenario: 随机路由

- **WHEN** 使用 RandomPool 路由策略
- **THEN** 系统随机选择一个 Worker 执行任务

#### Scenario: 最小邮箱路由

- **WHEN** 使用 SmallestMailboxPool 路由策略
- **THEN** 系统选择邮箱消息最少的 Worker 执行任务，实现负载均衡

### Requirement: 集群脑裂处理

系统 SHALL 配置 Akka Split Brain Resolver，防止网络分区导致的脑裂问题。

#### Scenario: 网络分区检测

- **WHEN** 集群发生网络分区，节点分为多个子集群
- **THEN** Split Brain Resolver 检测到分区，触发决策逻辑

#### Scenario: 保留多数派

- **WHEN** 使用 keep-majority 策略
- **THEN** 系统保留节点数量最多的子集群，关闭其他子集群的节点

#### Scenario: 保留最老节点

- **WHEN** 使用 keep-oldest 策略
- **THEN** 系统保留包含最老节点的子集群，关闭其他子集群的节点

### Requirement: 集群状态监控

系统 SHALL 提供集群状态查询接口，展示集群成员信息和健康状态。

#### Scenario: 查询集群成员列表

- **WHEN** 用户请求查询集群成员
- **THEN** 系统返回所有节点的地址、角色、状态（Up/Down/Unreachable）

#### Scenario: 查询节点健康状态

- **WHEN** 用户请求查询某个节点的健康状态
- **THEN** 系统返回节点的 CPU、内存、任务队列长度等指标

#### Scenario: 查询任务分布

- **WHEN** 用户请求查询任务在集群中的分布
- **THEN** 系统返回每个节点上运行的任务数量和任务列表

### Requirement: 节点角色配置

系统 SHALL 支持为节点配置角色（role），实现任务的定向路由。

#### Scenario: 配置 Worker 角色

- **WHEN** 节点启动时配置 role=worker
- **THEN** 系统将该节点纳入 TaskWorker 路由池，可以执行任务

#### Scenario: 配置 Scheduler 角色

- **WHEN** 节点启动时配置 role=scheduler
- **THEN** 系统在该节点上创建 DAGSchedulerActor，负责工作流调度

#### Scenario: 混合角色配置

- **WHEN** 节点启动时配置 role=scheduler,worker
- **THEN** 系统在该节点上同时创建调度器和 Worker，支持单节点部署

### Requirement: 集群配置热更新

系统 SHALL 支持部分集群配置的热更新，无需重启节点。

#### Scenario: 更新路由池大小

- **WHEN** 管理员通过 API 更新 ClusterRouterPool 的大小
- **THEN** 系统动态调整路由池中的 Worker 数量，无需重启

#### Scenario: 更新超时配置

- **WHEN** 管理员通过 API 更新任务超时时间
- **THEN** 系统应用新的超时配置到后续任务，无需重启

### Requirement: 集群安全配置

系统 SHALL 支持 Akka Cluster 的安全配置，包括 TLS 加密和节点认证。

#### Scenario: 启用 TLS 加密

- **WHEN** 配置 akka.remote.artery.transport=tls-tcp
- **THEN** 系统使用 TLS 加密节点间的通信

#### Scenario: 节点认证

- **WHEN** 配置 akka.cluster.seed-node-timeout
- **THEN** 系统在指定时间内验证 seed node 的身份，防止恶意节点加入
