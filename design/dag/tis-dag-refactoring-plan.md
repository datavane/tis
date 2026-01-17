# TIS DAG任务调度改造方案

## 文档信息

- **作者**: 百岁
- **创建日期**: 2026-01-12
- **版本**: v1.0
- **状态**: 待实施

## 一、改造背景

### 1.1 现状分析

TIS当前使用的任务调度机制基于Jenkins的`task-reactor`库:
- **依赖包**: `org.jenkins-ci:task-reactor:1.5` (实际使用`org.jvnet.hudson.reactor`)
- **核心类**:
  - `TISReactor`: 封装Reactor的执行逻辑 (`tis-dag/src/main/java/com/qlangtech/tis/fullbuild/taskflow/TISReactor.java:46`)
  - `DAGSessionSpec`: 构建DAG规范,采用字符串DSL方式 (`tis-sql-parser/src/main/java/com/qlangtech/tis/sql/parser/DAGSessionSpec.java:57`)
  - `TaskAndMilestone`: 任务和里程碑的包装类
  - `DataflowTask`: 抽象任务基类

### 1.2 现有问题

1. **技术老旧**: Jenkins Reactor版本较老(1.5),维护成本高
2. **单机限制**: 基于内存的单机执行模型,无法横向扩展
3. **缺乏可视化**: 没有直观的DAG执行状态监控界面
4. **故障恢复弱**: 缺少灵活的故障处理策略配置
5. **扩展性差**: 难以支持复杂的工作流场景

### 1.3 改造目标

1. **定时调度**: 支持DAG任务定时执行,可配置Cron表达式
2. **弹性伸缩**: 利用Akka框架实现单机到分布式的无缝切换,提高吞吐能力
3. **故障容错**: DAG节点支持失败跳过或终止后续任务的策略配置
4. **可视化监控**: 实时查看DAG等待队列和执行队列的任务状态

## 二、技术选型

### 2.1 PowerJob DAG核心类

**选择理由**:
- ✅ 设计优雅: 双重数据模型(点线表示法+引用表示法)
- ✅ 算法完善: 环检测、就绪节点计算、拓扑排序
- ✅ 流程控制灵活: 支持节点禁用、失败跳过、控制节点
- ✅ 生产验证: 开源项目,社区活跃,经过大规模生产验证

**核心类**:
- `PEWorkflowDAG`: 可序列化的DAG数据模型(点线表示法)
- `WorkflowDAG`: 运行时引用模型
- `WorkflowDAGUtils`: DAG算法工具类(环检测、就绪节点计算等)

**参考文档**: `/opt/misc/PowerJob-4.3.6` 或 `design/dag/powerjob-workflow-dag-analysis.md`

### 2.2 Akka任务执行框架

**选择理由**:
- ✅ Actor模型: 天然支持异步消息驱动,适合任务调度
- ✅ 弹性架构: 单机/集群模式配置化切换
- ✅ 容错机制: 内置Supervisor策略,支持故障自动恢复
- ✅ 高性能: 轻量级Actor,支持百万级并发

**使用场景**:
- 任务调度协调
- 任务分发路由
- 任务执行隔离
- 集群成员管理

### 2.3 技术栈总览

| 组件 | 技术选型 | 用途 |
|------|---------|------|
| DAG算法层 | PowerJob核心类 | DAG拓扑计算和验证 |
| 任务执行层 | Akka Actor System | 任务调度和执行 |
| 持久化层 | MySQL + MyBatis | 工作流定义和状态存储 |
| 定时调度 | Quartz / Spring Scheduler | Cron定时触发 |
| 序列化 | Kryo | Actor消息高性能序列化 |
| 监控 | Micrometer + Prometheus | 指标采集和监控 |

## 三、方案设计

### 3.1 总体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         TIS DAG调度系统                          │
│                                                                  │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐ │
│  │ 定时调度器    │─────▶│ DAG算法层     │─────▶│ Akka执行层    │ │
│  │  (Quartz)    │      │ (PowerJob)   │      │ (Actor System)│ │
│  └──────────────┘      └──────────────┘      └──────────────┘ │
│         │                     │                      │          │
│         ▼                     ▼                      ▼          │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                     持久化层 (MySQL)                       │ │
│  │  - workflow (工作流定义)                                   │ │
│  │  - workflow_build_history (工作流实例)                     │ │
│  │  - dag_node_execution (节点执行记录)                       │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                  Akka Actor System                         │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │ │
│  │  │DAGScheduler  │─▶│NodeDispatcher│─▶│ TaskWorker   │   │ │
│  │  │   Actor      │  │    Actor     │  │   (Pool)     │   │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │ │
│  │  ┌──────────────┐  ┌──────────────┐                     │ │
│  │  │DAGMonitor    │  │ClusterManager│                     │ │
│  │  │   Actor      │  │    Actor     │                     │ │
│  │  └──────────────┘  └──────────────┘                     │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 核心流程

#### 3.2.1 工作流创建流程

```
用户提交工作流定义
    │
    ▼
验证DAG合法性 (WorkflowDAGUtils.valid)
    │
    ▼
序列化为PEWorkflowDAG (JSON)
    │
    ▼
持久化到workflow表
    │
    ▼
注册定时调度任务 (可选)
```

#### 3.2.2 工作流执行流程

```
定时触发 / 手动触发
    │
    ▼
创建工作流实例 (workflow_build_history)
    │
    ▼
初始化DAG运行时状态 (所有节点标记WAITING)
    │
    ▼
发送StartWorkflow消息到DAGSchedulerActor
    │
    ▼
计算就绪节点 (WorkflowDAGUtils.listReadyNodes)
    │
    ▼
区分控制节点和任务节点
    │
    ├─▶ 控制节点: 同步执行 (决策逻辑)
    │       │
    │       ▼
    │   更新DAG状态 (可能禁用某些分支)
    │       │
    │       ▼
    │   重新计算就绪节点
    │
    └─▶ 任务节点: 异步分发到TaskWorker
            │
            ▼
        执行任务 (DataflowTask.run)
            │
            ▼
        任务完成,发送NodeCompleted消息
            │
            ▼
        更新节点状态 (SUCCEED/FAILED)
            │
            ▼
        检查失败策略 (skipWhenFailed)
            │
            ├─▶ 允许跳过: 继续执行后续节点
            │
            └─▶ 不允许跳过: 终止工作流,标记FAILED
            │
            ▼
        计算新的就绪节点
            │
            ▼
        重复流程,直到所有节点完成
            │
            ▼
        工作流完成 (SUCCEED/FAILED)
```

#### 3.2.3 单机到集群切换流程

```
应用启动
    │
    ▼
读取配置 (tis.dag.cluster.enabled)
    │
    ├─▶ false: 初始化单机模式
    │       │
    │       ▼
    │   创建本地Actor System
    │       │
    │       ▼
    │   创建RoundRobinPool (本地线程池)
    │       │
    │       ▼
    │   任务在本地执行
    │
    └─▶ true: 初始化集群模式
            │
            ▼
        创建Cluster Actor System
            │
            ▼
        配置seed nodes (集群发现)
            │
            ▼
        创建ClusterRouterPool (跨节点路由)
            │
            ▼
        任务分发到集群Worker节点
            │
            ▼
        监听节点上下线事件
            │
            ├─▶ MemberUp: 重新平衡任务
            │
            └─▶ MemberDown: 恢复失败任务
```

## 四、数据库设计

### 4.1 工作流定义表 (workflow)

**扩展字段**:

```sql
ALTER TABLE workflow
ADD COLUMN dag_spec_path VARCHAR(256) COMMENT 'DAG拓扑结构文件路径,相对于${TIS_HOME}/workflow/',
ADD COLUMN schedule_cron VARCHAR(64) COMMENT '定时调度Cron表达式',
ADD COLUMN enable_schedule TINYINT(1) DEFAULT 0 COMMENT '是否启用定时调度';
```

**字段说明**:
- `dag_spec_path`: 存储DAG定义文件的相对路径,如 `dataflow-order/dag-spec.json`
- `schedule_cron`: 标准Cron表达式,如 `0 0 2 * * ?` (每天凌晨2点)
- `enable_schedule`: 是否启用定时调度的开关

**设计说明**:

**为什么选择文件存储而非数据库存储?**

| 对比项 | 数据库TEXT字段 | 文件系统 | 结论 |
|--------|---------------|---------|------|
| **多节点共享** | ✅ 天然支持 | ❌ 需要共享存储 | TIS-Console单实例,无此需求 |
| **性能** | ❌ 大TEXT字段I/O慢 | ✅ 文件读取快 | 文件存储更优 |
| **版本管理** | ❌ 需要额外表 | ✅ 原生Git支持 | 文件存储更优 |
| **开发友好** | ❌ 需要序列化/反序列化 | ✅ 可直接查看/编辑 | 文件存储更优 |
| **备份恢复** | ⚠️ 依赖数据库备份 | ✅ 独立备份,更灵活 | 文件存储更优 |
| **符合现有架构** | ❌ WorkFlow已有gitPath | ✅ 保持一致 | 文件存储更优 |

**结论**: TIS-Console单实例部署,文件存储优势明显,且与现有的`gitPath`字段保持一致

### 4.2 工作流实例表 (workflow_build_history)

**扩展字段**:

```sql
ALTER TABLE workflow_build_history
ADD COLUMN dag_runtime TEXT COMMENT 'DAG运行时状态JSON,包含所有节点执行状态',
ADD COLUMN wf_context TEXT COMMENT '工作流上下文,节点间数据共享',
ADD COLUMN instance_status VARCHAR(32) COMMENT '实例状态:WAITING/RUNNING/SUCCEED/FAILED/STOPPED';
```

**字段说明**:
- `dag_runtime`: 存储运行时的DAG状态,每个节点的status/result/startTime/finishedTime都在这里
- `wf_context`: 工作流全局上下文,JSON格式的Map<String,String>,用于节点间传递数据
- `instance_status`: 工作流实例的整体状态

**状态枚举**:
- `WAITING`: 等待调度
- `RUNNING`: 运行中
- `SUCCEED`: 执行成功
- `FAILED`: 执行失败
- `STOPPED`: 人工停止

### 4.3 DAG节点执行详情表 (dag_node_execution)

**新建表**:

```sql
CREATE TABLE dag_node_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_instance_id INT NOT NULL COMMENT '关联workflow_build_history.id',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(32) NOT NULL COMMENT '节点类型:TASK/CONTROL',
    task_name VARCHAR(256) COMMENT '关联的任务名称',
    status VARCHAR(32) COMMENT '节点状态:WAITING/RUNNING/SUCCEED/FAILED/CANCELED',
    result TEXT COMMENT '节点执行结果',
    start_time DATETIME COMMENT '开始时间',
    finished_time DATETIME COMMENT '完成时间',
    skip_when_failed TINYINT(1) DEFAULT 0 COMMENT '失败时是否跳过',
    enable TINYINT(1) DEFAULT 1 COMMENT '节点是否启用',
    retry_times INT DEFAULT 0 COMMENT '重试次数',
    worker_address VARCHAR(128) COMMENT '执行节点地址(集群模式)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_wf_instance(workflow_instance_id),
    INDEX idx_node_id(node_id),
    INDEX idx_status(status),
    INDEX idx_worker(worker_address)
) COMMENT='DAG节点执行详情表';
```

**设计说明**:
- 细粒度记录每个节点的执行信息
- 支持按状态查询,便于监控等待队列和执行队列
- `worker_address`用于集群模式下跟踪任务执行位置,便于故障恢复

### 4.4 DAO层接口

#### 4.4.1 IWorkFlowDAO 扩展

```java
public interface IWorkFlowDAO {
    // 现有方法...

    // 新增方法
    WorkFlow selectByPrimaryKey(Integer id);
    List<WorkFlow> selectScheduledWorkflows(); // 查询所有启用定时调度的工作流
}
```

#### 4.4.2 IWorkFlowBuildHistoryDAO 扩展

```java
public interface IWorkFlowBuildHistoryDAO {
    // 现有方法...

    // 新增方法
    Integer insert(WorkFlowBuildHistory record);
    WorkFlowBuildHistory selectByPrimaryKey(Integer id);
    WorkFlowBuildHistory loadFromWriteDB(Integer id); // 加行锁查询
    int updateByPrimaryKeySelective(WorkFlowBuildHistory record);
    List<WorkFlowBuildHistory> selectStuckInstances(int timeoutMinutes); // 查询卡住的实例
}
```

#### 4.4.3 IDAGNodeExecutionDAO (新建)

```java
public interface IDAGNodeExecutionDAO {
    /**
     * 插入节点执行记录
     */
    Integer insert(DAGNodeExecution record);

    /**
     * 根据工作流实例ID查询所有节点
     */
    List<DAGNodeExecution> selectByWorkflowInstanceId(Integer wfInstanceId);

    /**
     * 根据节点ID查询
     */
    DAGNodeExecution selectByNodeId(Integer wfInstanceId, Long nodeId);

    /**
     * 更新节点状态
     */
    int updateStatus(Long id, String status, String result, Date finishedTime);

    /**
     * 查询运行中的节点
     */
    List<DAGNodeExecution> selectRunningNodes(Integer wfInstanceId);

    /**
     * 查询等待中的节点
     */
    List<DAGNodeExecution> selectWaitingNodes(Integer wfInstanceId);

    /**
     * 查询指定Worker上运行的任务(集群模式)
     */
    List<DAGNodeExecution> selectRunningNodesByWorker(String workerAddress);
}
```

