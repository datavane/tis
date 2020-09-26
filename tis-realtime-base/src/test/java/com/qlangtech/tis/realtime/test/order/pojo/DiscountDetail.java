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
public class DiscountDetail extends AbstractRowValueGetter implements Serializable {

    private String id;

    /**
     * prop:菜id
     */
    private String instanceId;

    /**
     * prop:订单id
     */
    private String orderId;

    /**
     * prop:优惠id
     */
    private String discountId;

    private String discountName;

    /**
     * prop:优惠类型
     */
    private Integer discountType;

    private Integer discountSubType;

    /**
     * prop:菜优惠金额
     */
    private BigDecimal discountFee;

    /**
     * prop:折扣率
     */
    private BigDecimal discountRatio;

    /**
     * prop:订单优惠金额
     */
    private BigDecimal orderDiscountFee;

    /**
     * prop:菜折后额
     */
    private BigDecimal ratioFee;

    /**
     * prop:菜原始价格
     */
    private BigDecimal originFee;

    private String entityId;

    private Integer lastVer;

    private Integer isValid;

    private Long createTime;

    private Long opTime;

    private String opUserId;

    /**
     * prop:活动id
     */
    private String activityId;

    /**
     * prop:服务器时间
     */
    private Integer loadTime;

    /**
     * prop:修改时间
     */
    private Integer modifyTime;

    /**
     * prop:订单优惠id
     */
    private String orderPromotionId;

    private String ext;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * get:菜id
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * set:菜id
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId == null ? null : instanceId.trim();
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
     * get:优惠id
     */
    public String getDiscountId() {
        return discountId;
    }

    /**
     * set:优惠id
     */
    public void setDiscountId(String discountId) {
        this.discountId = discountId == null ? null : discountId.trim();
    }

    public String getDiscountName() {
        return discountName;
    }

    public void setDiscountName(String discountName) {
        this.discountName = discountName == null ? null : discountName.trim();
    }

    /**
     * get:优惠类型
     */
    public Integer getDiscountType() {
        return discountType;
    }

    /**
     * set:优惠类型
     */
    public void setDiscountType(Integer discountType) {
        this.discountType = discountType;
    }

    public Integer getDiscountSubType() {
        return discountSubType;
    }

    public void setDiscountSubType(Integer discountSubType) {
        this.discountSubType = discountSubType;
    }

    /**
     * get:菜优惠金额
     */
    public BigDecimal getDiscountFee() {
        return discountFee;
    }

    /**
     * set:菜优惠金额
     */
    public void setDiscountFee(BigDecimal discountFee) {
        this.discountFee = discountFee;
    }

    /**
     * get:折扣率
     */
    public BigDecimal getDiscountRatio() {
        return discountRatio;
    }

    /**
     * set:折扣率
     */
    public void setDiscountRatio(BigDecimal discountRatio) {
        this.discountRatio = discountRatio;
    }

    /**
     * get:订单优惠金额
     */
    public BigDecimal getOrderDiscountFee() {
        return orderDiscountFee;
    }

    /**
     * set:订单优惠金额
     */
    public void setOrderDiscountFee(BigDecimal orderDiscountFee) {
        this.orderDiscountFee = orderDiscountFee;
    }

    /**
     * get:菜折后额
     */
    public BigDecimal getRatioFee() {
        return ratioFee;
    }

    /**
     * set:菜折后额
     */
    public void setRatioFee(BigDecimal ratioFee) {
        this.ratioFee = ratioFee;
    }

    /**
     * get:菜原始价格
     */
    public BigDecimal getOriginFee() {
        return originFee;
    }

    /**
     * set:菜原始价格
     */
    public void setOriginFee(BigDecimal originFee) {
        this.originFee = originFee;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    public Integer getLastVer() {
        return lastVer;
    }

    public void setLastVer(Integer lastVer) {
        this.lastVer = lastVer;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
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

    public String getOpUserId() {
        return opUserId;
    }

    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId == null ? null : opUserId.trim();
    }

    /**
     * get:活动id
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * set:活动id
     */
    public void setActivityId(String activityId) {
        this.activityId = activityId == null ? null : activityId.trim();
    }

    /**
     * get:服务器时间
     */
    public Integer getLoadTime() {
        return loadTime;
    }

    /**
     * set:服务器时间
     */
    public void setLoadTime(Integer loadTime) {
        this.loadTime = loadTime;
    }

    /**
     * get:修改时间
     */
    public Integer getModifyTime() {
        return modifyTime;
    }

    /**
     * set:修改时间
     */
    public void setModifyTime(Integer modifyTime) {
        this.modifyTime = modifyTime;
    }

    /**
     * get:订单优惠id
     */
    public String getOrderPromotionId() {
        return orderPromotionId;
    }

    /**
     * set:订单优惠id
     */
    public void setOrderPromotionId(String orderPromotionId) {
        this.orderPromotionId = orderPromotionId == null ? null : orderPromotionId.trim();
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }
}
