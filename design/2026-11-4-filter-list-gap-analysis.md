# Filter List Widget：Palantir vs TIS 实现差异分析

## 摘要

基于 Palantir 官方文档（`/opt/misc/palantir-study/foundry/workshop/detail/04-widgets/widgets-filter-list.md`）及截图，
与 TIS 当前实现（`FilterListWidget.java` + `filter-list.component.ts`）对比，发现 **14 项功能差异**。
按优先级分为三档：P0（核心缺失）5 项、P1（重要增强）5 项、P2（高级特性）4 项。

---

## 一、核心功能缺失（P0）

### 1. **直方图（Histogram）组件**

**Palantir 有什么**：
- 截图 `filter_list_configured.png` 显示 "Arrival delay (minutes)" 字段渲染成直方图，
  横轴是数值区间，纵轴是对象计数，用户点击柱子即过滤到该区间。
- 文档 L38："Options include keyword, histogram, single- and multi-select dropdowns, 
  **distribution chart**, single- and multi-date pickers, and timeline displays."

**TIS 当前状态**：
- 后端只有 4 种组件：`KeywordComponent` / `SingleSelectComponent` / `MultiSelectComponent` / `DatePickerComponent`。
- **完全没有** `HistogramComponent` 或 `DistributionChartComponent`。

**差距**：
- 数值属性（整数/浮点）无法可视化分布；
- 用户不能通过点击柱子快速筛选数值区间。

**实现方向**：
1. 后端新建 `HistogramComponent extends FilterComponent`：
   - 字段：`bucketCount`（区间数，默认 10）、`minValue` / `maxValue`（可选，自动推导）；
   - `createPayloadProps()` 返回 `{buckets: [{min, max, count}]}`（服务端聚合查询）。
2. 前端新建 `histogram-filter.component.ts`：
   - 用 ECharts 柱状图渲染，点击柱子写 `{operator: 'between', value: [min, max]}` 到 `filter.value`。
3. 只对数值属性类型（`INTEGER` / `DOUBLE`）开放该组件选项。

---

### 2. **Pills 布局（横向胶囊）**

**Palantir 有什么**：
- 截图 `filter_list_configured_pills.png` 显示过滤项横向排列成胶囊按钮（pill），
  每个 pill 显示 "属性名: 当前值"，点击弹出 popover 编辑该项。
- 文档 L46-50：
  ```
  * **Vertical layout** — top to bottom in a vertical scrolling fashion.
  * **Pills layout** — horizontally within an interactive pill. 
    Once selected, the pill opens a popover with filter configuration UI.
  ```

**TIS 当前状态**：
- 前端模板只有一种布局：垂直列表（`<div *ngFor="let filter of filters" class="filter-item">`），
  每个过滤项占一整行。
- **没有** `layout` 配置字段，没有 pill 渲染模式。

**差距**：
- 过滤项多时占用大量纵向空间；
- 无法紧凑展示已选过滤条件（pills 可以只显示有值的项）。

**实现方向**：
1. 后端 `FilterListWidget` 新增字段：
   ```java
   @FormField(type = FormFieldType.ENUM, ordinal = 19)
   public LayoutMode layout;  // VERTICAL / PILLS，默认 VERTICAL
   
   public enum LayoutMode { VERTICAL, PILLS }
   ```
2. 前端模板按 `config.layout` 分支：
   - `VERTICAL`：保持现有 `<div class="filter-item">` 布局；
   - `PILLS`：渲染成 `<nz-tag *ngFor="..." (click)="openFilterPopover(filter)">`，
     点击用 `nz-popover` 弹出原有的输入控件。
3. Pills 模式只显示**有值**的过滤项（`!isEmptyFilterValue(filter.value)`），
   加一个 "+ Add filter" 按钮让用户选其余项（见第 3 项）。

---

### 3. **用户动态增删过滤项（Allow user to add and remove filters）**

**Palantir 有什么**：
- 文档 L44-45："If enabled, users will see an **Add filter** button within the widget 
  and will be able to add and remove filterable properties."
