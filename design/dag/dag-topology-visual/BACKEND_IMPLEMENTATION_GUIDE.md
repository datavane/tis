# 后端实现指南 - 任务队列可视化

本文档提供了完整的后端实现步骤，用于支持前端的任务队列可视化功能。

## 概述

前端已经 100% 完成，等待后端提供以下数据：
1. **等待队列** (`waitingQueue`) - 来自 WorkflowInstanceActor 的内部状态
2. **运行队列** (`runningQueue`) - 来自 WorkflowInstanceActor 的内部状态  
3. **已完成任务** (`completedTasks`) - 来自数据库查询
4. **最大并发数** (`maxConcurrentTasks`) - 来自 WorkflowInstanceActor 的配置

## 架构说明

```
前端 → DAGWorkflowServlet.queryActorSystemStatus()
      → TISActorSystem.collectStatus()
      → 1. 查询所有活跃的 WorkflowInstanceActor（聚合队列数据）
      → 2. 查询 dag_node_execution 表（已完成任务）
      → 返回 ActorSystemStatus（包含队列数据）
```

## 实现步骤

### 步骤 1: 创建查询消息类

**目录**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-datax/tis-datax-local-akka-executor/src/main/java/com/qlangtech/tis/dag/actor/message/`

#### 文件 1: `QueryQueueStatus.java`

```java
package com.qlangtech.tis.dag.actor.message;

import java.io.Serializable;

/**
 * 查询工作流实例的队列状态
 * 用于从 WorkflowInstanceActor 获取等待队列和运行队列信息
 * 
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-07-28
 */
public class QueryQueueStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Integer workflowInstanceId;
    
    public QueryQueueStatus(Integer workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }
    
    public Integer getWorkflowInstanceId() {
        return workflowInstanceId;
    }
}
```

#### 文件 2: `QueueStatusResponse.java`

```java
package com.qlangtech.tis.dag.actor.message;

import com.qlangtech.tis.datax.ActorSystemStatus;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 队列状态响应消息
 * WorkflowInstanceActor 返回的队列状态数据
 * 
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-07-28
 */
public class QueueStatusResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer workflowInstanceId;
    private List<ActorSystemStatus.QueuedTask> waitingQueue = new ArrayList<>();
    private List<ActorSystemStatus.RunningTask> runningTasks = new ArrayList<>();
    private int maxConcurrentTasks;
    
    public Integer getWorkflowInstanceId() {
        return workflowInstanceId;
    }
    
    public void setWorkflowInstanceId(Integer workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }
    
    public List<ActorSystemStatus.QueuedTask> getWaitingQueue() {
        return waitingQueue;
    }
    
    public void setWaitingQueue(List<ActorSystemStatus.QueuedTask> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }
    
    public List<ActorSystemStatus.RunningTask> getRunningTasks() {
        return runningTasks;
    }
    
    public void setRunningTasks(List<ActorSystemStatus.RunningTask> runningTasks) {
        this.runningTasks = runningTasks;
    }
    
    public int getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }
    
    public void setMaxConcurrentTasks(int maxConcurrentTasks) {
        this.maxConcurrentTasks = maxConcurrentTasks;
    }
}
```

### 步骤 2: 修改 WorkflowInstanceActor

**文件**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-datax/tis-datax-local-akka-executor/src/main/java/com/qlangtech/tis/dag/actor/WorkflowInstanceActor.java`

#### 2.1 添加 import 语句（文件开头）

```java
import com.qlangtech.tis.dag.actor.message.QueryQueueStatus;
import com.qlangtech.tis.dag.actor.message.QueueStatusResponse;
import java.util.ArrayList;
```

#### 2.2 修改 createReceive() 方法（第174行）

