# TIS 前端插件表单体系（`<tis-plugins>`）问题清单与修改方案

> 状态：**待执行**（本文只做方案，不含代码改动）
> 建档日期：2026-09-21
> 涉及工程：`/Users/mozhenghua/j2ee_solution/project/tis-console`
> 核心文件：
> - `src/common/plugins.component.ts`（1274 行）
> - `src/common/plugin/item-prop-val.component.ts`（1627 行）
> - `src/common/tis.plugin.ts`（1961 行，模型层）

---

## 0. 结论先行

分层是对的，实现层的纪律松了。**不要动架构，只修纪律。**

- 应该保留并保护的：descriptor 驱动、`HeteroList → Item → ItemPropVal` 三层模型、`DescribleVal extends Item` 的递归、`Item.project()` 的投影边界。
- 应该修的：一个手写 CD 协议（问题 1）、一个会产出错误数据的异步级联（问题 3）、一个 1600 行的万能控件（问题 2）。
- 优先级：**问题 1 → 问题 3 → 问题 4/5 → 问题 6/7 → 问题 2（大重构，最后做）**。

---

## 1. 现状架构速览（给执行者）

三层模型，全部来自服务端 descriptor：

```
HeteroList            一个「插件分类」（reader / writer / 某扩展点）
  └─ items: Item[]    一个「插件实例」（如 MySQL reader）
       └─ vals: { [key]: ItemPropVal }   一个「字段」
            └─ descVal: DescribleVal extends Item   ← 多态字段：值本身又是一个 Item
```

- **schema 来源**：Java `@FormField` → JSON → `Descriptor.wrapDescriptor()` → `attrs: AttrDesc[]`。前端从不硬编码字段名。
- **渲染**：`plugins.component.ts` 遍历 `_heteroList`，每个 `Item` 遍历 `propVals`，渲染 `<item-prop-val>`；后者在遇到 `pp.descVal` 时**递归渲染自己**（`item-prop-val.component.ts:561`）。
- **提交**：`Item.project()` 递归投影出 `{impl, vals}` → `postHeteroList()` POST。
- **复用面**：全工程 **32 个组件、约 50 处**使用 `<tis-plugins>`（数据源配置、增量构建 4 步、DataX worker、快照对比、workshop 配置面板……）。

**这个架构本身不该动。** 下面所有问题都是实现纪律问题，不是分层问题。

---

## 2. 问题清单

证据强度标注：**【已核实】**= 逐行读过代码确认；**【推断】**= 由代码结构推理，未实测。

| ID | 问题 | 严重度 | 证据强度 |
|---|---|---|---|
| 1 | `cdr.detach()` 手写 CD 协议；加载失败后组件永不恢复 | **高（真 bug）** | 已核实 |
| 2 | `item-prop-val.component.ts` 1627 行、承担 8 种职责 | **高（维护性）** | 已核实 |
| 3 | `propValChange` 级联请求无序、无防抖；`0/false/''` 不触发 | **高（正确性）** | 已核实 + 推断 |
| 4 | 级联回填的字段变化不对外可见（`itemChanged` 看不到） | 中 | 已核实 |
| 5 | 静默失败遍布：输入设错不报错、异常被 `console.log` 吞掉 | 中 | 已核实 |
| 6 | 一个 boolean 承担三种语义（`shallInitializePluginItems`） | 中 | 已核实 |
| 7 | `EventEmitter` 当作 `@Input` 使用（反向控制通道） | 中 | 已核实 |
| 8 | 测试覆盖为零，且测试入口双重损坏 | 中 | 已核实 |
| 9 | 死代码与「真假」边界不清（`true \|\| flag`、大段注释代码） | 低 | 已核实 |
| 10 | 小事：`HeteroList.create()` 返回长度恒为 1 的数组；`Item.propVals` 是带副作用的模板 getter | 低 | 已核实 |

---

### 问题 1 · 手写 CD 协议，且加载失败后组件永不恢复

**症状**

`plugins.component.ts:868` 在构造期直接 `this.cdr.detach()`，之后靠手工 `reattach() + detectChanges()` 复活。恢复点共 **7 处**（`plugins.component.ts` 的 900/901/917/918/1053/1121/1151），另有 **12 处**裸 `detectChanges()`（两个文件合计）。

关键在 `ngAfterContentInit`（L894-903）→ `initializePluginItems` 回调（L907-923）：

