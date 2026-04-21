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

package com.qlangtech.tis.mcp.tools.pipeline;

import com.qlangtech.tis.coredefine.module.action.ChatPipelineAction;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.mcp.MCPTaskPlan;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import junit.framework.TestCase;
import org.apache.struts2.ActionContext;

import java.util.HashMap;
import java.util.Map;

import static com.qlangtech.tis.mcp.tools.pipeline.CreatePipelinePrepareTool.KEY_SOURCE_END;
import static com.qlangtech.tis.mcp.tools.pipeline.CreatePipelinePrepareTool.KEY_TARGET_END;
import static com.qlangtech.tis.runtime.module.action.SysInitializeAction.getClassPathXmlApplicationContext;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_IMPL;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * CreatePipelinePrepareTool.execHandle() 单元测试
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/4
 */
public class TestCreatePipelinePrepareTool extends TestCase {

  static {
    ActionContext.bind(ActionContext.of());
    BasicServlet.setApplicationContext(getClassPathXmlApplicationContext());
  }

  private TISHttpMcpServer mcpServer;
  private CreatePipelinePrepareTool tool;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    mcpServer = new TISHttpMcpServer();
    tool = new CreatePipelinePrepareTool(mcpServer);
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
   * 测试 execHandle 正常创建管道流程：
   * 1. 构造包含 source_end, target_end 和 pipeline 参数的请求
   * 2. 调用 execHandle 执行创建
   * 3. 验证返回结果成功
   * 4. 验证 pipeline process 实例已缓存到 MCP session 中
   */
  public void testExecHandle() throws Exception {
    McpSyncServerExchange exchange = createMockExchange();

    // 构造 pipeline 参数（DefaultDataxProcessor 所需字段）
    Map<String, Object> pipelineVals = new HashMap<>();
    pipelineVals.put("recept", "test");
    pipelineVals.put("name", "test_pipeline");
    pipelineVals.put("dptId", 123);
    pipelineVals.put("globalCfg", "test");

    Map<String, Object> pipelineArgs = new HashMap<>();
    pipelineArgs.put(PLUGIN_EXTENSION_IMPL, CreatePipelinePrepareTool.KEY_DEFAULT_DATAX_PROCESS_IMPL);
    pipelineArgs.put("vals", pipelineVals);

    // 构造完整请求参数
    Map<String, Object> args = new HashMap<>();
    args.put(KEY_SOURCE_END, IEndTypeGetter.EndType.MySQL.getVal());
    args.put(KEY_TARGET_END, IEndTypeGetter.EndType.Doris.getVal());
    args.put(IncrRateControllerCfgDTO.KEY_PIPELINE, pipelineArgs);

    McpTool.RequestArguments arguments = new McpTool.RequestArguments(args);

    McpAgentContext agentContext = createAgentContext(exchange);
    ExecuteResult result = tool.execHandle(agentContext, exchange, arguments);

    // 验证执行成功
    assertTrue("execHandle should return success", result.isSuccess());

    // 验证返回的 ActionExecResult 消息
    Object message = result.getMessage();
    assertNotNull("result message should not be null", message);
    assertTrue("message should be ActionExecResult", message instanceof AjaxValve.ActionExecResult);
    AjaxValve.ActionExecResult actionResult = (AjaxValve.ActionExecResult) message;
    assertTrue("action result should be success", actionResult.isSuccess());

    // 验证 pipeline process 实例已存入 MCP session
    ChatPipelineAction.ChatSession session = mcpServer.getSession(exchange);
    Describable cachedProcess = session.get(CreatePipelinePrepareTool.KEY_SESSION_PIPELINE_PROCESS);
    assertNotNull("pipeline process should be cached in MCP session", cachedProcess);

    verify(exchange);
  }

  private McpAgentContext createAgentContext(McpSyncServerExchange exchange) {

    MCPTaskPlan taskPlan = new MCPTaskPlan(this.mcpServer);
    // Context runtimeContext = taskPlan.getRuntimeContext(true);
    ChatPipelineAction.ChatSession session = mcpServer.getSession(exchange);
    return new McpAgentContext(session, taskPlan);

    //  return agentContext;
  }
}
