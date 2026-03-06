# TIS 与 OpenClaw 整合设计方案

## 一、项目背景与目标

### 1.1 项目背景

**TIS** 是企业级数据集成服务平台，基于批（DataX）、流（Flink-CDC、Chunjun）一体化架构，提供简单易用的操作界面来降低端到端数据同步的实施门槛。TIS 继承了 Jenkins 的设计思想，具有强大的 SPI 扩展机制。

**OpenClaw** 是 2026 年 GitHub 上最火的开源 AI Agent 项目，支持多 Agent 协作、Tool 调用、Plan-And-Execute 等核心能力，广泛应用于自动化任务编排领域。

### 1.2 整合目标

将 TIS 定位为 **OpenClaw 的数据管道引擎（Data Pipeline Engine）**，提供以下能力：

1. **REST API 接入**：标准 HTTP API，支持任何外部系统（包括 OpenClaw Agent）调用 TIS 的数据管道管理能力
2. **MCP Server 接入**：基于 Model Context Protocol 标准，让 LLM 和 AI Agent 直接操作 TIS
3. **OpenClaw Skill 封装**：将 TIS 的核心能力封装为 OpenClaw 原生 Skill，实现无缝集成

### 1.3 价值定位

```
┌──────────────────────────────────────────────────────────┐
│                     OpenClaw Agent                       │
│  "帮我把 MySQL 的 orders 表实时同步到 Paimon 数仓"         │
└───────────────┬─────────────────────┬────────────────────┘
                │                     │
         ┌──────▼──────┐       ┌──────▼──────┐
         │  TIS Skill  │       │  MCP Client │
         │  (原生集成)  │       │ (标准协议)   │
         └──────┬──────┘       └──────┬──────┘
                │                     │
         ┌──────▼─────────────────────▼──────┐
         │          TIS REST API             │
         │   数据管道全生命周期管理             │
         └──────┬─────────────────────┬──────┘
                │                     │
         ┌──────▼──────┐       ┌──────▼──────┐
         │  批量同步    │       │  实时同步    │
         │  (DataX)    │       │ (Flink-CDC) │
         └─────────────┘       └─────────────┘
```

---

## 二、TIS 现有 AI Agent 基础设施分析

### 2.1 已有组件

TIS 已经构建了一套 AI Agent 基础设施，位于 `tis-plugin` 模块的 `com.qlangtech.tis.aiagent` 包中：

| 组件 | 路径 | 职责 |
|------|------|------|
| `IAgentContext` | `aiagent/core/IAgentContext.java` | Agent 执行上下文接口，支持消息推送、Token 用量统计、LLM 状态回调 |
| `LLMProvider` | `aiagent/llm/LLMProvider.java` | 大模型接口抽象，支持多 Provider（DeepSeek 等），提供 `chat()` 和 `chatJson()` 两种调用模式 |
| `UserPrompt` | `aiagent/llm/UserPrompt.java` | 用户提示词封装，包含摘要和完整提示词 |
| `JsonSchema` | `aiagent/llm/JsonSchema.java` | JSON Schema 构建器，支持 `oneOf` 多态、插件描述符动态生成 Schema |
| `DescribableImpl` | `aiagent/plan/DescribableImpl.java` | 可描述插件实现的元数据，关联扩展点与具体实现 |
| `AIAssistSupport` | `extension/AIAssistSupport.java` | AI 辅助支持基类，提供插件自动化部署能力（如自动部署 Flink Standalone） |

### 2.2 核心设计模式

参考已有的 `chat-pipeline-design.md`，TIS 采用 **Plan-And-Execute Agent** 模式：

```
用户输入 → LLM 解析意图 → 匹配任务模板 → 生成执行计划
→ 逐步执行 → 实时反馈 → 验证参数 → 创建插件实例 → 触发任务
```

### 2.3 关键扩展点

| 扩展点 | 类 | 说明 |
|--------|-----|------|
| 数据同步任务提交 | `DataXJobSubmit` | SPI 扩展点，支持 AKKA/EMBEDDED/LOCAL 三种执行模式 |
| 任务参数管理 | `DataXJobSubmitParams` | 管理并行度、内存规格、超时时间等运行参数 |
| 插件描述符 | `Descriptor` | 每个插件的元数据描述，支持 JSON Schema 自动生成 |
| 参数配置 | `ParamsConfig` | 全局参数配置管理，`LLMProvider` 和 `DataXJobSubmitParams` 均继承自此类 |

---

## 三、REST API 设计

### 3.1 设计原则

- 遵循 RESTful 规范，使用标准 HTTP 方法
- 统一错误码和响应格式
- 支持 API Key 认证
- 操作幂等性设计
- 与 TIS 现有 Action 体系保持一致（继承 `BasicModule`）

### 3.2 认证机制

```
POST /tis/api/v1/*
Headers:
  Authorization: Bearer <api-key>
  Content-Type: application/json
```

API Key 通过 TIS 管理控制台生成，基于 `ParamsConfig` 扩展点实现存储和验证：

```java
public class ApiKeyConfig extends ParamsConfig {
    @FormField(ordinal = 0, identity = true, type = FormFieldType.INPUTTEXT)
    public String name;

    @FormField(ordinal = 1, type = FormFieldType.PASSWORD)
    public String apiKey;

    @FormField(ordinal = 2, type = FormFieldType.ENUM)
    public ApiPermission permission; // READ_ONLY, READ_WRITE, ADMIN
}
```

### 3.3 API 端点设计

#### 3.3.1 数据源管理

