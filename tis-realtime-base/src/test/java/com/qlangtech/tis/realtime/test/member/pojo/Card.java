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
package com.qlangtech.tis.realtime.test.member.pojo;

import com.qlangtech.tis.realtime.transfer.AbstractRowValueGetter;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Card extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:ID
     */
    private String id;

    /**
     * prop:会员卡类型ID
     */
    private String kindCardId;

    /**
     * prop:客户ID
     */
    private String customerId;

    /**
     * prop:会员卡号
     */
    private String code;

    /**
     * prop:会员内部卡号
     */
    private String innerCode;

    /**
     * prop:密码
     */
    private String pwd;

    /**
     * prop:应收金额
     */
    private BigDecimal pay;

    /**
     * prop:发卡时间
     */
    private Long activeDate;

    /**
     * prop:预充值额
     */
    private BigDecimal preFee;

    /**
     * prop:余额
     */
    private BigDecimal balance;

    /**
     * prop:赠送金额
     */
    private BigDecimal giftBalance;

    /**
     * prop:充值实际金额
     */
    private BigDecimal realBalance;

    /**
     * prop:积分
     */
    private BigDecimal degree;

    /**
     * prop:支付累计
     */
    private BigDecimal payAmount;

    /**
     * prop:消费累计
     */
    private BigDecimal consumeAmount;

    /**
     * prop:折扣累计
     */
    private BigDecimal ratioAmount;

    /**
     * prop:0/未使用 1/正常 2/挂失 /3注销
     */
    private Short status;

    /**
     * prop:领用状态: 0/未领用1/已领用
     */
    private Short getStatus;

    /**
     * prop:激活ID:激活时生成的唯一码，在各种操作时记录，表示同一次发卡的操作，在退卡时清空。下次发卡激活时重新生成
     */
    private String activeId;

    /**
     * prop:所属实体ID
     */
    private String entityId;

    /**
     * prop:是否有效
     */
    private Boolean isValid;

    /**
     * prop:记录生成时间
     */
    private Long createTime;

    /**
     * prop:修改时间
     */
    private Long opTime;

    /**
     * prop:版本号
     */
    private Long lastVer;

    /**
     * prop:售卡人ID
     */
    private String sellerId;

    /**
     * prop:卡最后消费时间
     */
    private Long lastConsumeTime;

    /**
     * prop:卡消费次数
     */
    private Integer consumeNum;

    /**
     * prop:扩展业务字段(json格式)
     */
    private String extendFields;

    /**
     * prop:会员卡类型(1:普通卡, 2:特权卡)
     */
    private Boolean kindCardType;

    /**
     * prop:赠送部分的余额
     */
    private BigDecimal giveBalance;

    /**
     * prop:会员卡来源,1-二维火,2-5i
     */
    private Short cardSource;

    /**
     * prop:所属商家会员体系ID
     */
    private String shopMemberSystemId;

    /**
     * prop:是否已转移成体系卡(0:否, 1:是)
     */
    private Boolean transferFlg;

    /**
     * prop:会员卡是否有效 1：有效,0:冻结
     */
    private Byte isEffective;

    /**
     * prop:操作来源
     */
    private String source;

    /**
     * prop:标识活动来源
     */
    private Short activitySource;

    /**
     * prop:标识活动ID
     */
    private String activityId;

    /**
     * prop:冻结余额(单位:元)2019-09-16添加
     */
    private BigDecimal freezeBalance;

    /**
     * prop:冻结赠送部分余额(单位:元)2019-09-16添加
     */
    private BigDecimal freezeGiveBalance;

    private static final long serialVersionUID = 1L;

    /**
     * get:ID
     */
    public String getId() {
        return id;
    }

    /**
     * set:ID
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * get:会员卡类型ID
     */
    public String getKindCardId() {
        return kindCardId;
    }

    /**
     * set:会员卡类型ID
     */
    public void setKindCardId(String kindCardId) {
        this.kindCardId = kindCardId == null ? null : kindCardId.trim();
    }

    /**
     * get:客户ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * set:客户ID
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId == null ? null : customerId.trim();
    }

    /**
     * get:会员卡号
     */
    public String getCode() {
        return code;
    }

    /**
     * set:会员卡号
     */
    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    /**
     * get:会员内部卡号
     */
    public String getInnerCode() {
        return innerCode;
    }

    /**
     * set:会员内部卡号
     */
    public void setInnerCode(String innerCode) {
        this.innerCode = innerCode == null ? null : innerCode.trim();
    }

    /**
     * get:密码
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * set:密码
     */
    public void setPwd(String pwd) {
        this.pwd = pwd == null ? null : pwd.trim();
    }

    /**
     * get:应收金额
     */
    public BigDecimal getPay() {
        return pay;
    }

    /**
     * set:应收金额
     */
    public void setPay(BigDecimal pay) {
        this.pay = pay;
    }

    /**
     * get:发卡时间
     */
    public Long getActiveDate() {
        return activeDate;
    }

    /**
     * set:发卡时间
     */
    public void setActiveDate(Long activeDate) {
        this.activeDate = activeDate;
    }

    /**
     * get:预充值额
     */
    public BigDecimal getPreFee() {
        return preFee;
    }

    /**
     * set:预充值额
     */
    public void setPreFee(BigDecimal preFee) {
        this.preFee = preFee;
    }

    /**
     * get:余额
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * set:余额
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * get:赠送金额
     */
    public BigDecimal getGiftBalance() {
        return giftBalance;
    }

    /**
     * set:赠送金额
     */
    public void setGiftBalance(BigDecimal giftBalance) {
        this.giftBalance = giftBalance;
    }

    /**
     * get:充值实际金额
     */
    public BigDecimal getRealBalance() {
        return realBalance;
    }

    /**
     * set:充值实际金额
     */
    public void setRealBalance(BigDecimal realBalance) {
        this.realBalance = realBalance;
    }

    /**
     * get:积分
     */
    public BigDecimal getDegree() {
        return degree;
    }

    /**
     * set:积分
     */
    public void setDegree(BigDecimal degree) {
        this.degree = degree;
    }

    /**
     * get:支付累计
     */
    public BigDecimal getPayAmount() {
        return payAmount;
    }

    /**
     * set:支付累计
     */
    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    /**
     * get:消费累计
     */
    public BigDecimal getConsumeAmount() {
        return consumeAmount;
    }

    /**
     * set:消费累计
     */
    public void setConsumeAmount(BigDecimal consumeAmount) {
        this.consumeAmount = consumeAmount;
    }

    /**
     * get:折扣累计
     */
    public BigDecimal getRatioAmount() {
        return ratioAmount;
    }

    /**
     * set:折扣累计
     */
    public void setRatioAmount(BigDecimal ratioAmount) {
        this.ratioAmount = ratioAmount;
    }

    /**
     * get:0/未使用 1/正常 2/挂失 /3注销
     */
    public Short getStatus() {
        return status;
    }

    /**
     * set:0/未使用 1/正常 2/挂失 /3注销
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * get:领用状态: 0/未领用1/已领用
     */
    public Short getGetStatus() {
        return getStatus;
    }

    /**
     * set:领用状态: 0/未领用1/已领用
     */
    public void setGetStatus(Short getStatus) {
        this.getStatus = getStatus;
    }

    /**
     * get:激活ID:激活时生成的唯一码，在各种操作时记录，表示同一次发卡的操作，在退卡时清空。下次发卡激活时重新生成
     */
    public String getActiveId() {
        return activeId;
    }

    /**
     * set:激活ID:激活时生成的唯一码，在各种操作时记录，表示同一次发卡的操作，在退卡时清空。下次发卡激活时重新生成
     */
    public void setActiveId(String activeId) {
        this.activeId = activeId == null ? null : activeId.trim();
    }

    /**
     * get:所属实体ID
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * set:所属实体ID
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    /**
     * get:是否有效
     */
    public Boolean getIsValid() {
        return isValid;
    }

    /**
     * set:是否有效
     */
    public void setIsValid(Boolean isValid) {
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
     * get:售卡人ID
     */
    public String getSellerId() {
        return sellerId;
    }

    /**
     * set:售卡人ID
     */
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId == null ? null : sellerId.trim();
    }

    /**
     * get:卡最后消费时间
     */
    public Long getLastConsumeTime() {
        return lastConsumeTime;
    }

    /**
     * set:卡最后消费时间
     */
    public void setLastConsumeTime(Long lastConsumeTime) {
        this.lastConsumeTime = lastConsumeTime;
    }

    /**
     * get:卡消费次数
     */
    public Integer getConsumeNum() {
        return consumeNum;
    }

    /**
     * set:卡消费次数
     */
    public void setConsumeNum(Integer consumeNum) {
        this.consumeNum = consumeNum;
    }

    /**
     * get:扩展业务字段(json格式)
     */
    public String getExtendFields() {
        return extendFields;
    }

    /**
     * set:扩展业务字段(json格式)
     */
    public void setExtendFields(String extendFields) {
        this.extendFields = extendFields == null ? null : extendFields.trim();
    }

    /**
     * get:会员卡类型(1:普通卡, 2:特权卡)
     */
    public Boolean getKindCardType() {
        return kindCardType;
    }

    /**
     * set:会员卡类型(1:普通卡, 2:特权卡)
     */
    public void setKindCardType(Boolean kindCardType) {
        this.kindCardType = kindCardType;
    }

    /**
     * get:赠送部分的余额
     */
    public BigDecimal getGiveBalance() {
        return giveBalance;
    }

    /**
     * set:赠送部分的余额
     */
    public void setGiveBalance(BigDecimal giveBalance) {
        this.giveBalance = giveBalance;
    }

    /**
     * get:会员卡来源,1-二维火,2-5i
     */
    public Short getCardSource() {
        return cardSource;
    }

    /**
     * set:会员卡来源,1-二维火,2-5i
     */
    public void setCardSource(Short cardSource) {
        this.cardSource = cardSource;
    }

    /**
     * get:所属商家会员体系ID
     */
    public String getShopMemberSystemId() {
        return shopMemberSystemId;
    }

    /**
     * set:所属商家会员体系ID
     */
    public void setShopMemberSystemId(String shopMemberSystemId) {
        this.shopMemberSystemId = shopMemberSystemId == null ? null : shopMemberSystemId.trim();
    }

    /**
     * get:是否已转移成体系卡(0:否, 1:是)
     */
    public Boolean getTransferFlg() {
        return transferFlg;
    }

    /**
     * set:是否已转移成体系卡(0:否, 1:是)
     */
    public void setTransferFlg(Boolean transferFlg) {
        this.transferFlg = transferFlg;
    }

    /**
     * get:会员卡是否有效 1：有效,0:冻结
     */
    public Byte getIsEffective() {
        return isEffective;
    }

    /**
     * set:会员卡是否有效 1：有效,0:冻结
     */
    public void setIsEffective(Byte isEffective) {
        this.isEffective = isEffective;
    }

    /**
     * get:操作来源
     */
    public String getSource() {
        return source;
    }

    /**
     * set:操作来源
     */
    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    /**
     * get:标识活动来源
     */
    public Short getActivitySource() {
        return activitySource;
    }

    /**
     * set:标识活动来源
     */
    public void setActivitySource(Short activitySource) {
        this.activitySource = activitySource;
    }

    /**
     * get:标识活动ID
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * set:标识活动ID
     */
    public void setActivityId(String activityId) {
        this.activityId = activityId == null ? null : activityId.trim();
    }

    /**
     * get:冻结余额(单位:元)2019-09-16添加
     */
    public BigDecimal getFreezeBalance() {
        return freezeBalance;
    }

    /**
     * set:冻结余额(单位:元)2019-09-16添加
     */
    public void setFreezeBalance(BigDecimal freezeBalance) {
        this.freezeBalance = freezeBalance;
    }

    /**
     * get:冻结赠送部分余额(单位:元)2019-09-16添加
     */
    public BigDecimal getFreezeGiveBalance() {
        return freezeGiveBalance;
    }

    /**
     * set:冻结赠送部分余额(单位:元)2019-09-16添加
     */
    public void setFreezeGiveBalance(BigDecimal freezeGiveBalance) {
        this.freezeGiveBalance = freezeGiveBalance;
    }
}
