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
package com.qlangtech.tis.runtime.pojo;

import java.io.Serializable;
import java.util.List;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年2月22日
 */
public class ConfigPush implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UploadResource> uploadResources;

    private String collection;

    // local snapshotid,本地上传id
    private Integer snapshotId;

    // 远端snapshotID
    private Integer remoteSnapshotId;

    // 对接人员
    private String reception;

    // 部门信息
    private Department department;

    public Integer getRemoteSnapshotId() {
        return remoteSnapshotId;
    }

    public void setRemoteSnapshotId(Integer remoteSnapshotId) {
        this.remoteSnapshotId = remoteSnapshotId;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }

    public List<UploadResource> getUploadResources() {
        return uploadResources;
    }

    public void setUploadResources(List<UploadResource> uploadResources) {
        this.uploadResources = uploadResources;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public Integer getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Integer snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getReception() {
        return reception;
    }

    public void setReception(String reception) {
        this.reception = reception;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