```ts
public initializePluginItems(e?: SavePluginEvent) {
  PluginsComponent.initializePluginItems(this, this.plugins, true
    , (success: boolean, hList: HeteroList[], showExtensionPoint: boolean) => {
        if (success) {
          this.showExtensionPoint.open = showExtensionPoint;
          this._heteroList = hList;
          this.afterInit.emit(this._heteroList);
          this.cdr.reattach();        // ← 只在成功分支
          this.cdr.detectChanges();
        }
        this.ajaxOccur.emit(new PluginSaveResponse(success, false, null));
      }, e);
}
```

**`success=false` 时组件永远停在 detached 状态**：表单空白、不可交互、无任何错误提示（错误只通过 `ajaxOccur` 发出，未订阅的宿主什么也看不到）。

**影响**

- 真 bug：配置加载失败 → 用户面对一个死表单，且不知道发生了什么。
- 放大器：`detach()` 让组件对 CD 完全不可见，**任何 mutate 状态却没走那 7 个恢复点的代码都静默不刷新**。曾经排查「workshop 画布为什么不重绘」的成本，本质上被这个协议放大了。
- 认知负担：装饰器声明 `ChangeDetectionStrategy.OnPush`（L134），实际机制却是「自己 detach / reattach」—— 装饰器在撒谎，读代码的人会按 OnPush 的心智模型推理，然后推错。

**根因**

`detach()` 被用来「避免数据到达前渲染空表单」，但缺少对应的失败分支恢复，也没有把这套协议命名/封装起来。

**修复方案**

**阶段 A（小、安全，建议先做）**

1. 把回调的两个分支都改成恢复：
   ```ts
   // 成功与失败都要恢复 CD —— detach 之后组件对 CD 不可见，不恢复就是死表单
   this.cdr.reattach();
   if (success) { ...赋值... }
   this.cdr.detectChanges();
   ```
2. 失败时给出可见反馈：`errorsPageShow` 为真时把错误写进模板已绑定的 `[result]="result"`（L142），复用既有 `tis-page-header` 渲染。
3. 把散落的 `reattach + detectChanges` 收成一个私有方法（如 `private refresh()`），至少让协议**有名字**，后续新代码照抄。

**阶段 B（需实测，独立进行）**

评估**彻底删掉 `detach()`**，改为「OnPush + 在异步边界 `markForCheck()`」。

- 反对删除的理由（改造前必须验证）：`detach()` 期间的空白可能是刻意为之。
- 支持删除的理由：模板 L137 的 `nz-spin [nzSpinning]="formDisabled"` 在 detached 期间**也渲染不出来**，也就是说 detach 得到的是「白屏」而非「加载中」——删掉 detach 反而能让 spinner 正常显示。
- 验证方式：给 `initializePluginItems` 的请求人为加 3 秒延迟，对比删除前后首屏表现。

**回归风险**

50 个调用点全部受影响。阶段 A 的改动只影响「加载失败」这一条路径（当前是彻底坏掉的），风险低。阶段 B 触碰首屏渲染时机，必须逐页手测。

---

### 问题 2 · `item-prop-val.component.ts`：1627 行、8 种职责

**症状**

同一个类里同时是：

| 职责 | 位置 |
|---|---|
| 表单控件渲染（input/number/textarea/enum/select/switch/color/file/date） | 模板 L175–L329，共 9 个 `inputValChange` 绑定点 |
| 递归子表单宿主 | L561（嵌套 `<item-prop-val>`） |
| 多态插件选择器 | L508/L529 → `changePlugin`(L1373)、`onOffSwitchChange`(L1390) |
| 多选元组编辑器宿主 | `descValItems`(L880)、`addMultiChildPlugin`(L915)、`removeMultiChildPlugin`(L893) |
| 文件上传 | `handleFileUploadChange`(L1531) |
| 全屏编辑器抽屉 | `openFullscreenEditor`(L1539) + 文件尾 L1611 起的 `@Component` |
| Router / 可选项管理器 | `openSelectableInputManager`(L1456)、`reloadSelectableItems`(L1491)、`CREATE_ROUTER_SEARCH_MIN_COUNT`(L832)、`routerSearchable`(L1171) |
| 本体特化集成 | `ontologyPropRoleTypeLinkChange`(L1006) |
| 插件安装/对话框编排 | `checkAndInstallPlugin`(L1237)、`openPluginDialog`(L1283) |

