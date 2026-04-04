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
1. 调用 `list_plugin_types` 检查可用插件
2. 调用 `get_plugin_schema` 获取 MySQL 数据源配置模板
3. 询问用户 MySQL 连接信息（host、port、username、password）
4. 调用 `create_plugin_instance` 创建 MySQL 数据源
5. 调用 `create_pipeline` 创建同步管道
6. 触发批量同步并监控执行进度

