## ADDED Requirements

### Requirement: Cron 表达式配置

系统 SHALL 支持为工作流配置 Cron 表达式，实现定时调度。

#### Scenario: 配置标准 Cron 表达式

- **WHEN** 用户为工作流配置 Cron 表达式（例如：0 0 2 * * ? 表示每天凌晨 2 点）
- **THEN** 系统验证 Cron 表达式的合法性，保存到 workflow 表的 schedule_cron 字段

#### Scenario: 验证 Cron 表达式

- **WHEN** 用户输入非法的 Cron 表达式
- **THEN** 系统拒绝保存并返回错误信息，提示正确的格式

#### Scenario: 预览下次执行时间

- **WHEN** 用户配置 Cron 表达式后
- **THEN** 系统计算并显示未来 5 次的执行时间，供用户确认

### Requirement: 定时调度开关

系统 SHALL 支持启用或禁用工作流的定时调度。

#### Scenario: 启用定时调度

- **WHEN** 用户将 enable_schedule 设置为 true
- **THEN** 系统注册定时调度任务到 Quartz，按 Cron 表达式触发工作流

#### Scenario: 禁用定时调度

- **WHEN** 用户将 enable_schedule 设置为 false
- **THEN** 系统从 Quartz 中移除定时调度任务，停止自动触发

#### Scenario: 工作流删除时清理调度

- **WHEN** 用户删除工作流
- **THEN** 系统自动从 Quartz 中移除对应的定时调度任务

### Requirement: Quartz 调度器集成

系统 SHALL 使用 Quartz 作为定时调度引擎，支持持久化和集群部署。

#### Scenario: 初始化 Quartz 调度器

- **WHEN** TIS 启动时
- **THEN** 系统初始化 Quartz 调度器，加载所有启用定时调度的工作流

#### Scenario: 持久化调度任务

- **WHEN** 注册定时调度任务
- **THEN** 系统将调度任务持久化到数据库（qrtz_* 表），确保重启后自动恢复

#### Scenario: 集群模式避免重复触发

- **WHEN** 多个 TIS 节点部署时
- **THEN** Quartz 使用数据库锁机制，确保同一时刻只有一个节点触发工作流

### Requirement: 定时触发工作流

系统 SHALL 在 Cron 表达式指定的时间自动触发工作流执行。

#### Scenario: 定时触发成功

- **WHEN** Cron 表达式指定的时间到达
- **THEN** 系统创建工作流实例，发送 StartWorkflow 消息到 DAGSchedulerActor

#### Scenario: 触发失败重试

- **WHEN** 定时触发工作流时发生异常（例如：数据库连接失败）
- **THEN** 系统记录错误日志，根据 Quartz 的重试策略进行重试

#### Scenario: 跳过已在运行的工作流

- **WHEN** Cron 表达式触发时，上一次执行的工作流实例仍在运行
- **THEN** 系统跳过本次触发，记录警告日志，避免并发执行

### Requirement: 手动触发与定时触发共存

系统 SHALL 支持工作流的手动触发和定时触发同时存在。

#### Scenario: 手动触发不影响定时调度

- **WHEN** 用户手动触发工作流
- **THEN** 系统立即创建工作流实例，不影响定时调度的执行

#### Scenario: 定时触发不影响手动触发

- **WHEN** 定时调度触发工作流
- **THEN** 用户仍然可以手动触发工作流，创建新的工作流实例

### Requirement: 调度历史记录

系统 SHALL 记录工作流的调度历史，包括触发时间、触发方式和执行结果。

#### Scenario: 记录定时触发

- **WHEN** 定时调度触发工作流
- **THEN** 系统在 workflow_build_history 表中记录触发方式为 SCHEDULED

#### Scenario: 记录手动触发

- **WHEN** 用户手动触发工作流
- **THEN** 系统在 workflow_build_history 表中记录触发方式为 MANUAL

#### Scenario: 查询调度历史

- **WHEN** 用户查询工作流的调度历史
- **THEN** 系统返回所有工作流实例的触发时间、触发方式、执行状态和执行结果

### Requirement: 调度任务动态管理

系统 SHALL 支持动态添加、修改和删除调度任务，无需重启 TIS。

#### Scenario: 动态添加调度任务

- **WHEN** 用户为工作流启用定时调度
- **THEN** 系统立即注册调度任务到 Quartz，无需重启

#### Scenario: 动态修改 Cron 表达式

- **WHEN** 用户修改工作流的 Cron 表达式
- **THEN** 系统更新 Quartz 中的调度任务，新的 Cron 表达式立即生效

#### Scenario: 动态删除调度任务

- **WHEN** 用户禁用工作流的定时调度
- **THEN** 系统从 Quartz 中移除调度任务，停止自动触发

### Requirement: 调度任务监控

系统 SHALL 提供调度任务的监控接口，展示调度状态和下次执行时间。

#### Scenario: 查询所有调度任务

- **WHEN** 用户请求查询所有调度任务
- **THEN** 系统返回所有启用定时调度的工作流列表，包括 Cron 表达式和下次执行时间

#### Scenario: 查询单个调度任务

- **WHEN** 用户请求查询某个工作流的调度任务
- **THEN** 系统返回该工作流的 Cron 表达式、下次执行时间、上次执行时间和执行结果

#### Scenario: 查询调度任务执行统计

- **WHEN** 用户请求查询调度任务的执行统计
- **THEN** 系统返回总触发次数、成功次数、失败次数和平均执行时间

### Requirement: 时区支持

系统 SHALL 支持配置工作流调度的时区，确保跨时区部署的正确性。

#### Scenario: 配置时区

- **WHEN** 用户为工作流配置时区（例如：Asia/Shanghai）
- **THEN** 系统使用指定时区解析 Cron 表达式，计算触发时间

#### Scenario: 默认时区

- **WHEN** 用户未配置时区
- **THEN** 系统使用服务器的默认时区

#### Scenario: 夏令时处理

- **WHEN** 时区存在夏令时调整
- **THEN** 系统自动处理夏令时变化，确保触发时间正确
