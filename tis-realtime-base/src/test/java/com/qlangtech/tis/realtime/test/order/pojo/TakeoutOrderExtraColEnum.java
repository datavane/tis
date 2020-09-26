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
public enum TakeoutOrderExtraColEnum {

    ORDER_ID("order_id", 12, true),
    ORDER_FROM("order_from", 5, false),
    VIEW_ID("view_id", 12, false),
    HAS_INVOICED("has_invoiced", -6, false),
    INVOICE_TITLE("invoice_title", 12, false),
    IS_THIRD_SHIPPING("is_third_shipping", -6, false),
    DAY_SEQ("day_seq", 12, false),
    COURIER_NAME("courier_name", 12, false),
    COURIER_PHONE("courier_phone", 12, false),
    CANCEL_REASON("cancel_reason", 12, false),
    ENTITY_ID("entity_id", 12, false),
    OUT_ID("out_id", 12, false),
    LAST_VER("last_ver", 4, false),
    BEGIN_EXPECT_DATE("begin_expect_date", -5, false),
    END_EXPECT_DATE("end_expect_date", -5, false),
    RESERVE_DATE_NAME("reserve_date_name", 12, false),
    ACTIVITY("activity", -1, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<TakeoutOrderExtraColEnum> pks = (new ImmutableList.Builder<TakeoutOrderExtraColEnum>()).add(ORDER_ID).build();

    private TakeoutOrderExtraColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<TakeoutOrderExtraColEnum> getPKs() {
        return pks;
    }
}
