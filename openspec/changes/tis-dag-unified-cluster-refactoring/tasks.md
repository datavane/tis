## 1. 基础设施准备

<!--
依赖配置:                                                                                                                                                                                                                             
  - tis-dag/pom.xml - 添加了 Akka 和 Kryo 依赖                                                                                                                                                                                          
  - tis-console/pom.xml - 添加了 Quartz 依赖                                                                                                                                                                                            
                                                                                                                                                                                                                                        
  配置文件:                                                                                                                                                                                                                             
  - tis-dag/src/main/resources/application.conf - Akka 集群配置                                                                                                                                                                         
                                                                                                                                                                                                                                        
  Java 类:                                                                                                                                                                                                                              
  - com.qlangtech.tis.dag.serialization.TISKryoInitializer - Kryo 序列化配置                                                                                                                                                            
  - com.qlangtech.tis.dag.init.WorkflowDirectoryInitializer - 工作流目录初始化器                                                                                                                                                        
                                                                                                                                                                                                                                        
  数据库脚本:                                                                                                                                                                                                                           
  - tis-common-dao/src/main/resources/db/migration/V1_workflow_dag_fields.sql                                                                                                                                                           
  - tis-common-dao/src/main/resources/db/migration/V2_workflow_build_history_dag_fields.sql                                                                                                                                             
  - tis-common-dao/src/main/resources/db/migration/V3_create_dag_node_execution.sql 
-->

- [x] 1.1 在 tis-dag 模块的 pom.xml 中添加 Akka 依赖（akka-actor、akka-cluster、akka-cluster-tools）
- [x] 1.2 在 tis-dag 模块的 pom.xml 中添加 Kryo 序列化依赖
- [x] 1.3 在 tis-console 模块的 pom.xml 中添加 Quartz 依赖
- [x] 1.4 创建 application.conf 配置文件，配置 Akka Actor System 和集群参数
- [x] 1.5 创建 Kryo 序列化配置类，注册消息类和 DAG 相关类
- [x] 1.6 编写数据库迁移脚本：扩展 workflow 表（dag_spec_path、schedule_cron、enable_schedule）
- [x] 1.7 编写数据库迁移脚本：扩展 workflow_build_history 表（dag_runtime、wf_context、instance_status）
- [x] 1.8 编写数据库迁移脚本：创建 dag_node_execution 表
- [x] 1.9 创建 ${TIS_HOME}/workflow/ 目录结构

## 2. PowerJob DAG 核心类集成

<!--
PowerJob DAG 核心类:
  - com.qlangtech.tis.powerjob.model.PEWorkflowDAG - DAG 数据模型（点线表示法）
  - com.qlangtech.tis.powerjob.algorithm.WorkflowDAG - DAG 运行时模型（引用表示法）
  - com.qlangtech.tis.powerjob.algorithm.WorkflowDAGUtils - DAG 算法工具类
  - com.qlangtech.tis.powerjob.model.InstanceStatus - 实例状态枚举
  - com.qlangtech.tis.powerjob.model.WorkflowNodeType - 节点类型枚举
  - com.qlangtech.tis.dag.validator.DAGValidator - DAG 验证工具类
-->

- [x] 2.1 从 PowerJob 项目中提取 PEWorkflowDAG 类到 com.qlangtech.tis.powerjob 包
- [x] 2.2 从 PowerJob 项目中提取 WorkflowDAG 类到 com.qlangtech.tis.powerjob 包
- [x] 2.3 从 PowerJob 项目中提取 WorkflowDAGUtils 类到 com.qlangtech.tis.powerjob 包
- [x] 2.4 提取相关的枚举类（NodeType、NodeStatus 等）
- [ ] 2.5 编写单元测试验证 DAG 算法：环检测、就绪节点计算、拓扑排序
- [x] 2.6 编写 DAG 定义验证工具类，集成 WorkflowDAGUtils.valid() 方法

## 3. DAG 文件管理器实现

<!--
WorkflowDAGFileManager 类:
  - com.qlangtech.tis.workflow.pojo.WorkflowDAGFileManager
  - 功能：DAG 定义文件的读写、版本管理、原子写入
  - 位置：tis-builder-api 模块
