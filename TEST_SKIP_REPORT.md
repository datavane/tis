# 单元测试跳过汇总(TEST_SKIP_REPORT)

> 生成方式:对两工程 84 个含测试的子模块,静态预筛(testcontainers/真 JDBC/Flink MiniCluster/
> 嵌入式服务端/真实 ZooKeeper)+ 实际运行验证(离线、JDK17、带 `--add-opens`,
> `-Dmaven.test.failure.ignore=true` 保证每个候选类都被执行)。
> **只有实际运行通过的类才被写入该模块的 `TestAll.java`**;其余见下表。

**总计:验证通过 136 个测试类,跳过 313 个测试类。**

---

## tis-solr

### `datax-config`

**TestAll.java 已生成,包含 2 个可运行测试类**:

- ✅ `com.qlangtech.tis.datax.TestTimeFormat`
- ✅ `com.qlangtech.tis.plugin.ds.TestTimeZone`

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.ds.TestDataType` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  getDecimalDigits must be null expected null, but was:<-1>
- ⏭ `com.qlangtech.tis.plugin.ds.TestDataTypeMeta` — [其他运行错误] 测试失败(详见 surefire 报告)

### `tis-assemble`

**TestAll.java 已生成,包含 9 个可运行测试类**:

- ✅ `com.qlangtech.tis.datax.TestDataXJobSubmit`
- ✅ `com.qlangtech.tis.flume.TestFlumeApplication`
- ✅ `com.qlangtech.tis.full.dump.TestClientIncrStatusUmbilicalProtocol`
- ✅ `com.qlangtech.tis.full.dump.TestStart`
- ✅ `com.qlangtech.tis.fullbuild.phasestatus.TestPhaseStatusCollection`
- ✅ `com.qlangtech.tis.fullbuild.servlet.TestTisServlet`
- ✅ `com.qlangtech.tis.fullbuild.taskflow.TestWorkflow`
- ✅ `com.qlangtech.tis.order.center.MockTest`
- ✅ `com.qlangtech.tis.order.center.TestIndexSwapTaskflowLauncher`

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.exec.impl.TestWorkflowDumpAndJoinInterceptor` — 依赖真实 ZooKeeper 连接
- ⏭ `com.qlangtech.tis.rpc.server.TestIncrStatusServer` — 依赖真实 ZooKeeper 连接

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.exec.TestActionInvocation` — [其他运行错误] java.lang.IllegalStateException:  subFiles size can not small than 1,file:/opt/data/tis/cfg_repo/tis_plugin_config/df/totalpay
- ⏭ `com.qlangtech.tis.exec.datax.TestDataXExecuteInterceptor` — [JDK17 反射限制(Field.modifiers 不可访问)] java.lang.NoSuchFieldException:  modifiers
- ⏭ `com.qlangtech.tis.exec.datax.TestDataXExecuteInterceptorForMethodBuildTaskTriggers` — [Mockito mock 构造失败] org.mockito.exceptions.misusing.UnfinishedStubbingException:
- ⏭ `com.qlangtech.tis.full.dump.TestHdfsIndexBuild` — [其他运行错误] com.qlangtech.tis.lang.TisException:  maxRetry:1,url:http://10.1.5.2:14844/hdfs_build?indexname=search4totalpay&cols=totalpay_id%2Ccurr_date%2Coutfee%2Csource_amount%2Cdiscount_amount%2Cresult_amount%
- ⏭ `com.qlangtech.tis.full.dump.TestTask` — [断言失败(金样/逻辑与当前环境不符)] java.lang.NullPointerException:  dagSpecPath can not be null
- ⏭ `com.qlangtech.tis.log.TestRealtimeLoggerCollectorAppender` — [其他运行错误] java.lang.IllegalStateException:  appender:full-123 relevant appender can not be null,exist keys:[]
- ⏭ `com.qlangtech.tis.manage.servlet.TestIncrTagHeatBeatMonitor` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  expected:<100> but was:<0>

### `tis-base-test`

**TestAll.java 已生成,包含 3 个可运行测试类**:

- ✅ `com.qlangtech.tis.manage.common.TestSnapshotDomainUtils`
- ✅ `com.qlangtech.tis.plugin.TestPluginUtils`
- ✅ `com.qlangtech.tis.realtime.s4totalpay.TestS4Totalpay`

(该模块全部候选测试均通过,无跳过项)

### `tis-builder-api`

**TestAll.java 已生成,包含 6 个可运行测试类**:

- ✅ `com.qlangtech.tis.AppTest`
- ✅ `com.qlangtech.tis.coredefine.module.action.TestSpecification`
- ✅ `com.qlangtech.tis.fs.TestIPath`
- ✅ `com.qlangtech.tis.realtime.transfer.TestUnderlineUtils`
- ✅ `com.qlangtech.tis.utils.TestTisMetaProps`
- ✅ `com.qlangtech.tis.utils.TestUtils`

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.manage.common.TestConfig` — 依赖本地配置文件(testGetAssembleHost 断言 assemble host 值,随环境变化)

### `tis-collection-info-collect`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.collectinfo.TestCoreStatisticsReport` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-common`

**TestAll.java 已生成,包含 1 个可运行测试类**:

