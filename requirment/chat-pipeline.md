# 总体需求说明
TIS经过长期发展，已经构建了一个围绕数据集成管道Pipeline管道，源端数据支持MySQL、SqlServer、PostgreSQL，目标短支持Apache Paimon、MySQL、Apache Doris等数据类型。
在TIS底层有一个相当健壮的围绕数据同步管道的领域模型层，架构设置借鉴了Jenkin的架构里面（部分代码也是从那边移植过来的）。

现在是时候为在TIS中构建一个Pipeline-Agent执行流程了，让用户在TIS前端dashboard上输入一段自然语言，例如：
```bash
我需要创建一个数据同步管道，从MySQL 同步到 Paimon 数据库，MySql 数据源，用户名为baisui，密码为123456，主机地址为192.168.28.200，端口为3306，数据库名称为order2
Paimon端的Hive配置为，db地址：192.168.28.200，db名称：default。同步管道创建完成自动触发历史数据同步，并开启增量同步，谢谢
```

# TIS现状
这部分的目的是要告诉Claude Code，目前TIS为实现需求提供的功能、组件，可以为Claude Code实现需求提供捷径。

## TIS领域模型的扩展点及实现插件

TIS中定义了设计优良的领域模型的扩展点，下面在实现基于Plan-and-Execute流程中中需要使用到，请好好领会。

- DataXReader 插件
  - 扩展点：`com.qlangtech.tis.datax.impl.DataxReader` (类路径文件：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/datax/impl/DataxReader.java)
  - MySQL批量数据显现插件： `com.qlangtech.tis.plugin.datax.DataxMySQLReader`（类路径文件：/Users/mozhenghua/j2ee_solution/project/plugins/tis-datax/tis-ds-mysql-plugin/src/main/java/com/qlangtech/tis/plugin/datax/DataxMySQLReader.java）
- HDFS资源定义插件
  - 扩展点：`com.qlangtech.tis.offline.FileSystemFactory` (类路径文件：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/offline/FileSystemFactory.java)
  - 默认HDFS插件实现点：`com.qlangtech.tis.hdfs.impl.HdfsFileSystemFactory` (类路径文件：/Users/mozhenghua/j2ee_solution/project/plugins/tis-datax/tis-datax-hdfs-plugin/src/main/java/com/qlangtech/tis/hdfs/impl/HdfsFileSystemFactory.java)
- 数据源插件
  - 扩展点：`com.qlangtech.tis.plugin.ds.DataSourceFactory`（类文件路径：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/ds/DataSourceFactory.java） 
  - MySQL DataSource：`com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory`（类文件路径：/Users/mozhenghua/j2ee_solution/project/plugins/tis-datax/tis-ds-mysql-v5-plugin/src/main/java/com/qlangtech/tis/plugin/ds/mysql/MySQLV5DataSourceFactory.java）
  - Hive DataSource：`com.qlangtech.tis.hive.Hiveserver2DataSourceFactory` （类文件路径：/Users/mozhenghua/j2ee_solution/project/plugins/tis-datax/tis-hive-plugin/src/main/java/com/qlangtech/tis/hive/Hiveserver2DataSourceFactory.java）
- DataXWriter 插件
  - 扩展点：`com.qlangtech.tis.datax.impl.DataxWriter` (类文件路径：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/datax/impl/DataxWriter.java)
  - Paimon批量写入插件：`com.qlangtech.tis.plugin.paimon.datax.DataxPaimonWriter` (类文件路径：/opt/misc/tis-paimon-plugin/tis-datax/tis-datax-paimonwriter-plugin/src/main/java/com/qlangtech/tis/plugin/paimon/datax/DataxPaimonWriter.java)
- 实时数据源监听 插件
  
  - 扩展点：`com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory`（类文件路径：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/async/message/client/consumer/impl/MQListenerFactory.java）
  - MySQL Binlog监听插件实现：`com.qlangtech.tis.plugins.incr.flink.cdc.mysql.FlinkCDCMySQLSourceFactory`（类文件路径：/Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-flink-cdc-mysql-plugin/src/main/java/com/qlangtech/tis/plugins/incr/flink/cdc/mysql/FlinkCDCMySQLSourceFactory.java）
- 实时数据端写入插件
  - 扩展点：`com.qlangtech.tis.plugin.incr.TISSinkFactory`（类文件路径：/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/incr/TISSinkFactory.java）
  - Paimon 实时数据写入实现：`com.qlangtech.tis.plugins.incr.flink.pipeline.paimon.sink.PaimonPipelineSinkFactory`（类文件路径：/opt/misc/tis-paimon-plugin/tis-incr/tis-flink-pipeline-paimon-plugin/src/main/java/com/qlangtech/tis/plugins/incr/flink/pipeline/paimon/sink/PaimonPipelineSinkFactory.java）

