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
public enum WaitinginstanceinfoColEnum {

    WAITINGINSTANCE_ID("waitinginstance_id", 12, true),
    WAITINGORDER_ID("waitingorder_id", 12, false),
    KIND("kind", 5, false),
    KINDMENU_ID("kindmenu_id", 12, false),
    KINDMENU_NAME("kindmenu_name", 12, false),
    NAME("name", 12, false),
    MENU_ID("menu_id", 12, false),
    MAKE_ID("make_id", 12, false),
    MAKENAME("makename", 12, false),
    MAKE_PRICE("make_price", 3, false),
    MAKE_PRICEMODE("make_pricemode", 5, false),
    SPEC_DETAIL_NAME("spec_detail_name", 12, false),
    SPEC_DETAIL_ID("spec_detail_id", 12, false),
    SPEC_PRICEMODE("spec_pricemode", 5, false),
    SPEC_DETAIL_PRICE("spec_detail_price", 3, false),
    NUM("num", 3, false),
    ACCOUNT_NUM("account_num", 3, false),
    UNIT("unit", 12, false),
    ACCOUNT_UNIT("account_unit", 12, false),
    MEMO("memo", 12, false),
    ORIGINAL_PRICE("original_price", 3, false),
    PRICE("price", 3, false),
    MEMBER_PRICE("member_price", 3, false),
    FEE("fee", 3, false),
    IS_RATIO("is_ratio", 5, false),
    TASTE("taste", 12, false),
    RATIO("ratio", 3, false),
    RATIO_FEE("ratio_fee", 3, false),
    IS_BACKAUTH("is_backauth", 5, false),
    PARENT_ID("parent_id", 12, false),
    PRICE_MODE("price_mode", 5, false),
    CHILD_ID("child_id", 12, false),
    SERVICE_FEEMODE("service_feemode", 5, false),
    SERVICE_FEE("service_fee", 3, false),
    STATUS("status", 5, false),
    ERROR_MSG("error_msg", 12, false),
    ENTITY_ID("entity_id", 12, false),
    IS_VALID("is_valid", 5, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", -5, false),
    BATCH_MSG("batch_msg", 12, false),
    TYPE("type", 5, false),
    ADDITION_PRICE("addition_price", 3, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<WaitinginstanceinfoColEnum> pks = (new ImmutableList.Builder<WaitinginstanceinfoColEnum>()).add(WAITINGINSTANCE_ID).build();

    private WaitinginstanceinfoColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<WaitinginstanceinfoColEnum> getPKs() {
        return pks;
    }
}
