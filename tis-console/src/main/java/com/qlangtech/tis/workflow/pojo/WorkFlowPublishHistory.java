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
package com.qlangtech.tis.workflow.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WorkFlowPublishHistory implements Serializable {

    private Integer id;

    private Date createTime;

    private Integer opUserId;

    private String opUserName;

    private Integer workflowId;

    private String workflowName;

    /**
     * prop:1：发布成功
     *            2：撤销
     *            3：发布中
     */
    private Byte publishState;

    /**
     * prop:1 添加
     *            2 更改
     *            3 删除
     */
    private Byte type;

    private String gitSha1;

    private Boolean inUse;

    private Date opTime;

    private String publishReason;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getOpUserId() {
        return opUserId;
    }

    public void setOpUserId(Integer opUserId) {
        this.opUserId = opUserId;
    }

    public String getOpUserName() {
        return opUserName;
    }

    public void setOpUserName(String opUserName) {
        this.opUserName = opUserName == null ? null : opUserName.trim();
    }

    public Integer getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName == null ? null : workflowName.trim();
    }

    /**
     * get:1：发布成功
     *            2：撤销
     *            3：发布中
     */
    public Byte getPublishState() {
        return publishState;
    }

    /**
     * set:1：发布成功
     *            2：撤销
     *            3：发布中
     */
    public void setPublishState(Byte publishState) {
        this.publishState = publishState;
    }

    /**
     * get:1 添加
     *            2 更改
     *            3 删除
     */
    public Byte getType() {
        return type;
    }

    /**
     * set:1 添加
     *            2 更改
     *            3 删除
     */
    public void setType(Byte type) {
        this.type = type;
    }

    public String getGitSha1() {
        return gitSha1;
    }

    public void setGitSha1(String gitSha1) {
        this.gitSha1 = gitSha1 == null ? null : gitSha1.trim();
    }

    public Boolean getInUse() {
        return inUse;
    }

    public void setInUse(Boolean inUse) {
        this.inUse = inUse;
    }

    public Date getOpTime() {
        return opTime;
    }

    public void setOpTime(Date opTime) {
        this.opTime = opTime;
    }

    public String getPublishReason() {
        return publishReason;
    }

    public void setPublishReason(String publishReason) {
        this.publishReason = publishReason == null ? null : publishReason.trim();
    }
}
