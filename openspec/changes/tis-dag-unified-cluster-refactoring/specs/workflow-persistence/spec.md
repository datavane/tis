## ADDED Requirements

### Requirement: DAG 定义文件存储

系统 SHALL 将 DAG 定义存储为 JSON 文件，路径为 `${TIS_HOME}/workflow/<workflow-name>/dag-spec.json`。

#### Scenario: 保存 DAG 定义

- **WHEN** 用户创建或修改工作流的 DAG 定义
- **THEN** 系统将 PEWorkflowDAG 对象序列化为 JSON，保存到文件系统

#### Scenario: 创建工作流目录

- **WHEN** 首次保存工作流的 DAG 定义
- **THEN** 系统自动创建 `${TIS_HOME}/workflow/<workflow-name>/` 目录

#### Scenario: 原子写入

- **WHEN** 保存 DAG 定义文件
- **THEN** 系统先写入临时文件，写入成功后重命名为正式文件，确保原子性

### Requirement: DAG 定义文件加载

系统 SHALL 从文件系统加载 DAG 定义，用于工作流执行。

#### Scenario: 加载 DAG 定义

- **WHEN** 工作流实例启动时
- **THEN** 系统根据 workflow 表的 dag_spec_path 字段，从文件系统加载 DAG 定义

#### Scenario: 文件不存在处理

- **WHEN** 加载 DAG 定义时文件不存在
- **THEN** 系统抛出异常，记录错误日志，工作流实例启动失败

#### Scenario: JSON 解析失败处理

- **WHEN** 加载 DAG 定义时 JSON 解析失败
- **THEN** 系统抛出异常，记录错误日志和文件内容，工作流实例启动失败

### Requirement: DAG 定义版本管理

系统 SHALL 支持 DAG 定义的 Git 版本管理（可选功能）。

#### Scenario: 初始化 Git 仓库

- **WHEN** 首次保存工作流的 DAG 定义且启用 Git 版本管理
- **THEN** 系统在工作流目录下初始化 Git 仓库

#### Scenario: 自动提交变更

- **WHEN** 保存 DAG 定义且启用 Git 版本管理
- **THEN** 系统自动提交变更到 Git 仓库，commit message 包含操作类型、用户和时间戳

#### Scenario: 查看历史版本

- **WHEN** 用户请求查看 DAG 定义的历史版本
- **THEN** 系统返回 Git 提交历史列表，包含 commit ID、时间、用户和 commit message

#### Scenario: 回滚到历史版本

- **WHEN** 用户请求回滚到某个历史版本
- **THEN** 系统从 Git 仓库检出指定版本的 dag-spec.json，保存为当前版本

### Requirement: 工作流定义数据库存储

系统 SHALL 在 workflow 表中存储工作流的元数据，包括 DAG 文件路径和调度配置。

#### Scenario: 保存工作流元数据

- **WHEN** 用户创建或修改工作流
- **THEN** 系统将工作流名称、dag_spec_path、schedule_cron、enable_schedule 等字段保存到 workflow 表

#### Scenario: 查询工作流元数据

- **WHEN** 用户请求查询工作流信息
- **THEN** 系统从 workflow 表加载工作流元数据，返回给用户

#### Scenario: 删除工作流

- **WHEN** 用户删除工作流
- **THEN** 系统删除 workflow 表记录，同时删除对应的 DAG 定义文件和工作流目录

### Requirement: 工作流实例状态持久化

系统 SHALL 在 workflow_build_history 表中存储工作流实例的执行状态。

#### Scenario: 创建工作流实例

- **WHEN** 工作流被触发执行
- **THEN** 系统在 workflow_build_history 表中插入新记录，初始状态为 WAITING

#### Scenario: 更新实例状态

- **WHEN** 工作流实例的状态发生变化
- **THEN** 系统更新 workflow_build_history 表的 instance_status 字段

#### Scenario: 持久化 DAG 运行时状态

- **WHEN** 节点状态发生变化
- **THEN** 系统将 DAG 运行时状态（所有节点的状态）序列化为 JSON，保存到 dag_runtime 字段