```
# 列出所有已配置的数据源
GET /tis/api/v1/datasources
Response: {
  "success": true,
  "data": [
    {
      "name": "my_mysql_ds",
      "type": "MySQL",
      "pluginImpl": "MySQLV5DataSourceFactory",
      "host": "192.168.1.100",
      "port": 3306,
      "database": "order_db"
    }
  ]
}

# 创建数据源
POST /tis/api/v1/datasources
Body: {
  "descriptorId": "MySQLV5DataSourceFactory",
  "props": {
    "name": "my_mysql_ds",
    "host": "192.168.1.100",
    "port": 3306,
    "userName": "root",
    "password": "****",
    "dbName": "order_db"
  }
}
Response: {
  "success": true,
  "data": { "name": "my_mysql_ds", "created": true }
}

# 测试数据源连接
POST /tis/api/v1/datasources/{name}/test-connection
Response: {
  "success": true,
  "data": { "connected": true, "latencyMs": 12 }
}

# 获取数据源下的表列表
GET /tis/api/v1/datasources/{name}/tables
Response: {
  "success": true,
  "data": {
    "tables": ["orders", "order_items", "users", "products"]
  }
}

# 获取表的列元信息
GET /tis/api/v1/datasources/{name}/tables/{tableName}/columns
Response: {
  "success": true,
  "data": {
    "columns": [
      { "name": "id", "type": "BIGINT", "primaryKey": true, "nullable": false },
      { "name": "order_no", "type": "VARCHAR(64)", "primaryKey": false, "nullable": false },
      { "name": "amount", "type": "DECIMAL(10,2)", "primaryKey": false, "nullable": true }
    ]
  }
}
```

#### 3.3.2 数据管道管理

```
# 列出所有数据管道
GET /tis/api/v1/pipelines
Query Params: ?status=active&page=1&size=20
Response: {
  "success": true,
  "data": {
    "items": [
      {
        "name": "mysql2paimon_orders",
        "readerType": "MySQL",
        "writerType": "Paimon",
        "tables": ["orders", "order_items"],
        "status": "active",
        "createTime": "2026-03-01T10:00:00Z"
      }
    ],
    "total": 15,
    "page": 1
  }
}

# 创建数据管道
POST /tis/api/v1/pipelines
Body: {
  "name": "mysql2paimon_orders",
  "reader": {
    "descriptorId": "DataxMySQLReader",
    "dataSourceName": "my_mysql_ds",
    "selectedTables": ["orders", "order_items"]
  },
  "writer": {
    "descriptorId": "DataxPaimonWriter",
    "props": {
      "warehouse": "hdfs://namenode:8020/paimon/warehouse",
      "catalogType": "hive"
    }
  },
  "syncMode": "batch"
}
Response: {
  "success": true,
  "data": {
    "name": "mysql2paimon_orders",
    "pipelineId": "dp_20260301_001",
    "created": true
  }
}

# 获取管道详情
GET /tis/api/v1/pipelines/{name}

# 删除管道
DELETE /tis/api/v1/pipelines/{name}
```

#### 3.3.3 任务执行管理

```
# 触发批量同步任务
POST /tis/api/v1/pipelines/{name}/trigger
Body: {
  "triggerType": "batch",
  "params": {
    "parallelism": 4
  }
}
Response: {
  "success": true,
  "data": {
    "taskId": 12345,
    "status": "RUNNING",
    "triggerTime": "2026-03-04T08:30:00Z"
  }
}

# 查询任务执行状态
GET /tis/api/v1/tasks/{taskId}/status
Response: {
  "success": true,
  "data": {
    "taskId": 12345,
    "pipelineName": "mysql2paimon_orders",
    "status": "RUNNING",
    "phase": "FullDump",
    "progress": {
      "totalTables": 2,
      "completedTables": 1,
      "currentTable": "order_items",
      "readRows": 150000,
      "writtenRows": 148500
    },
    "startTime": "2026-03-04T08:30:00Z"
  }
}

# 取消任务
POST /tis/api/v1/tasks/{taskId}/cancel
Response: {
  "success": true,
  "data": { "taskId": 12345, "cancelled": true }
}

# 获取任务执行历史
GET /tis/api/v1/pipelines/{name}/history
Query Params: ?limit=10
Response: {
  "success": true,
  "data": {
    "history": [
      {
        "taskId": 12345,
        "status": "SUCCESS",
        "startTime": "2026-03-04T08:30:00Z",
        "endTime": "2026-03-04T08:45:00Z",
        "totalRows": 298500
      }
    ]
  }
}
```

#### 3.3.4 增量同步管理

```
# 启动增量同步
POST /tis/api/v1/pipelines/{name}/incr/start
Body: {
  "sourceFactory": {
    "descriptorId": "FlinkCDCMySQLSourceFactory",
    "props": {
      "startupOptions": "latest"
    }
  },
  "sinkFactory": {
    "descriptorId": "PaimonPipelineSinkFactory"
  }
}

# 查询增量同步状态
GET /tis/api/v1/pipelines/{name}/incr/status
Response: {
  "success": true,
  "data": {
    "running": true,
    "flinkJobId": "flink-job-xxx",
    "checkpoint": {
      "lastCompleted": "2026-03-04T09:00:00Z",
      "totalProcessed": 5000000
    }
  }
}

# 停止增量同步
POST /tis/api/v1/pipelines/{name}/incr/stop
```

#### 3.3.5 插件元信息查询

```
# 查询可用的插件类型
GET /tis/api/v1/plugins/descriptors
Query Params: ?extendPoint=DataxReader&endType=MySQL
Response: {
  "success": true,
  "data": {
    "descriptors": [
      {
        "id": "DataxMySQLReader",
        "displayName": "MySQL Reader",
        "extendPoint": "com.qlangtech.tis.datax.IDataxReader",
        "endType": "MySQL",
        "schema": { /* JSON Schema for plugin configuration */ }
      }
    ]
  }
}

# 获取插件的 JSON Schema（用于 AI Agent 生成配置）
GET /tis/api/v1/plugins/descriptors/{descriptorId}/schema
Response: {
  "success": true,
  "data": {
    "descriptorId": "DataxMySQLReader",
    "jsonSchema": {
      "type": "object",
      "properties": {
        "dbName": { "type": "string", "description": "数据库名称" },
        "splitPk": { "type": "string", "description": "分片主键" }
      },
      "required": ["dbName"]
    },
    "fieldsDescription": [
      { "name": "dbName", "description": "数据库名称，需要先创建对应的数据源" },
      { "name": "splitPk", "description": "用于并行读取的分片主键字段" }
    ]
  }
}
```

### 3.4 统一响应格式

