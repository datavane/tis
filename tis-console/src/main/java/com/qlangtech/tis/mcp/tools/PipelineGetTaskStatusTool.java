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
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.coredefine.module.action.TISK8sDelegate;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.incr.IncrStreamFactory;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 获取端到端数据同步管道的状态（批量+增量）
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/30
 */
public class PipelineGetTaskStatusTool extends McpTool {

  private static final String KEY_LAST_BATCH_STATUS = "lastBatchStatus";
  private static final String KEY_LAST_BATCH_TIME = "lastBatchTime";
  private static final String KEY_LAST_BATCH_TASK_ID = "lastBatchTaskId";
  private static final String KEY_INCR_CONFIGURED = "incrConfigured";
  private static final String KEY_INCR_RUNNING = "incrRunning";

  public PipelineGetTaskStatusTool(TISHttpMcpServer mcpServer) {
    super("get_pipeline_status", "获取端到端数据同步管道的状态，包括最近一次批量同步结果和增量同步运行状态", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {

    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.String, "管道名称");
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.String, "管道名称");
    builder.addProperty(KEY_LAST_BATCH_STATUS, TISJsonSchema.FieldType.String, "最近一次批量同步状态");
    builder.addProperty(KEY_LAST_BATCH_TIME, TISJsonSchema.FieldType.String, "最近一次批量同步时间");
    builder.addProperty(KEY_LAST_BATCH_TASK_ID, TISJsonSchema.FieldType.Integer, "最近一次批量同步任务ID");
    builder.addProperty(KEY_INCR_CONFIGURED, TISJsonSchema.FieldType.Boolean, "是否已配置增量同步");
    builder.addProperty(KEY_INCR_RUNNING, TISJsonSchema.FieldType.Boolean, "增量同步是否运行中");
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    String pipelineName = arguments.get(IncrRateControllerCfgDTO.KEY_PIPELINE);
    if (StringUtils.isEmpty(pipelineName)) {
      throw new IllegalStateException("param pipelineName can not be empty");
    }

    JSONObject result = new JSONObject(true);
    result.put(IncrRateControllerCfgDTO.KEY_PIPELINE, pipelineName);

    // Batch status: get latest build history
    IWorkflowDAOFacade wfFacade = BasicServlet.getBeanByType(IWorkflowDAOFacade.class);
    WorkFlowBuildHistoryCriteria query = new WorkFlowBuildHistoryCriteria();
    query.createCriteria().andAppNameEqualTo(pipelineName);
    query.setOrderByClause("id desc");
    List<WorkFlowBuildHistory> histories =
      wfFacade.getWorkFlowBuildHistoryDAO().selectByExample(query, 1, 1);

    if (!histories.isEmpty()) {
      WorkFlowBuildHistory latest = histories.get(0);
      result.put(KEY_LAST_BATCH_STATUS, ExecResult.parse(latest.getState()));
      result.put(KEY_LAST_BATCH_TIME, latest.getEndTime() != null
        ? TimeFormat.yyyyMMdd_HH_mm_ss.format(latest.getEndTime()) : (latest.getStartTime() != null ?
        TimeFormat.yyyyMMdd_HH_mm_ss.format(latest.getStartTime()) : null));
      result.put(KEY_LAST_BATCH_TASK_ID, latest.getId());
    } else {
      result.put(KEY_LAST_BATCH_STATUS, "NO_HISTORY");
    }

    // Incr status
    //try {
    // IPluginStore<IncrStreamFactory> store = TIS.getPluginStore(pipelineName, IncrStreamFactory.class);
    boolean configured = IncrStreamFactory.getFactory(pipelineName, false) != null; // store != null && store
    // .getPlugin() != null;
    result.put(KEY_INCR_CONFIGURED, configured);
    result.put(KEY_INCR_RUNNING, false);
    if (configured) {
      // try {
      TISK8sDelegate k8s = TISK8sDelegate.getK8SDelegate(DataXName.createDataXPipeline(pipelineName));
      if (k8s.hasCreated()) {
        Object rcConfig = k8s.getRcConfig(true);
        result.put(KEY_INCR_RUNNING, rcConfig != null);
      }
      //      } catch (Exception e) {
      //        result.put("incrRunning", false);
      //      }
    }
    //    } catch (Exception e) {
    //      result.put("incrConfigured", false);
    //      result.put("incrRunning", false);
    //    }

    return ExecuteResult.createSuccess(result);
  }

}
