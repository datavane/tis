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
public class WorkFlowBuildPhase implements Serializable {

    private Integer id;

    private Integer workFlowBuildHistoryId;

    /**
     * prop:1??dump 2??join 3??index_build
     */
    private Integer phase;

    private Integer result;

    private Date createTime;

    private Date opTime;

    /**
     * prop:??¼һЩ????????Ϣ??????dump?˶??????
     */
    private String phaseInfo;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWorkFlowBuildHistoryId() {
        return workFlowBuildHistoryId;
    }

    public void setWorkFlowBuildHistoryId(Integer workFlowBuildHistoryId) {
        this.workFlowBuildHistoryId = workFlowBuildHistoryId;
    }

    public Integer getPhase() {
        return phase;
    }

    public void setPhase(Integer phase) {
        this.phase = phase;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getOpTime() {
        return opTime;
    }

    public void setOpTime(Date opTime) {
        this.opTime = opTime;
    }

    /**
     * get:??¼һЩ????????Ϣ??????dump?˶??????
     */
    public String getPhaseInfo() {
        return phaseInfo;
    }

    /**
     * set:??¼һЩ????????Ϣ??????dump?˶??????
     */
    public void setPhaseInfo(String phaseInfo) {
        this.phaseInfo = phaseInfo == null ? null : phaseInfo.trim();
    }
}
