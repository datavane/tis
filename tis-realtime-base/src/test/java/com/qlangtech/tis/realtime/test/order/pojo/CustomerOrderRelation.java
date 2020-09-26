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
public class CustomerOrderRelation extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:用户id
     */
    private String customerregisterId;

    /**
     * prop:订单waitingorderid
     */
    private String waitingorderId;

    /**
     * prop:订单类型:1/订位;2/外卖;3/扫码加菜;4/扫桌码开单
     */
    private Short kind;

    /**
     * prop:创建时间
     */
    private Long createTime;

    /**
     * prop:版本号
     */
    private Integer lastVer;

    private static final long serialVersionUID = 1L;

    /**
     * get:用户id
     */
    public String getCustomerregisterId() {
        return customerregisterId;
    }

    /**
     * set:用户id
     */
    public void setCustomerregisterId(String customerregisterId) {
        this.customerregisterId = customerregisterId == null ? null : customerregisterId.trim();
    }

    /**
     * get:订单waitingorderid
     */
    public String getWaitingorderId() {
        return waitingorderId;
    }

    /**
     * set:订单waitingorderid
     */
    public void setWaitingorderId(String waitingorderId) {
        this.waitingorderId = waitingorderId == null ? null : waitingorderId.trim();
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
     * get:创建时间
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * set:创建时间
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * get:版本号
     */
    public Integer getLastVer() {
        return lastVer;
    }

    /**
     * set:版本号
     */
    public void setLastVer(Integer lastVer) {
        this.lastVer = lastVer;
    }
}
