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
package com.qlangtech.tis.solrj.extend;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileReader;

/*
 * 从json文件中把所有doc添加到一个新的索引中
 * Created by Qinjiu on 7/10/2017.
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
        String fileName = "C:\\Users\\Qinjiu\\Downloads\\20170710-101835-ba03\\tmp\\20170710-101835-ba03\\solr-goods" + "-report003.prod.2dfire.info\\home\\jump\\result.txt";
        return IOUtils.toString(new FileReader(fileName));
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
