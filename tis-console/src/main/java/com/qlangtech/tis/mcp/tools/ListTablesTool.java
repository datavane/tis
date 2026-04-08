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
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.plugin.ds.TableInDB;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.qlangtech.tis.mcp.tools.GetPipelineDetailTool.KEY_TABLES;
import static com.qlangtech.tis.mcp.tools.GetTableColumnsTool.KEY_DATASOURCE_NAME;

/**
 * 列表显示某个数据源下的所有表
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/30
 */
public class ListTablesTool extends McpTool {
  private static final Logger logger = LoggerFactory.getLogger(ListTablesTool.class);

  public ListTablesTool(TISHttpMcpServer mcpServer) {
    super("list_tables", "列表显示某个数据源下的所有表", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    builder.addProperty(KEY_DATASOURCE_NAME, TISJsonSchema.FieldType.String, "数据源唯一性名称");
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    builder.addProperty(KEY_DATASOURCE_NAME, TISJsonSchema.FieldType.String, "数据源名称");
    builder.addProperty(KEY_TABLES, TISJsonSchema.FieldType.Array, "表名列表")
      .setItems(TISJsonSchema.FieldType.String);
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    String datasourceName = arguments.get(KEY_DATASOURCE_NAME);
    if (StringUtils.isEmpty(datasourceName)) {
      //return ExecuteResult.createFaild().setMessage("参数 datasourceName 不能为空");
      throw new IllegalArgumentException(KEY_DATASOURCE_NAME + " can not be null");
    }

    DataSourceFactoryPluginStore dsStore =
      TIS.getDataSourceFactoryPluginStore(new PostedDSProp(DBIdentity.parseId(datasourceName)));
    DataSourceFactory dsFactory = dsStore.getPlugin();
    if (dsFactory == null) {
      return ExecuteResult.createFaild().setMessage("未找到数据源: " + datasourceName);
    }

    JSONArray tablesArr = new JSONArray();
    try {
      TableInDB tablesInDB = dsFactory.getTablesInDB();
      if (tablesInDB != null) {
        tablesArr.addAll(tablesInDB.getTabs());
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return ExecuteResult.createFaild().setMessage("获取表列表失败: " + e.getMessage());
    }

    JSONObject result = new JSONObject(true);
    result.put(KEY_DATASOURCE_NAME, datasourceName);
    result.put(KEY_TABLES, tablesArr);
    return ExecuteResult.createSuccess(result);
  }
}