`changeDetection: ChangeDetectionStrategy.Default`（L126），且模板递归自身 —— 改任何一处都要先想清楚在哪一层 CD 上。

**影响**

- 任何表单类改动都要在 1600 行里定位，回归面覆盖全部 50 个调用点。
- 单测无法写：职责耦合在同一个类 + 模板里。
- 与问题 1 叠加：递归组件 + 手写 CD = 「改了不刷新」的高发区。

**根因**

按「能复用就塞进去」演化，缺按**控件族**拆分的边界。

**修复方案（大重构，最后做）**

按控件族拆出子组件，`ItemPropValComponent` 降级为**分派器**：

| 拟拆出 | 承接 | 依据 |
|---|---|---|
| `ScalarControlComponent` | input/number/textarea/enum/select/switch/color/date 的渲染 | `pp.descVal == null && pp.type` 为一组标量类型 |
| `PolymorphicFieldComponent` | `descVal` + `changePlugin` + `onOffSwitchChange` + 图标选择 | L500–L540 一带 |
| `SubFormFieldComponent` | 嵌套 `<item-prop-val>` 递归 + `addMultiChildPlugin` 系列 | L548–L566 + L880–L1002 |
| `SelectableManagerComponent` | router / 可选项 / 加载 | L1456–L1530 + L1171–L1230 |

**硬约束（改造时不许违反）**

1. **`propValChange` 级联必须留在分派器**（调用点在 `plugins.component.ts:796`，级联引擎在 `item-prop-val.component.ts:46`）—— 不能下沉进子组件，否则级联顺序失控。
2. **不改线格式**：`Item.project()` / `toPostBody()` 的行为必须逐字节不变。
3. **不改模型层**：`tis.plugin.ts` 不动。
4. **顺序**：必须在问题 1 和问题 3 修完之后再动 —— 否则会同时动「CD」和「异步」，拆出新的静默失败也无从归因。

**回归风险**

高。必须逐项手测第 7 节清单。建议按控件族**逐个**拆、每个族一次提交，不要一次性全拆。

---

### 问题 3 · `propValChange` 级联：请求无序、无防抖、假值不触发

**调用链（已核实，注意与直觉不同）**

```
控件 ngModelChange
  → ItemPropValComponent.inputValChange()            item-prop-val.component.ts:1397
      └─ 只做 this.valChange.emit(_pp)，不调级联
  → PluginsComponent.itemPropValChange()             plugins.component.ts:795
      ├─ propValChange(...)     ← 级联在这里触发
      └─ itemChanged.emit(...)  ← 2026-09-21 新增的实时通知
```

**级联引擎**（`item-prop-val.component.ts:46-122`）：

```ts
export function propValChange(changedProp, item, pluginMeta, basicForm, cdr) {
  const pipes = item.dspt.valueChangePipes;
  if (!pipes || pipes.length === 0) return;          // 无 pipe 直接返回
  const pipe = pipes.find(p => p.fromField === changedProp.key);
  if (!pipe) return;

  let primaryVal = changedProp.primary;
  if (!primaryVal) { return; }                       // ← (A) 假值缺陷

  ...
  basicForm.httpPost('/coredefine/corenodemanage.ajax', params).then(r => {
    if (!r.success) return;
    for (const toFieldName of pipe.toField) { ... toFieldProp.setPropValEnums(...) ... }
    cdr.detectChanges();
  });                                                // ← (B) 无序、无防抖、无取消
}
```

**(A) 假值缺陷** 【已核实】：`if (!primaryVal) return;` 用真值判断而非空值判断。`primary` 为 `0` / `false` / `''` 时级联**静默不触发** —— 数值型和布尔型的级联字段（例如「取 0 条」「关闭某开关」）永远不联动。

**(B) 无序响应** 【已核实结构 / 推断后果】：每次变更发一个 POST，没有请求序号、没有取消、没有 debounce。连打字符时**后发的响应可能先到，早先的响应会覆盖基于更新输入算出的选项**。这不是性能问题，是**产出错误数据**的问题。

**(C) 无取消** 【已核实】：组件销毁时在途请求不会被忽略，回调里还会调 `cdr.detectChanges()`（对已销毁组件）。

**修复方案**

