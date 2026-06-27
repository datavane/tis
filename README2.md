## Stargazers over time

[![Stargazers over time](https://starchart.cc/qlangtech/tis-solr.svg)](https://starchart.cc/qlangtech/tis-solr)

![](https://tokei.rs/b1/github/datavane/tis)


## 介绍

![tis](docs/tis-synoptic2.png)

**TIS** 是新一代 **AI 原生数据集成平台**。在大模型重塑行业的今天，TIS 率先将 LLM 深度融入数据工程全链路——

> 🗣️ **用自然语言创建数据管道**：告别复杂配置，一句话描述业务需求，Pipeline AI Agent 自动完成插件选型、参数生成与任务编排  
> 🧠 **用知识图谱理解业务语义**：Ontology 本体层 + Neo4j 图数据库，将业务术语、指标口径、实体关系沉淀为可持续积累的企业知识资产  
> 💬 **用自然语言洞察数据**：ChatBI 模块基于 GraphRAG 四路并行检索，业务用户无需 SQL，直接提问即得数据分析结果

底层仍保留经过生产验证的 **批流一体引擎**（DataX / Flink-CDC / Chunjun），让 AI 能力叠加在坚实的数据基础设施之上。[详细介绍 →](https://tis.pub/docs/)

## :fire: 推出新功能

### 1. ChatBI — 自然语言驱动数据洞察

基于 **Ontology知识图谱 + GraphRAG检索增强** 技术，TIS v5.0.0 引入 ChatBI 能力，让业务用户无需编写 SQL，直接用自然语言提问即可获得数据分析结果：

- **语义理解**：Ontology 本体层精确定义业务实体（Glossary词典、聚合函数、语义角色等），消除歧义
- **图谱检索**：GraphRAG 四路并行召回 + 子图扩展，精准定位相关数据实体及其关联关系
- **多模型支持**：无缝接入 DeepSeek、通义千问、GPT-4 等主流 LLM
- **知识沉淀**：Neo4j 图数据库持久化业务知识，随着使用不断积累，越用越聪明

```
用户问："上个月华东区销售额最高的产品是哪些？"
   ↓ Ontology 语义解析
   ↓ GraphRAG 知识检索
   ↓ LLM SQL生成
   ↓ 结果可视化
```

### 2. Pipeline AI Agent — 让数据管道听懂人话

- 告别复杂配置，自然语言描述需求即可创建管道
- 智能插件检测与自动安装，LLM辅助参数生成
- 支持DeepSeek/通义千问等国产模型，成本低至几毛钱 [立即了解](https://tis.pub/docs/guide/pipeline-ai-agent)

![tis](docs/ai-agent-pipeline.png)

## 核心特性

* :hammer:安装简单

  TIS的安装还是和传统软件安装一样，只需要三个步骤：一、下载tar包，二、解压tar包，三、启动TIS。是的，就这么简单。另外还支持多种部署模式（Docker、Docker-Compose、Kubernetes）。

* :rocket:强大的扩展性

  TIS 继承了Jenkin 的设计思想，使用微前端技术，重新构建了一套前端框架，前端页面可以自动渲染。

  TIS 提供了良好的扩展性和SPI机制，开发者可以很轻松地开发新的插件。

* :brain: AI驱动的智能运维

  v5.0.0 引入 Ontology 知识图谱 + GraphRAG 检索增强生成架构，构建以下核心能力：
    - **本体建模**：精确定义 Glossary 词典、聚合函数（SUM/AVG/COUNT等10+种）、5种语义角色
    - **图谱存储**：基于 Neo4j 的知识图谱持久化，实体关系清晰可查
    - **智能问答**：ChatBI 模块让业务用户用自然语言驱动数据查询与分析
    - **LLM集成**：支持主流大语言模型，打通从自然语言到 SQL 的全链路

* :repeat:支持各种Transformer算子

  通过在TIS流程中设置Transformer功能逻辑，可实现在ETL数仓功能中，在E（数据抽取）阶段即可高效实现各种字段值转换功能，如：字段脱敏，字段拼接，取子字符串等功能。

* :smile:易与大数据生态整合

  支持与DolphinScheduler整合，用户只需在TIS中配置数据管道，验证无误后，一键推送同步表任务到DolphinScheduler平台，即可在其上完成数据同步任务调度。为以DolphinScheduler构建ETL数仓方案如虎添翼。

* :ok_hand: 基于 DataOps 理念

  借鉴了 DataOps、DataPipeline 理念，对各各执行流程建模。不需要了解底层模块的实现原理，基本实现傻瓜化方式操作。

## :arrow_forward: 安装说明

快速安装（支持私有云、公有云等环境），方便、快捷 [:green_circle:单机](https://tis.pub/docs/install/tis/uber) [:purple_circle:Docker](https://tis.pub/docs/install/tis/docker) [:large_blue_circle:Docker Compose](https://tis.pub/docs/install/tis/docker-compose) [:brown_circle:K8S](https://tis.pub/docs/install/tis/kubernetes)

## 发布：

最新版本：v5.0.0（2025/12/29) https://github.com/datavane/tis/releases/tag/v5.0.0

**历史发布**：
* v4.3.0（2025/8/19) https://github.com/datavane/tis/releases/tag/v4.3.0
* v4.2.1 发布（2025/5/1）：https://github.com/datavane/tis/releases/tag/v4.2.1
* v4.1.0 发布（2025/2/2）：https://github.com/datavane/tis/releases/tag/v4.1.0
* v4.0.1 发布（2024/10/19）：https://github.com/datavane/tis/releases/tag/V4.0.1









## 支持的读写组件 

[https://tis.pub/docs/plugin/source-sink/](https://tis.pub/docs/plugin/source-sink/)


## 功能一瞥
- 示例
    * [基于TIS快速实现MySQL到StarRocks的实时数据同步方案](https://tis.pub/docs/example/mysql-sync-starrocks)
    * [多源同步Doris方案](https://tis.pub/docs/example/mysql-sync-doris)
    * [将数据变更同步到Kafka](https://tis.pub/docs/example/sink-2-kafka)
    * [利用TIS实现T+1离线分析](https://tis.pub/docs/example/dataflow)
    * [多源同步Paimon方案](https://tis.pub/docs/example/paimon/)
- 视频示例
    * [安装示例](https://www.bilibili.com/video/BV18q4y1p73B/)
    * [启用分布式执行功能](https://www.bilibili.com/video/BV1Cq4y1D7z4?share_source=copy_web)
    * [MySQL导入ElasticSearch](https://www.bilibili.com/video/BV1G64y1B7wm?share_source=copy_web)
    * [MySQL导入Hive](https://www.bilibili.com/video/BV1Vb4y1z7DN?share_source=copy_web)
    * [MySQL导入Clickhouse](https://www.bilibili.com/video/BV1x64y1B7V8/)
    * [MySQL同步StarRocks](https://www.bilibili.com/video/BV19o4y1M7eq/)
    * MySQL同步Doris [>批量同步](https://www.bilibili.com/video/BV1eh4y1o7yQ) [>实时同步](https://www.bilibili.com/video/BV1nX4y1h7SW)


### 批量导入流程设置

选择Reader/Writer插件类型
![tis](docs/datax-add-step2.png)

添加MySqlReader
![tis](docs/add-mysql-reader.png)

设置MySqlReader目标表、列  
![tis](docs/select-tab-cols.png)

设置MySqlReader目标列设置Transformer逻辑。例如，将表中JSON内容的列分拆成多个字段`json_splitter`
![tis](docs/select-tab-cols-json-splitter.png)

添加ElasticWriter,可视化设置ElasticSearch的Schema Mapping
![tis](docs/add-elastic-writer.png)

执行MySql->ElasticSearch DataX实例，运行状态
![tis](docs/datax-exec-status.png)

### 开通Flink实时数据通道

添加Flink-Cluster、设置重启策略、CheckPoint机制等
![tis](docs/incr_step_1.png)

设置Source/Sink组件属性
![tis](docs/incr_step_2.png)

TIS基于数据库元数据信息自动生成Flink-SQL脚本,您没看错全部脚本自动生！
![tis](docs/incr_step_3.png)

实时数据通道创建完成！控制台实时显示实时同步流量，并且，可以执行限流，启停（Pause/Resume）等操作[详细](https://tis.pub/docs/guide/rate-controller/)
![tis](docs/realtime_metric_show.gif)

构建一个实时数仓就这么简单！！！

## 依赖项目

- WEB UI [https://github.com/qlangtech/ng-tis](https://github.com/qlangtech/ng-tis)
- 发版信息生成器[https://github.com/qlangtech/tis-git-manager](https://github.com/qlangtech/tis-git-manager)
- 基于Ansible的打包工具 [https://github.com/qlangtech/tis-ansible](https://github.com/qlangtech/tis-ansible)
- TIS 插件 
   1. [plugins](https://github.com/qlangtech/plugins) 
   2. [tis-plugins-commercial](https://github.com/qlangtech/tis-plugins-commercial)
   3. SqlServer Connector [qlangtech/tis-sqlserver-plugin](https://github.com/qlangtech/tis-sqlserver-plugin)
   4. Paimon Connector [qlangtech/tis-paimon-plugin](https://github.com/qlangtech/tis-paimon-plugin)
   5. DaMeng Connector [qlangtech/tis-dameng-plugin](https://github.com/qlangtech/tis-dameng-plugin)
- TIS 插件元数据生成工具 [https://github.com/qlangtech/update-center2](https://github.com/qlangtech/update-center2)
- DataX [https://github.com/qlangtech/DataX](https://github.com/qlangtech/DataX)
- Flink Extend [https://github.com/qlangtech/flink](https://github.com/qlangtech/flink)
- Dolphinscheduler [https://github.com/qlangtech/dolphinscheduler](https://github.com/qlangtech/dolphinscheduler)
- TIS 插件功能脚手架 [https://github.com/qlangtech/tis-archetype-plugin](https://github.com/qlangtech/tis-archetype-plugin)
- Chunjun [https://github.com/qlangtech/chunjun](https://github.com/qlangtech/chunjun)
- TIS Docs Manager [https://github.com/qlangtech/tis-doc](https://github.com/qlangtech/tis-doc)
- 扩展Debezium [qlangtech/debezium](https://github.com/qlangtech/debezium)
- 扩展Flink-CDC [qlangtech/flink-cdc](https://github.com/qlangtech/flink-cdc)
- 借鉴StreamPark 任务状态监控机制 [apache/streampark](https://github.com/apache/streampark)
- 部分插件参数配置参考 Airbyte [https://github.com/airbytehq/airbyte](https://github.com/airbytehq/airbyte)
- 架构思想参考 [https://github.com/jenkinsci/jenkins](https://github.com/jenkinsci/jenkins)

## 如何开发

[https://tis.pub/docs/develop/compile-running/](https://tis.pub/docs/develop/compile-running/)

## 许可协议

TIS is under the Apache2 License. See the [LICENSE](https://github.com/qlangtech/tis-solr/blob/master/LICENSE) file for details.

## 反馈

您在使用过程中对TIS有任何不满或者批评都请不惜斧正，您提出的宝贵意见是对我们最大的支持和鼓励，[我要提建议](https://github.com/qlangtech/tis/issues/new)