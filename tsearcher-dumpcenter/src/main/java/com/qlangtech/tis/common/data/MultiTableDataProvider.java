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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.qlangtech.tis.common.data.sql.SqlFunction;

/*
 * 针对Taobao的分库分表的业务场景设计的<br> 将分库分表的规则用字符串的方式描述出来，会自动进行规则的解析，将分库分表的DataProvider转换成多个SingleDataProvider<br> 支持默认的分库分表表达式，可解析如：ds1:1,2,3,5-7;ds2:4(库ds1含有1，2，3，5，6，7表，库ds2 含有4表)<br> 针对一些不是这样简单的分库分表的规则，可以通过自行实现TableDescriptionParser，并注入自己的分库分表描述串的方式实现<br>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MultiTableDataProvider extends MultiDataProvider implements ApplicationContextAware, PlusSqlFunctionRegisterable {

    protected static final String tableSuffixPlaceHolder = "$tablename$";

    /**
     * @uml.property  name="subtableDesc"
     */
    protected String subtableDesc;

    /**
     * @uml.property  name="parser"
     * @uml.associationEnd
     */
    protected TableDescriptionParser parser;

    /**
     * @uml.property  name="sql"
     */
    protected String sql;

    /**
     * @uml.property  name="plusSqlFuncs"
     */
    protected List<SqlFunction> plusSqlFuncs;

    /**
     * @uml.property  name="applicationContext"
     */
    protected ApplicationContext applicationContext;

    /**
     * @uml.property  name="jdbcProperties"
     * @uml.associationEnd
     */
    protected JDBCProperties jdbcProperties;

    @Override
    protected void doInit() throws Exception {
        logger.warn("初始化MultiTableDatProvider,baseSql ==> " + sql);
        if (parser == null) {
            parser = new DefaultTableDescriptionParser();
        }
        if (plusSqlFuncs == null) {
            plusSqlFuncs = new ArrayList<SqlFunction>();
        }
        if (dataProviders == null) {
            Map<String, List<String>> subtableStore = null;
            try {
                subtableStore = parser.parse(subtableDesc);
            } catch (SubtableDescParseException sdpe) {
                logger.error("解析分表规则失败", sdpe);
                throw new Exception("解析分表规则失败.", sdpe);
            }
            int size = 0;
            for (List<String> sublist : subtableStore.values()) {
                size += sublist.size();
            }
            dataProviders = new ArrayList<DataProvider>(size);
            int i = 0;
            for (Entry<String, List<String>> entry : subtableStore.entrySet()) {
                for (String subtable : entry.getValue()) {
                    String fullSql = sql.replace(tableSuffixPlaceHolder, subtable);
                    logger.debug("[" + (++i) + "] 创建TableDataProvider,替换分表名后缀 [" + tableSuffixPlaceHolder + "] 后的 sql ==> " + fullSql);
                    DataSource tempDataSource = (DataSource) this.applicationContext.getBean(entry.getKey());
                    SingleTableDataProvider dataProvider = new SingleTableDataProvider();
                    dataProvider.setDataSource(tempDataSource);
                    dataProvider.setFunctionList(plusSqlFuncs);
                    dataProvider.setSql(fullSql);
                    dataProvider.setJdbcProperties(jdbcProperties);
                    dataProviders.add(dataProvider);
                    Collections.shuffle(dataProviders);
                }
            }
        }
        currentDataProviderIndex = 0;
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
     * @uml.property  name="plusSqlFuncs"
     */
    public List<SqlFunction> getPlusSqlFuncs() {
        return plusSqlFuncs;
    }

    /**
     * @param plusSqlFuncs
     * @uml.property  name="plusSqlFuncs"
     */
    public void setPlusSqlFuncs(List<SqlFunction> plusSqlFuncs) {
        this.plusSqlFuncs = plusSqlFuncs;
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
     * @uml.property  name="applicationContext"
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void unregisterAll() {
        if (this.plusSqlFuncs == null)
            return;
        this.plusSqlFuncs.clear();
    }

    @Override
    public void registerSqlFunction(SqlFunction sqlFunction) {
        if (plusSqlFuncs == null) {
            this.plusSqlFuncs = new ArrayList<SqlFunction>();
        }
        this.plusSqlFuncs.add(sqlFunction);
    }

    @Override
    public void unregisterSqlFunction(String name) {
    }
}
