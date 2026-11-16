# 任务提示词：为 TIS Ontology Workshop 补 Jest 单元测试（有选择地覆盖）

> 这份文档是一份**可直接投喂给另一个模型执行的提示词**。执行者不需要读设计文档，照着做即可。

---

## 0. 你的任务

在工程 `/Users/mozhenghua/j2ee_solution/project/tis-console` 中，为 `src/workshop/`（TIS Ontology Workshop 前端）**有选择地**补 Jest 单元测试。

**关键词是「有选择地」**：`src/workshop/` 有 112 个 `.ts` 文件、约 3.19 万行，全量覆盖既不可能也没价值。本任务只覆盖**静默失效（silent failure）风险最高**的**投影 / 归一化层**——那些「编译通过、运行不报错、只是不生效」的函数。

不要写 DOM 快照测试，不要追求行覆盖率数字，不要把整个目录塞进覆盖率统计。

目标产出：约 8~12 个 spec 文件、合计 150~250 个用例，全部落在 `src/workshop/` 源码旁边（`<源文件名>.spec.ts`）。

---

## 1. 为什么测这些（决定取舍的唯一标准）

TIS Workshop 的前后端通过 Describable 插件机制通信，配置在两侧流转时形状会变：`{impl, vals}` 信封 ↔ 扁平值、大写下发 ↔ 小写消费、`_primaryVal` ↔ 值本体。**写错了不会有任何报错**，只会表现为「配置面板里改了但画布没变」「保存后重开配置丢了」「表格一片空白」。

所以取舍标准只有一条：

> **这个函数写错了，会在多久之后、以什么形式暴露？**
>
> - 立刻报错 / 类型不通过 → 不值得测（tsc 和 AOT 已经管了）
> - 静默、值变 `undefined`、条件恒真/恒假、只在保存后才丢、只在重开面板后才丢 → **必须测**

不要测「显然正确的 getter」，不要测纯渲染模板，不要测 Angular 框架行为。

---

## 2. 环境事实（已经核实，直接采信，不要重新验证）

### 2.1 测试框架现状

- Jest 29 + `jest-preset-angular` 14，`setupFilesAfterEnv: ['<rootDir>/setup-jest.ts']`，`testEnvironment: 'jsdom'`。
- `testMatch: ['<rootDir>/src/**/*.spec.ts']` —— spec 放在源码旁边就会被拾取，不用改配置。
- `modulePaths: ['<rootDir>']` —— 工程里有 `import ... from 'src/common/...'` 这种根锚定导入，Jest 已支持。
- ts-jest 的 **diagnostics 是开的**：spec 里有 TypeScript 类型错误会直接让该 suite 失败，这不是配置问题，是门禁，修代码不要关它。
- `TextEncoder` / `TextDecoder` 已由 `setupZoneTestEnv()` 提供 polyfill，不用自己补。
- 既有 5 个 spec（4 个在 `src/common/`，1 个 `src/offline/workflow.schedule.config.component.spec.ts`）全绿。**不许为了让自己变绿去改它们的断言。**

### 2.2 覆盖率配置的两个陷阱（必读，踩了就红）

`jest.config.js` 的 `collectCoverageFrom` 目前只有 `src/common/**/*.ts`。

**❌ 绝对不要**把 `src/workshop/**/*.ts` 加进 `jest.config.js` 的 `collectCoverageFrom`。
原因：`jest.config.js` 里有一个 `global` 覆盖率门禁（statements 20 / branches 20 / functions 14 / lines 20）。分母里塞进三万行几乎未被覆盖的 workshop 代码，会让 global 立刻跌破 20%，`npm run test:ci` 变红。

**✅ 正确做法**：只在 `jest.ratchet.config.js` 里扩面。该文件没有 `global` 键，只按文件路径逐个校验，加多少文件都不影响别处。

```js
// jest.ratchet.config.js —— 形如：
module.exports = {
  ...base,
  coverageDirectory: 'coverage-ratchet',
  // 只扩这一处：把要棘轮的文件加进采集范围
  collectCoverageFrom: [
    'src/common/**/*.ts',
    '!src/common/**/*.d.ts',
    'src/workshop/models/*.model.ts',
    'src/workshop/models/widgets/*.config.ts',
    'src/workshop/models/layout-hydration.ts',
    'src/workshop/services/api/workshop-variable-api.service.ts',
    // ...按实际新建的 spec 对应的**被测文件**逐条列，不要写 src/workshop/**
  ],
  coverageThreshold: {
    // 既有三条阈值原样保留，不许下调
    './src/common/plugin/item-prop-val.component.ts': { statements: 76, branches: 68 },
    './src/common/plugins.component.ts': { statements: 58, branches: 50 },
    './src/common/tis.plugin.ts': { statements: 50, branches: 40 },
    // 新增：棘轮下限 = 你**实测值向下取整**
    './src/workshop/models/widget-config.model.ts': { statements: NN, branches: NN },
    // ...
  }
};
```

