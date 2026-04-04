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
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.IAjaxResult;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.mcp.tools.pipeline.CreatePipelinePrepareTool;
import com.qlangtech.tis.mcp.tools.CreatePluginInstanceTool;
import com.qlangtech.tis.mcp.tools.GetPluginSchemaTool;
import com.qlangtech.tis.mcp.tools.ListPluginTypesTool;
import com.qlangtech.tis.mcp.tools.RequireUserAddressTool;
import com.qlangtech.tis.mcp.tools.RequireUserAddressElicitationTool;
import com.qlangtech.tis.offline.module.manager.impl.OfflineManager;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.ItemsErrors;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.qlangtech.tis.aiagent.execute.impl.BasicStepExecutor.createPluginContext;

/**
 *
 * <a href="design/jsonschema-flatten-design.md" >design/jsonschema-flatten-design.md</>
 */
public class TISHttpMcpServer {
  private static final Logger logger = LoggerFactory.getLogger(TISHttpMcpServer.class);

  /**
   * MCP session cache: keyed by sessionId, stores per-session attributes (e.g., pipeline process instance)
   * so that subsequent tool calls within the same session can retrieve previously cached data.
   */
  private final Map<String, McpSession> mcpSessionCache = new ConcurrentHashMap<>();

  /**
   * Get or create the session attribute map for the given sessionId.
   *
   * @param exchange the MCP session ID from {@code exchange.sessionId()}
   * @return a thread-safe map for storing session-scoped data
   */
  public McpSession getSession(McpSyncServerExchange exchange) {
    String sessionId = exchange.sessionId();
    return mcpSessionCache.computeIfAbsent(sessionId, k -> new McpSession());
  }

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


  public static HttpServletStreamableServerTransportProvider getMcpProvider() {
    HttpServletStreamableServerTransportProvider transportProvider =
      HttpServletStreamableServerTransportProvider.builder().mcpEndpoint("/mcp").build();
    TISHttpMcpServer mcpServer = new TISHttpMcpServer();

    McpTool[] tools = new McpTool[]{//new DataSourceCreateTool(mcpServer),
      new ListPluginTypesTool(mcpServer),
      new CreatePluginInstanceTool(mcpServer) //
      , new GetPluginSchemaTool(mcpServer) //
      , new CreatePipelinePrepareTool(mcpServer)
      , new RequireUserAddressTool(mcpServer)
      , new RequireUserAddressElicitationTool(mcpServer)};

    McpSyncServer server = McpServer.sync(transportProvider).serverInfo("tis-mcp-server", "1.0.0") //
      .prompts().tools(Arrays.stream(tools).map(McpTool::createToolSpec).toList()).build();

    return transportProvider;
  }

  public static AttrValMap parseAttrValMap(McpTool.RequestArguments request) {
    Map<String, Object> args = request.getArgs();
    return AttrValMap.parseDescribableMap( //
      Optional.empty(), FlatJsonToTisConverter.convert(new JSONObject(args)));
  }


  BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> //
  createDataSourcecallHandler(String pluginDesc, DescribableImpl pluginImpl) {
    final Descriptor pluginImplDesc = pluginImpl.getImplDesc();
    return (exchange, request) -> {
      try {

        PluginStepExecutor pluginStepExecutor =
          new PluginStepExecutor(parseAttrValMap(new McpTool.RequestArguments(request.arguments())));

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
        refCreateor.setSelectableOpts(() -> com.qlangtech.tis.util.PluginItems.getExistDbs(pluginImplDesc
          .getDisplayName()));
        PluginExtraProps.CandidatePlugin candidate =
          new PluginExtraProps.CandidatePlugin(pluginImplDesc.getDisplayName(), Optional.empty(),
            dsHetero.getIdentity());
        AttrValMap valMap = pluginStepExecutor.createInnerPluginInstance(taskPlan, agentContext, prompt,
          refCreateor,
          candidate);
        DataXName dataXName = DataXName.createDS(valMap.getPrimaryFieldVal());
        PartialSettedPluginContext pluginContext = createPluginContext(taskPlan, dataXName);
        //          HeteroEnum hetero, TaskPlan plan, AgentContext context, Context ctx, PartialSettedPluginContext
        //          pluginCtx, UploadPluginMeta pluginMetaMeta, AttrValMap pluginVals
        UploadPluginMeta pluginMeta = PostedDSProp.createPluginMeta(DBIdentity.parseId(valMap.getPrimaryFieldVal()),
          false);
        //  HeteroEnum hetero, IAITaskPlan plan, AgentContext context, UploadPluginMeta pluginMetaMeta, AttrValMap
        //  pluginVals
        valMap = pluginStepExecutor.validateAttrValMap(dsHetero, taskPlan, agentContext, Optional.of(dataXName),
          valMap);

        if (runtimeContext.hasErrors()) {
          AjaxValve.ActionExecResult actionExecResult = AjaxValve.ActionExecResult.create(runtimeContext);
          actionExecResult.addErrorMsg(List.of("Internal server error. Please try again later. There is no need to "
            + "analyze this error."));
          return McpSchema.CallToolResult.builder() //
            .isError(true) //
            .structuredContent(toStructuredContent(actionExecResult)).build();
        }


        pluginStepExecutor.savePlugin(dsHetero, runtimeContext, pluginContext, pluginMeta, valMap);
        AjaxValve.ActionExecResult successResult = AjaxValve.ActionExecResult.create(runtimeContext);
        return McpSchema.CallToolResult.builder().structuredContent(toStructuredContent(successResult)).build();
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        // 不要让异常直接传播给客户端
        AjaxValve.ActionExecResult actionExecResult = AjaxValve.ActionExecResult.create(new DefaultContext());
        actionExecResult.addErrorMsg(List.of("Internal server error. Please try again later. There is no need to "
          + "analyze this error.", e.getMessage()));
        return McpSchema.CallToolResult.builder() //
          .isError(true) //
          .structuredContent(toStructuredContent(actionExecResult)).build();
        // throw new RuntimeException(e);
      }
    };
  }

  //  private static McpServerFeatures.SyncToolSpecification createToolSpec() {
  //    JsonSchema schema = new JsonSchema("object", Map.of("location", Map.of("type", "string", "description",
  //    "地区名称")),
  //      List.of("location"), null, null, null);
  //
  //    System.out.println(schema);
  //
  //    Tool tool = Tool.builder().name("get_temperature").description("查询指定地区的当前温度（摄氏）").inputSchema(schema).build();
  //
  //    return McpServerFeatures.SyncToolSpecification.builder().tool(tool)//
  //      .callHandler((exchange, request) -> {
  //        String location = (String) request.arguments().get("location");
  //        Integer temperature = TEMPERATURE_DATA.get(location);
  //        String text;
  //        if (temperature != null) {
  //          text = location + "的当前温度为 " + temperature + "°C";
  //        } else {
  //          text = "未找到 " + location + " 的温度数据";
  //        }
  //        return CallToolResult.builder().addTextContent(text).build();
  //      }).build();
  //  }

  /**
   * 将 fastjson JSONObject 深度转换为普通 Map
   */
  public static Map<String, Object> toPlainMap(JSONObject jsonObj) {
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
    TISHttpMcpServer mcpServer = new TISHttpMcpServer();
    // mcpServer.createDataSourceCreateToolSpec();
  }

}
