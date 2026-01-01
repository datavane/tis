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

#### 1.6 基于知识图谱的能力组合增强

通过知识图谱可以更智能地验证能力组合的可行性，并找到最优的执行路径：

```java
/**
 * 基于知识图谱的能力组合器
 */
public class GraphEnhancedCapabilityComposer extends CapabilityComposer {

    @Autowired
    private GraphDatabaseService graphDB;

    /**
     * 验证能力组合的可行性
     * 通过图谱检查能力之间是否存在冲突
     */
    @Override
    protected void validateCapabilityCompatibility(
            List<AtomicCapability> capabilities) {

        List<String> capabilityIds = capabilities.stream()
            .map(AtomicCapability::getCapabilityId)
            .collect(Collectors.toList());

        String query = """
            MATCH (c1:Capability)-[conflict:CONFLICTS_WITH]->(c2:Capability)
            WHERE c1.id IN $capIds AND c2.id IN $capIds
            RETURN c1.name as capability1,
                   c2.name as capability2,
                   conflict.reason as reason
            """;

        Result result = graphDB.execute(query, Map.of("capIds", capabilityIds));

        if (result.hasNext()) {
            StringBuilder conflicts = new StringBuilder();
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                conflicts.append(String.format("%s conflicts with %s: %s\n",
                    row.get("capability1"),
                    row.get("capability2"),
                    row.get("reason")));
            }
            throw new IncompatibleCapabilitiesException(conflicts.toString());
        }
    }

    /**
     * 使用图算法找到能力的最优组合路径
     */
    public List<AtomicCapability> findOptimalCapabilityPath(
            UserIntent intent) {

        String query = """
            // 定义起点和终点能力
            MATCH (start:Capability {type: 'READ'})
            MATCH (end:Capability {type: 'WRITE'})

            // 根据处理类型过滤相关的能力路径
            MATCH (pattern:ProcessPattern {type: $processingType})
                  -[:REQUIRES|SUGGESTS*1..3]->(cap:Capability)

            // 使用Dijkstra算法找最优路径
            CALL gds.shortestPath.dijkstra.stream({
                sourceNode: id(start),
                targetNode: id(end),
                nodeQuery: 'MATCH (n:Capability) RETURN id(n) as id',
                relationshipQuery: '''
                    MATCH (c1:Capability)-[r:LEADS_TO]->(c2:Capability)
                    RETURN id(c1) as source, id(c2) as target, r.cost as cost
                ''',
                relationshipWeightProperty: 'cost'
            })
            YIELD path, totalCost

            // 返回路径上的能力
            RETURN [node in nodes(path) | node.id] as capabilityPath,
                   totalCost
            ORDER BY totalCost ASC
            LIMIT 1
            """;

        Result result = graphDB.execute(query, Map.of(
            "processingType", intent.getProcessingType().name()
        ));

        if (result.hasNext()) {
            Map<String, Object> row = result.next();
            List<String> capabilityIds = (List<String>) row.get("capabilityPath");
            return capabilityIds.stream()
                .map(this::loadCapabilityById)
                .collect(Collectors.toList());
        }

        // 如果没有找到路径，使用默认的能力识别逻辑
        return super.identifyRequiredCapabilities(intent);
    }

    /**
     * 检查能力组合是否满足所有约束条件
     */
    public boolean validateConstraints(
            List<AtomicCapability> capabilities,
            UserIntent intent) {

        String query = """
            // 检查每个能力的前置条件是否满足
            UNWIND $capabilityIds as capId
            MATCH (cap:Capability {id: capId})

            OPTIONAL MATCH (cap)-[:HAS_PRECONDITION]->(pre:Constraint)
            WITH cap, collect(pre) as preconditions

            // 验证前置条件
            RETURN cap.id as capabilityId,
                   cap.name as capabilityName,
                   preconditions,
                   size([p in preconditions WHERE p.satisfied = false]) as unsatisfiedCount
            """;

        List<String> capIds = capabilities.stream()
            .map(AtomicCapability::getCapabilityId)
            .collect(Collectors.toList());

        Result result = graphDB.execute(query, Map.of("capabilityIds", capIds));

        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            long unsatisfied = (Long) row.get("unsatisfiedCount");
            if (unsatisfied > 0) {
                logger.warn("Capability {} has unsatisfied preconditions",
                    row.get("capabilityName"));
                return false;
            }
        }

        return true;
    }

    /**
     * 基于图谱推荐补充能力
     */
    public List<AtomicCapability> recommendAdditionalCapabilities(
            List<AtomicCapability> currentCapabilities,
            UserIntent intent) {

        List<String> currentCapIds = currentCapabilities.stream()
            .map(AtomicCapability::getCapabilityId)
            .collect(Collectors.toList());

        String query = """
            // 查找当前能力经常与哪些其他能力一起使用
            MATCH (current:Capability)
            WHERE current.id IN $currentCapIds

            MATCH (current)<-[:USED_CAPABILITY]-(case:SuccessCase)
                  -[:USED_CAPABILITY]->(other:Capability)
            WHERE NOT other.id IN $currentCapIds
              AND case.processingType = $processingType

            // 计算共现频率
            WITH other, count(case) as cooccurrence
            WHERE cooccurrence >= 3

            RETURN other.id as capabilityId,
                   other.name as capabilityName,
                   cooccurrence,
                   other.priority as priority
            ORDER BY cooccurrence DESC, priority DESC
            LIMIT 5
            """;

        Result result = graphDB.execute(query, Map.of(
            "currentCapIds", currentCapIds,
            "processingType", intent.getProcessingType().name()
        ));

        List<AtomicCapability> recommendations = new ArrayList<>();
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            String capId = (String) row.get("capabilityId");
            recommendations.add(loadCapabilityById(capId));
        }

        return recommendations;
    }
}
```

**知识图谱增强能力组合的优势：**

1. **冲突检测**：自动检测能力之间的不兼容性，避免无效组合
2. **路径优化**：使用图算法（Dijkstra）找到成本最低的能力组合路径
3. **约束验证**：确保所有前置条件和后置条件都得到满足
4. **智能推荐**：基于历史成功案例推荐补充能力
5. **动态适应**：随着案例积累，组合策略自动优化

#### 1.7 关系总结

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

#### 2.1 知识图谱增强的意图理解（推荐）

通过引入知识图谱，可以显著提升意图理解的准确性和智能化水平。采用**检索增强生成（RAG）**模式，结合领域知识进行语义推理：

```java
/**
 * 知识图谱增强的意图分析器
 * 采用RAG（Retrieval-Augmented Generation）模式
 */
public class KnowledgeGraphEnhancedIntentAnalyzer extends IntentAnalyzer {

    @Autowired
    private GraphDatabaseService graphDB;

    @Autowired
    private LLMProvider llmProvider;

    @Autowired
    private EntityExtractor entityExtractor;

    @Override
    public UserIntent analyze(String userRequest) {
        // Step 1: 实体抽取 - 从用户问题中提取关键实体
        List<Entity> entities = entityExtractor.extract(userRequest);

        // Step 2: 知识图谱召回 - 检索相关三元组
        List<Triple> relevantTriples = retrieveFromKnowledgeGraph(entities);

        // Step 3: 上下文构建 - 将三元组转化为自然语言上下文
        String domainContext = buildContextFromTriples(relevantTriples);

        // Step 4: LLM推理 - 结合领域知识进行意图理解
        String prompt = buildEnhancedPrompt(userRequest, domainContext);
        String llmResponse = llmProvider.generate(prompt);

        // Step 5: 结果解析 - 构建结构化的UserIntent
        UserIntent intent = parseIntentFromLLM(llmResponse);

        // Step 6: 图谱验证与补全 - 通过图谱推理验证并补充隐含需求
        intent = validateAndEnrichIntent(intent);

        return intent;
    }

    /**
     * 从知识图谱中检索相关知识
     * 采用多跳查询获取完整的上下文信息
     */
    private List<Triple> retrieveFromKnowledgeGraph(List<Entity> entities) {
        String query = """
            // 一跳查询：直接相关的关系
            MATCH (e:Entity)-[r]->(target)
            WHERE e.name IN $entityNames
            RETURN e.name as subject, type(r) as predicate, target.name as object, 1 as hop

            UNION

            MATCH (source)-[r]->(e:Entity)
            WHERE e.name IN $entityNames
            RETURN source.name as subject, type(r) as predicate, e.name as object, 1 as hop

            UNION

            // 二跳查询：获取更丰富的上下文
            MATCH (e1:Entity)-[r1]->(middle)-[r2]->(e2)
            WHERE e1.name IN $entityNames OR e2.name IN $entityNames
            RETURN e1.name as subject,
                   type(r1)+' -> '+middle.name+' -> '+type(r2) as predicate,
                   e2.name as object, 2 as hop
            ORDER BY hop ASC
            LIMIT 100
            """;

        Map<String, Object> params = Map.of(
            "entityNames",
            entities.stream().map(Entity::getName).collect(Collectors.toList())
        );

        return executeGraphQuery(query, params);
    }

    /**
     * 将三元组转化为自然语言上下文
     */
    private String buildContextFromTriples(List<Triple> triples) {
        StringBuilder context = new StringBuilder();
        context.append("## 领域知识（从知识图谱检索）\n\n");

        // 按主题分组
        Map<String, List<Triple>> groupedBySubject = triples.stream()
            .collect(Collectors.groupingBy(Triple::getSubject));

        for (Map.Entry<String, List<Triple>> entry : groupedBySubject.entrySet()) {
            String subject = entry.getKey();
            List<Triple> subjectTriples = entry.getValue();

            context.append(String.format("### %s\n", subject));
            for (Triple triple : subjectTriples) {
                context.append(String.format("- %s %s %s\n",
                    triple.getSubject(),
                    triple.getPredicate(),
                    triple.getObject()));
            }
            context.append("\n");
        }

        return context.toString();
    }

    /**
     * 构建增强的Prompt
     */
    private String buildEnhancedPrompt(String userRequest, String graphContext) {
        return String.format("""
            你是TIS数据集成平台的AI助手，需要理解用户的数据集成需求。

            ## 用户请求
            %s

            %s

            ## 任务
            请分析用户意图，输出JSON格式的结构化信息，包括：
            1. dataSources: 数据源列表，格式为 [{type, connection, table}]
            2. processingType: 处理类型（ETL/Streaming/CDC/Migration等）
            3. qualityRules: 需要的质量规则列表
            4. transformations: 数据转换需求
            5. executionMode: 执行模式（Batch/Realtime）
            6. implicitRequirements: 通过领域知识推理得出的隐含需求
            7. suggestedCapabilities: 建议使用的能力
            8. confidence: 意图理解的置信度（0-1）

            请基于提供的领域知识进行推理，输出严格的JSON格式。
            如果用户请求中有歧义，请在implicitRequirements中说明你的假设。
            """, userRequest, graphContext);
    }

    /**
     * 从LLM响应中解析UserIntent
     */
    private UserIntent parseIntentFromLLM(String llmResponse) {
        try {
            JSONObject json = JSON.parseObject(llmResponse);

            UserIntent intent = new UserIntent();
            intent.setDataSources(parseDataSources(json.getJSONArray("dataSources")));
            intent.setProcessingType(ProcessingType.valueOf(json.getString("processingType")));
            intent.setQualityRules(parseQualityRules(json.getJSONArray("qualityRules")));
            intent.setTransformations(parseTransformations(json.getJSONArray("transformations")));
            intent.setExecutionMode(ExecutionMode.valueOf(json.getString("executionMode")));
            intent.setImplicitRequirements(parseList(json.getJSONArray("implicitRequirements")));
            intent.setSuggestedCapabilities(parseList(json.getJSONArray("suggestedCapabilities")));
            intent.setConfidence(json.getDoubleValue("confidence"));

            return intent;
        } catch (Exception e) {
            throw new IntentParsingException("Failed to parse LLM response", e);
        }
    }

    /**
     * 通过图谱推理验证并补充意图
     */
    private UserIntent validateAndEnrichIntent(UserIntent intent) {
        // 1. 推理隐含的能力需求
        List<String> inferredCapabilities = inferCapabilitiesFromGraph(intent);
        intent.addSuggestedCapabilities(inferredCapabilities);

        // 2. 检查数据源组合的常见模式
        List<String> commonPatterns = findCommonPatterns(intent.getDataSources());
        if (!commonPatterns.isEmpty()) {
            intent.addMetadata("commonPatterns", commonPatterns);
        }

        // 3. 推荐质量规则
        if (intent.getQualityRules().isEmpty()) {
            List<QualityRule> recommendedRules = recommendQualityRules(intent);
            intent.setSuggestedQualityRules(recommendedRules);
        }

        return intent;
    }

    /**
     * 从图谱推理所需能力
     */
    private List<String> inferCapabilitiesFromGraph(UserIntent intent) {
        String query = """
            MATCH (source:DataSource {name: $sourceName})
                  -[:COMMON_PATTERN]->(pattern:ProcessPattern)
                  -[:REQUIRES|SUGGESTS]->(capability:Capability)
            WHERE pattern.type = $processingType
            RETURN DISTINCT capability.id as capabilityId,
                   capability.name as capabilityName,
                   type(pattern-[rel]->capability) as relationType
            ORDER BY
                CASE WHEN type(pattern-[rel]->capability) = 'REQUIRES' THEN 1 ELSE 2 END
            """;

        Map<String, Object> params = Map.of(
            "sourceName", intent.getDataSources().get(0).getType(),
            "processingType", intent.getProcessingType().name()
        );

        Result result = graphDB.execute(query, params);
        List<String> capabilities = new ArrayList<>();

        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            capabilities.add((String) row.get("capabilityId"));
        }

        return capabilities;
    }

    /**
     * 查找常见的处理模式
     */
    private List<String> findCommonPatterns(List<DataSource> dataSources) {
        if (dataSources.size() < 2) {
            return Collections.emptyList();
        }

        String sourceType = dataSources.get(0).getType();
        String targetType = dataSources.get(1).getType();

        String query = """
            MATCH (source:DataSource {name: $sourceType})
                  -[:COMMON_PATTERN]->(pattern:ProcessPattern)
                  <-[:COMMON_PATTERN]-(target:DataSource {name: $targetType})
            RETURN pattern.name as patternName,
                   pattern.description as description,
                   pattern.successRate as successRate
            ORDER BY pattern.successRate DESC
            LIMIT 5
            """;

        Map<String, Object> params = Map.of(
            "sourceType", sourceType,
            "targetType", targetType
        );

        Result result = graphDB.execute(query, params);
        List<String> patterns = new ArrayList<>();

        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            patterns.add((String) row.get("patternName"));
        }

        return patterns;
    }

    /**
     * 推荐质量规则
     */
    private List<QualityRule> recommendQualityRules(UserIntent intent) {
        String query = """
            MATCH (pattern:ProcessPattern {type: $processingType})
                  -[:SUGGESTS]->(rule:QualityRule)
            RETURN rule.id as ruleId,
                   rule.name as ruleName,
                   rule.description as description,
                   rule.priority as priority
            ORDER BY rule.priority DESC
            LIMIT 10
            """;

        Map<String, Object> params = Map.of(
            "processingType", intent.getProcessingType().name()
        );

        Result result = graphDB.execute(query, params);
        List<QualityRule> rules = new ArrayList<>();

        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            QualityRule rule = createQualityRuleFromGraph(row);
            rules.add(rule);
        }

        return rules;
    }
}
```

