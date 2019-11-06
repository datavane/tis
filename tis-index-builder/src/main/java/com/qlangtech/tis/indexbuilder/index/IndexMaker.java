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
import com.qlangtech.tis.indexbuilder.HdfsIndexBuilder;
import com.qlangtech.tis.indexbuilder.doc.SolrDocPack;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexMaker implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(IndexMaker.class);

	private static final AtomicInteger failureCount = new AtomicInteger();

	protected final IndexSchema indexSchema;

	private final AtomicInteger aliveDocMakerCount;

	private IndexConf indexConf;

	private final BlockingQueue<SolrDocPack> docPoolQueues;

	// 这个maker的产出物
	private BlockingQueue<RAMDirectory> ramDirQueue;

	private final SuccessFlag successFlag;

	private Counters counters;

	private Messages messages;

	public int docMakeCount;
	public int preDocMakeCount;
	// 所以提交时间总和
	public long allConsumeTimemillis;

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
			BlockingQueue<SolrDocPack> docPoolQueues, AtomicInteger aliveDocMakerCount, // ,
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
		this.docPoolQueues = docPoolQueues;
		this.ramDirQueue = ramDirQueue;
	}

	// private IndexWriter getRAMIndexWriter(IndexConf indexConf, IndexSchema
	// schema, IndexWriter oldWriter,
	// boolean create) throws Exception {
	// if (create) {
	// return createRAMIndexWriter(indexConf, schema, false/* mrege */);
	// } else {
	// if ((this.docMakeCount % 5000 == 0) && needFlush(oldWriter)) {
	// synchronized (oldWriter) {
	// if ((this.docMakeCount % 5000 == 0) && needFlush(oldWriter)) {
	// addRAMToMergeQueue(oldWriter);
	// return createRAMIndexWriter(this.indexConf, this.indexSchema, false/*
	// mrege */);
	// } else {
	// return oldWriter;
	// }
	// }
	// } else {
	// return oldWriter;
	// }
	// }
	// }

	public static IndexWriter createRAMIndexWriter(IndexConf indexConf, IndexSchema schema, boolean merge)
			throws IOException {
		RAMDirectory ramDirectory = new RAMDirectory();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(schema.getIndexAnalyzer());
		indexWriterConfig.setMaxBufferedDocs(Integer.MAX_VALUE);
		indexWriterConfig.setRAMBufferSizeMB(IndexWriterConfig.DISABLE_AUTO_FLUSH);

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
			indexWriterConfig.setUseCompoundFile(false);
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

		int[] failureCount = new int[1];
		IndexWriter indexWriter = createRAMIndexWriter(this.indexConf, this.indexSchema, false/* mrege */);

		SolrDocPack docPack = null;
		while (true) {
			/*
			 * if (interruptFlag.flag == InterruptFlag.Flag.PAUSE) {
			 * synchronized (interruptFlag) { interruptFlag.wait(); } }
			 */
			docPack = docPoolQueues.poll(2, TimeUnit.SECONDS);
			try {
				if (docPack == null) {
					if (this.aliveDocMakerCount.get() > 0) {
						Thread.sleep(1000);
						continue;
					}
					addRAMToMergeQueue(indexWriter);
					successFlag.setFlag(SuccessFlag.Flag.SUCCESS);
					successFlag.setMsg("LuceneDocMaker success");
					return;
				}
			} catch (Exception e1) {
				logger.error("IndexMaker+" + this.successFlag.getName(), e1);
				counters.incrCounter(Counters.Counter.INDEXMAKE_FAIL, 1);
				messages.addMessage(Messages.Message.ERROR_MSG, "index maker index error:" + e1.toString());
			}
			try {

				if (!appendDocument(indexWriter, docPack, failureCount)) {
					// 有错误中断了
					return;
				}

				if ((this.docMakeCount > (this.preDocMakeCount + 5000)) && needFlush(indexWriter)) {
					addRAMToMergeQueue(indexWriter);
					indexWriter = createRAMIndexWriter(this.indexConf, this.indexSchema, false/* mrege */);
					preDocMakeCount = this.docMakeCount;
				}

			} catch (Exception e) {
				logger.error("IndexMaker+" + successFlag.getName(), e);
				counters.incrCounter(Counters.Counter.INDEXMAKE_FAIL, 1);
				messages.addMessage(Messages.Message.ERROR_MSG, "index maker index error:" + e.toString());
				// messages.addMessage(Messages.Message.ERROR_MSG, "index maker
				// index error, solrDoc:" + docPack);
				if (failureCount[0]++ > indexConf.getMaxFailCount()) {
					successFlag.setFlag(SuccessFlag.Flag.FAILURE);
					successFlag.setMsg("LuceneDocMaker error:failureCount>" + indexConf.getMaxFailCount());
					return;
				}
			}
		}
	}

	private final boolean appendDocument(final IndexWriter indexWriter, SolrDocPack docPack, int[] failureCount)
			throws Exception {
		long current = System.currentTimeMillis();
		try {
			SolrInputDocument inputDoc = null;
			for (int i = 0; i <= docPack.getCurrentIndex(); i++) {
				inputDoc = docPack.getDoc(i);
				try {
					writeSolrInputDocument(indexWriter, inputDoc);
					this.docMakeCount++;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					messages.addMessage(Messages.Message.ERROR_MSG, "index maker index error, solrDoc:" + inputDoc);
					if (++failureCount[0] > indexConf.getMaxFailCount()) {
						successFlag.setFlag(SuccessFlag.Flag.FAILURE);
						successFlag.setMsg("LuceneDocMaker error:failureCount>" + indexConf.getMaxFailCount());
						return false;
					}
				}

			}
		} finally {
			this.allConsumeTimemillis += (System.currentTimeMillis() - current);
		}
		return true;
	}

	protected void writeSolrInputDocument(IndexWriter indexWriter, SolrInputDocument inputDoc) throws IOException {

		indexWriter.addDocument(DocumentBuilder.toDocument(inputDoc, this.indexSchema));

	}
}