### 4.5 DAG文件存储设计

#### 4.5.1 文件存储目录结构

**设计思想**: 将DAG定义文件存储在文件系统中,便于版本管理、开发调试和备份恢复

**目录结构**:

```
${TIS_HOME}/
  ├── workflow/
  │   ├── dataflow-order-process/           # 工作流目录(以工作流name命名)
  │   │   ├── dag-spec.json                 # ← DAG拓扑定义文件
  │   │   ├── task-configs/                 # 任务配置目录(可选)
  │   │   │   ├── dump-order.json
  │   │   │   ├── join-detail.json
  │   │   │   └── build-index.json
  │   │   ├── metadata.json                 # 工作流元数据(可选)
  │   │   └── .git/                         # Git版本控制(可选)
  │   │
  │   ├── dataflow-customer-sync/
  │   │   ├── dag-spec.json
  │   │   └── ...
  │   │
  │   └── realtime-order-sync/
  │       ├── dag-spec.json
  │       └── ...
```

#### 4.5.2 dag-spec.json文件格式

**文件内容**: PEWorkflowDAG对象序列化后的JSON

**示例**:

```json
{
  "nodes": [
    {
      "nodeId": 1,
      "nodeType": 1,
      "jobId": 100,
      "nodeName": "dump_order_table",
      "enable": true,
      "skipWhenFailed": false,
      "status": 1,
      "nodeParams": null
    },
    {
      "nodeId": 2,
      "nodeType": 1,
      "jobId": 101,
      "nodeName": "join_order_detail",
      "enable": true,
      "skipWhenFailed": false,
      "status": 1
    },
    {
      "nodeId": 3,
      "nodeType": 1,
      "jobId": 102,
      "nodeName": "build_search_index",
      "enable": true,
      "skipWhenFailed": true,
      "status": 1
    }
  ],
  "edges": [
    {
      "from": 1,
      "to": 2,
      "enable": true
    },
    {
      "from": 2,
      "to": 3,
      "enable": true
    }
  ]
}
```

#### 4.5.3 文件存储优势

与数据库存储相比,文件存储的优势:

| 优势项 | 说明 |
|--------|------|
| **开发友好** | 可直接用文本编辑器查看和修改,无需数据库工具 |
| **版本管理** | 原生支持Git版本控制,便于追溯历史变更 |
| **性能** | 读取大型DAG定义比数据库TEXT字段更快 |
| **备份恢复** | 可以独立备份,不依赖数据库备份 |
| **架构一致** | 与WorkFlow现有的gitPath字段保持一致 |
| **导入导出** | 可以直接复制文件进行工作流迁移 |

#### 4.5.4 Git版本管理集成(可选)

**设计**: 每次修改DAG定义时,自动提交到Git仓库

**实现步骤**:
1. 在工作流目录下初始化Git仓库
2. 每次保存dag-spec.json时提交一个commit
3. commit message格式: `Update DAG spec - {operation} by {user} at {timestamp}`

**优势**:
- 可以查看DAG定义的历史变更记录
- 支持回滚到历史版本
- 可以对比不同版本的差异

**示例Git历史**:

```bash
$ cd ${TIS_HOME}/workflow/dataflow-order-process
$ git log --oneline

a3f5b21 Update DAG spec - add build_index node by admin at 2026-01-12 10:30:00
c7d2e45 Update DAG spec - modify join node params by admin at 2026-01-10 15:20:00
9a1b3c8 Update DAG spec - initial creation by admin at 2026-01-08 09:00:00
```

### 4.6 WorkflowDAGFileManager - DAG文件管理器

#### 4.6.1 核心职责

**类名**: `WorkflowDAGFileManager`

**包路径**: `com.qlangtech.tis.workflow.pojo`

**职责**:
- 保存DAG定义到文件系统
- 从文件系统加载DAG定义
- 支持Git版本管理(可选)
- 提供文件路径解析
- 处理文件读写异常

#### 4.6.2 完整实现

```java
package com.qlangtech.tis.workflow.pojo;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.powerjob.PEWorkflowDAG;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * DAG文件管理器
 * 负责DAG定义文件的读写和版本管理
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-12
 */
public class WorkflowDAGFileManager {

    private static final Logger log = LoggerFactory.getLogger(WorkflowDAGFileManager.class);

    /** DAG定义文件名 */
    private static final String DAG_SPEC_FILENAME = "dag-spec.json";

    /** 工作流基础目录 */
    private final File workflowBaseDir;

    /** 是否启用Git版本管理 */
    private final boolean enableGitVersion;

    /**
     * 构造函数
     */
    public WorkflowDAGFileManager() {
        this(new File(TIS.get().getDataDir(), "workflow"), false);
    }

    /**
     * 构造函数
     * @param workflowBaseDir 工作流基础目录
     * @param enableGitVersion 是否启用Git版本管理
     */
    public WorkflowDAGFileManager(File workflowBaseDir, boolean enableGitVersion) {
        this.workflowBaseDir = Objects.requireNonNull(workflowBaseDir,
            "workflowBaseDir cannot be null");
        this.enableGitVersion = enableGitVersion;

        // 确保基础目录存在
        if (!workflowBaseDir.exists()) {
            if (!workflowBaseDir.mkdirs()) {
                throw new IllegalStateException(
                    "Failed to create workflow base directory: " + workflowBaseDir);
            }
        }

        log.info("WorkflowDAGFileManager initialized: baseDir={}, gitEnabled={}",
            workflowBaseDir.getAbsolutePath(), enableGitVersion);
    }

    /**
     * 保存DAG定义到文件
     *
     * @param workflowId 工作流ID
     * @param workflowName 工作流名称
     * @param dag DAG定义对象
     * @return 相对路径(存储到数据库的dag_spec_path字段)
     */
    public String saveDagSpec(Integer workflowId, String workflowName, PEWorkflowDAG dag) {
        if (StringUtils.isEmpty(workflowName)) {
            throw new IllegalArgumentException("workflowName cannot be empty");
        }
        Objects.requireNonNull(dag, "dag cannot be null");

        try {
            // 构建文件路径
            String relativePath = workflowName + "/" + DAG_SPEC_FILENAME;
            File dagFile = new File(workflowBaseDir, relativePath);

            // 确保父目录存在
            File parentDir = dagFile.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + parentDir);
                }
            }

            // 序列化DAG为JSON(格式化输出,便于阅读)
            String jsonContent = JSON.toJSONString(dag, true);

            // 写入文件
            FileUtils.writeStringToFile(dagFile, jsonContent, StandardCharsets.UTF_8);

            log.info("Saved DAG spec to file: workflowId={}, path={}, nodeCount={}, edgeCount={}",
                workflowId, dagFile.getAbsolutePath(),
                dag.getNodes() != null ? dag.getNodes().size() : 0,
                dag.getEdges() != null ? dag.getEdges().size() : 0);

            // 如果启用Git版本管理,提交变更
            if (enableGitVersion) {
                commitDagSpec(workflowName, "Update DAG spec for workflow: " + workflowName);
            }

            return relativePath;

        } catch (IOException e) {
            log.error("Failed to save DAG spec: workflowId={}, workflowName={}",
                workflowId, workflowName, e);
            throw new IllegalStateException("Failed to save DAG spec file", e);
        }
    }

    /**
     * 从文件加载DAG定义
     *
     * @param dagSpecPath 相对路径(来自数据库的dag_spec_path字段)
     * @return DAG定义对象
     */
    public PEWorkflowDAG loadDagSpec(String dagSpecPath) {
        if (StringUtils.isEmpty(dagSpecPath)) {
            throw new IllegalArgumentException("dagSpecPath cannot be empty");
        }

        try {
            File dagFile = new File(workflowBaseDir, dagSpecPath);

            if (!dagFile.exists()) {
                throw new IllegalStateException(
                    "DAG spec file not found: " + dagFile.getAbsolutePath());
            }

            // 读取文件内容
            String jsonContent = FileUtils.readFileToString(dagFile, StandardCharsets.UTF_8);

            // 反序列化为DAG对象
            PEWorkflowDAG dag = JSON.parseObject(jsonContent, PEWorkflowDAG.class);

            if (dag == null) {
                throw new IllegalStateException(
                    "Failed to parse DAG spec file: " + dagFile.getAbsolutePath());
            }

            log.info("Loaded DAG spec from file: path={}, nodeCount={}, edgeCount={}",
                dagFile.getAbsolutePath(),
                dag.getNodes() != null ? dag.getNodes().size() : 0,
                dag.getEdges() != null ? dag.getEdges().size() : 0);

            return dag;

        } catch (IOException e) {
            log.error("Failed to load DAG spec: dagSpecPath={}", dagSpecPath, e);
            throw new IllegalStateException("Failed to load DAG spec file", e);
        }
    }

    /**
     * 提交DAG变更到Git仓库(可选功能)
     *
     * @param workflowName 工作流名称
     * @param commitMessage 提交信息
     */
    public void commitDagSpec(String workflowName, String commitMessage) {
        if (!enableGitVersion) {
            log.debug("Git version control is disabled, skip commit");
            return;
        }

        try {
            File workflowDir = new File(workflowBaseDir, workflowName);

            if (!workflowDir.exists()) {
                log.warn("Workflow directory not found, skip Git commit: {}",
                    workflowDir.getAbsolutePath());
                return;
            }

            // 检查是否已初始化Git仓库
            File gitDir = new File(workflowDir, ".git");
            if (!gitDir.exists()) {
                // 初始化Git仓库
                GitUtils.init(workflowDir);
                log.info("Initialized Git repository: {}", workflowDir.getAbsolutePath());
            }

            // 添加文件到暂存区
            GitUtils.add(workflowDir, DAG_SPEC_FILENAME);

            // 提交变更
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String fullMessage = commitMessage + " at " + timestamp;
            GitUtils.commit(workflowDir, fullMessage);

            log.info("Committed DAG spec to Git: workflow={}, message={}",
                workflowName, fullMessage);

        } catch (Exception e) {
            log.error("Failed to commit DAG spec to Git: workflowName={}",
                workflowName, e);
            // 不抛出异常,Git提交失败不影响主流程
        }
    }

    /**
     * 获取工作流目录
     *
     * @param workflowName 工作流名称
     * @return 工作流目录
     */
    public File getWorkflowDir(String workflowName) {
        return new File(workflowBaseDir, workflowName);
    }

    /**
     * 检查DAG文件是否存在
     *
     * @param dagSpecPath 相对路径
     * @return 是否存在
     */
    public boolean exists(String dagSpecPath) {
        if (StringUtils.isEmpty(dagSpecPath)) {
            return false;
        }
        File dagFile = new File(workflowBaseDir, dagSpecPath);
        return dagFile.exists();
    }

    /**
     * 删除DAG文件
     *
     * @param dagSpecPath 相对路径
     */
    public void deleteDagSpec(String dagSpecPath) {
        if (StringUtils.isEmpty(dagSpecPath)) {
            return;
        }

        try {
            File dagFile = new File(workflowBaseDir, dagSpecPath);
            if (dagFile.exists()) {
                FileUtils.forceDelete(dagFile);
                log.info("Deleted DAG spec file: {}", dagFile.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to delete DAG spec file: {}", dagSpecPath, e);
            throw new IllegalStateException("Failed to delete DAG spec file", e);
        }
    }

    /**
     * Git工具类(简化实现,实际可以使用JGit)
     */
    private static class GitUtils {

        static void init(File dir) throws IOException {
            // 简化实现:执行git init命令
            ProcessBuilder pb = new ProcessBuilder("git", "init");
            pb.directory(dir);
            Process process = pb.start();
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("Git init failed with exit code: " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Git init interrupted", e);
            }
        }

        static void add(File dir, String filename) throws IOException {
            ProcessBuilder pb = new ProcessBuilder("git", "add", filename);
            pb.directory(dir);
            Process process = pb.start();
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("Git add failed with exit code: " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Git add interrupted", e);
            }
        }

        static void commit(File dir, String message) throws IOException {
            ProcessBuilder pb = new ProcessBuilder("git", "commit", "-m", message);
            pb.directory(dir);
            Process process = pb.start();
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("Git commit failed with exit code: " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Git commit interrupted", e);
            }
        }
    }
}
```

#### 4.6.3 使用示例

**创建工作流时保存DAG**:

```java
WorkflowDAGFileManager fileManager = new WorkflowDAGFileManager();

// 创建DAG定义
PEWorkflowDAG dag = new PEWorkflowDAG();
// ... 构建DAG节点和边

// 保存到文件
String dagSpecPath = fileManager.saveDagSpec(
    workflow.getId(),
    workflow.getName(),
    dag
);

// 更新数据库
workflow.setDagSpecPath(dagSpecPath);
workflowDAO.updateByPrimaryKeySelective(workflow);
```

**加载DAG用于执行**:

```java
WorkflowDAGFileManager fileManager = new WorkflowDAGFileManager();

// 从数据库获取路径
WorkFlow workflow = workflowDAO.selectByPrimaryKey(workflowId);
String dagSpecPath = workflow.getDagSpecPath();

// 从文件加载DAG
PEWorkflowDAG dag = fileManager.loadDagSpec(dagSpecPath);

// 使用DAG执行工作流
// ...
```