```json
// 成功响应
{
  "success": true,
  "data": { /* 业务数据 */ },
  "requestId": "req_abc123"
}

// 错误响应
{
  "success": false,
  "errormsg": "Pipeline 'xxx' not found",
  "errorCode": "PIPELINE_NOT_FOUND",
  "requestId": "req_abc456"
}
```

### 3.5 错误码定义

| HTTP Status | errorCode | 说明 |
|-------------|-----------|------|
| 400 | `INVALID_PARAMS` | 请求参数校验失败 |
| 401 | `UNAUTHORIZED` | API Key 无效或缺失 |
| 403 | `FORBIDDEN` | 权限不足 |
| 404 | `RESOURCE_NOT_FOUND` | 资源不存在 |
| 409 | `RESOURCE_CONFLICT` | 资源冲突（如重名管道） |
| 422 | `PLUGIN_VALIDATION_FAILED` | 插件配置验证失败 |
| 500 | `INTERNAL_ERROR` | 内部错误 |
| 503 | `SERVICE_UNAVAILABLE` | 服务不可用（如 Flink 集群未就绪） |

---

## 四、MCP Server 设计

### 4.1 概述

基于 [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) 标准，为 TIS 实现一个 MCP Server，使 LLM 和 AI Agent 能通过标准协议直接操作 TIS 的数据管道能力。

MCP Server 底层调用第三章定义的 REST API，保持逻辑统一。

### 4.2 传输方式

支持两种传输：
- **Streamable HTTP**（推荐）：通过 HTTP POST 发送 JSON-RPC 请求，适用于远程部署场景
- **SSE**：基于 Server-Sent Events 的双向通信，兼容旧版 MCP 客户端

```
MCP Server 端点：http://<tis-host>:8080/mcp
```

### 4.3 Tool 定义

#### 4.3.1 数据源操作

```json
{
  "name": "tis_list_datasources",
  "description": "列出TIS中已配置的所有数据源，返回数据源名称、类型、连接信息",
  "inputSchema": {
    "type": "object",
    "properties": {
      "type": {
        "type": "string",
        "description": "按数据源类型过滤，如 MySQL、PostgreSQL、Oracle 等",
        "enum": ["MySQL", "PostgreSQL", "Oracle", "StarRocks", "Doris", "Hive"]
      }
    }
  }
}
```

```json
{
  "name": "tis_create_datasource",
  "description": "在TIS中创建一个新的数据源连接。需要提供数据源类型（descriptorId）和连接参数。创建前请先通过 tis_get_plugin_schema 获取所需参数的 JSON Schema。",
  "inputSchema": {
    "type": "object",
    "properties": {
      "descriptorId": {
        "type": "string",
        "description": "数据源插件的描述符ID，如 MySQLV5DataSourceFactory"
      },
      "props": {
        "type": "object",
        "description": "数据源连接参数，具体字段由 descriptorId 对应的 JSON Schema 定义"
      }
    },
    "required": ["descriptorId", "props"]
  }
}
```

```json
{
  "name": "tis_test_datasource_connection",
  "description": "测试指定数据源的连接是否正常",
  "inputSchema": {
    "type": "object",
    "properties": {
      "datasourceName": {
        "type": "string",
        "description": "数据源名称"
      }
    },
    "required": ["datasourceName"]
  }
}
```

```json
{
  "name": "tis_list_tables",
  "description": "获取指定数据源下的所有表名列表",
  "inputSchema": {
    "type": "object",
    "properties": {
      "datasourceName": {
        "type": "string",
        "description": "数据源名称"
      }
    },
    "required": ["datasourceName"]
  }
}
```

```json
{
  "name": "tis_get_table_columns",
  "description": "获取指定表的列元信息，包括列名、数据类型、是否主键、是否可空",
  "inputSchema": {
    "type": "object",
    "properties": {
      "datasourceName": {
        "type": "string",
        "description": "数据源名称"
      },
      "tableName": {
        "type": "string",
        "description": "表名"
      }
    },
    "required": ["datasourceName", "tableName"]
  }
}
```

#### 4.3.2 数据管道操作

```json
{
  "name": "tis_create_pipeline",
  "description": "创建一个新的数据同步管道。指定 Reader（数据源端）和 Writer（目标端）插件及其配置，并选择要同步的表。支持批量同步（DataX）和实时同步（Flink-CDC）两种模式。",
  "inputSchema": {
    "type": "object",
    "properties": {
      "name": {
        "type": "string",
        "description": "管道名称，全局唯一标识符，只允许字母、数字和下划线"
      },
      "reader": {
        "type": "object",
        "description": "Reader 端配置",
        "properties": {
          "descriptorId": { "type": "string" },
          "dataSourceName": { "type": "string" },
          "selectedTables": {
            "type": "array",
            "items": { "type": "string" }
          }
        },
        "required": ["descriptorId", "dataSourceName", "selectedTables"]
      },
      "writer": {
        "type": "object",
        "description": "Writer 端配置",
        "properties": {
          "descriptorId": { "type": "string" },
          "props": { "type": "object" }
        },
        "required": ["descriptorId"]
      },
      "syncMode": {
        "type": "string",
        "enum": ["batch", "incremental", "both"],
        "description": "同步模式: batch=批量同步, incremental=增量同步, both=批量+增量"
      }
    },
    "required": ["name", "reader", "writer", "syncMode"]
  }
}
```

```json
{
  "name": "tis_list_pipelines",
  "description": "列出TIS中所有已创建的数据管道，返回管道名称、Reader/Writer类型、同步状态等信息",
  "inputSchema": {
    "type": "object",
    "properties": {
      "status": {
        "type": "string",
        "enum": ["active", "inactive", "all"],
        "description": "按状态过滤"
      }
    }
  }
}
```

```json
{
  "name": "tis_get_pipeline_detail",
  "description": "获取指定数据管道的详细配置信息，包括 Reader/Writer 配置、同步表列表、执行参数等",
  "inputSchema": {
    "type": "object",
    "properties": {
      "pipelineName": {
        "type": "string",
        "description": "管道名称"
      }
    },
    "required": ["pipelineName"]
  }
}
```

#### 4.3.3 任务执行操作

