# TIS 多步骤插件配置架构说明

## 概述

多步骤插件配置功能允许用户通过多个向导式步骤来配置复杂的插件实例。典型应用场景是ETL中的表Join规则定义，需要分步骤选择数据源、表、列和匹配条件。

**设计目标**：
- 将复杂的插件配置拆分为多个简单步骤
- 每个步骤可以依赖前面步骤的结果
- 支持步骤间的前进和后退
- 最终将所有步骤的配置组装成完整的插件实例

## 核心概念

### 1. 宿主插件 (Host Plugin)
实现 `MultiStepsSupportHost` 接口的插件，作为多个步骤插件的容器。

**职责**：
- 持久化所有步骤插件的配置
- 提供统一的访问接口

**示例**：`JoinerUDF` - 表Join的UDF插件

### 2. 步骤插件 (Step Plugin)
继承 `OneStepOfMultiSteps` 抽象类的插件，代表配置流程中的一个步骤。

**职责**：
- 处理当前步骤的业务逻辑
- 可以访问前面步骤的结果
- 定义下一步骤的描述符

**示例**：
- `JoinerSelectDataSource` - 第一步：选择数据源
- `JoinerSelectTable` - 第二步：选择表
- `JoinerSetMatchConditionAndCols` - 第三步：设置匹配条件和列

### 3. 步骤描述符 (Step Descriptor)
`OneStepOfMultiSteps.BasicDesc` 的子类，描述步骤的元信息。

**职责**：
- 定义步骤的显示名称和描述
- 指定步骤索引
- 返回下一步的描述符（如果有）

## 架构设计

### 前端架构

```
┌─────────────────────────────────────────────────────────┐
│         PluginsMultiStepsComponent (容器组件)            │
│  ┌───────────────────────────────────────────────────┐  │
│  │  左侧：步骤导航 (nz-steps)                         │  │
│  │  - 显示所有步骤                                    │  │
│  │  - 高亮当前步骤                                    │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────┐  │
│  │  右侧：步骤内容                                    │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │  tis-steps-tools-bar (工具栏)               │  │  │
│  │  │  - 后退按钮                                  │  │  │
│  │  │  - 下一步按钮                                │  │  │
│  │  │  - 提交按钮 (最后一步)                       │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │  tis-plugins (插件表单)                     │  │  │
│  │  │  - 动态渲染当前步骤的表单                    │  │  │
│  │  │  - 支持各种表单控件                          │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

**核心组件**：

1. **PluginsMultiStepsComponent** (`plugins.multi.steps.component.ts`)
   - 管理步骤流程和状态
   - 处理步骤切换逻辑
   - 保存历史步骤数据

2. **TableJoinMatchConditionComponent** (`table.join.match.condition.component.ts`)
   - 专门处理表Join匹配条件的UI组件
   - 支持添加/删除多个匹配条件
   - 列选择和验证

**核心数据模型**：

```typescript
// 多步骤描述符
class MultiStepsDescriptor extends Descriptor {
    steps: Array<StepConfig>;           // 所有步骤的配置
    firstDesc: Descriptor;              // 第一步的描述符
    stepExecContext: {[key: string]: any}; // 步骤执行上下文
}

// 步骤配置
class StepConfig {
    name: string;        // 步骤名称，如"第一步"
    description: string; // 步骤描述
}

// 历史保存的步骤
class HistorySavedStep {
    hlist: HeteroList[];  // 插件列表
    finalStep: boolean;   // 是否是最后一步
}
```

### 后端架构

```
┌─────────────────────────────────────────────────────────┐
│           MultiStepsSupportHost (宿主插件接口)          │
│  + setSteps(OneStepOfMultiSteps[])                      │
│  + getMultiStepsSavedItems(): OneStepOfMultiSteps[]     │
└─────────────────────────────────────────────────────────┘
                          △
                          │ implements
                          │
