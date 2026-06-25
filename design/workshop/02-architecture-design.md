# TIS Workshop 架构设计

## 概述

本文档详细描述 TIS Workshop 低代码平台的技术架构设计，包括核心组件、数据流、接口设计等。

## 整体架构

### 分层架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     应用层 (Application Layer)               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ 应用构建器    │  │ 应用运行时    │  │ 应用市场      │      │
│  │ (Designer)   │  │ (Runtime)    │  │ (Marketplace)│      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   逻辑执行层 (Logic Layer)                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Function     │  │ Action       │  │ Workflow     │      │
│  │ Registry     │  │ Engine       │  │ Engine       │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   本体层 (Ontology Layer)                    │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌─────────┐ ┌─────────┐ │
│  │ObjectType│Property│ │Linker │ │Constraint│ │Glossary│ │
│  └────────┘ └────────┘ └────────┘ └─────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   数据层 (Data Layer)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ 数据源连接    │  │ 数据读写      │  │ 数据同步      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

## 核心组件设计

### 1. Function Registry（函数注册中心）

#### 职责
- Function 的注册和发现
- Function 调用和缓存
- 参数验证和类型转换

#### 接口设计

```java
public interface FunctionRegistry {
    /**
     * 注册 Function
     */
    void register(OntologyFunction function);
    
    /**
     * 调用 Function
     */
    Object invoke(String functionName, Map<String, Object> inputs);
    
    /**
     * 批量调用 Function（性能优化）
     */
    List<Object> batchInvoke(String functionName, List<Map<String, Object>> inputsList);
    
    /**
     * 获取 Function 元信息
     */
    FunctionMeta getMeta(String functionName);
    
    /**
     * 列出所有 Function
     */
    List<FunctionMeta> listFunctions(FunctionCategory category);
}
```

#### 实现要点

**1. SPI 自动发现**
```java
// 启动时扫描所有 Function 实现
ServiceLoader<OntologyFunction> loader = ServiceLoader.load(OntologyFunction.class);
for (OntologyFunction function : loader) {
    registry.register(function);
}
```

**2. 结果缓存**
```java
// 使用 Guava Cache 缓存 Function 结果
Cache<String, Object> cache = CacheBuilder.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build();
```

**3. 批量优化**
```java
// 批量调用避免 N+1 问题
public List<Object> batchInvoke(String functionName, List<Map<String, Object>> inputsList) {
    // 一次性加载所有需要的数据
    // 批量计算并返回结果
}
```

### 2. Action Engine（动作执行引擎）

#### 职责
- Action 的注册和发现
- Action 执行编排（事务、重试、补偿）
- 权限检查和审计日志

#### 接口设计

```java
public interface ActionEngine {
    /**
     * 注册 Action
     */
    void register(OntologyAction action);
    
    /**
     * 同步执行 Action
     */
    ActionResult execute(String actionName, ActionContext context);
    
    /**
     * 异步执行 Action
     */
    CompletableFuture<ActionResult> executeAsync(String actionName, ActionContext context);
    
    /**
     * 获取 Action 元信息
     */
    ActionMeta getMeta(String actionName);
    
    /**
     * 列出用户可执行的 Action
     */
    List<ActionMeta> listExecutableActions(User user, ActionCategory category);
}
```

#### 执行流程

```java
public ActionResult execute(String actionName, ActionContext context) {
    // 1. 查找 Action
    OntologyAction action = findAction(actionName);
    
    // 2. 权限检查
    if (!checkPermission(action, context.getUser())) {
        return ActionResult.failure("权限不足");
    }
    
    // 3. 参数验证
    ValidationResult validation = action.precheck(context);
    if (!validation.isValid()) {
        return ActionResult.failure(validation.getMessage());
    }
    
    // 4. 执行 Action（带事务和重试）
    ActionResult result = executeWithTransaction(action, context);
    
    // 5. 记录审计日志
    auditLog(context.getUser(), actionName, context.getParams(), result);
    
    // 6. 触发后置事件
    if (result.isSuccess()) {
        publishEvent(new ActionExecutedEvent(actionName, context, result));
    }
    
    return result;
}
```

#### 事务管理

```java
@Transactional(rollbackFor = Exception.class)
public ActionResult executeWithTransaction(OntologyAction action, ActionContext context) {
    try {
        return action.execute(context);
    } catch (Exception e) {
        // 记录失败日志
        logger.error("Action execution failed", e);
        throw e;  // 触发事务回滚
    }
}
```

