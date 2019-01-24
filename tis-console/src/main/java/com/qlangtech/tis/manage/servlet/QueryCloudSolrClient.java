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
package com.qlangtech.tis.manage.servlet;

import java.io.IOException;
import java.util.Collections;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.common.util.NamedList;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class QueryCloudSolrClient extends SolrClient {

	private static final long serialVersionUID = 1L;

	private static final LBHttpSolrClient lbClient;

	static {
		CloseableHttpClient myClient = HttpClientUtil.createClient(null);
		LBHttpSolrClient.Builder clientBuilder = new LBHttpSolrClient.Builder();
		clientBuilder.withHttpClient(myClient);
		clientBuilder.withResponseParser(new BinaryResponseParser());
		clientBuilder.withSocketTimeout(40000);
		lbClient = clientBuilder.build();
		lbClient.setRequestWriter(new BinaryRequestWriter());

	}

	private final String applyUrl;

	public QueryCloudSolrClient(final String applyUrl) {
		super();
		this.applyUrl = applyUrl;
	}

	@Override
	public NamedList<Object> request(SolrRequest request, String collection) throws SolrServerException, IOException {
		LBHttpSolrClient.Req req = new LBHttpSolrClient.Req(request, Collections.singletonList(applyUrl));
		LBHttpSolrClient.Rsp rsp = lbClient.request(req);
		return rsp.getResponse();
	}

	@Override
	public void close() throws IOException {
	}
}
