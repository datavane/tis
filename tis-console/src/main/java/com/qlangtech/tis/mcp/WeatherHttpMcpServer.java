/*
 * MCP Weather Server Example — HTTP Streamable Transport (Embedded Tomcat)
 *
 * A minimal MCP server that exposes a "get_temperature" tool via HTTP,
 * using the Streamable HTTP transport with an embedded Tomcat server on port 8080.
 *
 * Dependencies (Maven):
 *   <dependency>
 *     <groupId>io.modelcontextprotocol.sdk</groupId>
 *     <artifactId>mcp</artifactId>
 *     <version>${mcp.version}</version>
 *   </dependency>
 *   <dependency>
 *     <groupId>org.apache.tomcat.embed</groupId>
 *     <artifactId>tomcat-embed-core</artifactId>
 *     <version>${tomcat.version}</version>
 *   </dependency>
 *
 * Run:
 *   java -cp <classpath> WeatherHttpMcpServer
 *
 * The server listens at http://localhost:8080/mcp
 */
package com.qlangtech.tis.mcp;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.core.ISessionData;
import com.qlangtech.tis.aiagent.core.RequestKey;
import com.qlangtech.tis.aiagent.llm.FlatJsonToTisConverter;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.IAjaxResult;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.offline.module.manager.impl.OfflineManager;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.ItemsErrors;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescriptorsJSONForAIPrompt;
import com.qlangtech.tis.util.DescriptorsMeta;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.qlangtech.tis.aiagent.execute.impl.BasicStepExecutor.createPluginContext;
import static com.qlangtech.tis.aiagent.llm.JsonSchema.SCHEMA_ADDITIONAL_PROPS;
import static com.qlangtech.tis.aiagent.llm.JsonSchema.SCHEMA_PROPERTIES;
import static com.qlangtech.tis.aiagent.llm.JsonSchema.SCHEMA_RERUIRED;
import static com.qlangtech.tis.aiagent.llm.JsonSchema.SCHEMA_TYPE;

public class WeatherHttpMcpServer {
  private static final Logger logger = LoggerFactory.getLogger(WeatherHttpMcpServer.class);
  private static final Map<String, Integer> TEMPERATURE_DATA = Map.of("北京", 22, "上海", 26, "广州", 30, "深圳", 29, "成都", 20);
  @Autowired
  private OfflineManager offlineManager;

  public void setOfflineManager(OfflineManager offlineManager) {
    this.offlineManager = offlineManager;
  }

  public OfflineManager getOfflineManager() {
    if (offlineManager == null) {
      BasicServlet.autowireBeanProperties(this);
     // BasicServlet.getBeanByType(OfflineManager.class);
      Objects.requireNonNull(this.offlineManager, "offlineManager can not be null");
    }
    return offlineManager;
  }

  private static final Map<String, Object> outputSchema //
    = Map.of("type", "object", //
    "properties", Map.of( //
      IAjaxResult.KEY_SUCCESS, Map.of( //
        "type", "boolean", //
        "description", "操作是否成功执行"), //
      IAjaxResult.KEY_ERROR_MSG, Map.of( //
        "type", "array", //
        "items", Map.of("type", "string"),//
        "description", "错误信息列表，操作失败时包含具体错误原因"), //
      IAjaxResult.KEY_MSG, Map.of(//
        "type", "array",//
        "items", Map.of("type", "string"),//
        "description", "成功消息列表，操作成功时包含结果描述"),//
      IAjaxResult.KEY_BIZRESULT, Map.of(//
        "type", "object", "description", "业务返回结果，具体结构取决于操作类型"),//
      IAjaxResult.KEY_ERROR_FIELDS, Map.of(//
        "type", "array",//
        "description", "插件表单字段级别的校验错误，每个元素对应一个插件实例的字段错误列表",//
        "items", Map.of(//
          "type", "array",//
          "items", Map.of(//
            "type", "object",//
            "properties", Map.of(//
              "name", Map.of("type", "string", "description", "出错的字段名"),//
              "content", Map.of("type", "string", "description", "错误描述内容")))))),//
    "required", List.of(IAjaxResult.KEY_SUCCESS));