**知识图谱增强带来的优势：**

1. **语义消歧**：理解"同步"、"导入"、"迁移"等同义词都指向数据集成场景
2. **隐含需求推理**：从"MySQL到ES"推理出需要JDBC配置、字段类型映射等
3. **最佳实践推荐**：基于历史成功案例推荐常用的质量规则和配置
4. **上下文补全**：通过多跳图查询获取完整的领域知识上下文
5. **验证与纠错**：检测用户意图中的不合理配置并给出建议

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

#### 3.1 知识图谱增强的能力注册表（强烈推荐）

通过将插件能力存储在知识图谱中，可以实现更智能的能力发现和匹配：

```java
/**
 * 基于知识图谱的插件能力注册表
 * 这是知识图谱应用的最佳场景之一
 */
public class GraphBasedCapabilityRegistry extends PluginCapabilityRegistry {

    @Autowired
    private GraphDatabaseService graphDB;

    @Autowired
    private PluginRegistry pluginRegistry;

    /**
     * 初始化时将插件信息同步到知识图谱
     */
    @Override
    @PostConstruct
    public void initializeRegistry() {
        super.initializeRegistry();

        // 将插件能力信息同步到图数据库
        syncPluginsToGraph();
    }

    /**
     * 将插件信息同步到知识图谱
     */
    private void syncPluginsToGraph() {
        for (Plugin plugin : pluginRegistry.getAllPlugins()) {
            String query = """
                MERGE (p:Plugin {id: $pluginId})
                SET p.name = $pluginName,
                    p.version = $version,
                    p.category = $category,
                    p.enabled = $enabled

                WITH p
                UNWIND $supportedDataSources as dsName
                MERGE (ds:DataSource {name: dsName})
                MERGE (ds)-[:SUPPORTED_BY]->(p)

                WITH p
                UNWIND $capabilities as capName
                MERGE (cap:Capability {name: capName})
                MERGE (p)-[:HAS_CAPABILITY]->(cap)

                WITH p
                FOREACH (dep IN $dependencies |
                    MERGE (depPlugin:Plugin {id: dep})
                    MERGE (p)-[:DEPENDS_ON]->(depPlugin)
                )
                """;

            Map<String, Object> params = Map.of(
                "pluginId", plugin.getId(),
                "pluginName", plugin.getName(),
                "version", plugin.getVersion(),
                "category", plugin.getCategory(),
                "enabled", plugin.isEnabled(),
                "supportedDataSources", plugin.getSupportedDataSources(),
                "capabilities", plugin.getCapabilities(),
                "dependencies", plugin.getDependencies()
            );

            graphDB.execute(query, params);
        }
    }

    /**
     * 通过图查询找到最匹配的插件组合
     * 考虑兼容性、成功率、性能等多维度因素
     */
    @Override
    public List<PluginCapability> findCapabilitiesForIntent(UserIntent intent) {
        String sourceType = intent.getDataSources().get(0).getType();
        String targetType = intent.getDataSources().size() > 1 ?
            intent.getDataSources().get(1).getType() : null;
        String processingType = intent.getProcessingType().name();

        String query = """
            // 查找支持源数据源的Reader插件
            MATCH (source:DataSource {name: $sourceType})
                  -[:SUPPORTED_BY]->(reader:Plugin)
                  -[:HAS_CAPABILITY]->(readCap:Capability)
            WHERE readCap.type = 'READ' AND reader.enabled = true

            // 如果有目标数据源，查找Writer插件
            OPTIONAL MATCH (target:DataSource {name: $targetType})
                  -[:SUPPORTED_BY]->(writer:Plugin)
                  -[:HAS_CAPABILITY]->(writeCap:Capability)
            WHERE writeCap.type = 'WRITE' AND writer.enabled = true

            // 检查兼容性
            OPTIONAL MATCH (reader)-[compat:COMPATIBLE_WITH]->(writer)

            // 查找历史成功案例数量
            OPTIONAL MATCH (reader)<-[:USED_PLUGIN]-(successCase:SuccessCase)
                          -[:USED_PLUGIN]->(writer)
            WHERE successCase.processingType = $processingType

            // 返回结果，按成功率排序
            RETURN reader,
                   writer,
                   count(DISTINCT successCase) as successCount,
                   compat.score as compatScore,
                   reader.performanceScore as readerPerf,
                   COALESCE(writer.performanceScore, 0) as writerPerf
            ORDER BY successCount DESC, compatScore DESC, readerPerf DESC
            LIMIT 10
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("sourceType", sourceType);
        params.put("targetType", targetType);
        params.put("processingType", processingType);

        Result result = graphDB.execute(query, params);
        return convertToPluginCapabilities(result);
    }

    /**
     * 推理所需的能力组合
     */
    public List<AtomicCapability> inferRequiredCapabilities(UserIntent intent) {
        String query = """
            MATCH (pattern:ProcessPattern {type: $processingType})
                  -[rel:REQUIRES|SUGGESTS]->(cap:Capability)

            // 检查当前系统是否有支持该能力的插件
            OPTIONAL MATCH (cap)<-[:HAS_CAPABILITY]-(plugin:Plugin)
            WHERE plugin.enabled = true

            RETURN cap.id as capabilityId,
                   cap.name as capabilityName,
                   cap.description as description,
                   type(rel) as relationType,
                   count(plugin) as availablePlugins,
                   CASE WHEN type(rel) = 'REQUIRES' THEN 1 ELSE 2 END as priority
            ORDER BY priority ASC, availablePlugins DESC
            """;

        Map<String, Object> params = Map.of(
            "processingType", intent.getProcessingType().name()
        );

        Result result = graphDB.execute(query, params);
        List<AtomicCapability> capabilities = new ArrayList<>();

        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            String capabilityId = (String) row.get("capabilityId");
            long availablePlugins = (Long) row.get("availablePlugins");

            if (availablePlugins == 0) {
                // 没有可用插件，记录警告
                logger.warn("Required capability {} has no available plugins",
                    row.get("capabilityName"));
            }

            AtomicCapability capability = createCapability(capabilityId);
            capabilities.add(capability);
        }

        return capabilities;
    }

    /**
     * 检查系统是否具备处理该意图的能力
     * 通过图谱进行深度验证
     */
    @Override
    public boolean canHandle(UserIntent intent) {
        String query = """
            MATCH (pattern:ProcessPattern {type: $processingType})
                  -[:REQUIRES]->(requiredCap:Capability)

            // 检查是否所有必需的能力都有可用插件
            OPTIONAL MATCH (requiredCap)<-[:HAS_CAPABILITY]-(plugin:Plugin)
            WHERE plugin.enabled = true

            WITH requiredCap, count(plugin) as availableCount
            WHERE availableCount = 0

            RETURN count(requiredCap) as missingCapabilities
            """;

        Map<String, Object> params = Map.of(
            "processingType", intent.getProcessingType().name()
        );

        Result result = graphDB.execute(query, params);
        if (result.hasNext()) {
            long missingCount = (Long) result.next().get("missingCapabilities");
            return missingCount == 0;
        }

        return false;
    }

    /**
     * 查找插件的最佳组合路径
     * 使用图算法找到从源到目标的最优路径
     */
    public List<PluginCapability> findOptimalPluginPath(
            DataSource source,
            DataSource target,
            UserIntent intent) {

        String query = """
            MATCH (src:DataSource {name: $sourceName})
            MATCH (tgt:DataSource {name: $targetName})

            // 使用Dijkstra算法找最短路径
            CALL gds.shortestPath.dijkstra.stream({
                sourceNode: id(src),
                targetNode: id(tgt),
                relationshipWeightProperty: 'cost'
            })
            YIELD nodeIds, costs, path

            // 获取路径上的所有插件
            UNWIND nodes(path) as node
            WITH node WHERE node:Plugin
            RETURN collect(node) as pluginsInPath,
                   sum(costs) as totalCost
            ORDER BY totalCost ASC
            LIMIT 1
            """;

        Map<String, Object> params = Map.of(
            "sourceName", source.getType(),
            "targetName", target.getType()
        );

        Result result = graphDB.execute(query, params);
        return convertToPluginCapabilities(result);
    }

    /**
     * 根据性能指标和成功率推荐最佳插件
     */
    public PluginCapability recommendBestPlugin(
            String capabilityType,
            UserIntent intent) {

        String query = """
            MATCH (cap:Capability {type: $capabilityType})
                  <-[:HAS_CAPABILITY]-(plugin:Plugin)
            WHERE plugin.enabled = true

            // 计算综合评分
            OPTIONAL MATCH (plugin)<-[:USED_PLUGIN]-(case:SuccessCase)
            WHERE case.processingType = $processingType

            WITH plugin,
                 count(case) as successCount,
                 plugin.performanceScore as perfScore,
                 plugin.reliabilityScore as reliScore

            // 综合评分：成功案例数 * 0.4 + 性能 * 0.3 + 可靠性 * 0.3
            RETURN plugin,
                   (successCount * 0.4 + perfScore * 0.3 + reliScore * 0.3) as score
            ORDER BY score DESC
            LIMIT 1
            """;

        Map<String, Object> params = Map.of(
            "capabilityType", capabilityType,
            "processingType", intent.getProcessingType().name()
        );

        Result result = graphDB.execute(query, params);
        if (result.hasNext()) {
            Map<String, Object> row = result.next();
            return convertToPluginCapability((Node) row.get("plugin"));
        }

        return null;
    }

    /**
     * 检查插件之间的冲突
     */
    public List<String> checkPluginConflicts(List<PluginCapability> plugins) {
        List<String> pluginIds = plugins.stream()
            .map(PluginCapability::getPluginId)
            .collect(Collectors.toList());

        String query = """
            MATCH (p1:Plugin)-[conflict:CONFLICTS_WITH]->(p2:Plugin)
            WHERE p1.id IN $pluginIds AND p2.id IN $pluginIds
            RETURN p1.name as plugin1,
                   p2.name as plugin2,
                   conflict.reason as reason
            """;

        Map<String, Object> params = Map.of("pluginIds", pluginIds);

        Result result = graphDB.execute(query, params);
        List<String> conflicts = new ArrayList<>();

        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            conflicts.add(String.format("%s conflicts with %s: %s",
                row.get("plugin1"),
                row.get("plugin2"),
                row.get("reason")));
        }

        return conflicts;
    }

    /**
     * 分析插件依赖关系
     */
    public Map<String, List<String>> analyzePluginDependencies(
            List<PluginCapability> selectedPlugins) {

        List<String> pluginIds = selectedPlugins.stream()
            .map(PluginCapability::getPluginId)
            .collect(Collectors.toList());

        String query = """
            MATCH (p:Plugin)-[:DEPENDS_ON*1..3]->(dep:Plugin)
            WHERE p.id IN $pluginIds
            RETURN p.id as pluginId,
                   collect(DISTINCT dep.id) as dependencies
            """;

        Map<String, Object> params = Map.of("pluginIds", pluginIds);

        Result result = graphDB.execute(query, params);
        Map<String, List<String>> dependencyMap = new HashMap<>();

        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            dependencyMap.put(
                (String) row.get("pluginId"),
                (List<String>) row.get("dependencies")
            );
        }

        return dependencyMap;
    }
}
```