### 3. Application Runtime（应用运行时）

#### 职责
- 加载应用定义
- 渲染应用界面
- 处理用户交互

#### 应用定义结构

```java
public class OntologyApplication extends Ontology {
    private String name;              // 应用唯一标识
    private String displayName;       // 显示名称
    private String description;       // 描述
    private List<AppPage> pages;      // 页面列表
    private ApplicationLayout layout; // 布局配置
    private Map<String, Object> config; // 其他配置
    
    // 权限配置
    private List<String> requiredRoles;
    
    // 数据源配置
    private List<String> usedObjectTypes;
    private List<String> usedDataSources;
}

public class AppPage {
    private String name;
    private String displayName;
    private PageType type;  // TABLE_VIEW / FORM_VIEW / DASHBOARD / WORKFLOW
    
    // 数据配置
    private String objectType;  // 绑定的 ObjectType
    private String dataSource;  // 可选：自定义数据源
    
    // 列配置
    private List<ColumnConfig> columns;
    
    // 筛选器
    private List<FilterConfig> filters;
    
    // Action 按钮
    private List<ActionButton> actions;
    
    // 布局配置
    private PageLayout layout;
}

public class ColumnConfig {
    private String name;        // 列名
    private String label;       // 显示标签
    private ColumnType type;    // PROPERTY / FUNCTION / CUSTOM
    
    // 如果是 PROPERTY
    private String propertyName;
    
    // 如果是 FUNCTION
    private String functionName;
    private Map<String, String> functionParams;
    
    // 格式化
    private String format;  // 如 "yyyy-MM-dd" / "0.00%" 等
    
    // 排序和筛选
    private boolean sortable;
    private boolean filterable;
}
```

#### 数据加载流程

```java
public class ApplicationDataLoader {
    
    public PageData loadPageData(AppPage page, Map<String, Object> params) {
        // 1. 加载 ObjectType 定义
        OntologyObjectType objectType = ontologyStore.load(page.getObjectType());
        
        // 2. 构建查询
        Query query = buildQuery(objectType, page.getFilters(), params);
        
        // 3. 执行查询
        List<Map<String, Object>> rows = dataSource.query(query);
        
        // 4. 计算派生列（调用 Function）
        for (Map<String, Object> row : rows) {
            for (ColumnConfig column : page.getColumns()) {
                if (column.getType() == ColumnType.FUNCTION) {
                    Object value = functionRegistry.invoke(
                        column.getFunctionName(),
                        buildFunctionInputs(column, row)
                    );
                    row.put(column.getName(), value);
                }
            }
        }
        
        // 5. 返回数据
        return new PageData(rows, query.getTotalCount());
    }
}
```

### 4. Workflow Engine（工作流引擎）

#### 职责
- 工作流定义和解析
- 工作流执行和状态管理
- 任务分配和通知

#### 工作流定义

```java
public class Workflow {
    private String name;
    private String displayName;
    private List<WorkflowStep> steps;
    private Map<String, Transition> transitions;
    
    // 触发条件
    private TriggerConfig trigger;
}

public class WorkflowStep {
    private String id;
    private String name;
    private StepType type;  // ACTION / APPROVAL / NOTIFICATION / CONDITION / PARALLEL
    
    // 如果是 ACTION 步骤
    private String actionName;
    private Map<String, Object> actionParams;
    
    // 如果是 APPROVAL 步骤
    private List<String> approvers;
    private ApprovalStrategy strategy;  // ANY / ALL / MAJORITY
    
    // 如果是 CONDITION 步骤
    private String condition;  // 条件表达式
    
    // 超时配置
    private Duration timeout;
    
    // 失败处理
    private FailureStrategy onFailure;  // RETRY / SKIP / ABORT / COMPENSATE
}
```

#### 执行引擎