```java
@Override
public Receive createReceive() {
    return receiveBuilder()
            .match(StartWorkflow.class, this::handleStartWorkflow)
            .match(NodeCompleted.class, this::handleNodeCompleted)
            .match(UpdateContext.class, this::handleUpdateContext)
            .match(CancelWorkflow.class, this::handleCancelWorkflow)
            .match(QueryWorkflowStatus.class, this::handleQueryWorkflowStatus)
            .match(QueryQueueStatus.class, this::handleQueryQueueStatus) // 新增这一行
            .match(ReceiveTimeout.class, this::handleTimeout)
            .matchAny(msg -> logger.warn("Received unknown message: type={}, content={}", 
                msg.getClass().getName(), msg))
            .build();
}
```

#### 2.3 添加处理方法（在 handleTimeout 方法之后，约第471行）

```java
/**
 * 处理查询队列状态消息
 * 用于监控面板实时展示等待队列和运行队列
 */
private void handleQueryQueueStatus(QueryQueueStatus msg) {
    logger.debug("Handling QueryQueueStatus: workflowInstanceId={}", msg.getWorkflowInstanceId());
    
    try {
        QueueStatusResponse response = new QueueStatusResponse();
        response.setWorkflowInstanceId(msg.getWorkflowInstanceId());
        response.setMaxConcurrentTasks(this.maxConcurrentTasks);
        
        // 转换等待队列
        List<ActorSystemStatus.QueuedTask> waitingTasks = new ArrayList<>();
        for (PEWorkflowDAG.Node node : this.waitingQueue) {
            ActorSystemStatus.QueuedTask task = new ActorSystemStatus.QueuedTask();
            task.setNodeId(node.getNodeId());
            task.setNodeName(node.getNodeName());
            task.setTaskId(this.taskId);
            // 简化实现：使用当前时间作为排队时间
            // 更精确的实现：在 Node 中添加 queuedTime 字段
            task.setQueuedTime(System.currentTimeMillis());
            waitingTasks.add(task);
        }
        response.setWaitingQueue(waitingTasks);
        
        // 转换运行任务
        List<ActorSystemStatus.RunningTask> runningTasksList = new ArrayList<>();
        for (Long nodeId : this.runningTasks) {
            PEWorkflowDAG.Node node = findNodeById(nodeId);
            if (node != null) {
                ActorSystemStatus.RunningTask task = new ActorSystemStatus.RunningTask();
                task.setNodeId(nodeId);
                task.setNodeName(node.getNodeName());
                task.setTaskId(this.taskId);
                // 简化实现：使用当前时间
                // 更精确的实现：在 runningTasks 中存储 Map<nodeId, startTime>
                task.setStartTime(System.currentTimeMillis());
                task.setWorkerAddress("unknown"); // TODO: 从 activeWorkers 获取实际地址
                runningTasksList.add(task);
            }
        }
        response.setRunningTasks(runningTasksList);
        
        getSender().tell(response, getSelf());
        
    } catch (Exception e) {
        logger.error("Failed to query queue status: workflowInstanceId={}", 
            msg.getWorkflowInstanceId(), e);
        getSender().tell(new akka.actor.Status.Failure(e), getSelf());
    }
}

/**
 * 根据 nodeId 查找节点
 */
private PEWorkflowDAG.Node findNodeById(Long nodeId) {
    if (this.dag == null || this.dag.getNodes() == null) {
        return null;
    }
    return this.dag.getNodes().stream()
        .filter(n -> n.getNodeId().equals(nodeId))
        .findFirst()
        .orElse(null);
}
```

### 步骤 3: 修改 TISActorSystem

