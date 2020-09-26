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
package com.qlangtech.tis.realtime.test.member.pojo;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public enum CardColEnum {

    ID("id", 12, true),
    KIND_CARD_ID("kind_card_id", 12, false),
    CUSTOMER_ID("customer_id", 12, false),
    CODE("code", 12, false),
    INNER_CODE("inner_code", 12, false),
    PWD("pwd", 12, false),
    PAY("pay", 3, false),
    ACTIVE_DATE("active_date", -5, false),
    PRE_FEE("pre_fee", 3, false),
    BALANCE("balance", 3, false),
    GIFT_BALANCE("gift_balance", 3, false),
    REAL_BALANCE("real_balance", 3, false),
    DEGREE("degree", 3, false),
    PAY_AMOUNT("pay_amount", 3, false),
    CONSUME_AMOUNT("consume_amount", 3, false),
    RATIO_AMOUNT("ratio_amount", 3, false),
    STATUS("status", 5, false),
    GET_STATUS("get_status", 5, false),
    ACTIVE_ID("active_id", 12, false),
    ENTITY_ID("entity_id", 12, false),
    IS_VALID("is_valid", -7, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", -5, false),
    SELLER_ID("seller_id", 12, false),
    LAST_CONSUME_TIME("last_consume_time", -5, false),
    CONSUME_NUM("consume_num", 4, false),
    EXTEND_FIELDS("extend_fields", 12, false),
    KIND_CARD_TYPE("kind_card_type", -7, false),
    GIVE_BALANCE("give_balance", 3, false),
    CARD_SOURCE("card_source", 5, false),
    SHOP_MEMBER_SYSTEM_ID("shop_member_system_id", 12, false),
    TRANSFER_FLG("transfer_flg", -7, false),
    IS_EFFECTIVE("is_effective", -6, false),
    SOURCE("source", 12, false),
    ACTIVITY_SOURCE("activity_source", 5, false),
    ACTIVITY_ID("activity_id", 12, false),
    FREEZE_BALANCE("freeze_balance", 3, false),
    FREEZE_GIVE_BALANCE("freeze_give_balance", 3, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<CardColEnum> pks = (new ImmutableList.Builder<CardColEnum>()).add(ID).build();

    private CardColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<CardColEnum> getPKs() {
        return pks;
    }
}
