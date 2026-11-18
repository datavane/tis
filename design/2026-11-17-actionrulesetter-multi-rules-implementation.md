# ActionRuleSetter 多规则支持详细实现指南

> **文档版本**: 2026-11-17  
> **目标**: 将 `ActionRuleSetter.actionType` 从单个 `ActionType` 改造为 `MULTI_DESCRIBABLE_PLUGIN` 规范的多规则列表  
> **参考案例**: `FilterItemConfig` 改造（memory: `project_tuplelist_to_multi_describle_migration.md`）

---

## 一、改造背景与目标

### 1.1 Palantir 设计要求

根据 Palantir 官方文档明确说明：

- **一个 action type 可以组合多个 rules**（`explore-action-types.md:26`）
- 多个规则会被编译成对每个对象的单个编辑操作（`rules.md:43`）
- 规则执行顺序会影响最终结果
- **唯一例外**：Function 规则不能与其他 Ontology 规则组合

### 1.2 当前实现的问题

```java
// ❌ 当前实现：只支持单个规则
@FormField(ordinal = 0, validate = {Validator.require})
public ActionType actionType;
```

### 1.3 目标架构

```java
// ✅ 目标实现：支持多个规则
@FormField(ordinal = 0, type = FormFieldType.MULTI_DESCRIBLE_PLUGIN,
           desClazz = ActionType.class, validate = {Validator.require})
public List<ActionType> actionTypes;
```

---

## 二、后端改造（5 处核心改动）

### 2.1 ActionType 实现 MultiDescribleElement 接口

**文件**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/impl/action/type/ActionType.java`

#### 2.1.1 实现接口

```java
import com.qlangtech.tis.plugin.IPluginStore;
import java.io.Serializable;

public abstract class ActionType implements Describable<ActionType>, 
        IPluginStore.MultiDescribleElement, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 添加 identity 字段（用于表格行的唯一标识）
    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT, 
               validate = {Validator.require, Validator.identity})
    public String identity;
    
    /**
     * 实现 MultiDescribleElement 接口要求的方法
     * 返回该规则在列表中的唯一标识
     */
    @Override
    public String identityValue() {
        return this.identity;
    }
    
    // ... 原有方法保持不变
}
```

#### 2.1.2 为什么需要 identity 字段

- `MULTI_DESCRIBABLE_PLUGIN` 要求每行有唯一标识
- `identity` 字段用于表格行的键，防止重复
- 前端会用它做 `trackBy`，提升渲染性能
- **注意**：`@Validator.identity` 会拒绝中文，建议使用英文或数字组合

#### 2.1.3 identity 字段的默认值策略

可以在构造函数中生成默认值：

```java
public ActionType() {
    // 自动生成唯一 ID（格式：rule_<timestamp>_<random>）
    if (this.identity == null) {
        this.identity = "rule_" + System.currentTimeMillis() + "_" + 
                        (int)(Math.random() * 10000);
    }
}
```

### 2.2 ActionType.BaseActionTypeDescriptor 扩展 MultiDescribleElementDescriptor

**文件**: 同上 `ActionType.java`

```java
/**
 * ActionType 基础 Descriptor
 */
public static abstract class BaseActionTypeDescriptor 
        extends IPluginStore.MultiDescribleElementDescriptor<ActionType>
        implements DescriptorUseableShortComment {
    
    /**
     * 必须调用父类构造器
     */
    public BaseActionTypeDescriptor() {
        super();
        // 如果有 SELECTABLE 类型的字段，需要在这里注册候选项来源
        // 例如：this.registerSelectOptions("someField", Collections::emptyList);
    }
    
    /**
     * 获取该 ActionType 所属的规则类别
     */
    public abstract RuleGroup getRuleGroup();

    @Override
    public final String getDisplayName() {
        return getRuleGroup().getDisplayName();
    }

    /**
     * 获取该 ActionType 的过滤标识
     * 默认返回 RuleGroup 名称，子类可重写以提供更精确的过滤
     */
    public String getRuleTypeFilter() {
        return getRuleGroup().name();
    }

    @Override
    public final String shortComment() {
        return getRuleGroup().getDescription();
    }
    
    /**
     * 配置表格视图样式（Table 视图）
     */
    @Override
    public ViewStyle viewStyle() {
        return ViewStyle.Table;
    }
    
    /**
     * 配置表格列
     * 必须至少有一列 setClickable()，否则用户无法打开行编辑面板
     */
    @Override
    public List<ColConfig> colsConfig() {
        return Lists.newArrayList(
            new ColConfig("identity", 20).setClickable(),  // 标识列，可点击编辑
            new ColConfig("ruleType", 30),                 // 规则类型
            new ColConfig("description", 50)               // 描述
        );
    }
    
    /**
     * 表格最大高度（像素）
     */
    @Override
    public int maxHeight() {
        return 400;
    }
}
```

#### 2.2.1 ViewStyle 选择

- **Table**（推荐）：规则信息较多，表格视图更清晰
- **List**：如果规则信息简单，可以用列表视图

#### 2.2.2 ColConfig 配置说明

- `width`: 百分比整数（如 `20` 表示 `20%`），`null` 表示自适应
- `setClickable()`: 标记该列可点击编辑，**至少有一列必须设置**
- 列名必须对应 `ActionType` 的实际字段名

### 2.3 ActionRuleSetter 字段改造

**文件**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/impl/action/ActionRuleSetter.java`