-->

- [x] 3.1 创建 WorkflowDAGFileManager 类（com.qlangtech.tis.workflow.pojo）
- [x] 3.2 实现 saveDagSpec() 方法：保存 DAG 定义到文件系统
- [x] 3.3 实现 loadDagSpec() 方法：从文件系统加载 DAG 定义
- [x] 3.4 实现 getWorkflowDir() 方法：获取工作流目录
- [x] 3.5 实现 exists() 方法：检查 DAG 文件是否存在
- [x] 3.6 实现 deleteDagSpec() 方法：删除 DAG 文件
- [x] 3.7 实现 Git 版本管理功能（可选）：commitDagSpec() 方法
- [ ] 3.8 编写单元测试验证文件读写和并发安全性

## 4. 数据库 DAO 层扩展

<!--
DAO 接口和映射文件:
  - IWorkFlowDAO - 扩展 selectScheduledWorkflows() 方法
  - IWorkFlowBuildHistoryDAO - 扩展 loadFromWriteDBWithLock()、selectStuckInstances() 方法
  - IDAGNodeExecutionDAO - 新建 DAO 接口
  - DAGNodeExecution - 新建实体类
  - MyBatis 映射文件：WorkFlowMapper.xml、WorkFlowBuildHistoryMapper.xml、DAGNodeExecutionMapper.xml
-->

- [x] 4.1 扩展 IWorkFlowDAO 接口：添加 selectScheduledWorkflows() 方法
- [x] 4.2 在 WorkFlow.xml 中添加对应的 SQL 映射
- [x] 4.3 扩展 IWorkFlowBuildHistoryDAO 接口：添加 loadFromWriteDBWithLock()、selectStuckInstances() 方法
- [x] 4.4 在 WorkFlowBuildHistory.xml 中添加对应的 SQL 映射（包含 FOR UPDATE）
- [x] 4.5 创建 IDAGNodeExecutionDAO 接口
- [x] 4.6 创建 DAGNodeExecution 实体类
- [x] 4.7 创建 DAGNodeExecution.xml MyBatis 映射文件
- [x] 4.8 实现所有 DAO 方法：insert、selectByWorkflowInstanceId、updateStatus 等
- [ ] 4.9 编写 DAO 层单元测试

## 5. Akka Actor 消息协议定义

<!--
Actor 消息类:
  - WorkflowInstanceActor 消息：StartWorkflow、NodeCompleted、NodeTimeout、UpdateContext、CancelWorkflow
  - DAGSchedulerActor 消息：LoadSchedules、ScheduleTriggered、RegisterSchedule、UnregisterSchedule
  - NodeDispatcherActor 消息：DispatchTask、TaskExecutionMessage
  - DAGMonitorActor 消息：QueryWorkflowStatus、QueryWaitingQueue、QueryRunningQueue
  - 响应消息：WorkflowRuntimeStatus、NodeStatus
  - 所有消息类已在 TISKryoInitializer 中注册
-->

- [x] 5.1 创建消息包 com.qlangtech.tis.dag.actor.message
- [x] 5.2 定义 WorkflowInstanceActor 消息：StartWorkflow、NodeCompleted、NodeTimeout、UpdateContext、CancelWorkflow
- [x] 5.2b 定义 DAGSchedulerActor 调度管理消息：LoadSchedules、ScheduleTriggered、RegisterSchedule、UnregisterSchedule
- [x] 5.3 定义 NodeDispatcherActor 消息：DispatchTask、TaskExecutionMessage
- [x] 5.4 定义 DAGMonitorActor 消息：QueryWorkflowStatus、QueryWaitingQueue、QueryRunningQueue
- [x] 5.5 定义响应消息：WorkflowRuntimeStatus、NodeStatus 等
- [x] 5.6 为所有消息类添加 Kryo 序列化注册

## 6. DAGSchedulerActor 实现

