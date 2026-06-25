# TIS Workshop 可行性分析

## 概述

本文档分析 TIS 构建类似 Palantir Workshop 低代码应用平台的可行性，基于现有的本体架构和插件机制进行评估。

## 目标定位

构建基于本体语义层的低代码应用平台，让用户能够：

1. **通过可视化方式构建应用** - 无需编写代码，通过拖拽和配置完成应用开发
2. **基于本体语义理解业务** - 应用自动理解数据结构、关系和约束
3. **动态执行业务操作** - 通过 Function 和 Action 实现业务逻辑
4. **快速响应业务变化** - 本体变更自动反映到应用层

## 可行性结论

### ✅ 完全可行

TIS 已经具备了构建 Workshop 的核心基础设施，相比从零开始有显著优势。

**核心原因**：
1. 完整的本体模型已经落地（ObjectType、Property、Linker、SharedProperty、Glossary）
2. 强大的插件机制本质上就是元数据驱动的 UI 系统
3. 成功的应用案例（ChatBI）已经验证了"本体 → 应用"的技术路径
4. 微前端架构天然支持模块化扩展

### 风险评估

**技术风险**：低
- 核心技术栈已验证
- 不涉及新的技术引入
- 主要是在现有基础上的能力扩展

**工程风险**：中
- 需要较大的工程量（特别是可视化设计器）
- 需要团队对本体和插件机制有深入理解
- 前后端协作要求较高

**业务风险**：低
- MVP 可以快速验证价值
- 分阶段实施，每个阶段都有交付物
- 可以先内部使用，再对外推广

## 现有优势分析

### 1. 完整的本体模型

TIS 已经实现了 Palantir Ontology 的核心概念：

| Palantir 概念 | TIS 实现 | 状态 |
|--------------|----------|------|
| Object Type | `OntologyObjectType` | ✅ 已实现 |
| Property | `OntologyProperty` | ✅ 已实现 |
| Link Type | `OntologyLinker` | ✅ 已实现 |
| Shared Property | `OntologySharedProperty` | ✅ 已实现 |
| Glossary | `OntologyGlossary` | ✅ 已实现 |
| Value Type | `OntologyValueType` | ✅ 已实现 |
| Constraint | `ValueConstraint` 体系 | ✅ 已实现 |

**优势**：
- 不需要重新设计本体模型
- 已有的本体数据可以直接使用
- 用户已经熟悉本体建模流程

### 2. 强大的插件机制

TIS 的插件机制天然支持元数据驱动的 UI：

**后端 - Descriptor 体系**：
```java
public class Descriptor<T> {
    // 字段定义
    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT)
    public PropertyType name;
    
    // 验证器
    public void validate(IFieldErrorHandler msgHandler, Context context, T plugin) { }
    
    // 联动逻辑
    public List<Option> doGetOptions() { }
}
```

**前端 - 自动渲染**：
```typescript
// tis-console/src/common/plugins.component.ts
// 根据后端 Descriptor 自动渲染表单
<plugins-component [formControlSpan]="20" 
                   [plugins]="pluginsMeta">
</plugins-component>
```

**优势**：
- 已经是元数据驱动的 UI 系统
- 前端自动渲染逻辑可以复用
- 插件注册和发现机制完善

### 3. 成功的应用案例 - ChatBI

ChatBI 已经证明了基于本体构建应用的可行性：

**ChatBI 的实现路径**：
```
Ontology (ObjectType/Property/Glossary)
    ↓
GraphRAG 检索（语义理解）
    ↓
NL → SQL 生成（LLM 驱动）
    ↓
数据查询和可视化（应用层）
```

**可复用的能力**：
- 本体 → 应用的映射机制
- GraphRAG 语义检索能力
- 前端表格、图表渲染组件
- LLM 集成框架

### 4. 微前端架构

TIS 采用微前端架构，支持模块化扩展：

**前端项目结构**：
```
tis-console/
  ├── src/base/              # 基础组件
  ├── src/common/            # 通用组件
  ├── src/runtime/           # 数据集成运行时
  └── src/ontology/          # 本体管理（可扩展为 Workshop）
```

