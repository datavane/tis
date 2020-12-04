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

import com.google.common.collect.Maps;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.db.parser.domain.DBConfig;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus.TableDumpStatus;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.plugin.ds.DataDumpers;
import com.qlangtech.tis.plugin.ds.IDataSourceDumper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @version 1.0
 * @description 多线程的数据源读取，每个表或者每个库使用一个线程写入到HDFS集群<br>
 * 子表多情况，可以考虑根据库启线程
 * @date 2020/04/13
 * @since 2011-8-4 上午12:19:57
 */
public class SourceDataProviderFactory {


    /**
     * 在单元测试过程中使用
     */
    public interface ISourceDataProviderFactoryInspect {

        public void look(DBConfig dbLinkMeta, SourceDataProviderFactory factory);
    }

    @SuppressWarnings("all")
    // public List<SourceDataProvider<String, String>> result = new ArrayList<>();

    private static final Pattern IP_PATTERN = Pattern.compile("//(.+?):");

    private static final Log logger = LogFactory.getLog(SourceDataProviderFactory.class);

    protected TSearcherDumpContext dumpContext;

    // 已經讀出的記錄數
    private AtomicInteger dbReaderCounter;

    // 所有表总共要dump的记录数目， 當這個有第一次讀出之後以後就不需要每次預先掃描數據庫表的記錄數了，直接從上次數據庫導出數目預估就行了
    private final ScheduledExecutorService statusSendScheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "table_dump_send_scheduler");
            t.setDaemon(true);
            return t;
        }
    });

    public long getDbReaderCounter() {
        return dbReaderCounter.get();
    }

    // public TSearcherDumpContext getDumpContext() {
//        return dumpContext;
//    }

    public void setDumpContext(TSearcherDumpContext dumpContext) {
        this.dumpContext = dumpContext;
    }


//    public void setSubTablesDesc(Map<String, String> subTablesDesc) {
//        this.subTablesDesc = subTablesDesc;
//        if (subTablesDesc.size() != 1) {
//            throw new IllegalArgumentException("subTablesDesc.size():" + subTablesDesc.size() + " shall be 1");
//        }
//    }