规则：**路径键以 `./` 开头**；**阈值必须等于该文件在你提交时的实测覆盖率向下取整**，不要写一个没测过的目标值。实测值从 `npm run test:ci` 的 `text-summary` / `text` 报告里读。

### 2.3 `src/workshop/models/` 是纯净的（这是本任务能低成本完成的前提）

已核实：`src/workshop/models/` 下**没有任何** `from '@angular/...'` 的值导入，**没有任何** `@Injectable` / `@Component` / `@Directive` 装饰器。

因此 **Tier 1 的所有测试都不需要 TestBed**，直接 `import { xxx } from './xxx.model';` 即可，跑得飞快。不要在纯函数测试里引入 TestBed——那会把单文件耗时从几十毫秒拉到几秒。

例外：`models/widgets/*.config.ts` 只 `import type` Angular 或什么都不 import，同样纯净。

实测佐证（本次已跑过并删除探针）：一个 spec 同时 `import` 下列全部符号并**不配 TestBed** 就绿了——
`widget-config` / `entity-form` / `section` / `layout-delta` / `object-set-filter` / `definition-config` /
`widget` / `click-action` / `header` / `page` / `object-list.config` / `chart-xy.config` / `index`（整个桶）/
`../components/widgets/pivot-table/pivot-transform`。整文件耗时约 2.5s（其中绝大部分是 jest 冷启动）。

`src/workshop/components/widgets/post-widget-data.ts` 不是纯函数（`postWidgetData` 要 `descriptor` + `tisService` + `moduleApi`），
但它只有 5 行且有明确契约（投影后调用、**不吞异常也不看 `success`**），要用一个三字段的 stub 测也是低成本的，
可选做，不列进上面清单。

### 2.4 组件测试的已知坑（只有 Tier 3 会踩）

`src/workshop/components/widgets/base-widget.component.ts` 用 `inject()` 做字段初始化（`cdr` / `editModeService` / `widgetDataStatus` / `renderCache`）。

- `new FilterListComponent(...)` **会抛错**，`TestBed.runInInjectionContext` 也不行。
- 唯一实测可行的写法：

```ts
TestBed.configureTestingModule({
  declarations: [FilterListComponent],
  imports: [FormsModule],
  schemas: [NO_ERRORS_SCHEMA],      // 子组件模板不展开
  providers: [
    { provide: VariableService, useValue: variableStub },
    { provide: WorkshopModuleApiService, useValue: moduleApiStub },
    { provide: TISService, useValue: tisStub },
    { provide: NzNotificationService, useValue: notificationStub }
  ]
});
const fixture = TestBed.createComponent(FilterListComponent);
const cmp = fixture.componentInstance;   // 不要调 fixture.detectChanges()，除非断言 DOM
```

Tier 3 只建议覆盖 `filter-list` 的**纯状态迁移方法**（`onClearFilterValue`、`renderEpoch`、`trackByProperty`），不测 DOM 内容。

---

## 3. 分层与目标清单

按「静默失效风险 × 覆盖成本」排序。**Tier 1 是主战场，至少完成 Tier 1 的 80% 条目；Tier 2 挑 6~8 个；Tier 3 至多 1 个组件文件（`filter-list`），另有一个被误归类到 Tier 3 的纯函数 `pivot-transform.ts` 按 Tier 1 标准测。**

### Tier 1 —— 归一化 / 投影纯函数（必须做）

每条目标都写了**必须钉死的不变量**。这些不变量来自真实踩过的坑，不是猜测——照着写断言。

#### 1.1 `src/workshop/models/widget-config.model.ts`（最高优先级）

