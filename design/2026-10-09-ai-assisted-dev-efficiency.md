# Workshop 开发中的 AI 辅助效率提升建议

> 状态：**建议，待取舍**（本文不含代码改动）
> 建档日期：2026-10-09
> 涉及工程：
> - `/Users/mozhenghua/j2ee_solution/project/tis-console`（前端，主战场）
> - `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin`（后端 widget 实现层）
> - `/Users/mozhenghua/j2ee_solution/project/tis-solr`（抽象/核心层）
> 起因：ObjectTable Widget 强类型映射层建设（2026-10-08）过程中暴露的协作瓶颈

---

## 0. 结论先行

**瓶颈不是 AI 不够快，而是「没有任何东西能自动发现 AI 写错了」。**

Workshop 前端目前是**零自动反馈回路**的状态：唯一的自动检查是 `tsc`，而它对字段名完全失明。
于是每一轮 AI 的产出都必须由人手工核对 —— 无论模型多强，吞吐都被这一步卡死。
当前人（你）是系统里**唯一的 oracle**。

因此提高效率的第一优先级**不是引入更多 AI**，而是：

> **让代码库能自己发现问题，把人从「验证者」降级为「审阅者」。**

落地上分三梯队（第 2/3/4 节），第一梯队约一小时、三项，就能堵住最近三轮里反复出现的两类事故。

---

## 1. 现状盘点：现在能自动发现什么

| 检查通道 | 状态 | 证据 |
|---|---|---|
| `tsc -p tsconfig.app.json --noEmit` | ✅ 能跑 | 唯一有效的自动检查 |
| —— 对**字段名**错误 | ❌ 失明 | `widget.model.ts:171` 的 `[key: string]: any`；`tsconfig.json:40` `strict: false` |
| —— 对**模板**错误 | ❌ 失明 | `tsconfig.json:63` `strictTemplates: false`；模板不参与类型检查 |
| ESLint | ❌ **从未跑过** | `.eslintrc.json` 的 `extends` 缺 legacy eslintrc 要求的 `plugin:` 前缀 → 在**任何**文件上（含未改动的 `src/main.ts`）都报 `ESLint couldn't find the config "@typescript-eslint/recommended"` |
| 单元测试 | ❌ 无 | 全仓无 `karma.conf.js` / `protractor.conf.js` / cypress / playwright 配置；`node_modules/.bin` 里只有二进制、没有配置 |
| E2E | ❌ 无 | 同上 |
| 后端字段 ↔ 前端字段一致性 | ❌ 无 | 只能人工 grep（2026-10-08 那轮临时写了 Python 脚本比对，做完即弃） |

### 这张表解释了什么

最近三轮里 AI 出的错，**全部**落在这张表的空档里 —— 不是判断力问题，是没有东西能报警：

| 实际事故 | 本可拦住它的通道 | 当时状态 |
|---|---|---|
| 删掉了 `applyHttpRequest(…, 'get_data', …)` 服务端取数调用 | 一条「删调用前先读后端实现」的规则 | 无 |
| 把 type-13 的 `columns` 当纯数据数组读（`col.label` 静默 `undefined`） | 断言「归一器吃两种形态」的测试 | 无测试 |
| `col.displayName` 恒渲染子描述符名 `"Column"` 而表头不报错 | 模板类型检查 | `strictTemplates: false` |
| 未使用的 import 残留 | ESLint `no-unused-vars`（已配为 error） | lint 从未跑过 |
| id → name 迁移：`tsc` 全绿、运行时全 `undefined` | 字段名核对 | 索引签名掩盖（记录在 `widget.model.ts:159-161`） |

**同一个坑已经踩过不止一次**（id→name 那次和这次 ObjectTable 是同一类）。
`widget.model.ts` 的注释里已经把这条教训写下来了，但注释不会报警。

---

## 2. 第一梯队：约一小时，回报最大

### 2.1 修 `.eslintrc.json`（一行）

`extends` 数组里的 `@typescript-eslint/recommended` / `@typescript-eslint/recommended-requiring-type-checking`
缺 legacy eslintrc 要求的 `plugin:` 前缀，`*.html` override 里的 `@angular-eslint/template/recommended` 同样。

**这是全仓性价比最高的一行改动** —— 修完，唯一一个已经写好配置、却从未执行的静态护卫第一次能跑。

注意事项（别踩）：
- 会一次性涌出大量**既有**告警，所以**单独一个提交**，先不加 `--fix`，按规则逐条评估后再开。
- `lint` 脚本自带 `--fix`，首次运行会改写大量无关文件。首次请手动 `npx eslint <file>` 逐文件过。

### 2.2 把「扔掉的验证」变成资产

2026-10-08 那轮为验证归一化器，我用 esbuild + node 写了一个 **48 条断言**的冒烟测试，
**跑全绿之后删掉了**（`/tmp/wsmoke`）。那是纯粹的浪费 —— 它应该进仓库。