  public static HttpServletStreamableServerTransportProvider getMcpProvider() {
    HttpServletStreamableServerTransportProvider transportProvider =
      HttpServletStreamableServerTransportProvider.builder().mcpEndpoint("/mcp").build();
    WeatherHttpMcpServer mcpServer = new WeatherHttpMcpServer();
    McpSyncServer server = McpServer.sync(transportProvider).serverInfo("weather-server", "1.0.0") //
      .prompts().tools(createToolSpec(), mcpServer.createDataSourceCreateToolSpec()).build();

    //	Tomcat tomcat = createEmbeddedTomcat(transportProvider);

    try {
      //			tomcat.start();
      //			System.out.println("Weather MCP Server listening on http://localhost:8080/mcp");
      //			tomcat.getServer().await();
    } finally {
      //		server.closeGracefully();
      //		tomcat.stop();
    }
    return transportProvider;
  }

  //	private static Tomcat createEmbeddedTomcat(
  //			HttpServletStreamableServerTransportProvider transportProvider) {
  //		Tomcat tomcat = new Tomcat();
  //		tomcat.setPort(8080);
  //
  //		String baseDir = System.getProperty("java.io.tmpdir");
  //		tomcat.setBaseDir(baseDir);
  //
  //		Context context = tomcat.addContext("", baseDir);
  //
  //		Wrapper wrapper = context.createWrapper();
  //		wrapper.setName("mcpServlet");
  //		wrapper.setServlet(transportProvider);
  //		wrapper.setLoadOnStartup(1);
  //		wrapper.setAsyncSupported(true);
  //		context.addChild(wrapper);
  //		context.addServletMappingDecoded("/*", "mcpServlet");
  //
  //		tomcat.getConnector().setAsyncTimeout(30000);
  //
  //		return tomcat;
  //	}

