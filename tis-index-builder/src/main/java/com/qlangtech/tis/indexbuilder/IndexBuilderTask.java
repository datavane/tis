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
package com.qlangtech.tis.indexbuilder;

import com.google.common.collect.Lists;
import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildSharedPhaseStatus;
import com.qlangtech.tis.indexbuilder.columnProcessor.ExternalDataColumnProcessor;
import com.qlangtech.tis.indexbuilder.doc.IInputDocCreator;
import com.qlangtech.tis.indexbuilder.doc.LuceneDocMaker;
import com.qlangtech.tis.indexbuilder.doc.SolrDocPack;
import com.qlangtech.tis.indexbuilder.doc.impl.AbstractInputDocCreator;
import com.qlangtech.tis.indexbuilder.exception.IndexBuildException;
import com.qlangtech.tis.indexbuilder.index.IndexMaker;
import com.qlangtech.tis.indexbuilder.index.IndexMerger;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.IndexGetConfig;
import com.qlangtech.tis.indexbuilder.map.RawDataProcessor;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag.Flag;
import com.qlangtech.tis.indexbuilder.merger.IndexMergerImpl;
import com.qlangtech.tis.indexbuilder.source.impl.HDFSReaderFactory;
import com.qlangtech.tis.indexbuilder.utils.Context;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import com.qlangtech.tis.manage.common.TISCollectionUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;
import com.qlangtech.tis.solrextend.cloud.TisSolrResourceLoader;
import com.tis.hadoop.rpc.RpcServiceReference;
import com.tis.hadoop.rpc.StatusRpcClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HdfsIndexBuilder 索引入口map类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexBuilderTask implements TaskMapper {

    public static final Logger logger = LoggerFactory.getLogger(IndexBuilderTask.class);

    long startTime;

    private ILocalTmpCfgFileGetter schemaCfgFileGetter = (indexConf) -> {
        return IndexGetConfig.getLocalTmpSchemaFile(indexConf.getCollectionName());
    };

    private Integer allRowCount;

    private final IndexBuilderTriggerFactory propsGetter;

    private final ExecutorService execService = Executors.newCachedThreadPool(new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            return t;
        }
    });

    public interface ILocalTmpCfgFileGetter {
        File get(IndexConf indexConf);
    }

    private final CompletionService<SuccessFlag> executorService = new ExecutorCompletionService<SuccessFlag>(execService);

    private final RpcServiceReference statusRpc;

    public static void setMdcAppName(String appname) {
        MDC.put(TISCollectionUtils.KEY_COLLECTION, appname);
    }

    public IndexBuilderTask(IndexBuilderTriggerFactory propsGetter, RpcServiceReference statusRpc) {
        startTime = System.currentTimeMillis();
        this.propsGetter = propsGetter;
        this.statusRpc = statusRpc;
    }

    @Override
    public void map(TaskContext context) {
        IndexConf indexConf = IndexGetConfig.getIndexConf(context);

        IndexMerger mergeTask = null;
        Counters counters = context.getCounters();
        Messages messages = context.getMessages();
        IndexMetaConfig indexMetaConfig = parseIndexMetadata(context, indexConf);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final int taskid = context.getTaskId();
        try {
            int rowCount = context.getAllRowCount();
            logger.warn("indexMaker.flushCountThreshold:" + indexConf.getFlushCountThreshold()
                    + ",indexMaker.flushSizeThreshold:" + indexConf.getFlushSizeThreshold());

            // 开始构建索引。。。
            logger.warn("[taskid:" + taskid + "]" + indexConf.getCoreName() + " indexing start......");
            int docMakerCount = indexConf.getDocMakerThreadCount();
            int indexMakerCount = indexConf.getIndexMakerThreadCount();
            logger.warn("----indexMakerCount=" + indexMakerCount);
            logger.warn("----docMakerCount=" + docMakerCount);
            AtomicInteger aliveIndexMakerCount = new AtomicInteger(indexMakerCount);
            AtomicInteger aliveDocMakerCount = new AtomicInteger(docMakerCount);
            // 存放document对象池的队列
            int docQueueSize = indexConf.getDocQueueSize();
            final BlockingQueue<SolrDocPack> docPoolQueues = new ArrayBlockingQueue<SolrDocPack>(docQueueSize);
            logger.info("RamDirQueueSize:" + indexConf.getRamDirQueueSize());
            BlockingQueue<RAMDirectory> dirQueue = new ArrayBlockingQueue<RAMDirectory>(indexConf.getRamDirQueueSize());
            HDFSReaderFactory readerFactory = new HDFSReaderFactory();
            readerFactory.setFs(propsGetter.getFileSystem());

            Context readerContext = new Context();
            readerContext.put("indexconf", indexConf);
            readerContext.put("taskcontext", context);
            setDumpFileTitles(context, readerContext);
            logger.info("----------readerContext:" + readerContext.toString());
            readerFactory.init(readerContext);
            final long  totalBuildSize = readerFactory.getTotalSize();
           // totalBuildSize.set(readerFactory.getTotalSize());
            final IInputDocCreator inputDocCreator = AbstractInputDocCreator.createDocumentCreator(
                    indexMetaConfig.schemaParse.getDocumentCreatorType()
                    , indexMetaConfig.rawDataProcessor, indexMetaConfig.indexSchema, createNewDocVersion(indexConf));
            // 多线程构建document
            for (int i = 0; i < docMakerCount; i++) {
                LuceneDocMaker luceneDocMaker = new LuceneDocMaker("docMaker-" + i, indexConf
                        , indexMetaConfig.indexSchema, inputDocCreator, messages, counters, docPoolQueues, readerFactory, aliveDocMakerCount);
                initialDataprocess(luceneDocMaker);
                luceneDocMaker.setName(indexConf.getCoreName() + "-docMaker-" + docMakerCount + "-" + i);
                executorService.submit(luceneDocMaker, luceneDocMaker.getResultFlag());
            }

            mergeTask = waitIndexMergeTask("mergetask-1", indexConf, aliveIndexMakerCount, counters, messages, dirQueue, indexMetaConfig.indexSchema);
            List<IndexMaker> indexMakers = Lists.newArrayList();
            // 多线程构建索引
            for (int i = 0; i < indexMakerCount; i++) {
                IndexMaker indexMaker = createIndexMaker(indexMetaConfig, "indexMaker-" + i, indexConf, counters
                        , messages, indexMetaConfig.indexSchema, aliveIndexMakerCount, aliveDocMakerCount, docPoolQueues, dirQueue);
                indexMakers.add(indexMaker);
                executorService.submit(indexMaker, indexMaker.getResultFlag());
            }
            final int[] preCount = new int[1];
            final long[] preSubmitConsume = new long[1];
            final int periodSec = 5;
            scheduler.scheduleAtFixedRate(() -> {
                reportIndexBuildStatus(indexConf, taskid, docQueueSize, docPoolQueues, totalBuildSize, indexMakers, preCount, preSubmitConsume, periodSec);
            }, 1, periodSec, TimeUnit.SECONDS);
            // try {
            int mergeTaskCount = 1;
            int allTaskCount = indexMakerCount + docMakerCount + mergeTaskCount;
            SuccessFlag result = null;
            for (// 
                    int threadCount = 0; //
                    threadCount < allTaskCount; threadCount++) {
                result = this.executorService.take().get();
                logger.info("({}/{})taskcomplete name:{},state:{},msg:{}", (threadCount + 1), allTaskCount, result.getName(), result.getFlag(), result.getMsg());
                if (result.getFlag() != Flag.SUCCESS) {
                    // return new TaskReturn(TaskReturn.ReturnCode.FAILURE, result.getMsg());
                    throw new IndexBuildException(result.getMsg());
                }
            }
            this.reportIndexBuildStatus(indexConf, taskid, docQueueSize, docPoolQueues, totalBuildSize, indexMakers, preCount, preSubmitConsume, periodSec);
            logger.info("shard:{} indexbuild complete all doc count:{}", indexConf.getCoreName(), indexMakers.stream().mapToInt((r) -> r.docMakeCount).sum());

            // return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");
        } catch (IndexBuildException ee) {
            throw ee;
        } catch (Throwable e1) {
            // return tr;
            throw new RuntimeException(e1);
        } finally {
            if (mergeTask != null) {
                mergeTask.shutdown();
            }
            try {
                scheduler.shutdownNow();
            } catch (Throwable e) {
            }
            try {
                // 终止所有线程执行
                execService.shutdownNow();
                execService.awaitTermination(10l, TimeUnit.SECONDS);
                logger.info("execService shutdown successful");
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    private void reportIndexBuildStatus(IndexConf indexConf, int taskid, int docQueueSize, BlockingQueue<SolrDocPack> docPoolQueues
            , long totalBuildSize, List<IndexMaker> indexMakers, int[] preCount, long[] preSubmitConsume, int periodSec) {
        int current = indexMakers.stream().mapToInt((r) -> r.docMakeCount).sum();
        long allConsume = indexMakers.stream().mapToLong((r) -> r.allConsumeTimemillis).sum();
        int allcount;
        if (preCount[0] >= 0 && (allcount = (current - preCount[0])) > 0) {
            long submitConsume = (allConsume - preSubmitConsume[0]);
            int speed = (allcount) / periodSec;
            logger.info("docMaker rate:{}r/s,queue:[used:{}/all:{}],adddoc RT:{}ms/r", speed
                    , (docQueueSize - docPoolQueues.remainingCapacity()), docQueueSize, (submitConsume / allcount));
            BuildSharedPhaseStatus buildStatus = new BuildSharedPhaseStatus();
            buildStatus.setAllBuildSize(totalBuildSize);
            buildStatus.setBuildReaded(current);
            buildStatus.setSharedName(indexConf.getCoreName());
            buildStatus.setWaiting(false);
            buildStatus.setTaskid(taskid);
            StatusRpcClient.AssembleSvcCompsite feedback = statusRpc.get();
            feedback.reportBuildIndexStatus(buildStatus);
        }
        preCount[0] = current;
        preSubmitConsume[0] = allConsume;
    }

    private static final String KEY_SOLR_CONFIG = "solrconfig";

    /**
     * 取得schema对象
     *
     * @param indexConf
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void getIndexSchema(IndexConf indexConf, IndexMetaConfig indexMetaConfig) throws Exception {

        Reader schemaInputStream = null;

        File schemaFile = this.schemaCfgFileGetter.get(indexConf);
        logger.warn(" local schema path ==>" + schemaFile.getAbsolutePath());
        try {
            try (InputStream solrinputStream = IndexBuilderTask.class.getResourceAsStream("solr-config-template.xml")) {
                Objects.requireNonNull(solrinputStream, "solr-config-template.xml can not be null");
                String schemaContent = FileUtils.readFileToString(schemaFile, TisUTF8.get());
                schemaInputStream = new StringReader(schemaContent);
                indexMetaConfig.indexSchema = new IndexSchema(indexConf.getSchemaName()
                        , new InputSource(schemaInputStream), Version.LATEST, createSolrResourceLoader(indexConf, solrinputStream), new Properties());
            }
        } finally {
            IOUtils.closeQuietly(schemaInputStream);
        }
        indexMetaConfig.schemaParse = SolrFieldsParser.parse(() -> FileUtils.readFileToByteArray(schemaFile)).getSchemaParseResult();
    }


    private IndexMetaConfig parseIndexMetadata(TaskContext context, IndexConf indexConf) {
        IndexMetaConfig indexMetaConfig = new IndexMetaConfig();
        try {
            getIndexSchema(indexConf, indexMetaConfig);
            List<ProcessorSchemaField> processorSchemas = indexMetaConfig.schemaParse.getProcessorSchemas();
            final RawDataProcessor rawDataProcessor = new RawDataProcessor();
            indexMetaConfig.rawDataProcessor = rawDataProcessor;
            ExternalDataColumnProcessor processor = null;
            for (ProcessorSchemaField ps : processorSchemas) {
                processor = ExternalDataColumnProcessor.create(ps, indexMetaConfig.schemaParse);
                if (ps.isTargetColumnEmpty()) {
                    rawDataProcessor.addRowProcessor(processor);
                } else {
                    rawDataProcessor.addColumnProcessor(ps.getTargetColumn(), processor);
                }
            }
            // }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        indexMetaConfig.validate();
        return indexMetaConfig;
    }

    private static class IndexMetaConfig {

        private IndexSchema indexSchema;

        private ParseResult schemaParse;

        private RawDataProcessor rawDataProcessor;

        // public String[] getSchemaFields() {
        // return this.indexSchema.getFields().keySet().toArray(new String[0]);
        // }
        private void validate() {
            logger.info(rawDataProcessor.toString());
            if (indexSchema == null || schemaParse == null || rawDataProcessor == null) {
                throw new IllegalStateException("schemaParse == null || rawDataProcessor == null	");
            }
        }
    }

    private String createNewDocVersion(IndexConf indexConf) {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            // String.valueOf( Long.parseLong());
            return dataFormat.format(dataFormat.parse(indexConf.getIncrTime()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    private final // 
    IndexMaker createIndexMaker(// 
                                IndexMetaConfig indexMetaConfig, String name, IndexConf indexConf, Counters counters
            , Messages messages, final IndexSchema indexSchema, AtomicInteger aliveIndexMakerCount, AtomicInteger aliveDocMakerCount
            , final BlockingQueue<SolrDocPack> docPoolQueues, BlockingQueue<RAMDirectory> dirQueue) throws Exception {
        String indexMakerClassName = indexMetaConfig.schemaParse.getIndexMakerClassName();
        if (ParseResult.DEFAULT.equals(indexMakerClassName)) {
           // logger.info("indexMakerClassName:{}", IndexMaker.class);
            return new IndexMaker(name, indexConf, indexSchema, messages, counters, dirQueue, docPoolQueues, aliveDocMakerCount, aliveIndexMakerCount);
        } else {
            Class<IndexMaker> clazz = (Class<IndexMaker>) Class.forName(indexMakerClassName);
            logger.info("indexMakerClassName:{}", clazz);
            Constructor<IndexMaker> cnstrt = (Constructor<IndexMaker>) clazz.getConstructor(String.class, IndexConf.class
                    , IndexSchema.class, Messages.class, Counters.class, BlockingQueue.class, BlockingQueue.class, AtomicInteger.class, AtomicInteger.class);
            return cnstrt.newInstance(name, indexConf, indexSchema, messages, counters, dirQueue, docPoolQueues, aliveDocMakerCount, aliveIndexMakerCount);
        }
    }

    /**
     * @return
     */
    protected SolrResourceLoader createSolrResourceLoader(final IndexConf indexConf, InputStream solrinputStream) {


        return new TisSolrResourceLoader(null, null, indexConf.getCollectionName()) {

            @Override
            public InputStream openResource(String resource) throws IOException {
                // 希望在服务端不需要校验schema的正确性
                if (StringUtils.equals("dtd/solrschema.dtd", resource)) {
                    return new ByteArrayInputStream(new byte[0]);
                }
                if (KEY_SOLR_CONFIG.equals(resource)) {
                    return solrinputStream;
                }
                return super.openResource(resource);
            }
        };
    }

    /**
     * baisui
     *
     * @param context
     * @param readerContext
     * @throws IOException
     */
    protected void setDumpFileTitles(TaskContext context, Context readerContext) throws IOException {
        String buildtabletitleitems = context.getUserParam(IndexBuildParam.INDEXING_BUILD_TABLE_TITLE_ITEMS);
        if (StringUtils.isBlank(buildtabletitleitems)) {
            throw new IllegalStateException(" indexing.buildtabletitleitems shall be set in user param ");
        }
        logger.info(IndexBuildParam.INDEXING_BUILD_TABLE_TITLE_ITEMS + ":" + buildtabletitleitems);
        readerContext.put("titletext", StringUtils.split(buildtabletitleitems, ","));
    }

    private IndexMerger waitIndexMergeTask(String name, final IndexConf indexConf, AtomicInteger aliveIndexMakerCount
            , Counters counters, Messages messages, BlockingQueue<RAMDirectory> dirQueue, IndexSchema schema) throws Exception {
        if (schema == null) {
            throw new IllegalArgumentException("schema can not be null");
        }
        final ClassLoader currentClassloader = Thread.currentThread().getContextClassLoader();
        // (IndexMerger)
        IndexMergerImpl indexMerger = new IndexMergerImpl(name, schema, this.propsGetter.getFileSystem());
        // clazz.newInstance();
        indexMerger.setAtomicInteger(aliveIndexMakerCount);
        indexMerger.setCounters(counters);
        indexMerger.setMessages(messages);
        // indexMerger.setDiskDir(mergeIndexPath);
        indexMerger.setIndexConf(indexConf);
        // indexMerger.setMergerAllocator(mergerAllocator);
        indexMerger.setDirQueue(dirQueue);
        indexMerger.init();
        logger.warn("indexmergeloader:" + currentClassloader.getClass());
        executorService.submit(indexMerger);
        return indexMerger;
    }

    public void setSchemaCfgFileGetter(ILocalTmpCfgFileGetter schemaCfgFileGetter) {
        this.schemaCfgFileGetter = schemaCfgFileGetter;
    }

    /**
     * @param luceneDocMaker
     */
    private void initialDataprocess(LuceneDocMaker luceneDocMaker) {
    }

}
