# TIS 本体系统引入 Action 和 Function 的场景应用设计

## 背景

目前TIS已经接入了本体的大部分实体，如ObjectType、ValueObject、Glossary、SharedType等，利用这些本体实例已经构建了ChatBI场景应用。但这只是利用本体实现了业务的静态分析，还没有将本体语义层的优势发挥出来。

通过引入Palantir的Action和Function，可以实现基于本体语义的动态业务执行，构建更丰富的应用场景。

## 1. 核心概念定义

### 1.1 Palantir Function（函数）

**定义**：纯计算逻辑，不产生副作用的派生计算

**特征**：
- 幂等性：相同输入总是产生相同输出
- 无副作用：不修改任何状态，不触发外部操作
- 可组合：可以被其他Function调用
- 声明式：描述"是什么"而非"怎么做"

**本质**：Function是**被动的计算规则**，在查询、展示、校验时被调用

### 1.2 Palantir Action（动作）

**定义**：用户可执行的操作，会修改对象状态或触发业务流程

**特征**：
- 有副作用：会修改数据、触发流程、产生事件
- 用户触发：通过UI按钮、API调用等方式主动执行
- 有权限控制：需要检查用户是否有执行权限
- 可审计：执行记录需要被记录和追踪

**本质**：Action是**主动的业务操作**，由用户或系统触发执行

### 1.3 架构对比

| 维度 | Function | Action |
|------|----------|--------|
| **调用方式** | 被动调用（查询时、展示时） | 主动触发（点击按钮、API调用） |
| **副作用** | 无副作用 | 有副作用 |
| **幂等性** | 幂等 | 通常非幂等 |
| **权限** | 基于数据可见性 | 需要显式执行权限 |
| **审计** | 不需要 | 必须审计 |
| **UI表现** | 计算字段、派生列 | 操作按钮、工作流触发器 |
| **示例** | 计算客户等级、标准化地址 | 创建同步任务、修复数据 |

---

## 2. TIS 数据集成场景应用

### 2.1 智能任务编排

**Function应用**：
- 分析任务依赖关系（基于Linker关系）
- 计算最优执行顺序（拓扑排序）
- 预测任务执行时间
- 检测循环依赖

**Action应用**：
- 创建复杂的多步骤数据管道
- 自动调度依赖任务
- 动态调整任务优先级
- 批量重跑失败任务

**业务价值**：用户通过拖拽ObjectType构建数据流，系统自动生成可执行的DataX/Flink任务链。

### 2.2 实时数据质量监控

**Function应用**：
- 实时计算数据质量指标（完整性、准确性、一致性、时效性）
- 基于Constraint定义自动生成校验规则
- 计算数据质量趋势分数

**Action应用**：
- 触发质量告警
- 自动熔断异常数据流
- 通知相关负责人
- 生成质量报告

**业务价值**：数据质量问题秒级发现，自动阻断脏数据传播，保障下游系统数据可信度。

### 2.3 数据版本管理与回滚

**Function应用**：
- 对比数据版本差异
- 计算影响范围
- 分析回滚风险

**Action应用**：
- 创建数据快照
- 回滚到历史版本
- 生成变更日志
- 通知下游系统数据已回滚

**业务价值**：误操作或错误数据可快速回滚，类似Git的数据版本控制。

---

## 3. TIS 性能优化场景应用

### 3.1 智能资源优化

**Function应用**：
- 分析任务资源使用情况（CPU、内存、网络）
- 预测资源需求
- 检测资源瓶颈
- 计算最优并发度

**Action应用**：
- 动态调整任务并发度
- 自动扩缩容
- 重新分配资源
- 迁移任务到空闲节点

**业务价值**：降低资源浪费，提升集群利用率，减少任务排队时间。

### 3.2 成本分析与优化

**Function应用**：
- 计算数据存储成本（按ObjectType统计）
- 计算传输成本
- 计算计算成本
- 分析成本趋势

