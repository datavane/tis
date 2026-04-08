///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.qlangtech.tis.mcp.tools;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
//import com.qlangtech.tis.datax.IDataxProcessor;
//import com.qlangtech.tis.datax.IDataxReader;
//import com.qlangtech.tis.datax.IDataxWriter;
//import com.qlangtech.tis.datax.impl.DataxProcessor;
//import com.qlangtech.tis.exec.ExecuteResult;
//import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
//import com.qlangtech.tis.manage.biz.dal.pojo.Application;
//import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
//import com.qlangtech.tis.manage.servlet.BasicServlet;
//import com.qlangtech.tis.mcp.McpAgentContext;
//import com.qlangtech.tis.mcp.McpTool;
//import com.qlangtech.tis.mcp.TISHttpMcpServer;
//import com.qlangtech.tis.plugin.ds.ISelectedTab;
//import io.modelcontextprotocol.server.McpSyncServerExchange;
//import org.apache.commons.lang3.StringUtils;
//
//import java.util.List;
//import java.util.Optional;
//
///**
// * 数据血缘追溯：给定目标端表名，查找TIS中哪个管道负责向该表同步数据
// * 性能有问题，暂时不采用
// *
// * @author 百岁 (baisui@qlangtech.com)
// * @date 2026/4/7
// */
//public class GetDataLineageTool extends McpTool {
//
//  public GetDataLineageTool(TISHttpMcpServer mcpServer) {
//    super("get_data_lineage",
//      "数据血缘追溯：给定目标端的表名，查找TIS中哪个管道负责向该表同步数据，返回完整的数据流转链路（源数据源→管道→目标数据源）",
//      mcpServer);
//  }
//
//  @Override
//  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
//    builder.addProperty("tableName", TISJsonSchema.FieldType.String, "目标端表名");
//    builder.addProperty("datasourceName", TISJsonSchema.FieldType.String,
//      "目标端数据源名称（可选，不指定则全局搜索）", false);
//    return super.getInputSchema(builder);
//  }
//
//  @Override
//  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
//    builder.addProperty("found", TISJsonSchema.FieldType.Boolean, "是否找到血缘关系");
//
//    TISJsonSchema.Builder lineageBuilder = TISJsonSchema.Builder.create("lineage_item", Optional.empty());
//    lineageBuilder.addProperty("pipeline", TISJsonSchema.FieldType.String, "管道名称");
//    lineageBuilder.addProperty("sourceTable", TISJsonSchema.FieldType.String, "源端表名");
//    lineageBuilder.addProperty("readerType", TISJsonSchema.FieldType.String, "Reader端插件类型");
//    lineageBuilder.addProperty("writerType", TISJsonSchema.FieldType.String, "Writer端插件类型");
//
//    builder.addProperty("lineage", TISJsonSchema.FieldType.Array, "血缘链路列表")
//      .setItems(lineageBuilder.build());
//    return super.getOutputStream(builder);
//  }
//
//  @Override
//  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
//                                  RequestArguments arguments) throws Exception {
//    String tableName = arguments.get("tableName");
//    if (StringUtils.isEmpty(tableName)) {
//      return ExecuteResult.createFaild().setMessage("参数 tableName 不能为空");
//    }
//
//    IApplicationDAO appDAO = BasicServlet.getBeanByType(IApplicationDAO.class);
//    ApplicationCriteria criteria = new ApplicationCriteria();
//    List<Application> apps = appDAO.selectByExample(criteria);
//
//    JSONArray lineageList = new JSONArray();
//
//    for (Application app : apps) {
//      String pipelineName = app.getProjectName();
//      try {
//        IDataxProcessor processor = DataxProcessor.load(null, pipelineName);
//        if (processor == null)
//          continue;
//
//        IDataxReader reader = processor.getReader(null);
//        if (reader == null)
//          continue;
//
//        List<ISelectedTab> tabs = reader.getSelectedTabs();
//        if (tabs == null)
//          continue;
//
//        for (ISelectedTab tab : tabs) {
//          if (tableName.equalsIgnoreCase(tab.getName())) {
//            JSONObject lineage = new JSONObject(true);
//            lineage.put("pipeline", pipelineName);
//            lineage.put("sourceTable", tab.getName());
//            lineage.put("readerType", reader.getDescriptor().getDisplayName());
//
//            IDataxWriter writer = processor.getWriter(null, false);
//            if (writer != null) {
//              lineage.put("writerType", writer.getDescriptor().getDisplayName());
//            }
//            lineageList.add(lineage);
//          }
//        }
//      } catch (Exception ignored) {
//        // skip pipelines that fail to load
//      }
//    }
//
//    JSONObject result = new JSONObject(true);
//    result.put("found", !lineageList.isEmpty());
//    result.put("lineage", lineageList);
//    return ExecuteResult.createSuccess(result);
//  }
//}
