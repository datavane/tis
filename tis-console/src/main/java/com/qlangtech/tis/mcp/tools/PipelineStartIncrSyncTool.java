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
package com.qlangtech.tis.mcp.tools;

import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;

/**
 * 启动或停止端到端管道增量实时同步
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/30
 */
public class PipelineStartIncrSyncTool extends McpTool {

  private static final String KEY_ACTION = "action";
  private static final String KEY_START = "start";
  private static final String KEY_STOP = "stop";

  public PipelineStartIncrSyncTool(TISHttpMcpServer mcpServer) {
    super("toggle_incr_sync", "启动或停止指定管道的增量（实时）同步", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.String, "管道名称");
    builder.addProperty(KEY_ACTION, TISJsonSchema.FieldType.String, "操作类型：" + KEY_START + "=启动, " + KEY_STOP + "=停止")
      .setValEnums(KEY_START, KEY_STOP);
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream() {
    return tisBizOutputSchema;
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {


    String pipelineName = arguments.get(IncrRateControllerCfgDTO.KEY_PIPELINE);
    String action = arguments.get(KEY_ACTION);

    if (StringUtils.isEmpty(pipelineName)) {
      throw new IllegalArgumentException("param pipelineName can not be empty");
    }
    if (StringUtils.isEmpty(action) || (!KEY_START.equals(action) && !KEY_STOP.equals(action))) {
      // return ExecuteResult.createFaild().setMessage("参数 action 必须为 start 或 stop");
      throw new IllegalArgumentException("param action must be  start 或 stop");
    }

    // JSONObject result = new JSONObject(true);

    //try {
    com.qlangtech.tis.coredefine.module.action.TISK8sDelegate k8s =
      com.qlangtech.tis.coredefine.module.action.TISK8sDelegate.getK8SDelegate(DataXName.createDataXPipeline(pipelineName));


    switch (action) {
      case KEY_START -> {
        // Stop incremental sync
        k8s.stopIncrProcess(agentContext.getTaskPlan().getControlMsgHandler(),
          agentContext.getRuntimeContext());
        agentContext.addActionMessage("已停止管道 " + pipelineName + " 的增量同步");
        break;
      }
      case KEY_STOP -> {
        agentContext.addActionMessage("增量同步启动请求已提交，管道: " + pipelineName
          + "。请通过 get_incr_sync_status 查询启动结果。");
        break;
      }
      default -> {
        throw new IllegalStateException("illega action:" + action);
      }
    }

    return ExecuteResult.createSuccess(agentContext.getActionExecResult());
  }
}
