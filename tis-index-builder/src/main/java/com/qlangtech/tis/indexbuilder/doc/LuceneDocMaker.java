/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.indexbuilder.doc;

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.indexbuilder.HdfsIndexBuilder;
import com.qlangtech.tis.indexbuilder.exception.FieldException;
import com.qlangtech.tis.indexbuilder.exception.RowException;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag;
import com.qlangtech.tis.indexbuilder.source.SourceReader;
import com.qlangtech.tis.indexbuilder.source.SourceReaderFactory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LuceneDocMaker implements Runnable {

    public static final Logger logger = LoggerFactory.getLogger(LuceneDocMaker.class);

    protected static long startTime = 0L;

    private SourceReaderFactory readerFactory;

    BlockingQueue<SolrDocPack> docPoolQueue;

    private AtomicInteger aliveDocMakerCount;

    private final SuccessFlag successFlag;

    private String name;

    private Counters counters;

    private Messages messages;

    private IInputDocCreator documentCreator;

    private Map<String, SchemaField> fieldMap;

    private IndexConf indexConf;

    private IndexSchema indexSchema;

    public static String BOOST_NAME = "!$boost";

    private final long newVersion;

    private static final AtomicLong allDocPutQueueTime = new AtomicLong();

    private static final AtomicLong allDocPutCount = new AtomicLong();

    /**
     * 取得结果标记位
     *
     * @return
     */
    public SuccessFlag getResultFlag() {
        return this.successFlag;
    }

    public LuceneDocMaker(String name, // 
    IndexConf indexConf, // 
    IndexSchema indexSchema, // 
    IInputDocCreator documentCreator, Messages messages, Counters counters, BlockingQueue<SolrDocPack> docPoolQueue, SourceReaderFactory readerFactory, AtomicInteger aliveDocMakerCount) {
        this.successFlag = new SuccessFlag(name);
        this.messages = messages;
        this.counters = counters;
        this.indexConf = indexConf;
        this.indexSchema = indexSchema;
        this.documentCreator = documentCreator;
        this.docPoolQueue = docPoolQueue;
        this.aliveDocMakerCount = aliveDocMakerCount;
        this.fieldMap = indexSchema.getFields();
        this.readerFactory = readerFactory;
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            this.newVersion = Long.parseLong(dataFormat.format(dataFormat.parse(indexConf.getIncrTime())));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        init();
    }

    public void init() {
    }

    @Override
    public void run() {
        try {
            HdfsIndexBuilder.setMdcAppName(this.indexConf.getCollectionName());
            doRun();
            successFlag.setMsg(SuccessFlag.Flag.SUCCESS, this.getName() + " success");
        } catch (Throwable e) {
            messages.addMessage(Messages.Message.ERROR_MSG, "doc maker fatal error:" + e.toString());
            logger.error("LuceneDocMaker ", e);
            // successFlag.setFlag(SuccessFlag.Flag.FAILURE);
            successFlag.setMsg(SuccessFlag.Flag.FAILURE, "doc maker fatal error:" + e.toString());
        } finally {
            aliveDocMakerCount.decrementAndGet();
        }
    }

    // private static final AtomicInteger successCount = new AtomicInteger();
    private void doRun() throws Exception {
        // long startDocPushTimestamp;
        // int count = 0;
        int failureCount = 0;
        int filteredCount = 0;
        // int successCountCore = 0;
        // boolean[] coreFull = new boolean[cores.length];
        // int index = 0;
        // docPool是否已经放到了queue中。
        // Map<String, String> row = null;
        SolrDocPack docPack = new SolrDocPack();
        while (true) {
            SourceReader recordReader = readerFactory.nextReader();
            if (recordReader == null) {
                logger.warn(name + ":filtered:" + filteredCount);
                if (docPack.isNotEmpty()) {
                    logger.info("add package,currentIndex:{}", docPack.getCurrentIndex());
                    docPoolQueue.put(docPack);
                }
                return;
            }
            SolrInputDocument solrDoc = null;
            while (true) {
                try {
                    solrDoc = this.documentCreator.createSolrInputDocument(recordReader);
                    if (solrDoc == null) {
                        break;
                    }
                    if (docPack.add(solrDoc)) {
                        // PACK已经装满
                        // logger.info("add package,currentIndex:{}",
                        // docPack.getCurrentIndex());
                        docPoolQueue.put(docPack);
                        docPack = new SolrDocPack();
                    }
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    counters.incrCounter(Counters.Counter.DOCMAKE_FAIL, 1);
                    String errorMsg = "";
                    if (e instanceof RowException || e instanceof FieldException) {
                        errorMsg = e.toString();
                    }
                    messages.addMessage(Messages.Message.ERROR_MSG, "doc maker exception:" + e + errorMsg);
                    if (++failureCount > indexConf.getMaxFailCount()) {
                        // successFlag.setFlag(SuccessFlag.Flag.FAILURE);
                        successFlag.setMsg(SuccessFlag.Flag.FAILURE, "LuceneDocMaker error:failureCount[" + failureCount + "]>" + indexConf.getMaxFailCount());
                        // error:failureCount>"+indexConf.getMaxFailCount());
                        return;
                    }
                }
            }
        }
    }

    /**
     * 数据流中每次构建生成一个InputDocument对象实体
     *
     * @param
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
