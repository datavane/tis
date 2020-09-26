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
public class RefundPayItem extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:主键ID
     */
    private String id;

    private String entityId;

    /**
     * prop:退款单ID
     */
    private String orderRefundId;

    private String orderId;

    /**
     * prop:电子支付都有waitingPay
     */
    private String waitingPayId;

    /**
     * prop:订单中生成的pay信息
     */
    private String payId;

    /**
     * prop:退款状态（1，退款中 ，2 退款状态未知 ，3退款失败 ，4 退款成功 ）
     */
    private Byte status;

    /**
     * prop:完成时间， 失败 成功 ，状态未知， 都是完成
     */
    private Long finishTime;

    /**
     * prop:失败时候，会有失败原因
     */
    private String msg;

    /**
     * prop:应该退款金额
     */
    private Integer shouldFee;

    /**
     * prop:实际退款金额
     */
    private Integer actualFee;

    /**
     * prop:扣款费率
     */
    private Double deductRatio;

    private Long createTime;

    private Long opTime;

    private Integer lastVer;

    private Byte isValid;

    private String ext;

    /**
     * prop:来源类型, 1 线上退款 ，2 线下退款，
     */
    private Byte fromType;

    private Integer type;

    /**
     * prop:退款方式 1， 原路退款 ，2 企业付款
     */
    private Byte refundWay;

    private String relaWaitingPayId;

    /**
     * prop:支付方式配置ID
     */
    private String kindpayId;

    private static final long serialVersionUID = 1L;

    /**
     * get:主键ID
     */
    public String getId() {
        return id;
    }

    /**
     * set:主键ID
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:退款单ID
     */
    public String getOrderRefundId() {
        return orderRefundId;
    }

    /**
     * set:退款单ID
     */
    public void setOrderRefundId(String orderRefundId) {
        this.orderRefundId = orderRefundId == null ? null : orderRefundId.trim();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * get:电子支付都有waitingPay
     */
    public String getWaitingPayId() {
        return waitingPayId;
    }

    /**
     * set:电子支付都有waitingPay
     */
    public void setWaitingPayId(String waitingPayId) {
        this.waitingPayId = waitingPayId == null ? null : waitingPayId.trim();
    }

    /**
     * get:订单中生成的pay信息
     */
    public String getPayId() {
        return payId;
    }

    /**
     * set:订单中生成的pay信息
     */
    public void setPayId(String payId) {
        this.payId = payId == null ? null : payId.trim();
    }

    /**
     * get:退款状态（1，退款中 ，2 退款状态未知 ，3退款失败 ，4 退款成功 ）
     */
    public Byte getStatus() {
        return status;
    }

    /**
     * set:退款状态（1，退款中 ，2 退款状态未知 ，3退款失败 ，4 退款成功 ）
     */
    public void setStatus(Byte status) {
        this.status = status;
    }

    /**
     * get:完成时间， 失败 成功 ，状态未知， 都是完成
     */
    public Long getFinishTime() {
        return finishTime;
    }

    /**
     * set:完成时间， 失败 成功 ，状态未知， 都是完成
     */
    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * get:失败时候，会有失败原因
     */
    public String getMsg() {
        return msg;
    }

    /**
     * set:失败时候，会有失败原因
     */
    public void setMsg(String msg) {
        this.msg = msg == null ? null : msg.trim();
    }

    /**
     * get:应该退款金额
     */
    public Integer getShouldFee() {
        return shouldFee;
    }

    /**
     * set:应该退款金额
     */
    public void setShouldFee(Integer shouldFee) {
        this.shouldFee = shouldFee;
    }

    /**
     * get:实际退款金额
     */
    public Integer getActualFee() {
        return actualFee;
    }

    /**
     * set:实际退款金额
     */
    public void setActualFee(Integer actualFee) {
        this.actualFee = actualFee;
    }

    /**
     * get:扣款费率
     */
    public Double getDeductRatio() {
        return deductRatio;
    }

    /**
     * set:扣款费率
     */
    public void setDeductRatio(Double deductRatio) {
        this.deductRatio = deductRatio;
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

    public Integer getLastVer() {
        return lastVer;
    }

    public void setLastVer(Integer lastVer) {
        this.lastVer = lastVer;
    }

    public Byte getIsValid() {
        return isValid;
    }

    public void setIsValid(Byte isValid) {
        this.isValid = isValid;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }

    /**
     * get:来源类型, 1 线上退款 ，2 线下退款，
     */
    public Byte getFromType() {
        return fromType;
    }

    /**
     * set:来源类型, 1 线上退款 ，2 线下退款，
     */
    public void setFromType(Byte fromType) {
        this.fromType = fromType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * get:退款方式 1， 原路退款 ，2 企业付款
     */
    public Byte getRefundWay() {
        return refundWay;
    }

    /**
     * set:退款方式 1， 原路退款 ，2 企业付款
     */
    public void setRefundWay(Byte refundWay) {
        this.refundWay = refundWay;
    }

    public String getRelaWaitingPayId() {
        return relaWaitingPayId;
    }

    public void setRelaWaitingPayId(String relaWaitingPayId) {
        this.relaWaitingPayId = relaWaitingPayId == null ? null : relaWaitingPayId.trim();
    }

    /**
     * get:支付方式配置ID
     */
    public String getKindpayId() {
        return kindpayId;
    }

    /**
     * set:支付方式配置ID
     */
    public void setKindpayId(String kindpayId) {
        this.kindpayId = kindpayId == null ? null : kindpayId.trim();
    }
}
