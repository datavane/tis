## baseUrl

MiMo API-KEY 在 MiMo 开放平台控制台申请，接口文档见
[https://mimo.mi.com/docs/zh-CN/api/chat/openai-api](https://mimo.mi.com/docs/zh-CN/api/chat/openai-api)。

接口完整地址为 `https://api.xiaomimimo.com/v1/chat/completions`，本字段只需填写到域名部分，
只有在通过代理网关访问时才需要修改。

## maxTokens

对应接口的 `max_completion_tokens` 参数（注意不是 OpenAI 传统的 `max_tokens`），
该上限**包含推理 token** 与最终可见输出，取值范围 `[1, 131072]`。

不同模型的默认值不同：`mimo-v2.5` 为 32768，`mimo-v2.5-pro` 为 131072。
开启深度思考后推理内容同样计入该上限，建议适当调大。

## thinking

控制模型是否开启思维链（Chain-of-Thought），取值 `enabled` / `disabled`：

- **disabled（默认）**：直接输出最终答案，表单中「高级采样」配置的 temperature / top_p 正常生效
- **enabled**：模型在给出最终答案前先输出推理过程（响应中的 `reasoning_content`），
  推理内容会计入 `max_completion_tokens`，因此需要预留更大的 token 上限

开启思考模式后，`temperature` 与 `top_p` 会被模型强制采用推荐默认值（1.0 / 0.95），
TIS 会主动剔除请求中的采样参数，避免与表单中「高级采样」的配置产生冲突。

## sampling

「高级采样」使用各模型共享的采样插件，可选 `temperature`（温度采样）与 `top-p`（核采样），
两者建议二选一调整。**MiMo 的取值区间比 QWen 等模型更窄**，请按下面的范围填写：

| 参数 | MiMo 取值范围 | 插件表单校验范围 |
| --- | --- | --- |
| `temperature` | `[0, 1.5]` | `[0, 2]` |
| `top_p` | `[0.01, 1.0]` | `[0, 1]` |

插件表单的校验范围是按 QWen 语义设定的，比 MiMo 更宽，因此超出上表的取值能够通过表单校验，
但会在真正发出请求前被 MiMo provider 拦截并报错。请按上表的 MiMo 范围填写。

在开启深度思考（`thinking = enabled`）的情况下，两个参数都会被模型忽略，无需配置。
