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
package com.qlangtech.tis.solrextend.handler.component.s4SupplyOrder;

import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import java.io.File;
import java.util.Map;

/*
 * Created by Qinjiu on 6/28/2017.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AbstractGroupSumComponentTest extends TestCase {

    public static final EmbeddedSolrServer server;

    public static final File solrHome;

    static {
        solrHome = new File("D:/workspace/solrhome");
        server = new EmbeddedSolrServer(solrHome.toPath(), "supplyOrder");
    }

    private static String val(Object o) {
        if (o == null) {
            return "";
        }
        if (!(o instanceof org.apache.lucene.document.Field)) {
            return String.valueOf(o);
        }
        org.apache.lucene.document.Field f = (org.apache.lucene.document.Field) o;
        return f.stringValue();
    }

    @SuppressWarnings("unchecked")
    public void testQuery() throws Exception {
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRows(0);
        query.set("GroupSum", true);
        query.set("groupField", "pay_time");
        query.set("sumField", "discount_amount");
        QueryResponse response = server.query(query);
        System.out.println("************");
        Map<String, Number> resultMap = (Map<String, Number>) response.getResponse().get("GroupSum");
        for (Map.Entry<String, Number> entry : resultMap.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}
