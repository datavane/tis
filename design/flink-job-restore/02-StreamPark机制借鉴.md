# StreamPark 机制借鉴

StreamPark 是一个优秀的 Flink 作业开发管理平台，在 Flink Job 的状态管理、故障检测和自动恢复方面有着成熟的实现。本章详细分析 StreamPark 的核心机制，为 TIS 的设计提供参考。

## 2.1 整体架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                    StreamPark Console                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ FlinkApplication │  │  FlinkAppHttp   │  │ FlinkK8s    │ │
│  │   ManageService  │  │    Watcher      │  │WatcherWrap  │ │
│  └────────┬────────┘  └────────┬────────┘  └──────┬──────┘ │
│           │                    │                   │        │
│  ┌────────▼────────┐  ┌────────▼────────┐  ┌──────▼──────┐ │
│  │ FlinkApplication │  │  FlinkCluster   │  │  FlinkJob   │ │
│  │  ActionService   │  │    Watcher      │  │StatusWatcher│ │
│  └────────┬────────┘  └─────────────────┘  └─────────────┘ │
│           │                                                 │
│  ┌────────▼────────┐  ┌─────────────────┐                  │
│  │  FlinkClient    │  │ FlinkCheckpoint │                  │
│  │  (Submit/Cancel)│  │   Processor     │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
           │                           │
           ▼                           ▼
    ┌─────────────┐            ┌─────────────┐
    │   Flink     │            │   MySQL     │
    │   Cluster   │            │   (t_flink_ │
    │             │            │   app /     │
    │             │            │t_flink_sp)  │
    └─────────────┘            └─────────────┘
```

## 2.2 状态持久化机制

### 2.2.1 数据库表设计

StreamPark 使用 MySQL 持久化作业状态，核心表包括：

#### t_flink_app（作业主表）

```sql
CREATE TABLE `t_flink_app` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `job_name` VARCHAR(255) NOT NULL COMMENT '作业名称',
  `state` TINYINT DEFAULT 0 COMMENT '当前状态：0-CREATED, 1-STARTING, 2-RUNNING, ...',
  `job_id` VARCHAR(64) DEFAULT NULL COMMENT '当前 Flink JobID',
  `cluster_id` VARCHAR(255) DEFAULT NULL COMMENT 'YARN/K8s 集群标识',
  `job_manager_url` VARCHAR(255) DEFAULT NULL COMMENT 'JobManager REST URL',
  `tracking` TINYINT DEFAULT 0 COMMENT '是否正在跟踪：0-否, 1-是',
  `restart_size` INT DEFAULT 0 COMMENT '最大自动重启次数',
  `restart_count` INT DEFAULT 0 COMMENT '已自动重启次数',
  `option_state` TINYINT DEFAULT 0 COMMENT '操作状态（防止并发操作）',
  `deploy_mode` TINYINT DEFAULT 0 COMMENT '部署模式',
  `flink_cluster_id` BIGINT DEFAULT NULL COMMENT '关联的 Flink 集群ID',
  -- ... 其他业务字段
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flink 应用表';
```

关键字段说明：

| 字段 | 作用 |
|------|------|
| `job_id` | 当前运行中的 Flink JobID，提交后回填，运行中可能更新 |
| `tracking` | 标记是否正在监控，Console 启动时加载 `tracking=1` 的作业 |
| `restart_size` | 自动重启次数上限，防止无限循环 |
| `restart_count` | 已执行的自动重启次数，每次手动启动重置为0 |
| `job_manager_url` | 直接缓存 JM URL，减少配置查询 |

#### t_flink_savepoint（恢复点表）

```sql
CREATE TABLE `t_flink_savepoint` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `app_id` BIGINT NOT NULL COMMENT '关联应用ID',
  `chk_id` VARCHAR(64) DEFAULT NULL COMMENT 'Checkpoint/Savepoint ID',
  `path` VARCHAR(1024) NOT NULL COMMENT '恢复点路径',
  `latest` TINYINT DEFAULT 0 COMMENT '是否是最新可用：0-否, 1-是',
  `type` TINYINT DEFAULT 1 COMMENT '类型：1-SAVEPOINT, 2-CHECKPOINT',
  `trigger_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',
  INDEX `idx_app_id` (`app_id`),
  INDEX `idx_latest` (`app_id`, `latest`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flink Savepoint/Checkpoint 表';
```

