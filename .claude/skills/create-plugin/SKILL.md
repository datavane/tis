---
name: create-plugin
description: Create a TIS plugin conforming to TIS plugin specifications
---

# TIS Plugin Creator

This skill helps you create a well-structured TIS (Data Integration Service) plugin that conforms to all TIS plugin specifications.

## Core Concepts

### 顶级插件 (Top-Level Plugin)

顶级插件是独立的主体，有独立的概念意义（可类比为一辆功能正常的车）。

**特征**：
- 不被任何其他插件所聚合
- 直接或间接实现 `com.qlangtech.tis.extension.Describable` 接口
- **必须实现 `com.qlangtech.tis.plugin.IdentityName` 接口** (`tis-builder-api/.../plugin/IdentityName.java`)
- 例如：`DeepSeekProvider` (LLM 提供者)、`DataxIcebergWriter` (数据写入器)

### 聚合属性插件 (Aggregated Property Plugin)

聚合属性插件单独存在没有意义，必须聚合在其他插件中才有意义（可类比为车上的轮胎）。

**特征**：
- 作为其他插件的 `@FormField` 属性类型存在
- 直接或间接实现 `com.qlangtech.tis.extension.Describable` 接口
- 内部有 `@TISExtension` 标注的 Descriptor 类
- **不实现 `IdentityName` 接口**
- Descriptor 通常继承父类的 `BasicDescriptor`
- 例如：`TemperatureSampling` (作为 `LLMProvider.sampling` 属性)、`HadoopCatalog` (作为 `DataxIcebergWriter.catalog` 属性)

**代码对比**：

```java
// 顶级插件示例
public class DeepSeekProvider extends LLMProvider implements IdentityName {
    @FormField(ordinal = 0, identity = true)
    public String name;  // IdentityName 要求的标识字段
    
    @TISExtension
    public static class DftDesc extends Descriptor<DeepSeekProvider> {
        // 可以覆盖 httpProcess() (如果前端有需求)
    }
}

// LLMProvider 父类（展示聚合属性插件如何被使用）
public abstract class LLMProvider implements Describable<LLMProvider>, IdentityName {
    // ↓ 这是聚合属性字段 —— Sampling 类型是 Describable，有多个子类型实现
    @FormField(ordinal = 128, validate = {Validator.require})
    public Sampling sampling;  // 前端可以选择 TemperatureSampling / TopPSampling / TopKSampling 等
}

// 聚合属性插件示例
public class TemperatureSampling extends Sampling {
    // 注意：未实现 IdentityName 接口
    
    @FormField(ordinal = 0, validate = {Validator.require})
    public Double temperature;  // 温度参数
    
    @TISExtension
    public static class DefaultDescriptor extends BasicDescriptor {
        // BasicDescriptor 定义在父类 Sampling 中
        // 不要覆盖 httpProcess() —— 聚合属性插件不暴露独立 HTTP 端点
        
        @Override
        public String shortComment() {
            return "温度采样策略";
        }
    }
}
```

**关系说明**：
- `DeepSeekProvider` 是顶级插件，实现 `IdentityName`，用户创建实例时命名
- `LLMProvider.sampling` 字段类型为 `Sampling`（抽象基类）
- `TemperatureSampling` 是 `Sampling` 的一个具体实现，作为聚合属性插件嵌入在 `LLMProvider` 中
- 前端展示 `DeepSeekProvider` 表单时，`sampling` 字段会渲染为下拉选择器，用户可选 `TemperatureSampling` / `TopPSampling` 等子类型

## What This Skill Does

When invoked, this skill will:
1. Analyze the user-provided context to understand the plugin's functionality
2. Design the plugin structure with appropriate properties (general or aggregated)
3. Generate the complete plugin implementation including:
   - Main plugin class implementing `Describable`
   - Inner `Descriptor` class with `@TISExtension`
   - Properties with proper `@FormField` annotations
   - Property validation methods
   - Corresponding `.json` property descriptor file
   - Optional `.md` help documentation

## TIS Plugin Specifications

### Core Requirements

Every TIS plugin MUST satisfy:

1. **Implement Describable Interface**
   - Plugin class must implement `com.qlangtech.tis.extension.Describable` (directly or via parent)

2. **Inner Descriptor Class**
   - Must have a `public` inner class extending `com.qlangtech.tis.extension.Descriptor`
   - Annotated with `@TISExtension` from `com.qlangtech.tis.extension.TISExtension`

3. **Property Requirements**
   - All properties must be `public`
   - Must be annotated with `@FormField`

4. **Property Descriptor Files**
   - A `.json` file in the same package under `resources/` directory
   - Optional `.md` file for rich markdown help content
   - **IMPORTANT:  parent classes also need `.json`!** Even if a class is `abstract` and has no `@TISExtension` Descriptor of its own, if it declares `@FormField` fields (public fields with `@FormField` annotation), it MUST have its own `.json` file at the matching resource path. These fields' help/placeholder/dftVal are inherited by concrete subclasses; TIS looks up the `.json` for each class in the hierarchy independently.

5. **Descriptor 命名：`getDisplayName()` 禁止返回中文（CRITICAL）**
   - `Descriptor#getDisplayName()` 是插件的机器可读短名，**必须由纯字母组成**：不得包含中文、空格以及任何标点符号
   - 需要表达中文功能说明时，让 Descriptor 实现 `com.qlangtech.tis.extension.DescriptorUseableShortComment` 接口
     （`tis-plugin/src/main/java/com/qlangtech/tis/extension/DescriptorUseableShortComment.java`），
     把原来的中文内容移到 `shortComment()` 的返回值，`getDisplayName()` 改写成对应的英文短名
   - 命名规则：**下划线分隔的帕斯卡命名**（每个单词首字母大写，单词之间用 `_` 分隔）；
     全大写缩写保持全大写（`LLM` / `URL` / `AIP`）；单个单词时无需下划线
   - 示例对照：

     | 类名 | ✅ `getDisplayName()` | ✅ `shortComment()` |
     |---|---|---|
     | `CloseOverlayConfig.DefaultDescriptor` | `Close_Overlay` | `关闭浮层` |
     | `DisableAutoRefreshConfig.DefaultDescriptor` | `Disable_Auto_Refresh` | `关闭自动刷新` |
     | `StreamLlmConfig.DefaultDescriptor` | `Stream_LLM` | `调用大模型` |
     | `RecomputeConfig.DefaultDescriptor` | `Recompute` | `重算变量` |
     | `UrlClickAction.DefaultDescriptor` | `URL` | `跳转链接` |

   - ❌ 反例：`return "关闭浮层";`（中文）、`return "Close Overlay";`（含空格）、`return "closeOverlay";`（单词首字母未大写）
   - 本规则对**所有** Descriptor 子类生效，顶级插件 (Top-Level Plugin) 与聚合属性插件 (Aggregated Property Plugin) 一视同仁

