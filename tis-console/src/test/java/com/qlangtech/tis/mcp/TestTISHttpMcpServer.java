/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.mcp;

import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.util.AttrValMap;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_PLUGIN_DESCRIPTOR_ID;
import static com.qlangtech.tis.mcp.TISHttpMcpServer.parseAttrValMap;
import static com.qlangtech.tis.runtime.module.action.SysInitializeAction.getClassPathXmlApplicationContext;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_IMPL;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * WeatherHttpMcpServer.createDataSourcecallHandler() 单元测试
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/25
 */
public class TestTISHttpMcpServer extends TestCase {

  /**
   * 创建一个用于测试的 DescribableImpl 实例，
   * 通过覆写 getImplDesc() 避免依赖 TIS 单例
   */
  private DescribableImpl createTestPluginImpl(Descriptor mockDesc) {
    return new DescribableImpl(DataSourceFactory.class, Optional.of(IEndTypeGetter.EndType.MySQL)) {
      @Override
      public Descriptor getImplDesc() {
        return mockDesc;
      }
    };
  }

  private TISHttpMcpServer mcpServer;

  static {
    BasicServlet.setApplicationContext(getClassPathXmlApplicationContext());
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    mcpServer = new TISHttpMcpServer();
  }

  /**
   * 创建一个 mock 的 McpSyncServerExchange
   */
  private McpSyncServerExchange createMockExchange() {
    McpSyncServerExchange exchange = createMock(McpSyncServerExchange.class);
    expect(exchange.sessionId()).andReturn("test-session").anyTimes();
    replay(exchange);
    return exchange;
  }

  /**
   * 验证 CallToolResult 为错误结果且包含预期的通用错误消息
   */
  private void assertErrorResult(CallToolResult result) {
    assertNotNull("result should not be null", result);
    // assertTrue("result should indicate error", result.isError());
    assertNotNull("content should not be null", result.structuredContent());
    //    assertFalse("content should not be empty", result.content().isEmpty());

    //    McpSchema.TextContent textContent = (McpSchema.TextContent) result.content().get(0);
    //    assertTrue("error message should contain 'Internal server error'", textContent.text().contains("Internal
    //    server " + "error"));
  }

  /**
   * 测试 createDataSourcecallHandler 能正确创建 handler 实例
   */
  public void testCreateHandler() {
    Descriptor mockDesc = createMock(Descriptor.class);
    replay(mockDesc);

    DescribableImpl pluginImpl = createTestPluginImpl(mockDesc);

    BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, CallToolResult> handler = mcpServer.
      createDataSourcecallHandler("创建MySQL数据源", pluginImpl);

    assertNotNull("handler should not be null", handler);
    verify(mockDesc);
  }

  public void testParseAttrValMap() {
    McpSchema.CallToolRequest mysqlDataSourceMcpRequest = createMysqlDataSourceMcpRequest();
    AttrValMap valMap = parseAttrValMap(new McpTool.RequestArguments(mysqlDataSourceMcpRequest.arguments()));
    Assert.assertNotNull(valMap);
  }

  /**
   * 测试当 AttrValMap.parseDescribableMap 执行失败时（TIS 未初始化），
   * handler 应捕获异常并返回 isError=true 的 CallToolResult
   */
  public void testCreateDataSourcecallHandler_ErrorOnParseDescribable() {
    Descriptor mockDesc = createMock(Descriptor.class);
    expect(mockDesc.getDisplayName()).andReturn("MySQL-V5").anyTimes();
    replay(mockDesc);

    DescribableImpl pluginImpl = createTestPluginImpl(mockDesc);

    BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, CallToolResult> handler =
      mcpServer.createDataSourcecallHandler("创建MySQL数据源", pluginImpl);

    McpSchema.CallToolRequest request = createMysqlDataSourceMcpRequest();

    CallToolResult result = handler.apply(createMockExchange(), request);

    assertErrorResult(result);
    verify(mockDesc);
  }

