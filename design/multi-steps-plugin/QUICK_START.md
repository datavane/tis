# 多步骤插件快速开发指南

本文档提供快速开发一个新的多步骤插件的步骤说明。

## 开发步骤

### 第一步：创建宿主插件类

创建一个实现 `MultiStepsSupportHost` 接口的类：

```java
package com.qlangtech.tis.plugin.datax.transformer.impl;

import com.qlangtech.tis.extension.MultiStepsSupportHost;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;

/**
 * 示例：创建一个数据转换的多步骤插件
 */
public class MyTransformerUDF extends UDFDefinition implements MultiStepsSupportHost {

    // 保存所有步骤的插件实例
    private OneStepOfMultiSteps[] stepsPlugin;

    @Override
    public void setSteps(OneStepOfMultiSteps[] stepsPlugin) {
        this.stepsPlugin = stepsPlugin;
    }

    @Override
    public OneStepOfMultiSteps[] getMultiStepsSavedItems() {
        return this.stepsPlugin;
    }

    // 实现你的业务逻辑方法
    @Override
    public void evaluate(ColumnAwareRecord record) {
        // 使用步骤插件的配置进行数据转换
        MyStep1 step1 = this.getOneStepOf(OneStepOfMultiSteps.Step.Step1);
        MyStep2 step2 = this.getOneStepOf(OneStepOfMultiSteps.Step.Step2);

        // ... 你的业务逻辑
    }
}
```

### 第二步：创建宿主插件描述符

在宿主插件类中添加静态内部类，实现 `MultiStepsSupportHostDescriptor`：

```java
@TISExtension
public static class DefaultDescriptor extends UDFDefinition.BasicUDFDesc
        implements MultiStepsSupportHostDescriptor<MyTransformerUDF> {

    @Override
    public Class<MyTransformerUDF> getHostClass() {
        return MyTransformerUDF.class;
    }

    @Override
    public List<OneStepOfMultiSteps.BasicDesc> getStepDescriptionList() {
        // 定义所有步骤
        return Lists.newArrayList(
            new MyStep1.Desc(),
            new MyStep2.Desc(),
            new MyStep3.Desc()
        );
    }

    @Override
    public void appendExternalProps(JSONObject multiStepsCfg) {
        // 可选：添加额外的上下文数据到前端
        // 例如：传递可选的列信息
        JSONArray availableCols = new JSONArray();
        // ... 填充数据
        multiStepsCfg.put("availableCols", availableCols);
    }

    @Override
    public String getDisplayName() {
        return "My Transformer";
    }
}
```

### 第三步：创建步骤插件类

为每个步骤创建一个继承 `OneStepOfMultiSteps` 的类：

#### 步骤1：选择数据源

```java
package com.qlangtech.tis.plugin.datax.transformer.impl.mysteps;

import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

import java.util.Optional;

/**
 * 第一步：选择数据源
 */
public class MyStep1 extends OneStepOfMultiSteps {

    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String dataSourceName;

    @Override
    protected void processPreSaved(IPluginContext pluginContext, Context currentCtx,
                                   OneStepOfMultiSteps[] preSavedStepPlugins) {
        // 可选：处理当前步骤保存前的逻辑
        // 例如：将数据源的元信息放入上下文，供下一步使用
        DataSourceFactory ds = DataSourceFactory.load(this.dataSourceName);
        currentCtx.put("dataSourceFactory", ds);
    }

    @TISExtension
    public static class Desc extends OneStepOfMultiSteps.BasicDesc {

        @Override
        public Step getStep() {
            return Step.Step1;
        }

        @Override
        public String getDisplayName() {
            return "第一步";
        }

        @Override
        public String getStepDescription() {
            return "选择数据源";
        }

        @Override
        public Optional<BasicDesc> nextPluginDesc() {
            // 返回下一步的描述符
            return Optional.of(new MyStep2.Desc());
        }
    }
}
```

#### 步骤2：选择表

