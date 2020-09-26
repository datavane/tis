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
package com.qlangtech.tis.manage.biz.dal.pojo;

import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
