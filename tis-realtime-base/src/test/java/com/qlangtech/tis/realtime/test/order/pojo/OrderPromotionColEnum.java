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
public enum OrderPromotionColEnum {

    ID("id", 12, true),
    ORDER_ID("order_id", 12, false),
    PROMOTION_ID("promotion_id", 12, false),
    PROMOTION_SHOW_NAME("promotion_show_name", 12, false),
    PROMOTION_NAME("promotion_name", 12, false),
    PROMOTION_TYPE("promotion_type", 4, false),
    PROMOTION_SUB_TYPE("promotion_sub_type", -6, false),
    PROMOTION_FEE("promotion_fee", 3, false),
    PROMOTION_RATIO("promotion_ratio", 3, false),
    ENTITY_ID("entity_id", 12, false),
    PROMOTION_SOURCE("promotion_source", 4, false),
    EXT("ext", 12, false),
    LAST_VER("last_ver", 4, false),
    IS_VALID("is_valid", 4, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LOAD_TIME("load_time", 4, false),
    MODIFY_TIME("modify_time", 4, false),
    OP_USER_ID("op_user_id", 12, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<OrderPromotionColEnum> pks = (new ImmutableList.Builder<OrderPromotionColEnum>()).add(ID).build();

    private OrderPromotionColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<OrderPromotionColEnum> getPKs() {
        return pks;
    }
}
