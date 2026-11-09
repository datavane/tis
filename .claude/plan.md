# 级联选项为空问题分析

## 问题现象

**后端数据**（module GET 响应，293行打印）：
```json
"vals": {
  "$pipe_field$filters[].property": [
    {"val": "report_id", "label": "report_id", ...},
    {"val": "report_date", "label": "report_date", ...},
    ...
  ],
  "filters": [
    {
      "impl": "FilterItemConfig",
      "vals": {
        "property": "report_id",  // ✅ 有值
        ...
      }
    }
  ]
}
```

**前端查询结果**（tis.plugin.ts:2057）：
```typescript
[
  "com.qlangtech.tis.plugin.ontology.workshop.widget.FilterItemConfig",
  "property",
  "report_date",
  null  // ❌ cascadeSelectOpts 为空
]
```

---

## 根因分析

### 问题出在哪

**`Item.getCascadeSelectOpts(ctx, at)` 查询逻辑**：

```typescript
private static getCascadeSelectOpts(pipeCtx: PipeCtx, at: AttrDesc): Array<any> {
  for (let i = pipeCtx.chain.length - 1; i >= 0; i--) {
    const anchor = pipeCtx.chain[i];
    const opts = anchor.item?.vals?.[`${PIPELINE_FIELD_PREFIX}${anchor.path}${at.key}`];
    //                                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    //                                  查询键 = "$pipe_field$" + anchor.path + at.key
    if (Array.isArray(opts)) {
      return opts;
    }
  }
  return undefined;
}
```

---

### 关键：查询键的构造

#### 后端下发的键

后端 `RootFormProperties#getInstancePropsJson` 对 `FilterListWidget` 下发：

```
"$pipe_field$filters[].property": [...]
```

这个键**挂在 FilterListWidget 实例上**（父级）。

#### 前端查询时构造的键

当前端处理 `FilterItemConfig` 实例（filters 数组的元素）的 `property` 字段时：

1. **调用点**：`FilterItemConfig.wrapItemVals()` 第 2054 行
   ```typescript
   let cascadeSelectOpts = Item.getCascadeSelectOpts(ctx, at);
   ```

2. **此时的 `ctx.chain`**（由 `createChildDescrible` 在 1709-1713 行构造）：
   ```typescript
   [
     {item: FilterListWidget实例, path: "filters[]."},  // 父锚点
     {item: FilterItemConfig实例, path: ""}             // 当前锚点
   ]
   ```

3. **查询逻辑**：
   - 先查**当前锚点**（`path = ""`）：
     ```
     key = "$pipe_field$" + "" + "property"
         = "$pipe_field$property"
     ```
     → FilterItemConfig 实例上没有这个键 ❌

   - 再查**父锚点**（`path = "filters[]."`）：
     ```
     key = "$pipe_field$" + "filters[]." + "property"
         = "$pipe_field$filters[].property"
     ```
     → FilterListWidget 实例上**有**这个键 ✅

---

### 为什么查不到？

**问题出在父锚点的 `item.vals` 状态**：

当 `FilterItemConfig.wrapItemVals()` 执行时（第 2054 行），父级 `FilterListWidget` 的 `this.vals` **还没有赋值**！

看 `FilterListWidget.wrapItemVals()` 的执行顺序：

```typescript
public wrapItemVals(pipeCtx?: PipeCtx): void {
  const ctx: PipeCtx = Item.pipeCtxOf(pipeCtx, this);
  let newVals = {};
  let ovals: any = this.vals;  // ← 读取原始 vals（包含 $pipe_field$ 键）

  this.dspt.attrs.forEach((at) => {
    let v = ovals[at.key];
    let cascadeSelectOpts = Item.getCascadeSelectOpts(ctx, at);  // ← 在这里查询
    
    if (at.key === 'filters') {  // TYPE_MULTI_DESCRIBLE_PLUGIN
      // wrapItemPropVal 会递归调 FilterItemConfig.wrapItemVals()
      // 但此时 this.vals 还是旧的 ovals（还有 $pipe_field$ 键）
      newVal = Item.wrapItemPropVal(this, v, at, ctx);
    }
    
    if (newVal) {
      newVals[at.key] = newVal;
    }
  });
  
  this.vals = newVals;  // ← 赋值在最后，递归调用时还是旧 vals
}
```

**时间线**：
1. `FilterListWidget.wrapItemVals()` 开始
2. `ovals = this.vals`（包含 `$pipe_field$filters[].property`）
3. 遍历到 `filters` 字段
4. 调用 `Item.wrapItemPropVal(this, v, at, ctx)`
5. → 进入 `createChildDescrible` 创建 `FilterItemConfig` 子实例
6. → 递归调用 `FilterItemConfig.wrapItemVals(childCtx)`
7. → 在子实例里查询 `getCascadeSelectOpts(childCtx, {key: "property"})`
8. → 查父锚点：`ctx.chain[0].item.vals["$pipe_field$filters[].property"]`
9. → **此时 `ctx.chain[0].item` 就是 `FilterListWidget` 实例，但它的 `this.vals` 还是旧的 `ovals`** ✅
10. → 应该能查到 ✅

---

### 真正的问题

让我重新检查：`ctx.chain[0].item` 指向的是什么？

在 `createChildDescrible` 第 1705 行：
```typescript
const ctx: PipeCtx = Item.pipeCtxOf(pipeCtx, item);
//                                            ^^^^
//                                            这是传入的父 item
```

