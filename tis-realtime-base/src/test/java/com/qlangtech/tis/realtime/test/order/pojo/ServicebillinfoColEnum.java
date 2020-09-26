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
public enum ServicebillinfoColEnum {

    SERVICEBILL_ID("servicebill_id", 12, true),
    OUTFEE("outfee", 3, false),
    ORIGIN_AMOUNT("origin_amount", 3, false),
    ORIGIN_SERVICE_CHARGE("origin_service_charge", 3, false),
    ORIGIN_LEAST_AMOUNT("origin_least_amount", 3, false),
    AGIO_AMOUNT("agio_amount", 3, false),
    AGIO_SERVICE_CHARGE("agio_service_charge", 3, false),
    AGIO_LEAST_AMOUNT("agio_least_amount", 3, false),
    ORIGIN_RECEIVABLES_AMOUNT("origin_receivables_amount", 3, false),
    AGIO_RECEIVABLES_AMOUNT("agio_receivables_amount", 3, false),
    ENTITY_ID("entity_id", 12, false),
    IS_VALID("is_valid", 5, false),
    AGIO_TOTAL("agio_total", 3, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", -5, false),
    OP_USER_ID("op_user_id", 12, false),
    RESERVE_AMOUNT("reserve_amount", 3, false),
    ORIGIN_TOTAL("origin_total", 3, false),
    FINAL_AMOUNT("final_amount", 3, false),
    LOAD_TIME("load_time", 4, false),
    MODIFY_TIME("modify_time", 4, false),
    USE_CASH_PROMOTION("use_cash_promotion", -6, false),
    TAX_FEE("tax_fee", 3, false),
    AGIO_TOTAL_RECEIVABLES("agio_total_receivables", 3, false),
    AGIO_RECEIVABLES_AMOUNT_RECEIVABLES("agio_receivables_amount_receivables", 3, false),
    FINAL_AMOUNT_RECEIVABLES("final_amount_receivables", 3, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<ServicebillinfoColEnum> pks = (new ImmutableList.Builder<ServicebillinfoColEnum>()).add(SERVICEBILL_ID).build();

    private ServicebillinfoColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<ServicebillinfoColEnum> getPKs() {
        return pks;
    }
}
