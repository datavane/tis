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

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileReader;

/**
 * 从json文件中把所有doc添加到一个新的索引中
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AddSolrDocument extends TestCase {

   // private TisCloudSolrClient client;

    public void setUp() throws Exception {
        super.setUp();
        String zkHost = "10.1.6.65:2181,10.1.6.67:2181,10.1.6.80:2181/tis/cloud";
//        client = new TisCloudSolrClient(zkHost);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    private void add(SolrInputDocument doc) throws SolrServerException {
        //client.add("search4supplyUnionTabs", doc, 1L);
    }

    private String getFile() throws Exception {
        return null;
    }

    public void testAddDoc() throws Exception {
        String fileString = this.getFile();
        JSONObject jsonObject = new JSONObject(fileString);
        JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("docs");
        for (Object aJsonArray : jsonArray) {
            JSONObject jsonObject1 = (JSONObject) aJsonArray;
            SolrInputDocument document = new SolrInputDocument();
            for (String key : jsonObject1.keySet()) {
                document.addField(key, jsonObject1.get(key));
            }
            System.out.println(document);
            this.add(document);
        }
    }
}