- 截图 `filter_list_configured.png` 右上角有 "+ Add filter" 按钮，
  点击后弹出属性选择器，用户可临时增删过滤项（不落盘配置）。

**TIS 当前状态**：
- 过滤项列表（`filters`）是**纯配置**，只有模块构建者在配置面板里能改。
- 最终用户看到的是固定列表，无法自行增删。
- **没有** "允许用户增删" 的开关字段。

**差距**：
- 最终用户只能用模块构建者预设的那几个过滤项；
- 无法临时加一个维度过滤、或隐藏不需要的项。

**实现方向**：
1. 后端 `FilterListWidget` 新增字段：
   ```java
   @FormField(type = FormFieldType.INPUTTEXT, ordinal = 23)
   public Boolean allowUserModify;  // 默认 false
   ```
2. 前端逻辑：
   - `allowUserModify === true` 时，渲染 "+ Add filter" 按钮；
   - 点击后弹出 `nz-select`，候选项 = `objectSetVar` 的全部属性 - 已有 `filters`；
   - 用户选中后，把新行 push 进 `this.filters`（**运行时数组**，不写回配置）；
   - 每行加 "×" 删除按钮（从 `this.filters` splice）。
3. 用户增删的项**只活在 session 内**，刷新页面恢复成配置的那些。

---

### 4. **默认过滤值（Default filtering criteria via output variable）**

**Palantir 有什么**：
- 文档 L52-57："The second role is providing a means of **setting the default filtering criteria** 
  for the Filter List; setting a default value for this variable will result in those default 
  property filters being applied within the Filter List when this module is initially loaded."
- 即：`filterOutputVar` 不仅是输出变量，还可以在**变量定义**处设置初始值
  （如 `{property: "status", operator: "equals", value: "Active"}`），
  Filter List 加载时自动回填到对应过滤项的 `filter.value`。

**TIS 当前状态**：
- `filterOutputVar` 只作为**写出目标**（`onFilterChange` 把当前过滤条件写进去）；
- **没有**读取该变量的初始值并回填的逻辑；
- 页面首次打开时所有 `filter.value` 都是 `undefined`（见 L236：`value: undefined`）。

**差距**：
- 模块构建者无法设置「打开页面时默认筛选 XX」；
- 用户每次都要从空白状态开始填过滤条件。

**实现方向**：
1. 前端 `ngOnInit` 里，在 `this.filters = config.filters.map(...)` 之后：
   ```ts
   const defaultFilter = await this.variableService.getVariableValue(config.filterOutputVar);
   if (defaultFilter?.filters) {
     this.filters.forEach(row => {
       const match = defaultFilter.filters.find(f => f.property === row.property);
       if (match) {
         row.value = match.value;  // 回填默认值
       }
     });
   }
   ```
2. 后端 `ObjectSetFilterVariable` 在定义时支持 `defaultValue` 字段
   （已有变量类型支持此模式，FilterList 只需消费）。

---

### 5. **关联对象过滤（Filter on linked properties）**

**Palantir 有什么**：
- 文档 L72-91：可以对**关联对象的属性**设置过滤条件。
- 截图 `filter_list_linked_config.png` 显示配置界面：
  - 选择一个 link（如 "Person"），展开后可以：
    - 加 "Has link" 过滤器（过滤「是否有该关联」）；
    - 加关联对象的属性过滤器（如 Person.name）；
    - 设置展示模式（Inline / Grouped）。
- 截图 `filter_list_grouped.png` 显示 Grouped 模式：
  关联过滤器聚成一组，带对象图标 + 关联对象计数。

**TIS 当前状态**：
- `FilterItemConfig.property` 只能选**当前对象集**的直接属性；
- **完全没有** link 选择器、关联属性选择器、"Has link" 过滤器；
- 不支持 Inline / Grouped 展示模式。

**差距**：
- 无法实现「筛选有特定关联的对象」（如"找出分配给 Alice 的所有任务"）；
- 无法在 Filter List 里用关联对象的属性过滤（必须提前在对象集定义里 join）。

