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
public class Orderdetail extends AbstractRowValueGetter implements Serializable {

    private String orderId;

    /**
     * prop:全局单号:用于在线支付
     */
    private String globalCode;

    /**
     * prop:为全局单号的简化号（后n位）
     */
    private String simpleCode;

    private String seatCode;

    private Integer code;

    private Date currDate;

    private String totalpayId;

    private String seatId;

    private Long peopleCount;

    private Long openTime;

    /**
     * prop:1正常 2并单 3撤消 4结账
     */
    private Short status;

    private String memo;

    /**
     * prop:格式：营业日期（YYYYMMDD）+流水号（4位）
     */
    private String innerCode;

    private String menutimeId;

    private String workerId;

    private Long endTime;

    private String feeplanId;

    private String opUserId;

    /**
     * prop:订单来源：0/商户自己录入;其他同WaitingOrder.OrderFrom
     */
    private Short orderFrom;

    /**
     * prop:1,正常开单;2预订开单;3.排队开单;4.外卖开单
     */
    private Short orderKind;

    private String areaId;

    private String name;

    private String mobile;

    private String tel;

    /**
     * prop:是否预约外送时间:0/立即下单;1/预约外送
     */
    private Short isAutocommit;

    private Long sendTime;

    private String address;

    /**
     * prop:支付方式：1/预付;2/到付；外卖接受到付，订位只能预付
     */
    private Short paymode;

    private BigDecimal outfee;

    private String senderId;

    private String customerregisterId;

    private String waitingorderId;

    /**
     * prop:0/未送货 1/已送货
     */
    private Short sendStatus;

    /**
     * prop:审核状态：0/无需审核;1/下单待审核;2/撤单待审核
     */
    private Short auditStatus;

    /**
     * prop:查询条件，如设为隐藏，则无相关权限的操作员在后台无法查看隐藏账单
     */
    private Byte isHide;

    private String entityId;

    private Short isValid;

    private Long createTime;

    private Long opTime;

    private Long lastVer;

    /**
     * prop:记录执行的服务器时间
     */
    private Integer loadTime;

    /**
     * prop:记录修改的服务器时间
     */
    private Integer modifyTime;

    private Boolean isLimittime;

    private String scanUrl;

    private String seatMark;

    private String reservetimeId;

    private Byte isWait;

    private Byte isPrint;

    private String bookId;

    private String reserveId;

    private String orignId;

    /**
     * prop:表示订单是否是预约字段，0表示非预约，1表示预约
     */
    private Byte reserveStatus;

    /**
     * prop:订单表的扩展字段
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * get:全局单号:用于在线支付
     */
    public String getGlobalCode() {
        return globalCode;
    }

    /**
     * set:全局单号:用于在线支付
     */
    public void setGlobalCode(String globalCode) {
        this.globalCode = globalCode == null ? null : globalCode.trim();
    }

    /**
     * get:为全局单号的简化号（后n位）
     */
    public String getSimpleCode() {
        return simpleCode;
    }

    /**
     * set:为全局单号的简化号（后n位）
     */
    public void setSimpleCode(String simpleCode) {
        this.simpleCode = simpleCode == null ? null : simpleCode.trim();
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode == null ? null : seatCode.trim();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Date getCurrDate() {
        return currDate;
    }

    public void setCurrDate(Date currDate) {
        this.currDate = currDate;
    }

    public String getTotalpayId() {
        return totalpayId;
    }

    public void setTotalpayId(String totalpayId) {
        this.totalpayId = totalpayId == null ? null : totalpayId.trim();
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId == null ? null : seatId.trim();
    }

    public Long getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Long peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Long getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Long openTime) {
        this.openTime = openTime;
    }

    /**
     * get:1正常 2并单 3撤消 4结账
     */
    public Short getStatus() {
        return status;
    }

    /**
     * set:1正常 2并单 3撤消 4结账
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    /**
     * get:格式：营业日期（YYYYMMDD）+流水号（4位）
     */
    public String getInnerCode() {
        return innerCode;
    }

