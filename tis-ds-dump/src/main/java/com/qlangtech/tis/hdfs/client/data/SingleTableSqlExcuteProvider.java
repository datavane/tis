/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.hdfs.client.data;

import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对于一个表的SQL操作，如果本表操作完毕则依次执行 nextDataProvider的表SQL操作<br>
 * 【注意】该类不适用多线程操作<br>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class SingleTableSqlExcuteProvider extends AbstractSourceDataProvider {

    //protected static Logger log = LoggerFactory.getLogger(SingleTableSqlExcuteProvider.class);

    private final AtomicInteger dbReaderCounter;

    // 需要导入的表的名称
    private String tableName;

    // private TISTable tabMeta;
    private final AtomicBoolean hasOpen = new AtomicBoolean(false);

    /**
     * @param dbReaderCounter
     */
    public SingleTableSqlExcuteProvider(AtomicInteger dbReaderCounter) {
        super();
        this.dbReaderCounter = dbReaderCounter;
    }

    // public void setTabMeta(TISTable tabMeta) {
    // this.tabMeta = tabMeta;
    // }
//    @Override
//    public String getDsName() {
//        return this.dsName;
//    }

    // public DumpTable logicTable;
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


    protected String selfFlag;

    protected int executeRowCount = 0;

    // protected String shardKey = "id";

//    @Override
//    public String getDbHost() {
//        return this.dbHost;
//    }

    public final CurrentOperatorLocation currentOperatorLocation = new CurrentOperatorLocation();

    // 求RowCount不使用select count(1) from XXX 这样可以提高效率
//    @Override
//    public int getRowSize() {
//        Connection connection = null;
//        Statement statement = null;
//        ResultSet result = null;
//        try {
//            connection = this.dataSource.getConnection();
//            StringBuffer refactSql = parseRowCountSql();
//            statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
//            result = statement.executeQuery(refactSql.toString());
//            result.last();
//            final int rowSize = result.getRow();
//            // this.resultSet.beforeFirst();
//            return rowSize;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                result.close();
//            } catch (Throwable e) {
//            }
//            try {
//                statement.close();
//            } catch (Throwable e) {
//            }
//            try {
//                connection.close();
//            } catch (Throwable e) {
//            }
//        }
//    }

    private static final Map<String, StringBuffer> rowCountSqlMap = new HashMap<>();

//    private StringBuffer parseRowCountSql() {
//        String executeSql = this.getFinalSql(getSql());
//        StringBuffer refactSql = null;
//        if ((refactSql = rowCountSqlMap.get(executeSql)) == null) {
//            synchronized (rowCountSqlMap) {
//                if ((refactSql = rowCountSqlMap.get(executeSql)) == null) {
//                    // final DumpTable logicTable = dumpContext.getDumpTable();
//                    TISTable tab = this.dumpContext.getTisTable();
//                    // Objects.requireNonNull(tabMeta, "tabMeta can not be null");
//                    // tabMeta = GitUtils.$().getTableConfig(logicTable.getDbName(), logicTable.getTableName());
//                    refactSql = new StringBuffer("SELECT 1 FROM ");
//                    refactSql.append(tab.getTableName());
//                    // FIXME where 先缺省以后加上
//                    StringBuffer where = null;
//                    if (where != null) {
//                        refactSql.append(" ").append(where.toString());
//                    }
//                    //log.info("rowCountSql:" + refactSql);
//                    rowCountSqlMap.put(executeSql, refactSql);
//                }
//            }
//        }
//        return refactSql;
//    }

//    @Override
//    public boolean hasNext() throws SourceDataReadException {
//        try {
//            //
//            if (resultSet != null && resultSet.next()) {
//                // 读取记录数++
//                dbReaderCounter.incrementAndGet();
//                return true;
//            }
//            return false;
//        } catch (SQLException e) {
//            throw new SourceDataReadException("【注意】操作数据源:" + dsName + ",表后缀为： " + suffixTableName + "出现错误：", e);
//        }
//    }

//    public void openResource() throws SourceDataReadException {
//        if (hasOpen.get()) {
//            return;
//        }
//        try {
//            this.connection = this.dataSource.getConnection();
//            this.currentOperatorLocation.dsName = dsName;
//            this.currentOperatorLocation.suffixTableName = suffixTableName;
//            //  this.currentOperatorLocation.shardKey = shardKey;
//
//            this.statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//
//            String executeSql = this.getFinalSql(getSql());
//            // log.warn("执行SQL ==> \n" + executeSql);
//            try {
//                resultSet = statement.executeQuery(executeSql);
//            } catch (SQLException e) {
//                //log.error("executeSQL faild,sql ==> \n" + executeSql, e);
//                resultSet = null;
//            }
//            metaData = resultSet.getMetaData();
//            columCount = metaData.getColumnCount();
//            hasOpen.set(true);
//        } catch (Exception e) {
//            this.closeResource(false);
//            throw new SourceDataReadException("can not create connection:[" + this.getDbHost() + "," + this.getDsName()
//                    + "," + this.tableName + ",suffixTableName:" + this.suffixTableName + "]", e);
//        }
//    }

    private String getFinalSql(String sql) {
        return sql;
    }

//    public List<ColumnMetaData> getMetaData() {
//        List<ColumnMetaData> result = new ArrayList<ColumnMetaData>();
//        try {
//            for (int i = 1; i <= columCount; i++) {
//                result.add(new ColumnMetaData((i - 1), metaData.getColumnLabel(i), metaData.getColumnType(i), false));
//            }
//            return result;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    @Override
//    public Map<String, String> next() throws SourceDataReadException {
//        // if (row != null)
//        // row = null;
//        // // if (!mustNext) {
//        //
//        row = new LinkedHashMap<String, String>(columCount);
//        for (int i = 1; i <= columCount; i++) {
//            String key = null;
//            String value = null;
//            try {
//                key = metaData.getColumnLabel(i);
//                // 防止特殊字符造成HDFS文本文件出现错误
//                value = FormatTool.filter(resultSet, i);
//                // value= resultSet.getString(i);
//            } catch (SQLException e) {
//                throw new SourceDataReadException("出现错误,table:" + this.tableName, e);
//                // } else {
//                // row.put(key, defaultIllFiledValue);
//                // continue;
//                // }
//            }
//            // 在数据来源为数据库情况下，客户端提供一行的数据对于Solr来说是一个Document
//            row.put(key, value != null ? value : "");
//        }
//        return row;
//        // } else {
//        // // return nextDataProvider.next();
//        // }
//    }

    // private boolean ignoreIllFieldRow = true;
    // private String defaultIllFiledValue = null;
//    public void closeResource(boolean isIteratorr) throws SourceDataReadException {
//        try {
//            if (resultSet != null)
//                resultSet.close();
//            if (statement != null)
//                statement.close();
//            if (connection != null)
//                connection.close();
//            hasOpen.set(false);
//        } catch (SQLException e) {
//            log.error("dbsuffix:" + this.suffixTableName, e);
//            throw new SourceDataReadException("release db error", e);
//        } finally {
//            resultSet = null;
//            statement = null;
//            connection = null;
//            metaData = null;
//            columCount = 0;
//            executeRowCount = 0;
//        }
//    }

    public static class CurrentOperatorLocation {

        public String dsName;

        public String suffixTableName;

        //  public String shardKey;
    }

//    @Override
//    public String getShardKey() {
//        return this.shardKey;
//    }

//    @Override
//    public void closeResource() throws SourceDataReadException {
//        this.closeResource(true);
//    }
//
//    @Override
//    public void init() throws SourceDataReadException {
//        if (dbReaderCounter == null) {
//            throw new IllegalStateException("dbReaderCounter can not be null");
//        }
//    }

    protected String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        try {
            this.tableName = StringUtils.substringAfterLast(sql.toLowerCase(), "from");
            this.tableName = StringUtils.trim(StringUtils.substringBefore(this.tableName, "where"));
        } catch (Throwable e) {
        }
        this.sql = sql;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
