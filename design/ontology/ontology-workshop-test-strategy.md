# Ontology Workshop 可持续测试方案

> 状态：设计草案 · 2026-10-25
> 关联：`ontology-roadmap.md`（能力拓展路线图）、`chat-bi/`（ChatBI 设计）
> 目标读者：开发 / 维护 Ontology Workshop 的工程师

---

## 1. 背景与问题定义

Ontology Workshop 是一个高度可配置的实体体系（Module → Page → Section → Widget 四层），
配置通过 TIS 的 Describable/Descriptor 插件机制在**前端 Angular 表单**与**后端 Java 模型**之间
双向流转。这条链路横跨三个独立仓库：

| 层次 | 仓库 | 职责 |
|---|---|---|
| 抽象/核心层 | `tis-solr`（`tis-plugin`） | 接口、抽象基类、`HeteroEnum` 注册、Groovy 桥接 |
| 具体实现层 | `plugins`（`tis-ontology-plugin`） | Widget 实现类、`.json` 资源、Describable 模型 |
| 前端 | `tis-console`（`ng-tis`） | 编辑器、组件、投影/归一化层 |

### 1.1 核心难点：静默失效（Silent Failure）

常规 CRUD 应用的 bug 有明确的失败形态（异常、报错、崩溃）。但 Ontology Workshop 的 bug
**绝大多数不报错、不崩溃，只是功能悄悄不工作**：

| 静默失效形态 | 典型后果 | 记忆契约示例 |
|---|---|---|
| 前后端字段错位 | 前端读后端不存在的字段，恒 `undefined` | `project_widget_frontend_backend_field_mismatch`（实测 9 处错位） |
| 枚举常量名大小写不一致 | 枚举反序列化失败、默认项丢失 | `project_describable_get_post_wire_format_inverse` |
| `dftVal` 匹配不上 | 默认项静默落到错误子类 | `project_describable_field_default_via_displayname` |
| `{impl,vals}` 线格式包错层级 | 保存静默取不到值 | `project_describable_get_post_wire_format_inverse` |
| `elementCreator` 缺 key | 前端 `buildMultiSelectedAttr` 落到 default 抛错，整张表单打不开 | `project_multi_selectable_tuple_contract` |
| Describable 嵌套成环 | 表单序列化 `StackOverflowError` | `project_self_referential_describable_form_recursion` |

这类 bug 的特点：**编译期查不出、运行期不报错、单元测试常规断言也抓不住**，只能在真实 UI 里
凭肉眼发现。因此测试资产必须围绕「**契约**」而不是「行为」组织。

### 1.2 测试方案的目标

1. **可持续**：测试被 CI 持续跑，而不是"写了但没人跑"。
2. **覆盖完备**：每一条已知的静默失效契约，都对应一个可执行断言。
3. **防假绿**：扫到 0 个目标（0 个子类、0 条契约）必须失败，而不是"一直绿但什么都没查"。

---

## 2. 现状盘点

### 2.1 后端已有资产（较成熟）

`plugins/tis-ontology-plugin/src/test/java/com/qlangtech/tis/plugin/ontology/` 下已有 **27 个测试类**，
沉淀出三套可复制的范式：

| 范式 | 代表类 | 依赖 TIS 运行期 | CI 友好度 |
|---|---|---|---|
| **结构契约**（sezpoz 枚举类 + classloader 读 json + 反射 descriptor） | `TestWidgetFieldStructure`、`TestWorkshopConfigPlugins`、`TestWidgetVariableContract` | 否 | ✅ |
| **线格式往返**（`{impl,vals}` → parse → newInstance） | `TestWorkshopPageFormRoundTrip` | 是（需 `--add-opens`） | ⚠️ 已配 argLine |
| **跨工程契约核对**（读前端源码 grep 字段名） | `TestWidgetFieldStructure#everyWidgetConfigInterfaceMirrorsBackendFormFields`、`TestWorkshopConfigPlugins#rowEditorWireFormatKeysMatchFrontend` | 否 | ✅ |

共享工具 `TisPluginFormTestSupport`（`workshop/TisPluginFormTestSupport.java`）已经沉淀
「扫 sezpoz 索引 → 反射实例化 descriptor → classloader 读 .json → 跨工程 grep 前端源码」四大手法，
是后续所有契约测试的公共底座。