  private static String mysqlSchema = " {\n" + "    \"type\": \"object\",\n" + "    \"properties\": {                "
    + "                                                                                                              "
    + "                                                                                       \n" + "      \"impl\": "
    + "{                                                                                                             "
    + "                                                                                                          \n" + "        \"const\": \"com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory\",\n" + "        \"description\": \"concrete plugin implement class\",\n" + "        \"type\": \"string\"\n" + "      },\n" + "      \"vals\": {\n" + "        \"type\": \"object\",\n" + "        \"additionalProperties\": false,\n" + "        \"properties\": {\n" + "          \"encode\": {\n" + "            \"type\": \"string\",\n" + "            \"default\": \"utf8\",\n" + "            \"pattern\": \"[A-Z\\\\da-z_\\\\-]+\",\n" + "            \"description\": \"数据数据\\n `default`:\\\"utf8\\\"\",\n" + "            \"enum\": [\"gbk\", \"utf8\"]\n" + "          },\n" + "          \"password\": {\n" + "            \"type\": \"string\",\n" + "            \"pattern\": \"([^\\\\s]+)\",\n" + "            \"description\": \"\"\n" + "          },\n" + "          \"port\": {\n" + "            \"type\": \"integer\",\n" + "            \"default\": 3306,\n" + "            \"pattern\": \"-?[1-9]{1}[\\\\d]{0,}|0\",\n" + "            \"description\": \"\\n `default`:3306\"\n" + "          },\n" + "          \"splitTableStrategy\": {\n" + "            \"description\": \"数据库中使用分表策略：\\n* `off`：不启用分表策略\\n* `on`: 启用分表策略。每张分表数据结构需要保证相同，且有规则的后缀作为物理表的分区规则，逻辑层面视为同一张表。如逻辑表`order` 对应的物理分表为：\n" + "  `order_01`,`order_02`,`order_03`,`order_04`\\n\\n**注意**：若用户未提及分表、多节点、表后缀等关键词，默认选择 id =\n" + "  'off'\\n\\n[详细说明](https://tis.pub/docs/guide/datasource/multi-table-rule/)\\n若用户未提及分表、多节点、表后缀等关键词，默认选择 id = 'off'\",\n" + "            \"oneOf\": [\n" + "              {\n" + "                \"type\": \"object\",\n" + "                \"additionalProperties\": false,\n" + "                \"properties\": {\n" + "                  \"id\": {\n" + "                    \"const\": \"off\",\n" + "                    \"description\": \"代表本`oneOf`之一的标识符\",\n" + "                    \"type\": \"string\"\n" + "                  },\n" + "                  \"host\": {\n" + "                    \"type\": \"string\",\n" + "                    \"pattern\": \"[-A-Za-z0-9+&@#/%?=~_|!,.;]+[-A-Za-z0-9+&@#/%=~_|]\",\n" + "                    \"description\": \"服务器节点连接地址，可以为IP或者域名,例子:192.168.28.200\\n单数据源地址（仅当未启用分表时使用）。注意：此字段与分表策略中的 nodeDesc 完全无关。\"\n" + "                  }\n" + "                },\n" + "                \"required\": [\"id\", \"host\"]\n" + "              },\n" + "              {\n" + "                \"type\": \"object\",\n" + "                \"additionalProperties\": false,\n" + "                \"properties\": {\n" + "                  \"id\": {\n" + "                    \"const\": \"on\",\n" + "                    \"description\": \"代表本`oneOf`之一的标识符\",\n" + "                    \"type\": \"string\"\n" + "                  },\n" + "                  \"prefixWildcardStyle\": {\n" + "                    \"type\": \"boolean\",\n" + "                    \"default\": false,\n" + "                    \"description\": \"使用前缀匹配的样式，在flink-cdc表前缀通配匹配的场景中使用\\n* 选择`是`：在增量监听流程中使用`逻辑表`+`*`的方式对目标表监听，例如，逻辑表名为`base`,启动时使用`base*` 对数据库中\n" + "  `base01`,`base02`启用增量监听，在运行期用户又增加了`base03`表则执行逻辑会自动对`base03`表开启监听\\n*\n" + "  选择`否`：在增量监听流程中使用物理表全匹配的方式进行匹配。在运行期用户增加的新的分表忽略，如需对新加的分表增量监听生效，需要重启增量执行管道。\\n `default`:false\",\n" + "                    \"enum\": [true, false]\n" + "                  },\n" + "                  \"tabPattern\": {\n" + "                    \"type\": \"string\",\n" + "                    \"description\": \"识别分表的正则式，默认识别分表策略为 `(tabname)_\\\\d+` , 如需使用其他分表策略，如带字母[a-z]的后缀则需要用户自定义\\n\\n`注意`：如输入自定义正则式，表达式中逻辑表名部分，必须要用括号括起来，不然无法\n" + "  从物理表名中抽取出逻辑表名。\\n\\n**可参考**：https://github.com/qlangtech/tis/issues/361,例子:(\\\\S+?)(_\\\\d+)?\"\n" + "                  },\n" + "                  \"testTab\": {\n" + "                    \"type\": \"string\",\n" + "                    \"description\": \"提交表单用户测试，所填正则式是否能正确识别物理分表。输入需要识别的逻辑表名，点击'校验'按钮会进行自动识别。,例子:orderdetail\"\n" + "                  },\n" + "                  \"nodeDesc\": {\n" + "                    \"type\": \"string\",\n" + "                    \"description\": \"将分布在多个数据库冗余节点中的物理表视作一个逻辑表，在数据同步管道中进行配置，输入框中可输入以下内容：\\n\\n* `192.168.28.200[00-07]` ： 单节点多库，导入 192.168.28.200:3306 节点的\n" + "  order00,order01,order02,order03,order04,order05,order06,order078个库。也可以将节点描述写成：`192.168.28.200[0-7]`，则会导入 192.168.28.200:3306 节点的 order0,order1,order2,order3,order4,order5,order6,order78个库\\n*\n" + "  `192.168.28.200[00-07],192.168.28.201[08-15]`：会导入 192.168.28.200:3306 节点的 order00,order01,order02,order03,order04,order05,order06,order078个库 和 192.168.28.201:3306 节点的 order08,order09,order10,order11,order12,order13,or\n" + "  der14,order158个库，共计16个库\\n\\n[详细说明](http://tis.pub/docs/guide/datasource/multi-ds-rule),例子:127.0.0.1[00-31],127.0.0.2[32-63],127.0.0.3,127.0.0.4[9],baisui.com[0-9]\\n仅当用户明确提到分表、多节点、表后缀范围（如\n" + "  [00-07]）时才填写此字段。\"\n" + "                  }\n" + "                },\n" + "                \"required\": [\"id\", \"prefixWildcardStyle\", \"tabPattern\", \"testTab\", \"nodeDesc\"]\n" + "              }\n" + "            ]\n" + "          },\n" + "          \"dbName\": {\n" + "            \"type\": \"string\",\n" + "            \"pattern\": \"[A-Z\\\\da-z_\\\\-]+\",\n" + "            \"description\": \"数据库名,创建JDBC实例时用\"\n" + "          },\n" + "          \"name\": {\n" + "            \"type\": \"string\",\n" + "            \"pattern\": \"[A-Z\\\\da-z_\\\\-]+\",\n" + "            \"description\": \"数据源实例名称，请起一个有意义且唯一的名称\"\n" + "          },\n" + "          \"timeZone\": {\n" + "            \"description\": \"设置服务端所在时区，有两种输入方式：1. default 从下拉框中选择，2. customize：用户手动输入时区编码\",\n" + "            \"oneOf\": [\n" + "              {\n" + "                \"type\": \"object\",\n" + "                \"additionalProperties\": false,\n" + "                \"properties\": {\n" + "                  \"id\": {\n" + "                    \"const\": \"customize\",\n" + "                    \"description\": \"代表本`oneOf`之一的标识符\",\n" + "                    \"type\": \"string\"\n" + "                  },\n" + "                  \"timeZone\": {\n" + "                    \"type\": \"string\",\n" + "                    \"description\": \"设置服务端所在时区编码,例子:example：Asia/Shanghai\"\n" + "                  }\n" + "                },\n" + "                \"required\": [\"id\", \"timeZone\"]\n" + "              },\n" + "              {\n" + "                \"type\": \"object\",\n" + "                \"additionalProperties\": false,\n" + "                \"properties\": {\n" + "                  \"id\": {\n" + "                    \"const\": \"default\",\n" + "                    \"description\": \"代表本`oneOf`之一的标识符\",\n" + "                    \"type\": \"string\"\n" + "                  },\n" + "                  \"timeZone\": {\n" + "                    \"type\": \"string\",\n" + "                    \"default\": \"Asia/Shanghai\",\n" + "                    \"description\": \"选择服务端所在时区\\n `default`:\\\"Asia/Shanghai\\\"\",\n" + "                    \"enum\": [\"Australia/Sydney\", \"Africa/Cairo\", \"Europe/Paris\", \"Asia/Tokyo\", \"Asia/Shanghai\"]\n" + "                  }\n" + "                },\n" + "                \"required\": [\"id\", \"timeZone\"]\n" + "              }\n" + "            ]\n" + "          },\n" + "          \"extraParams\": {\n" + "            \"type\": \"string\",\n" + "            \"description\": \"例子:a=123&b=456\"\n" + "          },\n" + "          \"userName\": {\n" + "            \"type\": \"string\",\n" + "            \"default\": \"root\",\n" + "            \"pattern\": \"[A-Z\\\\da-z_\\\\-\\\\.\\\\$]+\",\n" + "            \"description\": \"\\n `default`:\\\"root\\\"\"\n" + "          },\n" + "          \"useCompression\": {\n" + "            \"type\": \"boolean\",\n" + "            \"default\": true,\n" + "            \"description\": \"与服务端通信时采用zlib进行压缩，效果请参考[https://blog.csdn.net/Shadow_Light/article/details/100749537](https://blog.csdn.net/Shadow_Light/article/details/100749537)\\n `default`:true\",\n" + "            \"enum\": [true, false]\n" + "          }\n" + "        },\n" + "        \"required\": [\"encode\", \"password\", \"port\", \"splitTableStrategy\", \"dbName\", \"name\", \"timeZone\", \"extraParams\", \"userName\", \"useCompression\"]\n" + "      }\n" + "    },\n" + "    \"required\": [\"impl\", \"vals\"],\n" + "    \"additionalProperties\": false\n" + "  }";

