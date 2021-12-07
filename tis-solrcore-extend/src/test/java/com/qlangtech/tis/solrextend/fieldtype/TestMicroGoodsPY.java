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
