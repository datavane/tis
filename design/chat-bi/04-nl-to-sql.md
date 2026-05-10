# 04 - NL→SQL 生成与校验闭环

> **前置依赖**：03（提供检索得到的 prompt 上下文）、02（Doris 已有数据用于执行验证）。
> **后续被依赖**：05（测试方案直接调用本层 API）。

## 1. 目标

| 编号 | 目标 | 说明 |
|------|------|-----|
| Q1 | 给定 NLQ + GraphRAG 上下文，生成可执行的 Doris SQL | LLM 调用 + Doris 方言提示 |
| Q2 | 生成后做静态/动态校验，避免幻觉表/列 | AST 解析 + 子图符号集校验 |
| Q3 | 错误自动重试 ≤ 2 次 | 把错误信息回传给 LLM 修正 |
| Q4 | 输出可追溯：每次生成保留 (NLQ, prompt, sql, verify_log) | 便于 05 测试与调优 |

## 2. 流水线

```
NLQ
 │
 ▼
[Step1 检索]  ─►  GraphRAGService (03)
 │
 ▼  prompt_context + symbol_set
[Step2 拼装 Prompt]
 │
 ▼
[Step3 LLM 调用]  ─►  candidate_sql
 │
 ▼
[Step4 静态校验]  ─►  fail？回 Step3 (附 error_msg) 或最终失败
 │
 ▼
[Step5 EXPLAIN 校验 (可选)]  ─►  Doris 服务端语义校验
 │
 ▼
[Step6 落库执行 / 返回结果]
```

## 3. Prompt 拼装

### 3.1 模板（中文）

```
你是一名 Apache Doris SQL 专家。请根据下方"业务上下文"，把用户问题翻译成
一条**可在 Doris 上执行**的 SQL。仅输出 SQL 本身，不要任何解释。

## Doris 方言关键提示
- 时间字段使用 DATE / DATETIME，函数用 `date_trunc('day', col)` / `date_format`
- 分组排序使用 `GROUP BY` + `ORDER BY`，TOP-N 使用 `LIMIT`
- 不要使用 SQLite 特有函数（如 julianday）
- 字符串拼接使用 `concat(a, b)`
- 不要使用未在"相关数据表"中出现的表名/列名
- 默认对 NULL 安全：使用 `coalesce` 或 `is null`

## 业务上下文
{{ graphrag_context }}

## 用户问题
{{ nlq }}

## 输出
只输出 SQL，包裹在 ```sql ... ``` 中。
```

### 3.2 Few-shot

可选：从历史成功样本中按 NLQ embedding 召回 1~2 条 (NLQ, SQL) 对作为 in-context examples。一期可不实现。

## 4. LLM 调用

### 4.1 直接复用现有 `LLMProvider` SPI

TIS 已经在 `tis-plugin/src/main/java/com/qlangtech/tis/aiagent/llm/LLMProvider.java` 提供了大模型抽象基类，并已落地两个实现：

| 实现类 | 说明 |
|---|---|
| `com.qlangtech.tis.plugin.llm.QWenLLMProvider` | **本期默认**，阿里云通义千问，OpenAI 兼容协议，支持中文场景 |
| `com.qlangtech.tis.plugin.llm.DeepSeekProvider` | DeepSeek，备选 |

**ChatBI 直接复用，不再新建 `ChatModel` 接口**。LLMProvider 的核心方法已经覆盖我们的需求：

```java
public abstract class LLMProvider extends ParamsConfig {
    public abstract LLMResponse chat(IAgentContext context,
                                     UserPrompt prompt,
                                     List<String> systemPrompt);

    public abstract LLMResponse chatJson(IAgentContext context,
                                         UserPrompt prompt,
                                         List<String> systemPrompt,
                                         TISJsonSchema jsonSchema);

    public abstract String getProviderName();
    public abstract boolean isAvailable();

    // 用户隔离的加载入口
    public static LLMProvider load(IPluginContext pluginContext, String identityName);
}
```

返回的 `LLMResponse` 已自带：`content` / `jsonContent` / `promptTokens` / `completionTokens` / `model` / `executeLog`，正好对应我们 §7 trace 落盘需要的所有字段。

### 4.2 ChatBI 的调用片段

```java
// 在 ChatBIService 内部
LLMProvider llm = LLMProvider.load(pluginContext, /*identityName=*/ chatBiCfg.llmProviderName);
if (!llm.isAvailable()) {
    throw new IllegalStateException("LLM provider unavailable: " + chatBiCfg.llmProviderName);
}

UserPrompt userPrompt = new UserPrompt(
    /*abstractInfo=*/ "ChatBI:" + nlq,         // 摘要，用于日志检索
    /*prompt=*/       buildPromptUser(graphRagCtx, nlq)
);

List<String> systemPrompt = List.of(buildSystemPrompt());  // §3.1 中的中文系统提示词

LLMResponse resp = llm.chat(agentCtx, userPrompt, systemPrompt);

if (!resp.isSuccess()) {
    return ChatBIResult.fail(resp.getErrorMessage());
}
String candidateSql = extractSqlFromCodeBlock(resp.getContent());
```

