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

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.exception.DataImportHDFSException;
import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fs.ITaskContext;
import com.qlangtech.tis.fs.TISFSDataOutputStream;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherDumpContextImpl;
import com.qlangtech.tis.hdfs.client.process.BatchDataProcessor;
import com.qlangtech.tis.hdfs.util.Constants;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.order.dump.task.ITableDumpConstant;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.DataDumpers;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.IDataSourceDumper;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.trigger.util.TriggerParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @description 在写入方面为了充分利用HDFS集群的写入，针对一个表或者一个库启动一个线程写入<br>
 * 如果有分组情况，最终写入HDFS都是以分组为单位的文件<br>
 * @date 2020/04/13
 */
public class MultiThreadDataProvider {

    // 表Dump的时候默认使用16组，会生成16个通道，随机向里面写入
    public static final int MAX_PROCESS_ERROR = 100;

    // 正在执行dump数据库的繁忙程度
    private static final ConcurrentHashMap<String, AtomicInteger> /* dbip */
            dbBusyStatistics = new ConcurrentHashMap<>();


    // 百岁 add20130513
    // 默认最大并发dump任务数量
    public static final int DEFAULT_MAX_POOL_SIZE = 4;

    // 默认任务等待队列长度
    public static final int DEFUALT_WAIT_QUEUE_SIZE = 10;

    public static final ExecutorService dbReaderExecutor;

    static {
        dbReaderExecutor = createDbReaderExecutor();
    }

    protected static final Log log = LogFactory.getLog(MultiThreadDataProvider.class);

    private AtomicInteger writerFinishedCount = new AtomicInteger(0);

    private AtomicInteger shardInitCount;

    private static final String TAB = "\t";

    private static final String NEWLINE = "\n";

    private final boolean isIncrOrNot = false;


    private AtomicBoolean hasWriteTitles = new AtomicBoolean(false);

    private List<ColumnMetaData> rowKeys;

    private static AtomicInteger getDBServerBusyCount(String dbHost) {
        if (StringUtils.isBlank(dbHost)) {
            throw new IllegalArgumentException("dbHost can not be null");
        }
        AtomicInteger count = dbBusyStatistics.get(dbHost);
        if (count == null) {
            count = new AtomicInteger(0);
            AtomicInteger old = null;
            if ((old = dbBusyStatistics.putIfAbsent(dbHost, count)) != null) {
                count = old;
            }
        }
        return count;
    }


    private TSearcherDumpContextImpl dumpContext;

    private final TableDumpFactory flatTableBuilder;
    private final DataSourceFactory dataSourceFactory;

    private final int maxPoolSize;

    public MultiThreadDataProvider(TableDumpFactory flatTableBuilder, DataSourceFactory dataSourceFactory
            , int maxPoolSize, int waitQueueSize) {
        super();
        this.maxPoolSize = maxPoolSize;
        Objects.requireNonNull(flatTableBuilder, "param flatTableBuilder can not be null");
        this.flatTableBuilder = flatTableBuilder;
        this.dataSourceFactory = dataSourceFactory;
    }

    public TSearcherDumpContextImpl getDumpContext() {
        return this.dumpContext;
    }

    public void setDumpContext(TSearcherDumpContextImpl dumpContext) {
        this.dumpContext = dumpContext;
    }

    public IPath createPath(int num, String utf8StrTime) {
        EntityName dumptable = this.dumpContext.getDumpTable();
        return createPath((sbPath) -> {
            sbPath.append(num).append("/").append(dumptable.getTableName());
        }, utf8StrTime);
    }

    private IPath ceateSuccessTokenPath(String utf8StrTime) {
        return createPath((sbPath) -> {
            sbPath.append("success");
        }, utf8StrTime);
    }

    public IPath createColumnMetaDataPath(String utf8StrTime) {
        return createPath((sbPath) -> {
            sbPath.append(ColumnMetaData.KEY_COLS_METADATA);
        }, utf8StrTime);
    }

    private IPath createPath(IPathAppender appender, String utf8StrTime) {
        ITISFileSystem fsFactory = this.flatTableBuilder.getFileSystem();
        StringBuffer sbPath = new StringBuffer(fsFactory.getRootDir());
        sbPath.append("/");
        EntityName dumptable = this.dumpContext.getDumpTable();
        sbPath.append(dumptable.getDbName() + "/" + dumptable.getTableName()).append("/")
                .append(isIncrOrNot ? "incr" : "all").append("/").append(utf8StrTime).append("/");
        appender.append(sbPath);
        return this.getFileSystem().getPath(sbPath.toString());
    }

    private interface IPathAppender {

        void append(StringBuffer sbPath);
    }

    private static final String KEY_DUMP_LAUNCH_TIME = "dumpLaunchTime";

