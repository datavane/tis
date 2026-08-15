# TIS 插件表单布局与模式选择器 UX 优化备忘

> 时间：2026-08-12 ~ 2026-08-13
> 涉及前端工程：`/Users/mozhenghua/j2ee_solution/project/tis-console`（Angular 17 + ng-zorro-antd 17.4.1）
> 核心改动文件：`src/common/plugin/item-prop-val.component.ts`、`src/common/common.module.ts`

---

## 1. 背景

TIS 插件配置表单（以 Oracle 数据源 `OracleDataSourceFactory` 为例）由后端 Descriptor JSON 驱动，
经 `PluginsComponent` → `ItemPropValComponent`（`<item-prop-val>`）递归渲染。存在两个 UX 问题：

1. **label 区凌乱**：help "?" 图标只在部分字段出现（实例ID/连接方式等有，端口/用户名/密码没有），
   图标位于 label 文字与冒号之间、按需渲染，导致行结构不一致、蓝色图标呈锯齿状排列；
   子表单（formLevel=2）label 列仅 5/24 栅格，长 label（如"去除保留Schema"）被 `overflow:hidden` 截断。
2. **模式切换与普通枚举无法区分**：`describable=true` 的字段（连接方式 SID/ServiceName、
   包含授权、所在时区 default/customize）是"模式切换"——选项决定下方渲染哪个子表单，
   却与普通枚举（如时区编码）使用完全相同的下拉选择框，用户第一眼难以分辨。

## 2. 目标

- label 区对齐统一：help 图标固定槽位，无 help 的字段留白占位，视觉整齐划一。
- 模式切换字段与普通枚举选择一眼可辨：**所有** describable 字段统一使用 `nz-segmented`
  分段选择器（不分选项多少、不论是否可扩展），可扩展字段在分段选择器右侧提供扩展按钮。
- 不改动后端（describable 标识已在 JSON 中），不改变 `changePlugin` 的切换流程语义。

## 3. 修改内容

### 3.1 label 区对齐（item-prop-val.component.ts 模板 label 段）

- label 列宽：`[nzSpan]="horizontal ? effectiveLabelSpan : null"`，新增 getter
  `effectiveLabelSpan = formLevel > 1 ? 7 : labelSpan` —— 子表单 label 加宽防截断。
- 控件列宽：配套 `effectiveValSpan = formLevel > 1 ? 16 : formControlSpan`（7+16=23，总宽不变）。
- label 文字包一层 `<span class="label-text" [title]="_pp.label">`，极端截断时有 tooltip 兜底。
- help 图标**始终渲染**，无 help 内容时加 `field-help-placeholder` 类
  （`visibility: hidden; cursor: default;`）占住 20px 槽位。

### 3.2 模式切换统一为分段选择器（describable 分支）

- **删除**早期的双分支方案（"选项少用 segmented / 选项多或可扩展用下拉"），
  移除 `useSegmentedForDesc` getter、`descDropdownTpl` 与 `renderExtraPluginTemplate` 模板、
  `.mode-select` 相关样式。describable 字段**一律**渲染 `nz-segmented`。
- 结构为 `.mode-select-bar`（flex + gap 4px + wrap）容器：
  - 左侧 `<nz-segmented>`：`[nzOptions]="segmentedOpts"`（label/displayName + icon/endType 透传）；
  - 右侧仅 `extensible=true` 时渲染 `nz-button-group`：
    - **添加**（dashed，plus 图标）：`addNewPlugin()` 打开插件管理抽屉；
    - **刷新**（dashed，reload 图标）：`freshDescPropDescriptors()` 重新拉取可选项；
    - 均带 `nz-tooltip`，`data-testid` 保留（`_pp.key+'_add_new_plugin'` / `'_fresh_descs'` / `'_plugin_impl_select'`）。
