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


import com.qlangtech.tis.plugin.ds.DataSourceMeta.JDBCConnection;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-08-02 10:15
 **/
public class RdbmsRunningContext implements RunningContext {
    private final String dbName;
    private final String table;

    
    public static RdbmsRunningContext create(JDBCConnection conn, String table) {
        return new RdbmsRunningContext(Objects.requireNonNull(conn, "param conn can not be null").getCatalog(), table);
    }

    public RdbmsRunningContext(String dbName, String table) {
        this.dbName = Objects.requireNonNull(dbName, "param dbName can not be null");
        if (StringUtils.isEmpty(table)) {
            throw new IllegalArgumentException("param table can not be empty");
        }
        this.table = table;
    }

    public String getDbName() {
        return this.dbName;
    }

    public String getTable() {
        return this.table;
    }
}
