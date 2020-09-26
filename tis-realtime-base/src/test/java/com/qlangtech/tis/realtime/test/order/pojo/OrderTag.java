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
public class OrderTag extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:主键id
     */
    private Long id;

    /**
     * prop:订单id
     */
    private String orderId;

    /**
     * prop:关键字
     */
    private String tagKey;

    /**
     * prop:关键字值
     */
    private String tagValue;

    /**
     * prop:所属实体
     */
    private String entityId;

    /**
     * prop:业务线来源
     */
    private Boolean bizFrom;

    /**
     * prop:1：有效；0：无效
     */
    private Boolean isValid;

    /**
     * prop:记录创建时间
     */
    private Long createTime;

    /**
     * prop:记录修改时间
     */
    private Long opTime;

    /**
     * prop:版本号
     */
    private Integer lastVer;

    private static final long serialVersionUID = 1L;

    /**
     * get:主键id
     */
    public Long getId() {
        return id;
    }

    /**
     * set:主键id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * get:订单id
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * set:订单id
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * get:关键字
     */
    public String getTagKey() {
        return tagKey;
    }

    /**
     * set:关键字
     */
    public void setTagKey(String tagKey) {
        this.tagKey = tagKey == null ? null : tagKey.trim();
    }

    /**
     * get:关键字值
     */
    public String getTagValue() {
        return tagValue;
    }

    /**
     * set:关键字值
     */
    public void setTagValue(String tagValue) {
        this.tagValue = tagValue == null ? null : tagValue.trim();
    }

    /**
     * get:所属实体
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:所属实体
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:业务线来源
     */
    public Boolean getBizFrom() {
        return bizFrom;
    }

    /**
     * set:业务线来源
     */
    public void setBizFrom(Boolean bizFrom) {
        this.bizFrom = bizFrom;
    }

    /**
     * get:1：有效；0：无效
     */
    public Boolean getIsValid() {
        return isValid;
    }

    /**
     * set:1：有效；0：无效
     */
    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * get:记录创建时间
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * set:记录创建时间
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * get:记录修改时间
     */
    public Long getOpTime() {
        return opTime;
    }

    /**
     * set:记录修改时间
     */
    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }

    /**
     * get:版本号
     */
    public Integer getLastVer() {
        return lastVer;
    }

    /**
     * set:版本号
     */
    public void setLastVer(Integer lastVer) {
        this.lastVer = lastVer;
    }
}
