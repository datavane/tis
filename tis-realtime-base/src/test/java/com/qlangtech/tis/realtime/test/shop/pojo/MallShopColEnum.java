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
package com.qlangtech.tis.realtime.test.shop.pojo;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public enum MallShopColEnum {

    ID("id", 12, true),
    SHOP_ENTITY_ID("shop_entity_id", 12, false),
    MALL_ENTITY_ID("mall_entity_id", 12, false),
    MALL_TYPE("mall_type", 4, false),
    STATUS("status", -7, false),
    AREA_ID("area_id", 12, false),
    CASH_TYPE("cash_type", -7, false),
    CREATE_TIME("create_time", 4, false),
    OP_TIME("op_time", 4, false),
    LAST_VER("last_ver", 4, false),
    IS_VALID("is_valid", -7, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<MallShopColEnum> pks = (new ImmutableList.Builder<MallShopColEnum>()).add(ID).build();

    private MallShopColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<MallShopColEnum> getPKs() {
        return pks;
    }
}
