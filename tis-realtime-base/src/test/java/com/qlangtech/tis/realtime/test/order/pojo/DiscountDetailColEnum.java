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
public enum DiscountDetailColEnum {

    ID("id", 12, true),
    INSTANCE_ID("instance_id", 12, false),
    ORDER_ID("order_id", 12, false),
    DISCOUNT_ID("discount_id", 12, false),
    DISCOUNT_NAME("discount_name", 12, false),
    DISCOUNT_TYPE("discount_type", 4, false),
    DISCOUNT_SUB_TYPE("discount_sub_type", 4, false),
    DISCOUNT_FEE("discount_fee", 3, false),
    DISCOUNT_RATIO("discount_ratio", 3, false),
    ORDER_DISCOUNT_FEE("order_discount_fee", 3, false),
    RATIO_FEE("ratio_fee", 3, false),
    ORIGIN_FEE("origin_fee", 3, false),
    ENTITY_ID("entity_id", 12, false),
    LAST_VER("last_ver", 4, false),
    IS_VALID("is_valid", 4, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    OP_USER_ID("op_user_id", 12, false),
    ACTIVITY_ID("activity_id", 12, false),
    LOAD_TIME("load_time", 4, false),
    MODIFY_TIME("modify_time", 4, false),
    ORDER_PROMOTION_ID("order_promotion_id", 12, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<DiscountDetailColEnum> pks = (new ImmutableList.Builder<DiscountDetailColEnum>()).add(ID).build();

    private DiscountDetailColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<DiscountDetailColEnum> getPKs() {
        return pks;
    }
}
