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
public class Instancedetail extends AbstractRowValueGetter implements Serializable {

    private String instanceId;

    private String orderId;

    /**
     * prop:批次信息：时:分 点菜人
     */
    private String batchMsg;

    /**
     * prop:商品类型(0:菜 1: 打包盒)
     */
    private Short type;

    private String waitinginstanceId;

    /**
     * prop:1:普通菜;2:套菜;3:自定义菜;4:自定义套菜
     */
    private Short kind;

    private String parentId;

    /**
     * prop:1:固定价;2:浮动价
     */
    private Short pricemode;

    private String name;

    private String makename;

    private String taste;

    private String specDetailName;

    private BigDecimal num;

    private BigDecimal accountNum;

    private String unit;

    private String accountUnit;

    private BigDecimal price;

    private BigDecimal memberPrice;

    private BigDecimal fee;

    private BigDecimal ratio;

    private BigDecimal ratioFee;

    private String ratioCause;

    /**
     * prop:状态：1/未确认 2/正常 3/退菜标志
     */
    private Short status;

    private String kindmenuId;

    private String kindmenuName;

    private String menuId;

    private String memo;

    private Short isRatio;

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

    private Byte drawStatus;

    private String bookmenuId;

    private String makeId;

    private BigDecimal makePrice;

    private String prodplanId;

    private Byte isWait;

    private String specdetailId;

    private BigDecimal specdetailPrice;

    private Byte makepriceMode;

    private String originalPrice;

    private Byte isBuynumberChanged;

    private String ratioOperatorId;

    private String childId;

    private String kindBookmenuId;

    private Byte specpriceMode;

    private String workerId;

    private Byte isBackauth;

    private Byte serviceFeeMode;

    private String serviceFee;

    private String orignId;

    private BigDecimal additionPrice;

    private Byte hasAddition;

    private String seatId;

    /**
     * prop:扩展字段，不适合做查询字段
     */
    private String ext;

    private static final long serialVersionUID = 1L;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId == null ? null : instanceId.trim();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
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
     * get:商品类型(0:菜 1: 打包盒)
     */
    public Short getType() {
        return type;
    }

    /**
     * set:商品类型(0:菜 1: 打包盒)
     */
    public void setType(Short type) {
        this.type = type;
    }

    public String getWaitinginstanceId() {
        return waitinginstanceId;
    }

    public void setWaitinginstanceId(String waitinginstanceId) {
        this.waitinginstanceId = waitinginstanceId == null ? null : waitinginstanceId.trim();
    }

    /**
     * get:1:普通菜;2:套菜;3:自定义菜;4:自定义套菜
     */
    public Short getKind() {
        return kind;
    }

