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
		// SolrQueryResponse rsp = rb.rsp;
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
		// SolrQueryResponse rsp = rb.rsp;
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
		BytesRef root = idBytes.get();
		// SolrCore core, BytesRef idBytes, AtomicLong versionReturned,
		// Set<String> onlyTheseNonStoredDVs, boolean resolveFullDocument

		SolrDocument nestDoc = getNestDoc(req, core, root);

		SolrDocumentList docList = new SolrDocumentList();
		if (nestDoc != null) {
			docList.add(nestDoc);
			docList.setNumFound(1);
		}
		rb.rsp.addResponse(docList);
		// rsp.add("docs", Collections.singleton(doc));
	}

	/**
	 * 取得嵌套文档
	 * 
	 * @param req
	 * @param core
	 * @param root
	 * @return
	 * @throws IOException
	 */
	public static SolrDocument getNestDoc(SolrQueryRequest req, SolrCore core, BytesRef root) throws IOException {
		SolrInputDocument doc = RealTimeGetComponent.getInputDocumentFromTlog(core, root, null, null,
				true /* resolveFullDocument */);
		SolrDocument nestDoc = null;
		if (doc != null) {
			nestDoc = (convertDocument(doc));
		} else {
			RefCounted<SolrIndexSearcher> searchHolder = req.getCore().getSearcher();
			SolrIndexSearcher searcher = searchHolder.get();
			try {
				// Query rootQuery = QParser.getParser( parentFilter, null,
				// req).getQuery();
				TermQuery rootQuery = new TermQuery(new Term("_root_", root));
				SolrDocumentRootQueryCollector rootCollector = new SolrDocumentRootQueryCollector(core, root);
				searcher.search(rootQuery, rootCollector);
				nestDoc = rootCollector.getDocument();
				// if (solrDocument != null) {
				// docList.add(solrDocument);
				// docList.setNumFound(1);
				// }
			} finally {
				searchHolder.decref();
			}
		}
		return nestDoc;
	}

	private static class SolrDocumentRootQueryCollector extends RootQueryCollector<SolrDocument> {
		public SolrDocumentRootQueryCollector(SolrCore core, BytesRef _root_) {
			super(core, _root_);
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
		protected SolrDocument toSolrDoc(Document doc, IndexSchema schema) {
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
	}

	public static abstract class RootQueryCollector<T> extends SimpleCollector {

		private SortedDocValues idDV = null;

		private BytesRef _root_;

		// private int docBase = 0;
		private LeafReader reader = null;

		private final SolrCore core;

		// private SolrDocument parentDocument;
		//
		// private List<SolrDocument> children = new ArrayList<>();

		protected T parentDocument;

		protected List<T> children = new ArrayList<>();

		public RootQueryCollector(SolrCore core, BytesRef _root_) {
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
			T d = toSolrDoc(this.reader.document(doc), core.getLatestSchema());
			// if (StringUtils.equals(idref.utf8ToString(), this._root_)) {
			if (idref.equals(this._root_)) {
				parentDocument = d;
			} else {
				children.add(d);
			}
		}

		public abstract T getDocument();

		// {
		// if (parentDocument == null) {
		// return null;
		// }
		// for (SolrDocument c : children) {
		// parentDocument.addChildDocument(c);
		// }
		// return parentDocument;
		// }

		// public SolrDocument getDocument() {
		// if (parentDocument == null) {
		// return null;
		// }
		// for (SolrDocument c : children) {
		// parentDocument.addChildDocument(c);
		// }
		// return parentDocument;
		// }

		// private static SolrDocument toSolrDoc(Document doc, IndexSchema
		// schema) {
		// SolrDocument out = new SolrDocument();
		// for (IndexableField f : doc.getFields()) {
		// // Make sure multivalued fields are represented as lists
		// Object existing = out.get(f.name());
		// if (existing == null) {
		// SchemaField sf = schema.getFieldOrNull(f.name());
		// // don't return copyField targets
		// if (sf != null && schema.isCopyFieldTarget(sf))
		// continue;
		// if (sf != null && sf.multiValued()) {
		// List<Object> vals = new ArrayList<>();
		// vals.add(f);
		// out.setField(f.name(), vals);
		// } else {
		// out.setField(f.name(), f);
		// }
		// } else {
		// out.addField(f.name(), f);
		// }
		// }
		// return out;
		// }

		protected abstract T toSolrDoc(Document doc, IndexSchema schema);
	}

	protected static SolrDocument convertDocument(SolrInputDocument doc) {
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
