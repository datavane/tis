---
name: create-multi-describle-field-plugin
description: Create a MULTI_DESCRIBLE_PLUGIN field element plugin (multi-describable list element with table/list view, enumerable candidate set via MultiDescribleElementSetSelector, and valueChangePipe property linkage)
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
- **Linkable**: A host field change can replace the whole list, or rewrite the options of a field
  inside every row (`valueChangePipe`, see [Property Linkage](#property-linkage-valuechangepipe))

## Core Requirements

### 1. Host Plugin Field Configuration

The host plugin (like `UserProfile`) must declare the field with specific attributes:

```java
// In UserProfile.java (Host Plugin)
@FormField(ordinal = 7,
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
  - Location: `tis-plugin/src/main/java/com/qlangtech/tis/plugin/IPluginStore.java:184`
    (`interface MultiDescribleElement extends ManipuldateProcessor, IdentityName`)
- **Must have an identity field**:
  - One `@FormField` property marked with `identity = true`
  - Typically `String` type, often named `name` or `id`
  - Must have `validate = {Validator.require}` (identity cannot be empty)
- **Must implement `identityValue()` method**:
  - Returns the unique identifier for this element
  - Used by TIS framework for element tracking and updates

### 3. Element Descriptor Class

The element's Descriptor must extend `MultiDescribleElementSetSelector.MultiDescribleElementDescriptor`:

```java
@TISExtension
public static class DftDescriptor extends MultiDescribleElementSetSelector.MultiDescribleElementDescriptor<TestChild> {

    // REQUIRED (abstract on DescriptorUseableShortComment): Chinese description
    @Override
    public String shortComment() {
        return "测试子表单属性";  // <=10 chars, no punctuation
    }

    // RECOMMENDED: English display name. Default is clazz.getSimpleName(),
    // which resolves to the base class name and is usually not what you want.
    @Override
    public String getDisplayName() {
        return "TestChild";
    }

    // OPTIONAL: UI rendering hint. Default is EndType.Blank.
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
    protected List<Pair<Option, IPluginStore.MultiDescribleElement>> getEnumableCandidateSet(
            ParseDescribable<?> describable) {
        // Override if getEnumableSetCfg() returns true
        // Return the fixed candidate list
        return Lists.newArrayList();
    }
}
```

**Descriptor Location**: `tis-plugin/src/main/java/com/qlangtech/tis/plugin/MultiDescribleElementSetSelector.java:147`
(the element-set selector itself is the outer class at `:55`; the nested config value
classes `RowDragDropCfg` / `EnumableSetCfg` / `ColConfig` / `ViewStyle` are also members of it).

⚠️ **This class used to live at `IPluginStore.MultiDescribleElementDescriptor` and was migrated
out of `IPluginStore` with no compatibility alias.** Any code written against the old FQN no longer
compiles. `IPluginStore` (199 lines) now only contributes the `MultiDescribleElement` marker
interface at `:184`.

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
| `shortComment()` | Chinese description | **abstract** (`DescriptorUseableShortComment:32`) | Always (<=10 chars) |
| `getDisplayName()` | Plugin name (pure English) | `clazz.getSimpleName()` (`Descriptor.java:1711`) | Always — the default yields the base class name |
| `getEndType()` | UI rendering hint | `EndType.Blank` (`IEndTypeGetter.java:111`) | When the UI needs an end-type icon |

Only `shortComment()` is truly abstract. The other two have working defaults, so omitting them
compiles cleanly — which is exactly why they are easy to forget.

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
| `createRelevantFieldKeysCreator(String toPropertyKey)` | Field whitelist factory (`:365`) | Cascading sources + target field | Override when candidates depend on a field the whitelist cannot reach |

Line numbers refer to `MultiDescribleElementSetSelector.java`.

### Fixed Methods (do not override)

| Method | Line | Why final |
|--------|------|-----------|
| `httpProcess(...)` | `:259` | The whole `get_desc` / `generate_target_instance` request flow, including the whitelist + validate + instantiate sequence |
| `getExtractProps(...)` | `:476` | Serializes `viewStyle` / `colsCfg` / `maxHeight` / `enumableSet` / `enableRowDragDrop` and enforces "Table view needs a clickable column" |

See [The `httpProcess` Request Flow](#the-httpprocess-request-flow) for what these do.

### Property Linkage (declared on the **host** descriptor)

| Method | Purpose | Notes |
|--------|---------|-------|
| `valueChangePipe(fromField, toField...)` | Link a host field change to a target | Call in the descriptor **constructor**; repeated calls with the same `fromField` merge |
| `.render(fn)` | Broadcast render | Called once per pipe, shared by all `toField` |
| `.render(toField, fn)` | Per-field render | Overrides broadcast; `toField` must already be registered |

**Detailed Documentation**: the element-descriptor hooks in the three tables above have extensive
Javadoc on `MultiDescribleElementSetSelector.MultiDescribleElementDescriptor`
(`tis-plugin/src/main/java/com/qlangtech/tis/plugin/MultiDescribleElementSetSelector.java:147`);
`IPluginStore.java` no longer contains any of it. The `valueChangePipe` / `render` methods live on
`ValueChangePipe` + `Descriptor` instead.

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
protected List<Pair<Option, IPluginStore.MultiDescribleElement>> getEnumableCandidateSet(
        ParseDescribable<?> describable) {
    List<Pair<Option, IPluginStore.MultiDescribleElement>> candidates = Lists.newArrayList();
    
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
- `Option.value` must match `MultiDescribleElement.identityValue()`: the picker posts the checked
  `Option.value`s back as `MultiDescribleElementSetSelector.target`, and `generate_target_instance`
  keeps only candidates whose `identityValue()` is in that set — a mismatch adds nothing, silently
- Use `.setChecked(true)` for already-selected items

**Not the same as property linkage.** `getEnumableCandidateSet` is re-invoked every time the 「设置」 dialog opens, so it *does* see the host's current values — but it does nothing when a host field changes while the dialog is closed. If the list (or a per-row dropdown) must react to a host field change **in place**, you need `valueChangePipe` (next section), not this hook.

### The `httpProcess` Request Flow

`MultiDescribleElementSetSelector.MultiDescribleElementDescriptor#httpProcess`
(`MultiDescribleElementSetSelector.java:259`) is
`final` — you never write it, but you must understand it, because it decides **which host fields your
`getEnumableCandidateSet` actually sees**.

The endpoint is `coredefine:plugin_action:description_process`, and **only the enumerable-set branch
reaches it** — with `getEnumableSetCfg().isEnumableSet() == false` the 「添加」 button goes straight to
an empty row form and none of the code below runs. The request carries
`type` (an `ActionType` token), `property` (the `MULTI_DESCRIBLE_PLUGIN` field name) and `impl`
(the element descriptor's FQN). The two tokens (`tis.plugin.ts:805` `PluginActionType`) are:

| `type` | Triggered by | POST body | Response |
|---|---|---|---|
| `get_desc` | 「添加」 click → `addMultiChildPluginWithEnumableSet` (`multi-describle-plugin-property.component.ts:814`) → `Descriptor.postToPlugin` (`tis.plugin.ts:942`) | host form values | `MultiDescribleElementSetSelector` descriptor JSON → renders the checkbox picker |
| `generate_target_instance` | user confirms the picker (same method, `:838`) | `{"host": {...}}` | selected elements serialized as `[{impl, vals}]` in `bizResult` → replaces the list field |

`MultiDescribleElementSetSelector` itself is a normal `Describable` with one
`@FormField(type = MULTI_SELECTABLE) List<IdentityName> target` (`:58`) — that field is where the
user's picks land, and its `manipuldateProcess` (`:76`) stashes the instance in the request `Context`
so the second request can read it back. The picker's option list comes from
`MultiDescribleElementSetSelector.getTargetCandidate()` (`:66`), which reads the candidate pairs back
out of that same `Context`; it is wired up by the resource file
`tis-plugin/src/main/resources/com/qlangtech/tis/plugin/MultiDescribleElementSetSelector.json`
(`"enum": "...getTargetCandidate():uncache_true"`). You never need to touch either — but if the
picker renders empty, check that the server put the pairs in the context *before* serializing
`multiDescribleElementSetDesc`.

Both branches build the whitelist the same way:

```java
RelevantFieldsContextAttrValMap.RelevantFieldKeys relevantFieldKeysCreator =
        createRelevantFieldKeysCreator(toPropertyKey);                       // :277

RelevantFieldsContextAttrValMap<?> ctx =
        RelevantFieldsContextAttrValMap.createRelevantFieldsContext(
                paramGetter, context,
                actionType == GetDesc ? postContent : postContent.getJSONObject("host"),
                relevantFieldKeysCreator);

// get_desc: validate -> addToFieldNewInstanceRelevantKeys() -> validate again -> createDescribable()
// generate_target_instance: addToFieldNewInstanceRelevantKeys() -> createDescribable()
```

**This is why a host field your candidates depend on can silently be `null`.** The phase-1 whitelist
is `descriptor.getValueChangeFromFieldKeys(toPropertyKey)` (`Descriptor.java:1777`) — i.e. only fields
that declare a `valueChangePipe` toward this list — plus the target field itself, added after the
first `validate()`. Any other host field is filtered out of the property set, so the partially
instantiated host has `null` there.

⚠️ That helper matches `pipe.getToField()` **exactly**, not by prefix. A host declaring
`valueChangePipe(KEY_DOMAIN, "parameters[].domainValue")` contributes **nothing** when
`toPropertyKey` is `"parameters"`: `"parameters[].domainValue"` is not `"parameters"`. So an
enumerable set whose candidates depend on the host's domain still needs the
`createRelevantFieldKeysCreator` override below.

**When your candidates depend on a field the whitelist cannot reach (no pipe, or only a bracketed
`toField`), override `createRelevantFieldKeysCreator`** (`MultiDescribleElementSetSelector.java:365`):

```java
@Override
protected RelevantFieldsContextAttrValMap.RelevantFieldKeys createRelevantFieldKeysCreator(
        String toPropertyKey) {
    return new RelevantFieldsContextAttrValMap.RelevantFieldKeys(toPropertyKey) {
        @Override
        protected List<String> toFieldValiateRelevantKeysCreator(Descriptor descriptor) {
            List<String> keys = new ArrayList<>(super.toFieldValiateRelevantKeysCreator(descriptor));
            keys.add(KEY_DOMAIN);        // dependency with no valueChangePipe
            return keys;
        }
    };
}
```

Note the two-phase semantics of the whitelist (`RelevantFieldsContextAttrValMap`):
fields collected in phase 1 (`fieldValRequired=true`) **must** have a value, phase 2 fields
(`addToFieldNewInstanceRelevantKeys`, `fieldValRequired=false`) are "carry it if present, don't fail
when absent". `super.toFieldValiateRelevantKeysCreator(...)` returns the phase-1 list — wrap it in a
new `ArrayList` before mutating, don't assume it is mutable.

Deliberately not calling `registerSelectOptions` on the host field, or writing an empty
`valueChangePipe` just to smuggle a field into the whitelist, are both wrong fixes: the former
breaks the form, the latter changes live-linkage behaviour to satisfy an unrelated dependency.

## Property Linkage (`valueChangePipe`)

A field on the host changes → something inside the child list must follow. Three shapes involve a
`MULTI_DESCRIBLE_PLUGIN` field, and these are the three this skill covers (2b is 2 one level deeper):

| # | Shape | `toField` form | What changes |
|---|---|---|---|
| 1 | **Child list replacement** | `"<listField>"` | The whole `List<T>` is swapped for a new batch of records |
| 2 | **Per-row child property options** | `"<listField>[].<childField>"` — e.g. `"children[].reactSelect"` | The candidate options of `<childField>` inside **every existing row** |
| 2b | **Per-row grandchild options** (through a nested polymorphic sub-form) | `"<listField>[].<descField>.<leafField>"` — e.g. `"series[].aggregation.property"` | The candidate options of `<leafField>` inside `<descField>`'s **every impl**, for every existing row |

Note the `.` in shapes 2/2b: the frontend splits `toField` on dots and supports **at most three
segments** (`item-prop-val.component.ts#propValChange`); anything longer is `console.warn` + skip.
Segments 2b adds: `<descField>` is itself a `@FormField`-declared polymorphic sub-form (an
`AttrDesc` with `describable=true` and several `descriptors`), so the pipe must rewrite the
`<leafField>` AttrDesc **in each impl's Descriptor** — one AttrDesc is not enough. A three-segment
`toField` whose first segment is *not* a `MULTI_DESCRIBLE_PLUGIN` field is rejected too: there is no
row instance to backfill and the linkage would silently do nothing.

Both are declared in the **descriptor constructor**. There is no annotation for this:

```java
protected ValueChangePipe valueChangePipe(String fromField, String... toField)
```

### Declaring it

```java
@TISExtension
public static final class DftDescriptor extends ParamsConfig.BasicParamsConfigDescriptor {

    public DftDescriptor() {
        super(KEY_DISPLAY_NAME);

        // ---- Shape 1: control changes -> replace the entire children list ----
        this.valueChangePipe(KEY_FIELD_CONTROL, "children")
                .render((pluginMeta, param) -> getChildren(param.getString(KEY_FIELD_CONTROL)));

        // ---- Shape 2: childrenCascade changes -> every row's reactSelect options change ----
        this.valueChangePipe(KEY_FIELD_CHILDREN_CASCADE, "children[]." + TestChild.KEY_FIELD_REACT_SELECT)
                .render((pluginMeta, param) -> Lists.newArrayList(IdentityName.create("kk")));
    }
}
```

`render` is a `BiFunction<UploadPluginMeta, IParamGetter, List<? extends IdentityName>>`:

- `param.getString(fromField)` reads the **new** value of the source field.
- Two overloads:
  - `render(fn)` — **broadcast**. Called **once** per pipe; every `toField` without its own render reuses the same list.
  - `render(toField, fn)` — **per-field**. Only for that `toField`, overriding the broadcast. Use it when one `fromField` drives targets of *different shapes*. Registering a render for a `toField` that was not passed to `valueChangePipe(...)` throws `IllegalArgumentException` at construction time — that's intentional, otherwise it is a silent no-op.
- Return **must not be `null`** (`Objects.requireNonNull` → NPE). An empty list is legal.
- Returning `MultiDescribleElement` instances makes the result serialize as a child-plugin JSON array `[{impl, vals}]`; anything else serializes as an option array `[{label, val}]`. This is decided by `ValueChangePipe.renderValueSerialize2Json`, not by the caller.

Calling `valueChangePipe(sameFromField, ...)` multiple times is **merged** into one pipe; `toField` is deduped and keeps first-seen order.

**Mix shapes freely in one call** when both targets need the same candidate list. `ChartLayer` feeds
the layer's group-by key and every row's segment field from one `dataSourceVar`:

```java
this.valueChangePipe(KEY_DATA_SOURCE_VAR, KEY_X_AXIS_PROPERTY,
                KEY_SERIES + "[]." + SeriesConfig.KEY_SEGMENT_BY)
        .render((pluginMeta, params) -> WidgetOptionHelper.getObjectPropertyOptions(...));
```

The broadcast runs once and both `toField` reuse the list — correct here because both targets want
the same property-name options. When the shapes genuinely differ (dropdown options vs. a whole list of
child plugins), use `render(toField, fn)` per field instead — see
`plugins/tis-ontology-plugin/.../workshop/widget/impl/ObjectListWidget.java:315` where
`titleProperty` / `cardFields` diverge.

### Shape 1 — child list replacement

Backend is the one line shown above; `getChildren()` is just a normal business method returning `List<TestChild>`.

Frontend (`item-prop-val.component.ts` → `propValChange`, non-dot branch) rebuilds the field:

```ts
if (toFieldAttr.type === TYPE_MULTI_DESCRIBLE_PLUGIN) {
  const mulitDescribePlugins = r.bizresult[toFieldName];
  if (Array.isArray(mulitDescribePlugins)) {
    const newToFieldProp = Item.wrapItemPropVal(item, mulitDescribePlugins, toFieldAttr);
    if (newToFieldProp && newToFieldProp !== toFieldProp) {
      item.replacePropVal(toFieldName, newToFieldProp);
    }
    newToFieldProp.primary = undefined;
    newToFieldProp.pendingHighlight = true;
  }
}
```

Four semantics you must design around:

1. **The row set is re-derived — it is not appended to.** Rows that disappear from the returned
   list are dropped. If your requirement is "add to what's already there", this pipe is the wrong
   tool; use the enumerable-set 「设置」 button instead.
2. **Rows are matched to the old list by `identityValue`.** `Item.replacePropVal`
   (tis.plugin.ts) walks `oldVals` and, for every identity key that also exists in the new list,
   reuses the **old `Item` instance** — so a row the user has edited but not yet saved keeps those
   edits. Only identities absent from the old list are built fresh from the server payload. That
   means the same `identityValue()` must identify "the same row" across both payloads; if your
   element's identity is something like a random id, every linkage fires as a full rebuild.
3. **The new instance must be swapped in via `replacePropVal`.** Mutating the old instance leaves the
   template bound to the old `descVal` — nothing visibly changes.
4. **The result must be an array.** A non-array makes `Array.isArray` false and the frontend silently
   skips it.

### Shape 2 — per-row child property options

**Step 0 — the element field must be a `SELECTABLE` and it must be registered.** A pipe only rewrites
*candidate options*; it cannot turn a free-text input into a dropdown. So:

```java
// element class
@FormField(ordinal = 4, type = FormFieldType.SELECTABLE)   // INPUTTEXT -> the pipe has nowhere to land
public String segmentBy;

// element descriptor — MANDATORY
public DefaultDescriptor() {
  super();
  this.registerSelectOptions(KEY_SEGMENT_BY, Collections::emptyList);
}
```

The registration is not about supplying the real options (the pipe supplies those, field by field, at
render time). Without it `DescriptorsJSON#getSelectOptions` throws
`fieldName:X is select options has not been register` and **the whole form fails to open**. Register
`Collections::emptyList` as the fallback; precedent: `ObjectSetFilterConfig#property`
(plugins/tis-ontology-plugin/.../definition/).

`ENUM` is the wrong choice here even though the backfill also writes `eprops['enum']`: per the
project's `CLAUDE.md` rule 2, `ENUM` means "the candidate set is fixed in Java", while a pipe-supplied
candidate set is by definition runtime data. Use `SELECTABLE`.

The magic of the `toField` string:

- `children` — the host's `MULTI_DESCRIBLE_PLUGIN` field name;
- `[]` — "apply to **each record** of that list";
- `reactSelect` — the field name on the **element class** `TestChild`.

The server **never parses** `[]` or `.`. It stores the string verbatim and returns it verbatim as the `bizresult` key. The single parsing point is the frontend, which relies on `[]` for two things: ① strip it before matching `ItemPropVal.key` (keys never carry brackets, otherwise `find` always misses and the pipe silently dies); ② use it to pick the "per-row backfill" branch instead of the "clear and rebuild" branch.

The frontend then does **two things, both mandatory**:

**1) Rewrite the shared `AttrDesc` — this covers rows created later:**

```ts
for (const descriptor of descPropVal.descVal.descriptors.values()) {
  const attrDesc: AttrDesc = descriptor.findAttrDesc(cascadeField, false);
  if (!attrDesc) continue;
  attrDesc.eprops = attrDesc.eprops || {};      // may be undefined -> TypeError otherwise
  attrDesc.multiSelectableTypeOptions = enums;  // ENUM(5) reads eprops['enum']
  if (attrDesc.type === TYPE_PLUGIN_SELECTION) {
    attrDesc.options = enums;                   // SELECTABLE(6) reads AttrDesc.options
  }
}
```

**2) Backfill every existing row — this covers rows already in the list:**

```ts
for (const row of descPropVal.descVal.valItems) {
  const rowItem = row && row.item;
  if (!rowItem || !rowItem.dspt) continue;
  const rowProp = rowItem.propVals.find(p => p.key === cascadeField);
  if (!rowProp) continue;

  rowProp.setEProp(KEY_OPTIONS_ENUM, enums);      // ENUM(5) candidate set
  if (rowProp.type === TYPE_PLUGIN_SELECTION) {
    rowProp.options = enums;                      // must be a NEW array reference — enum-icon-select is OnPush
  }
  rowProp.primary = undefined;                    // candidate changed -> old selection is stale
  rowProp.has_set_primaryVal = true;              // else dftVal refills it in "new" mode
}
descPropVal.pendingHighlight = true;              // a table shows text only — flash to signal the change
```

Why step 2 cannot be skipped: **each row shallow-copied `eprops` and captured `options` by reference when it was created.** Changing the shared `AttrDesc` only affects rows created afterwards; existing rows each hold their own copy and must be patched one by one.

### Shape 2b — through a nested polymorphic sub-form

Same two halves, one level deeper: `"series[].aggregation.property"` where `aggregation` is itself a
polymorphic `AttrDesc` (6 impls). Two differences:

**1) Rewrite the leaf AttrDesc in *every impl*, not just one:**