### 4.3 配置入口

LLMProvider 实例本身就是 TIS 标准的 `ParamsConfig` 用户级隔离插件：
- 用户在 TIS 控制台配置好 `QWenLLMProvider` 实例（填 endpoint / apiKey / model / temperature 等），保存得到一个 `identityName`
- ChatBI 配置页只需要让用户**从下拉框选一个已存在的 LLMProvider 实例名**，本身不再单独维护 endpoint/key 等
- 多用户场景下，每个用户只能看到自己创建的 LLMProvider 实例（用户隔离由 `LLMProvider.getExistProviders()` 内置保证）

### 4.4 与 ChatBI 配置的拼装

新增的 `ChatBIConfig`（用户级 ParamsConfig）只挂三个核心配置：
```
llmProviderName: String     // 引用某个 LLMProvider 实例
graphragOpts:    JSON       // 03 的 RetrievalOptions（topK / maxHops / tokenBudget）
maxRetry:        Integer    // 04 §5 重试次数上限，默认 2
```
不再重复 LLM 连接参数。

## 5. 校验闭环

### 5.1 静态校验（必做）

| 校验项 | 实现 |
|--------|------|
| **SQL 可解析** | 用 Doris parser（或本仓库 `tis-sql-parser`）解析为 AST，失败即拒 |
| **表名白名单** | AST 遍历得到所有 `Table` 节点，检查是否全部属于检索阶段的 `objectTypes` |
| **列名白名单** | 检查每个 `ColumnRef` 是否属于其所在 OT 的 Property 集 |
| **JOIN 合法** | 检查每个 JOIN ON 是否能在 `linkers` 中找到对应边（按 sourceField↔targetField 模糊匹配） |
| **值约束** | WHERE 中 IN/= 的字面量与 `Enum*` 约束的允许集合比对（不命中给 warning，不拒绝） |

### 5.2 动态校验（可选）

- `EXPLAIN <sql>` 投递到 Doris，捕获语义错误
- 关键风险：执行计划本身可能很慢，需要超时控制（默认 5s）

### 5.3 错误回退

- 静态校验失败：把 `error_msg + failing_sql` 拼到 prompt 第二轮，要求 LLM 修正；最多重试 2 次
- 仍失败：返回结构化错误给前端

## 6. 接口

```java
public interface ChatBIService {
    ChatBIResult ask(String domain, String nlq, ChatBIOptions opts);
}

public record ChatBIResult(
    String sql,             // 最终 SQL，失败为 null
    QueryResult data,       // 执行结果，可选
    List<TraceStep> trace,  // 全过程日志（含 prompt、模型回复、校验日志）
    String error            // 失败原因
) {}
```

## 7. 输出可追溯

每次调用产生一条 trace（jsonl），落 `<TIS.dataDir>/chatbi/trace/<yyyy-mm-dd>/<reqId>.jsonl`，**关键字段直接源自 `LLMResponse`，无须自行封装**：

```json
{"step": "retrieve", "ots": [...], "linkers": [...], "ms": 89}
{"step": "prompt", "tokens": 1842, "system": "...", "user": "..."}
{"step": "llm", "model": "qwen-max", "promptTokens": 1842, "completionTokens": 124, "raw": "```sql\nSELECT...```", "ms": 1420}
{"step": "extract", "sql": "SELECT..."}
{"step": "validate", "ok": true, "issues": []}
{"step": "execute", "rows": 12, "ms": 230}
```

trace 既给运维排障，也为 Falcon 测试报告提供回放数据（见 05）。

## 8. 任务拆解

| 任务 | 估时 | 依赖 |
|------|------|------|
| T1 复用 `LLMProvider` SPI（默认接 `QWenLLMProvider`），新增 `ChatBIConfig` ParamsConfig | 0.5d | - |
| T2 Prompt 拼装器（系统 + 用户两段，对接 `UserPrompt`） | 0.5d | 03 |
| T3 SQL AST 校验（白名单） | 2d | 03 |
| T4 EXPLAIN 校验（可选） | 1d | T3, 02 |
| T5 重试编排 | 0.5d | T3 |
| T6 trace 落盘（复用 `LLMResponse.executeLog` + 自有 step jsonl） | 0.5d | - |
| T7 单元 + 端到端测试 | 1d | 全部 |

## 9. 验收标准

- 端到端：对预先准备的 20 条手工 NLQ，生成 SQL 并能在 Doris 执行成功率 ≥ 85%
- AST 校验：构造若干含"幻觉表名/列名"的样本，全部能被拦截
- trace：跑一次完整流程后能在文件系统看到完整 jsonl

## 10. 风险

| 风险 | 缓解 |
|------|------|
| Doris parser 缺依赖 | 第一版可先用 `tis-sql-parser` 做近似校验，覆盖率不足处用 EXPLAIN 兜底 |
| 重试造成 LLM 成本飙升 | 限定每个请求最多 2 次重试 + token 上限 |
| 用户 NLQ 涉及非本体表 | 拒绝并给出建议（"未发现 xxx，可在本体中创建"） |