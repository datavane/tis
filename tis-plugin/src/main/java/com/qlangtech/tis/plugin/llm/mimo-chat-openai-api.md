# MiMo Chat Completions API 说明文档（OpenAI 兼容）

> 文档来源：https://mimo.mi.com/docs/zh-CN/api/chat/openai-api
> MiMo-TTS 系列模型的协议文档请参考 [语音合成（MiMo-TTS 系列）- OpenAI API 兼容](https://mimo.mi.com/docs/zh-CN/api/audio/tts)。

MiMo 的 Chat Completions 接口与 OpenAI 协议兼容，下文为请求参数、响应格式及示例的完整说明。

---

## 1. 请求地址

```bash
POST https://api.xiaomimimo.com/v1/chat/completions
```

---

## 2. 请求头

接口支持以下两种认证方式，请选择其中一种添加到请求头中：

**API Key 鉴权**

```json
api-key: $MIMO_API_KEY
Content-Type: application/json
```

**Bearer 鉴权**

```json
Authorization: Bearer $MIMO_API_KEY
Content-Type: application/json
```

---

## 3. 请求体参数

| 参数 | 类型 | 必选 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `messages` | array | 是 | - | 对话的消息列表。 |
| `model` | string | 是 | - | 用于生成响应的模型 ID。可选值：`mimo-v2.5-pro`、`mimo-v2.5`。 |
| `frequency_penalty` | number \| null | 否 | `0` | 取值范围 `[-2.0, 2.0]`。正值会根据 token 在已有文本中的出现频率施加惩罚，降低模型重复相同内容的可能性。 |
| `max_completion_tokens` | integer \| null | 否 | `mimo-v2.5-pro` 默认 `131072`；`mimo-v2.5` 默认 `32768` | 补全中可生成 token 数的上限（含可见输出 token 与推理 token）。范围 `[1, 131072]`。 |
| `presence_penalty` | number \| null | 否 | `0` | 取值范围 `[-2.0, 2.0]`。正值会根据 token 是否已出现施加惩罚，增加谈论新主题的可能性。 |
| `response_format` | object | 否 | - | 指定模型必须输出的格式。 |
| `stop` | string \| array \| null | 否 | `null` | 最多 4 个序列，生成到这些序列时停止。返回文本不含这些停止序列。 |
| `stream` | boolean \| null | 否 | `false` | 设为 `true` 时，响应通过 SSE（server-sent events）流式传输。 |
| `thinking` | object | 否 | `mimo-v2.5-pro` / `mimo-v2.5` 默认 `enabled` | 控制模型是否启用思维链。 |
| `temperature` | number | 否 | `1.0` | 采样温度，范围 `[0, 1.5]`。较高值更随机，较低值更确定。建议与 `top_p` 二选一调整。 |
| `tool_choice` | string | 否 | `auto` | 控制模型如何选择工具。可选值：`auto`。（非 `auto` 值可能被后端移除，行为等同 `auto`。） |
| `tools` | array | 否 | - | 模型可能调用的工具列表，目前仅支持函数作为工具。 |
| `top_p` | number | 否 | `0.95` | 核采样概率阈值，范围 `[0.01, 1.0]`。 |

### 3.1 `messages` 消息对象

`messages` 为消息对象数组，消息按角色区分，支持以下类型：

- **Developer message（开发者消息）**：开发者提供的指令，模型应遵循而不受用户消息影响。
  - `messages.role`：必选，可选值 `developer`。
  - `messages.content`：必选，string 或 array（内容片段数组）。
  - `messages.name`：可选，参与者名称。
- **System message**：系统消息。
  - `messages.role`：必选，可选值 `system`。
  - `messages.content`：必选，string 或 array。
  - `messages.name`：可选。
- **User message**：用户消息。
  - `messages.role`：必选，可选值 `user`。
  - `messages.content`：必选，string 或 array（可含文本、图像等内容片段）。
  - `messages.name`：可选。
- **Assistant message**：助手历史消息。
  - `messages.role`：必选，可选值 `assistant`。
  - `messages.content`：必选，string 或 array。
  - `messages.name`：可选。
  - `messages.tool_calls`：可选，工具调用记录，用于多轮工具调用对话。
- **Tool message**：工具返回结果。
  - `messages.role`：必选，可选值 `tool`。
  - `messages.content`：必选。
  - `messages.tool_call_id`：必选，对应的工具调用 ID。

### 3.2 `response_format` 说明

- `response_format.type`：必选，所定义的响应格式类型。当前支持 `text`（默认文本响应）。
- 也支持 `json_object`（结构化 JSON 输出）模式。

### 3.3 `thinking` 说明

- `thinking.type`：必选，是否启用思维链。可选值 `enabled`、`disabled`。`mimo-v2.5-pro` / `mimo-v2.5` 默认 `enabled`。

> **注意**：思考模式下多轮工具调用时，模型会在返回 `tool_calls` 的同时返回 `reasoning_content`。若要继续对话，建议在后续每次请求 `messages` 中保留所有历史 `reasoning_content`，以获得最佳表现。

> **注意**：在思考模式下，`mimo-v2.5-pro`、`mimo-v2.5` **不支持**自定义 `temperature` 和 `top_p`。即使传入，实际生效值由模型强制采用推荐默认值（`temperature=1.0`、`top_p=0.95`）。

### 3.4 `tools` 工具说明

- `tools.type`：必选，工具类型。目前仅支持 `function`。
- `tools.function`：必选，函数工具对象。
  - `tools.function.name`：必选，函数名称。由 `a-z`、`A-Z`、`0-9`、下划线 `_`、连字符 `-` 组成，最大长度 64。
  - `tools.function.description`：可选，函数功能描述。
  - `tools.function.parameters`：可选，以 JSON Schema 形式描述参数。若省略表示无参数。
  - `tools.function.strict`：可选，默认 `false`。为 `true` 时模型严格遵循 `parameters` 定义的模式。

---

## 4. 响应格式

### 4.1 Chat 响应对象（非流式输出）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 响应的唯一标识符。 |
| `object` | string | 对象类型，仅为 `chat.completion`。 |
| `created` | integer | 对象创建时的 Unix 时间戳（秒）。 |
| `model` | string | 用于生成结果的模型。 |
| `choices` | array | 生成的回复选项列表。 |
| `usage` | object \| null | 该请求的用量信息。 |

**`choices` 子字段**

- `choices.index`：integer，选项索引。
- `choices.finish_reason`：string，停止生成的原因。
  - `stop`：到达自然停止点或停止序列。
  - `length`：达到最大 token 数。
  - `tool_calls`：模型调用了工具。
  - `content_filter`：内容被过滤策略拦截。
  - `repetition_truncation`：模型检测到复读。
- `choices.message`：object，模型生成的对话补全消息。
  - `choices.message.content`：string，消息内容。
  - `choices.message.reasoning_content`：string，最终答案之前的推理内容。
  - `choices.message.role`：string，消息作者角色。
  - `choices.message.tool_calls`：array，工具调用对象。
    - `tool_calls.id`：string，工具调用 ID。
    - `tool_calls.type`：string，工具类型，仅 `function`。
    - `tool_calls.function.name`：string，函数名称。
    - `tool_calls.function.arguments`：string，调用参数（JSON 字符串，可能非合法 JSON，需自行校验）。
  - `choices.message.annotations`：array，联网搜索后返回的引用注释。
    - `annotations.logo_url` / `site_name` / `publish_time` / `summary` / `title` / `type` / `url`。
  - `choices.message.error_message`：string，联网搜索的错误信息。

**`usage` 子字段**

- `usage.completion_tokens`：integer，输出 token 数。
- `usage.prompt_tokens`：integer，提示词 token 数。
- `usage.total_tokens`：integer，总 token 数（prompt + completion）。
- `usage.completion_tokens_details.reasoning_tokens`：integer，推理生成 token 数。
- `usage.prompt_tokens_details.cached_tokens`：integer，命中缓存 token 数。
- `usage.prompt_tokens_details.audio_tokens` / `image_tokens` / `video_tokens`：integer，对应模态输入 token 数。
- `usage.web_search_usage.tool_usage`：integer，联网搜索 api 调用次数。
- `usage.web_search_usage.page_usage`：integer，联网搜索返回的网页数。

### 4.2 Chat 响应 chunk 对象（流式输出）

当 `stream=true` 时，每个数据块（SSE）结构如下：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 对话补全对象唯一标识符（各 chunk 相同）。 |
| `object` | string | 对象类型，仅为 `chat.completion.chunk`。 |
| `created` | integer | 创建时间戳（各 chunk 相同）。 |
| `model` | string | 用于生成结果的模型。 |
| `choices` | array | 增量回复选项列表。 |
| `usage` | object \| null | 最终 chunk 携带的用量信息。 |

**`choices` 子字段**

- `choices.index`：integer，选项索引。
- `choices.finish_reason`：string \| null，停止原因（含义同非流式）。
- `choices.delta`：object，补全增量内容。
  - `delta.content`：string，内容增量。
  - `delta.reasoning_content`：string，推理内容增量。
  - `delta.role`：string，消息作者角色。
  - `delta.tool_calls`：array，工具调用增量。
    - `tool_calls.index`：integer，工具在列表中的索引（从 0）。
    - `tool_calls.id` / `type` / `function.name` / `function.arguments`。
  - `delta.annotations`：array，联网搜索引用注释增量。

---

## 5. 调用示例

### 5.1 基础调用（非流式）

```bash
curl --location --request POST 'https://api.xiaomimimo.com/v1/chat/completions' \
--header "api-key: $MIMO_API_KEY" \
--header "Content-Type: application/json" \
--data-raw '{
    "model": "mimo-v2.5-pro",
    "messages": [
        {
            "role": "system",
            "content": "You are MiMo, an AI assistant developed by Xiaomi. Today is date: Tuesday, December 16, 2025. Your knowledge cutoff date is December 2024."
        },
        {
            "role": "user",
            "content": "please introduce yourself"
        }
    ],
    "max_completion_tokens": 1024,
    "temperature": 1.0,
    "top_p": 0.95,
    "stream": false,
    "stop": null,
    "frequency_penalty": 0,
    "presence_penalty": 0,
    "thinking": {
        "type": "disabled"
    }
}'
```

**响应示例**

```json
{
    "id": "8b51f9e0515949cb8207fbd35ea6ea5c",
    "choices": [
        {
            "finish_reason": "stop",
            "index": 0,
            "message": {
                "content": "Hello! I'm MiMo, Xiaomi's AI assistant created by the Xiaomi LLM-Core team. I'm here to chat, help answer questions, and assist with various tasks—whether it's providing information, brainstorming ideas, or just having a friendly conversation. Feel free to ask me anything, and I'll do my best to help! 😊",
                "role": "assistant",
                "tool_calls": null
            }
        }
    ],
    "created": 1776848906,
    "model": "mimo-v2.5-pro",
    "object": "chat.completion",
    "usage": {
        "completion_tokens": 72,
        "prompt_tokens": 57,
        "total_tokens": 129,
        "completion_tokens_details": {
            "reasoning_tokens": 0
        },
        "prompt_tokens_details": null
    }
}
```

### 5.2 流式响应

将 `"stream": true`，响应以 SSE 逐块返回 `chat.completion.chunk` 对象，最后一个有效 chunk 通常携带 `usage` 字段与 `finish_reason`。

### 5.3 函数调用 / 工具调用

在请求 `tools` 中声明函数对象，模型可能返回 `message.tool_calls`（或流式 `delta.tool_calls`）；调用方需执行函数并将结果以 `tool` 角色消息回传继续对话。

### 5.4 联网搜索 / 多模态输入

模型支持联网搜索（返回 `annotations`）、图像、音频、视频等多模态输入；`usage` 中提供 `web_search_usage` 及各类模态 token 明细。

### 5.5 深度思考（thinking）

通过 `thinking.type = "enabled"` 开启思维链。思考模式下请保留历史 `reasoning_content` 以获得最佳多轮表现，且 `temperature` / `top_p` 将被忽略。

---

## 6. 参考链接

- [错误码说明](https://mimo.mi.com/docs/zh-CN/api/guidance/error-codes)
- [OpenAI Responses API](https://mimo.mi.com/docs/zh-CN/api/chat/responses)
