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

package com.qlangtech.tis.plugin.ds;

import com.google.common.collect.Sets;
import com.qlangtech.tis.db.parser.DBConfigParser;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-06 19:48
 **/
public abstract class BasicDataSourceFactory extends DataSourceFactory {

    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.identity})
    public String name;

    // 数据库名称
    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.identity})
    public String dbName;

    @FormField(ordinal = 2, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.identity})
    public String userName;

    @FormField(ordinal = 3, type = FormFieldType.PASSWORD, validate = {})
    public String password;

    @FormField(ordinal = 4, type = FormFieldType.INT_NUMBER, validate = {Validator.require, Validator.integer})
    public int port;
    /**
     * 数据库编码
     */
    @FormField(ordinal = 5, type = FormFieldType.ENUM, validate = {Validator.require, Validator.identity})
    public String encode;
    /**
     * 附加参数
     */
    @FormField(ordinal = 5, type = FormFieldType.INPUTTEXT)
    public String extraParams;
    /**
     * 节点描述
     */
    @FormField(ordinal = 6, type = FormFieldType.TEXTAREA, validate = {Validator.require})
    public String nodeDesc;


    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }


    @Override
    public List<ColumnMetaData> getTableMetadata(final String table) {
        if (StringUtils.isBlank(table)) {
            throw new IllegalArgumentException("param table can not be null");
        }
        List<ColumnMetaData> columns = new ArrayList<>();
        try {

            final DBConfig dbConfig = getDbConfig();
            dbConfig.vistDbName((config, ip, dbname) -> {
                visitConnection(config, ip, dbname, config.getUserName(), config.getPassword(), (conn) -> {
                    DatabaseMetaData metaData1 = null;
                    ResultSet primaryKeys = null;
                    ResultSet columns1 = null;
                    try {
                        metaData1 = conn.getMetaData();
                        primaryKeys = metaData1.getPrimaryKeys(null, null, table);
                        columns1 = metaData1.getColumns(null, null, table, null);
                        Set<String> pkCols = Sets.newHashSet();
                        while (primaryKeys.next()) {
                            // $NON-NLS-1$
                            String columnName = primaryKeys.getString("COLUMN_NAME");
                            pkCols.add(columnName);
                        }
                        int i = 0;
                        String colName = null;
                        while (columns1.next()) {
                            columns.add(new ColumnMetaData((i++), (colName = columns1.getString("COLUMN_NAME"))
                                    , columns1.getInt("DATA_TYPE"), pkCols.contains(colName)));
                        }

                    } finally {
                        closeResultSet(columns1);
                        closeResultSet(primaryKeys);
                    }
                });
                return true;
            });
            return columns;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
                ;
            }
        }
    }

    @Override
    public final List<String> getTablesInDB() {
        try {
            final List<String> tabs = new ArrayList<>();

            final DBConfig dbConfig = getDbConfig();

            dbConfig.vistDbName((config, ip, databaseName) -> {
                visitConnection(config, ip, databaseName, config.getUserName(), config.getPassword(), (conn) -> {
                    Statement statement = null;
                    ResultSet resultSet = null;
                    try {
                        statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                        statement.execute("show tables");
                        resultSet = statement.getResultSet();
                        while (resultSet.next()) {
                            tabs.add(resultSet.getString(1));
                        }
                    } finally {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                        if (statement != null) {
                            statement.close();
                        }
                    }
                });
                return true;
            });
            return tabs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected DBConfig getDbConfig() {
        final DBConfig dbConfig = new DBConfig();
        dbConfig.setName(this.dbName);
        dbConfig.setPassword(this.password);
        dbConfig.setUserName(this.userName);
        dbConfig.setPort(this.port);
        dbConfig.setDbEnum(DBConfigParser.parseDBEnum(dbName, this.nodeDesc));
        return dbConfig;
    }

//    @Override
//    public String getName() {
//        if (StringUtils.isEmpty(this.dbName)) {
//            throw new IllegalStateException("prop dbName can not be null");
//        }
//        return this.dbName;
//    }


    private void visitConnection(DBConfig db, String ip, String dbName
            , String username, String password, IConnProcessor p) throws Exception {
        if (db == null) {
            throw new IllegalStateException("param db can not be null");
        }
        if (StringUtils.isEmpty(ip)) {
            throw new IllegalArgumentException("param ip can not be null");
        }
        if (StringUtils.isEmpty(dbName)) {
            throw new IllegalArgumentException("param dbName can not be null");
        }
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("param username can not be null");
        }
//        if (StringUtils.isEmpty(password)) {
//            throw new IllegalArgumentException("param password can not be null");
//        }
        if (p == null) {
            throw new IllegalArgumentException("param IConnProcessor can not be null");
        }
        //Connection conn = null;
        String jdbcUrl = buidJdbcUrl(db, ip, dbName);
        try {
            validateConnection(jdbcUrl, db, username, password, p);
        } catch (Exception e) {
            //MethodHandles.lookup().lookupClass()
            throw new TisException("请确认插件:" + this.getClass().getSimpleName() + "配置:" + this.identityValue() + ",jdbcUrl:" + jdbcUrl, e);
        }
    }

    protected abstract String buidJdbcUrl(DBConfig db, String ip, String dbName);

    protected static void validateConnection(String jdbcUrl, DBConfig db, String username, String password, IConnProcessor p) {
        Connection conn = null;
        try {
            conn = getConnection(jdbcUrl, username, password);
            p.vist(conn);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    protected static Connection getConnection(String jdbcUrl, String username, String password) throws SQLException {
        // 密码可以为空
        return DriverManager.getConnection(jdbcUrl, username, StringUtils.trimToNull(password));
    }

    public interface IConnProcessor {
        void vist(Connection conn) throws SQLException;
    }

}
