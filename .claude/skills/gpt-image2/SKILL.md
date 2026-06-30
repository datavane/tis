---
name: gpt-image2
description: 基于 GPT Image2 模型生成图片。支持纯文本描述生成图片，或基于参考图片进行图片生成。自动下载并保存生成的图片到当前目录。
license: MIT
metadata:
  author: mozhenghua
  version: "1.0"
---

# GPT Image2 文生图工具

此命令基于 VectorEngine API 的 gpt-image-2:stable 模型，支持两种方式生成图片：
1. **纯文本生成**：根据文字描述生成图片
2. **图生图**：基于参考图片和文字描述生成新图片

## 使用方法

### 纯文本生成
```
/gpt-image2 <图片描述>
```

示例：
```
/gpt-image2 一只可爱的橙色小猫坐在窗台上看风景
/gpt-image2 A futuristic city skyline at sunset with neon lights
```

### 图生图（基于参考图片）
```
/gpt-image2 image:/path/to/image.png <图片描述>
```

示例：
```
/gpt-image2 image:/Users/mozhenghua/photo.jpg 将这张照片转换为水彩画风格
/gpt-image2 image:./reference.png 保持构图，改成卡通风格
```

## 任务说明

调用 VectorEngine API 的 GPT Image2 模型，支持：
1. 纯文本描述生成图片
2. 基于参考图片和文字描述生成新图片（图生图）

生成的图片自动下载保存到当前目录。

## 输入参数

用户需求接收格式：`$ARGUMENTS`

从用户输入中提取以下参数：
- **image**（可选）：参考图片路径，格式为 `image:/path/to/image.png`
- **prompt**（必需）：图片描述文字
- **model**（可选）：使用的模型，默认 `gpt-image-2:stable`
- **size**（可选）：图片尺寸，默认 `1024x1024`
- **n**（可选）：生成图片数量，默认 `1`

支持的尺寸选项：
- `1024x1024`（正方形，默认）
- `512x512`（小正方形）
- `1024x768`（横版）
- `768x1024`（竖版）

## 执行步骤

### 1. 参数提取与验证

从 `$ARGUMENTS` 中提取参数：
- 检查是否包含 `image:/path/to/file` 格式的参考图片路径
- 如果用户指定了 `model:xxx`、`size:xxx` 或 `n:xxx`，则解析相应参数
- prompt 为剩余的所有文字内容

示例解析：
- 输入：`一只小猫` → image=null, prompt="一只小猫", model="gpt-image-2:stable", size="1024x1024", n=1
- 输入：`size:512x512 一只小猫` → image=null, prompt="一只小猫", size="512x512"
- 输入：`image:/tmp/cat.jpg 改成水彩风格` → image="/tmp/cat.jpg", prompt="改成水彩风格"
- 输入：`n:2 一只小猫` → image=null, prompt="一只小猫", n=2

### 2. 处理参考图片（如果提供）

如果用户提供了参考图片路径：

```bash
# 检查文件是否存在
if [ ! -f "/path/to/image.png" ]; then
  echo "错误：图片文件不存在"
  exit 1
fi

# 获取图片格式
image_ext="${image_path##*.}"
image_format=$(echo "$image_ext" | tr '[:upper:]' '[:lower:]')

# 映射文件扩展名到 MIME 类型
case "$image_format" in
  jpg) mime_type="jpeg" ;;
  jpeg) mime_type="jpeg" ;;
  png) mime_type="png" ;;
  webp) mime_type="webp" ;;
  *) mime_type="jpeg" ;;
esac

# 使用 base64 编码图片（macOS）
image_base64=$(base64 -i "/path/to/image.png")

# 构造 data URI 格式
image_data_uri="data:image/${mime_type};base64,${image_base64}"
```

**重要说明**：
- 必须检查文件是否存在
- 使用 `base64` 命令对图片进行编码
- macOS 使用 `base64 -i <file>`，Linux 使用 `base64 -w 0 <file>`
- 图片必须按照 `data:image/[format];base64,[base64数据]` 格式编码
- 支持的格式：jpeg, png, webp

### 3. 调用 VectorEngine API

