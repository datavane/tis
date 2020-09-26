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
public enum PayinfoColEnum {

    PAY_ID("pay_id", 12, true),
    TOTALPAY_ID("totalpay_id", 12, false),
    KINDPAY_ID("kindpay_id", 12, false),
    KINDPAYNAME("kindpayname", 12, false),
    FEE("fee", 3, false),
    OPERATOR("operator", 12, false),
    OPERATOR_NAME("operator_name", 12, false),
    PAY_TIME("pay_time", -5, false),
    PAY("pay", 3, false),
    CHARGE("charge", 3, false),
    IS_VALID("is_valid", 5, false),
    ENTITY_ID("entity_id", 12, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", -5, false),
    OPUSER_ID("opuser_id", 12, false),
    CARD_ID("card_id", 12, false),
    CARD_ENTITY_ID("card_entity_id", 12, false),
    ONLINE_BILL_ID("online_bill_id", 12, false),
    TYPE("type", 5, false),
    CODE("code", 12, false),
    WAITINGPAY_ID("waitingpay_id", 12, false),
    LOAD_TIME("load_time", 4, false),
    MODIFY_TIME("modify_time", 4, false),
    IS_DEALED("is_dealed", -6, false),
    TYPE_NAME("type_name", 12, false),
    COUPON_FEE("coupon_fee", 3, false),
    COUPON_COST("coupon_cost", 3, false),
    COUPON_NUM("coupon_num", 5, false),
    PAY_FROM("pay_from", 5, false),
    PARENT_ENTITY_ID("parent_entity_id", 12, false),
    PARENT_ID("parent_id", 12, false),
    PARENT_CODE("parent_code", 12, false),
    KIND_CARD_ID("kind_card_id", 12, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<PayinfoColEnum> pks = (new ImmutableList.Builder<PayinfoColEnum>()).add(PAY_ID).build();

    private PayinfoColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<PayinfoColEnum> getPKs() {
        return pks;
    }
}