**Action应用**：
- 自动归档冷数据
- 优化存储策略（压缩、分区）
- 删除过期数据
- 生成成本优化建议报告

**业务价值**：量化数据资产成本，自动执行成本优化策略。

---

## 4. TIS 数据治理场景应用

### 4.1 数据安全与合规

**Function应用**：
- 检测敏感数据暴露风险
- 计算合规得分
- 识别未加密的敏感字段
- 分析数据访问模式

**Action应用**：
- 自动加密敏感数据
- 执行数据脱敏
- 生成合规报告
- 撤销违规访问权限

**业务价值**：满足GDPR、等保2.0等合规要求，防止数据泄露。

### 4.2 数据目录与发现

**Function应用**：
- 计算数据资产相似度
- 推荐相关数据集
- 评估数据资产价值（访问频率、业务重要性）
- 生成数据画像

**Action应用**：
- 发布数据API
- 创建数据订阅
- 申请数据访问权限
- 标记数据资产标签

**业务价值**：让数据消费者快速找到所需数据，避免重复建设。

### 4.3 跨系统数据一致性保障

**Function应用**：
- 检测多个系统间的数据不一致
- 计算一致性偏差度
- 分析不一致根因

**Action应用**：
- 触发数据对账
- 执行一致性修复
- 生成对账报告
- 通知不一致告警

**业务价值**：保障多系统数据最终一致性，提升数据可信度。

---

## 5. TIS 智能运维场景应用

### 5.1 智能异常检测与自愈

**Function应用**：
- 基于历史模式检测异常数据
- 检测任务异常行为（执行时间、资源消耗）
- 预测任务失败概率
- 分析异常影响范围

**Action应用**：
- 自动重试失败任务
- 切换备用数据源
- 降级处理（跳过非关键步骤）
- 触发人工介入流程

**业务价值**：减少人工干预，提升系统可用性，降低MTTR（平均恢复时间）。

### 5.2 影响分析（What-if Analysis）

**Function应用**：
- 模拟Schema变更的影响范围
- 预测任务执行时间变化
- 分析配置变更影响

**Action应用**：
- 生成变更影响报告
- 通知下游系统
- 创建变更审批流程
- 执行灰度变更

**业务价值**：变更前评估风险，避免大规模故障。

---

## 6. TIS ChatBI 增强场景应用

### 6.1 智能查询优化

**Function应用**：
- 分析查询复杂度
- 预测查询执行时间
- 生成查询优化建议
- 计算查询成本

**Action应用**：
- 自动创建物化视图
- 创建索引
- 重写低效查询
- 缓存热点查询结果

**业务价值**：加速ChatBI响应速度，降低数据库负载。

### 6.2 洞察自动化

**Function应用**：
- 检测数据异常趋势
- 发现业务指标相关性
- 生成可视化建议

**Action应用**：
- 自动生成洞察报告
- 创建仪表板
- 设置指标告警
- 分享洞察给团队

**业务价值**：从被动查询到主动洞察，提升数据驱动决策效率。

---

## 7. 实现架构设计

### 7.1 Function 实现架构

```java
// Function 基类
public abstract class OntologyFunction {
    
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TISFunction {
        String name();
        String description();
        FunctionCategory category();
        String[] inputProperties() default {};
        String outputType() default "String";
    }
    
    /**
     * 执行Function计算
     */
    public abstract Object apply(Object input, Map<String, Object> context);
    
    /**
     * 验证输入
     */
    public boolean validateInput(Object input) {
        return true;
    }
}

// Function 注册中心
public class FunctionRegistry {
    private final Map<String, OntologyFunction> functions = new ConcurrentHashMap<>();
    
    public void register(OntologyFunction function) {
        TISFunction annotation = function.getClass().getAnnotation(TISFunction.class);
        functions.put(annotation.name(), function);
    }
    
    public Object invoke(String functionName, Object input, Map<String, Object> context) {
        OntologyFunction function = functions.get(functionName);
        if (function == null) {
            throw new FunctionNotFoundException(functionName);
        }
        if (!function.validateInput(input)) {
            throw new InvalidInputException(functionName, input);
        }
        return function.apply(input, context);
    }
}
```

