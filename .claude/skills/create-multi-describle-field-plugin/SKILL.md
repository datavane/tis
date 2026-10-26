---
name: create-multi-describle-field-plugin
description: Create a MULTI_DESCRIBLE_PLUGIN field element plugin (multi-describable list element with table/list view)
---

# Multi-Describle Field Plugin Creator

This skill helps you create a well-structured `MULTI_DESCRIBLE_PLUGIN` field element plugin that conforms to TIS plugin specifications. This type of plugin is used for list-based sub-forms where each row is a complete `Describable` instance.

## What is MULTI_DESCRIBLE_PLUGIN?

`MULTI_DESCRIBLE_PLUGIN` is a special field type in TIS that renders structured sub-form lists. Each element in the list is a full plugin instance with its own properties, validation, and lifecycle.

### Relationship Pattern

- **Host Plugin (1) ↔ Element Plugin (N)**: Many-to-one relationship
- **Example**: `UserProfile` (host) has `List<TestChild>` (elements)
  - One `UserProfile` instance can have multiple `TestChild` instances
  - Each `TestChild` is a complete plugin with its own properties and validation

### Key Characteristics

- **List View or Table View**: Frontend renders as `<nz-list>` or `<nz-table>`
- **Full Plugin Instances**: Each list item is a complete `Describable` object
- **Identity Requirement**: Each element must have a unique identity field
- **Enumerable or Open**: Can be a fixed candidate set (enumerable) or open for user creation

## Core Requirements

### 1. Host Plugin Field Configuration

The host plugin (like `UserProfile`) must declare the field with specific attributes:

```java
// In UserProfile.java (Host Plugin)
@FormField(ordinal = 6, 
           desClazz = TestChild.class,                          // REQUIRED: Element class
           type = FormFieldType.MULTI_DESCRIBLE_PLUGIN,         // REQUIRED: Field type
           validate = {Validator.require})                      // OPTIONAL: Use if at least one item required
public List<TestChild> children;
```

**Key Points**:
- `desClazz`: Must specify the element class type (matches the List generic type)
- `type`: Must be `FormFieldType.MULTI_DESCRIBLE_PLUGIN`
- `validate = {Validator.require}`: Include if the list must have at least one element; omit if empty list is allowed
- Property type: Must be `List<ElementClass>` (generic List)

### 2. Element Plugin Class

The element class (like `TestChild`) must implement `IPluginStore.MultiDescribleElement`:

```java
// In TestChild.java (Element Plugin)
public class TestChild implements Describable<TestChild>, 
                                 IPluginStore.MultiDescribleElement {
    
    // REQUIRED: Identity field
    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT, 
               validate = {Validator.require})
    public String name;
    
    // Other properties...
    @FormField(ordinal = 1, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer age;
    
    // REQUIRED: Implement identity method
    @Override
    public String identityValue() {
        return this.name;
    }
}
```

**Critical Requirements**:
- **Must implement `IPluginStore.MultiDescribleElement` interface**
  - Location: `tis-plugin/src/main/java/com/qlangtech/tis/plugin/IPluginStore.java:199`
- **Must have an identity field**:
  - One `@FormField` property marked with `identity = true`
  - Typically `String` type, often named `name` or `id`
  - Must have `validate = {Validator.require}` (identity cannot be empty)
- **Must implement `identityValue()` method**:
  - Returns the unique identifier for this element
  - Used by TIS framework for element tracking and updates

### 3. Element Descriptor Class

The element's Descriptor must extend `IPluginStore.MultiDescribleElementDescriptor`:

```java
@TISExtension
public static class DftDescriptor extends IPluginStore.MultiDescribleElementDescriptor<TestChild> {
    
    // REQUIRED: Display name (pure English, no Chinese/spaces)
    @Override
    public String getDisplayName() {
        return "Test_Child";  // Underscore-separated PascalCase
    }
    
    // REQUIRED: Chinese description (implements DescriptorUseableShortComment)
    @Override
    public String shortComment() {
        return "测试子表单属性";  // <=10 chars, no punctuation
    }
    
    // REQUIRED: End type (for UI rendering hints)
    @Override
    public EndType getEndType() {
        return EndType.Copy;
    }
    
    // IMPORTANT: Override these methods based on your needs
    
    @Override
    public ViewStyle viewStyle() {
        return ViewStyle.Table;  // or ViewStyle.List
    }
    
    @Override
    public List<ColConfig> colsConfig() {
        // REQUIRED for Table view, at least one column must be clickable
        return Lists.newArrayList(
            new ColConfig("name", 12).setClickable(),  // clickable column for editing
            new ColConfig("age", 12),
            new ColConfig("sex")
        );
    }
    
    @Override
    public EnumableSetCfg getEnumableSetCfg() {
        // Return EnumableSetCfg(true) for fixed candidate set
        // Return EnumableSetCfg(false) for open user creation (default)
        return new EnumableSetCfg(false);
    }
    
    @Override
    public int maxHeight() {
        return 300;  // px, default is 100
    }
    
    @Override
    protected RowDragDropCfg isEnableRowDragDrop() {
        // Enable row drag-drop for manual sorting
        return new RowDragDropCfg(false, null);  // default: disabled
    }
    
    @Override
    protected List<Pair<Option, MultiDescribleElement>> getEnumableCandidateSet(
            ParseDescribable<?> describable) {
        // Override if getEnumableSetCfg() returns true
        // Return the fixed candidate list
        return Lists.newArrayList();
    }
}
```

