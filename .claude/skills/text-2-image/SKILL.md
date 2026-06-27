---
name: text-2-image
description: 基于阿里云 qwen-image-2.0 API 生成图片。支持纯文本描述生成图片，或基于参考图片进行图片生成。自动下载并保存生成的图片到当前目录。
license: MIT
metadata:
  author: mozhenghua
  version: "1.1"
---

# 阿里云文生图工具

此命令基于阿里云 Token Plan 的 qwen-image-2.0 模型，支持两种方式生成图片：
1. **纯文本生成**：根据文字描述生成图片
2. **图生图**：基于参考图片和文字描述生成新图片

## 使用方法

### 纯文本生成
```
/text-2-image <图片描述>
```

示例：
```
/text-2-image 一只可爱的橙色小猫坐在窗台上看风景
/text-2-image 未来科技感的城市夜景，霓虹灯闪烁
```

### 图生图（基于参考图片）
```
/text-2-image image:/path/to/image.png <图片描述>
```

示例：
```
/text-2-image image:/Users/mozhenghua/photo.jpg 将这张照片转换为水彩画风格
/text-2-image image:./reference.png 保持构图，改成卡通风格
```

## 任务说明

调用阿里云 Token Plan 文生图 API，支持：
1. 纯文本描述生成图片
2. 基于参考图片和文字描述生成新图片（图生图）

生成的图片自动下载保存到当前目录。

## 输入参数

用户需求接收格式：`$ARGUMENTS`

从用户输入中提取以下参数：
- **image**（可选）：参考图片路径，格式为 `image:/path/to/image.png`
- **prompt**（必需）：图片描述文字
- **model**（可选）：使用的模型，默认 `qwen-image-2.0`
- **size**（可选）：图片尺寸，默认 `1024*1024`

支持的尺寸选项：
- `1024*1024`（正方形，默认）
- `720*1280`（竖版）
- `1280*720`（横版）

## 执行步骤

### 1. 参数提取与验证

从 `$ARGUMENTS` 中提取参数：
- 检查是否包含 `image:/path/to/file` 格式的参考图片路径
- 如果用户指定了 `model:xxx` 或 `size:xxx`，则解析相应参数
- prompt 为剩余的所有文字内容

示例解析：
- 输入：`一只小猫` → image=null, prompt="一只小猫", model="qwen-image-2.0", size="1024*1024"
- 输入：`size:720*1280 一只小猫` → image=null, prompt="一只小猫", size="720*1280"
- 输入：`image:/tmp/cat.jpg 改成水彩风格` → image="/tmp/cat.jpg", prompt="改成水彩风格"

### 2. 处理参考图片（如果提供）

如果用户提供了参考图片路径：

```bash
# 检查文件是否存在
if [ ! -f "/path/to/image.png" ]; then
  echo "错误：图片文件不存在"
  exit 1
fi

# 使用 base64 编码图片
image_base64=$(base64 -i "/path/to/image.png")
```

**重要说明**：
- 必须检查文件是否存在
- 使用 `base64` 命令对图片进行编码
- macOS 使用 `base64 -i <file>`，Linux 使用 `base64 -w 0 <file>`

### 3. 调用阿里云 API

#### 3.1 纯文本生成（无参考图片）

使用 Bash 工具执行 curl 命令调用 API：

```bash
curl -s -X POST "https://llm-s8qpdvjob13kiwj5.cn-beijing.maas.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation" \
  -H "Authorization: Bearer $QWEN_AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "<model>",
    "input": {
      "messages": [
        {
          "role": "user",
          "content": [
            {
              "text": "<prompt>"
            }
          ]
        }
      ]
    },
    "parameters": {
      "size": "<size>"
    }
  }'
```

#### 3.2 图生图（有参考图片）

当提供参考图片时，content 数组需要同时包含图片和文本：

```bash
curl -s -X POST "https://llm-s8qpdvjob13kiwj5.cn-beijing.maas.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation" \
  -H "Authorization: Bearer $QWEN_AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "<model>",
    "input": {
      "messages": [
        {
          "role": "user",
          "content": [
            {"image": "<base64_encoded_image>"},
            {
              "text": "<prompt>"
            }
          ]
        }
      ]
    },
    "parameters": {
      "size": "<size>"
    }
  }'
```