## 如何使用插件来生成插件实例

假设，用户通过自然语言提交了一段任务描述，使用paln-and-execute的执行计划中一个有创建MySQLDataXReader插件实例的子任务，具体执行步骤如下：
1. 明确源端数据类型，找到对应的插件`com.qlangtech.tis.plugin.datax.DataxMySQLReader`
2. 通过调用以下代码： 
   ```java
    JsonUtil.toString(DescriptorsJSONForAIPromote.desc("com.qlangtech.tis.plugin.datax.DataxMySQLReader").getDescriptorsJSON(),true) 
   ```
   JsonUtil.toString() 方法可返回json，如下：

```json
{
	"impl": "com.qlangtech.tis.plugin.datax.DataxMySQLReader",
	"displayName": "MySQL",
	"extendPoint": "com.qlangtech.tis.datax.impl.DataxReader",
	"containAdvance": false,
	"veriflable": false,
	"attrs": [{
			"ord": 0,
			"eprops": {
				"creator": {
					"routerLink": "/offline/ds",
					"plugin": [{
							"descName": "MySQL-V5",
							"hetero": "datasource",
							"extraParam": "type_detailed,update_false,disableBizStore_true"
						},
						{
							"descName": "MySQL-V8",
							"hetero": "datasource",
							"extraParam": "type_detailed,update_false,disableBizStore_true"
						}
					],
					"label": "配置",
					"assistType": "dbQuickManager"
				},
				"label": "数据库名"
			},
			"describable": false,
			"pk": false,
			"type": 5,
			"key": "dbName",
			"required": true
		},
		{
			"ord": 1,
			"eprops": {
				"help": "进行数据抽取时，如果指定splitPk，表示用户希望使用splitPk代表的字段进行数据分片，DataX因此会启动并发任务进行数据同步，这样可以大大提供数据同步的效能。\n\n推荐splitPk用户使用表主键，因为表主键通常情况下比较均匀，因此切分出来的分片也不容易出现数据热点。\n\n 目前splitPk仅支持整形数据切分，不支持浮点、字符串、日期等其他类型。如果用户指定其他非支持类型，MysqlReader将报错！\n 如果splitPk不填写，包括不提供splitPk或者splitPk值为空，DataX视作使用单通道同步该表数据。",
				"dftVal": false,
				"enum": [{
						"val": true,
						"label": "是"
					},
					{
						"val": false,
						"label": "否"
					}
				]
			},
			"describable": false,
			"pk": false,
			"type": 5,
			"key": "splitPk",
			"required": true
		},
		{
			"ord": 98,
			"eprops": {
				"help": "执行数据批量导出时单次从数据库中提取记录条数，可以有效减少网络IO次数，提升导出效率。切忌不能设置太大以免OOM发生",
				"dftVal": 2000
			},
			"describable": false,
			"pk": false,
			"type": 4,
			"key": "fetchSize",
			"required": true
		},
		{
			"ord": 99,
			"eprops": {
				"mode": "text/velocity",
				"help": "无特殊情况请不要修改模版内容，避免不必要的错误",
				"dftVal": "com.qlangtech.tis.plugin.datax.DataxMySQLReader.getDftTemplate()",
				"style": "codemirror",
				"label": "配置模版",
				"rows": 18
			},
			"describable": false,
			"pk": false,
			"type": 2,
			"key": "template",
			"required": true
		}
	]
}
```
如上json内容中描述的是插件com.qlangtech.tis.plugin.datax.DataxMySQLReader 相关属性说明，及一些基本profile内容信息：
1. impl：插件的具体实现类
2. displayName：插件名称
3. extendPoint：插件扩展点，`impl`属性对应的实现类是继承于该扩展点抽象类（或接口）
4. veriflable：插件实例对象是否提供校验功能（boolean类型）
5. attrs：属性是一个array类型，内部包含该插件的属性对象

下面对`attrs` 数组中的每个元素属性进行说明：
1. ord：该元素在表单的中位值排序
2. describable: 是否是一个内嵌的插件输入项（TIS的插件支持嵌套）, 内部结构参考如上com.qlangtech.tis.plugin.datax.DataxMySQLReader 插件的说明
3. pk: 标明该属性是否是属性的主键，对应的字段一般是String类型的，对应的值可作为对象间的内嵌引用来使用
4. type: 字段类型
5. key: 字段名称,
6. required: 是否必须有输入内容
7. eprops： 内嵌了字段的额外属性说明，以下分别说明：
   1. help：元素属性的说明，作用是什么，起到什么作用
   2. dftVal：元素属性的默认值
   3. label：在前端页面上显示给用户看的，也可当作元素属性的说明
   4. enum：元素属性可选的值列表，列表中是元组，内有属性，1.val: 对应的值 ，2.label: 显示给用户看的。当元素属性没有提供dftVal值，并且required为true时，需要将enum的远组列表中的label显示给用户看询问选择哪一个
   