**文件**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-datax/tis-datax-local-akka-executor/src/main/java/com/qlangtech/tis/dag/TISActorSystem.java`

#### 3.1 添加 import 语句

```java
import com.qlangtech.tis.dag.actor.message.QueryQueueStatus;
import com.qlangtech.tis.dag.actor.message.QueueStatusResponse;
import scala.concurrent.Future;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
```

#### 3.2 修改 collectStatus() 方法（第407行）

在方法的 `return status;` 之前添加：

```java
public ActorSystemStatus collectStatus() {
    ActorSystemStatus status = new ActorSystemStatus();
    status.setSystemName(ACTOR_SYSTEM_NAME);
    status.setInitialized(initialized);
    status.setRunning(isRunning());
    status.setStartTime(startTimeMillis);

    if (actorSystem != null) {
        akka.actor.Address defaultAddress = actorSystem.provider().getDefaultAddress();
        status.setAddress(defaultAddress.toString());
        if (defaultAddress.host().isDefined()) {
            status.setHostname(defaultAddress.host().get());
        }
        if (defaultAddress.port().isDefined()) {
            status.setPort((Integer) defaultAddress.port().get());
        }
        status.setUptime(actorSystem.uptime() * 1000L);

        // Collect actor counts
        java.util.Map<String, Integer> actorCounts = new java.util.HashMap<>();
        actorCounts.put("ClusterManagerActor", clusterManagerActor != null ? 1 : 0);
        actorCounts.put("NodeDispatcherActor", nodeDispatcherActor != null ? 1 : 0);
        actorCounts.put("WorkflowInstanceRegion", workflowInstanceRegion != null ? 1 : 0);
        actorCounts.put("DAGSchedulerActor", dagSchedulerActor != null ? 1 : 0);
        status.setActorCounts(actorCounts);

        // Build actor topology
        status.setActorTopology(buildActorTopology(actorCounts));
        
        // 新增：收集队列数据
        if (initialized) {
            collectQueueData(status);
        }
    }

    return status;
}
```

#### 3.3 添加 collectQueueData() 方法（在 buildActorTopology() 方法之后）

```java
/**
 * 收集所有活跃工作流的队列数据
 * 聚合所有 WorkflowInstanceActor 的等待队列和运行队列
 * 
 * @param status ActorSystemStatus 对象，将填充队列数据
 */
private void collectQueueData(ActorSystemStatus status) {
    try {
        // 从 activeWorkflows 获取所有活跃的工作流实例 ID
        List<Integer> activeWorkflowIds = new ArrayList<>();
        if (status.getActiveWorkflows() != null) {
            for (ActorSystemStatus.ActiveWorkflowInfo workflow : status.getActiveWorkflows()) {
                activeWorkflowIds.add(workflow.getTaskId());
            }
        }
        
        if (activeWorkflowIds.isEmpty()) {
            logger.debug("No active workflows, skipping queue data collection");
            return;
        }
        
        logger.info("Collecting queue data from {} active workflows", activeWorkflowIds.size());
        
        // 聚合所有工作流的队列数据
        List<ActorSystemStatus.QueuedTask> allWaitingTasks = new ArrayList<>();
        List<ActorSystemStatus.RunningTask> allRunningTasks = new ArrayList<>();
        int maxConcurrent = 5; // 默认值
        
        // 向每个 WorkflowInstanceActor 发送查询消息
        for (Integer workflowId : activeWorkflowIds) {
            try {
                QueryQueueStatus queryMsg = new QueryQueueStatus(workflowId);
                
                // 使用 Akka Patterns.ask 发送查询消息并等待响应
                Future<Object> future = Patterns.ask(
                    workflowInstanceRegion, 
                    queryMsg, 
                    Duration.create(5, TimeUnit.SECONDS)
                );
                
                Object result = Await.result(future, Duration.create(5, TimeUnit.SECONDS));
                
                if (result instanceof QueueStatusResponse) {
                    QueueStatusResponse response = (QueueStatusResponse) result;
                    allWaitingTasks.addAll(response.getWaitingQueue());
                    allRunningTasks.addAll(response.getRunningTasks());
                    maxConcurrent = Math.max(maxConcurrent, response.getMaxConcurrentTasks());
                    
                    logger.debug("Collected queue data from workflow {}: waiting={}, running={}", 
                        workflowId, response.getWaitingQueue().size(), response.getRunningTasks().size());
                }
            } catch (Exception e) {
                logger.warn("Failed to query queue status for workflow: workflowId={}, error={}", 
                    workflowId, e.getMessage());
                // 继续查询其他工作流，不中断整个收集过程
            }
        }
        
        // 设置聚合数据
        status.setWaitingQueue(allWaitingTasks);
        status.setRunningQueue(allRunningTasks);
        status.setMaxConcurrentTasks(maxConcurrent);
        
        logger.info("Queue data collection completed: totalWaiting={}, totalRunning={}, maxConcurrent={}", 
            allWaitingTasks.size(), allRunningTasks.size(), maxConcurrent);
        
        // 收集已完成任务（从数据库查询）
        collectCompletedTasks(status);
        
    } catch (Exception e) {
        logger.error("Failed to collect queue data", e);
        // 失败不影响其他状态数据的返回
    }
}

