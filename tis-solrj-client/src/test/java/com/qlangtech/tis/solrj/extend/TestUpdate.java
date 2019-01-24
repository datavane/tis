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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestUpdate extends BasicTestCase {

    // public void testUpdate() throws Exception {
    // SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
    // Date now = new Date();
    // long time = Long.parseLong(f.format(now));
    // 
    // for (int i = 0; i < 999999999; i++) {
    // SolrInputDocument doc = new SolrInputDocument();
    // 
    // doc.setField("menu_id", "99999999999" + i);
    // // doc.setField("entity_id", "00005714");
    // // doc.setField("last_ver", "1");
    // // doc.setField("is_servicefee_ratio", "0");
    // // doc.setField("curr_date", "20141126");
    // // doc.setField("over_status", "0");
    // // doc.setField("status", "1");
    // // doc.setField("invoice", "0.0");
    // // doc.setField("op_user_id", "c2d6ff26810a5dad7721745c93ef60000k");
    // //
    // // doc.setField("is_minconsume_ratio", "0");
    // // doc.setField("recieve_amount", "0.0");
    // //
    // // doc.setField("is_full_ratio", "0");
    // // doc.setField("outfee", "0");
    // // doc.setField("discount_amount", "0");
    // // doc.setField("is_valid", "1");
    // // doc.setField("result_amount", "0");
    // // doc.setField("source_amount", "0");
    // // doc.setField("ratio", "100");
    // // doc.setField("is_hide", "0");
    // // doc.setField("all_menu",
    // //
    // "000129624fcc92d40150ccb6593a7d22_沙爹鲜肉云吞面_00012962427341a901428f5282203dfd_1.00_14.00_14.00_卡_1.00_");
    // // doc.setField("operate_date", "0");
    // // doc.setField("op_time", "20000000000");
    // 
    // doc.setField("_version_", time);
    // 
    // // doc.setField("_version_", "20151113233003");
    // client.add("search4menu", doc, time++);
    // // client.update("search4totalpay", doc,
    // // System.currentTimeMillis());
    // System.out.println(i);
    // Thread.sleep(1000);
    // 
    // }
    // }
    public void testUpdate() throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        int i = 0;
        while (true) {
            SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
            Date now = new Date();
            final String time = f.format(now);
            doc.setField("totalpay_id", "aaaaaaaaaaaa" + (i++));
            doc.setField("entity_id", "00005714");
            doc.setField("last_ver", "1");
            doc.setField("is_servicefee_ratio", "0");
            doc.setField("curr_date", "20141126");
            doc.setField("over_status", "0");
            doc.setField("status", "1");
            Map<String, String> value = new HashMap<>();
            value.put("set", "newvalue");
            doc.setField("xxxx", value);
            doc.setField("invoice", "0.0");
            doc.setField("op_user_id", "c2d6ff26810a5dad7721745c93ef60000k");
            doc.setField("is_minconsume_ratio", "0");
            doc.setField("recieve_amount", "0.0");
            doc.setField("is_full_ratio", "0");
            doc.setField("outfee", "0");
            doc.setField("discount_amount", "0");
            doc.setField("is_valid", "1");
            doc.setField("result_amount", "0");
            doc.setField("source_amount", "0");
            doc.setField("ratio", "100" + i);
            doc.setField("is_hide", "0");
            // doc.setField("all_menu",
            // "000129624fcc92d40150ccb6593a7d22_沙爹鲜肉云吞面_00012962427341a901428f5282203dfd_1.00_14.00_14.00_卡_1.00_");
            doc.setField("operate_date", "0" + i);
            doc.setField("op_time", time);
            doc.setField("_version_", time);
            // doc.setField("_version_", "20151113233003");
            client.add("search4totalpay", doc, Long.parseLong(time));
            // client.update("search4totalpay", doc,
            // System.currentTimeMillis());
            System.out.println("insert:" + i);
            Thread.sleep(800);
        }
    }
}
