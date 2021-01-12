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
import com.qlangtech.tis.manage.common.BasicOperationDomainLogger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public final class ServerGroup extends BasicOperationDomainLogger {

    private Integer gid;

    private Integer deleteFlag;

    private String appName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Integer getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Integer deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    private Integer appId;

    private Short runtEnvironment;

    private Short groupIndex;

    private Integer publishSnapshotId;

    private Date createTime;

    private Date updateTime;

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public Short getRuntEnvironment() {
        return runtEnvironment;
    }

    public void setRuntEnvironment(Short runtEnvironment) {
        this.runtEnvironment = runtEnvironment;
    }

    public Short getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(Short groupIndex) {
        this.groupIndex = groupIndex;
    }

    public Integer getPublishSnapshotId() {
        return publishSnapshotId;
    }

    public void setPublishSnapshotId(Integer publishSnapshotId) {
        this.publishSnapshotId = publishSnapshotId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    private String yuntiPath;

    public String getYuntiPath() {
        return yuntiPath;
    }

    public void setYuntiPath(String yuntiPath) {
        this.yuntiPath = yuntiPath;
    }
}