### 2.2.2 状态变更落库链路

```java
// FlinkApplicationManageServiceImpl.java
public void persistMetrics(FlinkApplication application) {
    // 将所有状态变更持久化到数据库
    LambdaUpdateWrapper<FlinkApplication> update = new LambdaUpdateWrapper<>();
    update.set(FlinkApplication::getState, application.getState())
          .set(FlinkApplication::getJobId, application.getJobId())
          .set(FlinkApplication::getJobManagerUrl, application.getJobManagerUrl())
          .set(FlinkApplication::getRestartCount, application.getRestartCount())
          // ... 其他字段
          .eq(FlinkApplication::getId, application.getId());
    this.baseMapper.update(null, update);
}
```

状态持久化的时机：
1. **作业提交成功**：回填 `job_id`
2. **Watcher 轮询后**：更新 `state`、`job_manager_url`
3. **自动重启时**：更新 `restart_count`
4. **Console 关闭前**：`@PreDestroy` 批量持久化内存中的状态

### 2.2.3 Tracking 机制

```java
// FlinkApplication.java
public boolean shouldTracking() {
    // 非终态的作业需要持续跟踪
    return FlinkAppStateEnum.of(this.state).isTracking();
}

// FlinkAppStateEnum.java
public boolean isTracking() {
    switch (this) {
        case RUNNING:
        case STARTING:
        case RESTARTING:
        case MAPPING:
        case SILENT:
            return true;
        default:
            return false;
    }
}
```

## 2.3 Console 重启后的恢复机制

### 2.3.1 Yarn/Remote/Standalone 模式

```java
// FlinkAppHttpWatcher.java
@Component
public class FlinkAppHttpWatcher {

    // 内存中的监控队列
    private static final ConcurrentHashMap<Long, FlinkApplication> WATCHING_APPS = 
        new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        WATCHING_APPS.clear();
        
        // Console 启动时，从数据库加载所有需要跟踪的作业
        List<FlinkApplication> applications = applicationManageService.list(
            new LambdaQueryWrapper<FlinkApplication>()
                .eq(FlinkApplication::getTracking, 1)
                .notIn(FlinkApplication::getDeployMode, 
                       FlinkDeployMode.getKubernetesMode())
        );
        
        applications.forEach(app -> {
            Long appId = app.getId();
            WATCHING_APPS.put(appId, app);
            STARTING_CACHE.put(appId, DEFAULT_FLAG_BYTE);
        });
    }

    @Scheduled(fixedDelay = 5000) // 每 5 秒轮询一次
    public void watch() {
        WATCHING_APPS.values().forEach(this::doWatch);
    }

    private void doWatch(FlinkApplication application) {
        // 1. 调用 Flink REST API 查询作业状态
        JobsOverview jobsOverview = getJobsOverview(application);
        
        // 2. 按 jobId 匹配
        Optional<JobOverview> matched = jobsOverview.getJobs().stream()
            .filter(job -> job.getId().equals(application.getJobId()))
            .findFirst();
        
        if (matched.isPresent()) {
            // Job 还在运行，更新状态
            application.setState(FlinkAppStateEnum.RUNNING.getValue());
            application.setJobId(matched.get().getId()); // 确保 JobID 最新
        } else {
            // Job 不在列表中，可能已失败或消失
            application.setState(FlinkAppStateEnum.LOST.getValue());
        }
        
        // 3. 持久化状态变更
        applicationManageService.persistMetrics(application);
    }
}
```

### 2.3.2 Kubernetes 模式

```java
// FlinkK8sWatcherWrapper.java
@Component
public class FlinkK8sWatcherWrapper {

    @Autowired
    private FlinkK8sWatcher trackMonitor;

    @PostConstruct
    public void init() {
        // 注册状态变更监听器
        trackMonitor.registerListener(flinkK8sChangeEventListener);
        
        // 从数据库恢复 K8s 作业的 TrackId 列表
        List<TrackId> k8sApps = getK8sWatchingApps();
        k8sApps.forEach(trackMonitor::doWatching);
    }

    private List<TrackId> getK8sWatchingApps() {
        return applicationManageService.list(
            new LambdaQueryWrapper<FlinkApplication>()
                .eq(FlinkApplication::getTracking, 1)
                .in(FlinkApplication::getDeployMode, 
                    FlinkDeployMode.getKubernetesMode())
        ).stream()
         .map(app -> new TrackId(app.getClusterId(), app.getJobId()))
         .collect(Collectors.toList());
    }
}
```