| 函数 | 行 | 必须钉死的不变量 |
|---|---|---|
| `payloadOf` | 471 | 三种入参形态都要成立：① `{impl, vals: {...}}` → 返回 `vals`；② 表单回传的 `{descVal: {vals: {...}}}` → 返回内层 `vals`；③ 已是扁平对象 → **原样返回**（不是 `{}`）。非对象（`null`/`undefined`/字符串/数字）→ `{}`。注意 ② 必须优先于 ③：`{descVal:{...}}` 本身是个对象，不加分支会被当成扁平值原样返回。 |
| `isOffImpl` | 490 | `'com.x.LegendOff'` → `true`；`'LegendOff'`（无包名）→ `true`；`'com.x.LegendConfig'` → `false`；`''`/`null`/`undefined` → `false`。**反向哨兵**：断言不能用 `resolveImplKind` 实现——`LegendOff` 剥 `Config` 后缀得到的是 `legend-off` 而非 `legend`，这是两个不同的判据。 |
| `toOptionalName` | 503 | `''` → `undefined`（空串是真值，这是最大陷阱）；`'   '` → `undefined`；`' abc '` → `'abc'`；数字 `0` → `'0'`（不能被 falsy 判断吞掉）；`null`/`undefined` → `undefined`。 |
| `toOptionalNumber` | 514 | `''`/`null`/`undefined` → `undefined`（表单清空会落 `null`，若返回 `null`，`[nzWidth]="null"` 会渲染成字面量 `"null"`）；`'12'` → `12`（字符串要能解析）；`'abc'` → `undefined`；`NaN`/`Infinity` → `undefined`；`0` → `0`。 |
| `toOptionalBool` | 521 | 字符串 `'false'` → `false`（不是 `true`）；`''` → `undefined`；`'0'` → `false`；`1` → `true`。 |
| `toTokenOf` | 540 | 不在 allowed 集合内 → `undefined`；命中时**原样返回、不做任何大小写或下划线变换**（`'default_'` 必须还是 `'default_'`，剥下划线是读侧 `antdToken` 的事）；带首尾空白要能 trim 后命中。 |
| `toColorHex` | 659 | 四个长度都要过：`#RGB` / `#RGBA` / `#RRGGBB` / `#RRGGBBAA`，大小写混写合法；`#GGG`（非 hex 字符）→ `undefined`；`'red'` → `undefined`；`''` → `undefined`。 |
| `contrastTextOn` | 671 | `'#ffffff'`/`'#fffbe6'`（浅底）→ 深色字 `'rgba(0, 0, 0, .88)'`；`'#000000'`/`'#1890ff'`（深底）→ `'#fff'`；非法 hex → `'#fff'`（兜底不抛）。简写 `#fff` 与全写 `#ffffff` 必须给出同一答案。 |
| `toWidgetColumn` / `toWidgetColumns` | 580 / 603 | 缺 `prop` 的行 → `toWidgetColumn` 返回 `null`，`toWidgetColumns` **丢弃该行并 `console.warn`**（用 `jest.spyOn(console, 'warn')` 断言被调用）；缺 `label` → 回落成 `prop`；`toWidgetColumns` 传入非数组（**尤其是 `{impl: undefined, vals: {}}` 这种恒真对象**）→ 返回 `[]` 而不是原物。 |
| `toClickActionPayload` | 708 | 输出契约是**扁平**的 `{impl, url, ...}`，**不是信封**。① 信封输入 `{impl:'X', vals:{url:'/a'}}` → 输出 `{impl:'X', url:'/a'}`（`vals` 被摊到同层，`impl` 补回）；② 扁平输入 → 原样（幂等）；③ 无 `impl` / 非对象 → `undefined`（类型身份只有一个来源，判不出种类就不分发，否则消费方 `action.url` 静默取 `undefined`，点按钮没反应）。`toEventConfigPayload` / `toEventConfigPayloads`（718 / 734）同规则；后者非数组 → `[]`。 |

#### 1.2 `src/workshop/models/entity-form.model.ts` —— 表单 ↔ 实体往返（第二优先级）

`toFormFieldValue`（539）与 `toEntityFieldValue`（595）是**互逆的一对**，必须成对测。往返测试是本任务最有价值的一类断言：

```ts
// 形态示意，具体字段以源码为准
const entityVal = toEntityFieldValue('section', 'styleConfig', formVal);
expect(toFormFieldValue('section', 'styleConfig', entityVal, entity)).toEqual(formVal);
```

必须钉死的不变量：

