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
package com.qlangtech.tis.exec.impl;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.ZkStateReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.dump.hive.HiveColumn;
import com.qlangtech.tis.dump.hive.HiveRemoveHistoryDataTask;
import com.qlangtech.tis.exec.ActionInvocation;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.IExecuteInterceptor;
import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.exec.lifecycle.hook.IIndexBuildLifeCycleHook;
import com.qlangtech.tis.fullbuild.servlet.BuildTriggerServlet;
import com.qlangtech.tis.fullbuild.taskflow.TaskConfigParser;
import com.qlangtech.tis.fullbuild.taskflow.hive.HiveInsertFromSelectParser;
import com.qlangtech.tis.order.center.IndexBackflowManager;
import com.qlangtech.tis.order.center.RemoteBuildCenterUtils;
import com.qlangtech.tis.trigger.feedback.DistributeLog;
import com.qlangtech.tis.trigger.jst.AbstractIndexBuildJob;
import com.qlangtech.tis.trigger.jst.AbstractIndexBuildJob.BuildResult;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo.HdfsSourcePathCreator;
import com.qlangtech.tis.trigger.jst.impl.RemoteIndexBuildJob;
import com.qlangtech.tis.trigger.socket.InfoType;

/*
 * 索引buid执行器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexBuildInterceptor implements IExecuteInterceptor {

    public static final String NAME = "indexBuild";

    public static final String KEY_INDEX_BACK_FLOW_QUEUE = "indexBackFlowQueue";

    protected static final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final Logger logger = LoggerFactory.getLogger(IndexBuildInterceptor.class);

    /**
     * 判断是否从索引build流程調用傳播過來的
     *
     * @param execContext
     * @return
     */
    public static boolean isPropagateFromIndexBuild(IExecChainContext execContext) {
        return execContext.getAttribute(KEY_INDEX_BACK_FLOW_QUEUE) != null;
    }

    public static IndexBackflowManager getIndeBackFlowQueue(IExecChainContext execContext) {
        IndexBackflowManager buildResultQueue = execContext.getAttribute(KEY_INDEX_BACK_FLOW_QUEUE);
        if (buildResultQueue == null) {
            throw new IllegalStateException("execContext.getAttribute('" + KEY_INDEX_BACK_FLOW_QUEUE + "') is null");
        }
        return buildResultQueue;
    }

    @Override
    public ExecuteResult intercept(ActionInvocation invocation) throws Exception {
        logger.info("component:" + this.getName() + " start execute");
        final IExecChainContext execContext = invocation.getContext();
        final String ps = execContext.getPartitionTimestamp();
        // ▼▼▼▼ 触发索引构建
        final HdfsSourcePathCreator pathCreator = createIndexBuildSourceCreator(execContext);
        final int groupSize = getGroupSize(execContext.getIndexName(), pathCreator, execContext.getDistributeFileSystem());
        if (groupSize < 1) {
            ExecuteResult faild = ExecuteResult.createFaild();
            // "target tab:" + sqlAST.getTargetTableName()
            faild.setMessage(" build source ps:" + ps + " is null");
            return faild;
        }
        try {
            if (!triggerIndexBuildJob(execContext.getIndexName(), ps, groupSize, pathCreator, execContext.getContextUserName(), execContext)) {
                String msg = "index build faild,ps:" + ps + ",groupsize:" + groupSize;
                logger.info(msg);
                ExecuteResult faild = ExecuteResult.createFaild();
                faild.setMessage(msg);
                return faild;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // ▲▲▲▲
        return invocation.invoke();
    }

    protected HdfsSourcePathCreator createIndexBuildSourceCreator(final IExecChainContext execContext) // final HiveInsertFromSelectParser sqlAST
    {
        final HiveInsertFromSelectParser sqlAST = TaskConfigParser.getLastJoinTaskSQLAST(execContext);
        final String hdfsPath = HiveRemoveHistoryDataTask.getJoinTableStorePath(execContext.getContextUserName(), // TODO 如果找不到宽表就应该找和该索引同名的表
        sqlAST.getTargetTableName()) + "/pt=%s/pmod=%s";
        logger.info("hdfs sourcepath:" + hdfsPath);
        return new HdfsSourcePathCreator() {

            @Override
            public String build(String group) {
                return String.format(hdfsPath, execContext.getPartitionTimestamp(), group);
            }
        };
    }

    /**
     * 触发索引build
     *
     * @param indexName
     * @param timepoint
     * @param groupSize
     * @param hdfsSourcePathCreator
     * @throws Exception
     */
    private boolean triggerIndexBuildJob(String indexName, final String timepoint, int groupSize, HdfsSourcePathCreator hdfsSourcePathCreator, String username, IExecChainContext execContext) throws Exception {
        // TSearcherConfigFetcher tisConfig = TSearcherConfigFetcher.get();
        // RunEnvironment runtime = tisConfig.getRuntime();//
        // RunEnvironment.getEnum(tisConfig.getRunEnvironment());
        // SnapshotDomain domain =
        // HttpConfigFileReader.getResource(tisConfig.getTerminatorConsoleHostAddress(),
        // indexName,
        // 0, runtime, ConfigFileReader.FILE_SOLOR, ConfigFileReader.FILE_SCHEMA);
        // if (domain == null) {
        // throw new IllegalStateException(
        // "index:" + indexName + ",runtime:" + runtime + " have not prepare for
        // confg");
        // }
        ImportDataProcessInfo processInfo = new ImportDataProcessInfo(999);
        IIndexMetaData indexMetaData = execContext.getIndexMetaData();
        // 读取schema的内容,取得配置信息
        // byte[] schemaContent = ConfigFileReader.FILE_SCHEMA.getContent(domain);
        // ByteArrayInputStream stream = new ByteArrayInputStream(schemaContent);
        // new
        String indexBuilder = indexMetaData.getSchemaParseResult().getIndexBuilder();
        // SolrFieldsParser().readSchema(stream).getIndexBuilder();
        if (indexBuilder != null) {
            processInfo.setIndexBuilder(indexBuilder);
        }
        processInfo.setTimepoint(timepoint);
        processInfo.setIndexName(indexName);
        processInfo.setHdfsSourcePathCreator(hdfsSourcePathCreator);
        processInfo.setLuceneVersion(indexMetaData.getLuceneVersion());
        final String rowCount = execContext.getString(BuildTriggerServlet.KEY_DUMP_ROW_COUNT);
        if (StringUtils.isNotBlank(rowCount)) {
            // 触发中心发送过来的是整份数据的大小的一个值，这里使用groupSize进行除，每组大小虽然不精确但是也能估算个大概，这样后期在yarn集群上可以計算build執行進度
            processInfo.setDumpCount(Long.parseLong(rowCount) / groupSize);
        }
        setBuildTableTitleItems(indexName, processInfo, execContext);
        final DistributeLog log = new DistributeLog() {

            @Override
            public void addLog(ImportDataProcessInfo state, String msg) {
                logger.info(msg);
            }

            @Override
            public void addLog(ImportDataProcessInfo state, InfoType level, String msg) {
                logger.info("level:" + level + "," + msg);
            }
        };
        final ExecutorCompletionService<BuildResult> completionService = new ExecutorCompletionService<BuildResult>(executorService);
        for (int grouIndex = 0; grouIndex < groupSize; grouIndex++) {
            AbstractIndexBuildJob indexBuildJob = createRemoteIndexBuildJob(processInfo, grouIndex, username);
            indexBuildJob.setLog(log);
            completionService.submit(indexBuildJob);
        }
        Future<BuildResult> result = completionService.poll(8, TimeUnit.HOURS);
        if (result == null) {
            logger.error("completionService.poll(7, TimeUnit.HOURS) is null");
            return false;
        }
        DocCollection collection = ZkStateReader.getCollectionLive(execContext.getZkStateReader(), execContext.getIndexName());
        if (collection == null) {
            throw new IllegalStateException("indexName:" + execContext.getIndexName() + " collection can not be null in solr cluster");
        }
        final IndexBackflowManager indexBackflowManager = new IndexBackflowManager(collection);
        // final Map<String /* node name */, ArrayQueue<BuildResult>>
        // sharedIndexBackFlowQueue = new HashMap<>(groupSize);
        // for (int groupIndex = 0; groupIndex < groupSize; groupIndex++) {
        // sharedIndexBackFlowQueue.put(groupIndex, new
        // ArrayQueue<BuildResult>());
        // }
        execContext.setAttribute(KEY_INDEX_BACK_FLOW_QUEUE, indexBackflowManager);
        if ((groupSize - 1) > 0) {
            // 里面会创建一个线程
            createFeedbackJob(execContext, groupSize - 1, completionService, indexBackflowManager);
        } else {
            // 索引只有一组（shared），已经build成功了
            IIndexMetaData indexMeta = execContext.getIndexMetaData();
            IIndexBuildLifeCycleHook buildHook = indexMeta.getIndexBuildLifeCycleHook();
            buildHook.buildSuccess(execContext);
        }
        return processBuildResult(result, indexBackflowManager);
    }

    private boolean processBuildResult(Future<BuildResult> result, final IndexBackflowManager indexBackflowManager) throws InterruptedException, ExecutionException {
        BuildResult buildResult;
        buildResult = result.get();
        if (!buildResult.isSuccess()) {
            logger.error("sourpath:" + buildResult.getHdfsSourcePath() + " build faild.");
            // build失败
            return false;
        }
        List<Replica> shardReplica = indexBackflowManager.getReplicByShard(buildResult.getGroupIndex());
        for (Replica r : shardReplica) {
            indexBackflowManager.addBackFlowTask(BuildResult.clone(buildResult).setReplica(r));
            logger.info("group:" + buildResult.getGroupIndex() + ",indexsize:" + buildResult.getIndexSize() + ",node:" + r.getCoreUrl());
        }
        return true;
    }

    private void createFeedbackJob(IExecChainContext execContext, int groupSize, ExecutorCompletionService<BuildResult> completionService, final IndexBackflowManager indexBackflowManager) {
        IIndexMetaData indexMeta = execContext.getIndexMetaData();
        IIndexBuildLifeCycleHook buildHook = indexMeta.getIndexBuildLifeCycleHook();
        final ExecutorService asynIndexBuildTask = Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        // 终止build任务执行
                        logger.error(e.getMessage(), e);
                        indexBackflowManager.shortCircuit();
                        buildHook.buildFaild(execContext);
                    }
                });
                return t;
            }
        });
        asynIndexBuildTask.execute(() -> {
            try {
                Future<BuildResult> result = null;
                for (int grouIndex = 0; grouIndex < groupSize; grouIndex++) {
                    result = completionService.poll(7, TimeUnit.HOURS);
                    if (result == null) {
                        continue;
                    }
                    if (!processBuildResult(result, indexBackflowManager)) {
                        return;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            buildHook.buildSuccess(execContext);
        });
    }

    /**
     * @param indexName
     * @param processinfo
     */
    protected void setBuildTableTitleItems(String indexName, ImportDataProcessInfo processinfo, IExecChainContext execContext) {
        // .getLastJoinTask();
        HiveInsertFromSelectParser sqlAST = TaskConfigParser.getLastJoinTaskSQLAST(execContext);
        List<HiveColumn> cols = sqlAST.getColsExcludePartitionCols();
        StringBuffer titleColumn = new StringBuffer();
        for (HiveColumn c : cols) {
            titleColumn.append(c.getName()).append(",");
        }
        processinfo.setBuildTableTitleItems(titleColumn.toString());
    }

    /**
     * @param processinfo
     * @param grouIndex
     * @return
     */
    protected final AbstractIndexBuildJob createRemoteIndexBuildJob(ImportDataProcessInfo processinfo, int grouIndex, String username) {
        // 暂时全部提交到32G机器上构建索引吧
        try {
            return new RemoteIndexBuildJob(processinfo, grouIndex, RemoteBuildCenterUtils.remoteJobTriggerFactory, RemoteBuildCenterUtils.taskPool, username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    // }
    }

    /**
     * @param indexName
     * @return
     */
    protected int getGroupSize(String indexName, HdfsSourcePathCreator pathCreator, FileSystem fileSystem) throws Exception {
        FileSystem hdfs = fileSystem;
        int groupIndex = 0;
        while (true) {
            if (!hdfs.exists(new Path(pathCreator.build(String.valueOf(groupIndex++))))) {
                break;
            }
        }
        return groupIndex - 1;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