K8s 模式下使用 EventBus 异步处理状态变更：

```java
// FlinkK8sChangeEventListener.java
@Component
public class FlinkK8sChangeEventListener implements FlinkK8sEventListener {

    @Override
    public void onJobStatusChanged(JobStatusEvent event) {
        FlinkApplication app = applicationService.getById(event.getAppId());
        app.setState(event.getJobState().getValue());
        app.setJobId(event.getJobId());
        applicationManageService.persistMetrics(app);
    }
}
```

## 2.4 JobID 跟踪机制

### 2.4.1 提交时生成 JobID

```java
// FlinkApplicationActionServiceImpl.java
public void start(FlinkApplication appParam, boolean auto) {
    FlinkApplication application = getById(appParam.getId());
    
    // 构建提交请求
    SubmitRequest submitRequest = new SubmitRequest(
        flinkEnv,
        flinkVersion,
        new JobID().toHexString(),  // 生成新的 JobID
        application.getJobName(),
        application.getArgs(),
        getSavepointPath(appParam, auto),  // 自动恢复时获取最新 Savepoint
        FlinkRestoreMode.of(appParam.getRestoreMode()),
        application.getDynamicProperties(),
        // ... 其他参数
    );
    
    // 提交到 Flink 集群
    SubmitResponse response = FlinkClient.submit(submitRequest);
    
    // 处理提交结果
    if (response.isSuccess()) {
        processForSuccess(application, response, auto);
    } else {
        processForFail(application, response);
    }
}
```

### 2.4.2 提交结果处理

```java
private void processForSuccess(FlinkApplication application, 
                                SubmitResponse response, 
                                boolean auto) {
    // 回填 Client 返回的 JobID
    if (StringUtils.isNoneEmpty(response.jobId())) {
        application.setJobId(response.jobId());
    }
    
    // 更新状态
    application.setState(FlinkAppStateEnum.STARTING.getValue());
    application.setTracking(1);
    
    if (auto) {
        // 自动恢复时，重启计数 +1
        application.setRestartCount(application.getRestartCount() + 1);
    } else {
        // 手动启动时，重置重启计数
        application.setRestartCount(0);
    }
    
    applicationManageService.persistMetrics(application);
}
```

### 2.4.3 运行期 JobID 回填

对于某些部署模式（如 YARN_APPLICATION），Client 提交时不会立即返回 JobID，需要通过 Watcher 后续回填：

```java
// FlinkAppHttpWatcher.java
private void getStateFromFlink(FlinkApplication application) {
    JobsOverview jobsOverview = getJobsOverview(application);
    
    Optional<JobOverview> optional = jobsOverview.getJobs().stream()
        .filter(job -> job.getId().equals(application.getJobId()))
        .findFirst();
    
    if (optional.isPresent()) {
        JobOverview jobOverview = optional.get();
        application.setJobId(jobOverview.getId()); // 更新/确认 JobID
        application.setState(FlinkAppStateEnum.RUNNING.getValue());
    } else {
        // 尝试通过 jobName 匹配（JobID 可能已变化）
        optional = jobsOverview.getJobs().stream()
            .filter(job -> job.getName().equals(application.getJobName()))
            .findFirst();
        
        if (optional.isPresent()) {
            // 发现同名 Job，可能是 HA 恢复后的新 JobID
            application.setJobId(optional.get().getId());
            application.setState(FlinkAppStateEnum.RUNNING.getValue());
        }
    }
}
```

### 2.4.4 手动 Mapping 机制

```java
// FlinkApplicationController.java
@PostMapping("mapping")
public RestResponse mapping(@RequestBody FlinkApplication appParam) {
    boolean success = applicationManageService.mapping(appParam);
    return RestResponse.success(success);
}

// FlinkApplicationManageServiceImpl.java
public boolean mapping(FlinkApplication appParam) {
    LambdaUpdateWrapper<FlinkApplication> update = new LambdaUpdateWrapper<>();
    update.set(appParam.getClusterId() != null, 
               FlinkApplication::getClusterId, appParam.getClusterId())
          .set(appParam.getJobId() != null, 
               FlinkApplication::getJobId, appParam.getJobId())
          .set(FlinkApplication::getState, FlinkAppStateEnum.MAPPING.getValue())
          .set(FlinkApplication::getTracking, 1)
          .set(FlinkApplication::getOptionState, OptionStateEnum.NONE.getValue())
          .eq(FlinkApplication::getId, appParam.getId());
    
    return this.update(update);
}
```