- `section` + `layout`：认不出的 layout 短 key → **返回 `undefined`**（让调用方保留 descriptor 默认值），绝不能塞一个坏信封。
- `section` + `styleConfig`：表单侧必须是 `{impl, vals}`（单实现也要补信封，否则 `wrapItemPropVal` 抛错并被上游吞掉 → 面板重开静默回落默认值）；实体侧必须**摊平**成 `vals`（渲染组件直接读 `section.styleConfig.borderStyle`，落成信封会让可选链静默取到 `undefined` → 「改了样式画布毫无变化」）。断言这两侧形状不同、且往返后等价。
- `conditionalVisibility`（section 与 widget 各一）：信封**原样透传**（尤其 `off` 实现必须保住自己的 impl，摊平后就分不清 off 与默认实现）；历史扁平值 → 按 `enabled` 实现补信封；`null`/`undefined` 原样返回 `null`/`undefined`（不能造出一个假信封）。
- `overlay` + `typeConfig`：多实现，摊平值**无法**补 impl → 不许猜，返回 `undefined` 交给校验报错。给一个摊平值，断言结果是 `undefined` 而不是某个猜测的 impl。
- `page`/`overlay` + `template`：已是多态 `{impl, vals}`，**双向都原样透传**，不做大小写或摊平。非 `{impl, vals}` 形状按「未选」处理（`undefined`）。

#### 1.3 `src/workshop/models/section.model.ts`

| 函数 | 行 | 不变量 |
|---|---|---|
| `resolveSectionLayoutKind` | 254 | 六个已知 layout 的 FQCN → 对应短 key；**认不出 → `undefined`**（调用方据此**拒绝写入**，不是回落某个默认值）。反向哨兵：断言未知 impl 不是 `'columns'` 或其他任何值，必须严格 `toBeUndefined()`。 |
| `toEntityLayoutConfig` ↔ `toFormLayoutConfig` | 271 / 283 | **只**对 `tabs` 的 `tabPosition` 做大小写转换：实体侧小写、表单侧大写。往返后必须回到原值。其它 layout 的 vals 必须**逐字段原样**通过（不能被 clone 丢字段）。后端下发 `TOP` → 实体 `top`（`nzTabPosition` 要小写，否则 antd 静默回落默认位置 = 「改了没反应」）。 |
| `normalizeConditionalVisibility` | 378 | 四种输入都归 `undefined`：`null`/缺失、`impl === ...Off`、默认实现但 `variableId` 为空白、无法识别的 impl。历史扁平值（无 `impl` 的 `{variableId, stateIfFalse}`）按默认实现处理。`stateIfFalse` **只有**等于 `'disabled'` 才是 `'disabled'`，其它一律 `'hidden'`。 |
| `resolveVisibilityState` | 405 | 未配置 → `'visible'`。真假判定**刻意不用 `Boolean(value)`**：`[]` → 假、`''` → 假、`'false'` → 假、`0` → 假、`null`/`undefined` → 假、非空数组/非空字符串/非 0 数字/`true` → 真。`[]` 判真是最典型的坑（`Boolean([]) === true`，会让「空对象集」显示成有数据）。`stateIfFalse='disabled'` 时假值 → `'disabled'`。 |
| `headerFormatKindOf` | 217 | `styleConfig` 缺失 / `headerFormat` 缺失 / impl 认不出 → `'title'`（分区标题不能因为新增样式配置而默认消失）。`none` / `title-with-description` 正确分派。 |
| `widgetDropListId` | 301 | 格式契约 `widget-drop-${id}`。生产方与消费方必须共用同一个函数——写错只会 `console.warn`，表现是「拖拽时灵时不灵」。断言精确字符串。 |

#### 1.4 `src/workshop/models/layout-delta.model.ts` —— 保存差集（脏数据 = 每次保存全量重写）

| 函数 | 行 | 不变量 |
|---|---|---|
| `stableStringify` | 130 | 两次 key 插入顺序不同的等价对象 → **同一字符串**；`undefined` 值被丢弃（不落成 `null`）；函数值被丢弃；数组顺序**保留**（数组有序，不能排序）。 |
| `canonicalWidget` | 200 | `conditionalVisibility` 归一（off 与未配置 → 同一个值）；`displayConfig` 中**递归**剔除 `displayName`（只剥顶层不够：GET 侧每一层子描述符都带 `displayName`，只剥一层会导致「只要有宽度配置，每次保存都重写一遍」）；`formItem` / `displayFormItem` / `descriptor` / `createdAt` / `updatedAt` **不计入**比较；`displayConfig` 必须**参与**比较（只改 Display 的 widget 也要进 upsert，否则服务端不重写 `_display.xml`）。 |
| `canonicalSection` | 177 | `conditionalVisibility` 归一；`childSections` 参与比较但子分区里的 `widgets` 不参与。 |
| `canonicalOverlay` | 163 | `typeConfig` 摊平/信封两种形态归一后等价；`variableBasedVisibility` 的信封/扁平/off/null 归一。 |
| `isEmptyDeltaDraft` | 456 | 只有 `undefined` / 空集合算空；不可变数组不算空。 |

