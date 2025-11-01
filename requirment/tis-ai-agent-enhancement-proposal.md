# TIS AI Agent 通用化架构改进方案

## 背景与问题

当前TIS的AI Agent功能存在以下局限性：
- `PlanGenerator.generateGenericPlan()` 方法只支持端到端数据管道构建
- 无法处理复杂的数据质量检测、实时分析等场景（如：Kafka源的实时刷单检测）
- 硬编码的功能模板无法覆盖千变万化的用户需求
- 与智能AI Agent的初衷相违背

## 核心设计理念

**从"场景枚举"转向"能力组合"** - 不再试图枚举所有可能的用户场景，而是将TIS的功能抽象为可组合的原子能力，通过动态组合来满足多样化需求。

## 架构改进方案

### 1. 原子能力抽象层

将TIS的功能分解为可组合的原子能力单元。这里需要区分三个层次的概念：

#### 1.1 概念层次说明

**三层抽象模型：**

```
PluginCapability（插件能力）     - 物理层：实际的插件实现
        ↓ 封装为
AtomicCapability（原子能力）     - 逻辑层：可组合的逻辑单元
        ↓ 组合成
CompositeCapability（复合能力）  - 业务层：完整的业务功能
```

#### 1.2 PluginCapability - 插件能力（物理层）

`PluginCapability` 是对 TIS 现有插件系统的直接封装，代表一个具体的插件所提供的能力：

```java
/**
 * 插件能力 - 对TIS插件的能力描述和封装
 * 这是与TIS现有插件系统的桥接层
 */
public class PluginCapability {

    // 插件标识
    private String pluginId;              // 如：mysql-reader-v2.1
    private String pluginType;            // 如：datasource-reader
    private String pluginVersion;         // 如：2.1.0

    // 插件的实际实现类
    private Class<? extends Plugin> pluginClass;
    private Plugin pluginInstance;        // 插件实例

    // 插件的能力描述
    private DataSchema inputSchema;       // 输入数据格式要求
    private DataSchema outputSchema;      // 输出数据格式
    private List<String> supportedOperations; // 支持的操作：read, write, transform等

    // 插件的配置要求
    private Map<String, PropertyDescriptor> requiredProperties;  // 必需的配置属性
    private Map<String, PropertyDescriptor> optionalProperties;  // 可选的配置属性

    // 插件的运行时要求
    private ResourceRequirements resourceRequirements;  // CPU、内存等资源需求
    private List<String> dependencies;    // 依赖的其他插件

    // 插件的质量属性
    private PerformanceMetrics performanceMetrics;  // 性能指标
    private ReliabilityLevel reliabilityLevel;      // 可靠性级别

    /**
     * 执行插件功能 - 这是与TIS插件系统的实际交互
     */
    public PluginExecutionResult executePlugin(PluginContext context) {
        // 1. 验证上下文是否满足插件要求
        validateContext(context);

        // 2. 准备插件配置
        Map<String, Object> config = prepareConfiguration(context);

        // 3. 调用插件实例
        return pluginInstance.execute(config);
    }

    /**
     * 检查插件是否可以处理特定的意图
     */
    public boolean canHandle(DataSourceType sourceType, OperationType opType) {
        // 检查插件是否支持该数据源类型和操作类型
        return supportedOperations.contains(opType.name()) &&
               isCompatibleWithDataSource(sourceType);
    }
}
```

#### 1.3 AtomicCapability - 原子能力（逻辑层）

`AtomicCapability` 是对插件能力的逻辑抽象，一个原子能力可能由一个或多个插件能力组合而成：

```java
/**
 * 原子能力 - 可组合的最小逻辑单元
 * 这是AI Agent进行推理和组合的基础单元
 */
public abstract class AtomicCapability {

    // 能力标识
    private String capabilityId;          // 唯一标识，如：data_extraction
    private String capabilityName;        // 能力名称，如：数据抽取
    private CapabilityCategory category;  // 能力分类

    // 能力的逻辑描述
    private String purpose;               // 能力用途描述
    private List<String> inputTypes;      // 可接受的输入类型
    private List<String> outputTypes;     // 产生的输出类型

    // 底层的插件能力（一个原子能力可能需要多个插件协同）
    protected List<PluginCapability> requiredPlugins;

    // 能力的前置和后置条件
    private List<Precondition> preconditions;   // 执行前必须满足的条件
    private List<Postcondition> postconditions; // 执行后保证的条件

    // 与其他能力的关系
    private List<String> incompatibleWith;  // 互斥的能力
    private List<String> enhancedBy;        // 可被增强的能力

    /**
     * 执行原子能力 - 这是逻辑层的执行
     */
    public abstract CapabilityResult execute(ExecutionContext context);

    /**
     * 检查是否可以与另一个原子能力组合
     */
    public boolean canCombineWith(AtomicCapability other) {
        // 检查是否互斥
        if (incompatibleWith.contains(other.getCapabilityId())) {
            return false;
        }

        // 检查输入输出是否匹配
        return isOutputCompatibleWithInput(this.outputTypes, other.inputTypes);
    }

    /**
     * 估算执行成本
     */
    public abstract ExecutionCost estimateCost(ExecutionContext context);
}

/**
 * 具体的原子能力实现示例 - 数据抽取能力
 */
public class DataExtractionCapability extends AtomicCapability {

    public DataExtractionCapability() {
        this.capabilityId = "data_extraction";
        this.capabilityName = "数据抽取";
        this.category = CapabilityCategory.DATA_SOURCE;

        // 可能使用多个插件来实现这个能力
        this.requiredPlugins = Arrays.asList(
            mysqlReaderPlugin,    // MySQL读取插件
            jdbcReaderPlugin,     // 通用JDBC读取插件
            csvReaderPlugin       // CSV读取插件
        );
    }

    @Override
    public CapabilityResult execute(ExecutionContext context) {
        // 1. 根据上下文选择合适的插件
        PluginCapability selectedPlugin = selectBestPlugin(context);

        // 2. 准备插件执行上下文
        PluginContext pluginContext = preparePluginContext(context);

        // 3. 执行插件
        PluginExecutionResult pluginResult = selectedPlugin.executePlugin(pluginContext);

        // 4. 将插件结果转换为能力结果
        return convertToCapabilityResult(pluginResult);
    }

    private PluginCapability selectBestPlugin(ExecutionContext context) {
        // 根据数据源类型、性能要求等选择最合适的插件
        DataSourceType sourceType = context.getDataSourceType();

        return requiredPlugins.stream()
            .filter(plugin -> plugin.canHandle(sourceType, OperationType.READ))
            .min(Comparator.comparing(plugin ->
                plugin.estimateCost(context)))
            .orElseThrow(() -> new NoSuitablePluginException());
    }
}

/**
 * 原子能力的分类
 */
public enum CapabilityCategory {
    DATA_SOURCE,      // 数据源操作
    DATA_TRANSFORM,   // 数据转换
    DATA_QUALITY,     // 数据质量
    DATA_SINK,        // 数据写入
    MONITORING,       // 监控告警
    ORCHESTRATION     // 编排调度
}
```

#### 1.4 CompositeCapability - 复合能力（业务层）

多个原子能力可以组合成复合能力，完成完整的业务功能：

```java
/**
 * 复合能力 - 由多个原子能力组合而成
 * 代表一个完整的业务功能
 */
public class CompositeCapability {

    private String compositeId;
    private String compositeName;

    // 组成这个复合能力的原子能力
    private List<AtomicCapability> atomicCapabilities;

    // 原子能力之间的编排关系
    private CapabilityGraph capabilityGraph;  // DAG图表示依赖关系

    /**
     * 执行复合能力
     */
    public CompositeResult execute(ExecutionContext context) {
        // 按照DAG图的拓扑顺序执行原子能力
        List<AtomicCapability> sortedCapabilities =
            capabilityGraph.topologicalSort(atomicCapabilities);

        CompositeResult result = new CompositeResult();
        Map<String, CapabilityResult> intermediateResults = new HashMap<>();

        for (AtomicCapability capability : sortedCapabilities) {
            // 准备该能力的输入（可能依赖前置能力的输出）
            ExecutionContext capContext = prepareContext(
                context, capability, intermediateResults
            );

            // 执行原子能力
            CapabilityResult capResult = capability.execute(capContext);
            intermediateResults.put(capability.getCapabilityId(), capResult);

            // 检查是否需要中断
            if (shouldAbort(capResult)) {
                result.setStatus(ExecutionStatus.ABORTED);
                break;
            }
        }

        return result;
    }
}
```

#### 1.5 能力组合器

基于细化后的概念，能力组合器的实现更加清晰：

```java
/**
 * 能力组合器 - 将原子能力组合成执行计划
 */
public class CapabilityComposer {

    @Autowired
    private PluginRegistry pluginRegistry;  // 所有可用的插件

    @Autowired
    private AtomicCapabilityFactory capabilityFactory;  // 原子能力工厂

    /**
     * 根据用户意图组合能力生成执行计划
     */
    public TaskPlan composePlan(UserIntent intent, AgentContext context) {

        // 1. 识别需要的原子能力
        List<AtomicCapability> requiredCapabilities =
            identifyRequiredCapabilities(intent);

        // 2. 检查能力的可组合性
        validateCapabilityCompatibility(requiredCapabilities);

        // 3. 构建能力依赖图
        CapabilityGraph graph = buildDependencyGraph(requiredCapabilities);

        // 4. 优化执行顺序（考虑并行执行的可能）
        ExecutionPlan optimizedPlan = optimizeExecutionPlan(graph);

        // 5. 为每个原子能力选择最佳的插件实现
        Map<AtomicCapability, PluginCapability> pluginMapping =
            selectPluginsForCapabilities(requiredCapabilities, context);

        // 6. 生成最终的任务计划
        return generateTaskPlan(optimizedPlan, pluginMapping);
    }

    /**
     * 识别完成用户意图所需的原子能力
     */
    private List<AtomicCapability> identifyRequiredCapabilities(UserIntent intent) {
        List<AtomicCapability> capabilities = new ArrayList<>();

        // 数据源能力
        if (intent.hasDataSource()) {
            capabilities.add(capabilityFactory.createDataExtractionCapability(
                intent.getDataSourceType()
            ));
        }

        // 数据质量能力
        if (intent.hasQualityRequirements()) {
            for (QualityRule rule : intent.getQualityRules()) {
                capabilities.add(capabilityFactory.createQualityCheckCapability(rule));
            }
        }

        // 数据转换能力
        if (intent.hasTransformations()) {
            capabilities.add(capabilityFactory.createTransformationCapability(
                intent.getTransformations()
            ));
        }

        // 数据写入能力
        if (intent.hasDataSink()) {
            capabilities.add(capabilityFactory.createDataSinkCapability(
                intent.getDataSinkType()
            ));
        }

        return capabilities;
    }

    /**
     * 为原子能力选择最佳的插件实现
     */
    private Map<AtomicCapability, PluginCapability> selectPluginsForCapabilities(
            List<AtomicCapability> capabilities,
            AgentContext context) {

        Map<AtomicCapability, PluginCapability> mapping = new HashMap<>();

        for (AtomicCapability capability : capabilities) {
            // 获取该能力可用的所有插件
            List<PluginCapability> availablePlugins =
                pluginRegistry.getPluginsForCapability(capability);

            // 选择最优的插件（考虑性能、成本、可靠性等）
            PluginCapability bestPlugin = selectOptimalPlugin(
                availablePlugins, context
            );

            mapping.put(capability, bestPlugin);
        }

        return mapping;
    }
}
```