```json
{
  "name": "tis_trigger_pipeline",
  "description": "触发指定数据管道执行批量同步任务。返回 taskId 用于后续状态查询。",
  "inputSchema": {
    "type": "object",
    "properties": {
      "pipelineName": {
        "type": "string",
        "description": "管道名称"
      },
      "parallelism": {
        "type": "integer",
        "description": "任务并行度，默认为1",
        "default": 1
      }
    },
    "required": ["pipelineName"]
  }
}
```

```json
{
  "name": "tis_get_task_status",
  "description": "查询数据同步任务的执行状态，包括当前阶段、进度、已读写行数等",
  "inputSchema": {
    "type": "object",
    "properties": {
      "taskId": {
        "type": "integer",
        "description": "任务ID"
      }
    },
    "required": ["taskId"]
  }
}
```

```json
{
  "name": "tis_cancel_task",
  "description": "取消正在执行的数据同步任务",
  "inputSchema": {
    "type": "object",
    "properties": {
      "taskId": {
        "type": "integer",
        "description": "任务ID"
      }
    },
    "required": ["taskId"]
  }
}
```

#### 4.3.4 增量同步操作

```json
{
  "name": "tis_start_incr_sync",
  "description": "启动指定管道的增量（实时）同步，基于 Flink CDC 实现变更数据捕获",
  "inputSchema": {
    "type": "object",
    "properties": {
      "pipelineName": { "type": "string" },
      "sourceDescriptorId": {
        "type": "string",
        "description": "CDC Source 插件描述符ID，如 FlinkCDCMySQLSourceFactory"
      },
      "sinkDescriptorId": {
        "type": "string",
        "description": "Sink 插件描述符ID，如 PaimonPipelineSinkFactory"
      }
    },
    "required": ["pipelineName"]
  }
}
```

```json
{
  "name": "tis_stop_incr_sync",
  "description": "停止指定管道的增量同步任务",
  "inputSchema": {
    "type": "object",
    "properties": {
      "pipelineName": { "type": "string" }
    },
    "required": ["pipelineName"]
  }
}
```

#### 4.3.5 插件元信息查询

```json
{
  "name": "tis_get_plugin_schema",
  "description": "获取指定TIS插件的JSON Schema配置模板，用于了解创建数据源或管道时需要哪些参数。返回 JSON Schema 和各字段的中文描述。",
  "inputSchema": {
    "type": "object",
    "properties": {
      "descriptorId": {
        "type": "string",
        "description": "插件描述符ID"
      }
    },
    "required": ["descriptorId"]
  }
}
```

```json
{
  "name": "tis_list_available_plugins",
  "description": "列出TIS中可用的插件列表，按扩展点类型（Reader/Writer/数据源）过滤",
  "inputSchema": {
    "type": "object",
    "properties": {
      "extendPoint": {
        "type": "string",
        "enum": ["DataxReader", "DataxWriter", "DataSourceFactory", "FlinkCDCSource", "FlinkSink"],
        "description": "扩展点类型"
      },
      "endType": {
        "type": "string",
        "description": "端类型过滤，如 MySQL、PostgreSQL、Paimon 等"
      }
    }
  }
}
```

### 4.4 Resource 定义

```json
[
  {
    "uri": "tis://pipelines",
    "name": "TIS Pipelines Overview",
    "description": "当前 TIS 实例中所有数据管道的概览信息",
    "mimeType": "application/json"
  },
  {
    "uri": "tis://pipelines/{name}",
    "name": "Pipeline Detail",
    "description": "指定管道的详细配置和运行状态",
    "mimeType": "application/json"
  },
  {
    "uri": "tis://datasources",
    "name": "TIS DataSources",
    "description": "已配置的所有数据源列表",
    "mimeType": "application/json"
  },
  {
    "uri": "tis://plugins/catalog",
    "name": "TIS Plugin Catalog",
    "description": "TIS 可用插件目录，包含所有 Reader/Writer/数据源插件的描述",
    "mimeType": "application/json"
  }
]
```

### 4.5 MCP Server 实现架构

```
┌────────────────────────────────────────┐
│           MCP Client (LLM/Agent)       │
└──────────────┬─────────────────────────┘
               │ JSON-RPC over HTTP/SSE
┌──────────────▼─────────────────────────┐
│         TIS MCP Server                 │
│  ┌─────────────────────────────────┐   │
│  │  MCPRequestHandler              │   │
│  │  - tools/list → Tool 列表       │   │
│  │  - tools/call → 路由到具体实现   │   │
│  │  - resources/read → 读取资源    │   │
│  └────────────┬────────────────────┘   │
│               │                        │
│  ┌────────────▼────────────────────┐   │
│  │  ToolExecutor (Tool 实现层)     │   │
│  │  - DataSourceTools              │   │
│  │  - PipelineTools                │   │
│  │  - TaskTools                    │   │
│  │  - PluginMetaTools              │   │
│  └────────────┬────────────────────┘   │
│               │                        │
│  ┌────────────▼────────────────────┐   │
│  │  TIS REST API Client            │   │
│  │  (内部调用 REST API 端点)        │   │
│  └─────────────────────────────────┘   │
└────────────────────────────────────────┘
```

### 4.6 Java 实现要点

MCP Server 以独立模块部署，可选嵌入 TIS Console 或独立进程运行：

```java
/**
 * TIS MCP Server 入口
 * 基于 Streamable HTTP 传输协议
 */
public class TISMCPServer {

    private final TISApiClient apiClient;

    /**
     * 注册所有 Tool
     */
    public void registerTools() {
        // 数据源操作
        registerTool("tis_list_datasources", this::listDatasources);
        registerTool("tis_create_datasource", this::createDatasource);
        registerTool("tis_test_datasource_connection", this::testConnection);
        registerTool("tis_list_tables", this::listTables);
        registerTool("tis_get_table_columns", this::getTableColumns);

        // 管道操作
        registerTool("tis_create_pipeline", this::createPipeline);
        registerTool("tis_list_pipelines", this::listPipelines);
        registerTool("tis_get_pipeline_detail", this::getPipelineDetail);

        // 任务执行
        registerTool("tis_trigger_pipeline", this::triggerPipeline);
        registerTool("tis_get_task_status", this::getTaskStatus);
        registerTool("tis_cancel_task", this::cancelTask);

        // 增量同步
        registerTool("tis_start_incr_sync", this::startIncrSync);
        registerTool("tis_stop_incr_sync", this::stopIncrSync);

        // 插件元信息
        registerTool("tis_get_plugin_schema", this::getPluginSchema);
        registerTool("tis_list_available_plugins", this::listAvailablePlugins);
    }
}
```

