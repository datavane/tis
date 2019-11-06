package com.qlangtech.tis.indexbuilder.doc;

import org.apache.solr.common.SolrInputDocument;

import com.qlangtech.tis.common.utils.Assert;

import junit.framework.TestCase;

public class TestSolrDocPack extends TestCase {
	public void testPack() {

		SolrDocPack docPack = new SolrDocPack();
		SolrInputDocument doc = null;
		int count = 0;
		do {
			doc = new SolrInputDocument();
			count++;
		} while (!docPack.add(doc));

		Assert.assertEquals(SolrDocPack.BUFFER_PACK_SIZE, count);
		Assert.assertEquals(SolrDocPack.BUFFER_PACK_SIZE - 1, docPack.getCurrentIndex());

		Assert.assertEquals(true, docPack.isNotEmpty());

	}
}
