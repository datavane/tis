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
public enum WaitingPayColEnum {

    ID("id", 12, true),
    ENTITY_ID("entity_id", 12, false),
    TYPE("type", 5, false),
    CODE("code", 12, false),
    EXT_ID("ext_id", 12, false),
    FEE("fee", 3, false),
    INNER_CODE("inner_code", 12, false),
    STATUS("status", 5, false),
    ORDER_ID("order_id", 12, false),
    ERROR_MESSAGE("error_message", 12, false),
    IS_VALID("is_valid", 5, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", -5, false),
    CUSTOMER_REGISTER_ID("customer_register_id", 12, false),
    MEMO("memo", 12, false),
    RELATION_ID("relation_id", 12, false),
    PAY_STATUS("pay_status", 5, false),
    PAY_FROM("pay_from", 5, false),
    CARD_ID("card_id", 12, false),
    CARD_ENTITY_ID("card_entity_id", 12, false),
    PAY("pay", 3, false),
    EXT("ext", 12, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<WaitingPayColEnum> pks = (new ImmutableList.Builder<WaitingPayColEnum>()).add(ID).build();

    private WaitingPayColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<WaitingPayColEnum> getPKs() {
        return pks;
    }
}