**实现方向**（**改动量最大**）：
1. 后端 `FilterItemConfig` 改为支持两种模式：
   ```java
   // 方案 A：单字段多态
   @FormField(type = FormFieldType.SELECTABLE, ordinal = 10)
   public String propertyOrLink;  // 下拉选项 = 直接属性 + link（加前缀区分）
   
   // 选了 link 时，再级联出关联对象的属性选择器：
   @FormField(type = FormFieldType.SELECTABLE, ordinal = 11)
   public String linkedProperty;
   
   // 或方案 B：嵌套多态（更清晰，但改动更大）
   @FormField(type = FormFieldType.SELECTABLE, ordinal = 10)
   public FilterTarget target;  // DirectProperty / LinkedProperty
   
   interface FilterTarget { String getPropertyPath(); }
   class DirectProperty implements FilterTarget { String property; }
   class LinkedProperty implements FilterTarget { String link; String property; Boolean hasLink; }
   ```
2. 前端增加 link 选择 UI、关联属性级联选择器；
3. 输出的谓词格式从 `{property, operator, value}` 扩展为
   `{property?, link?, linkedProperty?, operator, value}`。
4. 后端 `ObjectSetVariable` 施加谓词时，遇到 `link` 字段需转成 join 查询
   （或走 Neo4j 的关系遍历）。

---

## 二、重要增强功能（P1）

### 6. **分布图（Distribution Chart）**

**Palantir 有什么**：
- 文档 L38 提到 "distribution chart"（与 histogram 并列，可能是饼图 / 环图 / 堆叠柱）；
- 截图 `filter_list_configured.png` 里 "Flight status" 可能就是分布图（文档没放该字段的特写）。

**TIS 当前状态**：
- 没有独立的分布图组件。

**差距**：
- 分类属性（枚举 / 字符串）无法可视化比例；
- 用户不能点击扇区快速筛选。

**实现方向**：
1. 新建 `DistributionChartComponent`：
   - 后端聚合查询返回 `{value, count}[]`；
   - 前端用 ECharts 饼图渲染，点击扇区写 `{operator: 'equals', value: clickedValue}`。
2. 只对**分类属性**（字符串 / 枚举 / 布尔）开放该组件。

---

### 7. **时间线（Timeline）显示**

**Palantir 有什么**：
- 文档 L38 提到 "timeline displays"（日期属性的专用可视化，横轴时间、纵轴事件）。

**TIS 当前状态**：
- `DatePickerComponent` 只是普通日期选择器，没有时间线可视化。

**差距**：
- 无法看到时间维度上的对象分布（如"每天有多少笔订单"）；
- 无法通过拖选时间段快速筛选。

**实现方向**：
1. 新建 `TimelineComponent`：
   - 后端按天 / 周 / 月聚合，返回 `{date, count}[]`；
   - 前端用 ECharts 折线图 + dataZoom，拖选区间写 `{operator: 'between', value: [start, end]}`。
2. 只对 `DATE` / `TIMESTAMP` 属性开放。

---

### 8. **多日期选择器（Multi-date picker）**

**Palantir 有什么**：
- 文档 L38 提到 "multi-date pickers"（一次选多个日期，如"筛选 1/1、1/5、1/10 这三天"）。

**TIS 当前状态**：
- `DatePickerComponent` 只支持单个日期（`nzMode` 未设）。

**差距**：
- 无法筛选"多个离散日期"（必须用多个过滤项 + OR 逻辑，但 TIS 当前是 AND）。

**实现方向**：
1. `DatePickerComponent` 增加 `multi` 字段（布尔），
   前端用 `<nz-range-picker nzMode="date" [nzShowTime]="false" nzAllowClear nzMultiple>`；
2. 输出 `{operator: 'in', value: ['2024-01-01', '2024-01-05']}`。

---

### 9. **高级关键词搜索（Advanced keyword search）**

**Palantir 有什么**：
- 文档 L93-99："By switching the search type to **advanced syntax**, you can chain search operations 
  with each other and define the order of operations through brackets."
