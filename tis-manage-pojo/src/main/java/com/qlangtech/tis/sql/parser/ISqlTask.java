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

package com.qlangtech.tis.sql.parser;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.IPartionableWarehouse;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import com.qlangtech.tis.plugin.ds.ColMeta;
import com.qlangtech.tis.plugin.ds.IDBReservedKeys;
import com.qlangtech.tis.sql.parser.er.IPrimaryTabFinder;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import com.qlangtech.tis.sql.parser.meta.NodeType;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ISqlTask {

    String KEY_ID = "id";
    String KEY_SQL_SCRIPT = "sqlScript";
    String KEY_EXECUTE_TYPE = "executeType";
    String KEY_EXPORT_NAME = "exportName";
    //String KEY_DEPENDENCIES = "dependencies";
   // String KEY_DATAFLOW_NAME = "dataflow";

//    public static List<DependencyNode> deserializeDependencyNodes(JSONObject sqlTaskCfg) {
//        List<DependencyNode> dependencyNodes = JSONArray.parseArray(sqlTaskCfg.getString(KEY_DEPENDENCIES), DependencyNode.class);
//        return dependencyNodes;
//    }

    public static JSONObject json(ISqlTask sqlTask) {
        JSONObject task = new JSONObject();
        task.put(KEY_ID, sqlTask.getId());
        task.put(KEY_SQL_SCRIPT, sqlTask.getSql());
        task.put(KEY_EXECUTE_TYPE, NodeType.JOINER_SQL.getType());
        task.put(KEY_EXPORT_NAME, sqlTask.getExportName());
//        if (CollectionUtils.isEmpty(sqlTask.getDependencies())) {
//            throw new IllegalStateException("sqlTask.getDependencies() can not be empty");
//        }
        //  task.put(KEY_DEPENDENCIES, JSONArray.toJSONString(sqlTask.getDependencies()));
        return task;
    }

    public static SqlTaskCfg toCfg(JSONObject sqlTask) throws NodeType.NodeTypeParseException {
        SqlTaskCfg taskCfg = new SqlTaskCfg();
        taskCfg.id = sqlTask.getString(KEY_ID);
        taskCfg.sqlScript = sqlTask.getString(KEY_SQL_SCRIPT);
        taskCfg.executeType = NodeType.parse(sqlTask.getString(KEY_EXECUTE_TYPE));
        taskCfg.exportName = sqlTask.getString(KEY_EXPORT_NAME);
        // taskCfg.dependencies = deserializeDependencyNodes(sqlTask); // JSONArray.parseArray(sqlTask.getString(KEY_DEPENDENCIES), DependencyNode.class);
        return taskCfg;
    }

    class SqlTaskCfg {
        private String id;
        private String sqlScript;
        private NodeType executeType;
        private String exportName;
        // private List<DependencyNode> dependencies;

//        public List<DependencyNode> getDependencies() {
//            return dependencies;
//        }

        public String getId() {
            return id;
        }

        public String getSqlScript() {
            return sqlScript;
        }

        public NodeType getExecuteType() {
            return executeType;
        }

        public String getExportName() {
            return this.exportName;
        }
    }


    public String getId();

    String getExportName();

    public List<DependencyNode> getDependencies();

    String getSql();

    RewriteSql getRewriteSql(String taskName, TabPartitions dumpPartition, IPartionableWarehouse dumpTableNameRewriter, Supplier<IPrimaryTabFinder> erRules
            , IJoinTaskContext templateContext, boolean isFinalNode);

    class RewriteSql {
        private static final MessageFormat SQL_INSERT_TABLE
                = new MessageFormat("INSERT OVERWRITE TABLE {0} PARTITION (" + IDumpTable.PARTITION_PT + "," + IDumpTable.PARTITION_PMOD + ") \n {1}");

        public final String originSql;
        public final String rewriteSql;

        public final IAliasTable primaryTable;

        /**
         * @see com.qlangtech.tis.plugin.ds.ColMeta
         * @see com.qlangtech.tis.plugin.ds.ColumnMetaData
         */
        private final List<ColMeta> cols;

        public List<ColMeta> getCols() {
            if (CollectionUtils.isEmpty(this.cols)) {
                throw new IllegalStateException("cols can not be null");
            }
            return this.cols;
        }


        /**
         * 除去ps列
         */
        public List<ColMeta> getColsExcludePartitionCols() {
            return getCols().stream().filter((r) -> !IDumpTable.preservedPsCols.contains(r.getName())).collect(Collectors.toList());
        }

        /**
         * @param rewriteSql
         * @param cols         the finally output cols
         * @param primaryTable
         */
        public RewriteSql(String originSql, String rewriteSql, List<ColMeta> cols, IAliasTable primaryTable) {
            if (StringUtils.isEmpty(originSql)) {
                throw new IllegalArgumentException("param originSql can not be empty");
            }
            this.originSql = originSql;
            this.rewriteSql = rewriteSql;
            this.primaryTable = primaryTable;
            this.cols = cols;
        }


        public String convert2InsertIntoSQL(IDBReservedKeys dbReservedKeys, String exportTabName) {
            final EntityName newCreateTab = EntityName.parse(exportTabName);
            return SQL_INSERT_TABLE.format(
                    new Object[]{newCreateTab.getFullName(dbReservedKeys.getEscapeChar())
                            , this.rewriteSql});
        }

    }
}
