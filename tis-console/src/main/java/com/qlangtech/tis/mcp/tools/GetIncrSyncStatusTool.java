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

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.coredefine.module.action.TISK8sDelegate;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.incr.IncrStreamFactory;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;

/**
 * 获取指定管道增量（实时）同步的详细运行状态
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/7
 */
public class GetIncrSyncStatusTool extends McpTool {

  public GetIncrSyncStatusTool(TISHttpMcpServer mcpServer) {
    super("get_incr_sync_status",
      "获取指定管道增量（实时）同步的详细运行状态，包括Flink作业状态、消费延迟、TPS、checkpoint信息", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.String, "管道名称");
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.String, "管道名称");
    builder.addProperty("configured", TISJsonSchema.FieldType.Boolean, "是否已配置增量同步");
    builder.addProperty("running", TISJsonSchema.FieldType.Boolean, "增量同步是否运行中");
    builder.addProperty("state", TISJsonSchema.FieldType.String, "Flink作业状态");
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    String pipelineName = arguments.get(IncrRateControllerCfgDTO.KEY_PIPELINE);
    if (StringUtils.isEmpty(pipelineName)) {
      return ExecuteResult.createFaild().setMessage("参数 pipelineName 不能为空");
    }

    JSONObject result = new JSONObject(true);
    result.put(IncrRateControllerCfgDTO.KEY_PIPELINE, pipelineName);

    // Check if IncrStreamFactory is configured
    IPluginStore<IncrStreamFactory> store = TIS.getPluginStore(pipelineName, IncrStreamFactory.class);
    if (store == null || store.getPlugin() == null) {
      result.put("configured", false);
      result.put("running", false);
      result.put("state", "NOT_CONFIGURED");
      return ExecuteResult.createSuccess(result);
    }

    result.put("configured", true);

    // Try to get K8s deployment status
    try {
      TISK8sDelegate k8s = TISK8sDelegate.getK8SDelegate(DataXName.createDataXPipeline(pipelineName));
      if (!k8s.hasCreated()) {
        result.put("running", false);
        result.put("state", "NOT_DEPLOYED");
        return ExecuteResult.createSuccess(result);
      }

      k8s.checkUseable(false);
      Object rcConfig = k8s.getRcConfig(true);
      if (rcConfig != null) {
        result.put("running", true);
        result.put("state", "RUNNING");
      } else {
        result.put("running", false);
        result.put("state", "STOPPED");
      }
    } catch (Exception e) {
      result.put("running", false);
      result.put("state", "ERROR");
      result.put("errorMsg", e.getMessage());
    }

    return ExecuteResult.createSuccess(result);
  }
}