Mapping 功能的意义：
- 允许运维人员将一个已经在集群上运行的 Flink Job 绑定到 StreamPark 管理
- 适用于集群迁移、手动提交后纳入管理等场景

## 2.5 自动恢复机制

### 2.5.1 故障检测与自动重启

```java
// FlinkAppHttpWatcher.java
@Scheduled(fixedDelay = 5000)
public void watch() {
    WATCHING_APPS.values().forEach(application -> {
        FlinkAppStateEnum currentState = getCurrentState(application);
        
        switch (currentState) {
            case RUNNING:
                // 正常状态，更新指标
                updateMetrics(application);
                break;
                
            case FAILED:
                // 作业失败，触发自动恢复
                handleFailed(application);
                break;
                
            case LOST:
                // 集群失联，停止跟踪
                handleLost(application);
                break;
                
            case CANCELED:
                // 用户取消，停止跟踪
                handleCanceled(application);
                break;
        }
    });
}

private void handleFailed(FlinkApplication application) {
    // 检查是否允许自动重启
    if (application.isNeedRestartOnFailed()) {
        log.info("Application {} failed, triggering auto restart...", 
                 application.getJobName());
        
        // 自动启动，auto=true
        applicationActionService.start(application, true);
    } else {
        log.warn("Application {} failed, auto restart disabled or exceeded limit.", 
                 application.getJobName());
        application.setTracking(0);
        applicationManageService.persistMetrics(application);
        WATCHING_APPS.remove(application.getId());
    }
}

// FlinkApplication.java
public boolean isNeedRestartOnFailed() {
    if (this.restartSize != null && this.restartCount != null) {
        return this.restartSize > 0 && this.restartCount <= this.restartSize;
    }
    return false;
}
```

### 2.5.2 自动恢复时的 Savepoint 选择

```java
// FlinkApplicationActionServiceImpl.java
private String getSavepointPath(FlinkApplication appParam, boolean auto) {
    if (auto || appParam.getRestoreOrTriggerSavepoint()) {
        // 自动恢复时，优先使用用户指定的路径
        if (StringUtils.isNotBlank(appParam.getSavepointPath())) {
            return appParam.getSavepointPath();
        }
        
        // 否则从数据库获取最新的 Savepoint
        FlinkSavepoint savepoint = savepointService.getLatest(appParam.getId());
        if (savepoint != null) {
            return savepoint.getPath();
        }
    }
    return null;
}

// FlinkSavepointServiceImpl.java
public FlinkSavepoint getLatest(Long appId) {
    return this.lambdaQuery()
        .eq(FlinkSavepoint::getAppId, appId)
        .eq(FlinkSavepoint::getLatest, 1)
        .orderByDesc(FlinkSavepoint::getTriggerTime)
        .last("LIMIT 1")
        .one();
}
```

### 2.5.3 Checkpoint 连续失败触发 Failover

```java
// FlinkCheckpointProcessor.java
public void process(FlinkApplication application, CheckPoints checkPoints) {
    // 1. 记录最新的 Checkpoint/Savepoint
    processCheckPoints(application, checkPoints);
    
    // 2. 检查 Checkpoint 失败策略
    processFailoverStrategy(application, checkPoints);
}

private void processFailoverStrategy(FlinkApplication application, 
                                      CheckPoints checkPoints) {
    CheckpointConfig checkpointConfig = application.getCheckpointConfig();
    
    if (checkpointConfig.getCpFailureAction() == CpFailureActionEnum.RESTART) {
        // 检查在指定时间窗口内 Checkpoint 连续失败次数
        int recentFailures = countRecentFailures(checkPoints, 
                                                  checkpointConfig.getCpFailureInterval());
        
        if (recentFailures >= checkpointConfig.getCpFailureRateThreshold()) {
            log.error("Application {} checkpoint failed {} times in {}, triggering restart.",
                      application.getJobName(), recentFailures, 
                      checkpointConfig.getCpFailureInterval());
            
            // 先 cancel-with-savepoint，再 start-with-savepoint
            applicationActionService.restart(application);
        }
    }
}
```

### 2.5.4 Cancel 时自动触发 Savepoint