- 样式：
  - `.mode-select-segmented { max-width: 100%; overflow-x: auto; }` —— 选项特别多时横向滚动，
    不撑破表单（不能用 flex-wrap：会破坏绝对定位的选中滑块 `.ant-segmented-thumb`）。
  - `.mode-select-subform`：子表单左边线 2px 主色 + 淡主色背景，与模式选择器视觉联动
    （主题无 CSS 变量，直接硬编码 `#1890ff`）。
- `common.module.ts`：导入 `NzSegmentedModule`。

### 3.3 nz-segmented 索引桥接（关键 bug 修复）

**问题**：ng-zorro 17.x 的 `nz-segmented` 其 CVA 模型是**选项索引**而非选项 value：

- `writeValue(value: number | null)` 只接受 `number > -1`，绑定 impl 字符串被静默忽略；
- 点击后 `(ngModelChange)` 发出的是**索引**。

最初直接把 `[ngModel]` 绑到 `_pp.descVal.impl`、`(ngModelChange)` 接到 `changePlugin(_pp, $event)`，
导致点击后 `changePlugin` 收到索引：`descriptors.get(0/1)` 未命中（或索引 0 触发 `!impl` 提前返回），
`dspt=null` → `propVals` 为空 → 子表单 `*ngIf` 不成立 → **子表单完全空白**。

**修复**：

- 模板：`[ngModel]="segmentedSelectedIndex"`、`(ngModelChange)="changePluginByIndex(_pp,$event)"`。
- `segmentedSelectedIndex` getter：impl → 索引
  （`iconDescSelectOpts.findIndex(o => o.val === dv.impl)`）。
- `changePluginByIndex()`：索引 → impl 字符串 → 走原有 `changePlugin()` 流程
  （`clearPropVals()` + `newDesc` 逻辑不变）。
- **初始高亮修复**：`@ViewChild(NzSegmentedComponent)` setter 中 `setTimeout` 补写一次索引。
  原因：首次 `writeValue` 发生在 segmented 内部选项（`listOfOptions` 视图查询）就绪之前，
  会被静默丢弃（构造默认 `selectedIndex=0`），不补写时编辑场景下 impl 不在第一项会高亮错误。

## 4. 技术要点（后续复用）

| 事项 | 结论 |
|---|---|
| nz-segmented 17.x 模型 | **索引**（number），不是选项 value；17.4.1 无 value 模式、无 `nzCompareWith` |
| 选项自定义模板 | `nzLabelTemplate` 需配合 `useTemplate:true` 且与 `icon` 字段并存会重复渲染图标 → 用原生 `icon` 透传 |
| 初始值生效时机 | 首次 `writeValue` 早于 `listOfOptions` 就绪会被忽略，需视图就绪后补写 |
| 选项溢出 | `max-width:100%; overflow-x:auto`，勿用 flex-wrap（滑块定位会坏） |
| 主题变量 | theme.less 是 LESS 变量，无 `:root` CSS 变量，组件内联样式直接写 `#1890ff` |
| 验证命令 | `npx tsc -p tsconfig.app.json --noEmit` + `npx ng build`（eslint 配置损坏为既有问题，跳过） |

## 5. 验证

- `tsc --noEmit` 通过；AOT `ng build` 通过（Build at 2026-08-13T05:49:46Z）。
- 浏览器人工验证点：Oracle 数据源表单"连接方式/包含授权/所在时区"分段渲染与切换子表单正常；
  extensible 字段（如 transformer 规则选择）右侧"添加/刷新"按钮功能正常；
  选项很多的插件选择字段横向滚动不撑破布局；编辑已保存配置（模式非第一项）初始高亮正确。

## 6. 遗留事项

- `src/runtime/incr.build.step4.running.component.ts:216` 图表模式切换存在**同样的索引 bug**：
  `chartMode` 绑定字符串 `'iud'/'throughput'`，实际收到索引 `0/1`，
  `chartMode === 'throughput'` 永不成立，"In/Out 吞吐"视图切不过去。待修复。