<!--
DAGSchedulerActor 类:
  - com.qlangtech.tis.dag.actor.DAGSchedulerActor
  - 核心职责变更：从工作流生命周期管理 → 定时调度管理器
  - 启动后加载所有 BatchJobCrontab 插件实例，根据 cron 配置创建定时任务
  - 当 cron 触发时，发送 StartWorkflow 消息给 WorkflowInstanceActor（通过 Cluster Sharding 路由）
  - TIS 重启后自动恢复所有已启用的定时调度
  - 工作流生命周期管理由 WorkflowInstanceActor 负责（见 Section 6b）
-->

- [x] 6.1 创建 DAGSchedulerActor 类（定时调度管理器，com.qlangtech.tis.dag.actor）
- [x] 6.2 实现 handleLoadSchedules()：启动时加载所有 BatchJobCrontab 实例，创建定时任务
- [x] 6.3 实现 handleScheduleTriggered()：cron 触发后发 StartWorkflow 给 WorkflowInstanceActor
- [x] 6.4 实现 handleRegisterSchedule()：注册新的定时调度
- [x] 6.5 实现 handleUnregisterSchedule()：移除定时调度
- [x] 6.6 实现跳过已在运行的工作流逻辑
- [x] 6.7 实现 TIS 重启后自动恢复调度
- [x] 6.8 实现数据库行锁并发控制
- [ ] 6.9 编写单元测试验证各种场景

## 6b. WorkflowInstanceActor 实现（工作流生命周期管理）

<!--
WorkflowInstanceActor 类:
  - com.qlangtech.tis.dag.actor.WorkflowInstanceActor
  - 核心职责：每个 workflow 实例对应一个 Actor，负责完整生命周期管理
  - 通过 Cluster Sharding 实现分布式部署，基于 workflowInstanceId 分片路由
  - 缓存 WorkFlowBuildHistory 和 DAG 定义，避免重复数据库查询
-->

- [x] 6b.1 创建 WorkflowInstanceActor 类（com.qlangtech.tis.dag.actor）
- [x] 6b.2 实现 handleStartWorkflow()：加载 DAG、计算初始就绪节点、分发任务
- [x] 6b.3 实现 handleNodeCompleted()：更新节点状态、检查失败策略、计算新就绪节点
- [x] 6b.4 实现 handleNodeTimeout()：标记节点失败、触发重试或跳过
- [x] 6b.5 实现 handleUpdateContext()：合并上下文数据、持久化
- [x] 6b.6 实现 handleCancelWorkflow()：停止所有运行中的节点
- [x] 6b.7 实现控制节点处理逻辑：同步执行、动态禁用分支
- [x] 6b.8 实现工作流完成判断逻辑
- [x] 6b.9 实现 Cluster Sharding 配置（WorkflowInstanceMessageExtractor）
- [ ] 6b.10 编写单元测试验证各种场景

## 7. NodeDispatcherActor 实现

<!--
NodeDispatcherActor 类:
  - com.qlangtech.tis.dag.actor.NodeDispatcherActor
  - 核心职责：任务分发路由、超时监控
  - 已实现方法框架：preStart、handleDispatchTask、createTaskRouter、scheduleTimeout
  - 使用 ClusterRouterPool 支持单节点和多节点场景
-->

- [x] 7.1 创建 NodeDispatcherActor 类（com.qlangtech.tis.dag.actor）
- [x] 7.2 实现 preStart() 方法：初始化 ClusterRouterPool
- [x] 7.3 实现 handleDispatchTask() 方法：创建任务上下文、更新节点状态、路由到 Worker
- [x] 7.4 实现超时监控机制：使用 Akka Scheduler 发送 NodeTimeout 消息
- [x] 7.5 实现 createTaskRouter() 方法：配置 ClusterRouterPool（RoundRobinPool）
- [x] 7.6 编写单元测试验证任务分发和超时处理

## 8. TaskWorkerActor 实现

- [x] 8.1 创建 TaskWorkerActor 类（com.qlangtech.tis.dag.actor）
- [x] 8.2 实现 handleTaskExecution() 方法：加载任务上下文、执行 DataflowTask.run()
- [x] 8.3 实现异常捕获和错误处理
- [x] 8.4 实现任务执行成功后发送 NodeCompleted 消息
- [x] 8.5 实现任务执行失败后发送 NodeCompleted 消息（包含错误信息）
- [x] 8.6 实现 Supervisor 策略配置
- [ ] 8.7 编写单元测试验证任务执行和异常处理