```ts
// 模块级：按「item.impl + 字段名」维度记录最新请求序号，避免键无限增长
const pipeSeq = new Map<string, number>();

export function propValChange(changedProp, item, pluginMeta, basicForm, cdr) {
  ...
  // (A) 空值判断取代真值判断
  const primaryVal = changedProp.primary;
  if (primaryVal === undefined || primaryVal === null) { return; }
  if (Array.isArray(primaryVal) && primaryVal.some(v => v === null || v === undefined)) { return; }

  // (B) 认领序号；响应回来时若已不是最新，直接丢弃
  const key = `${item.impl}:${changedProp.key}`;
  const ticket = (pipeSeq.get(key) ?? 0) + 1;
  pipeSeq.set(key, ticket);

  basicForm.httpPost(...).then(r => {
    if (pipeSeq.get(key) !== ticket) { return; }   // 过期响应，丢弃
    if (!r.success) { return; }
    ...
  });
}
```

- **debounce**：作为**可选后续**，不放进第一次修复。原因：序号机制已解决正确性，debounce 只解决请求量；两者混在一起改会让回归归因困难。若实测请求过密，再加 200–300ms debounce（注意 debounce 会改变级联的"手感"，需要产品确认）。
- **取消**：序号机制天然覆盖，无需额外的 unsubscription。若后续要彻底清理，可在组件 `ngOnDestroy` 时 `pipeSeq.delete(key)`（模块级 Map 的生命周期问题，需一并考虑，避免长驻页面内存增长）。

**回归风险**

级联（`valueChangePipes`）在数据源配置里用得很多，必须逐条 pipe 手测。改 (A) 会让原本"不触发"的假值场景**开始触发**，可能暴露下游未处理的 0/空值 —— 这是修复的目的，但要预期到。

---

### 问题 4 · 级联回填的字段变化对外不可见

**症状**【已核实】

2026-09-21 新增的 `itemChanged` 事件（`plugins.component.ts:393`）在 `propValChange` 触发后立即 emit，但**级联的异步回填不 emit**：

```ts
// propValChange 内部，回填目标字段时没有任何通知
toFieldProp.setPropValEnums(options, _ => false);
toFieldProp.primary = undefined;         // 值被清空
toFieldProp.pendingHighlight = true;
// ← 没有 emit
```

**后果**：workshop 编辑器里，如果一个字段的值是被级联改写/清空的，「改即所见」不会反映到画布上。这是一个**看起来能用、个别字段不灵**的隐蔽缺陷。

**修复方案**

在 `propValChange` 的回填循环结束后补一次通知。但**不能**直接复用 `itemChanged`，因为：

- `itemChanged` 的载荷是 `{item, prop}`，且被 `PluginsComponent` 转发；
- 级联引擎是**自由函数**，拿不到 `PluginsComponent` 实例，只能拿到 `basicForm: BasicFormComponent`（形参已有）。

建议的设计（择一，执行时定）：

| 方案 | 做法 | 评价 |
|---|---|---|
| 甲 | `propValChange` 增加一个可选回调形参 `onCascadeApplied?: (props: ItemPropVal[]) => void`，由 `PluginsComponent.itemPropValChange` 传入转发到 `itemChanged` | 显式、可控；需要改函数签名（全工程仅 1 处调用，风险低） |
| 乙 | 在 `ItemPropVal` 上引入一个轻量的 `changed$` Subject，模型层统一发通知 | 更根本，但触碰模型层，与问题 2 耦合，建议留到大重构时一起做 |

**建议：先做甲（小），把乙记入问题 2 的改造目标。**

---

### 问题 5 · 静默失败：输入设错不报错、异常被吞

**已核实的位置**

| 位置 | 代码 | 后果 |
|---|---|---|
| `plugins.component.ts:852` | `setPluginMeta`: `if (!metas \|\| metas.length < 1) { return; }` | `[plugins]` 传错 → 整个表单静默空白 |
| `plugins.component.ts:465` | `try { HeteroList.create(...) } catch (e) { console.log(e); }` | 构造失败 → 对话框显示空表单，错误只在 console |
| `plugins.component.ts:1014` | `.catch(err => { console.log(err); ajaxOccur.emit(new PluginSaveResponse(false, false, e)); })` | **服务端返回的错误信息整个丢失**，宿主只能看到一个无内容的失败信号 |
| `plugins.component.ts:1027` | `removeItem` 手工遍历重建数组 | 风格问题（应为 `filter`），与上几条共同构成"对失败没有表达能力"的印象 |