**重要说明**：
- 图片必须在文本之前放入 content 数组
- 图片使用 `{"image": "base64编码的图片内容"}` 格式
- 使用环境变量 `$QWEN_AUTH_TOKEN` 作为阿里云 API 的 Bearer token
- 确保该环境变量已正确设置
- API 返回 JSON 格式响应

### 4. 解析响应并提取图片 URL

从 API 返回的 JSON 中提取图片 URL：
- 路径：`output.choices[0].message.content[0].image`
- 使用 `jq` 工具解析 JSON（如果可用）
- 或使用 `grep`/`sed` 等工具提取

示例响应结构：
```json
{
  "output": {
    "choices": [
      {
        "message": {
          "content": [
            {
              "image": "https://xxx.oss-cn-beijing.aliyuncs.com/xxx.png"
            }
          ]
        }
      }
    ]
  }
}
```

### 5. 下载图片

使用 curl 下载图片到当前目录：

```bash
curl -s -o "generated_$(date +%Y%m%d_%H%M%S).png" "<image_url>"
```

文件命名格式：`generated_YYYYMMDD_HHMMSS.png`

### 6. 向用户报告结果

输出信息应包含：
- 生成成功的提示
- 图片文件的完整路径
- 使用的模型和尺寸参数
- 是否使用了参考图片
- 原始 prompt

示例输出（纯文本生成）：
```
✓ 图片生成成功！

文件路径：/path/to/generated_20260626_143025.png
模型：qwen-image-2.0
尺寸：1024*1024
描述：一只可爱的橙色小猫坐在窗台上看风景
```

示例输出（图生图）：
```
✓ 图片生成成功！

文件路径：/path/to/generated_20260626_143025.png
模型：qwen-image-2.0
尺寸：1024*1024
参考图片：/tmp/cat.jpg
描述：改成水彩风格
```

## 错误处理

处理以下可能的错误情况：

1. **环境变量未设置**
   - 检查 `$QWEN_AUTH_TOKEN` 是否存在
   - 提示用户设置该环境变量

2. **参考图片文件不存在**
   - 检查用户提供的图片路径是否有效
   - 提示用户检查文件路径

3. **图片文件格式不支持**
   - 支持的格式：PNG, JPG, JPEG, WebP
   - 提示用户使用支持的格式

4. **图片文件过大**
   - 建议图片大小不超过 10MB
   - 提示用户压缩图片后重试

5. **API 调用失败**
   - 检查 HTTP 响应状态码
   - 解析错误信息并友好提示

6. **JSON 解析失败**
   - 打印原始响应便于调试
   - 提示用户重试或检查参数

7. **图片下载失败**
   - 检查 URL 有效性
   - 提示网络连接问题

## 最佳实践

1. **Prompt 编写建议**
   - 描述要具体明确
   - 包含主体、场景、风格等关键元素
   - 中英文均可，但中文效果更好
   - 使用参考图片时，prompt 应描述想要的变化或风格

2. **参考图片选择**
   - 图片质量越高，生成效果越好
   - 建议使用清晰、构图合理的图片
   - 图片大小建议在 1MB-10MB 之间
   - 支持格式：PNG, JPG, JPEG, WebP

3. **尺寸选择**
   - 正方形（1024*1024）：适合头像、图标
   - 横版（1280*720）：适合封面、横幅
   - 竖版（720*1280）：适合手机壁纸、海报

4. **文件管理**
   - 图片自动以时间戳命名，避免覆盖
   - 建议定期整理生成的图片文件

## 技术要求

- 必须使用 Bash 工具执行命令
- 使用 `curl -s` 静默模式避免进度输出
- JSON 数据使用单引号包裹，内部字符串使用双引号
- 正确转义特殊字符
- 使用 `-o` 参数保存文件，而不是重定向
- 图片编码时注意 macOS 和 Linux 的 base64 命令差异

## 依赖项

- `curl`：HTTP 请求工具（系统自带）
- `date`：时间戳生成（系统自带）
- `base64`：图片 Base64 编码（系统自带）
- `jq`（可选）：JSON 解析工具，如果可用则优先使用

## 注意事项

- API 调用可能需要几秒到十几秒，请耐心等待
- 生成的图片质量受 prompt 描述质量影响
- 使用参考图片时，生成时间可能更长（需要上传和处理图片）
- 每次调用都会产生 API 费用，请合理使用
- 确保有足够的磁盘空间存储图片（通常 < 5MB）
- 参考图片的 Base64 编码可能产生较大的 JSON 请求体
