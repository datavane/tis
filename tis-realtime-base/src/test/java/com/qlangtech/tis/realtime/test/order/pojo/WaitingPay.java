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
public class WaitingPay extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:id
     */
    private String id;

    /**
     * prop:店铺实体ID
     */
    private String entityId;

    /**
     * prop:支付类型约定：1/支付宝；2/快钱；3/会员卡；4/银联……
     */
    private Short type;

    /**
     * prop:订单号
     */
    private String code;

    /**
     * prop:扩展ID
     */
    private String extId;

    /**
     * prop:支付金额
     */
    private BigDecimal fee;

    /**
     * prop:单号
     */
    private String innerCode;

    /**
     * prop:状态：1/未处理;2/处理成功;3/处理失败
     */
    private Short status;

    /**
     * prop:订单ID
     */
    private String orderId;

    /**
     * prop:错误信息
     */
    private String errorMessage;

    /**
     * prop:是否有效
     */
    private Short isValid;

    /**
     * prop:创建时间
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
     * prop:用户ID
     */
    private String customerRegisterId;

    /**
     * prop:备注
     */
    private String memo;

    /**
     * prop:关联ID
     */
    private String relationId;

    /**
     * prop:支付状态
     */
    private Short payStatus;

    /**
     * prop:支付来源：定义同WaitingOrder.OrderFrom
     */
    private Short payFrom;

    /**
     * prop:会员卡ID
     */
    private String cardId;

    /**
     * prop:会员卡实体ID
     */
    private String cardEntityId;

    /**
     * prop:用户实际支付金额，通常情况fee==pay
     */
    private BigDecimal pay;

    /**
     * prop:扩展字段
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    /**
     * get:id
     */
    public String getId() {
        return id;
    }

    /**
     * set:id
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * get:店铺实体ID
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:店铺实体ID
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:支付类型约定：1/支付宝；2/快钱；3/会员卡；4/银联……
     */
    public Short getType() {
        return type;
    }

    /**
     * set:支付类型约定：1/支付宝；2/快钱；3/会员卡；4/银联……
     */
    public void setType(Short type) {
        this.type = type;
    }

    /**
     * get:订单号
     */
    public String getCode() {
        return code;
    }

    /**
     * set:订单号
     */
    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    /**
     * get:扩展ID
     */
    public String getExtId() {
        return extId;
    }

    /**
     * set:扩展ID
     */
    public void setExtId(String extId) {
        this.extId = extId == null ? null : extId.trim();
    }

    /**
     * get:支付金额
     */
    public BigDecimal getFee() {
        return fee;
    }

    /**
     * set:支付金额
     */
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    /**
     * get:单号
     */
    public String getInnerCode() {
        return innerCode;
    }

    /**
     * set:单号
     */
    public void setInnerCode(String innerCode) {
        this.innerCode = innerCode == null ? null : innerCode.trim();
    }

    /**
     * get:状态：1/未处理;2/处理成功;3/处理失败
     */
    public Short getStatus() {
        return status;
    }

    /**
     * set:状态：1/未处理;2/处理成功;3/处理失败
     */
    public void setStatus(Short status) {
        this.status = status;
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
     * get:错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * set:错误信息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? null : errorMessage.trim();
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
     * get:用户ID
     */
    public String getCustomerRegisterId() {
        return customerRegisterId;
    }

    /**
     * set:用户ID
     */
    public void setCustomerRegisterId(String customerRegisterId) {
        this.customerRegisterId = customerRegisterId == null ? null : customerRegisterId.trim();
    }

    /**
     * get:备注
     */
    public String getMemo() {
        return memo;
    }

    /**
     * set:备注
     */
    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    /**
     * get:关联ID
     */
    public String getRelationId() {
        return relationId;
    }

    /**
     * set:关联ID
     */
    public void setRelationId(String relationId) {
        this.relationId = relationId == null ? null : relationId.trim();
    }

    /**
     * get:支付状态
     */
    public Short getPayStatus() {
        return payStatus;
    }

    /**
     * set:支付状态
     */
    public void setPayStatus(Short payStatus) {
        this.payStatus = payStatus;
    }

    /**
     * get:支付来源：定义同WaitingOrder.OrderFrom
     */
    public Short getPayFrom() {
        return payFrom;
    }

    /**
     * set:支付来源：定义同WaitingOrder.OrderFrom
     */
    public void setPayFrom(Short payFrom) {
        this.payFrom = payFrom;
    }

    /**
     * get:会员卡ID
     */
    public String getCardId() {
        return cardId;
    }

    /**
     * set:会员卡ID
     */
    public void setCardId(String cardId) {
        this.cardId = cardId == null ? null : cardId.trim();
    }

    /**
     * get:会员卡实体ID
     */
    public String getCardEntityId() {
        return cardEntityId;
    }

    /**
     * set:会员卡实体ID
     */
    public void setCardEntityId(String cardEntityId) {
        this.cardEntityId = cardEntityId == null ? null : cardEntityId.trim();
    }

    /**
     * get:用户实际支付金额，通常情况fee==pay
     */
    public BigDecimal getPay() {
        return pay;
    }

    /**
     * set:用户实际支付金额，通常情况fee==pay
     */
    public void setPay(BigDecimal pay) {
        this.pay = pay;
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
