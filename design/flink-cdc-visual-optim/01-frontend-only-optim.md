# 增量通道实时监控面板 · 纯前端炫酷化改造方案（零后端改动）

> 设计日期: 2026-08-04
> 设计者: Claude
> 审核者: 百岁
> 范围: 仅前端，不改任何后端代码；数据全部来自现有 `mq_tags_status` WebSocket 消息
> 姊妹篇: [02-backend-metrics-extension.md](./02-backend-metrics-extension.md)（需后端改造的指标扩展方案）

---

## 一、背景与目标

增量通道（Flink-CDC）运行页"实时流量"区域（红框区域）当前展示形式比较朴素：
3 个青色 `nz-tag` 显示 Insert/Update/Delete 累计数 + 1 个 10 秒窗口消费数 + 1 条 100px 高的单线面积图。

**目标**：不触碰后端的前提下，将该区域改造为具有"监控大屏"观感的实时面板——动画指标卡、速率仪表盘、多系列渐变流量图、推送脉冲反馈，提升用户视觉体验。

**约束**（已与需求方确认）：
- 图表库继续使用 **chart.js 3.9.1 + ng2-charts 6.0.0**，不引入 ECharts；
- UI 组件库 ng-zorro-antd 17；
- 本次只做视觉与交互升级，不改变现有数据语义。

## 二、现状分析

### 2.1 组件位置

- 前端工程：`/Users/mozhenghua/j2ee_solution/project/tis-console`
- 目标文件：`src/runtime/incr.build.step4.running.component.ts`
- 红框模板区域：**第 137~212 行**（"实时流量"区块），图表配置与数据处理在 **第 437~556 行**。

### 2.2 现有元素清单

| 元素 | 位置 | 数据 |
|---|---|---|
| 流控状态按钮组（无限流/暂停中/泄洪中/限流） | L143-165 | `tisIncrStatus.controllerType` / `perSecRateNums` |
| Insert/Update/Delete 三个青色 nz-tag | L173-181 | `summary.tableInsertCount` 等累计值 |
| `nz-statistic` 消费数（带绿色闪烁） | L187-198 | `summary.tableConsumeCount`（约 10 秒窗口增量） |
| chart.js 单线面积图（100px） | L205-209 | `tags[].trantransferIncr` 最近 10 个点 |

### 2.3 数据源：`mq_tags_status` WebSocket 消息

后端 `IncrTagHeatBeatMonitor`（tis-assemble）按 `IndexCollectionConfig.duration` 配置的采集间隔推送，消息体（`TopicTagIncrStatus.TisIncrStatus` 序列化）：

```json
{
  "summary": {
    "tableConsumeCount": 70,
    "tableInsertCount": 12345,
    "tableUpdateCount": 678,
    "tableDeleteCount": 90
  },
  "tags": [
    { "tag": "tableConsumeCount", "trantransferIncr": 57, "binlogIncr": 33, "lastUpdate": "08:00:00" }
  ],
  "controllerType": 3,
  "perSecRateNums": -1
}
```

**字段语义（实现时必须严格遵守，见 `TopicTagIncrStatus.process()`）：**

| 字段 | 语义 | 说明 |
|---|---|---|
| `summary.tableConsumeCount` | 最近约 10 秒窗口内消费事件总数 | 窗口和，**不是每秒速率**；换算速率需除以窗口秒数 |
| `summary.tableInsertCount/Update/Delete` | 任务启动以来 I/U/D **累计绝对值** | 取区间增量 = 相邻两条消息差分 |
| `tags[]` | 当前仅 1 个 focusTag（`tableConsumeCount`），由 `IncrControlWebSocketServlet.getFocusTags()` 硬编码 | `trantransferIncr` 为窗口增量，`lastUpdate` 格式 `HH:mm:ss` |
| `controllerType` | 1=Paused 2=FloodDischargeRate 3=NoLimitParam 4=RateLimit | 见组件内 `LimitRateControllerType` 枚举（L704-710） |
| `perSecRateNums` | 限流上限（条/秒），仅 controllerType=4 时有效 | |

前端现有处理（`ngAfterContentInit`，L509-556）：把 `tags` 全部塞进 `FixedLengthQueue<TagState>(10)`（定义在本文件 L761），映射为图表的 labels/data。

### 2.4 现状问题（本次顺带修复）

1. **多 tag 混排隐患**：`serial.enqueue(tag)` 把 `msg.tags` 全部入同一个队列，若未来 focusTags 多于 1 个，图表会把不同 tag 的点交错画在同一条线上。本次按 tag 拆分队列时顺带解决。
2. 图表只有总量一条线，I/U/D 三个最有业务价值的维度没有趋势展示。
3. 累计数静止展示，无"活"的感觉；现有 `incomeRateFlash` 闪烁只作用在消费数上。

## 三、目标效果总览