```ts
const holder: AttrDesc = descriptor.findAttrDesc(descendantField, false);   // "aggregation"
if (!holder.describable || !holder.descriptors) { /* not a sub-form -> abort loudly */ }
holder.descriptors.forEach((implDesc: Descriptor) => {
  const leaf = implDesc.findAttrDesc(leafField, false);                     // "property"
  if (leaf) patchAttrDesc(leaf, enums);
});
```

If you patch only the currently-selected impl, switching `aggregation` to another impl later shows an
empty dropdown — because `changePlugin` swaps `descVal.dspt` and keeps `vals`, so the untouched impl's
AttrDesc is what the newly bound control reads.

**2) Backfill the row's `DescVal` — it *is* an `Item`.** `DescribleVal extends Item`, and switching
impl swaps `dspt` while keeping `vals`, so the row's leaf value lives on `rowProp.descVal.propVals`:

```ts
const leafProp = rowProp.descVal.propVals.find(p => p.key === leafField);
if (leafProp) patchRowItemProp(leafProp, enums);
```

⚠️ The backend needs no change, but `AbstractSeriesAggregation`-style base classes must put the
`registerSelectOptions` call in the **abstract base descriptor's constructor** so all six impls inherit
it — see `AbstractSeriesAggregation.BasicDescriptor`.

