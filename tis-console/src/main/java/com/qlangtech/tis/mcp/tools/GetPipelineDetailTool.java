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
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 获取指定数据管道的详细配置
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/7
 */
public class GetPipelineDetailTool extends McpTool {

  public static final String KEY_TABLES = "tables";
  private static final String KEY_READER_IMPL = "readerImpl";
  private static final String KEY_WRITER_IMPL = "writerImpl";

  public GetPipelineDetailTool(TISHttpMcpServer mcpServer) {
    super("get_pipeline_detail", "获取指定数据管道的详细配置，包括源端/目标端插件配置、同步表列表", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    builder.addProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, TISJsonSchema.FieldType.String, "管道名称");
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    builder.addProperty(HdfsColMeta.KEY_NAME, TISJsonSchema.FieldType.String, "管道名称");
    builder.addProperty(PipelineListTool.KEY_READER_TYPE, TISJsonSchema.FieldType.String, "Reader端插件类型");
    builder.addProperty(KEY_READER_IMPL, TISJsonSchema.FieldType.String, "Reader端插件实现类");
    builder.addProperty(PipelineListTool.KEY_WRITER_TYPE, TISJsonSchema.FieldType.String, "Writer端插件类型");
    builder.addProperty(KEY_WRITER_IMPL, TISJsonSchema.FieldType.String, "Writer端插件实现类");
    builder.addProperty(KEY_TABLES, TISJsonSchema.FieldType.Array, "同步的表名列表")
      .setItems(TISJsonSchema.FieldType.String);
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    String pipelineName = arguments.get(IncrRateControllerCfgDTO.KEY_PIPELINE);
    if (StringUtils.isEmpty(pipelineName)) {
      throw new IllegalStateException("param pipelineName can not be null");
    }

    JSONObject result = new JSONObject(true);
    result.put(HdfsColMeta.KEY_NAME, pipelineName);

    // Reader info
    try {
      DataxReader reader = DataxReader.load(null, pipelineName);//processor.getReader(null);
      if (reader != null) {
        result.put(PipelineListTool.KEY_READER_TYPE,
          ((DataxReader.BaseDataxReaderDescriptor) reader.getDescriptor()).getEndType());
        result.put(KEY_READER_IMPL, reader.getClass().getName());

        JSONArray tablesArr = new JSONArray();
        try {
          List<ISelectedTab> tabs = reader.getSelectedTabs();
          if (tabs != null) {
            for (ISelectedTab tab : tabs) {
              tablesArr.add(tab.getName());
            }
          }
        } catch (Exception ignored) {
        }
        result.put(KEY_TABLES, tablesArr);
      }
    } catch (Exception ignored) {
    }

    // Writer info
    try {
      DataxWriter writer = DataxWriter.load(null, pipelineName); //processor.getWriter(null, false);
      if (writer != null) {
        result.put(PipelineListTool.KEY_WRITER_TYPE,
          ((DataxWriter.BaseDataxWriterDescriptor) writer.getDescriptor()).getEndType());
        result.put(KEY_WRITER_IMPL, writer.getClass().getName());
      }
    } catch (Exception ignored) {
    }

    return ExecuteResult.createSuccess(result);
  }
}
