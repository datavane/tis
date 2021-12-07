/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.indexbuilder.index.nestdoc;

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.indexbuilder.doc.SolrDocPack;
import com.qlangtech.tis.indexbuilder.index.IndexMaker;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.update.DocumentBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NestIndexMaker extends IndexMaker {

    /**
     * @param indexConf
     * @param indexSchema
     * @param messages
     * @param counters
     * @param ramDirQueue
     * @param docPoolQueues
     * @param aliveDocMakerCount
     * @param aliveIndexMakerCount
     */
    public NestIndexMaker(String name, IndexConf indexConf, IndexSchema indexSchema, Messages messages, Counters counters, BlockingQueue<RAMDirectory> ramDirQueue, BlockingQueue<SolrDocPack> docPoolQueues, AtomicInteger aliveDocMakerCount, AtomicInteger aliveIndexMakerCount) {
        super(name, indexConf, indexSchema, messages, counters, ramDirQueue, docPoolQueues, aliveDocMakerCount, aliveIndexMakerCount);
    }

    protected void writeSolrInputDocument(IndexWriter indexWriter, SolrInputDocument inputDoc) throws IOException {
        List<Document> docs = getLuceneDocument(inputDoc, this.indexSchema);
        for (Document d : docs) {
            try {
                indexWriter.addDocument(d);
                this.docMakeCount++;
            } catch (Exception e) {
                StringBuffer docDesc = new StringBuffer();
                for (IndexableField f : d.getFields()) {
                    docDesc.append(f.name()).append(":").append(f.stringValue()).append(",");
                }
                throw new RuntimeException(docDesc.toString(), e);
            }
        }
    }

    // @Override
    // protected void appendDocument(IndexWriter indexWriter, SolrDocPack
    // docPack) throws IOException {
    // // SolrInputDocument doc = null;
    // List<Document> docs = null;
    // for (int i = 0; i <= docPack.getCurrentIndex(); i++) {
    //
    // docs = getLuceneDocument(docPack.getDoc(i), this.indexSchema);
    //
    // // indexWriter.addDocument(DocumentBuilder.toDocument(docPack.getDoc(i),
    // // this.indexSchema));
    //
    // for (Document d : docs) {
    // try {
    // indexWriter.addDocument(d);
    // this.docMakeCount++;
    // } catch (Exception e) {
    // StringBuffer docDesc = new StringBuffer();
    // for (IndexableField f : d.getFields()) {
    // docDesc.append(f.name()).append(":").append(f.stringValue()).append(",");
    // }
    // throw new RuntimeException(docDesc.toString(), e);
    // }
    // }
    //
    // }
    // List<Document> docs = getLuceneDocument(solrDoc, this.indexSchema);
    // for (Document doc : docs) {
    // try {
    // indexWriter.addDocument(doc);
    // } catch (Exception e) {
    // StringBuffer docDesc = new StringBuffer();
    // for (IndexableField f : doc.getFields()) {
    // docDesc.append(f.name()).append(":").append(f.stringValue()).append(",");
    // }
    // throw new RuntimeException(docDesc.toString(), e);
    // }
    // }
    // }
    protected List<Document> getLuceneDocument(SolrInputDocument doc, IndexSchema schema) {
        try {
            List<Document> allDocs = new ArrayList<>();
            List<SolrInputDocument> allSolrDocs = flatten(doc);
            String idField = getHashableId(doc, schema);
            for (SolrInputDocument aDoc : allSolrDocs) {
                aDoc.setField("_root_", idField);
                allDocs.add(DocumentBuilder.toDocument(aDoc, schema));
            }
            return allDocs;
        } catch (Exception e) {
            throw new RuntimeException(doc.toString(), e);
        }
    }

    protected String getHashableId(SolrInputDocument doc, IndexSchema schema) {
        SchemaField sf = schema.getUniqueKeyField();
        if (sf != null) {
            if (doc != null) {
                SolrInputField field = doc.getField(sf.getName());
                int count = field == null ? 0 : field.getValueCount();
                if (count == 0) {
                // if (overwrite) {
                // throw new
                // SolrException(SolrException.ErrorCode.BAD_REQUEST,
                // "Document is missing mandatory uniqueKey field: "
                // + sf.getName());
                // }
                } else if (count > 1) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Document contains multiple values for uniqueKey field: " + field);
                } else {
                    return field.getFirstValue().toString();
                }
            }
        }
        return null;
    }

    protected final List<SolrInputDocument> flatten(SolrInputDocument root) {
        List<SolrInputDocument> unwrappedDocs = new ArrayList<>();
        recUnwrapp(unwrappedDocs, root);
        return unwrappedDocs;
    }

    protected void recUnwrapp(List<SolrInputDocument> unwrappedDocs, SolrInputDocument currentDoc) {
        List<SolrInputDocument> children = currentDoc.getChildDocuments();
        if (children != null) {
            for (SolrInputDocument child : children) {
                recUnwrapp(unwrappedDocs, child);
            }
        }
        unwrappedDocs.add(currentDoc);
    }
}