**修复方案**

1. **错误信息透传（优先）**：`savePluginSetting` 的 catch 分支把 `err`（或其 `message` / 服务端 `msg`）带进 `PluginSaveResponse`，让宿主能显示。这是**收益最高、风险最低**的一条 —— 目前 50 个调用点在保存失败时都拿不到原因。
2. `HeteroList.create` 的 catch：至少 `console.error` + 失败时给出可见提示（与问题 1 阶段 A 的失败反馈合并做）。
3. `setPluginMeta` 空数组：**先改 `console.error`，不要直接抛异常** —— 需先审计 50 个调用点是否存在"初始化期间合法传空"的情况，审计后再决定是否抛。

---

### 问题 6 · 一个 boolean 承担三种语义

**症状**【已核实】

`shallInitializePluginItems`（L333）同时控制：

| 语义 | 位置 |
|---|---|
| 是否自动拉取插件配置 | L894（`ngAfterContentInit`） |
| 折叠面板 vs 平铺 | L144 `[ngSwitch]`、L247 |
| `item-block` 边框样式 | L182 `[ngClass]="{'item-block': shallInitializePluginItems \|\| useCollapsePanel}"` |
| 是否显示删除按钮 | L202 |
| 是否显示添加按钮 | L257 |

而 `useCollapsePanel`（L335）也混进同一个 `ngSwitch`。名字只描述了第一种含义。

**修复方案**

拆成语义明确的输入，**新增**而非改名（避免动 50 个调用点）：

- `autoLoadPluginConfig: boolean`（行为）
- `layoutMode: 'collapse' | 'plain'`（呈现）
- `boxed: boolean`（样式）

旧的 `shallInitializePluginItems` / `useCollapsePanel` 保留为**过渡期别名**，内部映射到新输入，加 `@deprecated` 注释；调用点**逐步**迁移，全部迁完后删除别名。

> 注意：Angular 的 `@Input()` setter 可以读取旧值做映射，实现别名成本很低，但**不要**用 `@Input({alias})` 做双向别名（会有赋值顺序问题）。

---

### 问题 7 · `EventEmitter` 当作 `@Input`

**症状**【已核实】

```ts
// plugins.component.ts:362
@Input() savePlugin: EventEmitter<SavePluginEvent>;

// plugins.component.ts:887-891
ngAfterContentInit(): void {
  if (this.savePlugin) {
    this.subscription = this.savePlugin.subscribe((e: SavePluginEvent) => {
      this.savePluginSetting(null, e || new SavePluginEvent());
    });
  }
  ...
}
```

父组件必须自己 `new EventEmitter()` 传进来，子组件再 subscribe —— 这是**反向的控制通道伪装成输入**。全工程只有一处使用。

**修复方案（低风险）**

把声明类型从 `EventEmitter<T>` 放宽为 `Subject<T>`：

```ts
@Input() savePlugin: Subject<SavePluginEvent>;
```

**这个改动是源码兼容的**：Angular 的 `EventEmitter<T> extends Subject<T>`，现有 `new EventEmitter<SavePluginEvent>()` 传参处**无需改动**。收益是语义正确（输入的是"一个可被触发的命令流"，不是"本组件的输出"），且新调用点可以直接传 `Subject`，不必再假装构造一个 `EventEmitter`。

---

### 问题 8 · 测试覆盖为零，测试入口双重损坏

**症状**【已核实】

- `find src -name "*.spec.ts"` → **1 个**文件（`src/offline/workflow.schedule.config.component.spec.ts`）。
- `angular.json:145-151` 的 test target 引用 `karma.conf.js` → **该文件不存在**。
- 同处引用 `tsconfig.spec.json` → **该文件也不存在**。
- `package.json` 的 `test` / `test:once` 脚本都调 `karma start karma.conf.js` → **必然失败**。

即：一个被 50 个调用点依赖、承载全部插件配置的框架层组件，**测试覆盖为零，且无从跑起测试**。

**修复方案**