关键环境前提已就绪：`plugins/pom.xml:184-191` 已配置 `--add-opens java.base/java.lang=ALL-UNNAMED`，
线格式往返测试（依赖 TIS 运行期）在 CI 里可直接跑。

### 2.2 前端几乎为零

| 项 | 现状 |
|---|---|
| workshop 目录 spec 数 | **0** |
| 全工程 spec 数 | **1**（`offline/workflow.schedule.config.component.spec.ts`） |
| `karma.conf.js` | 空文件 |
| `src/test.ts` | 老版 `zone.js/dist/zone-testing`（未随 Angular 升级更新） |

前端是静默失效的高发区（`widget.model.ts` 的 `[key: string]: any` 索引签名让「读不存在字段」
既不过编译也不过运行），却是零覆盖。

### 2.3 CI 现状

| 仓库 | 现状 |
|---|---|
| `plugins` | 仅有 `codeql-analysis.yml`，无测试 job |
| `tis-console` | `main.yml` 是 aot-build 骨架，但 build 命令被注释成 `ng --version`（半残） |
| `tis-solr` | `.github/workflows/main.yml` 空文件 |

> CI 平台判断：三仓库远程均为 gitee（`git@gitee.com:qlangtech/*.git`），但都跟踪 `.github/workflows`
> 且含 codeql（GitHub 专属），按「gitee 主仓 + GitHub Actions 跑 CI」设计。若实际跑 gitee 自研流水线，
> CI 脚本格式需换成 Gitee Go / `.workflow/` 流水线，见 §6.3。

---

## 3. 测试分层策略（金字塔）

```
        ┌────────── L5 端到端 (Playwright，少量，真实浏览器) ──────────┐
        │   ┌──── L4 前端契约/组件测试 (Jest，新建) ────┐              │
        │   │  ┌── L3 跨工程契约核对 (已有，扩展覆盖) ──┐ │              │
        │   │  │ ┌ L2 线格式往返 (已有，扩展到4类实体) ┐ │ │              │
        │   │  │ │ ┌── L1 结构契约 (已有，收敛成契约树) ┐│ │ │             │
        │   │  │ │ │     L0 编译/AOT 门禁 (即写即挡)    ││ │ │             │
```

### L0 — 编译/AOT 门禁（成本最低，最先建）

- **后端**：`mvn -pl tis-ontology-plugin -am compile`。
- **前端**：`npm run build`（AOT 编译）。这是唯一能挡住「前端读不存在字段」的手段——
  `widget.model.ts` 的索引签名让 TS 编译静默放过，只有 AOT + 严格类型能拦
  （见 `project_workshop_onpush_async_cd_contract`：tsc 查不出，须跑 ng build AOT）。

### L1 — 结构契约测试（已有，动作是「收敛 + 全量覆盖」）

复用 `TisPluginFormTestSupport` 的手法，对 4 类实体（Module/Page/Section/Widget）统一扫描。
关键纪律：

1. **收敛**：把散落的 3 个测试类统一到同一套扫描底座，扩展 `TisPluginFormTestSupport` 而非复制。
2. **哨兵护栏**：扫到 0 个子类直接 fail（`concreteSubclassesOf` 已实现），杜绝假绿。
3. **欠账**：`TestWidgetFieldStructure#noCycleInDescribableNesting` 的注释已点明「覆盖范围收窄在
   widget.impl」，需放宽到 4 类实体（前提：identity 迁移的断言重建完成）。

### L2 — 线格式往返（已有，扩到 4 类实体）

`TestWorkshopPageFormRoundTrip` 只测了 Page。需为 Module / Section / Widget 各建一个 round-trip，
样本**刻意取非默认值**（默认值会掩盖字段丢失——该测试类注释已点明）。这是钉住
`{impl,vals}` 线格式、`processPreSaved` 保存钩子等契约的唯一手段。

### L3 — 跨工程契约核对（已有，是静默失效主战场）

`everyWidgetConfigInterfaceMirrorsBackendFormFields` 已证明「后端 `@FormField` 集合 ↔ 前端 interface
集合双向相等」极其有效（一上来揪出 9 处错位）。扩展方向：

1. **扩到嵌套结构块**（`MetricConfig` / `WidgetColumnConfig` / `ChartLayer` / `AxisConfig` /
   `ObjectListDisplay`…）——该测试类注释明确说「刻意不覆盖」，是当前最大盲区。