，这段代码是设计文档中的伪代码示意，registerTool 并不是某个现有 API 的实际调用。但如果要真正实现 MCP Server，确实需要依赖第三方库。

Java 生态中实现 MCP Server 的标准 SDK 是 Spring AI MCP（原名 mcp-java-sdk），由 Spring 团队维护：
``` xml
  <dependency>
      <groupId>io.modelcontextprotocol.sdk</groupId>
      <artifactId>mcp</artifactId>
      <version>0.10.0</version>
  </dependency>
```
用这个 SDK 时，注册 Tool 的实际写法大致是这样的：

``` java
McpServer server = McpServer.using(transport)
.serverInfo("tis-mcp-server", "1.0.0")
.tools(
new McpServerFeatures.SyncToolSpecification(
new Tool("tis_list_datasources", "列出所有数据源", inputSchema),
(exchange, args) -> {
// 调用 TIS Service 层
return new CallToolResult(content, false);
}
)
)
.build();
```
所以设计文档中的 registerTool 只是简化表达，实际实现时需要引入 io.modelcontextprotocol.sdk:mcp 这个依赖，并按照其 API 来注册 Tool

---

## 五、OpenClaw Skill 设计

### 5.1 Skill 概述

将 TIS 的数据管道能力封装为 OpenClaw 原生 Skill，使 OpenClaw Agent 能直接调用 TIS 的功能而无需额外配置。

### 5.2 SKILL.md 模板

```markdown
---
name: tis-data-pipeline
version: 1.0.0
description: TIS 数据集成平台 Skill - 支持创建和管理端到端的数据同步管道
author: TIS Community
tags: [data-integration, etl, cdc, pipeline, datax, flink]
requires:
  - mcp: tis-mcp-server
---

# TIS Data Pipeline Skill

## 能力概述

该 Skill 提供以下数据集成能力：

1. **数据源管理**：创建和测试 MySQL、PostgreSQL、Oracle、StarRocks、Doris 等 20+ 种数据源连接
2. **批量同步**：基于 DataX 引擎，将源端数据批量同步到目标端
3. **实时同步**：基于 Flink CDC 引擎，捕获源端变更数据实时写入目标端
4. **管道监控**：查询任务执行状态、进度、历史记录

## 使用场景

### 场景 1：创建 MySQL 到 Paimon 的批量同步管道
用户说："帮我把 MySQL 数据库 order_db 中的 orders 和 order_items 表同步到 Paimon 数仓"

执行步骤：
1. 调用 `tis_list_datasources` 检查是否已有 MySQL 数据源
2. 如果没有，调用 `tis_get_plugin_schema` 获取 MySQL 数据源配置模板
3. 询问用户 MySQL 连接信息（host、port、username、password）
4. 调用 `tis_create_datasource` 创建 MySQL 数据源
5. 调用 `tis_test_datasource_connection` 验证连接
6. 调用 `tis_list_tables` 确认表存在
7. 调用 `tis_create_pipeline` 创建同步管道
8. 调用 `tis_trigger_pipeline` 触发批量同步
9. 调用 `tis_get_task_status` 监控执行进度

### 场景 2：启动实时增量同步
用户说："我需要把 MySQL 的数据变更实时同步到 StarRocks"

执行步骤：
1. 确认已存在对应的数据管道
2. 调用 `tis_start_incr_sync` 启动增量同步
3. 定期调用 `tis_get_task_status` 监控 Flink 作业状态

### 场景 3：查询同步任务状态
用户说："上次的同步任务执行完了吗？"

执行步骤：
1. 调用 `tis_list_pipelines` 获取管道列表
2. 调用 `tis_get_task_status` 查询最近任务状态
3. 向用户展示进度和结果

## 配置要求

在 OpenClaw 的配置文件中添加：

```yaml
skills:
  - name: tis-data-pipeline
    config:
      tis_api_base_url: "http://localhost:8080/tis/api/v1"
      tis_api_key: "your-api-key"
      tis_mcp_endpoint: "http://localhost:8080/mcp"
```

## 错误处理

| 错误场景 | Skill 行为 |
|---------|-----------|
| 数据源连接失败 | 提示用户检查网络和凭据，建议重试 |
| 插件未安装 | 自动提示可用插件列表，引导用户安装 |
| 任务执行失败 | 展示错误日志，建议排查方向 |
| 参数缺失 | 逐步询问用户补充必要参数 |
```

### 5.3 交互流程设计

#### 5.3.1 典型交互流程

```
┌─────────────────────────────────────────────────────────────┐
│                    OpenClaw Agent                           │
│                                                             │
│  用户: "帮我把 MySQL 的 orders 表同步到 Paimon"              │
│                                                             │
│  Agent 思考链:                                              │
│  1. 识别意图 → 数据同步 → 激活 tis-data-pipeline Skill      │
│  2. 分析需求 → 源端:MySQL, 目标端:Paimon, 表:orders         │
│  3. 规划步骤 → 检查数据源 → 创建管道 → 触发同步              │
└──────────┬──────────────────────────────────────────────────┘
           │
           │ Step 1: 检查已有数据源
           ▼
    tis_list_datasources(type="MySQL")
           │
           │ 返回: 没有找到 MySQL 数据源
           ▼
    Agent → 用户: "需要创建 MySQL 数据源，请提供连接信息"
           │
           │ 用户提供: host=10.1.1.100, port=3306, user=root, pwd=***
           ▼
    tis_create_datasource(descriptorId="MySQLV5DataSourceFactory", ...)
           │
           │ Step 2: 验证连接
           ▼
    tis_test_datasource_connection(datasourceName="my_mysql")
           │
           │ Step 3: 确认表存在
           ▼
    tis_list_tables(datasourceName="my_mysql") → 确认 orders 表存在
           │
           │ Step 4: 创建管道
           ▼
    tis_create_pipeline(name="mysql2paimon_orders", ...)
           │
           │ Step 5: 触发同步
           ▼
    tis_trigger_pipeline(pipelineName="mysql2paimon_orders")
           │
           │ Step 6: 监控进度
           ▼
    tis_get_task_status(taskId=12345)
           │
           │ 返回: status=SUCCESS, totalRows=150000
           ▼
    Agent → 用户: "同步完成！共同步 150,000 行数据"
```

