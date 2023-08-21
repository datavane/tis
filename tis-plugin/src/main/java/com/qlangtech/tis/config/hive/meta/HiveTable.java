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

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-03-12 13:19
 **/
public abstract class HiveTable {
    private final String name;

    /**
     * example: hdfs://namenode/user/admin/default/2022 0530 1539 50/instancedetail/hudi
     *
     * @return
     */
    public abstract String getStorageLocation();

    public abstract StoredAs getStoredAs();

    /**
     * @param filter the filter string,
     *               for example "part1 = \"p1_abc\" and part2 <= "\p2_test\"". Filtering can
     *               be done only on string partition keys.
     * @return
     */
    public abstract List<String> listPartitions(Optional<String> filter);

    public String getTableName() {
        return this.name;
    }

//    public abstract List<ColumnMetaData> getSchema();

    public HiveTable(String name) {
        this.name = name;
    }

    public static class StoredAs {
        public final String inputFormat;
        public final String outputFormat;

        public StoredAs(String inputFormat, String outputFormat) {
            this.inputFormat = inputFormat;
            this.outputFormat = outputFormat;
        }
    }
}
