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
import com.qlangtech.tis.manage.common.BasicOperationDomainLogger;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public final class ServerGroup extends BasicOperationDomainLogger implements IYuntiPath {

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
