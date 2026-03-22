# 使用 curl 测试 MCP 服务

## Context

tis-console Web 服务已成功启动，MCP 服务通过 `WeatherHttpMcpServer` 注册在 `http://localhost:8080/tjs/mcp`。
使用的是 MCP Java SDK `1.1.0`，传输方式为 Streamable HTTP（`HttpServletStreamableServerTransportProvider`）。
该服务暴露了一个 `get_temperature` 工具，可查询北京/上海/广州/深圳/成都的温度。

## MCP Streamable HTTP 协议说明

MCP Streamable HTTP 传输使用 **JSON-RPC 2.0** over **HTTP POST**。需要按以下步骤交互：

1. 发送 `initialize` 请求 → 获取 `Mcp-Session-Id`
2. 发送 `notifications/initialized` 通知
3. 发送 `tools/call` 调用工具

## curl 测试命令

### 第 1 步：初始化会话

```bash
curl -v -X POST http://localhost:8080/tjs/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2025-03-26",
      "capabilities": {},
      "clientInfo": {
        "name": "curl-test",
        "version": "1.0.0"
      }
    }
  }'
```

> 注意响应头中的 `Mcp-Session-Id`，后续请求需要带上这个 header。用 `-v` 可以看到响应头。

### 第 2 步：发送 initialized 通知

```bash
curl -X POST http://localhost:8080/tjs/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <替换为第1步返回的session-id>" \
  -d '{
    "jsonrpc": "2.0",
    "method": "notifications/initialized"
  }'
```

> 这是通知（没有 `id` 字段），服务端不会返回 body。

### 第 3 步：列出可用工具（可选）

```bash
curl -X POST http://localhost:8080/tjs/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <替换为第1步返回的session-id>" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```

### 第 4 步：调用 get_temperature 工具

```bash
curl -X POST http://localhost:8080/tjs/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <替换为第1步返回的session-id>" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "get_temperature",
      "arguments": {
        "location": "北京"
      }
    }
  }'
```

> 预期返回：`北京的当前温度为 22°C`
> 支持的城市：北京(22)、上海(26)、广州(30)、深圳(29)、成都(20)

## 注意事项

- 如果服务端返回 `text/event-stream`（SSE 格式），数据会以 `data: ...` 前缀返回，需要解析 SSE 事件中的 JSON
- `Mcp-Session-Id` 是必须的（除了第一次 initialize 请求），否则服务端会拒绝请求
- 如果 `protocolVersion` 不匹配，可尝试改为 `"2024-11-05"`