⚠️ Precedent for the shape: `ChartLayer` declares one pipe with three `toField`s (plain field +
2-segment + 3-segment) and a single broadcast `render`, because all three want the same option list.
Only reach for `render(toField, fn)` when the shapes genuinely differ.

⚠️ **Never call `clearPropVals(true)` on a `MULTI_DESCRIBLE_PLUGIN` field.** `Item.clearPropVals` deletes `_propVals` / sets `vals = {}` / `impl = null` / `dspt = null` — for a type-13 container that means deleting every record row, and this branch has no rebuild path. (`clearPropVals(true)` is correct for the *non-list* form such as `aggregation.targetProperty`, which is why the two branches are mutually exclusive.)

### Edit mode replay: `$pipe_field$`

Opening an already-saved plugin to edit it triggers a separate path: nobody changed `control`, but each row's `reactSelect` still has to show the right options.

`RootFormProperties` runs each pipe whose `fromField` is a **plain (non-Describable, non-type-13) property** and writes the result as:

```text
vals["$pipe_field$" + toField] = ValueChangePipe.renderValueSerialize2Json(...)
```

The frontend reads it back with the same prefix (`PIPELINE_FIELD_PREFIX` in `type.utils.ts`):

```text
$pipe_field$<attrKey>                        plain field
$pipe_field$<attrKey>.<childKey>             sub-property of a Describable field
$pipe_field$<attrKey>[].<childKey>           field inside each row of a type-13 list
$pipe_field$<attrKey>[].<descKey>.<childKey> grandchild: row's polymorphic sub-form field
```

