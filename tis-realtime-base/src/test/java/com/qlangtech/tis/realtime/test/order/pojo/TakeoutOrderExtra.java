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
public class TakeoutOrderExtra extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:订单id和waitingorderid值一样
     */
    private String orderId;

    /**
     * prop:订单来源
     */
    private Short orderFrom;

    /**
     * prop:显示订单号id
     */
    private String viewId;

    /**
     * prop:是否开发票
     */
    private Byte hasInvoiced;

    /**
     * prop:发票抬头
     */
    private String invoiceTitle;

    /**
     * prop:是否第三方配送
     */
    private Byte isThirdShipping;

    /**
     * prop:当日流水号
     */
    private String daySeq;

    /**
     * prop:外卖送货人名称
     */
    private String courierName;

    /**
     * prop:外卖送货人电话
     */
    private String courierPhone;

    /**
     * prop:外卖取消原因
     */
    private String cancelReason;

    /**
     * prop:实体id
     */
    private String entityId;

    /**
     * prop:订单外部编号
     */
    private String outId;

    private Integer lastVer;

    /**
     * prop:开始的期望送达/取货时间
     */
    private Long beginExpectDate;

    /**
     * prop:结束的期望送达/取货时间
     */
    private Long endExpectDate;

    /**
     * prop:外卖预约时间名称
     */
    private String reserveDateName;

    /**
     * prop:活动信息
     */
    private String activity;

    /**
     * prop:额外信息
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    /**
     * get:订单id和waitingorderid值一样
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * set:订单id和waitingorderid值一样
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * get:订单来源
     */
    public Short getOrderFrom() {
        return orderFrom;
    }

    /**
     * set:订单来源
     */
    public void setOrderFrom(Short orderFrom) {
        this.orderFrom = orderFrom;
    }

    /**
     * get:显示订单号id
     */
    public String getViewId() {
        return viewId;
    }

    /**
     * set:显示订单号id
     */
    public void setViewId(String viewId) {
        this.viewId = viewId == null ? null : viewId.trim();
    }

    /**
     * get:是否开发票
     */
    public Byte getHasInvoiced() {
        return hasInvoiced;
    }

    /**
     * set:是否开发票
     */
    public void setHasInvoiced(Byte hasInvoiced) {
        this.hasInvoiced = hasInvoiced;
    }

    /**
     * get:发票抬头
     */
    public String getInvoiceTitle() {
        return invoiceTitle;
    }

    /**
     * set:发票抬头
     */
    public void setInvoiceTitle(String invoiceTitle) {
        this.invoiceTitle = invoiceTitle == null ? null : invoiceTitle.trim();
    }

    /**
     * get:是否第三方配送
     */
    public Byte getIsThirdShipping() {
        return isThirdShipping;
    }

    /**
     * set:是否第三方配送
     */
    public void setIsThirdShipping(Byte isThirdShipping) {
        this.isThirdShipping = isThirdShipping;
    }

    /**
     * get:当日流水号
     */
    public String getDaySeq() {
        return daySeq;
    }

    /**
     * set:当日流水号
     */
    public void setDaySeq(String daySeq) {
        this.daySeq = daySeq == null ? null : daySeq.trim();
    }

    /**
     * get:外卖送货人名称
     */
    public String getCourierName() {
        return courierName;
    }

    /**
     * set:外卖送货人名称
     */
    public void setCourierName(String courierName) {
        this.courierName = courierName == null ? null : courierName.trim();
    }

    /**
     * get:外卖送货人电话
     */
    public String getCourierPhone() {
        return courierPhone;
    }

    /**
     * set:外卖送货人电话
     */
    public void setCourierPhone(String courierPhone) {
        this.courierPhone = courierPhone == null ? null : courierPhone.trim();
    }

    /**
     * get:外卖取消原因
     */
    public String getCancelReason() {
        return cancelReason;
    }

    /**
     * set:外卖取消原因
     */
    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason == null ? null : cancelReason.trim();
    }

    /**
     * get:实体id
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:实体id
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:订单外部编号
     */
    public String getOutId() {
        return outId;
    }

    /**
     * set:订单外部编号
     */
    public void setOutId(String outId) {
        this.outId = outId == null ? null : outId.trim();
    }

    public Integer getLastVer() {
        return lastVer;
    }

    public void setLastVer(Integer lastVer) {
        this.lastVer = lastVer;
    }

    /**
     * get:开始的期望送达/取货时间
     */
    public Long getBeginExpectDate() {
        return beginExpectDate;
    }

    /**
     * set:开始的期望送达/取货时间
     */
    public void setBeginExpectDate(Long beginExpectDate) {
        this.beginExpectDate = beginExpectDate;
    }

    /**
     * get:结束的期望送达/取货时间
     */
    public Long getEndExpectDate() {
        return endExpectDate;
    }

    /**
     * set:结束的期望送达/取货时间
     */
    public void setEndExpectDate(Long endExpectDate) {
        this.endExpectDate = endExpectDate;
    }

    /**
     * get:外卖预约时间名称
     */
    public String getReserveDateName() {
        return reserveDateName;
    }

    /**
     * set:外卖预约时间名称
     */
    public void setReserveDateName(String reserveDateName) {
        this.reserveDateName = reserveDateName == null ? null : reserveDateName.trim();
    }

    /**
     * get:活动信息
     */
    public String getActivity() {
        return activity;
    }

    /**
     * set:活动信息
     */
    public void setActivity(String activity) {
        this.activity = activity == null ? null : activity.trim();
    }

    /**
     * get:额外信息
     */
    public String getExt() {
        return ext;
    }

    /**
     * set:额外信息
     */
    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }
}
