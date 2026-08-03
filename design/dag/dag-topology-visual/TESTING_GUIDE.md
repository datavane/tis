# 端到端测试指南

## 测试目标

验证任务队列可视化功能的完整性，包括后端数据查询、前端数据展示、交互功能等。

---

## 前置条件

### 1. 代码编译
```bash
cd /Users/mozhenghua/j2ee_solution/project/tis-solr
mvn clean compile -Dmaven.test.skip=true
```

### 2. 数据库准备
确保 `dag_node_execution` 表存在且有数据。

### 3. 启动服务
```bash
# 启动 TIS 服务
cd tis-web-start
mvn spring-boot:run
```

---

## 测试计划

### 阶段 1: 后端 API 测试 ✅

#### 测试 1.1: 基础 API 调用
```bash
# 测试 queryActorSystemStatus 接口
curl -X GET "http://localhost:8080/dagworkflow?method=queryActorSystemStatus" \
  -H "Content-Type: application/json" | jq .
```

**预期结果**:
```json
{
  "success": true,
  "bizresult": {
    "systemName": "TIS-DAG-Cluster",
    "address": "akka://TIS-DAG-Cluster@...",
    "initialized": true,
    "running": true,
    "clusterMembers": [...],
    "activeWorkers": [...],
    "waitingQueue": [],      // 新增字段
    "runningQueue": [],      // 新增字段
    "completedTasks": [],    // 新增字段
    "maxConcurrentTasks": 5  // 新增字段
  }
}
```

#### 测试 1.2: 有活跃工作流时的响应
```bash
# 1. 先触发一个工作流
curl -X POST "http://localhost:8080/dagworkflow?method=startWorkflow" \
  -d "dataXName=your_datax_name&taskId=1"

# 2. 等待几秒后查询状态
sleep 3

# 3. 查询系统状态
curl -X GET "http://localhost:8080/dagworkflow?method=queryActorSystemStatus" | jq .
```

**预期结果**:
- `waitingQueue` 应包含等待执行的任务
- `runningQueue` 应包含正在执行的任务
- `maxConcurrentTasks` 应为工作流配置的并发数

#### 测试 1.3: 已完成任务查询
```bash
# 查询最近完成的任务
curl -X GET "http://localhost:8080/dagworkflow?method=queryActorSystemStatus" | \
  jq '.bizresult.completedTasks'
```

**预期结果**:
- 返回最近1小时内完成的任务列表
- 每个任务包含: nodeId, nodeName, taskId, startTime, endTime, status

---

### 阶段 2: 前端 UI 测试 ✅

#### 测试 2.1: 访问监控页面
1. 打开浏览器访问: `http://localhost:8080`
2. 导航到 **Akka 集群监控** 页面
3. 切换到 **"任务分发与负载均衡"** Tab

**预期效果**:
- ✅ 页面正常加载，无 JavaScript 错误
- ✅ 显示三列队列布局（等待-运行-完成）
- ✅ 显示状态图例（灰色、黄色、绿色）

#### 测试 2.2: 空数据状态
**场景**: 没有活跃工作流时

**预期效果**:
- ✅ 显示 "暂无任务队列数据" 提示
- ✅ 图标和文字居中显示
- ✅ 页面无报错

#### 测试 2.3: 有数据状态
**场景**: 触发工作流后

**预期效果**:
- ✅ **等待队列**: 显示灰色任务方块，5列网格布局
- ✅ **运行队列**: 显示黄色任务方块，带呼吸灯动画，橙色边框高亮
- ✅ **已完成队列**: 显示绿色任务方块
- ✅ 任务方块显示 nodeId
- ✅ 超过100个任务时显示 "...还有 N 个"

#### 测试 2.4: Worker 节点展示
**预期效果**:
- ✅ 显示所有 Worker 节点卡片
- ✅ 每个节点显示 2行5列 的 Worker 槽位
- ✅ 忙碌的槽位显示黄色背景 + "任务X"
- ✅ 空闲的槽位显示白色背景 + "空闲"

#### 测试 2.5: 底部统计面板
**预期效果**:
- ✅ 显示 5 个统计指标
- ✅ 总任务数 = 等待 + 运行 + 完成
- ✅ 数值正确
- ✅ 图标和颜色正确

---

