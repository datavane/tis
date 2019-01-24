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
package com.qlangtech.tis.common.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/*
 * 默认的sql查询执行器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultSqlExecutor implements SqlExecutor, ApplicationContextAware {

    public static final String DATASOURCE_HOLDER = "$dataSource$";

    public static final String TABLE_SUFFIX_PLACE_HOLDER = "$tablename$";

    public static final String SHARD_KEY = "$shardKey$";

    protected static Log logger = LogFactory.getLog(DefaultSqlExecutor.class);

    // 以下属性需要注入
    /**
     * @uml.property  name="baseSql"
     */
    protected String baseSql;

    /**
     * @uml.property  name="subtableDesc"
     */
    protected String subtableDesc;

    /**
     * @uml.property  name="router"
     * @uml.associationEnd
     */
    protected TableRouter router;

    // 以下用默认
    /**
     * @uml.property  name="parser"
     * @uml.associationEnd
     */
    protected TableDescriptionParser parser;

    // 以下为自己的参数
    protected List<Connection> connectionList;

    protected Map<String, Statement> statementMap;

    /**
     * @uml.property  name="applicationContext"
     */
    protected ApplicationContext applicationContext;

    protected AtomicBoolean isInited = new AtomicBoolean(false);

    public DefaultSqlExecutor() {
    }

    public DefaultSqlExecutor(String baseSql, String subtableDesc, TableRouter router) {
        this.baseSql = baseSql;
        this.subtableDesc = subtableDesc;
        this.router = router;
    }

    public DefaultSqlExecutor(String baseSql, String subtableDesc, TableRouter router, TableDescriptionParser parser) {
        this(baseSql, subtableDesc, router);
        this.parser = parser;
    }

    public void init() throws Exception {
        if (!isInited.getAndSet(true)) {
            if (parser == null) {
                parser = new DefaultTableDescriptionParser();
            }
            connectionList = new ArrayList<Connection>();
            statementMap = new HashMap<String, Statement>();
            this.generateStatement();
        }
    }

    /**
     * 执行SQL语句的方法
     * @param param 需要替换到基本SQL语句的参数
     */
    @Override
    public List<Map<String, String>> execute(Map<String, String> param) throws Exception {
        Statement stmt = null;
        if (param.containsKey(SHARD_KEY)) {
            String tableSuffix = router.getSubtableDesc(param.get(param.get(SHARD_KEY)));
            stmt = statementMap.get(tableSuffix);
            param.put(TABLE_SUFFIX_PLACE_HOLDER, tableSuffix);
            param.remove(SHARD_KEY);
        } else {
            if (param.containsKey(DATASOURCE_HOLDER)) {
                stmt = statementMap.get(param.get(DATASOURCE_HOLDER));
                param.remove(DATASOURCE_HOLDER);
            } else {
                throw new Exception("您需要传递一个shardKey或定义一个不分表的数据源");
            }
        }
        String sql = this.buildSql(param);
        if (logger.isDebugEnabled()) {
            logger.debug("即将执行的编译过的SQL语句为===>" + sql);
        }
        ResultSet resultSet = stmt.executeQuery(sql);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columCount = metaData.getColumnCount();
        Map<String, String> dataMap = null;
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        while (resultSet.next()) {
            dataMap = new HashMap<String, String>(columCount);
            for (int i = 1; i <= columCount; i++) {
                String key = null;
                String value = null;
                try {
                    key = metaData.getColumnLabel(i);
                    value = resultSet.getString(i);
                } catch (SQLException e) {
                    throw new Exception("调用next方法失败", e);
                }
                dataMap.put(key, value != null ? value : " ");
            }
            resultList.add(dataMap);
        }
        return resultList;
    }

    public void close() throws Exception {
        if (!this.isInited.get()) {
            logger.warn("尚未初始化，无法调用close方法！");
            return;
        }
        try {
            try {
                if (statementMap.size() != 0) {
                    for (Entry<String, Statement> entry : statementMap.entrySet()) {
                        if (entry.getValue() != null) {
                            try {
                                entry.getValue().close();
                            } catch (SQLException se) {
                                logger.error("statement关闭失败", se);
                            } catch (RuntimeException re) {
                                logger.error("statement关闭时发生运行时异常", re);
                            } finally {
                                statementMap.put(entry.getKey(), null);
                            }
                        }
                    }
                    statementMap.clear();
                }
            } catch (Exception e) {
                logger.error("关闭statement过程发生异常", e);
            } finally {
                if (connectionList.size() != 0) {
                    for (Connection connection : connectionList) {
                        try {
                            connection.close();
                        } catch (SQLException se) {
                            logger.error("关闭connection失败", se);
                        } catch (RuntimeException re) {
                            logger.error("关闭connection时发生运行时异常", re);
                        }
                    }
                    connectionList.clear();
                }
            }
        } finally {
            isInited.set(false);
        }
    }

    /**
     * @param applicationContext
     * @throws BeansException
     * @uml.property  name="applicationContext"
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected String buildSql(Map<String, String> param) {
        String builtSql = this.baseSql;
        for (Entry<String, String> entry : param.entrySet()) {
            builtSql = builtSql.replace(entry.getKey(), entry.getValue());
        }
        return builtSql;
    }

    protected void generateStatement() throws Exception {
        Map<String, List<String>> subtableMaps = null;
        try {
            subtableMaps = parser.parse(subtableDesc);
        } catch (SubtableDescParseException e) {
            logger.error("分表规则解析失败", e);
            throw e;
        }
        Set<String> dataSources = subtableMaps.keySet();
        for (String dsname : dataSources) {
            DataSource dataSource = (DataSource) this.applicationContext.getBean(dsname);
            try {
                Connection connection = dataSource.getConnection();
                this.connectionList.add(connection);
                Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                if (dataSource instanceof JDBCPropertiesSupport && ((JDBCPropertiesSupport) dataSource).getJdbcProperties() != null) {
                    this.setPropertiesBeforeQuery(statement, ((JDBCPropertiesSupport) dataSource).getJdbcProperties());
                }
                if (subtableMaps.get(dsname) != null && subtableMaps.get(dsname).size() == 0) {
                    this.statementMap.put(dsname, statement);
                    continue;
                }
                for (String subtable : subtableMaps.get(dsname)) {
                    this.statementMap.put(subtable, statement);
                }
            } catch (SQLException e) {
                logger.error("生成statement时出现异常", e);
                throw e;
            }
        }
    }

    protected void setPropertiesBeforeQuery(Statement statement, JDBCProperties jdbcProperties) throws SQLException {
        statement.setFetchSize(jdbcProperties.getStatementFetchSize());
    /*statement.setMaxRows(jdbcProperties.getStatementMaxRow());
		statement.setQueryTimeout(jdbcProperties.getStatementQueryTimeout());*/
    }

    /**
     * @return
     * @uml.property  name="baseSql"
     */
    public String getBaseSql() {
        return baseSql;
    }

    /**
     * @param baseSql
     * @uml.property  name="baseSql"
     */
    public void setBaseSql(String baseSql) {
        this.baseSql = baseSql;
    }

    /**
     * @return
     * @uml.property  name="subtableDesc"
     */
    public String getSubtableDesc() {
        return subtableDesc;
    }

    /**
     * @param subtableDesc
     * @uml.property  name="subtableDesc"
     */
    public void setSubtableDesc(String subtableDesc) {
        this.subtableDesc = subtableDesc;
    }

    /**
     * @return
     * @uml.property  name="router"
     */
    public TableRouter getRouter() {
        return router;
    }

    /**
     * @param router
     * @uml.property  name="router"
     */
    public void setRouter(TableRouter router) {
        this.router = router;
    }

    /**
     * @return
     * @uml.property  name="parser"
     */
    public TableDescriptionParser getParser() {
        return parser;
    }

    /**
     * @param parser
     * @uml.property  name="parser"
     */
    public void setParser(TableDescriptionParser parser) {
        this.parser = parser;
    }

    /**
     * @return
     * @uml.property  name="applicationContext"
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void main(String[] args) {
        String baseSql = "SELECT * FROM test$tablename$ WHERE id=#id# OR id=#id2#";
        String subtableDesc = "ds1:0-1";
        ModTableRouter router = new ModTableRouter(2);
        ApplicationContext ac = new FileSystemXmlApplicationContext("d:\\ac.xml");
        DefaultSqlExecutor executor = new DefaultSqlExecutor(baseSql, subtableDesc, router);
        executor.setApplicationContext(ac);
        Map<String, String> param = new HashMap<String, String>();
        param.put("#id#", String.valueOf(15));
        param.put("#id2#", String.valueOf(16));
        param.put(SHARD_KEY, "#id2#");
        try {
            executor.init();
            List<Map<String, String>> results = executor.execute(param);
            for (Map<String, String> result : results) {
                for (Entry<String, String> entry : result.entrySet()) {
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                executor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