- ✅ `com.qlangtech.tis.AppTest`

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.TestGetAppConfigBySnid` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  config resource is not exist,snid:222
- ⏭ `com.qlangtech.tis.TestGetAppconfig` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  config resource is not exist,appname:search4punish groupIndex:0 runtime:DAILY
- ⏭ `com.qlangtech.tis.manage.common.TestGitUtils` — [依赖内网服务] com.qlangtech.tis.lang.TisException:  maxRetry:1,url:http://192.168.28.96:8080/tjs/config/stream_script_repo.action?path=cfg_repo%2Fdb%2Fdb%2Forder,No route to host

### `tis-console`

**TestAll.java 已生成,包含 6 个可运行测试类**:

- ✅ `com.qlangtech.tis.LockTest`
- ✅ `com.qlangtech.tis.TestBeann`
- ✅ `com.qlangtech.tis.aiagent.core.TestRealTISPlanAndExecuteAgent`
- ✅ `com.qlangtech.tis.alert.TestFlinkJobsMonitor`
- ✅ `com.qlangtech.tis.manage.common.TestUserUtils`
- ✅ `com.qlangtech.tis.util.groovy.TestConsoleGroovyShellFactory`

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.derby.WwdEmbedded` — 依赖外部数据库(JDBC)

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.TestFullBuild` — [依赖内网服务] com.qlangtech.tis.lang.TisException:  maxRetry:1,url:http://10.1.5.129:8080/trigger?appname=search4xxx&workflow_id=15,HTTP connect timed out
- ⏭ `com.qlangtech.tis.aiagent.core.ITTISPlanAndExecuteAgent` — [Mockito mock 构造失败] org.mockito.exceptions.base.MockitoException:
- ⏭ `com.qlangtech.tis.aiagent.execute.impl.BasicStepExecutorTest` — [其他运行错误] java.lang.IllegalStateException:  can not find llmProvider with name:default
- ⏭ `com.qlangtech.tis.aiagent.execute.impl.PluginInstanceCreateExecutorTest` — [Mockito mock 构造失败] org.mockito.exceptions.base.MockitoException:
- ⏭ `com.qlangtech.tis.aiagent.execute.impl.TestPluginDownloadAndInstallExecutor` — [Mockito mock 构造失败] org.mockito.exceptions.base.MockitoException:
- ⏭ `com.qlangtech.tis.compiler.java.TestJavaCompilerProcess` — [其他运行错误] java.lang.IllegalArgumentException:  param sourceDir can not be null
- ⏭ `com.qlangtech.tis.config.module.action.TestFullbuildWorkflowAction` — [依赖数据库] org.springframework.transaction.CannotCreateTransactionException:  Could not open JDBC Connection for transaction
- ⏭ `com.qlangtech.tis.coredefine.module.action.TestDataXAction` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError: 	at org.junit.Assert.fail(Assert.java:87)
- ⏭ `com.qlangtech.tis.coredefine.module.action.TestLoopQueue` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.coredefine.module.action.TestPluginAction` — [其他运行错误] 测试失败(详见 surefire 报告)
- ⏭ `com.qlangtech.tis.coredefine.module.action.TestPluginItemsParser` — [断言失败(金样/逻辑与当前环境不符)] java.lang.NullPointerException:  Cannot invoke "java.util.List.size()" because "errorMsgs" is null
- ⏭ `com.qlangtech.tis.coredefine.module.action.TestSSEConnection` — [其他运行错误] java.util.concurrent.RejectedExecutionException:  Task com.qlangtech.tis.coredefine.module.action.TestSSEConnection$$Lambda$182/0x000000080115f398@1be2019a rejected from java.util.concurrent.ThreadPoo
- ⏭ `com.qlangtech.tis.manage.biz.dal.dao.impl.TestApplicationDAOImpl` — [其他运行错误] org.apache.commons.dbcp.SQLNestedException:
- ⏭ `com.qlangtech.tis.manage.spring.TestTISDataSourceFactory` — [其他运行错误] java.sql.SQLException:  Table/View 'APPLICATION' already exists in Schema 'APP'.
- ⏭ `com.qlangtech.tis.mcp.TestTISHttpMcpServer` — [其他运行错误] com.qlangtech.tis.mcp.TestTISHttpMcpServer.testCreateDataSourcecallHandler_ErrorMessageDoesNotLeakDetails:  Time elapsed: 0.027 s  <<< ERROR!
- ⏭ `com.qlangtech.tis.mcp.tools.pipeline.TestCreatePipelinePrepareTool` — [Mockito mock 构造失败] org.mockito.exceptions.base.MockitoException:
- ⏭ `com.qlangtech.tis.offline.module.action.TestOfflineDatasourceAction` — [其他运行错误] 测试失败(详见 surefire 报告)
- ⏭ `com.qlangtech.tis.plugin.TestPluginAndCfgsSnapshotOnWorkflow` — [其他运行错误] java.lang.RuntimeException:  targetName:tpch_parse_on_hive,resType:DataFlow,store file is not exist:/opt/data/tis/cfg_repo/tis_plugin_config/df/tpch_parse_on_hive/com.qlangtech.tis.manage.IAppSource.x
- ⏭ `com.qlangtech.tis.runtime.module.action.TestSchemaAction` — [依赖数据库] org.springframework.transaction.CannotCreateTransactionException:  Could not open JDBC Connection for transaction
- ⏭ `com.qlangtech.tis.runtime.module.action.TestSysInitializeAction` — [其他运行错误] java.lang.RuntimeException:  dataDir:/opt/data/tis must be empty
- ⏭ `com.qlangtech.tis.util.TestPluginItems` — [JDK17 反射限制(Field.modifiers 不可访问)] java.lang.NoSuchFieldException:  modifiers

### `tis-dag`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.datax.impl.TestDataxProcessor` — [其他运行错误] java.lang.RuntimeException:  targetName:uCTk,resType:DataApp,store file is not exist:/opt/data/tis/cfg_repo/tis_plugin_config/ap/uCTk/com.qlangtech.tis.manage.IAppSource.xml
- ⏭ `com.qlangtech.tis.sql.parser.stream.generate.TestS4EmployeeStreamComponentCodeGenerator` — [其他运行错误] java.lang.IllegalStateException:  appName:search4test relevant appSource can not be null
- ⏭ `com.qlangtech.tis.sql.parser.stream.generate.TestStreamComponentCodeGenerator` — [其他运行错误] java.lang.IllegalStateException:  subFiles size can not small than 1,file:/opt/data/tis/cfg_repo/tis_plugin_config/df/totalpay2
- ⏭ `com.qlangtech.tis.sql.parser.stream.generate.TestStreamComponentCodeGeneratorFlink` — [运行环境缺依赖(NoClassDefFound)] java.lang.NoClassDefFoundError:  Could not initialize class com.qlangtech.tis.extension.impl.PropertyType
- ⏭ `com.qlangtech.tis.sql.parser.stream.generate.TestTikvEmployee` — [其他运行错误] java.lang.IllegalStateException:  subFiles size can not small than 1,file:/opt/data/tis/cfg_repo/tis_plugin_config/df/tikv-employee

### `tis-hadoop-rpc`

**TestAll.java 已生成,包含 1 个可运行测试类**:

- ✅ `com.qlangtech.tis.rpc.grpc.log.stream.TestPhaseStatusCollection`

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.rpc.grpc.log.TestLoggerAppenderClient` — 依赖真实 ZooKeeper 连接
- ⏭ `com.tis.hadoop.rpc.TestStatusRpcClientFactory` — 依赖真实 ZooKeeper 连接

### `tis-manage-pojo`

**TestAll.java 已生成,包含 4 个可运行测试类**:

- ✅ `com.qlangtech.tis.offline.TestDataxUtils`
- ✅ `com.qlangtech.tis.runtime.module.misc.TestEnumTypeJsonSerializer`
- ✅ `com.qlangtech.tis.runtime.module.misc.impl.TestDefaultFieldErrorHandler`
- ✅ `com.qlangtech.tis.trigger.util.TestJsonUtil`

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.manage.common.TestCenterResource` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  /tmp/tis/cfg_repo/tis_plugin_config/com.qlangtech.tis.plugin.incr.IncrStreamFactory.xml.lastmodified
- ⏭ `com.qlangtech.tis.manage.common.TestHttpUtils` — [依赖内网服务] com.qlangtech.tis.lang.TisException:  maxRetry:1,url:http://192.168.28.67:8080/tjs/config/stream_script_repo.action?path=cfg_repo%2Ftis_plugin_config%2Fparams-cfg%2FelasticToken%2Fcom.qlangtech.tis.co
- ⏭ `com.qlangtech.tis.plugin.TestValidatorCommons` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError: 	at org.junit.Assert.fail(Assert.java:87)
- ⏭ `com.qlangtech.tis.realtime.yarn.rpc.JobTypeTest` — [依赖内网服务] com.qlangtech.tis.lang.TisException:  maxRetry:1,url:http://192.168.28.96:8083/tis-assemble/incr-control,No route to host

### `tis-plugin`

**TestAll.java 已生成,包含 31 个可运行测试类**:

- ✅ `com.qlangtech.tis.datax.impl.TestGenerateCfgs`
- ✅ `com.qlangtech.tis.datax.impl.TestTableAlias`
- ✅ `com.qlangtech.tis.extension.TestValueChangePipe`
- ✅ `com.qlangtech.tis.extension.impl.TestEnumFormFieldType`
- ✅ `com.qlangtech.tis.extension.impl.TestPropertyType`
- ✅ `com.qlangtech.tis.extension.util.AbstractPropAssistTest`
- ✅ `com.qlangtech.tis.extension.util.TestPluginExtraProps`
- ✅ `com.qlangtech.tis.manage.common.UserProfileTest`
- ✅ `com.qlangtech.tis.offline.TestFileSystemFactory`
- ✅ `com.qlangtech.tis.plugin.annotation.TestFormFieldTypeColor`
- ✅ `com.qlangtech.tis.plugin.annotation.TestValidator`
- ✅ `com.qlangtech.tis.plugin.datax.TestCreateDDL`
- ✅ `com.qlangtech.tis.plugin.datax.doris.TestDorisSelectedTab`
- ✅ `com.qlangtech.tis.plugin.datax.format.guesstype.TestTargetTabsEntities`
- ✅ `com.qlangtech.tis.plugin.ds.TestDataSourceFactory`
- ✅ `com.qlangtech.tis.plugin.k8s.TestK8sImage`
- ✅ `com.qlangtech.tis.plugin.license.TestTISLicense`
- ✅ `com.qlangtech.tis.plugin.llm.TestQWenLLMProvider`
- ✅ `com.qlangtech.tis.plugin.ontology.OntologyObjectTypeTest`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.objtype.TestDataSourceQueryCriteria`
- ✅ `com.qlangtech.tis.plugin.proxy.TestHttpRequestProxy`
- ✅ `com.qlangtech.tis.plugin.table.join.TableJoinFilterConditionTest`
- ✅ `com.qlangtech.tis.plugin.utils.HexColorValidatorTest`
- ✅ `com.qlangtech.tis.util.TestAttrValMap`
- ✅ `com.qlangtech.tis.util.TestDescribableJSONModelAssist`
- ✅ `com.qlangtech.tis.util.TestDescribableJSONParentChain`
- ✅ `com.qlangtech.tis.util.TestDescriptorsJSONSkipAttrs`
- ✅ `com.qlangtech.tis.util.TestPluginMeta`
- ✅ `com.qlangtech.tis.util.TestRelevantFieldsContextAttrValMap`
- ✅ `com.qlangtech.tis.util.TestTransformerRuleKey`
- ✅ `com.qlangtech.tis.util.TestUploadPluginMeta`

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.TestTIS` — [其他运行错误] java.lang.IllegalStateException:  parent:/opt/data/tis/cfg_repo/tis_plugin_config/search4totalpay is not exist
- ⏭ `com.qlangtech.tis.aiagent.llm.LLMProviderTest` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError: 	at org.junit.Assert.fail(Assert.java:87)
- ⏭ `com.qlangtech.tis.coredefine.module.action.TriggerBuildResultTest` — [依赖内网服务] com.qlangtech.tis.lang.TisException:  maxRetry:1,url:http://192.168.28.96:8083/tis-assemble/trigger,No route to host
- ⏭ `com.qlangtech.tis.datax.impl.TestDataXCfgGenerator` — [其他运行错误] java.lang.IllegalArgumentException:  param tableName can not be empty
- ⏭ `com.qlangtech.tis.datax.impl.TestDataxReader` — [JDK17 反射限制(Field.modifiers 不可访问)] java.lang.NoSuchFieldException:  modifiers
- ⏭ `com.qlangtech.tis.db.parser.TestDBConfigParser` — [其他运行错误] java.lang.IllegalStateException:  parse dbconfig file falid
- ⏭ `com.qlangtech.tis.db.parser.TestScannerPatterns` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError: 	at org.junit.Assert.fail(Assert.java:87)
- ⏭ `com.qlangtech.tis.exec.TestAbstractExecContext` — [断言失败(金样/逻辑与当前环境不符)] java.lang.NullPointerException:  Cannot invoke "com.qlangtech.tis.datax.IDataxProcessor.identityValue()" because "processor" is null
- ⏭ `com.qlangtech.tis.exec.TestIExecChainContext` — [其他运行错误] java.lang.RuntimeException:  targetName:test,resType:DataApp,store file is not exist:/opt/data/tis/cfg_repo/tis_plugin_config/ap/test/com.qlangtech.tis.manage.IAppSource.xml
- ⏭ `com.qlangtech.tis.extension.TestContainAdvanceFieldPlugin` — [断言失败(金样/逻辑与当前环境不符)] java.lang.NullPointerException:  Cannot invoke "com.alibaba.fastjson.JSONObject.getBoolean(String)" because the return value of "com.alibaba.fastjson.JSONArray.getJSONObject(int)" is null
- ⏭ `com.qlangtech.tis.extension.TestDescriptor` — [其他运行错误] 测试失败(详见 surefire 报告)
- ⏭ `com.qlangtech.tis.extension.impl.TestPluginManifest` — [运行环境缺依赖(NoClassDefFound)] java.lang.ClassNotFoundException:
- ⏭ `com.qlangtech.tis.extension.impl.TestSuFormProperties` — [断言失败(金样/逻辑与当前环境不符)] java.lang.NullPointerException:  Cannot invoke "com.qlangtech.tis.util.IPluginContext.getCollectionName()" because "pluginContext" is null
- ⏭ `com.qlangtech.tis.extension.impl.TestXmlFile` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  expected:<2> but was:<0>
- ⏭ `com.qlangtech.tis.extension.model.TestUpdateCenter` — [JDK17 反射限制(Field.modifiers 不可访问)] java.lang.NoSuchFieldException:  modifiers
- ⏭ `com.qlangtech.tis.extension.util.TestGroovyShellEvaluate` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError: 	at org.junit.Assert.fail(Assert.java:87)
- ⏭ `com.qlangtech.tis.manage.common.TaskSoapUtilsTest` — [断言失败(金样/逻辑与当前环境不符)] java.lang.RuntimeException: 	at com.qlangtech.tis.common.utils.Assert.fail(Assert.java:69)
- ⏭ `com.qlangtech.tis.plugin.TestComponentMeta` — [JDK17 反射限制(Field.modifiers 不可访问)] java.lang.NoSuchFieldException:  modifiers
- ⏭ `com.qlangtech.tis.plugin.TestInnerPropOfIdentityName` — [其他运行错误] java.lang.RuntimeException:  java.lang.RuntimeException: Failed to serialize com.qlangtech.tis.plugin.PluginStore#plugins for class com.qlangtech.tis.plugin.KeyedPluginStore
- ⏭ `com.qlangtech.tis.plugin.TestKeyedPluginStore` — [其他运行错误] java.lang.RuntimeException:  java.lang.IllegalStateException: plugin must be type of com.qlangtech.tis.manage.IAppSource, but now is com.qlangtech.tis.plugin.TestPlugin
- ⏭ `com.qlangtech.tis.plugin.TestPluginAndCfgsSnapshot` — [依赖数据库] java.lang.RuntimeException:  targetName:mysql,resType:DataApp,store file is not exist:/opt/data/tis/cfg_repo/tis_plugin_config/ap/mysql/com.qlangtech.tis.manage.IAppSource.xml
- ⏭ `com.qlangtech.tis.plugin.TestPluginStore` — [其他运行错误] java.lang.IllegalArgumentException:  pluginContext can not be null
- ⏭ `com.qlangtech.tis.plugin.credentials.TestParamsConfigPluginStore` — [其他运行错误] java.lang.IllegalArgumentException:  pluginContext can not be null
- ⏭ `com.qlangtech.tis.plugin.datax.TestSelectedTab` — [断言失败(金样/逻辑与当前环境不符)] java.lang.NullPointerException:  store can not be null
- ⏭ `com.qlangtech.tis.plugin.datax.transformer.TestRecordTransformerRules` — [其他运行错误] java.lang.RuntimeException:  record-transformer-rules-descriptor.json
- ⏭ `com.qlangtech.tis.plugin.datax.transformer.impl.TestTransformerRuleElementCreatorFactory` — [断言失败(金样/逻辑与当前环境不符)] java.lang.RuntimeException:  java.lang.NullPointerException: propertyType.extraProp can not be null
- ⏭ `com.qlangtech.tis.plugin.datax.transformer.jdbcprop.TestTargetColType` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:
- ⏭ `com.qlangtech.tis.plugin.ds.TestDataSourceFactoryPluginStore` — [JDK17 反射限制(Field.modifiers 不可访问)] java.lang.NoSuchFieldException:  modifiers
- ⏭ `com.qlangtech.tis.plugin.ds.TestTableInDB` — [JDK17 反射限制(Field.modifiers 不可访问)] java.lang.NoSuchFieldException:  modifiers
- ⏭ `com.qlangtech.tis.util.TestDescriptorsJSONForAIPromote` — [其他运行错误] java.lang.ClassCastException:  class com.qlangtech.tis.aiagent.llm.TISJsonSchema$OneOfFieldsDesc$PluginDesc cannot be cast to class java.util.List (com.qlangtech.tis.aiagent.llm.TISJsonSchema$OneOfFie
- ⏭ `com.qlangtech.tis.util.TestHeteroList` — [JDK17 反射限制(Field.modifiers 不可访问)] java.lang.NoSuchFieldException:  modifiers
- ⏭ `com.qlangtech.tis.util.plugin.TestContainEnumsFieldPlugin` — [其他运行错误] 测试失败(详见 surefire 报告)

### `tis-plugin-sezpoz`

**TestAll.java 已生成,包含 1 个可运行测试类**:

- ✅ `com.qlangtech.tis.extension.TestTISExtendsionInterceptor`

(该模块全部候选测试均通过,无跳过项)

### `tis-scala-compiler`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.compiler.streamcode.TestGenerateDAOAndIncrScript` — [其他运行错误] java.lang.RuntimeException:

### `tis-solrconfig-parser`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.solrdao.TestSolrFieldParser` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  isValid shall be true

### `tis-sql-parser`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.manage.common.incr.TestUberJarUtil` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.powerjob.TestSelectedTabTriggers` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestDBNode` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestOrderCustomersSqlParse` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestPrestoSqlFormat` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestSplitGetIndexProp` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestSqlDataFlowTopology` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestSqlRewriter` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestSqlTaskNode` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestSqlTaskNodeMeta` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestTmpPaySqlParse` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.TestTopologyDir` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.er.TestERRules` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.shop.TestShopTopologyParse` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.stream.generate.TestFlatTableRelation` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.supplyGoods.TestSupplyGoodsParse` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.sql.parser.visitor.TestTableDependencyVisitor` — [其他运行错误] 未运行(模块未验证)

### `tis-web-start`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.web.start.TestTisApp` — [断言失败(金样/逻辑与当前环境不符)] java.lang.NullPointerException:  Cannot read the array length because "<local3>" is null

### `tis-web-start-api`

**TestAll.java 已生成,包含 1 个可运行测试类**:

- ✅ `com.qlangtech.tis.web.start.TestTisRunMode`

(该模块全部候选测试均通过,无跳过项)

### `xmodifier`

**TestAll.java 已生成,包含 2 个可运行测试类**:

- ✅ `org.shai.xmodifier.StringUtilTest`
- ✅ `org.shai.xmodifier.XModifierTest`

(该模块全部候选测试均通过,无跳过项)

---

## plugins

### `tis-asyncmsg-rocketmq-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.async.message.client.consumer.TestRocketMQListenerFactory` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.component.TestRockMqPluginValidate` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.mq.TestConsumer` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.mq.TestEmployeeProducter` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.mq.TestTotalpayProducter` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.util.TestHeteroList` — [其他运行错误] 未运行(模块未验证)

### `tis-datax/executor/dolphinscheduler-task-tis-datasync`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.dolphinscheduler.task.TestTISDatasyncTask` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.dolphinscheduler.task.TestTISDatasyncTaskChannelFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.dolphinscheduler.task.impl.TestDSTaskContext` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/executor/powerjob-worker-samples`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.datax.powerjob.TestSplitTabSync` — 依赖真实 ZooKeeper 连接

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.datax.powerjob.TestTISInitializeProcessor` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.datax.powerjob.TestTISTableJoinProcessor` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/executor/tis-datax-executor`

**TestAll.java 已生成,包含 2 个可运行测试类**:

- ✅ `com.qlangtech.tis.datax.TestDataXJobConsumer`
- ✅ `com.qlangtech.tis.datax.executor.BasicTISTableDumpProcessorTest`

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.datax.TestDataxPrePostConsumer` — [依赖数据库] com.qlangtech.tis.datax.DataXJobSingleProcessorException:  dataX:mysql_hive3,ERROR MSG:Process exited with an error: 1 (Exit value: 1)
- ⏭ `com.qlangtech.tis.datax.TestTisFlumeLogstashV1Appender` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-aliyun-jindo-sdk-extends/tis-datax-hdfs-aliyun-emr-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.hdfs.impl.TestAliayunJindoFSFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-cassandra-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXCassandraReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXCassandraWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.ds.cassandra.TestCassandraDatasourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-clickhouse-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXClickhouseWriter` — [其他运行错误] java.lang.UnsupportedOperationException:  please use: realExecuteDump(String dataXName, final WriterJson writerJson, IDataXPluginMeta dataxWriter)
- ⏭ `com.qlangtech.tis.plugin.ds.clickhouse.TestClickHouseDataSourceFactory` — [其他运行错误] java.lang.RuntimeException:  com.google.common.util.concurrent.UncheckedExecutionException: java.lang.RuntimeException: com.qlangtech.tis.lang.TisException: ru.yandex.clickhouse.except.ClickHouseUnkno

### `tis-datax/tis-datax-common-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataFlowDataXProcessor` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXGlobalConfig` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXJobSubmit` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDefaultDataxProcessor` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDistributedOverseerDataXJobSubmit` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.TestTriggrWorkflowJobs` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.common.TestBasicDataXRdbmsWriter` — [其他运行错误] 未运行(模块未验证)

### `tis-datax/tis-datax-dameng-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.dameng.ds.TestDaMengDataSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.dameng.reader.TestDataXDaMengReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.dameng.writer.TestDataXDaMengWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.dameng.writer.TestDataXDaMengWriterReal` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-dfs-plugin`

**TestAll.java 已生成,包含 2 个可运行测试类**:

- ✅ `com.qlangtech.tis.plugin.datax.TestDataXDFSReaderWithMeta`
- ✅ `com.qlangtech.tis.plugin.datax.resmatcher.TestMetaAwareDFSResMatcher`

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXDFSReader` — [其他运行错误] java.lang.RuntimeException:  java.lang.InstantiationException
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXDFSWriter` — [其他运行错误] java.lang.RuntimeException:  java.lang.RuntimeException: java.lang.reflect.InaccessibleObjectException: Unable to make private static java.lang.Object java.util.function.Function.lambda$identity$2(jav
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXDFSWriterReal` — [其他运行错误] java.lang.IllegalStateException:  Could not find a valid Docker environment. Please see logs and check configuration
- ⏭ `com.qlangtech.tis.plugin.datax.format.TestCSVFormat` — [其他运行错误] java.lang.UnsupportedOperationException: 	at com.qlangtech.tis.plugin.datax.format.CSVReaderFormat.createWriter(CSVReaderFormat.java:72)
- ⏭ `com.qlangtech.tis.plugin.datax.format.TestTextFormat` — [其他运行错误] java.lang.UnsupportedOperationException: 	at com.qlangtech.tis.plugin.datax.format.TextReaderFormat.createWriter(TextReaderFormat.java:45)

### `tis-datax/tis-datax-dolphinscheduler-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.doplinscheduler.TestDSWorkflowPayload` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.doplinscheduler.export.TestExportTISPipelineToDolphinscheduler` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.doplinscheduler.history.TestDSWorkFlowBuildHistoryPayload` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-doris-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.doris.TestDataXDorisWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.ds.doris.TestDorisSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-elasticsearch-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXElasticsearchWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.elastic.TestElasticEndpoint` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-hbase11x-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHbase11xReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHbase11xWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-hbase11xsql-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHbase11xsqlReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHbase11xsqlWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-hbase20xsql-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHbase20xsqlReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHbase20xsqlWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-hdfs-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.hdfs.impl.TestHdfsFileSystemFactory` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  expected:<2> but was:<3>
- ⏭ `com.qlangtech.tis.hdfs.impl.TestHdfsFileSystemFactoryIT` — [其他运行错误] java.lang.RuntimeException:  java.lang.RuntimeException: java.lang.IllegalStateException: Name:tis_kerberos,type:kerberos can not find relevant config in[]
- ⏭ `com.qlangtech.tis.plugin.amazon.s3.AmazonS3FileSystemFactoryTest` — [其他运行错误] java.lang.RuntimeException:  java.lang.RuntimeException: create connection for S3/MinIO service faild: Failed to create S3 FileSystem for path: s3a://tis/test, detail: doesBucketExist on tis: com.amaz

### `tis-datax/tis-datax-hdfs-reader-writer-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.hdfs.impl.TestHdfsFileSystemFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHdfsReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHdfsWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-kingbase-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.ds.kingbase.TestKingBaseDataSourceFactory` — [依赖数据库] java.lang.RuntimeException:  com.qlangtech.tis.lang.TisException: dispatch can not be null,jdbcUrl:jdbc:kingbase8://192.168.28.201:4321/test?ssl=false&stringtype=unspecified

### `tis-datax/tis-datax-local-akka-executor`

**TestAll.java 已生成,包含 1 个可运行测试类**:

- ✅ `com.qlangtech.tis.plugin.akka.DAORestDelegateFacadeTest`

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.dag.actor.TestWorkflowInstanceActor` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  assertion failed: timeout (5 minutes) during expectMsgClass waiting for class com.qlangtech.tis.dag.actor.message.DispatchTask
- ⏭ `com.qlangtech.tis.plugin.akka.DistributedAKKAJobDataXJobSubmitTest` — [依赖数据库] java.lang.RuntimeException:  com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: Could not create connection to database server. Attempted reconnect 3 times. Giving up.

### `tis-datax/tis-datax-local-embedded-executor`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestEmbeddedDataXJobSubmit` — [其他运行错误] java.lang.UnsupportedOperationException: 	at com.qlangtech.tis.datax.DataXJobSubmit.previewRowsData(DataXJobSubmit.java:267)

### `tis-datax/tis-datax-local-executor`

**TestAll.java 已生成,包含 1 个可运行测试类**:

- ✅ `com.qlangtech.tis.plugin.datax.T`

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestLocalDataXJobSubmit` — 依赖真实 ZooKeeper 连接

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXPipelinePreviewProcessorExecutor` — [其他运行错误] io.grpc.StatusRuntimeException:  UNAVAILABLE: io exception
- ⏭ `com.qlangtech.tis.plugin.datax.TestDefaultDataxSplitTabSyncConsumer` — [Mockito mock 构造失败] org.mockito.exceptions.base.MockitoException:

### `tis-datax/tis-datax-local-powerjob-executor`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDistributedPowerJobDataXJobSubmit` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.powerjob.TestTISPowerJobClient` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-mongodb-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXMongodbReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXMongodbReaderRealDump` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXMongodbWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.mongo.TestMongoCMeta` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.mongo.TestMongoCMetaCreatorFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.mongo.TestMongoColumnMetaData` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.ds.mangodb.TestMangoDBDataSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-odps-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXOdpsWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-oracle-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXOracleReader` — [其他运行错误] 测试失败(详见 surefire 报告)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXOracleReaderDump` — [其他运行错误] java.lang.IllegalStateException:  Previous attempts to find a Docker environment failed. Will not retry. Please see logs and check configuration
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXOracleWriter` — [其他运行错误] java.lang.RuntimeException:  java.lang.RuntimeException: java.lang.reflect.InaccessibleObjectException: Unable to make private static java.lang.Object java.util.function.Function.lambda$identity$2(jav
- ⏭ `com.qlangtech.tis.plugin.ds.oracle.TestOracleDataSourceFactory` — [其他运行错误] java.lang.IllegalStateException:  Could not find a valid Docker environment. Please see logs and check configuration

### `tis-datax/tis-datax-postgresql-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXPostgresqlReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXPostgresqlWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.ds.postgresql.TestPGDataSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-datax-sqlserver-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXSqlserverReader` — [断言失败(金样/逻辑与当前环境不符)] java.lang.NullPointerException:  splitTableStrategy can not be null
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXSqlserverWriter` — [其他运行错误] java.lang.RuntimeException:  java.lang.RuntimeException: java.lang.reflect.InaccessibleObjectException: Unable to make private static java.lang.Object java.util.function.Function.lambda$identity$2(jav
- ⏭ `com.qlangtech.tis.plugin.ds.sqlserver.TestSqlServerDatasourceFactory` — [其他运行错误] java.lang.RuntimeException:  java.lang.InstantiationException

### `tis-datax/tis-datax-starrocks-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.starrocks.TestDataXStarRocksWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-ds-mysql-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataxMySQLReaderDump` — 依赖 Testcontainers/Docker 环境

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataxMySQLReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataxMySQLWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.ds.mysql.TestMySQLDataSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-ds-mysql-v5-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.ds.mysql.TestMySQLV5DataSourceFactory` — [断言失败(金样/逻辑与当前环境不符)] java.lang.RuntimeException:  java.lang.NullPointerException: splitTableStrategy can not be null
- ⏭ `com.qlangtech.tis.plugin.ds.mysql.TestMySQLV5DataSourceReader` — [断言失败(金样/逻辑与当前环境不符)] java.lang.RuntimeException:  java.lang.NullPointerException: splitTableStrategy can not be null

### `tis-datax/tis-ds-tidb-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXTiDBReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.ds.tidb.TestTiKVDataSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-hive-flat-table-builder-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.alibaba.datax.plugin.unstructuredstorage.reader.TestTEXTFormat` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.dump.hive.TestHiveDBUtils` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.dump.hive.TestHiveRemoveHistoryDataTask` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.dump.hive.TestHiveTableBuilder` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hdfs.impl.TestJobConfig` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.Test` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestDefaultHiveConnGetter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHdfsFileType` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHdfsFormat` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHiveInsertFromSelectParser` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHiveMeta` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHiveserver2DataSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHiveserverAliyunDataSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.reader.TestDataXHiveReader` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHiveWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXHiveWriterDump` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.datax.TestDataXSparkWriter` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-datax/tis-hive-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.dump.hive.TestHiveDBUtils` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestDefaultHiveConnGetter` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHiveMeta` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHiveserver2DataSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHiveserverAliyunDataSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-hdfs-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.hdfs.impl.TestHdfsFileSystemFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.hive.TestHdfsFormat` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-chunjun-base-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.sink.TestSinkTabPropsExtends` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.sink.TestUniqueKeySetter` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-cdc-kingbase-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.cdc.kingbase.FlinkCDCKingBaseSourceFactoryTest` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.cdc.kingbase.source.KingBaseConnectionPoolFactoryTest` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-cdc-mongdb-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.cdc.mongdb.MongoDBFullChangelogITCase` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.cdc.mongdb.TestTISFlinkCDCMongoDBSourceFunction` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.cdc.mongdb.TestX` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-cdc-mysql-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.cdc.maria.TestFlinkCDCMariaDBSourceFactory` — 依赖 Testcontainers/Docker 环境
- ⏭ `com.qlangtech.tis.plugins.incr.flink.cdc.mysql.TestFlinkCDCMySQLSourceFactory` — 依赖 Testcontainers/Docker 环境

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.cdc.mysql.BatchUpdate` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.cdc.mysql.TestTime` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.cdc.mysql.startup.TestTimestampStartupOptions` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-cdc-oracle-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.cdc.oracle.TestTISFlinkCDCOracleSourceFunction` — 依赖 Testcontainers/Docker 环境

### `tis-incr/tis-flink-cdc-postgresql-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.cdc.postgresql.TestFlinkCDCPostgreSQLSourceFunction` — 依赖 Testcontainers/Docker 环境

### `tis-incr/tis-flink-chunjun-clickhouse-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.clickhouse.sink.TestChunjunClickhouseSinkFactory` — 依赖 Testcontainers/Docker 环境

### `tis-incr/tis-flink-chunjun-dameng-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.dameng.sink.TestChunjunDamengSinkFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-chunjun-doris-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.doris.sink.TestChunjunDorisSinkFactory` — 依赖 Testcontainers/Docker 环境

**运行验证跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.doris.sink.TestChunjunDorisSinkFactoryWithoutDocker` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.doris.sink.TestLogger` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-chunjun-kafka-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.sink.TestChujunKafkaSinkFactoryIntegration` — 依赖 Testcontainers/Docker 环境

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.cdc.pipeline.kafka.sink.TestKafkaPipelineSinkFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.sink.TestChujunKafkaSinkFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-chunjun-kingbase-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.connector.kingbase.sink.TestKingBaseSinkFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-chunjun-mysql-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.connector.source.TestMySQLSourceFactory` — 依赖 Testcontainers/Docker 环境

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.connector.sink.TestMySQLSinkFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-chunjun-oracle-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.oracle.sink.TestChunjunOracleSinkFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.oracle.source.TestChunjunOracleSourceFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.oracle.source.TestDataSourceGetter` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-chunjun-postgresql-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.postgresql.sink.TestChunjunPostgreSQLSinkFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.chunjun.postgresql.sink.TestChunjunPostgreSQLSinkFactoryByFullTypes` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-chunjun-starrocks-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.starrocks.TestChunjunStarRocksSinkFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-extends`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.TestHudiTISFlinkClassLoaderFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.TestTISFlinkClassLoaderFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-flink-msg-format-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.format.FormatFactoryTest` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.format.TestTISCanalJsonFormatFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.format.TestTISSinkDebeziumJsonFormatFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.format.canaljson.TISCanalJsonFormatFactoryTest` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.format.debeziumjson.TISSourceDebeziumJsonFormatFactoryTest` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.format.json.SourceJsonFormatFactoryTest` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.format.json.TestSinkJsonFormatFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.incr.flink.chunjun.kafka.format.json.TestSourceJsonFormatFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-realtime-flink`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.plugins.incr.flink.TestTISFlinkCDCStart` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.cdc.TestAbstractRowDataMapper` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.cdc.TestFlinkCol` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.cdc.TestSourceChannel` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.common.TestFlinkCluster` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.launch.TestFlinkIncrJobStatus` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.launch.TestFlinkTaskNodeController` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.launch.TestTISFlinkCDCStreamFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.launch.ckpt.TestCheckpointFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.plugins.incr.flink.launch.clustertype.StandaloneFlinkDeployingAIAssistSupportTest` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugins.flink.client.TestFlinkClient` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-incr/tis-scala-compiler`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.compiler.streamcode.TestCompileAndPackage` — [其他运行错误] 未运行(模块未验证)

### `tis-incr/tis-sink-elasticsearch7-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.connector.elasticsearch7.TestElasticSearchSinkFactory` — 依赖 Flink 测试集群(AbstractTestBase/MiniCluster,部分 test-utils 制品本地缺失)

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugins.incr.flink.connector.elasticsearch7.TestTISXContentBuilderExtension` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-k8s-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.config.k8s.impl.TestDefaultK8SImage` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.config.k8s.impl.TestDefaultK8sContext` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.TestK8SDataXJobWorker` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.powerjob.TestK8SDataXJobWorker` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.powerjob.TestK8SDataXPowerJobWorker` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.powerjob.impl.coresource.TestDefaultPowerjobCoreDataSource` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.powerjob.impl.coresource.TestEmbeddedPowerjobCoreDataSource` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.datax.powerjob.impl.serverport.TestNodePort` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.incr.TestDefaultIncrK8sConfig` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.incr.TestDefaultWatchPodLog` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.incr.TestK8sIncrSync` — [其他运行错误] 未运行(模块未验证)
- ⏭ `com.qlangtech.tis.plugin.k8s.TestK8SUtils` — [其他运行错误] 未运行(模块未验证)

### `tis-kafka-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.async.message.client.kafka.TestKafkaMQListener` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-kerberos-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.kerberos.TestKerberosCfg` — [其他运行错误] 未运行(模块未验证)

### `tis-ontology-plugin`

**TestAll.java 已生成,包含 60 个可运行测试类**:

- ✅ `com.qlangtech.tis.plugin.ontology.EnableChatBITest`
- ✅ `com.qlangtech.tis.plugin.ontology.TestStreamingJsonOntologyParser`
- ✅ `com.qlangtech.tis.plugin.ontology.TestStreamingJsonOntologyParserDebug`
- ✅ `com.qlangtech.tis.plugin.ontology.aggregation.TestAggregationCriteria`
- ✅ `com.qlangtech.tis.plugin.ontology.chatbi.FalconChatBITestExampleTest`
- ✅ `com.qlangtech.tis.plugin.ontology.chatbi.TestTraceStep`
- ✅ `com.qlangtech.tis.plugin.ontology.chatbi.validation.TestAstValidator`
- ✅ `com.qlangtech.tis.plugin.ontology.chatbi.validation.TestExplainValidator`
- ✅ `com.qlangtech.tis.plugin.ontology.chatbi.validation.TestKeywordWhitelistValidator`
- ✅ `com.qlangtech.tis.plugin.ontology.chatbi.validation.TestPrestoParserDebug`
- ✅ `com.qlangtech.tis.plugin.ontology.chatbi.validation.TestPrestoParserDebug2`
- ✅ `com.qlangtech.tis.plugin.ontology.graphrag.DefaultGraphRAGServiceITTest`
- ✅ `com.qlangtech.tis.plugin.ontology.graphrag.DefaultGraphRAGServiceTest`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.action.TestActionFormDescriptorBuilder`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.action.TestActionParameterMapper`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.action.TestActionRelevantPayload`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.action.TestActionRuleRidCascade`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.action.TestActionRuleSetterPersistence`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.action.TestActionRulesMulti`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.action.TestActionStepChain`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.action.type.TestLinkRuleSupport`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.action.type.TestMultiObjectTypeNamespace`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.infer.InferOntologyFromLLMStep1Test`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.objtype.TestDataSourceBindingAggregation`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.objtype.TestObjectSetRowQuery`
- ✅ `com.qlangtech.tis.plugin.ontology.impl.objtype.aggregation.TestAggregationSqlBuilder`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.TestWidgetDisplayReconcile`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.TestWorkshopFormDefaults`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.TestWorkshopPageFormRoundTrip`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.WorkshopModuleReloadTest`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.desc.TestNoLegacyVariableOperationEndpoint`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.model.TestConditionalVisibilitySplit`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.model.TestSecondaryMetricSplit`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.model.TestTimeSeriesVisualizationSplit`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.model.TestWorkshopOverlayFormRoundTrip`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.model.TestWorkshopVariableTypes`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.model.config.TestVariableBasedVisibilitySplit`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.model.config.TestWorkshopConfigPlugins`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.model.definition.TestObjectSetFilterCriteria`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.model.section.TestWorkshopSectionConfig`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.service.TestVariableReferenceScanner`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.store.WorkshopLayoutStoreTest`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.store.WorkshopPageStoreTest`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.store.WorkshopWidgetDisplayStoreTest`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.TestFilterItemConfigContract`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.TestWidgetColumnConfigColumnMapping`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.component.TestHistogramComponentPayload`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.component.TestHistogramDataProcessor`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.component.TestOpsSourceOptions`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.ObjectListWidgetTest`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.TestNoLegacyObjectSetEndpoint`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.TestObjectListActiveObjectSelection`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.TestObjectListWidgetMaxObjects`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.TestObjectListWidgetSortingAndSelection`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.TestObjectTableWidgetPagenator`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.TestObjectTableWidgetSortOrder`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.chartxy.TestAggregationPushdownPlanner`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.chartxy.TestChartXYAggregation`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.chartxy.TestTemporalBucketFormat`
- ✅ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.chartxy.TestTemporalBucketStart`

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.ontology.chatbi.DefaultChatBIServiceITTest` — [其他运行错误] java.lang.IllegalStateException:  EXPLAIN validation failed
- ⏭ `com.qlangtech.tis.plugin.ontology.chatbi.prompt.TestPromptBuilder` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  28 英文字符 ≈ 7 tokens expected:<7> but was:<6>
- ⏭ `com.qlangtech.tis.plugin.ontology.impl.action.TestCustomizeContent` — [其他运行错误] java.lang.NoSuchFieldException:  submitButton
- ⏭ `com.qlangtech.tis.plugin.ontology.impl.action.widget.TestUserInputWidgetType` — [依赖数据库] java.lang.AssertionError:  以下 FormFieldType 常量没有任何子类覆盖（界面上该控件类型选不到）：[Color, JDBCColumn, MULTI_DESCRIBLE_PLUGIN, MULTI_SELECTABLE, SELECTABLE]
- ⏭ `com.qlangtech.tis.plugin.ontology.impl.glossary.DefaultOntologyGlossaryTest` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError: 	at org.junit.Assert.fail(Assert.java:87)
- ⏭ `com.qlangtech.tis.plugin.ontology.impl.infer.DeserializeOntologyResTest` — [其他运行错误] java.lang.IllegalStateException:  domain 'order2' has no ObjectType, please export tables first
- ⏭ `com.qlangtech.tis.plugin.ontology.impl.linker.RelationshipTypeBackingObjectTypeTest` — [其他运行错误] java.lang.IllegalStateException:  typeDir:/opt/data/tis/cfg_repo/tis_plugin_config/ontology-domain/falcon_14/object-type must be exist
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.model.TestWorkshopSectionChildSections` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  子分区默认 List 视图 expected:<List> but was:<Table>
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.model.TestWorkshopVariableDeleteStored` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError: 	at org.junit.Assert.fail(Assert.java:87)
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.model.WorkshopVariableTest` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  变量应落在 /opt/data/tis/cfg_repo/tis_plugin_config/ontology-domain/test_workshop_variable_domain/workshop/test_workshop_module/test_string_var.xml
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.model.definition.TestVariableTypeDefinitions` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  VariableType.OBJECT_SET 的可用定义方式与设计矩阵不一致 expected:<2> but was:<3>
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.model.header.TestWorkshopHeaderConfig` — [其他运行错误] 测试失败(详见 surefire 报告)
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.widget.TestChartXYStructure` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  property 不得挂 Validator.require：Count 子类把它 disable 了，挂了会让 Count 的默认配置永远校验失败
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.widget.TestWidgetFieldStructure` — [其他运行错误] java.lang.RuntimeException:  parse desc:com.qlangtech.tis.plugin.ontology.workshop.widget.impl.ChartPieWidget
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.widget.TestWidgetVariableContract` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.widget.TestWorkshopWidgetEnumPropertyType` — [断言失败(金样/逻辑与当前环境不符)] java.lang.AssertionError:  expected null, but was:<纯文本>
- ⏭ `com.qlangtech.tis.plugin.ontology.workshop.widget.impl.TestFilterListOptionsEndpoint` — [其他运行错误] java.lang.RuntimeException:  parse desc:com.qlangtech.tis.plugin.ontology.workshop.widget.impl.FilterListWidget

### `tis-plugin-test/customer-plugin-test`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.hdfs.impl.TestHdfsFileSystemFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)
- ⏭ `com.qlangtech.tis.plugin.incr.TestDefaultIncrK8sConfig` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-plugin-test/tis-testcontainer-dameng`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.ds.oracle.TestDamengDSFactoryContainer` — [其他运行错误] java.lang.IllegalStateException:  Could not find a valid Docker environment. Please see logs and check configuration

### `tis-plugin-test/tis-testcontainer-kingbase`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.ds.oracle.TestKingBaseDSFactoryContainer` — [其他运行错误] java.lang.IllegalStateException:  kingbaseConf:/opt/misc/kingbase_data

### `tis-plugin-test/tis-testcontainer-mysql`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**静态预筛跳过**:

- ⏭ `com.ververica.cdc.connectors.mysql.testutils.TestMySqlContainer` — 依赖 Testcontainers/Docker 环境

### `tis-solr-plugin`

**无可运行测试类**(TestAll.java 为空套件或未生成):

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.solr.schema.TestJSONFieldTypeFactory` — [其他运行错误] 未运行(模块构建失败或运行中止)

### `tis-transformer`

**TestAll.java 已生成,包含 3 个可运行测试类**:

- ✅ `com.qlangtech.tis.plugin.datax.transformer.impl.joiner.JoinCacheKeyBuilderTest`
- ✅ `com.qlangtech.tis.plugin.datax.transformer.impl.joiner.cache.TargetRowsCacheFullTest`
- ✅ `com.qlangtech.tis.plugin.datax.transformer.impl.joiner.cache.TargetRowsCacheOnTest`

**运行验证跳过**:

- ⏭ `com.qlangtech.tis.plugin.datax.transformer.impl.TestAutoGenerateBigIntIDUDF` — [其他运行错误] 测试失败(详见 surefire 报告)
- ⏭ `com.qlangtech.tis.plugin.datax.transformer.impl.TestCopyValUDF` — [其他运行错误] 测试失败(详见 surefire 报告)
- ⏭ `com.qlangtech.tis.plugin.datax.transformer.impl.TestDataMaskingUDF` — [断言失败(金样/逻辑与当前环境不符)] java.lang.RuntimeException:  java.lang.NullPointerException: resource:data_masking_udf/descriptor.json can not find relevant content,relevant class:com.qlangtech.tis.plugin.datax.transformer.impl.Data
- ⏭ `com.qlangtech.tis.plugin.datax.transformer.impl.TestJSONSplitterUDF` — [断言失败(金样/逻辑与当前环境不符)] java.lang.RuntimeException:  expected:<2> but was:<1>
- ⏭ `com.qlangtech.tis.plugin.datax.transformer.impl.TestRegexPatternReplace` — [断言失败(金样/逻辑与当前环境不符)] java.lang.RuntimeException:  java.lang.NullPointerException: resource:regex_pattern_replace/descriptor.json can not find relevant content,relevant class:com.qlangtech.tis.plugin.datax.transformer.impl

---

## 模块级整组跳过说明

- `tis-solr/tis-sql-parser` — 模块 test-compile 失败:`TestJoinTaskContext.getTaskId()` 返回 `int` 与接口 `Integer` 不兼容(迁移前已存在,已用 HEAD 工作树验证),该模块全部测试无法验证
- `plugins/tis-datax/tis-datax-common-plugin` — 模块 test-compile 失败:`IAppSource.load` 缺强转 + 引用被注释的 `DataXJobSubmit.InstanceType.DISTRIBUTE`(迁移前已存在)
- `plugins/tis-k8s-plugin` — 模块 test-compile 失败:测试引用被整文件注释掉的 `EmbeddedPowerjobCoreDataSource`(迁移前已存在)
- `plugins/tis-asyncmsg-rocketmq-plugin` — 模块 test-compile 失败:引用已删除的 `IMQConsumerStatusFactory` 与过期的 `Descriptor.FormVaildateType`(迁移前已存在)
- `plugins/tis-kerberos-plugin` — 模块 test-compile 失败:引用过期的 `Descriptor.FormVaildateType`(迁移前已存在)
- `plugins/tis-incr/tis-scala-compiler` — 离线环境缺制品 `com.qlangtech.tis:ibator:1.0.1`,依赖无法解析(环境限制)
- `plugins/tis-incr/tis-flink-dependency` — 离线环境缺制品 `tisasm-maven-plugin:1.0.7`、`flink-runtime:tis-1.20.1`(环境限制)
- `plugins/tis-plugin-test/tis-testcontainer-postgresql` — 离线环境缺制品 `flink-test-utils:tis-1.20.1`(环境限制;且模块本身即 testcontainer 用例)
- `tis-solr/tis-collection-info-collect` — 独立项目(版本 3.6.0-alpha),父 POM `com.qlangtech.tis:tis:3.6.0-alpha` 离线不可解析;且不在根 pom reactor 中,CI 不会构建该模块
- `tis-solr/tis-console` — `TestLoopQueue` 运行挂起(240s 无响应,疑似等待外部资源)单独跳过;其余 26 个候选类均已运行验证

### plugins 中未在 reactor 启用的模块(pom `<modules>` 被注释)

这些模块的测试不会进入 CI 构建,其用例全部按“模块未启用”跳过(未运行):

- `tis-datax/executor/dolphinscheduler-task-tis-datasync`(3 个测试类)
- `tis-datax/executor/powerjob-worker-samples`(3 个测试类)
- `tis-datax/tis-datax-cassandra-plugin`(3 个测试类)
- `tis-datax/tis-datax-dolphinscheduler-plugin`(3 个测试类)
- `tis-datax/tis-datax-hbase11x-plugin`(2 个测试类)
- `tis-datax/tis-datax-hbase11xsql-plugin`(2 个测试类)
- `tis-datax/tis-datax-hbase20xsql-plugin`(2 个测试类)
- `tis-datax/tis-datax-local-powerjob-executor`(2 个测试类)
- `tis-datax/tis-ds-tidb-plugin`(2 个测试类)
- `tis-datax/tis-hive-plugin`(5 个测试类)
- `tis-hdfs-plugin`(2 个测试类)
- `tis-kafka-plugin`(1 个测试类)
- `tis-plugin-test/customer-plugin-test`(2 个测试类)
- `tis-solr-plugin`(1 个测试类)

---

## CI 使用说明

1. `tis-solr` 根 pom surefire 已配置 `<includes>**/TestAll.java</includes>`,
   GitHub Actions 直接 `mvn test` 即只会运行各模块 `TestAll.java`。
2. 已在 tis-solr 根 pom surefire 配置中补充:
   `<argLine>--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED</argLine>`,
   descriptor 扫描类测试(ClassLoaderReflectionToolkit)在 JDK17 必需。
3. `TestAll.java` 中为空套件(`@Suite.SuiteClasses({})`)的模块,surefire 会运行 0 个测试并保持绿。
4. plugins 根 pom 的 surefire 配置处于注释状态(默认跑全部 `*Test`),
   如需同样只跑 TestAll,请自行启用并复用相同 includes 配置。
