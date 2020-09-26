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
public enum PresellOrderExtraColEnum {

    ORDER_ID("order_id", 12, true),
    ENTITY_ID("entity_id", 12, false),
    STOCK_ID("stock_id", -5, false),
    TIME_FRAME_ID("time_frame_id", -5, false),
    TIME_FRAME_NAME("time_frame_name", 12, false),
    SEAT_TYPE_ID("seat_type_id", -5, false),
    SEAT_TYPE_NAME("seat_type_name", 12, false),
    DISCOUNT_RATIO("discount_ratio", 8, false),
    START_TIME("start_time", -5, false),
    END_TIME("end_time", -5, false),
    VERIFY_TIME("verify_time", -5, false),
    LAST_VER("last_ver", 4, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    OVERTIME("overtime", -5, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<PresellOrderExtraColEnum> pks = (new ImmutableList.Builder<PresellOrderExtraColEnum>()).add(ORDER_ID).build();

    private PresellOrderExtraColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<PresellOrderExtraColEnum> getPKs() {
        return pks;
    }
}
