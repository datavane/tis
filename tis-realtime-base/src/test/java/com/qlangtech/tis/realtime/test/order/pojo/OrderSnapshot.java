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
public class OrderSnapshot extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:主键id
     */
    private String snapshotId;

    /**
     * prop:订单id
     */
    private String orderId;

    /**
     * prop:预支付订单id
     */
    private String waitingorderId;

    /**
     * prop:支付用户id
     */
    private String customerregisterId;

    /**
     * prop:总金额
     */
    private Integer totalFee;

    /**
     * prop:待支付金额
     */
    private Integer needFee;

    /**
     * prop:折扣金额
     */
    private Integer discountFee;

    /**
     * prop:服务费
     */
    private Integer serviceFee;

    /**
     * prop:已支付金额
     */
    private Integer payedFee;

    /**
     * prop:优惠来源，1：收银机，2：云端
     */
    private Short promotionFrom;

    /**
     * prop:快照状态，-1：支付失败，1：支付成功，2：正在支付
     */
    private Short status;

    /**
     * prop:md5数据，用于判断账单是否一样
     */
    private String md5;

    /**
     * prop:实体id
     */
    private String entityId;

    /**
     * prop:版本号
     */
    private Integer lastVer;

    /**
     * prop:是否有效
     */
    private Short isValid;

    /**
     * prop:修改时间
     */
    private Long opTime;

    /**
     * prop:创建时间
     */
    private Long createTime;

    /**
     * prop:原始金额
     */
    private Integer originFee;

    /**
     * prop:订单创建时间
     */
    private Long orderCtime;

    /**
     * prop:最低消费金额
     */
    private Integer leastAmount;

    /**
     * prop:优惠json
     */
    private String promotions;

    /**
     * prop:支付方式json
     */
    private String funds;

    /**
     * prop:新增菜json
     */
    private String incInstances;

    /**
     * prop:所有菜json
     */
    private String allInstances;

    /**
     * prop:扩展字段
     */
    private String ext;

    /**
     * prop:第三方优惠
     */
    private String thirdPromotions;

    /**
     * prop:第三方支付
     */
    private String thirdFunds;

    private static final long serialVersionUID = 1L;

    /**
     * get:主键id
     */
    public String getSnapshotId() {
        return snapshotId;
    }

    /**
     * set:主键id
     */
    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId == null ? null : snapshotId.trim();
    }

    /**
     * get:订单id
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * set:订单id
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * get:预支付订单id
     */
    public String getWaitingorderId() {
        return waitingorderId;
    }

    /**
     * set:预支付订单id
     */
    public void setWaitingorderId(String waitingorderId) {
        this.waitingorderId = waitingorderId == null ? null : waitingorderId.trim();
    }

    /**
     * get:支付用户id
     */
    public String getCustomerregisterId() {
        return customerregisterId;
    }

    /**
     * set:支付用户id
     */
    public void setCustomerregisterId(String customerregisterId) {
        this.customerregisterId = customerregisterId == null ? null : customerregisterId.trim();
    }

    /**
     * get:总金额
     */
    public Integer getTotalFee() {
        return totalFee;
    }

    /**
     * set:总金额
     */
    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    /**
     * get:待支付金额
     */
    public Integer getNeedFee() {
        return needFee;
    }

    /**
     * set:待支付金额
     */
    public void setNeedFee(Integer needFee) {
        this.needFee = needFee;
    }

    /**
     * get:折扣金额
     */
    public Integer getDiscountFee() {
        return discountFee;
    }

    /**
     * set:折扣金额
     */
    public void setDiscountFee(Integer discountFee) {
        this.discountFee = discountFee;
    }

    /**
     * get:服务费
     */
    public Integer getServiceFee() {
        return serviceFee;
    }

    /**
     * set:服务费
     */
    public void setServiceFee(Integer serviceFee) {
        this.serviceFee = serviceFee;
    }

    /**
     * get:已支付金额
     */
    public Integer getPayedFee() {
        return payedFee;
    }

    /**
     * set:已支付金额
     */
    public void setPayedFee(Integer payedFee) {
        this.payedFee = payedFee;
    }

    /**
     * get:优惠来源，1：收银机，2：云端
     */
    public Short getPromotionFrom() {
        return promotionFrom;
    }

    /**
     * set:优惠来源，1：收银机，2：云端
     */
    public void setPromotionFrom(Short promotionFrom) {
        this.promotionFrom = promotionFrom;
    }

    /**
     * get:快照状态，-1：支付失败，1：支付成功，2：正在支付
     */
    public Short getStatus() {
        return status;
    }

    /**
     * set:快照状态，-1：支付失败，1：支付成功，2：正在支付
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * get:md5数据，用于判断账单是否一样
     */
    public String getMd5() {
        return md5;
    }

    /**
     * set:md5数据，用于判断账单是否一样
     */
    public void setMd5(String md5) {
        this.md5 = md5 == null ? null : md5.trim();
    }

    /**
     * get:实体id
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:实体id
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
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

    /**
     * get:是否有效
     */
    public Short getIsValid() {
        return isValid;
    }

    /**
     * set:是否有效
     */
    public void setIsValid(Short isValid) {
        this.isValid = isValid;
    }

    /**
     * get:修改时间
     */
    public Long getOpTime() {
        return opTime;
    }

    /**
     * set:修改时间
     */
    public void setOpTime(Long opTime) {
        this.opTime = opTime;
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
     * get:原始金额
     */
    public Integer getOriginFee() {
        return originFee;
    }

    /**
     * set:原始金额
     */
    public void setOriginFee(Integer originFee) {
        this.originFee = originFee;
    }

    /**
     * get:订单创建时间
     */
    public Long getOrderCtime() {
        return orderCtime;
    }

    /**
     * set:订单创建时间
     */
    public void setOrderCtime(Long orderCtime) {
        this.orderCtime = orderCtime;
    }

    /**
     * get:最低消费金额
     */
    public Integer getLeastAmount() {
        return leastAmount;
    }

    /**
     * set:最低消费金额
     */
    public void setLeastAmount(Integer leastAmount) {
        this.leastAmount = leastAmount;
    }

    /**
     * get:优惠json
     */
    public String getPromotions() {
        return promotions;
    }

    /**
     * set:优惠json
     */
    public void setPromotions(String promotions) {
        this.promotions = promotions == null ? null : promotions.trim();
    }

    /**
     * get:支付方式json
     */
    public String getFunds() {
        return funds;
    }

    /**
     * set:支付方式json
     */
    public void setFunds(String funds) {
        this.funds = funds == null ? null : funds.trim();
    }

    /**
     * get:新增菜json
     */
    public String getIncInstances() {
        return incInstances;
    }

    /**
     * set:新增菜json
     */
    public void setIncInstances(String incInstances) {
        this.incInstances = incInstances == null ? null : incInstances.trim();
    }

    /**
     * get:所有菜json
     */
    public String getAllInstances() {
        return allInstances;
    }

    /**
     * set:所有菜json
     */
    public void setAllInstances(String allInstances) {
        this.allInstances = allInstances == null ? null : allInstances.trim();
    }

    /**
     * get:扩展字段
     */
    public String getExt() {
        return ext;
    }

    /**
     * set:扩展字段
     */
    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }

    /**
     * get:第三方优惠
     */
    public String getThirdPromotions() {
        return thirdPromotions;
    }

    /**
     * set:第三方优惠
     */
    public void setThirdPromotions(String thirdPromotions) {
        this.thirdPromotions = thirdPromotions == null ? null : thirdPromotions.trim();
    }

    /**
     * get:第三方支付
     */
    public String getThirdFunds() {
        return thirdFunds;
    }

    /**
     * set:第三方支付
     */
    public void setThirdFunds(String thirdFunds) {
        this.thirdFunds = thirdFunds == null ? null : thirdFunds.trim();
    }
}
