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
public class GroupInfo {

    private Integer gid;

    private Integer appId;

    private Short groupCount;

    private Short runEnvironment;

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

    public Short getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(Short groupCount) {
        this.groupCount = groupCount;
    }

    /**
     * get:閽堝搴旂敤绫诲瀷锛�锛氭棩甯�1: daily 2锛氱嚎涓�
     */
    public Short getRunEnvironment() {
        return runEnvironment;
    }

    /**
     * set:閽堝搴旂敤绫诲瀷锛�锛氭棩甯�1: daily 2锛氱嚎涓�
     */
    public void setRunEnvironment(Short runEnvironment) {
        this.runEnvironment = runEnvironment;
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
}
