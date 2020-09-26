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
import java.math.BigDecimal;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Specialfee extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:id
     */
    private String specialfeeId;

    /**
     * prop:相关账单
     */
    private String totalpayId;

    /**
     * prop:订单ID
     */
    private String orderId;

    /**
     * prop:额外费用信息、最低消费信息、损益信息，分别对应kind=1/2/3
     */
    private Byte kind;

    /**
     * prop:额外费用详细，从额外费用明细中取得
     */
    private String feedetailId;

    /**
     * prop:发生额
     */
    private BigDecimal fee;

    /**
     * prop:所属实体
     */
    private String entityId;

    /**
     * prop:是否有效
     */
    private Byte isValid;

    /**
     * prop:记录生成时间
     */
    private Long createTime;

    /**
     * prop:操作时间
     */
    private Long opTime;

    /**
     * prop:版本号
     */
    private Long lastVer;

    /**
     * prop:操作人
     */
    private String opUserId;

    /**
     * prop:服务器创建时间
     */
    private Integer loadTime;

    /**
     * prop:服务器修改时间
     */
    private Integer modifyTime;

    private static final long serialVersionUID = 1L;

    /**
     * get:id
     */
    public String getSpecialfeeId() {
        return specialfeeId;
    }

    /**
     * set:id
     */
    public void setSpecialfeeId(String specialfeeId) {
        this.specialfeeId = specialfeeId == null ? null : specialfeeId.trim();
    }

    /**
     * get:相关账单
     */
    public String getTotalpayId() {
        return totalpayId;
    }

    /**
     * set:相关账单
     */
    public void setTotalpayId(String totalpayId) {
        this.totalpayId = totalpayId == null ? null : totalpayId.trim();
    }

    /**
     * get:订单ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * set:订单ID
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * get:额外费用信息、最低消费信息、损益信息，分别对应kind=1/2/3
     */
    public Byte getKind() {
        return kind;
    }

    /**
     * set:额外费用信息、最低消费信息、损益信息，分别对应kind=1/2/3
     */
    public void setKind(Byte kind) {
        this.kind = kind;
    }

    /**
     * get:额外费用详细，从额外费用明细中取得
     */
    public String getFeedetailId() {
        return feedetailId;
    }

    /**
     * set:额外费用详细，从额外费用明细中取得
     */
    public void setFeedetailId(String feedetailId) {
        this.feedetailId = feedetailId == null ? null : feedetailId.trim();
    }

    /**
     * get:发生额
     */
    public BigDecimal getFee() {
        return fee;
    }

    /**
     * set:发生额
     */
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    /**
     * get:所属实体
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:所属实体
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:是否有效
     */
    public Byte getIsValid() {
        return isValid;
    }

    /**
     * set:是否有效
     */
    public void setIsValid(Byte isValid) {
        this.isValid = isValid;
    }

    /**
     * get:记录生成时间
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * set:记录生成时间
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * get:操作时间
     */
    public Long getOpTime() {
        return opTime;
    }

    /**
     * set:操作时间
     */
    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }

    /**
     * get:版本号
     */
    public Long getLastVer() {
        return lastVer;
    }

    /**
     * set:版本号
     */
    public void setLastVer(Long lastVer) {
        this.lastVer = lastVer;
    }

    /**
     * get:操作人
     */
    public String getOpUserId() {
        return opUserId;
    }

    /**
     * set:操作人
     */
    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId == null ? null : opUserId.trim();
    }

    /**
     * get:服务器创建时间
     */
    public Integer getLoadTime() {
        return loadTime;
    }

    /**
     * set:服务器创建时间
     */
    public void setLoadTime(Integer loadTime) {
        this.loadTime = loadTime;
    }

    /**
     * get:服务器修改时间
     */
    public Integer getModifyTime() {
        return modifyTime;
    }

    /**
     * set:服务器修改时间
     */
    public void setModifyTime(Integer modifyTime) {
        this.modifyTime = modifyTime;
    }
}