/**
 * 从数据库查询已完成的任务
 * 
 * @param status ActorSystemStatus 对象，将填充已完成任务数据
 */
private void collectCompletedTasks(ActorSystemStatus status) {
    try {
        // TODO: 实现数据库查询
        // 需要在 IDAGNodeExecutionDAO 中添加查询方法
        // List<DAGNodeExecution> completedExecutions = 
        //     dagNodeExecutionDAO.selectRecentlyCompletedTasks(100, 3600000L);
        
        // 临时实现：返回空列表
        status.setCompletedTasks(new ArrayList<>());
        
        logger.debug("Completed tasks collection: count={}", status.getCompletedTasks().size());
        
    } catch (Exception e) {
        logger.error("Failed to collect completed tasks", e);
        status.setCompletedTasks(new ArrayList<>());
    }
}
```

### 步骤 4: 实现数据库查询（可选）

#### 4.1 在 IDAGNodeExecutionDAO 中添加方法

**文件**: 查找 `IDAGNodeExecutionDAO` 接口（通常在 `tis-plugin` 模块）

```java
/**
 * 查询最近完成的任务
 * 
 * @param limit 返回数量限制
 * @param timeWindowMillis 时间窗口（毫秒），例如 3600000 表示最近1小时
 * @return 完成的任务列表
 */
