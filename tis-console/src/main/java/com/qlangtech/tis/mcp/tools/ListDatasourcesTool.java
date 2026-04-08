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
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.ds.DBConfig;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import io.modelcontextprotocol.server.McpSyncServerExchange;

import java.util.List;
import java.util.Optional;

/**
 * 列出TIS中已配置的所有数据源
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/7
 */
public class ListDatasourcesTool extends McpTool {

  private static final String KEY_JDBC_URL = "jdbcUrl";
  private static final String KEY_DATASOURCES = "datasources";

  public ListDatasourcesTool(TISHttpMcpServer mcpServer) {
    super("list_datasources", "列出TIS中已配置的所有数据源，返回数据源名称、类型、连接摘要信息", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    // builder.addProperty("type", TISJsonSchema.FieldType.String, "按数据源类型过滤，如 MySQL、PostgreSQL、Doris 等", false);
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    TISJsonSchema.Builder dsBuilder = TISJsonSchema.Builder.create("datasource_item", Optional.empty());
    dsBuilder.addProperty(HdfsColMeta.KEY_NAME, TISJsonSchema.FieldType.String, "数据源名称");
    dsBuilder.addProperty(HdfsColMeta.KEY_TYPE, TISJsonSchema.FieldType.String, "数据源类型，如MySQL、Doris等");
    dsBuilder.addProperty(KEY_JDBC_URL, TISJsonSchema.FieldType.String, "数据库" + KEY_JDBC_URL + "连接地址");
    builder.addProperty(KEY_DATASOURCES, TISJsonSchema.FieldType.Array, "已配置的数据源列表")
      .setItems(dsBuilder.build());
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    //IEndTypeGetter.EndType filterType = IEndTypeGetter.EndType.parse(arguments.get("type"));

    IWorkflowDAOFacade wfFacade = BasicServlet.getBeanByType(IWorkflowDAOFacade.class);
    DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
    //    if (filterType != null) {
    //      dbCriteria.createCriteria().andExtendClassEqualTo(filterType.toLowerCase());
    //    }
    List<DatasourceDb> dbs = wfFacade.getDatasourceDbDAO().selectByExample(dbCriteria);

    JSONArray datasources = new JSONArray();
    for (DatasourceDb db : dbs) {
      JSONObject dsJson = new JSONObject(true);
      dsJson.put(HdfsColMeta.KEY_NAME, db.getName());
      dsJson.put(HdfsColMeta.KEY_TYPE, db.getExtendClass());
      try {
        DataSourceFactoryPluginStore dsStore =
          TIS.getDataSourceFactoryPluginStore(new PostedDSProp(DBIdentity.parseId(db.getName())));
        DataSourceFactory dsFactory = dsStore.getPlugin();

        if (dsFactory != null) {
          DBConfig dbConfig = dsFactory.getDbConfig();
          if (dbConfig != null) {
            dbConfig.vistDbName(new DBConfig.IProcess() {
              @Override
              public boolean visit(DBConfig config, String jdbcUrl, String ip, String dbName) throws Exception {
                dsJson.put(KEY_JDBC_URL, jdbcUrl);
                return true;
              }
            });
            datasources.add(dsJson);
          }
        }
      } catch (Exception ignored) {
      }

    }

    JSONObject result = new JSONObject(true);
    result.put(KEY_DATASOURCES, datasources);
    return ExecuteResult.createSuccess(result);
  }
}