#### 1.6 关系总结

```java
/**
 * 三者关系的完整示例
 */
public class ConceptRelationshipExample {

    public void demonstrateRelationship() {
        // 1. 插件能力 - 具体的MySQL读取插件
        PluginCapability mysqlPlugin = new PluginCapability();
        mysqlPlugin.setPluginId("mysql-reader-v2.1");
        mysqlPlugin.setPluginType("datasource-reader");
        mysqlPlugin.setSupportedOperations(Arrays.asList("read", "stream"));

        // 2. 原子能力 - 数据抽取（可能使用多个插件）
        AtomicCapability dataExtraction = new DataExtractionCapability();
        dataExtraction.setRequiredPlugins(Arrays.asList(
            mysqlPlugin,
            postgresPlugin,
            oraclePlugin
        ));

        // 3. 原子能力 - 数据质量检查
        AtomicCapability qualityCheck = new DataQualityCapability();

        // 4. 组合成复合能力 - 完整的ETL流程
        CompositeCapability etlProcess = new CompositeCapability();
        etlProcess.setAtomicCapabilities(Arrays.asList(
            dataExtraction,    // 先抽取
            qualityCheck,      // 再检查质量
            transformation,    // 转换
            dataSink          // 最后写入
        ));

        // 5. 执行时的选择过程
        // 用户说："从MySQL导入数据到ElasticSearch"
        // -> AI识别需要 dataExtraction + dataSink 两个原子能力
        // -> dataExtraction选择mysqlPlugin
        // -> dataSink选择elasticsearchPlugin
        // -> 组合成执行计划
    }
}
```

这样的三层设计清晰地分离了：
- **物理实现**（PluginCapability）
- **逻辑抽象**（AtomicCapability）
- **业务组合**（CompositeCapability）

使得系统既能利用现有的插件资源，又能进行智能的能力组合和推理。

### 2. 意图理解层

在生成执行计划之前，先深入理解用户的真实意图：

```java
public class IntentAnalyzer {

    public UserIntent analyze(String userRequest) {
        UserIntent intent = new UserIntent();

        // 识别数据源类型
        intent.setDataSources(extractDataSources(userRequest));

        // 识别数据处理需求
        intent.setProcessingType(identifyProcessingType(userRequest));

        // 提取数据质量规则
        if (hasQualityRequirement(userRequest)) {
            intent.setQualityRules(extractQualityRules(userRequest));
        }

        // 识别实时/批处理需求
        intent.setExecutionMode(identifyExecutionMode(userRequest));

        return intent;
    }

    private List<QualityRule> extractQualityRules(String request) {
        // 使用NLP技术提取规则
        // 例如："同一用户每秒下单超过3次" -> FrequencyRule(user_id, 3, 1s)
        // "同一设备关联超过10个不同账号" -> CardinalityRule(device_id, user_id, 10)
    }
}
```

### 3. 插件能力注册表

建立完整的系统能力清单，让AI Agent了解所有可用资源：

```java
public class PluginCapabilityRegistry {

    private Map<String, PluginCapability> registry = new HashMap<>();

    @PostConstruct
    public void initializeRegistry() {
        // 自动扫描所有可用插件
        scanAndRegisterPlugins();

        // 注册插件的输入输出模式
        registerIOSchemas();

        // 注册插件支持的操作类型
        registerSupportedOperations();
    }

    public List<PluginCapability> findCapabilitiesForIntent(UserIntent intent) {
        return registry.values().stream()
            .filter(cap -> cap.matches(intent))
            .sorted(Comparator.comparing(cap -> cap.getMatchScore(intent)))
            .collect(Collectors.toList());
    }

    public boolean canHandle(UserIntent intent) {
        // 判断系统是否具备处理该意图的能力
        return !findCapabilitiesForIntent(intent).isEmpty();
    }
}
```

### 4. 动态模板系统

使用可配置、可扩展的模板系统替代硬编码：

```java
public class TemplatePlanGenerator {

    private TemplateRepository templateRepo;
    private RuleEngine ruleEngine;

    public TaskPlan generatePlan(UserIntent intent, List<PluginCapability> capabilities) {
        // 1. 查找基础模板
        PlanTemplate baseTemplate = templateRepo.findBestMatch(intent);

        // 2. 创建计划实例
        TaskPlan plan = new TaskPlan();

        // 3. 动态添加数据源配置步骤
        plan.addStep(createDataSourceStep(intent.getDataSources(), capabilities));

        // 4. 添加数据质量检测（如需要）
        if (intent.hasQualityRequirements()) {
            plan.addStep(createQualityCheckStep(intent.getQualityRules()));
        }

        // 5. 添加数据转换步骤
        if (intent.hasTransformation()) {
            plan.addStep(createTransformationStep(intent.getTransformations()));
        }

        // 6. 添加目标端配置
        plan.addStep(createSinkStep(intent.getSink(), capabilities));

        // 7. 优化执行顺序
        return optimizePlan(plan);
    }

    private TaskStep createQualityCheckStep(List<QualityRule> rules) {
        TaskStep step = new TaskStep("data_quality_check");

        for (QualityRule rule : rules) {
            if (rule instanceof FrequencyRule) {
                // 添加频率检测组件
                step.addComponent(new FrequencyCheckComponent(rule));
            } else if (rule instanceof CardinalityRule) {
                // 添加基数检测组件
                step.addComponent(new CardinalityCheckComponent(rule));
            }
        }

        return step;
    }
}
```

#### 4.1 PlanTemplate 详细设计

本节深入阐述 PlanTemplate 的设计理念、内部结构、工作机制及具体实现。

##### 4.1.1 PlanTemplate 的定义和作用

**定义**

`PlanTemplate` 是 TIS AI Agent 动态模板系统的核心抽象，它封装了一类数据处理场景的通用执行模式，包括：
- 数据流的拓扑结构（Source → Transform → Sink）
- 每个阶段可配置的参数模板
- 步骤间的依赖关系和执行顺序
- 条件分支和动态决策逻辑

**作用**

1. **知识复用**：将成熟的数据处理模式固化为可复用的模板
2. **快速实例化**：根据用户意图快速生成具体的执行计划
3. **最佳实践**：内置业界最佳实践，降低错误配置风险
4. **扩展基础**：为复杂场景提供可组合的基础构建块

##### 4.1.2 内部数据结构设计

```java
/**
 * 计划模板核心类
 * 使用建造者模式支持灵活配置
 */
public class PlanTemplate {

    // 模板基础信息
    private String templateId;           // 唯一标识
    private String name;                 // 模板名称
    private String description;          // 模板描述
    private TemplateCategory category;   // 模板分类（ETL、实时处理、数据质量等）
    private Set<String> requiredCapabilities; // 所需系统能力

    // 模板结构定义
    private List<StageTemplate> stages;  // 阶段模板列表
    private Map<String, String> metadata; // 元数据（版本、作者等）

    // 继承和组合
    private String parentTemplateId;     // 父模板ID（支持继承）
    private List<String> mixinTemplateIds; // 混入模板ID列表

    // 参数定义
    private Map<String, ParameterDefinition> parameters; // 可配置参数
    private Map<String, Object> defaultValues;           // 默认值

    // 验证规则
    private List<ValidationRule> validationRules;

    // 条件逻辑
    private Map<String, ConditionalBranch> conditionalBranches;

    /**
     * 阶段模板：定义流程中的一个处理阶段
     */
    public static class StageTemplate {
        private String stageId;
        private String stageName;
        private StageType type; // SOURCE, TRANSFORM, SINK, QUALITY_CHECK
        private int order;      // 执行顺序

        // 该阶段需要的组件
        private List<ComponentTemplate> components;

        // 该阶段的输入输出模式
        private DataSchema inputSchema;
        private DataSchema outputSchema;

        // 前置依赖
        private List<String> dependencies;

        // 条件执行
        private String condition; // SpEL表达式，如：#{intent.hasQualityRequirements()}
    }

    /**
     * 组件模板：阶段内的具体处理单元
     */
    public static class ComponentTemplate {
        private String componentId;
        private String componentType; // 对应TIS插件类型
        private Map<String, ParameterBinding> parameterBindings; // 参数绑定关系
        private boolean optional; // 是否可选
        private String condition; // 条件渲染表达式
    }

    /**
     * 参数定义：描述模板的可配置参数
     */
    public static class ParameterDefinition {
        private String name;
        private Class<?> type;
        private String description;
        private boolean required;
        private Object defaultValue;
        private List<String> allowedValues; // 枚举值
        private String validationRegex;     // 验证正则
        private ParameterSource source;     // 参数来源
    }

    /**
     * 参数绑定：定义组件参数如何从上下文获取值
     */
    public static class ParameterBinding {
        private String targetField;    // 目标字段
        private BindingType bindingType; // 绑定类型
        private String expression;     // 绑定表达式

        public enum BindingType {
            LITERAL,       // 字面值：直接使用配置值
            VARIABLE,      // 变量：从上下文变量获取
            EXPRESSION,    // 表达式：SpEL表达式计算
            REFERENCE,     // 引用：引用其他组件的输出
            PROMPT         // 提示：需要用户交互提供
        }
    }

    /**
     * 条件分支：支持根据条件选择不同的执行路径
     */
    public static class ConditionalBranch {
        private String branchId;
        private String condition;      // 条件表达式
        private List<StageTemplate> trueStages;  // 条件为真的执行路径
        private List<StageTemplate> falseStages; // 条件为假的执行路径
    }
}

/**
 * 参数来源枚举
 */
public enum ParameterSource {
    USER_INTENT,    // 从用户意图提取
    CONTEXT,        // 从执行上下文获取
    SYSTEM,         // 系统自动推断
    USER_INPUT      // 需要用户交互输入
}

/**
 * 模板分类
 */
public enum TemplateCategory {
    BATCH_ETL,           // 批量ETL
    REALTIME_STREAM,     // 实时流处理
    DATA_QUALITY,        // 数据质量
    DATA_INTEGRATION,    // 数据集成
    DATA_MIGRATION,      // 数据迁移
    CUSTOM               // 自定义
}

/**
 * 阶段类型
 */
public enum StageType {
    SOURCE,          // 数据源
    TRANSFORM,       // 数据转换
    QUALITY_CHECK,   // 质量检测
    AGGREGATION,     // 聚合分析
    SINK,            // 数据目标
    NOTIFICATION     // 通知告警
}
```