- 支持布尔逻辑：`NOT`、`AND`、`OR`、括号、引号（精确匹配）。
- 截图 `advanced-keyword-search.png` 显示搜索框右侧有 "Advanced syntax" 下拉切换。

**TIS 当前状态**：
- `KeywordComponent` 只有普通输入框（`<input nz-input>`），
  输出单个 `contains` 谓词。
- **不支持**布尔逻辑、括号、精确匹配。

**差距**：
- 无法实现「包含 A 但不包含 B」；
- 无法实现「包含 A 或 B」。

**实现方向**：
1. 前端 `KeywordComponent` 增加 "Advanced syntax" 切换开关；
2. 高级模式下，输入框旁显示语法提示（如 `AND`, `OR`, `NOT`, `()`）；
3. 提交时，前端**不解析**布尔表达式（太复杂），直接把原始字符串 + `mode: 'advanced'` 传给后端；
4. 后端在施加谓词时，用正则 / 全文检索引擎（如 Elasticsearch）执行高级查询。

---

### 10. **正则表达式搜索（Regex search）**

**Palantir 有什么**：
- 文档 L41-43："For string properties with **regex queries enabled** in Ontology Manager, 
  regex search is available as a filter option."
- 关键词输入框会显示"该属性支持正则"的提示。

**TIS 当前状态**：
- `KeywordComponent` 不支持正则模式（`operator` 只有 `contains` / `equals`）。

**差距**：
- 无法用正则匹配复杂模式（如"以 A 开头且包含数字"）。

**实现方向**：
1. `KeywordComponent` 增加 `searchMode` 字段（`NORMAL` / `REGEX`）；
2. 前端在输入框旁加 "🔍 Regex" 切换按钮（只对标记为 `regexEnabled` 的属性显示）；
3. Regex 模式下，输出 `{operator: 'regex', value: pattern}`；
4. 后端施加谓词时，走数据库的正则匹配（PostgreSQL `~`、MySQL `REGEXP`）。

---

## 三、高级特性（P2）

### 11. **过滤多种对象类型的联合集（Filter object sets of multiple object types）**

**Palantir 有什么**：
- 文档 L59-68：`objectSetVar` 可以是**多个对象类型的 union**，
  Filter List 会自动识别：
  - 公共属性（property ID 相同）→ 一个过滤项作用于所有类型；
  - 单一属性（只在某个类型里有）→ 该过滤项只作用于对应类型。
- 截图 `filter-list-multi.png` 显示配置界面里属性标签旁有对象类型图标。

**TIS 当前状态**：
- `objectSetVar` 假设是**单一对象类型**（候选项来源、属性选择器都只看一个 `objectType`）。
- 不支持 union 集合。

**差距**：
- 无法实现「同时筛选 Person 和 Organization，按共同的 name 属性」；
- 必须分成两个 Filter List 或提前在对象集定义里 join。

**实现方向**（**需本体层支持**）：
1. 后端 `ObjectSetVariable` 支持 `union` 模式（多个 `objectType`）；
2. `FilterItemConfig.property` 选择器返回**所有类型的属性并集**，
   标注每个属性所属的类型（前端显示图标）；
3. 施加谓词时，按对象类型分别执行（只对有该属性的类型生效）。

---

### 12. **Object Storage v1/v2 兼容性检查**

**Palantir 有什么**：
- 文档 L72-74：对 OSv1 有 "linked object filter limit of 100,000 objects" 警告，
  建议迁移到 OSv2。

**TIS 当前状态**：
- 没有存储版本概念（TIS 的本体层未区分 v1/v2）。

**差距**：
- 未来如果 TIS 引入存储版本，Filter List 不会自动提示性能限制。

**实现方向**：
- 暂不实施（TIS 本体层尚未分版本）；
- 若未来分版本，在 `FilterItemConfig` 选 link 时检查版本并显示警告。

---

### 13. **过滤值提取（Filter value extraction）**

**Palantir 有什么**：
- 文档 L57："Filtered values may also be **extracted** from the widget's filter output field 
  by setting up filter value extraction on the object set filter variable, 
  allowing them to be used elsewhere in your module."
