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
package com.qlangtech.tis.plugin.ds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源meta信息获取
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 15:51
 */
public interface DataSourceMeta {

    String METHOD_GET_PARTITION_KEYS = "getPartitionKeys";
    String METHOD_GET_PRIMARY_KEYS = "getPrimaryKeys";

    static ThreadLocal<Map<String, List<ColumnMetaData>>> tableMetadataLocal = ThreadLocal.withInitial(() -> {
        return new HashMap<>();
    });

    /**
     * Get all the tables in dataBase
     *
     * @return
     */
    default List<String> getTablesInDB() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get table column metaData list
     *
     * @param table
     * @return
     */
    default List<ColumnMetaData> getTableMetadata(String table) {
        throw new UnsupportedOperationException();
    }
}
