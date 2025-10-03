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