```java
/**
 * 第二步：选择表
 */
public class MyStep2 extends OneStepOfMultiSteps {

    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String tableName;

    /**
     * 动态获取可选的表列表（依赖第一步的结果）
     */
    public static List<Option> selectableTables() {
        // 获取第一步的实例
        MyStep1 step1 = getPreviousStepInstance(MyStep1.class);

        // 使用第一步的数据源获取表列表
        DataSourceFactory ds = DataSourceFactory.load(step1.dataSourceName);
        return ds.getTablesInDB().getTabs().stream()
            .map(Option::new)
            .collect(Collectors.toList());
    }

    @Override
    protected void processPreSaved(IPluginContext pluginContext, Context currentCtx,
                                   OneStepOfMultiSteps[] preSavedStepPlugins) {
        // 将表的列信息放入上下文
        MyStep1 step1 = (MyStep1) preSavedStepPlugins[Step.Step1.getStepIndex()];
        DataSourceFactory ds = DataSourceFactory.load(step1.dataSourceName);

        try {
            List<ColumnMetaData> cols = ds.getTableMetadata(
                false, null, EntityName.parse(this.tableName));
            currentCtx.put("tableCols", cols);
        } catch (TableNotFoundException e) {
            throw new RuntimeException("Table not found: " + this.tableName, e);
        }
    }

    @TISExtension
    public static class Desc extends OneStepOfMultiSteps.BasicDesc {

        @Override
        public Step getStep() {
            return Step.Step2;
        }

        @Override
        public String getDisplayName() {
            return "第二步";
        }

        @Override
        public String getStepDescription() {
            return "选择表";
        }

        @Override
        public Optional<BasicDesc> nextPluginDesc() {
            return Optional.of(new MyStep3.Desc());
        }
    }
}
```

#### 步骤3：配置转换规则（最后一步）

```java
/**
 * 第三步：配置转换规则
 */
public class MyStep3 extends OneStepOfMultiSteps {

    @FormField(ordinal = 0, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
    public List<CMeta> selectedCols;

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String transformRule;

    /**
     * 获取可选的列（依赖第二步的结果）
     */
    public static List<CMeta> getSelectableCols() {
        // 从上下文中获取第二步保存的列信息
        IPluginContext pluginContext = IPluginContext.getThreadLocalInstance();
        List<ColumnMetaData> cols = (List<ColumnMetaData>)
            pluginContext.getContext().get("tableCols");

        return cols.stream()
            .map(col -> new CMeta(col.getName(), col.getType()))
            .collect(Collectors.toList());
    }

    @Override
    protected void processPreSaved(IPluginContext pluginContext, Context currentCtx,
                                   OneStepOfMultiSteps[] preSavedStepPlugins) {
        // 最后一步通常不需要处理
    }

    @TISExtension
    public static class Desc extends OneStepOfMultiSteps.BasicDesc {

        @Override
        public Step getStep() {
            return Step.Step3;
        }

        @Override
        public String getDisplayName() {
            return "第三步";
        }

        @Override
        public String getStepDescription() {
            return "配置转换规则";
        }

        @Override
        public Optional<BasicDesc> nextPluginDesc() {
            // 最后一步返回 empty
            return Optional.empty();
        }
    }
}
```

### 第四步：前端集成（可选）

如果需要自定义UI组件，可以在前端创建：

```typescript
// 在 src/common/multi-selected/ 目录下创建自定义组件
@Component({
  selector: 'my-custom-step-component',
  template: `
    <!-- 你的自定义UI -->
  `
})
export class MyCustomStepComponent {
  // 组件逻辑
}
```

然后在 `tis.plugin.ts` 中注册组件类型。

## 完整示例：表Join插件

参考现有的 `JoinerUDF` 实现：

```
plugins/tis-transformer/src/main/java/com/qlangtech/tis/plugin/datax/transformer/impl/
├── JoinerUDF.java                          # 宿主插件
└── joiner/
    ├── JoinerSelectDataSource.java         # 第一步
    ├── JoinerSelectTable.java              # 第二步
    └── JoinerSetMatchConditionAndCols.java # 第三步
```

## 常用注解说明

### @FormField

定义表单字段：

```java
@FormField(
    ordinal = 0,                    // 字段顺序
    type = FormFieldType.INPUTTEXT, // 字段类型
    validate = {Validator.require}  // 验证规则
)
public String fieldName;
```

