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
