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
package com.qlangtech.tis.indexbuilder.index.nestdoc;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;
import com.taobao.terminator.build.metrics.Counters;
import com.taobao.terminator.build.metrics.Messages;
import com.qlangtech.tis.indexbuilder.index.IndexMaker;
import com.qlangtech.tis.indexbuilder.map.HdfsIndexBuilder;
import com.qlangtech.tis.indexbuilder.map.IndexConf;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NestDocIndexBuilder extends HdfsIndexBuilder {

    /**
     * @throws IOException
     */
    public NestDocIndexBuilder() throws IOException {
        super();
    }

    protected IndexMaker createIndexMaker(IndexConf indexConf, Counters counters, Messages messages, final IndexSchema indexSchema, AtomicInteger aliveIndexMakerCount, AtomicInteger aliveDocMakerCount, final BlockingQueue<SolrInputDocument> docPoolQueues, BlockingQueue<RAMDirectory> dirQueue) {
        return new NestIndexMaker(indexConf, indexSchema, messages, counters, dirQueue, docPoolQueues, aliveDocMakerCount, aliveIndexMakerCount);
    }
}
