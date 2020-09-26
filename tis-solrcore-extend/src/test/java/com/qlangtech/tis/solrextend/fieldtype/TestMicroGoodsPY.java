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
package com.qlangtech.tis.solrextend.fieldtype;

import com.qlangtech.tis.solrj.extend.TisCloudSolrClient;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestMicroGoodsPY extends TestCase {

    public void testPy() throws Exception {
        TisCloudSolrClient client = new TisCloudSolrClient("10.1.6.65:2181,10.1.6.67:2181,10.1.6.80:2181/tis/cloud");
        SolrQuery query = new SolrQuery();
        query.setQuery("mic_entity_id:99928282");
        query.setRows(1);
        System.out.println(query.toString());
        QueryResponse response = client.query("search4microgoods", "99928282", query);
        String s = response.getResponse().get("mix_name").toString();
        System.out.println(s);
    }
}
