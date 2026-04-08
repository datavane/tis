# TIS MCP Tool 设计方案

## 一、设计哲学

### 1.1 核心原则：Tool 是 LLM 的「传感器」，不是「执行器」

经过实践验证，在当前 MCP 协议生态下（OpenClaw 等主流客户端尚未支持 Sampling 和 Elicitation），复杂的创建类 Tool（如端到端管道创建）面临以下问题：

- 多 Tool 协作编排，LLM 执行逻辑不可控
- 参数结构复杂，LLM 极易生成错误参数
- 测试难度大，难以保证稳定性

参考 Apache Doris MCP Server 等业界实践，成熟的 MCP Server 普遍遵循 **「读多写少、查询优先」** 的设计模式：

```
✅ 适合 MCP Tool：查询/检索、诊断/分析、参数简单的轻量操作
❌ 不适合 MCP Tool：复杂创建、多步编排、破坏性操作
```

### 1.2 TIS MCP 的差异化价值

TIS 作为数据集成平台，其 MCP 的独特价值不在于「通过 AI 创建管道」，而在于：

> **让开发者在 AI IDE（Claude Desktop / Cursor / OpenClaw）中拥有「数据集成平台的全局视角」——
> 知道数据从哪来、怎么流、现在什么状态、出了什么问题。**

这是一个真实痛点：开发者写代码时经常需要这些信息，但现在必须切到 TIS UI 去查。MCP 把这个信息差抹平了。

对比 Doris MCP（只能看到单个数据库内部），TIS MCP 能提供 **跨数据源的全局数据流视角**，这是 TIS 独有的能力。

### 1.3 典型用户场景

| 场景 | 用户在 IDE 中的提问 | TIS MCP 的价值 |
|------|-------------------|---------------|
| 写报表代码 | "dwd_order_detail 表的数据从哪来的？同步延迟多少？" | 数据血缘追溯 |
| 排查数据问题 | "线上订单数据不对，同步管道是不是挂了？" | 管道状态 + 错误日志 |
| 新增字段 | "上游 MySQL 有 discount_rate 这个字段吗？" | 跨数据源表结构查询 |
| 上线前检查 | "所有管道最近一周的执行成功率怎么样？" | 执行历史统计 |
| 触发同步 | "帮我跑一下 mysql2doris_orders 的全量同步" | 轻量操作（参数极简） |

---

## 二、Tool 清单

### 2.1 总览

按优先级分三层，共 12 个 Tool：

| 层级 | 定位 | Tool 数量 | 参数复杂度 |
|------|------|----------|-----------|
| 第一层 | 数据资产感知 | 6 个 | 极低 |
| 第二层 | 运维诊断 | 4 个 | 低 |
| 第三层 | 轻量操作 | 2 个 | 极低 |

### 2.2 第一层：数据资产感知（最高优先级）

这是 TIS 独有的、其他单一数据库 MCP Server 做不到的能力 —— **跨数据源的全局视角**。

#### tis_list_datasources

列出 TIS 中已配置的所有数据源。

```json
{
  "name": "tis_list_datasources",
  "description": "列出TIS中已配置的所有数据源，返回数据源名称、类型、连接摘要信息",
  "inputSchema": {
    "type": "object",
    "properties": {
      "type": {
        "type": "string",
        "description": "按数据源类型过滤，如 MySQL、PostgreSQL、Doris 等"
      }
    }
  }
}
```

输出示例：
```json
{
  "datasources": [
    { "name": "order_mysql", "type": "MySQL", "host": "10.1.1.100", "port": 3306, "database": "order_db" },
    { "name": "analytics_doris", "type": "Doris", "host": "10.1.1.200", "port": 9030, "database": "analytics" }
  ]
}
```

#### tis_list_pipelines

列出所有数据同步管道。

```json
{
  "name": "tis_list_pipelines",
  "description": "列出TIS中所有端到端数据同步管道，返回管道名称、源端/目标端类型、同步模式、运行状态",
  "inputSchema": {
    "type": "object",
    "properties": {}
  }
}
```

输出示例：
```json
{
  "pipelines": [
    {
      "name": "mysql2doris_orders",
      "readerType": "MySQL",
      "writerType": "Doris",
      "tables": ["orders", "order_items"],
      "supportBatchSync": true,
      "supportIncrSync": true,
      "incrSyncRunning": true
    }
  ]
}
```

#### tis_get_pipeline_detail

获取管道详细配置：源表、目标表、字段映射、同步模式。

