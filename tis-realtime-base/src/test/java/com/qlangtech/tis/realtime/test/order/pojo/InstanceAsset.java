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
public class InstanceAsset extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:主键ID
     */
    private String id;

    /**
     * prop:操作人ID
     */
    private String opUserId;

    /**
     * prop:操作人的ID
     */
    private String verifyUserId;

    /**
     * prop:核销时间
     */
    private Long verifyTime;

    /**
     * prop:资产状态1、未处理，2、资产交付
     */
    private Byte assetStatus;

    /**
     * prop:商品的ID
     */
    private String instanceId;

    /**
     * prop:订单的ID
     */
    private String orderId;

    /**
     * prop:资产编码
     */
    private String assetCode;

    /**
     * prop:用户手机号，可以没有
     */
    private String mobile;

    /**
     * prop:店铺ID
     */
    private String entityId;

    /**
     * prop:删除标识
     */
    private Byte isValid;

    /**
     * prop:创建时间
     */
    private Long createTime;

    /**
     * prop:修改时间
     */
    private Long opTime;

    /**
     * prop:版本号
     */
    private Integer lastVer;

    /**
     * prop:扩展数据， 预留， 暂时不需要
     */
    private String ext;

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

    /**
     * get:操作人ID
     */
    public String getOpUserId() {
        return opUserId;
    }

    /**
     * set:操作人ID
     */
    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId == null ? null : opUserId.trim();
    }

    /**
     * get:操作人的ID
     */
    public String getVerifyUserId() {
        return verifyUserId;
    }

    /**
     * set:操作人的ID
     */
    public void setVerifyUserId(String verifyUserId) {
        this.verifyUserId = verifyUserId == null ? null : verifyUserId.trim();
    }

    /**
     * get:核销时间
     */
    public Long getVerifyTime() {
        return verifyTime;
    }

    /**
     * set:核销时间
     */
    public void setVerifyTime(Long verifyTime) {
        this.verifyTime = verifyTime;
    }

    /**
     * get:资产状态1、未处理，2、资产交付
     */
    public Byte getAssetStatus() {
        return assetStatus;
    }

    /**
     * set:资产状态1、未处理，2、资产交付
     */
    public void setAssetStatus(Byte assetStatus) {
        this.assetStatus = assetStatus;
    }

    /**
     * get:商品的ID
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * set:商品的ID
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId == null ? null : instanceId.trim();
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
     * get:资产编码
     */
    public String getAssetCode() {
        return assetCode;
    }

    /**
     * set:资产编码
     */
    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode == null ? null : assetCode.trim();
    }

    /**
     * get:用户手机号，可以没有
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * set:用户手机号，可以没有
     */
    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    /**
     * get:店铺ID
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:店铺ID
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:删除标识
     */
    public Byte getIsValid() {
        return isValid;
    }

    /**
     * set:删除标识
     */
    public void setIsValid(Byte isValid) {
        this.isValid = isValid;
    }

    /**
     * get:创建时间
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * set:创建时间
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * get:修改时间
     */
    public Long getOpTime() {
        return opTime;
    }

    /**
     * set:修改时间
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

    /**
     * get:扩展数据， 预留， 暂时不需要
     */
    public String getExt() {
        return ext;
    }

    /**
     * set:扩展数据， 预留， 暂时不需要
     */
    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }
}
