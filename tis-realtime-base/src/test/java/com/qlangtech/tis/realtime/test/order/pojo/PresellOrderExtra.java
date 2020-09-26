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
public class PresellOrderExtra extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:扩展信息表以订单id作为主键ID
     */
    private String orderId;

    /**
     * prop:分库字段
     */
    private String entityId;

    /**
     * prop:库存量单位
     */
    private Long stockId;

    /**
     * prop:时段的ID
     */
    private Long timeFrameId;

    /**
     * prop:时间类型名称（如早茶，下午茶，晚餐）
     */
    private String timeFrameName;

    /**
     * prop:桌位类型ID
     */
    private Long seatTypeId;

    /**
     * prop:桌位类型名称
     */
    private String seatTypeName;

    /**
     * prop:商家提供的折扣比例
     */
    private Double discountRatio;

    /**
     * prop:开始时间
     */
    private Long startTime;

    /**
     * prop:结束时间
     */
    private Long endTime;

    /**
     * prop:核销的时间，未核销时候 此处为0
     */
    private Long verifyTime;

    private Integer lastVer;

    private Long createTime;

    private Long opTime;

    /**
     * prop:未核销单过期时间
     */
    private Long overtime;

    /**
     * prop:扩展字段
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    /**
     * get:扩展信息表以订单id作为主键ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * set:扩展信息表以订单id作为主键ID
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * get:分库字段
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:分库字段
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:库存量单位
     */
    public Long getStockId() {
        return stockId;
    }

    /**
     * set:库存量单位
     */
    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    /**
     * get:时段的ID
     */
    public Long getTimeFrameId() {
        return timeFrameId;
    }

    /**
     * set:时段的ID
     */
    public void setTimeFrameId(Long timeFrameId) {
        this.timeFrameId = timeFrameId;
    }

    /**
     * get:时间类型名称（如早茶，下午茶，晚餐）
     */
    public String getTimeFrameName() {
        return timeFrameName;
    }

    /**
     * set:时间类型名称（如早茶，下午茶，晚餐）
     */
    public void setTimeFrameName(String timeFrameName) {
        this.timeFrameName = timeFrameName == null ? null : timeFrameName.trim();
    }

    /**
     * get:桌位类型ID
     */
    public Long getSeatTypeId() {
        return seatTypeId;
    }

    /**
     * set:桌位类型ID
     */
    public void setSeatTypeId(Long seatTypeId) {
        this.seatTypeId = seatTypeId;
    }

    /**
     * get:桌位类型名称
     */
    public String getSeatTypeName() {
        return seatTypeName;
    }

    /**
     * set:桌位类型名称
     */
    public void setSeatTypeName(String seatTypeName) {
        this.seatTypeName = seatTypeName == null ? null : seatTypeName.trim();
    }

    /**
     * get:商家提供的折扣比例
     */
    public Double getDiscountRatio() {
        return discountRatio;
    }

    /**
     * set:商家提供的折扣比例
     */
    public void setDiscountRatio(Double discountRatio) {
        this.discountRatio = discountRatio;
    }

    /**
     * get:开始时间
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * set:开始时间
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * get:结束时间
     */
    public Long getEndTime() {
        return endTime;
    }

    /**
     * set:结束时间
     */
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    /**
     * get:核销的时间，未核销时候 此处为0
     */
    public Long getVerifyTime() {
        return verifyTime;
    }

    /**
     * set:核销的时间，未核销时候 此处为0
     */
    public void setVerifyTime(Long verifyTime) {
        this.verifyTime = verifyTime;
    }

    public Integer getLastVer() {
        return lastVer;
    }

    public void setLastVer(Integer lastVer) {
        this.lastVer = lastVer;
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

    /**
     * get:未核销单过期时间
     */
    public Long getOvertime() {
        return overtime;
    }

    /**
     * set:未核销单过期时间
     */
    public void setOvertime(Long overtime) {
        this.overtime = overtime;
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