### 7.2 Action 实现架构

```java
// Action 基类
public abstract class OntologyAction {
    
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TISAction {
        String name();
        String description();
        ActionCategory category();
        String requiredPermission();
        boolean needsConfirmation() default true;
    }
    
    /**
     * 执行Action
     */
    public abstract ActionResult execute(ActionContext context);
    
    /**
     * 权限检查
     */
    public boolean checkPermission(User user) {
        TISAction annotation = this.getClass().getAnnotation(TISAction.class);
        return permissionService.hasPermission(user, annotation.requiredPermission());
    }
    
    /**
     * 预检查
     */
    public ValidationResult precheck(ActionContext context) {
        return ValidationResult.success();
    }
}

// Action 执行引擎
public class ActionEngine {
    public ActionResult executeAction(String actionName, ActionContext context) {
        // 1. 查找Action
        OntologyAction action = actionRegistry.get(actionName);
        if (action == null) {
            return ActionResult.failure().withMessage("Action不存在");
        }
        
        // 2. 权限检查
        if (!action.checkPermission(context.getUser())) {
            return ActionResult.failure().withMessage("无权限执行此操作");
        }
        
        // 3. 预检查
        ValidationResult validation = action.precheck(context);
        if (!validation.isSuccess()) {
            return ActionResult.failure().withMessage(validation.getMessage());
        }
        
        // 4. 执行Action（带事务）
        ActionResult result;
        try {
            result = transactionTemplate.execute(status -> {
                return action.execute(context);
            });
        } catch (Exception e) {
            result = ActionResult.failure()
                .withMessage("执行失败：" + e.getMessage())
                .withError(e);
        }
        
        // 5. 记录审计日志
        auditLog.record(context.getUser(), actionName, context.getParams(), result);
        
        // 6. 触发后置处理
        if (result.isSuccess()) {
            eventBus.post(new ActionExecutedEvent(actionName, context, result));
        }
        
        return result;
    }
}
```

### 7.3 与本体系统集成

```java
// 在ObjectType定义中关联Function
public class PropertyDef {
    private String name;
    private ValueType type;
    private List<ValueConstraint> constraints;
    
    // 派生属性关联的Function
    private String derivedByFunction;
    private Map<String, String> functionParams;
    
    /**
     * 获取属性值（支持派生属性）
     */
    public Object getValue(ObjectInstance instance) {
        if (derivedByFunction != null) {
            // 调用Function计算派生值
            return functionRegistry.invoke(
                derivedByFunction,
                instance,
                functionParams
            );
        } else {
            // 直接返回存储值
            return instance.getStoredValue(name);
        }
    }
}

// 在ObjectType定义中关联Action
public class ObjectType {
    private String name;
    private List<PropertyDef> properties;
    
    // 可用的Action列表
    private List<String> availableActions;
    
    /**
     * 获取可执行的Action（基于用户权限过滤）
     */
    public List<ActionMetadata> getExecutableActions(User user) {
        return availableActions.stream()
            .map(actionName -> actionRegistry.getMetadata(actionName))
            .filter(action -> action.checkPermission(user))
            .collect(Collectors.toList());
    }
}
```

---

## 8. 具体Function示例

### 8.1 数据标准化Function

```java
/**
 * 电话号码标准化Function
 */
@TISFunction(
    name = "standardizePhoneNumber",
    description = "标准化电话号码格式",
    category = FunctionCategory.DATA_TRANSFORM
)
public class PhoneNumberStandardizer extends OntologyFunction {
    @Override
    public String apply(String rawPhone, Map<String, Object> context) {
        String digits = rawPhone.replaceAll("[^0-9]", "");
        if (digits.startsWith("86") && digits.length() == 13) {
            return formatChinaPhone(digits);
        }
        return rawPhone;
    }
}
```

