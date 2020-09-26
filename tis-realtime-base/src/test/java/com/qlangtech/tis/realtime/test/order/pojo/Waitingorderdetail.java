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
public class Waitingorderdetail extends AbstractRowValueGetter implements Serializable {

    private String waitingorderId;

    /**
     * prop:订单来源：1/淘宝点点;2/卡包；3/服务生app；4/微信；……
     */
    private Short orderFrom;

    /**
     * prop:批次信息：时:分 点菜人
     */
    private String batchMsg;

    /**
     * prop:订单类型:1/订位;2/外卖;3/扫码加菜;4/扫桌码开单
     */
    private Short kind;

    private String code;

    private String seatCode;

    private String name;

    private Integer peopleCount;

    private String mobile;

    private String tel;

    /**
     * prop:预订日期时间：长整形表示的日期时间，如为-1表示立即送货（用于外送订单）
     */
    private Long reserveDate;

    private String memo;

    private BigDecimal totalPrice;

    private BigDecimal realPrice;

    private String shopname;

    /**
     * prop:送货地址:Kind=2时填写
     */
    private String address;

    /**
     * prop:预付/到付模式：1/预付;2/到付；外卖接受到付，订位只能预付
     */
    private Short payMode;

    /**
     * prop:预付费方式时，填写支付类型：
     *   1/会员卡余额支付；2/银联支付；3/支付宝支付。。。
     */
    private Short payType;

    /**
     * prop:预付费方式时，填写支付相关信息，如卡号、流水等
     */
    private String payMemo;

    private BigDecimal outfee;

    private String cardEntityId;

    private String cardId;

    private String payId;

    private BigDecimal advancePay;

    private BigDecimal advanceSeatPay;

    /**
     * prop:支付状态：
     *   0-不用支付定金
     *   1-未支付
     *   2-已支付
     */
    private Short payStatus;

    private String reserveSeatId;

    private String reserveTimeId;

    /**
     * prop:预订状态定义：
     *   2- 待审核
     *   3- 已取消
     *   4- 已生效
     *   5- 已到达
     *   外卖的状态定义：
     *   2- 待审核
     *   3- 已取消
     *   4- 已生效，未下单
     *   5- 已生效，已下单
     *   6- 已生效，已送货
     *   7- 已结单
     *   其他：
     *   -1- 已超时
     */
    private Short status;

    /**
     * prop:隐藏状态定义：0/显示;1/隐藏
     */
    private Short hideStatus;

    private Short reserveStatus;

    /**
     * prop:审核状态：0/无需审核;1/下单待审核;2/撤单待审核
     */
    private Short auditStatus;

    private String orderId;

    private String dealMessage;

    private String errormessage;

    private String sender;

    private String senderId;

    private String customerregisterId;

    private String entityId;

    private Short isValid;

    private Long createTime;

    private Long opTime;

    private Long lastVer;

    private String outId;

    private Short outType;

    /**
     * prop:json格式的扩展字段
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    public String getWaitingorderId() {
        return waitingorderId;
    }

    public void setWaitingorderId(String waitingorderId) {
        this.waitingorderId = waitingorderId == null ? null : waitingorderId.trim();
    }

    /**
     * get:订单来源：1/淘宝点点;2/卡包；3/服务生app；4/微信；……
     */
    public Short getOrderFrom() {
        return orderFrom;
    }

    /**
     * set:订单来源：1/淘宝点点;2/卡包；3/服务生app；4/微信；……
     */
    public void setOrderFrom(Short orderFrom) {
        this.orderFrom = orderFrom;
    }

    /**
     * get:批次信息：时:分 点菜人
     */
    public String getBatchMsg() {
        return batchMsg;
    }

    /**
     * set:批次信息：时:分 点菜人
     */
    public void setBatchMsg(String batchMsg) {
        this.batchMsg = batchMsg == null ? null : batchMsg.trim();
    }

    /**
     * get:订单类型:1/订位;2/外卖;3/扫码加菜;4/扫桌码开单
     */
    public Short getKind() {
        return kind;
    }

    /**
     * set:订单类型:1/订位;2/外卖;3/扫码加菜;4/扫桌码开单
     */
    public void setKind(Short kind) {
        this.kind = kind;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode == null ? null : seatCode.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel == null ? null : tel.trim();
    }

    /**
     * get:预订日期时间：长整形表示的日期时间，如为-1表示立即送货（用于外送订单）
     */
    public Long getReserveDate() {
        return reserveDate;
    }