- 紧凑垂直布局（`compactVerticalLayout=true`，多步骤表单使用）的 label/help 渲染逻辑本次未动。

## 7. 后续修复（2026-08-13，本体属性表单场景暴露的两个问题）

背景：本体对象属性（`OntologyProperty`）表单中"语义角色"（roleType）与"聚合函数"（aggregation）
两个 describable 字段暴露了分段选择器方案的两个遗留问题。

### 7.1 shortComment 说明标签丢失

`Descriptor.iconOption`（`tis.plugin.ts:488`）本就带 `comment: {color, content}`
（来源 `extractProps.shortComment`，如"用于分组与筛选的分类型属性"）。旧版 `nz-select` 下拉靠
`enum-icon-select.component.ts` 的 `commentTagTpl` 把它渲染成选项最右侧的 `nz-tag`；
但 `segmentedOpts` getter 把 `iconDescSelectOpts` 映射成 `nz-segmented` 的 `{label,value,icon,disabled}`
时把 `comment` 丢了，`nz-segmented` 原生也没有渲染它的槽位。

修复：`segmentedOpts` 补上 `comment` 字段与 `useTemplate:true`；新增 `nz-segmented`
的 `nzLabelTemplate`（`segmentedLabelTpl`），在 label 文字后加一个 `info-circle` 图标，
挂 `nz-tooltip` 显示 `comment.content`（`nzTooltipColor` 用 `comment.color`）。
一处改动同时覆盖"语义角色"和"聚合函数"（两者走同一套 describable 渲染路径）。
点击图标 `stopPropagation`，避免误触发选项切换。

### 7.2 选项过多时的横向滚动条

`.mode-select-segmented { overflow-x: auto }` 选项多时出现横向滚动条，需要拖动才能看全部选项。
不能用 `flex-wrap` 换行——`nz-segmented` 的选中滑块（`.ant-segmented-thumb`）靠
`offsetLeft`/`clientWidth` 绝对定位做动画，换行后第二行滑块位置会算错。

修复：滚动容器从 `nz-segmented` 自身移到外层新增的 `.mode-select-scroll-wrap`
（`overflow-x:auto` + `scrollbar-width:none` + `::-webkit-scrollbar{display:none}` 隐藏原生滚动条），
两侧加"‹ ›"箭头按钮（`scrollSegmented(-1|1)`，`el.scrollBy` 平滑滚动 60% 容器宽度）。
箭头按钮仅在内容确实溢出时显示（`segmentedHasOverflow`，靠 `scrollWidth > clientWidth` 判断），
且按滚动位置动态置灰左右箭头（`canScrollSegmentedLeft/Right`）。
滚动状态在 `ViewChild(segmentedScrollWrap)` setter、`(scroll)` 事件、`window:resize` 三处刷新。

### 7.3 验证

- `npx tsc -p tsconfig.app.json --noEmit` 通过；`npx ng build` 通过（exit 0，仅有与本次改动无关的既有 CSS/CommonJS 警告）。
- 待补充：浏览器人工验证 roleType/aggregation 分段选择器的 info 图标 tooltip 显示 shortComment；
  选项很多的字段两侧箭头出现、可点击滚动、滚动到底/顶时对应箭头置灰。

### 7.4 回归：页面卡死（用户实测发现，7.2 引入的死循环）

上面 7.2 的滚动状态刷新逻辑上线后，用户反馈页面一打开就卡死；去掉 L424-433
（`mode-select-scroll-wrap` 容器 + `nz-segmented` 本身）卡死消失，定位到问题出在这段代码。

根因是两处叠加：

1. `segmentedOpts` getter 每次求值都用 `.map()` 生成全新的数组和对象（无缓存），
   而它绑定在 `[nzOptions]="segmentedOpts"` 上。`ItemPropValComponent` 是默认（非 OnPush）
   变更检测策略，只要发生一次 `detectChanges()`，模板重新求值就会拿到一个新的数组引用，
   触发 `nz-segmented` 内部 `ngOnChanges({nzOptions})` 重新 normalize 选项、重新布局选中滑块。