This replay is what makes `[]` useful at load time too. `Item.createChildDescrible` walks the path and
carries a `PipeCtx` (`tis.plugin.ts:1433`) down:

```ts
export interface PipeCtx {
  /** candidate anchors, outermost first, the current instance last */
  chain: PipeAnchor[];     // PipeAnchor = {item: Item, path: string}
  /** already inside a MULTI_DESCRIBLE_PLUGIN row (decides whether to also patch the shared AttrDesc) */
  inList: boolean;
}
```

Lookup is `Item.getCascadeSelectOpts(pipeCtx, at)` (`:1722`), which walks `chain` **last → first**
(nearest anchor first) and returns the first `anchor.item.vals["$pipe_field$" + anchor.path + at.key]`
that is an array. That chain is what lets one declaration serve rows whose key is emitted on the row
itself (`ChartLayer`'s in-row `$pipe_field$xAxisProperty`) and instances where it is emitted on the
parent (`ObjectSetDefinitionConfig`'s `filters[].property`). `inList` is what decides whether the
cascade options must also be written back to the **shared** `AttrDesc` (rows are built from it, so
without the write-back rows added later have empty dropdowns). `Item.wrapItemVals(pipeCtx?)` recurses
through `wrapItemPropVal(..., pipeCtx)`.

⚠️ Adding a deeper shape means threading `pipeCtx` everywhere. The old parameterless
`getCascadeSelectOpts(attr, childAttr)` on `Item` was removed in favour of
`Item.getCascadeSelectOpts(pipeCtx, at)`. If you re-add a local variant that ignores the anchor chain,
the load path silently reads a key the server never wrote → empty dropdowns in edit mode only.

⚠️ Do not "simplify" the chain to a single root item. Per-instance emission means the hit point can be
*any* level; a root-only lookup still compiles, still renders the form, and just returns `undefined`
for every anchor that isn't the outermost one.

Three consequences:

1. This path only supplies **initial** options for rows that already exist. Once the user changes the
   source field, in-place row updates come only from shape 2's per-row backfill.
2. Exceptions here are only `logger.warn`-ed — they never fail the page. If "the dropdown is empty in
   edit mode", check the server log for warnings first.
3. Because it replays per `fromField`, **`fromField` must be a same-level plain field**. A field
   declared inside a `Describable` sub-form is never collected.

### Linkage checklist

Run through this before shipping — every row is a *silent* failure mode:

| # | Rule | Violation |
|---|---|---|
| 1 | `fromField` must be same-level as `toField`, and a plain field | Never fires, no error |
| 1b | Shape 2's `<childField>` must be `SELECTABLE` (not `INPUTTEXT`) and registered in the element descriptor's `registerSelectOptions` | Left as `INPUTTEXT` the options are rewritten into `AttrDesc.options` / `ItemPropVal.options` that nothing reads — silent, user keeps typing free text; without the registration the form won't open at all |
| 2 | `render` must not return `null` | NPE |
| 3 | `toField` string must match **character for character** across server and frontend (`children[]` — dropping `[]` breaks it) | Frontend `find` misses / `bizresult` lookup misses; silent |
| 4 | `render(toField, fn)` requires that `toField` was registered first | `IllegalArgumentException` at construction |
| 5 | Shape 2 requires **both** halves (shared `AttrDesc` + per-row backfill) | New rows get options, old rows don't (or vice versa) |
| 5b | Shape 2b must patch the leaf AttrDesc in **every impl**, and backfill `rowProp.descVal.propVals` (DescVal *is* an Item) | Options appear for the currently selected impl only; switching impl shows an empty dropdown |
| 6 | When clearing a row's `primary`, also set `has_set_primaryVal = true` | In "new" mode `dftVal` refills the stale value |
| 7 | Never `clearPropVals(true)` in the type-13 branch | Whole list wiped, unrecoverable |
| 8 | Frontend returns early when the source value is `""` / `0` / `false` / `[]` | "Clearing the dropdown" does not re-render. Resetting to a default candidate set is not supported by the current mechanism |

Two more engineering notes:

- **No debounce, no request sequencing, no cancellation.** Two fast changes can land out of order and the later response wins. Keep `render` cheap and never let side effects depend on response order.
- **The path depth cap is three segments.** `"<listField>[].<descField>.<leafField>"` is the deepest
  supported form (shape 2b). Four or more segments, empty segments, or a three-segment form whose
  first segment is not a `MULTI_DESCRIBLE_PLUGIN` field are `console.warn`-ed and skipped
  (`item-prop-val.component.ts:112-130`). The backend never validates this — an over-deep `toField`
  is stored and returned verbatim; in production the only symptom is the linkage silently not
  firing, because the `console.warn` that would have told you is in the browser console.

`valueChangePipe` is *not* the same thing as the enumerable candidate set (`getEnumableCandidateSet`): the latter only controls what the 「设置」 dialog offers, and is re-read at dialog-open time — it does not react to host field changes.

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
   - If enumerable: what determines the candidate list, and for each host field it reads, does a
     `valueChangePipe` declare this list as `toField` **verbatim** (`"<listField>"`, not
     `"<listField>[].x"`)? If not, a `createRelevantFieldKeysCreator` override is required —
     otherwise the field is `null` (see [The `httpProcess` Request Flow](#the-httpprocess-request-flow))
   - Enable row drag-drop sorting?
   - If drag-drop: which field stores the order?
   - **Property linkage**: does any host field change need to affect the list? If so, pick the shape:
     - *Child list replacement* — the whole `List<T>` is re-derived from the host field
       (`valueChangePipe(fromField, "<listField>")`)
     - *Per-row child property options* — one field **inside every row** gets new candidates
       (`valueChangePipe(fromField, "<listField>[].<childField>")`)
     - Both at once is fine (they are two separate `fromField`s / pipes). Note shape 1 re-derives the
       row set: rows whose identity disappears from the returned list are dropped.
   - Which host field drives it, and is that field a **same-level plain field** (not nested in a
     Describable sub-form, not the `MULTI_DESCRIBLE_PLUGIN` field itself)?

### Step 2: Design Structure

Present the design to the user for approval before implementation.

### Step 3: Generate Host Plugin Field

If the host plugin already exists, show the code to add to it.
If the host plugin doesn't exist, delegate to `/create-plugin` first.

### Step 4: Generate Element Plugin

Generate the complete element plugin with all required files.

### Step 5: Verify Implementation

Check against comprehensive checklist covering host field, element class, descriptor, and standard
plugin requirements. If any `valueChangePipe` was declared, also walk the
[linkage checklist](#linkage-checklist) — those failures are all silent, so nothing in the build will
catch them. If the enumerable set was enabled, additionally check:

- the `extends` clause reads `MultiDescribleElementSetSelector.MultiDescribleElementDescriptor<...>`
  (the old `IPluginStore.MultiDescribleElementDescriptor` FQN no longer exists and does not compile)
- every host field read inside `getEnumableCandidateSet` is reachable: either some `valueChangePipe`
  declares this list as `toField` **verbatim** (a bracketed `"<listField>[].x"` does not count), or the
  field is listed by an overridden `createRelevantFieldKeysCreator`
- `Option.value` equals the candidate's `identityValue()` character for character

### Step 6: Provide Usage Instructions

Explain where files were created, how to test, and frontend integration details.

## Common Patterns

### Pattern 1: Simple Table (Open Set)

User manually creates configuration rows (property mappings, column definitions, filter rules).

### Pattern 2: Enumerable Set with Dependencies

Choose from templates/candidates that depend on another field (Action parameters depend on Action type).

### Pattern 3: List View with Drag-Drop

Simple list with explicit ordering (priority queue, execution sequence).

### Pattern 4: Host Field Drives the Whole Child List

`control` selects a template family; the `children` list is fully re-derived from it
(`valueChangePipe("control", "children")`). Use when the child records are *derived data* rather than
hand-entered data — see shape 1.

### Pattern 5: Host Field Drives a Per-Row Dropdown

`childrenCascade` narrows the options of `reactSelect` inside every existing row
(`valueChangePipe("childrenCascade", "children[].reactSelect")`). Use when each row picks one value
from a candidate set that depends on a host-level selection — see shape 2.

### Pattern 6: Where the Driving Field Can Live

The `valueChangePipe` registry is **per descriptor**, and the frontend looks it up as
`item.dspt.valueChangePipes` on whatever `Item` owns the changed field. That means:

- A **host** descriptor's pipe drives fields on the host (both patterns above).
- An **element** descriptor *can* declare its own pipe for fields on that element class, and the row
  edit dialog (which renders the element's own descriptor) will honour it. No implementation in this
  repo does this today, so treat it as untested.
- Host field → field inside a nested row (**cross-level**) is **not** supported: the host's pipe only
  reaches its own `toField` names, and `RootFormProperties` collects pipe sources from plain
  same-level fields only.

## Reference Implementations

1. **TestChild** (`tis-plugin/src/main/java/com/qlangtech/tis/manage/common/TestChild.java:44`) —
   complete reference. Abstract element class at `:44`, `identityValue()` at `:71`, and
   `DftDescriptor extends MultiDescribleElementSetSelector.MultiDescribleElementDescriptor<TestChild>`
   at `:100` with both an enumerable candidate set and a `registerSelectOptions` call.
2. **ActionParameterConfig** (`plugins/tis-ontology-plugin/.../impl/action/ActionParameterConfig.java:281`)
   — advanced enumerable-set example: `getEnumableSetCfg` at `:304`, `getEnumableCandidateSet` at `:309`.
   ⚠️ Still written against the **old** `IPluginStore.MultiDescribleElementDescriptor` FQN, so it is
   currently uncompilable against this tis-plugin tree — the rest of the class is the pattern to copy.
3. **UserProfile.children** (`tis-plugin/.../manage/common/UserProfile.java:97`) — host field example
   (`@FormField(ordinal = 7, desClazz = TestChild.class, type = MULTI_DESCRIBLE_PLUGIN,
   validate = {Validator.require})` at `:96`), and **both** linkage shapes in `UserProfile.DftDescriptor`:
   `valueChangePipe(KEY_FIELD_CONTROL, "children")` at `:187` and
   `valueChangePipe(KEY_FIELD_CHILDREN_CASCADE, "children[]." + KEY_FIELD_REACT_SELECT)` at `:196`.
4. **ObjectSetDefinitionConfig** (`plugins/tis-ontology-plugin/.../workshop/model/definition/ObjectSetDefinitionConfig.java:209`)
   — real production use of shape 2: `objectType` → `filters[].property`
5. **ChartLayer** (`plugins/tis-ontology-plugin/.../workshop/model/ChartLayer.java:221`) — one
   `fromField` feeding three targets at once: `dataSourceVar` → `xAxisProperty` (plain),
   `series[].segmentBy` (shape 2) **and** `series[].aggregation.property` (shape 2b). Element-side
   `SELECTABLE` + `registerSelectOptions` halves live in `SeriesConfig` and
   `AbstractSeriesAggregation.BasicDescriptor`
   (`.../workshop/widget/impl/chartxy/AbstractSeriesAggregation.java` — the latter is where an
   abstract base must register, so all six impl descriptors inherit it)
6. **Frontend Handler** (`tis-console/src/common/plugin/item-prop-val.component.ts`) — the exported
   function `propValChange` starts at line **68** (the component method that forwards to it is at
   `:1192`). It is the single parsing point for `[]` / `.`; the three-segment cap is enforced at
   `:112-113`.
7. **Frontend picker flow** (`tis-console/src/common/plugin/multi-describle-plugin-property.component.ts`)
   — `addMultiChildPluginWithEnumableSet` at `:814` issues `type=get_desc`, then
   `type=generate_target_instance` at `:838`; `enumableSetItemDependencyDepth` selects the parent item
   the host form is projected from (`:815`). Branching on `enumableSet` is in the template at `:65`.
8. **Load-path replay** (`tis-console/src/common/tis.plugin.ts`) — `PipeCtx` at `:1433`,
   `Item.getCascadeSelectOpts` at `:1722`, `Item.createChildDescrible` at `:1775`,
   `Item.wrapItemVals(pipeCtx?)` at `:2088`; every level must thread the context or edit-mode
   dropdowns go empty
9. **Pipe implementation + unit tests** (`tis-plugin/.../extension/ValueChangePipe.java`,
   `tis-plugin/src/test/java/com/qlangtech/tis/extension/TestValueChangePipe.java`)

## Important Notes

1. **Do NOT confuse with `@SubForm`**: Use `MULTI_DESCRIBLE_PLUGIN` for complex elements with full plugin lifecycle
2. **Identity field is mandatory**: Required for element tracking, updates, and duplicate detection
3. **Descriptor methods extensively documented**: Read Javadoc on
   `MultiDescribleElementSetSelector.MultiDescribleElementDescriptor`
   (`tis-plugin/src/main/java/com/qlangtech/tis/plugin/MultiDescribleElementSetSelector.java:147`).
   `IPluginStore.java` no longer hosts this class.
4. **Class location moved — no alias**: The element Descriptor base is
   `MultiDescribleElementSetSelector.MultiDescribleElementDescriptor`. `IPluginStore` keeps only the
   `MultiDescribleElement` marker interface (`IPluginStore.java:184`). Code written against
   `IPluginStore.MultiDescribleElementDescriptor` fails to compile; grep for it before assuming a
   plugin is healthy.
5. **Test thoroughly**: Test add/edit/delete, enumerable selection, drag-drop, validation
6. **Follow standard plugin conventions**: All `/create-plugin` rules apply
7. **Linkage is silent when wrong**: every rule in the linkage checklist fails without an error.
   Verify by actually changing the source field in the browser — a compile-clean pipe proves nothing.
8. **Whitelist is silent when wrong**: a host field missing from the whitelist is `null` inside
   `getEnumableCandidateSet` — no exception, just an empty/wrong candidate list. A bracketed pipe target
   (`"<listField>[].x"`) does **not** put `<listField>` itself in the whitelist.