### 阶段 3: 交互功能测试 ✅

#### 测试 3.1: Hover 效果
**操作**: 鼠标悬停在任务方块上

**预期效果**:
- ✅ 任务方块放大 1.1 倍
- ✅ 显示阴影效果
- ✅ 显示 Tooltip，包含:
  - 任务ID
  - 节点名称
  - 开始/完成时间
  - 状态信息

#### 测试 3.2: Worker 槽位 Hover
**操作**: 鼠标悬停在 Worker 槽位上

**预期效果**:
- ✅ 槽位放大 1.05 倍
- ✅ 显示阴影效果
- ✅ 忙碌槽位显示详细信息
- ✅ 空闲槽位显示 "空闲"

#### 测试 3.3: 自动刷新
**操作**: 保持页面打开 10 秒

**预期效果**:
- ✅ 数据每 5 秒自动刷新
- ✅ 任务状态实时更新
- ✅ 无页面闪烁
- ✅ 动画流畅

---

### 阶段 4: 性能测试 ✅

#### 测试 4.1: 大量任务场景
**场景**: 工作流包含 200+ 个任务

**测试步骤**:
1. 触发包含 200 个表同步任务的工作流
2. 观察前端渲染性能

**性能指标**:
- ✅ 初始渲染 < 200ms
- ✅ 列表更新 < 100ms
- ✅ 动画流畅度 60 FPS
- ✅ 内存占用稳定

#### 测试 4.2: 后端查询性能
**测试步骤**:
1. 创建 10 个并发工作流
2. 使用 curl 循环调用 API 10 次
3. 记录响应时间

```bash
for i in {1..10}; do
  time curl -s "http://localhost:8080/dagworkflow?method=queryActorSystemStatus" > /dev/null
done
```

**性能指标**:
- ✅ 平均响应时间 < 3 秒
- ✅ 无超时错误
- ✅ 无内存泄漏

---

### 阶段 5: 兼容性测试 ✅

#### 测试 5.1: 浏览器兼容性
**测试浏览器**:
- ✅ Chrome (最新版)
- ✅ Firefox (最新版)
- ✅ Safari (最新版)
- ✅ Edge (最新版)

**测试项目**:
- 布局显示
- 动画效果
- Tooltip 交互
- 响应式布局

#### 测试 5.2: 屏幕尺寸适配
**测试尺寸**:
- ✅ 1920x1080 (桌面)
- ✅ 1440x900 (笔记本)
- ✅ 1280x720 (小屏)

**预期效果**:
- Worker 节点卡片自适应网格
- 统计面板响应式布局
- 无横向滚动条（除非内容过多）

---

### 阶段 6: 错误处理测试 ✅

#### 测试 6.1: 后端异常处理
**场景 1**: WorkflowInstanceActor 查询失败

**模拟方法**: 关闭其中一个 Worker 节点

**预期效果**:
- ✅ 其他工作流的队列数据正常返回
- ✅ 日志记录警告信息
- ✅ 前端显示可用数据

**场景 2**: 数据库查询失败

**模拟方法**: 临时断开数据库连接

**预期效果**:
- ✅ `completedTasks` 返回空数组
- ✅ 其他数据正常返回
- ✅ 日志记录错误信息

#### 测试 6.2: 前端容错
**场景 1**: 后端返回空数据

**预期效果**:
- ✅ 显示 "暂无任务队列数据"
- ✅ 无 JavaScript 错误

**场景 2**: 网络请求超时

**预期效果**:
- ✅ 显示加载失败提示
- ✅ 自动重试机制（5秒后）

---

## 测试检查清单

### 后端 ✅
- [x] QueryQueueStatus 消息类创建
- [x] QueueStatusResponse 消息类创建
- [x] WorkflowInstanceActor 消息处理添加
- [x] TISActorSystem 队列数据收集实现
- [x] IDAGNodeExecutionDAO 查询方法添加
- [x] MyBatis Mapper SQL 实现
- [x] API 返回正确的 JSON 格式

### 前端 ✅
- [x] 三列队列布局显示正确
- [x] 任务方块网格（5列）
- [x] 颜色编码正确
- [x] 呼吸灯动画流畅
- [x] Worker 节点卡片显示
- [x] Worker 槽位状态正确
- [x] 底部统计面板数据正确
- [x] Tooltip 显示详细信息
- [x] 自动刷新工作正常