而 `FilterListWidget.wrapItemVals()` 第 2060 行调用时：
```typescript
newVal = Item.wrapItemPropVal(this, v, at, ctx);
//                            ^^^^
//                            传的是 this（FilterListWidget 实例）
```

所以 `ctx.chain[0].item` 确实指向 `FilterListWidget` 实例。

**但是**，`getCascadeSelectOpts` 查的是 `anchor.item?.vals?.[key]`。

此时 `FilterListWidget` 实例的 `this.vals` **还是**原始 JSON 响应里的 `vals` 对象（包含 `$pipe_field$` 键），因为 `this.vals = newVals` 在最后才执行。

---

### 验证假设

让我再看一遍调用链，确认 `this.vals` 的状态：

**module GET 响应 → Descriptor.wrapItemVals(item)**

在 `workshop-module-api.service.ts:292` 打印的 `module.pages[0]` 里，`widgets[0]` 的结构是：
```json
{
  "impl": "...",
  "vals": {
    "$pipe_field$filters[].property": [...],  // ← 在这里
    "filters": [...]
  }
}
```

这个原始 JSON 对象被传给 `Descriptor.wrapItemVals(item)`：
```typescript
public wrapItemVals(item: any): Item {
  let savedItem: Item = Object.assign(new Item(this), item);
  savedItem.wrapItemVals();
  return savedItem;
}
```

所以 `savedItem.vals` 初始就是那个包含 `$pipe_field$` 键的对象。

然后 `savedItem.wrapItemVals()` 执行时，`ovals = this.vals` 读到的就是它。

---

## 问题确认

**实际问题不在 `wrapItemVals` 的递归时序，而在于 `getCascadeSelectOpts` 查询时 `anchor.item.vals` 的内容**。

让我检查 `getCascadeSelectOpts` 第 1691 行查询的键是否正确：

```typescript
const opts = anchor.item?.vals?.[`${PIPELINE_FIELD_PREFIX}${anchor.path}${at.key}`];
```

父锚点：
- `anchor.item` = FilterListWidget 实例
- `anchor.path` = `"filters[]."`
- `at.key` = `"property"`
- 查询键 = `"$pipe_field$filters[].property"` ✅

FilterListWidget 实例的 `this.vals` 里**确实有**这个键（从 module GET 响应带过来的）。

---

## 最可能的原因

**console.log 打印时机不对，或者 ctx.chain 构造有误**。

让我检查 `createChildDescrible` 第 1709-1713 行的 `childChain` 构造：

```typescript
const childChain: PipeAnchor[] = ctx.chain.map((anchor) => ({
  item: anchor.item,
  path: anchor.path + at.key + (at.type === TYPE_MULTI_DESCRIBLE_PLUGIN ? '[]' : '') + '.'
}));
```

对于 `filters` 字段（`TYPE_MULTI_DESCRIBLE_PLUGIN`）：
- 父锚点 `{item: FilterListWidget实例, path: ""}` 
- 转换后 `{item: FilterListWidget实例, path: "" + "filters" + "[]" + "." = "filters[]."}`

然后压入当前子实例：
```typescript
childChain.push({item: childItem, path: ''});
```

所以 `childCtx.chain` = `[{item: FilterListWidget, path: "filters[]."}, {item: FilterItemConfig, path: ""}]`

查询时从后往前：
1. 先查 `FilterItemConfig.vals["$pipe_field$property"]` → 没有
2. 再查 `FilterListWidget.vals["$pipe_field$filters[].property"]` → **应该有**

---

## 诊断结论

**cascadeSelectOpts 为 null 的唯一可能原因**：

`FilterListWidget` 实例的 `this.vals["$pipe_field$filters[].property"]` **不存在或不是数组**。

可能的情况：
1. **`$pipe_field$` 键被提前删除了**
2. **该键的值不是数组**（`getCascadeSelectOpts` 第 1692 行的 `Array.isArray(opts)` 判定失败）
3. **`ctx.chain[0].item` 不是预期的 FilterListWidget 实例**

---

## 建议的调试步骤

在 `tis.plugin.ts` 第 2057 行的 console.log 之前，增加以下调试信息：

```typescript
if ('property' === at.key) {
  console.log('=== DEBUG property 查询 ===');
  console.log('ctx.chain:', ctx.chain.map(a => ({
    impl: a.item?.dspt?.impl,
    path: a.path,
    hasVals: !!a.item?.vals,
    valsKeys: a.item?.vals ? Object.keys(a.item.vals).filter(k => k.startsWith('$pipe_field$')) : []
  })));
  console.log('查询的键:', ctx.chain.map(a => `$pipe_field$${a.path}${at.key}`));
  console.log('查询结果:', ctx.chain.map(a => {
    const key = `$pipe_field$${a.path}${at.key}`;
    const val = a.item?.vals?.[key];
    return {key, exists: val !== undefined, isArray: Array.isArray(val), length: Array.isArray(val) ? val.length : 'N/A'};
  }));
  console.log([this.dspt.impl, at.key, v, cascadeSelectOpts]);
}
```

这样可以明确看到：
- chain 里有几个锚点，每个锚点的 path 是什么
- 每个锚点的 vals 里有哪些 `$pipe_field$` 键
- 查询时构造的键是什么，查询结果是什么