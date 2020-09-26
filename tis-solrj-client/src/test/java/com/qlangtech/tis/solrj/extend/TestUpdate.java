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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;

/**
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
