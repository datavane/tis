/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.solrextend.handler.component.s4SupplyOrder;

import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import java.io.File;
import java.util.Map;

/**
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
