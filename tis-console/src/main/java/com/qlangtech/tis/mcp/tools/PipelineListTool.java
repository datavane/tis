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

import com.alibaba.datax.plugin.writer.hdfswriter.HdfsColMeta;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;

import java.util.List;
import java.util.Optional;

/**
 * 列表显示TIS中的所有端到端数据管道
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/30
 */
public class PipelineListTool extends McpTool {

  public static final String KEY_READER_TYPE = "readerType";
  public static final String KEY_WRITER_TYPE = "writerType";
  private static final String KEY_SUPPORT_BATCH_SYNCHRONIZE = "supportBatchSynchronize";
  private static final String KEY_SUPPORT_INCR_SYNCHRONIZE = "supportIncrSynchronize";

  public PipelineListTool(TISHttpMcpServer mcpServer) {
    super("list_pipeline", "列表显示TIS中的所有端到端数据管道", mcpServer);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    TISJsonSchema.Builder pipeSchemaBuilder = TISJsonSchema.Builder.create("pipeline_schema", Optional.empty());
    pipeSchemaBuilder.addProperty(HdfsColMeta.KEY_NAME, TISJsonSchema.FieldType.String, "端到端数据同步管道名称");
    pipeSchemaBuilder.addProperty(KEY_READER_TYPE, TISJsonSchema.FieldType.String, "Reader端插件类型");
    pipeSchemaBuilder.addProperty(KEY_WRITER_TYPE, TISJsonSchema.FieldType.String, "Writer端插件类型");
    pipeSchemaBuilder.addProperty(KEY_SUPPORT_BATCH_SYNCHRONIZE, TISJsonSchema.FieldType.Boolean, "是否支持批量端到端数据同步功能");
    pipeSchemaBuilder.addProperty(KEY_SUPPORT_INCR_SYNCHRONIZE, TISJsonSchema.FieldType.Boolean, "是否支持增量端到端数据同步功能");

    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.Array,
        "列表显示当前TIS的所有端到端数据同步管道")
      .setItems(pipeSchemaBuilder.build());
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    IApplicationDAO appDAO = BasicServlet.getBeanByType(IApplicationDAO.class);
    ApplicationCriteria criteria = new ApplicationCriteria();
    criteria.setOrderByClause("last_process_time desc,app_id desc");
    List<Application> apps = appDAO.selectByExample(criteria, 1, 1000);

    JSONArray pipelines = new JSONArray();
    for (Application app : apps) {
      String pipelineName = app.getProjectName();
      JSONObject pipeJson = new JSONObject(true);
      pipeJson.put(HdfsColMeta.KEY_NAME, pipelineName);

      try {
        DataxReader reader = DataxReader.load(null, pipelineName);
        DataxWriter writer = DataxWriter.load(null, pipelineName);


        if (reader != null && writer != null) {
          DataxReader.BaseDataxReaderDescriptor readerDescriptor =
            (DataxReader.BaseDataxReaderDescriptor) reader.getDescriptor();
          pipeJson.put(KEY_READER_TYPE, readerDescriptor.getEndType());

          DataxWriter.BaseDataxWriterDescriptor writerDescriptor =
            (DataxWriter.BaseDataxWriterDescriptor) writer.getDescriptor();
          pipeJson.put(KEY_WRITER_TYPE, writerDescriptor.getEndType());

          pipeJson.put(KEY_SUPPORT_BATCH_SYNCHRONIZE,
            readerDescriptor.isSupportBatch() && writerDescriptor.isSupportBatch());
          pipeJson.put(KEY_SUPPORT_INCR_SYNCHRONIZE,
            readerDescriptor.isSupportIncr() && writerDescriptor.isSupportIncr());

          pipelines.add(pipeJson);
        }
        //}
      } catch (Exception ignored) {
      }


    }

    JSONObject result = new JSONObject(true);
    result.put(IncrRateControllerCfgDTO.KEY_PIPELINE, pipelines);
    return ExecuteResult.createSuccess(result);
  }
}
