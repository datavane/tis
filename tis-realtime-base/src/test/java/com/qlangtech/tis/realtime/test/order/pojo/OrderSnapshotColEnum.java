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
public enum OrderSnapshotColEnum {

    SNAPSHOT_ID("snapshot_id", 12, true),
    ORDER_ID("order_id", 12, false),
    WAITINGORDER_ID("waitingorder_id", 12, false),
    CUSTOMERREGISTER_ID("customerregister_id", 12, false),
    TOTAL_FEE("total_fee", 4, false),
    NEED_FEE("need_fee", 4, false),
    DISCOUNT_FEE("discount_fee", 4, false),
    SERVICE_FEE("service_fee", 4, false),
    PAYED_FEE("payed_fee", 4, false),
    PROMOTION_FROM("promotion_from", 5, false),
    STATUS("status", 5, false),
    MD5("md5", 12, false),
    ENTITY_ID("entity_id", 12, false),
    LAST_VER("last_ver", 4, false),
    IS_VALID("is_valid", 5, false),
    OP_TIME("op_time", -5, false),
    CREATE_TIME("create_time", -5, false),
    ORIGIN_FEE("origin_fee", 4, false),
    ORDER_CTIME("order_ctime", -5, false),
    LEAST_AMOUNT("least_amount", 4, false),
    PROMOTIONS("promotions", -1, false),
    FUNDS("funds", -1, false),
    INC_INSTANCES("inc_instances", -1, false),
    ALL_INSTANCES("all_instances", -1, false),
    EXT("ext", -1, false),
    THIRD_PROMOTIONS("third_promotions", -1, false),
    THIRD_FUNDS("third_funds", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<OrderSnapshotColEnum> pks = (new ImmutableList.Builder<OrderSnapshotColEnum>()).add(SNAPSHOT_ID).build();

    private OrderSnapshotColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<OrderSnapshotColEnum> getPKs() {
        return pks;
    }
}