┌─────────────────────────────────────────────────────────┐
│                    JoinerUDF (具体实现)                  │
│  - stepsPlugin: OneStepOfMultiSteps[]                   │
│  + getLiteria(): List<UDFDesc>                          │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│      MultiStepsSupportHostDescriptor (宿主描述符接口)    │
│  + getStepDescriptionList(): List<BasicDesc>            │
│  + appendExternalProps(JSONObject)                      │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│         OneStepOfMultiSteps (步骤插件抽象类)             │
│  + manipuldateProcess(IPluginContext, Context)          │
│  # processPreSaved(IPluginContext, Context, [])         │
└─────────────────────────────────────────────────────────┘
                          △
                          │ extends
          ┌───────────────┼───────────────┐
          │               │               │
┌─────────────────┐ ┌─────────────┐ ┌──────────────────┐
│JoinerSelectData │ │JoinerSelect │ │JoinerSetMatch    │
│Source           │ │Table        │ │ConditionAndCols  │
│(第一步)         │ │(第二步)     │ │(第三步)          │
└─────────────────┘ └─────────────┘ └──────────────────┘
```

**核心接口和类**：

1. **MultiStepsSupportHost** - 宿主插件接口
   ```java
   public interface MultiStepsSupportHost {
       void setSteps(OneStepOfMultiSteps[] stepsPlugin);
       OneStepOfMultiSteps[] getMultiStepsSavedItems();
       <T extends OneStepOfMultiSteps> T getOneStepOf(Step step);
   }
   ```

2. **MultiStepsSupportHostDescriptor** - 宿主描述符接口
   ```java
   public interface MultiStepsSupportHostDescriptor<T> {
       Class<T> getHostClass();
       List<OneStepOfMultiSteps.BasicDesc> getStepDescriptionList();
       void appendExternalProps(JSONObject multiStepsCfg);
   }
   ```

3. **OneStepOfMultiSteps** - 步骤插件抽象类
   ```java
   public abstract class OneStepOfMultiSteps {
       // 处理步骤逻辑的核心方法
       public final void manipuldateProcess(IPluginContext, Optional<Context>);

       // 子类实现：处理前置步骤数据
       protected void processPreSaved(IPluginContext, Context, OneStepOfMultiSteps[]);

       // 获取前一步的插件实例
       public static <T> T getPreviousStepInstance(Class<T> pluginClass);
   }
   ```

## 数据流程

### 完整流程图

```
用户操作                前端                    后端
   │                     │                       │
   │  1. 打开多步骤配置   │                       │
   ├──────────────────>│                       │
   │                     │  2. 请求第一步描述符   │
   │                     ├──────────────────────>│
   │                     │                       │ 3. 返回第一步Descriptor
   │                     │<──────────────────────┤    和步骤列表
   │                     │                       │
   │  4. 填写第一步表单   │                       │
   ├──────────────────>│                       │
   │                     │                       │
   │  5. 点击"下一步"    │                       │
   ├──────────────────>│                       │
   │                     │  6. 提交第一步数据     │
   │                     ├──────────────────────>│
   │                     │                       │ 7. 保存第一步
   │                     │                       │    返回第二步Descriptor
   │                     │<──────────────────────┤    和当前保存的数据
   │                     │                       │
   │  8. 填写第二步表单   │                       │
   ├──────────────────>│                       │
   │                     │                       │
   │  9. 点击"下一步"    │                       │
   ├──────────────────>│                       │
   │                     │  10. 提交第二步数据    │
   │                     │     (包含第一步数据)   │
   │                     ├──────────────────────>│
   │                     │                       │ 11. 保存第二步
   │                     │                       │     返回第三步Descriptor
   │                     │<──────────────────────┤
   │                     │                       │
   │  12. 填写第三步表单  │                       │
   ├──────────────────>│                       │
   │                     │                       │
   │  13. 点击"提交"     │                       │
   ├──────────────────>│                       │
   │                     │  14. 提交第三步数据    │
   │                     ├──────────────────────>│
   │                     │                       │ 15. 保存第三步
   │                     │                       │     标记为最后一步
   │                     │<──────────────────────┤
   │                     │                       │
   │                     │  16. 提交宿主插件      │
   │                     │     (包含所有步骤)     │
   │                     ├──────────────────────>│
   │                     │                       │ 17. 组装并持久化
   │                     │                       │     完整的插件实例
   │                     │<──────────────────────┤
   │                     │                       │
   │  18. 显示成功消息   │                       │
   │<────────────────────┤                       │