  @SuppressWarnings("all")
  private McpServerFeatures.SyncToolSpecification createDataSourceCreateToolSpec() {

    DescribableImpl pluginImpl = new DescribableImpl(DataSourceFactory.class,
      Optional.of(IEndTypeGetter.EndType.MySQL));
    pluginImpl.addImpl("com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory");

    //DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta
    Pair<DescriptorsMeta, DescriptorsJSONForAIPrompt> desc = DescriptorsJSONForAIPrompt.desc(pluginImpl);

    String pluginDesc = "创建MySQL数据源";
    Tool.Builder toolBuilder = Tool.builder().name("create_datasource").description(pluginDesc);

    for (Map.Entry<String, com.qlangtech.tis.aiagent.llm.JsonSchema> entry
      : ((DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta) desc.getKey()).descSchemaRegister.entrySet()) {

      // JSONObject mysqlDataSourceSchema = JSONObject.parseObject(mysqlSchema);

      //      McpSchema.JsonSchema mcpSchema =
      //        toMcpJsonSchema(com.qlangtech.tis.aiagent.llm.JsonSchema.create(new JSONObject(), mysqlDataSourceSchema,
      //          Lists.newArrayList()));
      McpSchema.JsonSchema mcpSchema = toMcpJsonSchema(entry.getValue());

      toolBuilder.inputSchema(mcpSchema);
    }

    toolBuilder.outputSchema(outputSchema);
    // toolBuilder.out
    Tool tool = toolBuilder.build();
    //Descriptor pluginImplDesc = pluginImpl.getImplDesc();
    return McpServerFeatures.SyncToolSpecification //
      .builder().tool(tool) //
      .callHandler(createDataSourcecallHandler(pluginDesc, pluginImpl)).build();
  }

