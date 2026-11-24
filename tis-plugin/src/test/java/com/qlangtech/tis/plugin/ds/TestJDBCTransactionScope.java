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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * {@link JDBCTransactionScope} 的契约测试。
 *
 * <p>钉住四条契约：
 * <ol>
 *   <li>作用域内同一 jdbcUrl 的 {@code getConnection} 恒返回同一条 {@code autoCommit=false} 的连接，
 *       作用域外完全不受影响（走原有池/直连路径）；</li>
 *   <li>作用域内连接的 {@code close()} 是空操作 —— 否则 {@code validateConnection} 的 finally
 *       会在首条规则执行完就把事务连接物理关掉；</li>
 *   <li>commit 成功路径：统一提交、close 不回滚；未 commit 直接 close：全部回滚并恢复 autoCommit；</li>
 *   <li>多数据源没有 XA：第 N 个连接提交失败时已提交的不动、未提交的由 close 回滚；
 *       线程本地随 close 清理，不支持嵌套 open。</li>
 * </ol>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/20
 */
public class TestJDBCTransactionScope {

    private static final String URL_A = "jdbc:mock://host1/db_a";
    private static final String URL_B = "jdbc:mock://host2/db_b";

    private final Map<String, JDBCConnection> connMocks = new HashMap<>();
    private final Map<String, Connection> rawConnMocks = new HashMap<>();
    private StubDataSourceFactory dsFactory;

    private void stubConn(String jdbcUrl) {
        Connection raw = Mockito.mock(Connection.class);
        JDBCConnection conn = Mockito.mock(JDBCConnection.class);
        Mockito.when(conn.getUrl()).thenReturn(jdbcUrl);
        Mockito.when(conn.getConnection()).thenReturn(raw);
        rawConnMocks.put(jdbcUrl, raw);
        connMocks.put(jdbcUrl, conn);
    }

    private StubDataSourceFactory dsFactory() {
        if (dsFactory == null) {
            dsFactory = new StubDataSourceFactory(connMocks);
        }
        return dsFactory;
    }

    private static JDBCConnection connectionOf(StubDataSourceFactory factory, String jdbcUrl) throws SQLException {
        JDBCConnection conn = factory.getConnection(jdbcUrl, Optional.empty(), true);
        Assert.assertNotNull("scope active, getConnection must return enrolled connection", conn);
        return conn;
    }

    /**
     * 契约 1a：作用域内同 jdbcUrl 复用同一条连接，autoCommit=false 只设置一次。
     */
    @Test
    public void testSameUrlReusesEnrolledConnectionAndAutoCommitOff() throws Exception {
        stubConn(URL_A);
        try (JDBCTransactionScope tx = JDBCTransactionScope.open()) {
            Assert.assertTrue(JDBCTransactionScope.isActive());
            JDBCConnection first = connectionOf(dsFactory(), URL_A);
            JDBCConnection second = connectionOf(dsFactory(), URL_A);
            Assert.assertSame("same jdbcUrl must reuse the enrolled connection", first, second);
            // 底层物理连接上 autoCommit=false 只置一次（首条规则建连时），后续复用不再重复设置
            Mockito.verify(rawConnMocks.get(URL_A), Mockito.times(1)).setAutoCommit(false);
            Assert.assertEquals("create only once per jdbcUrl", 1, dsFactory().createCount);
            tx.commit();
        }
        Mockito.verify(rawConnMocks.get(URL_A)).commit();
    }

    /**
     * 契约 1b：作用域外 getConnection 不进注册表（enroll 返回 null → 原有池/直连路径）。
     */
    @Test
    public void testNoScopeConnectionNotEnrolled() throws Exception {
        stubConn(URL_A);
        Assert.assertFalse(JDBCTransactionScope.isActive());
        StubDataSourceFactory factory = dsFactory();
        factory.getConnection(URL_A, Optional.empty(), true);
        // 无作用域：直接走 createConnection，不触碰事务相关方法
        Mockito.verify(rawConnMocks.get(URL_A), Mockito.never()).setAutoCommit(false);
        Mockito.verify(rawConnMocks.get(URL_A), Mockito.never()).commit();
        Mockito.verify(rawConnMocks.get(URL_A), Mockito.never()).rollback();
    }

    /**
     * 契约 2：作用域内连接 close() 是空操作，物理连接只能由作用域 close 关闭。
     */
    @Test
    public void testEnrolledConnectionCloseIsNoOp() throws Exception {
        stubConn(URL_A);
        try (JDBCTransactionScope tx = JDBCTransactionScope.open()) {
            JDBCConnection enrolled = connectionOf(dsFactory(), URL_A);
            // 模拟 validateConnection 的 finally close：对作用域连接必须是空操作
            enrolled.close();
            Mockito.verify(rawConnMocks.get(URL_A), Mockito.never()).close();
            tx.commit();
        }
        Mockito.verify(rawConnMocks.get(URL_A)).close();
    }

