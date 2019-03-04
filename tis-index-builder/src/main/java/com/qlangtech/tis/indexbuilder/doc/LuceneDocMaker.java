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
package com.qlangtech.tis.indexbuilder.doc;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.indexbuilder.exception.FieldException;
import com.qlangtech.tis.indexbuilder.exception.RowException;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag;
import com.qlangtech.tis.indexbuilder.source.SourceReader;
import com.qlangtech.tis.indexbuilder.source.SourceReaderFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LuceneDocMaker implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(LuceneDocMaker.class);

	protected static long startTime = 0L;

	private SourceReaderFactory readerFactory;

	BlockingQueue<SolrInputDocument> docPoolQueue;

	private AtomicInteger aliveDocMakerCount;

	private final SuccessFlag successFlag = new SuccessFlag();

	private String name;

	private Counters counters;

	private Messages messages;

	// private DataProcessor dataprocessor;
	private IInputDocCreator documentCreator;

	private Map<String, SchemaField> fieldMap;

	private float DEFAULT_DOCUMENT_BOOST;

	// private int docPoolSize;
	private IndexConf indexConf;

	private IndexSchema indexSchema;

	// private final String uniqueKeyField;
	// private final Set<String> schemaFields;
	private boolean hashMutiValue;

	public static String BOOST_NAME = "!$boost";

	int printInterval;

	// boolean filterDelete;
	// private final long newVersion;
	// private RawDataProcessor rawDataProcessor;
	// private final boolean hasRowProcessor;
	/**
	 * 取得结果标记位
	 *
	 * @return
	 */
	public SuccessFlag getResultFlag() {
		return this.successFlag;
	}

	public LuceneDocMaker(//
			IndexConf indexConf, //
			IndexSchema indexSchema, //
			IInputDocCreator documentCreator, //
			Messages messages, Counters counters, BlockingQueue<SolrInputDocument> docPoolQueue,
			SourceReaderFactory readerFactory, AtomicInteger aliveDocMakerCount) {
		this.messages = messages;
		this.counters = counters;
		this.indexConf = indexConf;
		this.indexSchema = indexSchema;
		this.documentCreator = documentCreator;
		// this.uniqueKeyField = this.indexSchema.getUniqueKeyField().getName();
		// this.rawDataProcessor = rawDataProcessor;
		// this.hasRowProcessor = (rawDataProcessor.getRowProcess().size() > 0);
		// this.schemaFields = indexSchema.getFields().keySet();
		this.docPoolQueue = docPoolQueue;
		this.aliveDocMakerCount = aliveDocMakerCount;
		this.fieldMap = indexSchema.getFields();
		this.DEFAULT_DOCUMENT_BOOST = indexConf.getDocBoost();
		this.readerFactory = readerFactory;
		this.printInterval = indexConf.getPrintInterval();

		init();
	}

	public void init() {
		for (SchemaField field : fieldMap.values()) {
			if (field.multiValued()) {
				this.hashMutiValue = true;
				break;
			}
		}
	}

	@Override
	public void run() {
		try {
			doRun();
		} catch (Throwable e) {
			messages.addMessage(Messages.Message.ERROR_MSG, "doc maker fatal error:" + e.toString());
			logger.error("LuceneDocMaker ", e);
			successFlag.setFlag(SuccessFlag.Flag.FAILURE);
			successFlag.setMsg("doc maker fatal error:" + e.toString());
		} finally {
			aliveDocMakerCount.decrementAndGet();
		}
	}

	private static final AtomicInteger successCount = new AtomicInteger();

	private void doRun() throws Exception {
		// int count = 0;
		int failureCount = 0;
		int filteredCount = 0;
		// int successCountCore = 0;
		// boolean[] coreFull = new boolean[cores.length];
		// int index = 0;
		// docPool是否已经放到了queue中。
		Map<String, String> row = null;
		while (true) {

			SourceReader recordReader = readerFactory.nextReader();
			if (recordReader == null) {
				successFlag.setFlag(SuccessFlag.Flag.SUCCESS);
				counters.setCounterValue(Counters.Counter.DOCMAKE_COMPLETE, successCount.get());
				logger.warn(name + ":filtered:" + filteredCount);
				// 已处理完成
				return;
			}
			// }
			SolrInputDocument solrDoc = null;
			while (true) {
				try {
					// createSolrInputDocument(recordReader);
					solrDoc = this.documentCreator.createSolrInputDocument(recordReader);
					if (solrDoc == null) {
						break;
					}
					docPoolQueue.put(solrDoc);
					successCount.incrementAndGet();

					if ((successCount.get() % 10000) == 0) {
						counters.setCounterValue(Counters.Counter.DOCMAKE_COMPLETE, successCount.get());
						logger.warn(name + ":success:" + successCount + "failure:" + failureCount);
					}
				} catch (Throwable e) {
					logger.error("", e);
					counters.incrCounter(Counters.Counter.DOCMAKE_FAIL, 1);
					String errorMsg = "";
					if (e instanceof RowException || e instanceof FieldException) {
						errorMsg = e.toString();
					}
					messages.addMessage(Messages.Message.ERROR_MSG, "doc maker exception:" + e + errorMsg);

					if (++failureCount > indexConf.getMaxFailCount()) {
						successFlag.setFlag(SuccessFlag.Flag.FAILURE);
						successFlag.setMsg("LuceneDocMaker error:failureCount>" + indexConf.getMaxFailCount());
						// error:failureCount>"+indexConf.getMaxFailCount());
						return;
					}
				}
			}
			logger.info(recordReader.toString());
		}
	}

	/**
	 * 数据流中每次构建生成一个InputDocument对象实体
	 *
	 * @param recordReader
	 * @return
	 * @throws Exception
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void main(String[] args) {

	}
}