#### 2.3.1 字段声明改造

```java
package com.qlangtech.tis.plugin.ontology.impl.action;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.SubForm;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.impl.action.type.ActionType;
import com.qlangtech.tis.plugin.ontology.impl.action.type.FunctionActionType;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.IPluginContext;

import java.util.List;
import java.util.Optional;

/**
 * Action Type Step 2: 规则设置
 * <p>
 * 配置 Action 的执行规则（支持多个规则）
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/6
 */
public class ActionRuleSetter extends OneStepOfMultiSteps {

    // ❌ 删除旧字段（保留用于 readResolve 迁移）
    @Deprecated
    private transient ActionType actionType;

    // ✅ 新字段：支持多个规则
    @FormField(ordinal = 0, type = FormFieldType.MULTI_DESCRIBLE_PLUGIN,
               desClazz = ActionType.class, validate = {Validator.require})
    public List<ActionType> actionTypes;
    

    @Override
    public void processPreSaved(IPluginContext pluginContext, Context currentCtx,
                                OneStepOfMultiSteps[] preSavedStepPlugins) {
        // 保存当前步骤数据到上下文
        currentCtx.put(ActionRuleSetter.class.getName(), this);
        super.processPreSaved(pluginContext, currentCtx, preSavedStepPlugins);
    }

    @TISExtension
    public static class Desc extends OneStepOfMultiSteps.BasicDesc {

        @Override
        public String getStepDescription() {
            return "Rules";
        }

        @Override
        public Step getStep() {
            return Step.Step2;
        }

        @Override
        public Optional<BasicDesc> nextPluginDesc(OneStepOfMultiSteps current) {
            // 第二步完成后不再有下一步，直接结束向导
            return Optional.empty();
        }

        @Override
        public boolean isFinalStep() {
            return true;
        }

        /**
         * 多规则组合校验
         */
        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, 
                                      PostFormVals postFormVals) {
            ActionRuleSetter ruleSetter = postFormVals.newInstance();
            
            if (ruleSetter.actionTypes == null || ruleSetter.actionTypes.isEmpty()) {
                msgHandler.addErrorMessage(context, "至少需要配置一个规则");
                return false;
            }
            
            // 调用规则组合校验
            if (!validateRuleCombination(msgHandler, context, ruleSetter.actionTypes)) {
                return false;
            }

            return super.validateAll(msgHandler, context, postFormVals);
        }
        
        /**
         * 规则组合合法性校验
         * 根据 Palantir 文档要求实现
         */
        private boolean validateRuleCombination(IControlMsgHandler msgHandler, 
                                               Context context, 
                                               List<ActionType> rules) {
            // 1. Function 规则不能与其他 Ontology 规则组合
            long functionRuleCount = rules.stream()
                .filter(r -> r instanceof FunctionActionType)
                .count();
            
            if (functionRuleCount > 0 && rules.size() > 1) {
                msgHandler.addErrorMessage(context, 
                    "Function 规则不能与其他 Ontology 规则组合，必须单独使用");
                return false;
            }
            
            // 2. 检查对象操作的顺序合法性
            // TODO: 实现以下检查
            // - 对象不能在添加或修改之前被删除
            // - 对象不能在添加之前被修改
            // - 对象不能在一次提交中创建两次
            
            return true;
        }
    }
}
```

#### 2.3.2 关键点说明

1. **`@Deprecated transient ActionType actionType`**
   - 保留旧字段用于反序列化迁移
   - `transient` 防止新数据再序列化它
   - `@Deprecated` 标记为废弃