#### Scenario: 持久化工作流上下文

- **WHEN** 工作流上下文数据发生变化
- **THEN** 系统将上下文数据序列化为 JSON，保存到 wf_context 字段

### Requirement: 节点执行记录持久化

系统 SHALL 在 dag_node_execution 表中记录每个节点的执行详情。

#### Scenario: 创建节点执行记录

- **WHEN** 节点开始执行
- **THEN** 系统在 dag_node_execution 表中插入新记录，记录节点 ID、名称、类型、状态、开始时间

#### Scenario: 更新节点执行状态

- **WHEN** 节点执行完成
- **THEN** 系统更新 dag_node_execution 表的 status、result、finished_time 字段

#### Scenario: 记录 Worker 地址

- **WHEN** 节点被分发到某个 Worker 执行
- **THEN** 系统记录 Worker 地址到 worker_address 字段

#### Scenario: 记录重试次数

- **WHEN** 节点执行失败并重试
- **THEN** 系统更新 retry_times 字段，记录重试次数

### Requirement: 数据库事务管理

系统 SHALL 使用数据库事务确保工作流状态的一致性。

#### Scenario: 原子更新工作流状态

- **WHEN** 更新工作流实例状态和节点执行记录
- **THEN** 系统在同一个事务中执行所有更新操作，确保原子性

#### Scenario: 行锁防止并发冲突

- **WHEN** DAGSchedulerActor 处理消息需要更新工作流状态
- **THEN** 系统执行 SELECT ... FOR UPDATE 加行锁，防止并发修改

#### Scenario: 事务回滚

- **WHEN** 更新工作流状态时发生异常
- **THEN** 系统回滚事务，保持数据一致性

### Requirement: 数据清理策略

系统 SHALL 支持配置工作流实例和节点执行记录的清理策略。

#### Scenario: 按时间清理历史数据

- **WHEN** 配置保留历史数据 N 天
- **THEN** 系统定期清理 N 天前的 workflow_build_history 和 dag_node_execution 记录

#### Scenario: 按数量清理历史数据

- **WHEN** 配置每个工作流保留最近 M 个实例
- **THEN** 系统定期清理超过 M 个的历史实例记录

#### Scenario: 归档历史数据

- **WHEN** 配置启用历史数据归档
- **THEN** 系统将清理的数据导出到归档文件，而不是直接删除

### Requirement: 数据备份与恢复

系统 SHALL 支持工作流定义和执行数据的备份与恢复。

#### Scenario: 备份 DAG 定义文件

- **WHEN** 用户请求备份工作流
- **THEN** 系统将 `${TIS_HOME}/workflow/<workflow-name>/` 目录打包为 tar.gz 文件

#### Scenario: 恢复 DAG 定义文件

- **WHEN** 用户请求恢复工作流
- **THEN** 系统解压备份文件到 `${TIS_HOME}/workflow/<workflow-name>/` 目录，恢复 DAG 定义

#### Scenario: 备份数据库数据

- **WHEN** 用户请求备份工作流执行数据
- **THEN** 系统导出 workflow、workflow_build_history、dag_node_execution 表的相关记录为 SQL 文件

#### Scenario: 恢复数据库数据

- **WHEN** 用户请求恢复工作流执行数据
- **THEN** 系统执行 SQL 文件，恢复数据库记录

### Requirement: 数据一致性校验

系统 SHALL 提供数据一致性校验功能，检测文件和数据库的不一致。

#### Scenario: 校验 DAG 文件存在性

- **WHEN** 用户请求校验工作流数据一致性
- **THEN** 系统检查 workflow 表中所有 dag_spec_path 对应的文件是否存在

#### Scenario: 校验孤立文件

- **WHEN** 用户请求校验工作流数据一致性
- **THEN** 系统检查 `${TIS_HOME}/workflow/` 目录下是否存在没有对应 workflow 记录的孤立文件

#### Scenario: 修复数据不一致

- **WHEN** 检测到数据不一致
- **THEN** 系统提供修复建议，用户确认后执行修复操作（删除孤立文件或重新生成缺失文件）