##### 4.1.3 模板的继承和组合机制

**继承机制**

模板支持单继承，子模板可以：
- 继承父模板的所有阶段和参数定义
- 覆盖（override）特定阶段的配置
- 添加新的阶段和参数

```java
/**
 * 模板继承解析器
 */
public class TemplateInheritanceResolver {

    private TemplateRepository repository;

    /**
     * 解析模板继承链，生成完整的模板定义
     */
    public PlanTemplate resolve(PlanTemplate template) {
        if (template.getParentTemplateId() == null) {
            return template; // 无继承
        }

        // 递归解析父模板
        PlanTemplate parent = repository.getTemplate(template.getParentTemplateId());
        PlanTemplate resolvedParent = resolve(parent);

        // 合并父子模板
        return mergeTemplates(resolvedParent, template);
    }

    private PlanTemplate mergeTemplates(PlanTemplate parent, PlanTemplate child) {
        PlanTemplate merged = parent.clone();

        // 1. 合并阶段：子模板的阶段优先
        Map<String, StageTemplate> stageMap = new HashMap<>();
        parent.getStages().forEach(s -> stageMap.put(s.getStageId(), s));
        child.getStages().forEach(s -> stageMap.put(s.getStageId(), s));
        merged.setStages(new ArrayList<>(stageMap.values()));

        // 2. 合并参数定义：子模板参数覆盖父模板
        merged.getParameters().putAll(child.getParameters());

        // 3. 合并默认值
        merged.getDefaultValues().putAll(child.getDefaultValues());

        // 4. 合并验证规则
        merged.getValidationRules().addAll(child.getValidationRules());

        // 5. 更新元数据
        merged.setTemplateId(child.getTemplateId());
        merged.setName(child.getName());
        merged.setDescription(child.getDescription());

        return merged;
    }
}
```

**组合机制（Mixin）**

通过 Mixin 支持多个模板片段的组合：

```java
/**
 * 模板组合器
 */
public class TemplateMixinComposer {

    /**
     * 将多个 Mixin 模板混入到基础模板中
     */
    public PlanTemplate compose(PlanTemplate base, List<PlanTemplate> mixins) {
        PlanTemplate composed = base.clone();

        for (PlanTemplate mixin : mixins) {
            // 添加 mixin 的阶段
            composed.getStages().addAll(mixin.getStages());

            // 合并参数定义
            composed.getParameters().putAll(mixin.getParameters());

            // 合并能力要求
            composed.getRequiredCapabilities().addAll(mixin.getRequiredCapabilities());

            // 合并条件分支
            composed.getConditionalBranches().putAll(mixin.getConditionalBranches());
        }

        // 重新排序阶段
        sortStagesByOrder(composed);

        return composed;
    }

    private void sortStagesByOrder(PlanTemplate template) {
        List<StageTemplate> stages = template.getStages();
        stages.sort(Comparator.comparingInt(StageTemplate::getOrder));
    }
}
```

**实际应用示例：组合数据质量检测能力**

```java
// 基础 ETL 模板
PlanTemplate baseETL = templateRepo.getTemplate("base_etl");

// 数据质量检测 Mixin
PlanTemplate qualityMixin = templateRepo.getTemplate("mixin_quality_check");

// 实时告警 Mixin
PlanTemplate alertMixin = templateRepo.getTemplate("mixin_alert");

// 组合成增强的 ETL 模板
PlanTemplate enhancedETL = mixinComposer.compose(
    baseETL,
    Arrays.asList(qualityMixin, alertMixin)
);
```

##### 4.1.4 模板变量和占位符系统

**变量系统**

模板使用变量和占位符实现动态配置，支持以下变量来源：

1. **用户意图变量**：从 UserIntent 提取
2. **上下文变量**：从 ExecutionContext 获取
3. **系统变量**：系统自动提供
4. **临时变量**：执行过程中计算得出

```java
/**
 * 模板变量解析器
 */
public class TemplateVariableResolver {

    private SpelExpressionParser parser = new SpelExpressionParser();
    private StandardEvaluationContext evalContext;

    /**
     * 解析模板中的所有变量占位符
     */
    public Map<String, Object> resolveVariables(
            PlanTemplate template,
            UserIntent intent,
            AgentContext context) {

        Map<String, Object> variables = new HashMap<>();

        // 1. 设置上下文根对象
        evalContext.setRootObject(new TemplateContext(intent, context));

        // 2. 注册标准变量
        variables.put("intent", intent);
        variables.put("context", context);
        variables.put("system", getSystemVariables());

        // 3. 解析参数定义中的变量
        for (ParameterDefinition param : template.getParameters().values()) {
            Object value = resolveParameter(param, intent, context);
            variables.put(param.getName(), value);
        }

        // 4. 解析自定义变量
        for (Map.Entry<String, String> entry : template.getMetadata().entrySet()) {
            if (entry.getKey().startsWith("var.")) {
                String varName = entry.getKey().substring(4);
                Object value = evaluateExpression(entry.getValue());
                variables.put(varName, value);
            }
        }

        return variables;
    }

    /**
     * 解析单个参数
     */
    private Object resolveParameter(
            ParameterDefinition param,
            UserIntent intent,
            AgentContext context) {

        switch (param.getSource()) {
            case USER_INTENT:
                return extractFromIntent(param, intent);

            case CONTEXT:
                return extractFromContext(param, context);

            case SYSTEM:
                return inferFromSystem(param);

            case USER_INPUT:
                // 标记为需要用户输入
                return new RequiredUserInput(param);

            default:
                return param.getDefaultValue();
        }
    }

    /**
     * 从用户意图提取参数值
     */
    private Object extractFromIntent(ParameterDefinition param, UserIntent intent) {
        // 使用反射或表达式从 intent 中提取
        Expression expr = parser.parseExpression(
            String.format("intent.%s", param.getName())
        );
        return expr.getValue(evalContext);
    }

    /**
     * 计算表达式
     */
    private Object evaluateExpression(String expression) {
        try {
            Expression expr = parser.parseExpression(expression);
            return expr.getValue(evalContext);
        } catch (Exception e) {
            throw new TemplateExpressionException(
                "表达式计算失败: " + expression, e
            );
        }
    }

    /**
     * 获取系统变量
     */
    private Map<String, Object> getSystemVariables() {
        Map<String, Object> systemVars = new HashMap<>();
        systemVars.put("timestamp", System.currentTimeMillis());
        systemVars.put("date", LocalDate.now());
        systemVars.put("user", getCurrentUser());
        systemVars.put("workspace", getCurrentWorkspace());
        return systemVars;
    }
}

/**
 * 模板上下文：用于 SpEL 表达式计算
 */
public class TemplateContext {
    private final UserIntent intent;
    private final AgentContext context;
    private final Map<String, Object> variables;

    public TemplateContext(UserIntent intent, AgentContext context) {
        this.intent = intent;
        this.context = context;
        this.variables = new HashMap<>();
    }

    // Getter 方法供 SpEL 访问
    public UserIntent getIntent() { return intent; }
    public AgentContext getContext() { return context; }
    public Map<String, Object> getVariables() { return variables; }

    // 便捷方法
    public boolean hasQualityRequirements() {
        return intent.hasQualityRequirements();
    }

    public boolean isRealtimeMode() {
        return intent.getExecutionMode() == ExecutionMode.REALTIME;
    }
}
```

**占位符语法**

模板中支持以下占位符格式：

```
${variable}              - 简单变量替换
#{expression}            - SpEL 表达式计算
@{reference}             - 引用其他组件的输出
${variable:defaultValue} - 带默认值的变量
```

**使用示例**

```java
// 组件参数配置示例
ComponentTemplate kafkaSource = new ComponentTemplate();
kafkaSource.setComponentType("kafka-reader");

Map<String, ParameterBinding> bindings = new HashMap<>();

// 直接从意图获取
bindings.put("topic", new ParameterBinding(
    "topic",
    BindingType.VARIABLE,
    "${intent.sourceTopic}"
));

// 使用表达式计算
bindings.put("groupId", new ParameterBinding(
    "groupId",
    BindingType.EXPRESSION,
    "#{intent.pipelineName + '_consumer_' + system.timestamp}"
));

// 带默认值
bindings.put("maxPollRecords", new ParameterBinding(
    "maxPollRecords",
    BindingType.LITERAL,
    "${intent.batchSize:500}"
));

kafkaSource.setParameterBindings(bindings);
```

##### 4.1.5 条件逻辑和动态步骤

**条件执行**

模板支持基于条件动态启用或禁用某些阶段：