**优势**：
- Workshop 模块可以独立开发
- 不影响现有功能
- 可以渐进式集成

### 5. 数据集成场景的天然优势

TIS 本身就是数据集成平台，Workshop 可以直接利用：

- **数据源连接** - 已支持数十种数据源
- **任务调度** - 可以通过 Action 触发 DataX/Flink 任务
- **数据血缘** - 可以基于 Linker 构建血缘关系
- **数据质量** - 基于 Constraint 构建质量规则

## 关键差距分析

### 差距 1：Function 和 Action 实体层 ⚠️

**现状**：
- 设计文档已完成（`design/ontology-action-function-scenarios.md`）
- 代码尚未实现

**需要补充**：
```java
// 核心实体
OntologyFunction     // 无副作用的计算（派生字段、数据转换、质量评分）
OntologyAction       // 有副作用的操作（创建任务、修复数据、发送通知）

// 注册和执行
FunctionRegistry     // Function 注册中心（基于 SPI 自动发现）
ActionEngine         // Action 执行引擎（带事务、权限、审计）
```

**工程量估算**：2-3 周（基础框架）

**优先级**：🔴 最高（阻塞后续所有工作）

### 差距 2：应用定义层 ⚠️

**现状**：
- 无应用定义实体
- 应用和本体是割裂的

**需要补充**：

```java
// 应用定义实体
public class OntologyApplication extends Ontology {
    private String name;
    private String description;
    private List<AppPage> pages;           // 页面列表
    private List<String> usedObjectTypes;  // 使用的 ObjectType
    private List<String> availableActions; // 可用的 Action
    private ApplicationLayout layout;      // 布局配置
}

// 页面定义
public class AppPage {
    private String name;
    private PageType type;        // TABLE_VIEW / FORM_VIEW / DASHBOARD / WORKFLOW
    private String objectType;    // 绑定的 ObjectType
    private List<ColumnConfig> columns;  // 显示哪些列
    private List<FilterConfig> filters;  // 筛选器
    private List<String> actions;        // 页面上的 Action 按钮
}
```

**配置方式**：
```xml
<!-- applications/data-quality-inspector.xml -->
<application>
    <name>data-quality-inspector</name>
    <displayName>数据质量巡检</displayName>
    
    <pages>
        <page name="overview" type="TABLE_VIEW">
            <objectType>quality_score</objectType>
            <columns>
                <column name="objectTypeName" label="对象类型"/>
                <column name="completeness" label="完整性" function="calculateCompleteness"/>
            </columns>
            <actions>
                <action name="generateReport" label="生成报告"/>
            </actions>
        </page>
    </pages>
</application>
```

**工程量估算**：2-3 周（实体定义 + 加载机制）

**优先级**：🟡 高（依赖差距 1）

### 差距 3：可视化应用构建器 ⚠️

**现状**：
- 无可视化设计器
- 应用需要手写 XML 配置

**需要补充**：

**前端模块**：
```
tis-console/src/application-builder/
  ├── app-designer.component.ts      # 应用设计器主界面
  ├── page-designer.component.ts     # 页面设计器（拖拽布局）
  ├── action-config.component.ts     # Action 配置面板
  ├── function-config.component.ts   # Function 配置面板
  └── app-preview.component.ts       # 应用预览
```

**功能要求**：
1. 拖拽式页面布局
2. 组件属性配置面板
3. Function/Action 可视化配置
4. 实时预览
5. 应用发布和版本管理

**工程量估算**：3-6 个月（取决于 UI 复杂度）

**优先级**：🟢 中（MVP 阶段可以手写配置，后续再做可视化）

### 差距 4：工作流引擎 ⚠️

**现状**：
- 无工作流编排能力
- 多步骤操作需要手动串联

**需要补充**：

```java
// 工作流定义
public class Workflow {
    private String name;
    private List<WorkflowStep> steps;
    private Map<String, Transition> transitions;  // 步骤间转移条件
}

// 工作流步骤
public class WorkflowStep {
    private String name;
    private StepType type;  // ACTION / APPROVAL / NOTIFICATION / CONDITION
    private String actionName;  // 执行的 Action
    private String nextStep;    // 下一步
}
```

