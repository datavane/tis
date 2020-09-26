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
public class ApplicationApply extends BasicOperationDomainLogger {

    private Integer appId;

    private String projectName;

    private String recept;

    private String manager;

    private Date createTime;

    private Date updateTime;

    private boolean isAutoDeploy;

    /**
     * prop:memo
     */
    private String memo;

    /**
     * prop:1:yunti 2:database
     */
    private Byte fullSourceType;

    /**
     * prop:dpt_id
     */
    private Integer dptId;

    /**
     * prop:dpt_name
     */
    private String dptName;

    /**
     * prop:pv
     */
    private Integer pv = 0;

    /**
     * prop:uv
     */
    private Integer uv = 0;

    /**
     * prop:describe the online servers
     */
    private String onlineServers;

    /**
     * prop:publish date time
     */
    private Date publishDate;

    /**
     * prop:has pass the daily test?
     */
    private boolean isPassedTest;

    /**
     * prop:1:common 2:realtime
     */
    private Byte incrType;

    public Byte getIncrType() {
        return incrType;
    }

    public void setIncrType(Byte incrType) {
        this.incrType = incrType;
    }

    /**
     * prop:has pass the daily test?
     */
    private Byte status;

    /**
     * prop:user id
     */
    private String createUsrId;

    public String getCreateUsrId() {
        return createUsrId;
    }

    public void setCreateUsrId(String createUsrId) {
        this.createUsrId = createUsrId;
    }

    private static final long serialVersionUID = 1L;

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName == null ? null : projectName.trim();
    }

    public String getRecept() {
        return recept;
    }

    public void setRecept(String recept) {
        this.recept = recept == null ? null : recept.trim();
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager == null ? null : manager.trim();
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

    public boolean getIsAutoDeploy() {
        return isAutoDeploy;
    }

    public void setIsAutoDeploy(boolean isAutoDeploy) {
        this.isAutoDeploy = isAutoDeploy;
    }

    /**
     * get:dpt_id
     */
    public Integer getDptId() {
        return dptId;
    }

    /**
     * set:dpt_id
     */
    public void setDptId(Integer dptId) {
        this.dptId = dptId;
    }

    /**
     * get:dpt_name
     */
    public String getDptName() {
        return dptName;
    }

    /**
     * set:dpt_name
     */
    public void setDptName(String dptName) {
        this.dptName = dptName == null ? null : dptName.trim();
    }

    /**
     * get:pv
     */
    public Integer getPv() {
        return pv;
    }

    /**
     * set:pv
     */
    public void setPv(Integer pv) {
        this.pv = pv;
    }

    /**
     * get:uv
     */
    public Integer getUv() {
        return uv;
    }

    /**
     * set:uv
     */
    public void setUv(Integer uv) {
        this.uv = uv;
    }

    /**
     * get:describe the online servers
     */
    public String getOnlineServers() {
        return onlineServers;
    }

    /**
     * set:describe the online servers
     */
    public void setOnlineServers(String onlineServers) {
        this.onlineServers = onlineServers == null ? null : onlineServers.trim();
    }

    /**
     * get:publish date time
     */
    public Date getPublishDate() {
        return publishDate;
    }

    /**
     * set:publish date time
     */
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    /**
     * get:has pass the daily test?
     */
    public boolean getIsPassedTest() {
        return isPassedTest;
    }

    /**
     * set:has pass the daily test?
     */
    public void setIsPassedTest(boolean isPassedTest) {
        this.isPassedTest = isPassedTest;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Byte getFullSourceType() {
        return fullSourceType;
    }

    public void setFullSourceType(Byte fullSourceType) {
        this.fullSourceType = fullSourceType;
    }
}
