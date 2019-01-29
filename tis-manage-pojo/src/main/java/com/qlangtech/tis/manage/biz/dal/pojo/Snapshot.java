/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.biz.dal.pojo;

import java.util.Date;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Snapshot {

    private Integer snId;

    private Integer appId;

    // private Integer pid;
    private Long resSchemaId;

    private Long resSolrId;

    private Long resJarId;

    private Long resCorePropId;

    private Long resDsId;

    private Long resApplicationId;

    private Date createTime;

    private Long createUserId;

    private String createUserName;

    private Date updateTime;

    private Integer preSnId;

   

    private String memo;

    private Integer bizId;

   
    public Integer getBizId() {
        return bizId;
    }

    public void setBizId(Integer bizId) {
        this.bizId = bizId;
    }

   
    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo == null ? null : memo.trim();
    }

    public Integer getSnId() {
        return snId;
    }

    public void setSnId(Integer snId) {
        this.snId = snId;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    // public Integer getPid() {
    // return pid;
    // }
    // 
    // public void setPid(Integer pid) {
    // this.pid = pid;
    // }
    public Long getResSchemaId() {
        return resSchemaId;
    }

    public void setResSchemaId(Long resSchemaId) {
        this.resSchemaId = resSchemaId;
    }

    public Long getResSolrId() {
        return resSolrId;
    }

    public void setResSolrId(Long resSolrId) {
        this.resSolrId = resSolrId;
    }

    public Long getResJarId() {
        return resJarId;
    }

    public void setResJarId(Long resJarId) {
        this.resJarId = resJarId;
    }

    public Long getResCorePropId() {
        return resCorePropId;
    }

    public void setResCorePropId(Long resCorePropId) {
        this.resCorePropId = resCorePropId;
    }

    public Long getResDsId() {
        return resDsId;
    }

    public void setResDsId(Long resDsId) {
        this.resDsId = resDsId;
    }

    public Long getResApplicationId() {
        return resApplicationId;
    }

    public void setResApplicationId(Long resApplicationId) {
        this.resApplicationId = resApplicationId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName == null ? null : createUserName.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getPreSnId() {
        return preSnId;
    }

    public void setPreSnId(Integer preSnId) {
        this.preSnId = preSnId;
    }
}