1. **恢复测试入口**（先决条件，独立于本次重构）：补 `karma.conf.js` 与 `tsconfig.spec.json`，让 `ng test --single-run` 能跑通一个 hello-world spec。
2. **补测试，按"投资回报"排序**：
   - 最高：**模型层纯函数**（`tis.plugin.ts`）——`Item.project()` 的递归投影、`Descriptor.createNewItem` 的字段构造、`Item.propVals` 的 memoize 与副作用（问题 10）。这些是纯逻辑、容易测、且是所有 bug 的汇聚点。
   - 次高：`propValChange` 的级联 —— mock `httpPost`，断言：(a) 假值不触发；(b) 乱序响应只应用最新的一次。**这正是问题 3 的回归防线**，应当与问题 3 的修复同批交付。
   - 再次：`PluginsComponent` 的 shallow 测试 —— 断言"加载失败 → 组件仍可交互 / 错误可见"（问题 1 的回归防线）。
3. 不建议为 `item-prop-val` 的模板/控件渲染补测试：成本高、收益低，且问题 2 的拆分会让这些测试全部作废。

---

### 问题 9 · 死代码与「真假」边界不清

**已核实的位置**

| 位置 | 内容 |
|---|---|
| `plugins.component.ts:194` | `*ngIf="true \|\| showExtensionPoint.open"` —— 用析取式表达"永远为真且保留调试开关" |
| `plugins.component.ts:588` | 整体注释掉的 `getPluginMetaParam`（已迁移为自由函数） |
| `plugins.component.ts:1202` | 整体注释掉的 `createPostPayload` |
| `plugins.component.ts:1213` | 文件尾整体注释掉的 `NotebookwrapperComponent` |
| `plugins.component.ts:838-845` | `setPlugins(metas, opt?)` 的 `opt` **无人使用**，体内只剩注释掉的调用 |
| `plugins.component.ts:144-170` | `*ngSwitchCase="true"` / `"false"` —— 本质是 `*ngIf/else` 写成 switch |

**修复方案**

机械清理即可，但**分清两类**：

- **删**：注释掉的代码块（`getPluginMetaParam`、`createPostPayload`、`NotebookwrapperComponent`）、无人使用的 `opt` 形参。这些在 git history 里，删掉不丢信息。
- **改**：`true || flag` → 直接 `*ngIf` 并在旁边留一行注释说明为什么恒真（若确有调试开关需求，就把它变成真正的 `@Input`）；`*ngSwitchCase` → `*ngIf/else`。

> 保留下来的**解释性注释**（"为什么这么做"）不要动 —— 本代码库注释密度高且质量不低，这是资产。

---

### 问题 10 · 小事（可选，最后做）

**(a) `HeteroList.create()` 返回长度恒为 1 的数组**【已核实】

```ts
// tis.plugin.ts:1706
public static create(desc, pluginCategory, itemPropSetter?, updateModel?): HeteroList[] {
  ...
  Descriptor.addNewItem(h, desc, updateModel, itemPropSetter);
  return [h];                  // ← 永远只有 1 个元素（L1720）
}
```

于是每个消费者写 `hlist[0]` 或 `for` 一个永不为多的数组（`plugins.component.ts:456-464`、workshop 配置面板）。用复数类型表达单数事实。

**建议**：新增 `HeteroList.createOne()` 返回 `HeteroList`，旧方法保留 `@deprecated`。**优先级最低** —— 收益仅是类型清晰，改动面却不小（调用点分布在两个工程）。

**(b) `Item.propVals` 是带副作用、被模板读取、且失效条件不完整的 memoize getter**【已核实】

```ts
// tis.plugin.ts:1487
public get propVals(): ItemPropVal[] {
  if (this._propVals) { return this._propVals; }
  ...
  this.dspt.attrs.forEach(attr => {
    let ip = this.vals[attr.key];
    if (!ip) {
      ip = attr.addNewEmptyItemProp(this.updateModel);
      this.vals[attr.key] = ip;         // ← getter 改状态
    }
    this._propVals.push(ip);
  });
  return this._propVals;
}
```

失效只靠 `clearPropVals()`（L1477）；`dspt` / `attrs` 变化**不会**失效。

**注意**：这个 getter 是**承重的** —— 2026-09-21 新增的 `itemChanged` 能直接拿到活对象，正依赖"`prop` 即 `item.vals[key]` 本体"。所以**不要改它的行为**，只做两件事：
1. 补注释，写明"此 getter 会写入 `vals`，且缓存不随 `attrs` 失效"，防止后来者踩坑；
2. 在 `dspt` 的 setter 或 `wrapItemVals()` 中评估是否需要一并 `delete this._propVals`。

---

## 3. 执行批次与依赖顺序

