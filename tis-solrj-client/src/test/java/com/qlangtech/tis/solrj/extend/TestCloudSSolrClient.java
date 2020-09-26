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

import org.apache.solr.common.SolrInputDocument;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestCloudSSolrClient extends BasicTestCase {

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
