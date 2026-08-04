# 增量通道监控 · 后端指标扩展与新实时视图设计方案

> 设计日期: 2026-08-04
> 设计者: Claude
> 审核者: 百岁
> 范围: 涉及后端改造——CDC 延迟、反压、吞吐、Checkpoint、每表流量等 Flink 指标接入与对应前端新视图
> 姊妹篇: [01-frontend-only-optim.md](./01-frontend-only-optim.md)（纯前端炫酷化，零后端改动，建议先行实施）

---

## 一、背景与目标

纯前端方案（01 文档）只能榨取现有 `mq_tags_status` 消息里的数据（I/U/D 累计 + 窗口增量 + 限流状态）。而 Flink/Flink-CDC 运行时可以提供更多高价值运维指标，目前都没有透出到 TIS 控制台：

- **CDC 端到端延迟**（数据从源库 binlog 产生到写入目标端的滞后）——CDC 场景用户第一关心的指标；
- **反压（Backpressure）**——pipeline 健康度，Flink Web UI 里藏得很深；
- **实时 TPS**（numRecordsIn/OutPerSecond）——真正的每秒速率（现有 tableConsumeCount 是窗口和）；
- **Checkpoint 状态**——exactly-once 语义的信心来源，排障必备；
- **每表流量排行**——多表同步场景的定位利器。

**目标**：把这些指标接入 TIS 现有监控数据链路，并在增量通道运行页提供对应实时视图。

## 二、现有数据链路全景

### 2.1 链路一：作业内指标 → WebSocket（本次方案 A 的载体）

```
Flink Job (TaskManager 进程内)
  │  指标注册为 Flink Metric（Counter/Gauge/Meter）
  ▼
TISPBReporter                                    [plugins/tis-incr/tis-flink-extends]
  │  extends AbstractReporter implements Scheduled
  │  notifyOfAddedMetric(): 按 IIncreaseCounter.COLLECTABLE_* 白名单过滤
  │  report(): 定期收集 → sendMetric2TISAssemble()
  ▼  gRPC: StatusRpcClientFactory.getService(coordinator).reportStatus(UpdateCounterMap)
IncrStatusUmbilicalProtocolImpl                  [tis-assemble/rpc/server]
  │  updateCounterStatus: pipeline → (uuid → TableMultiDataIndexStatus)
  │  getUpdateAbsoluteCountMap(): 对每个 tag 取各 subtask 最后累加值并【求和】
  ▼
IncrTagHeatBeatMonitor.build()                   [tis-assemble/manage/servlet]
  │  按 IndexCollectionConfig.duration 周期循环
  │  TopicTagIncrStatus: 10 秒滑动窗口算区间增量
  ▼  ExecuteState.create(LogType.MQ_TAGS_STATUS, TisIncrStatus)
IncrControlWebSocketServlet → WebSocket 推送      [tis-assemble/realtime/servlet]
  ▼
前端 IncrBuildStep4RunningComponent (mq_tags_status 订阅)
```

**关键事实（已核实）：**
- `TISPBReporter` 目前只收两类：`COLLECTABLE_TABLE_COUNT_METRIC`（4 个 Counter）和 `COLLECTABLE_METRIC_LIMIT_GAUGE`（2 个限流 Gauge），见 `IIncreaseCounter.java:55-57`；
- `TableSingleDataIndexStatus.put(tag, val)` 是**通用 key-value 透传**（tis-manage-pojo），gRPC 侧的 `TableSingleDataIndexStatus`（tis-hadoop-rpc）同为 map 结构——**新增指标 key 不需要改 proto**；
- `getTableUpdateCountMap()`（IncrStatusUmbilicalProtocolImpl.java:658）对同一 tag 跨 subtask **求和**——对 Counter 正确，对 Gauge（延迟/反压）语义错误，方案 A 必须处理（见 4.3）。

### 2.2 链路二：Flink JobManager REST（本次方案 B 的载体）

```
Flink JobManager REST API
  ▼  RestClusterClient（参考 FlinkCluster.java:196 的 createFlinkRestClusterClient 模式）
JobDetailsInfo
  ▼
ExtendFlinkJobDeploymentDetails                 [plugins/tis-incr/tis-realtime-flink]
  │  组装点：FlinkTaskNodeController.java:238
  ▼
K8SControllerStatus.flinkJobDetail → 前端 IndexIncrStatus（页面加载/刷新时获取）
```

