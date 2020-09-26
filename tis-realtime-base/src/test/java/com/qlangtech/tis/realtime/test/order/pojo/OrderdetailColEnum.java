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
package com.qlangtech.tis.realtime.test.order.pojo;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public enum OrderdetailColEnum {

    ORDER_ID("order_id", 12, true),
    GLOBAL_CODE("global_code", 12, false),
    SIMPLE_CODE("simple_code", 12, false),
    SEAT_CODE("seat_code", 12, false),
    CODE("code", 4, false),
    CURR_DATE("curr_date", 91, false),
    TOTALPAY_ID("totalpay_id", 12, false),
    SEAT_ID("seat_id", 12, false),
    PEOPLE_COUNT("people_count", -5, false),
    OPEN_TIME("open_time", -5, false),
    STATUS("status", 5, false),
    MEMO("memo", 12, false),
    INNER_CODE("inner_code", 12, false),
    MENUTIME_ID("menutime_id", 12, false),
    WORKER_ID("worker_id", 12, false),
    END_TIME("end_time", -5, false),
    FEEPLAN_ID("feeplan_id", 12, false),
    OP_USER_ID("op_user_id", 12, false),
    ORDER_FROM("order_from", 5, false),
    ORDER_KIND("order_kind", 5, false),
    AREA_ID("area_id", 12, false),
    NAME("name", 12, false),
    MOBILE("mobile", 12, false),
    TEL("tel", 12, false),
    IS_AUTOCOMMIT("is_autocommit", 5, false),
    SEND_TIME("send_time", -5, false),
    ADDRESS("address", 12, false),
    PAYMODE("paymode", 5, false),
    OUTFEE("outfee", 3, false),
    SENDER_ID("sender_id", 12, false),
    CUSTOMERREGISTER_ID("customerregister_id", 12, false),
    WAITINGORDER_ID("waitingorder_id", 12, false),
    SEND_STATUS("send_status", 5, false),
    AUDIT_STATUS("audit_status", 5, false),
    IS_HIDE("is_hide", -6, false),
    ENTITY_ID("entity_id", 12, false),
    IS_VALID("is_valid", 5, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", -5, false),
    LOAD_TIME("load_time", 4, false),
    MODIFY_TIME("modify_time", 4, false),
    IS_LIMITTIME("is_limittime", -7, false),
    SCAN_URL("scan_url", 12, false),
    SEAT_MARK("seat_mark", 12, false),
    RESERVETIME_ID("reservetime_id", 12, false),
    IS_WAIT("is_wait", -6, false),
    IS_PRINT("is_print", -6, false),
    BOOK_ID("book_id", 12, false),
    RESERVE_ID("reserve_id", 12, false),
    ORIGN_ID("orign_id", 12, false),
    RESERVE_STATUS("reserve_status", -6, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<OrderdetailColEnum> pks = (new ImmutableList.Builder<OrderdetailColEnum>()).add(ORDER_ID).build();

    private OrderdetailColEnum(String name, int jdbcType, boolean pk) {
        this.jdbcType = jdbcType;
        this.name = name;
        this.pk = pk;
    }

    public String getName() {
        return this.name;
    }

    public int getJdbcType() {
        return this.jdbcType;
    }

    public boolean isPK() {
        return this.pk;
    }

    public static List<OrderdetailColEnum> getPKs() {
        return pks;
    }
}