以上json内容会附带到提交给大模型的promote内容中，由大模型进行处理，解析用户提交的任务描述信息，将解析出来的信息组装成以下格式的json内容反馈给agent程序处理：
```json
{
			"impl": "com.qlangtech.tis.plugin.datax.DataxMySQLReader",
			"vals": {
				"dbName": {
					"_primaryVal": "order2"
				},
				"splitPk": {
					"_primaryVal": false
				},
				"fetchSize": {
					"_primaryVal": 2000
				},
				"template": {
					"_primaryVal": "{\n  \"name\": \"mysqlreader\",\n  \"parameter\": {\n    \"connection\": [\n      {\n        \"jdbcUrl\": [\n          \"${reader.jdbcUrl}\"\n        ],\n#if($reader.splitTable)\n \"table\": [${reader.splitTabs}]\n#else\n \"querySql\": [\n     \"SELECT ${reader.cols} FROM ${reader.sourceEntityName} #if($reader.containWhere) WHERE ${reader.where} #end\"\n   ]\n#end\n      }\n    ]\n    #if($reader.splitTable)\n    ,\"column\": [${reader.colsQuotes}]\n    #end\n    ,\"dataxName\": \"${reader.dataXName}\"\n    ,\"password\": \"${reader.password}\"\n    ,\"username\": \"${reader.username}\"\n  }\n}\n"
				}
			}
		}
```
以下是以上json内容中属性 dbName值为`order2`对应的数据源插件实例：
```json
{
			"impl": "com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory",
			"vals": {
				"name": {
					"_primaryVal": "order2"
				},
				"splitTableStrategy": {
					"descVal": {
						"impl": "com.qlangtech.tis.plugin.ds.split.DefaultSplitTableStrategy",
						"vals": {
							"nodeDesc": {
								"_primaryVal": "192.168.28.200[1-2]"
							},
							"testTab": {
								"_primaryVal": "orderdetail"
							},
							"prefixWildcardStyle": {
								"_primaryVal": false
							},
							"tabPattern": {
								"_primaryVal": ""
							}
						}
					}
				},
				"port": {
					"_primaryVal": 3306
				},
				"dbName": {
					"_primaryVal": "order"
				},
				"userName": {
					"_primaryVal": "root"
				},
				"password": {
					"_primaryVal": "123456"
				},
				"timeZone": {
					"descVal": {
						"impl": "com.qlangtech.tis.plugin.timezone.DefaultTISTimeZone",
						"vals": {
							"timeZone": {
								"_primaryVal": "Asia/Shanghai"
							}
						}
					}
				},
				"encode": {
					"_primaryVal": "utf8"
				},
				"useCompression": {
					"_primaryVal": false
				},
				"extraParams": {
					"_primaryVal": ""
				}
			}
		}
```

# 细化需求

## 前端实现
已经在前端项目（/Users/mozhenghua/j2ee_solution/project/tis-console）中，构建了一个入口（src/runtime/root-welcome-component.ts），点击`nz-card`的元素id为`pipeline-chat`
调用openChat()方法，打开`ChatPipelineComponent`页面，该面打开使用nzDrawer方式打开，占用整个窗口80%的宽度，在该component中需要实现以下功能：
1. 整体效果如各个大模型使用页面呈现的效果一样（如：https://chat.deepseek.com/ ， https://www.tongyi.com/qianwen/ ）
2. 页面左右有一个历史任务提交一览，将之前已经提交过的任务在列表中呈现，用户点击列表中的任何一项都能查看该任务的执行日志，还能继续基于该任务作为上下文继续提交新的任务执行
3. 页面组件中与服务端交互需要实现打字机效果，服务端接收处理开始向客户端推送反馈消息，使用SSE（Server-Sent Events）来实现服务端推送数据，消息的内容分为两种，如下：
    1. text类型的消息，普通文本消息告知用户执行结果，如：“我们正在开始执行任务”，“您提交的任务已经完成”
    2. 服务端反馈的json结构的消息，如插件`com.qlangtech.tis.plugin.datax.DataxMySQLReader` 对应的json结构的描述，客户端程序需要能够方便地识别服务端发送的json文本段
       ，这样可以将该json内容通过TIS组件（src/common/plugins.component.ts中的PluginsComponent）渲染成对应的组件对话框，类似以下方式：
       ```typescript
          let desc: Descriptor = ...;
          PluginsComponent.openPluginDialog({
              saveBtnLabel: '更新',
              enableDeleteProcess: true,
              shallLoadSavedItems: false
              , item: i
              , savePluginEventCreator: () => {
                return opt;
              }
            }
            , this, desc // manipuldateMeta.descMeta
            , {name: 'noStore', require: true}
            , `${desc.displayName}`
            , (event, biz) => {
             
            });
       ```