```
批次 0（先决条件，可立即开始）
  └─ 问题 8.1  恢复 karma.conf.js / tsconfig.spec.json，让测试能跑
       理由：后续每条修复都需要回归防线；现在没有防线

批次 1（小修 + 高收益，互相独立，可并行）
  ├─ 问题 1A  失败分支也 reattach + 失败时可见反馈 + 封装 refresh()
  ├─ 问题 3   propValChange 序号防重 + 假值判断修正
  ├─ 问题 4甲 级联回填补通知（回调形参）
  └─ 问题 5.1 保存失败时透传服务端错误信息
       理由：都是几行到几十行的局部改动，风险低，且各自消除一个真 bug

批次 2（语义与清理，风险低）
  ├─ 问题 7   EventEmitter → Subject（源码兼容）
  ├─ 问题 6   拆分语义化输入 + 旧输入保留为过渡别名
  ├─ 问题 9   死代码清理
  └─ 问题 5.2/5.3  审计 50 个调用点后再决定 setPluginMeta 是否抛异常

批次 3（需实测）
  └─ 问题 1B  评估删除 detach()，改 OnPush + markForCheck
       前置：批次 1、2 已合并；已补问题 8.3 的 shallow 测试

批次 4（大重构，最后）
  └─ 问题 2   拆分 item-prop-val（逐控件族拆，一族一提交）
       前置：批次 1、2、3 全部完成
       理由：问题 1/3 未修之前拆分，会同时动 CD 与异步，新 bug 无从归因

批次 5（可选）
  └─ 问题 10  HeteroList.createOne；propVals 注释与失效评估
```

---

## 4. 明确不做的事

1. **不动分层**：descriptor 驱动、三层模型、`DescribleVal extends Item` 的递归、`Item.project()` 的投影边界 —— 全部保留。
2. **不动线格式**：`Item.project()` / `toPostBody()` 的 JSON 形状逐字节不变。
3. **不引入状态管理库**：不引入 NgRx/Akita。当前问题的根因是 CD 协议与异步纪律，不是状态管理缺失。
4. **不重写模板**：`ng-zorro` 的控件用法保持原样。
5. **不动后端契约**：`/coredefine/corenodemanage.ajax` 的 `render_value_change`、`save_plugin_config` 接口不变。

---

## 5. 回归验证清单

前置：`cd /Users/mozhenghua/j2ee_solution/project/tis-console && node_modules/.bin/tsc -p tsconfig.app.json --noEmit` 必须 `EXIT=0`。

> **重要**：`tsconfig.json` 的 `strictTemplates: false`，**模板里的错误 tsc 不会报**。所有模板相关改动只能靠手测。

| # | 场景 | 期望 | 覆盖问题 |
|---|---|---|---|
| 1 | 数据源配置页，正常加载 | 表单正常渲染、可交互 | 1 |
| 2 | 人为让插件配置接口失败 | **表单仍可交互 + 有可见错误**（当前：白屏死表单） | 1A |
| 3 | 增量构建 4 步流程 | 每步表单正常、可保存 | 1/2 |
| 4 | 任意带 pipe 的字段，连续快速输入 | 选项最终与**最后一次输入**一致（不出现回退） | 3B |
| 5 | 任意带 pipe 的字段，值设为 `0` | 级联**正常触发**（当前：不触发） | 3A |
| 6 | 保存时服务端返回业务错误 | 前端**能看到错误原因**（当前：只有失败信号） | 5.1 |
| 7 | `[plugins]` 传空数组 | console 有 error 提示（当前：静默） | 5.2 |
| 8 | 传入自定义 `savePlugin` 流并触发 | 能触发保存流程 | 7 |
| 9 | workshop 编辑器改 header 的 `orientation`（嵌套字段） | 画布即时刷新 | 4 |
| 10 | workshop 编辑器改一个**被级联影响**的字段 | 画布即时刷新（当前：不刷新） | 4 |
| 11 | 50 个 `<tis-plugins>` 调用点抽查 | 无渲染回归 | 全部 |

---

## 6. 关键代码位置索引