**Descriptor Location**: `tis-plugin/src/main/java/com/qlangtech/tis/plugin/IPluginStore.java:211`

### 4. Element Plugin Specifications

The element plugin MUST also satisfy standard TIS plugin requirements from `/create-plugin`:

- Proper `@FormField` annotations on all properties
- Corresponding `.json` descriptor file in resources
- Optional `.md` help file for complex properties
- Follow all validation rules, FormFieldType principles, and naming conventions

**Reference**: See `.claude/skills/create-plugin/SKILL.md` for complete plugin specifications

## Descriptor Methods Reference

### Required Methods

| Method | Purpose | Default | When to Override |
|--------|---------|---------|------------------|
| `getDisplayName()` | Plugin name (pure English) | Required | Always (English name) |
| `shortComment()` | Chinese description | Required | Always (<=10 chars) |
| `getEndType()` | UI rendering hint | Required | Always |

### View Configuration Methods

| Method | Purpose | Default | When to Override |
|--------|---------|---------|------------------|
| `viewStyle()` | List or Table view | `ViewStyle.List` | Override for Table view |
| `colsConfig()` | Table columns config | Empty list | Required for Table view (>=1 clickable) |
| `maxHeight()` | List/table max height (px) | 100 | Override for taller lists |

### Advanced Configuration Methods

| Method | Purpose | Default | When to Override |
|--------|---------|---------|------------------|
| `getEnumableSetCfg()` | Enumerable candidate set | `false` (open) | Override for fixed candidate pool |
| `getEnumableCandidateSet()` | Provide candidates | Empty list | Override when enumerable=true |
| `isEnableRowDragDrop()` | Manual row sorting | Disabled | Override to enable drag-drop |
| `createRelevantFieldKeysCreator()` | Field whitelist factory | Standard | Override for custom dependencies |

**Detailed Documentation**: All methods have extensive Javadoc in `IPluginStore.java:211` (recently added by this conversation).

## View Styles

### List View vs Table View

| Feature | List View | Table View |
|---------|-----------|------------|
| Component | `<nz-list>` | `<nz-table>` |
| Density | Loose (more vertical space) | Compact (tight rows) |
| Column Config | Optional | Required (>=1 clickable) |
| Drag-drop | Not supported | Supported |
| Best for | <=3 fields, long text | >=4 fields, short values |

### Table View Column Configuration

```java
@Override
public List<ColConfig> colsConfig() {
    return Lists.newArrayList(
        new ColConfig("parameterId", 15).setClickable(),  // 15% width, clickable
        new ColConfig("displayName", 15),                 // 15% width
        new ColConfig("dataType")                         // auto width
    );
}
```

**Rules**:
- **At least one column must be clickable** - this opens the edit panel
- Column width is percentage (15 = 15% of table width)
- `null` width = auto-allocate remaining space
- Column order is independent of `@FormField(ordinal=...)`

## Enumerable Set Mode

### Open Set (Default) vs Enumerable Set

| Mode | User Behavior | Use Case |
|------|---------------|----------|
| **Open Set** (`false`) | Click "Add" -> Fill blank form -> Create freely | User defines arbitrary items |
| **Enumerable Set** (`true`) | Click "Add" -> Select from checkboxes -> Add selected | Choose from predefined templates |

### Implementing Enumerable Set