2. `refreshSegmentedScrollState()` 无条件调用 `this.cdr.detectChanges()`，而它被四个入口触发：
   `modeSegmented`/`segmentedScrollWrapRef` 两个 `@ViewChild` setter 的 `setTimeout`、
   `.mode-select-scroll-wrap` 的 `(scroll)` 事件、`window:resize`。

两者叠加形成死循环：`detectChanges()` → `segmentedOpts` 返回新引用 →
`nz-segmented` 重新布局/滑块动画 → 内容尺寸/滚动位置发生细微变化 →
`.mode-select-scroll-wrap` 触发原生 `scroll` 事件 → `onSegmentedScroll()` →
`refreshSegmentedScrollState()` → 又一次 `detectChanges()`……循环不会自然停止，
主线程被持续占用，页面表现为卡死。递归渲染的子表单（roleType 下还嵌 aggregation 等
describable 字段）让多个 `ItemPropValComponent` 实例同时跑这个循环，卡死更明显。

修复（`item-prop-val.component.ts`）：

- `segmentedOpts` 增加缓存：只有当 `dv.iconDescSelectOpts`（该数组本身在 `DescribleVal` 里
  是稳定引用）变化时才重新 `.map()`，否则直接返回上次缓存的数组，保证 `[nzOptions]` 绑定
  在选项集合不变时永远拿到同一个引用，不再触发 `nz-segmented` 的 `ngOnChanges`。
- `refreshSegmentedScrollState()` 增加脏检查：先算出 `hasOverflow`/`canLeft`/`canRight`，
  只有和当前值不同时才赋值并调用 `detectChanges()`；四个触发入口重复调用时天然形成空转
  而不产生副作用，切断了循环的continuation。

验证：`npx tsc -p tsconfig.json --noEmit` 通过；`npx ng build` 通过（exit 0）。
浏览器人工验证仍待补充（这正是上次遗漏、导致回归的环节）。

## 8. 放弃 nz-segmented，改为 radio/dropdown 双模式（2026-08-13）

7.4 的卡死修复上线后用户浏览器实测效果仍不理想，遂决定放弃 `nz-segmented` 方案。
参考 airbyte 连接器规范文档（Connector Specification Reference）中 `oneOf` 字段的
`display_type: dropdown|radio` 展示方式：选项少时用纵向 radio、标题下方常驻显示描述文字；
选项多时用下拉框。TIS 的 describable 字段（模式切换）与 airbyte 的 `oneOf` 模式选择语义一致，
借鉴其双模式方案同时天然解决 shortComment 常驻展示问题，并彻底绕开 nz-segmented
索引桥接、滚动状态刷新、动画 `detectChanges` 等一整套复杂度和卡死风险。

### 8.1 显示策略

`_pp.descVal.iconDescSelectOpts.length < 3` → `nz-radio-group`（纵向排列，每个选项
标题下方常驻一行 `shortComment` 描述，不需要 hover/点击）；`>= 3` → `enum-icon-select`
（已有组件，内部是 `nz-select` + `nzCustomContent`，本就支持 `comment` 展示）。

### 8.2 具体改动（`item-prop-val.component.ts`）

- 整块替换 describable 分支（`*ngSwitchCase="false"`）的模板：删除 `nz-segmented`、
  两侧滚动箭头、`.mode-select-scroll-wrap`、`segmentedLabelTpl`；改为 `[ngSwitch]`
  两个分支渲染 `nz-radio-group`（复用既有 `flat-radio-option/-content/-icon/-label/-desc`
  样式类，与 L241-257 的 `flat_single_choice` 枚举单选完全同款视觉）或 `enum-icon-select`。