```java
/**
 * 条件执行引擎
 */
public class ConditionalExecutionEngine {

    private TemplateVariableResolver variableResolver;
    private SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 根据条件筛选需要执行的阶段
     */
    public List<StageTemplate> filterStages(
            List<StageTemplate> stages,
            Map<String, Object> variables) {

        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        variables.forEach(evalContext::setVariable);

        return stages.stream()
            .filter(stage -> shouldExecuteStage(stage, evalContext))
            .collect(Collectors.toList());
    }

    /**
     * 判断阶段是否应该执行
     */
    private boolean shouldExecuteStage(
            StageTemplate stage,
            EvaluationContext context) {

        String condition = stage.getCondition();
        if (condition == null || condition.isEmpty()) {
            return true; // 无条件，总是执行
        }

        try {
            Expression expr = parser.parseExpression(condition);
            Boolean result = expr.getValue(context, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            logger.warn("条件表达式执行失败: {}", condition, e);
            return false; // 表达式错误，跳过该阶段
        }
    }

    /**
     * 处理条件分支
     */
    public List<StageTemplate> resolveBranch(
            ConditionalBranch branch,
            Map<String, Object> variables) {

        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        variables.forEach(evalContext::setVariable);

        try {
            Expression expr = parser.parseExpression(branch.getCondition());
            Boolean result = expr.getValue(evalContext, Boolean.class);

            if (Boolean.TRUE.equals(result)) {
                return branch.getTrueStages();
            } else {
                return branch.getFalseStages();
            }
        } catch (Exception e) {
            logger.error("条件分支计算失败: {}", branch.getCondition(), e);
            return branch.getFalseStages(); // 默认走 false 分支
        }
    }
}
```

**动态步骤生成**

根据运行时信息动态生成处理步骤：

```java
/**
 * 动态步骤生成器
 */
public class DynamicStepGenerator {

    /**
     * 根据质量规则动态生成检测步骤
     */
    public List<StageTemplate> generateQualityCheckStages(
            List<QualityRule> rules) {

        List<StageTemplate> stages = new ArrayList<>();

        for (int i = 0; i < rules.size(); i++) {
            QualityRule rule = rules.get(i);

            StageTemplate stage = new StageTemplate();
            stage.setStageId("quality_check_" + i);
            stage.setStageName(rule.getName());
            stage.setType(StageType.QUALITY_CHECK);
            stage.setOrder(100 + i); // 质量检测阶段在转换后执行

            // 根据规则类型选择对应的组件
            ComponentTemplate component = selectComponentForRule(rule);
            stage.setComponents(Collections.singletonList(component));

            stages.add(stage);
        }

        return stages;
    }

    /**
     * 根据规则类型选择处理组件
     */
    private ComponentTemplate selectComponentForRule(QualityRule rule) {
        ComponentTemplate component = new ComponentTemplate();

        if (rule instanceof FrequencyRule) {
            component.setComponentType("frequency-checker");
            FrequencyRule freqRule = (FrequencyRule) rule;

            Map<String, ParameterBinding> bindings = new HashMap<>();
            bindings.put("groupBy", literalBinding(freqRule.getGroupBy()));
            bindings.put("window", literalBinding(freqRule.getWindow()));
            bindings.put("threshold", literalBinding(freqRule.getThreshold()));
            component.setParameterBindings(bindings);

        } else if (rule instanceof CardinalityRule) {
            component.setComponentType("cardinality-checker");
            CardinalityRule cardRule = (CardinalityRule) rule;

            Map<String, ParameterBinding> bindings = new HashMap<>();
            bindings.put("groupBy", literalBinding(cardRule.getGroupBy()));
            bindings.put("countDistinct", literalBinding(cardRule.getCountDistinct()));
            bindings.put("threshold", literalBinding(cardRule.getThreshold()));
            component.setParameterBindings(bindings);

        } else {
            throw new UnsupportedRuleException("不支持的规则类型: " + rule.getClass());
        }

        return component;
    }

    private ParameterBinding literalBinding(Object value) {
        return new ParameterBinding(null, BindingType.LITERAL, String.valueOf(value));
    }
}
```

##### 4.1.6 具体的实现代码示例

**完整的模板实例化流程**

```java
/**
 * 模板实例化器：将模板转换为可执行的任务计划
 */
@Component
public class TemplateInstantiator {

    @Autowired
    private TemplateRepository templateRepo;

    @Autowired
    private TemplateInheritanceResolver inheritanceResolver;

    @Autowired
    private TemplateMixinComposer mixinComposer;

    @Autowired
    private TemplateVariableResolver variableResolver;

    @Autowired
    private ConditionalExecutionEngine conditionalEngine;

    @Autowired
    private DynamicStepGenerator dynamicStepGenerator;

    /**
     * 实例化模板为任务计划
     */
    public TaskPlan instantiate(
            String templateId,
            UserIntent intent,
            AgentContext context) {

        // 1. 加载并解析模板
        PlanTemplate template = loadTemplate(templateId);

        // 2. 解析变量
        Map<String, Object> variables = variableResolver.resolveVariables(
            template, intent, context
        );

        // 3. 条件筛选阶段
        List<StageTemplate> stages = conditionalEngine.filterStages(
            template.getStages(), variables
        );

        // 4. 动态添加阶段
        if (intent.hasQualityRequirements()) {
            List<StageTemplate> qualityStages = dynamicStepGenerator
                .generateQualityCheckStages(intent.getQualityRules());
            stages.addAll(qualityStages);
        }

        // 5. 排序阶段
        stages.sort(Comparator.comparingInt(StageTemplate::getOrder));

        // 6. 实例化为任务计划
        TaskPlan plan = new TaskPlan();
        plan.setPlanId(generatePlanId());
        plan.setName(template.getName());
        plan.setTemplateId(templateId);

        for (StageTemplate stageTemplate : stages) {
            TaskStep step = instantiateStage(stageTemplate, variables);
            plan.addStep(step);
        }

        // 7. 设置上下文
        plan.setContext(context);
        plan.setIntent(intent);
        plan.setVariables(variables);

        return plan;
    }

    /**
     * 加载并解析模板（包括继承和混入）
     */
    private PlanTemplate loadTemplate(String templateId) {
        PlanTemplate template = templateRepo.getTemplate(templateId);

        // 解析继承
        template = inheritanceResolver.resolve(template);

        // 应用混入
        if (!template.getMixinTemplateIds().isEmpty()) {
            List<PlanTemplate> mixins = template.getMixinTemplateIds().stream()
                .map(templateRepo::getTemplate)
                .collect(Collectors.toList());
            template = mixinComposer.compose(template, mixins);
        }

        return template;
    }

    /**
     * 实例化单个阶段为任务步骤
     */
    private TaskStep instantiateStage(
            StageTemplate stageTemplate,
            Map<String, Object> variables) {

        TaskStep step = new TaskStep(stageTemplate.getStageId());
        step.setName(stageTemplate.getStageName());
        step.setType(stageTemplate.getType().name());

        // 实例化组件
        for (ComponentTemplate compTemplate : stageTemplate.getComponents()) {
            if (shouldIncludeComponent(compTemplate, variables)) {
                TaskComponent component = instantiateComponent(compTemplate, variables);
                step.addComponent(component);
            }
        }

        // 设置依赖
        step.setDependencies(stageTemplate.getDependencies());

        return step;
    }

    /**
     * 判断组件是否应该包含
     */
    private boolean shouldIncludeComponent(
            ComponentTemplate template,
            Map<String, Object> variables) {

        if (template.isOptional() && template.getCondition() != null) {
            return conditionalEngine.evaluateCondition(
                template.getCondition(), variables
            );
        }
        return true;
    }

    /**
     * 实例化组件
     */
    private TaskComponent instantiateComponent(
            ComponentTemplate template,
            Map<String, Object> variables) {

        TaskComponent component = new TaskComponent();
        component.setType(template.getComponentType());

        // 绑定参数
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, ParameterBinding> entry :
                template.getParameterBindings().entrySet()) {

            String paramName = entry.getKey();
            ParameterBinding binding = entry.getValue();
            Object value = resolveBinding(binding, variables);
            params.put(paramName, value);
        }
        component.setParameters(params);

        return component;
    }

    /**
     * 解析参数绑定
     */
    private Object resolveBinding(
            ParameterBinding binding,
            Map<String, Object> variables) {

        switch (binding.getBindingType()) {
            case LITERAL:
                return parseLiteral(binding.getExpression());

            case VARIABLE:
                return variables.get(extractVariableName(binding.getExpression()));

            case EXPRESSION:
                return variableResolver.evaluateExpression(
                    binding.getExpression(), variables
                );

            case REFERENCE:
                return resolveReference(binding.getExpression(), variables);

            case PROMPT:
                return new RequiredUserInput(binding);

            default:
                throw new IllegalArgumentException(
                    "不支持的绑定类型: " + binding.getBindingType()
                );
        }
    }

    private String extractVariableName(String expression) {
        // ${varName} -> varName
        return expression.replaceAll("\\$\\{(.*?)\\}", "$1");
    }

    private Object parseLiteral(String expression) {
        // 尝试解析为数字、布尔等类型
        if (expression.matches("\\d+")) {
            return Integer.parseInt(expression);
        } else if (expression.matches("true|false")) {
            return Boolean.parseBoolean(expression);
        } else {
            return expression;
        }
    }
}
```

##### 4.1.7 实际模板示例

以下是几个具体的模板定义示例：

**示例 1：基础 ETL 模板**