#### 3.1 纯文本生成（无参考图片）

使用 Bash 工具执行 curl 命令调用 API：

```bash
curl -s -X POST "https://api.vectorengine.ai/v1/images/generations" \
  -H "Authorization: Bearer $GPT_IMAGE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "<model>",
    "prompt": "<prompt>",
    "size": "<size>",
    "n": <n>,
    "format": "png"
  }'
```

#### 3.2 图生图（有参考图片）

当提供参考图片时，使用 edits endpoint：

```bash
curl -s -X POST "https://api.vectorengine.ai/v1/images/edits" \
  -H "Authorization: Bearer $GPT_IMAGE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "<model>",
    "prompt": "<prompt>",
    "image": "<image_data_uri>",
    "n": "<n>",
    "size": "<size>",
    "quality": "auto",
    "background": "auto",
    "moderation": "auto"
  }'
```

**重要说明**：
- 图生图时，image 字段必须是完整的 data URI 格式：`data:image/[format];base64,[base64数据]`
- 使用环境变量 `$GPT_IMAGE_TOKEN` 作为 Bearer token
- 确保该环境变量已正确设置（格式：sk-YOUR_TOKEN）
- API 返回 JSON 格式响应
- 特殊字符需要正确转义

**JSON 构造技巧**：
由于 prompt 和 base64 数据可能包含特殊字符，建议使用 jq 工具构造 JSON（如果可用）：

```bash
# 纯文本生成（使用 jq）
curl --max-time 600 -s -X POST "https://api.vectorengine.ai/v1/images/generations" \
  -H "Authorization: Bearer $GPT_IMAGE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --arg model "$model" \
    --arg prompt "$prompt" \
    --arg size "$size" \
    --argjson n "$n" \
    '{model: $model, prompt: $prompt, size: $size, n: $n, format: "png"}')"

# 图生图（使用 jq）
curl --max-time 600 -s -X POST "https://api.vectorengine.ai/v1/images/edits" \
  -H "Authorization: Bearer $GPT_IMAGE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --arg model "$model" \
    --arg prompt "$prompt" \
    --arg image "$image_data_uri" \
    --arg size "$size" \
    --argjson n "$n" \
    '{model: $model, prompt: $prompt, image: $image, n: $n, size: $size, quality: "auto", background: "auto", moderation: "auto"}')"
```

如果没有 jq，需要手动转义特殊字符（引号、换行符等）。

### 4. 解析响应并保存图片

API 可能返回两种格式的响应：
1. **URL 格式**：`data[].url` - 图片的临时下载链接
2. **Base64 格式**：`data[].b64_json` - Base64 编码的图片数据

需要同时支持这两种格式。

示例响应结构：

**URL 格式响应**：
```json
{
  "created": 1234567890,
  "data": [
    {
      "url": "https://example.com/image1.png"
    }
  ]
}
```

**Base64 格式响应**：
```json
{
  "created": 1234567890,
  "data": [
    {
      "b64_json": "iVBORw0KGgoAAAANS..."
    }
  ]
}
```

### 5. 提取并保存图片

使用以下逻辑处理两种响应格式：

```bash
counter=1

# 检查响应中是否包含 url 字段
if echo "$response" | jq -e '.data[0].url' > /dev/null 2>&1; then
  # URL 格式：下载图片
  image_urls=$(echo "$response" | jq -r '.data[].url')
  
  for url in $image_urls; do
    if [ $n -eq 1 ]; then
      filename="generated_$(date +%Y%m%d_%H%M%S).png"
    else
      filename="generated_$(date +%Y%m%d_%H%M%S)_${counter}.png"
    fi
    curl -s -o "$filename" "$url"
    echo "已保存：$filename"
    counter=$((counter + 1))
  done
  
elif echo "$response" | jq -e '.data[0].b64_json' > /dev/null 2>&1; then
  # Base64 格式：解码并保存
  echo "$response" | jq -r '.data[].b64_json' | while read -r b64_data; do
    if [ $n -eq 1 ]; then
      filename="generated_$(date +%Y%m%d_%H%M%S).png"
    else
      filename="generated_$(date +%Y%m%d_%H%M%S)_${counter}.png"
    fi
    
    # 解码 base64 数据并保存
    echo "$b64_data" | base64 -d > "$filename"
    echo "已保存：$filename"
    counter=$((counter + 1))
  done
  
else
  echo "错误：无法从响应中提取图片数据"
  echo "原始响应："
  echo "$response"
  exit 1
fi
```

