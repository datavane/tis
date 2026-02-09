## ADDED Requirements

### Requirement: 节点失败跳过策略

系统 SHALL 支持为每个节点配置失败跳过策略（skipWhenFailed），决定节点失败时是否继续执行后续节点。

#### Scenario: 配置允许跳过

- **WHEN** 用户为节点配置 skipWhenFailed=true
- **THEN** 系统保存配置到 DAG 定义，节点失败时不终止工作流

#### Scenario: 配置不允许跳过

- **WHEN** 用户为节点配置 skipWhenFailed=false
- **THEN** 系统保存配置到 DAG 定义，节点失败时终止工作流

#### Scenario: 默认策略

- **WHEN** 用户未配置 skipWhenFailed
- **THEN** 系统使用默认值 false，节点失败时终止工作流

### Requirement: 失败跳过执行逻辑

系统 SHALL 在节点执行失败时，根据 skipWhenFailed 配置决定后续流程。

#### Scenario: 允许跳过时继续执行

- **WHEN** 节点执行失败且 skipWhenFailed=true
- **THEN** 系统将节点标记为 FAILED，但将其视为已完成，继续计算后续就绪节点并执行

#### Scenario: 不允许跳过时终止工作流

- **WHEN** 节点执行失败且 skipWhenFailed=false
- **THEN** 系统将节点标记为 FAILED，终止工作流执行，标记工作流实例为 FAILED

#### Scenario: 记录失败原因

- **WHEN** 节点执行失败
- **THEN** 系统记录失败原因到 dag_node_execution 表的 result 字段，包含异常堆栈信息

### Requirement: 节点重试策略

系统 SHALL 支持为节点配置重试策略，包括重试次数和重试间隔。

#### Scenario: 配置重试次数

- **WHEN** 用户为节点配置 maxRetryTimes=3
- **THEN** 系统保存配置到 DAG 定义，节点失败时最多重试 3 次

#### Scenario: 配置重试间隔

- **WHEN** 用户为节点配置 retryInterval=60（秒）
- **THEN** 系统保存配置到 DAG 定义，每次重试前等待 60 秒

#### Scenario: 执行重试

- **WHEN** 节点执行失败且未达到最大重试次数
- **THEN** 系统等待重试间隔后，重新分发任务到 Worker，更新 retry_times 字段

#### Scenario: 重试次数耗尽

- **WHEN** 节点重试次数达到 maxRetryTimes
- **THEN** 系统不再重试，根据 skipWhenFailed 配置决定是否终止工作流

### Requirement: 节点超时处理

系统 SHALL 支持为节点配置超时时间，超时后自动终止任务。

#### Scenario: 配置超时时间

- **WHEN** 用户为节点配置 timeout=3600（秒）
- **THEN** 系统保存配置到 DAG 定义，节点执行超过 3600 秒后自动终止

#### Scenario: 超时终止任务

- **WHEN** 节点执行时间超过配置的超时时间
- **THEN** 系统发送 NodeTimeout 消息到 DAGSchedulerActor，终止任务执行，标记节点为 FAILED

#### Scenario: 超时后重试

- **WHEN** 节点超时且配置了重试策略
- **THEN** 系统将超时视为失败，根据重试策略决定是否重试

### Requirement: 节点禁用策略

系统 SHALL 支持动态禁用节点，跳过节点执行但不影响后续节点。

#### Scenario: 配置节点禁用

- **WHEN** 用户为节点配置 enable=false
- **THEN** 系统保存配置到 DAG 定义，节点在执行时被跳过

#### Scenario: 跳过禁用节点

- **WHEN** 计算就绪节点时遇到禁用节点
- **THEN** 系统跳过该节点，但将其视为已完成，解锁后续节点

#### Scenario: 控制节点动态禁用

- **WHEN** 控制节点执行完成并返回禁用某些后续节点的决策
- **THEN** 系统更新 DAG 运行时状态，将指定节点的 enable 设置为 false

### Requirement: 失败通知策略

系统 SHALL 支持为节点配置失败通知策略，在节点失败时发送告警。

#### Scenario: 配置失败通知

- **WHEN** 用户为节点配置 notifyOnFailure=true
- **THEN** 系统保存配置到 DAG 定义，节点失败时发送告警通知

#### Scenario: 发送失败通知

- **WHEN** 节点执行失败且 notifyOnFailure=true
- **THEN** 系统发送告警通知（邮件/钉钉/企业微信），包含节点名称、失败原因、工作流实例 ID

#### Scenario: 配置通知接收人

- **WHEN** 用户为节点配置 notifyReceivers=["user1@example.com", "user2@example.com"]
- **THEN** 系统保存配置到 DAG 定义，节点失败时发送通知到指定接收人

### Requirement: 失败恢复策略

系统 SHALL 支持从失败的工作流实例恢复执行。

#### Scenario: 从失败节点恢复

- **WHEN** 用户请求从失败的工作流实例恢复执行
- **THEN** 系统加载工作流实例的运行时状态，从失败节点重新开始执行

#### Scenario: 跳过失败节点恢复

- **WHEN** 用户请求跳过失败节点恢复执行
- **THEN** 系统将失败节点标记为 SUCCEED，从后续节点继续执行

#### Scenario: 重置工作流实例

- **WHEN** 用户请求重置工作流实例
- **THEN** 系统将所有节点状态重置为 WAITING，从头开始执行

### Requirement: 部分失败处理

系统 SHALL 支持工作流部分失败的处理，允许部分节点失败但工作流整体成功。

#### Scenario: 统计失败节点数量

- **WHEN** 工作流执行完成
- **THEN** 系统统计失败节点数量和成功节点数量

#### Scenario: 部分失败判定

- **WHEN** 工作流执行完成且存在失败节点但都配置了 skipWhenFailed=true
- **THEN** 系统标记工作流实例为 PARTIAL_SUCCESS，记录失败节点列表

#### Scenario: 完全失败判定

- **WHEN** 工作流执行过程中某个节点失败且 skipWhenFailed=false
- **THEN** 系统标记工作流实例为 FAILED，终止后续节点执行

### Requirement: 失败分析报告

系统 SHALL 提供失败分析报告，帮助用户定位问题。

#### Scenario: 生成失败报告

- **WHEN** 工作流实例执行失败
- **THEN** 系统生成失败报告，包含失败节点列表、失败原因、执行日志、依赖关系

#### Scenario: 失败根因分析

- **WHEN** 用户请求分析失败根因
- **THEN** 系统分析失败节点的依赖关系，识别最早失败的节点作为根因

#### Scenario: 失败趋势分析

- **WHEN** 用户请求查看节点的失败趋势
- **THEN** 系统统计节点的历史失败次数和失败率，生成趋势图

### Requirement: 失败自动修复

系统 SHALL 支持配置失败自动修复策略，在特定条件下自动恢复执行。

#### Scenario: 配置自动重试条件

- **WHEN** 用户配置节点在特定异常类型下自动重试
- **THEN** 系统保存配置到 DAG 定义，节点失败时检查异常类型，匹配则自动重试

#### Scenario: 配置自动跳过条件

- **WHEN** 用户配置节点在特定异常类型下自动跳过
- **THEN** 系统保存配置到 DAG 定义，节点失败时检查异常类型，匹配则自动跳过

#### Scenario: 配置自动降级

- **WHEN** 用户配置节点失败时自动降级到备用任务
- **THEN** 系统保存配置到 DAG 定义，节点失败时执行备用任务逻辑