#### 5.3.2 错误恢复流程

```
    tis_test_datasource_connection → 连接失败
           │
           ▼
    Agent 分析错误 → "Connection refused"
           │
           ▼
    Agent → 用户: "MySQL 连接被拒绝，请确认:
                    1. MySQL 服务是否正在运行
                    2. 主机地址 10.1.1.100 是否正确
                    3. 端口 3306 是否被防火墙阻挡
                    请更正后告诉我"
           │
           │ 用户更正: host=10.1.1.200
           ▼
    tis_create_datasource(... host=10.1.1.200 ...)
    tis_test_datasource_connection → 连接成功
           │
           ▼
    继续后续步骤...
```

---

## 六、架构总览

### 6.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         用户 / 外部系统                              │
│   ┌───────────┐  ┌───────────────┐  ┌────────────────┐             │
│   │ OpenClaw  │  │  其他 AI Agent │  │  自定义应用     │             │
│   │  Agent    │  │  (LangChain等) │  │  (脚本/CI/CD)  │             │
│   └─────┬─────┘  └───────┬───────┘  └───────┬────────┘             │
│         │                │                   │                      │
└─────────┼────────────────┼───────────────────┼──────────────────────┘
          │                │                   │
          │  OpenClaw Skill│  MCP Protocol     │  HTTP REST
          │  (内置集成)     │  (标准协议)        │  (通用接口)
          ▼                ▼                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       TIS 接入层                                     │
│                                                                     │
│  ┌─────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │
│  │  OpenClaw Skill  │  │  MCP Server      │  │  REST API         │  │
│  │  Adapter         │  │  (JSON-RPC)      │  │  Controller       │  │
│  │                  │  │                  │  │  (Action层)       │  │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬──────────┘  │
│           │                     │                      │            │
│           └─────────────────────┼──────────────────────┘            │
│                                 │                                   │
│                    ┌────────────▼───────────────┐                   │
│                    │    API Service Layer       │                   │
│                    │    (统一业务逻辑层)          │                   │
│                    │                            │                   │
│                    │  - DataSourceService       │                   │
│                    │  - PipelineService         │                   │
│                    │  - TaskExecutionService    │                   │
│                    │  - PluginMetaService       │                   │
│                    │  - IncrSyncService         │                   │
│                    └────────────┬───────────────┘                   │
│                                 │                                   │
└─────────────────────────────────┼───────────────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────────────┐
│                       TIS 核心层 (现有)                              │
│                                                                     │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────────────────┐ │
│  │  Plugin SPI   │  │ DataXJobSubmit │  │  Flink CDC Integration  │ │
│  │  Extension    │  │ (AKKA/LOCAL/  │  │  (MQListenerFactory/    │ │
│  │  System       │  │  EMBEDDED)    │  │   TISSinkFactory)       │ │
│  └──────────────┘  └───────────────┘  └──────────────────────────┘ │
│                                                                     │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────────────────┐ │
│  │  AI Agent    │  │ PhaseStatus   │  │  Descriptor/             │ │
│  │  Infra       │  │ Collection    │  │  Describable System      │ │
│  │  (LLMProvider│  │ (任务状态跟踪) │  │  (插件元数据)             │ │
│  │   /Context)  │  │               │  │                          │ │
│  └──────────────┘  └───────────────┘  └──────────────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       数据处理引擎                                   │
│                                                                     │
│  ┌─────────────────────────┐  ┌─────────────────────────────────┐  │
│  │  DataX 批量同步引擎      │  │  Flink CDC 实时同步引擎          │  │
│  │  (全量数据导入/导出)      │  │  (变更数据捕获/实时写入)         │  │
│  └─────────────────────────┘  └─────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.2 模块划分

| 模块 | Maven Artifact | 职责 |
|------|---------------|------|
| `tis-api` | `tis-api` | REST API Controller 层，实现所有 HTTP 端点 |
| `tis-api-service` | `tis-api-service` | API 业务逻辑层，REST API 和 MCP Server 共用 |
| `tis-mcp-server` | `tis-mcp-server` | MCP Server 实现，基于 JSON-RPC 协议 |
| `tis-openclaw-skill` | `tis-openclaw-skill` | OpenClaw Skill 打包，包含 SKILL.md 和适配代码 |

### 6.3 与现有模块的关系

```
tis-api-service
  ├── 依赖 tis-plugin       (插件 SPI 系统、Descriptor 元数据)
  ├── 依赖 tis-manage-pojo  (PhaseStatusCollection、数据模型)
  ├── 依赖 tis-builder-api  (DataXJobSubmit、任务执行)
  └── 调用 tis-console      (复用现有的 Action 业务逻辑)

tis-mcp-server
  └── 依赖 tis-api-service

tis-api (REST Controller)
  └── 依赖 tis-api-service

tis-openclaw-skill
  └── 依赖 tis-mcp-server (通过 MCP 协议与 TIS 通信)
```

---

## 七、典型使用场景

### 7.1 场景 1：OpenClaw Agent 自动创建数据管道

**用户对话**：
> "我有一个 MySQL 数据库 (10.1.1.100:3306/order_db)，需要每天把 orders 和 order_items 两张表同步到 Paimon 数仓 (HDFS: hdfs://ns1:8020/paimon/warehouse)，同时还需要实时同步增量数据。"

**Agent 执行计划**：