**说明**：
- 优先检查 URL 格式，如果存在则直接下载
- 如果不存在 URL，则检查 b64_json 格式
- 使用 `base64 -d` 解码 Base64 数据并保存为文件
- 如果两种格式都不存在，打印错误和原始响应便于调试

文件命名格式：
- 单张图片：`generated_YYYYMMDD_HHMMSS.png`
- 多张图片：`generated_YYYYMMDD_HHMMSS_1.png`, `generated_YYYYMMDD_HHMMSS_2.png`, ...

### 6. 向用户报告结果

输出信息应包含：
- 生成成功的提示
- 图片文件的完整路径（多张图片则列出所有路径）
- 使用的模型和尺寸参数
- 生成数量
- 是否使用了参考图片
- 原始 prompt

示例输出（纯文本生成）：
```
✓ 图片生成成功！

文件路径：/path/to/generated_20260626_143025.png
模型：gpt-image-2:stable
尺寸：1024x1024
数量：1
描述：A futuristic city skyline at sunset with neon lights
```

示例输出（图生图，多张）：
```
✓ 图片生成成功！

文件路径：
  1. /path/to/generated_20260626_143025_1.png
  2. /path/to/generated_20260626_143025_2.png
模型：gpt-image-2:stable
尺寸：1024x1024
数量：2
参考图片：/tmp/cat.jpg
描述：改成水彩风格
```

## 错误处理

处理以下可能的错误情况：

1. **环境变量未设置**
   - 检查 `$GPT_IMAGE_TOKEN` 是否存在
   - 提示用户设置该环境变量（格式：sk-YOUR_TOKEN）

2. **参考图片文件不存在**
   - 检查用户提供的图片路径是否有效
   - 提示用户检查文件路径

3. **图片文件格式不支持**
   - 支持的格式：PNG, JPG, JPEG, WebP
   - 提示用户使用支持的格式

4. **图片文件过大**
   - 建议图片大小不超过 10MB
   - 提示用户压缩图片后重试
   - Base64 编码后的数据可能导致请求体过大

5. **API 调用失败**
   - 检查 HTTP 响应状态码
   - 解析错误信息并友好提示
   - 常见错误：401 (token 无效), 429 (请求过多), 500 (服务器错误)

6. **JSON 解析失败**
   - 打印原始响应便于调试
   - 提示用户重试或检查参数

7. **图片下载失败**
   - 检查 URL 有效性
   - 提示网络连接问题
   - URL 可能有时效性限制

8. **特殊字符转义问题**
   - prompt 中包含引号、换行符等特殊字符
   - 建议使用 jq 工具自动处理转义
   - 如果没有 jq，需要手动转义

## 最佳实践

1. **Prompt 编写建议**
   - 描述要具体明确
   - 包含主体、场景、风格等关键元素
   - 中英文均可，英文效果可能更好（取决于模型训练数据）
   - 使用参考图片时，prompt 应描述想要的变化或风格
   - 避免过于复杂或矛盾的描述

2. **参考图片选择**
   - 图片质量越高，生成效果越好
   - 建议使用清晰、构图合理的图片
   - 图片大小建议在 100KB-5MB 之间（太大会导致 base64 编码后请求体过大）
   - 支持格式：PNG, JPG, JPEG, WebP
   - 避免使用过于抽象或模糊的参考图

3. **尺寸选择**
   - 正方形（1024x1024）：适合头像、图标、社交媒体
   - 小正方形（512x512）：快速预览、测试效果
   - 横版（1024x768）：适合封面、横幅
   - 竖版（768x1024）：适合手机壁纸、海报

4. **批量生成**
   - 使用 `n:2` 或更大值一次生成多张变体
   - 适合需要多个选项的场景
   - 注意：n 越大，等待时间越长，费用越高

