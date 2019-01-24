/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.solrj.extend.pojo;

import org.apache.solr.client.solrj.beans.Field;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Totalpay {

    @Field("totalpay_id")
    private String totalpayId;

    @Field("curr_date")
    private Long currDate;

    @Field("outfee")
    private Float outfee;

    @Field("source_amount")
    private Float sourceAmount;

    @Field("discount_amount")
    private Float discountAmount;

    @Field("result_amount")
    private Float resultAmount;

    @Field("recieve_amount")
    private Float recieveAmount;

    @Field("ratio")
    private Float ratio;

    @Field("status")
    private Integer status;

    @Field("entity_id")
    private String entityId;

    @Field("is_valid")
    private Integer isValid;

    @Field("op_time")
    private Long opTime;

    @Field("last_ver")
    private Long lastVer;

    @Field("op_user_id")
    private String opUserId;

    @Field("discount_plan_id")
    private String discountPlanId;

    @Field("operator")
    private String operator;

    @Field("operate_date")
    private Long operateDate;

    @Field("card_id")
    private String cardId;

    @Field("card")
    private String card;

    @Field("card_entity_id")
    private String cardEntityId;

    @Field("is_full_ratio")
    private Integer isFullRatio;

    @Field("is_minconsume_ratio")
    private Integer isMinconsumeRatio;

    @Field("is_servicefee_ratio")
    private Integer isServicefeeRatio;

    @Field("invoice_code")
    private String invoiceCode;

    @Field("invoice_memo")
    private String invoiceMemo;

    @Field("invoice")
    private Float invoice;

    @Field("over_status")
    private Integer overStatus;

    @Field("is_hide")
    private Integer isHide;

    @Field("load_time")
    private Long loadTime;

    @Field("modify_time")
    private Long modifyTime;

    @Field("area_id")
    private String areaId;

    @Field("seat_id")
    private String seatId;

    @Field("is_valido")
    private Integer isValido;

    @Field("kindpay")
    private String kindpay;

    @Field("all_ratio_fee")
    private Float allRatioFee;

    @Field("all_menu")
    private String allMenu;

    @Field("_version_")
    private Long Version;

    public void setTotalpayId(String totalpayId) {
        this.totalpayId = totalpayId;
    }

    public String getTotalpayId() {
        return this.totalpayId;
    }

    public void setCurrDate(Long currDate) {
        this.currDate = currDate;
    }

    public Long getCurrDate() {
        return this.currDate;
    }

    public void setOutfee(Float outfee) {
        this.outfee = outfee;
    }

    public Float getOutfee() {
        return this.outfee;
    }

    public void setSourceAmount(Float sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public Float getSourceAmount() {
        return this.sourceAmount;
    }

    public void setDiscountAmount(Float discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Float getDiscountAmount() {
        return this.discountAmount;
    }

    public void setResultAmount(Float resultAmount) {
        this.resultAmount = resultAmount;
    }

    public Float getResultAmount() {
        return this.resultAmount;
    }

    public void setRecieveAmount(Float recieveAmount) {
        this.recieveAmount = recieveAmount;
    }

    public Float getRecieveAmount() {
        return this.recieveAmount;
    }

    public void setRatio(Float ratio) {
        this.ratio = ratio;
    }

    public Float getRatio() {
        return this.ratio;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public Integer getIsValid() {
        return this.isValid;
    }

    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }

    public Long getOpTime() {
        return this.opTime;
    }

    public void setLastVer(Long lastVer) {
        this.lastVer = lastVer;
    }

    public Long getLastVer() {
        return this.lastVer;
    }

    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId;
    }

    public String getOpUserId() {
        return this.opUserId;
    }

    public void setDiscountPlanId(String discountPlanId) {
        this.discountPlanId = discountPlanId;
    }

    public String getDiscountPlanId() {
        return this.discountPlanId;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperateDate(Long operateDate) {
        this.operateDate = operateDate;
    }

    public Long getOperateDate() {
        return this.operateDate;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getCard() {
        return this.card;
    }

    public void setCardEntityId(String cardEntityId) {
        this.cardEntityId = cardEntityId;
    }

    public String getCardEntityId() {
        return this.cardEntityId;
    }

    public void setIsFullRatio(Integer isFullRatio) {
        this.isFullRatio = isFullRatio;
    }

    public Integer getIsFullRatio() {
        return this.isFullRatio;
    }

    public void setIsMinconsumeRatio(Integer isMinconsumeRatio) {
        this.isMinconsumeRatio = isMinconsumeRatio;
    }

    public Integer getIsMinconsumeRatio() {
        return this.isMinconsumeRatio;
    }

    public void setIsServicefeeRatio(Integer isServicefeeRatio) {
        this.isServicefeeRatio = isServicefeeRatio;
    }

    public Integer getIsServicefeeRatio() {
        return this.isServicefeeRatio;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public String getInvoiceCode() {
        return this.invoiceCode;
    }

    public void setInvoiceMemo(String invoiceMemo) {
        this.invoiceMemo = invoiceMemo;
    }

    public String getInvoiceMemo() {
        return this.invoiceMemo;
    }

    public void setInvoice(Float invoice) {
        this.invoice = invoice;
    }

    public Float getInvoice() {
        return this.invoice;
    }

    public void setOverStatus(Integer overStatus) {
        this.overStatus = overStatus;
    }

    public Integer getOverStatus() {
        return this.overStatus;
    }

    public void setIsHide(Integer isHide) {
        this.isHide = isHide;
    }

    public Integer getIsHide() {
        return this.isHide;
    }

    public void setLoadTime(Long loadTime) {
        this.loadTime = loadTime;
    }

    public Long getLoadTime() {
        return this.loadTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Long getModifyTime() {
        return this.modifyTime;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getAreaId() {
        return this.areaId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getSeatId() {
        return this.seatId;
    }

    public void setIsValido(Integer isValido) {
        this.isValido = isValido;
    }

    public Integer getIsValido() {
        return this.isValido;
    }

    public void setKindpay(String kindpay) {
        this.kindpay = kindpay;
    }

    public String getKindpay() {
        return this.kindpay;
    }

    public void setAllRatioFee(Float allRatioFee) {
        this.allRatioFee = allRatioFee;
    }

    public Float getAllRatioFee() {
        return this.allRatioFee;
    }

    public void setAllMenu(String allMenu) {
        this.allMenu = allMenu;
    }

    public String getAllMenu() {
        return this.allMenu;
    }

    public void setVersion(Long Version) {
        this.Version = Version;
    }

    public Long getVersion() {
        return this.Version;
    }
}
