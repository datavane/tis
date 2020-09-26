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
public class OrderBill extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:等于totalpay_id
     */
    private String id;

    /**
     * prop:订单ID
     */
    private String orderId;

    /**
     * prop:账单状态
     */
    private Integer status;

    /**
     * prop:已支付金额
     */
    private BigDecimal paidFee;

    private BigDecimal outfee;

    private BigDecimal originAmount;

    private BigDecimal originServiceCharge;

    private BigDecimal originLeastAmount;

    private BigDecimal agioAmount;

    private BigDecimal agioServiceCharge;

    private BigDecimal agioLeastAmount;

    private BigDecimal originReceivablesAmount;

    private BigDecimal agioReceivablesAmount;

    private String entityId;

    private Short isValid;

    private BigDecimal agioTotal;

    private Long lastVer;

    private String opUserId;

    private BigDecimal reserveAmount;

    private BigDecimal originTotal;

    private BigDecimal finalAmount;

    /**
     * prop:是否是有收银优惠，0否，1是
     */
    private Byte useCashPromotion;

    private Long createTime;

    /**
     * prop:记录修改的服务器时间
     */
    private Integer modifyTime;

    private Long opTime;

    /**
     * prop:记录执行的服务器时间
     */
    private Integer loadTime;

    /**
     * prop:折后消费金额（优先扣减优惠）
     */
    private BigDecimal agioTotalReceivables;

    /**
     * prop:折后应收金额（优先扣减优惠）
     */
    private BigDecimal agioReceivablesAmountReceivables;

    /**
     * prop:最终金额（优先扣减优惠）
     */
    private BigDecimal finalAmountReceivables;

    /**
     * prop:扩展字段，格式为json
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    /**
     * get:等于totalpay_id
     */
    public String getId() {
        return id;
    }

    /**
     * set:等于totalpay_id
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
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
     * get:账单状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * set:账单状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * get:已支付金额
     */
    public BigDecimal getPaidFee() {
        return paidFee;
    }

    /**
     * set:已支付金额
     */
    public void setPaidFee(BigDecimal paidFee) {
        this.paidFee = paidFee;
    }

    public BigDecimal getOutfee() {
        return outfee;
    }

    public void setOutfee(BigDecimal outfee) {
        this.outfee = outfee;
    }

    public BigDecimal getOriginAmount() {
        return originAmount;
    }

    public void setOriginAmount(BigDecimal originAmount) {
        this.originAmount = originAmount;
    }

    public BigDecimal getOriginServiceCharge() {
        return originServiceCharge;
    }

    public void setOriginServiceCharge(BigDecimal originServiceCharge) {
        this.originServiceCharge = originServiceCharge;
    }

    public BigDecimal getOriginLeastAmount() {
        return originLeastAmount;
    }

    public void setOriginLeastAmount(BigDecimal originLeastAmount) {
        this.originLeastAmount = originLeastAmount;
    }

    public BigDecimal getAgioAmount() {
        return agioAmount;
    }

    public void setAgioAmount(BigDecimal agioAmount) {
        this.agioAmount = agioAmount;
    }

    public BigDecimal getAgioServiceCharge() {
        return agioServiceCharge;
    }

    public void setAgioServiceCharge(BigDecimal agioServiceCharge) {
        this.agioServiceCharge = agioServiceCharge;
    }

    public BigDecimal getAgioLeastAmount() {
        return agioLeastAmount;
    }

    public void setAgioLeastAmount(BigDecimal agioLeastAmount) {
        this.agioLeastAmount = agioLeastAmount;
    }

    public BigDecimal getOriginReceivablesAmount() {
        return originReceivablesAmount;
    }

    public void setOriginReceivablesAmount(BigDecimal originReceivablesAmount) {
        this.originReceivablesAmount = originReceivablesAmount;
    }

    public BigDecimal getAgioReceivablesAmount() {
        return agioReceivablesAmount;
    }

    public void setAgioReceivablesAmount(BigDecimal agioReceivablesAmount) {
        this.agioReceivablesAmount = agioReceivablesAmount;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    public Short getIsValid() {
        return isValid;
    }

    public void setIsValid(Short isValid) {
        this.isValid = isValid;
    }

    public BigDecimal getAgioTotal() {
        return agioTotal;
    }

    public void setAgioTotal(BigDecimal agioTotal) {
        this.agioTotal = agioTotal;
    }

    public Long getLastVer() {
        return lastVer;
    }

    public void setLastVer(Long lastVer) {
        this.lastVer = lastVer;
    }

    public String getOpUserId() {
        return opUserId;
    }

    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId == null ? null : opUserId.trim();
    }

    public BigDecimal getReserveAmount() {
        return reserveAmount;
    }

    public void setReserveAmount(BigDecimal reserveAmount) {
        this.reserveAmount = reserveAmount;
    }

    public BigDecimal getOriginTotal() {
        return originTotal;
    }

    public void setOriginTotal(BigDecimal originTotal) {
        this.originTotal = originTotal;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    /**
     * get:是否是有收银优惠，0否，1是
     */
    public Byte getUseCashPromotion() {
        return useCashPromotion;
    }

    /**
     * set:是否是有收银优惠，0否，1是
     */
    public void setUseCashPromotion(Byte useCashPromotion) {
        this.useCashPromotion = useCashPromotion;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
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

    public Long getOpTime() {
        return opTime;
    }

    public void setOpTime(Long opTime) {
        this.opTime = opTime;
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
     * get:折后消费金额（优先扣减优惠）
     */
    public BigDecimal getAgioTotalReceivables() {
        return agioTotalReceivables;
    }

    /**
     * set:折后消费金额（优先扣减优惠）
     */
    public void setAgioTotalReceivables(BigDecimal agioTotalReceivables) {
        this.agioTotalReceivables = agioTotalReceivables;
    }

    /**
     * get:折后应收金额（优先扣减优惠）
     */
    public BigDecimal getAgioReceivablesAmountReceivables() {
        return agioReceivablesAmountReceivables;
    }

    /**
     * set:折后应收金额（优先扣减优惠）
     */
    public void setAgioReceivablesAmountReceivables(BigDecimal agioReceivablesAmountReceivables) {
        this.agioReceivablesAmountReceivables = agioReceivablesAmountReceivables;
    }

    /**
     * get:最终金额（优先扣减优惠）
     */
    public BigDecimal getFinalAmountReceivables() {
        return finalAmountReceivables;
    }

    /**
     * set:最终金额（优先扣减优惠）
     */
    public void setFinalAmountReceivables(BigDecimal finalAmountReceivables) {
        this.finalAmountReceivables = finalAmountReceivables;
    }

    /**
     * get:扩展字段，格式为json
     */
    public String getExt() {
        return ext;
    }

    /**
     * set:扩展字段，格式为json
     */
    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }
}