```java
@Override
public EnumableSetCfg getEnumableSetCfg() {
    // itemDependencyDepth:
    //   0 = depends on direct host instance (default)
    //   1 = depends on host's parent instance
    //   2+ = depends on ancestor at that depth
    return new EnumableSetCfg(true, 0);
}

@Override
protected List<Pair<Option, MultiDescribleElement>> getEnumableCandidateSet(
        ParseDescribable<?> describable) {
    List<Pair<Option, MultiDescribleElement>> candidates = Lists.newArrayList();
    
    // 1. Extract host instance from describable
    UserProfile userProfile = describable.getInstance();
    
    // 2. Get currently selected items (to mark them as checked)
    Set<String> selected = userProfile.children.stream()
        .map(TestChild::identityValue)
        .collect(Collectors.toSet());
    
    // 3. Generate candidate list from business logic
    List<TestChild> templates = UserProfile.getChildren(userProfile.control);
    
    // 4. Convert to Option/Element pairs
    for (TestChild template : templates) {
        Option opt = new Option(template.identityValue())
            .setChecked(selected.contains(template.identityValue()));
        candidates.add(Pair.of(opt, template));
    }
    
    return candidates;
}
```

**Key Points**:
- `describable.getInstance()` gives you a **partially-instantiated** host object
  - Only fields in the relevant fields whitelist have values
  - Other fields are `null`
- `Option.value` must match `MultiDescribleElement.identityValue()`
- Use `.setChecked(true)` for already-selected items

## Row Drag-Drop Sorting

### When to Enable

Enable when the element class has an `order` or `priority` field for explicit ordering:

```java
// In element class
@FormField(ordinal = 2, type = FormFieldType.INT_NUMBER)
public Integer order;  // Stores display position

// In descriptor
@Override
protected RowDragDropCfg isEnableRowDragDrop() {
    return new RowDragDropCfg(true, "order");  // Write to "order" field after drag
}
```

**Frontend Behavior**:
- Renders drag handles in Table view
- After drag, frontend recalculates all `order` values (1, 2, 3, ...)
- Values are submitted when user saves the host form
- Backend should return list sorted by `order` field

## Implementation Workflow

When the user invokes `/create-multi-describle-field-plugin`, follow these steps:

### Step 1: Gather Requirements

Ask the user (or extract from context):

1. **Host Plugin Information**:
   - What is the host plugin class name?
   - What will the field name be? (e.g., `children`, `parameters`, `rules`)
   - Should the list be required (at least one item) or can it be empty?

2. **Element Plugin Information**:
   - What is the element class name?
   - What properties does each element have?
   - What will be the identity field?

3. **View Configuration**:
   - List view or Table view?
   - If Table view: which columns to show? Which column(s) clickable?
   - What maximum height? (default 100px)

4. **Advanced Features**:
   - Enumerable set (fixed candidates) or open set (user creates)?
   - If enumerable: what determines the candidate list?
   - Enable row drag-drop sorting?
   - If drag-drop: which field stores the order?

### Step 2: Design Structure

Present the design to the user for approval before implementation.

### Step 3: Generate Host Plugin Field

If the host plugin already exists, show the code to add to it.
If the host plugin doesn't exist, delegate to `/create-plugin` first.

### Step 4: Generate Element Plugin

Generate the complete element plugin with all required files.

### Step 5: Verify Implementation

Check against comprehensive checklist covering host field, element class, descriptor, and standard plugin requirements.

### Step 6: Provide Usage Instructions

Explain where files were created, how to test, and frontend integration details.

## Common Patterns

### Pattern 1: Simple Table (Open Set)

User manually creates configuration rows (property mappings, column definitions, filter rules).

### Pattern 2: Enumerable Set with Dependencies

Choose from templates/candidates that depend on another field (Action parameters depend on Action type).

### Pattern 3: List View with Drag-Drop

Simple list with explicit ordering (priority queue, execution sequence).

## Reference Implementations

1. **TestChild** (tis-plugin/.../manage/common/TestChild.java) - Complete reference
2. **ActionParameterConfig** (plugins/tis-ontology-plugin/.../action/ActionParameterConfig.java) - Advanced features
3. **UserProfile.children** (tis-plugin/.../manage/common/UserProfile.java:88) - Host field example
4. **Frontend Handler** (tis-console/.../plugin/item-prop-val.component.ts:1305) - Frontend integration

## Important Notes

1. **Do NOT confuse with `@SubForm`**: Use `MULTI_DESCRIBLE_PLUGIN` for complex elements with full plugin lifecycle
2. **Identity field is mandatory**: Required for element tracking, updates, and duplicate detection
3. **Descriptor methods extensively documented**: Read Javadoc in `IPluginStore.java:211`
4. **Test thoroughly**: Test add/edit/delete, enumerable selection, drag-drop, validation
5. **Follow standard plugin conventions**: All `/create-plugin` rules apply