## 9. DAGMonitorActor 实现

- [x] 9.1 创建 DAGMonitorActor 类（com.qlangtech.tis.dag.actor）
- [x] 9.2 实现 handleQueryWorkflowStatus() 方法：查询工作流实例状态
- [x] 9.3 实现 handleQueryWaitingQueue() 方法：查询所有等待节点
- [x] 9.4 实现 handleQueryRunningQueue() 方法：查询所有运行节点
- [x] 9.5 实现队列统计功能
- [ ] 9.6 编写单元测试验证查询功能

## 10. ClusterManagerActor 实现

- [x] 10.1 创建 ClusterManagerActor 类（com.qlangtech.tis.dag.actor）
- [x] 10.2 订阅 Akka Cluster 事件：MemberUp、MemberRemoved、UnreachableMember
- [x] 10.3 实现节点上线处理：记录节点信息、触发任务重新平衡
- [x] 10.4 实现节点下线处理：从集群成员列表中移除
- [x] 10.5 实现节点不可达处理：标记节点、触发故障恢复
- [x] 10.6 实现集群状态查询接口
- [ ] 10.7 编写单元测试验证集群事件处理

## 11. Actor System 初始化

- [x] 11.1 创建 TISActorSystem 类（com.qlangtech.tis.dag）
- [x] 11.2 实现 Actor System 初始化逻辑：加载 application.conf
- [x] 11.3 实现单节点和多节点配置自动适配（读取 AKKA_SEED_NODES 环境变量）
- [x] 11.4 创建所有核心 Actor：DAGSchedulerActor（调度管理）、DAGMonitorActor、ClusterManagerActor + WorkflowInstanceActor（Cluster Sharding）
- [x] 11.5 实现 Actor System 优雅关闭
- [x] 11.6 集成到 TIS 启动流程
- [ ] 11.7 编写集成测试验证 Actor System 启动和关闭

## 12. BatchJobCrontab 定时调度配置

<!--
BatchJobCrontab 插件类:
  - com.qlangtech.tis.dag.BatchJobCrontab
  - 继承 DefaultDataXProcessorManipulate，是 TIS 原生插件扩展点
  - 通过 TIS 表单系统管理 crontab 表达式和启用开关
  - DAGSchedulerActor 负责加载 BatchJobCrontab 实例并创建定时任务
-->

- [x] 12.1 创建 BatchJobCrontab 插件类（extends DefaultDataXProcessorManipulate）
- [x] 12.2 实现 crontab 表单字段定义（crontab、turnOn）
- [x] 12.3 实现 BatchJobCrontab.DftDesc 描述器（@TISExtension）
- [x] 12.4 实现 verify() 表单验证（CronScheduleBuilder 校验）
- [x] 12.5 实现 manipulateStatusSummary() 运行状态展示
- [x] 12.6 实现 describePlugin() 元数据描述
- [x] 12.7 实现 EndType.Crontab 图标资源
- [x] 12.8 实现与 DAGSchedulerActor 的集成（加载/注册/移除）
- [ ] 12.9 编写单元测试验证调度功能

## 13. Web API 实现

- [x] 13.1 创建 WorkflowController：处理工作流 CRUD 操作
- [x] 13.2 实现保存工作流 API：验证 DAG、保存文件、保存数据库
- [x] 13.3 实现加载工作流 API：返回 DAG 定义和元数据
- [x] 13.4 实现删除工作流 API：删除文件、删除数据库记录、移除调度任务
- [x] 13.5 创建 WorkflowExecutionController：处理工作流执行操作
- [x] 13.6 实现手动触发工作流 API：创建实例、发送 StartWorkflow 消息
- [x] 13.7 实现取消工作流 API：发送 CancelWorkflow 消息
- [x] 13.8 实现查询工作流状态 API：调用 DAGMonitorActor
- [x] 13.9 实现查询等待队列 API
- [x] 13.10 实现查询执行队列 API
- [x] 13.11 实现查询工作流历史 API：支持分页和过滤
- [x] 13.12 实现查询节点执行详情 API
- [ ] 13.13 编写 API 集成测试

