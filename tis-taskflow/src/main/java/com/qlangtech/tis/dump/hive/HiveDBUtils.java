/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.dump.hive;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.DelegatingStatement;
import org.apache.commons.lang.StringUtils;
import org.apache.hive.jdbc.HiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HiveDBUtils {

    private static final BasicDataSource hiveDatasource = new BasicDataSource();

    private static final Logger log = LoggerFactory.getLogger(HiveDBUtils.class);

    private static final int DEFAULT_QUERY_PROGRESS_INTERVAL = 500;

    private static final ExecutorService exec = Executors.newCachedThreadPool();

    private static HiveDBUtils hiveHelper;

    public static HiveDBUtils getInstance() {
        if (hiveHelper == null) {
            synchronized (HiveDBUtils.class) {
                if (hiveHelper == null) {
                    hiveHelper = new HiveDBUtils();
                }
            }
        }
        return hiveHelper;
    }

    private HiveDBUtils() {
    }

    private static final String hiveHost;

    static {
        hiveDatasource.setDriverClassName("org.apache.hive.jdbc.HiveDriver");
        hiveDatasource.setMaxActive(-1);
        hiveDatasource.setRemoveAbandoned(true);
        hiveDatasource.setRemoveAbandonedTimeout(300 * 30);
        // 测试空闲的连接是否有效,这个参数很重要
        hiveDatasource.setTestWhileIdle(true);
        hiveDatasource.setTestOnBorrow(true);
        hiveDatasource.setValidationQuery("select 1");
        hiveHost = TSearcherConfigFetcher.get().getHiveHost();
        if (StringUtils.isBlank(hiveHost)) {
            throw new IllegalStateException("hivehost can not be null");
        }
        String hiveJdbcUrl = "jdbc:hive2://" + hiveHost + "/tis";
        hiveDatasource.setUrl(hiveJdbcUrl);
        log.info("hiveJdbcUrl:" + hiveJdbcUrl);
    }

    public Connection createConnection() {
        return createConnection(0);
    }

    public Connection createConnection(int retry) {
        Connection conn = null;
        try {
            conn = hiveDatasource.getConnection();
            execute(conn, "set hive.exec.dynamic.partition.mode=nonstrict", false);
            return conn;
        } catch (Exception e) {
            if (retry < 5) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
                try {
                    if (conn != null) {
                        close(conn);
                    }
                } catch (Throwable e1) {
                }
                log.warn("retry:" + retry, e);
                return createConnection(++retry);
            } else {
                throw new IllegalStateException("retry:" + retry + "hivehost:" + hiveHost, e);
            }
        }
    }

    public void close(Connection conn) {
        try {
            conn.close();
        } catch (Throwable e) {
        }
    }

    public boolean execute(Connection conn, String sql) throws SQLException {
        return execute(conn, sql, true);
    }

    /**
     * 执行一个sql语句
     *
     * @param sql
     * @return
     * @throws Exception
     */
    public boolean execute(Connection conn, String sql, boolean listenLog) throws SQLException {
        synchronized (HiveDBUtils.class) {
            try (Statement stmt = conn.createStatement()) {
                // exec.submit(createLogRunnable(stmt));
                Future<?> f = null;
                try {
                    if (listenLog) {
                        f = exec.submit(createLogRunnable(stmt));
                    }
                    return stmt.execute(sql);
                } catch (SQLException e) {
                    throw new RuntimeException(sql, e);
                } finally {
                    try {
                        if (listenLog) {
                            f.cancel(true);
                        }
                    } catch (Throwable e) {
                    }
                }
            }
        }
    }

    private Runnable createLogRunnable(Statement statement) {
        final String collection = MDC.get("app");
        // org.apache.commons.dbcp.DelegatingStatement
        if (statement instanceof org.apache.commons.dbcp.DelegatingStatement) {
            final HiveStatement hiveStatement = (HiveStatement) ((DelegatingStatement) statement).getInnermostDelegate();
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    MDC.put("app", collection);
                    while (hiveStatement.hasMoreLogs()) {
                        try {
                            // console
                            for (String logmsg : hiveStatement.getQueryLog()) {
                                log.info(logmsg);
                            }
                            try {
                                Thread.sleep(DEFAULT_QUERY_PROGRESS_INTERVAL);
                            } catch (Throwable e) {
                                return;
                            }
                        } catch (SQLException e) {
                            log.error(e.getMessage(), e);
                            return;
                        }
                    }
                }
            };
            return runnable;
        } else {
            log.debug("The statement instance is not HiveStatement type: " + statement.getClass());
            return new Runnable() {

                @Override
                public void run() {
                // do nothing.
                }
            };
        }
    }

    /**
     * 执行一个查询语句
     *
     * @param sql
     * @param resultProcess
     * @throws Exception
     */
    public void query(Connection conn, String sql, ResultProcess resultProcess) throws Exception {
        synchronized (HiveDBUtils.class) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    try (ResultSet result = stmt.executeQuery(sql)) {
                        while (result.next()) {
                            resultProcess.callback(result);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(sql, e);
                }
            }
        }
    }

    public static interface ResultProcess {

        public void callback(ResultSet result) throws Exception;
    }

    public static void main(String[] args) throws Exception {
        Connection con = hiveDatasource.getConnection();
        // Connection con = DriverManager.getConnection(
        // "jdbc:hive://10.1.6.211:10000/tis", "", "");
        System.out.println("start create connection");
        // Connection con = DriverManager.getConnection(
        // "jdbc:hive2://hadoop6:10001/tis", "", "");
        System.out.println("create conn");
        Statement stmt = con.createStatement();
        ResultSet result = stmt.executeQuery("desc totalpay_summary");
        while (result.next()) {
            System.out.println("cols:" + result.getString(1));
        }
        // String tableName = "testHiveDriverTable";
        // // stmt.executeQuery("drop table " + tableName);
        // 
        // stmt.execute("drop table " + tableName);
        // ResultSet res = null;
        // stmt.execute("create table " + tableName +
        // " (key int, value string)");
        // // show tables
        // String sql = "show tables '" + tableName + "'";
        // System.out.println("Running: " + sql);
        // res = stmt.executeQuery(sql);
        // if (res.next()) {
        // System.out.println(res.getString(1));
        // }
        // // describe table
        // sql = "describe " + tableName;
        // System.out.println("Running: " + sql);
        // res = stmt.executeQuery(sql);
        // while (res.next()) {
        // System.out.println(res.getString(1) + "\t" + res.getString(2));
        // }
        // 
        // // load data into table
        // // NOTE: filepath has to be local to the hive server
        // // NOTE: /tmp/a.txt is a ctrl-A separated file with two fields per
        // line
        // String filepath = "/tmp/a.txt";
        // sql = "load data local inpath '" + filepath + "' into table "
        // + tableName;
        // System.out.println("Running: " + sql);
        // res = stmt.executeQuery(sql);
        // 
        // // select * query
        // sql = "select * from " + tableName;
        // System.out.println("Running: " + sql);
        // res = stmt.executeQuery(sql);
        // while (res.next()) {
        // System.out.println(String.valueOf(res.getInt(1)) + "\t"
        // + res.getString(2));
        // }
        // 
        // // regular hive query
        // String sql = "select count(1) from " + tableName;
        // HiveDBUtils hiveHelper = new HiveDBUtils();
        // String sql = IOUtils.toString(Thread.currentThread()
        // .getContextClassLoader()
        // .getResourceAsStream("create_tmp_order_instance.txt"));
        // System.out.println("Running: " + sql);
        // // ResultSet res =
        // stmt.execute(sql);
        // stmt.close();
        // 
        // System.out.println("another conn");
        // hiveHelper.query(con, "show tables", new ResultProcess() {
        // @Override
        // public void callback(ResultSet result) throws Exception {
        // System.out.println(result.getString(1));
        // }
        // });
        // System.out.println("===============================================");
        // System.out.println("same connection");
        // stmt = con.createStatement();
        // ResultSet res = stmt.executeQuery("show tables");
        // while (res.next()) {
        // System.out.println(res.getString(1));
        // }
        stmt.close();
        con.close();
    }
}