    /**
     * 契约 3a：未 commit 直接 close（规则失败路径）→ 回滚 + 恢复 autoCommit + 物理关闭。
     */
    @Test
    public void testCloseWithoutCommitRollsBack() throws Exception {
        stubConn(URL_A);
        try (JDBCTransactionScope tx = JDBCTransactionScope.open()) {
            connectionOf(dsFactory(), URL_A);
            // 模拟规则执行失败：跳过 commit，直接走 close
        }
        Mockito.verify(rawConnMocks.get(URL_A)).rollback();
        Mockito.verify(rawConnMocks.get(URL_A)).setAutoCommit(true);
        Mockito.verify(rawConnMocks.get(URL_A)).close();
        Mockito.verify(rawConnMocks.get(URL_A), Mockito.never()).commit();
        Assert.assertFalse("threadlocal must be cleared after close", JDBCTransactionScope.isActive());
    }

    /**
     * 契约 3b：commit 成功后 close 不回滚。
     */
    @Test
    public void testCloseAfterCommitSkipsRollback() throws Exception {
        stubConn(URL_A);
        try (JDBCTransactionScope tx = JDBCTransactionScope.open()) {
            connectionOf(dsFactory(), URL_A);
            tx.commit();
        }
        Mockito.verify(rawConnMocks.get(URL_A)).commit();
        Mockito.verify(rawConnMocks.get(URL_A), Mockito.never()).rollback();
    }

    /**
     * 契约 4a：多 jdbcUrl 各建一条事务连接，commit 逐个提交。
     */
    @Test
    public void testMultiUrlCommitEach() throws Exception {
        stubConn(URL_A);
        stubConn(URL_B);
        try (JDBCTransactionScope tx = JDBCTransactionScope.open()) {
            JDBCConnection connA = connectionOf(dsFactory(), URL_A);
            JDBCConnection connB = connectionOf(dsFactory(), URL_B);
            Assert.assertNotSame("different jdbcUrl must get different connections", connA, connB);
            tx.commit();
        }
        Mockito.verify(rawConnMocks.get(URL_A)).commit();
        Mockito.verify(rawConnMocks.get(URL_B)).commit();
        Mockito.verify(rawConnMocks.get(URL_A), Mockito.never()).rollback();
        Mockito.verify(rawConnMocks.get(URL_B), Mockito.never()).rollback();
    }

    /**
     * 契约 4b：多数据源非 XA —— 第二个连接 commit 失败时，已提交的不再回滚，
     * 失败连接由 close 回滚，异常透传给调用方。
     */
    @Test
    public void testPartialCommitFailureRollsBackOnlyUncommitted() throws Exception {
        stubConn(URL_A);
        stubConn(URL_B);
        Mockito.doThrow(new SQLException("mock commit failure"))
                .when(rawConnMocks.get(URL_B)).commit();
        try (JDBCTransactionScope tx = JDBCTransactionScope.open()) {
            connectionOf(dsFactory(), URL_A);
            connectionOf(dsFactory(), URL_B);
            try {
                tx.commit();
                Assert.fail("commit must fail when one url's commit fails");
            } catch (RuntimeException expect) {
                // 透传
            }
        }
        Mockito.verify(rawConnMocks.get(URL_A)).commit();
        Mockito.verify(rawConnMocks.get(URL_A), Mockito.never()).rollback();
        Mockito.verify(rawConnMocks.get(URL_B)).rollback();
    }

    /**
     * 契约 4c：不支持嵌套 open；重复 commit 拒绝。
     */
    @Test
    public void testNestedOpenAndDoubleCommitRejected() throws Exception {
        stubConn(URL_A);
        try (JDBCTransactionScope outer = JDBCTransactionScope.open()) {
            try {
                JDBCTransactionScope.open();
                Assert.fail("nested open must be rejected");
            } catch (IllegalStateException expect) {
                // fail-fast
            }
            connectionOf(dsFactory(), URL_A);
            outer.commit();
            try {
                outer.commit();
                Assert.fail("double commit must be rejected");
            } catch (IllegalStateException expect) {
                // fail-fast
            }
        }
        Assert.assertFalse(JDBCTransactionScope.isActive());
    }

    /**
     * 最小 DataSourceFactory 桩：只实现抽象方法 + 覆写 {@code createConnection}，
     * 让 final 的 {@code getConnection} 走「事务注册 → 无池 → 直连」这条最短路径。
     */
    private static final class StubDataSourceFactory extends DataSourceFactory {
        private final Map<String, JDBCConnection> conns;
        int createCount = 0;

        StubDataSourceFactory(Map<String, JDBCConnection> conns) {
            this.conns = conns;
        }

        @Override
        public DBConfig getDbConfig() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitFirstConnection(IConnProcessor connProcessor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void refresh() {
            // no-op
        }

        @Override
        protected JDBCConnection createConnection(String jdbcUrl, Optional<Properties> props, boolean verify) {
            this.createCount++;
            return this.conns.get(jdbcUrl);
        }
    }
}