### 8.2 业务规则派生Function

```java
/**
 * 客户等级计算Function
 */
@TISFunction(
    name = "deriveCustomerLevel",
    description = "计算客户等级",
    category = FunctionCategory.BUSINESS_RULE
)
public class CustomerLevelDeriver extends OntologyFunction {
    @Override
    public CustomerLevel apply(ObjectInstance customer) {
        BigDecimal totalAmount = customer.getProperty("totalOrderAmount");
        Integer orderCount = customer.getProperty("orderCount");
        Integer memberYears = customer.getProperty("memberYears");
        
        int score = calculateScore(totalAmount, orderCount, memberYears);
        
        if (score >= 1000) return CustomerLevel.DIAMOND;
        if (score >= 500) return CustomerLevel.PLATINUM;
        if (score >= 200) return CustomerLevel.GOLD;
        if (score >= 50) return CustomerLevel.SILVER;
        return CustomerLevel.NORMAL;
    }
}
```

### 8.3 数据质量评分Function

```java
/**
 * 数据完整性评分Function
 */
@TISFunction(
    name = "calculateCompleteness",
    description = "计算数据完整性得分",
    category = FunctionCategory.DATA_QUALITY
)
public class CompletenessCalculator extends OntologyFunction {
    @Override
    public Integer apply(ObjectInstance instance) {
        ObjectType objectType = instance.getObjectType();
        List<PropertyDef> properties = objectType.getProperties();
        
        int totalWeight = 0;
        int actualWeight = 0;
        
        for (PropertyDef prop : properties) {
            int weight = prop.isRequired() ? 2 : 1;
            totalWeight += weight;
            
            Object value = instance.getProperty(prop.getName());
            if (value != null && !value.toString().isEmpty()) {
                boolean valid = prop.getConstraints().stream()
                    .allMatch(constraint -> constraint.validate(value));
                if (valid) {
                    actualWeight += weight;
                }
            }
        }
        
        return (int) ((actualWeight * 100.0) / totalWeight);
    }
}
```

---

## 9. 具体Action示例

### 9.1 数据同步Action

```java
/**
 * 创建数据同步Action
 */
@TISAction(
    name = "createSyncAction",
    description = "基于本体映射创建数据同步任务",
    category = ActionCategory.DATA_INTEGRATION,
    requiredPermission = "ontology:sync:create"
)
public class CreateSyncAction extends OntologyAction {
    @Override
    public ActionResult execute(ActionContext context) {
        String sourceObjectType = context.getParam("sourceObjectType");
        String targetObjectType = context.getParam("targetObjectType");
        Map<String, String> fieldMapping = context.getParam("fieldMapping");
        
        // 1. 加载本体定义
        ObjectType source = ontologyStore.getObjectType(sourceObjectType);
        ObjectType target = ontologyStore.getObjectType(targetObjectType);
        
        // 2. 生成DataX配置
        DataXConfig dataxConfig = generateDataXConfig(source, target, fieldMapping);
        
        // 3. 创建并启动同步任务
        DataXJob job = dataxService.createJob(dataxConfig);
        job.start();
        
        // 4. 记录审计日志
        auditLog.record(context.getUser(), "CREATE_SYNC",
            Map.of("source", sourceObjectType, "target", targetObjectType, "jobId", job.getId()));
        
        return ActionResult.success()
            .withMessage("同步任务已创建")
            .withData("jobId", job.getId());
    }
}
```

### 9.2 数据修复Action

