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
package com.qlangtech.tis.indexbuilder.index;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.NoMergePolicy;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.update.DocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.indexbuilder.map.HdfsIndexBuilder;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexMaker implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(IndexMaker.class);

	private static final AtomicInteger failureCount = new AtomicInteger();

	// private GroupRAMDirectory[] addRamDirectorys;
	protected final IndexSchema indexSchema;

	private final AtomicInteger aliveDocMakerCount;

	// private AtomicInteger aliveIndexMakerCount;
	private IndexConf indexConf;

	// private SimpleStack<Document> docPool;
	// private SimpleStack<Document> clearDocPool;
	BlockingQueue<SolrInputDocument> docPoolQueues;

	// BlockingQueue<SimpleStack<Document>> clearDocPoolQueues;
	// 这个maker的产出物
	private BlockingQueue<RAMDirectory> ramDirQueue;

	// private String name;

	private final SuccessFlag successFlag;

	private Counters counters;

	private Messages messages;

	/**
	 * 取得结果运行标记
	 *
	 * @return
	 */
	public SuccessFlag getResultFlag() {
		return this.successFlag;
	}

	// 存活的文档生成器
	private final AtomicInteger aliveIndexMakerCount;

	public IndexMaker(String name, IndexConf indexConf, IndexSchema indexSchema, Messages messages, Counters counters, // 这个是下游的产出结果
			BlockingQueue<RAMDirectory> ramDirQueue, // 这个是上游管道
			BlockingQueue<SolrInputDocument> docPoolQueues, AtomicInteger aliveDocMakerCount, // ,
			AtomicInteger aliveIndexMakerCount)
	// makerAllocator
	{
		this.successFlag = new SuccessFlag(name);
		this.counters = counters;
		this.messages = messages;
		this.aliveDocMakerCount = aliveDocMakerCount;
		this.aliveIndexMakerCount = aliveIndexMakerCount;
		this.indexConf = indexConf;
		if (indexSchema == null) {
			throw new IllegalArgumentException("indexSchema can not be null");
		}
		this.indexSchema = indexSchema;
		// this.docPoolSize = indexConf.getDocPoolSize();
		this.docPoolQueues = docPoolQueues;
		// this.clearDocPoolQueues = clearDocPoolQueues;
		this.ramDirQueue = ramDirQueue;
		// this.clearDocPool = new SimpleStack<Document>(docPoolSize);
		// this.flushCheckInterval = indexConf.getFlushCheckInterval();
		// this.printInterval = indexConf.getPrintInterval();
		// this.routeKey = indexConf.getRouteKey();
		// cores = indexConf.getCores();
		// this.addWriter = new IndexWriter();
		// this.addRamDirectorys = new GroupRAMDirectory[cores.length];
		// this.makerAllocator = makerAllocator;
	}

	public static IndexWriter createRAMIndexWriter(IndexConf indexConf, IndexSchema schema, boolean merge)
			throws IOException {
		RAMDirectory ramDirectory = new RAMDirectory();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(schema.getIndexAnalyzer());
		indexWriterConfig.setMaxBufferedDocs(Integer.MAX_VALUE);
		indexWriterConfig.setRAMBufferSizeMB(32);

		// indexWriterConfig.setTermIndexInterval(Integer.MAX_VALUE);
		if (merge) {

			TieredMergePolicy mergePolicy = new TieredMergePolicy();
			mergePolicy.setNoCFSRatio(1.0);
			// mergePolicy.setUseCompoundFile(indexConf.getMakerUseCompoundFile());

			mergePolicy.setSegmentsPerTier(10);
			mergePolicy.setMaxMergeAtOnceExplicit(50);
			mergePolicy.setMaxMergeAtOnce(50);
			indexWriterConfig.setMergePolicy(mergePolicy);

		} else {
			indexWriterConfig.setMergePolicy(NoMergePolicy.INSTANCE);
		}

		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		IndexWriter addWriter = new IndexWriter(ramDirectory, indexWriterConfig);
		// 必须commit一下才会产生segment*文件，如果不commit，indexReader读会报错。
		addWriter.commit();
		return addWriter;
	}

	private boolean needFlush(IndexWriter writer) {
		int maxDoc = writer.maxDoc();

		long ramSize = ((RAMDirectory) writer.getDirectory()).ramBytesUsed();
		boolean overNum = maxDoc >= this.indexConf.getFlushCountThreshold();
		boolean overSize = ramSize >= this.indexConf.getFlushSizeThreshold();
		if (overNum || overSize) {
			logger.warn("ram has reach the threshold ，while Flush to disk,has Doc count{" + maxDoc + "},ram consume: {"
					+ ramSize / (1024 * 1024) + " M}");
			return true;
		}
		return false;
	}

	private // Directory ramdir
	void addRAMToMergeQueue(// Directory ramdir
			IndexWriter indexWriter) throws Exception {
		RAMDirectory ramDirectory = (RAMDirectory) indexWriter.getDirectory();
		try {
			indexWriter.commit();
			indexWriter.close();
			logger.warn("ramIndexQueueSize=" + ramDirQueue.size());
			// logger.warn("ramIndexQueueSize="+ramIndexQueue.size());
			ramDirQueue.put(ramDirectory);
		} catch (OutOfMemoryError e) {
			throw new RuntimeException("RAM has overhead:" + (ramDirectory.ramBytesUsed() / (1024 * 1024))
					+ "M,FlushCountThreshold:" + this.indexConf.getFlushCountThreshold() + ",FlushSizeThreshold:"
					+ this.indexConf.getFlushSizeThreshold(), e);
		}
	}

	public void run() {
		try {
			HdfsIndexBuilder.setMdcAppName(this.indexConf.getCollectionName());
			doRun();
		} catch (Throwable e) {
			logger.error("maker error" + e.toString(), e);
			messages.addMessage(Messages.Message.ERROR_MSG, "index maker fatal error:" + e.toString());
			successFlag.setFlag(SuccessFlag.Flag.FAILURE);
		} finally {
			aliveIndexMakerCount.decrementAndGet();
		}
	}

	public void doRun() throws IOException, InterruptedException {

		int indexMakeCount = 0;
		// for (int i = 0; i < cores.length; i++) {
		IndexWriter indexWriter = createRAMIndexWriter(this.indexConf, this.indexSchema, false/* mrege */);
		// }
		// int printCount = 0;
		SolrInputDocument solrDoc = null;
		while (true) {
			/*
			 * if (interruptFlag.flag == InterruptFlag.Flag.PAUSE) {
			 * synchronized (interruptFlag) { interruptFlag.wait(); } }
			 */
			solrDoc = docPoolQueues.poll(2, TimeUnit.SECONDS);
			try {
				if (solrDoc == null) {
					if (this.aliveDocMakerCount.get() > 0) {
						Thread.sleep(1000);
						continue;
					}
					addRAMToMergeQueue(indexWriter);
					successFlag.setFlag(SuccessFlag.Flag.SUCCESS);
					successFlag.setMsg("LuceneDocMaker success");
					printIndexMakeCount(indexMakeCount);
					return;
				}
			} catch (Exception e1) {
				logger.error("IndexMaker+" + this.successFlag.getName(), e1);
				counters.incrCounter(Counters.Counter.INDEXMAKE_FAIL, 1);
				messages.addMessage(Messages.Message.ERROR_MSG, "index maker index error:" + e1.toString());
			}
			try {
				appendDocument(indexWriter, solrDoc);
				if ((indexMakeCount++) % 10000 == 0) {
					printIndexMakeCount(indexMakeCount);
				}
				if (needFlush(indexWriter)) {
					addRAMToMergeQueue(indexWriter);
					indexWriter = createRAMIndexWriter(this.indexConf, this.indexSchema, false/* mrege */);
				}
			} catch (Exception e) {
				logger.error("IndexMaker+" + successFlag.getName(), e);
				counters.incrCounter(Counters.Counter.INDEXMAKE_FAIL, 1);
				messages.addMessage(Messages.Message.ERROR_MSG, "index maker index error:" + e.toString());
				messages.addMessage(Messages.Message.ERROR_MSG, "index maker index error, solrDoc:" + solrDoc);
				if (failureCount.incrementAndGet() > indexConf.getMaxFailCount()) {
					successFlag.setFlag(SuccessFlag.Flag.FAILURE);
					successFlag.setMsg("LuceneDocMaker error:failureCount>" + indexConf.getMaxFailCount());
					return;
				}
			}
		}
	}

	protected void appendDocument(IndexWriter indexWriter, SolrInputDocument solrDoc) throws IOException {
		indexWriter.addDocument(DocumentBuilder.toDocument(solrDoc, this.indexSchema));
	}

	/**
	 * @param indexMakeCount
	 */
	private void printIndexMakeCount(int indexMakeCount) {
		counters.incrCounter(Counters.Counter.INDEXMAKE_COMPLETE, indexMakeCount);
		counters.incrCounter(Counters.Counter.MAP_INPUT_RECORDS, indexMakeCount);
	}

}