**基于知识图谱的能力注册表优势：**

1. **智能插件匹配**：通过图查询综合考虑兼容性、成功率、性能等多维度因素
2. **路径优化**：使用图算法（Dijkstra）找到最优的插件组合路径
3. **冲突检测**：自动检测插件之间的不兼容性
4. **依赖分析**：追踪多层依赖关系，确保所有依赖插件都可用
5. **动态推荐**：基于历史成功案例动态推荐最佳插件
6. **可扩展性**：新增插件时自动建立与现有插件的关系

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

### 6. 知识图谱应用架构

知识图谱是TIS AI Agent智能化的核心支撑，本节详细阐述知识图谱的本体模型、Schema设计及在各个组件中的应用。

#### 6.1 本体模型设计

TIS数据集成领域的本体模型包含以下核心概念层次：

**实体类型（Entity Types）**

```java
/**
 * 知识图谱中的实体类型定义
 */
public enum EntityType {
    DATA_SOURCE("数据源"),           // MySQL, PostgreSQL, Kafka, ES等
    PLUGIN("插件"),                  // mysql-reader, es-writer等
    CAPABILITY("能力"),              // Read, Write, Transform等
    PROCESS_PATTERN("处理模式"),     // ETL, CDC, Streaming等
    QUALITY_RULE("质量规则"),        // Completeness, Frequency等
    SUCCESS_CASE("成功案例"),        // 历史执行记录
    TRANSFORM_RULE("转换规则"),      // 字段映射、类型转换等
    TERM("术语"),                    // 同义词、领域术语等
    CONSTRAINT("约束"),              // 前置条件、后置条件等
    ;

    private final String description;

    EntityType(String description) {
        this.description = description;
    }
}
```

**关系类型（Relationship Types）**

```java
/**
 * 知识图谱中的关系类型定义
 */
public enum RelationType {
    // 支持关系
    SUPPORTED_BY("支持"),           // DataSource -[:SUPPORTED_BY]-> Plugin
    HAS_CAPABILITY("具有能力"),     // Plugin -[:HAS_CAPABILITY]-> Capability

    // 依赖关系
    DEPENDS_ON("依赖"),             // Plugin -[:DEPENDS_ON]-> Plugin
    REQUIRES("需要"),               // ProcessPattern -[:REQUIRES]-> Capability
    SUGGESTS("建议"),               // ProcessPattern -[:SUGGESTS]-> Capability

    // 兼容性关系
    COMPATIBLE_WITH("兼容"),        // Plugin -[:COMPATIBLE_WITH]-> Plugin
    CONFLICTS_WITH("冲突"),         // Plugin -[:CONFLICTS_WITH]-> Plugin

    // 模式关系
    COMMON_PATTERN("常见模式"),     // DataSource -[:COMMON_PATTERN]-> ProcessPattern
    APPLIES_TO("适用于"),           // QualityRule -[:APPLIES_TO]-> ProcessPattern

    // 语义关系
    SYNONYM("同义词"),              // Term -[:SYNONYM]-> Term
    IS_A("继承"),                   // MySQL -[:IS_A]-> RelationalDatabase
    HAS_PROPERTY("具有属性"),       // DataSource -[:HAS_PROPERTY]-> Property

    // 案例关系
    USED_PLUGIN("使用插件"),        // SuccessCase -[:USED_PLUGIN]-> Plugin
    FROM_SOURCE("来源"),            // SuccessCase -[:FROM_SOURCE]-> DataSource
    TO_TARGET("目标"),              // SuccessCase -[:TO_TARGET]-> DataSource

    // 转换关系
    MAPS_TO("映射到"),              // Term -[:MAPS_TO]-> ProcessPattern
    TRANSFORMS_TO("转换为"),        // DataType -[:TRANSFORMS_TO]-> DataType
    ;

    private final String description;

    RelationType(String description) {
        this.description = description;
    }
}
```

#### 6.2 知识图谱Schema定义

使用Cypher语言定义完整的图谱Schema：