```java
public class WorkflowExecutionEngine {
    
    public WorkflowInstance executeWorkflow(String workflowName, Map<String, Object> inputs) {
        // 1. 加载工作流定义
        Workflow workflow = workflowStore.load(workflowName);
        
        // 2. 创建工作流实例
        WorkflowInstance instance = new WorkflowInstance(workflow, inputs);
        instance.setStatus(WorkflowStatus.RUNNING);
        workflowInstanceStore.save(instance);
        
        // 3. 执行第一个步骤
        executeNextStep(instance);
        
        return instance;
    }
    
    private void executeNextStep(WorkflowInstance instance) {
        WorkflowStep currentStep = instance.getCurrentStep();
        
        try {
            switch (currentStep.getType()) {
                case ACTION:
                    executeActionStep(instance, currentStep);
                    break;
                case APPROVAL:
                    executeApprovalStep(instance, currentStep);
                    break;
                case NOTIFICATION:
                    executeNotificationStep(instance, currentStep);
                    break;
                case CONDITION:
                    executeConditionStep(instance, currentStep);
                    break;
                case PARALLEL:
                    executeParallelStep(instance, currentStep);
                    break;
            }
        } catch (Exception e) {
            handleStepFailure(instance, currentStep, e);
        }
    }
    
    private void executeActionStep(WorkflowInstance instance, WorkflowStep step) {
        // 执行 Action
        ActionResult result = actionEngine.execute(
            step.getActionName(),
            buildActionContext(instance, step)
        );
        
        if (result.isSuccess()) {
            // 记录结果
            instance.recordStepResult(step.getId(), result);
            
            // 进入下一步
            transitionToNextStep(instance);
        } else {
            throw new ActionExecutionException(result.getMessage());
        }
    }
}
```

## 数据流设计

### 1. 应用加载流程

```
用户访问应用
    ↓
加载应用定义 (OntologyApplication)
    ↓
渲染应用框架（页面标签、导航）
    ↓
加载当前页面定义 (AppPage)
    ↓
构建数据查询（基于 ObjectType + Filters）
    ↓
执行查询
    ↓
计算派生列（调用 Function）
    ↓
渲染页面（表格/表单/Dashboard）
    ↓
等待用户交互
```

### 2. Function 调用流程

```
页面渲染触发 Function 调用
    ↓
检查缓存（命中则直接返回）
    ↓
验证输入参数
    ↓
执行 Function.apply()
    ↓
缓存结果
    ↓
返回给前端
```

### 3. Action 执行流程

```
用户点击 Action 按钮
    ↓
弹出参数配置对话框
    ↓
用户填写参数并确认
    ↓
前端调用 /api/action/execute
    ↓
后端 Action Engine 接收请求
    ↓
权限检查
    ↓
参数验证（precheck）
    ↓
开启事务
    ↓
执行 Action.execute()
    ↓
提交事务
    ↓
记录审计日志
    ↓
触发后置事件
    ↓
返回结果给前端
    ↓
前端显示成功/失败消息
    ↓
刷新页面数据
```

## 接口设计

### 1. 应用管理 API

```
GET  /api/workshop/applications           # 列出所有应用
GET  /api/workshop/applications/{name}    # 获取应用定义
POST /api/workshop/applications           # 创建应用
PUT  /api/workshop/applications/{name}    # 更新应用
DELETE /api/workshop/applications/{name}  # 删除应用
```

### 2. 页面数据 API

```
POST /api/workshop/pages/{pageName}/data  # 加载页面数据
  Body: {
    "filters": {...},
    "pagination": {"page": 1, "pageSize": 20},
    "sort": {"field": "createTime", "order": "desc"}
  }
  Response: {
    "data": [...],
    "total": 100
  }
```

### 3. Function 调用 API

```
POST /api/workshop/functions/{functionName}/invoke
  Body: {
    "inputs": {...}
  }
  Response: {
    "result": ...
  }

POST /api/workshop/functions/{functionName}/batch-invoke
  Body: {
    "inputsList": [...]
  }
  Response: {
    "results": [...]
  }
```

### 4. Action 执行 API

```
POST /api/workshop/actions/{actionName}/execute
  Body: {
    "params": {...},
    "context": {...}
  }
  Response: {
    "success": true,
    "message": "操作成功",
    "data": {...}
  }

GET /api/workshop/actions/{actionName}/meta
  Response: {
    "name": "createSyncTask",
    "displayName": "创建同步任务",
    "category": "DATA_INTEGRATION",
    "params": [...]
  }
```

## 性能优化策略

### 1. Function 计算优化

**问题**：表格中每行都调用 Function，可能产生 N+1 查询问题

**方案 1：批量计算**
```java
// 不推荐：逐行调用
for (Map<String, Object> row : rows) {
    row.put("score", functionRegistry.invoke("calculateScore", row));
}

// 推荐：批量调用
List<Object> scores = functionRegistry.batchInvoke(
    "calculateScore",
    rows.stream().map(this::buildInputs).collect(Collectors.toList())
);
```

**方案 2：结果缓存**
```java
// 使用 Guava Cache
@Cacheable(key = "#functionName + ':' + #inputs.hashCode()")
public Object invoke(String functionName, Map<String, Object> inputs) {
    // ...
}
```

