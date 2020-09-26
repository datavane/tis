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
public class Queueop extends AbstractRowValueGetter implements Serializable {

    private String queueopId;

    /**
     * prop:操作类型：(1.开始排队,2.停止排队,3.取号,4.叫号,5.过号,6.取消排队(系统),7.取消排队(火小二))
     */
    private Short opType;

    /**
     * prop:来源：(1-火排队,2-火取号,3-火小二)
     */
    private Short source;

    /**
     * prop:相关Id
     */
    private String sourceId;

    /**
     * prop:业务说明
     */
    private String memo;

    /**
     * prop:操作人Id
     */
    private String opUserId;

    /**
     * prop:操作人名
     */
    private String opUserName;

    /**
     * prop:所属实体
     */
    private String entityId;

    /**
     * prop:是否有效
     */
    private Short isValid;

    /**
     * prop:记录生成时间
     */
    private Long createTime;

    /**
     * prop:操作时间
     */
    private Long opTime;

    /**
     * prop:记录版本号
     */
    private Long lastVer;

    private static final long serialVersionUID = 1L;

    public String getQueueopId() {
        return queueopId;
    }

    public void setQueueopId(String queueopId) {
        this.queueopId = queueopId == null ? null : queueopId.trim();
    }

    /**
     * get:操作类型：(1.开始排队,2.停止排队,3.取号,4.叫号,5.过号,6.取消排队(系统),7.取消排队(火小二))
     */
    public Short getOpType() {
        return opType;
    }

    /**
     * set:操作类型：(1.开始排队,2.停止排队,3.取号,4.叫号,5.过号,6.取消排队(系统),7.取消排队(火小二))
     */
    public void setOpType(Short opType) {
        this.opType = opType;
    }

    /**
     * get:来源：(1-火排队,2-火取号,3-火小二)
     */
    public Short getSource() {
        return source;
    }

    /**
     * set:来源：(1-火排队,2-火取号,3-火小二)
     */
    public void setSource(Short source) {
        this.source = source;
    }

    /**
     * get:相关Id
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * set:相关Id
     */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId == null ? null : sourceId.trim();
    }

    /**
     * get:业务说明
     */
    public String getMemo() {
        return memo;
    }

    /**
     * set:业务说明
     */
    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    /**
     * get:操作人Id
     */
    public String getOpUserId() {
        return opUserId;
    }

    /**
     * set:操作人Id
     */
    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId == null ? null : opUserId.trim();
    }

    /**
     * get:操作人名
     */
    public String getOpUserName() {
        return opUserName;
    }

    /**
     * set:操作人名
     */
    public void setOpUserName(String opUserName) {
        this.opUserName = opUserName == null ? null : opUserName.trim();
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
     * get:是否有效
     */
    public Short getIsValid() {
        return isValid;
    }

    /**
     * set:是否有效
     */
    public void setIsValid(Short isValid) {
        this.isValid = isValid;
    }

    /**
     * get:记录生成时间
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * set:记录生成时间
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * get:操作时间
     */
    public Long getOpTime() {
        return opTime;
    }

    /**
     * set:操作时间
     */
    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }

    /**
     * get:记录版本号
     */
    public Long getLastVer() {
        return lastVer;
    }

    /**
     * set:记录版本号
     */
    public void setLastVer(Long lastVer) {
        this.lastVer = lastVer;
    }
}