```cypher
// ============================================
// 1. 约束和索引
// ============================================

// 唯一性约束
CREATE CONSTRAINT datasource_name IF NOT EXISTS
FOR (ds:DataSource) REQUIRE ds.name IS UNIQUE;

CREATE CONSTRAINT plugin_id IF NOT EXISTS
FOR (p:Plugin) REQUIRE p.id IS UNIQUE;

CREATE CONSTRAINT capability_id IF NOT EXISTS
FOR (c:Capability) REQUIRE c.id IS UNIQUE;

// 性能索引
CREATE INDEX datasource_type IF NOT EXISTS
FOR (ds:DataSource) ON (ds.type);

CREATE INDEX plugin_category IF NOT EXISTS
FOR (p:Plugin) ON (p.category);

CREATE INDEX capability_type IF NOT EXISTS
FOR (c:Capability) ON (c.type);

// ============================================
// 2. 数据源本体
// ============================================

// 关系型数据库
CREATE (mysql:DataSource {
    name: 'MySQL',
    type: 'Relational',
    category: 'JDBC',
    jdbcDriver: 'com.mysql.cj.jdbc.Driver',
    defaultPort: 3306
})

CREATE (postgres:DataSource {
    name: 'PostgreSQL',
    type: 'Relational',
    category: 'JDBC',
    jdbcDriver: 'org.postgresql.Driver',
    defaultPort: 5432
})

// NoSQL数据库
CREATE (es:DataSource {
    name: 'ElasticSearch',
    type: 'Search',
    category: 'NoSQL',
    protocol: 'HTTP',
    defaultPort: 9200
})

CREATE (mongo:DataSource {
    name: 'MongoDB',
    type: 'Document',
    category: 'NoSQL',
    defaultPort: 27017
})

// 消息队列
CREATE (kafka:DataSource {
    name: 'Kafka',
    type: 'Stream',
    category: 'MessageQueue',
    protocol: 'TCP',
    defaultPort: 9092
})

// 继承关系
CREATE (relationalDB:Category {name: 'RelationalDatabase', type: 'Category'})
CREATE (mysql)-[:IS_A]->(relationalDB)
CREATE (postgres)-[:IS_A]->(relationalDB)

// ============================================
// 3. 插件本体
// ============================================

CREATE (mysqlReader:Plugin {
    id: 'mysql-reader',
    name: 'MySQL Reader',
    version: '2.1.0',
    category: 'Reader',
    enabled: true,
    performanceScore: 0.85,
    reliabilityScore: 0.90
})

CREATE (esWriter:Plugin {
    id: 'es-writer',
    name: 'ElasticSearch Writer',
    version: '3.0.0',
    category: 'Writer',
    enabled: true,
    performanceScore: 0.88,
    reliabilityScore: 0.92
})

CREATE (flinkCdcMysql:Plugin {
    id: 'flink-cdc-mysql',
    name: 'Flink CDC MySQL',
    version: '2.3.0',
    category: 'StreamingReader',
    enabled: true,
    performanceScore: 0.90,
    reliabilityScore: 0.85
})

// ============================================
// 4. 能力本体
// ============================================

CREATE (readCap:Capability {
    id: 'data_read',
    name: '数据读取',
    type: 'READ',
    description: '从数据源读取数据'
})

CREATE (writeCap:Capability {
    id: 'data_write',
    name: '数据写入',
    type: 'WRITE',
    description: '向数据源写入数据'
})

CREATE (transformCap:Capability {
    id: 'data_transform',
    name: '数据转换',
    type: 'TRANSFORM',
    description: '转换数据格式和内容'
})

CREATE (qualityCap:Capability {
    id: 'quality_check',
    name: '质量检测',
    type: 'QUALITY',
    description: '检测数据质量'
})

CREATE (cdcCap:Capability {
    id: 'change_data_capture',
    name: '变更数据捕获',
    type: 'CDC',
    description: '实时捕获数据变更'
})

// ============================================
// 5. 处理模式本体
// ============================================

CREATE (etl:ProcessPattern {
    id: 'etl',
    name: 'ETL',
    type: 'Batch',
    description: '批量ETL处理',
    successRate: 0.95
})

CREATE (cdc:ProcessPattern {
    id: 'cdc',
    name: 'CDC',
    type: 'Realtime',
    description: '变更数据捕获',
    successRate: 0.88
})

CREATE (streaming:ProcessPattern {
    id: 'streaming',
    name: 'Streaming',
    type: 'Realtime',
    description: '实时流处理',
    successRate: 0.82
})

// ============================================
// 6. 质量规则本体
// ============================================

CREATE (freqCheck:QualityRule {
    id: 'frequency_check',
    name: '频率检测',
    type: 'Frequency',
    description: '检测时间窗口内的频率',
    priority: 1
})

CREATE (cardCheck:QualityRule {
    id: 'cardinality_check',
    name: '基数检测',
    type: 'Cardinality',
    description: '检测唯一值数量',
    priority: 2
})

CREATE (completenessCheck:QualityRule {
    id: 'completeness_check',
    name: '完整性检测',
    type: 'Completeness',
    description: '检测必填字段的完整性',
    priority: 1
})

// ============================================
// 7. 建立关系
// ============================================

// 数据源与插件的支持关系
CREATE (mysql)-[:SUPPORTED_BY]->(mysqlReader)
CREATE (mysql)-[:SUPPORTED_BY]->(flinkCdcMysql)
CREATE (es)-[:SUPPORTED_BY]->(esWriter)

// 插件与能力的关系
CREATE (mysqlReader)-[:HAS_CAPABILITY]->(readCap)
CREATE (flinkCdcMysql)-[:HAS_CAPABILITY]->(readCap)
CREATE (flinkCdcMysql)-[:HAS_CAPABILITY]->(cdcCap)
CREATE (esWriter)-[:HAS_CAPABILITY]->(writeCap)

// 处理模式与能力的关系
CREATE (etl)-[:REQUIRES]->(readCap)
CREATE (etl)-[:REQUIRES]->(writeCap)
CREATE (etl)-[:SUGGESTS]->(transformCap)
CREATE (etl)-[:SUGGESTS]->(qualityCap)

CREATE (cdc)-[:REQUIRES]->(cdcCap)
CREATE (cdc)-[:REQUIRES]->(writeCap)
CREATE (cdc)-[:SUGGESTS]->(qualityCap)

CREATE (streaming)-[:REQUIRES]->(readCap)
CREATE (streaming)-[:REQUIRES]->(writeCap)
CREATE (streaming)-[:SUGGESTS]->(qualityCap)

// 数据源与处理模式的关系
CREATE (mysql)-[:COMMON_PATTERN {successRate: 0.95}]->(etl)
CREATE (mysql)-[:COMMON_PATTERN {successRate: 0.88}]->(cdc)
CREATE (kafka)-[:COMMON_PATTERN {successRate: 0.90}]->(streaming)

// 质量规则的适用场景
CREATE (freqCheck)-[:APPLIES_TO]->(streaming)
CREATE (cardCheck)-[:APPLIES_TO]->(streaming)
CREATE (completenessCheck)-[:APPLIES_TO]->(etl)

// 插件兼容性
CREATE (mysqlReader)-[:COMPATIBLE_WITH {score: 0.95}]->(esWriter)
CREATE (flinkCdcMysql)-[:COMPATIBLE_WITH {score: 0.92}]->(esWriter)

// 同义词关系
CREATE (syncTerm:Term {name: '同步'})
CREATE (importTerm:Term {name: '导入'})
CREATE (migrationTerm:Term {name: '迁移'})
CREATE (etlTerm:Term {name: 'ETL'})

CREATE (syncTerm)-[:SYNONYM]->(importTerm)
CREATE (importTerm)-[:SYNONYM]->(migrationTerm)
CREATE (syncTerm)-[:MAPS_TO]->(etl)
CREATE (importTerm)-[:MAPS_TO]->(etl)
CREATE (migrationTerm)-[:MAPS_TO]->(etl)
CREATE (etlTerm)-[:MAPS_TO]->(etl)
```

#### 6.3 图数据库集成组件

提供统一的图数据库访问接口，支持多种图数据库：

```java
/**
 * 图数据库服务接口
 */
public interface GraphDatabaseService {

    /**
     * 执行Cypher查询
     */
    Result execute(String query, Map<String, Object> parameters);

    /**
     * 执行批量查询
     */
    List<Result> executeBatch(List<String> queries);

    /**
     * 开启事务
     */
    Transaction beginTransaction();

    /**
     * 获取节点
     */
    Node getNode(long nodeId);

    /**
     * 创建节点
     */
    Node createNode(Map<String, Object> properties, String... labels);

    /**
     * 创建关系
     */
    Relationship createRelationship(
        Node startNode,
        Node endNode,
        String relationshipType,
        Map<String, Object> properties
    );
}

/**
 * Neo4j实现
 */
@Service
public class Neo4jGraphDatabaseService implements GraphDatabaseService {

    @Autowired
    private Driver neo4jDriver;

    @Override
    public Result execute(String query, Map<String, Object> parameters) {
        try (Session session = neo4jDriver.session()) {
            org.neo4j.driver.Result result = session.run(query, parameters);
            return convertToResult(result);
        }
    }

    @Override
    public Transaction beginTransaction() {
        Session session = neo4jDriver.session();
        org.neo4j.driver.Transaction tx = session.beginTransaction();
        return new Neo4jTransaction(tx, session);
    }

    // 其他方法实现...
}

/**
 * 知识图谱仓库 - 提供高层次的图操作API
 */
@Repository
public class KnowledgeGraphRepository {

    @Autowired
    private GraphDatabaseService graphDB;

    /**
     * 添加数据源到图谱
     */
    public void addDataSource(DataSource dataSource) {
        String query = """
            MERGE (ds:DataSource {name: $name})
            SET ds.type = $type,
                ds.category = $category,
                ds.defaultPort = $port,
                ds.protocol = $protocol
            """;

        Map<String, Object> params = Map.of(
            "name", dataSource.getName(),
            "type", dataSource.getType(),
            "category", dataSource.getCategory(),
            "port", dataSource.getDefaultPort(),
            "protocol", dataSource.getProtocol()
        );

        graphDB.execute(query, params);
    }

    /**
     * 添加插件到图谱
     */
    public void addPlugin(Plugin plugin) {
        String query = """
            MERGE (p:Plugin {id: $id})
            SET p.name = $name,
                p.version = $version,
                p.category = $category,
                p.enabled = $enabled,
                p.performanceScore = $perfScore,
                p.reliabilityScore = $reliScore
            """;

        Map<String, Object> params = Map.of(
            "id", plugin.getId(),
            "name", plugin.getName(),
            "version", plugin.getVersion(),
            "category", plugin.getCategory(),
            "enabled", plugin.isEnabled(),
            "perfScore", plugin.getPerformanceScore(),
            "reliScore", plugin.getReliabilityScore()
        );

        graphDB.execute(query, params);
    }

    /**
     * 建立数据源与插件的支持关系
     */
    public void linkDataSourceToPlugin(String dataSourceName, String pluginId) {
        String query = """
            MATCH (ds:DataSource {name: $dsName})
            MATCH (p:Plugin {id: $pluginId})
            MERGE (ds)-[:SUPPORTED_BY]->(p)
            """;

        graphDB.execute(query, Map.of(
            "dsName", dataSourceName,
            "pluginId", pluginId
        ));
    }

    /**
     * 记录成功案例
     */
    public void recordSuccessCase(SuccessCase successCase) {
        String query = """
            CREATE (sc:SuccessCase {
                id: $caseId,
                timestamp: $timestamp,
                processingType: $processingType,
                duration: $duration,
                recordCount: $recordCount
            })

            WITH sc
            MATCH (source:DataSource {name: $sourceName})
            MATCH (target:DataSource {name: $targetName})
            CREATE (sc)-[:FROM_SOURCE]->(source)
            CREATE (sc)-[:TO_TARGET]->(target)

            WITH sc
            UNWIND $pluginIds as pluginId
            MATCH (p:Plugin {id: pluginId})
            CREATE (sc)-[:USED_PLUGIN]->(p)

            RETURN sc
            """;

        Map<String, Object> params = Map.of(
            "caseId", successCase.getId(),
            "timestamp", successCase.getTimestamp(),
            "processingType", successCase.getProcessingType(),
            "duration", successCase.getDuration(),
            "recordCount", successCase.getRecordCount(),
            "sourceName", successCase.getSourceName(),
            "targetName", successCase.getTargetName(),
            "pluginIds", successCase.getPluginIds()
        );

        graphDB.execute(query, params);
    }

    /**
     * 查找相似案例
     */
    public List<SuccessCase> findSimilarCases(
            String sourceType,
            String targetType,
            String processingType,
            double similarityThreshold) {

        String query = """
            MATCH (sc:SuccessCase)-[:FROM_SOURCE]->(source:DataSource {name: $sourceType})
            MATCH (sc)-[:TO_TARGET]->(target:DataSource {name: $targetType})
            WHERE sc.processingType = $processingType

            // 计算案例质量分数
            WITH sc,
                 sc.duration as duration,
                 sc.recordCount as recordCount
            ORDER BY recordCount DESC, duration ASC
            LIMIT 20

            RETURN sc, duration, recordCount
            """;

        Result result = graphDB.execute(query, Map.of(
            "sourceType", sourceType,
            "targetType", targetType,
            "processingType", processingType
        ));

        return convertToSuccessCases(result);
    }
}
```

#### 6.4 知识图谱初始化

系统启动时自动初始化知识图谱：

