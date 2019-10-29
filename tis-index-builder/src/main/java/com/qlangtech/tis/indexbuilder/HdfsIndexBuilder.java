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
package com.qlangtech.tis.indexbuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.build.task.TaskReturn;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.columnProcessor.ExternalDataColumnProcessor;
import com.qlangtech.tis.indexbuilder.doc.IInputDocCreator;
import com.qlangtech.tis.indexbuilder.doc.LuceneDocMaker;
import com.qlangtech.tis.indexbuilder.doc.impl.AbstractInputDocCreator;
import com.qlangtech.tis.indexbuilder.index.IndexMaker;
import com.qlangtech.tis.indexbuilder.index.IndexMerger;
import com.qlangtech.tis.indexbuilder.map.HdfsIndexGetConfig;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.RawDataProcessor;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag.Flag;
import com.qlangtech.tis.indexbuilder.merger.IndexMergerImpl;
import com.qlangtech.tis.indexbuilder.source.impl.HDFSReaderFactory;
import com.qlangtech.tis.indexbuilder.utils.Context;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;

/*
 * HdfsIndexBuilder 索引入口map类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsIndexBuilder implements TaskMapper {

	public static final Logger logger = LoggerFactory.getLogger(HdfsIndexBuilder.class);
	public static final String KEY_COLLECTION = "app";
	long startTime;
	private Integer allRowCount;

	private final ExecutorService execService = Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			return t;
		}
	});
	private final CompletionService<SuccessFlag> executorService = new ExecutorCompletionService<SuccessFlag>(
			execService);

	public static void setMdcAppName(String appname) {
		MDC.put(KEY_COLLECTION, appname);
	}

	// 由consel传入的taskid
	private String taskid = "";
	// private Future<SuccessFlag> mergeExecResult;

	public HdfsIndexBuilder() throws IOException {
		startTime = System.currentTimeMillis();
	}

	@Override
	public TaskReturn map(TaskContext context) {

		IndexConf indexConf = HdfsIndexGetConfig.getIndexConf(context);
		IndexMerger mergeTask = null;
		Counters counters = context.getCounters();
		Messages messages = context.getMessages();
		// indexConf.loadFrom(context);
		IndexMetaConfig indexMetaConfig = parseIndexMetadata(context, indexConf);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		try {
			// String taskAttemptId = context.getInnerParam("task.map.task.id");
			// String configFile = context.getUserParam("configFile");

			String rowCount = context.getUserParam(IndexBuildParam.INDEXING_ROW_COUNT);
			if (StringUtils.isNotBlank(rowCount)) {
				this.allRowCount = Integer.parseInt(rowCount);
			}

			taskid = context.getUserParam("indexing.taskid");

			logger.warn("indexMaker.flushCountThreshold:" + indexConf.getFlushCountThreshold()
					+ ",indexMaker.flushSizeThreshold:" + indexConf.getFlushSizeThreshold());

			// final IndexSchema indexSchema = getIndexSchema(indexConf);

			String[] schemaFields = indexMetaConfig.getSchemaFields();
			// 开始构建索引。。。
			logger.warn("[taskid:" + taskid + "]" + indexConf.getCoreName() + " indexing start......");

			int docMakerCount = indexConf.getDocMakerThreadCount();
			int indexMakerCount = indexConf.getIndexMakerThreadCount();
			logger.warn("----indexMakerCount=" + indexMakerCount);
			logger.warn("----docMakerCount=" + docMakerCount);

			AtomicInteger aliveIndexMakerCount = new AtomicInteger(indexMakerCount);
			AtomicInteger aliveDocMakerCount = new AtomicInteger(docMakerCount);
			// 存放document对象池的队列
			final BlockingQueue<SolrInputDocument> docPoolQueues = new ArrayBlockingQueue<SolrInputDocument>(
					indexConf.getDocQueueSize());
			// 已清空内容的可重用的document的对象池队列
			// BlockingQueue<SimpleStack<Document>> clearDocPoolQueues = new
			// ArrayBlockingQueue<SimpleStack<Document>>(
			// indexConf.getDocQueueSize() * 2 + 10);
			logger.info("RamDirQueueSize:" + indexConf.getRamDirQueueSize());
			BlockingQueue<RAMDirectory> dirQueue = new ArrayBlockingQueue<RAMDirectory>(indexConf.getRamDirQueueSize());
			HDFSReaderFactory readerFactory = new HDFSReaderFactory();
			readerFactory.setIndexSchema(indexMetaConfig.indexSchema);
			Context readerContext = new Context();
			readerContext.put("schemaFields", schemaFields);
			readerContext.put("indexconf", indexConf);
			readerContext.put("taskcontext", context);
			// readerContext.put("fieldsequence", fieldSequence);
			setDumpFileTitles(context, readerContext);
			logger.info("----------readerContext:" + readerContext.toString());
			readerFactory.init(readerContext);
			// List<SuccessFlag> resultFlagSet = new ArrayList<SuccessFlag>();
			final IInputDocCreator inputDocCreator = AbstractInputDocCreator.createDocumentCreator(
					indexMetaConfig.schemaParse.getDocumentCreatorType(), indexMetaConfig.rawDataProcessor,
					indexMetaConfig.indexSchema, createNewDocVersion(indexConf));
			// 多线程构建document
			for (int i = 0; i < docMakerCount; i++) {
				/*
				 * SourceRecordReader srreader = new HdfsSourceRecordReader(fs,
				 * splitQueues, fieldSequence, indexConf.getDelimiter());
				 */
				LuceneDocMaker luceneDocMaker = new LuceneDocMaker("docMaker-" + i, indexConf,
						indexMetaConfig.indexSchema, inputDocCreator, messages, counters, docPoolQueues, // clearDocPoolQueues,
						readerFactory, aliveDocMakerCount);
				initialDataprocess(luceneDocMaker);
				// luceneDocMaker.setConfigFile(configFile);
				luceneDocMaker.setName(indexConf.getCoreName() + "-docMaker-" + docMakerCount + "-" + i);
				// resultFlagSet.add(luceneDocMaker.getResultFlag());

				executorService.submit(luceneDocMaker, luceneDocMaker.getResultFlag());
			}
			// merge线程
			// this.mergeExecResult =

			mergeTask = waitIndexMergeTask("mergetask-1", indexConf, aliveIndexMakerCount, counters, messages, dirQueue,
					indexMetaConfig.indexSchema);

			List<IndexMaker> indexMakers = Lists.newArrayList();
			// 多线程构建索引
			for (int i = 0; i < indexMakerCount; i++) {
				IndexMaker indexMaker = createIndexMaker(indexMetaConfig, "indexMaker-" + i, indexConf, counters,
						messages, indexMetaConfig.indexSchema, aliveIndexMakerCount, aliveDocMakerCount, docPoolQueues,
						dirQueue);
				indexMakers.add(indexMaker);
				executorService.submit(indexMaker, indexMaker.getResultFlag());
			}
			
			final int[] preCount = new int[1];
			scheduler.scheduleAtFixedRate(() -> {
				int current = indexMakers.stream().mapToInt((r) -> r.docMakeCount).sum();
				if (preCount[0] >= 0) {
					logger.info("docMaker rate:{}r/s", (current - preCount[0]) / 5);
				}
				preCount[0] = current;
			}, 20, 5, TimeUnit.SECONDS);
			
			// try {
			int mergeTaskCount = 1;
			int allTaskCount = indexMakerCount + docMakerCount + mergeTaskCount;
			SuccessFlag result = null;
			for (int threadCount = 0; threadCount < allTaskCount //
			; threadCount++) {
				result = this.executorService.take().get();
				logger.info("({}/{})taskcomplete name:{},state:{},msg:{}", (threadCount + 1), allTaskCount,
						result.getName(), result.getFlag(), result.getMsg());
				if (result.getFlag() != Flag.SUCCESS) {
					return new TaskReturn(TaskReturn.ReturnCode.FAILURE, result.getMsg());
				}
			}

			return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");

		} catch (Throwable e1) {

			logger.error("[taskid:" + taskid + "] build error:", e1);
			context.getMessages().addMessage(Messages.Message.ERROR_MSG, e1.toString());
			TaskReturn tr = new TaskReturn(TaskReturn.ReturnCode.FAILURE, e1.toString());
			return tr;
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

		InputStream schemaInputStream = null;
		File schemaFile = HdfsIndexGetConfig.getLocalTmpSchemaFile();
		logger.warn(" local schema path ==>" + schemaFile.getAbsolutePath());

		try {

			try (InputStream solrinputStream = this.getClass().getClassLoader()
					.getResourceAsStream("solr-config-template.xml")) {
				SolrConfig solrConfig = new SolrConfig(createSolrResourceLoader(), "solrconfig",
						new InputSource(solrinputStream));

				schemaInputStream = FileUtils.openInputStream(schemaFile);// new

				indexMetaConfig.indexSchema = new IndexSchema(solrConfig, indexConf.getSchemaName(),
						new InputSource(schemaInputStream));
			}
		} finally {
			IOUtils.closeQuietly(schemaInputStream);
		}

		try (InputStream is = new FileInputStream(schemaFile)) {
			SolrFieldsParser parse = new SolrFieldsParser();
			indexMetaConfig.schemaParse = parse.parseSchema(is, false);
		}
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

		public String[] getSchemaFields() {
			return this.indexSchema.getFields().keySet().toArray(new String[0]);
		}

		private void validate() {
			logger.info(rawDataProcessor.toString());
			if (indexSchema == null || schemaParse == null || rawDataProcessor == null) {
				throw new IllegalStateException(
						"indexSchema == null || schemaParse == null || rawDataProcessor == null	");
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
	private final IndexMaker createIndexMaker(IndexMetaConfig indexMetaConfig //
			, String name, IndexConf indexConf, Counters counters, Messages messages, final IndexSchema indexSchema,
			AtomicInteger aliveIndexMakerCount, AtomicInteger aliveDocMakerCount,
			final BlockingQueue<SolrInputDocument> docPoolQueues, BlockingQueue<RAMDirectory> dirQueue)
			throws Exception {

		String indexMakerClassName = indexMetaConfig.schemaParse.getIndexMakerClassName();
		if (ParseResult.DEFAULT.equals(indexMakerClassName)) {
			logger.info("indexMakerClassName:{}", IndexMaker.class);
			return new IndexMaker(name, indexConf, indexSchema, messages, counters, dirQueue, docPoolQueues,
					aliveDocMakerCount, aliveIndexMakerCount);
		} else {
			Class<IndexMaker> clazz = (Class<IndexMaker>) Class.forName(indexMakerClassName);
			logger.info("indexMakerClassName:{}", clazz);
			Constructor<IndexMaker> cnstrt = (Constructor<IndexMaker>) clazz.getConstructor(String.class,
					IndexConf.class, IndexSchema.class, Messages.class, Counters.class, BlockingQueue.class,
					BlockingQueue.class, AtomicInteger.class, AtomicInteger.class);
			return cnstrt.newInstance(name, indexConf, indexSchema, messages, counters, dirQueue, docPoolQueues,
					aliveDocMakerCount, aliveIndexMakerCount);
		}

	}

	/**
	 * @return
	 */
	protected SolrResourceLoader createSolrResourceLoader() {
		return new SolrResourceLoader(null) {

			@Override
			public InputStream openResource(String resource) throws IOException {
				// 希望在服务端不需要校验schema的正确性
				if (StringUtils.equals("dtd/solrschema.dtd", resource)) {
					return new ByteArrayInputStream(new byte[0]);
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

	private IndexMerger waitIndexMergeTask(String name, final IndexConf indexConf, AtomicInteger aliveIndexMakerCount,
			Counters counters, Messages messages, BlockingQueue<RAMDirectory> dirQueue, IndexSchema schema)
			throws Exception {
		if (schema == null) {
			throw new IllegalArgumentException("schema can not be null");
		}
		final ClassLoader currentClassloader = Thread.currentThread().getContextClassLoader();
		// (IndexMerger)
		IndexMerger indexMerger = new IndexMergerImpl(name, schema);
		// clazz.newInstance();
		indexMerger.setAtomicInteger(aliveIndexMakerCount);
		indexMerger.setCounters(counters);
		indexMerger.setMessages(messages);
		// indexMerger.setDiskDir(mergeIndexPath);
		indexMerger.setIndexConf(indexConf);
		// indexMerger.setMergerAllocator(mergerAllocator);
		indexMerger.setDirQueue(dirQueue);

		logger.warn("indexmergeloader:" + currentClassloader.getClass());
		executorService.submit(indexMerger);
		return indexMerger;

	}

	/**
	 * @param luceneDocMaker
	 */
	private void initialDataprocess(LuceneDocMaker luceneDocMaker) {

	}

	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		System.out.println(sdf.format(new Date(System.currentTimeMillis())));
	}

}