## 五、Akka执行层详细设计

### 5.1 Actor体系结构

```
ActorSystem: TIS-DAG-System
│
├─ /user/dag-scheduler (DAGSchedulerActor)
│   └─ 职责: 工作流生命周期管理,DAG流转控制
│
├─ /user/dag-monitor (DAGMonitorActor)
│   └─ 职责: 实时监控,状态查询,队列统计
│
├─ /user/cluster-manager (ClusterManagerActor)
│   └─ 职责: 集群成员管理,故障恢复
│
└─ /user/node-dispatcher-{instanceId} (NodeDispatcherActor)
    └─ 职责: 任务分发路由,超时监控
    └─ children: task-worker-pool (TaskWorkerActor Pool)
        └─ 职责: 实际执行任务
```

### 5.2 消息协议设计

#### 5.2.1 调度器消息 (DAGSchedulerActor)

```java
// 启动工作流
class StartWorkflow {
    Integer workflowInstanceId;
    Map<String, String> initParams;
}

// 节点完成
class NodeCompleted {
    Integer workflowInstanceId;
    Long nodeId;
    String status;  // SUCCEED/FAILED
    String result;
}

// 节点超时
class NodeTimeout {
    Integer workflowInstanceId;
    Long nodeId;
}

// 更新工作流上下文
class UpdateContext {
    Integer workflowInstanceId;
    Map<String, String> contextData;
}

// 取消工作流
class CancelWorkflow {
    Integer workflowInstanceId;
}
```

#### 5.2.2 分发器消息 (NodeDispatcherActor)

```java
// 分发任务
class DispatchTask {
    Integer workflowInstanceId;
    PEWorkflowDAG.Node node;
}

// 任务执行消息
class TaskExecutionMessage {
    TaskExecutionContext context;
    PEWorkflowDAG.Node node;
}
```

#### 5.2.3 监控器消息 (DAGMonitorActor)

```java
// 查询工作流状态
class QueryWorkflowStatus {
    Integer workflowInstanceId;
}

// 查询等待队列
class QueryWaitingQueue { }

// 查询执行队列
class QueryRunningQueue { }

// 工作流运行时状态(响应)
class WorkflowRuntimeStatus {
    Integer instanceId;
    String status;
    List<NodeStatus> nodes;
    long startTime;
    long updateTime;
}
```

#### 5.2.4 集群管理消息 (ClusterManagerActor)

```java
// Akka内置消息,订阅即可
// - MemberUp: 节点上线
// - MemberRemoved: 节点移除
// - UnreachableMember: 节点不可达
```

### 5.3 核心Actor实现要点

#### 5.3.1 DAGSchedulerActor

**关键方法**:

```java
public class DAGSchedulerActor extends AbstractActor {

    /**
     * 启动工作流
     * - 加载DAG定义
     * - 计算就绪节点
     * - 处理控制节点(循环直到没有控制节点)
     * - 分发任务节点
     */
    private void handleStartWorkflow(StartWorkflow msg);

    /**
     * 处理节点完成(核心流转逻辑)
     * - 更新节点状态
     * - 检查失败策略
     * - 计算新就绪节点
     * - 处理控制节点
     * - 判断是否全部完成
     * - 继续分发或标记完成
     */
    private void handleNodeCompleted(NodeCompleted msg);

    /**
     * 处理超时
     * - 标记节点失败
     * - 触发重试或跳过
     */
    private void handleNodeTimeout(NodeTimeout msg);

    /**
     * 更新工作流上下文
     * - 合并新数据到wfContext
     * - 持久化
     */
    private void handleUpdateContext(UpdateContext msg);
}
```

**并发控制**:
- 使用数据库行锁: `loadFromWriteDB(instanceId)` 执行 `SELECT ... FOR UPDATE`
- Actor单线程处理消息,天然串行化

#### 5.3.2 NodeDispatcherActor

**关键方法**:

```java
public class NodeDispatcherActor extends AbstractActor {

    private ActorRef taskRouter;

    /**
     * 初始化路由器
     * - 单机模式: RoundRobinPool
     * - 集群模式: ClusterRouterPool
     */
    @Override
    public void preStart() {
        taskRouter = createTaskRouter();
    }

    /**
     * 分发任务
     * - 创建任务执行上下文
     * - 加载工作流上下文
     * - 更新节点状态为RUNNING
     * - 路由到Worker
     * - 设置超时监控
     */
    private void handleDispatchTask(DispatchTask msg);

    /**
     * 创建路由器
     */
    private ActorRef createTaskRouter() {
        if (isSingleMode()) {
            // 单机: 本地池化
            return getContext().actorOf(
                new RoundRobinPool(Runtime.getRuntime().availableProcessors() * 2)
                    .props(Props.create(TaskWorkerActor.class)),
                "task-worker-pool"
            );
        } else {
            // 集群: 跨节点路由
            return getContext().actorOf(
                new ClusterRouterPool(
                    new RoundRobinPool(10),
                    new ClusterRouterPoolSettings(100, 10, true, "worker")
                ).props(Props.create(TaskWorkerActor.class)),
                "task-worker-cluster-pool"
            );
        }
    }
}
```

**路由策略**:
- 单机: `RoundRobinPool` - 轮询本地Worker
- 集群: `ClusterRouterPool` - 跨节点轮询

#### 5.3.3 TaskWorkerActor

**关键方法**:

```java
public class TaskWorkerActor extends AbstractActor {

    /**
     * 执行任务
     * - 查找Task实现
     * - 注入工作流上下文
     * - 执行任务
     * - 收集输出到上下文
     * - 通知调度器完成
     */
    private void executeTask(TaskExecutionMessage msg) {
        String status = "SUCCEED";
        String result = null;
        Map<String, String> contextData = new HashMap<>();

        try {
            // 1. 查找Task实现
            DataflowTask task = findTaskImpl(msg.node, msg.context);

            // 2. 注入上下文
            if (task instanceof WorkflowContextAware) {
                ((WorkflowContextAware) task).setWorkflowContext(
                    msg.context.getWorkflowContext()
                );
            }

            // 3. 执行
            task.run();

            // 4. 收集输出
            if (task instanceof WorkflowContextProducer) {
                contextData = ((WorkflowContextProducer) task).produceContext();
            }

            result = "task completed successfully";

        } catch (Exception e) {
            status = "FAILED";
            result = "task failed: " + e.getMessage();
            log.error("Task execution failed", e);
        }

        // 5. 更新上下文
        if (!contextData.isEmpty()) {
            ActorRef scheduler = getContext().actorFor("/user/dag-scheduler");
            scheduler.tell(new UpdateContext(
                msg.context.getWorkflowInstanceId(), contextData
            ), getSelf());
        }

        // 6. 通知完成
        ActorRef scheduler = getContext().actorFor("/user/dag-scheduler");
        scheduler.tell(new NodeCompleted(
            msg.context.getWorkflowInstanceId(),
            msg.node.getNodeId(),
            status,
            result
        ), getSelf());
    }
}
```

**任务隔离**:
- 每个Worker独立执行,异常不影响其他Worker
- Actor Supervisor策略:遇到异常重启Actor,不影响整个系统

#### 5.3.4 DAGMonitorActor

**关键方法**:

```java
public class DAGMonitorActor extends AbstractActor {

    // 内存缓存
    private final Map<Integer, WorkflowRuntimeStatus> runtimeStatusCache = new ConcurrentHashMap<>();

    /**
     * 更新节点状态到缓存
     * - 订阅NodeCompleted事件
     * - 更新内存缓存
     */
    private void updateNodeStatus(NodeCompleted msg);

    /**
     * 查询工作流状态
     * - 优先从缓存读取
     * - 缓存未命中则查数据库
     */
    private void handleQueryStatus(QueryWorkflowStatus msg);

    /**
     * 查询等待队列
     * - 统计所有status=WAITING的节点
     */
    private void handleQueryWaiting(QueryWaitingQueue msg);

    /**
     * 查询执行队列
     * - 统计所有status=RUNNING的节点
     */
    private void handleQueryRunning(QueryRunningQueue msg);
}
```

**性能优化**:
- 内存缓存减少数据库查询
- 订阅事件实时更新缓存
- 定期刷新缓存(防止数据不一致)

#### 5.3.5 ClusterManagerActor

**关键方法**:

```java
public class ClusterManagerActor extends AbstractActor {

    private final Cluster cluster = Cluster.get(getContext().getSystem());

    @Override
    public void preStart() {
        // 订阅集群事件
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
            MemberEvent.class, UnreachableMember.class);
    }

    /**
     * 新节点加入
     * - 记录节点信息
     * - 触发任务重新平衡
     */
    private void handleMemberUp(MemberUp msg);

    /**
     * 节点移除
     * - 查询该节点上的运行任务
     * - 标记失败并重新调度
     */
    private void handleMemberRemoved(MemberRemoved msg);

    /**
     * 节点不可达
     * - 启动故障检测
     * - 准备任务迁移
     */
    private void handleUnreachableMember(UnreachableMember msg);

    /**
     * 恢复任务
     * - 查询数据库中该节点上的RUNNING任务
     * - 重新发送NodeCompleted(FAILED)触发重试
     */
    private void recoverTasksFromNode(Member member);
}
```

**故障恢复策略**:
1. 节点下线时,立即查询该节点上的运行任务
2. 将这些任务标记为FAILED
3. 通过调度器重新调度(如果配置了重试)

### 5.4 配置文件设计

#### 5.4.1 单机模式配置

**application-standalone.conf**:

```hocon
tis.dag {
  cluster.enabled = false

  actor {
    worker-pool-size = 16  # 本地Worker线程池大小
    task-timeout = 3600000  # 任务超时时间(毫秒)
  }
}

akka {
  actor {
    provider = "local"
  }

  loggers = ["akka.event.slf4j.Slf4jLogger"]
  loglevel = "INFO"
}
```

#### 5.4.2 集群模式配置

**application-cluster.conf**:

```hocon
tis.dag {
  cluster.enabled = true

  cluster {
    seed-nodes = [
      "akka://TIS-DAG-Cluster@192.168.1.10:2551",
      "akka://TIS-DAG-Cluster@192.168.1.11:2551",
      "akka://TIS-DAG-Cluster@192.168.1.12:2551"
    ]
    roles = ["worker"]
  }

  actor {
    worker-pool-size = 10  # 每个节点的Worker数量
    task-timeout = 3600000
  }
}

akka {
  actor {
    provider = "cluster"

    serializers {
      kryo = "io.altoo.akka.serialization.kryo.KryoSerializer"
    }

    serialization-bindings {
      "com.qlangtech.tis.dag.message.TaskMessage" = kryo
    }
  }

  remote.artery {
    canonical.hostname = ${AKKA_HOST}
    canonical.port = ${AKKA_PORT}
  }

  cluster {
    seed-nodes = ${tis.dag.cluster.seed-nodes}
    roles = ${tis.dag.cluster.roles}

    # 故障检测
    failure-detector {
      threshold = 8.0
      acceptable-heartbeat-pause = 3s
      heartbeat-interval = 1s
    }
  }
}
```

#### 5.4.3 配置加载逻辑

```java
public class DAGActorSystemManager {

    public void initAdaptiveMode() {
        boolean clusterEnabled = ConfigUtils.getBoolean("tis.dag.cluster.enabled", false);

        if (clusterEnabled) {
            Config config = ConfigFactory.load("application-cluster.conf");
            system = ActorSystem.create("TIS-DAG-Cluster", config);
        } else {
            Config config = ConfigFactory.load("application-standalone.conf");
            system = ActorSystem.create("TIS-DAG-System", config);
        }

        // 创建顶层Actor
        system.actorOf(Props.create(DAGSchedulerActor.class), "dag-scheduler");
        system.actorOf(Props.create(DAGMonitorActor.class), "dag-monitor");

        if (clusterEnabled) {
            system.actorOf(Props.create(ClusterManagerActor.class), "cluster-manager");
        }
    }
}
```

### 5.5 动态扩容和集群管理

#### 5.5.1 运行时动态添加Worker节点

**核心特性**: Akka Cluster完全支持运行时动态添加和移除节点,无需重启现有集群。

**Seed Nodes的作用**:

Seed Nodes(种子节点)不是集群中唯一可以存在的节点,它们的作用是:

1. **集群引导**: 帮助新节点发现集群
2. **初始联系点**: 新节点启动时首先尝试连接seed nodes
3. **不是限制**: 集群中可以有任意数量的非seed节点
4. **应该是稳定节点**: Seed nodes建议选择长期运行的核心节点

**关键理解**:
- 配置文件中的`seed-nodes`只是告诉新节点"去哪里找集群"
- 加入集群后,节点会自动获取完整的成员列表
- 新Worker节点可以随时启动并自动加入集群

#### 5.5.2 动态扩容实战指南

**场景**: TIS运行期间,任务积压严重,需要临时增加3个Worker节点提高吞吐率

**步骤1: 准备新Worker节点配置**

创建专用的Worker配置文件 `application-worker.conf`:

```hocon
# 新Worker节点专用配置
tis.dag {
  cluster.enabled = true

  cluster {
    # 使用与现有集群相同的seed nodes配置
    seed-nodes = [
      "akka://TIS-DAG-Cluster@tis-node-1:2551",
      "akka://TIS-DAG-Cluster@tis-node-2:2551",
      "akka://TIS-DAG-Cluster@tis-node-3:2551"
    ]
    # 标记为worker角色
    roles = ["worker"]
  }

  actor {
    worker-pool-size = 10
    task-timeout = 3600000
  }
}

akka {
  actor {
    provider = "cluster"

    serializers {
      kryo = "io.altoo.akka.serialization.kryo.KryoSerializer"
    }

    serialization-bindings {
      "com.qlangtech.tis.dag.message.TaskMessage" = kryo
    }
  }

  remote.artery {
    # 新节点的地址(通过环境变量注入)
    canonical.hostname = ${AKKA_HOST}
    canonical.port = ${AKKA_PORT}
  }

  cluster {
    seed-nodes = ${tis.dag.cluster.seed-nodes}
    roles = ${tis.dag.cluster.roles}

    # 种子节点连接超时
    seed-node-timeout = 5s

    # 允许弱连接成员快速加入(加快扩容速度)
    allow-weakly-up-members = on

    # 故障检测(适当放宽,避免误判)
    failure-detector {
      threshold = 12.0
      acceptable-heartbeat-pause = 5s
      heartbeat-interval = 1s
    }
  }
}
```

**步骤2: 启动新Worker节点**

```bash
# 在新服务器上启动TIS(Worker模式)
export AKKA_HOST=192.168.1.100  # 新节点IP
export AKKA_PORT=2551

java -jar \
  -Dconfig.file=application-worker.conf \
  -Xmx4g \
  tis-web-start.jar
```

**步骤3: 自动加入流程**

```
新Worker节点启动
    │
    ▼
读取seed-nodes配置
    │
    ▼
尝试连接任意一个seed node
    │
    ▼
通过seed node获取当前集群成员列表
    │
    ▼
发送Join消息到集群
    │
    ▼
Seed nodes验证并接受加入请求
    │
    ▼
集群广播MemberUp事件
    │
    ▼
所有节点收到通知,更新成员列表
    │
    ▼
ClusterRouterPool自动将新节点纳入路由
    │
    ▼
新Worker开始接收任务分发
```

**步骤4: 验证节点加入成功**

```bash
# 查询集群状态
curl http://tis-node-1:8080/api/cluster/status

# 响应示例
{
  "code": 0,
  "data": {
    "selfAddress": "akka://TIS-DAG-Cluster@tis-node-1:2551",
    "leader": "akka://TIS-DAG-Cluster@tis-node-1:2551",
    "totalMembers": 6,
    "upMembers": 6,
    "members": [
      {
        "address": "akka://TIS-DAG-Cluster@tis-node-1:2551",
        "status": "Up",
        "roles": "seed-node,worker"
      },
      {
        "address": "akka://TIS-DAG-Cluster@tis-node-2:2551",
        "status": "Up",
        "roles": "seed-node,worker"
      },
      {
        "address": "akka://TIS-DAG-Cluster@tis-node-3:2551",
        "status": "Up",
        "roles": "seed-node,worker"
      },
      {
        "address": "akka://TIS-DAG-Cluster@192.168.1.100:2551",
        "status": "Up",
        "roles": "worker"
      },
      {
        "address": "akka://TIS-DAG-Cluster@192.168.1.101:2551",
        "status": "Up",
        "roles": "worker"
      },
      {
        "address": "akka://TIS-DAG-Cluster@192.168.1.102:2551",
        "status": "Up",
        "roles": "worker"
      }
    ]
  }
}
```

#### 5.5.3 集群事件监听和自动负载均衡

扩展`DAGActorSystemManager`以支持集群事件监听:

```java
public class DAGActorSystemManager {

    private ActorSystem system;
    private Cluster cluster;

    /**
     * 初始化集群模式
     */
    public void initClusterMode() {
        Config config = ConfigFactory.load("application-cluster.conf");
        system = ActorSystem.create("TIS-DAG-Cluster", config);
        cluster = Cluster.get(system);

        // 创建顶层Actor
        system.actorOf(Props.create(DAGSchedulerActor.class), "dag-scheduler");
        system.actorOf(Props.create(DAGMonitorActor.class), "dag-monitor");

        // 创建集群事件监听器
        ActorRef listener = system.actorOf(
            Props.create(ClusterEventListener.class),
            "cluster-event-listener"
        );

        // 订阅集群事件
        cluster.subscribe(
            listener,
            ClusterEvent.initialStateAsEvents(),
            MemberEvent.class,
            UnreachableMember.class
        );

        log.info("Cluster initialized: address={}, roles={}",
            cluster.selfAddress(), cluster.selfRoles());
    }

    /**
     * 集群事件监听器
     */
    public static class ClusterEventListener extends AbstractActor {

        private final Cluster cluster = Cluster.get(getContext().getSystem());
        private final Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                .match(MemberUp.class, this::handleMemberUp)
                .match(MemberRemoved.class, this::handleMemberRemoved)
                .match(UnreachableMember.class, this::handleUnreachable)
                .match(MemberExited.class, this::handleMemberExited)
                .build();
        }

        /**
         * 新节点加入集群
         */
        private void handleMemberUp(MemberUp event) {
            Member member = event.member();

            log.info("=== New member joined cluster ===");
            log.info("  Address: {}", member.address());
            log.info("  Roles: {}", member.getRoles());
            log.info("  Status: {}", member.status());

            // 如果是worker节点,触发任务重新平衡
            if (member.hasRole("worker")) {
                int workerCount = countWorkerNodes();
                log.info("New worker node available, total workers: {}", workerCount);
                log.info("Triggering task rebalancing...");

                rebalanceTasks(workerCount);
            }

            // 发送系统事件通知
            getContext().getSystem().eventStream().publish(
                new WorkerNodeAdded(
                    member.address().toString(),
                    member.getRoles().mkString(",")
                )
            );

            // 可选:更新监控指标
            updateClusterMetrics();
        }

        /**
         * 节点从集群移除
         */
        private void handleMemberRemoved(MemberRemoved event) {
            Member member = event.member();

            log.warn("=== Member removed from cluster ===");
            log.warn("  Address: {}", member.address());
            log.warn("  Previous status: {}", event.previousStatus());

            // 如果是worker节点,恢复该节点上的任务
            if (member.hasRole("worker")) {
                log.warn("Worker node lost, recovering tasks...");
                recoverTasksFromNode(member);
            }

            // 发送系统事件通知
            getContext().getSystem().eventStream().publish(
                new WorkerNodeRemoved(member.address().toString())
            );
        }

        /**
         * 节点不可达
         */
        private void handleUnreachable(UnreachableMember event) {
            log.warn("=== Member unreachable ===");
            log.warn("  Address: {}", event.member().address());

            // 标记该节点上的任务状态,准备故障转移
            if (event.member().hasRole("worker")) {
                markTasksAsSuspect(event.member());
            }
        }

        /**
         * 节点正常退出
         */
        private void handleMemberExited(MemberExited event) {
            log.info("=== Member exited gracefully ===");
            log.info("  Address: {}", event.member().address());
        }

        /**
         * 统计Worker节点数量
         */
        private int countWorkerNodes() {
            return (int) cluster.state().getMembers().stream()
                .filter(m -> m.hasRole("worker"))
                .filter(m -> m.status() == MemberStatus.up())
                .count();
        }

        /**
         * 任务重新平衡
         */
        private void rebalanceTasks(int newWorkerCount) {
            // 1. 计算建议的Router池大小
            int poolSize = newWorkerCount * 10;  // 每个worker 10个actor

            log.info("Rebalancing tasks: new pool size = {}", poolSize);

            // 2. 通知调度器更新路由配置(可选)
            ActorRef scheduler = getContext().actorFor("/user/dag-scheduler");
            scheduler.tell(new UpdateRouterPool(poolSize), getSelf());

            // 3. 重新分配等待中的任务
            // 注:ClusterRouterPool会自动发现新节点,无需手动干预
            // 这里可以实现额外的优化逻辑
        }

        /**
         * 恢复任务
         */
        private void recoverTasksFromNode(Member member) {
            // 查询该节点上正在运行的任务
            String workerAddress = member.address().toString();

            // 通过数据库查询
            List<DAGNodeExecution> runningTasks =
                dagNodeExecutionDAO.selectRunningNodesByWorker(workerAddress);

            log.warn("Found {} running tasks on failed node, recovering...",
                runningTasks.size());

            // 重新调度这些任务
            ActorRef scheduler = getContext().actorFor("/user/dag-scheduler");

            for (DAGNodeExecution task : runningTasks) {
                // 标记任务失败,触发重试机制
                scheduler.tell(new NodeCompleted(
                    task.getWorkflowInstanceId(),
                    task.getNodeId(),
                    "FAILED",
                    "worker node down: " + workerAddress
                ), getSelf());
            }
        }

        /**
         * 标记任务为可疑状态
         */
        private void markTasksAsSuspect(Member member) {
            String workerAddress = member.address().toString();

            log.warn("Marking tasks on unreachable node as suspect: {}",
                workerAddress);

            // 启动超时监控,如果节点在一定时间内恢复则继续,否则触发故障转移
            getContext().getSystem().scheduler().scheduleOnce(
                Duration.ofSeconds(30),
                getSelf(),
                new CheckUnreachableNodeTasks(member),
                getContext().dispatcher(),
                getSelf()
            );
        }

        /**
         * 更新集群监控指标
         */
        private void updateClusterMetrics() {
            int totalMembers = cluster.state().getMembers().size();
            int upMembers = (int) cluster.state().getMembers().stream()
                .filter(m -> m.status() == MemberStatus.up())
                .count();
            int workerCount = countWorkerNodes();

            // 发布到Prometheus
            metricsCollector.recordGauge("akka.cluster.members.total", totalMembers);
            metricsCollector.recordGauge("akka.cluster.members.up", upMembers);
            metricsCollector.recordGauge("akka.cluster.workers", workerCount);
        }
    }

    /**
     * 获取当前集群状态
     */
    public ClusterStatusVO getClusterStatus() {
        if (cluster == null) {
            throw new IllegalStateException("Cluster not initialized");
        }

        Set<Member> members = cluster.state().getMembers();

        ClusterStatusVO status = new ClusterStatusVO();
        status.setSelfAddress(cluster.selfAddress().toString());
        status.setLeader(cluster.state().getLeader() != null
            ? cluster.state().getLeader().toString()
            : "unknown");

        List<MemberVO> memberList = members.stream()
            .map(m -> new MemberVO(
                m.address().toString(),
                m.status().toString(),
                m.getRoles().mkString(","),
                m.equals(cluster.state().getLeader())
            ))
            .collect(Collectors.toList());

        status.setMembers(memberList);
        status.setTotalMembers(members.size());
        status.setUpMembers((int) members.stream()
            .filter(m -> m.status() == MemberStatus.up())
            .count());
        status.setWorkerCount((int) members.stream()
            .filter(m -> m.hasRole("worker"))
            .filter(m -> m.status() == MemberStatus.up())
            .count());

        return status;
    }
}
```

#### 5.5.4 集群管理API

提供HTTP API方便运维人员管理集群:

```java
@RestController
@RequestMapping("/api/cluster")
public class ClusterManagementController {

    @Autowired
    private DAGActorSystemManager actorSystemManager;

    /**
     * 获取集群状态
     */
    @GetMapping("/status")
    public Result<ClusterStatusVO> getClusterStatus() {
        ClusterStatusVO status = actorSystemManager.getClusterStatus();
        return Result.success(status);
    }

    /**
     * 列出所有Worker节点
     */
    @GetMapping("/workers")
    public Result<List<WorkerNodeVO>> listWorkers() {
        ClusterStatusVO status = actorSystemManager.getClusterStatus();

        List<WorkerNodeVO> workers = status.getMembers().stream()
            .filter(m -> m.getRoles().contains("worker"))
            .filter(m -> "Up".equals(m.getStatus()))
            .map(m -> {
                WorkerNodeVO worker = new WorkerNodeVO();
                worker.setAddress(m.getAddress());
                worker.setStatus("active");
                worker.setIsLeader(m.getIsLeader());

                // 查询该worker上的任务数量
                int taskCount = dagNodeExecutionDAO.countRunningTasksByWorker(m.getAddress());
                worker.setRunningTasks(taskCount);

                return worker;
            })
            .collect(Collectors.toList());

        return Result.success(workers);
    }

    /**
     * 获取集群拓扑
     */
    @GetMapping("/topology")
    public Result<ClusterTopologyVO> getClusterTopology() {
        ClusterStatusVO status = actorSystemManager.getClusterStatus();

        ClusterTopologyVO topology = new ClusterTopologyVO();
        topology.setLeader(status.getLeader());
        topology.setNodes(status.getMembers());

        // 计算节点间的连接关系(简化版)
        List<ClusterTopologyVO.Link> links = new ArrayList<>();
        for (MemberVO member : status.getMembers()) {
            if (!member.getIsLeader()) {
                links.add(new ClusterTopologyVO.Link(
                    member.getAddress(),
                    status.getLeader()
                ));
            }
        }
        topology.setLinks(links);

        return Result.success(topology);
    }
}
```

**响应示例**:

```json
{
  "code": 0,
  "data": {
    "selfAddress": "akka://TIS-DAG-Cluster@tis-node-1:2551",
    "leader": "akka://TIS-DAG-Cluster@tis-node-1:2551",
    "totalMembers": 6,
    "upMembers": 6,
    "workerCount": 6,
    "members": [
      {
        "address": "akka://TIS-DAG-Cluster@tis-node-1:2551",
        "status": "Up",
        "roles": "seed-node,worker",
        "isLeader": true
      },
      {
        "address": "akka://TIS-DAG-Cluster@192.168.1.100:2551",
        "status": "Up",
        "roles": "worker",
        "isLeader": false
      }
    ]
  }
}
```

#### 5.5.5 动态扩容最佳实践

**1. Seed Nodes选择建议**:

```hocon
# 生产环境推荐配置
akka.cluster {
  # 选择3-5个稳定的核心节点作为seed nodes
  seed-nodes = [
    "akka://TIS-DAG-Cluster@${SEED_NODE_1_HOST}:${SEED_NODE_1_PORT}",
    "akka://TIS-DAG-Cluster@${SEED_NODE_2_HOST}:${SEED_NODE_2_PORT}",
    "akka://TIS-DAG-Cluster@${SEED_NODE_3_HOST}:${SEED_NODE_3_PORT}"
  ]

  # Seed nodes应该具备以下特点:
  # - 长期运行,不频繁重启
  # - 网络稳定,高可用
  # - 可以是专用的协调节点,也可以同时作为Worker
}
```

**2. 角色划分策略**:

| 角色 | 说明 | 数量建议 |
|------|------|---------|
| seed-node | 集群引导节点 | 3-5个 |
| worker | 执行任务的工作节点 | 根据负载动态调整 |
| seed-node,worker | 既是种子节点又是Worker | 推荐配置 |

**3. 扩容操作流程**:

```bash
# 1. 准备新服务器
ssh new-worker-1

# 2. 部署TIS应用
scp tis-web-start.jar new-worker-1:/opt/tis/
scp application-worker.conf new-worker-1:/opt/tis/

# 3. 设置环境变量
export AKKA_HOST=$(hostname -i)
export AKKA_PORT=2551

# 4. 启动Worker(后台运行)
nohup java -jar \
  -Dconfig.file=application-worker.conf \
  -Xmx4g -Xms4g \
  /opt/tis/tis-web-start.jar \
  > /opt/tis/logs/worker.log 2>&1 &

# 5. 验证加入成功
sleep 10
curl http://seed-node-1:8080/api/cluster/status | jq '.data.totalMembers'

# 6. 监控任务分配
curl http://seed-node-1:8080/api/cluster/workers | jq '.data[].runningTasks'
```

**4. 缩容操作流程**:

```bash
# 优雅下线Worker节点

# 1. 禁止新任务分配到该节点(可选,取决于实现)
curl -X POST http://worker-node:8080/api/cluster/drain

# 2. 等待当前任务完成
while true; do
  tasks=$(curl -s http://worker-node:8080/api/cluster/self/tasks | jq '.data.count')
  if [ "$tasks" -eq 0 ]; then
    break
  fi
  echo "Waiting for $tasks tasks to complete..."
  sleep 10
done

# 3. 优雅停止(会自动通知集群)
curl -X POST http://worker-node:8080/actuator/shutdown

# 或者直接发送SIGTERM信号
kill -TERM $(cat /opt/tis/worker.pid)
```

**5. 监控扩容效果**:

在Grafana中创建监控面板:

```promql
# 集群成员数量趋势
akka_cluster_members_total

# Worker节点数量
akka_cluster_workers

# 任务分配均衡度(标准差)
stddev(dag_task_executed_total) by (worker)

# 新节点加入后的吞吐量变化
rate(dag_task_executed_total[5m])
```

#### 5.5.6 故障场景处理

**场景1: Worker节点宕机**

```
Worker节点宕机
    │
    ▼
Akka检测到节点不可达(10s内)
    │
    ▼
触发UnreachableMember事件
    │
    ▼
ClusterEventListener标记任务为可疑
    │
    ▼
等待30秒观察是否恢复
    │
    ├─▶ 恢复: 任务继续执行
    │
    └─▶ 未恢复: 触发MemberRemoved事件
            │
            ▼
        查询该节点上的运行任务
            │
            ▼
        标记任务为FAILED
            │
            ▼
        DAGScheduler重新调度任务
            │
            ▼
        任务被分配到其他Worker执行
```

**场景2: 网络分区**

```
网络分区导致集群分裂
    │
    ▼
Akka检测到多个节点不可达
    │
    ▼
集群进入split-brain状态
    │
    ▼
Split Brain Resolver策略介入
    │
    ├─▶ Keep Majority: 保留多数派,移除少数派
    │
    ├─▶ Keep Oldest: 保留最老的节点所在分区
    │
    └─▶ Keep Referee: 保留包含特定节点的分区
    │
    ▼
少数派节点被移除,任务恢复到多数派
```

**配置Split Brain Resolver**:

```hocon
akka.cluster {
  downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"

  split-brain-resolver {
    # 策略选择
    active-strategy = keep-majority

    # 稳定期(确认分区)
    stable-after = 20s

    # keep-majority策略的角色
    keep-majority {
      role = ""
    }
  }
}
```

**场景3: Seed Node全部宕机**

```
所有Seed Nodes宕机
    │
    ▼
现有Worker节点继续正常工作(不影响任务执行)
    │
    ▼
但新节点无法加入集群
    │
    ▼
需要恢复至少1个Seed Node
    │
    ▼
或者手动将某个Worker提升为Seed Node
```

**手动提升Worker为Seed Node**:

```bash
# 1. 修改配置文件,添加seed-node角色
vi /opt/tis/application-worker.conf
# roles = ["seed-node", "worker"]

# 2. 重启该Worker节点
systemctl restart tis-worker

# 3. 更新其他节点的seed-nodes配置(逐步重启)
```

#### 5.5.7 生产环境部署建议

**小规模集群(< 10节点)**:

```yaml
部署方案:
  Seed Nodes: 3个 (同时作为Worker)
  Pure Workers: 根据负载动态添加

配置要点:
  - 所有节点使用相同的seed-nodes配置
  - Worker节点可以随时启停
  - Seed Nodes保持稳定运行
```

**中等规模集群(10-50节点)**:

```yaml
部署方案:
  Seed Nodes: 5个 (专用协调节点,不执行任务)
  Worker Nodes: 根据负载动态添加

配置要点:
  - Seed Nodes高可用部署
  - Worker使用统一的配置模板
  - 使用配置中心(Consul/Etcd)管理seed-nodes
```

**大规模集群(> 50节点)**:

```yaml
部署方案:
  Seed Nodes: 7个 (跨机房部署)
  Worker Nodes: 分组部署,每组10-20个

配置要点:
  - 使用Akka Cluster Sharding分片
  - 使用Kubernetes StatefulSet管理Seed Nodes
  - 使用Kubernetes Deployment管理Worker Nodes
  - 配置resource limits防止资源竞争
```

## 六、PowerJob核心类集成

### 6.1 需要提取的类

从PowerJob项目中提取以下核心类到TIS项目:

```
com/qlangtech/tis/powerjob/
├── model/
│   ├── PEWorkflowDAG.java          # DAG数据模型(点线表示法)
│   ├── WorkflowNodeType.java       # 节点类型枚举
│   └── InstanceStatus.java         # 实例状态枚举
├── algorithm/
│   ├── WorkflowDAG.java            # DAG运行时模型(引用表示法)
│   └── WorkflowDAGUtils.java       # DAG算法工具类
└── exception/
    └── PowerJobException.java      # 异常类
```

### 6.2 核心类说明

#### 6.2.1 PEWorkflowDAG (点线表示法)

**用途**: 可序列化的DAG数据模型,用于持久化和网络传输

**核心属性**:

```java
public class PEWorkflowDAG implements Serializable {

    // 节点列表
    private List<Node> nodes;

    // 边列表
    private List<Edge> edges;

    // 节点定义
    public static class Node {
        private Long nodeId;           // 节点唯一ID
        private Integer nodeType;      // 节点类型(任务/控制)
        private Long jobId;            // 关联的任务ID
        private String nodeName;       // 节点名称
        private Long instanceId;       // 运行时实例ID
        private String nodeParams;     // 节点参数
        private Integer status;        // 节点状态
        private String result;         // 执行结果
        private Boolean enable;        // 是否启用
        private Boolean skipWhenFailed;// 失败时跳过
        private String startTime;      // 开始时间
        private String finishedTime;   // 完成时间
    }

    // 边定义
    public static class Edge {
        private Long from;             // 起始节点ID
        private Long to;               // 目标节点ID
        private String property;       // 边属性(条件分支)
        private Boolean enable;        // 边是否启用
    }
}
```

**序列化示例**:

```json
{
  "nodes": [
    {
      "nodeId": 1,
      "nodeType": 1,
      "jobId": 100,
      "nodeName": "dump_order",
      "enable": true,
      "skipWhenFailed": false
    },
    {
      "nodeId": 2,
      "nodeType": 1,
      "jobId": 101,
      "nodeName": "join_order_detail",
      "enable": true,
      "skipWhenFailed": false
    },
    {
      "nodeId": 3,
      "nodeType": 1,
      "jobId": 102,
      "nodeName": "build_index",
      "enable": true,
      "skipWhenFailed": true
    }
  ],
  "edges": [
    {"from": 1, "to": 2, "enable": true},
    {"from": 2, "to": 3, "enable": true}
  ]
}
```

#### 6.2.2 WorkflowDAG (引用表示法)

**用途**: 运行时模型,节点直接持有引用,便于图算法

**核心属性**:

```java
public class WorkflowDAG {

    // 所有根节点(入度为0)
    private List<Node> roots;

    // nodeId -> Node映射
    private Map<Long, Node> nodeMap;

    // 节点内部类
    public static class Node {
        private Long nodeId;
        private PEWorkflowDAG.Node holder;  // 持有原始Node

        // 上游依赖节点(直接引用)
        private List<Node> dependencies;

        // 下游后继节点(直接引用)
        private List<Node> successors;

        // 依赖边映射
        private Map<Node, PEWorkflowDAG.Edge> dependenceEdgeMap;

        // 后继边映射
        private Map<Node, PEWorkflowDAG.Edge> successorEdgeMap;
    }
}
```

#### 6.2.3 WorkflowDAGUtils (算法工具类)

**核心方法**:

```java
public class WorkflowDAGUtils {

    /**
     * 校验DAG合法性
     * - 检查节点是否为空
     * - 检查节点ID是否重复
     * - 检测环路(DFS)
     * - 检测孤立环
     */
    public static boolean valid(WorkflowDAG dag);

    /**
     * 点线表示法转引用表示法
     * - 创建所有节点
     * - 建立双向引用关系
     * - 识别根节点
     */
    public static WorkflowDAG convert(PEWorkflowDAG peWorkflowDAG);

    /**
     * 获取就绪节点(核心算法)
     * - 找出所有前置依赖已完成的节点
     * - 自动跳过禁用节点
     * - 支持失败节点跳过(skipWhenFailed)
     */
    public static List<PEWorkflowDAG.Node> listReadyNodes(PEWorkflowDAG peWorkflowDAG);

    /**
     * 处理被禁用的边
     * - 找出仅能通过禁用边到达的节点
     * - 标记为CANCELED
     */
    public static void handleDisableEdges(PEWorkflowDAG dag);

    /**
     * 重置可重试节点
     * - 失败且不允许跳过的节点
     * - 被手动停止的节点
     */
    public static List<PEWorkflowDAG.Node> resetRetryableNode(PEWorkflowDAG dag);

    /**
     * 获取所有根节点
     */
    public static List<PEWorkflowDAG.Node> listRoots(PEWorkflowDAG dag);
}
```

**环检测算法(DFS)**:

```java
private static boolean invalidPath(WorkflowDAG.Node root,
                                   Set<Long> ids,
                                   Set<Long> nodeIdContainer) {
    // 递归出口: 出现之前的节点则代表有环
    if (ids.contains(root.getNodeId())) {
        return true;
    }

    nodeIdContainer.add(root.getNodeId());

    // 出现无后继者节点,则说明该路径成功
    if (root.getSuccessors().isEmpty()) {
        return false;
    }

    ids.add(root.getNodeId());

    for (WorkflowDAG.Node node : root.getSuccessors()) {
        // 每个分支使用独立的路径集合
        if (invalidPath(node, Sets.newHashSet(ids), nodeIdContainer)) {
            return true;
        }
    }

    return false;
}
```

**就绪节点算法(拓扑排序)**:

```java
public static List<PEWorkflowDAG.Node> listReadyNodes(PEWorkflowDAG dag) {
    // 1. 构建依赖关系图
    Multimap<Long, Long> relyMap = LinkedListMultimap.create();
    for (PEWorkflowDAG.Edge edge : dag.getEdges()) {
        if (edge.getEnable()) {
            relyMap.put(edge.getTo(), edge.getFrom());
        }
    }

    // 2. 遍历所有节点,找出就绪节点
    List<PEWorkflowDAG.Node> readyNodes = new ArrayList<>();
    List<PEWorkflowDAG.Node> skipNodes = new ArrayList<>();

    for (PEWorkflowDAG.Node node : dag.getNodes()) {
        // 跳过已完成的节点
        if (isCompletedStatus(node.getStatus())) {
            continue;
        }

        // 检查是否就绪
        if (!isReadyNode(node.getNodeId(), nodeId2Node, relyMap)) {
            continue;
        }

        // 区分禁用节点和正常节点
        if (node.getEnable() != null && !node.getEnable()) {
            skipNodes.add(node);
        } else {
            readyNodes.add(node);
        }
    }

    // 3. 处理禁用节点,递归找出新的就绪节点
    for (PEWorkflowDAG.Node skipNode : skipNodes) {
        skipNode.setStatus(InstanceStatus.SUCCEED.getV());
        readyNodes.addAll(moveAndObtainReadySuccessor(skipNode, nodeId2Node, relyMap, successorMap));
    }

    return readyNodes;
}

// 检查节点是否就绪
private static boolean isReadyNode(Long nodeId,
                                   Map<Long, PEWorkflowDAG.Node> nodeMap,
                                   Multimap<Long, Long> relyMap) {
    Collection<Long> dependencies = relyMap.get(nodeId);

    if (dependencies == null || dependencies.isEmpty()) {
        return true;  // 没有依赖,直接就绪
    }

    // 检查所有依赖是否完成
    for (Long depNodeId : dependencies) {
        PEWorkflowDAG.Node depNode = nodeMap.get(depNodeId);

        // 依赖未完成
        if (!isCompletedStatus(depNode.getStatus())) {
            return false;
        }

        // 依赖失败且不允许跳过
        if (depNode.getStatus() == InstanceStatus.FAILED.getV()
            && !depNode.getSkipWhenFailed()) {
            return false;
        }
    }

    return true;
}
```

### 6.3 集成到TIS

#### 6.3.1 重构DAGSessionSpec

**修改前** (tis-sql-parser/src/main/java/com/qlangtech/tis/sql/parser/DAGSessionSpec.java):

```java
public class DAGSessionSpec implements IDAGSessionSpec {
    Map<String, DAGSessionSpec> dptNodes = Maps.newHashMap();

    // 使用字符串DSL: "task1,task2->task3->task4"
    public StringBuffer buildSpec() {
        // ...
    }
}
```

**修改后**:

```java
public class DAGSessionSpec implements IDAGSessionSpec {

    // 使用PowerJob的数据模型
    private PEWorkflowDAG dag;
    private Map<String, TaskAndMilestone> taskMap;

    /**
     * 添加节点
     */
    public void addNode(Long nodeId, String nodeName, Long jobId, boolean skipWhenFailed) {
        PEWorkflowDAG.Node node = new PEWorkflowDAG.Node();
        node.setNodeId(nodeId);
        node.setNodeName(nodeName);
        node.setJobId(jobId);
        node.setNodeType(WorkflowNodeType.JOB.getCode());
        node.setEnable(true);
        node.setSkipWhenFailed(skipWhenFailed);
        node.setStatus(InstanceStatus.WAITING_DISPATCH.getV());

        dag.getNodes().add(node);
    }

    /**
     * 添加边
     */
    public void addEdge(Long fromNodeId, Long toNodeId) {
        PEWorkflowDAG.Edge edge = new PEWorkflowDAG.Edge();
        edge.setFrom(fromNodeId);
        edge.setTo(toNodeId);
        edge.setEnable(true);

        dag.getEdges().add(edge);
    }

    /**
     * 校验DAG
     */
    public boolean validate() {
        WorkflowDAG runtimeDAG = WorkflowDAGUtils.convert(this.dag);
        return WorkflowDAGUtils.valid(runtimeDAG);
    }

    /**
     * 序列化为JSON
     */
    public String toJson() {
        return JSON.toJSONString(dag);
    }

    /**
     * 从JSON反序列化
     */
    public static DAGSessionSpec fromJson(String json) {
        PEWorkflowDAG dag = JSON.parseObject(json, PEWorkflowDAG.class);
        DAGSessionSpec spec = new DAGSessionSpec();
        spec.dag = dag;
        return spec;
    }
}
```

#### 6.3.2 适配现有Task实现

**新增接口**:

```java
// 工作流上下文感知接口
public interface WorkflowContextAware {
    void setWorkflowContext(Map<String, String> context);
}

// 工作流上下文生产接口
public interface WorkflowContextProducer {
    Map<String, String> produceContext();
}
```

**改造DataflowTask**:

```java
public abstract class DataflowTask implements WorkflowContextAware {

    protected final String id;
    protected Map<String, String> workflowContext;

    @Override
    public void setWorkflowContext(Map<String, String> context) {
        this.workflowContext = context;
    }

    protected String getContextValue(String key) {
        return workflowContext != null ? workflowContext.get(key) : null;
    }

    // ... 其他方法
}
```

**改造示例 - DumpTask**:

```java
public class DumpTask extends DataflowTask implements WorkflowContextProducer {

    @Override
    public void run() throws Exception {
        // 可以读取工作流上下文
        String initParams = getContextValue("initParams");

        // 执行Dump逻辑
        // ...
    }

    @Override
    public Map<String, String> produceContext() {
        // 输出数据到工作流上下文
        Map<String, String> output = new HashMap<>();
        output.put("dumpRecordCount", String.valueOf(recordCount));
        output.put("dumpFilePath", filePath);
        return output;
    }
}
```

## 七、定时调度实现

### 7.1 基于Quartz的调度器

**核心类**:

```java
@Component
public class WorkflowScheduler {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private IWorkFlowDAO workflowDAO;

    /**
     * 应用启动时加载所有定时工作流
     */
    @PostConstruct
    public void initScheduledWorkflows() {
        List<WorkFlow> workflows = workflowDAO.selectScheduledWorkflows();

        for (WorkFlow workflow : workflows) {
            if (workflow.getEnableSchedule()) {
                scheduleWorkflow(workflow);
            }
        }
    }

    /**
     * 调度工作流
     */
    public void scheduleWorkflow(WorkFlow workflow) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(WorkflowTriggerJob.class)
                .withIdentity("workflow-" + workflow.getId(), "TIS-DAG")
                .usingJobData("workflowId", workflow.getId())
                .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-" + workflow.getId(), "TIS-DAG")
                .withSchedule(CronScheduleBuilder.cronSchedule(workflow.getScheduleCron()))
                .build();

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("Scheduled workflow: id={}, name={}, cron={}",
                workflow.getId(), workflow.getName(), workflow.getScheduleCron());

        } catch (SchedulerException e) {
            log.error("Failed to schedule workflow: " + workflow.getId(), e);
        }
    }

    /**
     * 取消调度
     */
    public void unscheduleWorkflow(Integer workflowId) {
        try {
            scheduler.deleteJob(JobKey.jobKey("workflow-" + workflowId, "TIS-DAG"));
        } catch (SchedulerException e) {
            log.error("Failed to unschedule workflow: " + workflowId, e);
        }
    }
}
```

**Job实现**:

```java
public class WorkflowTriggerJob implements Job {

    @Autowired
    private WorkflowInstanceManager instanceManager;

    @Autowired
    private DAGActorSystemManager actorSystemManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Integer workflowId = context.getJobDetail().getJobDataMap().getInt("workflowId");

        try {
            // 1. 创建工作流实例
            Integer instanceId = instanceManager.createInstance(workflowId, null);

            // 2. 触发Akka执行
            ActorRef scheduler = actorSystemManager.getSchedulerActor();
            scheduler.tell(new StartWorkflow(instanceId, new HashMap<>()), ActorRef.noSender());

            log.info("Triggered workflow execution: workflowId={}, instanceId={}",
                workflowId, instanceId);

        } catch (Exception e) {
            log.error("Failed to trigger workflow: " + workflowId, e);
            throw new JobExecutionException(e);
        }
    }
}
```

### 7.2 工作流管理API

```java
@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowScheduler scheduler;

    @Autowired
    private IWorkFlowDAO workflowDAO;

    /**
     * 创建工作流
     */
    @PostMapping("/create")
    public Result createWorkflow(@RequestBody WorkflowCreateRequest request) {
        // 1. 校验DAG
        DAGSessionSpec dagSpec = request.getDagSpec();
        if (!dagSpec.validate()) {
            return Result.error("Invalid DAG structure");
        }

        // 2. 保存工作流
        WorkFlow workflow = new WorkFlow();
        workflow.setName(request.getName());
        workflow.setDagSpec(dagSpec.toJson());
        workflow.setScheduleCron(request.getCronExpr());
        workflow.setEnableSchedule(request.isEnableSchedule());

        Integer workflowId = workflowDAO.insert(workflow);

        // 3. 注册定时调度
        if (request.isEnableSchedule()) {
            scheduler.scheduleWorkflow(workflow);
        }

        return Result.success(workflowId);
    }

    /**
     * 更新调度配置
     */
    @PutMapping("/{workflowId}/schedule")
    public Result updateSchedule(@PathVariable Integer workflowId,
                                  @RequestBody ScheduleUpdateRequest request) {
        WorkFlow workflow = workflowDAO.selectByPrimaryKey(workflowId);

        // 取消旧调度
        if (workflow.getEnableSchedule()) {
            scheduler.unscheduleWorkflow(workflowId);
        }

        // 更新配置
        workflow.setScheduleCron(request.getCronExpr());
        workflow.setEnableSchedule(request.isEnable());
        workflowDAO.updateByPrimaryKeySelective(workflow);

        // 注册新调度
        if (request.isEnable()) {
            scheduler.scheduleWorkflow(workflow);
        }

        return Result.success();
    }

    /**
     * 手动触发执行
     */
    @PostMapping("/{workflowId}/trigger")
    public Result triggerWorkflow(@PathVariable Integer workflowId,
                                   @RequestBody Map<String, String> initParams) {
        // 创建实例并触发执行
        Integer instanceId = instanceManager.createInstance(workflowId, initParams);

        ActorRef scheduler = actorSystemManager.getSchedulerActor();
        scheduler.tell(new StartWorkflow(instanceId, initParams), ActorRef.noSender());

        return Result.success(instanceId);
    }
}
```

## 八、可视化监控实现

### 8.1 查询API

```java
@RestController
@RequestMapping("/api/dag/monitor")
public class DAGMonitorController {

    @Autowired
    private DAGActorSystemManager actorSystemManager;

    /**
     * 查询工作流实例状态
     */
    @GetMapping("/workflow/{instanceId}")
    public Result<WorkflowStatusVO> getWorkflowStatus(@PathVariable Integer instanceId) {
        ActorRef monitor = actorSystemManager.getMonitorActor();

        // 使用Akka的ask模式同步获取结果
        CompletionStage<Object> future = Patterns.ask(
            monitor,
            new QueryWorkflowStatus(instanceId),
            Duration.ofSeconds(5)
        );

        try {
            WorkflowRuntimeStatus status = (WorkflowRuntimeStatus) future.toCompletableFuture().get();
            return Result.success(convertToVO(status));

        } catch (Exception e) {
            log.error("Failed to query workflow status", e);
            return Result.error("Query timeout");
        }
    }

    /**
     * 查询等待队列
     */
    @GetMapping("/queue/waiting")
    public Result<List<NodeStatusVO>> getWaitingQueue() {
        ActorRef monitor = actorSystemManager.getMonitorActor();

        CompletionStage<Object> future = Patterns.ask(
            monitor,
            new QueryWaitingQueue(),
            Duration.ofSeconds(5)
        );

        try {
            @SuppressWarnings("unchecked")
            List<NodeStatus> nodes = (List<NodeStatus>) future.toCompletableFuture().get();
            return Result.success(convertToVOList(nodes));

        } catch (Exception e) {
            log.error("Failed to query waiting queue", e);
            return Result.error("Query timeout");
        }
    }

    /**
     * 查询执行队列
     */
    @GetMapping("/queue/running")
    public Result<List<NodeStatusVO>> getRunningQueue() {
        ActorRef monitor = actorSystemManager.getMonitorActor();

        CompletionStage<Object> future = Patterns.ask(
            monitor,
            new QueryRunningQueue(),
            Duration.ofSeconds(5)
        );

        try {
            @SuppressWarnings("unchecked")
            List<NodeStatus> nodes = (List<NodeStatus>) future.toCompletableFuture().get();
            return Result.success(convertToVOList(nodes));

        } catch (Exception e) {
            log.error("Failed to query running queue", e);
            return Result.error("Query timeout");
        }
    }

    /**
     * 获取DAG拓扑图数据
     */
    @GetMapping("/workflow/{instanceId}/topology")
    public Result<DAGTopologyVO> getDAGTopology(@PathVariable Integer instanceId) {
        WorkFlowBuildHistory instance = workflowBuildHistoryDAO.selectByPrimaryKey(instanceId);
        PEWorkflowDAG dag = JSON.parseObject(instance.getDagRuntime(), PEWorkflowDAG.class);

        DAGTopologyVO topology = new DAGTopologyVO();
        topology.setNodes(dag.getNodes().stream()
            .map(this::convertNodeToVO)
            .collect(Collectors.toList()));
        topology.setEdges(dag.getEdges().stream()
            .map(this::convertEdgeToVO)
            .collect(Collectors.toList()));

        return Result.success(topology);
    }

    /**
     * 实时推送(WebSocket)
     */
    @MessageMapping("/dag/subscribe/{instanceId}")
    @SendTo("/topic/dag/{instanceId}")
    public void subscribeWorkflowStatus(@DestinationVariable Integer instanceId) {
        // WebSocket订阅,实时推送状态变化
        // 订阅EventStream中的NodeCompleted事件
    }
}
```

### 8.2 前端数据结构

**工作流状态VO**:

```java
public class WorkflowStatusVO {
    private Integer instanceId;
    private String workflowName;
    private String status;  // WAITING/RUNNING/SUCCEED/FAILED
    private Long startTime;
    private Long endTime;
    private Long duration;

    private int totalNodes;
    private int waitingNodes;
    private int runningNodes;
    private int completedNodes;
    private int failedNodes;

    private List<NodeStatusVO> nodes;
}
```

**节点状态VO**:

```java
public class NodeStatusVO {
    private Long nodeId;
    private String nodeName;
    private String nodeType;  // TASK/CONTROL
    private String status;    // WAITING/RUNNING/SUCCEED/FAILED/CANCELED
    private String result;
    private Long startTime;
    private Long finishedTime;
    private Long duration;
    private Boolean skipWhenFailed;
    private String workerAddress;  // 执行节点地址
}
```

**DAG拓扑VO**:

```java
public class DAGTopologyVO {
    private List<NodeVO> nodes;
    private List<EdgeVO> edges;

    public static class NodeVO {
        private Long id;
        private String name;
        private String status;
        private String type;
        private Boolean skipWhenFailed;
    }

    public static class EdgeVO {
        private Long from;
        private Long to;
        private String property;
        private Boolean enable;
    }
}
```

### 8.3 前端可视化建议

#### 8.3.1 DAG拓扑图展示

推荐使用G6图可视化引擎:

```javascript
import G6 from '@antv/g6';

// DAG拓扑图配置
const graph = new G6.Graph({
  container: 'dag-container',
  width: 1000,
  height: 600,
  layout: {
    type: 'dagre',
    rankdir: 'LR',  // 从左到右布局
    nodesep: 50,
    ranksep: 100
  },
  defaultNode: {
    type: 'rect',
    size: [120, 40],
    style: {
      fill: '#5B8FF9',
      stroke: '#5B8FF9',
      lineWidth: 2
    },
    labelCfg: {
      style: {
        fill: '#fff',
        fontSize: 12
      }
    }
  },
  defaultEdge: {
    type: 'polyline',
    style: {
      stroke: '#e2e2e2',
      lineWidth: 2,
      endArrow: true
    }
  },
  nodeStateStyles: {
    running: {
      fill: '#52c41a',
      stroke: '#52c41a'
    },
    failed: {
      fill: '#f5222d',
      stroke: '#f5222d'
    },
    succeed: {
      fill: '#1890ff',
      stroke: '#1890ff'
    }
  }
});

// 渲染DAG数据
function renderDAG(topology) {
  const data = {
    nodes: topology.nodes.map(node => ({
      id: node.id.toString(),
      label: node.name,
      status: node.status
    })),
    edges: topology.edges.map(edge => ({
      source: edge.from.toString(),
      target: edge.to.toString()
    }))
  };

  graph.data(data);
  graph.render();

  // 根据状态设置节点样式
  data.nodes.forEach(node => {
    if (node.status === 'RUNNING') {
      graph.setItemState(node.id, 'running', true);
    } else if (node.status === 'FAILED') {
      graph.setItemState(node.id, 'failed', true);
    } else if (node.status === 'SUCCEED') {
      graph.setItemState(node.id, 'succeed', true);
    }
  });
}
```

#### 8.3.2 队列监控面板

使用Ant Design的统计卡片:

```jsx
import { Card, Statistic, Row, Col } from 'antd';
import { ClockCircleOutlined, SyncOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';

function QueueMonitor({ status }) {
  return (
    <Row gutter={16}>
      <Col span={6}>
        <Card>
          <Statistic
            title="等待队列"
            value={status.waitingNodes}
            prefix={<ClockCircleOutlined />}
            valueStyle={{ color: '#faad14' }}
          />
        </Card>
      </Col>
      <Col span={6}>
        <Card>
          <Statistic
            title="执行队列"
            value={status.runningNodes}
            prefix={<SyncOutlined spin />}
            valueStyle={{ color: '#52c41a' }}
          />
        </Card>
      </Col>
      <Col span={6}>
        <Card>
          <Statistic
            title="已完成"
            value={status.completedNodes}
            prefix={<CheckCircleOutlined />}
            valueStyle={{ color: '#1890ff' }}
          />
        </Card>
      </Col>
      <Col span={6}>
        <Card>
          <Statistic
            title="失败"
            value={status.failedNodes}
            prefix={<CloseCircleOutlined />}
            valueStyle={{ color: '#f5222d' }}
          />
        </Card>
      </Col>
    </Row>
  );
}
```

#### 8.3.3 实时更新(WebSocket)

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function connectDAGMonitor(instanceId, onUpdate) {
  const socket = new SockJS('/ws');
  const stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    // 订阅工作流状态更新
    stompClient.subscribe(`/topic/dag/${instanceId}`, (message) => {
      const status = JSON.parse(message.body);
      onUpdate(status);
    });

    // 发送订阅请求
    stompClient.send(`/app/dag/subscribe/${instanceId}`, {}, {});
  });

  return stompClient;
}

// 使用示例
useEffect(() => {
  const client = connectDAGMonitor(instanceId, (status) => {
    setWorkflowStatus(status);
    renderDAG(status.topology);
  });

  return () => client.disconnect();
}, [instanceId]);
```

## 九、实施计划

### 9.1 分阶段实施

#### **第一阶段: 基础搭建 (2周)**

**目标**: 搭建Akka执行层框架,完成核心Actor实现

**任务清单**:
1. ✅ 提取PowerJob核心类到TIS项目
2. ✅ 创建Akka配置文件(单机/集群)
3. ✅ 实现DAGActorSystemManager
4. ✅ 实现DAGSchedulerActor(基础版)
5. ✅ 实现NodeDispatcherActor
6. ✅ 实现TaskWorkerActor
7. ✅ 编写单元测试

**交付物**:
- Akka Actor System可正常启动
- 可以提交简单的DAG任务执行
- 单元测试覆盖率 > 70%

#### **第二阶段: 持久化集成 (1周)**

**目标**: 完成数据库设计和持久化逻辑

**任务清单**:
1. ✅ 执行数据库DDL脚本
2. ✅ 创建实体类和DAO接口
3. ✅ 实现工作流实例创建逻辑
4. ✅ 实现DAG状态持久化
5. ✅ 实现节点执行记录保存
6. ✅ 集成测试

**交付物**:
- 数据库表创建完成
- 持久化逻辑测试通过
- 可以从数据库恢复工作流状态

#### **第三阶段: 定时调度集成 (1周)**

**目标**: 实现Cron定时调度功能

**任务清单**:
1. ✅ 集成Quartz调度框架
2. ✅ 实现WorkflowScheduler
3. ✅ 实现WorkflowTriggerJob
4. ✅ 实现工作流管理API
5. ✅ 测试定时触发

**交付物**:
- 支持Cron表达式配置
- 定时任务可正常触发
- 支持手动触发执行

#### **第四阶段: 监控可视化 (2周)**

**目标**: 实现监控API和前端可视化

**任务清单**:
1. ✅ 实现DAGMonitorActor
2. ✅ 实现监控查询API
3. ✅ 实现WebSocket实时推送
4. ✅ 前端DAG拓扑图展示(G6)
5. ✅ 前端队列监控面板
6. ✅ 前端实时状态更新

**交付物**:
- 监控API可正常调用
- 前端可视化DAG拓扑图
- 实时显示等待队列和执行队列

#### **第五阶段: 集群模式和故障恢复 (2周)**

**目标**: 实现集群模式和高可用

**任务清单**:
1. ✅ 实现ClusterManagerActor
2. ✅ 配置ClusterRouterPool
3. ✅ 实现节点故障检测
4. ✅ 实现任务自动恢复
5. ✅ 集群模式压测
6. ✅ 故障演练测试

**交付物**:
- 支持多节点集群部署
- 节点下线任务自动迁移
- 集群吞吐量提升 > 3倍

#### **第六阶段: 灰度上线和优化 (2周)**

**目标**: 灰度替换现有调度,全量上线

**任务清单**:
1. ✅ 迁移现有工作流到新系统
2. ✅ 双轨运行监控对比
3. ✅ 性能优化和调优
4. ✅ 文档编写
5. ✅ 全量切换
6. ✅ 移除旧代码和依赖

**交付物**:
- 所有工作流迁移完成
- 性能指标达标
- 完整的使用文档

### 9.2 时间线

```
Week 1-2  : 第一阶段 - Akka执行层框架
Week 3    : 第二阶段 - 持久化集成
Week 4    : 第三阶段 - 定时调度
Week 5-6  : 第四阶段 - 监控可视化
Week 7-8  : 第五阶段 - 集群模式
Week 9-10 : 第六阶段 - 灰度上线

总计: 10周 (约2.5个月)
```

### 9.3 风险评估

| 风险项 | 概率 | 影响 | 应对措施 |
|--------|------|------|---------|
| Akka学习曲线陡峭 | 中 | 中 | 提前学习,参考官方文档和示例 |
| PowerJob类集成不兼容 | 低 | 高 | 充分测试,必要时修改源码 |
| 现有任务迁移复杂 | 中 | 中 | 编写自动迁移脚本,分批迁移 |
| 集群模式稳定性问题 | 中 | 高 | 充分压测,灰度上线 |
| 性能不达预期 | 低 | 中 | 性能监控,及时优化 |

### 9.4 依赖管理

#### 9.4.1 新增Maven依赖

```xml
<!-- Akka Actor -->
<dependency>
    <groupId>com.typesafe.akka</groupId>
    <artifactId>akka-actor-typed_2.13</artifactId>
    <version>2.6.20</version>
</dependency>

<!-- Akka Cluster -->
<dependency>
    <groupId>com.typesafe.akka</groupId>
    <artifactId>akka-cluster-typed_2.13</artifactId>
    <version>2.6.20</version>
</dependency>

<!-- Akka Serialization (Kryo) -->
<dependency>
    <groupId>io.altoo</groupId>
    <artifactId>akka-kryo-serialization_2.13</artifactId>
    <version>2.4.3</version>
</dependency>

<!-- Quartz Scheduler -->
<dependency>
    <groupId>org.quartz-scheduler</groupId>
    <artifactId>quartz</artifactId>
    <version>2.3.2</version>
</dependency>

<!-- Spring Quartz Integration -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>${spring-version}</version>
</dependency>

<!-- Micrometer (Monitoring) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
    <version>1.9.0</version>
</dependency>

<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <version>1.9.0</version>
</dependency>
```

#### 9.4.2 移除旧依赖

```xml
<!-- 可以移除的旧依赖 -->
<dependency>
    <groupId>org.jenkins-ci</groupId>
    <artifactId>task-reactor</artifactId>
    <version>1.5</version>
    <!-- 第六阶段移除 -->
</dependency>
```

## 十、测试策略

### 10.1 单元测试

#### 10.1.1 PowerJob算法测试

```java
public class WorkflowDAGUtilsTest {

    @Test
    public void testValidDAG() {
        // 测试合法的DAG
        PEWorkflowDAG dag = createSimpleDAG();
        WorkflowDAG runtimeDAG = WorkflowDAGUtils.convert(dag);
        assertTrue(WorkflowDAGUtils.valid(runtimeDAG));
    }

    @Test
    public void testInvalidDAGWithCycle() {
        // 测试有环的DAG: 1->2->3->1
        PEWorkflowDAG dag = createCyclicDAG();
        WorkflowDAG runtimeDAG = WorkflowDAGUtils.convert(dag);
        assertFalse(WorkflowDAGUtils.valid(runtimeDAG));
    }

    @Test
    public void testListReadyNodes() {
        // 测试就绪节点计算
        PEWorkflowDAG dag = createComplexDAG();
        List<PEWorkflowDAG.Node> readyNodes = WorkflowDAGUtils.listReadyNodes(dag);

        // 初始只有根节点就绪
        assertEquals(2, readyNodes.size());
    }

    @Test
    public void testSkipWhenFailed() {
        // 测试失败跳过逻辑
        PEWorkflowDAG dag = createDAGWithSkipNode();

        // 标记节点失败
        dag.getNodes().get(1).setStatus(InstanceStatus.FAILED.getV());
        dag.getNodes().get(1).setSkipWhenFailed(true);

        List<PEWorkflowDAG.Node> readyNodes = WorkflowDAGUtils.listReadyNodes(dag);

        // 下游节点应该就绪(跳过失败节点)
        assertTrue(readyNodes.size() > 0);
    }
}
```

#### 10.1.2 Actor行为测试

```java
public class DAGSchedulerActorTest extends AbstractActorTest {

    @Test
    public void testStartWorkflow() {
        // 创建测试Actor
        TestKit probe = new TestKit(system);
        ActorRef scheduler = system.actorOf(Props.create(DAGSchedulerActor.class));

        // 发送启动消息
        scheduler.tell(new StartWorkflow(1, new HashMap<>()), probe.getRef());

        // 验证消息
        probe.expectMsgClass(WorkflowStarted.class);
    }

    @Test
    public void testNodeCompletedFlow() {
        TestKit probe = new TestKit(system);
        ActorRef scheduler = system.actorOf(Props.create(DAGSchedulerActor.class));

        // 启动工作流
        scheduler.tell(new StartWorkflow(1, new HashMap<>()), probe.getRef());

        // 模拟节点完成
        scheduler.tell(new NodeCompleted(1, 1L, "SUCCEED", "ok"), probe.getRef());

        // 验证后续节点被分发
        probe.expectMsgClass(DispatchTask.class);
    }
}
```

### 10.2 集成测试

#### 10.2.1 端到端测试

```java
@SpringBootTest
public class WorkflowExecutionIntegrationTest {

