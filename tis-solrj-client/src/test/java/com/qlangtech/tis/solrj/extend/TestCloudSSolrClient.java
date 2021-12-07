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

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.FastStreamingDocsCallback;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.StreamingBinaryResponseParser;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DataEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestCloudSSolrClient extends BasicTestCase {





    public void testStreamIterator() throws Exception {
        URL url = new URL("http://192.168.28.200:8080/solr/search4employee2/export?fl=emp_no,_version_&q=emp_no:29959&sort=emp_no%20asc&wt=json");
        HttpUtils.get(url, new ConfigFileContext.StreamProcess<Void>() {
            @Override
            public List<ConfigFileContext.Header> getHeaders() {
                return PostFormStreamProcess.ContentType.Application_x_www_form_urlencoded.getHeaders();
            }

            @Override
            public Void p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                try {
                    FileUtils.copyInputStreamToFile(stream, new File("search4employee2_export.bin"));

//                    byte[] bytes = IOUtils.toByteArray(stream);
//                    System.out.println("bytes.length:" + bytes.length);

//                    LineIterator lineIterator = IOUtils.lineIterator(stream, TisUTF8.get());
//                    while (lineIterator.hasNext()) {
//                        System.out.println(lineIterator.nextLine());
//                    }
                    //System.out.println(IOUtils.toString(stream, TisUTF8.get()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return null;
            }
        });
    }

    public void testQueryAndStreamResponse() throws Exception {
        List<String> zks = Lists.newArrayList(zkHost);
        CloudSolrClient solrClient = (new CloudSolrClient.Builder(zks, Optional.empty())).build();

        SolrQuery query = new SolrQuery();
        // query.set("wt", "javabin");

        query.addSort("emp_no", SolrQuery.ORDER.asc);
        query.setRequestHandler("/export").setQuery("*:*");

        query.setFields("emp_no");
        solrClient.queryAndStreamResponse("search4employee2", query, new FastStreamingDocsCallback() {
            @Override
            public Object startDoc(Object docListObj) {
                return new Pojo();
            }

            @Override
            public void field(DataEntry field, Object docObj) {
                Pojo pojo = (Pojo) docObj;
                if ("emp_no".equals(field.name())) {
                    pojo.empNo = field.val();
                }
            }

            @Override
            public void endDoc(Object docObj) {
                Pojo pojo = (Pojo) docObj;
                System.out.println(pojo);
            }
        });
        Thread.sleep(999999);
    }

    static class Pojo {
        Object empNo;
    }

    public void testStream() throws Exception {
        //String collection, SolrQuery query, String routerId, final ResponseCallback<T> resultProcess, final Class<T> clazz
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.set("wt", "javabin");
        query.setFields("emp_no");
        query.addSort("emp_no", SolrQuery.ORDER.asc);
        this.client.queryAndStreamResponse("search4employee2", query, "emp_no", new AbstractTisCloudSolrClient.ResponseCallback<Tuple>() {
            @Override
            public void process(Tuple pojo) {
                System.out.println(pojo.get("emp_no"));
            }

            @Override
            public void lististInfo(long numFound, long start) {
                System.out.println("numFound:" + numFound + ",start:" + start);
            }
        }, Tuple.class);
    }

    public void testAddTotlal() throws Exception {
        long count = 1l;
        while (true) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.setField("totalpay_id", "9999999999999999999" + (++count));
            doc.setField("entity_id", "00005714");
            doc.setField("last_ver", "1");
            doc.setField("is_servicefee_ratio", "0");
            doc.setField("curr_date", "20141126");
            doc.setField("over_status", "0");
            doc.setField("status", "1");
            doc.setField("invoice", "0.0");
            doc.setField("op_user_id", "c2d6ff26810a5dad7721745c93ef6f33");
            doc.setField("is_minconsume_ratio", "0");
            doc.setField("recieve_amount", "0.0");
            doc.setField("is_full_ratio", "0");
            doc.setField("outfee", "0");
            doc.setField("discount_amount", "0");
            doc.setField("is_valid", "1");
            doc.setField("result_amount", "0");
            doc.setField("source_amount", "0");
            doc.setField("ratio", "100");
            doc.setField("is_hide", "0");
            doc.setField("operate_date", "0");
            doc.setField("op_time", "20141126101114");
            // doc.setField("_version_", "20151113233003");
            client.add("search4totalpay", doc, System.currentTimeMillis());
            System.out.println("insert:" + count);
            Thread.sleep(10);
            return;
        }
    }

    public void tesAdd() throws Exception {
        // add(String collection, String group,
        // SolrInputDocument doc)
        long count = 1l;
        while (true) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.setField("order_id", "cc" + count);
            doc.setField("last_ver", count);
            doc.setField("is_print", 0);
            doc.setField("seat_code", "18");
            doc.setField("entity_id", "00000505");
            doc.setField("outfee", 0);
            doc.setField("is_valid", 1);
            doc.setField("open_time", "1398945331" + count);
            doc.setField("simple_code", "ppppppppp");
            doc.setField("send_time", 0);
            doc.setField("people_count", 3);
            doc.setField("global_code", "kk");
            doc.setField("order_from", 0);
            doc.setField("modify_time", 1398945331);
            doc.setField("is_wait", 0);
            doc.setField("curr_date", 20140501);
            doc.setField("paymode", 0);
            doc.setField("status", 1);
            doc.setField("totalpay_id", "0000024145b572e20145b77e23603174");
            doc.setField("load_time", 1398942868);
            doc.setField("code", 83);
            doc.setField("send_status", 0);
            doc.setField("end_time", 0);
            doc.setField("order_kind", 1);
            doc.setField("is_hide", 0);
            doc.setField("is_limittime", 0);
            doc.setField("create_time", "139894286" + count);
            doc.setField("op_time", "1398945331" + count);
            doc.setField("seat_id", "0000008321947c27012195a388ef12b2");
            doc.setField("is_autocommit", 0);
            doc.setField("audit_status", 0);
            client.add("search4OrderInfo", doc, System.currentTimeMillis());
            System.out.println("insert:" + count);
            Thread.sleep(10);
            count++;
        }
    }
}
