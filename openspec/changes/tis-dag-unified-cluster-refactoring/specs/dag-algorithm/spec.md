## ADDED Requirements

### Requirement: DAG 定义验证

系统 SHALL 在保存 DAG 定义时验证其合法性，包括环检测、节点连通性检查和拓扑结构验证。

#### Scenario: 检测到环形依赖

- **WHEN** 用户保存包含环形依赖的 DAG 定义（例如：节点 A → B → C → A）
- **THEN** 系统拒绝保存并返回错误信息，明确指出环路路径

#### Scenario: 检测到孤立节点

- **WHEN** 用户保存包含孤立节点的 DAG 定义（节点没有任何入边或出边）
- **THEN** 系统发出警告但允许保存，提示用户该节点可能无法执行

#### Scenario: 验证通过

- **WHEN** 用户保存合法的 DAG 定义（无环、所有节点连通）
- **THEN** 系统成功保存并返回验证通过的确认信息

### Requirement: 就绪节点计算

系统 SHALL 在工作流执行过程中动态计算就绪节点（所有前置依赖已完成的节点）。

#### Scenario: 初始就绪节点

- **WHEN** 工作流开始执行
- **THEN** 系统计算并返回所有没有前置依赖的节点作为初始就绪节点

#### Scenario: 动态计算就绪节点

- **WHEN** 某个节点执行完成
- **THEN** 系统重新计算就绪节点，将所有前置依赖已完成的后续节点标记为就绪

#### Scenario: 考虑节点禁用状态

- **WHEN** 某个节点被标记为禁用（enable=false）
- **THEN** 系统在计算就绪节点时跳过该节点，但将其视为已完成以解锁后续节点

#### Scenario: 考虑失败跳过策略

- **WHEN** 某个节点执行失败且配置为允许跳过（skipWhenFailed=true）
- **THEN** 系统将该节点视为已完成，继续计算后续就绪节点

### Requirement: 拓扑排序

系统 SHALL 提供 DAG 的拓扑排序功能，用于可视化展示和执行顺序规划。

#### Scenario: 生成拓扑排序

- **WHEN** 用户请求查看 DAG 的拓扑排序
- **THEN** 系统返回一个节点列表，保证每个节点都排在其所有前置依赖之后

#### Scenario: 多个合法排序

- **WHEN** DAG 存在多个合法的拓扑排序（例如：A → B 和 A → C 可以并行）
- **THEN** 系统返回其中一个合法排序，并标注可并行执行的节点

### Requirement: DAG 运行时状态管理

系统 SHALL 维护 DAG 的运行时状态，包括每个节点的执行状态、开始时间、完成时间和执行结果。

#### Scenario: 初始化运行时状态

- **WHEN** 工作流实例创建
- **THEN** 系统初始化 DAG 运行时状态，将所有节点标记为 WAITING

#### Scenario: 更新节点状态

- **WHEN** 节点开始执行
- **THEN** 系统更新节点状态为 RUNNING，记录开始时间

#### Scenario: 记录执行结果

- **WHEN** 节点执行完成（成功或失败）
- **THEN** 系统更新节点状态为 SUCCEED 或 FAILED，记录完成时间和执行结果

#### Scenario: 持久化运行时状态

- **WHEN** 节点状态发生变化
- **THEN** 系统将运行时状态持久化到数据库，确保故障恢复时可以恢复状态

### Requirement: 控制节点支持

系统 SHALL 支持控制节点（CONTROL 类型），用于实现条件分支和动态流程控制。

#### Scenario: 同步执行控制节点

- **WHEN** 控制节点就绪
- **THEN** 系统同步执行控制节点的决策逻辑，不分发到 Worker

#### Scenario: 根据控制节点结果禁用分支

- **WHEN** 控制节点执行完成并返回禁用某些后续节点的决策
- **THEN** 系统更新 DAG 运行时状态，将指定节点标记为禁用（enable=false）

#### Scenario: 重新计算就绪节点

- **WHEN** 控制节点执行完成并更新了节点禁用状态
- **THEN** 系统重新计算就绪节点，考虑最新的禁用状态

### Requirement: DAG 完成判断

系统 SHALL 判断工作流是否已完成（所有节点都处于终态）。

#### Scenario: 所有节点成功

- **WHEN** 所有节点都执行成功（状态为 SUCCEED）
- **THEN** 系统标记工作流实例为 SUCCEED

#### Scenario: 存在失败节点且不允许跳过

- **WHEN** 某个节点执行失败且配置为不允许跳过（skipWhenFailed=false）
- **THEN** 系统终止工作流执行，标记工作流实例为 FAILED

#### Scenario: 存在失败节点但允许跳过

- **WHEN** 某个节点执行失败但配置为允许跳过（skipWhenFailed=true）
- **THEN** 系统继续执行后续节点，最终根据整体情况判断工作流状态

#### Scenario: 存在禁用节点

- **WHEN** 某些节点被禁用（enable=false）
- **THEN** 系统将禁用节点视为已完成，不影响工作流完成判断