```java
/**
 * 基础 ETL 模板工厂
 */
public class BasicETLTemplateFactory {

    public PlanTemplate createTemplate() {
        PlanTemplate template = new PlanTemplate();
        template.setTemplateId("base_etl");
        template.setName("基础ETL数据管道");
        template.setDescription("标准的数据抽取-转换-加载流程");
        template.setCategory(TemplateCategory.BATCH_ETL);

        // 必需能力
        template.setRequiredCapabilities(Set.of(
            "datasource_read",
            "datasource_write",
            "data_transform"
        ));

        // 参数定义
        Map<String, ParameterDefinition> params = new HashMap<>();

        params.put("sourceType", ParameterDefinition.builder()
            .name("sourceType")
            .type(String.class)
            .description("数据源类型")
            .required(true)
            .source(ParameterSource.USER_INTENT)
            .build());

        params.put("sinkType", ParameterDefinition.builder()
            .name("sinkType")
            .type(String.class)
            .description("目标数据源类型")
            .required(true)
            .source(ParameterSource.USER_INTENT)
            .build());

        params.put("batchSize", ParameterDefinition.builder()
            .name("batchSize")
            .type(Integer.class)
            .description("批处理大小")
            .required(false)
            .defaultValue(1000)
            .source(ParameterSource.SYSTEM)
            .build());

        template.setParameters(params);

        // 阶段定义
        List<StageTemplate> stages = new ArrayList<>();

        // 阶段 1：数据源配置
        StageTemplate sourceStage = new StageTemplate();
        sourceStage.setStageId("source");
        sourceStage.setStageName("配置数据源");
        sourceStage.setType(StageType.SOURCE);
        sourceStage.setOrder(10);

        ComponentTemplate sourceComponent = new ComponentTemplate();
        sourceComponent.setComponentType("${sourceType}-reader");
        Map<String, ParameterBinding> sourceBindings = new HashMap<>();
        sourceBindings.put("jdbcUrl", new ParameterBinding(
            "jdbcUrl", BindingType.VARIABLE, "${intent.sourceJdbcUrl}"
        ));
        sourceBindings.put("table", new ParameterBinding(
            "table", BindingType.VARIABLE, "${intent.sourceTable}"
        ));
        sourceComponent.setParameterBindings(sourceBindings);
        sourceStage.setComponents(Collections.singletonList(sourceComponent));

        stages.add(sourceStage);

        // 阶段 2：数据转换（可选）
        StageTemplate transformStage = new StageTemplate();
        transformStage.setStageId("transform");
        transformStage.setStageName("数据转换");
        transformStage.setType(StageType.TRANSFORM);
        transformStage.setOrder(20);
        transformStage.setCondition("#{intent.hasTransformations()}");
        transformStage.setDependencies(Collections.singletonList("source"));

        ComponentTemplate transformComponent = new ComponentTemplate();
        transformComponent.setComponentType("field-mapper");
        Map<String, ParameterBinding> transformBindings = new HashMap<>();
        transformBindings.put("mappings", new ParameterBinding(
            "mappings", BindingType.VARIABLE, "${intent.fieldMappings}"
        ));
        transformComponent.setParameterBindings(transformBindings);
        transformStage.setComponents(Collections.singletonList(transformComponent));

        stages.add(transformStage);

        // 阶段 3：数据写入
        StageTemplate sinkStage = new StageTemplate();
        sinkStage.setStageId("sink");
        sinkStage.setStageName("写入目标数据源");
        sinkStage.setType(StageType.SINK);
        sinkStage.setOrder(30);
        sinkStage.setDependencies(Arrays.asList("source", "transform"));

        ComponentTemplate sinkComponent = new ComponentTemplate();
        sinkComponent.setComponentType("${sinkType}-writer");
        Map<String, ParameterBinding> sinkBindings = new HashMap<>();
        sinkBindings.put("jdbcUrl", new ParameterBinding(
            "jdbcUrl", BindingType.VARIABLE, "${intent.sinkJdbcUrl}"
        ));
        sinkBindings.put("table", new ParameterBinding(
            "table", BindingType.VARIABLE, "${intent.sinkTable}"
        ));
        sinkBindings.put("batchSize", new ParameterBinding(
            "batchSize", BindingType.VARIABLE, "${batchSize}"
        ));
        sinkComponent.setParameterBindings(sinkBindings);
        sinkStage.setComponents(Collections.singletonList(sinkComponent));

        stages.add(sinkStage);

        template.setStages(stages);

        return template;
    }
}
```

**示例 2：实时流处理模板**

```java
/**
 * 实时流处理模板工厂
 */
public class RealtimeStreamTemplateFactory {

    public PlanTemplate createTemplate() {
        PlanTemplate template = new PlanTemplate();
        template.setTemplateId("realtime_stream");
        template.setName("实时流数据处理");
        template.setDescription("基于Flink-CDC的实时数据流处理");
        template.setCategory(TemplateCategory.REALTIME_STREAM);

        // 继承基础 ETL 模板
        template.setParentTemplateId("base_etl");

        // 额外的必需能力
        template.setRequiredCapabilities(Set.of(
            "stream_processing",
            "flink_cdc"
        ));

        // 参数定义
        Map<String, ParameterDefinition> params = new HashMap<>();

        params.put("checkpointInterval", ParameterDefinition.builder()
            .name("checkpointInterval")
            .type(Integer.class)
            .description("检查点间隔（毫秒）")
            .required(false)
            .defaultValue(60000)
            .source(ParameterSource.SYSTEM)
            .build());

        params.put("parallelism", ParameterDefinition.builder()
            .name("parallelism")
            .type(Integer.class)
            .description("并行度")
            .required(false)
            .defaultValue(4)
            .source(ParameterSource.SYSTEM)
            .build());

        template.setParameters(params);

        // 阶段定义
        List<StageTemplate> stages = new ArrayList<>();

        // 覆盖父模板的 source 阶段，使用 CDC 源
        StageTemplate cdcSourceStage = new StageTemplate();
        cdcSourceStage.setStageId("source");
        cdcSourceStage.setStageName("CDC数据源");
        cdcSourceStage.setType(StageType.SOURCE);
        cdcSourceStage.setOrder(10);

        ComponentTemplate cdcComponent = new ComponentTemplate();
        cdcComponent.setComponentType("flink-cdc-${sourceType}");
        Map<String, ParameterBinding> cdcBindings = new HashMap<>();
        cdcBindings.put("hostname", new ParameterBinding(
            "hostname", BindingType.VARIABLE, "${intent.sourceHost}"
        ));
        cdcBindings.put("port", new ParameterBinding(
            "port", BindingType.VARIABLE, "${intent.sourcePort}"
        ));
        cdcBindings.put("database", new ParameterBinding(
            "database", BindingType.VARIABLE, "${intent.sourceDatabase}"
        ));
        cdcBindings.put("tables", new ParameterBinding(
            "tables", BindingType.VARIABLE, "${intent.sourceTables}"
        ));
        cdcComponent.setParameterBindings(cdcBindings);
        cdcSourceStage.setComponents(Collections.singletonList(cdcComponent));

        stages.add(cdcSourceStage);

        // 新增：流处理配置阶段
        StageTemplate streamConfigStage = new StageTemplate();
        streamConfigStage.setStageId("stream_config");
        streamConfigStage.setStageName("流处理配置");
        streamConfigStage.setType(StageType.TRANSFORM);
        streamConfigStage.setOrder(15);

        ComponentTemplate streamConfigComponent = new ComponentTemplate();
        streamConfigComponent.setComponentType("flink-stream-config");
        Map<String, ParameterBinding> streamBindings = new HashMap<>();
        streamBindings.put("checkpointInterval", new ParameterBinding(
            "checkpointInterval", BindingType.VARIABLE, "${checkpointInterval}"
        ));
        streamBindings.put("parallelism", new ParameterBinding(
            "parallelism", BindingType.VARIABLE, "${parallelism}"
        ));
        streamBindings.put("restartStrategy", new ParameterBinding(
            "restartStrategy", BindingType.LITERAL, "fixed-delay"
        ));
        streamConfigComponent.setParameterBindings(streamBindings);
        streamConfigStage.setComponents(Collections.singletonList(streamConfigComponent));

        stages.add(streamConfigStage);

        template.setStages(stages);

        return template;
    }
}
```

**示例 3：数据质量检测模板（Mixin）**