- 即：把 `filterOutputVar` 里某个过滤项的 `value` 单独提取成一个变量
  （如 `statusFilter.value` → 独立的 `selectedStatus` 变量），
  供其它组件直接读（不必再解析整个 filter 对象）。

**TIS 当前状态**：
- `filterOutputVar` 只能作为整体被 `ObjectSetVariable` 引用；
- **没有**提取单个过滤项值的机制。

**差距**：
- 无法把用户选的过滤值直接传给非对象集组件（如标题、文本框）；
- 必须通过对象集查询绕路。

**实现方向**：
1. `ObjectSetFilterVariable` 增加 `extractedValues` 字段（Map<property, variableName>）；
2. 前端在变量定义处配置「把 property X 的值提取成变量 Y」；
3. `VariableService` 在 `filterOutputVar` 变化时，自动更新提取出的变量。

---

### 14. **属性精确匹配支持检测（Exact match support tooltip）**

**Palantir 有什么**：
- 文档 L40-41："Properties without exact match support show the **Keyword** search option, 
  while other options appear **disabled with a tooltip** explaining why they are unavailable."
- 配置面板会自动检测属性是否支持精确匹配（索引类型），
  不支持的属性禁用 Single-select / Multi-select 选项并显示原因。

**TIS 当前状态**：
- 组件选择器（`FilterItemConfig.component`）对所有属性都开放全部 4 种组件；
- **没有**检测属性是否有索引 / 是否支持精确匹配。

**差距**：
- 用户可能给一个全文检索字段配 Single-select，实际查不到结果；
- 没有提示"为什么某个组件不可用"。

**实现方向**：
1. 本体管理器（Ontology Manager）为每个属性标注 `exactMatchSupported`（布尔）；
2. 前端组件选择器读取该标注，禁用不支持的选项并加 tooltip；
3. Keyword 永远可用（Palantir 的兜底选项）。

---

## 四、改动优先级建议

| 优先级 | 项目 | 工作量 | 业务价值 | 备注 |
|---|---|---|---|---|
| **P0-1** | 直方图组件 | 中 | 高 | 数值属性可视化，通用需求 |
| **P0-2** | Pills 布局 | 小 | 高 | 紧凑展示，提升空间利用率 |
| **P0-3** | 用户动态增删过滤项 | 中 | 高 | 灵活性提升，用户体验关键 |
| **P0-4** | 默认过滤值 | 小 | 中 | 减少用户重复操作 |
| **P0-5** | 关联对象过滤 | **大** | **高** | **最复杂**，需本体层 + 查询层配合 |
| **P1-6** | 分布图 | 中 | 中 | 补充可视化手段 |
| **P1-7** | 时间线 | 中 | 中 | 时间维度分析 |
| **P1-8** | 多日期选择器 | 小 | 低 | 边缘场景 |
| **P1-9** | 高级关键词搜索 | 大 | 中 | 需后端布尔查询引擎 |
| **P1-10** | 正则搜索 | 小 | 低 | 高级用户需求 |
| **P2-11** | 多对象类型联合集 | 大 | 低 | 需本体层支持 union |
| **P2-12** | 存储版本检查 | 小 | 低 | TIS 未分版本，暂不需要 |
| **P2-13** | 过滤值提取 | 中 | 低 | 边缘场景，可用变量引用绕路 |
| **P2-14** | 精确匹配检测 | 小 | 低 | 体验优化 |

**建议实施顺序**：
1. **第一阶段（2 周）**：P0-2（Pills 布局）+ P0-3（用户增删）+ P0-4（默认值）→ 快速提升易用性；
2. **第二阶段（3 周）**：P0-1（直方图）+ P1-6（分布图）→ 补齐可视化短板；
3. **第三阶段（4-6 周）**：P0-5（关联对象过滤）→ 最复杂、需跨层协作；
4. **后续按需**：P1-7/9/10、P2-13/14 按用户反馈排期。

---

## 五、技术依赖与风险

### 依赖项