目前只取了 `/jobs/:id/overview` 级别的信息（vertex 的 bytes/records 读写）。Checkpoint、反压、具体 metric 都在同一 JobManager REST 上，照此模式扩展即可。

## 三、新增指标清单

| 指标 | 来源 | Flink 指标类型 | 聚合方式 | 用途视图 |
|---|---|---|---|---|
| `currentFetchEventTimeLag` | CDC source operator | Gauge (ms) | 跨 subtask 取 **max** | 延迟大字卡 |
| `currentEmitEventTimeLag` | CDC source operator | Gauge (ms) | max | 延迟大字卡 |
| `numRecordsInPerSecond` | 各 operator | Meter | **sum** | In/Out 吞吐双线图 |
| `numRecordsOutPerSecond` | 各 operator | Meter | sum | 同上 |
| `busyTimeMsPerSecond` | task 级 | Gauge (ms/s) | max | 反压热力条 |
| `idleTimeMsPerSecond` | task 级 | Gauge (ms/s) | max | 同上 |
| `backPressuredTimeMsPerSecond` | task 级 | Gauge (ms/s) | max | 同上 |
| lastCheckpointDuration / Size / 失败数等 | JM REST `/jobs/:id/checkpoints` | — | — | Checkpoint 面板 |
| 每表 I/U/D Counter | 作业内按表注册 | Counter | sum | 每表流量 bar race |

> **注意**：CDC 指标名以实际 connector 注册为准（flink-connector-mysql-cdc 系为上述名字，挂在 source operator 的 MetricGroup 下）。实施第一步：起一个 CDC 任务，调 `GET /jobs/:jobId/vertices/:vertexId/metrics` 列出全部可用指标名，确认后再加白名单。不同 source（Kingbase/Oracle/SQLServer 等 debezium 系）指标名可能略有差异，白名单按"集合可配置"设计。

## 四、方案 A：TISPBReporter 白名单扩展（延迟 / 反压 / TPS）

### 4.1 `IIncreaseCounter`（tis-manage-pojo）

```java
// 新增常量
String METRIC_CDC_FETCH_LAG_MS   = "currentFetchEventTimeLag";
String METRIC_CDC_EMIT_LAG_MS    = "currentEmitEventTimeLag";
String METRIC_BUSY_MS_PER_SEC    = "busyTimeMsPerSecond";
String METRIC_IDLE_MS_PER_SEC    = "idleTimeMsPerSecond";
String METRIC_BACKPRESSURED_MS_PER_SEC = "backPressuredTimeMsPerSecond";
// Meter 类（吞吐）
String METRIC_RECORDS_IN_PER_SEC  = "numRecordsInPerSecond";
String METRIC_RECORDS_OUT_PER_SEC = "numRecordsOutPerSecond";

// 新增集合（不要混入现有限流专用集合 COLLECTABLE_METRIC_LIMIT_GAUGE）
Set<String> COLLECTABLE_LAG_GAUGE = Sets.newHashSet(METRIC_CDC_FETCH_LAG_MS, METRIC_CDC_EMIT_LAG_MS);
Set<String> COLLECTABLE_BACKPRESSURE_GAUGE = Sets.newHashSet(...);
Set<String> COLLECTABLE_THROUGHPUT_METER = Sets.newHashSet(...);
```

### 4.2 `TISPBReporter`（plugins/tis-incr/tis-flink-extends）

1. `notifyOfAddedMetric()`：对新集合的 Gauge/Meter 放行（Meter 需新增 `meterMetricIdentifierMapper`，`AbstractReporter` 自带 `meters` map）；
2. `report()`：遍历 `meters`，取值用 `meter.getOneMinuteRate()`（1 分钟滑动均值，抗抖动）；Gauge 直取 `getValue()`；
3. **重构 `UseableGaugeMetricForTIS.putCounterMetric()`**：当前硬编码只认两个限流 key（TISPBReporter.java:284-298），改为通用 `singleDataIndexStatus.put(metricName, longVal)`——gRPC `TableSingleDataIndexStatus.put` 本身就是通用 map，透传即可，**proto 不用改**。