```
┌────────────────────────────────────────────────────────────────────────┐
│ 实时流量   [速率仪表盘]                                                  │
│                                                                        │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐     ╭──────────╮      │
│  │ ▲ INSERT    │ │ ● UPDATE    │ │ ▼ DELETE    │     │  1,024   │      │
│  │ 1,234,567   │ │   89,012    │ │    3,456    │     │  evt/s   │      │
│  │ +128/窗口 ↑ │ │ +12/窗口 ↑  │ │ +2/窗口 ↑   │     │ ╲限速500╱ │      │
│  │ ▁▂▃▅▃▂▇ (迷你趋势) 每张卡内嵌 │     ╰──────────╯      │
│  └─────────────┘ └─────────────┘ └─────────────┘   [暂停/泄洪/限流状态] │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  I/U/D 三色堆叠渐变面积图（160px，随推送向左滚动）                  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

改造项一览（全部纯前端）：

| # | 改造项 | 数据来源 | 难度 |
|---|---|---|---|
| 4.1 | I/U/D 动画指标卡（CountUp + 迷你 sparkline + 环比角标） | summary 差分 | 中 |
| 4.2 | 实时速率仪表盘（替换/整合流控状态按钮组） | tags[0].trantransferIncr + perSecRateNums | 中 |
| 4.3 | 主图升级为 I/U/D 三色堆叠渐变面积图 | summary 差分入三条队列 | 低 |
| 4.4 | WebSocket 推送脉冲反馈（卡片 glow） | 现有 incomeRateFlash 机制扩展 | 低 |
| 4.5 | （可选）暗色监控大屏风格皮肤 | 纯 CSS | 低 |

## 四、改造项明细

### 4.1 I/U/D 动画指标卡

替换现有 L173-181 的三个 `nz-tag`。

**数据准备**：在组件内维护上一条消息的 summary 快照：

```typescript
// 处理 mq_tags_status 消息处（现 L525 附近）增加：
const prev = this.lastSummary;                     // 上一条快照，首条为 null
this.lastSummary = msg.summary;
this.incrDelta = prev == null ? {insert: 0, update: 0, delete: 0} : {
  insert: Math.max(0, msg.summary.tableInsertCount - prev.tableInsertCount),
  update: Math.max(0, msg.summary.tableUpdateCount - prev.tableUpdateCount),
  delete: Math.max(0, msg.summary.tableDeleteCount - prev.tableDeleteCount),
};
// 防误判：任务重启/savepoint 恢复后累计值可能回退，Math.max(0, ...) 兜底
```

**卡片结构**（每张卡）：
- 事件类型名 + 图标：Insert（`plus-circle`，绿 `#52c41a`）、Update（`edit`，橙 `#fa8c16`）、Delete（`minus-circle`，红 `#f5222d`）；
- 累计大数字：**CountUp 动画**——手写约 30 行 `requestAnimationFrame` 插值（不引第三方库），1 秒内从旧值滚到新值；
- 环比角标：`+N`（本窗口增量），N>0 时短暂高亮；
- 迷你 sparkline：卡片底部 20px 高的微型面积图，展示该事件最近 10 个窗口的增量序列（复用 4.3 的差分队列，chart.js 迷你实例，关闭所有坐标轴/网格/tooltip）。

**组件拆分建议**：step4 组件目前是内联 template（已 700+ 行），建议新建
`src/runtime/incr-stat-card.component.ts`（`incr-stat-card` 选择器，@Input 传 type/total/delta/history），保持 step4 可读性。模块注册在 `runtime.module.ts`（与现有组件同模块，参考 `line.chart.component.ts` 的注册方式）。

### 4.2 实时速率仪表盘（Gauge）

整合并替换现有流控状态按钮组（L143-165）的**展示**职能（"流控"设置按钮保留）。

**实现**（chart.js doughnut，无需插件）：

```typescript
{
  type: 'doughnut',
  data: { datasets: [{ data: [current, rest], ... }] },
  options: { circumference: 180, rotation: 270, cutout: '72%', ... }
}
```

- 当前速率 = `tags[0].trantransferIncr / 采集窗口秒数`（窗口秒数见"七、风险"第 3 条的取值方式）；
- 表盘上限：`controllerType === 4`（RateLimit）时 = `perSecRateNums`；否则取动态上限 `max(历史峰值 × 1.2, 100)`；
- 区间配色：0~70% 绿、70~90% 橙、90~100% 红；限流状态下表盘描边改紫色（对应现有限流按钮的紫色 `#680091`），泄洪状态改黄色，暂停状态置灰并显示暂停图标——**四种流控状态完全融入表盘**；
- 中心大数字：当前 evt/s，同样走 CountUp。

### 4.3 主流量图升级

替换 L437-459 的图表配置与 L531-532 的数据装配：

