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
public enum InstancedetailColEnum {

    INSTANCE_ID("instance_id", 12, true),
    ORDER_ID("order_id", 12, false),
    BATCH_MSG("batch_msg", 12, false),
    TYPE("type", 5, false),
    WAITINGINSTANCE_ID("waitinginstance_id", 12, false),
    KIND("kind", 5, false),
    PARENT_ID("parent_id", 12, false),
    PRICEMODE("pricemode", 5, false),
    NAME("name", 12, false),
    MAKENAME("makename", 12, false),
    TASTE("taste", 12, false),
    SPEC_DETAIL_NAME("spec_detail_name", 12, false),
    NUM("num", 3, false),
    ACCOUNT_NUM("account_num", 3, false),
    UNIT("unit", 12, false),
    ACCOUNT_UNIT("account_unit", 12, false),
    PRICE("price", 3, false),
    MEMBER_PRICE("member_price", 3, false),
    FEE("fee", 3, false),
    RATIO("ratio", 3, false),
    RATIO_FEE("ratio_fee", 3, false),
    RATIO_CAUSE("ratio_cause", 12, false),
    STATUS("status", 5, false),
    KINDMENU_ID("kindmenu_id", 12, false),
    KINDMENU_NAME("kindmenu_name", 12, false),
    MENU_ID("menu_id", 12, false),
    MEMO("memo", 12, false),
    IS_RATIO("is_ratio", 5, false),
    ENTITY_ID("entity_id", 12, false),
    IS_VALID("is_valid", 5, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", -5, false),
    LOAD_TIME("load_time", 4, false),
    MODIFY_TIME("modify_time", 4, false),
    DRAW_STATUS("draw_status", -6, false),
    BOOKMENU_ID("bookmenu_id", 12, false),
    MAKE_ID("make_id", 12, false),
    MAKE_PRICE("make_price", 3, false),
    PRODPLAN_ID("prodplan_id", 12, false),
    IS_WAIT("is_wait", -6, false),
    SPECDETAIL_ID("specdetail_id", 12, false),
    SPECDETAIL_PRICE("specdetail_price", 3, false),
    MAKEPRICE_MODE("makeprice_mode", -6, false),
    ORIGINAL_PRICE("original_price", 12, false),
    IS_BUYNUMBER_CHANGED("is_buynumber_changed", -6, false),
    RATIO_OPERATOR_ID("ratio_operator_id", 12, false),
    CHILD_ID("child_id", 12, false),
    KIND_BOOKMENU_ID("kind_bookmenu_id", 12, false),
    SPECPRICE_MODE("specprice_mode", -6, false),
    WORKER_ID("worker_id", 12, false),
    IS_BACKAUTH("is_backauth", -6, false),
    SERVICE_FEE_MODE("service_fee_mode", -6, false),
    SERVICE_FEE("service_fee", 12, false),
    ORIGN_ID("orign_id", 12, false),
    ADDITION_PRICE("addition_price", 3, false),
    HAS_ADDITION("has_addition", -6, false),
    SEAT_ID("seat_id", 12, false),
    EXT("ext", -1, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<InstancedetailColEnum> pks = (new ImmutableList.Builder<InstancedetailColEnum>()).add(INSTANCE_ID).build();

    private InstancedetailColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<InstancedetailColEnum> getPKs() {
        return pks;
    }
}