| 步骤 | Tool 调用 | 说明 |
|------|----------|------|
| 1 | `tis_list_datasources(type="MySQL")` | 检查已有 MySQL 数据源 |
| 2 | `tis_get_plugin_schema(descriptorId="MySQLV5DataSourceFactory")` | 获取配置模板 |
| 3 | `tis_create_datasource(...)` | 创建 MySQL 数据源 |
| 4 | `tis_test_datasource_connection(...)` | 验证连接 |
| 5 | `tis_list_tables(...)` | 确认表存在 |
| 6 | `tis_get_table_columns(datasourceName, "orders")` | 获取表结构 |
| 7 | `tis_create_pipeline(syncMode="both", ...)` | 创建管道（批量+增量） |
| 8 | `tis_trigger_pipeline(...)` | 触发首次全量同步 |
| 9 | `tis_get_task_status(...)` | 等待全量完成 |
| 10 | `tis_start_incr_sync(...)` | 启动增量同步 |

### 7.2 场景 2：通过 MCP 与 Claude Desktop 集成

用户在 Claude Desktop 中配置 TIS MCP Server：

```json
{
  "mcpServers": {
    "tis": {
      "url": "http://localhost:8080/mcp",
      "headers": {
        "Authorization": "Bearer your-api-key"
      }
    }
  }
}
```

然后直接在对话中操作：

> 用户："帮我查看一下 TIS 里有哪些数据管道在运行"
>
> Claude：调用 `tis_list_pipelines(status="active")`，返回结果并格式化展示

### 7.3 场景 3：CI/CD 自动化

通过 REST API 在 CI/CD 流水线中自动触发数据同步：

```bash
# 触发批量同步
TASK_ID=$(curl -s -X POST \
  http://tis-server:8080/tis/api/v1/pipelines/mysql2paimon_orders/trigger \
  -H "Authorization: Bearer $TIS_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"triggerType": "batch"}' | jq -r '.data.taskId')

# 轮询检查状态
while true; do
  STATUS=$(curl -s http://tis-server:8080/tis/api/v1/tasks/$TASK_ID/status \
    -H "Authorization: Bearer $TIS_API_KEY" | jq -r '.data.status')
  if [ "$STATUS" = "SUCCESS" ]; then
    echo "Sync completed successfully"
    break
  elif [ "$STATUS" = "FAILED" ]; then
    echo "Sync failed"
    exit 1
  fi
  sleep 10
done
```

---

## 八、实施路线图

### 第一阶段：REST API 基础

**目标**：实现核心 REST API 端点

- 实现 API Key 认证机制（基于 `ParamsConfig` 扩展）
- 实现数据源管理 API（CRUD + 连接测试）
- 实现管道管理 API（创建、查询、删除）
- 实现任务执行 API（触发、状态查询、取消）
- API 统一响应格式和错误处理
- 集成测试

### 第二阶段：API 增强

**目标**：完善 API 功能

- 实现增量同步管理 API
- 实现插件元信息查询 API（Schema 导出）
- 复用 `JsonSchema` 体系生成插件配置模板
- API 文档生成（OpenAPI/Swagger）
- 分页、过滤、排序支持

### 第三阶段：MCP Server

**目标**：实现 MCP Server

- 基于 REST API 实现所有 MCP Tool
- 实现 MCP Resource 端点
- Streamable HTTP 传输协议支持
- SSE 传输协议兼容支持
- 与 Claude Desktop 集成测试
- 与主流 MCP 客户端兼容性测试

### 第四阶段：OpenClaw Skill 集成

**目标**：封装 OpenClaw 原生 Skill

- 编写 SKILL.md 定义文件
- 实现 Skill Adapter（OpenClaw Skill API → MCP/REST 调用）
- 典型场景端到端测试
- Skill 发布到 OpenClaw Skill Registry
- 编写用户使用文档

### 第五阶段：生产化与优化

**目标**：生产环境就绪

- API 限流和熔断保护
- 操作审计日志
- 监控指标暴露（Prometheus metrics）
- 性能优化（批量操作、异步执行）
- 安全加固（输入校验、SQL 注入防护、敏感信息脱敏）
- 多租户支持

---

## 九、现有 AI Agent 代码复用与迁移清单

### 9.1 概述

TIS 已在 `tis-plugin` 模块的 `com.qlangtech.tis.aiagent` 包中构建了一套 AI Agent 基础设施（共 33 个相关文件）。在转向 MCP 标准协议集成的技术路线后，需要对这些代码进行系统性的复用/废弃分析。

以下按四个层次分类：**可直接复用 → 业务逻辑可提炼复用 → Agent 编排层应废弃 → Agent 会话/交互层应废弃**。

### 9.2 第一层：可直接复用（tis-plugin 层基础设施）

这些组件提供的能力与 MCP 集成方向完全兼容，可直接沿用。

| 文件 | 模块 | 复用方式 |
|------|------|---------|
| `JsonSchema.java` | tis-plugin | MCP Tool 的 `inputSchema` 定义可直接使用 Builder 模式生成 |
| `DescriptorsJSONForAIPrompt.java` | tis-plugin | 其 `AISchemaDescriptorsMeta` 从 Descriptor 自动生成 JSON Schema，是 `tis_get_plugin_schema` MCP Tool 的核心实现 |
| `DescribableImpl.java` | tis-plugin | 插件扩展点与实现的映射关系，MCP Tool 查找正确的插件实现时需要 |

### 9.3 第二层：业务逻辑可提炼复用（需要从 Agent 编排中解耦）

这些文件中的核心业务逻辑（创建插件、校验配置、触发任务）可直接对应到 MCP Tool 的实现，但当前与 `TaskPlan`、`AgentContext`、`LLMProvider` 强耦合，需要提取到独立的 Service 层。

