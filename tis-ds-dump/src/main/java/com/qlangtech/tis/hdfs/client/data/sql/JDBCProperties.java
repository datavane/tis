///**
// * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
// *
// * This program is free software: you can use, redistribute, and/or modify
// * it under the terms of the GNU Affero General Public License, version 3
// * or later ("AGPL"), as published by the Free Software Foundation.
// *
// * This program is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package com.qlangtech.tis.hdfs.client.data.sql;
//
//import java.util.Properties;
//
///**
// * @description
// * @since  2011-8-30 03:13:55
// * @version  1.0
// *
// * @author 百岁（baisui@qlangtech.com）
// * @date 2020/04/13
// */
//public class JDBCProperties extends Properties {
//
//    private static final long serialVersionUID = -7921975974894453161L;
//
//    public static final int MYSQL_STATEMENT_MAX_ROW = 50000;
//
//    public static final int MYSQL_STATEMENT_QUERY_TIMEOUT = 3000;
//
//    // Integer.MIN_VALUE;
//    public static final int MYSQL_STATEMENT_FETCH_SIZE = 5000;
//
//    public static final int ORACLE_STATEMENT_MAX_ROW = 50000;
//
//    public static final int ORACLE_STATEMENT_QUERY_TIMEOUT = 3000;
//
//    public static final int ORACLE_STATEMENT_FETCH_SIZE = 5000;
//
//    /**
//     * @uml.property name="sTATEMENT_MAX_ROW"
//     */
//    public static final String STATEMENT_MAX_ROW = "maxRow";
//
//    /**
//     * @uml.property name="sTATEMENT_QUERY_TIMEOUT"
//     */
//    public static final String STATEMENT_QUERY_TIMEOUT = "queryTimeout";
//
//    /**
//     * @uml.property name="sTATEMENT_FETCH_SIZE"
//     */
//    public static final String STATEMENT_FETCH_SIZE = "fetchSize";
//
//    public static final String MYSQL_DBMS_NAME = "MySQL";
//
//    public static final String ORACLE_DBMS_NAME = "Oracle";
//
//    /**
//     * @uml.property name="type"
//     * @uml.associationEnd
//     */
//    protected DBMSType type;
//
//    public JDBCProperties(DBMSType type) {
//        super();
//        this.type = type;
//        this.init();
//    }
//
//    public JDBCProperties(DBMSType type, Properties defaults) {
//        super(defaults);
//        this.type = type;
//        this.init();
//    }
//
//    /**
//     * @return
//     * @uml.property name="sTATEMENT_MAX_ROW"
//     */
//    public int getStatementMaxRow() {
//        String strMaxRow = this.getProperty(STATEMENT_MAX_ROW);
//        int maxRow = 0;
//        try {
//            maxRow = Integer.parseInt(strMaxRow);
//        } catch (NumberFormatException nfe) {
//            maxRow = this.type == DBMSType.MYSQL ? MYSQL_STATEMENT_MAX_ROW : ORACLE_STATEMENT_MAX_ROW;
//        }
//        return maxRow;
//    }
//
//    /**
//     * @return
//     * @uml.property name="sTATEMENT_QUERY_TIMEOUT"
//     */
//    public int getStatementQueryTimeout() {
//        String strQueryTimeout = this.getProperty(STATEMENT_QUERY_TIMEOUT);
//        int queryTimeout = 0;
//        try {
//            queryTimeout = Integer.parseInt(strQueryTimeout);
//        } catch (NumberFormatException nfe) {
//            queryTimeout = this.type == DBMSType.MYSQL ? MYSQL_STATEMENT_QUERY_TIMEOUT : ORACLE_STATEMENT_QUERY_TIMEOUT;
//        }
//        return queryTimeout;
//    }
//
//    /**
//     * 返回用户设置的fetchSize值，如果不能转化为int，则返回默认值
//     *
//     * @return
//     * @uml.property name="sTATEMENT_FETCH_SIZE"
//     */
//    public int getStatementFetchSize() {
//        return MYSQL_STATEMENT_FETCH_SIZE;
//    // String strFetchSize = this.getProperty(STATEMENT_FETCH_SIZE);
//    // int fetchSize = 0;
//    // try {
//    // fetchSize = Integer.parseInt(strFetchSize);
//    // } catch(NumberFormatException nfe) {
//    // fetchSize = this.type==DBMSType.MYSQL? MYSQL_STATEMENT_FETCH_SIZE: ORACLE_STATEMENT_FETCH_SIZE;
//    // }
//    // fetchSize = fetchSize < 0 ? Integer.MIN_VALUE : fetchSize;
//    // this.put(STATEMENT_FETCH_SIZE, fetchSize);
//    // return fetchSize;
//    }
//
//    /**
//     * 返回本Properties的DBMS类型
//     *
//     * @return 本Properties的DBMS类型
//     * @uml.property name="type"
//     */
//    public DBMSType getType() {
//        return type;
//    }
//
//    /**
//     * 返回本Properties的DBMS名称
//     *
//     * @return 本Properties的DBMS名称
//     */
//    public String getDBMSName() {
//        switch(this.type) {
//            case ORACLE:
//                return ORACLE_DBMS_NAME;
//            default:
//                return MYSQL_DBMS_NAME;
//        }
//    }
//
//    public static enum DBMSType {
//
//        /**
//         * @uml.property name="mYSQL"
//         * @uml.associationEnd
//         */
//        MYSQL,
//        /**
//         * @uml.property name="oRACLE"
//         * @uml.associationEnd
//         */
//        ORACLE;
//
//        public static DBMSType getDBMSTpye(String dbName) {
//            if ("oracle".equalsIgnoreCase(dbName))
//                return ORACLE;
//            else
//                return MYSQL;
//        }
//    }
//
//    /**
//     * 初始化设置，先把默认值根据DBMS类型加进去
//     */
//    private void init() {
//        if (this.type == DBMSType.ORACLE) {
//            this.put(STATEMENT_MAX_ROW, ORACLE_STATEMENT_MAX_ROW);
//            this.put(STATEMENT_QUERY_TIMEOUT, ORACLE_STATEMENT_QUERY_TIMEOUT);
//            this.put(STATEMENT_FETCH_SIZE, ORACLE_STATEMENT_FETCH_SIZE);
//        } else {
//            this.put(STATEMENT_MAX_ROW, MYSQL_STATEMENT_MAX_ROW);
//            this.put(STATEMENT_QUERY_TIMEOUT, MYSQL_STATEMENT_QUERY_TIMEOUT);
//            this.put(STATEMENT_FETCH_SIZE, MYSQL_STATEMENT_FETCH_SIZE);
//        }
//    }
//}
