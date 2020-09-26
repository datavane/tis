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
public enum OrderRefundColEnum {

    ID("id", 12, true),
    ENTITY_ID("entity_id", 12, false),
    ORDER_ID("order_id", 12, false),
    REFUND_FROM("refund_from", -6, false),
    REASON("reason", 12, false),
    OP_USER_ID("op_user_id", 12, false),
    STATUS("status", -6, false),
    CREATE_TIME("create_time", -5, false),
    OP_TIME("op_time", -5, false),
    LAST_VER("last_ver", 4, false),
    IS_VALID("is_valid", -6, false),
    EXT("ext", 12, false),
    FINISH_TIME("finish_time", -5, false),
    SUB_STATUS("sub_status", -6, false),
    REFUND_CODE("refund_code", 12, false),
    MAX_REFUND_FEE("max_refund_fee", 4, false),
    APPLY_REFUND_FEE("apply_refund_fee", 4, false),
    REASON_TYPE("reason_type", -6, false),
    APPLY_DESC("apply_desc", 12, false),
    REJECT_DESC("reject_desc", 12, false),
    PIC_EVIDENCE("pic_evidence", 12, false),
    APPLY_USER_ID("apply_user_id", 12, false),
    TIMED_TASK_JSON("timed_task_json", 12, false),
    NEED_AUDIT("need_audit", -6, false),
    REFUND_SCENE("refund_scene", 5, false),
    ORDER_FROM("order_from", 5, false),
    ORDER_KIND("order_kind", 5, false),
    LIQUIDATED_DAMAGES_FEE("liquidated_damages_fee", 4, false),
    WAITINGORDER_ID("waitingorder_id", 12, false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<OrderRefundColEnum> pks = (new ImmutableList.Builder<OrderRefundColEnum>()).add(ID).build();

    private OrderRefundColEnum(String name, int jdbcType, boolean pk) {
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

    public static List<OrderRefundColEnum> getPKs() {
        return pks;
    }
}
