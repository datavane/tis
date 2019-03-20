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
package com.qlangtech.tis.indexbuilder.map;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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
	private final CompletionService<SuccessFlag> executorService = new ExecutorCompletionService<SuccessFlag>(
			Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					return t;
				}
			}));

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

		Counters counters = context.getCounters();
		Messages messages = context.getMessages();
		// indexConf.loadFrom(context);
		IndexMetaConfig indexMetaConfig = parseIndexMetadata(context, indexConf);
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

			String[] schemaFields = indexMetaConfig.indexSchema.getFields().keySet().toArray(new String[0]);
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

			waitIndexMergeTask("mergetask-1", indexConf, aliveIndexMakerCount, counters, messages, dirQueue,
					indexMetaConfig.indexSchema);

			// 多线程构建索引
			for (int i = 0; i < indexMakerCount; i++) {
				IndexMaker indexMaker = createIndexMaker("indexMaker-" + i, indexConf, counters, messages,
						indexMetaConfig.indexSchema, aliveIndexMakerCount, aliveDocMakerCount, docPoolQueues, dirQueue);
				// indexMaker.setName(indexConf.getCoreName() + "-indexMaker-" +
				// indexMakerCount + "-" + i);
				// resultFlagSet.add(indexMaker.getResultFlag());
				executorService.submit(indexMaker, indexMaker.getResultFlag());
			}

			try {
				int allTaskCount = indexMakerCount + docMakerCount + 1;// merge
																		// task;
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
				// 之前的任何一个進程出錯都會短路
			} catch (Exception e) {
				logger.warn(indexConf.getCoreName() + " dump fail!!");
				logger.error("[taskid:" + taskid + "]" + "dump fail!!" + e.getMessage());
				messages.addMessage(Messages.Message.ERROR_MSG, e.getMessage());
				return new TaskReturn(TaskReturn.ReturnCode.FAILURE, e.getMessage());
			}

			// SuccessFlag flag = mergeExecResult.get();
			// // 检查docmake 和index make 是否出错，出错的话要终止此次流程继续执行
			// for (SuccessFlag f : resultFlagSet) {
			// if (f.getFlag() == SuccessFlag.Flag.FAILURE) {
			// return new TaskReturn(TaskReturn.ReturnCode.FAILURE, f.getMsg());
			// }
			// }
			// 检查各个线程的运行状态
			// if (flag.getFlag() == SuccessFlag.Flag.SUCCESS) {
			// logger.warn("[taskid:" + taskid + "]" + indexConf.getCoreName() +
			// " dump done!!");
			// logger.warn("[taskid:" + taskid + "]" + "indexing done,take "
			// + (System.currentTimeMillis() + 1 - startTime) / (1000 * 60) + "
			// minutes!");
			// messages.addMessage(Messages.Message.ALL_TIME,
			// (System.currentTimeMillis() - startTime) / 1000 + " seconds");
			// return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");
			// }

			// logger.warn(indexConf.getCoreName() + " dump fail!!");
			// logger.error("[taskid:" + taskid + "]" + "dump fail!!" +
			// flag.getMsg());
			// messages.addMessage(Messages.Message.ERROR_MSG, flag.getMsg());
			return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");

		} catch (Throwable e1) {
			logger.error("[taskid:" + taskid + "]" + "build error:", e1);
			context.getMessages().addMessage(Messages.Message.ERROR_MSG, e1.toString());
			TaskReturn tr = new TaskReturn(TaskReturn.ReturnCode.FAILURE, e1.toString());
			return tr;
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

	// /**
	// * 索引merge是否完成
	// *
	// * @return
	// */
	// public boolean getMergeOver() {
	// if (mergeExecResult == null) {
	// return false;
	// }
	// return mergeExecResult.isDone();
	// }

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

		// private List<IIndexBuildLifeCycleHook> indexBuildLifeCycleHooks;
		private void validate() {
			logger.info(rawDataProcessor.toString());
			if (indexSchema == null || schemaParse == null || rawDataProcessor == null) // ||
																						// indexBuildLifeCycleHooks
																						// ==
																						// null
			{
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

	protected IndexMaker createIndexMaker(String name, IndexConf indexConf, Counters counters, Messages messages,
			final IndexSchema indexSchema, AtomicInteger aliveIndexMakerCount, AtomicInteger aliveDocMakerCount,
			final BlockingQueue<SolrInputDocument> docPoolQueues, BlockingQueue<RAMDirectory> dirQueue) {
		return new IndexMaker(name, indexConf, indexSchema, messages, counters, dirQueue, docPoolQueues,
				aliveDocMakerCount, aliveIndexMakerCount);
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

	private void waitIndexMergeTask(String name, final IndexConf indexConf, AtomicInteger aliveIndexMakerCount,
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

	}

	/**
	 * @param luceneDocMaker
	 */
	private void initialDataprocess(LuceneDocMaker luceneDocMaker) {

	}

	// protected SuccessFlag checkSuccessFlag(List<SuccessFlag> threadList) {
	// SuccessFlag flag = new SuccessFlag();
	// flag.setFlag(Flag.SUCCESS);
	// for (SuccessFlag sf : threadList) {
	// if (sf.getFlag() == SuccessFlag.Flag.FAILURE) {
	// return sf;
	// }
	// if (sf.getFlag() == SuccessFlag.Flag.RUNNING) {
	// flag.setFlag(Flag.RUNNING);
	// }
	// }
	// return flag;
	// }

	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		System.out.println(sdf.format(new Date(System.currentTimeMillis())));
	}
	// private boolean downloadJar() throws Exception {
	// logger.info("downloadJar start");
	// // 使用HttpConfigFileReader 工具类取得终搜后台仓库中的配置文件
	// SnapshotDomain domain = HttpConfigFileReader.getResource(
	// indexConf.getRemoteJarHost(), indexConf.getServiceName(), 0,
	// indexConf.getRunEnvironment(), ConfigFileReader.FILE_JAR);
	// logger.info("appId=" + domain.getAppId());
	// UploadResource resource = domain.getJarFile();
	// if (resource != null) {
	// logger.info("upload_resource:ur_id=" + resource.getUrId());
	// FileOutputStream fos = null;
	// FileChannel fc = null;
	// try {
	// File jarPath = new File(indexConf.getServicejarpath());
	// if (!jarPath.exists()) {
	// jarPath.mkdirs();
	// }
	// File jarFile = new File(jarPath, indexConf.getServiceName()
	// + "." + "jar");
	// if (jarFile.exists()) {
	// jarFile.delete();
	// }
	// jarFile.createNewFile();
	// fos = new FileOutputStream(jarFile);
	// fc = fos.getChannel();
	// if (null == resource.getContent()) {
	// logger.error("resource.getContent() is null,ur_id="
	// + resource.getUrId());
	// }
	// ByteBuffer bb = ByteBuffer.wrap(resource.getContent());
	// fc.write(bb);
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// } finally {
	// if (fos != null)
	// fos.close();
	// if (fc != null)
	// fc.close();
	// }
	// return true;
	// } else {
	// logger.warn("no user jar file!");
	// }
	// return false;
	// }
	// private boolean downloadTisJar() throws Exception {
	// if (!getJarStatus()) {
	// return false;
	// }
	// StringBuffer ossKey = new StringBuffer();
	// ossKey.append(JarOssClient.OSS_ROOT_KEY)
	// .append(indexConf.getRunEnvironment().getKeyName()).append("_")
	// .append(indexConf.getServiceName());
	// StringBuffer destPath = new StringBuffer();
	// destPath.append(indexConf.getServicejarpath()).append(File.separator)
	// .append(indexConf.getServiceName()).append(".jar");
	//
	// logger.info("downloadTisJar start");
	//
	// try {
	// File jarPath = new File(indexConf.getServicejarpath());
	// if (!jarPath.exists()) {
	// jarPath.mkdirs();
	// }
	// File jarFile = new File(jarPath, indexConf.getServiceName() + "."
	// + "jar");
	// if (jarFile.exists()) {
	// jarFile.delete();
	// }
	// jarOssClient.download(ossKey.toString(), destPath.toString());
	// logger.info("ossKey : " + ossKey + " ---- destPath : " + destPath);
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// return true;
	//
	// }
	// private boolean getJarStatus() {
	// try {
	// StringBuffer ossKey = new StringBuffer();
	// ossKey.append(JarOssClient.OSS_ROOT_KEY)
	// .append(indexConf.getRunEnvironment().getKeyName())
	// .append("_").append(indexConf.getServiceName());
	// StringBuffer ossStatus = new StringBuffer();
	// ossStatus.append(ossKey).append("_status");
	// if (!jarOssClient.getJarStatus(ossStatus.toString())) {
	// return false;
	// }
	// } catch (Exception e) {
	// return false;
	// }
	// return true;
	// }
}