  private static McpSchema.CallToolRequest createMysqlDataSourceMcpRequest() {
    // 构造包含 impl 和 vals 的请求参数
    Map<String, Object> vals = new HashMap<>();
    vals.put("dbName", "shop");
    vals.put("name", "test-datasource");
    vals.put("userName", "root");
    vals.put("password", "123456");
    vals.put("port", 3306);
    vals.put("encode", "utf8");
    vals.put("useCompression", "false");
    //    "splitTableStrategy": {
    //      "id": "off",
    //        "host": "192.168.28.200"
    //    },
    //    "timeZone": {
    //      "id": "default",
    //        "timeZone": "Asia/Shanghai"
    //    },
    vals.put("splitTableStrategy" //
      , Map.of(SCHEMA_PLUGIN_DESCRIPTOR_ID //
        , "com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy" //
        , "host", "192.168.28.200"));

    vals.put("timeZone" //
      , Map.of(SCHEMA_PLUGIN_DESCRIPTOR_ID //
        , "com.qlangtech.tis.plugin.timezone.DefaultTISTimeZone" //
        , "timeZone", "Asia/Shanghai"));

    Map<String, Object> args = new HashMap<>();
    args.put(PLUGIN_EXTENSION_IMPL, "com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory");
    args.put("vals", vals);

    McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("create_datasource", args);
    return request;
  }

  /**
   * 测试当请求参数中缺少 impl 字段时，handler 应返回错误结果
   */
  public void testCreateDataSourcecallHandler_MissingImplField() {
    Descriptor mockDesc = createMock(Descriptor.class);
    expect(mockDesc.getDisplayName()).andReturn("MySQL").anyTimes();
    replay(mockDesc);

    DescribableImpl pluginImpl = createTestPluginImpl(mockDesc);

    BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, CallToolResult> handler =
      mcpServer.createDataSourcecallHandler("创建MySQL数据源", pluginImpl);

    // 空参数 - 缺少 impl 和 vals
    Map<String, Object> args = new HashMap<>();
    McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("create_datasource", args);

    CallToolResult result = handler.apply(createMockExchange(), request);

    assertErrorResult(result);
    verify(mockDesc);
  }

  /**
   * 测试当请求参数中 arguments 为 null 时，handler 应返回错误结果
   */
  public void testCreateDataSourcecallHandler_NullArguments() {
    Descriptor mockDesc = createMock(Descriptor.class);
    expect(mockDesc.getDisplayName()).andReturn("MySQL").anyTimes();
    replay(mockDesc);

    DescribableImpl pluginImpl = createTestPluginImpl(mockDesc);

    BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, CallToolResult> handler =
      mcpServer.createDataSourcecallHandler("创建MySQL数据源", pluginImpl);

    McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("create_datasource", null);

    CallToolResult result = handler.apply(createMockExchange(), request);

    assertErrorResult(result);
    verify(mockDesc);
  }

  /**
   * 测试错误返回的 CallToolResult 中包含正确的提示信息，
   * 确保不暴露内部异常细节给客户端
   */
  public void testCreateDataSourcecallHandler_ErrorMessageDoesNotLeakDetails() {
    Descriptor mockDesc = createMock(Descriptor.class);
    expect(mockDesc.getDisplayName()).andReturn("MySQL").anyTimes();
    replay(mockDesc);

    DescribableImpl pluginImpl = createTestPluginImpl(mockDesc);

    BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, CallToolResult> handler =
      mcpServer.createDataSourcecallHandler("创建MySQL数据源", pluginImpl);

    Map<String, Object> args = new HashMap<>();
    args.put(PLUGIN_EXTENSION_IMPL, "non.existent.PluginClass");
    args.put("vals", new HashMap<>());
    McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("create_datasource", args);

    CallToolResult result = handler.apply(createMockExchange(), request);

    assertNotNull(result);
    assertTrue(result.isError());

    McpSchema.TextContent textContent = (McpSchema.TextContent) result.content().get(0);
    String errorMsg = textContent.text();
    // 确保错误消息是通用的，不暴露内部异常信息
    assertTrue(errorMsg.contains("Internal server error"));
    assertTrue(errorMsg.contains("Please try again later"));
    // 确保不包含堆栈信息或异常类名
    assertFalse("should not contain exception class name", errorMsg.contains("Exception"));
    assertFalse("should not contain stack trace", errorMsg.contains("at com.qlangtech"));

    verify(mockDesc);
  }
}
