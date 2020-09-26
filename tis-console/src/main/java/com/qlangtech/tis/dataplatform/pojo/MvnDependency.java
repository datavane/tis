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
package com.qlangtech.tis.dataplatform.pojo;

import java.io.Serializable;
import java.util.Date;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MvnDependency implements Serializable {

    private Long id;

    private Long nobelAppId;

    private Long tisAppId;

    private String groupId;

    private String artifactId;

    private String version;

    private Date modifyTime;

    private Date createTime;

    private String runtime;

    private String from;

    private String tisAppName;

    public String getTisAppName() {
        return tisAppName;
    }

    public void setTisAppName(String tisAppName) {
        this.tisAppName = tisAppName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    private static final long serialVersionUID = 1L;

    @JSONField(serialize = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JSONField(serialize = false)
    public Long getNobelAppId() {
        return nobelAppId;
    }

    public void setNobelAppId(Long nobelAppId) {
        this.nobelAppId = nobelAppId;
    }

    @JSONField(serialize = false)
    public Long getTisAppId() {
        return tisAppId;
    }

    public void setTisAppId(Long tisAppId) {
        this.tisAppId = tisAppId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId == null ? null : artifactId.trim();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }

    @JSONField(serialize = false)
    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    @JSONField(serialize = false)
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime == null ? null : runtime.trim();
    }
}