3. **`validateRuleCombination()` 校验**
   - Function 规则互斥检查（已实现）
   - 对象操作顺序检查（待实现）
   - 返回 `false` 时必须调用 `addErrorMessage`

### 2.4 新增 ActionType.json 资源文件（可选）

**文件**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/ActionType.json`

```json
{
  "identity": {
    "help": "规则的唯一标识，用于区分不同规则",
    "placeholder": "例如：create_employee_rule"
  }
}
```

**说明**：
- 枚举选项走 `shortComment`，不需要在 json 中写 `enum` 数组
- 只写需要特殊说明的字段
- 如果所有字段都有清晰的 `shortComment`，可以不创建此文件

### 2.5 新增规则组合校验逻辑（详细实现）

**文件**: 新建 `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/impl/action/RuleCombinationValidator.java`

```java
package com.qlangtech.tis.plugin.ontology.impl.action;

import com.qlangtech.tis.plugin.ontology.impl.action.rule.*;
import com.qlangtech.tis.plugin.ontology.impl.action.type.*;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则组合合法性校验器
 * 
 * 根据 Palantir 文档要求实现以下约束：
 * 1. 对象不能在添加或修改之前被删除
 * 2. 对象不能在添加之前被修改
 * 3. 对象不能在一次提交中创建两次
 * 4. Function 规则不能与其他 Ontology 规则组合
 * 
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/17
 */
public class RuleCombinationValidator {
    
