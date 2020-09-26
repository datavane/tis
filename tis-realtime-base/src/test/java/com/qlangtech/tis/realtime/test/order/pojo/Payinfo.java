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
public class Payinfo extends AbstractRowValueGetter implements Serializable {

    private String payId;

    private String totalpayId;

    private String kindpayId;

    private String kindpayname;

    private BigDecimal fee;

    private String operator;

    private String operatorName;

    private Long payTime;

    private BigDecimal pay;

    private BigDecimal charge;

    private Short isValid;

    private String entityId;

    private Long createTime;

    private Long opTime;

    private Long lastVer;

    private String opuserId;

    private String cardId;

    private String cardEntityId;

    private String onlineBillId;

    /**
     * prop:支付类型：同OnlineBill.Type
     */
    private Short type;

    private String code;

    private String waitingpayId;

    /**
     * prop:记录执行的服务器时间
     */
    private Integer loadTime;

    /**
     * prop:记录修改的服务器时间
     */
    private Integer modifyTime;

    private Byte isDealed;

    private String typeName;

    /**
     * prop:代金券面额
     */
    private BigDecimal couponFee;

    /**
     * prop:代金券实际购买金额
     */
    private BigDecimal couponCost;

    /**
     * prop:代金券使用张数
     */
    private Short couponNum;

    /**
     * prop:支付来源:同waitingPay.payFrom 表示来源 TradeBillType
     */
    private Short payFrom;

    /**
     * prop:父支付店铺实体id
     */
    private String parentEntityId;

    /**
     * prop:父支付id
     */
    private String parentId;

    /**
     * prop:父支付支付流水号
     */
    private String parentCode;

    /**
     * prop:卡类型ID
     */
    private String kindCardId;

    /**
     * prop:扩展字段
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId == null ? null : payId.trim();
    }

    public String getTotalpayId() {
        return totalpayId;
    }

    public void setTotalpayId(String totalpayId) {
        this.totalpayId = totalpayId == null ? null : totalpayId.trim();
    }

    public String getKindpayId() {
        return kindpayId;
    }

    public void setKindpayId(String kindpayId) {
        this.kindpayId = kindpayId == null ? null : kindpayId.trim();
    }

    public String getKindpayname() {
        return kindpayname;
    }

    public void setKindpayname(String kindpayname) {
        this.kindpayname = kindpayname == null ? null : kindpayname.trim();
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator == null ? null : operator.trim();
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName == null ? null : operatorName.trim();
    }

    public Long getPayTime() {
        return payTime;
    }

    public void setPayTime(Long payTime) {
        this.payTime = payTime;
    }

    public BigDecimal getPay() {
        return pay;
    }

    public void setPay(BigDecimal pay) {
        this.pay = pay;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    public Short getIsValid() {
        return isValid;
    }

    public void setIsValid(Short isValid) {
        this.isValid = isValid;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
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

    public Long getLastVer() {
        return lastVer;
    }

    public void setLastVer(Long lastVer) {
        this.lastVer = lastVer;
    }

    public String getOpuserId() {
        return opuserId;
    }

    public void setOpuserId(String opuserId) {
        this.opuserId = opuserId == null ? null : opuserId.trim();
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId == null ? null : cardId.trim();
    }

    public String getCardEntityId() {
        return cardEntityId;
    }

    public void setCardEntityId(String cardEntityId) {
        this.cardEntityId = cardEntityId == null ? null : cardEntityId.trim();
    }

    public String getOnlineBillId() {
        return onlineBillId;
    }

    public void setOnlineBillId(String onlineBillId) {
        this.onlineBillId = onlineBillId == null ? null : onlineBillId.trim();
    }

    /**
     * get:支付类型：同OnlineBill.Type
     */
    public Short getType() {
        return type;
    }

    /**
     * set:支付类型：同OnlineBill.Type
     */
    public void setType(Short type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public String getWaitingpayId() {
        return waitingpayId;
    }

    public void setWaitingpayId(String waitingpayId) {
        this.waitingpayId = waitingpayId == null ? null : waitingpayId.trim();
    }

    /**
     * get:记录执行的服务器时间
     */
    public Integer getLoadTime() {
        return loadTime;
    }

    /**
     * set:记录执行的服务器时间
     */
    public void setLoadTime(Integer loadTime) {
        this.loadTime = loadTime;
    }

    /**
     * get:记录修改的服务器时间
     */
    public Integer getModifyTime() {
        return modifyTime;
    }

    /**
     * set:记录修改的服务器时间
     */
    public void setModifyTime(Integer modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Byte getIsDealed() {
        return isDealed;
    }

    public void setIsDealed(Byte isDealed) {
        this.isDealed = isDealed;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName == null ? null : typeName.trim();
    }

    /**
     * get:代金券面额
     */
    public BigDecimal getCouponFee() {
        return couponFee;
    }

    /**
     * set:代金券面额
     */
    public void setCouponFee(BigDecimal couponFee) {
        this.couponFee = couponFee;
    }

    /**
     * get:代金券实际购买金额
     */
    public BigDecimal getCouponCost() {
        return couponCost;
    }

    /**
     * set:代金券实际购买金额
     */
    public void setCouponCost(BigDecimal couponCost) {
        this.couponCost = couponCost;
    }

    /**
     * get:代金券使用张数
     */
    public Short getCouponNum() {
        return couponNum;
    }

    /**
     * set:代金券使用张数
     */
    public void setCouponNum(Short couponNum) {
        this.couponNum = couponNum;
    }

    /**
     * get:支付来源:同waitingPay.payFrom 表示来源 TradeBillType
     */
    public Short getPayFrom() {
        return payFrom;
    }

    /**
     * set:支付来源:同waitingPay.payFrom 表示来源 TradeBillType
     */
    public void setPayFrom(Short payFrom) {
        this.payFrom = payFrom;
    }

    /**
     * get:父支付店铺实体id
     */
    public String getParentEntityId() {
        return parentEntityId;
    }

    /**
     * set:父支付店铺实体id
     */
    public void setParentEntityId(String parentEntityId) {
        this.parentEntityId = parentEntityId == null ? null : parentEntityId.trim();
    }

    /**
     * get:父支付id
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * set:父支付id
     */
    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    /**
     * get:父支付支付流水号
     */
    public String getParentCode() {
        return parentCode;
    }

    /**
     * set:父支付支付流水号
     */
    public void setParentCode(String parentCode) {
        this.parentCode = parentCode == null ? null : parentCode.trim();
    }

    /**
     * get:卡类型ID
     */
    public String getKindCardId() {
        return kindCardId;
    }

    /**
     * set:卡类型ID
     */
    public void setKindCardId(String kindCardId) {
        this.kindCardId = kindCardId == null ? null : kindCardId.trim();
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
}
