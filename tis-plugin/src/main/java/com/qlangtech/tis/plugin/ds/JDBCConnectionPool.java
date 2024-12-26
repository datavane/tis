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

import com.google.common.collect.Maps;
import com.qlangtech.tis.plugin.ds.DataSourceMeta.ResultProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-10-05 19:40
 **/
public abstract class JDBCConnectionPool implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(DefaultJDBCConnectionPool.class);

    public static <CONN extends JDBCConnection> CONN getJDBCConnectionFromPool(JDBCConnection conn) {
        if (conn instanceof DelegatePoolJDBCConnection) {
            return (CONN) ((DelegatePoolJDBCConnection) conn).conn;
        } else {
            return (CONN) conn;
        }
    }

    public abstract JDBCConnection getConnection(String jdbcUrl, boolean verify);

    public abstract JDBCConnection getConnection(String jdbcUrl, boolean verify, Function<String, JDBCConnection> mappingFunction);

    public static JDBCConnectionPool create() {
        return new DefaultJDBCConnectionPool();
    }

    private static class DefaultJDBCConnectionPool extends JDBCConnectionPool {


        private final ConcurrentMap<String, DelegatePoolJDBCConnection> connectionCache = Maps.newConcurrentMap();

        public DefaultJDBCConnectionPool() {
        }

        @Override
        public JDBCConnection getConnection(String jdbcUrl, boolean verify, Function<String, JDBCConnection> mappingFunction) {
            return connectionCache.computeIfAbsent(createCacheKey(jdbcUrl, verify), mappingFunction.andThen((conn) -> {
                return new DelegatePoolJDBCConnection(conn, conn.getUrl());
            }));
        }

        private static String createCacheKey(String jdbcUrl, boolean verify) {
            return jdbcUrl + "_verify_" + verify;
        }

        @Override
        public JDBCConnection getConnection(String jdbcUrl, boolean verify) {
            return connectionCache.get(createCacheKey(jdbcUrl, verify));
        }


        @Override
        public void close() throws Exception {
            for (Map.Entry<String, DelegatePoolJDBCConnection> entry : connectionCache.entrySet()) {
                try {
                    entry.getValue().conn.close();
                } catch (SQLException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
    }


    /**
     * 保证在池中的连接器不会被关闭
     *
     * @see com.qlangtech.tis.plugin.ds.JDBCConnectionPool.DelegatePoolJDBCConnection#close()
     */
    private final static class DelegatePoolJDBCConnection extends JDBCConnection {
        private final JDBCConnection conn;

        public DelegatePoolJDBCConnection(JDBCConnection conn, String url) {
            super(null, url);
            this.conn = conn;
        }

        @Override
        public final String getSchema() {
            return conn.getSchema();
        }

        @Override
        public String getCatalog() {
            return conn.getCatalog();
        }

        @Override
        public Statement createStatement() throws SQLException {
            return conn.createStatement();
        }

        @Override
        public PreparedStatement preparedStatement(String sql) throws SQLException {
            return conn.preparedStatement(sql);
        }

        @Override
        public Connection getConnection() {
            return conn.getConnection();
        }

        @Override
        public void query(String sql, ResultProcess resultProcess) throws Exception {
            conn.query(sql, resultProcess);
        }

        @Override
        public boolean execute(String sql) throws Exception {
            return conn.execute(sql);
        }

        @Override
        public void close() throws SQLException {
            // 最主要的不能被关掉
        }

    }

}