**常用字段类型**：
- `INPUTTEXT`: 文本输入框
- `ENUM`: 下拉选择框
- `MULTI_SELECTABLE`: 多选列表
- `TEXTAREA`: 多行文本框
- `INT_NUMBER`: 整数输入框

**常用验证规则**：
- `Validator.require`: 必填
- `Validator.db_col_name`: 数据库列名格式
- `Validator.integer`: 整数
- `Validator.url`: URL格式

### @TISExtension

标记为TIS扩展点，必须添加到Descriptor类上：

```java
@TISExtension
public static class Desc extends OneStepOfMultiSteps.BasicDesc {
    // ...
}
```

## 调试技巧

### 1. 前端调试

在 `PluginsMultiStepsComponent.afterSave()` 方法中添加日志：

```typescript
afterSave($event: PluginSaveResponse) {
    console.log('Step save response:', $event.biz());
    // ...
}
```

### 2. 后端调试

在 `OneStepOfMultiSteps.manipuldateProcess()` 方法中添加日志：

```java
@Override
public final void manipuldateProcess(IPluginContext pluginContext, Optional<Context> context) {
    System.out.println("Processing step: " + this.getClass().getSimpleName());
    JSONObject postContent = pluginContext.getJSONPostContent();
    System.out.println("Post content: " + postContent.toJSONString());
    // ...
}
```

### 3. 查看网络请求

使用浏览器开发者工具的Network标签，查看：
- 请求URL
- 请求Payload
- 响应数据

## 常见错误及解决方案

### 错误1：步骤插件找不到

**错误信息**：`Previous step plugin for pluginClass:xxx can not be null`

**原因**：前一步的插件实例没有正确保存到Context中

**解决方案**：
1. 检查 `OneStepOfMultiSteps.manipuldateProcess()` 是否正确执行
2. 确认 `currentCtx.put(this.getClass().getName(), this)` 被调用

### 错误2：描述符未注册

**错误信息**：`No descriptor found for class xxx`

**原因**：忘记添加 `@TISExtension` 注解

**解决方案**：在Descriptor类上添加 `@TISExtension` 注解

### 错误3：前端无法显示下一步

**错误信息**：前端停留在当前步骤

**原因**：
1. 后端没有返回 `nextStepPluginDesc`
2. `nextPluginDesc()` 返回了null而不是Optional.empty()

**解决方案**：
1. 检查 `BasicDesc.nextPluginDesc()` 的返回值
2. 最后一步应该返回 `Optional.empty()`

### 错误4：表单验证失败

**错误信息**：字段显示红色错误提示

**原因**：
1. 字段值不符合验证规则
2. 动态选项方法返回空列表

**解决方案**：
1. 检查 `@FormField` 的 `validate` 属性
2. 确认动态选项方法（如 `selectableTables()`）返回有效数据

## 测试清单

开发完成后，按以下清单测试：

### 创建模式测试
- [ ] 第一步可以正常显示和保存
- [ ] 第二步可以访问第一步的数据
- [ ] 第三步可以访问前面步骤的数据
- [ ] 可以后退到前面的步骤
- [ ] 后退后修改数据，再前进，数据正确更新
- [ ] 最后一步可以成功提交
- [ ] 提交后可以正确保存到数据库
- [ ] 表单验证正常工作
- [ ] 错误提示清晰明确

### 编辑模式测试
- [ ] 点击"编辑"按钮后，自动跳转到最后一步
- [ ] 最后一步显示之前保存的配置数据
- [ ] 可以修改最后一步的配置
- [ ] 可以后退到前面的步骤查看/修改
- [ ] 后退后的步骤显示正确的历史数据
- [ ] 修改任意步骤后，可以前进到后续步骤
- [ ] 提交后更新成功
- [ ] 重新打开编辑，仍然跳转到最后一步
- [ ] 所有步骤的 descriptor 正确加载
- [ ] 表单验证在编辑模式下正常工作

## 编辑模式开发注意事项

### 1. 确保 processCurrentStep() 方法正确实现

编辑模式依赖 `processCurrentStep()` 方法来设置上下文:

