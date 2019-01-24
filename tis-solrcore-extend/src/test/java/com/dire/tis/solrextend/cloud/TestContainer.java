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
package com.dire.tis.solrextend.cloud;

import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.TisCoreContainer;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestContainer extends TestCase {

    public void testLaunch() throws Exception {
    // TisCoreContainer coreContainer = new TisCoreContainer(
    // "D:\\solr\\testconfig");
    // coreContainer.load();
    // //		EmbeddedSolrServer solrServer = new EmbeddedSolrServer(coreContainer,
    // //				"search4dfireOrderInfo");
    // 
    // EmbeddedSolrServer solrServer = new EmbeddedSolrServer(coreContainer,
    // "search2dfiretest_1");
    // 
    // 
    // 
    // SolrQuery query = new SolrQuery();
    // query.setQuery("tg_type:1");
    // // query.setFields("ju_id");
    // QueryResponse result = solrServer.query(query);
    // 
    // System.out.println("result.getResults().getNumFound():"
    // + result.getResults().getNumFound());
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }
}
