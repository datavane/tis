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
public class OrderRefund extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:主键ID
     */
    private String id;

    private String entityId;

    /**
     * prop:订单的ID
     */
    private String orderId;

    /**
     * prop:退款来源（1用户发起，2，云收银发起，3本地收银发起，4超时系统自动发起）
     */
    private Byte refundFrom;

    /**
     * prop:退款的原因
     */
    private String reason;

    /**
     * prop:操作人ID用户操作则是customerId
     */
    private String opUserId;

    /**
     * prop:处理状态（ 1 处理中 ，2,失败,3, 完成 ,4 异常 , 5 撤销）， 2失败表示当前退单失败 ，3 表示退单成功
     */
    private Byte status;

    private Long createTime;

    private Long opTime;

    private Integer lastVer;

    private Byte isValid;

    private String ext;

    /**
     * prop:完成时间， 失败 成功 ，状态未知， 都是完成
     */
    private Long finishTime;

    /**
     * prop:退款流转子状态（开放给业务使用）
     */
    private Byte subStatus;

    /**
     * prop:退款单号码
     */
    private String refundCode;

    /**
     * prop:最大可退金额
     */
    private Integer maxRefundFee;

    /**
     * prop:申请处理退款金额
     */
    private Integer applyRefundFee;

    /**
     * prop:退款原因类型，业务方确定
     */
    private Byte reasonType;

    /**
     * prop:退款的发起原因
     */
    private String applyDesc;

    /**
     * prop:拒绝原因
     */
    private String rejectDesc;

    /**
     * prop:图片证据json 格式的list
     */
    private String picEvidence;

    /**
     * prop:发起人用户ID,用户发起就是customerId 商家发起就是商家员工ID
     */
    private String applyUserId;

    /**
     * prop:定时任务信息
     */
    private String timedTaskJson;

    /**
     * prop:是否需要审核退款
     */
    private Byte needAudit;

    /**
     * prop:退款场景-详见RefundSceneType
     */
    private Short refundScene;

    /**
     * prop:订单来源：0/商户自己录入;其他同WaitingOrder.OrderFrom
     */
    private Short orderFrom;

    /**
     * prop:1,正常开单;2预订开单;3.排队开单;4.外卖开单
     */
    private Short orderKind;

    /**
     * prop:违约金
     */
    private Integer liquidatedDamagesFee;

    /**
     * prop:waitingOrder的id
     */
    private String waitingorderId;

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
     * get:订单的ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * set:订单的ID
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * get:退款来源（1用户发起，2，云收银发起，3本地收银发起，4超时系统自动发起）
     */
    public Byte getRefundFrom() {
        return refundFrom;
    }

    /**
     * set:退款来源（1用户发起，2，云收银发起，3本地收银发起，4超时系统自动发起）
     */
    public void setRefundFrom(Byte refundFrom) {
        this.refundFrom = refundFrom;
    }

    /**
     * get:退款的原因
     */
    public String getReason() {
        return reason;
    }

    /**
     * set:退款的原因
     */
    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    /**
     * get:操作人ID用户操作则是customerId
     */
    public String getOpUserId() {
        return opUserId;
    }

    /**
     * set:操作人ID用户操作则是customerId
     */
    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId == null ? null : opUserId.trim();
    }

    /**
     * get:处理状态（ 1 处理中 ，2,失败,3, 完成 ,4 异常 , 5 撤销）， 2失败表示当前退单失败 ，3 表示退单成功
     */
    public Byte getStatus() {
        return status;
    }

    /**
     * set:处理状态（ 1 处理中 ，2,失败,3, 完成 ,4 异常 , 5 撤销）， 2失败表示当前退单失败 ，3 表示退单成功
     */
    public void setStatus(Byte status) {
        this.status = status;
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
     * get:退款流转子状态（开放给业务使用）
     */
    public Byte getSubStatus() {
        return subStatus;
    }

    /**
     * set:退款流转子状态（开放给业务使用）
     */
    public void setSubStatus(Byte subStatus) {
        this.subStatus = subStatus;
    }

    /**
     * get:退款单号码
     */
    public String getRefundCode() {
        return refundCode;
    }

    /**
     * set:退款单号码
     */
    public void setRefundCode(String refundCode) {
        this.refundCode = refundCode == null ? null : refundCode.trim();
    }

    /**
     * get:最大可退金额
     */
    public Integer getMaxRefundFee() {
        return maxRefundFee;
    }

    /**
     * set:最大可退金额
     */
    public void setMaxRefundFee(Integer maxRefundFee) {
        this.maxRefundFee = maxRefundFee;
    }

    /**
     * get:申请处理退款金额
     */
    public Integer getApplyRefundFee() {
        return applyRefundFee;
    }

    /**
     * set:申请处理退款金额
     */
    public void setApplyRefundFee(Integer applyRefundFee) {
        this.applyRefundFee = applyRefundFee;
    }

    /**
     * get:退款原因类型，业务方确定
     */
    public Byte getReasonType() {
        return reasonType;
    }

    /**
     * set:退款原因类型，业务方确定
     */
    public void setReasonType(Byte reasonType) {
        this.reasonType = reasonType;
    }

    /**
     * get:退款的发起原因
     */
    public String getApplyDesc() {
        return applyDesc;
    }

    /**
     * set:退款的发起原因
     */
    public void setApplyDesc(String applyDesc) {
        this.applyDesc = applyDesc == null ? null : applyDesc.trim();
    }

    /**
     * get:拒绝原因
     */
    public String getRejectDesc() {
        return rejectDesc;
    }

    /**
     * set:拒绝原因
     */
    public void setRejectDesc(String rejectDesc) {
        this.rejectDesc = rejectDesc == null ? null : rejectDesc.trim();
    }

    /**
     * get:图片证据json 格式的list
     */
    public String getPicEvidence() {
        return picEvidence;
    }

    /**
     * set:图片证据json 格式的list
     */
    public void setPicEvidence(String picEvidence) {
        this.picEvidence = picEvidence == null ? null : picEvidence.trim();
    }

    /**
     * get:发起人用户ID,用户发起就是customerId 商家发起就是商家员工ID
     */
    public String getApplyUserId() {
        return applyUserId;
    }

    /**
     * set:发起人用户ID,用户发起就是customerId 商家发起就是商家员工ID
     */
    public void setApplyUserId(String applyUserId) {
        this.applyUserId = applyUserId == null ? null : applyUserId.trim();
    }

    /**
     * get:定时任务信息
     */
    public String getTimedTaskJson() {
        return timedTaskJson;
    }

    /**
     * set:定时任务信息
     */
    public void setTimedTaskJson(String timedTaskJson) {
        this.timedTaskJson = timedTaskJson == null ? null : timedTaskJson.trim();
    }

    /**
     * get:是否需要审核退款
     */
    public Byte getNeedAudit() {
        return needAudit;
    }

    /**
     * set:是否需要审核退款
     */
    public void setNeedAudit(Byte needAudit) {
        this.needAudit = needAudit;
    }

    /**
     * get:退款场景-详见RefundSceneType
     */
    public Short getRefundScene() {
        return refundScene;
    }

    /**
     * set:退款场景-详见RefundSceneType
     */
    public void setRefundScene(Short refundScene) {
        this.refundScene = refundScene;
    }

    /**
     * get:订单来源：0/商户自己录入;其他同WaitingOrder.OrderFrom
     */
    public Short getOrderFrom() {
        return orderFrom;
    }

    /**
     * set:订单来源：0/商户自己录入;其他同WaitingOrder.OrderFrom
     */
    public void setOrderFrom(Short orderFrom) {
        this.orderFrom = orderFrom;
    }

    /**
     * get:1,正常开单;2预订开单;3.排队开单;4.外卖开单
     */
    public Short getOrderKind() {
        return orderKind;
    }

    /**
     * set:1,正常开单;2预订开单;3.排队开单;4.外卖开单
     */
    public void setOrderKind(Short orderKind) {
        this.orderKind = orderKind;
    }

    /**
     * get:违约金
     */
    public Integer getLiquidatedDamagesFee() {
        return liquidatedDamagesFee;
    }

    /**
     * set:违约金
     */
    public void setLiquidatedDamagesFee(Integer liquidatedDamagesFee) {
        this.liquidatedDamagesFee = liquidatedDamagesFee;
    }

    /**
     * get:waitingOrder的id
     */
    public String getWaitingorderId() {
        return waitingorderId;
    }

    /**
     * set:waitingOrder的id
     */
    public void setWaitingorderId(String waitingorderId) {
        this.waitingorderId = waitingorderId == null ? null : waitingorderId.trim();
    }
}
