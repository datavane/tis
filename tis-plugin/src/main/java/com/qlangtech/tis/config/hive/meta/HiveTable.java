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

package com.qlangtech.tis.config.hive.meta;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-03-12 13:19
 **/
public abstract class HiveTable {

    /**
     * 对应PT最新的分区值
     */
    public static final String KEY_PT_LATEST = "latest";

    private final String name;

    /**
     * example: hdfs://namenode/user/admin/default/2022 0530 1539 50/instancedetail/hudi
     *
     * @return
     */
    public abstract String getStorageLocation();

    public abstract StoredAs getStoredAs();

    public abstract List<String> getPartitionKeys();

    public abstract List<HiveTabColType> getCols();

    public static class HiveTabColType {
        private final String colName;
        private final String type;

        public HiveTabColType(String colName, String type) {
            this.colName = colName;
            this.type = type;
        }

        public String getColName() {
            return colName;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * @param filter the filter string,
     *               for example "part1 = \"p1_abc\" and part2 <= "\p2_test\"". Filtering can
     *               be done only on string partition keys.
     * @return 返回最终的存储数据的path列表集合
     */
    public abstract List<String> listPaths(PartitionFilter filter);

    public String getTableName() {
        return this.name;
    }

//    public abstract List<ColumnMetaData> getSchema();

    public HiveTable(String name) {
        this.name = name;
    }

    public static abstract class StoredAs {
        public final String inputFormat;
        public final String outputFormat;

       // private final Object serdeInfo;

        /**
         * @param inputFormat
         * @param outputFormat
         * // @param serdeInfo    org.apache.hadoop.hive.metastore.api.SerdeInfo
         */
        public StoredAs(String inputFormat, String outputFormat) {
            this.inputFormat = inputFormat;
            this.outputFormat = outputFormat;
          //  this.serdeInfo = serdeInfo;
        }

        public abstract Properties getSerdeProperties(HiveTable table);

        public abstract String getSerializationLib();

//        public <SerDeInfo> SerDeInfo getSerdeInfo() {
//            return (SerDeInfo) serdeInfo;
//        }
    }
}
