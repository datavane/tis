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
import com.qlangtech.tis.extension.util.MultiItemsViewType;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.DataType;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 获取某个数据源中某个表的列的元数据列表
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/30
 */
public class GetTableColumnsTool extends McpTool {

  public static final String KEY_DATASOURCE_NAME = "datasourceName";
  private static final String KEY_TABLE_NAME = "tableName";


  public GetTableColumnsTool(TISHttpMcpServer mcpServer) {
    super("get_table_columns", "获取某个数据源中某个表的列的元数据列表，包括列名、数据类型、是否主键、是否可空", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    builder.addProperty(KEY_DATASOURCE_NAME, TISJsonSchema.FieldType.String, "代表数据源实例的唯一名称");
    builder.addProperty(KEY_TABLE_NAME, TISJsonSchema.FieldType.String, "表名称");
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    TISJsonSchema.Builder colMetaBuilder = TISJsonSchema.Builder.create("cols_meta", Optional.empty());
    colMetaBuilder.addProperty(HdfsColMeta.KEY_NAME, TISJsonSchema.FieldType.String, "列的名称");
    colMetaBuilder.addObjectProperty(HdfsColMeta.KEY_TYPE, (inner) -> {
      /**
       * @see DataType
       */
      inner.addProperty(DataType.KEY_TYPE_NAME, TISJsonSchema.FieldType.String, "类型名称");
      inner.addProperty(DataType.KEY_COLUMN_SIZE, TISJsonSchema.FieldType.Integer, "列长度", false);
      inner.addProperty(DataType.KEY_FIELD_UNSIGNED
        , TISJsonSchema.FieldType.Boolean, "属性限制数据库表中的数值类型列只能存储非负数（即0和正整数)", false);
      inner.addProperty(DataType.KEY_DECUNAK_DIGITS, TISJsonSchema.FieldType.Integer, "浮点类型的小数位长度", false);
    });
    colMetaBuilder.addProperty(HdfsColMeta.KEY_PK, TISJsonSchema.FieldType.Boolean, "是否为主键");
    colMetaBuilder.addProperty(HdfsColMeta.KEY_NULLABLE, TISJsonSchema.FieldType.Boolean, "是否可为空");
    colMetaBuilder.addProperty(ColumnMetaData.KEY_COMMENT, TISJsonSchema.FieldType.String, "列注释", false);
    builder.addProperty(MultiItemsViewType.keyColsMeta, TISJsonSchema.FieldType.Array, "属于表的列元数据列表信息")
      .setItems(colMetaBuilder.build());
    builder.addProperty(KEY_DATASOURCE_NAME, TISJsonSchema.FieldType.String, "代表数据源实例的唯一名称");
    builder.addProperty(KEY_TABLE_NAME, TISJsonSchema.FieldType.String, "表名称");
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    String datasourceName = arguments.get(KEY_DATASOURCE_NAME);
    String tableName = arguments.get(KEY_TABLE_NAME);

    if (StringUtils.isEmpty(datasourceName)) {
      return ExecuteResult.createFaild().setMessage("参数 datasourceName 不能为空");
    }
    if (StringUtils.isEmpty(tableName)) {
      return ExecuteResult.createFaild().setMessage("参数 tableName 不能为空");
    }

    DataSourceFactoryPluginStore dsStore =
      TIS.getDataSourceFactoryPluginStore(new PostedDSProp(DBIdentity.parseId(datasourceName)));
    DataSourceFactory dsFactory = dsStore.getPlugin();
    if (dsFactory == null) {
      return ExecuteResult.createFaild().setMessage("未找到数据源: " + datasourceName);
    }

    List<ColumnMetaData> colsMeta;
    try {
      colsMeta = dsFactory.getTableMetadata(false, agentContext.getTaskPlan().getControlMsgHandler(),
        EntityName.parse(tableName));
    } catch (Exception e) {
      return ExecuteResult.createFaild().setMessage("获取表列元数据失败: " + e.getMessage());
    }

    JSONArray colsArr = new JSONArray();
    if (colsMeta != null) {
      for (ColumnMetaData col : colsMeta) {
        JSONObject colJson = new JSONObject(true);
        colJson.put(HdfsColMeta.KEY_NAME, col.getName());
        DataType type = col.getType();
        colJson.put(HdfsColMeta.KEY_TYPE, type.convert2JsonDesc());
        colJson.put(HdfsColMeta.KEY_PK, col.isPk());
        colJson.put(HdfsColMeta.KEY_NULLABLE, col.isNullable());
        if (StringUtils.isNotEmpty(col.getComment())) {
          colJson.put(ColumnMetaData.KEY_COMMENT, col.getComment());
        }
        colsArr.add(colJson);
      }
    }

    JSONObject result = new JSONObject(true);
    result.put(KEY_DATASOURCE_NAME, datasourceName);
    result.put(KEY_TABLE_NAME, tableName);
    result.put(MultiItemsViewType.keyColsMeta, colsArr);
    return ExecuteResult.createSuccess(result);
  }


}
