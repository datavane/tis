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
public enum RefundPayItemColEnum {

    ID("id", 12, true),
    ENTITY_ID("entity_id", 12, false),
    ORDER_REFUND_ID("order_refund_id", 12, false),
    ORDER_ID("order_id", 12, false),
    WAITING_PAY_ID("waiting_pay_id", 12, false),
    PAY_ID("pay_id", 12, false),
    STATUS("status", -6, false),
    FINISH_TIME("finish_time", -5, false),
    MSG("msg", 12, false),
    SHOULD_FEE("should_fee", 4, false),
    ACTUAL_FEE("actual_fee", 4, false),
    DEDUCT_RATIO("deduct_ratio", 8, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", 4, false),
    IS_VALID("is_valid", -6, false),
    EXT("ext", 12, false),
    FROM_TYPE("from_type", -6, false),
    TYPE("type", 4, false),
    REFUND_WAY("refund_way", -6, false),
    RELA_WAITING_PAY_ID("rela_waiting_pay_id", 12, false),
    KINDPAY_ID("kindpay_id", 12, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<RefundPayItemColEnum> pks = (new ImmutableList.Builder<RefundPayItemColEnum>()).add(ID).build();

    private RefundPayItemColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<RefundPayItemColEnum> getPKs() {
        return pks;
    }
}