```java
/**
 * 数据质量检测 Mixin 模板工厂
 */
public class DataQualityMixinFactory {

    public PlanTemplate createTemplate() {
        PlanTemplate template = new PlanTemplate();
        template.setTemplateId("mixin_quality_check");
        template.setName("数据质量检测增强");
        template.setDescription("为流程添加数据质量检测能力");
        template.setCategory(TemplateCategory.DATA_QUALITY);

        // 必需能力
        template.setRequiredCapabilities(Set.of(
            "quality_check",
            "rule_engine"
        ));

        // 参数定义
        Map<String, ParameterDefinition> params = new HashMap<>();

        params.put("qualityThreshold", ParameterDefinition.builder()
            .name("qualityThreshold")
            .type(Double.class)
            .description("质量阈值（0-1）")
            .required(false)
            .defaultValue(0.95)
            .source(ParameterSource.USER_INTENT)
            .build());

        params.put("onQualityFail", ParameterDefinition.builder()
            .name("onQualityFail")
            .type(String.class)
            .description("质量检测失败时的操作")
            .required(false)
            .defaultValue("alert")
            .allowedValues(Arrays.asList("alert", "reject", "mark"))
            .source(ParameterSource.USER_INTENT)
            .build());

        template.setParameters(params);

        // 阶段定义
        List<StageTemplate> stages = new ArrayList<>();

        // 阶段：完整性检测
        StageTemplate completenessStage = new StageTemplate();
        completenessStage.setStageId("quality_completeness");
        completenessStage.setStageName("完整性检测");
        completenessStage.setType(StageType.QUALITY_CHECK);
        completenessStage.setOrder(25); // 在 transform 之后，sink 之前
        completenessStage.setCondition("#{intent.qualityRules.contains('completeness')}");

        ComponentTemplate completenessComponent = new ComponentTemplate();
        completenessComponent.setComponentType("completeness-checker");
        Map<String, ParameterBinding> completenessBindings = new HashMap<>();
        completenessBindings.put("requiredFields", new ParameterBinding(
            "requiredFields", BindingType.VARIABLE, "${intent.requiredFields}"
        ));
        completenessBindings.put("threshold", new ParameterBinding(
            "threshold", BindingType.VARIABLE, "${qualityThreshold}"
        ));
        completenessComponent.setParameterBindings(completenessBindings);
        completenessStage.setComponents(Collections.singletonList(completenessComponent));

        stages.add(completenessStage);

        // 阶段：一致性检测
        StageTemplate consistencyStage = new StageTemplate();
        consistencyStage.setStageId("quality_consistency");
        consistencyStage.setStageName("一致性检测");
        consistencyStage.setType(StageType.QUALITY_CHECK);
        consistencyStage.setOrder(26);
        consistencyStage.setCondition("#{intent.qualityRules.contains('consistency')}");

        ComponentTemplate consistencyComponent = new ComponentTemplate();
        consistencyComponent.setComponentType("consistency-checker");
        Map<String, ParameterBinding> consistencyBindings = new HashMap<>();
        consistencyBindings.put("rules", new ParameterBinding(
            "rules", BindingType.VARIABLE, "${intent.consistencyRules}"
        ));
        consistencyComponent.setParameterBindings(consistencyBindings);
        consistencyStage.setComponents(Collections.singletonList(consistencyComponent));

        stages.add(consistencyStage);

        // 阶段：异常值检测
        StageTemplate anomalyStage = new StageTemplate();
        anomalyStage.setStageId("quality_anomaly");
        anomalyStage.setStageName("异常值检测");
        anomalyStage.setType(StageType.QUALITY_CHECK);
        anomalyStage.setOrder(27);
        anomalyStage.setCondition("#{intent.qualityRules.contains('anomaly')}");

        ComponentTemplate anomalyComponent = new ComponentTemplate();
        anomalyComponent.setComponentType("anomaly-detector");
        Map<String, ParameterBinding> anomalyBindings = new HashMap<>();
        anomalyBindings.put("fields", new ParameterBinding(
            "fields", BindingType.VARIABLE, "${intent.anomalyFields}"
        ));
        anomalyBindings.put("method", new ParameterBinding(
            "method", BindingType.LITERAL, "zscore"
        ));
        anomalyComponent.setParameterBindings(anomalyBindings);
        anomalyStage.setComponents(Collections.singletonList(anomalyComponent));

        stages.add(anomalyStage);

        // 条件分支：质量检测失败处理
        ConditionalBranch failureBranch = new ConditionalBranch();
        failureBranch.setBranchId("on_quality_fail");
        failureBranch.setCondition("#{qualityResult.passed == false}");

        // True 分支：记录并继续
        List<StageTemplate> trueStages = new ArrayList<>();
        StageTemplate alertStage = new StageTemplate();
        alertStage.setStageId("quality_alert");
        alertStage.setStageName("质量告警");
        alertStage.setType(StageType.NOTIFICATION);
        alertStage.setOrder(28);

        ComponentTemplate alertComponent = new ComponentTemplate();
        alertComponent.setComponentType("alert-sender");
        Map<String, ParameterBinding> alertBindings = new HashMap<>();
        alertBindings.put("message", new ParameterBinding(
            "message",
            BindingType.EXPRESSION,
            "#{'数据质量检测失败: ' + qualityResult.failureReason}"
        ));
        alertComponent.setParameterBindings(alertBindings);
        alertStage.setComponents(Collections.singletonList(alertComponent));
        trueStages.add(alertStage);

        failureBranch.setTrueStages(trueStages);
        failureBranch.setFalseStages(Collections.emptyList());

        Map<String, ConditionalBranch> branches = new HashMap<>();
        branches.put("on_quality_fail", failureBranch);
        template.setConditionalBranches(branches);

        template.setStages(stages);

        return template;
    }
}
```

**使用示例：组合模板处理实时刷单检测**

```java
/**
 * 实时刷单检测场景示例
 */
public class FraudDetectionExample {

    @Autowired
    private TemplateRepository templateRepo;

    @Autowired
    private TemplateInstantiator instantiator;

    public TaskPlan createFraudDetectionPipeline(UserIntent intent, AgentContext context) {

        // 1. 基础模板：实时流处理
        String baseTemplateId = "realtime_stream";

        // 2. Mixin：数据质量检测
        String qualityMixinId = "mixin_quality_check";

        // 3. 加载并组合模板
        PlanTemplate baseTemplate = templateRepo.getTemplate(baseTemplateId);
        baseTemplate.setMixinTemplateIds(Collections.singletonList(qualityMixinId));

        // 4. 添加特定的刷单检测规则到 intent
        List<QualityRule> fraudRules = new ArrayList<>();

        // 规则1：同一用户短时间内频繁下单
        FrequencyRule freqRule = new FrequencyRule();
        freqRule.setName("高频下单检测");
        freqRule.setGroupBy("user_id");
        freqRule.setWindow("1s");
        freqRule.setThreshold(3);
        freqRule.setAction("mark_suspicious");
        fraudRules.add(freqRule);

        // 规则2：同一设备关联多个账号
        CardinalityRule cardRule = new CardinalityRule();
        cardRule.setName("设备多账号检测");
        cardRule.setGroupBy("device_id");
        cardRule.setCountDistinct("user_id");
        cardRule.setThreshold(10);
        cardRule.setAction("alert");
        fraudRules.add(cardRule);

        intent.setQualityRules(fraudRules);

        // 5. 实例化模板
        TaskPlan plan = instantiator.instantiate(baseTemplateId, intent, context);

        // 6. 返回可执行计划
        return plan;
    }
}
```

通过以上设计，PlanTemplate 系统实现了：

1. **高度可复用**：基础模板可被多个场景复用
2. **灵活组合**：通过继承和 Mixin 实现能力组合
3. **动态适配**：根据用户意图动态生成步骤
4. **类型安全**：参数定义和绑定确保配置正确性
5. **易于扩展**：添加新模板或新能力无需修改核心代码

这套模板系统是 TIS AI Agent 通用化的基石，使其能够处理从简单 ETL 到复杂实时分析的各种场景。

### 5. DSL支持

引入领域特定语言，让用户能更自然地描述需求：

```yaml
# 用户可提供的DSL配置示例
pipeline:
  name: "实时订单质量监控"

  source:
    type: kafka
    config:
      topic: orders
      group_id: quality_monitor
      format: json

  quality_rules:
    - name: "刷单检测-频率"
      type: frequency_check
      config:
        group_by: user_id
        window: 1s
        threshold: 3
        action: mark_suspicious

    - name: "刷单检测-设备关联"
      type: cardinality_check
      config:
        group_by: device_id
        count_distinct: user_id
        threshold: 10
        action: alert

  transformations:
    - type: filter
      condition: "status = 'valid'"
    - type: enrichment
      lookup: user_profile

  sink:
    - type: elasticsearch
      index: clean_orders
    - type: alert
      channel: webhook
      url: ${ALERT_WEBHOOK_URL}
```

对应的DSL解析器：

```java
public class PipelineDSLParser {

    public UserIntent parseFromDSL(String dslContent) {
        // 解析YAML/JSON格式的DSL
        Map<String, Object> config = parseYaml(dslContent);

        UserIntent intent = new UserIntent();
        intent.setPipelineName((String) config.get("name"));
        intent.setDataSources(parseDataSources(config.get("source")));
        intent.setQualityRules(parseQualityRules(config.get("quality_rules")));
        intent.setTransformations(parseTransformations(config.get("transformations")));
        intent.setSinks(parseSinks(config.get("sink")));

        return intent;
    }
}
```

### 6. 自学习引擎

建立学习机制，持续改进Agent能力：

```java
public class AgentLearningEngine {

    private CaseDatabase caseDB;
    private ModelTrainer trainer;

    // 记录成功案例
    public void recordSuccessfulExecution(UserIntent intent, TaskPlan plan,
                                         ExecutionResult result) {
        SuccessCase successCase = new SuccessCase();
        successCase.setIntent(intent);
        successCase.setPlan(plan);
        successCase.setMetrics(result.getMetrics());
        successCase.setTimestamp(new Date());

        caseDB.save(successCase);

        // 触发模型更新
        if (shouldUpdateModel()) {
            trainer.retrain(caseDB.getRecentCases());
        }
    }

    // 查找相似案例
    public Optional<TaskPlan> findSimilarPlan(UserIntent newIntent) {
        List<SuccessCase> similarCases = caseDB.findSimilar(newIntent, 0.8);

        if (!similarCases.isEmpty()) {
            // 选择最佳匹配并适配
            SuccessCase bestMatch = selectBestCase(similarCases);
            return Optional.of(adaptPlan(bestMatch.getPlan(), newIntent));
        }

        return Optional.empty();
    }

    // 从失败中学习
    public void recordFailure(UserIntent intent, TaskPlan plan, Exception error) {
        FailureCase failureCase = new FailureCase();
        failureCase.setIntent(intent);
        failureCase.setPlan(plan);
        failureCase.setError(error);

        caseDB.saveFailure(failureCase);

        // 分析失败原因，更新规则
        analyzeAndUpdateRules(failureCase);
    }
}
```

### 7. 增强的PlanGenerator实现

整合所有组件的完整实现：

```java
@Component
public class EnhancedPlanGenerator {

    @Autowired
    private IntentAnalyzer intentAnalyzer;

    @Autowired
    private PluginCapabilityRegistry capabilityRegistry;

    @Autowired
    private TemplatePlanGenerator templateGenerator;

    @Autowired
    private AgentLearningEngine learningEngine;

    @Autowired
    private PipelineDSLParser dslParser;

    @Autowired
    private PlanValidator validator;

    public TaskPlan generatePlan(String userRequest, AgentContext context) {
        try {
            // 1. 检查是否提供了DSL配置
            UserIntent intent;
            if (isDSLFormat(userRequest)) {
                intent = dslParser.parseFromDSL(userRequest);
            } else {
                // 2. 自然语言理解
                intent = intentAnalyzer.analyze(userRequest);
            }

            // 3. 验证意图可行性
            if (!capabilityRegistry.canHandle(intent)) {
                throw new UnsupportedIntentException(
                    "系统暂不支持该类型的需求，需要的能力：" + intent.getRequiredCapabilities()
                );
            }

            // 4. 尝试从历史案例学习
            Optional<TaskPlan> historicalPlan = learningEngine.findSimilarPlan(intent);
            if (historicalPlan.isPresent()) {
                TaskPlan adaptedPlan = adaptHistoricalPlan(historicalPlan.get(), intent);
                if (validator.validate(adaptedPlan)) {
                    return adaptedPlan;
                }
            }

            // 5. 查找可用插件能力
            List<PluginCapability> capabilities = capabilityRegistry.findCapabilitiesForIntent(intent);

            // 6. 生成新的执行计划
            TaskPlan plan = templateGenerator.generatePlan(intent, capabilities);

            // 7. 验证计划
            ValidationResult validation = validator.validate(plan);
            if (!validation.isValid()) {
                // 尝试自动修复
                plan = tryAutoFix(plan, validation.getIssues());
            }

            // 8. 设置执行上下文
            plan.setContext(context);
            plan.setIntent(intent);

            return plan;

        } catch (Exception e) {
            // 记录失败用于学习
            learningEngine.recordFailure(intent, null, e);
            throw new PlanGenerationException("生成执行计划失败", e);
        }
    }

    private TaskPlan adaptHistoricalPlan(TaskPlan historical, UserIntent current) {
        TaskPlan adapted = historical.clone();

        // 替换数据源配置
        adapted.replaceDataSources(current.getDataSources());

        // 更新质量规则
        if (current.hasQualityRequirements()) {
            adapted.updateQualityRules(current.getQualityRules());
        }

        // 调整目标配置
        adapted.updateSinks(current.getSinks());

        return adapted;
    }
}
```