```java
/**
 * 知识图谱初始化器
 */
@Component
public class KnowledgeGraphInitializer {

    @Autowired
    private KnowledgeGraphRepository graphRepo;

    @Autowired
    private PluginRegistry pluginRegistry;

    @Autowired
    private DataSourceRegistry dataSourceRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeKnowledgeGraph() {
        logger.info("Initializing knowledge graph...");

        try {
            // 1. 清空现有数据（可选，仅用于开发环境）
            if (isDevMode()) {
                clearGraph();
            }

            // 2. 初始化数据源本体
            initializeDataSources();

            // 3. 初始化插件本体
            initializePlugins();

            // 4. 建立数据源与插件的关系
            linkDataSourcesAndPlugins();

            // 5. 初始化处理模式
            initializeProcessPatterns();

            // 6. 初始化质量规则
            initializeQualityRules();

            // 7. 初始化同义词和术语
            initializeTerminology();

            logger.info("Knowledge graph initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize knowledge graph", e);
            throw new KnowledgeGraphInitializationException(
                "Knowledge graph initialization failed", e);
        }
    }

    private void initializeDataSources() {
        List<DataSource> dataSources = dataSourceRegistry.getAllDataSources();

        for (DataSource ds : dataSources) {
            graphRepo.addDataSource(ds);
        }

        logger.info("Initialized {} data sources", dataSources.size());
    }

    private void initializePlugins() {
        List<Plugin> plugins = pluginRegistry.getAllPlugins();

        for (Plugin plugin : plugins) {
            graphRepo.addPlugin(plugin);

            // 添加插件的能力
            for (String capability : plugin.getCapabilities()) {
                graphRepo.linkPluginToCapability(plugin.getId(), capability);
            }
        }

        logger.info("Initialized {} plugins", plugins.size());
    }

    private void linkDataSourcesAndPlugins() {
        // 通过插件的元数据建立关系
        List<Plugin> plugins = pluginRegistry.getAllPlugins();

        for (Plugin plugin : plugins) {
            for (String dataSourceName : plugin.getSupportedDataSources()) {
                graphRepo.linkDataSourceToPlugin(dataSourceName, plugin.getId());
            }
        }
    }

    // 其他初始化方法...
}
```

#### 6.5 实体抽取方案对比与选择

在知识图谱增强的意图理解中，实体抽取是关键的第一步（Line 676: `List<Entity> entities = entityExtractor.extract(userRequest)`）。TIS支持多种实体抽取方案，每种方案都有其适用场景。

##### 6.5.1 方案对比

| 维度 | 方案A：本地预训练模型 | 方案B：远程大模型（推荐） | 方案C：混合方案 |
|------|---------------------|------------------------|---------------|
| **准确率** | ⭐⭐⭐ 依赖训练数据质量 | ⭐⭐⭐⭐⭐ 通用能力强，理解上下文 | ⭐⭐⭐⭐⭐ 结合两者优势 |
| **领域适配** | ⭐⭐ 需要领域数据微调 | ⭐⭐⭐⭐ 通过提示词即可适配 | ⭐⭐⭐⭐ 提示词+词典双保险 |
| **部署成本** | ⭐⭐ 需要模型文件和推理环境 | ⭐⭐⭐⭐⭐ 无需本地部署 | ⭐⭐⭐⭐ 仅需API调用 |
| **响应速度** | ⭐⭐⭐⭐⭐ 本地推理快（<100ms） | ⭐⭐⭐ 网络调用有延迟（~500ms） | ⭐⭐⭐⭐ 缓存优化后接近本地 |
| **维护成本** | ⭐⭐ 需要定期更新模型 | ⭐⭐⭐⭐⭐ 无需维护 | ⭐⭐⭐ 仅维护词典 |
| **灵活性** | ⭐⭐ 固定抽取规则 | ⭐⭐⭐⭐⭐ 可动态调整抽取策略 | ⭐⭐⭐⭐ 灵活性高 |
| **同义词处理** | ⭐⭐ 需要维护同义词词典 | ⭐⭐⭐⭐⭐ 自动理解同义词（pg→PostgreSQL） | ⭐⭐⭐⭐⭐ 双重保障 |
| **Token消耗** | ⭐⭐⭐⭐⭐ 无消耗 | ⭐⭐⭐ 每次调用消耗Token | ⭐⭐⭐ 降级时节省成本 |
| **推荐指数** | ⭐⭐⭐ 适合离线场景 | ⭐⭐⭐⭐⭐ 适合实时交互 | ⭐⭐⭐⭐ 生产环境最佳 |

**推荐策略：**
- **开发/测试环境**：使用方案B（远程大模型），快速迭代
- **生产环境**：使用方案C（混合方案），兼顾性能和可用性
- **离线批处理**：使用方案A（本地模型），降低成本

##### 6.5.2 推荐方案：基于大模型的实体抽取器

**核心优势：**
1. **自动同义词标准化**：自动识别 `pg/postgres` → `PostgreSQL`，`ck/clickhouse` → `ClickHouse`
2. **上下文理解**：理解"实时的"表示Streaming模式，"同步"表示ETL模式
3. **零维护成本**：无需训练模型或维护复杂的NER规则
4. **动态扩展**：新增数据源类型时，只需更新提示词即可

**实现代码：**

```java
/**
 * 基于大模型的实体抽取器
 * 解决同义词匹配问题的关键组件
 */
@Component
public class LLMBasedEntityExtractor implements EntityExtractor {

    @Autowired
    private LLMProvider llmProvider;

    @Autowired
    private AgentContext context;

    @Override
    public List<Entity> extract(String userRequest) {
        // 构建实体抽取的提示词
        String prompt = buildEntityExtractionPrompt(userRequest);

        // 定义输出的 JSON Schema
        JsonSchema schema = JsonSchema.create(getEntityExtractionSchema());

        // 调用大模型
        LLMProvider.LLMResponse response = llmProvider.chatJson(
            context,
            new UserPrompt("Identifying entities in your request...", prompt),
            Collections.singletonList(buildSystemPrompt()),
            schema
        );

        if (!response.isSuccess()) {
            throw new IllegalStateException("LLM entity extraction failed: "
                + response.getErrorMessage());
        }

        // 解析大模型返回的实体列表
        JSONObject result = response.getJsonContent();
        return parseEntities(result.getJSONArray("entities"));
    }

    /**
     * 构建实体抽取提示词
     * 关键：提供详细的同义词映射表
     */
    private String buildEntityExtractionPrompt(String userRequest) {
        return String.format("""
            ## Task
            Extract key entities from the user's data integration request.

            ## User Input
            %s

            ## Entity Types to Identify

            ### 1. DataSource Entity (数据源实体)
            Identify database types with their aliases:

            **Relational Databases:**
            - MySQL → normalizedName: "MySQL"
              Aliases: mysql, MySQL
            - PostgreSQL → normalizedName: "PostgreSQL"
              Aliases: pg, postgres, postgresql, pgsql
            - Oracle → normalizedName: "Oracle"
              Aliases: oracle, ora
            - SQLServer → normalizedName: "SQLServer"
              Aliases: mssql, sqlserver, sql-server

            **NoSQL Databases:**
            - ElasticSearch → normalizedName: "ElasticSearch"
              Aliases: es, elastic, elasticsearch
            - MongoDB → normalizedName: "MongoDB"
              Aliases: mongo, mongodb
            - Redis → normalizedName: "Redis"
              Aliases: redis
            - ClickHouse → normalizedName: "ClickHouse"
              Aliases: ck, clickhouse

            **Big Data:**
            - Doris → normalizedName: "Doris"
              Aliases: doris, apache-doris
            - StarRocks → normalizedName: "StarRocks"
              Aliases: sr, starrocks
            - Hive → normalizedName: "Hive"
              Aliases: hive
            - Paimon → normalizedName: "Paimon"
              Aliases: paimon

            **Message Queue:**
            - Kafka → normalizedName: "Kafka"
              Aliases: kafka

            ### 2. Table Entity (表实体)
            - Table names: e.g., user, order, base
            - Table patterns: e.g., user* (prefix match), *_log (suffix match)
            - Exclude phrases: e.g., "除AA、BB表以外" should be extracted as exclusion pattern

            ### 3. Operation Type (操作类型)
            - 同步/导入/迁移 → normalizedName: "ETL"
            - 实时同步/流处理 → normalizedName: "Streaming"
            - 增量同步/CDC → normalizedName: "CDC"

            ### 4. Configuration Parameters (配置参数)
            - Connection info: host, port, database
            - Performance params: batch size, concurrency

            ## Extraction Requirements
            1. Identify ALL relevant entities including synonyms and abbreviations
            2. Standardize entity names to their normalizedName (CRITICAL for synonym matching)
            3. Mark confidence level for each entity (0-1)
            4. Extract relationships between entities (e.g., "MySQL → Doris" indicates source and target)
            5. If entity is ambiguous, list all possible interpretations

            ## Output Format
            Strictly follow the JSON Schema output format.
            """, userRequest);
    }

    /**
     * 系统提示词
     */
    private String buildSystemPrompt() {
        return """
            You are an entity recognition expert for the TIS data integration platform.
            You are proficient in identifying various databases, data sources, and their aliases/abbreviations.
            Your task is to accurately identify entities from user input and standardize them to
            the formats that TIS platform can recognize.

            IMPORTANT: Always use the normalized standard names (e.g., PostgreSQL not pg)
            in the 'normalizedName' field to ensure knowledge graph matching.
            """;
    }

    /**
     * 定义实体抽取的 JSON Schema
     */
    private String getEntityExtractionSchema() {
        return """
            {
              "entities": [
                {
                  "name": "Entity name as it appears in user input, string type",
                  "type": "Entity type, enum: DataSource|Table|Operation|Parameter",
                  "originalText": "Original text from user input, string type",
                  "normalizedName": "Standardized name (e.g., pg→PostgreSQL), string type, REQUIRED",
                  "aliases": "List of synonyms, array type, optional",
                  "confidence": "Recognition confidence, number type, 0-1",
                  "attributes": "Entity attributes, object type, optional"
                }
              ],
              "relations": [
                {
                  "source": "Source entity name, string type",
                  "relation": "Relation type, e.g., source_to_target, belongs_to",
                  "target": "Target entity name, string type"
                }
              ]
            }
            """;
    }

    /**
     * 解析大模型返回的实体
     * 关键：使用 normalizedName 字段作为标准名称
     */
    private List<Entity> parseEntities(JSONArray entitiesArray) {
        List<Entity> entities = new ArrayList<>();

        for (int i = 0; i < entitiesArray.size(); i++) {
            JSONObject entityJson = entitiesArray.getJSONObject(i);

            Entity entity = new Entity();
            // 使用标准化名称，解决同义词匹配问题
            entity.setName(entityJson.getString("normalizedName"));
            entity.setType(EntityType.valueOf(entityJson.getString("type")));
            entity.setOriginalText(entityJson.getString("originalText"));
            entity.setConfidence(entityJson.getDoubleValue("confidence"));

            // 保存别名信息，用于知识图谱多重匹配
            if (entityJson.containsKey("aliases")) {
                entity.setAliases(entityJson.getJSONArray("aliases")
                    .toJavaList(String.class));
            }

            // 保存属性
            if (entityJson.containsKey("attributes")) {
                entity.setAttributes(entityJson.getJSONObject("attributes"));
            }

            entities.add(entity);
        }

        return entities;
    }
}
```

**使用示例：**

