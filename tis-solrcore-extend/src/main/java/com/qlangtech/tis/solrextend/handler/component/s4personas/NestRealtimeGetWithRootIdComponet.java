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
package com.qlangtech.tis.solrextend.handler.component.s4personas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
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
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.ReturnFields;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SolrReturnFields;
import org.apache.solr.util.RefCounted;

/*
 * 通过到tlog中获取数据，需要把对应的子文档记录也要取出来
 * @time 2017年9月14日下午7:38:40
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NestRealtimeGetWithRootIdComponet extends SearchComponent {

	public static final String COMPONENT_NAME = "nestgetWithRootId";

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

		// SolrCore core, BytesRef idBytes, AtomicLong versionReturned,
		// Set<String> onlyTheseNonStoredDVs, boolean resolveFullDocument

		SolrInputDocument doc = RealTimeGetComponent.getInputDocumentFromTlog(core, idBytes.get(), null, null,
				true /* resolveFullDocument */);
		SolrDocumentList docList = new SolrDocumentList();
		if (doc != null) {
			docList.add(convertDocument(doc));
			docList.setNumFound(1);
		} else {
			RefCounted<SolrIndexSearcher> searchHolder = req.getCore().getSearcher();
			SolrIndexSearcher searcher = searchHolder.get();
			try {
				// Query rootQuery = QParser.getParser( parentFilter, null,
				// req).getQuery();
				TermQuery rootQuery = new TermQuery(new Term("_root_", id));
				RootQueryCollector rootCollector = new RootQueryCollector(core, id);
				searcher.search(rootQuery, rootCollector);
				SolrDocument solrDocument = rootCollector.getDocument();
				if (solrDocument != null) {
					docList.add(solrDocument);
					docList.setNumFound(1);
				}
			} finally {
				searchHolder.decref();
			}
		}
		rb.rsp.addResponse(docList);
		// rsp.add("docs", Collections.singleton(doc));
	}

	private static class RootQueryCollector extends SimpleCollector {

		private SortedDocValues idDV = null;

		private String _root_;

		// private int docBase = 0;
		private LeafReader reader = null;

		private final SolrCore core;

		private SolrDocument parentDocument;

		private List<SolrDocument> children = new ArrayList<>();

		RootQueryCollector(SolrCore core, String _root_) {
			this.core = core;
			this._root_ = _root_;
		}

		@Override
		public boolean needsScores() {
			return false;
		}

		@Override
		protected void doSetNextReader(LeafReaderContext context) throws IOException {
			// this.docBase = context.docBase;
			this.idDV = DocValues.getSorted(context.reader(), "id");
			this.reader = context.reader();
		}

		@Override
		public void collect(int doc) throws IOException {
			
			idDV.advance(doc);
			
			BytesRef idref = idDV.binaryValue();
			if (idref == null) {
				return;
			}
			SolrDocument d = toSolrDoc(this.reader.document(doc), core.getLatestSchema());
			if (StringUtils.equals(idref.utf8ToString(), this._root_)) {
				parentDocument = d;
			} else {
				children.add(d);
			}
		}

		public SolrDocument getDocument() {
			if (parentDocument == null) {
				return null;
			}
			for (SolrDocument c : children) {
				parentDocument.addChildDocument(c);
			}
			return parentDocument;
		}
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
