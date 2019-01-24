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

import java.util.Properties;

/*
 * 扩展{ @link Properties }，使之可以支持一些数据源的默认参数设置等操作
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JDBCProperties extends Properties {

    private static final long serialVersionUID = -7921975974894453161L;

    public static final int MYSQL_STATEMENT_MAX_ROW = 50000;

    public static final int MYSQL_STATEMENT_QUERY_TIMEOUT = 3000;

    public static final int MYSQL_STATEMENT_FETCH_SIZE = Integer.MIN_VALUE;

    public static final int ORACLE_STATEMENT_MAX_ROW = 50000;

    public static final int ORACLE_STATEMENT_QUERY_TIMEOUT = 3000;

    public static final int ORACLE_STATEMENT_FETCH_SIZE = 5000;

    /**
     * @uml.property  name="sTATEMENT_MAX_ROW"
     */
    public static final String STATEMENT_MAX_ROW = "maxRow";

    /**
     * @uml.property  name="sTATEMENT_QUERY_TIMEOUT"
     */
    public static final String STATEMENT_QUERY_TIMEOUT = "queryTimeout";

    /**
     * @uml.property  name="sTATEMENT_FETCH_SIZE"
     */
    public static final String STATEMENT_FETCH_SIZE = "fetchSize";

    public static final String MYSQL_DBMS_NAME = "MySQL";

    public static final String ORACLE_DBMS_NAME = "Oracle";

    /**
     * @uml.property  name="type"
     * @uml.associationEnd
     */
    protected DBMSType type;

    /**
     * 用于存储DataSource的附加属性
     * @param type 所用DBMS类型
     */
    public JDBCProperties(DBMSType type) {
        super();
        this.type = type;
        this.init();
    }

    /**
     * 用于存储DataSource的附加属性
     * @param type 所用DBMS类型
     * @param defaults 默认值
     */
    public JDBCProperties(DBMSType type, Properties defaults) {
        super(defaults);
        this.type = type;
        this.init();
    }

    /**
     * 返回用户设置的MaxRow值，如果不能转化为int，则返回默认值
     * @return  maxRow的值
     * @uml.property  name="sTATEMENT_MAX_ROW"
     */
    public int getStatementMaxRow() {
        String strMaxRow = this.getProperty(STATEMENT_MAX_ROW);
        int maxRow = 0;
        try {
            maxRow = Integer.parseInt(strMaxRow);
        } catch (NumberFormatException nfe) {
            maxRow = this.type == DBMSType.MYSQL ? MYSQL_STATEMENT_MAX_ROW : ORACLE_STATEMENT_MAX_ROW;
        }
        return maxRow;
    }

    /**
     * 返回用户设置的queryTimeout值，如果不能转化为int，则返回默认值
     * @return
     * @uml.property  name="sTATEMENT_QUERY_TIMEOUT"
     */
    public int getStatementQueryTimeout() {
        String strQueryTimeout = this.getProperty(STATEMENT_QUERY_TIMEOUT);
        int queryTimeout = 0;
        try {
            queryTimeout = Integer.parseInt(strQueryTimeout);
        } catch (NumberFormatException nfe) {
            queryTimeout = this.type == DBMSType.MYSQL ? MYSQL_STATEMENT_QUERY_TIMEOUT : ORACLE_STATEMENT_QUERY_TIMEOUT;
        }
        return queryTimeout;
    }

    /**
     * 返回用户设置的fetchSize值，如果不能转化为int，则返回默认值
     * @return
     * @uml.property  name="sTATEMENT_FETCH_SIZE"
     */
    public int getStatementFetchSize() {
        String strFetchSize = this.getProperty(STATEMENT_FETCH_SIZE);
        int fetchSize = 0;
        try {
            fetchSize = Integer.parseInt(strFetchSize);
        } catch (NumberFormatException nfe) {
            fetchSize = this.type == DBMSType.MYSQL ? MYSQL_STATEMENT_FETCH_SIZE : ORACLE_STATEMENT_FETCH_SIZE;
        }
        fetchSize = fetchSize < 0 ? Integer.MIN_VALUE : fetchSize;
        this.put(STATEMENT_FETCH_SIZE, fetchSize);
        return fetchSize;
    }

    /**
     * 返回本Properties的DBMS类型
     * @return  本Properties的DBMS类型
     * @uml.property  name="type"
     */
    public DBMSType getType() {
        return type;
    }

    /**
     * 返回本Properties的DBMS名称
     * @return 本Properties的DBMS名称
     */
    public String getDBMSName() {
        switch(this.type) {
            case ORACLE:
                return ORACLE_DBMS_NAME;
            default:
                return MYSQL_DBMS_NAME;
        }
    }

    /**
     * DBMS类型，  {@value DBMSType#mysql}  为MySQL（默认值），  {@value DBMSType#oracle}  为Oracle
     */
    public static enum DBMSType {

        /**
         * @uml.property  name="mYSQL"
         * @uml.associationEnd
         */
        MYSQL,
        /**
         * @uml.property  name="oRACLE"
         * @uml.associationEnd
         */
        ORACLE;

        public static DBMSType getDBMSTpye(String dbName) {
            if ("oracle".equalsIgnoreCase(dbName))
                return ORACLE;
            else
                return MYSQL;
        }
    }

    /**
     * 初始化设置，先把默认值根据DBMS类型加进去
     */
    private void init() {
        if (this.type == DBMSType.ORACLE) {
            this.put(STATEMENT_MAX_ROW, ORACLE_STATEMENT_MAX_ROW);
            this.put(STATEMENT_QUERY_TIMEOUT, ORACLE_STATEMENT_QUERY_TIMEOUT);
            this.put(STATEMENT_FETCH_SIZE, ORACLE_STATEMENT_FETCH_SIZE);
        } else {
            this.put(STATEMENT_MAX_ROW, MYSQL_STATEMENT_MAX_ROW);
            this.put(STATEMENT_QUERY_TIMEOUT, MYSQL_STATEMENT_QUERY_TIMEOUT);
            this.put(STATEMENT_FETCH_SIZE, MYSQL_STATEMENT_FETCH_SIZE);
        }
    }
}
