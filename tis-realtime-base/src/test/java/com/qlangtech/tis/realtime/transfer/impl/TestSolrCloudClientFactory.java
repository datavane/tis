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
package com.qlangtech.tis.realtime.transfer.impl;

import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-08-19 11:01
 */
public class TestSolrCloudClientFactory extends TestCase {

    static {
        AbstractTisCloudSolrClient.initHashcodeRouter();
    }

    private static final String COLLECTION = "search4totalpay";

    public void testGetDoc() throws Exception {
        SolrCloudClientFactory solrClientFactory = new SolrCloudClientFactory();
        SolrCloudClientFactory.SolrCloudClient cloudClient = (SolrCloudClientFactory.SolrCloudClient) solrClientFactory.create();
        while (true) {
            SolrQuery query = new SolrQuery();
            query.setQuery("totalpay_id:999264985818715001581a6c6938069b");
            QueryResponse r = cloudClient.solrClient.query(COLLECTION, "99926498", query);
            System.out.println("NumFound:" + r.getResults().getNumFound());
            // SolrDocument search4totalpay
            // = (SolrDocument) cloudClient.getDocById("search4totalpay", "999264985818715001581a6c6938069b", "99926498");
            // System.out.println("search4totalpay:" + search4totalpay);
            Thread.sleep(1000);
        }
    }
}