#### 1.5 `src/workshop/models/object-set-filter.model.ts`

- `mergeObjectSetFilterValues`（324）：**纯函数**，被拒项**随返回值**交给调用方（`{filters, rejected}`），函数内部**不** `console.warn`。断言：`null`/`undefined` 输入静默跳过（不进 `rejected`）；非 filter-value 形状 → `rejected` 且 `reason === 'not-a-filter-value'`；objectType 双方都声明且不一致 → `reason === 'object-type-mismatch'`；`rejected` 里要带 `source`/`objectType` 供上游拼日志。
- `isFilterValueApplicable`（296）：任一端**缺失即放行**（通配）。只有双方都声明且不同才拒绝。
- `toOntologyFilters`（147）与 `definition-config.model.ts#toFilterRows` 的**丢弃策略不同**：前者丢弃 value 为空的行（发给后端前的闸门），后者保留（视图模型）。写测试时验证这个差异，别把两者写成一样。

#### 1.6 `src/workshop/models/definition-config.model.ts`

- `toDefinitionConfig`（209）：认不出的 impl → **`null`**，不是「字段全空的对象」（后者在下游表现为「变量算出空值」，比 `null` 难查得多）。每种 kind 的必填字符串字段缺省补 `''`、列表缺省补 `[]`；`filterVar` / `parameters` 语义可选，缺省即**不出现**（不是空数组）。用 `jest.spyOn(console, 'warn')` 断言未知 impl 有告警。
- `resolveDefinitionKindFromImpl`（292）：未知 → 空字符串 `''`（不是 `undefined`，这是刻意的）。

#### 1.7 `src/workshop/models/widget.model.ts`

- `resolveWidgetKind`（367）：FQCN `com.qlangtech...ObjectTableWidget` → `'object-table'`；已是短 key（不含 `.`）→ 原样返回；缩写边界 `SQLQuery` → `'sql-query'`（连续大写后跟大写+小写要切）；`widget.descriptor.impl` 作为回落；两者都空 → `''`。
- `endTypeOf`（398）：`descriptor` 是空壳 `{} as Descriptor`（metadata 未到时）**不能抛**——必须返回 `undefined`。取的是 `descriptor.extractProps['endType']` 而不是 `descriptor.endtype` getter（后者没判 `extractProps` 空）。反向哨兵：给空 `extractProps`，断言不抛且为 `undefined`。
- `omitFormItem`（245）：key 为 `formItem` / `displayFormItem` → `undefined`；其它 key → 值原样。

#### 1.8 `src/workshop/models/header.model.ts`

- `resolveHeaderView`（226）：`header` 为 `null` → 后端默认的一套（水平、不可折叠、可见、`favoriteEnabled` 默认真）。**折叠配置挂在 `vals.orientation` 的三层嵌套里**——建一个带 `collapseConfig` 垂直 header，断言 `collapsible === true`；再建一个把 `collapseConfig` 放到 `vals` 顶层的错误结构，断言 `collapsible === false`（少摊一层会恒 `undefined` 且不报错）。
- `createCollapseConfig`（265）：`off` 侧的 `vals` **必须为空对象**（零字段子类）。
- `createOrientation`（258）：切方向时**整体重建**——`horizontal` 的载荷只有 `height`，`vertical` 只有 `width` + `collapseConfig`，另一侧的字段必须**不出现**（矛盾态不可表示）。
- `implOf` / `valsOf`（182 / 187）：既要认 `item.impl` / `item.vals`，也要认表单回传的 `item.descVal.impl` / `item.descVal.vals`。
- `resolveLogoConfig`（138）：`impl` 认不出 / `icon`/`url` 为空白 → `undefined`（页头只是不显示 logo，不抛）。

#### 1.9 `src/workshop/models/page.model.ts`

