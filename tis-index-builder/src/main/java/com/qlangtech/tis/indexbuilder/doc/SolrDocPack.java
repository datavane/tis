package com.qlangtech.tis.indexbuilder.doc;

import org.apache.solr.common.SolrInputDocument;

public class SolrDocPack // implements Iterable<SolrInputDocument>
{
	private final SolrInputDocument[] docs = new SolrInputDocument[1000];
	int index = -1;

	public SolrInputDocument getDoc(int index) {
		return this.docs[index];
	}

	public boolean isNotEmpty() {
		return this.index >= 0;
	}

	public int getCurrentIndex() {
		return this.index;
	}

	/**
	 * 
	 * @param doc
	 * @return full 放满了
	 */
	public boolean add(SolrInputDocument doc) {
		this.docs[++index] = doc;
		return (index + 1) >= docs.length;
	}
}