5. **文件管理**
   - 图片自动以时间戳命名，避免覆盖
   - 建议定期整理生成的图片文件
   - 多张图片生成时会自动添加序号

## 技术要求

- 必须使用 Bash 工具执行命令
- 使用 `curl -s` 静默模式避免进度输出
- JSON 数据使用单引号包裹，内部字符串使用双引号（或使用 jq 构造）
- 正确转义特殊字符（建议使用 jq）
- 使用 `-o` 参数保存文件，而不是重定向
- 图片编码时注意 macOS 和 Linux 的 base64 命令差异
- base64 编码的图片可能很大，确保命令行能处理（通常没问题）

## 依赖项

- `curl`：HTTP 请求工具（系统自带）
- `date`：时间戳生成（系统自带）
- `base64`：图片 Base64 编码（系统自带）
- `jq`（强烈推荐）：JSON 解析和构造工具
  - 用于解析 API 响应
  - 用于构造复杂的 JSON 请求（自动处理转义）
  - 如果没有安装，可以通过 `brew install jq`（macOS）或包管理器安装

## 注意事项

- API 调用可能需要几秒到几十秒，请耐心等待
- 生成的图片质量受 prompt 描述质量影响
- 使用参考图片时，生成时间可能更长（需要上传和处理图片）
- 每次调用都会产生 API 费用，请合理使用
- 确保有足够的磁盘空间存储图片（通常 < 5MB 每张）
- 参考图片的 Base64 编码可能产生较大的 JSON 请求体（约为原图的 1.37 倍）
- 生成多张图片（n>1）时费用和时间成倍增加
- 确保 `$GPT_IMAGE_TOKEN` 环境变量格式正确：`sk-YOUR_TOKEN`
- VectorEngine API 可能有速率限制，频繁调用可能被限流

## 环境变量设置

在使用前，需要设置 API token：

```bash
# 临时设置（当前会话）
export GPT_IMAGE_TOKEN="sk-YOUR_TOKEN"

# 永久设置（添加到 ~/.bashrc 或 ~/.zshrc）
echo 'export GPT_IMAGE_TOKEN="sk-YOUR_TOKEN"' >> ~/.bashrc
source ~/.bashrc
```

## API 参考

- API Base URL: `https://api.vectorengine.ai`
- 纯文本生成 Endpoint: `/v1/images/generations`
- 图生图 Endpoint: `/v1/images/edits`
- 认证方式: Bearer Token
- 请求格式: JSON
- 响应格式: JSON

## 与 text-2-image 的对比

| 特性 | text-2-image (Qwen) | gpt-image2 (GPT) |
|------|---------------------|------------------|
| API 提供商 | 阿里云 Token Plan | VectorEngine |
| 模型 | qwen-image-2.0 | gpt-image-2:stable |
| 尺寸格式 | 1024*1024 | 1024x1024 |
| 批量生成 | 不支持 | 支持（n 参数） |
| 图片输入格式 | 纯 base64 | data URI 格式 |
| API 风格 | 自定义 | OpenAI 兼容 |
| 环境变量 | QWEN_AUTH_TOKEN | GPT_IMAGE_TOKEN |

## 故障排查

### 问题：图片生成失败，提示 401 错误
- 检查 `$GPT_IMAGE_TOKEN` 是否正确设置
- 确认 token 格式为 `sk-YOUR_TOKEN`
- 验证 token 是否过期或无效

### 问题：图片下载失败
- 检查网络连接
- 确认 API 返回的 URL 格式正确
- 注意 URL 可能有时效性，需要及时下载

### 问题：JSON 解析错误
- 打印原始响应查看具体内容
- 检查 API 是否返回了错误信息
- 确认响应格式符合预期

### 问题：参考图片 base64 编码后请求失败
- 检查图片文件大小，建议 < 5MB
- 确认 base64 编码格式正确（data URI）
- 尝试压缩图片后重试

### 问题：prompt 中特殊字符导致 JSON 格式错误
- 使用 jq 工具自动处理转义
- 手动转义引号、换行符等特殊字符
- 简化 prompt 描述
