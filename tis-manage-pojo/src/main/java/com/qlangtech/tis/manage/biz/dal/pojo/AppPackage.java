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
public class AppPackage {

    private Integer pid;

    private Integer appId;

    private String uploadUser;

    private Short testStatus;

    private Date lastTestTime;

    private Integer lastTerUserId;

    private String lastTestUser;

    private Integer successSnapshotId;

    private Date createTime;

    private Date updateTime;

    private Short runtEnvironment;

    private Integer deleteFlag;

    public Integer getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Integer deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public Integer getPid() {
        return this.pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getAppId() {
        return this.appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getUploadUser() {
        return this.uploadUser;
    }

    public void setUploadUser(String uploadUser) {
        this.uploadUser = (uploadUser == null ? null : uploadUser.trim());
    }

    public Short getTestStatus() {
        return this.testStatus;
    }

    public void setTestStatus(Short testStatus) {
        this.testStatus = testStatus;
    }

    public Date getLastTestTime() {
        return this.lastTestTime;
    }

    public void setLastTestTime(Date lastTestTime) {
        this.lastTestTime = lastTestTime;
    }

    public Integer getLastTerUserId() {
        return this.lastTerUserId;
    }

    public void setLastTerUserId(Integer lastTerUserId) {
        this.lastTerUserId = lastTerUserId;
    }

    public String getLastTestUser() {
        return this.lastTestUser;
    }

    public void setLastTestUser(String lastTestUser) {
        this.lastTestUser = (lastTestUser == null ? null : lastTestUser.trim());
    }

    public Integer getSuccessSnapshotId() {
        return this.successSnapshotId;
    }

    public void setSuccessSnapshotId(Integer successSnapshotId) {
        this.successSnapshotId = successSnapshotId;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Short getRuntEnvironment() {
        return this.runtEnvironment;
    }

    public void setRuntEnvironment(Short runtEnvironment) {
        this.runtEnvironment = runtEnvironment;
    }
}
