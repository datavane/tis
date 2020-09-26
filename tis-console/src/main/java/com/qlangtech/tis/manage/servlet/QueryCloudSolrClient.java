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
package com.qlangtech.tis.manage.servlet;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.common.util.NamedList;
import java.io.IOException;
import java.util.Collections;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月7日 下午10:29:30
 */
public class QueryCloudSolrClient extends SolrClient {

    private static final long serialVersionUID = 1L;

    private static final LBHttpSolrClient lbClient;

    static {
        // CloseableHttpClient myClient = HttpClientUtil.createClient(null);
        // lbClient = new LBHttpSolrClient(myClient);
        // lbClient.setRequestWriter(new BinaryRequestWriter());
        // lbClient.setParser(new BinaryResponseParser());
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
        LBSolrClient.Req req = new LBSolrClient.Req(request, Collections.singletonList(applyUrl));
        // LBHttpSolrClient.Req req = new LBHttpSolrClient.Req(request, Collections.singletonList(applyUrl));
        LBSolrClient.Rsp rsp = lbClient.request(req);
        return rsp.getResponse();
    }

    @Override
    public void close() throws IOException {
    }
}