4. 页面中需要放置几个按钮，用户点击按钮后，可以方便地将事先准备好的任务模版内容自动拷贝到任务输入对话框中，这样可以第一次使用TIS的用户使用。
5. 页面与服务端交互过程中需要在某个位置上显示当前消耗大模型的token数目，这样可以让户大致了解调用大模型消费了多少钱。
## 后端实现
假设，用户提交了自然语言任务说明，如下：
```bash
我需要创建一个数据同步管道，从MySQL 同步到 Paimon 数据库，MySql 数据源，用户名为baisui，密码为123456，主机地址为192.168.28.200，端口为3306，数据库名称为order2
Paimon端的Hive配置为，db地址：192.168.28.200，db名称：default。同步管道创建完成自动触发历史数据同步，并开启增量同步，谢谢
```

TIS Pipeline Agent主程序识别出是构建MySQL到Paimon表的任务同步管道（识别自然语言可以使用大模型deepseek或者通义qianwen大模型（模型的配置需要提供抽象化接口，以便于今后可以切换不同的大模型REST- API接口）
，这部分需要设计一个合适的提示词模版可以复用） ，现在Agent任务明确。

则服务端实现Agent功能需要基于 Plan-And-Execute模式实现
，在Agent主程序（也就是TIS构建的代码中）需要会从TIS已经保存的模版库中获得MySQL同步Paimon表的子任务步骤列表：

```markdown
1. 下载相关数据端的插件包
   查找MySQL，Paimon对应的端插件包是否已经下载安装成功？如果没有，需要安装，成功之后进入下一步。
2. 创建基于MySQL的Reader
   
   该任务下包括了两个子任务：
    * 创建（或引用已经创建的）MySQL数据源引用（创建插件实例`com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory`）
    * 创建MySQLDataXReader （创建插件实例`com.qlangtech.tis.plugin.datax.DataxMySQLReader`）
3. 创建基于Paimon的Writer
   
   该任务下包括了三个子任务：
    * 创建（或引用已经创建的）Hive数据源引用（创建插件实例：`com.qlangtech.tis.hive.Hiveserver2DataSourceFactory`）
    * 创建（或引用已经创建的）Hdfs资源引用（创建插件实例：`com.qlangtech.tis.hdfs.impl.HdfsFileSystemFactory`）
    * 创建PaimonDataXWriter（创建插件实例：`com.qlangtech.tis.plugin.paimon.datax.DataxPaimonWriter`）
4. 选择第1步中定义的MySQL数据源中哪些表需要同步，用户如果没有在任务说明需要选择哪些表，则需要向用户询问是否立即执行
5. 用户如果在promote中提出触发批量历史同步，则触发批量历史数据同步执行，如没有在promote中提出，则询问用户是否要立即触发执行，执行过程如果出错则要告知用户，终止后续task执行
6. 用户如果在promote中提需要启动增量执行通道，则进入后续流程，如没有则需要询问用户是否需要开通增量通道   
7. 创建MySQL Binlog增量实时同步组件（创建插件实例：`com.qlangtech.tis.plugins.incr.flink.cdc.mysql.FlinkCDCMySQLSourceFactory`）
8. 创建Paimon增量实时写入组件 (创建插件实例：`com.qlangtech.tis.plugins.incr.flink.pipeline.paimon.sink.PaimonPipelineSinkFactory`)   
9. 触发增量实时同步任务
```

以上每个创建插件实例步骤，需要对对提交表单中required为true的属性进行校验，如果解析用户提交的任务信息中不能获得，则要继续向用户询问，直到补足必要的属性为止，不然不能进入到下一步。

`Plan-And-Execute`模式任务列表，需要设计一个通用，逻辑清晰，且健壮有错误恢复的执行逻辑单元，入口类就叫`TISPanAndExecuteAgent`吧，请claude code 设计一个逻辑清晰
，考虑到各种执行细节的大模型系统提示词。

所有的ai-agent相关的类需要放在 com.qlangtech.tis.aiagent包下面，服务端代码放在tis-console子工程下