**方案 3：异步计算 + 前端轮询**
```java
// 对于耗时的 Function，先返回 placeholder
if (function.isExpensive()) {
    submitAsyncTask(functionName, inputs);
    return "计算中...";
}
```

### 2. 数据加载优化

**分页加载**
```java
// 前端虚拟滚动 + 后端分页
public PageData loadPageData(int page, int pageSize) {
    // 只查询当前页的数据
}
```

**字段裁剪**
```java
// 只查询需要显示的列
SELECT col1, col2, col3  -- 而不是 SELECT *
```

**预加载关联数据**
```java
// 使用 JOIN 避免 N+1
SELECT ot.*, link.target_name
FROM object_type ot
LEFT JOIN linker link ON ot.id = link.source_id
```

### 3. 前端渲染优化

**虚拟滚动**
```typescript
<cdk-virtual-scroll-viewport itemSize="50" class="viewport">
  <div *cdkVirtualFor="let row of data">
    {{ row.name }}
  </div>
</cdk-virtual-scroll-viewport>
```

**懒加载组件**
```typescript
// 按需加载 Dashboard 组件
const DashboardComponent = () => import('./dashboard.component');
```

## 安全设计

### 1. 权限控制

**三级权限体系**：
- **应用级别**：用户是否能访问该应用
- **页面级别**：用户是否能访问该页面
- **Action 级别**：用户是否能执行该 Action

**实现**：
```java
public boolean checkPermission(User user, String actionName) {
    OntologyAction action = actionRegistry.get(actionName);
    String requiredPermission = action.getMeta().getRequiredPermission();
    
    return permissionService.hasPermission(user, requiredPermission);
}
```

### 2. 审计日志

**记录所有 Action 执行**：
```java
public class ActionAuditLog {
    private String actionName;
    private String userId;
    private String userName;
    private Map<String, Object> params;
    private ActionResult result;
    private long executionTime;
    private Timestamp timestamp;
    private String ip;
    private String userAgent;
}
```

**日志存储**：
- 使用独立的审计日志表
- 定期归档到对象存储
- 支持日志查询和分析

### 3. 数据安全

**SQL 注入防护**：
```java
// 使用参数化查询
PreparedStatement stmt = conn.prepareStatement(
    "SELECT * FROM ? WHERE id = ?"
);
stmt.setString(1, tableName);
stmt.setLong(2, id);
```

**敏感数据脱敏**：
```java
// 在 Function 中自动脱敏
if (property.isSensitive()) {
    return maskSensitiveData(value);
}
```

## 扩展性设计

### 1. 插件化扩展

**Function 插件**：
```java
@TISPlugin(name = "customFunction")
public class CustomFunction extends OntologyFunction {
    @Override
    public Object apply(Map<String, Object> inputs) {
        // 自定义逻辑
    }
}
```

**Action 插件**：
```java
@TISPlugin(name = "customAction")
public class CustomAction extends OntologyAction {
    @Override
    public ActionResult execute(ActionContext context) {
        // 自定义逻辑
    }
}
```

### 2. 事件驱动

**发布事件**：
```java
// Action 执行后发布事件
eventBus.post(new ActionExecutedEvent(
    actionName,
    context,
    result
));
```

**订阅事件**：
```java
@Subscribe
public void onActionExecuted(ActionExecutedEvent event) {
    // 自定义后置处理
    // 如：发送通知、同步数据等
}
```

### 3. 自定义页面类型

**注册自定义页面类型**：
```java
PageTypeRegistry.register(new CustomPageType() {
    @Override
    public String getTypeName() {
        return "CUSTOM_DASHBOARD";
    }
    
    @Override
    public PageRenderer createRenderer() {
        return new CustomDashboardRenderer();
    }
});
```

## 总结

TIS Workshop 的架构设计遵循以下原则：

1. **元数据驱动** - 应用、页面、逻辑都是可配置的
2. **插件化扩展** - Function、Action、PageType 都支持插件
3. **分层解耦** - 应用层、逻辑层、本体层、数据层各司其职
4. **性能优先** - 批量计算、结果缓存、异步执行
5. **安全可控** - 权限控制、审计日志、数据脱敏

这个架构设计可以支撑：
- ✅ 快速构建数据应用（配置化）
- ✅ 灵活扩展业务逻辑（插件化）
- ✅ 高性能数据处理（批量 + 缓存）
- ✅ 企业级安全要求（权限 + 审计）