## 14. 定时调度配置界面
<!--
 workflow.schedule.config.component.ts
 workflow.schedule.service.ts
 API 对接                                                                                                                                                                                                                             │
                                                                                                                                                                                                                                      │
 后端 API 端点 (已在第 13 节实现):                                                                                                                                                                                                    │
 - 保存工作流: POST /workflow/doSaveWorkflow                                                                                                                                                                                          │
   - 参数: id, name, dagJson, scheduleCron, enableSchedule                                                                                                                                                                            │
 - 加载工作流: POST /workflow/doLoadWorkflow                                                                                                                                                                                          │
   - 参数: id     
-->
<!--
  ✅ 已完成的工作：                                                                                                                                                                                                                    
                                                                                                                                                                                                                                        
   1. 依赖安装                                                                                                                                                                                                                          
      - 安装 cron-parser (^4.9.0)                                                                                                                                                                                                       
                                                                                                                                                                                                                                        
   2. 创建的组件 (共 3 个)                                                                                                                                                                                                              
      - CronExpressionInputComponent - Cron 表达式输入组件                                                                                                                                                                              
      - CronExpressionPreviewComponent - Cron 表达式预览组件                                                                                                                                                                            
      - WorkflowScheduleConfigComponent - 工作流调度配置主组件                                                                                                                                                                          
                                                                                                                                                                                                                                        
   3. 创建的服务 (共 1 个)                                                                                                                                                                                                              
      - WorkflowScheduleService - 工作流调度服务                                                                                                                                                                                        
                                                                                                                                                                                                                                        
   4. 更新的模块配置                                                                                                                                                                                                                    
      - common.module.ts - 注册 Cron 组件                                                                                                                                                                                               
      - offline.module.ts - 注册调度配置组件                                                                                                                                                                                            
      - offline-routing.module.ts - 添加路由                                                                                                                                                                                            
                                                                                                                                                                                                                                        
   5. 集成到工作流列表                                                                                                                                                                                                                  
      - workflow.component.ts - 添加"调度配置"菜单项                                                                                                                                                                                    
                                                                                                                                                                                                                                        
   6. 测试文件                                                                                                                                                                                                                          
      - workflow.schedule.config.component.spec.ts                                                                                                                                                                                      
                                                                                                                                                                                                                                        
   7. 文档                                                                                                                                                                                                                              
      - WORKFLOW_SCHEDULE_README.md                                                                                                                                                                                                     
                                                                                                                                                                                                                                        
   ========================================                                                                                                                                                                                             
   功能特性：                                                                                                                                                                                                                           
   ========================================                                                                                                                                                                                             
                                                                                                                                                                                                                                        
   ✓ 定时调度开关                                                                                                                                                                                                                       
   ✓ Cron 表达式输入和验证                                                                                                                                                                                                              
   ✓ 快捷选项（每分钟、每小时、每天等）                                                                                                                                                                                                 
   ✓ 未来 5 次执行时间预览                                                                                                                                                                                                              
   ✓ 实时表单验证                                                                                                                                                                                                                       
   ✓ 友好的错误提示                                                                                                                                                                                                                     
   ✓ Cron 表达式格式说明                                                                                                                                                                                                                
   ✓ 保存和加载配置                                                                                                                                                                                                                     
                                                                                                                                                                                                                                        
   ========================================                                                                                                                                                                                             
   访问路径：                                                                                                                                                                                                                           
   ========================================                                                                                                                                                                                             
                                                                                                                                                                                                                                        
   /offline/wf_profile/{workflowName}/schedule
-->
- [x ] 14.1 在前端创建工作流配置页面组件
- [x ] 14.2 实现 Cron 表达式输入框和验证
- [x ] 14.3 实现 Cron 表达式预览功能（显示未来 5 次执行时间）
- [x ] 14.4 实现定时调度开关（enable_schedule）
- [x ] 14.5 实现保存配置功能：调用后端 API
- [ ] 14.6 编写前端单元测试

