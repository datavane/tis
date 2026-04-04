# TIS OpenClaw Plugin

TIS 数据集成平台的 OpenClaw 插件包，将 TIS 的数据管道能力封装为 OpenClaw 可安装的标准插件。

## 功能特性

- **MCP 协议支持**：基于 Model Context Protocol 标准与 TIS 服务通信
- **Skill 封装**：提供 OpenClaw 原生 Skill 定义
- **工具集成**：包含数据源管理、管道创建、任务执行等核心工具

## 安装方式

```bash
openclaw plugins install tis-openclaw-plugin.tar.gz
```

## 配置要求

确保 TIS 服务已启动并可访问：

```yaml
# OpenClaw 配置
skills:
  - name: tis-data-pipeline
    config:
      tis_mcp_endpoint: "http://localhost:8080/mcp"
```

## 可用工具

1. `list_plugin_types` - 列出可用的 TIS 插件类型
2. `create_plugin_instance` - 创建插件实例（数据源等）
3. `get_plugin_schema` - 获取插件配置 Schema
4. `create_pipeline` - 创建数据同步管道

## 使用示例

在 OpenClaw 中直接对话：

> "帮我创建一个 MySQL 数据源，连接到 192.168.1.100:3306"

OpenClaw Agent 将自动调用相应的 TIS MCP 工具完成操作。

## 更多信息

- TIS 项目：https://github.com/qlangtech/tis
- 文档：https://tis.pub/docs/
