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
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Customer extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:ID
     */
    private String id;

    /**
     * prop:手机
     */
    private String mobile;

    /**
     * prop:电话
     */
    private String phone;

    /**
     * prop:0/未知;1/男;2/女
     */
    private Short sex;

    /**
     * prop:生日
     */
    private Date birthday;

    /**
     * prop:身份证号
     */
    private String certificate;

    /**
     * prop:拼写
     */
    private String spell;

    /**
     * prop:会员姓名
     */
    private String name;

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
     * prop:国家ID
     */
    private String contryId;

    /**
     * prop:国家代码
     */
    private String contryCode;

    /**
     * prop:消费累计
     */
    private BigDecimal consumeAmount;

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
     * prop:国家代码
     */
    private String countryCode;

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
     * get:手机
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * set:手机
     */
    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    /**
     * get:电话
     */
    public String getPhone() {
        return phone;
    }

    /**
     * set:电话
     */
    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    /**
     * get:0/未知;1/男;2/女
     */
    public Short getSex() {
        return sex;
    }

    /**
     * set:0/未知;1/男;2/女
     */
    public void setSex(Short sex) {
        this.sex = sex;
    }

    /**
     * get:生日
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * set:生日
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     * get:身份证号
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * set:身份证号
     */
    public void setCertificate(String certificate) {
        this.certificate = certificate == null ? null : certificate.trim();
    }

    /**
     * get:拼写
     */
    public String getSpell() {
        return spell;
    }

    /**
     * set:拼写
     */
    public void setSpell(String spell) {
        this.spell = spell == null ? null : spell.trim();
    }

    /**
     * get:会员姓名
     */
    public String getName() {
        return name;
    }

    /**
     * set:会员姓名
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
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
     * get:国家ID
     */
    public String getContryId() {
        return contryId;
    }

    /**
     * set:国家ID
     */
    public void setContryId(String contryId) {
        this.contryId = contryId == null ? null : contryId.trim();
    }

    /**
     * get:国家代码
     */
    public String getContryCode() {
        return contryCode;
    }

    /**
     * set:国家代码
     */
    public void setContryCode(String contryCode) {
        this.contryCode = contryCode == null ? null : contryCode.trim();
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
     * get:国家代码
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * set:国家代码
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode == null ? null : countryCode.trim();
    }
}