```java
// 示例1：简称识别
// 用户输入："从pg的user表同步到ck，要实时的"
// LLM输出：
{
  "entities": [
    {
      "name": "pg",
      "type": "DataSource",
      "originalText": "pg",
      "normalizedName": "PostgreSQL",  // 自动标准化！
      "aliases": ["pg", "postgres", "postgresql"],
      "confidence": 0.95
    },
    {
      "name": "user",
      "type": "Table",
      "originalText": "user表",
      "normalizedName": "user",
      "confidence": 1.0
    },
    {
      "name": "ck",
      "type": "DataSource",
      "originalText": "ck",
      "normalizedName": "ClickHouse",  // 自动标准化！
      "aliases": ["ck", "clickhouse"],
      "confidence": 0.95
    },
    {
      "name": "实时",
      "type": "Operation",
      "originalText": "实时的",
      "normalizedName": "Streaming",  // 语义理解！
      "confidence": 0.9
    }
  ],
  "relations": [
    {
      "source": "PostgreSQL",
      "relation": "source_to_target",
      "target": "ClickHouse"
    }
  ]
}

// 示例2：复杂表选择
// 用户输入："MySQL中除了AA、BB表以外的所有表导入到Doris"
// LLM输出：
{
  "entities": [
    {
      "name": "MySQL",
      "type": "DataSource",
      "originalText": "MySQL",
      "normalizedName": "MySQL",
      "confidence": 1.0
    },
    {
      "name": "exclude_pattern",
      "type": "Table",
      "originalText": "除了AA、BB表以外的所有表",
      "normalizedName": "*",
      "attributes": {
        "exclude": ["AA", "BB"],
        "pattern": "all_except"
      },
      "confidence": 0.9
    },
    {
      "name": "Doris",
      "type": "DataSource",
      "originalText": "Doris",
      "normalizedName": "Doris",
      "confidence": 1.0
    },
    {
      "name": "导入",
      "type": "Operation",
      "originalText": "导入",
      "normalizedName": "ETL",
      "confidence": 0.95
    }
  ]
}
```

**关键优势对比：**

| 场景 | 传统NER方法 | 大模型方法 |
|------|-----------|-----------|
| "pg" → "PostgreSQL" | ❌ 需要维护同义词词典 | ✅ 自动理解和标准化 |
| "实时的" → "Streaming" | ❌ 无法理解语义 | ✅ 语义理解 |
| "除AA、BB表以外" | ❌ 难以提取复杂模式 | ✅ 提取为结构化属性 |
| 新数据源支持 | ❌ 需要重新训练模型 | ✅ 更新提示词即可 |
| 多语言混用 | ❌ 需要多个模型 | ✅ 统一处理 |

##### 6.5.3 备选方案：基于字典和模式的实体抽取器

对于不希望依赖外部LLM服务的场景，TIS提供基于字典和正则表达式的传统实体抽取方法：

```java
/**
 * 实体抽取器 - 从用户输入中识别领域实体
 */
@Component
public class EntityExtractor {

    @Autowired
    private KnowledgeGraphRepository graphRepo;

    /**
     * 从用户请求中抽取实体
     */
    public List<Entity> extract(String userRequest) {
        List<Entity> entities = new ArrayList<>();

        // 1. 基于字典的实体识别
        entities.addAll(extractByDictionary(userRequest));

        // 2. 基于模式的实体识别
        entities.addAll(extractByPattern(userRequest));

        // 3. 去重和合并
        return deduplicateAndMerge(entities);
    }

    /**
     * 基于字典的实体识别
     */
    private List<Entity> extractByDictionary(String text) {
        List<Entity> entities = new ArrayList<>();

        // 从知识图谱获取所有已知实体名称
        List<String> knownEntities = graphRepo.getAllEntityNames();

        for (String entityName : knownEntities) {
            if (text.contains(entityName)) {
                Entity entity = new Entity();
                entity.setName(entityName);
                entity.setType(inferEntityType(entityName));
                entity.setConfidence(1.0);
                entities.add(entity);
            }
        }

        return entities;
    }

    /**
     * 基于模式的实体识别
     */
    private List<Entity> extractByPattern(String text) {
        List<Entity> entities = new ArrayList<>();

        // JDBC URL模式
        Pattern jdbcPattern = Pattern.compile(
            "jdbc:([a-z]+)://([^:/]+):?(\\d+)?/([a-zA-Z0-9_]+)"
        );
        Matcher matcher = jdbcPattern.matcher(text);
        if (matcher.find()) {
            String dbType = matcher.group(1);
            entities.add(new Entity(
                capitalizeFirst(dbType),
                EntityType.DATA_SOURCE,
                0.9
            ));
        }

        // 表名模式
        Pattern tablePattern = Pattern.compile(
            "表\\s*[：:]?\\s*([a-zA-Z0-9_]+)"
        );
        matcher = tablePattern.matcher(text);
        while (matcher.find()) {
            entities.add(new Entity(
                matcher.group(1),
                EntityType.TABLE,
                0.8
            ));
        }

        return entities;
    }

    private EntityType inferEntityType(String entityName) {
        // 通过查询图谱推断实体类型
        String query = """
            MATCH (n)
            WHERE n.name = $name OR n.id = $name
            RETURN labels(n)[0] as entityType
            LIMIT 1
            """;

        Result result = graphRepo.executeQuery(query, Map.of("name", entityName));
        if (result.hasNext()) {
            String typeStr = (String) result.next().get("entityType");
            return EntityType.valueOf(typeStr.toUpperCase());
        }

        return EntityType.UNKNOWN;
    }
}
```

##### 6.5.4 推荐方案：混合降级策略

在生产环境中，推荐采用混合方案，以LLM为主，字典匹配为备用，实现智能降级：

```java
/**
 * 混合实体抽取器 - 智能降级策略
 * 优先使用LLM，当LLM不可用时自动降级到字典匹配
 */
@Component
public class HybridEntityExtractor implements EntityExtractor {

    @Autowired
    private LLMBasedEntityExtractor llmExtractor;

    @Autowired
    private DictionaryBasedEntityExtractor dictExtractor;

    @Autowired(required = false)
    private LLMHealthChecker healthChecker;

    private static final Logger logger = LoggerFactory.getLogger(HybridEntityExtractor.class);

    // 缓存配置
    private final Cache<String, List<Entity>> extractionCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();

    // 降级统计
    private final AtomicInteger llmSuccessCount = new AtomicInteger(0);
    private final AtomicInteger fallbackCount = new AtomicInteger(0);

    @Override
    public List<Entity> extract(String userRequest) {
        // 1. 检查缓存
        List<Entity> cached = extractionCache.getIfPresent(userRequest);
        if (cached != null) {
            logger.debug("Entity extraction cache hit for request: {}", userRequest);
            return cached;
        }

        List<Entity> entities;

        // 2. 尝试使用LLM抽取
        try {
            // 健康检查（可选）
            if (healthChecker != null && !healthChecker.isLLMAvailable()) {
                logger.warn("LLM health check failed, fallback to dictionary extraction");
                entities = fallbackToDictionary(userRequest);
            } else {
                entities = llmExtractor.extract(userRequest);
                llmSuccessCount.incrementAndGet();
                logger.debug("LLM entity extraction succeeded");
            }

        } catch (LLMTimeoutException e) {
            logger.warn("LLM timeout after {}ms, fallback to dictionary extraction",
                e.getTimeoutMillis());
            entities = fallbackToDictionary(userRequest);

        } catch (LLMUnavailableException e) {
            logger.warn("LLM service unavailable: {}, fallback to dictionary extraction",
                e.getMessage());
            entities = fallbackToDictionary(userRequest);

        } catch (LLMQuotaExceededException e) {
            logger.warn("LLM quota exceeded, fallback to dictionary extraction");
            entities = fallbackToDictionary(userRequest);

        } catch (Exception e) {
            logger.error("LLM entity extraction failed with unexpected error, fallback", e);
            entities = fallbackToDictionary(userRequest);
        }

        // 3. 结果增强：合并两种方法的结果
        if (shouldEnhanceWithDictionary(entities)) {
            List<Entity> dictEntities = dictExtractor.extract(userRequest);
            entities = mergeResults(entities, dictEntities);
            logger.debug("Enhanced LLM results with dictionary extraction");
        }

        // 4. 缓存结果
        extractionCache.put(userRequest, entities);

        return entities;
    }

    /**
     * 降级到字典匹配方法
     */
    private List<Entity> fallbackToDictionary(String userRequest) {
        fallbackCount.incrementAndGet();
        logFallbackStatistics();
        return dictExtractor.extract(userRequest);
    }

    /**
     * 判断是否需要用字典方法增强LLM结果
     */
    private boolean shouldEnhanceWithDictionary(List<Entity> llmEntities) {
        // 如果LLM没有识别到任何实体，或者置信度都很低
        if (llmEntities.isEmpty()) {
            return true;
        }

        double avgConfidence = llmEntities.stream()
            .mapToDouble(Entity::getConfidence)
            .average()
            .orElse(0.0);

        return avgConfidence < 0.7; // 平均置信度低于70%时启用增强
    }

    /**
     * 合并LLM和字典抽取的结果
     */
    private List<Entity> mergeResults(List<Entity> llmEntities, List<Entity> dictEntities) {
        Map<String, Entity> mergedMap = new HashMap<>();

        // 优先保留LLM结果（因为LLM能做标准化）
        for (Entity entity : llmEntities) {
            mergedMap.put(entity.getName().toLowerCase(), entity);
        }

        // 补充字典结果中LLM没有识别到的实体
        for (Entity dictEntity : dictEntities) {
            String key = dictEntity.getName().toLowerCase();
            if (!mergedMap.containsKey(key)) {
                mergedMap.put(key, dictEntity);
            }
        }

        return new ArrayList<>(mergedMap.values());
    }

    /**
     * 记录降级统计信息
     */
    private void logFallbackStatistics() {
        int success = llmSuccessCount.get();
        int fallback = fallbackCount.get();
        int total = success + fallback;

        if (total > 0 && total % 100 == 0) {
            double fallbackRate = (double) fallback / total * 100;
            logger.info("Entity extraction statistics - Total: {}, LLM: {}, Fallback: {} ({:.2f}%)",
                total, success, fallback, fallbackRate);

            // 如果降级率过高，发出告警
            if (fallbackRate > 30) {
                logger.warn("High fallback rate detected: {:.2f}%, please check LLM service",
                    fallbackRate);
            }
        }
    }

    /**
     * 获取降级统计信息
     */
    public FallbackStatistics getStatistics() {
        return new FallbackStatistics(
            llmSuccessCount.get(),
            fallbackCount.get()
        );
    }

    /**
     * 降级统计数据类
     */
    public static class FallbackStatistics {
        private final int llmSuccessCount;
        private final int fallbackCount;

        public FallbackStatistics(int llmSuccessCount, int fallbackCount) {
            this.llmSuccessCount = llmSuccessCount;
            this.fallbackCount = fallbackCount;
        }

        public double getFallbackRate() {
            int total = llmSuccessCount + fallbackCount;
            return total > 0 ? (double) fallbackCount / total : 0.0;
        }

        public int getTotalCount() {
            return llmSuccessCount + fallbackCount;
        }

        // Getters
        public int getLlmSuccessCount() { return llmSuccessCount; }
        public int getFallbackCount() { return fallbackCount; }
    }
}

/**
 * LLM健康检查器（可选组件）
 */
@Component
public class LLMHealthChecker {

    @Autowired
    private LLMProvider llmProvider;

    private volatile boolean lastCheckResult = true;
    private volatile long lastCheckTime = 0;
    private static final long CHECK_INTERVAL = 60_000; // 1分钟检查一次

    /**
     * 检查LLM服务是否可用
     */
    public boolean isLLMAvailable() {
        long now = System.currentTimeMillis();

        // 如果距离上次检查不到1分钟，直接返回缓存结果
        if (now - lastCheckTime < CHECK_INTERVAL) {
            return lastCheckResult;
        }

        try {
            // 发送一个简单的测试请求
            LLMProvider.LLMResponse response = llmProvider.chat(
                "test",
                new UserPrompt("Health check", "Echo: OK"),
                Collections.emptyList(),
                2000 // 2秒超时
            );

            lastCheckResult = response.isSuccess();
            lastCheckTime = now;
            return lastCheckResult;

        } catch (Exception e) {
            lastCheckResult = false;
            lastCheckTime = now;
            return false;
        }
    }
}

/**
 * LLM异常定义
 */
public class LLMTimeoutException extends RuntimeException {
    private final long timeoutMillis;

    public LLMTimeoutException(long timeoutMillis) {
        super("LLM request timeout after " + timeoutMillis + "ms");
        this.timeoutMillis = timeoutMillis;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }
}

public class LLMUnavailableException extends RuntimeException {
    public LLMUnavailableException(String message) {
        super(message);
    }

    public LLMUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class LLMQuotaExceededException extends RuntimeException {
    public LLMQuotaExceededException(String message) {
        super(message);
    }
}
```