```java
/**
 * 修复约束违规Action
 */
@TISAction(
    name = "fixConstraintViolations",
    description = "批量修复违反约束的数据",
    category = ActionCategory.DATA_REPAIR,
    requiredPermission = "ontology:data:repair"
)
public class FixConstraintViolationsAction extends OntologyAction {
    @Override
    public ActionResult execute(ActionContext context) {
        String objectTypeName = context.getParam("objectType");
        String propertyName = context.getParam("property");
        RepairStrategy strategy = context.getParam("repairStrategy");
        
        // 1. 查询违规数据
        List<ViolationRecord> violations =
            dataQualityService.findViolations(objectTypeName, propertyName);
        
        // 2. 根据策略修复
        List<RepairResult> results = new ArrayList<>();
        for (ViolationRecord violation : violations) {
            RepairResult result = applyRepairStrategy(violation, strategy);
            results.add(result);
        }
        
        // 3. 执行修复
        int successCount = results.stream()
            .filter(r -> r.isSuccess())
            .mapToInt(r -> dataService.update(r.getRecordId(), r.getNewValue()))
            .sum();
        
        // 4. 记录审计
        auditLog.record(context.getUser(), "FIX_VIOLATIONS",
            Map.of("objectType", objectTypeName, "property", propertyName,
                   "total", violations.size(), "success", successCount));
        
        return ActionResult.success()
            .withMessage(String.format("已修复 %d/%d 条违规数据", successCount, violations.size()));
    }
}
```

### 9.3 Schema演进Action

```java
/**
 * 应用Schema变更Action
 */
@TISAction(
    name = "applySchemaChange",
    description = "将本体定义的变更应用到物理表",
    category = ActionCategory.SCHEMA_EVOLUTION,
    requiredPermission = "ontology:schema:evolve"
)
public class ApplySchemaChangeAction extends OntologyAction {
    @Override
    public ActionResult execute(ActionContext context) {
        String objectTypeName = context.getParam("objectType");
        ObjectType newDef = context.getParam("newDefinition");
        
        // 1. 对比新旧定义
        ObjectType oldDef = ontologyStore.getObjectType(objectTypeName);
        SchemaDiff diff = SchemaDiffer.compare(oldDef, newDef);
        
        // 2. 生成DDL
        List<String> ddlStatements = ddlGenerator.generate(diff);
        
        // 3. 执行DDL
        try {
            schemaService.executeDDL(ddlStatements);
            ontologyStore.updateObjectType(objectTypeName, newDef);
            dependencyService.notifySchemaChange(objectTypeName);
            
            return ActionResult.success()
                .withMessage("Schema变更已应用")
                .withData("ddl", ddlStatements);
        } catch (Exception e) {
            return ActionResult.failure()
                .withMessage("Schema变更失败：" + e.getMessage());
        }
    }
}
```

---

## 10. 总结

通过引入Palantir的Function和Action概念，TIS可以在现有的本体静态分析能力之上，构建动态业务执行能力：

### 核心价值

1. **Function让数据转换、业务规则、数据质量检查等计算逻辑成为本体的一部分**
   - 在ChatBI查询、数据展示、质量监控等场景中被自动调用
   - 声明式定义，易于维护和复用

2. **Action让数据集成、数据修复、Schema演进、血缘分析、数据治理等业务操作成为标准化的本体操作**
   - 可审计、权限可控
   - 触发式执行，响应业务事件

3. **构成完整的"静态建模 + 动态执行"闭环**
   - 从单纯的数据集成工具演进为基于本体语义的智能数据平台
   - 从被动响应到主动治理

### 实施路径

**第一阶段：核心Function/Action**
- 数据标准化Function
- 数据质量评分Function
- 数据同步Action
- 数据修复Action

**第二阶段：智能增强**
- 智能任务编排
- 实时质量监控
- 自动异常检测与自愈

**第三阶段：生态完善**
- ChatBI深度集成
- 数据治理全流程
- 成本优化自动化

通过分阶段实施，逐步将TIS打造为企业级智能数据运营平台。