    public static String getDumpLaunchTimestamp(Map context) {
        String time = (String) context.get(KEY_DUMP_LAUNCH_TIME);
        Assert.assertNotNull(time);
        return time;
    }

    @SuppressWarnings("all")
    public static void setDumpLaunchTime(Map context, String time) {
        Assert.assertNotNull(time);
        context.put(KEY_DUMP_LAUNCH_TIME, time);
    }


    public synchronized void importServiceData(Map context) {
        //TIS.getDataBasePluginStore();
        if (dumpContext == null) {
            throw new IllegalStateException("dumpContext can not be null");
        }
        // int split = 0;
        Map<String, TISFSDataOutputStream> outMap = null;
        final long currentTimeStamp = System.nanoTime();

        DataDumpers dataDumpers = this.dataSourceFactory.getDataDumpers(this.getDumpContext().getTisTable());
        Iterator<IDataSourceDumper> dumpers = dataDumpers.dumpers;
        IDataSourceDumper dumper = null;

        try {

            log.warn(currentTimeStamp + ":obj get execute lock");
            shardInitCount = new AtomicInteger(0);
            outMap = new HashMap<String, TISFSDataOutputStream>();
            final String utf8StrTime = getDumpLaunchTimestamp(context);
            TriggerParam triggerParam = new TriggerParam();
            triggerParam.setTime(utf8StrTime);
            context.put(Constants.TIME_POINT, triggerParam);
            List<String> importCount = new ArrayList<String>();
            for (int i = 0; i < ITableDumpConstant.RAND_GROUP_NUMBER; i++) {
                IPath path = createPath(i, utf8StrTime);
                getFileSystem().delete(path);
                TISFSDataOutputStream output = getFileSystem().create(path, true);
                outMap.put(String.valueOf(i), output);
                importCount.add(String.valueOf(i));
            }
            context.put(Constants.IMPORT_COUNT, importCount);
            outMap = Collections.unmodifiableMap(outMap);
            List<HashMap<String, Object>> resultCollect = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(dataDumpers.splitCount);
            final AtomicInteger processErrorCount = new AtomicInteger();

            while (dumpers.hasNext()) {
                dumper = dumpers.next();

                HashMap<String, Object> result = new HashMap<String, Object>();
                resultCollect.add(result);
                // 一张表为一个执行单元
                dbReaderExecutor.execute(addDBReader(new DBTableReaderTask(latch, dumper
                        , getFileSystem(), outMap, utf8StrTime, result
                        , getDBServerBusyCount(dumper.getDbHost()), processErrorCount, dumpContext)));
            }

            if (!latch.await(7, TimeUnit.HOURS)) {
                throw new IllegalStateException("dump table:" + this.dumpContext.getDumpTable() + " time expire");
            }
            // waitDumpFinish(split, exec);
            String loginfo = ("full") + "dump all task has over,readrows:" + sourceDataFactory.getDbReaderCounter();
            log.warn(loginfo);
            long count = 0;

            for (Map<String, Object> map : resultCollect) {
                Collection<String> errorList = (Collection) map.get(Constants.IMPORT_HDFS_ERROR);
                if (errorList != null) {
                    for (String errorRow : errorList) {
                        sourceDataFactory.reportDumpStatus(true, /* faild */
                                true);
                        throw new DataImportHDFSException(loginfo);
                    }
                }
            }
            // baisui add
            // 执行成功了
            sourceDataFactory.reportDumpStatus(false, true);
        } catch (DataImportHDFSException e) {
            throw e;
        } catch (InterruptedException e) {
            throw new DataImportHDFSException(e.getMessage(), e);
        } catch (Exception e) {
            throw new DataImportHDFSException(e.getMessage(), e);
        } finally {

            log.warn(currentTimeStamp + ":has release the lock");
            // if (outMap != null) {
            for (Entry<String, TISFSDataOutputStream> entry : outMap.entrySet()) {
                TISFSDataOutputStream outputStream = null;
                String shard = null;
                try {
                    shard = entry.getKey();
                    outputStream = entry.getValue();
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            if (shardInitCount != null) {
                shardInitCount = null;
            }
        }
    }

    private Runnable addDBReader(DBTableReaderTask dbTableReaderTask) {
        this.sourceDataFactory.addReadAccumulator(dbTableReaderTask);
        return dbTableReaderTask;
    }

    public void createSuccessToken(String time) throws Exception {
        // 创建成功构建索引信号文件
        try (TISFSDataOutputStream out = getFileSystem().create(ceateSuccessTokenPath(time), true)) {
        }
        // 执行成功之后最后一次报告状态
        sourceDataFactory.reportDumpStatus();
    }

    // @Override
    public boolean hasSuccessToken(String time) throws Exception {
        IPath success = ceateSuccessTokenPath(time);
        return getFileSystem().exists(success);
    }

    /**
     * 负责判断dump任务是否要启动，并且负责历史数据删除任务
     *
     * @param time
     * @param force
     * @param context
     * @return
     * @throws Exception
     */
    public boolean shallProcessDumpTask(String time, boolean force, ITaskContext context) throws Exception {
        if (flatTableBuilder == null) {
            throw new IllegalStateException("historyDataClearTask can not be null");
        }
        EntityName dumptable = this.dumpContext.getDumpTable();
        if (this.hasSuccessToken(time)) {
            // 需要的數據已經有了
            if (force) {
                flatTableBuilder.deleteHistoryFile(dumptable, context, time);
                return true;
            } else {
                // this.createSuccessToken(time);
                this.sourceDataFactory.reportDumpStatus();
                return false;
            }
        } else {
            // 清理历史文件
            flatTableBuilder.deleteHistoryFile(dumptable, context);
            return true;
        }
    }

    private static final int MAX_DUMP_THREAD_COUNT = 60;

    /**
     * @return
     */
    private static ExecutorService createDbReaderExecutor() {
        final PriorityBlockingQueue<Runnable> dbReaderQueue = new PriorityBlockingQueue<Runnable>(20, new Comparator<Runnable>() {

            @Override
            public int compare(Runnable o1, Runnable o2) {
                AbstractDBTableReaderTask task1 = (AbstractDBTableReaderTask) o1;
                AbstractDBTableReaderTask task2 = (AbstractDBTableReaderTask) o2;
                AtomicInteger task1DbBusy = getDBServerBusyCount(task1.getDbIP());
                AtomicInteger task2DbBusy = getDBServerBusyCount(task2.getDbIP());
                return task1DbBusy.get() - task2DbBusy.get();
            }
        });
        final MyUncaughtExceptionHandler exceptionHandler = new MyUncaughtExceptionHandler();
        // 最大执行线程数
        // TSearcherConfigFetcher.get().getMaxDBDumpThreadCount();
        Integer maxDBDumpThreadCount = null;
        if (maxDBDumpThreadCount == null) {
            maxDBDumpThreadCount = MAX_DUMP_THREAD_COUNT;
        }
        final ExecutorService exec = new ThreadPoolExecutor(maxDBDumpThreadCount, maxDBDumpThreadCount, 30l, TimeUnit.SECONDS, dbReaderQueue, new ThreadFactory() {

            int index = 0;

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "read thread #" + index++);
                t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        log.error(t.getName(), e);
                        exceptionHandler.uncaughtException(t, e);
                    }
                });
                return t;
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
        exceptionHandler.setExecService(exec);
        return exec;
    }

