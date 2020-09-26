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
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Totalpayinfo extends AbstractRowValueGetter implements Serializable {

    private String totalpayId;

    private Date currDate;

    private BigDecimal outfee;

    private BigDecimal sourceAmount;

    private BigDecimal discountAmount;

    private BigDecimal resultAmount;

    private BigDecimal recieveAmount;

    private BigDecimal ratio;

    private Byte status;

    private String entityId;

    private Byte isValid;

    private Long createTime;

    private Long opTime;

    private Long lastVer;

    private String opUserId;

    private String discountPlanId;

    private String operator;

    private Long operateDate;

    private String cardId;

    private String card;

    private String cardEntityId;

    private Byte isFullRatio;

    private Byte isMinconsumeRatio;

    private Byte isServicefeeRatio;

    private String invoiceCode;

    private String invoiceMemo;

    /**
     * prop:0表示未开发票
     */
    private BigDecimal invoice;

    /**
     * prop:1未交班/2已交班
     */
    private Byte overStatus;

    /**
     * prop:查询条件，如设为隐藏，则无相关权限的操作员在后台无法查看隐藏账单
     */
    private Byte isHide;

    /**
     * prop:记录执行的服务器时间
     */
    private Integer loadTime;

    /**
     * prop:记录修改的服务器时间
     */
    private Integer modifyTime;

    private Integer printnum1;

    private Integer printnum2;

    /**
     * prop:代金券优惠总额
     */
    private BigDecimal couponDiscount;

    /**
     * prop:折后应收金额（优先扣减优惠）
     */
    private BigDecimal discountAmountReceivables;

    /**
     * prop:最终金额（优先扣减优惠）
     */
    private BigDecimal resultAmountReceivables;

    /**
     * prop:扩展字段
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    public String getTotalpayId() {
        return totalpayId;
    }

    public void setTotalpayId(String totalpayId) {
        this.totalpayId = totalpayId == null ? null : totalpayId.trim();
    }

    public Date getCurrDate() {
        return currDate;
    }

    public void setCurrDate(Date currDate) {
        this.currDate = currDate;
    }

    public BigDecimal getOutfee() {
        return outfee;
    }

    public void setOutfee(BigDecimal outfee) {
        this.outfee = outfee;
    }

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public void setSourceAmount(BigDecimal sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getResultAmount() {
        return resultAmount;
    }

    public void setResultAmount(BigDecimal resultAmount) {
        this.resultAmount = resultAmount;
    }

    public BigDecimal getRecieveAmount() {
        return recieveAmount;
    }

    public void setRecieveAmount(BigDecimal recieveAmount) {
        this.recieveAmount = recieveAmount;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    public Byte getIsValid() {
        return isValid;
    }

    public void setIsValid(Byte isValid) {
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

    public String getDiscountPlanId() {
        return discountPlanId;
    }

    public void setDiscountPlanId(String discountPlanId) {
        this.discountPlanId = discountPlanId == null ? null : discountPlanId.trim();
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator == null ? null : operator.trim();
    }

    public Long getOperateDate() {
        return operateDate;
    }

    public void setOperateDate(Long operateDate) {
        this.operateDate = operateDate;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId == null ? null : cardId.trim();
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card == null ? null : card.trim();
    }

    public String getCardEntityId() {
        return cardEntityId;
    }

    public void setCardEntityId(String cardEntityId) {
        this.cardEntityId = cardEntityId == null ? null : cardEntityId.trim();
    }

    public Byte getIsFullRatio() {
        return isFullRatio;
    }

    public void setIsFullRatio(Byte isFullRatio) {
        this.isFullRatio = isFullRatio;
    }

    public Byte getIsMinconsumeRatio() {
        return isMinconsumeRatio;
    }

    public void setIsMinconsumeRatio(Byte isMinconsumeRatio) {
        this.isMinconsumeRatio = isMinconsumeRatio;
    }

    public Byte getIsServicefeeRatio() {
        return isServicefeeRatio;
    }

    public void setIsServicefeeRatio(Byte isServicefeeRatio) {
        this.isServicefeeRatio = isServicefeeRatio;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode == null ? null : invoiceCode.trim();
    }

    public String getInvoiceMemo() {
        return invoiceMemo;
    }

    public void setInvoiceMemo(String invoiceMemo) {
        this.invoiceMemo = invoiceMemo == null ? null : invoiceMemo.trim();
    }

    /**
     * get:0表示未开发票
     */
    public BigDecimal getInvoice() {
        return invoice;
    }

    /**
     * set:0表示未开发票
     */
    public void setInvoice(BigDecimal invoice) {
        this.invoice = invoice;
    }

    /**
     * get:1未交班/2已交班
     */
    public Byte getOverStatus() {
        return overStatus;
    }

    /**
     * set:1未交班/2已交班
     */
    public void setOverStatus(Byte overStatus) {
        this.overStatus = overStatus;
    }

    /**
     * get:查询条件，如设为隐藏，则无相关权限的操作员在后台无法查看隐藏账单
     */
    public Byte getIsHide() {
        return isHide;
    }

    /**
     * set:查询条件，如设为隐藏，则无相关权限的操作员在后台无法查看隐藏账单
     */
    public void setIsHide(Byte isHide) {
        this.isHide = isHide;
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

    public Integer getPrintnum1() {
        return printnum1;
    }

    public void setPrintnum1(Integer printnum1) {
        this.printnum1 = printnum1;
    }

    public Integer getPrintnum2() {
        return printnum2;
    }

    public void setPrintnum2(Integer printnum2) {
        this.printnum2 = printnum2;
    }

    /**
     * get:代金券优惠总额
     */
    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    /**
     * set:代金券优惠总额
     */
    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    /**
     * get:折后应收金额（优先扣减优惠）
     */
    public BigDecimal getDiscountAmountReceivables() {
        return discountAmountReceivables;
    }

    /**
     * set:折后应收金额（优先扣减优惠）
     */
    public void setDiscountAmountReceivables(BigDecimal discountAmountReceivables) {
        this.discountAmountReceivables = discountAmountReceivables;
    }

    /**
     * get:最终金额（优先扣减优惠）
     */
    public BigDecimal getResultAmountReceivables() {
        return resultAmountReceivables;
    }

    /**
     * set:最终金额（优先扣减优惠）
     */
    public void setResultAmountReceivables(BigDecimal resultAmountReceivables) {
        this.resultAmountReceivables = resultAmountReceivables;
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
