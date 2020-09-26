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
public enum InstanceAssetColEnum {

    ID("id", 12, true),
    OP_USER_ID("op_user_id", 12, false),
    VERIFY_USER_ID("verify_user_id", 12, false),
    VERIFY_TIME("verify_time", -5, false),
    ASSET_STATUS("asset_status", -6, false),
    INSTANCE_ID("instance_id", 12, false),
    ORDER_ID("order_id", 12, false),
    ASSET_CODE("asset_code", 12, false),
    MOBILE("mobile", 12, false),
    ENTITY_ID("entity_id", 12, false),
    IS_VALID("is_valid", -6, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", 4, false),
    EXT("ext", 12, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<InstanceAssetColEnum> pks = (new ImmutableList.Builder<InstanceAssetColEnum>()).add(ID).build();

    private InstanceAssetColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<InstanceAssetColEnum> getPKs() {
        return pks;
    }
}