那套测试的性质很好：全部针对 `widget-config.model.ts` / `item-value.model.ts` 里的**纯函数**
（归一化器、`resolveSizingMode`、`formatColumnValue`、`toObjectTableWidget`…），
**不需要 DOM、不需要 Angular、不需要后端**。用例覆盖的也是真实的静默失效形态：
信封形态的 `columns`、恒真坏对象 `{impl: undefined, vals: {}}`、`boolean_` 尾下划线、
`dft → default` 常量名映射、`absolute` 缺 `pixels`。

建议用 **Vitest** 起（对纯 TS 接入成本最低），脚本名 `npm run test:model`。
这套东西一旦存在，模型层的每次改动都有回归保护，且它能跑在 CI 里。

### 2.3 Java ↔ TS 字段名核对脚本

2026-10-08 那轮我临时写了个 Python：从 `WorkshopWidget` / `FullDisplayWidget` / `ObjectTableWidget`
抽 `@FormField` 字段名，与 TS 侧读的键比对，结果 8/8 一致。
**这已经是一个永久工具的 80%。**

它的价值：把「后端改名 → 前端静默 `undefined`」从**运行时偶发**变成 **CI 红**。
而且这是**唯一**能自动 sweep 全部 21 个 widget 的手段。

落地要求：
- 不要对 ObjectTable 硬编码 —— 遍历 widget 包下的实现类 + 抽象基类，解析继承链上的 `@FormField`。
- 注意已被注释掉的 `@FormField`：`WorkshopWidget.sortOrder` 就是这种情况（字段在、`@FormField` 被注释），
  它不进表单也不随 `Item` 下发，脚本必须能区分「有字段」和「进表单」。
- 放在 `tis-console` 侧还是独立脚本仓库，取决于你希望它进哪条 CI。

### 2.4 在 `CLAUDE.md` 加一条针对性规则

> **删除前端已有的调用或字段之前，必须先读对应后端实现、确认其副作用，并在回复里说明。**

上一轮 `applyHttpRequest` 被删就是这个规则的缺失代价 —— 当时我只看了前端那个空的 `.then` 体，
没读后端 `httpProcess`，而它其实执行了一次真实的服务端查询。

一条规则换一次事故，很划算。同类可加的还有一条：
> 声称「无法验证」时，必须同时给出「需要什么才能验证」。

（2026-10-08 那轮的正确做法就是把 esbuild 冒烟测试写出来，而不是直接认输。）

---

## 3. 第二梯队：一两天，但改变协作形态

### 3.1 一个 headless E2E（Playwright），2–3 条 workshop 冒烟用例

**这是「提高效率」里真正的大头。**

它一旦存在，「绑定 `objectSetVar` → 列立即出现」这类断言就不再需要人开浏览器：
**从「每个改动的验证者」变成「绿灯变更的审阅者」** —— 这是数量级的差别。

**可行性已核实**（2026-10-08）：后端其实一直活着。
- TIS 启动器 PID 43700（`StartTISWeb,testStart`，Jetty 12.1.7）监听 `*:8080` / `*:50000`
- `http://localhost:8080/tjs/` → **200**（`/tjs` 是 console 的 context path，见 `proxy.conf.json`）
- Angular dev server `localhost:4200` → **200**（仅绑 IPv6 `[::1]:4200`；`127.0.0.1:4200` **不通**，写脚本时注意）

所以这不是空想。老实说它需要处理登录/会话建立，**一两天才能拿到第一条全绿路径**，但值得。

优先覆盖的用例（都是历史上真出过问题的点）：
1. 绑定 `objectSetVar` → 列立即出现（同时验证 `valueChangePipe` 的实体回填）
2. 表头显示后端 `label` 而非 `"Column"`；单元格有值（`number` / `date` / `boolean_` 各一列）
3. 点行 → `activeObjectVar` 变量被写入
4. 高度设为「固定像素」→ 滚动区高度随之变化

### 3.2 其余 20 个 widget 的同类扫荡，做成并行 fan-out

ObjectTable 那轮已经把模式立住了，可以照抄：

```
投影接口（无索引签名）  →  归一化器  →  resolveXxx(widget) 单入口  →  grep 审计
```

其余 20 个的错位是**同一个 bug 类**（前端读后端不存在的字段名，或读到嵌套信封当纯数据），
且归一化器已经可复用。这是最典型的可并行任务：每个 widget 一个 agent、同一份验收标准、
同一份检查清单。**现在做，边际成本远低于第一轮。**

已登记的清单见 ObjectTable 计划的「后续（本轮登记不改）」表，其中确认的幻影字段名包括：
- `object-list.component.ts:43` 的 `config.displayProperties` → 后端是 `cardFields`；`prop.key` → 元素字段是 `prop`
- `object-list` 的 `ObjectListDisplay`（Grid/List）从未被读取
- `property-list` 的本地 `PropertyDef` 重复定义并用 `antdToken` 把 `boolean_` 剥成 `boolean`，与 `ColumnFormat` 口径分叉