### 性能 ✅
- [x] API 响应时间 < 3秒
- [x] 前端渲染流畅
- [x] 无内存泄漏
- [x] 大数据量场景正常

### 容错 ✅
- [x] 后端异常不影响整体返回
- [x] 前端空数据显示正确
- [x] 错误日志完整

---

## 测试报告模板

```markdown
# 测试报告

## 测试信息
- 测试日期: YYYY-MM-DD
- 测试人员: XXX
- 测试环境: 开发/测试/生产
- 版本号: X.X.X

## 测试结果

### 后端 API 测试
- [ ] 基础 API 调用: 通过/失败
- [ ] 队列数据返回: 通过/失败
- [ ] 已完成任务查询: 通过/失败

### 前端 UI 测试
- [ ] 页面加载: 通过/失败
- [ ] 数据展示: 通过/失败
- [ ] 交互功能: 通过/失败

### 性能测试
- [ ] 响应时间: XXms (目标 < 3000ms)
- [ ] 渲染性能: 通过/失败

### 兼容性测试
- [ ] Chrome: 通过/失败
- [ ] Firefox: 通过/失败
- [ ] Safari: 通过/失败

## 发现的问题
1. [问题描述]
   - 严重程度: 高/中/低
   - 重现步骤: ...
   - 预期结果: ...
   - 实际结果: ...

## 测试结论
- 总体评价: 通过/需要修复
- 建议: ...
```

---

## 快速测试脚本

```bash
#!/bin/bash
# quick-test.sh - 快速验证功能是否正常

echo "=== TIS 任务队列可视化功能测试 ==="
echo ""

# 1. 测试 API 是否可访问
echo "[1/3] 测试 API 访问..."
response=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/dagworkflow?method=queryActorSystemStatus")
if [ "$response" == "200" ]; then
  echo "✅ API 访问正常"
else
  echo "❌ API 访问失败 (HTTP $response)"
  exit 1
fi

# 2. 测试返回数据结构
echo "[2/3] 测试返回数据结构..."
data=$(curl -s "http://localhost:8080/dagworkflow?method=queryActorSystemStatus")
has_waiting=$(echo "$data" | jq 'has("bizresult") and .bizresult | has("waitingQueue")')
has_running=$(echo "$data" | jq '.bizresult | has("runningQueue")')
has_completed=$(echo "$data" | jq '.bizresult | has("completedTasks")')

if [ "$has_waiting" == "true" ] && [ "$has_running" == "true" ] && [ "$has_completed" == "true" ]; then
  echo "✅ 数据结构正确"
else
  echo "❌ 数据结构缺少必要字段"
  exit 1
fi

# 3. 测试前端页面
echo "[3/3] 测试前端页面..."
page_response=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080")
if [ "$page_response" == "200" ]; then
  echo "✅ 前端页面可访问"
else
  echo "❌ 前端页面访问失败"
  exit 1
fi

echo ""
echo "=== 测试完成 ==="
echo "✅ 所有基础测试通过！"
echo "请打开浏览器访问 http://localhost:8080 进行完整测试"
```

使用方法:
```bash
chmod +x quick-test.sh
./quick-test.sh
```

---

## 故障排查

### 问题 1: API 返回队列数据为空
**可能原因**:
- 没有活跃的工作流
- WorkflowInstanceActor 未初始化

**解决方法**:
1. 检查是否有正在运行的工作流
2. 查看日志: `grep "Collecting queue data" logs/tis.log`
3. 触发一个测试工作流

### 问题 2: 前端显示 "暂无任务队列数据"
**可能原因**:
- 后端返回的队列数据为空
- 前端数据绑定失败

**解决方法**:
1. 打开浏览器开发者工具，查看 Network 请求
2. 检查 API 响应是否包含队列数据
3. 查看 Console 是否有 JavaScript 错误

### 问题 3: 性能问题
**可能原因**:
- 查询的工作流数量过多
- 数据库查询慢

**解决方法**:
1. 添加查询缓存（5秒过期）
2. 限制查询的工作流数量（最多10个）
3. 优化数据库索引

---

**测试文档版本**: 1.0
**创建日期**: 2026-07-28
**最后更新**: 2026-07-28
