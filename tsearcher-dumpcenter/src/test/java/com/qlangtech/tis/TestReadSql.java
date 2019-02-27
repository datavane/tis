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
package com.qlangtech.tis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestReadSql extends TestCase {

    static final String sql = " SELECT  order_id, global_code, simple_code, seat_code, code," + "DATE_FORMAT(curr_date,'%Y%m%d') as  curr_date, totalpay_id, " + "seat_id, people_count, open_time, status, memo, inner_code, menutime_id, worker_id, " + "end_time, feeplan_id, op_user_id, order_from, order_kind, area_id, name, mobile," + "tel, is_autocommit, send_time, address, paymode, outfee, sender_id, customerregister_id," + "waitingorder_id, send_status, audit_status, is_hide, entity_id, is_valid, " + "create_time, op_time, last_ver, load_time, modify_time, scan_url, seat_mark, " + "reservetime_id, is_wait, is_print, book_id, reserve_id, is_limittime " + " FROM `orderinfo`";

    public void testRead() throws Exception {
//        BasicDataSource datasource = (BasicDataSource) TestOrderinfoDump.context.getBean("orderds1");
//        Connection conn = datasource.getConnection();
//        Statement statement = conn.createStatement();
//        ResultSet result = statement.executeQuery(sql);
//        long current = System.currentTimeMillis();
//        int count = 0;
//        while (result.next()) {
//            result.getString("send_time");
//            result.getString("address");
//            result.getString("paymode");
//            result.getString("outfee");
//            result.getString("sender_id");
//            result.getString("customerregister_id");
//            result.getString("waitingorder_id");
//            result.getString("send_status");
//            result.getString("audit_status");
//            result.getString("is_hide");
//            result.getString("entity_id");
//            result.getString("is_valid");
//            result.getString("create_time");
//            result.getString("op_time");
//            result.getString("last_ver");
//            result.getString("load_time");
//            result.getString("modify_time");
//            result.getString("scan_url");
//            // /////////////////////////////////////
//            result.getString("order_id");
//            result.getString("global_code");
//            result.getString("simple_code");
//            result.getString("seat_code");
//            result.getString("code");
//            result.getString("curr_date");
//            result.getString("seat_id");
//            result.getString("people_count");
//            result.getString("open_time");
//            result.getString("status");
//            result.getString("memo");
//            result.getString("menutime_id");
//            result.getString("worker_id");
//            result.getString("end_time");
//            result.getString("feeplan_id");
//            result.getString("op_user_id");
//            result.getString("order_from");
//            result.getString("order_kind");
//            result.getString("area_id");
//            result.getString("name");
//            result.getString("mobile");
//            result.getString("tel");
//            result.getString("is_autocommit");
//            count++;
//            if ((count % 1000) == 0) {
//                System.out.println(count);
//            }
//        }
//        System.out.println("consume:" + (System.currentTimeMillis() - current) + ",count:" + count);
//        result.close();
//        statement.close();
    }
}
