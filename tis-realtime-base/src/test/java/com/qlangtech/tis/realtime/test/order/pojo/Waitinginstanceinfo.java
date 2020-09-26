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
public class Waitinginstanceinfo extends AbstractRowValueGetter implements Serializable {

    private String waitinginstanceId;

    /**
     * prop:订单ID：关联WaitingOrder.Id
     */
    private String waitingorderId;

    /**
     * prop:点菜类型：1:普通菜;2:套菜;3:自定义菜;4:自定义套菜
     */
    private Short kind;

    private String kindmenuId;

    private String kindmenuName;

    private String name;

    private String menuId;

    private String makeId;

    private String makename;

    private BigDecimal makePrice;

    /**
     * prop:1,一次性加价;2,按点菜单位加价;3,按结账单位加价
     */
    private Short makePricemode;

    private String specDetailName;

    private String specDetailId;

    /**
     * prop:1/使用PriceScale字段按比例计算价格 2/使用PriceScale字段按加价计算价格
     */
    private Short specPricemode;

    private BigDecimal specDetailPrice;

    private BigDecimal num;

    private BigDecimal accountNum;

    private String unit;

    private String accountUnit;

    private String memo;

    /**
     * prop:AMOUNT
     */
    private BigDecimal originalPrice;

    private BigDecimal price;

    private BigDecimal memberPrice;

    private BigDecimal fee;

    private Short isRatio;

    private String taste;

    private BigDecimal ratio;

    private BigDecimal ratioFee;

    private Short isBackauth;

    private String parentId;

    /**
     * prop:1:固定价;2:浮动价
     */
    private Short priceMode;

    private String childId;

    /**
     * prop:服务费收取方式serviceFeeMode（0/不收取，1/固定费用，2/菜价百分比），默认为不收取
     */
    private Short serviceFeemode;

    /**
     * prop:<1:为菜价的百分比；>1即为收取的数值
     */
    private BigDecimal serviceFee;

    /**
     * prop:status：0/待发送；1/已发送待审核;2/下单超时;3/下单失败；9/下单成功
     */
    private Short status;

    private String errorMsg;

    private String entityId;

    private Short isValid;

    private Long createTime;

    private Long opTime;

    private Long lastVer;

    /**
     * prop:批次信息：时:分 点菜人
     */
    private String batchMsg;

    /**
     * prop:商品类型：
     * 0:菜
     * 1: 打包盒
     */
    private Short type;

    /**
     * prop:加料价
     */
    private BigDecimal additionPrice;

    /**
     * prop:扩展字段，不适合做查询字段。
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    public String getWaitinginstanceId() {
        return waitinginstanceId;
    }

    public void setWaitinginstanceId(String waitinginstanceId) {
        this.waitinginstanceId = waitinginstanceId == null ? null : waitinginstanceId.trim();
    }

    /**
     * get:订单ID：关联WaitingOrder.Id
     */
    public String getWaitingorderId() {
        return waitingorderId;
    }

    /**
     * set:订单ID：关联WaitingOrder.Id
     */
    public void setWaitingorderId(String waitingorderId) {
        this.waitingorderId = waitingorderId == null ? null : waitingorderId.trim();
    }

    /**
     * get:点菜类型：1:普通菜;2:套菜;3:自定义菜;4:自定义套菜
     */
    public Short getKind() {
        return kind;
    }

    /**
     * set:点菜类型：1:普通菜;2:套菜;3:自定义菜;4:自定义套菜
     */
    public void setKind(Short kind) {
        this.kind = kind;
    }

    public String getKindmenuId() {
        return kindmenuId;
    }

    public void setKindmenuId(String kindmenuId) {
        this.kindmenuId = kindmenuId == null ? null : kindmenuId.trim();
    }

    public String getKindmenuName() {
        return kindmenuName;
    }

    public void setKindmenuName(String kindmenuName) {
        this.kindmenuName = kindmenuName == null ? null : kindmenuName.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId == null ? null : menuId.trim();
    }

    public String getMakeId() {
        return makeId;
    }

    public void setMakeId(String makeId) {
        this.makeId = makeId == null ? null : makeId.trim();
    }

    public String getMakename() {
        return makename;
    }

    public void setMakename(String makename) {
        this.makename = makename == null ? null : makename.trim();
    }

    public BigDecimal getMakePrice() {
        return makePrice;
    }

    public void setMakePrice(BigDecimal makePrice) {
        this.makePrice = makePrice;
    }

    /**
     * get:1,一次性加价;2,按点菜单位加价;3,按结账单位加价
     */
    public Short getMakePricemode() {
        return makePricemode;
    }

