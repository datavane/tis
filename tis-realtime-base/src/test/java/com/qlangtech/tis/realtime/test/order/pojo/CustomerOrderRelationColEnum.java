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
public enum CustomerOrderRelationColEnum {

    CUSTOMERREGISTER_ID("customerregister_id", 12, true), WAITINGORDER_ID("waitingorder_id", 12, true), KIND("kind", 5, false), CREATE_TIME("create_time", -5, false), LAST_VER("last_ver", 4, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<CustomerOrderRelationColEnum> pks = (new ImmutableList.Builder<CustomerOrderRelationColEnum>()).add(CUSTOMERREGISTER_ID).add(WAITINGORDER_ID).build();

    private CustomerOrderRelationColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<CustomerOrderRelationColEnum> getPKs() {
        return pks;
    }
}