**工程量估算**：2-3 个月（需要状态机、持久化、监控）

**优先级**：🟢 低（MVP 可以不包含，阶段 2 再做）

### 差距 5：权限和审计增强 ⚠️

**现状**：
- TIS 有基于插件的权限控制
- 但 Action 级别的权限控制尚未建立

**需要补充**：

```java
// Action 权限定义
public class ActionPermission {
    private String actionName;
    private List<String> requiredRoles;
    private PermissionCheckStrategy strategy;
}

// 审计日志
public class ActionAuditLog {
    private String actionName;
    private User user;
    private Map<String, Object> params;
    private ActionResult result;
    private long executionTime;
    private Timestamp timestamp;
}
```

**工程量估算**：2-3 周（集成现有权限系统）

**优先级**：🟡 高（生产环境必需）

## Palantir Workshop 功能对比

### 核心功能对比表

| 功能模块 | Palantir Workshop | TIS 现状 | 差距分析 |
|---------|------------------|---------|---------|
| **本体建模** | Object Type, Property, Link | ✅ 已实现 | 无差距 |
| **Function** | 派生字段、计算规则 | ❌ 未实现 | 需要实现实体和注册机制 |
| **Action** | 用户操作、业务流程 | ❌ 未实现 | 需要实现实体和执行引擎 |
| **应用定义** | Application as Code | ❌ 未实现 | 需要实现应用实体 |
| **可视化设计器** | 拖拽式 UI 构建 | ❌ 未实现 | 需要完整前端模块（工程量最大）|
| **表格视图** | 基于 OT 的数据表格 | ⚠️ 部分实现 | 可复用现有组件 |
| **表单视图** | 基于 OT 的数据表单 | ⚠️ 部分实现 | 可复用插件表单渲染 |
| **Dashboard** | 可视化仪表板 | ❌ 未实现 | 需要集成图表库 |
| **工作流** | 多步骤业务流程 | ❌ 未实现 | 需要工作流引擎 |
| **权限控制** | Action 级别权限 | ⚠️ 部分实现 | 需要扩展到 Action 级别 |
| **审计日志** | 完整的操作记录 | ⚠️ 部分实现 | 需要增强 Action 审计 |

### 差异化竞争力

TIS Workshop 相比 Palantir 的独特优势：

1. **数据集成原生支持** - Palantir 主要是数据分析，TIS 擅长数据集成
2. **开源生态** - TIS 是开源项目，Workshop 可以开源
3. **中国市场优化** - 更好的中文支持、本土化场景
4. **成本优势** - Palantir 非常昂贵，TIS 可以提供更低成本方案

## 架构设计要点

### 1. 分层架构

```
┌─────────────────────────────────────────┐
│         应用层 (Application Layer)       │
│  - 可视化应用构建器                       │
│  - 应用运行时                            │
│  - 应用市场                              │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│    逻辑层 (Logic Layer)                  │
│  - Function Registry (计算逻辑)          │
│  - Action Engine (业务操作)              │
│  - Workflow Engine (流程编排)            │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│    本体层 (Ontology Layer)               │
│  - ObjectType (对象类型)                 │
│  - Property (属性)                       │
│  - Linker (关系)                         │
│  - Constraint (约束)                     │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│    数据层 (Data Layer)                   │
│  - 数据源连接                            │
│  - 数据读写                              │
│  - 数据同步                              │
└─────────────────────────────────────────┘
```

### 2. 关键设计原则

#### 原则 1：元数据驱动

**一切皆配置**：
- 应用定义是配置（XML/JSON）
- Function 是可插拔的插件
- Action 是可插拔的插件
- UI 根据元数据自动渲染

**优势**：
- 不需要重新编译部署
- 支持热更新
- 易于版本管理

#### 原则 2：插件化扩展