    /**
     * 校验规则组合的合法性
     * 
     * @param rules 规则列表（按用户配置的顺序）
     * @return 校验结果，失败时包含错误消息
     */
    public static ValidationResult validate(List<ActionType> rules) {
        if (rules == null || rules.isEmpty()) {
            return ValidationResult.error("规则列表不能为空");
        }
        
        // 1. Function 规则互斥检查
        ValidationResult functionResult = validateFunctionRuleExclusive(rules);
        if (!functionResult.isValid()) {
            return functionResult;
        }
        
        // 2. 对象操作顺序检查（按对象类型分组）
        ValidationResult orderResult = validateObjectOperationOrder(rules);
        if (!orderResult.isValid()) {
            return orderResult;
        }
        
        // 3. 对象重复创建检查
        ValidationResult duplicateResult = validateNoDuplicateCreation(rules);
        if (!duplicateResult.isValid()) {
            return duplicateResult;
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 检查 Function 规则是否与其他规则混用
     */
    private static ValidationResult validateFunctionRuleExclusive(List<ActionType> rules) {
        long functionCount = rules.stream()
            .filter(r -> r instanceof FunctionActionType)
            .count();
        
        if (functionCount > 0 && rules.size() > 1) {
            return ValidationResult.error(
                "Function 规则不能与其他 Ontology 规则组合。" +
                "Function 规则必须单独使用，因为它可以表达所有其他规则的能力。"
            );
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 检查对象操作的顺序合法性
     * 
     * 规则：
     * - DELETE 不能在 CREATE/MODIFY 之前
     * - MODIFY 不能在 CREATE 之前
     */
    private static ValidationResult validateObjectOperationOrder(List<ActionType> rules) {
        // 按对象类型分组检查
        Map<String, List<ObjectOperation>> operationsByObject = new HashMap<>();
        
        for (int i = 0; i < rules.size(); i++) {
            ActionType rule = rules.get(i);
            if (!(rule instanceof ObjectActionType objRule)) {
                continue;
            }
            
            String objectType = objRule.objectTypeReference;
            ObjectOperation op = new ObjectOperation(i, objRule);
            operationsByObject.computeIfAbsent(objectType, k -> new ArrayList<>())
                              .add(op);
        }
        
        // 对每个对象类型的操作序列进行检查
        for (Map.Entry<String, List<ObjectOperation>> entry : operationsByObject.entrySet()) {
            String objectType = entry.getKey();
            List<ObjectOperation> ops = entry.getValue();
            
            ValidationResult result = validateOperationSequence(objectType, ops);
            if (!result.isValid()) {
                return result;
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 检查单个对象类型的操作序列
     */
    private static ValidationResult validateOperationSequence(
            String objectType, List<ObjectOperation> ops) {
        
        Integer createIndex = null;
        Integer modifyIndex = null;
        Integer deleteIndex = null;
        
        for (ObjectOperation op : ops) {
            OntologyActionRule rule = op.actionType.getRule();
            
            if (rule instanceof CreateObjectRule) {
                if (createIndex != null) {
                    return ValidationResult.error(String.format(
                        "对象类型 '%s' 不能在一次提交中创建两次（规则 #%d 和 #%d）",
                        objectType, createIndex + 1, op.index + 1
                    ));
                }
                createIndex = op.index;
            } else if (rule instanceof ModifyObjectRule) {
                if (modifyIndex == null) {
                    modifyIndex = op.index;
                }
                // MODIFY 不能在 CREATE 之前
                if (createIndex != null && op.index < createIndex) {
                    return ValidationResult.error(String.format(
                        "对象类型 '%s' 不能在创建（规则 #%d）之前修改（规则 #%d）",
                        objectType, createIndex + 1, op.index + 1
                    ));
                }
            } else if (rule instanceof DeleteObjectRule) {
                if (deleteIndex == null) {
                    deleteIndex = op.index;
                }
                // DELETE 不能在 CREATE/MODIFY 之前
                if (createIndex != null && op.index < createIndex) {
                    return ValidationResult.error(String.format(
                        "对象类型 '%s' 不能在创建（规则 #%d）之前删除（规则 #%d）",
                        objectType, createIndex + 1, op.index + 1
                    ));
                }
                if (modifyIndex != null && op.index < modifyIndex) {
                    return ValidationResult.error(String.format(
                        "对象类型 '%s' 不能在修改（规则 #%d）之前删除（规则 #%d）",
                        objectType, modifyIndex + 1, op.index + 1
                    ));
                }
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 检查是否有对象被创建两次
     */
    private static ValidationResult validateNoDuplicateCreation(List<ActionType> rules) {
        Set<String> createdObjects = new HashSet<>();
        
        for (int i = 0; i < rules.size(); i++) {
            ActionType rule = rules.get(i);
            if (!(rule instanceof ObjectActionType objRule)) {
                continue;
            }
            
            if (objRule.getRule() instanceof CreateObjectRule) {
                String objectType = objRule.objectTypeReference;
                if (createdObjects.contains(objectType)) {
                    return ValidationResult.error(String.format(
                        "对象类型 '%s' 不能在一次提交中创建两次（规则 #%d）",
                        objectType, i + 1
                    ));
                }
                createdObjects.add(objectType);
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 对象操作记录
     */
    private static class ObjectOperation {
        final int index;  // 在规则列表中的位置
        final ObjectActionType actionType;
        
        ObjectOperation(int index, ObjectActionType actionType) {
            this.index = index;
            this.actionType = actionType;
        }
    }
    
    /**
     * 校验结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
```

#### 2.5.1 在 ActionRuleSetter.Desc 中使用校验器

```java
@Override
protected boolean validateAll(IControlMsgHandler msgHandler, Context context, 
                              PostFormVals postFormVals) {
    ActionRuleSetter ruleSetter = postFormVals.newInstance();
    
    if (ruleSetter.actionTypes == null || ruleSetter.actionTypes.isEmpty()) {
        msgHandler.addErrorMessage(context, "至少需要配置一个规则");
        return false;
    }
    
    // 使用校验器
    RuleCombinationValidator.ValidationResult result = 
        RuleCombinationValidator.validate(ruleSetter.actionTypes);
    
    if (!result.isValid()) {
        msgHandler.addErrorMessage(context, result.getErrorMessage());
        return false;
    }

    return super.validateAll(msgHandler, context, postFormVals);
}
```

---

## 三、前端改造（2-4 处）

### 3.1 确认是否需要自定义渲染

**检查点**：
1. 是否有自定义的 `actionType` 渲染组件？
2. 是否有特殊的表单处理逻辑？

**如果没有**：前端零改动，`MULTI_DESCRIBABLE_PLUGIN` 会自动渲染表格/列表视图。

**如果有**：参考 `FilterItemConfig` 的前端改造。

### 3.2 前端改造清单（如果需要）

#### 3.2.1 归一化器调整

**文件**: `/Users/mozhenghua/j2ee_solution/project/tis-console/src/base/common/action-parameters.model.ts` 或相关文件

```typescript
// 如果有特殊的 actionType 处理逻辑，需要调整为处理数组

// ❌ 旧逻辑
interface ActionRuleSetter {
  actionType: ActionType;
}

// ✅ 新逻辑
interface ActionRuleSetter {
  actionTypes: ActionType[];
}

// 归一化函数示例
function normalizeActionRuleSetter(data: any): ActionRuleSetter {
  return {
    actionTypes: Array.isArray(data.actionTypes) 
      ? data.actionTypes.map(normalizeActionType)
      : []
  };
}
```

#### 3.2.2 删除旧的处理逻辑

搜索并删除旧的 `actionType`（单数）处理代码：

```bash
# 在 tis-console 中搜索
cd /Users/mozhenghua/j2ee_solution/project/tis-console
grep -rn "\.actionType" src/ --include="*.ts" --include="*.html"
```

**注意**：只删除 `src/` 目录下的代码，不要删除 `.angular/cache/` 中的编译产物。

---

## 四、单元测试

### 4.1 后端测试

**文件**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/test/java/com/qlangtech/tis/plugin/ontology/impl/action/TestRuleCombinationValidator.java`

```java
package com.qlangtech.tis.plugin.ontology.impl.action;

import com.google.common.collect.Lists;
import com.qlangtech.tis.plugin.ontology.impl.action.rule.*;
import com.qlangtech.tis.plugin.ontology.impl.action.type.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 规则组合校验器单元测试
 */
public class TestRuleCombinationValidator {
    
    @Test
    public void testFunctionRuleExclusive() {
        // Function 规则单独使用 - 应该通过
        FunctionActionType functionRule = new FunctionActionType();
        functionRule.identity = "func1";
        
        List<ActionType> rules = Lists.newArrayList(functionRule);
        RuleCombinationValidator.ValidationResult result = 
            RuleCombinationValidator.validate(rules);
        
        Assert.assertTrue("Function 规则单独使用应该通过", result.isValid());
    }
    
    @Test
    public void testFunctionRuleWithOthers() {
        // Function 规则与其他规则混用 - 应该失败
        FunctionActionType functionRule = new FunctionActionType();
        functionRule.identity = "func1";
        
        ObjectActionType objectRule = new ObjectActionType();
        objectRule.identity = "obj1";
        objectRule.objectTypeReference = "Employee";
        objectRule.objectRule = new CreateObjectRule();
        
        List<ActionType> rules = Lists.newArrayList(functionRule, objectRule);
        RuleCombinationValidator.ValidationResult result = 
            RuleCombinationValidator.validate(rules);
        
        Assert.assertFalse("Function 规则不能与其他规则混用", result.isValid());
        Assert.assertTrue(result.getErrorMessage().contains("Function 规则不能与其他 Ontology 规则组合"));
    }
    
    @Test
    public void testValidObjectOperationOrder() {
        // 正确的顺序：CREATE -> MODIFY -> DELETE
        ObjectActionType createRule = createObjectRule("Employee", "create1");
        ObjectActionType modifyRule = modifyObjectRule("Employee", "modify1");
        ObjectActionType deleteRule = deleteObjectRule("Employee", "delete1");
        
        List<ActionType> rules = Lists.newArrayList(createRule, modifyRule, deleteRule);
        RuleCombinationValidator.ValidationResult result = 
            RuleCombinationValidator.validate(rules);
        
        Assert.assertTrue("正确的操作顺序应该通过", result.isValid());
    }
    
    @Test
    public void testInvalidOrderModifyBeforeCreate() {
        // 错误的顺序：MODIFY -> CREATE
        ObjectActionType createRule = createObjectRule("Employee", "create1");
        ObjectActionType modifyRule = modifyObjectRule("Employee", "modify1");
        
        List<ActionType> rules = Lists.newArrayList(modifyRule, createRule);
        RuleCombinationValidator.ValidationResult result = 
            RuleCombinationValidator.validate(rules);
        
        Assert.assertFalse("MODIFY 不能在 CREATE 之前", result.isValid());
        Assert.assertTrue(result.getErrorMessage().contains("不能在创建"));
    }
    
    @Test
    public void testInvalidOrderDeleteBeforeCreate() {
        // 错误的顺序：DELETE -> CREATE
        ObjectActionType createRule = createObjectRule("Employee", "create1");
        ObjectActionType deleteRule = deleteObjectRule("Employee", "delete1");
        
        List<ActionType> rules = Lists.newArrayList(deleteRule, createRule);
        RuleCombinationValidator.ValidationResult result = 
            RuleCombinationValidator.validate(rules);
        
        Assert.assertFalse("DELETE 不能在 CREATE 之前", result.isValid());
        Assert.assertTrue(result.getErrorMessage().contains("不能在创建"));
    }
    
    @Test
    public void testDuplicateCreation() {
        // 同一个对象创建两次
        ObjectActionType createRule1 = createObjectRule("Employee", "create1");
        ObjectActionType createRule2 = createObjectRule("Employee", "create2");
        
        List<ActionType> rules = Lists.newArrayList(createRule1, createRule2);
        RuleCombinationValidator.ValidationResult result = 
            RuleCombinationValidator.validate(rules);
        
        Assert.assertFalse("同一对象不能创建两次", result.isValid());
        Assert.assertTrue(result.getErrorMessage().contains("不能在一次提交中创建两次"));
    }
    
    @Test
    public void testMultipleObjectTypes() {
        // 多个对象类型，各自操作合法
        ObjectActionType createEmployee = createObjectRule("Employee", "create_emp");
        ObjectActionType createDepartment = createObjectRule("Department", "create_dept");
        ObjectActionType modifyEmployee = modifyObjectRule("Employee", "modify_emp");
        
        List<ActionType> rules = Lists.newArrayList(
            createEmployee, createDepartment, modifyEmployee
        );
        RuleCombinationValidator.ValidationResult result = 
            RuleCombinationValidator.validate(rules);
        
        Assert.assertTrue("多个对象类型各自操作合法应该通过", result.isValid());
    }
    
    // 辅助方法
    private ObjectActionType createObjectRule(String objectType, String identity) {
        ObjectActionType rule = new ObjectActionType();
        rule.identity = identity;
        rule.objectTypeReference = objectType;
        rule.objectRule = new CreateObjectRule();
        return rule;
    }
    
    private ObjectActionType modifyObjectRule(String objectType, String identity) {
        ObjectActionType rule = new ObjectActionType();
        rule.identity = identity;
        rule.objectTypeReference = objectType;
        rule.objectRule = new ModifyObjectRule();
        return rule;
    }
    
    private ObjectActionType deleteObjectRule(String objectType, String identity) {
        ObjectActionType rule = new ObjectActionType();
        rule.identity = identity;
        rule.objectTypeReference = objectType;
        rule.objectRule = new DeleteObjectRule();
        return rule;
    }
}
```

### 4.2 XStream 反序列化测试

**文件**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/test/java/com/qlangtech/tis/plugin/ontology/impl/action/TestActionRuleSetterMigration.java`

```java
package com.qlangtech.tis.plugin.ontology.impl.action;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * ActionRuleSetter 存量数据迁移测试
 */
public class TestActionRuleSetterMigration {
    
    @Test
    public void testMigrateSingleRuleToList() {
        // 模拟存量 XML（只有单个 actionType）
        String oldXml = 
            "<com.qlangtech.tis.plugin.ontology.impl.action.ActionRuleSetter>\n" +
            "  <actionType class=\"com.qlangtech.tis.plugin.ontology.impl.action.type.ObjectActionType\">\n" +
            "    <identity>rule1</identity>\n" +
            "    <objectTypeReference>Employee</objectTypeReference>\n" +
            "  </actionType>\n" +
            "</com.qlangtech.tis.plugin.ontology.impl.action.ActionRuleSetter>";
        
        XStream xstream = new XStream();
        xstream.processAnnotations(ActionRuleSetter.class);
        
        // 反序列化
        ActionRuleSetter ruleSetter = (ActionRuleSetter) xstream.fromXML(oldXml);
        
        // 验证 readResolve 已执行迁移
        Assert.assertNotNull("actionTypes 不应为 null", ruleSetter.actionTypes);
        Assert.assertEquals("应该有 1 个规则", 1, ruleSetter.actionTypes.size());
        Assert.assertEquals("规则 identity 应该匹配", 
                          "rule1", ruleSetter.actionTypes.get(0).identity);
    }
    
    @Test
    public void testNewDataStructure() {
        // 新数据结构（actionTypes 列表）
        ActionRuleSetter ruleSetter = new ActionRuleSetter();
        
        ObjectActionType rule1 = new ObjectActionType();
        rule1.identity = "rule1";
        rule1.objectTypeReference = "Employee";
        
        ObjectActionType rule2 = new ObjectActionType();
        rule2.identity = "rule2";
        rule2.objectTypeReference = "Department";
        
        ruleSetter.actionTypes = Lists.newArrayList(rule1, rule2);
        
        // 序列化
        XStream xstream = new XStream();
        xstream.processAnnotations(ActionRuleSetter.class);
        String xml = xstream.toXML(ruleSetter);
        
        // 反序列化
        ActionRuleSetter deserialized = (ActionRuleSetter) xstream.fromXML(xml);
        
        // 验证
        Assert.assertNotNull("actionTypes 不应为 null", deserialized.actionTypes);
        Assert.assertEquals("应该有 2 个规则", 2, deserialized.actionTypes.size());
        Assert.assertEquals("rule1", deserialized.actionTypes.get(0).identity);
        Assert.assertEquals("rule2", deserialized.actionTypes.get(1).identity);
    }
}
```

---

## 五、前端验证（如果有自定义逻辑）

### 5.1 表单回填测试

**文件**: `/Users/mozhenghua/j2ee_solution/project/tis-console/src/base/ontology.action-type.detail.component.spec.ts`

```typescript
import { TestBed } from '@angular/core/testing';
// ... 其他导入

describe('ActionRuleSetter 多规则支持', () => {
  it('应该正确回填多个规则', () => {
    const data = {
      actionTypes: [
        {
          impl: 'ObjectActionType',
          displayName: 'Object',
          vals: {
            identity: 'rule1',
            objectTypeReference: 'Employee'
          }
        },
        {
          impl: 'LinkActionType',
          displayName: 'Link',
          vals: {
            identity: 'rule2',
            linkTypeReference: 'WorksFor'
          }
        }
      ]
    };
    
    // 触发表单回填
    // ... 测试代码
    
    // 验证
    expect(component.actionTypes.length).toBe(2);
    expect(component.actionTypes[0].identity).toBe('rule1');
  });
});
```

---

## 六、部署与回滚方案

### 6.1 部署步骤

1. **构建插件**
   ```bash
   cd /Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin
   mvn clean package -Dmaven.test.skip=true
   ```

2. **构建前端**（如果有改动）
   ```bash
   cd /Users/mozhenghua/j2ee_solution/project/tis-console
   npm run build
   ```

3. **部署验证**
   - 先在测试环境验证
   - 验证存量单规则数据能正常加载
   - 验证新建多规则功能正常
   - 验证规则组合校验生效

### 6.2 回滚方案

如果出现问题，可以回滚：

1. **保留旧字段兼容**
   ```java
   // 保留这行代码不删除
   @Deprecated
   private transient ActionType actionType;
   ```

2. **XStream 别名兼容**
   ```java
   // 如果需要，添加 XStream 别名
   xstream.aliasField("actionType", ActionRuleSetter.class, "actionTypes");
   ```

---

## 七、完整改动检查清单

### 7.1 后端检查清单

- [ ] `ActionType` 实现 `IPluginStore.MultiDescribleElement`
- [ ] `ActionType` 添加 `identity` 字段
- [ ] `ActionType` 实现 `identityValue()` 方法
- [ ] `ActionType.BaseActionTypeDescriptor` 扩展 `MultiDescribleElementDescriptor`
- [ ] `BaseActionTypeDescriptor` 实现 `viewStyle()` 方法
- [ ] `BaseActionTypeDescriptor` 实现 `colsConfig()` 方法
- [ ] `BaseActionTypeDescriptor` 实现 `maxHeight()` 方法
- [ ] `ActionRuleSetter.actionType` 改为 `actionTypes`（List）
- [ ] `ActionRuleSetter` 添加 `readResolve()` 迁移逻辑
- [ ] `ActionRuleSetter.Desc` 添加规则组合校验
- [ ] 新增 `RuleCombinationValidator` 类
- [ ] 实现 Function 规则互斥检查
- [ ] 实现对象操作顺序检查
- [ ] 实现重复创建检查
- [ ] 创建 `ActionType.json` 资源文件（可选）
- [ ] 编写单元测试（至少 8 个测试用例）
- [ ] 所有测试通过

### 7.2 前端检查清单

- [ ] 搜索并删除旧的 `actionType`（单数）处理代码
- [ ] 更新类型定义为 `actionTypes: ActionType[]`
- [ ] 更新归一化器（如果有）
- [ ] 验证表格视图正常渲染
- [ ] 验证添加/删除/编辑功能正常
- [ ] 验证拖拽排序功能正常
- [ ] 编写前端测试（如果有自定义逻辑）

### 7.3 测试检查清单

- [ ] 单规则功能正常
- [ ] 多规则功能正常
- [ ] 存量数据迁移正常
- [ ] Function 规则互斥校验生效
- [ ] 对象操作顺序校验生效
- [ ] 重复创建校验生效
- [ ] 表格视图渲染正常
- [ ] 添加规则功能正常
- [ ] 删除规则功能正常
- [ ] 编辑规则功能正常
- [ ] 拖拽排序功能正常
- [ ] 保存和加载功能正常

---

## 八、常见问题与解决方案

### 8.1 identity 字段重复问题

**问题**：用户可能输入重复的 identity。

**解决方案**：
```java
@FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT, 
           validate = {Validator.require, Validator.identity})
public String identity;
```

`Validator.identity` 会自动拒绝重复值。

### 8.2 表格不显示问题

**问题**：配置完后表格不显示或显示空白。

**排查步骤**：
1. 检查 `viewStyle()` 是否返回 `ViewStyle.Table`
2. 检查 `colsConfig()` 是否至少有一列 `setClickable()`
3. 检查列名是否对应实际字段名
4. 检查 `identity` 字段是否有值

### 8.3 规则校验不生效

**问题**：配置了非法组合但没有报错。

**排查步骤**：
1. 检查 `validateAll()` 是否正确调用了 `RuleCombinationValidator.validate()`
2. 检查是否在失败时调用了 `msgHandler.addErrorMessage()`
3. 检查校验逻辑是否正确处理了所有规则类型

### 8.4 存量数据迁移失败

**问题**：旧数据加载后 `actionTypes` 仍为空。

**排查步骤**：
1. 检查 `readResolve()` 方法是否在 `ActionRuleSetter` 类中声明
2. 检查 `readResolve()` 的逻辑是否正确
3. 在 `readResolve()` 中添加日志输出，确认是否被调用

### 8.5 前端表单不显示

**问题**：配置完后前端表单不显示 actionTypes 字段。

**排查步骤**：
1. 检查 `@FormField` 注解的 `type` 是否为 `MULTI_DESCRIBLE_PLUGIN`
2. 检查 `desClazz` 是否正确指定
3. 检查 `@SubForm` 注解是否添加
4. 清理前端缓存后重试

---

## 九、参考资料

### 9.1 代码参考

- **实现案例**: `FilterItemConfig.java`
- **descriptor 基类**: `IPluginStore.MultiDescribleElementDescriptor`
- **前端组件**: `multi-describle-plugin-property.component.ts`

### 9.2 文档参考

- **Palantir 文档**: `/opt/misc/palantir-study/foundry/ontology/action-types/detail/00-overview/`
- **Memory 记录**:
  - `project_tuplelist_to_multi_describle_migration.md`
  - `project_multi_describle_plugin_viewstyle_contract.md`
  - `project_multi_describle_plugin_blog_post.md`
- **TIS 文档**: `/opt/misc/tis-docs2/blog/2026-10-28-multi-describle-plugin-guide/index.md`

### 9.3 工具命令

```bash
# 搜索 MULTI_DESCRIBABLE_PLUGIN 使用示例
grep -rn "MULTI_DESCRIBLE_PLUGIN" \
  /Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src \
  --include="*.java"

# 搜索前端相关代码
grep -rn "actionType" \
  /Users/mozhenghua/j2ee_solution/project/tis-console/src \
  --include="*.ts" --include="*.html"

# 运行后端测试
cd /Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin
mvn test -Dtest=TestRuleCombinationValidator

# 运行前端构建验证
cd /Users/mozhenghua/j2ee_solution/project/tis-console
npm run build
```

---

## 十、总结

本文档详细描述了将 `ActionRuleSetter.actionType` 从单个规则改造为多规则列表的完整实现方案。

**核心改动**：
1. `ActionType` 实现 `MultiDescribleElement` 接口
2. `BaseActionTypeDescriptor` 扩展 `MultiDescribleElementDescriptor`
3. `ActionRuleSetter.actionType` → `actionTypes: List<ActionType>`
4. 新增规则组合校验逻辑
5. 前端零改动或最小改动

**关键约束**：
- Function 规则必须单独使用
- 对象操作顺序：CREATE → MODIFY → DELETE
- 同一对象不能创建两次
- 存量数据通过 `readResolve()` 迁移

**验证重点**：
- 单元测试覆盖所有校验场景
- 存量数据迁移测试
- UI 交互测试（表格视图、拖拽排序）
- 端到端功能测试

按照本文档实施后，TIS 的 Action Types 将完全符合 Palantir 的多规则组合设计。