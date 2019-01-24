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
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.common.data.sql.SqlFunction;
import com.qlangtech.tis.common.data.sql.SqlFunctionCollectors;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SingleTableDataProvider extends AbstractDataProvider implements PlusSqlFunctionRegisterable {

    protected static Log logger = LogFactory.getLog(SingleTableDataProvider.class);

    /**
     * @uml.property  name="dataSource"
     */
    protected DataSource dataSource;

    /**
     * @uml.property  name="connection"
     */
    protected Connection connection;

    /**
     * @uml.property  name="statement"
     */
    protected Statement statement;

    /**
     * @uml.property  name="metaData"
     */
    protected ResultSetMetaData metaData;

    /**
     * @uml.property  name="resultSet"
     */
    protected ResultSet resultSet;

    /**
     * @uml.property  name="columCount"
     */
    protected int columCount;

    /**
     * @uml.property  name="jdbcProperties"
     * @uml.associationEnd
     */
    protected JDBCProperties jdbcProperties;

    /**
     * @uml.property  name="sql"
     */
    protected String sql;

    /**
     * @uml.property  name="functionList"
     */
    protected List<SqlFunction> functionList;

    /**
     * @uml.property  name="functionCollectors"
     * @uml.associationEnd
     */
    protected SqlFunctionCollectors functionCollectors;

    // protected Map<String,String> row =
    protected Map<String, String> row;

    @Override
    protected void doInit() throws Exception {
        connect(1);
    }

    private void connect(int i) throws Exception {
        try {
            this.connection = this.dataSource.getConnection();
            if (this.jdbcProperties == null && this.dataSource instanceof JDBCPropertiesSupport) {
                this.jdbcProperties = ((JDBCPropertiesSupport) dataSource).getJdbcProperties();
            }
            this.statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (this.jdbcProperties != null) {
                this.setPropertiesBeforeQuery(this.statement, this.jdbcProperties);
            }
            String executeSql = this.buildSql(sql);
            logger.debug("执行SQL ==> \n" + executeSql);
            if (statement.execute(executeSql)) {
                resultSet = statement.getResultSet();
            } else {
                logger.fatal("执行SQL失败,sql ==> \n" + executeSql);
                resultSet = null;
            }
            metaData = resultSet.getMetaData();
            columCount = metaData.getColumnCount();
        } catch (Exception e) {
            this.close();
            logger.error(e);
            if (i > 5) {
                throw new Exception("SingleTableDataProvider初始化异常,直接释放所有资源，调用close方法.", e);
            }
            connect(++i);
        }
    }

    protected String buildSql(String sql) {
        if (functionCollectors == null) {
            functionCollectors = new SqlFunctionCollectors();
        }
        if (functionList != null && functionList.size() != 0) {
            for (SqlFunction func : functionList) {
                functionCollectors.register(func);
            }
        }
        return functionCollectors.parseSql(sql);
    }

    /**
     * 针对一些JDBC的参数，可在这里实现，用户可覆盖这个方法，想做什么都在这里做吧
     *
     * @param statement
     * @param jdbcProperties
     * @throws SQLException
     */
    protected void setPropertiesBeforeQuery(Statement statement, JDBCProperties jdbcProperties) throws SQLException {
        logger.debug("设置JDBC的相关属性 ==> " + jdbcProperties.toString());
        statement.setFetchSize(jdbcProperties.getStatementFetchSize());
    }

    @Override
    protected void doClose() throws Exception {
        logger.debug("关闭DataProvider,释放数据库连接资源，状态标志归位.");
        try {
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            throw new Exception("释放数据库连接资源失败", e);
        } finally {
            resultSet = null;
            statement = null;
            connection = null;
            metaData = null;
            columCount = 0;
        }
    }

    @Override
    public boolean hasNext() throws Exception {
        return resultSet != null && resultSet.next();
    }

    @Override
    public Map<String, String> next() throws Exception {
        // 此处设置为null，便于GC回收
        if (row != null) {
            row = null;
        }
        row = new HashMap<String, String>(columCount);
        for (int i = 1; i <= columCount; i++) {
            String key = null;
            String value = null;
            try {
                key = metaData.getColumnLabel(i);
                value = resultSet.getString(i);
            } catch (SQLException e) {
                if (ignoreIllFieldRow) {
                    throw new Exception("调用next方法失败", e);
                } else {
                    row.put(key, defaultIllFiledValue);
                    continue;
                }
            }
            row.put(key, value != null ? value : " ");
        }
        return row;
    }

    /**
     * @uml.property  name="ignoreIllFieldRow"
     */
    private boolean ignoreIllFieldRow = true;

    /**
     * @uml.property  name="defaultIllFiledValue"
     */
    private String defaultIllFiledValue = null;

    /**
     * @return
     * @uml.property  name="sql"
     */
    public String getSql() {
        return sql;
    }

    /**
     * @param sql
     * @uml.property  name="sql"
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * @return
     * @uml.property  name="connection"
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @param connection
     * @uml.property  name="connection"
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * @return
     * @uml.property  name="statement"
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     * @param statement
     * @uml.property  name="statement"
     */
    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    /**
     * @return
     * @uml.property  name="resultSet"
     */
    public ResultSet getResultSet() {
        return resultSet;
    }

    /**
     * @param resultSet
     * @uml.property  name="resultSet"
     */
    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    /**
     * @return
     * @uml.property  name="metaData"
     */
    public ResultSetMetaData getMetaData() {
        return metaData;
    }

    /**
     * @param metaData
     * @uml.property  name="metaData"
     */
    public void setMetaData(ResultSetMetaData metaData) {
        this.metaData = metaData;
    }

    /**
     * @return
     * @uml.property  name="columCount"
     */
    public int getColumCount() {
        return columCount;
    }

    /**
     * @param columCount
     * @uml.property  name="columCount"
     */
    public void setColumCount(int columCount) {
        this.columCount = columCount;
    }

    /**
     * @return
     * @uml.property  name="dataSource"
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource
     * @uml.property  name="dataSource"
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return
     * @uml.property  name="jdbcProperties"
     */
    public JDBCProperties getJdbcProperties() {
        return jdbcProperties;
    }

    /**
     * @param jdbcProperties
     * @uml.property  name="jdbcProperties"
     */
    public void setJdbcProperties(JDBCProperties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }

    /**
     * @return
     * @uml.property  name="functionList"
     */
    public List<SqlFunction> getFunctionList() {
        return functionList;
    }

    /**
     * @param functionList
     * @uml.property  name="functionList"
     */
    public void setFunctionList(List<SqlFunction> functionList) {
        this.functionList = functionList;
    }

    /**
     * @return
     * @uml.property  name="functionCollectors"
     */
    public SqlFunctionCollectors getFunctionCollectors() {
        return functionCollectors;
    }

    /**
     * @param functionCollectors
     * @uml.property  name="functionCollectors"
     */
    public void setFunctionCollectors(SqlFunctionCollectors functionCollectors) {
        this.functionCollectors = functionCollectors;
    }

    /**
     * @return
     * @uml.property  name="ignoreIllFieldRow"
     */
    public boolean isIgnoreIllFieldRow() {
        return ignoreIllFieldRow;
    }

    /**
     * @param ignoreIllFieldRow
     * @uml.property  name="ignoreIllFieldRow"
     */
    public void setIgnoreIllFieldRow(boolean ignoreIllFieldRow) {
        this.ignoreIllFieldRow = ignoreIllFieldRow;
    }

    /**
     * @return
     * @uml.property  name="defaultIllFiledValue"
     */
    public String getDefaultIllFiledValue() {
        return defaultIllFiledValue;
    }

    /**
     * @param defaultIllFiledValue
     * @uml.property  name="defaultIllFiledValue"
     */
    public void setDefaultIllFiledValue(String defaultIllFiledValue) {
        this.defaultIllFiledValue = defaultIllFiledValue;
    }

    @Override
    public void unregisterAll() {
        if (this.functionList == null)
            return;
        this.functionList.clear();
    }

    @Override
    public void registerSqlFunction(SqlFunction sqlFunction) {
        if (functionList == null) {
            this.functionList = new ArrayList<SqlFunction>();
        }
        this.functionList.add(sqlFunction);
    }

    @Override
    public void unregisterSqlFunction(String name) {
    }
}