**复用 TIS 现有的插件机制**：
```java
@TISPlugin(name = "calculateCompleteness")
public class CompletenessFunction extends OntologyFunction {
    @Override
    public Object apply(Map<String, Object> inputs) {
        // 实现逻辑
    }
}
```

**优势**：
- 与现有生态一致
- 开发者熟悉
- 支持第三方扩展

#### 原则 3：声明式优于命令式

**Function 是声明式的**：
```xml
<column name="completeness" 
        label="完整性" 
        function="calculateCompleteness"
        params="objectType=${row.objectType}"/>
```

而不是命令式的：
```java
// 不推荐
double completeness = service.calculateCompleteness(row.getObjectType());
```

**优势**：
- 配置更简洁
- 易于理解和维护
- 支持自动优化（如批量计算）

#### 原则 4：渐进式增强

**MVP → 完整功能 → 生态完善**：
1. 先做能跑的最小版本（手写配置）
2. 再做好用的版本（可视化设计器）
3. 最后做生态（模板市场、第三方扩展）

**优势**：
- 快速验证价值
- 降低风险
- 持续交付

### 3. 技术选型

#### 后端技术栈

| 组件 | 技术选型 | 理由 |
|-----|---------|------|
| Function Registry | Java SPI | 与现有插件机制一致 |
| Action Engine | Spring 事务管理 | 保证 ACID |
| 工作流引擎 | Activiti / Camunda | 成熟的 BPMN 引擎 |
| 权限控制 | Spring Security | 与 TIS 现有体系集成 |
| 审计日志 | 自定义（基于 TIS 日志） | 轻量级 |

#### 前端技术栈

| 组件 | 技术选型 | 理由 |
|-----|---------|------|
| UI 框架 | Angular（现有） | 保持一致 |
| 拖拽库 | Angular CDK Drag & Drop | 官方支持 |
| 图表库 | ECharts | 强大且开源 |
| 表单渲染 | 复用 plugins.component.ts | 已验证 |
| 状态管理 | RxJS | Angular 标配 |

## 实施建议

### 建议 1：分阶段实施，每个阶段都有可交付成果

**阶段 1（2-3 个月）**：核心能力验证
- 实现 Function 和 Action 基础框架
- 实现 3-5 个典型 Function 和 Action
- 构建一个完整的 MVP 应用（数据质量巡检）
- **交付物**：可运行的应用原型

**阶段 2（6-12 个月）**：可视化增强
- 实现应用设计器（拖拽式）
- 支持更多页面类型（Dashboard、工作流）
- 构建应用模板库
- **交付物**：可视化应用构建平台

**阶段 3（12+ 个月）**：生态完善
- 应用市场
- 第三方插件支持
- 多租户和权限增强
- **交付物**：企业级低代码平台

### 建议 2：先手写配置，后可视化

**不要一开始就做可视化设计器**：
1. 先用 XML/JSON 手写应用配置
2. 验证配置结构是否合理
3. 发现缺失的能力
4. 等运行时稳定后再做可视化

**理由**：
- 可视化设计器工程量巨大
- 配置结构可能需要多次调整
- 先用手写配置可以快速迭代

### 建议 3：复用现有能力，避免重复造轮

**可以复用的组件**：
- ✅ 插件表单渲染（`plugins.component.ts`）
- ✅ 表格组件（nz-table）
- ✅ 数据源连接（`DataSourceFactory`）
- ✅ 任务调度（现有调度系统）
- ✅ GraphRAG 检索（用于智能推荐）

**不要重复造轮**：
- ❌ 不要重新实现权限系统（集成现有的）
- ❌ 不要重新实现日志系统（扩展现有的）
- ❌ 不要重新设计本体模型（在现有基础上扩展）

### 建议 4：内部先用，再对外推广

**内部场景优先**：
1. 数据质量巡检应用（内部使用）
2. 任务监控 Dashboard（运维使用）
3. 成本分析报表（管理层使用）

**优势**：
- 快速收集反馈
- 降低试错成本
- 打磨产品体验

**对外推广条件**：
- 至少 3 个内部应用稳定运行
- 文档和培训材料完善
- 性能和安全测试通过