- radio/dropdown 都直接绑定 `_pp.descVal.impl` 字符串、`(ngModelChange)`/`(valueChange)`
  直接调用既有 `changePlugin(_pp, $event)`——不再需要 `changePluginByIndex` 索引桥接层，
  这层桥接本身正是 nz-segmented 特有的复杂度，随方案替换一并删除。
- 删除全部 nz-segmented 相关 TS 成员：`segmentedOpts`（含缓存字段）、
  `segmentedSelectedIndex`、`changePluginByIndex`、`modeSegmented`（`@ViewChild`）、
  `segmentedScrollWrapRef`（`@ViewChild`）、`_segmentedScrollWrapEl`、
  `refreshSegmentedScrollState`、`onSegmentedScroll`、`onWindowResize`
  （`@HostListener('window:resize')`）、`scrollSegmented`；以及未再使用的
  `NzSegmentedComponent`/`ElementRef`/`HostListener`/`ViewChild`/`EnumIconOption` import。
- 删除对应 CSS：`.mode-select-scroll-wrap`、`.mode-select-segmented`、
  `.segmented-scroll-btn`、`.segmented-label-text`、`.segmented-comment-icon`；
  `.mode-select-bar` 的 `align-items` 从 `center` 改为 `flex-start`
  （radio 内容多行、add/refresh 按钮单行，顶部对齐才不会错位）；新增
  `.mode-select-radio-group { flex:1 1 auto; min-width:0 }` 让 radio 组占满可用宽度。
- 添加/刷新扩展按钮（`addNewPlugin`/`freshDescPropDescriptors`）位置不变——仍在
  `.mode-select-bar` 内、模式选择控件右侧，逻辑和 `data-testid` 均未改动。

### 8.3 与 airbyte 的差异（本次未做）

- airbyte 的 description 支持内嵌 HTML（`<i>Recommended</i>`、`<a href="...">`）；
  grep 了插件端所有 `shortComment()` 实现，目前全是纯中文文字，没有 HTML/链接场景，
  故未对 `comment.content` 做 `[innerHTML]` 处理，需要时再加。
- `enum-icon-select.component.ts` 内部的 `commentTagTpl` 目前仍是"标题右侧一个 `nz-tag`"
  的横向布局，与 airbyte"标题下方另起一行完整描述"的纵向布局不同；这属于选项 >= 3
  的下拉分支的进一步视觉打磨，本次未动该组件，作为后续可选项。

### 8.3.1 dropdown 分支尝试贴近 airbyte 样式，已回退（2026-08-13）

曾在选项 >= 3 的 dropdown 分支外面加原生 `<fieldset><legend>` 包裹，legend 展示当前
选中项的 `shortComment`（`selectedModeComment(_pp)`），`enum-icon-select` 放在描边框内
靠右对齐、固定宽度 260px。

用户浏览器实测后反馈效果不理想，要求去掉，已完整回退：删除 `<fieldset>/<legend>`
模板、`selectedModeComment()` 方法、`.mode-select-fieldset`/`.mode-select-legend`/
`.mode-select-enum-select` 三块 CSS，dropdown 分支恢复为 8.3 节的裸 `enum-icon-select`
（不带任何包裹元素）。radio 分支（选项 < 3）未受影响。

验证：`npx tsc -p tsconfig.json --noEmit` 通过；`npx ng build` 通过（exit 0）。

### 8.4 验证

- `npx tsc -p tsconfig.json --noEmit` 通过；`npx ng build` 通过（exit 0，仅既有 CSS/CommonJS 警告）。
- 浏览器人工验证仍待补充：roleType（3 个选项，应走 dropdown）、aggregation（10 个选项，
  应走 dropdown）建议在插件端临时调整选项数验证 radio 分支（<3 时）的纵向布局与描述展示；
  确认页面不再卡死、切换模式后子表单正常渲染、编辑已保存配置时初始选中正确。