    /**
     * set:1,一次性加价;2,按点菜单位加价;3,按结账单位加价
     */
    public void setMakePricemode(Short makePricemode) {
        this.makePricemode = makePricemode;
    }

    public String getSpecDetailName() {
        return specDetailName;
    }

    public void setSpecDetailName(String specDetailName) {
        this.specDetailName = specDetailName == null ? null : specDetailName.trim();
    }

    public String getSpecDetailId() {
        return specDetailId;
    }

    public void setSpecDetailId(String specDetailId) {
        this.specDetailId = specDetailId == null ? null : specDetailId.trim();
    }

    /**
     * get:1/使用PriceScale字段按比例计算价格 2/使用PriceScale字段按加价计算价格
     */
    public Short getSpecPricemode() {
        return specPricemode;
    }

    /**
     * set:1/使用PriceScale字段按比例计算价格 2/使用PriceScale字段按加价计算价格
     */
    public void setSpecPricemode(Short specPricemode) {
        this.specPricemode = specPricemode;
    }

    public BigDecimal getSpecDetailPrice() {
        return specDetailPrice;
    }

    public void setSpecDetailPrice(BigDecimal specDetailPrice) {
        this.specDetailPrice = specDetailPrice;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public BigDecimal getAccountNum() {
        return accountNum;
    }

    public void setAccountNum(BigDecimal accountNum) {
        this.accountNum = accountNum;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit == null ? null : unit.trim();
    }

    public String getAccountUnit() {
        return accountUnit;
    }

    public void setAccountUnit(String accountUnit) {
        this.accountUnit = accountUnit == null ? null : accountUnit.trim();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    /**
     * get:AMOUNT
     */
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    /**
     * set:AMOUNT
     */
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getMemberPrice() {
        return memberPrice;
    }

    public void setMemberPrice(BigDecimal memberPrice) {
        this.memberPrice = memberPrice;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public Short getIsRatio() {
        return isRatio;
    }

    public void setIsRatio(Short isRatio) {
        this.isRatio = isRatio;
    }

    public String getTaste() {
        return taste;
    }

    public void setTaste(String taste) {
        this.taste = taste == null ? null : taste.trim();
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public BigDecimal getRatioFee() {
        return ratioFee;
    }

    public void setRatioFee(BigDecimal ratioFee) {
        this.ratioFee = ratioFee;
    }

    public Short getIsBackauth() {
        return isBackauth;
    }

    public void setIsBackauth(Short isBackauth) {
        this.isBackauth = isBackauth;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    /**
     * get:1:固定价;2:浮动价
     */
    public Short getPriceMode() {
        return priceMode;
    }

    /**
     * set:1:固定价;2:浮动价
     */
    public void setPriceMode(Short priceMode) {
        this.priceMode = priceMode;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId == null ? null : childId.trim();
    }

    /**
     * get:服务费收取方式serviceFeeMode（0/不收取，1/固定费用，2/菜价百分比），默认为不收取
     */
    public Short getServiceFeemode() {
        return serviceFeemode;
    }

    /**
     * set:服务费收取方式serviceFeeMode（0/不收取，1/固定费用，2/菜价百分比），默认为不收取
     */
    public void setServiceFeemode(Short serviceFeemode) {
        this.serviceFeemode = serviceFeemode;
    }

    /**
     * get:<1:为菜价的百分比；>1即为收取的数值
     */
    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    /**
     * set:<1:为菜价的百分比；>1即为收取的数值
     */
    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    /**
     * get:status：0/待发送；1/已发送待审核;2/下单超时;3/下单失败；9/下单成功
     */
    public Short getStatus() {
        return status;
    }

    /**
     * set:status：0/待发送；1/已发送待审核;2/下单超时;3/下单失败；9/下单成功
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg == null ? null : errorMsg.trim();
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
     * get:商品类型：
     * 0:菜
     * 1: 打包盒
     */
    public Short getType() {
        return type;
    }

    /**
     * set:商品类型：
     * 0:菜
     * 1: 打包盒
     */
    public void setType(Short type) {
        this.type = type;
    }

    /**
     * get:加料价
     */
    public BigDecimal getAdditionPrice() {
        return additionPrice;
    }

    /**
     * set:加料价
     */
    public void setAdditionPrice(BigDecimal additionPrice) {
        this.additionPrice = additionPrice;
    }

    /**
     * get:扩展字段，不适合做查询字段。
     */
    public String getExt() {
        return ext;
    }

    /**
     * set:扩展字段，不适合做查询字段。
     */
    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }
}
