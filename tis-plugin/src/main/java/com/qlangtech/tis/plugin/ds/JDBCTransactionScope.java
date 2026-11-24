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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 线程本地的 JDBC 事务作用域：作用域内 {@link DataSourceFactory#getConnection} 取到的连接
 * 不再走请求级连接池（{@link JDBCConnectionPool}），而是按 jdbcUrl 注册到本作用域 ——
 * 同一 jdbcUrl 恒返回同一条 {@code autoCommit=false} 的物理连接，全部注册连接由
 * {@link #commit()} 统一提交，或者在 {@link #close()} 时统一回滚。
 *
 * <p>典型用法是把一段「多步骤写操作」包成一次原子编辑：
 * <pre>
 * try (JDBCTransactionScope tx = JDBCTransactionScope.open()) {
 *     for (ActionType actionType : actionTypes) {
 *         actionType.executeActionRule(...);   // 任一步骤失败 → 异常抛出
 *     }
 *     tx.commit();                             // 全部成功 → 统一提交
 * }   // close()：未提交则回滚全部已注册连接，并恢复 autoCommit、关闭物理连接
 * </pre>
 *
 * <p><b>语义边界</b>（与请求级池的区别）：
 * <ul>
 *   <li>作用域内连接的生命周期由本作用域管理：首个 {@code getConnection} 时建立，{@code close()}
 *       时物理关闭 —— 比请求级池「活到请求结束」更短；</li>
 *   <li>{@code validateConnection} 等调用方在处理器返回后的 {@code conn.close()} 对作用域内连接是
 *       空操作（{@link TxEnrolledConnection#close()}），否则第一条规则执行完就会把事务连接关掉；</li>
 *   <li>多个 jdbcUrl（多数据源）各自一条事务连接，{@link #commit()} 按注册顺序逐个提交 ——
 *       没有 XA，前面提交成功、后面提交失败时无法整体回滚，只能记日志并抛出（已提交的保持提交，
 *       未提交的在 {@code close()} 中回滚）；</li>
 *   <li>只覆盖走 JDBC 通道的数据源；MongoDB 等不经过 {@code DataSourceFactory#getConnection}
 *       的存储天然不在作用域内。</li>
 * </ul>
 *
 * <p>与 {@link JDBCConnectionPool} 的线程本地互不冲突：作用域优先级更高，作用域关闭后
 * {@code getConnection} 自动恢复原有的池判定逻辑。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/20
 * @see JDBCConnectionPool
 * @see DataSourceFactory#getConnection(String, java.util.Optional, boolean)
 */
public final class JDBCTransactionScope implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(JDBCTransactionScope.class);

    private static final ThreadLocal<JDBCTransactionScope> activeScope = new ThreadLocal<>();

    /**
     * 开启事务作用域并绑定到当前线程。作用域必须用 try-with-resources 关闭，
     * 否则线程本地残留会让当前线程后续所有 {@code getConnection} 永远走事务注册路径。
     *
     * <p>不支持嵌套：当前线程已有活动作用域时抛 {@link IllegalStateException}
     * （fail-fast，避免内层 close 把外层作用域一并清掉）。
     */
    public static JDBCTransactionScope open() {
        if (activeScope.get() != null) {
            throw new IllegalStateException("a transaction scope is already active on current thread,"
                    + " nested scope is not supported");
        }
        JDBCTransactionScope scope = new JDBCTransactionScope();
        activeScope.set(scope);
        return scope;
    }

    /**
     * 当前线程是否有活动的事务作用域。
     */
    public static boolean isActive() {
        return activeScope.get() != null;
    }

    /**
     * 把 jdbcUrl 对应的连接注册进当前活动作用域；无活动作用域时返回 {@code null}，
     * 调用方（{@code DataSourceFactory#getConnection}）随即回落到原有的池判定逻辑。
     *
     * <p>同一 jdbcUrl 只建立一条物理连接（首次注册时经 {@code creator} 创建并置
     * {@code autoCommit=false}），之后全部复用。
     */
    static JDBCConnection enroll(String jdbcUrl, ConnectionFactory creator) throws SQLException {
        JDBCTransactionScope scope = activeScope.get();
        if (scope == null) {
            return null;
        }
        JDBCConnection enrolled = scope.enrolled.get(jdbcUrl);
        if (enrolled != null) {
            return enrolled;
        }
        JDBCConnection physical = creator.create();
        try {
            physical.getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            try {
                physical.close();
            } catch (Throwable closeErr) {
                logger.warn("failed to close connection while setting autoCommit off:" + jdbcUrl, closeErr);
            }
            throw e;
        }
        JDBCConnection guarded = new TxEnrolledConnection(physical);
        scope.enrolled.put(jdbcUrl, guarded);
        return guarded;
    }

    private final Map<String, JDBCConnection> enrolled = new LinkedHashMap<>();
    /**
     * 已成功 commit 的 jdbcUrl 集合：多数据源部分提交失败时，{@code close()} 只回滚
     * 尚未提交的连接，已提交的不能再动。
     */
    private final Set<String> committedUrls = new HashSet<>();
    private boolean commitCompleted;

    private JDBCTransactionScope() {
    }

    /**
     * 提交作用域内全部事务连接（按注册顺序）。全部成功后本作用域视为已提交，
     * {@link #close()} 不再做回滚。
     *
     * <p>多数据源没有 XA：第 N 个连接提交失败时，前 N-1 个已提交的无法回滚 ——
     * 记日志并抛出；失败连接及之后未提交的留给 {@link #close()} 回滚。
     */
    public void commit() {
        if (commitCompleted) {
            throw new IllegalStateException("transaction scope has already been committed");
        }
        SQLException failure = null;
        String failedUrl = null;
        for (Map.Entry<String, JDBCConnection> entry : this.enrolled.entrySet()) {
            try {
                entry.getValue().getConnection().commit();
                committedUrls.add(entry.getKey());
            } catch (SQLException e) {
                failure = e;
                failedUrl = entry.getKey();
                logger.warn("failed to commit transaction on jdbcUrl:" + failedUrl
                        + ",committed urls:" + committedUrls.size() + "/" + enrolled.size()
                        + "（非 XA，已提交部分无法回滚）", e);
                break;
            }
        }
        if (failure != null) {
            throw new RuntimeException("failed to commit transaction on jdbcUrl:" + failedUrl, failure);
        }
        this.commitCompleted = true;
    }

    /**
     * 结束作用域：先清线程本地（后续 {@code getConnection} 恢复池判定），再逐个处理注册连接 ——
     * 未提交的回滚、全部恢复 {@code autoCommit=true} 并物理关闭。任何一步失败只记日志不抛出，
     * 不遮蔽 try-with-resources 主体里正在传播的原始异常。
     */
    @Override
    public void close() {
        activeScope.remove();
        for (Map.Entry<String, JDBCConnection> entry : this.enrolled.entrySet()) {
            String jdbcUrl = entry.getKey();
            Connection physical = entry.getValue().getConnection();
            if (!committedUrls.contains(jdbcUrl)) {
                try {
                    physical.rollback();
                } catch (Throwable e) {
                    logger.warn("failed to rollback transaction on jdbcUrl:" + jdbcUrl, e);
                }
            }
            try {
                physical.setAutoCommit(true);
            } catch (Throwable e) {
                logger.warn("failed to restore autoCommit on jdbcUrl:" + jdbcUrl, e);
            }
            try {
                physical.close();
            } catch (Throwable e) {
                logger.warn("failed to close transaction connection on jdbcUrl:" + jdbcUrl, e);
            }
        }
    }

    /**
     * 事务连接的建连工厂：由 {@code DataSourceFactory#getConnection} 注入自家的
     * {@code createConnection}，作用域不感知各家驱动细节。
     */
    @FunctionalInterface
    interface ConnectionFactory {
        JDBCConnection create() throws SQLException;
    }

    /**
     * 注册进作用域的连接包装：转发一切 JDBC 操作给物理连接，唯独 {@link #close()} 是空操作 ——
     * 作用域内连接的生命周期只归 {@link JDBCTransactionScope#close()} 管。
     *
     * <p>写法与 {@code JDBCConnectionPool.DelegatePoolJDBCConnection} 同一套（基类构造传
     * {@code super(null, url)}，基类方法全部覆写为委托），保证继承到的任何方法都不会触达
     * null 底层字段。
     */
    private static final class TxEnrolledConnection extends JDBCConnection {
        private final JDBCConnection physical;

        TxEnrolledConnection(JDBCConnection physical) {
            super(null, physical.getUrl());
            this.physical = physical;
        }

        @Override
        public String getSchema() {
            return physical.getSchema();
        }

        @Override
        public String getCatalog() {
            return physical.getCatalog();
        }

        @Override
        public Statement createStatement() throws SQLException {
            return physical.createStatement();
        }

        @Override
        public PreparedStatement preparedStatement(String sql) throws SQLException {
            return physical.preparedStatement(sql);
        }

        @Override
        public java.sql.Connection getConnection() {
            return physical.getConnection();
        }

        @Override
        public boolean query(String sql, DataSourceMeta.ResultProcess resultProcess) throws Exception {
            return physical.query(sql, resultProcess);
        }

        @Override
        public boolean execute(String sql) throws Exception {
            return physical.execute(sql);
        }

        @Override
        public void close() {
            // 事务作用域内的连接不能被调用方随手关掉，统一由 JDBCTransactionScope#close() 处理
        }
    }
}
