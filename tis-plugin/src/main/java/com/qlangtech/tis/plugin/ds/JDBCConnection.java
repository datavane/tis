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

import com.qlangtech.tis.plugin.ds.DataSourceMeta.ResultProcess;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-10-05 19:38
 **/
public class JDBCConnection implements AutoCloseable {

    public static ThreadLocal<JDBCConnectionPool> connectionPool = new ThreadLocal<>();

    private final Connection conn;
    private final String url;

    public JDBCConnection(Connection conn, String url) {
        this.conn = conn;
        this.url = url;
    }

    public String getSchema() {
        try {
            return conn.getSchema();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取得dbName
     *
     * @return
     */
    public String getCatalog() {
        try {
            String catalog = conn.getCatalog();
            String result = StringUtils.defaultString(catalog, this.getSchema());
            if (StringUtils.isEmpty(result)) {
                throw new IllegalStateException("connUrl:" + this.url
                        + " relevant catalog can not be empty,catalog:"
                        + catalog + ",schema:" + this.getSchema());
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Statement createStatement() throws SQLException {
        return this.conn.createStatement();
    }

    public Connection getConnection() {
        return this.conn;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public void close() throws SQLException {
        this.conn.close();
    }

    /**
     * 执行一个查询语句
     *
     * @param sql
     * @param resultProcess
     * @throws Exception
     */
    public void query(String sql, ResultProcess resultProcess) throws Exception {
        synchronized (JDBCConnection.class) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    try (ResultSet result = stmt.executeQuery(sql)) {
                        while (result.next()) {
                            if (!resultProcess.callback(result)) {
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(sql, e);
                }
            }
        }
    }

    public boolean execute(String sql) throws Exception {
        synchronized (JDBCConnection.class) {
            try (Statement stmt = conn.createStatement()) {
                return stmt.execute(sql);
            }
        }
    }

}
