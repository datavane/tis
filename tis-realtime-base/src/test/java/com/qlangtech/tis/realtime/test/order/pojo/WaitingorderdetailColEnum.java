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
public enum WaitingorderdetailColEnum {

    WAITINGORDER_ID("waitingorder_id", 12, true),
    ORDER_FROM("order_from", 5, false),
    BATCH_MSG("batch_msg", 12, false),
    KIND("kind", 5, false),
    CODE("code", 12, false),
    SEAT_CODE("seat_code", 12, false),
    NAME("name", 12, false),
    PEOPLE_COUNT("people_count", 4, false),
    MOBILE("mobile", 12, false),
    TEL("tel", 12, false),
    RESERVE_DATE("reserve_date", -5, false),
    MEMO("memo", 12, false),
    TOTAL_PRICE("total_price", 3, false),
    REAL_PRICE("real_price", 3, false),
    SHOPNAME("shopname", 12, false),
    ADDRESS("address", 12, false),
    PAY_MODE("pay_mode", 5, false),
    PAY_TYPE("pay_type", 5, false),
    PAY_MEMO("pay_memo", 12, false),
    OUTFEE("outfee", 3, false),
    CARD_ENTITY_ID("card_entity_id", 12, false),
    CARD_ID("card_id", 12, false),
    PAY_ID("pay_id", 12, false),
    ADVANCE_PAY("advance_pay", 3, false),
    ADVANCE_SEAT_PAY("advance_seat_pay", 3, false),
    PAY_STATUS("pay_status", 5, false),
    RESERVE_SEAT_ID("reserve_seat_id", 12, false),
    RESERVE_TIME_ID("reserve_time_id", 12, false),
    STATUS("status", 5, false),
    HIDE_STATUS("hide_status", 5, false),
    RESERVE_STATUS("reserve_status", 5, false),
    AUDIT_STATUS("audit_status", 5, false),
    ORDER_ID("order_id", 12, false),
    DEAL_MESSAGE("deal_message", 12, false),
    ERRORMESSAGE("errormessage", 12, false),
    SENDER("sender", 12, false),
    SENDER_ID("sender_id", 12, false),
    CUSTOMERREGISTER_ID("customerregister_id", 12, false),
    ENTITY_ID("entity_id", 12, false),
    IS_VALID("is_valid", 5, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", -5, false),
    OUT_ID("out_id", 12, false),
    OUT_TYPE("out_type", 5, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<WaitingorderdetailColEnum> pks = (new ImmutableList.Builder<WaitingorderdetailColEnum>()).add(WAITINGORDER_ID).build();

    private WaitingorderdetailColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<WaitingorderdetailColEnum> getPKs() {
        return pks;
    }
}