```java
// FlinkApplicationActionServiceImpl.java
public void cancel(FlinkApplication application) {
    CancelRequest cancelRequest = new CancelRequest(
        flinkEnv,
        application.getJobId(),
        application.getJobManagerUrl(),
        application.getClusterId(),
        application.getDeployMode(),
        application.getRestoreOrTriggerSavepoint() // 是否触发 Savepoint
    );
    
    CancelResponse cancelResponse = FlinkClient.cancel(cancelRequest);
    
    // 如果触发了 Savepoint，记录到数据库
    if (cancelResponse != null && cancelResponse.savepointDir() != null) {
        FlinkSavepoint savepoint = new FlinkSavepoint();
        savepoint.setPath(cancelResponse.savepointDir());
        savepoint.setAppId(application.getId());
        savepoint.setLatest(true);
        savepoint.setType(CheckPointTypeEnum.SAVEPOINT.get());
        savepointService.save(savepoint);
    }
}
```

## 2.6 Client 侧 Savepoint 恢复配置

### 2.6.1 SubmitRequest 组装

```scala
// SubmitRequest.scala
class SubmitRequest(
    val flinkEnv: FlinkEnv,
    val flinkVersion: FlinkVersion,
    val jobId: String,
    val jobName: String,
    val args: String,
    val savePoint: String,           // Savepoint/Checkpoint 路径
    val restoreMode: FlinkRestoreMode, // CLAIM / NO_CLAIM
    val allowNonRestoredState: Boolean = false,
    // ... 其他字段
) {

  lazy val savepointRestoreSettings: SavepointRestoreSettings = {
    savePoint match {
      case sp if Try(sp.isEmpty).getOrElse(true) =>
        SavepointRestoreSettings.none
      case sp => 
        SavepointRestoreSettings.forPath(sp, allowNonRestoredState)
    }
  }
}
```

### 2.6.2 Flink Configuration 注入

```scala
// FlinkClientTrait.scala
def prepareConfig(submitRequest: SubmitRequest): Configuration = {
    val flinkConfig = new Configuration()
    
    // 设置 Savepoint 恢复路径
    if (StringUtils.isNotBlank(submitRequest.savePoint)) {
        flinkConfig.set(SavepointConfigOptions.SAVEPOINT_PATH, submitRequest.savePoint)
        flinkConfig.setBoolean(SavepointConfigOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE, 
                               submitRequest.allowNonRestoredState)
        flinkConfig.setString(FlinkRestoreMode.RESTORE_MODE, 
                              submitRequest.restoreMode.getName)
    }
    
    flinkConfig
}

// 在构建 JobGraph 时注入
def getJobGraph(submitRequest: SubmitRequest): JobGraph = {
    val packagedProgramBuilder = PackagedProgram.newBuilder()
        .setSavepointRestoreSettings(submitRequest.savepointRestoreSettings)
        .setEntryPointClassName(submitRequest.mainClass)
        .setJarFile(new File(submitRequest.flinkJar))
    
    // ... 构建 JobGraph
}
```

## 2.7 关键经验总结

| 机制 | StreamPark 实现 | TIS 可借鉴程度 |
|------|----------------|---------------|
| **数据库持久化** | `t_flink_app` + `t_flink_savepoint` | ⭐⭐⭐⭐⭐ 核心借鉴 |
| **自动恢复** | Watcher 检测 FAILED → 自动 start | ⭐⭐⭐⭐⭐ 核心借鉴 |
| **恢复点自动管理** | CheckpointProcessor 自动落库 | ⭐⭐⭐⭐ 重要借鉴 |
| **重启次数限制** | `restart_size` / `restart_count` | ⭐⭐⭐⭐ 建议引入 |
| **手动 Mapping** | `mapping` 接口绑定已有 Job | ⭐⭐⭐ 建议引入 |
| **K8s 事件驱动** | EventBus + 异步状态更新 | ⭐⭐⭐ 视部署模式而定 |
| **恢复模式配置** | `CLAIM` / `NO_CLAIM` | ⭐⭐⭐ 建议引入 |

**核心洞察**：StreamPark 的优雅不在于"追踪到新 JobID"，而在于：
1. **状态集中持久化** → Console 重启不丢失
2. **主动轮询检测** → 及时发现故障
3. **自动 Savepoint 重投** → 无需人工介入即可恢复等价作业
4. **策略化恢复** → 可控的自动重启次数和恢复模式
