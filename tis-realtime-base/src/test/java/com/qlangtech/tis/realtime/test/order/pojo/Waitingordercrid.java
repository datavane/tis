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
public class Waitingordercrid extends AbstractRowValueGetter implements Serializable {

    private String waitingorderId;

    private String customerregisterId;

    private String entityId;

    /**
     * prop:订单类型:1/订位;2/外卖;3/扫码加菜;4/扫桌码开单
     */
    private Short kind;

    /**
     * prop:预订状态定义：
     * 2- 待审核
     * 3- 已取消
     * 4- 已生效
     * 5- 已到达
     * 外卖的状态定义：
     * 2- 待审核
     * 3- 已取消
     * 4- 已生效，未下单
     * 5- 已生效，已下单
     * 6- 已生效，已送货
     * 7- 已结单
     * 其他：
     * -1- 已超时
     */
    private Short status;

    private Long lastVer;

    private Short isValid;

    private Long createTime;

    private Long opTime;

    private static final long serialVersionUID = 1L;

    public String getWaitingorderId() {
        return waitingorderId;
    }

    public void setWaitingorderId(String waitingorderId) {
        this.waitingorderId = waitingorderId == null ? null : waitingorderId.trim();
    }

    public String getCustomerregisterId() {
        return customerregisterId;
    }

    public void setCustomerregisterId(String customerregisterId) {
        this.customerregisterId = customerregisterId == null ? null : customerregisterId.trim();
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:订单类型:1/订位;2/外卖;3/扫码加菜;4/扫桌码开单
     */
    public Short getKind() {
        return kind;
    }

    /**
     * set:订单类型:1/订位;2/外卖;3/扫码加菜;4/扫桌码开单
     */
    public void setKind(Short kind) {
        this.kind = kind;
    }

    /**
     * get:预订状态定义：
     * 2- 待审核
     * 3- 已取消
     * 4- 已生效
     * 5- 已到达
     * 外卖的状态定义：
     * 2- 待审核
     * 3- 已取消
     * 4- 已生效，未下单
     * 5- 已生效，已下单
     * 6- 已生效，已送货
     * 7- 已结单
     * 其他：
     * -1- 已超时
     */
    public Short getStatus() {
        return status;
    }

    /**
     * set:预订状态定义：
     * 2- 待审核
     * 3- 已取消
     * 4- 已生效
     * 5- 已到达
     * 外卖的状态定义：
     * 2- 待审核
     * 3- 已取消
     * 4- 已生效，未下单
     * 5- 已生效，已下单
     * 6- 已生效，已送货
     * 7- 已结单
     * 其他：
     * -1- 已超时
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    public Long getLastVer() {
        return lastVer;
    }

    public void setLastVer(Long lastVer) {
        this.lastVer = lastVer;
    }

    public Short getIsValid() {
        return isValid;
    }

    public void setIsValid(Short isValid) {
        this.isValid = isValid;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getOpTime() {
        return opTime;
    }

    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }
}