**`src/common/plugins.component.ts`**
- L134 `changeDetection: OnPush`（与 detach 矛盾）
- L144-170 `ngSwitch` 渲染分支 / L182 `ngClass` / L202 / L257（`shallInitializePluginItems` 的多种语义）
- L194 `*ngIf="true || showExtensionPoint.open"`
- L333 `shallInitializePluginItems` / L335 `useCollapsePanel`
- L362 `@Input() savePlugin: EventEmitter`（问题 7）
- L393 `@Output() itemChanged`（2026-09-21 新增）
- L456-464 `HeteroList.create()` 调用点（问题 10a）
- L465-467 `catch (e) { console.log(e); }`
- L795-799 `itemPropValChange`：级联 + itemChanged 转发
- L838-845 `setPlugins(metas, opt?)` 的废弃形参
- L852-857 `setPluginMeta` 空数组静默返回
- L868 `this.cdr.detach()`
- L894-903 `ngAfterContentInit` 两个分支
- L907-923 `initializePluginItems`（**reattach 只在成功分支**）
- L1014-1016 保存失败吞掉错误信息
- L1027-1036 `removeItem`

**`src/common/plugin/item-prop-val.component.ts`**
- L46-122 `propValChange` 级联引擎（L53-56 假值缺陷、L75 POST、L120 detectChanges）
- L126 `changeDetection: Default`
- L175-329 标量控件模板（9 个 `inputValChange` 绑定点）
- L508/L529 `changePlugin` 绑定
- L561 嵌套 `<item-prop-val>` 递归
- L880-1002 多选子插件（`descValItems` / `addMultiChildPlugin` / `removeMultiChildPlugin`）
- L1006 `ontologyPropRoleTypeLinkChange`
- L1018-1032 `itemPropValChange`（嵌套冒泡接收端，2026-09-21 改）
- L1237 `checkAndInstallPlugin` / L1283 `openPluginDialog`
- L1373 `changePlugin`（2026-09-21 补 emit）
- L1397-1401 `inputValChange`（**只 emit，不调级联**）
- L1456-1530 Router / 可选项管理
- L1531 文件上传 / L1539 全屏编辑器

**`src/common/tis.plugin.ts`**
- L165 `ItemPropVal`
- L348 `Descriptor` / L371 `wrapDescriptor` / L514 `createNewItem` / L537 `addNewItem`
- L926 `Item` / L963 `project()`
- L1477 `clearPropVals` / **L1487 `get propVals`（承重，见问题 10b）**
- L1516 `DescribleVal extends Item`
- L1681 `HeteroList` / L1706 `HeteroList.create()` 返回数组

---

## 7. 附：2026-09-21 已交付的改动（本文档的背景）

workshop 编辑器「改配置即刷新画布」的改造已落地（未提交），共 7 文件 +126 行代码（另有 139 行注释）：

| 文件 | 改动 |
|---|---|
| `common/plugins.component.ts` | 新增 `@Output() itemChanged`；`itemPropValChange` 中先级联后 emit |
| `common/plugin/item-prop-val.component.ts` | `itemPropValChange` 向上冒泡 re-emit；`changePlugin` 补 emit |
| `workshop/models/editor/editor-state.model.ts` | 三个 `update-*` 的 patch 类型放宽为 `Partial<实体>` |
| `workshop/services/module-state.service.ts` | 补 `case 'update-overlay'` + `applyUpdateOverlay` |
| `workshop/services/api/workshop-editor-api.service.ts` | `operations` 序列化补 `{kind, params}` 包装 |
| `workshop/services/editor/edit-mode.service.ts` | `pendingOps` 同 key 合并 |
| `workshop/components/editor/editor-config-panel/*` | 5 处 `(itemChanged)` 绑定 + 绑定表 + applier + 转换器 |

**这次改造顺带暴露的问题**（已收入本文档）：
- 问题 4：级联回填不 emit，`itemChanged` 看不到 —— 本次改造**未解决**，遗留。
- 问题 1：`detach()` 协议放大了"改了不刷新"的排查成本。
- 问题 7：`<tis-plugins>` 从来就缺一个"值变了"的输出，说明模型层缺少变更通知是**设计缺口**，而非后来者没找到用法。

**已知遗留（不在本文档范围，属 workshop 侧）**：
- 保存不真正落盘：服务端 `WorkshopEditorService.applyBatch` 是只校验不执行的 stub，kind 白名单只有 6 个 widget 操作。
- `EditorHistoryService.push()` 全工程无调用方，undo/redo 是死的。
- `WorkshopOverlay.variableBasedVisibility` 是普通 POJO，既不刷新也不提交流程覆盖。