    /**
     * set:1:普通菜;2:套菜;3:自定义菜;4:自定义套菜
     */
    public void setKind(Short kind) {
        this.kind = kind;
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
    public Short getPricemode() {
        return pricemode;
    }

    /**
     * set:1:固定价;2:浮动价
     */
    public void setPricemode(Short pricemode) {
        this.pricemode = pricemode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getMakename() {
        return makename;
    }

    public void setMakename(String makename) {
        this.makename = makename == null ? null : makename.trim();
    }

    public String getTaste() {
        return taste;
    }

    public void setTaste(String taste) {
        this.taste = taste == null ? null : taste.trim();
    }

    public String getSpecDetailName() {
        return specDetailName;
    }

    public void setSpecDetailName(String specDetailName) {
        this.specDetailName = specDetailName == null ? null : specDetailName.trim();
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

    public String getRatioCause() {
        return ratioCause;
    }

    public void setRatioCause(String ratioCause) {
        this.ratioCause = ratioCause == null ? null : ratioCause.trim();
    }

    /**
     * get:状态：1/未确认 2/正常 3/退菜标志
     */
    public Short getStatus() {
        return status;
    }

    /**
     * set:状态：1/未确认 2/正常 3/退菜标志
     */
    public void setStatus(Short status) {
        this.status = status;
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

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId == null ? null : menuId.trim();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    public Short getIsRatio() {
        return isRatio;
    }

    public void setIsRatio(Short isRatio) {
        this.isRatio = isRatio;
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

    public Byte getDrawStatus() {
        return drawStatus;
    }

    public void setDrawStatus(Byte drawStatus) {
        this.drawStatus = drawStatus;
    }

    public String getBookmenuId() {
        return bookmenuId;
    }

    public void setBookmenuId(String bookmenuId) {
        this.bookmenuId = bookmenuId == null ? null : bookmenuId.trim();
    }

    public String getMakeId() {
        return makeId;
    }

    public void setMakeId(String makeId) {
        this.makeId = makeId == null ? null : makeId.trim();
    }

    public BigDecimal getMakePrice() {
        return makePrice;
    }

    public void setMakePrice(BigDecimal makePrice) {
        this.makePrice = makePrice;
    }

    public String getProdplanId() {
        return prodplanId;
    }

    public void setProdplanId(String prodplanId) {
        this.prodplanId = prodplanId == null ? null : prodplanId.trim();
    }

    public Byte getIsWait() {
        return isWait;
    }

    public void setIsWait(Byte isWait) {
        this.isWait = isWait;
    }

    public String getSpecdetailId() {
        return specdetailId;
    }

    public void setSpecdetailId(String specdetailId) {
        this.specdetailId = specdetailId == null ? null : specdetailId.trim();
    }

    public BigDecimal getSpecdetailPrice() {
        return specdetailPrice;
    }

    public void setSpecdetailPrice(BigDecimal specdetailPrice) {
        this.specdetailPrice = specdetailPrice;
    }

    public Byte getMakepriceMode() {
        return makepriceMode;
    }

    public void setMakepriceMode(Byte makepriceMode) {
        this.makepriceMode = makepriceMode;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice == null ? null : originalPrice.trim();
    }

    public Byte getIsBuynumberChanged() {
        return isBuynumberChanged;
    }

    public void setIsBuynumberChanged(Byte isBuynumberChanged) {
        this.isBuynumberChanged = isBuynumberChanged;
    }

    public String getRatioOperatorId() {
        return ratioOperatorId;
    }

    public void setRatioOperatorId(String ratioOperatorId) {
        this.ratioOperatorId = ratioOperatorId == null ? null : ratioOperatorId.trim();
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId == null ? null : childId.trim();
    }

    public String getKindBookmenuId() {
        return kindBookmenuId;
    }

    public void setKindBookmenuId(String kindBookmenuId) {
        this.kindBookmenuId = kindBookmenuId == null ? null : kindBookmenuId.trim();
    }

    public Byte getSpecpriceMode() {
        return specpriceMode;
    }

    public void setSpecpriceMode(Byte specpriceMode) {
        this.specpriceMode = specpriceMode;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId == null ? null : workerId.trim();
    }

    public Byte getIsBackauth() {
        return isBackauth;
    }

    public void setIsBackauth(Byte isBackauth) {
        this.isBackauth = isBackauth;
    }

    public Byte getServiceFeeMode() {
        return serviceFeeMode;
    }

    public void setServiceFeeMode(Byte serviceFeeMode) {
        this.serviceFeeMode = serviceFeeMode;
    }

    public String getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(String serviceFee) {
        this.serviceFee = serviceFee == null ? null : serviceFee.trim();
    }

    public String getOrignId() {
        return orignId;
    }

    public void setOrignId(String orignId) {
        this.orignId = orignId == null ? null : orignId.trim();
    }

    public BigDecimal getAdditionPrice() {
        return additionPrice;
    }

    public void setAdditionPrice(BigDecimal additionPrice) {
        this.additionPrice = additionPrice;
    }

    public Byte getHasAddition() {
        return hasAddition;
    }

    public void setHasAddition(Byte hasAddition) {
        this.hasAddition = hasAddition;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId == null ? null : seatId.trim();
    }

    /**
     * get:扩展字段，不适合做查询字段
     */
    public String getExt() {
        return ext;
    }

    /**
     * set:扩展字段，不适合做查询字段
     */
    public void setExt(String ext) {
        this.ext = ext == null ? null : ext.trim();
    }
}