    /**
     * set:格式：营业日期（YYYYMMDD）+流水号（4位）
     */
    public void setInnerCode(String innerCode) {
        this.innerCode = innerCode == null ? null : innerCode.trim();
    }

    public String getMenutimeId() {
        return menutimeId;
    }

    public void setMenutimeId(String menutimeId) {
        this.menutimeId = menutimeId == null ? null : menutimeId.trim();
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId == null ? null : workerId.trim();
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getFeeplanId() {
        return feeplanId;
    }

    public void setFeeplanId(String feeplanId) {
        this.feeplanId = feeplanId == null ? null : feeplanId.trim();
    }

    public String getOpUserId() {
        return opUserId;
    }

    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId == null ? null : opUserId.trim();
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

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId == null ? null : areaId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
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
     * get:是否预约外送时间:0/立即下单;1/预约外送
     */
    public Short getIsAutocommit() {
        return isAutocommit;
    }

    /**
     * set:是否预约外送时间:0/立即下单;1/预约外送
     */
    public void setIsAutocommit(Short isAutocommit) {
        this.isAutocommit = isAutocommit;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    /**
     * get:支付方式：1/预付;2/到付；外卖接受到付，订位只能预付
     */
    public Short getPaymode() {
        return paymode;
    }

    /**
     * set:支付方式：1/预付;2/到付；外卖接受到付，订位只能预付
     */
    public void setPaymode(Short paymode) {
        this.paymode = paymode;
    }

    public BigDecimal getOutfee() {
        return outfee;
    }

    public void setOutfee(BigDecimal outfee) {
        this.outfee = outfee;
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

    public String getWaitingorderId() {
        return waitingorderId;
    }

    public void setWaitingorderId(String waitingorderId) {
        this.waitingorderId = waitingorderId == null ? null : waitingorderId.trim();
    }

    /**
     * get:0/未送货 1/已送货
     */
    public Short getSendStatus() {
        return sendStatus;
    }

    /**
     * set:0/未送货 1/已送货
     */
    public void setSendStatus(Short sendStatus) {
        this.sendStatus = sendStatus;
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

    public Boolean getIsLimittime() {
        return isLimittime;
    }

    public void setIsLimittime(Boolean isLimittime) {
        this.isLimittime = isLimittime;
    }

    public String getScanUrl() {
        return scanUrl;
    }

    public void setScanUrl(String scanUrl) {
        this.scanUrl = scanUrl == null ? null : scanUrl.trim();
    }

    public String getSeatMark() {
        return seatMark;
    }

    public void setSeatMark(String seatMark) {
        this.seatMark = seatMark == null ? null : seatMark.trim();
    }

    public String getReservetimeId() {
        return reservetimeId;
    }

    public void setReservetimeId(String reservetimeId) {
        this.reservetimeId = reservetimeId == null ? null : reservetimeId.trim();
    }

    public Byte getIsWait() {
        return isWait;
    }

    public void setIsWait(Byte isWait) {
        this.isWait = isWait;
    }

    public Byte getIsPrint() {
        return isPrint;
    }

    public void setIsPrint(Byte isPrint) {
        this.isPrint = isPrint;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId == null ? null : bookId.trim();
    }

    public String getReserveId() {
        return reserveId;
    }

    public void setReserveId(String reserveId) {
        this.reserveId = reserveId == null ? null : reserveId.trim();
    }

    public String getOrignId() {
        return orignId;
    }

    public void setOrignId(String orignId) {
        this.orignId = orignId == null ? null : orignId.trim();
    }

    /**
     * get:表示订单是否是预约字段，0表示非预约，1表示预约
     */
    public Byte getReserveStatus() {
        return reserveStatus;
    }

    /**
     * set:表示订单是否是预约字段，0表示非预约，1表示预约
     */
    public void setReserveStatus(Byte reserveStatus) {
        this.reserveStatus = reserveStatus;
    }

    /**
     * get:订单表的扩展字段
     */
    public String getExt() {
        return ext;
    }

    /**
     * set:订单表的扩展字段
     */
    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }
}