### 4.3 `IncrStatusUmbilicalProtocolImpl`（tis-assemble）——聚合语义修正

现状 `getTableUpdateCountMap()`（L658-686）对所有 tag 跨 subtask **求和**。需按指标类型区分：

- Counter / Meter（计数、速率）→ 维持 **sum**；
- Gauge（延迟、反压占比）→ 改为 **max**（最坏的 subtask 决定整体延迟/反压观感）。

实现建议：在 `IIncreaseCounter` 的集合上增加一个 `isMaxAggregate(tag)` 判断（或直接按集合名判断），`getTableUpdateCountMap` 里分支处理。

> **实施时确认**：`TableMultiDataIndexStatus.getTableNames()`（tis-manage-pojo L98）返回的是否为全部 tag key（含新 key）。若其内部另有白名单过滤，需同步放开。

### 4.4 消息载荷扩展

`TopicTagIncrStatus.TisIncrStatus` 增加一个通用扩展字段，避免每加一个指标改一次结构：

```java
// TisIncrStatus 新增
private final Map<String, Long> extraMetrics;   // key = 指标名, val = 聚合后的值
public Map<String, Long> getExtraMetrics() { return extraMetrics; }
```

`IncrTagHeatBeatMonitor.build()` 组装时从 `getUpdateAbsoluteCountMap()` 返回的 map 里摘出白名单 key 填入（这些 key 会随 4.3 自动流入 `transferTagStatus`，注意现有 `build()` 只把 `ALL_SUMMARY_KEYS` 喂给 `topicTagIncrStatus`，extraMetrics 应**直接**从 count map 读取，不走 10 秒窗口增量逻辑——延迟/反压是瞬时值不是增量）。

前端 `TisIncrStatus` interface 同步加 `extraMetrics: {[key: string]: number}`（incr.build.step4.running.component.ts L712-718）。

### 4.5 对应前端新视图

1. **CDC 延迟大字卡**（最有价值）：`emitLag` 为主指标大字号展示"端到端延迟 X.X s"，`fetchLag` 为辅；阈值变色：<5s 绿 / <60s 橙 / ≥60s 红；带最近 10 点迷你趋势。snapshot 阶段或 source 空闲时 connector 上报值可能为 0 或停滞，前端显示 `--` 并 tooltip 说明，不误判为"零延迟"。
2. **反压热力条**：每个 task 一格（或按 operator 聚合），按 `backPressuredTimeMsPerSecond/1000` 占比染绿→黄→红渐变；hover 显示 busy/idle/backpressured 三项占比。
3. **In/Out 吞吐双线图**：source 侧 `numRecordsInPerSecond` 与 sink 侧 `numRecordsOutPerSecond` 双线，两线张口 = 链路堆积。可并入 01 文档的主图作为可切换视图。

## 五、方案 B：Checkpoint 面板（JobManager REST 代理）

checkpoint 信息在 **JobManager** 侧（TM 上报不了），走链路二扩展：

1. **后端**：tis-console `CoreAction` 新增 ajax 端点（如 `event_submit_do_get_job_checkpoints`），内部通过 `IFlinkClusterConfig.getJobManagerAddress()` 发起 HTTP GET：
   - `/jobs/:jobId/checkpoints`（最近完成/失败 checkpoint 的时长、大小、触发时间、对齐耗时、历史 counts）
   - `/jobs/:jobId/checkpoints/config`（间隔、超时、模式）
   - HTTP 客户端与超时控制参照现有 REST 访问点（`FlinkCluster.createFlinkRestClusterClient`，plugins/tis-incr/tis-realtime-flink FlinkCluster.java:196；`JobDetailsInfo` 组装点 FlinkTaskNodeController.java:238）；
   - 集群地址鉴权/可达性异常要兜底（返回空结构 + 错误提示，不打断页面）。
2. **前端**：运行页新增"Checkpoint"卡片或 Tab：
   - 最近一次 checkpoint：时长 / 大小 / 触发距今 / 状态；
   - 历史 N 次的时长与大小双柱图（chart.js bar）；
   - 失败计数红色角标；
   - 拉取方式：进入视图拉一次 + 每 10s 轮询（REST 即可，不必走 WebSocket）。