List<DAGNodeExecution> selectRecentlyCompletedTasks(
    @Param("limit") int limit, 
    @Param("timeWindowMillis") long timeWindowMillis
);
```

#### 4.2 在 MyBatis Mapper XML 中实现 SQL

```xml
<select id="selectRecentlyCompletedTasks" resultMap="BaseResultMap">
    SELECT 
        id, task_id, node_id, node_name, status, 
        start_time, end_time, worker_address, retry_count
    FROM dag_node_execution
    WHERE status IN ('SUCCEED', 'FAILED')
      AND end_time > (UNIX_TIMESTAMP() * 1000 - #{timeWindowMillis})
    ORDER BY end_time DESC
    LIMIT #{limit}
</select>
```

#### 4.3 修改 collectCompletedTasks() 方法

```java
private void collectCompletedTasks(ActorSystemStatus status) {
    try {
        // 查询最近1小时内完成的任务，最多100条
        long oneHourMillis = 60 * 60 * 1000L;
        List<DAGNodeExecution> completedExecutions = 
            dagNodeExecutionDAO.selectRecentlyCompletedTasks(100, oneHourMillis);
        
        List<ActorSystemStatus.CompletedTask> completedTasks = new ArrayList<>();
        for (DAGNodeExecution execution : completedExecutions) {
            ActorSystemStatus.CompletedTask task = new ActorSystemStatus.CompletedTask();
            task.setNodeId(execution.getNodeId());
            task.setNodeName(execution.getNodeName());
            task.setTaskId(execution.getTaskId());
            task.setStartTime(execution.getStartTime());
            task.setEndTime(execution.getEndTime());
            task.setStatus(execution.getStatus());
            completedTasks.add(task);
        }
        
        status.setCompletedTasks(completedTasks);
        
        logger.debug("Completed tasks collection: count={}", completedTasks.size());
        
    } catch (Exception e) {
        logger.error("Failed to collect completed tasks", e);
        status.setCompletedTasks(new ArrayList<>());
    }
}
```

## 测试步骤

### 1. 单元测试

创建测试类验证消息处理：

```java
@Test
public void testQueryQueueStatus() {
    // 创建测试用的 WorkflowInstanceActor
    // 发送 QueryQueueStatus 消息
    // 验证响应数据
}
```

### 2. 集成测试

```bash
# 1. 启动 TIS 服务
# 2. 触发一个 DAG 工作流执行
# 3. 访问监控接口
curl http://localhost:8080/dagworkflow?method=queryActorSystemStatus

# 4. 验证返回的 JSON 包含队列数据
{
  "waitingQueue": [...],
  "runningQueue": [...],
  "completedTasks": [...],
  "maxConcurrentTasks": 10
}
```

### 3. 前端联调

1. 启动前端开发服务器
2. 访问 Akka 集群监控页面
3. 切换到"任务分发与负载均衡" Tab
4. 验证显示效果

## 性能考虑

### 问题：频繁查询可能影响性能

**解决方案**：

1. **添加缓存**：
```java
// 缓存队列数据，5秒过期
private final Cache<String, ActorSystemStatus> statusCache = 
    CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build();
```

2. **限制查询的工作流数量**：
```java
// 只查询前10个活跃工作流
List<Integer> topActiveWorkflows = activeWorkflowIds.stream()
    .limit(10)
    .collect(Collectors.toList());
```

3. **异步查询**：
```java
// 使用 CompletableFuture 并行查询多个工作流
List<CompletableFuture<QueueStatusResponse>> futures = 
    activeWorkflowIds.stream()
        .map(id -> CompletableFuture.supplyAsync(() -> queryQueueStatus(id)))
        .collect(Collectors.toList());
```

## 故障排查

### 问题1：QueryQueueStatus 消息未处理

**症状**：日志显示 "Received unknown message: QueryQueueStatus"

**解决**：检查 `createReceive()` 方法是否添加了 `.match(QueryQueueStatus.class, ...)` 行

### 问题2：返回的队列数据为空

**症状**：前端显示"暂无任务队列数据"

**调试步骤**：
1. 检查是否有活跃的工作流：`status.getActiveWorkflows().size()`
2. 检查 Akka 消息发送是否成功：查看日志中的 "Collected queue data from workflow"
3. 检查 WorkflowInstanceActor 是否正确初始化：`this.dag != null`

### 问题3：性能问题

**症状**：`queryActorSystemStatus` 调用耗时过长（>5秒）

**解决**：
1. 添加超时控制：`Duration.create(2, TimeUnit.SECONDS)`
2. 减少查询的工作流数量
3. 添加缓存机制

## 预计工作量

- **步骤 1-2**（消息类 + WorkflowInstanceActor）：1 小时
- **步骤 3**（TISActorSystem 聚合逻辑）：1-1.5 小时
- **步骤 4**（数据库查询，可选）：0.5 小时
- **测试和调试**：1 小时
- **总计**：3.5-4 小时

## 完成标准

- [ ] 创建 `QueryQueueStatus` 和 `QueueStatusResponse` 消息类
- [ ] WorkflowInstanceActor 能处理 `QueryQueueStatus` 消息
- [ ] TISActorSystem.collectStatus() 能聚合多个工作流的队列数据
- [ ] 前端能正常显示队列可视化界面
- [ ] 性能满足要求（响应时间 < 3秒）

---

**文档版本**: 1.0
**创建日期**: 2026-07-28
**作者**: Claude (AI Assistant)