- `columnWidthToCss`（165）：`PercentColumnWidth` → `${percentage}%`（缺省 30）；`PixelColumnWidth` → `${pixels}px`（缺省 260）；传入 `null` / 认不出 → `'30%'`。
- `headerHeightToCss`（177）：`PixelHeaderHeight` → `${pixels}px`（缺省 80）；其它 → `'auto'`。
- `extractPageLayoutStyle`（198）：`null` → `{ gridTemplateColumns: '30% minmax(0,1fr)' }`；四种模板各返回不同键（`two-column`/`header-content` 用 rows 或 columns、`grid` 用 `repeat(auto-fit, minmax(Npx, 1fr))`、`three-column` 三列）。
- `isFirstChildSpanAll`（258）：只有 `grid` 模板才有这个概念；`firstChildSpanAll !== false` 时默认**通栏**（缺省为真）。

#### 1.10 `src/workshop/models/click-action.model.ts`

- `resolveImplKind`（40）：`com.x.OpenOverlayConfig` → `'open-overlay'`（剥 `Config` + 插分隔 + 小写）；已是短 key（不含 `.`）→ 原样；`impl` 为空 → `''`；自定义 `stripSuffix` 生效（`'Layout'` 用于 section）。
- `antdToken`（61）：`'default_'` → `'default'`；`'primary'` 原样；空值 → fallback。
- `toBool`（75）：`'false'` / `'0'` / 空串等字符串不得判真（「开关关不掉」的成因）。

#### 1.11 `src/workshop/models/widget-config.model.ts#resolveWidgetConfig`（1834）—— **单条最高优先级**

这是所有 widget 读取配置的**唯一入口**，三件事必须各有一组断言：

1. `widget.formItem` 存在 → 走 `fromItem`（活的表单实例优先）。
2. `formItem` 不存在 → 回落 `fromEntity`（不能对 `null` 调 `fromItem`）。
3. **`displayConfig` 恒从实体取**，不用 `fromItem` 的结果覆盖 —— `fromItem` 那条路径读不到它（它是独立落盘的插件实体，由信封字段下发）。反向哨兵：造一个「有 formItem + 有 displayConfig」的 widget，断言结果的 `displayConfig` **不是**空对象（`fromItem` 恒返回空对象，这是已发生过的真实 bug）。
4. `formItem` 路径返回的 `id` 要用 `widget.id` 覆盖（若 `widget.id` 有值）。

对应用法：`resolveChartXYWidget`（`widgets/chart-xy.config.ts:659`）、`resolveObjectTableWidget`、`resolveObjectListWidget`、`resolveFilterListWidget`、`resolvePivotTableWidget`、`resolveMetricCardWidget`、`resolveChartPieWidget` 都是它的薄包装。**测 `resolveWidgetConfig` 本身即可**，不必每个 wrapper 都测；挑 2 个 wrapper 做端到端即可。

### Tier 2 —— 单 Widget 的解析器（挑 6~8 个，优先下面这些）

| 目标 | 位置 | 不变量 |
|---|---|---|
| `resolveObjectListDisplay` | `widgets/object-list.config.ts:123` | 三种入参：裸 impl 串（短 key 或 FQCN）/ 实体信封 `{impl, vals}` / 已是值形态（**幂等**，再传一次结果不变）。认不出 → `undefined`（**不猜**，猜错会让画布从网格变列表）。 |
| `snapObjectListColumns` | `:92` | 吸附到 `[1,2,3,4,6,8,12,24]`：5→4、7→6；`0`/负数→1；`>=24`→24；`NaN`/`undefined`→默认 4。 |
| `snapObjectListListColumns` | `:107` | **不吸附**固定集合，只夹到 `1..6`；非整数/非正数/缺失 → 默认 2。与上一个的差异要被断言出来。 |
| `resolveObjectListSorting` / `resolveObjectListSelection` | `:192` / `:236` | 分页有「关态安全默认」，展示形态没有——这个差异要断言。⚠️ 排序的开启态**缺 `sortField` 时也回落 `{type:'no'}`**（半配好的开启态无意义），这是分页没有的分支。 |
| `resolvePagenator` | `widget-config.model.ts:1579` | 四种入参（裸串 / 信封 / `descVal` 信封 / 已是值形态）全部认，且**幂等**；认不出 / 非对象 → `{type:'no'}`（安全默认，与 `resolveObjectListDisplay` 的「不猜」相反）。它是后文所有「四形态归一」函数的范式，值得单独钉死。 |
| `toChartSeriesConfig` / `toChartSeries` | `widgets/chart-xy.config.ts:157 / 209` | identity 字段缺失的行丢弃（走 `toWidgetColumns` 同口径）；`toChartSeriesAggregation` 的取值集合是**大写**（`SUM`/`AVERAGE`…），与 `pivot-table` 的小写 `sum`/`count` **不同名同值域**，断言不被互相抄。 |
| `toChartLayers` / `toChartLayer` | `:553 / 492` | 坏层丢弃；层内结构块（xAxis/yAxis/legend）走各自的 off/on 多态判据（`isOffImpl`）。 |
| `toChartLegend` | `:591` | `LegendOff` → 关闭态的稳定形状。 |
| `toFilterItems` / `resolveFilterComponent` | `widget-config.model.ts:1151 / 901` | 组件种类认不出时的回落；空数组入参 → `[]`。 |
| `toMetricConfigs` / `toConditionalFormatRules` | `:1363 / 1276` | 非数组 → `[]`；坏行丢弃 + `console.warn`。 |

