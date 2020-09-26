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
package com.qlangtech.tis.solrextend.update.processor;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.RealTimeGetComponent;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.AtomicUpdateDocumentMerger;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.apache.solr.util.RefCounted;
import com.qlangtech.tis.solrextend.handler.component.s4personas.NestRealtimeGetWithRootIdComponet.RootQueryCollector;

/**
 * Nest文档实现原子更新，現有solr无法实现nest原子更新
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年3月29日
 */
public class NestAtomicUpdateProcessorFactory extends UpdateRequestProcessorFactory {

    public static final String NEST_ATOMIC_UPDATE_TOKEN = "tis_nest_child_atomic";

    private static final String YES = "y";

    private static final String _ROOT_ = "_root_";

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest request, SolrQueryResponse resp, UpdateRequestProcessor processor) {
        return new NestAtomicUpdateRequestProcessor(processor, request);
    }

    private class NestAtomicUpdateRequestProcessor extends UpdateRequestProcessor {

        private final AtomicUpdateDocumentMerger docMerger;

        public NestAtomicUpdateRequestProcessor(UpdateRequestProcessor next, SolrQueryRequest request) {
            super(next);
            docMerger = new AtomicUpdateDocumentMerger(request);
        }

        @Override
        public void processAdd(AddUpdateCommand cmd) throws IOException {
            SolrInputDocument solrDoc = cmd.solrDoc;
            final SolrInputDocument d = solrDoc;
            if (YES.equals(solrDoc.getField(NEST_ATOMIC_UPDATE_TOKEN))) {
                SolrQueryRequest req = cmd.getReq();
                SolrInputField root = solrDoc.getField(_ROOT_);
                SchemaField idField = req.getSchema().getUniqueKeyField();
                FieldType fieldType = idField.getType();
                BytesRefBuilder idBytes = new BytesRefBuilder();
                fieldType.readableToIndexed(String.valueOf(root.getFirstValue()), idBytes);
                BytesRef rootRef = idBytes.get();
                // SolrDocument rootDoc =
                // NestRealtimeGetWithRootIdComponet.getNestDoc(cmd.getReq(),
                // cmd.getReq().getCore(), rootRef);
                SolrInputDocument doc = RealTimeGetComponent.getInputDocumentFromTlog(req.getCore(), rootRef, null, null, true);
                if (doc != null) {
                    solrDoc = doc;
                } else {
                    RefCounted<SolrIndexSearcher> searchHolder = req.getCore().getSearcher();
                    SolrIndexSearcher searcher = searchHolder.get();
                    try {
                        TermQuery rootQuery = new TermQuery(new Term("_root_", rootRef));
                        SolrInputDocumentRootQueryCollector rootCollector = new SolrInputDocumentRootQueryCollector(req.getCore(), rootRef);
                        searcher.search(rootQuery, rootCollector);
                        solrDoc = rootCollector.getDocument();
                    } finally {
                        searchHolder.decref();
                    }
                }
                if (solrDoc == null) {
                    super.processAdd(cmd);
                    return;
                }
                d.removeField(NEST_ATOMIC_UPDATE_TOKEN);
                boolean merged = false;
                for (SolrInputDocument dd : solrDoc.getChildDocuments()) {
                    if (d.getFieldValue(idField.getName()).equals(dd.getField(idField.getName()))) {
                        merged = true;
                        docMerger.merge(dd, d);
                    }
                }
                if (!merged) {
                    solrDoc.addChildDocument(d);
                }
                cmd.solrDoc = solrDoc;
            }
            super.processAdd(cmd);
        }
    }

    private static class SolrInputDocumentRootQueryCollector extends RootQueryCollector<SolrInputDocument> {

        public SolrInputDocumentRootQueryCollector(SolrCore core, BytesRef _root_) {
            super(core, _root_);
        }

        @Override
        public SolrInputDocument getDocument() {
            if (parentDocument == null) {
                return null;
            }
            for (SolrInputDocument c : children) {
                parentDocument.addChildDocument(c);
            }
            return parentDocument;
        }

        @Override
        protected SolrInputDocument toSolrDoc(Document doc, IndexSchema schema) {
            SolrInputDocument out = new SolrInputDocument();
            for (IndexableField f : doc.getFields()) {
                String fname = f.name();
                SchemaField sf = schema.getFieldOrNull(f.name());
                Object val = null;
                if (sf != null) {
                    if ((!sf.hasDocValues() && !sf.stored()) || schema.isCopyFieldTarget(sf))
                        continue;
                    // object or external
                    val = sf.getType().toObject(f);
                // string?
                } else {
                    val = f.stringValue();
                    if (val == null)
                        val = f.numericValue();
                    if (val == null)
                        val = f.binaryValue();
                    if (val == null)
                        val = f;
                }
                // todo: how to handle targets of copy fields (including
                // polyfield sub-fields)?
                out.addField(fname, val);
            }
            return out;
        }
    }
}
