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
package com.qlangtech.tis.indexbuilder.index.nestdoc.s4personas;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;
import com.taobao.terminator.build.metrics.Counters;
import com.taobao.terminator.build.metrics.Messages;
import com.qlangtech.tis.indexbuilder.index.nestdoc.NestIndexMaker;
import com.qlangtech.tis.indexbuilder.map.IndexConf;

/*
 * 可能有部分nestdocument的parent文档为空
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class S4personasNestIndexMaker extends NestIndexMaker {

    public S4personasNestIndexMaker(IndexConf indexConf, IndexSchema indexSchema, Messages messages, Counters counters, BlockingQueue<RAMDirectory> ramDirQueue, BlockingQueue<SolrInputDocument> docPoolQueues, AtomicInteger aliveDocMakerCount, AtomicInteger aliveIndexMakerCount) {
        super(indexConf, indexSchema, messages, counters, ramDirQueue, docPoolQueues, aliveDocMakerCount, aliveIndexMakerCount);
    }
    // @Override
    // protected List<Document> getLuceneDocument(SolrInputDocument doc, IndexSchema schema) {
    // 
    // try {
    // List<Document> allDocs = new ArrayList<>();
    // List<SolrInputDocument> allSolrDocs = flatten(doc);
    // String idField = getHashableId(doc, schema);
    // 
    // for (SolrInputDocument aDoc : allSolrDocs) {
    // aDoc.setField("_root_", idField == null ? "-1" : idField);
    // allDocs.add(DocumentBuilder.toDocument(aDoc, schema));
    // }
    // return allDocs;
    // } catch (Exception e) {
    // throw new RuntimeException(doc.toString(), e);
    // }
    // }
    // protected void recUnwrapp(List<SolrInputDocument> unwrappedDocs, SolrInputDocument currentDoc) {
    // String idField = getHashableId(currentDoc, this.indexSchema);
    // 
    // List<SolrInputDocument> children = currentDoc.getChildDocuments();
    // if (children != null) {
    // for (SolrInputDocument child : children) {
    // recUnwrapp(unwrappedDocs, child);
    // }
    // }
    // 
    // if (idField != null) {
    // unwrappedDocs.add(currentDoc);
    // }
    // }
}