### Tier 3 —— 组件（至多 1 个文件，且只测纯状态迁移）

- `src/workshop/components/widgets/filter-list/filter-list.component.ts`
  - 可测：`onClearFilterValue()` → 绑定值变 `undefined` 且 `renderEpoch` 自增 1；`trackByProperty` 返回精确 key。
  - 不可测（本任务范围外）：DOM 渲染、子组件交互、CSS。
- ❌ **不要测 `base-widget.component.ts`。** 它**刻意不声明 `ngOnDestroy`**（类注释里写明了理由：21 个子类里 18 个自己实现，基类再声明一个会让漏写 `super.ngOnDestroy()` 的子类静默丢清理逻辑）。它也没有可独立断言的行为，测它只能测 Angular 的 `inject()` 本身。

**另有一个纯函数挂在这个目录下，但按 Tier 1 标准测**：`src/workshop/components/widgets/pivot-table/pivot-transform.ts#buildPivotFromAggregation`（50）。重点：缺格**不补 `0`**（0 是真实数据，补 0 等于凭空造数据）；单元格键分隔符是 `\u0000` 而不是空格（`"New York"+" 1"` 与 `"New York "+"1"` 用空格拼会撞键 → 一格静默覆盖另一格，界面上完全看不出异常）；行/列顺序按首次出现顺序，不排序。

---

## 4. 测试写法纪律

### 4.1 每个 `describe` 对应一个被测函数，用中文描述行为

```ts
describe('payloadOf —— 三种线格式形态的摊包', () => {
  it('信封形态取 vals', () => { ... });
  it('表单回传的 descVal 形态取内层 vals', () => { ... });
  it('已是扁平对象时原样返回，不返回空对象', () => { ... });
  it('非对象一律返回空对象', () => { ... });
});
```

用 `it('...')` 描述**不变量**，不用 `it('should work')`。

### 4.2 必须包含「反向哨兵」

每个 spec 文件至少有一组断言是**针对某个具体已知 bug 的回归**，并在注释里标明：

```ts
// 反向哨兵：[] 是空对象集，必须判假。
// Boolean([]) === true，用它判断会让「没有数据的对象集」显示成有数据。
expect(resolveVisibilityState(vis, [])).toBe('hidden');
```

### 4.3 禁止的事

- ❌ **不要 `jest.mock('.../models/...')`** —— 模型层就是被测对象。要截断只在 HTTP 边界截断（见 4.4）。
- ❌ **不要写 snapshot 测试**。快照无法表达契约，只会把当前的 bug 固化成期望值。
- ❌ **不要测私有函数**（`isTruthyValue`、`hexToRgb`、`sortKeysDeep`、`cellKey` 等未导出的）。通过导出函数间接覆盖——如果发现某个私有函数必须有独立测试才能说清，说明它该被导出，**报告**而不是擅自导出。
- ❌ **不要 `as any` 绕开类型后断言 `undefined`** 来表达「没有这个能力」。要断言形状就用精确的 `toEqual`。
- ❌ **不要改生产代码让它好测**。若确实发现生产代码的 bug，**写下会失败的测试，然后在报告里单列一节（文件:行 / 症状 / 建议修法），不要顺手改**。唯一的例外是需要导出一个私有函数——那也要报告。
- ❌ **不要降低 `jest.ratchet.config.js` 里任何既有阈值**，也不要删既有 spec 的断言。
- ❌ **不要给 `src/workshop/**` 加整体覆盖率统计**（见 §2.2）。

### 4.4 需要 HTTP 层的数据时，复用既有 fixture