```java
@Override
protected void processPreSaved(IPluginContext pluginContext, Context currentCtx,
                               OneStepOfMultiSteps[] preSavedStepPlugins) {
    // 从前置步骤获取数据
    if (preSavedStepPlugins.length > 0) {
        JoinerSelectDataSource dataSource = (JoinerSelectDataSource) preSavedStepPlugins[0];
        // 使用 dataSource 的数据
    }

    // 设置当前步骤的数据到 Context
    currentCtx.put("myKey", myData);
}
```

### 2. 前端调用时传递 editMode 标志

```typescript
// 添加新插件
TransformerRulesComponent.openTransformerRuleDialog(this, desc, undefined, false);

// 编辑已有插件
TransformerRulesComponent.openTransformerRuleDialog(this, udfItem.dspt, udfItem, true);
```

### 3. 测试编辑模式的常见场景

1. **场景1：只修改最后一步**
   - 打开编辑 → 修改最后一步 → 提交
   - 验证：只有最后一步的数据被更新

2. **场景2：修改中间步骤**
   - 打开编辑 → 后退到第二步 → 修改 → 前进到第三步 → 提交
   - 验证：第二步和第三步的数据都被更新

3. **场景3：修改第一步**
   - 打开编辑 → 后退到第一步 → 修改 → 逐步前进 → 提交
   - 验证：所有步骤的数据都被更新

### 4. 调试技巧

#### 前端调试

在 `PluginsMultiStepsComponent.ngOnInit()` 中添加日志:

```typescript
ngOnInit() {
    super.ngOnInit();
    console.log('editMode:', this.editMode);
    console.log('stepSavedPlugin:', this.stepSavedPlugin);

    if (this.editMode) {
        let newCurrent: number = (this.hostDesc.steps.length - 1);
        console.log('Jumping to step:', newCurrent);
        // ...
    }
}
```

#### 后端调试

在 `MultiStepsHostPluginFormProperties.getInstancePropsJson()` 中添加日志:

```java
@Override
public JSON getInstancePropsJson(Object instance) {
    try {
        // ...
        for (OneStepOfMultiSteps childStep : multiSteps) {
            System.out.println("Processing step: " + childStep.getClass().getSimpleName());
            childStep.processCurrentStep(threadLocalInstance, context, allSteps);
        }
        // ...
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
    }
}
```

### 5. 常见编辑模式错误

#### 错误1：编辑时显示第一步而不是最后一步

**原因**：
- `editMode` 标志没有正确传递
- `stepSavedPlugin` Map 为空

**解决方案**：
1. 检查 `openTransformerRuleDialog()` 调用时是否传递了 `editMode=true`
2. 检查 `rebuildStepSavedPlugin()` 方法是否正确执行
3. 在浏览器控制台查看 `editMode` 和 `stepSavedPlugin` 的值

#### 错误2：编辑时表单数据为空

**原因**：
- 后端没有返回 `allStepDesc`
- 前端没有正确包装 descriptor

**解决方案**：
1. 检查后端 `getInstancePropsJson()` 是否调用了 `processCurrentStep()`
2. 检查前端 `tis.plugin.ts` 中的 `wrapItemVals()` 逻辑
3. 在网络请求中查看返回的 JSON 数据

#### 错误3：后退到前面步骤时数据丢失

**原因**：
- `HistorySavedStep` 没有正确保存
- `wrapper()` 方法没有被调用

**解决方案**：
1. 检查 `rebuildStepSavedPlugin()` 中是否调用了 `historyStep.wrapper(d)`
2. 确认每个步骤的 `item.dspt` 不为 null

## 性能优化建议

1. **避免重复查询**：将查询结果缓存到Context中
2. **延迟加载**：只在需要时才加载数据
3. **分页处理**：如果选项很多，考虑分页或搜索
4. **异步加载**：对于耗时操作，考虑异步处理

## 下一步

- 阅读 [架构说明文档](README.md) 了解详细设计
- 参考 `JoinerUDF` 的完整实现
- 查看 TIS 插件开发文档

## 联系方式

如有问题，请联系：
- 作者：百岁 (baisui@qlangtech.com)
- 项目地址：https://github.com/datavane/tis
