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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DescriptorsJSONForAIPrompt;
import com.qlangtech.tis.util.DescriptorsMeta;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.apache.commons.lang3.tuple.Pair;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WeatherHttpMcpServer {

  private static final Map<String, Integer> TEMPERATURE_DATA = Map.of("北京", 22, "上海", 26, "广州", 30, "深圳", 29, "成都", 20);

  public static HttpServletStreamableServerTransportProvider getMcpProvider() {
    HttpServletStreamableServerTransportProvider transportProvider =
      HttpServletStreamableServerTransportProvider.builder().mcpEndpoint("/mcp").build();

    McpSyncServer server =
      McpServer.sync(transportProvider).serverInfo("weather-server", "1.0.0").tools(createToolSpec(),
        createDataSourceCreateToolSpec()).build();

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

  private static McpServerFeatures.SyncToolSpecification createDataSourceCreateToolSpec() {

    DescribableImpl pluginImpl = new DescribableImpl(DataSourceFactory.class,
      Optional.of(IEndTypeGetter.EndType.MySQL));
    pluginImpl.addImpl("com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory");
    //DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta
    Pair<DescriptorsMeta, DescriptorsJSONForAIPrompt> desc = DescriptorsJSONForAIPrompt.desc(pluginImpl);

    Tool.Builder toolBuilder = Tool.builder().name("create_datasource").description("创建MySQL数据源");

    for (Map.Entry<String, com.qlangtech.tis.aiagent.llm.JsonSchema> entry :
      ((DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta) desc.getKey()).descSchemaRegister.entrySet()) {
      McpSchema.JsonSchema mcpSchema = toMcpJsonSchema(entry.getValue());

      toolBuilder.inputSchema(mcpSchema);
    }

    Tool tool = toolBuilder.build();

    return McpServerFeatures.SyncToolSpecification.builder().tool(tool).callHandler((exchange, request) -> {
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
    String type = schema.getString("type");

    Map<String, Object> properties = toPlainMap(schema.getJSONObject("properties"));

    JSONArray requiredArr = schema.getJSONArray("required");
    List<String> required = (requiredArr != null) ? requiredArr.toJavaList(String.class) : null;

    Boolean additionalProperties = schema.getBoolean("additionalProperties");

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

  public static void main(String[] args) {
    createDataSourceCreateToolSpec();
  }

}