## AI Agent 开发复杂性分析与应对策略

### 复杂性来源

#### 1. 概念层次的复杂性
- 需要在物理实现和逻辑抽象之间找到平衡
- 要处理 插件能力 → 原子能力 → 复合能力 的层层映射
- 每一层都有自己的职责和约束

#### 2. 组合爆炸的复杂性
```
假设有：
- 10 种数据源
- 5 种转换方式
- 10 种目标端
- 3 种质量检测规则

理论组合数 = 10 × 5 × 10 × 3 = 1,500 种可能
```
这就是为什么"组合优于枚举"如此重要。

#### 3. 决策逻辑的复杂性
AI Agent 需要做很多智能决策：
- 理解用户真实意图（NLP）
- 选择最优的插件组合（优化算法）
- 处理错误和异常（容错机制）
- 从历史中学习（机器学习）

#### 4. 工程实现的复杂性
- 状态管理
- 并发控制
- 事务一致性
- 性能优化
- 测试覆盖

### 降低复杂度的策略

#### 1. 分而治之
将大问题分解为小问题：
- 意图理解 → 专门的 NLP 模块
- 能力选择 → 专门的匹配算法
- 计划生成 → 专门的模板引擎
- 执行监控 → 专门的状态机

#### 2. 站在巨人的肩膀上
利用成熟的工具和框架：
- **规则引擎**：Drools（不用自己写复杂的 if-else）
- **工作流引擎**：Activiti/Camunda（不用自己管理状态）
- **NLP工具**：HanLP（不用自己做分词）
- **机器学习**：DL4J/Smile（不用自己实现算法）

#### 3. 借鉴成功经验
从 Claude Code Agent 等成熟系统学习：
- 工具组合模式
- 任务追踪机制
- 上下文管理
- 并行执行策略

## 渐进式实施路线图

### Phase 0：MVP - 最小可行产品（2周）

**目标**：实现最基础的功能，验证技术可行性

```java
// 最简单的实现
public class SimpleAgent {
    public TaskPlan generatePlan(String request) {
        // 1. 简单的关键词匹配
        if (request.contains("MySQL") && request.contains("ElasticSearch")) {
            return createMySQLToESPlan();
        }
        // 2. 返回预定义的模板
        return getDefaultPlan();
    }
}
```

**交付物**：
- [ ] 基础的请求解析
- [ ] 5个预定义的执行计划模板
- [ ] 简单的参数提取
- [ ] 基本的执行框架

### Phase 1：模板系统（1个月）

**目标**：引入可配置的模板系统，覆盖80%常见场景

```java
// 添加可配置的模板
public class TemplateBasedAgent {
    private Map<String, PlanTemplate> templates = new HashMap<>();

    public TaskPlan generatePlan(String request) {
        // 1. 简单的意图识别
        String intent = identifyIntent(request);
        // 2. 选择对应模板
        PlanTemplate template = templates.get(intent);
        // 3. 填充参数
        return instantiate(template, extractParams(request));
    }
}
```

**交付物**：
- [ ] 实现 IntentAnalyzer 基础版本
- [ ] 构建 10-15 个常用模板
- [ ] 支持模板参数化配置
- [ ] 实现 PluginCapabilityRegistry
- [ ] 基础的错误处理机制

### Phase 2：能力组合（2个月）

**目标**：支持原子能力的动态组合

```java
// 支持能力组合
public class ComposableAgent {
    public TaskPlan generatePlan(String request) {
        // 1. 识别需要的原子能力
        List<AtomicCapability> capabilities = identifyCapabilities(request);
        // 2. 组合成执行计划
        return composeCapabilities(capabilities);
    }
}
```

**交付物**：
- [ ] 实现 AtomicCapability 抽象
- [ ] 开发 CapabilityComposer
- [ ] 支持简单的 DSL 解析
- [ ] 实现数据质量检测能力
- [ ] 添加条件执行逻辑
- [ ] 支持任务并行执行

### Phase 3：智能优化（3个月）

**目标**：引入学习机制和智能决策

```java
// 从历史中学习
public class LearningAgent {
    private CaseDatabase caseDB;

    public TaskPlan generatePlan(String request) {
        // 1. 查找相似案例
        Optional<TaskPlan> similar = caseDB.findSimilar(request);
        if (similar.isPresent()) {
            return adaptPlan(similar.get(), request);
        }
        // 2. 生成新计划
        TaskPlan plan = generateNewPlan(request);
        // 3. 记录供未来学习
        caseDB.record(request, plan);
        return plan;
    }
}
```

**交付物**：
- [ ] 实现 AgentLearningEngine
- [ ] 集成 NLP 工具提升意图理解
- [ ] 实现案例相似度匹配
- [ ] 优化插件选择算法
- [ ] 完善 DSL 功能
- [ ] 性能优化和缓存机制

### Phase 4：生产就绪（4-6个月）

**目标**：达到生产环境要求

**交付物**：
- [ ] 完整的错误恢复机制
- [ ] 分布式执行支持
- [ ] 监控和告警系统
- [ ] 完善的日志和审计
- [ ] 性能调优
- [ ] 完整的测试覆盖
- [ ] 用户文档和 API 文档

### Phase 5：持续演进（长期）

**目标**：基于用户反馈持续改进

- [ ] 收集和分析用户反馈
- [ ] 扩展原子能力库
- [ ] 优化学习算法
- [ ] 支持更多数据源
- [ ] 提升并发处理能力
- [ ] 探索新的 AI 技术应用

## 核心开发原则

### 1. 迭代式开发
```
简单规则 → 模板系统 → 动态组合 → 智能学习
```
每一步都是可用的产品，逐步增加智能程度。

### 2. 核心循环优先
先实现核心的 理解→规划→执行→学习 循环：

```java
public class CoreAgentLoop {
    public void execute(String request) {
        // 1. 理解
        Intent intent = understand(request);

        // 2. 规划
        Plan plan = plan(intent);

        // 3. 执行
        Result result = execute(plan);

        // 4. 学习
        learn(intent, plan, result);
    }
}
```

### 3. 渐进式细化
- `understand()`: 从关键词匹配 → 正则表达式 → NLP
- `plan()`: 从固定模板 → 参数化模板 → 动态组合
- `execute()`: 从顺序执行 → 并行执行 → 智能调度
- `learn()`: 从简单记录 → 模式识别 → 预测优化

## 团队协作建议

### 角色分工
- **架构师**：负责整体设计和技术选型
- **AI工程师**：负责意图理解和学习算法
- **后端工程师**：负责插件集成和执行引擎
- **测试工程师**：负责测试策略和质量保证
- **产品经理**：负责需求优先级和用户反馈

### 并行开发策略
```
Team 1: 意图理解 + NLP
Team 2: 模板系统 + DSL
Team 3: 执行引擎 + 插件集成
Team 4: 学习机制 + 优化算法
```

## 风险管理

### 技术风险
- **风险**：NLP 准确率不足
- **缓解**：先用规则匹配，逐步引入 NLP

### 复杂度风险
- **风险**：系统过于复杂难以维护
- **缓解**：严格的模块化设计，清晰的接口定义

### 性能风险
- **风险**：组合爆炸导致性能问题
- **缓解**：缓存机制，预计算，剪枝算法

### 用户接受度风险
- **风险**：用户不信任 AI 的决策
- **缓解**：提供决策解释，允许人工干预

## 成功标准

### Phase 1 成功标准
- 能处理 80% 的常见数据集成场景
- 响应时间 < 3秒
- 准确率 > 85%

### Phase 2 成功标准
- 支持自定义组合能力
- 能处理 90% 的场景
- 支持并行执行

### Phase 3 成功标准
- 从历史案例学习，提升 10% 效率
- 支持复杂的数据质量规则
- 意图理解准确率 > 90%

### 最终目标
- 成为 TIS 的核心智能引擎
- 用户满意度 > 90%
- 显著降低数据集成的实施成本
- 成为业界领先的数据集成 AI Agent

## 技术选型建议

### NLP技术栈
- 中文分词：jieba、HanLP
- 意图识别：使用预训练模型如BERT进行fine-tuning
- 实体抽取：CRF、BiLSTM-CRF

### 规则引擎
- Drools：用于复杂规则管理
- Easy Rules：轻量级规则引擎

### 机器学习框架
- DL4J：Java原生深度学习框架
- Smile：统计机器学习库

## 关键优势

1. **可扩展性**：新功能只需添加原子能力，无需修改核心逻辑
2. **智能化**：通过学习不断提升处理能力
3. **用户友好**：支持自然语言和DSL两种输入方式
4. **可维护性**：模块化设计，职责清晰
5. **领域适配**：专注于大数据集成领域，积累领域知识

## 风险与挑战

1. **复杂度管理**：需要良好的架构设计避免过度复杂
2. **性能优化**：动态组合可能带来性能开销
3. **学习曲线**：开发团队需要理解新架构
4. **测试覆盖**：动态生成的计划需要更完善的测试策略

## 与 Claude Code Agent 的设计共通点分析

### 设计理念对比

TIS AI Agent 的设计与 Claude Code Agent 有诸多共通之处，这些共通点揭示了构建智能 Agent 的核心模式。

#### 1. 工具组合而非单一能力