```

### 前端数据流

**步骤切换流程**：

1. **用户点击"下一步"**
   ```typescript
   createStepNext() {
       // 1. 创建保存事件
       let evt = new SavePluginEvent();

       // 2. 将Map结构的历史步骤转换为数组
       let postStepSavedPlugin = this.convertStepSavedPluginToArray();

       // 3. 添加步骤执行上下文
       evt.postPayload = {
           stepSavedPlugin: postStepSavedPlugin,
           ...this.hostDesc.stepExecContext
       };

       // 4. 触发保存事件
       this.savePlugin.emit(evt);
   }
   ```

2. **接收后端响应**
   ```typescript
   afterSave($event: PluginSaveResponse) {
       // 1. 检查保存是否成功
       if (!$event.saveSuccess) return;

       // 2. 保存当前步骤到历史
       this.saveCurrentStepToHistory($event.biz());

       // 3. 准备下一步
       if (hasNextStep) {
           this.prepareNextStep($event.biz());
           this.currentStep++;
       } else {
           this.isFinalPhase = true;
       }

       // 4. 如果是最终提交，执行宿主插件保存
       if (this.pendingFinalSubmit) {
           this.submitFinalForm();
       }
   }
   ```

3. **最终提交**
   ```typescript
   submitFinalForm() {
       // 1. 创建宿主插件的HeteroList
       let hostHetero: HeteroList[] = [];

       // 2. 将所有步骤的Item组装到宿主插件中
       let hostItem = new Item(this.hostDesc);
       hostItem.vals[KEY_MULTI_STEPS_SAVED_ITEMS] = this.getAllStepItems();

       // 3. 提交到后端
       PluginsComponent.postHeteroList(...);
   }
   ```

### 后端数据流

**步骤处理流程**：

1. **接收步骤数据**
   ```java
   public void manipuldateProcess(IPluginContext pluginContext, Optional<Context> context) {
       Context currentCtx = context.orElseThrow();

       // 1. 解析前端提交的历史步骤数据
       JSONArray preStepSavedPlugin = pluginContext.getJSONPostContent()
           .getJSONArray(KEY_STEP_SAVED_PLUGIN);

       // 2. 转换为步骤插件数组
       OneStepOfMultiSteps[] preSavedStepPlugins = parseStepsPlugin(...);

       // 3. 调用子类的业务逻辑处理方法
       this.processPreSaved(pluginContext, currentCtx, preSavedStepPlugins);

       // 4. 将当前步骤实例放入上下文，供下一步使用
       currentCtx.put(this.getClass().getName(), this);

       // 5. 构建返回结果
       JSONObject result = buildStepResult(currentCtx);
       pluginContext.setBizResult(currentCtx, result);
   }
   ```

2. **构建返回结果**
   ```java
   private JSONObject buildStepResult(Context currentCtx) {
       JSONObject saved = new JSONObject();

       // 1. 保存当前步骤的数据
       DescribableJSON pluginJSON = new DescribableJSON(this);
       saved.put(KEY_CURRENT_SAVED, pluginJSON.getItemJson());
       saved.put(KEY_CURRENT_STEP_INDEX, descriptor.getStep().stepIndex);

       // 2. 如果有下一步，返回下一步的描述符
       Optional<BasicDesc> nextPluginDesc = descriptor.nextPluginDesc();
       if (nextPluginDesc.isPresent()) {
           BasicDesc nextDesc = nextPluginDesc.get();
           DefaultDescriptorsJSON desc2Json = new DefaultDescriptorsJSON(nextDesc);
           saved.put(KEY_NEXT_STEP_PLUGIN_DESC, desc2Json.getDescriptorsJSON());
           saved.put(KEY_NEXT_STEP_PLUGIN_INDEX, nextDesc.getStep().stepIndex);
           saved.put(KEY_FINAL_STEP, nextDesc.nextPluginDesc().isEmpty());
       }

       return saved;
   }
   ```

3. **最终组装**
   ```java
   // 在 Descriptor.newInstance() 方法中
   if (instance instanceof MultiStepsSupportHost) {
       // 1. 从请求中获取所有步骤的数据
       JSONArray stepsArray = req.getJSONArray(KEY_MULTI_STEPS_SAVED_ITEMS);

       // 2. 反序列化为步骤插件实例
       OneStepOfMultiSteps[] steps = new OneStepOfMultiSteps[stepsArray.size()];
       for (int i = 0; i < stepsArray.size(); i++) {
           steps[i] = parseStepPlugin(stepsArray.getJSONObject(i));
       }

       // 3. 设置到宿主插件中
       ((MultiStepsSupportHost) instance).setSteps(steps);
   }
   ```

## 关键设计点

### 1. 步骤间数据传递

**问题**：后面的步骤需要访问前面步骤的结果

**解决方案**：
- 前端：使用 `stepSavedPlugin: Map<number, HistorySavedStep>` 保存所有历史步骤
- 后端：通过 `Context` 传递步骤实例，使用 `getPreviousStepInstance()` 获取

**示例**：
```java
// 第二步需要访问第一步选择的数据源
public class JoinerSelectTable extends OneStepOfMultiSteps {
    public static List<Option> selectableTabs() {
        // 获取第一步的实例
        JoinerSelectDataSource prevPlugin =
            getPreviousStepInstance(JoinerSelectDataSource.class);

        // 使用第一步的结果
        return prevPlugin.getDataSourceFactory().getTablesInDB().getTabs();
    }
}
```

### 2. 步骤状态管理

**问题**：用户可能需要后退到前面的步骤修改

**解决方案**：
- 使用 `HistorySavedStep` 保存每个步骤的完整状态
- 支持 `goBack()` 方法恢复历史状态

```typescript
goBack() {
    // 1. 步骤索引减1
    this.currentStep--;

    // 2. 从历史中恢复该步骤的状态
    let preHlist = this.stepSavedPlugin.get(this.currentStep);
    this._hlist = preHlist.hlist;
    this.isFinalPhase = preHlist.finalStep;
}
```

### 3. 最终提交的两阶段处理

**问题**：最后一步需要先保存当前步骤，再提交整个宿主插件

**解决方案**：使用 `pendingFinalSubmit` 标志位

```typescript
saveForm($event: MouseEvent) {
    // 1. 设置标志位
    this.pendingFinalSubmit = true;

    // 2. 先保存当前步骤
    this.createStepNext();
}

