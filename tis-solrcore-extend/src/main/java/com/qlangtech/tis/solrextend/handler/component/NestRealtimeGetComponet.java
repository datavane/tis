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
package com.qlangtech.tis.solrextend.handler.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.RealTimeGetComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.BasicResultContext;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.ReturnFields;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SolrReturnFields;
import org.apache.solr.util.RefCounted;

/**
 * copy from RealTimeGetComponent
 * 通过到tlog中获取数据，需要把对应的子文档记录也要取出来
 * @time 2017年9月14日下午7:38:40
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NestRealtimeGetComponet extends SearchComponent {

    public static final String COMPONENT_NAME = "nestget";

    @Override
    public String getDescription() {
        return COMPONENT_NAME;
    }

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        SolrQueryRequest req = rb.req;
        SolrQueryResponse rsp = rb.rsp;
        SolrParams params = req.getParams();
        if (!params.getBool(COMPONENT_NAME, false)) {
            return;
        }
        // Set field flags
        ReturnFields returnFields = new SolrReturnFields(rb.req);
        rb.rsp.setReturnFields(returnFields);
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {
        SolrQueryRequest req = rb.req;
        SolrQueryResponse rsp = rb.rsp;
        SolrParams params = req.getParams();
        if (!params.getBool(COMPONENT_NAME, false)) {
            return;
        }
        String id = params.get("id");
        SchemaField idField = req.getSchema().getUniqueKeyField();
        FieldType fieldType = idField.getType();
        BytesRefBuilder idBytes = new BytesRefBuilder();
        fieldType.readableToIndexed(id, idBytes);
        SolrCore core = req.getCore();
        SolrInputDocument doc = RealTimeGetComponent.getInputDocumentFromTlog(core, idBytes.get(), null, null, true);
        SolrDocumentList docList = new SolrDocumentList();
        if (doc != null) {
            docList.add(convertDocument(doc));
            docList.setNumFound(1);
        } else {
            RefCounted<SolrIndexSearcher> searchHolder = req.getCore().getSearcher();
            SolrIndexSearcher searcher = searchHolder.get();
            // 取得transfer
            DocTransformer transformer = rsp.getReturnFields().getTransformer();
            if (transformer != null) {
                ResultContext context = new BasicResultContext(null, rsp.getReturnFields(), null, null, req);
                transformer.setContext(context);
            }
            try {
                int docid = -1;
                long segAndId = searcher.lookupId(idBytes.get());
                if (segAndId >= 0) {
                    int segid = (int) segAndId;
                    LeafReaderContext ctx = searcher.getTopReaderContext().leaves().get((int) (segAndId >> 32));
                    docid = segid + ctx.docBase;
                }
                if (docid >= 0) {
                    Document luceneDocument = searcher.doc(docid, rsp.getReturnFields().getLuceneFieldNames());
                    SolrDocument d = toSolrDoc(luceneDocument, core.getLatestSchema());
                    // searcher.decorateDocValueFields(d, docid, searcher.getNonStoredDVs(true));
                    if (transformer != null) {
                        transformer.transform(d, docid, 0);
                    }
                    docList.add(d);
                    docList.setNumFound(1);
                }
            } finally {
                searchHolder.decref();
            }
        }
        rb.rsp.addResponse(docList);
    // rsp.add("docs", Collections.singleton(doc));
    }

    private static SolrDocument toSolrDoc(Document doc, IndexSchema schema) {
        SolrDocument out = new SolrDocument();
        for (IndexableField f : doc.getFields()) {
            // Make sure multivalued fields are represented as lists
            Object existing = out.get(f.name());
            if (existing == null) {
                SchemaField sf = schema.getFieldOrNull(f.name());
                // don't return copyField targets
                if (sf != null && schema.isCopyFieldTarget(sf))
                    continue;
                if (sf != null && sf.multiValued()) {
                    List<Object> vals = new ArrayList<>();
                    vals.add(f);
                    out.setField(f.name(), vals);
                } else {
                    out.setField(f.name(), f);
                }
            } else {
                out.addField(f.name(), f);
            }
        }
        return out;
    }

    protected SolrDocument convertDocument(SolrInputDocument doc) {
        SolrDocument sdoc = new SolrDocument();
        for (String k : doc.getFieldNames()) {
            sdoc.setField(k, doc.getFieldValue(k));
        }
        if (doc.hasChildDocuments()) {
            for (SolrInputDocument s : doc.getChildDocuments()) {
                sdoc.addChildDocument(convertDocument(s));
            }
        }
        return sdoc;
    }
}