### Property Types

#### General Properties
Basic Java types:
- `Boolean` / `boolean`
- `Integer` / `int`
- `String`
- `Long` / `long`
- `java.util.Date`
- `com.qlangtech.tis.plugin.MemorySize`
- Duration types

#### Aggregated Properties
Properties that are themselves `Describable` plugins, providing polymorphic capability. Example: `ClusterType` in `TISFlinkCDCStreamFactory`.

**CRITICAL - @FormField for Aggregated Properties**:
- **NEVER specify `type` attribute in @FormField for aggregated properties**
- TIS automatically detects aggregated properties (properties implementing `Describable`)
- Adding `type = FormFieldType.SELECTABLE` will cause TIS runtime errors

**Correct** ✅:
```java
@FormField(ordinal = 0, validate = {Validator.require})
public OntologyActionRule sideEffectRule;
```

**Wrong** ❌:
```java
@FormField(ordinal = 0, type = FormFieldType.SELECTABLE, validate = {Validator.require})
public OntologyActionRule sideEffectRule;  // This will fail at runtime!
```

**IMPORTANT**: Each concrete implementation class of a 聚合属性插件 (Aggregated Property Plugin) is a full plugin and MUST have:
- Its own Java class with `@FormField` properties
- Its own `@TISExtension` Descriptor
- **Descriptor MUST implement `DescriptorUseableShortComment` interface** (from `tis-plugin/src/main/java/com/qlangtech/tis/extension/DescriptorUseableShortComment.java`)
  - Implement `shortComment()` method returning a **brief Chinese description** of the plugin's function
  - **Requirements for shortComment()**:
    - Use Chinese language (中文)
    - **Maximum 10 characters** (10个字以内)
    - **NO punctuation marks** (no periods, commas, etc.)
    - Will be displayed in the frontend UI to help users understand the plugin at a glance
    - Example: "Hadoop文件系统管理" or "Hive元数据存储"
  - 🔴 **同一个 Descriptor 的 `getDisplayName()` 不得返回中文**：中文说明只出现在 `shortComment()`，
    `getDisplayName()` 必须返回纯字母的英文短名（如 `Hadoop_Catalog`）。详见 "Core Requirements" 第 5 条
- Its own `.json` descriptor file in resources (same package path)
- Optional `.md` help file if properties are complex

**IdentityName Interface Rule (CRITICAL)**:
- **聚合属性插件 (Aggregated Property Plugin) implementation classes generally DO NOT need to implement `IdentityName` interface**
  - IdentityName is typically used for 顶级插件 (Top-Level Plugin), not nested 聚合属性插件
  - Reference: `tis-builder-api/src/main/java/com/qlangtech/tis/plugin/IdentityName.java`
- **If a plugin implements `IdentityName` interface, it MUST have an identity property**:
  - One `@FormField` property MUST be marked with `identity = true`
  - Example: `@FormField(ordinal = 0, identity = true, validate = {Validator.require, Validator.identity})`
  - Without this, TIS will fail to start with an error
  - The identity property acts as the unique identifier for the plugin instance
- **When to implement IdentityName**:
  - 顶级插件 (Top-Level Plugin) that users create multiple instances of (data sources, writers, readers)
  - Plugins that need to be referenced by name across the system
  - NOT for 聚合属性插件 (Aggregated Property Plugin) embedded within other plugins

**Parent Class Requirements for 聚合属性插件 (Aggregated Property Plugin)**:
- If a 聚合属性插件 type (e.g., `IcebergCatalog`) will have multiple concrete implementations, the **parent abstract class MUST define a `BasicDescriptor`**:
  - Must be a **protected abstract static** inner class
  - Must extend `Descriptor<ParentClassName>`
  - All concrete implementation classes' Descriptors MUST extend this `BasicDescriptor`
  - Example structure in parent class:
    ```java
    public abstract class IcebergCatalog implements Describable<IcebergCatalog> {
        // properties...
        
        protected abstract static class BasicDescriptor extends Descriptor<IcebergCatalog> {
            // common descriptor logic for all implementations
        }
    }
    ```
  - Example in implementation class:
    ```java
    public class HadoopCatalog extends IcebergCatalog {
        // properties...
        
        @TISExtension()
        public static class DefaultDescriptor extends BasicDescriptor 
                implements DescriptorUseableShortComment {
            @Override
            public String shortComment() {
                return "基于HDFS文件系统";
            }
            
            @Override
            public String getDisplayName() {
                // 纯字母英文短名，下划线分隔帕斯卡命名；中文一律放 shortComment()
                return "Hadoop_Catalog";
            }
        }
    }
    ```