2. **枚举契约**（`project_enum_field_label_json_override`、`project_plugin_action_routing_contract` 的
   `type` 三族 token）用同样手法钉住。

### L4 — 前端契约/组件测试（从零建，重点投入）

1. **工具换 Jest**（Angular 官方已转投，比老 Karma 快、好配），替换空 `karma.conf.js` 和老 `test.ts`。
2. **优先测「投影/归一化」层而非 DOM**：`entity-form.model.ts#toFormFieldValue`、`widget.model.ts`
   归一化器、`tis.plugin.ts` 的 `dftVal` 匹配、`buildPluginActionParams`——这些纯函数是后端线格式的
   镜像，最易单测、也最易静默错。DOM 交互测试放最后。
3. **反向哨兵**：后端测试读前端（L3），前端测试也应反向断言后端契约，两边各留一份哨兵，任一改坏都红。

### L5 — 端到端（Playwright，少量冒烟）

只覆盖最关键用户旅程，不测视觉细节：

- 打开 `ontology/test_qq` → 进 workshop edit → 拖一个 widget → 改配置 → 保存 → 刷新后配置还在
  （直接验证 `project_workshop_page_snapshot_persistence` 的整对象快照链路）。
- 每类 widget 一条「能打开表单、能保存、能回填」冒烟。

---

## 4. 契约清单机制（核心资产）

### 4.1 原则

memory 里每条 `project_*` 契约，都应翻译成**一个可执行断言 + 一份哨兵护栏**。契约清单不是一份
会漂移的 markdown，而是「可追踪、可自检」的活文档。

### 4.2 契约清单（人类可读导航表）

落点：`plugins/tis-ontology-plugin/src/test/java/com/qlangtech/tis/plugin/ontology/workshop/contracts/contracts.md`

覆盖状态四态：`✅ 已覆盖` / `⚠️ 部分覆盖` / `❌ 未覆盖` / `🔨 待建测试`。

（下表为**节选示例**，完整 30+ 条在落地时从 memory 索引逐条筛选翻译。）

| 契约名 | 静默失效症状 | 层 | 对应测试类 | 覆盖 |
|---|---|---|---|---|
| `describable_get_post_wire_format_inverse` | `{impl,vals}` 层数包错，保存静默取不到值 | L2 | `TestWorkshopPageFormRoundTrip` | ⚠️ 仅 Page |
| `widget_frontend_backend_field_mismatch` | 前端读后端不存在字段，恒 `undefined` | L3 | `TestWidgetFieldStructure#everyWidgetConfigInterfaceMirrorsBackendFormFields` | ✅ 顶层 / ❌ 嵌套块 |
| `multi_describle_plugin_viewstyle_contract` | 子表单风格/列宽/滚动静默失效 | L1 | `TestWidgetFieldStructure` | ✅ |
| `self_referential_describable_form_recursion` | 表单序列化 StackOverflowError | L1 | `TestWidgetFieldStructure#noCycleInDescribableNesting` | ⚠️ 仅 widget.impl |
| `enum_field_label_json_override` | 枚举 label 恒为常量名、中文丢失 | L1 | — | ❌ |
| `plugin_action_routing_contract` | `type` 三族 token 拼错静默转 SSE | L3 | — | ❌ |
| `onestep_save_nosavestore_hook` | 子步骤保存不落盘 | L2 | — | ❌ |
| `workshop_page_snapshot_persistence` | 保存走整对象快照、差集删除 | L5 | — | 🔨 |

### 4.3 契约自检测试（防清单漂移）

新建 `WorkshopContractInventoryTest`：断言契约清单里标记「已覆盖」的每一行，其「对应测试类」
必须真实存在且能被 classloader 加载；扫到 0 条契约直接 fail。

> 复用 `TisPluginFormTestSupport` 的反射能力做「类存在性」校验，不新造轮子。

---

## 5. CI 接入

### 5.1 plugins 仓库

新建 `.github/workflows/ontology-workshop-test.yml`：

- 运行 `mvn -pl tis-ontology-plugin -am test`（`--add-opens` 已由 pom argLine 承担）。
- **关键**：L3 跨工程测试需要前端源码在场。用 `actions/checkout` 把 `ng-tis` checkout 到
  `RUNNER_WORKSPACE/tis-console`（注意 GITHUB_WORKSPACE 边界，用绝对路径拼），使
  `FRONTEND_DIR_CANDIDATES` 命中；前端缺席时这些测试会被 `Assume` 静默跳过，恰是它们要防的「假绿」。