//    public void setPlusSqlFuncs(List<SqlFunction> plusSqlFuncs) {
//        this.plusSqlFuncs = plusSqlFuncs;
//    }

    // protected static final String tableSuffixPlaceHolder = "$tablename$";


    // private Map<String, String> /* dump sql */ subTablesDesc;


    //  protected List<SqlFunction> plusSqlFuncs = null;

    private String allDumpStartTime;

    private final AtomicBoolean haveInitialize = new AtomicBoolean(false);

    public void init() throws Exception {
        if (!haveInitialize.compareAndSet(false, true)) {
            throw new IllegalStateException(dumpContext.getDumpTable() + ",sourceDataProvider have been initialize again.");
        }

        Assert.assertNotNull("dumpContext can not be null", this.dumpContext);
        // Assert.assertNotNull("timeManager can not be null",
        // this.dumpContext.getTimeProvider());
        // 每隔2秒向状态收集中心发送一次dump执行状态
        statusSendScheduler.scheduleAtFixedRate(() -> {
            reportDumpStatus();
        }, 1, 2, TimeUnit.SECONDS);
        // 记录当前已经读入的数据表
        this.dbReaderCounter = new AtomicInteger();

        this.parseSubTablesDesc();

    }

    public void reportDumpStatus() {
        this.reportDumpStatus(false, /* faild */
                false);
    }

    /**
     * 报告当前dump数据的状态
     */
    public void reportDumpStatus(boolean faild, boolean complete) {
        int read = dbReaderCounter.get();
        int all = dumpContext.getAllTableDumpRows().get();
        if (all < 1 && !(faild || complete)) {
            return;
        }
        TableDumpStatus tableDumpStatus = new TableDumpStatus(String.valueOf(dumpContext.getDumpTable()), dumpContext.getTaskId());
        tableDumpStatus.setWaiting(false);
        tableDumpStatus.setAllRows(all);
        tableDumpStatus.setFaild(faild);
        tableDumpStatus.setReadRows(read);
        tableDumpStatus.setComplete(complete);
        logger.info("read:" + read + ",percent:" + (read * 100 / (all + 1)) + "%");
        dumpContext.getStatusReportRPC().reportDumpTableStatus(tableDumpStatus);
    }

    public void parseSubTablesDesc() throws Exception {
//        if (this.dataSourceGetter == null) {
//            throw new IllegalStateException("dataSourceGetter can not be null");
//        }

        int i = 0;
        // try {

//            for (Entry<String, String> mapEntry : subTablesDesc.entrySet()) {
//                // 关于数据源的描述信息
//                String dataSourceDesc = mapEntry.getKey();
//                Map<String, List<String>> dsProperty = parser.parseDescription(dataSourceDesc);
//                final String sql = mapEntry.getValue();
//                StringBuffer dbNames = new StringBuffer();
//                for (String dbKey : dsProperty.keySet()) {
//                    dbNames.append(dbKey).append(",");
//                    validateDataSource(dbKey);
//                    DataSource source = null; // dataSourceGetter.getDataSource(dbKey);
//                    if (source == null) {
//                        throw new IllegalArgumentException("dbKey:" + dbKey + " relevant source can not be null");
//                    }
//                    // .getBean(dbKey);
//                    String dbip = getDbIp(source);
//                    if (StringUtils.isBlank(dbip)) {
//                        throw new IllegalStateException("dbip can not be null,dbkey:" + dbKey);
//                    }
//                    subtables = dsProperty.get(dbKey);
//                    if (subtables != null) {
//                        // 分表可能多库情况
//                        for (String subtable : subtables) {
//                            SingleTableSqlExcuteProvider sqlprovider = new SingleTableSqlExcuteProvider(dbReaderCounter);
//                            sqlprovider.dbHost = dbip;
//                            sqlprovider.setDumpContext(this.dumpContext);
//                            // 库名
//                            sqlprovider.dsName = dbKey;
//                            sqlprovider.suffixTableName = subtable;
//                            String fullSql = sql.replace(tableSuffixPlaceHolder, subtable);
//                            logger.warn("[" + (++i) + "] create SingleTableSqlExcuteProvider,table suffix [" + tableSuffixPlaceHolder + "] 后的 sql ==> "
//                                    + fullSql);
//                            sqlprovider.setSql(fullSql);
//                            sqlprovider.dataSource = source;
//
//
//                            // sqlprovider.shardKey = shardKey;
//                            sqlprovider.init();
//                            result.add(sqlprovider);
//                        }
//                    } else {
//                        // 无分表,可能多库情况
//                        SingleTableSqlExcuteProvider sqlprovider = new SingleTableSqlExcuteProvider(dbReaderCounter);
//                        sqlprovider.dbHost = dbip;
//                        // 库名
//                        sqlprovider.setDumpContext(this.dumpContext);
//                        sqlprovider.dsName = dbKey;
//                        ++i;
//                        sqlprovider.setSql(sql);
//                        sqlprovider.dataSource = source;
//
//
//                        //  sqlprovider.shardKey = shardKey;
//                        sqlprovider.init();
//                        result.add(sqlprovider);
//                    }
//                }
//                if (subtables == null) {
//                    logger.warn(" create dump on dbs:" + dbNames + "\n use sql ==> " + sql);
//                }
//            }
        if (dumpContext == null) {
            throw new IllegalStateException("dumpContext can not be null");
        }
        DataDumpers dataDumpers = dumpContext.getDataSourceFactory().getDataDumpers(this.dumpContext.getTisTable());
        Iterator<IDataSourceDumper> dumpers = dataDumpers.dumpers;
        IDataSourceDumper dumper = null;

        CountDownLatch countdown = new CountDownLatch(dataDumpers.splitCount);
        logger.info("dataprovider:" + dumpContext.getDumpTable() + ",split count:" + dataDumpers.splitCount);
        AtomicReference<Throwable> exp = new AtomicReference<Throwable>();

        while (dumpers.hasNext()) {
            dumper = dumpers.next();
            InitialDBTableReaderTask initTask = InitialDBTableReaderTask.create(exp, countdown, dumper, dumpContext);
            MultiThreadDataProvider.dbReaderExecutor.execute(initTask);
        }
//            for (SourceDataProvider<String, String> tabProvider : this.result) {
//                InitialDBTableReaderTask initTask = InitialDBTableReaderTask.create(exp, countdown, tabProvider, dumpContext);
//                MultiThreadDataProvider.dbReaderExecutor.execute(initTask);
//            }
        if (!countdown.await(10, TimeUnit.MINUTES)) {
            throw new IllegalStateException("wait 10 minutes expire timeout");
        }

        if (exp.get() != null) {
            throw new SourceDataReadException(exp.get());
        }
        logger.info(this.dumpContext.getDumpTable() + " row count:" + this.dumpContext.getAllTableDumpRows().get());
//        } catch (SourceDataReadException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new SourceDataReadException("par table source error", e);
//        }
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
     * @param
     */
    protected void validateDataSource(String dbKey) {
    }

    private static class InitialDBTableReaderTask extends AbstractDBTableReaderTask {

        // private final AtomicInteger allRows;
        // private final CountDownLatch countdown;
        private final AtomicReference<Throwable> exceptionCollector;

        public static InitialDBTableReaderTask create(AtomicReference<Throwable> exceptionCollector, CountDownLatch latch, IDataSourceDumper dataProvider, TSearcherDumpContext dumpContext) {
            AtomicInteger dbHostBusyCount = new AtomicInteger();
            AtomicInteger processErrorCount = new AtomicInteger();
            InitialDBTableReaderTask result = new InitialDBTableReaderTask(latch, dataProvider, Maps.newHashMap(), /* threadResult */
                    dbHostBusyCount, processErrorCount, dumpContext, exceptionCollector);
            return result;
        }

        private InitialDBTableReaderTask(CountDownLatch latch, IDataSourceDumper dataProvider, Map<String, Object> threadResult, AtomicInteger dbHostBusyCount, AtomicInteger processErrorCount, TSearcherDumpContext dumpContext, AtomicReference<Throwable> exceptionCollector) {
            super(latch, dataProvider, threadResult, dbHostBusyCount, processErrorCount, dumpContext);
            this.exceptionCollector = exceptionCollector;
        }

        @Override
        public void run() {
            try {
                // logger.info("open " + dataProvider.getDsName() + "." +
                // this.dumpContext.getDumpTable().getTableName());
                // dataProvider.openResource();
                int rowSize = dumper.getRowSize();
                this.dumpContext.getAllTableDumpRows().addAndGet(rowSize);
                // this.allRows.addAndGet(rowSize);
                logger.info(dumper.getDbHost() + "." + dumpContext.getDumpTable().getTableName() + " row count:" + rowSize);
            } catch (Throwable e) {
                while (latch.getCount() > 0) {
                    latch.countDown();
                }
                exceptionCollector.set(e);
            } finally {
                // dataProvider.closeResource();
                latch.countDown();
            }
        }
    }

}