**混合方案的优势：**

1. **高可用性**：LLM服务异常时自动降级，不影响系统运行
2. **性能优化**：通过缓存机制减少重复调用
3. **智能增强**：低置信度时自动合并字典结果
4. **可观测性**：统计降级率，及时发现LLM服务问题
5. **灵活配置**：可通过健康检查提前预测并避免超时

**配置示例：**

```yaml
tis:
  ai-agent:
    entity-extraction:
      # 实体抽取器类型：llm, dictionary, hybrid
      type: hybrid

      # LLM配置
      llm:
        timeout: 5000  # 超时时间(ms)
        retry: 2       # 重试次数

      # 缓存配置
      cache:
        enabled: true
        max-size: 1000
        expire-minutes: 10

      # 降级配置
      fallback:
        enabled: true
        health-check: true
        alert-threshold: 30  # 降级率超过30%时告警
```

**使用建议：**

- **生产环境**：使用混合方案，确保高可用性
- **开发/测试环境**：直接使用LLM方案，快速迭代
- **离线环境**：使用字典方案，完全本地化部署
- **成本敏感场景**：配置降级策略，控制LLM调用次数

这个知识图谱架构为TIS AI Agent提供了强大的知识支撑，使其能够进行智能推理、案例匹配和最佳实践推荐。

### 7. 自学习引擎

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

#### 7.1 基于知识图谱的智能学习引擎（强烈推荐）

通过知识图谱存储和检索历史案例，可以实现更智能的案例匹配和学习：

```java
/**
 * 基于知识图谱的学习引擎
 */
public class GraphBasedLearningEngine extends AgentLearningEngine {

    @Autowired
    private KnowledgeGraphRepository graphRepo;

    @Autowired
    private GraphDatabaseService graphDB;

    /**
     * 记录成功案例到知识图谱
     */
    @Override
    public void recordSuccessfulExecution(
            UserIntent intent,
            TaskPlan plan,
            ExecutionResult result) {

        // 调用父类方法保存到传统数据库
        super.recordSuccessfulExecution(intent, plan, result);

        // 额外保存到知识图谱
        saveToKnowledgeGraph(intent, plan, result);
    }

    /**
     * 将成功案例保存到知识图谱
     */
    private void saveToKnowledgeGraph(
            UserIntent intent,
            TaskPlan plan,
            ExecutionResult result) {

        String query = """
            CREATE (sc:SuccessCase {
                id: $caseId,
                timestamp: $timestamp,
                processingType: $processingType,
                duration: $duration,
                recordCount: $recordCount,
                throughput: $throughput,
                successRate: $successRate
            })

            // 关联数据源
            WITH sc
            MATCH (source:DataSource {name: $sourceName})
            MATCH (target:DataSource {name: $targetName})
            CREATE (sc)-[:FROM_SOURCE]->(source)
            CREATE (sc)-[:TO_TARGET]->(target)

            // 关联使用的插件
            WITH sc
            UNWIND $pluginIds as pluginId
            MATCH (p:Plugin {id: pluginId})
            CREATE (sc)-[:USED_PLUGIN]->(p)

            // 关联使用的能力
            WITH sc
            UNWIND $capabilityIds as capId
            MATCH (cap:Capability {id: capId})
            CREATE (sc)-[:USED_CAPABILITY]->(cap)

            // 关联处理模式
            WITH sc
            MATCH (pattern:ProcessPattern {type: $processingType})
            CREATE (sc)-[:FOLLOWS_PATTERN]->(pattern)

            // 更新模式的成功率
            WITH pattern, sc
            SET pattern.totalCases = COALESCE(pattern.totalCases, 0) + 1,
                pattern.successRate = (
                    COALESCE(pattern.successRate * pattern.totalCases, 0) + $successRate
                ) / (pattern.totalCases + 1)

            RETURN sc
            """;

        Map<String, Object> params = Map.of(
            "caseId", UUID.randomUUID().toString(),
            "timestamp", result.getTimestamp(),
            "processingType", intent.getProcessingType().name(),
            "duration", result.getDuration(),
            "recordCount", result.getRecordCount(),
            "throughput", result.getThroughput(),
            "successRate", result.getSuccessRate(),
            "sourceName", intent.getDataSources().get(0).getType(),
            "targetName", intent.getDataSources().get(1).getType(),
            "pluginIds", plan.getUsedPluginIds(),
            "capabilityIds", plan.getUsedCapabilityIds()
        );

        graphDB.execute(query, params);
    }

    /**
     * 通过图相似度查找相似案例
     * 使用多维度相似度计算
     */
    @Override
    public Optional<TaskPlan> findSimilarPlan(UserIntent newIntent) {
        List<SuccessCase> graphCases = findSimilarCasesFromGraph(newIntent);

        if (!graphCases.isEmpty()) {
            SuccessCase bestCase = selectBestCase(graphCases);
            return Optional.of(adaptPlan(bestCase.getPlan(), newIntent));
        }

        // 如果图谱中没有找到，回退到传统方法
        return super.findSimilarPlan(newIntent);
    }

    /**
     * 从知识图谱查找相似案例
     * 综合考虑数据源、处理类型、能力等多个维度
     */
    private List<SuccessCase> findSimilarCasesFromGraph(UserIntent intent) {
        String query = """
            // 匹配数据源组合相同的案例
            MATCH (sc:SuccessCase)-[:FROM_SOURCE]->(source:DataSource {name: $sourceName})
            MATCH (sc)-[:TO_TARGET]->(target:DataSource {name: $targetName})
            WHERE sc.processingType = $processingType

            // 计算能力相似度
            OPTIONAL MATCH (sc)-[:USED_CAPABILITY]->(cap:Capability)
            WHERE cap.id IN $requiredCapabilities
            WITH sc,
                 count(DISTINCT cap) as matchedCapabilities,
                 $requiredCapCount as requiredCapCount

            // 计算综合相似度分数
            WITH sc,
                 CASE
                     WHEN requiredCapCount > 0
                     THEN toFloat(matchedCapabilities) / requiredCapCount
                     ELSE 0.5
                 END as capabilitySimilarity,
                 sc.successRate as successRate,
                 sc.throughput as throughput

            // 综合评分：相似度 * 0.5 + 成功率 * 0.3 + 吞吐量归一化 * 0.2
            WITH sc,
                 capabilitySimilarity * 0.5 +
                 successRate * 0.3 +
                 (throughput / 10000.0) * 0.2 as totalScore

            WHERE capabilitySimilarity >= 0.6

            RETURN sc,
                   capabilitySimilarity,
                   totalScore
            ORDER BY totalScore DESC
            LIMIT 10
            """;

        List<String> requiredCaps = intent.getSuggestedCapabilities();

        Map<String, Object> params = Map.of(
            "sourceName", intent.getDataSources().get(0).getType(),
            "targetName", intent.getDataSources().get(1).getType(),
            "processingType", intent.getProcessingType().name(),
            "requiredCapabilities", requiredCaps,
            "requiredCapCount", requiredCaps.size()
        );

        Result result = graphDB.execute(query, params);
        return convertToSuccessCases(result);
    }

    /**
     * 从失败中学习 - 更新图谱中的关系权重
     */
    @Override
    public void recordFailure(UserIntent intent, TaskPlan plan, Exception error) {
        super.recordFailure(intent, plan, error);

        // 更新图谱中的失败统计
        String query = """
            CREATE (fc:FailureCase {
                id: $caseId,
                timestamp: $timestamp,
                processingType: $processingType,
                errorType: $errorType,
                errorMessage: $errorMessage
            })

            // 关联失败的插件组合
            WITH fc
            UNWIND $pluginIds as pluginId
            MATCH (p:Plugin {id: pluginId})
            CREATE (fc)-[:FAILED_WITH_PLUGIN]->(p)

            // 如果是插件冲突导致的失败，创建冲突关系
            WITH fc, $pluginIds as pIds
            WHERE size(pIds) >= 2 AND $errorType = 'PLUGIN_CONFLICT'
            MATCH (p1:Plugin {id: pIds[0]})
            MATCH (p2:Plugin {id: pIds[1]})
            MERGE (p1)-[conflict:CONFLICTS_WITH]->(p2)
            ON CREATE SET conflict.count = 1,
                          conflict.reason = $errorMessage
            ON MATCH SET conflict.count = conflict.count + 1

            RETURN fc
            """;

        Map<String, Object> params = Map.of(
            "caseId", UUID.randomUUID().toString(),
            "timestamp", System.currentTimeMillis(),
            "processingType", intent.getProcessingType().name(),
            "errorType", classifyError(error),
            "errorMessage", error.getMessage(),
            "pluginIds", plan.getUsedPluginIds()
        );

        graphDB.execute(query, params);
    }

    /**
     * 分析趋势和模式
     */
    public Map<String, Object> analyzeTrends(String processingType) {
        String query = """
            MATCH (sc:SuccessCase {processingType: $processingType})
            WHERE sc.timestamp > $startTime

            // 按时间窗口聚合
            WITH sc,
                 sc.timestamp - (sc.timestamp % $windowSize) as timeWindow

            // 统计每个时间窗口的指标
            WITH timeWindow,
                 avg(sc.duration) as avgDuration,
                 avg(sc.throughput) as avgThroughput,
                 avg(sc.successRate) as avgSuccessRate,
                 count(sc) as caseCount

            RETURN timeWindow,
                   avgDuration,
                   avgThroughput,
                   avgSuccessRate,
                   caseCount
            ORDER BY timeWindow DESC
            LIMIT 30
            """;

        long oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        long oneDayMillis = 24 * 60 * 60 * 1000;

        Result result = graphDB.execute(query, Map.of(
            "processingType", processingType,
            "startTime", oneMonthAgo,
            "windowSize", oneDayMillis
        ));

        return convertToTrendData(result);
    }

    /**
     * 推荐优化建议
     */
    public List<OptimizationSuggestion> getOptimizationSuggestions(
            UserIntent intent) {

        String query = """
            // 找到当前配置
            MATCH (currentSource:DataSource {name: $sourceName})
                  -[:SUPPORTED_BY]->(currentPlugin:Plugin)

            // 查找性能更好的替代插件
            MATCH (currentSource)-[:SUPPORTED_BY]->(altPlugin:Plugin)
                  -[:HAS_CAPABILITY]->(cap:Capability)
            WHERE altPlugin.id <> currentPlugin.id
              AND altPlugin.enabled = true

            // 比较历史性能
            OPTIONAL MATCH (altCase:SuccessCase)-[:USED_PLUGIN]->(altPlugin)
            WHERE altCase.processingType = $processingType

            OPTIONAL MATCH (currentCase:SuccessCase)-[:USED_PLUGIN]->(currentPlugin)
            WHERE currentCase.processingType = $processingType

            WITH altPlugin,
                 avg(altCase.throughput) as altThroughput,
                 avg(currentCase.throughput) as currentThroughput

            WHERE altThroughput > currentThroughput * 1.2

            RETURN altPlugin.id as pluginId,
                   altPlugin.name as pluginName,
                   altThroughput,
                   currentThroughput,
                   (altThroughput - currentThroughput) / currentThroughput as improvement
            ORDER BY improvement DESC
            LIMIT 5
            """;

        Result result = graphDB.execute(query, Map.of(
            "sourceName", intent.getDataSources().get(0).getType(),
            "processingType", intent.getProcessingType().name()
        ));

        return convertToSuggestions(result);
    }

    private String classifyError(Exception error) {
        String errorClass = error.getClass().getSimpleName();
        if (errorClass.contains("Conflict")) {
            return "PLUGIN_CONFLICT";
        } else if (errorClass.contains("Connection")) {
            return "CONNECTION_ERROR";
        } else if (errorClass.contains("Timeout")) {
            return "TIMEOUT";
        }
        return "UNKNOWN";
    }
}
```

