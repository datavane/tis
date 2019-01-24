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
package com.qlangtech.tis.hdfs.client.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.hdfs.client.data.sql.JDBCProperties;
import com.qlangtech.tis.hdfs.client.data.sql.JDBCPropertiesSupport;
import com.qlangtech.tis.hdfs.client.data.sql.SqlFunctionCollectors;
import com.qlangtech.tis.hdfs.util.FormatTool;

/*
 * @description 对于一个表的SQL操作，如果本表操作完毕则依次执行 nextDataProvider的表SQL操作<br>
 *              【注意】该类不适用多线程操作<br>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SingleTableSqlExcuteProvider extends AbstractSourceDataProvider {

    protected static Log log = LogFactory.getLog(SingleTableSqlExcuteProvider.class);

    private final AtomicLong dbReaderCounter;

    // 需要导入的表的名称
    private String tableName;

    /**
     * @param dbReaderCounter
     */
    public SingleTableSqlExcuteProvider(AtomicLong dbReaderCounter) {
        super();
        this.dbReaderCounter = dbReaderCounter;
    }

    @Override
    public String getDsName() {
        return this.dsName;
    }

    // public SingleTableSqlExcuteProvider nextDataProvider;
    public String dsName;

    public String dbHost;

    public String suffixTableName;

    protected DataSource dataSource;

    protected Connection connection;

    protected Statement statement;

    protected ResultSetMetaData metaData;

    protected ResultSet resultSet;

    protected Map<String, String> row;

    protected int columCount;

    private String sql;

    // protected boolean mustNext = false;
    protected SqlFunctionCollectors sqlFuncs;

    protected JDBCProperties jdbcProperties;

    protected String selfFlag;

    protected int executeRowCount = 0;

    // protected boolean isTest;
    protected String shardKey = "id";

    @Override
    public String getDbHost() {
        return this.dbHost;
    }

    public final CurrentOperatorLocation currentOperatorLocation = new CurrentOperatorLocation();

    @Override
    public boolean hasNext() throws SourceDataReadException {
        // boolean flag = false;
        try {
            // 
            if (resultSet != null && resultSet.next()) {
                dbReaderCounter.incrementAndGet();
                return true;
            }
            return false;
        // if (isTest && executeRowCount++ > 2) {
        // flag = false;
        // }
        // 
        } catch (SQLException e) {
            throw new SourceDataReadException("【注意】操作数据源:" + dsName + ",表后缀为： " + suffixTableName + "出现错误：", e);
        }
    // if (!flag) {
    // if (this.hasNextDatProvider()) {
    // if (!mustNext) {
    // /**
    // * 进入下一个Provider,那么打开下一个Provider的资源连接, 同时关闭当前Provider的资源连接
    // */
    // this.getNextDataProvider().openResource();
    // this.closeResource(false);
    // mustNext = true;
    // }
    // flag = this.getNextDataProvider().hasNext();
    // }
    // //}
    // 
    // return flag;
    }

    public void openResource() throws SourceDataReadException {
        try {
            this.connection = this.dataSource.getConnection();
            this.currentOperatorLocation.dsName = dsName;
            this.currentOperatorLocation.suffixTableName = suffixTableName;
            this.currentOperatorLocation.shardKey = shardKey;
            if (this.jdbcProperties == null && this.dataSource instanceof JDBCPropertiesSupport) {
                this.jdbcProperties = ((JDBCPropertiesSupport) dataSource).getJdbcProperties();
            }
            this.statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (this.jdbcProperties != null) {
                this.setPropertiesBeforeQuery(this.statement, this.jdbcProperties);
            }
            String executeSql = this.getFinalSql(getSql());
            if (statement.execute(executeSql)) {
                resultSet = statement.getResultSet();
            } else {
                log.fatal("执行SQL失败,sql ==> \n" + executeSql);
                resultSet = null;
            }
            // 
            // connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            // resultSet.last();
            // resultSet.getRow();
            // resultSet.first(); 移动到第一条
            metaData = resultSet.getMetaData();
            columCount = metaData.getColumnCount();
        } catch (Exception e) {
            this.closeResource(false);
            throw new SourceDataReadException("can not create connection:[" + this.getDbHost() + "," + this.getDsName() + "," + this.tableName + this.suffixTableName + "],\n" + ToStringBuilder.reflectionToString(this.dataSource, ToStringStyle.MULTI_LINE_STYLE), e);
        }
    }

    /**
     * 针对一些JDBC的参数，可在这里实现，用户可覆盖这个方法，想做什么都在这里做吧 *
     *
     * @param statement
     * @param jdbcProperties
     * @throws SQLException
     */
    protected void setPropertiesBeforeQuery(Statement statement, JDBCProperties jdbcProperties) throws SQLException {
        // log.warn("设置JDBC的相关属性 ==> " + jdbcProperties.toString());
        statement.setFetchSize(jdbcProperties.getStatementFetchSize());
    }

    private String getFinalSql(String sql) {
        // sqlFuncs.register( sqlFuncs.new StartDateFunction(isInrc));
        return sqlFuncs.parseSql(sql);
    }

    public List<RowMetaData> getMetaData() {
        List<RowMetaData> result = new ArrayList<RowMetaData>();
        try {
            for (int i = 1; i <= columCount; i++) {
                result.add(new RowMetaData((i - 1), metaData.getColumnLabel(i), metaData.getColumnType(i)));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public class RowMetaData {

        private final String key;

        private final int type;

        private final int index;

        /**
         * @param key
         * @param type
         */
        public RowMetaData(int index, String key, int type) {
            super();
            this.key = key;
            this.type = type;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public String getKey() {
            return key;
        }

        public int getType() {
            return type;
        }
    }

    @Override
    public Map<String, String> next() throws SourceDataReadException {
        // if (row != null)
        // row = null;
        // // if (!mustNext) {
        // 
        row = new LinkedHashMap<String, String>(columCount);
        for (int i = 1; i <= columCount; i++) {
            String key = null;
            String value = null;
            try {
                key = metaData.getColumnLabel(i);
                // 防止特殊字符造成HDFS文本文件出现错误
                value = FormatTool.filter(resultSet, i);
            // value= resultSet.getString(i);
            } catch (SQLException e) {
                // if (ignoreIllFieldRow) {
                throw new SourceDataReadException("出现错误,table:" + this.tableName, e);
            // } else {
            // row.put(key, defaultIllFiledValue);
            // continue;
            // }
            }
            // 在数据来源为数据库情况下，客户端提供一行的数据对于Solr来说是一个Document
            row.put(key, value != null ? value : "");
        }
        return row;
    // } else {
    // // return nextDataProvider.next();
    // }
    }

    // private boolean ignoreIllFieldRow = true;
    // private String defaultIllFiledValue = null;
    public void closeResource(boolean isIterator) throws SourceDataReadException {
        // if (isIterator)
        // log.warn("嵌套关闭后缀名为" + this.suffixTableName + "的数据库资源");
        // else {
        // log.warn("关闭本Provider后缀名为" + this.suffixTableName + "的数据库资源");
        // }
        log.info("db:" + this.getDsName() + ",table:" + this.tableName + ",import:" + this.dbReaderCounter.get());
        try {
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            log.error("关闭本Provider后缀名为" + this.suffixTableName + "的数据库资源出现致命错误", e);
            throw new SourceDataReadException("释放数据库连接资源失败", e);
        } finally {
            resultSet = null;
            statement = null;
            connection = null;
            metaData = null;
            columCount = 0;
            executeRowCount = 0;
        }
    }

    public static class CurrentOperatorLocation {

        public String dsName;

        public String suffixTableName;

        public String shardKey;
    }

    public static void main(String[] args) {
    }

    @Override
    public String getShardKey() {
        return this.shardKey;
    }

    @Override
    public void closeResource() throws SourceDataReadException {
        this.closeResource(true);
    }

    @Override
    public void init() throws SourceDataReadException {
        if (dbReaderCounter == null) {
            throw new IllegalStateException("dbReaderCounter can not be null");
        }
    }

    protected String getSql() {
        return sql;
    }

    protected void setSql(String sql) {
        try {
            this.tableName = StringUtils.substringAfterLast(sql.toLowerCase(), "from");
            this.tableName = StringUtils.trim(StringUtils.substringBefore(this.tableName, "where"));
        } catch (Throwable e) {
        }
        this.sql = sql;
    }
}