| 源文件 | 可复用方法/逻辑 | 对应 MCP Tool |
|--------|---------------|-------------|
| `BasicStepExecutor.java` | `createPluginInstance()` 创建插件实例 | `tis_create_datasource` |
| `BasicStepExecutor.java` | `validateAttrValMap()` 校验插件配置 | `tis_create_datasource`, `tis_create_pipeline` |
| `BasicStepExecutor.java` | `checkInstallPlugin()` 检查并安装插件 | 所有创建类 Tool 的前置检查 |
| `PluginInstanceCreateExecutor.java` | 创建 DataxProcessor/Reader/Writer 完整流程 | `tis_create_pipeline` |
| `PluginInstanceCreateExecutor.java` | 表选择和保存逻辑 | `tis_create_pipeline` (selectedTables 参数) |
| `PluginInstanceCreateExecutor.java` | `DataxAction.generateDataXCfgs()` 调用 | `tis_create_pipeline` 完成后自动生成 |
| `PluginDownloadAndInstallExecutor.java` | 插件自动安装流程 | 所有创建类 Tool 的前置步骤 |
| `PipelineBatchExecutor.java` | 触发批量同步（`DataXJobSubmit`） | `tis_trigger_pipeline` |
| `PipelineIncrExecutor.java` | 增量同步完整流程（Flink 插件安装 → StreamFactory/MQ/Sink 创建 → 部署） | `tis_start_incr_sync` |

**解耦要点**：上述逻辑当前与 `TaskPlan`、`AgentContext`、`LLMProvider` 强耦合。需要将纯业务操作（创建插件、校验配置、触发任务）提取到独立的 Service 层，去除 LLM 调用和 SSE 会话管理的依赖。

### 9.4 第三层：应废弃（Agent 编排层 — 由外部 Agent 替代）

Plan-And-Execute 编排逻辑将完全由外部 Agent（OpenClaw / Claude Desktop 等）承担，TIS 仅作为 Tool 提供方，不再维护自己的 Agent 编排。

| 文件 | 废弃原因 |
|------|---------|
| `TISPlanAndExecuteAgent.java` | Plan-And-Execute 编排由外部 Agent（OpenClaw/Claude Desktop 等）承担 |
| `PlanGenerator.java` | LLM 意图解析和计划生成不再需要 |
| `TaskPlan.java` | 执行计划模型与内部 Agent 编排强绑定 |
| `TaskStep.java` | 步骤模型与内部 Agent 编排强绑定 |
| `AgentTaskIntention.java` | 意图枚举不再需要 |
| `StepExecutor.java` | 步骤执行器接口与内部步骤模型强绑定 |
| `TaskTemplateRegistry.java` | 任务模板不再需要 |

### 9.5 第四层：应废弃（Agent 会话/交互层 — 由 MCP 协议替代）

Agent 运行时所需的会话管理、用户交互机制将由 MCP 传输协议和外部 Agent 的 UI 层替代。

| 文件 | 废弃原因 |
|------|---------|
| `AgentContext.java` | SSE 会话管理、锁/等待机制由 MCP 传输协议替代 |
| `IAgentContext.java` | Agent 上下文接口由 MCP 请求上下文替代 |
| `RequestKey.java` | 异步请求标识由 MCP JSON-RPC requestId 替代 |
| `SelectionOptions.java` | 用户选择交互由外部 Agent UI 处理 |
| `ISessionData.java` | 会话数据由 MCP 会话管理替代 |
| `PluginPropsComplement.java` | 属性补全由外部 Agent 驱动 |
| `TableSelectApplySessionData.java` | 表选择会话不再需要 |
| `ColsMetaSetterSessionData.java` | 列元信息会话不再需要 |
| `NormalSelectionOption.java` | 普通选项不再需要 |
| `PipelineSourceSelectTabsExecutor.java` | 空实现，无实际逻辑 |

### 9.6 LLM 层评估

| 文件 | 建议 |
|------|------|
| `LLMProvider.java` | **保留但降低优先级**。MCP 场景下 TIS 不直接调用 LLM（由外部 Agent 完成），但如果未来 TIS 还有其他 AI 辅助功能（智能推荐配置等）可保留 |
| `UserPrompt.java` | 与 `LLMProvider` 同步处理 |

### 9.7 迁移策略

将第二层中各 Executor 的核心业务逻辑提取到第六章定义的 `tis-api-service` 模块 Service 类中，方法映射如下：

```
BasicStepExecutor.createPluginInstance()     → DataSourceService.createDataSource()
BasicStepExecutor.validateAttrValMap()        → PluginService.validatePluginConfig()
BasicStepExecutor.checkInstallPlugin()        → PluginService.ensurePluginsInstalled()
PluginInstanceCreateExecutor.execute()        → PipelineService.createPipeline()
PipelineBatchExecutor.execute()               → TaskExecutionService.triggerBatchSync()
PipelineIncrExecutor.execute()                → IncrSyncService.startIncrementalSync()
```

提取后的 Service 层将同时被 REST API Controller 和 MCP Server ToolExecutor 调用，保证逻辑统一（参见 6.1 架构图中的 API Service Layer）。

---

## 附录

### A. 已有核心代码引用

| 文件 | 模块 | 关键能力 |
|------|------|---------|
| `DataXJobSubmit.java` | tis-plugin | 数据同步任务提交 SPI，支持 AKKA/LOCAL/EMBEDDED 模式 |
| `DataXJobSubmitParams.java` | tis-plugin | 任务运行参数（并行度、内存规格、超时） |
| `LLMProvider.java` | tis-plugin (aiagent/llm) | 大模型抽象接口，支持 chat/chatJson |
| `IAgentContext.java` | tis-plugin (aiagent/core) | Agent 上下文，消息推送和状态回调 |
| `JsonSchema.java` | tis-plugin (aiagent/llm) | JSON Schema 动态生成，支持插件配置模板导出 |
| `AIAssistSupport.java` | tis-plugin (extension) | AI 辅助支持基类，自动化环境部署 |
| `PhaseStatusCollection.java` | tis-manage-pojo | 任务阶段状态跟踪（FullDump/JOIN） |
| `DescribableImpl.java` | tis-plugin (aiagent/plan) | 插件元数据描述，关联扩展点与实现 |

### B. TIS 支持的数据端类型（部分）

**Reader（数据源端）**：MySQL、PostgreSQL、Oracle、SQL Server、MongoDB、Elasticsearch、Kafka、HDFS、S3、FTP

**Writer（目标端）**：Paimon、StarRocks、Doris、ClickHouse、Elasticsearch、Hive、HDFS、Kafka、PostgreSQL、MySQL
