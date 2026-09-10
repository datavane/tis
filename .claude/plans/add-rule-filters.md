# Plan: Add subDescEnumFilter for OntologyActionRule Properties in ActionType Subclasses

## Problem Analysis

Currently, `ObjectActionType` has a JSON descriptor file that filters its `objectRule` property to only show `OntologyActionRule` implementations whose `getRuleType().getGroup()` returns `RuleGroup.ONTOLOGY_OBJECT`. This prevents users from selecting incompatible rule types.

The same pattern needs to be applied to other ActionType subclasses that have rule properties:
- `LinkActionType.linkRule` → filter by `ONTOLOGY_LINK`
- `InterfaceActionType.interfaceRule` → filter by `ONTOLOGY_INTERFACE`
- `FunctionActionType.functionRule` → filter by `ONTOLOGY_FUNCTION`
- `SideEffectActionType.sideEffectRule` → filter by `SIDE_EFFECT`

**Note**: `ScheduleActionType` and `ApplyScenarioActionType` have TODO comments indicating their rule properties are not yet implemented, so they don't need JSON descriptors yet.

## Current State

### ObjectActionType (Reference Implementation)
- **Java**: Has `objectRule` property at line 49
- **JSON**: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/ObjectActionType.json`
  ```json
  {
    "objectRule": {
      "subDescEnumFilter": "return com.qlangtech.tis.plugin.ontology.impl.action.rule.OntologyActionRule.descFilter(desc,\"ONTOLOGY_OBJECT\");"
    }
  }
  ```

### ActionType Subclasses Needing JSON Descriptors

1. **LinkActionType**
   - Property: `linkRule` (line 47)
   - RuleGroup: `ONTOLOGY_LINK`
   - No JSON file exists yet

2. **InterfaceActionType**
   - Property: `interfaceRule` (line 50)
   - RuleGroup: `ONTOLOGY_INTERFACE`
   - No JSON file exists yet

3. **FunctionActionType**
   - Property: `functionRule` (line 45)
   - Type: `FunctionBackedRule` (subclass of `OntologyActionRule`)
   - RuleGroup: `ONTOLOGY_FUNCTION`
   - No JSON file exists yet

4. **SideEffectActionType**
   - Property: `sideEffectRule` (line 44)
   - RuleGroup: `SIDE_EFFECT`
   - No JSON file exists yet

## Implementation Plan

### Step 1: Create LinkActionType.json
- Path: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/LinkActionType.json`
- Content:
  ```json
  {
    "linkRule": {
      "subDescEnumFilter": "return com.qlangtech.tis.plugin.ontology.impl.action.rule.OntologyActionRule.descFilter(desc,\"ONTOLOGY_LINK\");"
    }
  }
  ```

### Step 2: Create InterfaceActionType.json
- Path: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/InterfaceActionType.json`
- Content:
  ```json
  {
    "interfaceRule": {
      "subDescEnumFilter": "return com.qlangtech.tis.plugin.ontology.impl.action.rule.OntologyActionRule.descFilter(desc,\"ONTOLOGY_INTERFACE\");"
    }
  }
  ```

### Step 3: Create FunctionActionType.json
- Path: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/FunctionActionType.json`
- Content:
  ```json
  {
    "functionRule": {
      "subDescEnumFilter": "return com.qlangtech.tis.plugin.ontology.impl.action.rule.OntologyActionRule.descFilter(desc,\"ONTOLOGY_FUNCTION\");"
    }
  }
  ```

### Step 4: Create SideEffectActionType.json
- Path: `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/SideEffectActionType.json`
- Content:
  ```json
  {
    "sideEffectRule": {
      "subDescEnumFilter": "return com.qlangtech.tis.plugin.ontology.impl.action.rule.OntologyActionRule.descFilter(desc,\"SIDE_EFFECT\");"
    }
  }
  ```

## Implementation Details

### Filter Mechanism
- All filters call `OntologyActionRule.descFilter(desc, "<RULE_GROUP>")`
- This method filters descriptors by matching `getRuleType().getGroup()` against the specified `RuleGroup` enum value
- The filter is defined in `OntologyActionRule.java:41-46`:
  ```java
  public static List<BaseRuleDescriptor> descFilter(List<BaseRuleDescriptor> descs, String ruleType) {
      final RuleGroup ruleGroup = RuleGroup.valueOf(ruleType);
      return descs.stream().filter((desc) -> {
          return ruleGroup == desc.getRuleGroup();
      }).toList();
  }
  ```

### RuleGroup Enum Values (from RuleGroup.java)
- `ONTOLOGY_OBJECT` - "对象操作"
- `ONTOLOGY_LINK` - "链接操作"
- `ONTOLOGY_FUNCTION` - "函数操作"
- `ONTOLOGY_INTERFACE` - "接口操作"
- `SIDE_EFFECT` - "副作用操作"
- `ADVANCED` - "高级操作" (used by ScheduleActionType and ApplyScenarioActionType)

## Verification

After implementation:
1. Compile the plugin module: `cd /Users/mozhenghua/j2ee_solution/project/plugins && mvn compile -pl tis-ontology-plugin -am -o`
2. Verify JSON files exist in the resources directory
3. No code changes needed - only JSON descriptor files

## Files to Create

1. `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/LinkActionType.json`
2. `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/InterfaceActionType.json`
3. `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/FunctionActionType.json`
4. `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/action/type/SideEffectActionType.json`

## Summary

This is a straightforward task that follows the established pattern from `ObjectActionType.json`. Each ActionType subclass needs its own JSON descriptor file that filters the available `OntologyActionRule` implementations to only those matching the correct `RuleGroup`.