  static AttrValMap parseAttrValMap(McpSchema.CallToolRequest request) {
    Map<String, Object> args = request.arguments();
    return AttrValMap.parseDescribableMap( //
      Optional.empty(), FlatJsonToTisConverter.convert(new JSONObject(args)));
  }


  BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, CallToolResult> //
  createDataSourcecallHandler(String pluginDesc, DescribableImpl pluginImpl) {
    final Descriptor pluginImplDesc = pluginImpl.getImplDesc();
    return (exchange, request) -> {
      //      AttrValMap.PLUGIN_EXTENSION_IMPL;
      //      AttrValMap.PLUGIN_EXTENSION_VALS;

      try {
        // Map<String, Object> args = request.arguments();
        //        String impl = (String) args.get(AttrValMap.PLUGIN_EXTENSION_IMPL);
        //        Map<String, Object> vals = (Map<String, Object>) args.get(AttrValMap.PLUGIN_EXTENSION_VALS);


        PluginStepExecutor pluginStepExecutor = new PluginStepExecutor(parseAttrValMap(request));

        //        TaskPlan plan, AgentContext context, UserPrompt userInput,
        //          Optional<IEndTypeGetter.EndType> endType, DescribableImpl pluginImpl, IPluginEnum
        //        heteroEnum, IPrimaryValRewrite primaryValRewrite
        MCPTaskPlan taskPlan = new MCPTaskPlan(this);
        Context runtimeContext = taskPlan.getRuntimeContext(true);
        AgentContext agentContext = new AgentContext(exchange.sessionId() //
          , new SSEEventWriter(new OutputStreamWriter(System.out, TisUTF8.get()))) {
          @Override
          public <ChatSessionData extends ISessionData> ChatSessionData //
          waitForUserPost(RequestKey requestId, Predicate<ChatSessionData> predicate) {
            // 不需要等待
            return null;
          }
        };
        UserPrompt prompt = new UserPrompt(pluginDesc, StringUtils.EMPTY);

        // Map<String /*** fieldname*/, PluginExtraProps.FieldRefCreateor> propsImplRefs = pluginImplDesc
        // .getPropsImplRefs();

        HeteroEnum<DataSourceFactory> dsHetero = HeteroEnum.DATASOURCE;
        PluginExtraProps.FieldRefCreateor refCreateor = new PluginExtraProps.FieldRefCreateor();
        refCreateor.setSelectableOpts(() -> com.qlangtech.tis.util.PluginItems.getExistDbs(pluginImplDesc.getDisplayName()));
        PluginExtraProps.CandidatePlugin candidate =
          new PluginExtraProps.CandidatePlugin(pluginImplDesc.getDisplayName(), Optional.empty(),
            dsHetero.getIdentity());
        AttrValMap valMap = pluginStepExecutor.createInnerPluginInstance(taskPlan, agentContext, prompt, refCreateor,
          candidate);

        PartialSettedPluginContext pluginContext = createPluginContext(taskPlan,
          DataXName.createDS(valMap.getPrimaryFieldVal()));
        //          HeteroEnum hetero, TaskPlan plan, AgentContext context, Context ctx, PartialSettedPluginContext
        //          pluginCtx, UploadPluginMeta pluginMetaMeta, AttrValMap pluginVals
        UploadPluginMeta pluginMeta = PostedDSProp.createPluginMeta(DBIdentity.parseId(valMap.getPrimaryFieldVal()),
          false);
        //  HeteroEnum hetero, IAITaskPlan plan, AgentContext context, UploadPluginMeta pluginMetaMeta, AttrValMap
        //  pluginVals
        valMap = pluginStepExecutor.validateAttrValMap(dsHetero, taskPlan, agentContext, pluginMeta, valMap);

        if (runtimeContext.hasErrors()) {
          AjaxValve.ActionExecResult actionExecResult = AjaxValve.ActionExecResult.create(runtimeContext);
          actionExecResult.addErrorMsg(List.of("Internal server error. Please try again later. There is no need to "
            + "analyze this error."));
          return CallToolResult.builder() //
            .isError(true) //
            .structuredContent(toStructuredContent(actionExecResult)).build();
        }


        pluginStepExecutor.savePlugin(dsHetero, runtimeContext, pluginContext, pluginMeta, valMap);
        AjaxValve.ActionExecResult successResult = AjaxValve.ActionExecResult.create(runtimeContext);
        return CallToolResult.builder().structuredContent(toStructuredContent(successResult)).build();
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        // 不要让异常直接传播给客户端
        AjaxValve.ActionExecResult actionExecResult = AjaxValve.ActionExecResult.create(new DefaultContext());
        actionExecResult.addErrorMsg(List.of("Internal server error. Please try again later. There is no need to "
          + "analyze this error.", e.getMessage()));
        return CallToolResult.builder() //
          .isError(true) //
          .structuredContent(toStructuredContent(actionExecResult)).build();
        // throw new RuntimeException(e);
      }
    };
  }