```json
{
  "name": "tis_get_pipeline_detail",
  "description": "获取指定数据管道的详细配置，包括源端/目标端插件配置、同步表列表、字段映射关系",
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

#### tis_list_tables

列出某个数据源下的所有表。

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

#### tis_get_table_columns

获取表的列元数据。

```json
{
  "name": "tis_get_table_columns",
  "description": "获取指定数据源中某张表的列元信息，包括列名、数据类型、是否主键、是否可空",
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

#### tis_get_data_lineage

**数据血缘追溯** —— TIS MCP 的杀手级功能。给定一个目标表，追溯它的数据来源链路。

```json
{
  "name": "tis_get_data_lineage",
  "description": "数据血缘追溯：给定目标端的数据源名和表名，查找TIS中哪个管道负责向该表同步数据，返回完整的数据流转链路（源数据源→管道→目标数据源）",
  "inputSchema": {
    "type": "object",
    "properties": {
      "datasourceName": {
        "type": "string",
        "description": "目标端数据源名称"
      },
      "tableName": {
        "type": "string",
        "description": "目标端表名"
      }
    },
    "required": ["tableName"]
  }
}
```

输出示例：
```json
{
  "found": true,
  "lineage": {
    "target": { "datasource": "analytics_doris", "table": "dwd_order_detail" },
    "pipeline": { "name": "mysql2doris_orders", "syncMode": "batch+incr" },
    "source": { "datasource": "order_mysql", "table": "orders", "database": "order_db" }
  }
}
```

### 2.3 第二层：运维诊断（高优先级）

开发者在 IDE 里排查数据问题时最需要的能力。

#### tis_get_pipeline_status

获取管道当前运行状态。

```json
{
  "name": "tis_get_pipeline_status",
  "description": "获取指定管道的当前运行状态，包括批量同步最近一次执行结果、增量同步是否运行中及其延迟指标",
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

输出示例：
```json
{
  "pipelineName": "mysql2doris_orders",
  "batch": {
    "lastExecStatus": "SUCCESS",
    "lastExecTime": "2026-04-03T08:30:00Z",
    "totalRows": 298500
  },
  "incr": {
    "running": true,
    "flinkJobId": "flink-job-xxx",
    "delayMs": 1200,
    "tps": 350
  }
}
```

#### tis_get_pipeline_exec_history

获取管道最近的执行记录。

```json
{
  "name": "tis_get_pipeline_exec_history",
  "description": "获取指定管道最近N次批量同步的执行记录，包括每次执行的状态、耗时、读写行数",
  "inputSchema": {
    "type": "object",
    "properties": {
      "pipelineName": {
        "type": "string",
        "description": "管道名称"
      },
      "limit": {
        "type": "integer",
        "description": "返回最近几次记录，默认10",
        "default": 10
      }
    },
    "required": ["pipelineName"]
  }
}
```

#### tis_get_task_log

获取某次执行的日志（尤其是错误日志）。

```json
{
  "name": "tis_get_task_log",
  "description": "获取指定任务的执行日志，支持按日志级别过滤。排查同步失败原因时使用",
  "inputSchema": {
    "type": "object",
    "properties": {
      "taskId": {
        "type": "integer",
        "description": "任务ID，可通过 tis_get_pipeline_exec_history 获取"
      },
      "level": {
        "type": "string",
        "enum": ["ALL", "ERROR", "WARN"],
        "description": "日志级别过滤，默认ERROR",
        "default": "ERROR"
      },
      "limit": {
        "type": "integer",
        "description": "返回最近几条日志，默认50",
        "default": 50
      }
    },
    "required": ["taskId"]
  }
}
```

#### tis_get_incr_sync_status

获取增量同步的详细状态。

```json
{
  "name": "tis_get_incr_sync_status",
  "description": "获取指定管道增量（实时）同步的详细运行状态，包括Flink作业状态、消费延迟、TPS、checkpoint信息",
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

### 2.4 第三层：轻量操作（参数极简，LLM 不会出错）

这些操作参数都只有一个 `pipelineName`，确定性高，适合通过 MCP Tool 执行。

#### tis_trigger_batch_sync

触发一次批量全量同步。

```json
{
  "name": "tis_trigger_batch_sync",
  "description": "触发指定管道执行一次批量全量数据同步，返回taskId用于后续状态查询",
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

#### tis_toggle_incr_sync

启动或停止增量同步。

```json
{
  "name": "tis_toggle_incr_sync",
  "description": "启动或停止指定管道的增量（实时）同步",
  "inputSchema": {
    "type": "object",
    "properties": {
      "pipelineName": {
        "type": "string",
        "description": "管道名称"
      },
      "action": {
        "type": "string",
        "enum": ["start", "stop"],
        "description": "操作类型：start=启动, stop=停止"
      }
    },
    "required": ["pipelineName", "action"]
  }
}
```

---

## 三、已废弃的 Tool（及废弃原因）

以下 Tool 在之前的设计中存在，经实践验证后决定在本版本中移除：

| Tool | 废弃原因 |
|------|---------|
| `CreatePipelinePrepareTool` | 参数结构极其复杂（Reader/Writer/表选择/字段映射），需要多 Tool 协作，LLM 不可控 |
| `CreatePipelineCommitTool` | 依赖 `CreatePipelinePrepareTool` 的会话状态，多步编排不稳定 |
| `CreatePluginInstanceTool` | 插件参数结构复杂（嵌套 JSON），LLM 极易生成错误参数 |
| `ClarificationSubmitTool` | 为弥补 Elicitation 缺失而设计的补丁，去掉创建流程后不再需要 |
| `GetPluginSchemaTool` | 主要服务于创建流程，去掉后意义不大 |
| `ListPluginTypesTool` | 同上 |
| `InstallPluginTool` | 同上 |
| `DataSourceCreateTool` | 参数复杂，应通过 TIS UI 完成 |

> 这些创建类功能应引导用户到 TIS Web UI 完成。等 MCP 协议的 Sampling 和 Elicitation 在各客户端普遍落地后，可重新评估。

---

## 四、与现有代码的映射

### 4.1 已有 Tool 复用情况

| 新 Tool | 已有代码 | 状态                                                                  |
|---------|---------|---------------------------------------------------------------------|
| `tis_list_datasources` | 无，需新建 | 对应 `HeteroEnum.DATASOURCE.getExistItems()`, ListDatasourcesTool     |
| `tis_list_pipelines` | `PipelineListTool.java`（骨架已有，`execHandle` 返回 null） | 需补充实现                                                               |
| `tis_get_pipeline_detail` | 无，需新建 | 对应 `DataxAction.doDataxProcessorDesc()` ,GetPipelineDetailTool.java |
| `tis_list_tables` | `ListTablesTool.java`（骨架已有，`execHandle` 返回 null） | 需补充实现                                                               |
| `tis_get_table_columns` | `GetTableColumnsTool.java`（骨架已有，返回空结果） | 需补充实现                                                               |
| `tis_get_data_lineage` | 无，需新建 | 需遍历所有管道的 Reader/Writer 配置反向查找                                       |
| `tis_get_pipeline_status` | `PipelineGetTaskStatusTool.java`（骨架已有） | 需补充实现，对应 `CoreAction.doGetIncrStatus()`                             |
| `tis_get_pipeline_exec_history` | 无，需新建 | 对应 `CoreAction.doGetFullBuildHistory()`                             |
| `tis_get_task_log` | 无，需新建 | 对应 `CoreAction.doDownloadTaskLog()`  ,GetTaskLogTool.java           |
| `tis_get_incr_sync_status` | 无，需新建 | 对应 `CoreAction.doGetIncrStatus()`                                   |
| `tis_trigger_batch_sync` | `PipelineTriggerBatchTool.java`（骨架已有） | 需补充实现，对应 `CoreAction.doTriggerDump()`                               |
| `tis_toggle_incr_sync` | `PipelineStartIncrSyncTool.java`（骨架已有） | 需补充实现，对应 `CoreAction.doStartIncrSyncChannal()` / `doIncrStop()`     |

### 4.2 应删除的 Tool 文件

```
tis-console/src/main/java/com/qlangtech/tis/mcp/tools/
├── ClarificationSubmitTool.java          ← 删除
├── CreatePluginInstanceTool.java         ← 删除
├── DataSourceCreateTool.java             ← 删除
├── GetPluginSchemaTool.java              ← 删除
├── InstallPluginTool.java                ← 删除
├── ListPluginTypesTool.java              ← 删除
├── RequireUserAddressElicitationTool.java ← 删除（测试用）
├── RequireUserAddressTool.java           ← 删除（测试用）
├── TestDatasourceConnectionTool.java     ← 删除（已注释）
└── pipeline/
    ├── CreatePipelinePrepareTool.java    ← 删除
    └── CreatePipelineCommitTool.java     ← 删除
```

---

## 五、实施计划

### 第一阶段：数据资产感知（6 个 Tool）

优先实现第一层，让 TIS MCP 能在 AI IDE 中提供基本的数据资产查询能力。

1. 实现 `tis_list_datasources`、`tis_list_pipelines`、`tis_list_tables`、`tis_get_table_columns`
2. 实现 `tis_get_pipeline_detail`
3. 实现 `tis_get_data_lineage`（杀手级功能）
4. 删除废弃的 Tool 文件，清理 `TISHttpMcpServer.getMcpProvider()` 中的 Tool 注册列表
5. 与 Claude Desktop 集成测试

### 第二阶段：运维诊断（4 个 Tool）

1. 实现 `tis_get_pipeline_status`、`tis_get_incr_sync_status`
2. 实现 `tis_get_pipeline_exec_history`、`tis_get_task_log`
3. 端到端场景测试：模拟「数据不对 → 查管道状态 → 看错误日志 → 定位问题」

### 第三阶段：轻量操作（2 个 Tool）

1. 实现 `tis_trigger_batch_sync`、`tis_toggle_incr_sync`
2. 安全性考虑：操作类 Tool 需要确认机制（依赖 MCP 客户端的 Tool 调用确认弹窗）

---

## 六、Claude Desktop 配置示例

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

配置后，用户可以在 Claude Desktop 中直接提问：

> "TIS 里有哪些数据管道？mysql2doris_orders 最近执行情况怎么样？"
>
> "Doris 里的 dwd_order_detail 表数据是从哪同步过来的？"
>
> "帮我跑一下 mysql2doris_orders 的全量同步"