### 5.2 tis-console 仓库

修好半残的 `main.yml`：把被注释的 `run: ng --version` 恢复成真正的 `npm run build`（AOT 门禁）。

### 5.3 tis-solr 仓库

可选的 core 层 job：跑 `tis-plugin` 下 ontology 相关单测（`OntologyObjectTypeTest`、
`TestDataSourceQueryCriteria`）。优先级低，本阶段先挂空壳或跳过。

---

## 6. 优先级与路线图

### 6.1 推荐落地顺序

```
Phase 0（本阶段）: 契约清单 + CI 骨架
     │  契约清单(contracts.md) + 契约自检测试 + plugins CI + 修 tis-console AOT
     ▼
Phase 1: 后端 L1/L3 扩展
     │  L1 收敛到统一底座 + 放宽到4类实体；L3 扩到嵌套结构块
     ▼
Phase 2: 后端 L2 扩展
     │  Module/Section/Widget 的 round-trip
     ▼
Phase 3: 前端 L4
     │  换 Jest + 测投影/归一化层
     ▼
Phase 4: 端到端 L5
     │  Playwright 冒烟
     ▼
Phase 5: 持续回填
        每踩一个新坑 → 更新契约清单 + 补测试 + 防回退
```

理由：L0/L1/L3 全是「不依赖运行期、CI 好跑、已有成熟范式」，投入产出比最高，能立刻把
memory 里已踩过的坑钉死防回退；前端 Jest 和 E2E 是新基建，前期投入大，放后面。

### 6.2 本阶段（Phase 0）明确不做

不扩到嵌套结构块（L3 扩面）、不建 Module/Section/Widget round-trip（L2 扩面）、不搭前端 Jest、
不写 Playwright——这些是契约清单标记 `🔨` 后按优先级在后续阶段逐个落地。本阶段只交付「清单 + 骨架」。

### 6.3 待确认前提

- **CI 平台**：若 CI 实际跑 gitee 自研流水线（`.workflow/` 目录），需把 `.github/workflows` 换成
  Gitee Go 格式（tis-solr 下有 `pipeline-20230422.yml` 可参考）。
- **前端 checkout 边界**：L3 跨工程测试在 CI 里如何拿到 `ng-tis` 源码（checkout 兄弟仓 vs 镜像子模块），
  需在 Phase 0 落地时确认。

---

## 7. 验证方式

- **契约清单自检**：`mvn -pl tis-ontology-plugin -am test -Dtest=WorkshopContractInventoryTest`
  （工作目录 `plugins`），确认自检通过、且覆盖状态与 27 个现有测试类的真实对应关系一致。
- **后端契约测试整体**：`mvn -pl tis-ontology-plugin -am test` 全绿（含依赖运行期的 L2 往返测试）。
- **CI 干跑**：push 到 plugins 分支触发 workflow，确认 job 绿、且 L3 跨工程测试**真的跑了**
  （而非被 Assume 跳过）——检查日志里 `TestWidgetFieldStructure` 的镜像对齐断言有执行。
- **前端 AOT**：tis-console 跑 `npm run build` 通过。

---

## 附：关键文件索引

| 文件 | 作用 |
|---|---|
| `plugins/tis-ontology-plugin/src/test/.../workshop/TisPluginFormTestSupport.java` | 契约测试公共底座（sezpoz/反射/json/跨工程 grep） |
| `plugins/tis-ontology-plugin/src/test/.../workshop/widget/TestWidgetFieldStructure.java` | 结构契约 + 前端镜像对齐（范式参照） |
| `plugins/tis-ontology-plugin/src/test/.../workshop/TestWorkshopPageFormRoundTrip.java` | 线格式往返（范式参照，仅 Page） |
| `plugins/pom.xml:184-191` | `--add-opens` argLine（L2 在 CI 可跑的前提） |
| `tis-console/src/workshop/components/editor/editor-shell/editor-shell.component.ts` | 编辑器入口（E2E 锚点） |
| `tis-console/src/base/ontology.detail.component.ts` | ontology 入口（E2E 锚点） |