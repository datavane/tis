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
public class Paydetail extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:付款额外信息id
     */
    private String paydetailId;

    /**
     * prop:付款信息ID
     */
    private String payId;

    /**
     * prop:付款类型额外信息项
     */
    private String kindpaydetailId;

    /**
     * prop:所选择的选项ID
     */
    private String kindpaydetailOptionId;

    /**
     * prop:表示付款类型是否需要输入额外信息，如信用卡付款，需要输入卡号；如果字段为空，则不需要输入额外信息
     */
    private String memo;

    /**
     * prop:所属实体
     */
    private String entityId;

    /**
     * prop:是否有效
     */
    private Byte isValid;

    /**
     * prop:记录生成时间
     */
    private Long createTime;

    /**
     * prop:操作时间
     */
    private Long opTime;

    /**
     * prop:版本号
     */
    private Long lastVer;

    /**
     * prop:服务器创建时间
     */
    private Integer loadTime;

    /**
     * prop:服务器修改时间
     */
    private Integer modifyTime;

    private static final long serialVersionUID = 1L;

    /**
     * get:付款额外信息id
     */
    public String getPaydetailId() {
        return paydetailId;
    }

    /**
     * set:付款额外信息id
     */
    public void setPaydetailId(String paydetailId) {
        this.paydetailId = paydetailId == null ? null : paydetailId.trim();
    }

    /**
     * get:付款信息ID
     */
    public String getPayId() {
        return payId;
    }

    /**
     * set:付款信息ID
     */
    public void setPayId(String payId) {
        this.payId = payId == null ? null : payId.trim();
    }

    /**
     * get:付款类型额外信息项
     */
    public String getKindpaydetailId() {
        return kindpaydetailId;
    }

    /**
     * set:付款类型额外信息项
     */
    public void setKindpaydetailId(String kindpaydetailId) {
        this.kindpaydetailId = kindpaydetailId == null ? null : kindpaydetailId.trim();
    }

    /**
     * get:所选择的选项ID
     */
    public String getKindpaydetailOptionId() {
        return kindpaydetailOptionId;
    }

    /**
     * set:所选择的选项ID
     */
    public void setKindpaydetailOptionId(String kindpaydetailOptionId) {
        this.kindpaydetailOptionId = kindpaydetailOptionId == null ? null : kindpaydetailOptionId.trim();
    }

    /**
     * get:表示付款类型是否需要输入额外信息，如信用卡付款，需要输入卡号；如果字段为空，则不需要输入额外信息
     */
    public String getMemo() {
        return memo;
    }

    /**
     * set:表示付款类型是否需要输入额外信息，如信用卡付款，需要输入卡号；如果字段为空，则不需要输入额外信息
     */
    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
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
    public Byte getIsValid() {
        return isValid;
    }

    /**
     * set:是否有效
     */
    public void setIsValid(Byte isValid) {
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
     * get:版本号
     */
    public Long getLastVer() {
        return lastVer;
    }

    /**
     * set:版本号
     */
    public void setLastVer(Long lastVer) {
        this.lastVer = lastVer;
    }

    /**
     * get:服务器创建时间
     */
    public Integer getLoadTime() {
        return loadTime;
    }

    /**
     * set:服务器创建时间
     */
    public void setLoadTime(Integer loadTime) {
        this.loadTime = loadTime;
    }

    /**
     * get:服务器修改时间
     */
    public Integer getModifyTime() {
        return modifyTime;
    }

    /**
     * set:服务器修改时间
     */
    public void setModifyTime(Integer modifyTime) {
        this.modifyTime = modifyTime;
    }
}
