# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

TIS 是一个企业级数据集成服务平台，基于批(DataX)、流(Flink-CDC、Chunjun)一体化架构，提供简单易用的操作界面来降低端到端数据同步的实施门槛。项目采用微前端技术，继承了Jenkins的设计思想，具有强大的扩展性和SPI机制。

## 核心架构

### 主要模块结构
- **tis-console**: Web控制台核心模块，包含主要的业务逻辑和API
- **tis-plugin**: 插件系统核心，实现各种数据源的Reader/Writer
- **tis-web-start**: Web服务启动模块
- **tis-assemble**: 项目打包和分发模块
- **tis-sql-parser**: SQL解析器模块
- **tis-hadoop-rpc**: Hadoop RPC通信模块
- **tis-k8s**: Kubernetes集成模块
- **maven-tpi-plugin**: Maven插件，用于TIS插件开发

### 技术栈
- 后端: Java, Maven, Spring框架
- 数据处理: DataX, Flink-CDC, Chunjun
- 容器化: Docker, Kubernetes
- 构建工具: Maven

## 常用开发命令

### 构建命令
```bash
# 完整构建项目(跳过测试)
mvn clean package -Dmaven.test.skip=true -Dappname=all -o

# 安装到本地仓库
mvn clean install -Dmaven.test.skip=true -Dappname=all -o

# 部署到远程仓库(特定模块)
mvn clean deploy -Dmaven.test.skip=true -Dautoconfig.skip -pl tis-plugin,maven-tpi-plugin,tis-sql-parser,tis-web-start,tis-logback-flume-parent -am -Ptis-repo
```

### 开发脚本
- `./package.sh`: 项目打包脚本
- `./install.sh`: 项目安装脚本  
- `./deploy.sh`: 项目部署脚本
- `./setversion.sh`: 版本设置脚本

### 测试相关
项目构建脚本中通常使用 `-Dmaven.test.skip=true` 跳过测试，具体测试命令需要根据各子模块的配置确定。

## 代码组织原则

### Maven模块依赖
项目采用Maven多模块结构，主要依赖关系：
- 所有模块继承自 `tis-parent`
- 核心版本号通过 `${revision}` 属性统一管理
- 当前版本: 4.3.0

### 插件开发
- 插件基于SPI机制实现
- 支持数据源Reader/Writer插件扩展
- 插件开发可参考 `tis-plugin` 模块
- 使用 `maven-tpi-plugin` 进行插件打包

## 部署方式

项目支持多种部署方式：
- 单机部署 (tar包解压启动)
- Docker容器化部署
- Docker Compose编排部署  
- Kubernetes集群部署

## 开发参考

- 详细开发文档: https://tis.pub/docs/develop/compile-running/
- 插件开发脚手架: https://github.com/qlangtech/tis-archetype-plugin
- Web UI项目: https://github.com/qlangtech/ng-tis
- 本项目的所有前端项目代码在`/Users/mozhenghua/j2ee_solution/project/tis-console`目录下
- 本项目是Core内核层，构建了TIS的抽象层，负责TIS所有插件的生命周期管理，大部分插件实现在`/Users/mozhenghua/j2ee_solution/project/plugins`这个路径所对应的项目中
- modelcontextprotocol相关的原代码已经克隆到本地，路径为：/opt/misc/java-sdk

## 工程边界

项目中 TIS 插件代码分布在两个工程：

| 层次 | 工程路径 | 内容 |
|---|---|---|
| **抽象/核心层** | `tis-solr/tis-plugin/` (`/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin`) | 接口、抽象基类、复用配置类（如 `WidgetColumnConfig`）、`HeteroEnum` 注册、Groovy 桥接类 |
| **具体实现层** | `plugins/tis-ontology-plugin/` (`/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin`) | 具体 Widget 实现类（`ObjectTableWidget` 等）、对应 `.json` 资源文件 |

规则：**抽象层不放具体 Widget 实现**，**实现层不放可复用的抽象/配置类**。

## 插件属性设计原则

### 原则一：禁止 JSON 字符串输入

插件的 `@FormField` 属性**不得让用户填写 JSON 字符串**。结构化数据必须使用 TIS 提供的结构化表单机制：

| 场景 | 正确做法 | 错误做法 |
|---|---|---|
| 列表化结构对象（如表格列定义） | `@SubForm(desClazz = XxxConfig.class)` + `List<XxxConfig>` 字段 | `@FormField(type = FormFieldType.TEXTAREA)` 手写 `[{...}]` |
| 单个结构对象 | 字段类型声明为 `Describable` 子类（不指定 type，TIS 自动检测为嵌套子表单） | `@FormField(type = FormFieldType.TEXTAREA)` 手写 `{...}` |

示例：
```java
// ✅ 正确：使用 @SubForm
@SubForm(desClazz = WidgetColumnConfig.class, atLeastOne = false,
         idListGetScript = "return java.util.Collections.emptyList();")
public List<WidgetColumnConfig> columns;

// ❌ 错误：让用户手写 JSON 字符串
@FormField(type = FormFieldType.TEXTAREA)
public String columns;  // 用户需输入 [{prop:"name",label:"名称"}]
```

### 原则二：固定取值用 ENUM

当字段的取值有固定、有限的选项集合时，必须使用 `FormFieldType.ENUM` + Java 枚举类，**禁止**使用 `INPUTTEXT` 或 `SELECTABLE`（SELECTABLE 仅用于选项来自外部运行时数据的动态场景）。

```java
// ✅ 正确：使用 ENUM + Java 枚举
@FormField(type = FormFieldType.ENUM, ordinal = 3, required = true)
public AggregationType aggregation;

public enum AggregationType {
    sum("求和"), count("计数"), avg("平均值"), min("最小值"), max("最大值");
    public final String label;
    AggregationType(String label) { this.label = label; }
}

// ❌ 错误：使用 SELECTABLE（误导，选项明明是固定的）
@FormField(type = FormFieldType.SELECTABLE, ordinal = 3)
public String aggregation;

// ❌ 错误：使用 INPUTTEXT（用户不知道有哪些合法取值）
@FormField(type = FormFieldType.INPUTTEXT, ordinal = 3)
public String aggregation;
```

何时使用 ENUM vs SELECTABLE：
- **ENUM**：选项完全固定（如聚合方式 sum/count/avg/min/max、对齐方式 left/center/right、级别 1/2/3/4）。选项在 Java 代码中硬编码。
- **SELECTABLE**：选项来自运行时外部数据源，通过 Descriptor 的 `registerSelectOptions()` 或 `doGetOptions()` 动态供给（如当前 module 的变量列表、数据库中某个表的数据）。

### 原则三：枚举中的颜色类字段附带色值

如果枚举字段是颜色选择（如标题文字颜色），应在 `.json` resource 中为每个枚举项附带 `hex` 色值，以便前端渲染颜色预览圆点：

```json
{
  "color": {
    "help": "选择文本颜色",
    "dftVal": "inherit",
    "enum": [
      { "label": "红色", "val": "red", "hex": "#f5222d" },
      { "label": "蓝色", "val": "blue", "hex": "#1890ff" },
      ...
    ]
  }
}
``` 