1. **三条差分队列**：`insertQueue / updateQueue / deleteQueue`（各 `FixedLengthQueue<number>(10)`，或直接扩容到 20 个点让曲线更平滑），每条消息到达时把 4.1 算出的增量入队；
2. **堆叠面积图**：`type: 'line'`，三个 dataset `stack: 'events'` + `fill: true`，`options.scales.y.stacked = true`；
3. **渐变填充**：首次渲染时通过 `canvas.getContext('2d').createLinearGradient` 生成三色渐变（绿/橙/红自上而下透明化），chart.js 3.x 原生支持 scriptable backgroundColor；
4. 图表高度 100px → **160px**；保留 y 轴隐藏、无网格的极简风格；
5. （可选）加 `nz-segmented` 切换："分类堆叠 / 总量曲线"，总量模式保留现有单线样式，老用户无迁移成本。

### 4.4 推送脉冲反馈

扩展现有 `incomeRateFlash`（L524-538 已有 500ms 定时复位逻辑）：

- 消息到达时给整个面板容器加 `.pulse` class，CSS `box-shadow` 呼吸扩散动画 500ms 后移除；
- 三张指标卡分别按"本窗口是否有增量"独立 glow（增量为 0 的卡不闪，避免全盘乱闪）。

### 4.5 （可选）暗色监控大屏皮肤

- 面板容器加深色渐变背景（如 `#0d1b2a → #1b263b`）+ 1px 发光描边 + 圆角卡片化；
- 与下方白色 sources 表格形成"控制台 vs 大屏"的视觉反差；
- 通过容器 class 开关（如 `.dark-monitor`），默认是否开启由需求方定；chart.js 坐标/文字颜色需配套切换。

## 五、实施步骤（文件级）

| 步骤 | 文件 | 改动 |
|---|---|---|
| 1 | `src/runtime/incr-stat-card.component.ts`（新建） | I/U/D 指标卡：CountUp、迷你 sparkline、环比角标、glow class |
| 2 | `src/runtime/incr-rate-gauge.component.ts`（新建） | 速率仪表盘 doughnut + 流控状态展示 |
| 3 | `src/runtime/runtime.module.ts` | 声明两个新组件（参照同目录现有组件注册方式） |
| 4 | `src/runtime/incr.build.step4.running.component.ts` | 模板 L137-212 重写；类内增加 `lastSummary` 快照、差分计算、三条增量队列、图表配置替换（L437-459、L531-532）；`incomeRateFlash` 扩展为面板级 pulse |
| 5 | 同上 styles 块（L339-426） | 追加卡片/glow/渐变/暗色皮肤样式 |

**注意**：两个新组件若用 `ChangeDetectionStrategy.OnPush`，@Input 必须传新引用（不可变更新），与本组件现有的 Default 策略混用时以 input 新对象触发变更检测。

## 六、验证方式

1. 前端本地调试：`tis-console` 工程 `ng serve`，连一个运行中的 TIS 实例；
2. 造数据：准备一个 MySQL→目标端 的增量通道，对源表持续执行 insert/update/delete 混合 SQL（可用 `mysqlslap` 或简单脚本按可调控率写入）；
3. 观察清单：
   - [ ] 三张指标卡数字随推送滚动（CountUp 动画），颜色正确；
   - [ ] 环比角标 `+N` 与源库实际写入速率吻合；
   - [ ] 仪表盘读数 ≈ 实际每秒事件数（±窗口误差）；在"流控"里设置限流 500/s 后表盘上限与紫色描边生效，暂停时置灰；
   - [ ] 主图三色堆叠滚动，总量与三卡增量之和大致相等；
   - [ ] 每次推送面板脉冲一次，无增量卡片不闪；
   - [ ] 任务 savepoint 恢复后累计值回退不出现负数增量；
   - [ ] 切换 Tab（基本/配置/操作/Savepoint）再返回，图表与订阅不泄漏（`ngOnDestroy` 退订，L605-612）；
4. 构建回归：`tis-console` 前端整体 build 通过，控制台无 chart.js 报错。

## 七、风险与注意点

1. **差分语义**：I/U/D 趋势依赖相邻消息差分，首条消息（或页面刚打开）无快照时增量显示 0；WS 断连重连后需重置 `lastSummary` 避免跨长时间差分出一个巨大尖峰。
2. **窗口速率换算**：`trantransferIncr` 是约 10 秒窗口和（`TopicTagIncrStatus` 缓存 10 秒快照），不是消息间隔内的增量。仪表盘换算 evt/s 时应除以窗口时长；窗口时长前端不易直接拿到——可用相邻 `lastUpdate` 时间戳差值估算，或在分母上取固定经验值并在 tooltip 注明"约值"。
3. **采集间隔可变**：推送间隔 = `IndexCollectionConfig.duration`（用户可在"采集参数"里改），脉冲/动画时长不要与推送间隔强耦合。
4. **图表性能**：3 张迷你 sparkline + 1 主图 + 1 仪表盘共 5 个 chart.js 实例，推送间隔默认 10s 时无压力；若用户把采集间隔调到 1s，需确认动画不卡顿（必要时迷你图改纯 SVG 折线）。
5. **组件销毁**：所有 `setTimeout`/`requestAnimationFrame`/chart 实例必须在 `ngOnDestroy` 清理，现有组件已有 `componentDestroy` 标记模式，沿用之。