## 15. DAG 可视化监控界面

- [x] 15.1 在前端创建 DAG 监控页面组件
- [x] 15.2 实现 DAG 图渲染：使用 React Flow 或 D3.js
- [x] 15.3 实现节点状态着色：WAITING=灰色、RUNNING=蓝色、SUCCEED=绿色、FAILED=红色
- [x] 15.4 实现实时状态更新：WebSocket 连接
- [x] 15.5 实现节点点击查看详情：显示执行日志和结果
- [x] 15.6 实现等待队列展示组件
- [x] 15.7 实现执行队列展示组件
- [x] 15.8 实现工作流历史列表组件
- [ ] 15.9 编写前端单元测试

## 16. WebSocket 实时推送

- [x] 16.1 创建 WebSocket 配置类
- [x] 16.2 创建 WorkflowMonitorWebSocketHandler 类
- [x] 16.3 实现客户端订阅工作流实例
- [x] 16.4 实现节点状态变化推送
- [x] 16.5 实现工作流完成事件推送
- [x] 16.6 实现连接管理和心跳检测
- [ ] 16.7 编写 WebSocket 集成测试

## 17. 故障恢复机制

- [ ] 17.1 实现 TIS 重启后扫描 RUNNING 状态的工作流实例
- [ ] 17.2 实现根据运行时状态恢复工作流执行
- [ ] 17.3 实现节点宕机后任务恢复：检测 Worker 不可达、标记任务失败
- [ ] 17.4 实现 Split Brain Resolver 配置：keep-majority 策略
- [ ] 17.5 编写故障恢复集成测试

## 18. 向后兼容性支持

- [ ] 18.1 保留 TISReactor 接口和实现
- [ ] 18.2 实现工作流类型判断：根据 dag_spec_path 是否为空选择新旧机制
- [ ] 18.3 实现 DAGSessionSpec 适配新 DAG 模型
- [ ] 18.4 编写迁移工具：将旧工作流转换为新格式
- [ ] 18.5 编写兼容性测试：验证旧工作流仍可正常运行

## 19. 单元测试和集成测试

- [ ] 19.1 编写 PowerJob DAG 算法测试：环检测、就绪节点计算、拓扑排序
- [ ] 19.2 编写 WorkflowDAGFileManager 测试：文件读写、并发安全
- [ ] 19.3 编写 DAGSchedulerActor 调度管理测试
- [ ] 19.3b 编写 WorkflowInstanceActor 测试：各种消息处理场景
- [ ] 19.4 编写 NodeDispatcherActor 测试：任务分发和超时
- [ ] 19.5 编写 TaskWorkerActor 测试：任务执行和异常处理
- [ ] 19.6 编写 BatchJobCrontab 插件测试：表单验证、调度触发
- [ ] 19.7 编写完整工作流执行集成测试：从触发到完成
- [ ] 19.8 编写集群扩展测试：单节点到多节点
- [ ] 19.9 编写故障恢复测试：节点宕机、网络分区
- [ ] 19.10 编写性能测试：并发工作流执行

## 20. 文档和部署

- [ ] 20.1 编写开发文档：架构说明、核心类介绍
- [ ] 20.2 编写 Akka 配置说明文档
- [ ] 20.3 编写部署文档：单机部署、Docker 部署、Kubernetes 部署
- [ ] 20.4 编写数据库迁移文档
- [ ] 20.5 编写用户手册：如何配置定时调度、如何查看监控
- [ ] 20.6 编写故障排查文档
- [ ] 20.7 更新 CHANGELOG
- [ ] 20.8 准备演示 Demo

## 21. 灰度发布和验证

- [ ] 21.1 选择 1-2 个非核心工作流进行灰度测试
- [ ] 21.2 监控灰度工作流的执行情况
- [ ] 21.3 收集用户反馈
- [ ] 21.4 修复发现的问题
- [ ] 21.5 逐步扩大灰度范围
- [ ] 21.6 全量发布
- [ ] 21.7 监控生产环境运行情况