## 六、每表流量排行榜（focusTags 扩展）

- **现状**：`IncrControlWebSocketServlet.getFocusTags()`（L169-172）硬编码返回单个 focusTag = `tableConsumeCount`，即"每表"维度当前不存在；
- **后端改造**：
  1. `getFocusTags()` 按当前 pipeline 的表清单生成每表一个 tag；
  2. 作业内需有按表维度的 Counter（按表名注册 `tableConsumeCount_<table>` 之类）。**实施时确认** TIS Flink-CDC source function 是否已按表注册 counter；若没有，需在 source 端按表 `inc()`，改动落在 plugins/tis-incr 的 cdc source 实现处；
- **前端**：chart.js horizontalBar，每帧按当前值排序并重排（bar race），Top N（如 8）表 + 每表 evt/窗口。

## 七、（可选）Source→Sink 拓扑流动画

- 依赖已有：`@antv/g6` 4.8（DAG 可视化在用，见项目内 akka dag visual view）；
- 节点 = job vertices（`FlinkJobDetail.sources` 已有名称/状态/并行度），边流速 = 方案 A 的 per-operator `numRecordsOutPerSecond`；
- 粒子密度/速度映射流速，节点颜色映射反压状态；
- 视觉最炫但工作量最大，**建议放在所有数据视图稳定之后再做**。

## 八、实施分期建议

| 期 | 内容 | 价值/成本 |
|---|---|---|
| P0 | 方案 A：CDC 延迟 + TPS 接入（4.1-4.5 的延迟卡与吞吐图） | 价值最高，改动集中在白名单+透传，1 个迭代 |
| P1 | 方案 A 剩余：反压热力条；方案 B：Checkpoint 面板 | 中 |
| P2 | 每表 bar race（依赖作业侧按表 counter，改动最深） | 多表场景才明显 |
| P3 | g6 拓扑流动画 | 锦上添花 |

每期都可独立上线；01 文档的纯前端改造与本方案 P0 可并行。

## 九、验证方式

1. **单测**：`tis-assemble/src/test/java/com/qlangtech/tis/manage/servlet/TestIncrTagHeatBeatMonitor.java` 已有心跳监控测试基建，照其模式为 extraMetrics 组装与 max/sum 聚合补用例；
2. **端到端**：本地 standalone Flink + MySQL-CDC 通道：
   - 造 binlog 风暴，对比 TIS 面板延迟值与 Flink Web UI 的 `currentEmitEventTimeLag` 一致；
   - 用慢 sink（如 sleep 的 print sink）制造反压，验证热力条变红；
   - 手动触发/等待 checkpoint，验证面板数据与 JM REST `/jobs/:id/checkpoints` 一致；
   - 并行度 >1 时验证 Gauge 取 max、Counter/Meter 取 sum 的聚合正确性；
3. **回归**：确认原 I/U/D 计数、限流状态推送不受影响（白名单是纯增量）。

## 十、风险与注意点

1. **聚合语义是最大坑点**：现有链路默认 sum，Gauge 直接接入会产生"并行度倍数"的错误读数——4.3 的 max 分流必须随第一批指标一起上，不能后补。
2. **TISPBReporter 运行在 TM 进程内**：report 周期沿用现有调度，不要为实时性调高频——延迟/反压指标 5~10 秒粒度足够；指标过多时注意每次 report 的 gRPC 报文大小。
3. **指标名兼容性**：不同 CDC connector（mysql/kingbase/oracle…）与不同 Flink 版本的指标名/挂载层级可能不同，白名单集合设计成可追加；找不到指标时前端显示 `--` 而非 0。
4. **snapshot 阶段读数失真**：全量快照阶段 CDC lag 指标不更新，前端需按"读数停滞 N 个周期"降级展示。
5. **多 job 共存**：`updateCounterStatus` 以 pipeline 名隔离，新指标同样按 pipeline 隔离，避免串数据。
6. **方案 B 的 REST 代理**不要穿透到前端直连（JM 地址不一定对浏览器可达），必须经 console 后端中转。