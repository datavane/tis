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
public enum GridFieldColEnum {

    ID("id", -5, true),
    GRID_NAME("grid_name", 12, false),
    FIELD_NAME("field_name", 12, false),
    FIELD_CAPTION("field_caption", 12, false),
    DISPLAY_ORDER("display_order", 4, false),
    FIELD_DEFAULT_VALUE("field_default_value", 12, false),
    FIELD_CAN_USED("field_can_used", 12, false),
    DIC_NO("dic_no", 12, false),
    FIELD_TYPE("field_type", -6, false),
    IS_VALID("is_valid", -7, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", 4, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<GridFieldColEnum> pks = (new ImmutableList.Builder<GridFieldColEnum>()).add(ID).build();

    private GridFieldColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<GridFieldColEnum> getPKs() {
        return pks;
    }
}