afterSave($event: PluginSaveResponse) {
    // ...保存当前步骤...

    // 3. 检查标志位，执行最终提交
    if (this.pendingFinalSubmit) {
        this.pendingFinalSubmit = false;
        this.submitFinalForm();
    }
}
```

### 4. 描述符的动态生成

**问题**：每个步骤的表单结构需要动态生成

**解决方案**：
- 后端：通过 `DescriptorsJSON.multiStepsPropsSet()` 生成多步骤配置
- 前端：通过 `Descriptor.wrapDescriptors()` 解析并包装

```java
// 后端生成
private void multiStepsPropsSet(MultiStepsHostPluginFormProperties props) {
    JSONObject multiStepsCfg = new JSONObject();

    // 1. 生成步骤列表
    JSONArray steps = new JSONArray();
    for (OneStepOfMultiSteps.BasicDesc stepDesc : props.getStepDescriptionList()) {
        JSONObject step = new JSONObject();
        step.put("stepName", stepDesc.getDisplayName());
        step.put("stepDescription", stepDesc.getStepDescription());
        steps.add(step);
    }
    multiStepsCfg.put("multiSteps", steps);

    // 2. 生成第一步的描述符
    multiStepsCfg.put("firstStepDesc", firstStepDescriptorJSON);

    // 3. 添加执行上下文
    multiStepsCfg.put("context", stepContext);

    desJson.put("multiStepsCfg", multiStepsCfg);
}
```

```typescript
// 前端解析
public static wrapDescriptors(descriptors: Map<string, Descriptor>) {
    for (let impl in descriptors) {
        let rawDesc = descriptors[impl];
        let stepsCfg = rawDesc["multiStepsCfg"];

        if (stepsCfg) {
            // 创建 MultiStepsDescriptor
            let stepDesc = new MultiStepsDescriptor(
                stepsCfg["multiSteps"].map(s => new StepConfig(s.stepName, s.stepDescription)),
                firstDesc,
                stepsCfg["context"]
            );
            return stepDesc;
        }
    }
}
```

## 相关文件清单

### 前端文件

| 文件路径 | 说明 |
|---------|------|
| `src/common/plugins.multi.steps.component.ts` | 多步骤容器组件 |
| `src/common/tis.plugin.ts` | 插件数据模型和工具类 |
| `src/common/multi-selected/table.join.match.condition.component.ts` | 表Join匹配条件组件 |

### 后端文件

| 文件路径 | 说明 |
|---------|------|
| `tis-plugin/src/main/java/com/qlangtech/tis/extension/MultiStepsSupportHost.java` | 宿主插件接口 |
| `tis-plugin/src/main/java/com/qlangtech/tis/extension/MultiStepsSupportHostDescriptor.java` | 宿主描述符接口 |
| `tis-plugin/src/main/java/com/qlangtech/tis/extension/OneStepOfMultiSteps.java` | 步骤插件抽象类 |
| `tis-plugin/src/main/java/com/qlangtech/tis/extension/impl/MultiStepsHostPluginFormProperties.java` | 宿主插件表单属性 |
| `tis-plugin/src/main/java/com/qlangtech/tis/util/DescriptorsJSON.java` | 描述符JSON生成器 |
| `plugins/tis-transformer/src/main/java/com/qlangtech/tis/plugin/datax/transformer/impl/JoinerUDF.java` | JoinerUDF宿主插件实现 |
| `plugins/tis-transformer/src/main/java/com/qlangtech/tis/plugin/datax/transformer/impl/joiner/JoinerSelectDataSource.java` | 第一步：选择数据源 |
| `plugins/tis-transformer/src/main/java/com/qlangtech/tis/plugin/datax/transformer/impl/joiner/JoinerSelectTable.java` | 第二步：选择表 |
| `plugins/tis-transformer/src/main/java/com/qlangtech/tis/plugin/datax/transformer/impl/joiner/JoinerSetMatchConditionAndCols.java` | 第三步：设置匹配条件和列 |

## 常见问题

### Q1: 如何添加新的步骤？

1. 创建新的 `OneStepOfMultiSteps` 子类
2. 在前一步的 `BasicDesc.nextPluginDesc()` 中返回新步骤的描述符
3. 在宿主描述符的 `getStepDescriptionList()` 中添加新步骤

### Q2: 步骤之间如何传递复杂数据？

使用 `Context` 对象：
```java
// 在当前步骤中设置数据
currentCtx.put("myData", complexObject);

// 在下一步中获取数据
Object data = currentCtx.get("myData");
```

### Q3: 如何在前端自定义步骤的UI？

通过 `FormFieldType` 指定表单类型，或者创建自定义组件：
```java
@FormField(ordinal = 0, type = FormFieldType.MULTI_SELECTABLE)
public List<TableJoinMatchCondition> matchCondition;
```

### Q4: 如何调试多步骤流程？

1. 前端：在 `afterSave()` 方法中打印 `$event.biz()`
2. 后端：在 `manipuldateProcess()` 方法中打印 `JSONObject`
3. 使用浏览器开发者工具查看网络请求

## 最佳实践

1. **步骤数量**：建议不超过5个步骤，太多会影响用户体验
2. **步骤命名**：使用清晰的名称，如"第一步：选择数据源"
3. **错误处理**：每个步骤都应该有完善的表单验证
4. **性能优化**：避免在步骤切换时重复加载数据
5. **测试**：为每个步骤编写单元测试

## 版本历史

- **v1.0** (2026-01-21): 初始版本，支持基本的多步骤配置功能
- 作者：百岁 (baisui@qlangtech.com)