---

## 4. 第三梯队：结构性

### 4.1 继续收缩索引签名

`widget.model.ts:171` 的 `[key: string]: any` 是上述所有静默失效的**根因**。
但**不要**一次性收敛成联合类型 —— 21 个组件一起动风险太高。

正确做法是已经在用的那个：**无索引签名的投影接口**（如 `ObjectTableWidget`）。
后端改名时，构造它的函数会编译报错 —— 这正是索引签名做不到的。
**每转一个 widget 就少一块盲区，可增量推进。**

### 4.2 把线格式的不变量写成可执行断言，而不只是写成注释

这几轮反复被重新推导的知识（散落在 memory 与代码注释里）：

- GET / POST 两套线格式的嵌套层数互逆（叶子包 `_primaryVal`、嵌套描述符包 `descVal`）
- type-13 字段在实体上是 `{impl, displayName, vals}[]`，不是纯数据数组
- `ItemPropVal.primary` 对未赋值的 `_primaryVal` 回落成 `''`，而空串是真值
- `dftVal` 靠**逐字匹配**子类 `getDisplayName()`；关闭状态的约定值是 `"off"`
- 枚举字段的选项 label 取**常量名**而非 `label` 字段
- `boolean_` 尾下划线是 Java 关键字所迫，不能被 `antdToken` 剥掉

其中**可检验的那部分**（归一化器的入参形态）写成测试，就从「会腐坏的注释」变成「会报警的知识」。
这是 2.2 那套测试的自然延伸。

---

## 5. 协作方式建议（针对人机配合，非代码）

- **继续给原始 dump。** 2026-10-08 那轮直接把 `console.log(config.formItem)` 的约 1500 行 JSON
  贴出来，是整个对象表轮次里**最有效的一次输入** —— 它把「线格式到底长什么样」从猜测变成了事实。
  凡是能 dump 的都 dump，比文字描述线格式强得多。
- **把验收标准提前说出来**（「我会在浏览器里点 X 看 Y」）。AI 才能朝着它建验证手段，
  而不是写完才说「无法自动验证」。
- **AI 说「无法验证」时，追问「那需要什么才能验证」。** 上一轮的正确答案是把冒烟测试写出来。
- **大轮次里给出明确的「不做什么」边界。** 2026-10-08 那轮你定的三个决策
  （值形态投影 / 先不做多选 / 只修 ObjectTable）让轮次没有跑偏；
  唯一出事的那处恰好是 AI 在批准范围之外自己下的判断。
- **对「删除」类改动要求二次确认。** 删除比新增危险得多 —— 新增错了通常看得见，
  删除错了往往几个月后才发现（`applyHttpRequest` 就是典型）。

---

## 6. 建议的落地顺序

| 优先级 | 项 | 成本 | 收益 |
|---|---|---|---|
| P0 | 修 `.eslintrc.json`（2.1） | 一行 | 解锁唯一现成的静态护卫 |
| P0 | `CLAUDE.md` 加两条规则（2.4） | 十分钟 | 直接防住上一轮的事故类型 |
| P0 | Java↔TS 字段名核对脚本（2.3） | 半天 | 21 个 widget 的唯一自动 sweep 手段 |
| P1 | Vitest + 模型层测试（2.2） | 半天 | 模型层改动有回归保护（先移植那 48 条断言） |
| P1 | 20 个 widget 并行扫荡（3.2） | 一两天 | 清掉剩余的同类静默失效 |
| P2 | Playwright E2E（3.1） | 一两天 | **协作形态改变**：人从验证者变审阅者 |
| P2 | 索引签名增量收缩（4.1） | 持续 | 逐块消灭盲区 |
| P3 | 线格式不变量测试化（4.2） | 持续 | 把注释变成会报警的知识 |

**先做 P0 三项**：加起来不到一小时，就能堵住最近三轮反复出现的两类事故。

---

## 附：本文引用的核实结论（2026-10-08 实测）

- `npx eslint <任意文件>` 失败；用**未改动**的 `src/main.ts` 复现同样失败 → 确认 lint 是**既有问题**，非本轮引入
- `tsconfig.json`：`:40` `"strict": false`、`:63` `"strictTemplates": false`
- `widget.model.ts`：`:171` 索引签名、`:159-161` id→name 迁移事故记录
- 无测试运行器：全仓无 `karma.conf.js` / `protractor.conf.js` / `cypress.config.*` / `playwright.config.*`
- 后端可达：`localhost:8080/tjs/` → 200；dev server `localhost:4200` → 200（仅 IPv6）
- 字段名核对：`WorkshopWidget`(name/title) + `FullDisplayWidget`(displayConfig)
  + `ObjectTableWidget`(objectSetVar/columns/pageSize/activeObjectVar/selectedObjectsVar) = 8 个 `@FormField`，
  与前端读的 8 个键**逐字一致**