Example: If `DataxIcebergWriter` has a 聚合属性插件 (Aggregated Property Plugin) `IcebergCatalog catalog`, and `HadoopCatalog` extends `IcebergCatalog`, then you need:
- `IcebergCatalog.java` (abstract parent with protected abstract static BasicDescriptor inner class)
- `HadoopCatalog.java` (with its own @FormField properties, Descriptor extends BasicDescriptor)
- `HadoopCatalog.json` (describing HadoopCatalog's own properties)
- Descriptor implements `DescriptorUseableShortComment` with `shortComment()` returning Chinese text (≤10 chars, no punctuation)
- Optional `HadoopCatalog.md` (if properties are complex)
- **DO NOT implement IdentityName** unless this is a 顶级插件 (Top-Level Plugin) (it's not in this case)

### @FormField Annotation Attributes

- **identity**: Unique identifier for the plugin instance (acts as primary key)
- **ordinal**: Display order in UI form (lower = higher priority, semantically related fields should be adjacent)
- **advance**: Mark as advanced setting (must have default value, can be hidden)
- **validate**: Frontend validation rules (see Validator options below)
- **type**: Field type (see FormFieldType options below)

### Validator Options

Reference: `tis-plugin/src/main/java/com/qlangtech/tis/plugin/annotation/Validator.java`

- `require`: Required field
- `user_name`: Username format (letters, numbers, underscore, dot, dash)
- `email`: Email format
- `forbid_start_with_number`: Cannot start with number
- `identity`: Primary key format
- `integer`: Integer format
- `host`: Internet domain with optional port (e.g., `192.168.28.200:7070`)
- `hostWithoutPort`: Domain without port
- `url`: URL starting with http/https
- `db_col_name`: Database column name format
- `relative_path`: File system relative path
- `absolute_path`: Unix absolute path
- `none_blank`: Non-empty content

### FormFieldType Options

Reference: `tis-plugin/src/main/java/com/qlangtech/tis/plugin/annotation/FormFieldType.java`

- `MULTI_SELECTABLE`: Multi-select, property type `List<IdentityName>` or `List<String>`
- `INPUTTEXT`: Single-line text input, property type `String`
- `SELECTABLE`: Single-select dropdown (register options via `registerSelectOptions()` in Descriptor)
- `PASSWORD`: Password input
- `FILE`: File upload (only one per form, plugin must implement `ITmpFileStore`)
- `TEXTAREA`: Multi-line text input (for SQL scripts, XML, etc.)
- `DATE`: Date picker
- `JDBCColumn`: JDBC column type
- `INT_NUMBER`: Integer number input
- `ENUM`: Enumeration selection
- `DateTime`: Date-time (UTC, property type `long` or `java.util.Date`)
- `DECIMAL_NUMBER`: Decimal number
- `DURATION_OF_SECOND`: Duration in seconds
- `DURATION_OF_MINUTE`: Duration in minutes
- `DURATION_OF_HOUR`: Duration in hours
- `MEMORY_SIZE_OF_BYTE`: Memory size in bytes
- `MEMORY_SIZE_OF_KIBI`: Memory size in KB
- `MEMORY_SIZE_OF_MEGA`: Memory size in MB

### FormFieldType 选用原则

#### 原则一：禁止 JSON 字符串输入

结构化数据不得使用 `TEXTAREA` 让用户填写 JSON 字符串，必须使用 TIS 的结构化表单机制：

| 场景 | 正确做法 | 错误做法 |
|---|---|---|
| 列表化结构对象（如表格列配置） | `@SubForm(desClazz = XxxConfig.class)` + `List<XxxConfig>` | `@FormField(type = FormFieldType.TEXTAREA)` 手写 `[{...}]` |
| 单个结构对象 | 字段类型声明为 `Describable` 子类，不指定 type（TIS 自动检测） | `@FormField(type = FormFieldType.TEXTAREA)` 手写 `{...}` |

```java
// ✅ 正确：使用 @SubForm
@SubForm(desClazz = WidgetColumnConfig.class, atLeastOne = false,
         idListGetScript = "return java.util.Collections.emptyList();")
public List<WidgetColumnConfig> columns;

// ❌ 错误：让用户手写 JSON
@FormField(type = FormFieldType.TEXTAREA)
public String columns;
```

#### 原则二：固定取值用 ENUM，动态选项用 SELECTABLE

- **`ENUM`**：字段的取值集合同定有限，在 Java 代码中用枚举类硬编码。如：聚合方式（sum/count/avg/min/max）、对齐方式（left/center/right）、级别（1/2/3/4）、颜色选择（20种常用颜色）。
- **`SELECTABLE`**：选项来自运行时外部数据源，通过 `registerSelectOptions()` 或 `doGetOptions()` 动态供给。如：当前 module 的变量列表、数据库表名列。

```java
// ✅ 正确：固定取值用 ENUM + Java 枚举类
@FormField(type = FormFieldType.ENUM, ordinal = 3, required = true)
public AggregationType aggregation;

public enum AggregationType {
    sum("求和"), count("计数"), avg("平均值"), min("最小值"), max("最大值");
    public final String label;
    AggregationType(String label) { this.label = label; }
}

// ❌ 错误：固定取值用 SELECTABLE（误导为动态选项）
@FormField(type = FormFieldType.SELECTABLE, ordinal = 3)
public String aggregation;

// ❌ 错误：固定取值用 INPUTTEXT（用户不知道合法值）
@FormField(type = FormFieldType.INPUTTEXT, ordinal = 3)
public String aggregation;
```

#### 原则三：颜色类枚举附带 hex 色值

枚举字段为颜色选择时，在 `.json` resource 中为每个枚举项附带 `hex` 色值，用以前端渲染颜色预览圆点：

```json
{
  "color": {
    "help": "选择文本颜色",
    "dftVal": "inherit",
    "enum": [
      { "label": "红色", "val": "red", "hex": "#f5222d" },
      { "label": "蓝色", "val": "blue", "hex": "#1890ff" }
    ]
  }
}
```

#### 原则四：SELECTABLE 字段必须在 Descriptor 中注册选项

当字段使用 `@FormField(type = FormFieldType.SELECTABLE)` 时，其选项来自运行时动态数据源（如当前 Module 的变量列表、插件存储中的实例列表、外部系统查询结果等）。此时 **必须在 Descriptor 构造函数中调用 `registerSelectOptions()` 注册选项提供者**，否则前端渲染下拉框时抛出 `IllegalStateException`：

```java
// Descriptor.java:1758
"fieldName:" + name + " is select options has not been register"
```

**4.1 字段名常量约定**

`registerSelectOptions()` 的第一个参数（字段名）必须定义为 `public static final String KEY_xxx = "xxx"` 常量，在 `registerSelectOptions()` 和 `valueChangePipe()` 中复用，杜绝构造函数中的临时字符串字面量。

**4.2 API 签名**

```java
// 注册单字段的 SELECTABLE 选项
protected final void registerSelectOptions(
    String fieldName,
    Callable<List<? extends IdentityName>> getter
);

// 构建前端级联联动（fromField 值变化 → 刷新 toField 选项列表）
protected ValueChangePipe valueChangePipe(
    String fromField,
    String... toField
);
// ValueChangePipe.render() 接受回调：
public void render(
    BiFunction<UploadPluginMeta, IParamGetter, List<? extends Option>> function
);
```

**4.3 三种提供者模式**

| 模式 | 使用场景 | 本仓库参考 |
|---|---|---|
| 方法引用 | 选项来自同一类/工具类的 **静态方法** | `UserProfile.java`（`tis-plugin/.../manage/common/UserProfile.java`） |
| Lambda + PluginStore | 选项来自 `IPluginStore.getPlugins()` | `DataXJobWorker.java`（`tis-plugin/.../datax/job/DataXJobWorker.java`） |
| Lambda + 抽象方法 | 选项需子类各自实现 | `TDFSLinker.java`（`tis-plugin/.../plugin/tdfs/TDFSLinker.java`） |

**模式一：方法引用**

```java
// 来源：UserProfile.java
public class UserProfile extends ParamsConfig {
    public static final String KEY_FIELD_LLM_NAME = "llm";

    @FormField(type = FormFieldType.SELECTABLE, ordinal = 1, validate = {Validator.identity})
    public String llm;

    @TISExtension
    public static final class DftDescriptor extends ParamsConfig.BasicParamsConfigDescriptor {
        public DftDescriptor() {
            super(KEY_DISPLAY_NAME);
            this.registerSelectOptions(KEY_FIELD_LLM_NAME, LLMProvider::getExistProviders);
        }
    }
}
```

**模式二：Lambda + PluginStore**

```java
// 来源：DataXJobWorker.java
public abstract class DataXJobWorker implements Describable<DataXJobWorker> {
    public static final String KEY_FIELD_NAME = "k8sImage";

    @FormField(ordinal = 1, type = FormFieldType.SELECTABLE, validate = {Validator.require})
    public String k8sImage;

    protected static abstract class BasicDescriptor extends Descriptor<DataXJobWorker> {
        public BasicDescriptor() {
            super();
            this.registerSelectOptions(KEY_FIELD_NAME, () -> {
                IPluginStore pluginStore = this.getK8SImageCategory().getPluginStore();
                return pluginStore.getPlugins();
            });
        }
        protected abstract K8sImage.ImageCategory getK8SImageCategory();
    }
}
```

**模式三：Lambda + 抽象方法**

```java
// 来源：TDFSLinker.java
public abstract class TDFSLinker implements Describable<TDFSLinker> {
    public static final String KEY_FTP_SERVER_LINK = "linker";

    @FormField(ordinal = 1, type = FormFieldType.SELECTABLE, validate = {Validator.require})
    public String linker;

    protected static abstract class BasicDescriptor extends Descriptor<TDFSLinker>
            implements DescriptorUseableShortComment {
        public BasicDescriptor() {
            super();
            this.registerSelectOptions(KEY_FTP_SERVER_LINK, () -> createRefLinkers());
        }
        protected abstract List<? extends IdentityName> createRefLinkers();
    }
}
```

**4.4 IdentityName 创建**

`registerSelectOptions()` 的 Callable 需要返回 `List<? extends IdentityName>`。`IdentityName` 是函数式接口，只有 `identityValue()` 一个方法。创建选项的方式如下：

| 场景 | 做法 |
|---|---|
| 纯字符串选项 | `IdentityName.create("value")`（工厂方法，返回匿名 `IdentityName` 实例） |
| 已有 `IdentityName` 对象 | 直接返回即可 |
| 已有实现了 `IdentityName` 的 `Describable` 实例 | 直接返回 |

```java
// IdentityName.create() 工厂方法
IdentityName opt = IdentityName.create("myValue");
// opt.identityValue() → "myValue"

// 用于 Stream 中转换字符串列表
List<IdentityName> opts = variables.stream()
    .filter(v -> v.type == VariableType.OBJECT_SET)
    .map(v -> IdentityName.create(v.name))
    .collect(Collectors.toList());
```

**4.5 级联联动 (valueChangePipe)**

当一个 SELECTABLE 字段的选中值决定另一组字段的可选范围时，使用 `valueChangePipe()` 实现级联。前端在 fromField 触发 `onChange` 时，自动向后端请求刷新 toField 的选项。

典型场景：选择一个"对象集变量"后，其"属性选择"字段的选项随之更新。

```java
// 级联注册模式示例
public class MyWidget extends WorkshopWidgetDescribable {

    public static final String KEY_OBJECT_SET_VAR = "objectSetVar";
    public static final String KEY_PROP_A = "propA";
    public static final String KEY_PROP_B = "propB";

    @FormField(type = FormFieldType.SELECTABLE, ordinal = 3, validate = {Validator.require})
    public String objectSetVar;

    @FormField(type = FormFieldType.SELECTABLE, ordinal = 4, validate = {Validator.require})
    public String propA;

    @FormField(type = FormFieldType.SELECTABLE, ordinal = 5, validate = {Validator.require})
    public String propB;

    @TISExtension
    public static class DescriptorImpl extends Descriptor<IWorkshopWidget> {

        public DescriptorImpl() {
            super();

            // Step 1：注册源头字段的选项
            this.registerSelectOptions(KEY_OBJECT_SET_VAR, MyHelper::getObjectSetVariableOptions);

            // Step 2：级联目标字段初始化为空（未选源头时不显示选项）
            this.registerSelectOptions(KEY_PROP_A, Collections::emptyList);
            this.registerSelectOptions(KEY_PROP_B, Collections::emptyList);

            // Step 3：建立级联管道 —— objectSetVar 变化时刷新 propA/propB 的选项
            this.valueChangePipe(KEY_OBJECT_SET_VAR, KEY_PROP_A, KEY_PROP_B)
                    .render((pluginMeta, params) -> {
                        String selectedVar = params.getString(KEY_OBJECT_SET_VAR);
                        if (selectedVar == null) {
                            return Collections.emptyList();
                        }
                        return MyHelper.getObjectPropertyOptions(selectedVar, pluginMeta);
                    });
        }
    }
}
```

`render()` 回调签名说明：

| 参数 | 类型 | 说明 |
|---|---|---|
| `pluginMeta` | `UploadPluginMeta` | 当前插件上下文元数据，含 domain、extra params 等 |
| `params` | `IParamGetter` | 前端发来的表单参数字典，`params.getString(fieldName)` 读取 fromField 当前值 |
| **返回值** | `List<? extends Option>` | 所有 toField 字段共用的选项列表 |

> **注意**：`render()` 返回 `List<? extends Option>`（而非 `IdentityName`）。`Option` 有 `getName()` 和 `getValue()` 两个方法。如需携带 EndType 信息，使用 `OptionWithEndType`。

**4.6 共享工具类模式**

当多个 Widget（或插件的多个 SELECTABLE 字段）需要相同的选项逻辑时，抽取为一个共享工具类的静态方法。这是防止重复代码的标准做法：

```java
public final class MyHelper {

    private MyHelper() {}

    /** 获取 ObjectSet 类型变量的选项列表 */
    public static List<IdentityName> getObjectSetVariableOptions() {
        IPluginContext ctx = IPluginContext.getThreadLocalInstance();
        if (ctx == null) return Collections.emptyList();
        // ... 从上下文中加载变量列表，过滤 type == OBJECT_SET ...
        return variables.stream()
            .filter(v -> v.type == VariableType.OBJECT_SET)
            .map(v -> IdentityName.create(v.name))
            .collect(Collectors.toList());
    }

    /** 根据选中的 ObjectSet 变量名，获取其对象类型的属性选项（用于级联） */
    public static List<? extends Option> getObjectPropertyOptions(
            String varName, UploadPluginMeta pluginMeta) {
        if (StringUtils.isEmpty(varName)) return Collections.emptyList();
        try {
            // 1. 根据 varName 找到变量定义
            // 2. 获取其中引用的对象类型
            // 3. 加载对象类型的属性列表并返回
            // ... 业务逻辑 ...
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
```

**上下文获取**：静态方法中通过 `IPluginContext.getThreadLocalInstance()` 获取当前请求上下文，从中提取 domain、module 等信息来加载所需数据。如果前端需要传递额外参数，通过 `UploadPluginMeta` 的 extra params 传参。

### Step 5 追加验证项

在现有 Step 5 验证检查项中，追加两条：

- **SELECTABLE 字段选项注册**：所有 `FormFieldType.SELECTABLE` 字段是否在 Descriptor 构造函数中调用了 `registerSelectOptions()`。如果涉及级联联动，`valueChangePipe()` 是否完整配置
- **字段名常量**：`registerSelectOptions()` 的第一个参数是否使用 `public static final String` 常量，而非字符串字面量

### Validation Logic

**IMPORTANT - DO NOT Generate Redundant Validation Code**:
TIS framework automatically generates validation logic based on `@FormField` annotations' `validate` attribute. For example:
- `@FormField(validate = {Validator.require})` → TIS auto-validates the field is not empty
- `@FormField(validate = {Validator.identity})` → TIS auto-validates identity format
- `@FormField(validate = {Validator.require, Validator.identity})` → TIS auto-validates both

**DO NOT write manual validation code for these standard validators**. Only implement custom `validate*` methods when you need **business-specific logic** that cannot be expressed through standard validators.

Reference: `tis-plugin/src/main/java/com/qlangtech/tis/plugin/annotation/Validator.java`

#### Single Property Validation

**Only add when standard validators are insufficient**. Add a method in Descriptor with pattern `validate{PropertyName}`:

```java
// ONLY NEEDED for custom business logic validation
// DO NOT implement if @FormField(validate={...}) already covers it
public boolean validateAge(IFieldErrorHandler msgHandler, Context context, String fieldName, String value) {
    int age = Integer.parseInt(value);
    if (age < MIN_AGE) {
        msgHandler.addFieldError(context, fieldName, "Cannot be less than: " + MIN_AGE);
        return false;
    }
    if (age > MAX_AGE) {
        msgHandler.addFieldError(context, fieldName, "Cannot be greater than: " + MAX_AGE);
        return false;
    }
    return true;
}
```

#### Multi-Property Joint Validation

Override `validateAll` in Descriptor:

```java
@Override
protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
    // Perform joint validation
    if (!validateUserCredentials(postFormVals.newInstance())) {
        msgHandler.addErrorMessage(context, "Validation failed");
        return false;
    }
    return true;
}
```

## Descriptor HTTP 业务执行（httpProcess）

### 概述

Descriptor 可以通过覆盖 `httpProcess()` 方法处理自定义 HTTP 请求，
由 `PluginAction.doDescriptionProcess()` 路由调用：

```
POST /coredefine/corenodemanage.ajax
  ?event_submit_do_description_process=y
  &action=plugin_action
  &impl=com.qlangtech.tis.plugin.XxxDescriptor$DftDesc
```

路由链路：

```
Frontend POST →
  PluginAction.doDescriptionProcess() →
    TIS.get().getDescriptor(impl) →
      targetPlugin.httpProcess(paramGetter, pluginContext, context)
```

### 何时使用（USE WHEN）

**唯一条件：前端有明确的 CRUD/HTTP 调用需求**

`httpProcess()` 是**需求驱动**的，不是"先备着"的。仅在以下情况才应考虑：
- 前端需要发送 POST 请求到 `/coredefine/corenodemanage.ajax?impl=...` 来操作插件管理的实体
- 例如：Workshop 前端需要创建/查询/更新/删除 Module → `WorkshopModuleOperationDesc` 的 httpProcess()
- 例如：LLM 推理页面需要启动/停止/恢复推理 → `BasicInfterExecuteDesc` 的 httpProcess()
- 例如：某个插件的管理页面需要一个非标准 AJAX 端点

**"前端可能需要"或"为扩展保留"都不构成理由**——没有前端调用方，httpProcess() 永远不会被执行。

### 何时不使用（DO NOT USE WHEN）

**例外一：聚合属性插件 (Aggregated Property Plugin)**

参见文档开头的 "Core Concepts" 节中的定义。

判定标准：
- ✅ **顶级插件 (Top-Level Plugin) 可以覆盖 `httpProcess()`**（如果前端有需求）
  ```java
  // 实现了 IdentityName 接口
  public class DeepSeekProvider extends LLMProvider implements IdentityName {
      @TISExtension
      public static class DftDesc extends Descriptor<DeepSeekProvider> {
          // 可以覆盖 httpProcess()
      }
  }
  ```
- ❌ **聚合属性插件 (Aggregated Property Plugin) 不要覆盖 `httpProcess()`**
  ```java
  // 未实现 IdentityName，且 Descriptor 继承父类的 BasicDescriptor
  public class TemperatureSampling extends Sampling {
      @TISExtension
      public static class DefaultDescriptor extends BasicDescriptor {
          // 不要覆盖 httpProcess() —— 聚合属性插件不暴露独立 HTTP 端点
      }
  }
  ```

原因：聚合属性插件 (Aggregated Property Plugin) 是嵌入在其他插件内部的组件，前端无法通过 `impl=...` 路由到它们。

**例外二：纯配置的顶级插件 (Top-Level Plugin)**
- 插件只是配置数据（数据源 Reader/Writer、告警通道、Catalog 管理等）
  → 由 TIS 框架的标准表单提交机制处理，无需覆盖 httpProcess()
- 插件的业务逻辑可以通过标准 @FormField + Validator 完成
  → 覆盖 httpProcess() 会增加不必要的复杂度

### 模式一：CRUD 操作分派

通过 `type` 参数分派到不同的 CRUD 操作，适用于 Workshop Module 等实体的管理：

```java
public abstract class XxxOperationDesc extends Descriptor<XxxModel> {
    @Override
    public final void httpProcess(IControlMsgHandler msgHandler,
                                  IPluginContext pluginContext,
                                  Context context) throws Exception {
        String type = msgHandler.getString("type");
        switch (type) {
            case "create": doCreate(msgHandler, pluginContext, context); break;
            case "get":    doGet(msgHandler, pluginContext, context);    break;
            case "list":   doList(msgHandler, pluginContext, context);   break;
            case "update": doUpdate(msgHandler, pluginContext, context); break;
            case "delete": doDelete(msgHandler, pluginContext, context); break;
            default: pluginContext.addErrorMessage(context, "unsupported: " + type);
        }
    }
    // 每个操作读取参数 → 调用 Service → setBizResult()
    // 用户名通过 pluginContext.getLoginUser().getName() 获取
}
```

参考实现：`WorkshopModuleOperationDesc`（CRUD 分派模式，见 Reference Implementations）

### 模式二：SSE 流式处理

用于 LLM 推理等长时间运行任务的实时状态推送，通过 `SSEEventWriter`
向前端推送进度事件：

```java
public abstract class XxxExecuteDesc extends Descriptor<XxxModel> {
    @Override
    public final void httpProcess(IControlMsgHandler msgHandler,
                                  IPluginContext pluginContext,
                                  Context context) throws Exception {
        String type = msgHandler.getString("type");
        if ("stop".equals(type))   { /* 停止推理任务 */ return; }
        if ("resume".equals(type)) { /* 恢复推理 */ return; }
        // 默认：启动新推理、通过 SSE 流式推送进度
        SSEEventWriter sseEventWriter = new SSEEventWriter(context);
        // ... 异步推理、推送事件
    }
}
```

参考实现：`BasicInfterExecuteDesc`（SSE 流式模式，见 Reference Implementations）

### 参数与响应约定

| 操作 | 方式 |
|---|---|
| 参数读取 | `paramGetter.getString("key")` |
| 正常响应 | `pluginContext.setBizResult(context, jsonObject)` → 前端收到 `response.bizresult` |
| 错误响应 | `pluginContext.addErrorMessage(context, message)` |
| 请求编码 | `application/x-www-form-urlencoded` |
| 必含参数 | `event_submit_do_description_process=y`, `action=plugin_action`, `impl=...` |

## Reference Implementations

Excellent examples in the codebase:
1. `tis-plugin/src/main/java/com/qlangtech/tis/config/authtoken/impl/DefaultHiveUserToken.java`
2. `tis-plugin/src/main/java/com/qlangtech/tis/manage/common/UserProfile.java`
3. `tis-plugin/src/main/java/com/qlangtech/tis/plugin/alert/impl/LoginPlugin.java` (multi-property validation)
4. `plugins/tis-ontology-plugin/.../workshop/desc/WorkshopModuleOperationDesc.java` — `httpProcess()` CRUD 分派模式
5. `plugins/tis-ontology-plugin/.../impl/infer/BasicInfterExecuteDesc.java` — `httpProcess()` SSE 流式模式

> **注意**：上述 `plugins/` 下的参考实现在 `/Users/mozhenghua/j2ee_solution/project/plugins` 工程中，不在本仓库

## Instructions for Claude

When the user invokes `/create-plugin`, follow this workflow:

### Step 1: Analyze Context
- Ask the user to provide context about the plugin they want to create (or the user may provide it directly)
- Analyze the context to determine:
  - Plugin's purpose and functionality
  - What properties are needed (general vs aggregated)
  - Parent class to extend (if any)
  - Validation rules required
  - **是否需要自定义 HTTP 端点（`httpProcess()`）**：
    - **先排除聚合属性插件 (Aggregated Property Plugin)**：如果这个类是另一个顶级插件的聚合属性实现（未实现 `IdentityName` 接口，且 Descriptor 继承父类的 `BasicDescriptor`），则**跳过此项**——聚合属性插件不暴露独立 HTTP 端点
    - **需求驱动判断**：前端是否已有明确的 CRUD/HTTP 调用需求？
      - **有**（前端页面需调用 PluginAction URL 管理该插件的实体）→ 参考 "Descriptor HTTP 业务执行" 节
      - **无**（只是展示配置表单，标准 @FormField 即可）→ **不要生成 httpProcess()**——没有调用方就是死代码
    - 如何识别"前端有需求"：
      - ✅ **有需求**：用户说"需要在 Workshop 页面上创建/编辑/删除 Module"
      - ✅ **有需求**：用户说"推理页面需要启动/停止/查看进度"
      - ❌ **无需求**：用户说"创建一个 DataX Reader 插件"（纯配置，表单提交即可）
      - ❌ **无需求**：用户说"为扩展性预留 CRUD 接口"（没有实际调用方）

### Step 2: Design Plugin Structure
- Determine property organization:
  - Which properties should be general types?
  - Which properties should be 聚合属性插件 (Aggregated Property Plugin) (polymorphic)?
  - **For each 聚合属性插件 (Aggregated Property Plugin)**:
    - Will it have multiple concrete implementations?
    - If yes, the parent abstract class MUST define a `BasicDescriptor` inner class
    - List all planned implementation classes
  - Group semantically related properties via ordinal ordering
- Plan validation logic:
  - Which properties need business logic validation?
  - Are there any multi-property joint validations?
- **如果选择了 `httpProcess()` 模式（仅限顶级插件 (Top-Level Plugin)，且前端确有需求）**：
  1. 确定执行模式：CRUD 操作分派 or SSE 流式处理？
  2. 如果是 **CRUD 模式**：
     - 需要哪些操作（create/get/list/update/delete）？
     - 是否需要 Service 层（推荐：是，分离业务逻辑）
  3. 如果是 **SSE 流式模式**：
     - 确定 type 分派点（start/stop/resume 等）
     - SSE 事件结构设计

### Step 3: Confirm Design with User
Before implementation, present:
- Plugin class name and package
- List of properties with types, FormFieldType, and validators
- **Any 聚合属性插件 (Aggregated Property Plugin) and their subtypes** (each subtype will be a separate plugin with its own files)
- For each 聚合属性插件 subtype, list:
  - Subtype class name and parent class
  - Its own properties with types and validators
  - Files to be generated (Java + JSON + optional MD)
- Validation methods to be implemented
- Wait for user approval

### Step 4: Implement Plugin
Generate these files in the correct structure:

1. **Plugin Java Class**
   - Package declaration
   - Imports
   - Plugin class implementing `Describable<PluginClassName>`
   - Public properties with `@FormField` annotations
   - Business logic methods
   - Inner `Descriptor` class with:
     - `@TISExtension` annotation
     - `getDisplayName()` override — **必须返回纯字母英文短名，禁止中文/空格**（见 "Core Requirements" 第 5 条）
     - `shortComment()` override — 中文功能说明（实现 `DescriptorUseableShortComment`）
     - `validate*` methods for property validation
     - `validateAll()` if needed for joint validation
     - `registerSelectOptions()` if using SELECTABLE fields

2. **Property Descriptor JSON** (`PluginClassName.json`)
   - **CRITICAL: Correct JSON Format**
     ```json
     {
       "propertyName1": {
         "label": "Display Label",
         "help": "Short one-sentence help text",
         "dftVal": "default value"
       },
       "propertyName2": {
         "label": "Another Property",
         "help": "Brief description"
       }
     }
     ```
   - **WRONG Format** (DO NOT USE):
     ```json
     {
       "formFields": [
         {"key": "propertyName1", "label": "...", "help": "..."}
       ]
     }
     ```
   - In `src/main/resources/{package_path}/`
   - Direct object-to-object mapping (propertyName → property descriptor)
   - Each property descriptor contains:
     - `label`: Display label (required)
     - `placeholder`: Input placeholder (optional, **only applicable for `FormFieldType.INPUTTEXT` or `FormFieldType.TEXTAREA`**). **Do NOT set alongside `dftVal`** — if a `dftVal` is specified, `placeholder` is redundant and unnecessary.
     - `help`: Brief one-sentence help text (optional, **ONLY for simple properties that do NOT have detailed help in .md file**)
     - `dftVal`: Default value (optional, for optional fields). **Prefer to set when semantically reasonable** — it pre-fills the input, reducing the user's effort and making the form more self-explanatory. However, **do NOT force an artificial default** that carries no semantic meaning (e.g., setting `"0"` or `""` purely to avoid a blank field).
   - **CRITICAL - JSON help vs MD file distribution rule**:
     - **If a property already has detailed help in the `.md` file** (under `## propertyName` section), **DO NOT include `help` field in JSON** for that property
     - **Only include `help` in JSON when**:
       - The property is simple enough to be explained in one sentence AND
       - There is NO corresponding section in the `.md` file
     - **Reason**: Avoid duplicate help content - use either JSON `help` OR `.md` section, never both

3. **Optional Markdown Help** (`PluginClassName.md`)
   - **Create ONLY for properties that need detailed multi-line explanation**
   - **CRITICAL - MD File Structure Rules**:
     - **DO NOT include a top-level title** (like `# PluginClassName - Description`) - this is redundant
     - **Start directly with property sections** using `## propertyName` headers
     - Only include properties that genuinely need detailed explanation
   - **Help Information Distribution Rule**:
     - **Simple properties** (username, password, port, simple flags): Only define `help` in JSON file with one sentence, **DO NOT create sections in .md file**
     - **Complex properties** (catalog types, file formats, configuration objects, properties needing examples): Define detailed help in `.md` file with `## propertyName` headers, **DO NOT include `help` in JSON file**
     - **DO NOT duplicate**: If a property has detailed help in `.md` file, **omit the `help` field** in JSON for that property
   - **Decision criteria for .md file**:
     - Property requires multiple sentences to explain
     - Property needs code examples or configuration samples
     - Property has multiple options that need detailed comparison
     - Property involves technical concepts requiring elaboration
   - Use `## propertyName` headers for each complex property that needs detailed documentation
   - **Example of correct .md structure**:
     ```markdown
     ## catalogType
     
     选择 Iceberg Catalog 类型，不同类型对应不同的元数据存储方式：
     
     - **Hadoop Catalog**: 基于 HDFS 文件系统存储元数据
     - **Hive Metastore**: 使用 Hive 元数据服务
     - **Glue Catalog**: 使用 AWS Glue 数据目录
     
     ## warehouse
     
     Iceberg 表数据存储的根路径。支持：
     - HDFS 路径: `hdfs://namenode:8020/warehouse/iceberg`
     - 本地路径: `file:///data/iceberg`
     - S3 路径: `s3://bucket/warehouse`
     ```
   - **WRONG - Do NOT include top-level title**:
     ```markdown
     # IcebergCatalog - Iceberg 元数据目录配置  ← DO NOT DO THIS
     
     ## catalogType
     ...
     ```

4. **For 聚合属性插件 (Aggregated Property Plugin): Parent Abstract Class**
   - **IMPORTANT**: If the plugin has 聚合属性插件 with multiple implementations, first create or verify the parent abstract class:
   
   **Parent Abstract Class** (e.g., `IcebergCatalog.java`)
   - Implements `Describable<ParentClassName>`
   - Has `@FormField` properties that are common to all implementations
   - **MUST define a `BasicDescriptor` inner class**:
     - Declared as `protected abstract static class BasicDescriptor extends Descriptor<ParentClassName>`
     - Contains common descriptor logic for all implementations
     - May implement common validation methods
     - Example:
       ```java
       public abstract class IcebergCatalog implements Describable<IcebergCatalog> {
           @FormField(ordinal = 1, validate = {Validator.require})
           public String catalogName;
           
           @FormField(ordinal = 2)
           public String databaseName;
           
           // abstract methods...
           
           protected abstract static class BasicDescriptor extends Descriptor<IcebergCatalog> {
               // common descriptor logic
               @Override
               protected boolean verify(IFieldErrorHandler msgHandler, Context context, PostFormVals postFormVals) {
                   return super.verify(msgHandler, context, postFormVals);
               }
           }
       }
       ```

5. **For Each 聚合属性插件 (Aggregated Property Plugin) Implementation Class**
   - **IMPORTANT**: If the plugin has 聚合属性插件 (polymorphic plugin properties), EACH concrete implementation class is a separate plugin and needs:
   
   **Example**: Main plugin `DataxIcebergWriter` has property `IcebergCatalog catalog`, and you're creating `HadoopCatalog extends IcebergCatalog`:
   
   a. **Implementation Java Class** (`HadoopCatalog.java`)
      - Extends the abstract parent class (e.g., `IcebergCatalog`)
      - Has its own `@FormField` properties (in addition to inherited ones)
      - Has its own `@TISExtension` Descriptor that:
        - **MUST extend parent's BasicDescriptor**
        - **MUST implement `DescriptorUseableShortComment` interface**
        - **MUST override `shortComment()` method** with these requirements:
          - Return Chinese text (中文) describing the plugin function
          - **Maximum 10 characters** (10个字以内)
          - **NO punctuation marks** (不要标点符号)
          - Be concise and clear
          - Example for HadoopCatalog: `return "基于HDFS文件系统";`
          - Example for HiveMetastoreCatalog: `return "使用Hive元数据";`
        - **MUST NOT override `getDisplayName()` with Chinese text** — 必须返回纯字母的英文短名
          （下划线分隔帕斯卡命名，如 `Hadoop_Catalog`），中文一律放 `shortComment()`
      - Same structure as main plugin
   
   b. **Implementation JSON Descriptor** (`HadoopCatalog.json`)
      - In `src/main/resources/{package_path}/` (same package as HadoopCatalog.java)
      - Describes **only HadoopCatalog's own properties** (not inherited ones)
      - Same format rules as main plugin JSON
      - Example:
        ```json
        {
          "warehouse": {
            "label": "Warehouse Path",
            "help": "HDFS or local filesystem path"
          },
          "hadoopConfDir": {
            "label": "Hadoop Config Directory",
            "help": "Path to Hadoop configuration directory"
          }
        }
        ```
   
   c. **Optional Implementation MD** (`HadoopCatalog.md`)
      - Create if HadoopCatalog has complex properties
      - Same rules as main plugin MD file
   
   d. **Repeat for ALL implementation classes**
      - If there's also `HiveMetastoreCatalog`, create its Java + JSON + optional MD
      - If there's `GlueCatalog`, create its Java + JSON + optional MD
      - Each implementation is a complete plugin with full file set

### Step 5: Verify Implementation
- Check all requirements are satisfied:
  - Implements `Describable`
  - Has `@TISExtension` inner Descriptor
  - All properties are public with `@FormField`
  - Ordinal values are properly sequenced
  - Validation rules are appropriate
  - **JSON descriptor format is correct** (object-to-object, NOT formFields array)
  - **All properties in JSON file** exist as public fields in Java class
  - **Help information distribution is correct**:
    - Simple properties: only `help` in JSON (one sentence)
    - Complex properties: detailed help in `.md` file, brief or no `help` in JSON
    - No duplicate help content between JSON and .md files
  - **Descriptor 命名正确（对每个 Descriptor 子类逐一检查）**:
    - `getDisplayName()` 返回值是否**纯字母**（无中文 / 无空格 / 无标点）
    - 是否采用下划线分隔的帕斯卡命名（如 `Close_Overlay`、`Open_Workshop_Module`），缩写是否全大写（`Stream_LLM`）
    - 中文功能说明是否已移到 `shortComment()`，且 Descriptor 是否实现了 `DescriptorUseableShortComment` 接口
  - **For 聚合属性插件 (Aggregated Property Plugin)**: 
    - **Parent abstract class requirements**:
      - Has a `protected abstract static class BasicDescriptor extends Descriptor<ParentClass>`
      - BasicDescriptor is properly defined in the parent class
    - **Each implementation class has its complete file set**:
      - Java class exists with @TISExtension Descriptor
      - **Descriptor extends parent's BasicDescriptor**
      - **Descriptor implements `DescriptorUseableShortComment` interface**
      - **`shortComment()` method implemented correctly**:
        - Returns Chinese text (中文)
        - Length ≤ 10 characters
        - No punctuation marks
        - Clearly describes the plugin's function
      - JSON descriptor exists in correct resources path
      - JSON describes only the implementation's own properties (not inherited ones)
      - Optional MD file if implementation has complex properties
- Suggest any improvements

### Step 6: Provide Usage Instructions
- Explain where the plugin files were created
- How to build and test the plugin
- How to register the plugin in TIS (if special steps needed)

## Important Considerations

1. **JSON Format (CRITICAL)**: The property descriptor JSON file MUST use object-to-object format, NOT array format.
   - ✅ Correct: `{"propertyName": {"label": "...", "help": "..."}}`
   - ❌ Wrong: `{"formFields": [{"key": "propertyName", ...}]}`
   - Wrong format will cause TIS backend to fail loading the plugin

2. **Help Information Distribution**: Avoid duplicating help content between JSON and .md files.
   - **Simple properties** (username, password, port, boolean flags): Use only JSON `help` field with one concise sentence
   - **Complex properties** (catalog types, configuration objects, properties with multiple options): Use .md file with detailed explanation, keep JSON `help` brief or omit it
   - **Rule of thumb**: If explanation fits in one sentence, use JSON only. If it needs examples, bullet points, or multiple paragraphs, use .md file
   - Let the AI judge the complexity: analyze each property's nature and decide the appropriate help placement

3. **Ordinal Sequencing**: Assign ordinal values carefully. Related properties (like username/password) should have consecutive ordinals to appear together in the UI.

4. **Default Values**: Advanced properties (with `advance = true`) MUST have sensible default values.

5. **Validation**: Always implement business logic validation for properties that need it. Don't rely solely on frontend validation.

6. **Parent Classes**: Check if there's an appropriate parent class to extend (like `AlertChannel` for alert plugins) to inherit common functionality.

7. **Package Organization**: Place the plugin in the appropriate package that reflects its functionality (e.g., alert plugins in `*.plugin.alert.impl.*`).

## Example Interaction

```
User: /create-plugin

I want to create an email alert channel plugin for TIS. It should support:
- SMTP host and port
- Authentication (username/password)  
- SSL support
- Sender email address