    @Autowired
    private WorkflowInstanceManager instanceManager;

    @Autowired
    private DAGActorSystemManager actorSystemManager;

    @Test
    public void testCompleteWorkflowExecution() throws Exception {
        // 1. 创建工作流定义
        DAGSessionSpec dagSpec = createTestDAG();
        WorkFlow workflow = createWorkflow("test-workflow", dagSpec);

        // 2. 创建实例
        Integer instanceId = instanceManager.createInstance(workflow.getId(), null);

        // 3. 触发执行
        ActorRef scheduler = actorSystemManager.getSchedulerActor();
        scheduler.tell(new StartWorkflow(instanceId, new HashMap<>()), ActorRef.noSender());

        // 4. 等待完成(最多60秒)
        await().atMost(60, TimeUnit.SECONDS).until(() -> {
            WorkFlowBuildHistory instance = historyDAO.selectByPrimaryKey(instanceId);
            return "SUCCEED".equals(instance.getInstanceStatus())
                || "FAILED".equals(instance.getInstanceStatus());
        });

        // 5. 验证结果
        WorkFlowBuildHistory instance = historyDAO.selectByPrimaryKey(instanceId);
        assertEquals("SUCCEED", instance.getInstanceStatus());

        List<DAGNodeExecution> nodeExecutions = nodeExecutionDAO.selectByWorkflowInstanceId(instanceId);
        assertTrue(nodeExecutions.stream().allMatch(n -> "SUCCEED".equals(n.getStatus())));
    }
}
```

#### 10.2.2 定时调度测试

```java
@SpringBootTest
public class WorkflowSchedulerTest {

    @Autowired
    private WorkflowScheduler scheduler;

    @Test
    public void testCronSchedule() throws Exception {
        // 创建每5秒执行一次的工作流
        WorkFlow workflow = createWorkflow("cron-test", "0/5 * * * * ?");

        // 调度
        scheduler.scheduleWorkflow(workflow);

        // 等待至少执行2次
        Thread.sleep(12000);

        // 验证执行记录
        List<WorkFlowBuildHistory> instances = historyDAO.selectByWorkflowId(workflow.getId());
        assertTrue(instances.size() >= 2);
    }
}
```

### 10.3 性能测试

#### 10.3.1 并发压测

```java
public class WorkflowPerformanceTest {

    @Test
    public void testConcurrentExecution() throws Exception {
        int concurrentWorkflows = 100;
        CountDownLatch latch = new CountDownLatch(concurrentWorkflows);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrentWorkflows; i++) {
            executor.submit(() -> {
                try {
                    Integer instanceId = instanceManager.createInstance(workflowId, null);
                    scheduler.tell(new StartWorkflow(instanceId, new HashMap<>()), ActorRef.noSender());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Created " + concurrentWorkflows + " workflows in " + duration + "ms");
        System.out.println("Throughput: " + (concurrentWorkflows * 1000.0 / duration) + " workflows/sec");

        // 验证吞吐量
        assertTrue(duration < 5000);  // 5秒内完成100个工作流创建
    }
}
```

#### 10.3.2 集群吞吐量测试

```java
@Test
public void testClusterThroughput() throws Exception {
    // 启动3节点集群
    List<ActorSystem> nodes = startCluster(3);

    // 提交1000个任务
    int taskCount = 1000;
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < taskCount; i++) {
        // 随机选择节点提交
        ActorSystem node = nodes.get(i % nodes.size());
        ActorRef scheduler = node.actorFor("/user/dag-scheduler");
        scheduler.tell(new StartWorkflow(instanceId, new HashMap<>()), ActorRef.noSender());
    }

    // 等待全部完成
    awaitAllCompleted(taskCount);

    long duration = System.currentTimeMillis() - startTime;

    System.out.println("Cluster throughput: " + (taskCount * 1000.0 / duration) + " tasks/sec");

    // 验证集群吞吐量至少是单机的2倍
    assertTrue(clusterThroughput > singleNodeThroughput * 2);
}
```

### 10.4 故障恢复测试

```java
@Test
public void testNodeFailureRecovery() throws Exception {
    // 启动3节点集群
    List<ActorSystem> nodes = startCluster(3);

    // 提交任务到节点1
    Integer instanceId = instanceManager.createInstance(workflowId, null);
    ActorRef scheduler = nodes.get(0).actorFor("/user/dag-scheduler");
    scheduler.tell(new StartWorkflow(instanceId, new HashMap<>()), ActorRef.noSender());

    // 等待任务开始执行
    Thread.sleep(1000);

    // 模拟节点1故障(shutdown)
    nodes.get(0).terminate();

    // 等待故障检测和任务恢复
    Thread.sleep(5000);

    // 验证任务被其他节点接管并完成
    WorkFlowBuildHistory instance = historyDAO.selectByPrimaryKey(instanceId);
    assertEquals("SUCCEED", instance.getInstanceStatus());
}
```

## 十一、监控和运维

### 11.1 监控指标

#### 11.1.1 业务指标

| 指标 | 类型 | 说明 |
|------|------|------|
| dag.workflow.submitted | Counter | 提交的工作流总数 |
| dag.workflow.completed | Counter | 完成的工作流总数 |
| dag.workflow.failed | Counter | 失败的工作流总数 |
| dag.workflow.duration | Timer | 工作流执行时长 |
| dag.task.executed | Counter | 执行的任务总数 |
| dag.task.failed | Counter | 失败的任务总数 |
| dag.task.duration | Timer | 任务执行时长 |
| dag.queue.waiting.size | Gauge | 等待队列长度 |
| dag.queue.running.size | Gauge | 执行队列长度 |

#### 11.1.2 系统指标

| 指标 | 类型 | 说明 |
|------|------|------|
| akka.actor.count | Gauge | Actor数量 |
| akka.mailbox.size | Gauge | 消息队列长度 |
| akka.dispatcher.running | Gauge | 运行中的线程数 |
| akka.cluster.members | Gauge | 集群成员数量 |
| akka.cluster.unreachable | Gauge | 不可达节点数 |

#### 11.1.3 数据库指标

| 指标 | 类型 | 说明 |
|------|------|------|
| db.connection.active | Gauge | 活跃连接数 |
| db.query.duration | Timer | 查询耗时 |
| db.query.error | Counter | 查询错误数 |

### 11.2 监控实现

#### 11.2.1 Micrometer集成

```java
@Component
public class DAGMetricsCollector {

    private final MeterRegistry registry;

    public DAGMetricsCollector(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 记录工作流提交
     */
    public void recordWorkflowSubmitted(String workflowName) {
        registry.counter("dag.workflow.submitted",
            "workflow", workflowName).increment();
    }

    /**
     * 记录工作流完成
     */
    public void recordWorkflowCompleted(String workflowName, String status, long durationMs) {
        registry.counter("dag.workflow.completed",
            "workflow", workflowName,
            "status", status).increment();

        registry.timer("dag.workflow.duration",
            "workflow", workflowName,
            "status", status).record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录任务执行
     */
    public void recordTaskExecuted(String taskName, String status, long durationMs) {
        registry.counter("dag.task.executed",
            "task", taskName,
            "status", status).increment();

        registry.timer("dag.task.duration",
            "task", taskName,
            "status", status).record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录队列长度
     */
    public void recordQueueSize(String queueType, int size) {
        Gauge.builder("dag.queue.size", () -> size)
            .tag("type", queueType)
            .register(registry);
    }
}
```

#### 11.2.2 Prometheus导出

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,info
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: tis-dag
      instance: ${HOSTNAME}
```

#### 11.2.3 Grafana仪表盘

**建议监控面板**:
1. **工作流概览**:
   - 提交数 / 完成数 / 失败数
   - 成功率趋势
   - 平均执行时长

2. **任务执行**:
   - 任务执行速率
   - 任务失败率
   - Top 10 慢任务

3. **队列监控**:
   - 等待队列长度
   - 执行队列长度
   - 队列积压告警

4. **集群状态**:
   - 集群成员数
   - 不可达节点
   - 节点负载分布

5. **系统资源**:
   - CPU使用率
   - 内存使用率
   - 数据库连接池

### 11.3 日志规范

#### 11.3.1 日志级别

| 级别 | 使用场景 |
|------|---------|
| ERROR | 系统错误,需要立即处理 |
| WARN | 潜在问题,需要关注 |
| INFO | 关键业务流程,状态变更 |
| DEBUG | 详细调试信息 |

#### 11.3.2 日志格式

```java
// 工作流启动
log.info("Workflow started: workflowId={}, instanceId={}, triggerType={}",
    workflowId, instanceId, triggerType);

// 节点执行
log.info("Node executing: instanceId={}, nodeId={}, nodeName={}, worker={}",
    instanceId, nodeId, nodeName, workerAddress);

// 节点完成
log.info("Node completed: instanceId={}, nodeId={}, nodeName={}, status={}, duration={}ms",
    instanceId, nodeId, nodeName, status, duration);

// 工作流完成
log.info("Workflow completed: workflowId={}, instanceId={}, status={}, totalDuration={}ms, nodeCount={}",
    workflowId, instanceId, status, duration, nodeCount);

// 错误日志
log.error("Task execution failed: instanceId={}, nodeId={}, nodeName={}, error={}",
    instanceId, nodeId, nodeName, e.getMessage(), e);
```

#### 11.3.3 MDC上下文

```java
public class LoggingMDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            MDC.put("instanceId", request.getParameter("instanceId"));
            MDC.put("workflowId", request.getParameter("workflowId"));
            MDC.put("traceId", UUID.randomUUID().toString());

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

### 11.4 告警规则

#### 11.4.1 Prometheus告警规则

```yaml
groups:
  - name: tis-dag-alerts
    interval: 30s
    rules:
      # 工作流失败率过高
      - alert: HighWorkflowFailureRate
        expr: |
          rate(dag_workflow_failed_total[5m]) / rate(dag_workflow_completed_total[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "工作流失败率过高"
          description: "最近5分钟工作流失败率超过10%"

      # 等待队列积压
      - alert: QueueBacklog
        expr: dag_queue_waiting_size > 100
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "等待队列积压"
          description: "等待队列长度超过100,持续10分钟"

      # 集群节点不可达
      - alert: ClusterNodeUnreachable
        expr: akka_cluster_unreachable > 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "集群节点不可达"
          description: "检测到不可达的集群节点"

      # 任务执行时长过长
      - alert: LongRunningTask
        expr: dag_task_duration_seconds > 3600
        for: 0s
        labels:
          severity: warning
        annotations:
          summary: "任务执行时间过长"
          description: "任务执行时间超过1小时"
```

### 11.5 故障排查

#### 11.5.1 常见问题

| 问题 | 原因 | 排查方法 |
|------|------|---------|
| 工作流卡住不执行 | 1. DAG有环<br>2. 节点依赖错误<br>3. Actor消息丢失 | 1. 检查DAG拓扑<br>2. 查看节点执行记录<br>3. 检查Akka日志 |
| 任务执行失败 | 1. 任务逻辑错误<br>2. 资源不足<br>3. 超时 | 1. 查看任务日志<br>2. 检查系统资源<br>3. 调整超时配置 |
| 集群节点掉线 | 1. 网络分区<br>2. 节点宕机<br>3. GC过长 | 1. 检查网络连接<br>2. 查看节点日志<br>3. 监控JVM指标 |
| 队列积压 | 1. Worker数量不足<br>2. 任务执行慢<br>3. 数据库瓶颈 | 1. 扩容Worker<br>2. 优化任务逻辑<br>3. 优化SQL查询 |

#### 11.5.2 排查工具

```bash
# 查看Actor System状态
curl http://localhost:8080/actuator/metrics/akka.actor.count

# 查看消息队列长度
curl http://localhost:8080/actuator/metrics/akka.mailbox.size

# 查看集群成员
curl http://localhost:8080/actuator/metrics/akka.cluster.members

# 查看等待队列
curl http://localhost:8080/api/dag/monitor/queue/waiting

# 查看执行队列
curl http://localhost:8080/api/dag/monitor/queue/running

# 查看工作流状态
curl http://localhost:8080/api/dag/monitor/workflow/{instanceId}
```

## 十二、附录

### 12.1 参考资料

1. **PowerJob官方文档**: http://www.powerjob.tech/
2. **Akka官方文档**: https://doc.akka.io/docs/akka/current/
3. **Quartz官方文档**: http://www.quartz-scheduler.org/documentation/
4. **G6图可视化**: https://g6.antv.vision/

### 12.2 术语表

| 术语 | 说明 |
|------|------|
| DAG | Directed Acyclic Graph,有向无环图 |
| Actor | Akka中的并发执行单元 |
| Supervisor | Akka中的监管策略 |
| Router | Akka中的路由器,用于负载均衡 |
| Seed Node | Akka集群的种子节点 |
| Kryo | 高性能序列化框架 |
| Cron | 定时任务表达式 |
| Milestone | 里程碑,用于任务依赖 |

### 12.3 联系方式

**技术负责人**: 百岁
**项目地址**: `/Users/mozhenghua/j2ee_solution/project/tis-solr`
**设计文档**: `design/dag/`

---

**文档结束**
