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
public class OrderPromotion extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:主键id
     */
    private String id;

    /**
     * prop:订单id
     */
    private String orderId;

    /**
     * prop:优惠id
     */
    private String promotionId;

    /**
     * prop:优惠展示名称
     */
    private String promotionShowName;

    /**
     * prop:优惠名称
     */
    private String promotionName;

    /**
     * prop:优惠类型
     */
    private Integer promotionType;

    /**
     * prop:优惠子类型
     */
    private Byte promotionSubType;

    /**
     * prop:订单优惠金额
     */
    private BigDecimal promotionFee;

    /**
     * prop:折扣率
     */
    private BigDecimal promotionRatio;

    /**
     * prop:店铺ID
     */
    private String entityId;

    /**
     * prop:优惠承担方
     */
    private Integer promotionSource;

    /**
     * prop:扩展字段
     */
    private String ext;

    /**
     * prop:版本号
     */
    private Integer lastVer;

    /**
     * prop:是否有效
     */
    private Integer isValid;

    /**
     * prop:创建时间
     */
    private Long createTime;

    /**
     * prop:更新时间
     */
    private Long opTime;

    /**
     * prop:服务器时间
     */
    private Integer loadTime;

    /**
     * prop:修改时间
     */
    private Integer modifyTime;

    /**
     * prop:操作人id
     */
    private String opUserId;

    private static final long serialVersionUID = 1L;

    /**
     * get:主键id
     */
    public String getId() {
        return id;
    }

    /**
     * set:主键id
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
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
    public String getPromotionId() {
        return promotionId;
    }

    /**
     * set:优惠id
     */
    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId == null ? null : promotionId.trim();
    }

    /**
     * get:优惠展示名称
     */
    public String getPromotionShowName() {
        return promotionShowName;
    }

    /**
     * set:优惠展示名称
     */
    public void setPromotionShowName(String promotionShowName) {
        this.promotionShowName = promotionShowName == null ? null : promotionShowName.trim();
    }

    /**
     * get:优惠名称
     */
    public String getPromotionName() {
        return promotionName;
    }

    /**
     * set:优惠名称
     */
    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName == null ? null : promotionName.trim();
    }

    /**
     * get:优惠类型
     */
    public Integer getPromotionType() {
        return promotionType;
    }

    /**
     * set:优惠类型
     */
    public void setPromotionType(Integer promotionType) {
        this.promotionType = promotionType;
    }

    /**
     * get:优惠子类型
     */
    public Byte getPromotionSubType() {
        return promotionSubType;
    }

    /**
     * set:优惠子类型
     */
    public void setPromotionSubType(Byte promotionSubType) {
        this.promotionSubType = promotionSubType;
    }

    /**
     * get:订单优惠金额
     */
    public BigDecimal getPromotionFee() {
        return promotionFee;
    }

    /**
     * set:订单优惠金额
     */
    public void setPromotionFee(BigDecimal promotionFee) {
        this.promotionFee = promotionFee;
    }

    /**
     * get:折扣率
     */
    public BigDecimal getPromotionRatio() {
        return promotionRatio;
    }

    /**
     * set:折扣率
     */
    public void setPromotionRatio(BigDecimal promotionRatio) {
        this.promotionRatio = promotionRatio;
    }

    /**
     * get:店铺ID
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:店铺ID
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:优惠承担方
     */
    public Integer getPromotionSource() {
        return promotionSource;
    }

    /**
     * set:优惠承担方
     */
    public void setPromotionSource(Integer promotionSource) {
        this.promotionSource = promotionSource;
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
    public Integer getIsValid() {
        return isValid;
    }

    /**
     * set:是否有效
     */
    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
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
     * get:更新时间
     */
    public Long getOpTime() {
        return opTime;
    }

    /**
     * set:更新时间
     */
    public void setOpTime(Long opTime) {
        this.opTime = opTime;
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
     * get:操作人id
     */
    public String getOpUserId() {
        return opUserId;
    }

    /**
     * set:操作人id
     */
    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId == null ? null : opUserId.trim();
    }
}
