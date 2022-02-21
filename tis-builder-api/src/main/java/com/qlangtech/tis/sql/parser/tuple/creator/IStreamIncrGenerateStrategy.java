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
package com.qlangtech.tis.sql.parser.tuple.creator;

import com.qlangtech.tis.realtime.transfer.UnderlineUtils;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-06 09:49
 */
public interface IStreamIncrGenerateStrategy {

    String TEMPLATE_FLINK_TABLE_HANDLE_SCALA = "flink_table_handle_scala.vm";

    default boolean isExcludeFacadeDAOSupport() {
        return true;
    }

    default String getFlinkStreamGenerateTemplateFileName() {
        return "flink_source_handle_scala.vm";
    }

    default IStreamTemplateData decorateMergeData(IStreamTemplateData mergeData) {
        return mergeData;
    }

//    Map<IEntityNameGetter, List<IValChain>> getTabTriggerLinker();
//
//    /**
//     * map<dbname,list<tables>>
//     *
//     * @return
//     */
//    Map<DBNode, List<String>> getDependencyTables(IDBTableNamesGetter dbTableNamesGetter);
//
//    IERRules getERRule();


    /**
     *
     **/
    interface IStreamTemplateData {

        /**
         * TIS App 应用名称
         *
         * @return
         */
        public String getCollection();

        public default String getJavaName() {
            return UnderlineUtils.getJavaName(this.getCollection());
        }


        public List<EntityName> getDumpTables();
    }

    public static abstract class AdapterStreamTemplateData implements IStreamTemplateData {
        private final IStreamTemplateData data;

        public AdapterStreamTemplateData(IStreamTemplateData data) {
            this.data = data;
        }

        @Override
        public String getCollection() {
            return data.getCollection();
        }

        @Override
        public String getJavaName() {
            return data.getJavaName();
        }

        @Override
        public List<EntityName> getDumpTables() {
            return data.getDumpTables();
        }
    }
}
