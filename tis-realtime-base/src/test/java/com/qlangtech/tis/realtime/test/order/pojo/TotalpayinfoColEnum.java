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
public enum TotalpayinfoColEnum {

    TOTALPAY_ID("totalpay_id", 12, true),
    CURR_DATE("curr_date", 91, false),
    OUTFEE("outfee", 3, false),
    SOURCE_AMOUNT("source_amount", 3, false),
    DISCOUNT_AMOUNT("discount_amount", 3, false),
    RESULT_AMOUNT("result_amount", 3, false),
    RECIEVE_AMOUNT("recieve_amount", 3, false),
    RATIO("ratio", 3, false),
    STATUS("status", -6, false),
    ENTITY_ID("entity_id", 12, false),
    IS_VALID("is_valid", -6, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", -5, false),
    OP_USER_ID("op_user_id", 12, false),
    DISCOUNT_PLAN_ID("discount_plan_id", 12, false),
    OPERATOR("operator", 12, false),
    OPERATE_DATE("operate_date", -5, false),
    CARD_ID("card_id", 12, false),
    CARD("card", 12, false),
    CARD_ENTITY_ID("card_entity_id", 12, false),
    IS_FULL_RATIO("is_full_ratio", -6, false),
    IS_MINCONSUME_RATIO("is_minconsume_ratio", -6, false),
    IS_SERVICEFEE_RATIO("is_servicefee_ratio", -6, false),
    INVOICE_CODE("invoice_code", 12, false),
    INVOICE_MEMO("invoice_memo", 12, false),
    INVOICE("invoice", 3, false),
    OVER_STATUS("over_status", -6, false),
    IS_HIDE("is_hide", -6, false),
    LOAD_TIME("load_time", 4, false),
    MODIFY_TIME("modify_time", 4, false),
    PRINTNUM1("printnum1", 4, false),
    PRINTNUM2("printnum2", 4, false),
    COUPON_DISCOUNT("coupon_discount", 3, false),
    DISCOUNT_AMOUNT_RECEIVABLES("discount_amount_receivables", 3, false),
    RESULT_AMOUNT_RECEIVABLES("result_amount_receivables", 3, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<TotalpayinfoColEnum> pks = (new ImmutableList.Builder<TotalpayinfoColEnum>()).add(TOTALPAY_ID).build();

    private TotalpayinfoColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<TotalpayinfoColEnum> getPKs() {
        return pks;
    }
}
