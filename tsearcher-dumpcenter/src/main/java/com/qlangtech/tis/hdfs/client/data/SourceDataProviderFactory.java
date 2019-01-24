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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.qlangtech.tis.exception.DataSourceParseException;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.hdfs.client.data.parse.DataSourceParser;
import com.qlangtech.tis.hdfs.client.data.parse.DefaultDataSourceParser;
import com.qlangtech.tis.hdfs.client.data.sql.JDBCProperties;
import com.qlangtech.tis.hdfs.client.data.sql.JDBCProperties.DBMSType;
import com.qlangtech.tis.hdfs.client.data.sql.SqlFunction;
import com.qlangtech.tis.hdfs.client.data.sql.SqlFunctionCollectors;
import com.qlangtech.tis.trigger.util.Assert;

/*
 * @description 多线程的数据源读取，每个表或者每个库使用一个线程写入到HDFS集群<br>
 *              子表多情况，可以考虑根据库启线程
 * @since 2011-8-4 上午12:19:57
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SourceDataProviderFactory implements ApplicationContextAware {

    @SuppressWarnings("all")
    public List<SourceDataProvider> result = new ArrayList<SourceDataProvider>();

    private static final Pattern IP_PATTERN = Pattern.compile("//(.+?):");

    private static final Log logger = LogFactory.getLog(SourceDataProviderFactory.class);

    private AtomicLong dbReaderCounter;

    public static void main(String[] args) {
        String jdbcurl = "jdbc:mysql://cluster1211.my.2dfire-inc.com:3306/order30?useUnicode=yes&characterEncoding=utf8";
        Matcher m = IP_PATTERN.matcher(jdbcurl);
        if (m.find()) {
            System.out.println(m.group(1));
        }
    }

    public long getDbReaderCounter() {
        return dbReaderCounter.get();
    }

    protected TSearcherDumpContext dumpContext;

    public TSearcherDumpContext getDumpContext() {
        return dumpContext;
    }

    public void setDumpContext(TSearcherDumpContext dumpContext) {
        this.dumpContext = dumpContext;
    }

    int hashMask = 1;

    int allDsName = 0;

    private boolean isInrc = true;

    public void setShardKey(String shardKey) {
        this.shardKey = shardKey;
    }

    private String shardKey = null;

    // 表后缀格式化标示，如果在Spring配置文件中不对其进行设置，默认格式化”_0000“;
    private String formatString = null;

    /**
     * @param formatString
     * @uml.property name="formatString"
     */
    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    /**
     * @param parser
     * @uml.property name="parser"
     */
    public void setParser(DataSourceParser parser) {
        this.parser = parser;
    }

    public void setSubTablesDesc(Map<String, String> subTablesDesc) {
        this.subTablesDesc = subTablesDesc;
        if (subTablesDesc.size() != 1) {
            throw new IllegalArgumentException("subTablesDesc.size():" + subTablesDesc.size() + " shall be 1");
        }
    }

    public void setSqlFuncs(SqlFunctionCollectors sqlFuncs) {
        this.sqlFuncs = sqlFuncs;
    }

    public void setJdbcProperties(JDBCProperties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }

    public void setPlusSqlFuncs(List<SqlFunction> plusSqlFuncs) {
        this.plusSqlFuncs = plusSqlFuncs;
    }

    protected static final String tableSuffixPlaceHolder = "$tablename$";

    /**
     * @uml.property name="parser"
     * @uml.associationEnd
     */
    private DataSourceParser parser;

    protected IDataSourceGetter dataSourceGetter;

    private Map<String, String> /* dump sql */
    subTablesDesc;

    protected SqlFunctionCollectors sqlFuncs;

    protected JDBCProperties jdbcProperties;

    protected List<SqlFunction> plusSqlFuncs = null;

    private String allDumpStartTime;

    public void setAllDumpStartTime(String allDumpStartTime) {
        this.allDumpStartTime = allDumpStartTime;
    }

    private final AtomicBoolean haveInitialize = new AtomicBoolean(false);

    public void init() throws SourceDataReadException {
        if (!haveInitialize.compareAndSet(false, true)) {
            throw new IllegalStateException(dumpContext.getServiceName() + ",sourceDataProvider have been initialize again.");
        }
        try {
            Assert.assertNotNull("dumpContext can not be null", this.dumpContext);
            // Assert.assertNotNull("timeManager can not be null",
            // this.dumpContext.getTimeProvider());
            // 记录当前已经读入的数据表
            this.dbReaderCounter = new AtomicLong();
            // }
            if (sqlFuncs == null) {
                // 
                sqlFuncs = new SqlFunctionCollectors();
                sqlFuncs.initDefaultFunctions();
                if (allDumpStartTime != null)
                    sqlFuncs.allDumpStartTime = allDumpStartTime;
                if (plusSqlFuncs != null && !plusSqlFuncs.isEmpty()) {
                    logger.warn("用户自定义了SqlFunctions");
                    for (SqlFunction f : plusSqlFuncs) {
                        if (sqlFuncs.register(f) != null) {
                            logger.warn("用户自定义的SqlFunction ==> " + f.getPlaceHolderName() + " 覆盖了默认的Function.");
                        }
                    }
                }
            // sqlFuncs.setTimerManager(this.dumpContext.getTimeProvider());
            }
            if (jdbcProperties == null) {
                jdbcProperties = new JDBCProperties(DBMSType.MYSQL);
            }
            // 解析数据源
            this.parseSubTablesDesc(this.isInrc);
        } catch (SourceDataReadException e) {
            throw e;
        }
    }

    // @Override
    // public void openResource() throws SourceDataReadException {
    // 
    // }
    public void parseSubTablesDesc(boolean isIncr) throws SourceDataReadException {
        if (this.dataSourceGetter == null) {
            throw new IllegalStateException("dataSourceGetter can not be null");
        }
        if (parser == null) {
            parser = new DefaultDataSourceParser();
            if (formatString != null) {
                parser.setDefaultSubTableString(formatString);
            }
            parser.init();
        }
        int count = 0;
        int i = 0;
        try {
            // CurrentOperatorLocation currentOperatorLocation = new
            // SingleTableSqlExcuteProvider.CurrentOperatorLocation();
            List<String> subtables = null;
            for (Entry<String, String> mapEntry : subTablesDesc.entrySet()) {
                // 关于数据源的描述信息
                String dataSourceDesc = mapEntry.getKey();
                Map<String, List<String>> dsProperty = parser.parseDescription(dataSourceDesc);
                final String sql = mapEntry.getValue();
                StringBuffer dbNames = new StringBuffer();
                for (String dbKey : dsProperty.keySet()) {
                    dbNames.append(dbKey).append(",");
                    validateDataSource(dbKey);
                    DataSource source = dataSourceGetter.getDataSource(dbKey);
                    // .getBean(dbKey);
                    String dbip = getDbIp(source);
                    if (StringUtils.isBlank(dbip)) {
                        throw new IllegalStateException("dbip can not be null,dbkey:" + dbKey);
                    }
                    subtables = dsProperty.get(dbKey);
                    if (subtables != null) {
                        // 分表可能多库情况
                        for (String subtable : subtables) {
                            SingleTableSqlExcuteProvider sqlprovider = new SingleTableSqlExcuteProvider(dbReaderCounter);
                            sqlprovider.dbHost = dbip;
                            // 库名
                            sqlprovider.dsName = dbKey;
                            sqlprovider.suffixTableName = subtable;
                            String fullSql = sql.replace(tableSuffixPlaceHolder, subtable);
                            logger.warn("[" + (++i) + "] create SingleTableSqlExcuteProvider,table suffix [" + tableSuffixPlaceHolder + "] 后的 sql ==> " + fullSql);
                            sqlprovider.setSql(fullSql);
                            sqlprovider.dataSource = source;
                            sqlprovider.jdbcProperties = jdbcProperties;
                            sqlprovider.sqlFuncs = sqlFuncs;
                            // sqlprovider.timeProvider = timeManager;
                            sqlprovider.shardKey = shardKey;
                            // sqlprovider.setDbReaderCounter(dbReaderCounter);
                            sqlprovider.init();
                            // if (testOrNot) {// 测试模式每次只执行几条而已
                            // sqlprovider.isTest = true;
                            // }
                            // if (count++ == 0) {
                            // currentOperatorLocation.dsName =
                            // sqlprovider.dsName;
                            // currentOperatorLocation.suffixTableName =
                            // sqlprovider.suffixTableName;
                            // }
                            // if (threadForTable) {// 按table进行多线程操作
                            // 每个provider 共用同一个currentOperaatorLocation
                            // sqlprovider.currentOperatorLocation =
                            // currentOperatorLocation;
                            result.add(sqlprovider);
                        // } else {// 按照库不同将SingleTableSqlExcuteProvider
                        // // 构造成一个Hash
                        // // 链表的数据结构
                        // this.partitionByDataBase(sqlprovider.dsName,
                        // sqlprovider);
                        // }
                        }
                    } else {
                        // 无分表,可能多库情况
                        SingleTableSqlExcuteProvider sqlprovider = new SingleTableSqlExcuteProvider(dbReaderCounter);
                        sqlprovider.dbHost = dbip;
                        // 库名
                        sqlprovider.dsName = dbKey;
                        ++i;
                        sqlprovider.setSql(sql);
                        sqlprovider.dataSource = source;
                        sqlprovider.jdbcProperties = jdbcProperties;
                        sqlprovider.sqlFuncs = sqlFuncs;
                        // sqlprovider.timeProvider = timeManager;
                        sqlprovider.shardKey = shardKey;
                        sqlprovider.init();
                        // if (count++ == 0) {
                        // currentOperatorLocation.dsName = sqlprovider.dsName;
                        // currentOperatorLocation.suffixTableName =
                        // sqlprovider.suffixTableName;
                        // }
                        // if (threadForTable) {// 按table进行多线程操作
                        // / 每个provider 共用同一个currentOperaatorLocation
                        // sqlprovider.currentOperatorLocation =
                        // currentOperatorLocation;
                        result.add(sqlprovider);
                    // } else {// 按照库不同将SingleTableSqlExcuteProvider
                    // 构造成一个Hash
                    // // 链表的数据结构
                    // this.partitionByDataBase(sqlprovider.dsName,
                    // sqlprovider);
                    // }
                    }
                }
                if (subtables == null) {
                    logger.warn(" create dump on dbs:" + dbNames + "\n use sql ==> " + sql);
                }
            }
            logger.info("dataprovider:" + dumpContext.getServiceName() + ",datasource count:" + result.size());
        } catch (DataSourceParseException e) {
            throw new SourceDataReadException("解析数据源表达式出錯" + e);
        }
    }

    /**
     * @param source
     */
    private String getDbIp(DataSource source) {
        if (source instanceof BasicDataSource) {
            BasicDataSource dbcpSource = (BasicDataSource) source;
            // System.out.println("dbcpSource.getUrl:" + dbcpSource.getUrl());
            Matcher matcher = IP_PATTERN.matcher(dbcpSource.getUrl());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new IllegalStateException(dbcpSource.getUrl() + " is not match pattern:" + IP_PATTERN);
            }
        }
        throw new IllegalStateException("datasoure is illegal:" + source);
    }

    /**
     * @param dsEntry
     */
    protected void validateDataSource(String dbKey) {
    // if (!applicationContext.containsBean(dbKey)) {
    // StringBuffer buffer = new StringBuffer();
    // for (String name : applicationContext.getBeanDefinitionNames()) {
    // buffer.append(name).append(",");
    // }
    // throw new IllegalStateException(
    // "can not find bean name:" + dbKey + " in[" + buffer + "]");
    // }
    }

    /**
     * 为执行链注入currentOperatorLocation
     *
     * @param provider
     */
    // private void setOperationLocation(SingleTableSqlExcuteProvider provider)
    // {
    // if (provider != null) {
    // CurrentOperatorLocation currentOperatorLocation = new
    // SingleTableSqlExcuteProvider.CurrentOperatorLocation();
    // SingleTableSqlExcuteProvider temProvider = (SingleTableSqlExcuteProvider)
    // provider.nextDataProvider;
    // provider.currentOperatorLocation = currentOperatorLocation;
    // while (temProvider != null) {
    // temProvider.currentOperatorLocation = currentOperatorLocation;
    // temProvider = (SingleTableSqlExcuteProvider)
    // temProvider.nextDataProvider;
    // }
    // }
    // }
    // public void partitionByDataBase(String dsName,
    // SingleTableSqlExcuteProvider current) {
    // int hashPos = dsName.hashCode() & hashMask;
    // SingleTableSqlExcuteProvider stp = (SingleTableSqlExcuteProvider)
    // providerHash[hashPos];
    // 
    // while (stp != null && stp.dsName.equals(dsName)) {
    // stp = (SingleTableSqlExcuteProvider) stp.nextDataProvider;
    // }
    // stp = current;
    // stp.nextDataProvider = providerHash[hashPos];
    // providerHash[hashPos] = stp;
    // allDsName++;
    // if (allDsName >= providerHash.length / 2) {
    // rehash();
    // }
    // }
    /**
     * 重新Hash
     */
    // private void rehash() {
    // final int newHashSize = (int) (providerHash.length * 2);
    // assert newHashSize > providerHash.length;
    // 
    // final SingleTableSqlExcuteProvider newHashArray[] = new
    // SingleTableSqlExcuteProvider[newHashSize];
    // 
    // // Rehash
    // int newHashMask = newHashSize - 1;// new hash Mark
    // for (int j = 0; j < providerHash.length; j++) {
    // SingleTableSqlExcuteProvider st0 = (SingleTableSqlExcuteProvider)
    // providerHash[j];
    // while (st0 != null) {
    // final int hashPos2 = st0.dsName.hashCode() & newHashMask;
    // SingleTableSqlExcuteProvider nextFP0 = (SingleTableSqlExcuteProvider)
    // st0.nextDataProvider;
    // st0.nextDataProvider = newHashArray[hashPos2];
    // newHashArray[hashPos2] = st0;
    // st0 = nextFP0;
    // }
    // }
    // 
    // providerHash = newHashArray;
    // hashMask = newHashMask;
    // }
    // @Override
    // public void setNextDataProvider(SourceDataProvider nextProvider) {
    // 
    // }
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.dataSourceGetter = new IDataSourceGetter() {

            @Override
            public DataSource getDataSource(String dbKeyName) {
                return (DataSource) applicationContext.getBean(dbKeyName);
            }
        };
    }

    public void setDataSourceGetter(IDataSourceGetter dataSourceGetter) {
        this.dataSourceGetter = dataSourceGetter;
    }

    /**
     * @return
     */
    // @Override
    @SuppressWarnings("all")
    public List<SourceDataProvider> getSourceDataProvider() {
        return this.result;
    }
}