**基于知识图谱的学习引擎优势：**

1. **多维度相似度**：综合考虑数据源、能力、性能等多个维度进行案例匹配
2. **实时更新**：每次执行都更新图谱，模式和关系权重动态调整
3. **趋势分析**：基于时间窗口聚合分析性能趋势
4. **智能推荐**：基于历史数据推荐性能更优的插件组合
5. **失败学习**：自动识别并记录冲突关系，避免重复错误
6. **知识传播**：通过图的连通性，知识可以在不同场景间传播

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
- [ ] **（知识图谱）搭建Neo4j环境，完成基础连接**
- [ ] **（知识图谱）定义核心本体模型（10-15个数据源，5-8个能力）**
- [ ] **（知识图谱）实现图数据库基础操作接口**

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
- [ ] **（知识图谱）初始化知识图谱Schema**
- [ ] **（知识图谱）实现基础的三元组检索功能**
- [ ] **（知识图谱）将现有插件元数据同步到图谱**
- [ ] **（知识图谱）实现GraphBasedCapabilityRegistry MVP**
- [ ] **（知识图谱）集成HanLP进行实体抽取**

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
- [ ] **（知识图谱）实现KnowledgeGraphEnhancedIntentAnalyzer**
- [ ] **（知识图谱）集成RAG流程到意图理解**
- [ ] **（知识图谱）实现GraphEnhancedCapabilityComposer**
- [ ] **（知识图谱）使用Dijkstra算法进行路径优化**
- [ ] **（知识图谱）实现能力冲突检测**
- [ ] **（知识图谱）扩展图谱覆盖到30+数据源**

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
- [ ] **（知识图谱）实现GraphBasedLearningEngine**
- [ ] **（知识图谱）建立成功/失败案例的图谱存储**
- [ ] **（知识图谱）实现基于图相似度的案例匹配**
- [ ] **（知识图谱）开发趋势分析和优化建议功能**
- [ ] **（知识图谱）实现图谱自动更新机制**
- [ ] **（知识图谱）性能优化：查询缓存、索引优化**
- [ ] **（知识图谱）接入Neo4j GDS图算法库**

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
- [ ] **（知识图谱）图谱高可用部署（Neo4j集群）**
- [ ] **（知识图谱）图谱备份和恢复策略**
- [ ] **（知识图谱）图谱数据质量监控**
- [ ] **（知识图谱）图谱查询性能监控和告警**
- [ ] **（知识图谱）开发图谱可视化工具**
- [ ] **（知识图谱）完善图谱文档和最佳实践**

### Phase 5：持续演进（长期）

**目标**：基于用户反馈持续改进

- [ ] 收集和分析用户反馈
- [ ] 扩展原子能力库
- [ ] 优化学习算法
- [ ] 支持更多数据源
- [ ] 提升并发处理能力
- [ ] 探索新的 AI 技术应用
- [ ] **（知识图谱）持续扩展本体覆盖范围**
- [ ] **（知识图谱）引入图神经网络（GNN）进行高级推理**
- [ ] **（知识图谱）探索多模态知识图谱（文本+代码+配置）**
- [ ] **（知识图谱）实现跨项目的知识共享机制**
- [ ] **（知识图谱）研究联邦学习在图谱更新中的应用**

### 知识图谱专项里程碑

为了确保知识图谱能力的顺利实施，制定专项里程碑：

**M1：图谱基础设施（Week 1-2）**
- 完成Neo4j安装和配置
- 建立开发/测试/生产环境
- 实现GraphDatabaseService接口
- 完成基础CRUD操作测试

**M2：本体建模（Week 3-4）**
- 完成核心实体和关系类型定义
- 创建Cypher schema脚本
- 导入初始数据（10-15个数据源）
- 验证图谱查询性能

**M3：RAG集成（Week 5-8）**
- 实现EntityExtractor
- 开发三元组检索逻辑
- 集成到IntentAnalyzer
- A/B测试对比传统方法

**M4：能力增强（Week 9-12）**
- 实现GraphBasedCapabilityRegistry
- 开发GraphEnhancedCapabilityComposer
- 集成图算法（Dijkstra）
- 性能基准测试

**M5：学习引擎（Week 13-16）**
- 实现GraphBasedLearningEngine
- 建立案例存储机制
- 开发相似度匹配算法
- 实现趋势分析功能

**M6：生产优化（Week 17-20）**
- 查询性能优化
- 实现缓存策略
- 高可用部署
- 监控告警系统

**M7：持续改进（Long-term）**
- 扩展本体覆盖
- 优化推理算法
- 引入新技术
- 知识质量提升

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

**实体抽取（推荐方案）**

- **主要方案：远程大模型（LLM）**
  - 优势：自动同义词标准化、零维护成本、高准确率
  - 适用场景：生产环境、开发测试环境
  - 实现：通过LLMProvider调用远程API（DeepSeek、通义千问等）
  - 参考实现：`LLMBasedEntityExtractor`（见第6.5.2节）

- **备选方案：HanLP + 自定义词典**
  - 优势：完全本地化、无网络依赖
  - 适用场景：离线环境、对外部依赖有限制的场景
  - 实现：基于HanLP的NER + 领域词典扩展
  - 参考实现：`DictionaryBasedEntityExtractor`（见第6.5.3节）

- **推荐方案：混合降级策略**
  - 优势：高可用性、智能降级
  - 适用场景：生产环境首选
  - 实现：LLM为主 + 字典为备用
  - 参考实现：`HybridEntityExtractor`（见第6.5.4节）

**其他NLP组件**

- 中文分词：jieba、HanLP（用于辅助文本预处理）
- 意图识别：通过LLM直接完成，无需单独的意图分类模型
- 同义词处理：由LLM在实体抽取阶段自动完成标准化

### 规则引擎
- Drools：用于复杂规则管理
- Easy Rules：轻量级规则引擎

### 机器学习框架
- DL4J：Java原生深度学习框架
- Smile：统计机器学习库

### 图数据库技术栈（新增）

**推荐方案：Neo4j**

```java
// Maven依赖
<dependency>
    <groupId>org.neo4j.driver</groupId>
    <artifactId>neo4j-java-driver</artifactId>
    <version>5.15.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-neo4j</artifactId>
</dependency>
```

**Neo4j特点：**
- 成熟稳定，社区活跃
- Cypher查询语言表达力强
- 内置图算法库（Neo4j GDS）
- 性能优异，支持数十亿节点
- 可视化工具完善（Neo4j Browser）
- 支持企业级高可用部署

**配置示例：**

```yaml
spring:
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: your-password
    connection:
      pool:
        max-size: 50
```

**备选方案对比：**

| 数据库 | 优势 | 劣势 | 适用场景 |
|--------|------|------|----------|
| **Neo4j** | 成熟稳定、查询性能好、生态完善 | 企业版收费 | **推荐首选** |
| JanusGraph | 开源免费、支持大规模图 | 社区不够活跃 | 超大规模场景 |
| ArangoDB | 多模型支持、灵活 | 图算法较少 | 需要文档+图的场景 |
| OrientDB | 集成度高、国产化友好 | 文档较少 | 对国产化有要求 |

### 图计算框架

**Neo4j Graph Data Science (GDS)**

```java
// Maven依赖
<dependency>
    <groupId>org.neo4j.gds</groupId>
    <artifactId>neo4j-graph-data-science</artifactId>
    <version>2.5.0</version>
</dependency>
```

**常用图算法：**
- **路径查找**：Dijkstra、A*、Yen's K-Shortest Paths
- **中心性**：PageRank、Betweenness Centrality
- **社区检测**：Louvain、Label Propagation
- **相似度**：Node Similarity、Jaccard Index
- **链接预测**：Adamic Adar、Common Neighbors

**使用示例：**

```cypher
// 使用Dijkstra算法找最短路径
CALL gds.shortestPath.dijkstra.stream({
    sourceNode: id(startNode),
    targetNode: id(endNode),
    relationshipWeightProperty: 'cost'
})
YIELD path, totalCost
RETURN path, totalCost
```

### 备选图计算框架

- **Apache TinkerPop/Gremlin**：通用图遍历语言
- **Apache Giraph**：适合超大规模图计算
- **GraphX (Spark)**：与Spark生态集成

### 实体抽取和NER

**推荐方案：HanLP + 自定义词典**

```java
<dependency>
    <groupId>com.hankcs</groupId>
    <artifactId>hanlp</artifactId>
    <version>portable-1.8.4</version>
</dependency>
```

**集成示例：**

```java
@Component
public class EntityExtractor {

    private StandardTokenizer tokenizer = new StandardTokenizer();

    public List<Entity> extract(String text) {
        List<Term> terms = tokenizer.segment(text);

        return terms.stream()
            .filter(term -> isEntity(term))
            .map(this::convertToEntity)
            .collect(Collectors.toList());
    }

    private boolean isEntity(Term term) {
        // 识别数据源名称、插件名等实体
        return term.nature == Nature.nz || // 专有名词
               term.nature == Nature.nt;   // 术语
    }
}
```

### 向量数据库（可选，用于语义搜索）

如果需要基于语义相似度进行意图匹配，可以引入向量数据库：

**推荐方案：Milvus或Qdrant**

```java
// Milvus Java SDK
<dependency>
    <groupId>io.milvus</groupId>
    <artifactId>milvus-sdk-java</artifactId>
    <version>2.3.4</version>
</dependency>
```

**使用场景：**
- 用户问题的语义相似度匹配
- 基于embedding的案例检索
- 跨语言的意图理解

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