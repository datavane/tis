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

import com.qlangtech.tis.realtime.transfer.AbstractRowValueGetter;
import java.io.Serializable;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Promotion extends AbstractRowValueGetter implements Serializable {

    private String promotionId;

    /**
     * prop:分库路由
     */
    private String entityId;

    /**
     * prop:waitingorder的id
     */
    private String waitingorderId;

    /**
     * prop:订单的id
     */
    private String orderId;

    /**
     * prop:有效标识
     */
    private Short isValid;

    /**
     * prop:创建时间，单位是毫秒(ms)
     */
    private Long createTime;

    /**
     * prop:修改时间，单位是毫秒(ms)
     */
    private Long opTime;

    /**
     * prop:优惠活动信息，以json格式存放
     */
    private String content;

    private static final long serialVersionUID = 1L;

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId == null ? null : promotionId.trim();
    }

    /**
     * get:分库路由
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:分库路由
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:waitingorder的id
     */
    public String getWaitingorderId() {
        return waitingorderId;
    }

    /**
     * set:waitingorder的id
     */
    public void setWaitingorderId(String waitingorderId) {
        this.waitingorderId = waitingorderId == null ? null : waitingorderId.trim();
    }

    /**
     * get:订单的id
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * set:订单的id
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * get:有效标识
     */
    public Short getIsValid() {
        return isValid;
    }

    /**
     * set:有效标识
     */
    public void setIsValid(Short isValid) {
        this.isValid = isValid;
    }

    /**
     * get:创建时间，单位是毫秒(ms)
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * set:创建时间，单位是毫秒(ms)
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * get:修改时间，单位是毫秒(ms)
     */
    public Long getOpTime() {
        return opTime;
    }

    /**
     * set:修改时间，单位是毫秒(ms)
     */
    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }

    /**
     * get:优惠活动信息，以json格式存放
     */
    public String getContent() {
        return content;
    }

    /**
     * set:优惠活动信息，以json格式存放
     */
    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }
}
