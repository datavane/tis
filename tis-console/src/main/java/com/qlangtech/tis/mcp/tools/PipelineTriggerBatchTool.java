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
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.coredefine.module.action.TriggerBuildResult;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.manage.common.IAjaxResult;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 触发端到端批量数据同步
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/30
 */
public class PipelineTriggerBatchTool extends McpTool {

  public PipelineTriggerBatchTool(TISHttpMcpServer mcpServer) {
    super("trigger_pipeline_batch_synchronize", "触发指定管道执行一次批量全量数据同步，返回taskId用于后续状态查询。注意：执行此功能需要确保数据管道支持批**量数据同步**", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.String, "管道唯一性名称");
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    builder.addProperty(IAjaxResult.KEY_SUCCESS, TISJsonSchema.FieldType.Boolean, "是否触发成功");
    builder.addProperty(JobParams.KEY_TASK_ID, TISJsonSchema.FieldType.Integer, "触发的任务ID，可用于后续查询状态和日志");
    builder.addProperty(IAjaxResult.KEY_MSG, TISJsonSchema.FieldType.String, "结果描述");
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    String pipelineName = arguments.get(IncrRateControllerCfgDTO.KEY_PIPELINE);
    if (StringUtils.isEmpty(pipelineName)) {
      throw new IllegalStateException("param pipelineName can not be null");
    }

    List<com.qlangtech.tis.manage.common.HttpUtils.PostParam> params = new java.util.ArrayList<>();
    params.add(new com.qlangtech.tis.manage.common.HttpUtils.PostParam(
      TriggerBuildResult.KEY_APPNAME, pipelineName));

    TriggerBuildResult triggerResult =
      TriggerBuildResult.triggerBuild(agentContext.getTaskPlan().getControlMsgHandler(),
        agentContext.getRuntimeContext(), params);

    JSONObject result = new JSONObject(true);
    result.put(IAjaxResult.KEY_SUCCESS, triggerResult.success);
    if (triggerResult.success) {
      result.put(JobParams.KEY_TASK_ID, triggerResult.getTaskid());
      result.put(IAjaxResult.KEY_MSG, "已成功触发管道 " + pipelineName + " 的批量全量同步");
    } else {
      result.put(IAjaxResult.KEY_MSG, "触发失败 ");
    }
    return ExecuteResult.createSuccess(result);
  }
}
