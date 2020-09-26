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

    private TisCloudSolrClient client;

    public void setUp() throws Exception {
        super.setUp();
        String zkHost = "10.1.6.65:2181,10.1.6.67:2181,10.1.6.80:2181/tis/cloud";
        client = new TisCloudSolrClient(zkHost);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    private void add(SolrInputDocument doc) throws SolrServerException {
        client.add("search4supplyUnionTabs", doc, 1L);
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
