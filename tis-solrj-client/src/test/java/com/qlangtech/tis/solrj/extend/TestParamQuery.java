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
package com.qlangtech.tis.solrj.extend;

import java.net.URLDecoder;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient.SimpleQueryResult;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestParamQuery extends BasicTestCase {

    public void testQuery() throws Exception {
        String param = "q={!mrecommend+entity_id%3D00028788+rules%3D2%252C1%252C3%252C1_0_1%253B1%252C1%252C3%252C1_0_2%253B1%252C1%252C2%252C1_0_3%253B1%252C1%252C2%252C1_0_4%253B1%252C1%252C2%252C1_0_5%253B1%252C1%252C3%252C1_0_6%253B1%252C1%252C3%252C1_0_7%253B1%252C1%252C1%252C1_2_12%253B1%252C1%252C1%252C1_2_13%253B1%252C1%252C1%252C1_2_14%253B1%252C1%252C1%252C1_2_15%253B1%252C1%252C1%252C1_2_16%253B1%252C1%252C1%252C1_2_17%253B1%252C1%252C1%252C1_2_19%253B1%252C1%252C1%252C1_2_20%253B1%252C1%252C1%252C1_2_21%253B1%252C1%252C1%252C1_2_23%253B1%252C1%252C1%252C1_2_24%253B1%252C1%252C1%252C1_2_25%253B1%252C1%252C1%252C1_2_26%253B1%252C1%252C1%252C1_2_27}000287884db34f48014db39d22e40050:1,000287884db34f48014db39d233d0056:1+AND+menu_spec:[*+TO+*]&_stateVer_=search4menu:355&single.slice.query=true&distrib=false&wt=javabin&fq=entity_id:00028788+AND+is_include:0+AND+-is_self:0+AND+is_reserve:1+AND+is_additional:0&version=2&rows=0";
        System.out.println();
        String[] args = param.split("&");
        String[] pair = null;
        SolrQuery query = new SolrQuery();
        for (String arg : args) {
            pair = StringUtils.split(arg, "=");
            System.out.print(pair[0]);
            System.out.print("=");
            System.out.print(URLDecoder.decode(pair[1]));
            System.out.println();
            query.set(pair[0], URLDecoder.decode(pair[1]));
        }
    // SimpleQueryResult<Object> result =
    // client.query("search4waitInstance",
    // "99001331", query, Object.class);
    //
    // System.out.println("getNumberFound:" + result.getNumberFound());
    }
}
