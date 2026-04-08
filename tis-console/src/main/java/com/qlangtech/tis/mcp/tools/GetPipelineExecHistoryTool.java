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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.qlangtech.tis.config.module.action.CollectionAction.KEY_QUERY_LIMIT;

/**
 * 获取指定管道最近N次批量同步的执行记录
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/7
 */
public class GetPipelineExecHistoryTool extends McpTool {
  private static final String KEY_STATUS = "status";
  private static final String KEY_START_TIME = "startTime";
  private static final String KEY_END_TIME = "endTime";
  private static final String KEY_HISTORY = "history";
  private static final String KEY_DURATION_SEC = "durationSec";

  public GetPipelineExecHistoryTool(TISHttpMcpServer mcpServer) {
    super("get_pipeline_exec_history",
      "获取指定管道最近N次批量同步的执行记录，包括每次执行的状态、耗时、起止时间", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.String, "管道名称");
    builder.addProperty(KEY_QUERY_LIMIT, TISJsonSchema.FieldType.Integer, "返回最近几次记录，默认10", false);
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.String, "管道名称");

    TISJsonSchema.Builder histBuilder = TISJsonSchema.Builder.create("history_item", Optional.empty());
    histBuilder.addProperty(JobParams.KEY_TASK_ID, TISJsonSchema.FieldType.Integer, "任务ID");
    histBuilder.addProperty(KEY_STATUS, TISJsonSchema.FieldType.String, "执行状态：RUNNING/SUCCESS/FAILED/CANCELLED");
    histBuilder.addProperty(KEY_START_TIME, TISJsonSchema.FieldType.String, "开始时间");
    histBuilder.addProperty(KEY_END_TIME, TISJsonSchema.FieldType.String, "结束时间");
    histBuilder.addProperty(KEY_DURATION_SEC, TISJsonSchema.FieldType.Integer, "耗时（秒）");

    builder.addProperty(KEY_HISTORY, TISJsonSchema.FieldType.Array, "执行历史记录列表")
      .setItems(histBuilder.build());
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    String pipelineName = arguments.get(IncrRateControllerCfgDTO.KEY_PIPELINE);
    if (StringUtils.isEmpty(pipelineName)) {
      //  return ExecuteResult.createFaild().setMessage("参数 pipelineName 不能为空");
      throw new IllegalStateException("illegal param pipelineName");
    }

    Integer limit = arguments.get(KEY_QUERY_LIMIT);
    if (limit == null || limit <= 0) {
      limit = 10;
    }

    IWorkflowDAOFacade wfFacade = BasicServlet.getBeanByType(IWorkflowDAOFacade.class);
    WorkFlowBuildHistoryCriteria query = new WorkFlowBuildHistoryCriteria();
    query.createCriteria().andAppNameEqualTo(pipelineName);
    query.setOrderByClause("id desc");

    List<WorkFlowBuildHistory> histories =
      wfFacade.getWorkFlowBuildHistoryDAO().selectByExample(query, 1, limit);

    JSONArray historyArr = new JSONArray();
    for (WorkFlowBuildHistory h : histories) {
      JSONObject item = new JSONObject(true);
      item.put(JobParams.KEY_TASK_ID, h.getId());
      item.put(KEY_STATUS, ExecResult.parse(h.getState()));
      item.put(KEY_START_TIME, TimeFormat.yyyyMMdd_HH_mm_ss.format(h.getStartTime()));
      item.put(KEY_END_TIME, TimeFormat.yyyyMMdd_HH_mm_ss.format(h.getEndTime()));

      if (h.getStartTime() != null && h.getEndTime() != null) {
        long durationSec = (h.getEndTime().getTime() - h.getStartTime().getTime()) / 1000;
        item.put(KEY_DURATION_SEC, durationSec);
      }
      historyArr.add(item);
    }

    JSONObject result = new JSONObject(true);
    result.put(IncrRateControllerCfgDTO.KEY_PIPELINE, pipelineName);
    result.put(KEY_HISTORY, historyArr);
    return ExecuteResult.createSuccess(result);
  }


}