**Claude Code Agent:**
- 拥有多种原子工具：`Read`、`Write`、`Edit`、`Bash`、`Grep`、`WebFetch` 等
- 通过组合这些工具完成复杂任务
- 工具之间可以并行调用提升效率

**TIS AI Agent:**
- 原子能力（AtomicCapability）概念
- 通过 CapabilityComposer 组合能力
- 动态组合生成执行计划

**共通点：** 都采用了"原子操作组合"的思想，而不是试图用一个巨大的函数解决所有问题。

#### 2. 意图理解层

**Claude Code Agent:**
- 分析用户请求，理解真实意图
- 判断是需要搜索、编辑还是执行
- 根据意图选择合适的工具组合

**TIS AI Agent:**
- IntentAnalyzer 分析用户请求
- 提取数据源、处理逻辑、质量规则等
- 基于意图匹配可用能力

**共通点：** 都强调先理解"用户想要什么"，而不是直接执行字面指令。

#### 3. 任务管理和追踪

**Claude Code Agent:**
- `TodoWrite` 工具管理任务列表
- 实时更新任务状态（pending/in_progress/completed）
- 将复杂任务分解为小步骤

**TIS AI Agent:**
- TaskPlan 管理执行计划
- 任务步骤的依赖关系管理
- 执行状态追踪

**共通点：** 都认识到复杂任务需要系统化的管理和追踪。

#### 4. 专门化的子代理

**Claude Code Agent:**
- `Task` 工具可以启动专门的子代理
- 不同类型的代理：general-purpose、explore、statusline-setup
- 每个代理有特定的能力集

**TIS AI Agent:**
- 不同的 PlanTemplate 对应不同场景
- 模板的继承和 Mixin 机制
- 特定领域的处理器（如质量检测、实时流处理）

**共通点：** 都采用了"专家系统"思想，让专门的组件处理专门的任务。

#### 5. 上下文感知

**Claude Code Agent:**
- 维护 AgentContext（工作目录、git状态等）
- 根据上下文调整行为
- 支持 hooks 和用户配置

**TIS AI Agent:**
- AgentContext 维护执行上下文
- TemplateContext 用于表达式计算
- 根据上下文动态调整计划

**共通点：** 都强调上下文的重要性，决策基于当前环境状态。

#### 6. 模板和可复用模式

**Claude Code Agent:**
- Slash commands（/review-pr、/help 等）
- 预定义的操作模式
- 可扩展的命令系统

**TIS AI Agent:**
- PlanTemplate 系统
- 模板继承和组合
- 可配置的执行模式

**共通点：** 都支持将常见操作模式化，提高效率。

#### 7. 动态决策和条件执行

**Claude Code Agent:**
- 根据文件类型选择不同工具
- 条件性地执行某些操作
- 动态调整执行策略

**TIS AI Agent:**
- ConditionalExecutionEngine
- 基于 SpEL 表达式的条件判断
- 动态步骤生成

**共通点：** 都支持运行时的动态决策，而不是静态的执行流程。

#### 8. 错误处理和学习

**Claude Code Agent:**
- 处理工具执行失败
- 从用户反馈中学习
- 记录和优化执行路径

**TIS AI Agent:**
- AgentLearningEngine
- 记录成功/失败案例
- 基于历史优化未来执行

**共通点：** 都重视从执行结果中学习和改进。

### 可借鉴的设计模式

#### 1. 工具优先原则（Tool-First Principle）

```java
// 像 Claude Code 一样，TIS 也应该将每个能力抽象为工具
public interface TISCapability {
    String getName();
    boolean canHandle(UserIntent intent);
    ExecutionResult execute(ExecutionContext context);
    // 支持并行执行
    boolean canRunInParallel();
}
```

#### 2. 渐进式细化（Progressive Refinement）

Claude Code 会逐步细化任务，TIS 也可以采用这种策略：

```java
public class ProgressiveIntentAnalyzer {
    public UserIntent analyze(String request) {
        // 第一步：粗粒度理解
        UserIntent roughIntent = quickAnalyze(request);

        // 第二步：如需要，深入分析
        if (roughIntent.needsRefinement()) {
            UserIntent detailedIntent = deepAnalyze(request, roughIntent);
            return detailedIntent;
        }

        return roughIntent;
    }
}
```

#### 3. 声明式配置（Declarative Configuration）

两者都倾向于声明式而非命令式：

```yaml
# Claude Code 的 slash command
/review-pr: "Review the pull request and provide feedback"

# TIS 的模板定义
template:
  id: "realtime-quality-check"
  extends: "base-streaming"
  mixins: ["quality-rules", "alerting"]
  description: "实时数据质量检测管道"
```

#### 4. 能力发现机制（Capability Discovery）

```java
// 像 Claude Code 自动发现可用工具一样
public class CapabilityDiscovery {
    @Autowired
    private PluginRegistry pluginRegistry;

    public List<AvailableCapability> discover() {
        List<AvailableCapability> capabilities = new ArrayList<>();

        // 扫描所有已注册插件
        for (Plugin plugin : pluginRegistry.getAllPlugins()) {
            if (plugin.isEnabled() && plugin.hasRequiredPermissions()) {
                capabilities.add(new AvailableCapability(plugin));
            }
        }

        return capabilities;
    }
}
```

#### 5. 上下文传递链（Context Chain）

```java
// 保持上下文在整个执行链中传递
public class ContextChain {
    private final Stack<Context> contextStack = new Stack<>();

    public void push(Context ctx) {
        contextStack.push(ctx);
    }

    public Context current() {
        return contextStack.peek();
    }

    public void pop() {
        contextStack.pop();
    }

    // 支持上下文合并
    public Context merge(Context newContext) {
        Context current = current();
        return current.merge(newContext);
    }
}
```

#### 6. 并行执行策略

借鉴 Claude Code 的并行执行策略：

```java
public class ParallelExecutor {
    public List<ExecutionResult> executeParallel(List<Task> tasks) {
        // 识别可并行的任务
        Map<Boolean, List<Task>> grouped = tasks.stream()
            .collect(Collectors.partitioningBy(Task::canRunInParallel));

        List<Task> parallelTasks = grouped.get(true);
        List<Task> sequentialTasks = grouped.get(false);

        // 并行执行独立任务
        CompletableFuture<?>[] futures = parallelTasks.stream()
            .map(task -> CompletableFuture.supplyAsync(task::execute))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        // 顺序执行依赖任务
        List<ExecutionResult> results = new ArrayList<>();
        for (Task task : sequentialTasks) {
            results.add(task.execute());
        }

        return results;
    }
}
```

### 核心启示

#### 最重要的共同理念："组合优于枚举"

- **不要试图预测所有场景**：用户需求是无限的
- **提供可组合的基础能力**：让复杂功能通过组合涌现
- **智能在于选择和组合**：Agent 的价值在于知道如何组合工具

#### 实施建议

1. **从 Claude Code 的工具模型开始**
   - 每个 TIS 插件都可以被抽象为一个"工具"
   - 工具之间通过明确的接口通信
   - 支持工具的动态发现和加载

2. **借鉴 Task/Sub-agent 模式**
   - 复杂的数据处理任务可以委托给专门的子 Agent
   - 例如：数据质量 Agent、性能优化 Agent、故障诊断 Agent
   - 每个子 Agent 专注于特定领域

3. **采用类似 TodoWrite 的进度管理**
   - 让用户能看到 Agent 的思考和执行过程
   - 提供透明度和可控性
   - 支持任务的暂停、恢复和取消

4. **实现智能的工具选择**
   ```java
   public class IntelligentToolSelector {
       public List<Tool> selectTools(UserIntent intent, List<Tool> available) {
           // 基于意图评分
           Map<Tool, Double> scores = available.stream()
               .collect(Collectors.toMap(
                   tool -> tool,
                   tool -> calculateMatchScore(tool, intent)
               ));

           // 选择最匹配的工具组合
           return optimizeToolCombination(scores, intent);
       }
   }
   ```

### 设计对比总结

| 特性 | Claude Code Agent | TIS AI Agent | 共同理念 |
|------|------------------|--------------|----------|
| 基础单元 | Tools (Read, Write, etc.) | AtomicCapability | 原子化操作 |
| 复杂任务处理 | Tool Composition | Capability Composer | 组合优于枚举 |
| 任务管理 | TodoWrite | TaskPlan | 系统化追踪 |
| 专门化处理 | Sub-agents | PlanTemplate | 专家系统 |
| 上下文管理 | AgentContext | ExecutionContext | 上下文感知 |
| 扩展机制 | Slash Commands | Template System | 模式复用 |
| 学习机制 | User Feedback | Learning Engine | 持续优化 |
| 执行策略 | Parallel Tools | Dynamic Steps | 智能调度 |

### 关键架构原则

通过对比分析，我们可以提炼出构建智能 Agent 的关键架构原则：

1. **模块化和组合性**：功能应该是可组合的模块，而不是单体函数
2. **声明式优于命令式**：描述"要什么"而不是"怎么做"
3. **上下文驱动**：所有决策都基于当前上下文
4. **渐进式处理**：从粗粒度到细粒度逐步细化
5. **透明和可控**：用户应该能看到和控制执行过程
6. **持续学习**：从每次执行中学习和优化
7. **领域专家**：专门的问题用专门的组件解决
8. **并行优化**：尽可能并行执行以提升效率

这些原则不仅适用于 TIS AI Agent，也是构建任何智能 Agent 系统的通用最佳实践。

## 总结

通过这套改进方案，并借鉴 Claude Code Agent 的成功经验，TIS的AI Agent将能够：
- 理解更复杂的用户需求
- 动态组合能力满足多样化场景
- 从历史执行中持续学习优化
- 提供更智能、更通用的数据集成服务

最关键的是，两个系统都认识到：**真正的智能不在于预定义所有可能，而在于提供灵活的组合机制**。这就像给 Agent 一套"乐高积木"，让它能根据需要构建出各种解决方案。

这种设计理念对 TIS 来说特别合适，因为大数据集成场景的多样性正好需要这种灵活的组合能力。通过借鉴 Claude Code 的成功经验，TIS AI Agent 可以更快地达到生产级别的成熟度。

这将使TIS真正成为一个智能的大数据集成平台，而不仅仅是一个功能固定的工具。