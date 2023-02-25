package com.qlangtech.tis.sql.parser;

import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.taskflow.ITemplateContext;
import com.qlangtech.tis.hive.HiveColumn;
import com.qlangtech.tis.plugin.ds.ColMeta;
import com.qlangtech.tis.sql.parser.er.IPrimaryTabFinder;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ISqlTask {

    public String getId();

    String getExportName();

    public List<DependencyNode> getDependencies();

    String getSql();

    RewriteSql getRewriteSql(String taskName, TabPartitions dumpPartition, IPrimaryTabFinder erRules
            , ITemplateContext templateContext, boolean isFinalNode);

    class RewriteSql {

        public final String sqlContent;

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
         * @param sqlContent
         * @param cols         the finally output cols
         * @param primaryTable
         */
        public RewriteSql(String sqlContent, List<ColMeta> cols, IAliasTable primaryTable) {
            this.sqlContent = sqlContent;
            this.primaryTable = primaryTable;
            this.cols = cols;
        }
    }
}