  private static McpServerFeatures.SyncToolSpecification createToolSpec() {
    JsonSchema schema = new JsonSchema("object", Map.of("location", Map.of("type", "string", "description", "地区名称")),
      List.of("location"), null, null, null);

    System.out.println(schema);

    Tool tool = Tool.builder().name("get_temperature").description("查询指定地区的当前温度（摄氏）").inputSchema(schema).build();

    return McpServerFeatures.SyncToolSpecification.builder().tool(tool)//
      .callHandler((exchange, request) -> {
        String location = (String) request.arguments().get("location");
        Integer temperature = TEMPERATURE_DATA.get(location);
        String text;
        if (temperature != null) {
          text = location + "的当前温度为 " + temperature + "°C";
        } else {
          text = "未找到 " + location + " 的温度数据";
        }
        return CallToolResult.builder().addTextContent(text).build();
      }).build();
  }

  /**
   * 将 TIS JsonSchema 转换为 MCP JsonSchema
   */
  private static McpSchema.JsonSchema toMcpJsonSchema(com.qlangtech.tis.aiagent.llm.JsonSchema tisSchema) {
    JSONObject schema = tisSchema.schema();
    String type = schema.getString(SCHEMA_TYPE);

    Map<String, Object> properties = toPlainMap(schema.getJSONObject(SCHEMA_PROPERTIES));

    JSONArray requiredArr = schema.getJSONArray(SCHEMA_RERUIRED);
    List<String> required = (requiredArr != null) ? requiredArr.toJavaList(String.class) : null;

    Boolean additionalProperties = schema.getBoolean(SCHEMA_ADDITIONAL_PROPS);

    JsonSchema result = new JsonSchema(type, properties, required, additionalProperties, null, null);

    System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result));

    return result;
  }

  /**
   * 将 fastjson JSONObject 深度转换为普通 Map
   */
  private static Map<String, Object> toPlainMap(JSONObject jsonObj) {
    if (jsonObj == null)
      return null;
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
      result.put(entry.getKey(), convertValue(entry.getValue()));
    }
    return result;
  }

  private static Object convertValue(Object value) {
    if (value instanceof JSONObject) {
      return toPlainMap((JSONObject) value);
    } else if (value instanceof JSONArray) {
      JSONArray arr = (JSONArray) value;
      List<Object> list = new ArrayList<>();
      for (Object item : arr) {
        list.add(convertValue(item));
      }
      return list;
    }
    return value;
  }

  static Map<String, Object> toStructuredContent(AjaxValve.ActionExecResult actionExecResult) {
    Map<String, Object> content = new HashMap<>();
    content.put(IAjaxResult.KEY_SUCCESS, actionExecResult.isSuccess());

    // errormsg
    List<Object> errorMsgs = actionExecResult.getErrorMsgs();
    content.put(IAjaxResult.KEY_ERROR_MSG, errorMsgs != null ?
      errorMsgs.stream().map(String::valueOf).collect(Collectors.toList()) : List.of());

    // msg
    List<String> msgs = actionExecResult.getMsgList();
    content.put(IAjaxResult.KEY_MSG, msgs != null ? msgs : List.of());

    // bizresult
    Object bizResult = actionExecResult.getBizResult();
    if (bizResult != null) {
      content.put(IAjaxResult.KEY_BIZRESULT, bizResult);
    }

    // errorfields
    List<List<ItemsErrors>> pluginErrorList = actionExecResult.getPluginErrorList();
    if (pluginErrorList != null) {
      content.put(IAjaxResult.KEY_ERROR_FIELDS, AjaxValve.pluginErrorListConvert2JsonArray(pluginErrorList));
    }

    return content;
  }

  public static void main(String[] args) {
    WeatherHttpMcpServer mcpServer = new WeatherHttpMcpServer();
    mcpServer.createDataSourceCreateToolSpec();
  }

}