### 建议 5：建立开发者生态

**鼓励第三方开发 Function 和 Action**：
1. 提供脚手架工具（类似 `tis-archetype-plugin`）
2. 建立插件市场
3. 完善文档和示例
4. 举办开发者竞赛

**优势**：
- 扩展平台能力
- 降低维护成本
- 形成生态闭环

## 投入产出分析

### 投入估算

| 阶段 | 时间 | 人力 | 重点工作 |
|-----|------|------|---------|
| 阶段 1 | 2-3 个月 | 2-3 人 | Function/Action 框架 + MVP 应用 |
| 阶段 2 | 6-12 个月 | 3-5 人 | 可视化设计器 + 应用模板 |
| 阶段 3 | 12+ 个月 | 5-8 人 | 生态建设 + 商业化 |

### 产出预期

**阶段 1 产出**：
- 1 个可运行的应用原型
- 验证架构可行性
- 内部用户反馈

**阶段 2 产出**：
- 可视化应用构建平台
- 10+ 个内置应用模板
- 文档和培训材料

**阶段 3 产出**：
- 企业级低代码平台
- 应用市场（100+ 应用）
- 商业化落地

### 商业价值

**对内价值**：
1. **降低开发成本** - 数据应用开发从 2 周降到 2 天
2. **提升响应速度** - 业务需求快速落地
3. **统一数据治理** - 基于本体的统一视图

**对外价值**：
1. **产品差异化** - 国内首个基于本体的低代码平台
2. **市场机会** - 低代码市场规模巨大
3. **生态价值** - 建立开发者社区和插件生态

## 风险和应对

### 风险 1：技术复杂度高

**风险**：可视化设计器开发难度大

**应对**：
- 阶段 1 不做可视化，先验证架构
- 考虑引入成熟的低代码 UI 库（如 Formily）
- 聘请前端架构师

### 风险 2：用户学习成本

**风险**：用户不理解本体建模

**应对**：
- 提供丰富的应用模板（开箱即用）
- 提供可视化的本体建模工具
- 完善的文档和视频教程

### 风险 3：性能瓶颈

**风险**：Function 批量计算可能影响性能

**应对**：
- Function 结果缓存
- 异步计算 + 前端轮询
- 预计算 + 物化视图

### 风险 4：生态建设缓慢

**风险**：第三方开发者参与度低

**应对**：
- 先做好内部应用，打造标杆
- 举办开发者大会和竞赛
- 提供激励机制（插件收益分成）

## 总结

### 可行性评估结果

| 维度 | 评分 | 说明 |
|-----|------|------|
| 技术可行性 | ⭐⭐⭐⭐⭐ | 基础设施完善，技术路径清晰 |
| 工程可行性 | ⭐⭐⭐⭐ | 需要较大工程量，但可分阶段实施 |
| 商业可行性 | ⭐⭐⭐⭐⭐ | 市场需求明确，差异化竞争力强 |
| 团队可行性 | ⭐⭐⭐⭐ | 需要加强前端能力，其他能力充足 |

### 核心结论

1. **✅ 完全可行** - TIS 具备构建 Workshop 的所有基础能力
2. **🎯 分阶段实施** - 通过 MVP 快速验证，再逐步完善
3. **💡 差异化竞争** - 数据集成 + 本体语义 + 低代码 = 独特价值
4. **🚀 立即启动** - 建议 2-3 周内启动阶段 1 的开发

### 下一步行动

1. **组建项目团队**（1 周内）
   - 后端架构师 1 名
   - 前端架构师 1 名
   - 全栈工程师 2-3 名

2. **启动 MVP 开发**（2 周内）
   - 实现 Function 和 Action 基础框架
   - 选定 MVP 应用场景（建议：数据质量巡检）

3. **内部试用验证**（2 个月内）
   - 至少 3 个内部用户试用
   - 收集反馈并迭代

4. **评审和决策**（3 个月内）
   - 基于 MVP 效果决定是否进入阶段 2
   - 规划阶段 2 的资源投入
