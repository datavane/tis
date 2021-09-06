## Stargazers over time

[![Stargazers over time](https://starchart.cc/qlangtech/tis-solr.svg)](https://starchart.cc/qlangtech/tis-solr)
![tis](docs/tis-logo.png)
## TIS介绍

TIS快速为您构建企业级数仓库服务，**基于DataX，提供简单易用的 操作界面，降低用户使用DataX的学习成本，缩短任务配置时间，避免配置过程中出错，使数据抽取变得简单、易用** [详细介绍](http://tis.pub/docs/enhance-extra/) 
<!--
TIS平台是一套为企业级用户提供大数据多维、实时、查询的搜索中台产品。用户可以在TIS上自助快速构建搜索服务，它大大降低了搜索技术的门槛 [详细说明](http://tis.pub/docs/) 
-->
> 视频： [>>TIS介绍](https://www.bilibili.com/video/BV11y4y1B7Mk) [>>操作实例](https://www.bilibili.com/video/BV1Uv41167SH/)
 
## 安装说明

  速将TIS在分布式环境中一键安装（支持私有云、公有云等环境），方便、快捷 [详细说明](http://tis.pub/docs/install/uber)

## 架构

 ![tis](docs/tis-synoptic.png)
 
## 功能一瞥 

- 视频示例
    * [安装示例](https://www.bilibili.com/video/BV18q4y1p73B/)
    * [启用分布式执行功能](https://www.bilibili.com/video/BV1Cq4y1D7z4?share_source=copy_web)
    * [MySQL导入ElasticSearch](https://www.bilibili.com/video/BV1G64y1B7wm?share_source=copy_web)
    * [MySQL导入Hive](https://www.bilibili.com/video/BV1Vb4y1z7DN?share_source=copy_web)
    * [MySQL导入Clickhouse](https://www.bilibili.com/video/BV1x64y1B7V8/)


选择Reader/Writer插件类型
  ![tis](docs/datax-add-step2.png)

添加MySqlReader
  ![tis](docs/add-mysql-reader.png)

设置MySqlReader目标表、列  
   ![tis](docs/select-tab-cols.png)
   
添加ElasticWriter,可视化设置ElasticSearch的Schema Mapping
   ![tis](docs/add-elastic-writer.png) 

执行MySql->ElasticSearch DataX实例，运行状态 
   ![tis](docs/datax-exec-status.png) 
 
## 相关代码 

- WEB UI [https://github.com/qlangtech/ng-tis](https://github.com/qlangtech/ng-tis)
- 基于Ansible的打包工具 [https://github.com/qlangtech/tis-ansible](https://github.com/qlangtech/tis-ansible)
- TIS 插件 [https://github.com/qlangtech/plugins](https://github.com/qlangtech/plugins)
- TIS 插件元数据生成工具 [https://github.com/qlangtech/update-center2](https://github.com/qlangtech/update-center2)
- DataX [https://github.com/qlangtech/DataX](https://github.com/qlangtech/DataX)
 
## 许可协议

 TIS is under the AGPL-3.0 License. See the [LICENSE](https://github.com/qlangtech/tis-solr/blob/master/LICENSE) file for details.
 
## 反馈
 
  您在使用过程中对TIS有任何不满或者批评都请不惜斧正，您提出的宝贵意见是对我们最大的支持和鼓励，[我要提意见](https://github.com/qlangtech/tis-solr/issues/new)