    /**
     * set:预订日期时间：长整形表示的日期时间，如为-1表示立即送货（用于外送订单）
     */
    public void setReserveDate(Long reserveDate) {
        this.reserveDate = reserveDate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getRealPrice() {
        return realPrice;
    }

    public void setRealPrice(BigDecimal realPrice) {
        this.realPrice = realPrice;
    }

    public String getShopname() {
        return shopname;
    }

    public void setShopname(String shopname) {
        this.shopname = shopname == null ? null : shopname.trim();
    }

    /**
     * get:送货地址:Kind=2时填写
     */
    public String getAddress() {
        return address;
    }

    /**
     * set:送货地址:Kind=2时填写
     */
    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    /**
     * get:预付/到付模式：1/预付;2/到付；外卖接受到付，订位只能预付
     */
    public Short getPayMode() {
        return payMode;
    }

    /**
     * set:预付/到付模式：1/预付;2/到付；外卖接受到付，订位只能预付
     */
    public void setPayMode(Short payMode) {
        this.payMode = payMode;
    }

    /**
     * get:预付费方式时，填写支付类型：
     *   1/会员卡余额支付；2/银联支付；3/支付宝支付。。。
     */
    public Short getPayType() {
        return payType;
    }

    /**
     * set:预付费方式时，填写支付类型：
     *   1/会员卡余额支付；2/银联支付；3/支付宝支付。。。
     */
    public void setPayType(Short payType) {
        this.payType = payType;
    }

    /**
     * get:预付费方式时，填写支付相关信息，如卡号、流水等
     */
    public String getPayMemo() {
        return payMemo;
    }

    /**
     * set:预付费方式时，填写支付相关信息，如卡号、流水等
     */
    public void setPayMemo(String payMemo) {
        this.payMemo = payMemo == null ? null : payMemo.trim();
    }

    public BigDecimal getOutfee() {
        return outfee;
    }

    public void setOutfee(BigDecimal outfee) {
        this.outfee = outfee;
    }

    public String getCardEntityId() {
        return cardEntityId;
    }

    public void setCardEntityId(String cardEntityId) {
        this.cardEntityId = cardEntityId == null ? null : cardEntityId.trim();
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId == null ? null : cardId.trim();
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId == null ? null : payId.trim();
    }

    public BigDecimal getAdvancePay() {
        return advancePay;
    }

    public void setAdvancePay(BigDecimal advancePay) {
        this.advancePay = advancePay;
    }

    public BigDecimal getAdvanceSeatPay() {
        return advanceSeatPay;
    }

    public void setAdvanceSeatPay(BigDecimal advanceSeatPay) {
        this.advanceSeatPay = advanceSeatPay;
    }

    /**
     * get:支付状态：
     *   0-不用支付定金
     *   1-未支付
     *   2-已支付
     */
    public Short getPayStatus() {
        return payStatus;
    }

    /**
     * set:支付状态：
     *   0-不用支付定金
     *   1-未支付
     *   2-已支付
     */
    public void setPayStatus(Short payStatus) {
        this.payStatus = payStatus;
    }

    public String getReserveSeatId() {
        return reserveSeatId;
    }

    public void setReserveSeatId(String reserveSeatId) {
        this.reserveSeatId = reserveSeatId == null ? null : reserveSeatId.trim();
    }

    public String getReserveTimeId() {
        return reserveTimeId;
    }

    public void setReserveTimeId(String reserveTimeId) {
        this.reserveTimeId = reserveTimeId == null ? null : reserveTimeId.trim();
    }

    /**
     * get:预订状态定义：
     *   2- 待审核
     *   3- 已取消
     *   4- 已生效
     *   5- 已到达
     *   外卖的状态定义：
     *   2- 待审核
     *   3- 已取消
     *   4- 已生效，未下单
     *   5- 已生效，已下单
     *   6- 已生效，已送货
     *   7- 已结单
     *   其他：
     *   -1- 已超时
     */
    public Short getStatus() {
        return status;
    }

    /**
     * set:预订状态定义：
     *   2- 待审核
     *   3- 已取消
     *   4- 已生效
     *   5- 已到达
     *   外卖的状态定义：
     *   2- 待审核
     *   3- 已取消
     *   4- 已生效，未下单
     *   5- 已生效，已下单
     *   6- 已生效，已送货
     *   7- 已结单
     *   其他：
     *   -1- 已超时
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * get:隐藏状态定义：0/显示;1/隐藏
     */
    public Short getHideStatus() {
        return hideStatus;
    }

    /**
     * set:隐藏状态定义：0/显示;1/隐藏
     */
    public void setHideStatus(Short hideStatus) {
        this.hideStatus = hideStatus;
    }

    public Short getReserveStatus() {
        return reserveStatus;
    }

    public void setReserveStatus(Short reserveStatus) {
        this.reserveStatus = reserveStatus;
    }

    /**
     * get:审核状态：0/无需审核;1/下单待审核;2/撤单待审核
     */
    public Short getAuditStatus() {
        return auditStatus;
    }

    /**
     * set:审核状态：0/无需审核;1/下单待审核;2/撤单待审核
     */
    public void setAuditStatus(Short auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getDealMessage() {
        return dealMessage;
    }

    public void setDealMessage(String dealMessage) {
        this.dealMessage = dealMessage == null ? null : dealMessage.trim();
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage == null ? null : errormessage.trim();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender == null ? null : sender.trim();
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId == null ? null : senderId.trim();
    }

    public String getCustomerregisterId() {
        return customerregisterId;
    }

    public void setCustomerregisterId(String customerregisterId) {
        this.customerregisterId = customerregisterId == null ? null : customerregisterId.trim();
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

    public String getOutId() {
        return outId;
    }

    public void setOutId(String outId) {
        this.outId = outId == null ? null : outId.trim();
    }

    public Short getOutType() {
        return outType;
    }

    public void setOutType(Short outType) {
        this.outType = outType;
    }

    /**
     * get:json格式的扩展字段
     */
    public String getExt() {
        return ext;
    }

    /**
     * set:json格式的扩展字段
     */
    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }
}