| 功能 | 前端依赖 | 后端依赖 | 本体层依赖 |
|---|---|---|---|
| 直方图 / 分布图 / 时间线 | ECharts | 聚合查询（GROUP BY + COUNT） | 属性类型标注（数值 / 分类 / 时间） |
| 关联对象过滤 | 级联选择器 | JOIN 查询 或 Neo4j 遍历 | Link 元数据（关系类型 / 目标对象类型） |
| 高级关键词搜索 | 布尔表达式 UI | 全文检索引擎（Elasticsearch / Solr） | 属性全文索引配置 |
| 多对象类型联合集 | 对象类型图标 | Union 查询 | `ObjectSetVariable` 支持多 objectType |

### 风险

1. **关联对象过滤（P0-5）**：
   - 改动面最大（`FilterItemConfig` 多态化 + 查询层 JOIN）；
   - Neo4j 与关系数据库的 JOIN 语义不同，需统一抽象；
   - 性能风险：关联对象多层嵌套时查询可能很慢。

2. **高级关键词搜索（P1-9）**：
   - 需引入全文检索引擎（当前 TIS 是否已有？）；
   - 布尔表达式解析容易出错（建议后端用现成库，如 Lucene QueryParser）。

3. **Pills 布局（P0-2）**：
   - Popover 内嵌套表单控件，Angular 的 form 上下文可能传递不正确
     （需测试 `ngModel` 在 popover 里是否生效）。

---

## 六、验收标准

每项功能的验收标准（示例）：

### P0-2：Pills 布局
- [ ] 配置 `layout: 'PILLS'` 后，过滤项横向排列成胶囊；
- [ ] 点击 pill 弹出 popover，内含原有的输入控件；
- [ ] Popover 关闭后，pill 上显示 "属性名: 当前值"；
- [ ] 只显示有值的过滤项，空项不渲染 pill；
- [ ] "+ Add filter" 按钮在 pills 列表末尾，点击可临时增加过滤项。

### P0-5：关联对象过滤
- [ ] 配置面板 "Add filter" 下拉里，link 与普通属性分组显示；
- [ ] 选中 link 后，展开二级选择器（关联对象的属性）；
- [ ] 可以加 "Has link" 过滤器（复选框，"是否有该关联"）；
- [ ] 写出的谓词包含 `{link, linkedProperty, operator, value}`；
- [ ] 后端查询正确执行 JOIN（验证：SQL 日志 / Neo4j Cypher）；
- [ ] Grouped 模式下，关联过滤器带对象图标 + 计数。

（其余功能类似，不一一展开）

---

## 七、补充资料

- **Palantir 文档原文**：`/opt/misc/palantir-study/foundry/workshop/detail/04-widgets/widgets-filter-list.md`
- **TIS 当前实现**：
  - 后端：`FilterListWidget.java` + `FilterItemConfig.java` + 4 个 component 类
  - 前端：`filter-list.component.ts` + `widget-config.model.ts` (归一化器)
- **截图文件**（按功能对应）：
  - 垂直布局：`filter_list_configured.png`
  - Pills 布局：`filter_list_configured_pills.png`
  - 关联对象配置：`filter_list_linked_config.png`
  - Grouped 展示：`filter_list_grouped.png`
  - 高级搜索：`advanced-keyword-search.png`
  - 多对象类型：`filter-list-multi.png`
  - 空态：`filter_list_empty_state.png`

---

## 八、总结

TIS 当前的 Filter List 实现了 Palantir 的**核心过滤框架**（关键词、单选、多选、日期选择 + 候选项下发），
但缺失了 **5 项 P0 功能**（直方图、Pills 布局、用户增删、默认值、关联对象过滤），
这些是 Palantir Filter List 区别于「普通表单」的关键特性。

建议优先实施 **P0-2/3/4**（Pills + 用户增删 + 默认值），快速提升易用性（约 2 周）；
然后补齐 **P0-1**（直方图），解决数值属性无可视化的短板（约 2 周）；
最后攻坚 **P0-5**（关联对象过滤），这是最复杂但业务价值最高的功能（约 4-6 周）。

P1/P2 功能可按用户反馈和实际需求排期，不影响 Filter List 的基本可用性。