    private static class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

        private ExecutorService execService;

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (execService == null) {
                throw new IllegalArgumentException("execService can not be null");
            }
            execService.shutdownNow();
        }

        public void setExecService(ExecutorService execService) {
            this.execService = execService;
        }
    }

    private static String toJSONString(Map<String, String> o) {
        JSONObject json = new JSONObject(o);
        return json.toString();
    }

    // 负责将将数数据库持久层数据导入到hdfs中去
    public class DBTableReaderTask extends AbstractDBTableReaderTask implements IReadAccumulator {
        private final String utf8StrTime;

        private final ITISFileSystem fileSystem;

        private final int outMapSize;

        private Map<String, TISFSDataOutputStream> outMap;

        private final Random nextMapIndexRandom;

        private int readCount;

        public DBTableReaderTask(CountDownLatch latch, IDataSourceDumper dumper
                , ITISFileSystem fileSystem, Map<String, TISFSDataOutputStream> outMap
                , String utf8StrTime, Map<String, Object> threadResult, AtomicInteger dbHostBusyCount
                , AtomicInteger processErrorCount, TSearcherDumpContext dumpContext) {
            super(latch, dumper, threadResult, dbHostBusyCount, processErrorCount, dumpContext);
            this.utf8StrTime = utf8StrTime;
            this.fileSystem = fileSystem;
            this.outMap = outMap;
            this.outMapSize = this.outMap.size();
            this.nextMapIndexRandom = new Random();
        }

        @Override
        public int getReadCount() {
            return this.readCount;
        }

        @Override
        public void run() {
            Collection<String> errorList = new ConcurrentLinkedQueue<String>();
            threadResult.put(Constants.IMPORT_HDFS_ERROR, errorList);
            try {
                this.dbHostBusyCount.incrementAndGet();
                if (this.processErrorCount.get() > MAX_PROCESS_ERROR) {
                    return;
                }
                AtomicInteger filtercount = new AtomicInteger();
                Iterator<Map<String, String>> rowsIt = dumper.startDump();
                writeTitle();
                long startTime = System.currentTimeMillis();
                long interTime = startTime;
                long threadBeginTime = System.currentTimeMillis();
                long allcount = 0;
                String info = null;
                AtomicInteger submitSize = new AtomicInteger(0);
                long readCount = 0;
                while (rowsIt.hasNext()) {
                    readCount++;
                    Map<String, String> row = rowsIt.next();
                    submit(row, errorList, submitSize, filtercount);
                    if (++allcount % 10000 == 0) {
                        // 是否超过最大錯誤限度
                        if (this.processErrorCount.get() > MAX_PROCESS_ERROR) {
                            return;
                        }
                        info = "thread" + Thread.currentThread().getName() + "ip:" + dumper.getDbHost() + " plush rows:10000 consume:" + (System.currentTimeMillis() - interTime) / 1000 + "s  all time consume: " + (System.currentTimeMillis() - startTime) / 1000 + "s accumulate: " + allcount;
                        log.warn(info);
                        interTime = System.currentTimeMillis();
                    }
                }

                threadResult.put(Constants.IMPORT_HDFS_ROW_COUNT, allcount);
            } catch (Exception e) {
                this.processErrorCount.addAndGet(MAX_PROCESS_ERROR);
                log.error(">>>>>ERROR[" + dumpContext.getDumpTable() + "]execute dump", e);
                errorList.add(e.getMessage());
                throw new DataImportHDFSException(e.getMessage(), e);
            } finally {
                try {
                    this.dbHostBusyCount.decrementAndGet();
                } catch (Throwable e) {
                }
                try {
                    this.dumper.closeResource();
                } catch (Throwable e) {
                }
                latch.countDown();
            }
        }

        private void writeTitle() {
            try {
                if (!hasWriteTitles.get() && hasWriteTitles.compareAndSet(false, true)) {
                    List<ColumnMetaData> rowmetalist = this.dumper.getMetaData();
                    MultiThreadDataProvider.this.rowKeys = Collections.unmodifiableList(rowmetalist);
                    try (TISFSDataOutputStream output = fileSystem.create(createColumnMetaDataPath(utf8StrTime), true)) {
                        IOUtils.write(JSON.toJSONString(rowmetalist, false), output, TisUTF8.get());
                        log.info("success write title" + dumpContext.getDumpTable());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String getRowWrite2HdfsOutPutStream(Map<String, String> row) {
            return String.valueOf(nextMapIndexRandom.nextInt(this.outMapSize));
        }

        @SuppressWarnings("all")
        private void submit(final Map<String, String> row, final Collection<String> errorList, final AtomicInteger batchSize, final AtomicInteger filtercount) throws Exception {
            String info = null;
            try {
                BatchDataProcessor dataprocess = dumpContext.getDataProcessor();
                if (dataprocess != null && !dataprocess.process(row)) {
                    filtercount.incrementAndGet();
                    log.warn("record has been filtered:" + toJSONString(row));
                    return;
                }
                TISFSDataOutputStream output = null;
                String shard = null;
                shard = getRowWrite2HdfsOutPutStream(row);
                // shard = dumpContext.getGroupRouter()
                // .getGroupName(row);
                output = outMap.get(shard);
                if (output == null) {
                    throw new IllegalStateException("shard:" + shard + " output channel can not be null");
                }
                StringBuffer content = new StringBuffer();
                int count = 0;
                // titleKeys.size();
                int size = rowKeys.size();
                for (int i = 0; i < size; i++) {
                    String value = row.get(rowKeys.get(i).getKey());
                    content.append(value != null ? value : " ");
                    if (count < size - 1)
                        content.append(TAB);
                    else if (count == size - 1) {
                        content.append(NEWLINE);
                    }
                    count++;
                }
                output.write(content.toString().getBytes(TisUTF8.get()));
                readCount++;
                content = null;
            } catch (Exception e) {
                if (this.processErrorCount.incrementAndGet() > MAX_PROCESS_ERROR) {
                    errorList.add("exceed the max error count:" + MAX_PROCESS_ERROR);
                    throw new Exception("exceed the max error count:" + MAX_PROCESS_ERROR, e);
                }
                info = "process ignore:" + row.toString();
                log.warn(info);
                filtercount.incrementAndGet();
                return;
            }
        }
    }


    @SuppressWarnings("all")
    private SourceDataProviderFactory sourceDataFactory;

    @SuppressWarnings("all")
    public SourceDataProviderFactory getSourceData() {
        return sourceDataFactory;
    }

    @SuppressWarnings("all")
    public void setSourceData(SourceDataProviderFactory sourceData) {
        this.sourceDataFactory = sourceData;
    }


    /**
     * @return the fileSystem
     */
    private ITISFileSystem getFileSystem() {
        return this.flatTableBuilder.getFileSystem();
    }
}
