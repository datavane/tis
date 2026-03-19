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
import java.util.List;
import java.util.Map;

//import org.apache.catalina.Context;
//import org.apache.catalina.Wrapper;
//import org.apache.catalina.startup.Tomcat;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class WeatherHttpMcpServer {

	private static final Map<String, Integer> TEMPERATURE_DATA = Map.of("北京", 22, "上海", 26, "广州", 30, "深圳", 29,
			"成都", 20);

	public static HttpServletStreamableServerTransportProvider getMcpProvider()  {
		HttpServletStreamableServerTransportProvider transportProvider = HttpServletStreamableServerTransportProvider
			.builder()
			.mcpEndpoint("/mcp")
			.build();

		McpSyncServer server = McpServer.sync(transportProvider)
			.serverInfo("weather-server", "1.0.0")
			.tools(createToolSpec())
			.build();

	//	Tomcat tomcat = createEmbeddedTomcat(transportProvider);

		try {
//			tomcat.start();
//			System.out.println("Weather MCP Server listening on http://localhost:8080/mcp");
//			tomcat.getServer().await();
		}
		finally {
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

	private static McpServerFeatures.SyncToolSpecification createToolSpec() {
		Tool tool = Tool.builder()
			.name("get_temperature")
			.description("查询指定地区的当前温度（摄氏）")
			.inputSchema(new JsonSchema("object",
					Map.of("location", Map.of("type", "string", "description", "地区名称")),
					List.of("location"), null, null, null))
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler((exchange, request) -> {
				String location = (String) request.arguments().get("location");
				Integer temperature = TEMPERATURE_DATA.get(location);
				String text;
				if (temperature != null) {
					text = location + "的当前温度为 " + temperature + "°C";
				}
				else {
					text = "未找到 " + location + " 的温度数据";
				}
				return CallToolResult.builder().addTextContent(text).build();
			})
			.build();
	}

}