`src/test/fixtures/plugin-fixtures.ts` 已经提供了在 **HTTP 边界**截断的一整套构造器（`pluginConfigInfoResponse` / `pluginSaveSuccessResponse` / `heteroListWithHostItem` / `wrapHostDescriptor` / `assertFixtureSane` 等），设计原则是「在 HTTP 边界截断，模型层绝不 mock」。

**如果某个 spec 需要类似的构造器，从 fixture 文件 import，不要在被测 spec 里再抄一份。**（既有的 `src/common/tis.plugin.spec.ts` 就是反面教材，把 fixture 内联复制了一份，那是个已知的债，别扩大它。）

### 4.5 一条自检：验证测试真的在测

写完后挑 **3 个**最有价值的断言做突变验证：

1. 临时把生产代码里对应的一个字面量/分支改坏（例如把 `'horizontal'` 改成 `'horiz'`、把 `endsWith('Off')` 改成 `endsWith('On')`）。
2. 跑该 spec，**必须变红**。
3. `git checkout -- <该生产文件>` 还原。

如果改坏了还是绿，说明那条断言没测到东西——重写它。**做完务必确认 `git status` 里没有残留的生产代码改动。**

---

## 5. 交付与验收

### 5.1 交付物

- 8~12 个 `<源文件名>.spec.ts`，与被测源码同目录。
- `jest.ratchet.config.js`：新增 `collectCoverageFrom` 条目 + 新增文件级阈值（实测值向下取整）。
- 一份简短报告（见 5.3）。

### 5.2 验收命令（必须全绿）

```bash
cd /Users/mozhenghua/j2ee_solution/project/tis-console

# 1. 单文件快速迭代
npx jest src/workshop/models/widget-config.model.spec.ts

# 2. 全量门禁（两次 jest：global 门禁 + 棘轮）
npm run test:ci

# 3. 生产代码类型门禁（spec 的类型检查由 ts-jest diagnostics 逐文件承担，见 §2.1）
npm run build

# 4. 生产代码未被改动（除 jest.ratchet.config.js 与新 spec）
git status --porcelain
```

> ⚠️ **不要用 `npx tsc -p tsconfig.spec.json --noEmit` 做验收**：该 tsconfig 会按 spec 的依赖闭包
> 拉进 `src/runtime/misc/RCDeployment.ts`，那里有一个**先天红**的 `TS2503`（`import X = flink.…`
> 依赖全局 ambient 声明，全量单程序编译看不见）。这条命令在本次改动之前就是红的，与你的工作无关，
> 别去改它。spec 自身的类型错误由 ts-jest diagnostics 在 `npx jest` 时就暴露了。

任何一条失败都不要通过放宽配置来「修」——先判断是测试写错了还是发现了真 bug（见 4.3）。

### 5.3 报告格式

结束时输出：

1. **新建的 spec 文件清单**，每个文件：被测源文件、用例数、覆盖的关键不变量（一行一条）。
2. **`jest.ratchet.config.js` 的改动**：新增哪些文件、各是多少阈值、对应实测值多少。
3. **`npm run test:ci` 的 text-summary 末几行**（原样粘贴，不要转述）。
4. **发现的疑似生产 bug**（若有）：`文件:行` / 症状 / 用一个失败断言证明 / 建议修法。没有就写「无」。
5. **未完成 / 刻意跳过的条目**及理由。

---

## 6. 建议执行顺序

```
1. 读 src/workshop/models/widget-config.model.ts（1851 行，最核心）
   → 写 widget-config.model.spec.ts（Tier 1.1 + resolveWidgetConfig）
   → npx jest 确认绿

2. entity-form.model.spec.ts（往返对）+ section.model.spec.ts（可见性/布局对）
   → 这两个文件是「改一处漏一处」的高危区

3. layout-delta.model.spec.ts（canonical + stableStringify）
   → 断言「不同插入顺序 → 同一字符串」

4. object-set-filter / definition-config / widget / click-action / page / header
   → 每个 15~30 个用例，比重小、静态强

5. widgets/*.config.ts 挑 3~4 个（object-list / chart-xy / pivot-table）

6. Tier 3：只做 filter-list（纯状态迁移），外加 pivot-transform（纯函数，按住 Tier 1 标准）

7. 扩 jest.ratchet.config.js，跑 npm run test:ci
```

如果时间/预算有限，**至少完成 1、2、3**——这三块覆盖了 Workshop「保存丢配置」「画布不刷新」「面板重开回落默认值」这三类最主要的静默失效。
