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

import java.io.Serializable;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class BizFuncAuthority implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer bfId;

    private Integer dptId;

    private String funcId;

    private Integer appId;

    private Date createTime;

    private Date updateTime;

    private String isDeleted;

    private String dptName;

    /**
     * prop:authority functin descript
     */
    private String funcDesc;

    /**
     * prop:application name
     */
    private String appName;

    public Integer getBfId() {
        return bfId;
    }

    public void setBfId(Integer bfId) {
        this.bfId = bfId;
    }

    public Integer getDptId() {
        return dptId;
    }

    public void setDptId(Integer dptId) {
        this.dptId = dptId;
    }

    public String getFuncId() {
        return funcId;
    }

    public void setFuncId(String funcId) {
        this.funcId = funcId == null ? null : funcId.trim();
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
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

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted == null ? null : isDeleted.trim();
    }

    public String getDptName() {
        return dptName;
    }

    public void setDptName(String dptName) {
        this.dptName = dptName == null ? null : dptName.trim();
    }

    /**
     * get:authority functin descript
     */
    public String getFuncDesc() {
        return funcDesc;
    }

    /**
     * set:authority functin descript
     */
    public void setFuncDesc(String funcDesc) {
        this.funcDesc = funcDesc == null ? null : funcDesc.trim();
    }

    /**
     * get:application name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * set:application name
     */
    public void setAppName(String appName) {
        this.appName = appName == null ? null : appName.trim();
    }

    // <result column="fjob_id" property="fjobId" jdbcType="INTEGER" />
    // <result column="fjob_type" property="fjobType" jdbcType="INTEGER" />
    // <result column="fcrontab" property="fcrontab" jdbcType="VARCHAR" />
    // 
    // <result column="ijob_id" property="ijobId" jdbcType="INTEGER" />
    // <result column="ijob_type" property="ijobType" jdbcType="INTEGER" />
    // <result column="icrontab" property="icrontab" jdbcType="VARCHAR" />
    private Integer fjobId;

    private Integer fjobType;

    private String fcrontab;

    // 该定时任务是否已经停止？
    private boolean fstop;

    private Integer ijobId;

    private Integer ijobType;

    private String icrontab;

    // 该定时任务是否已经停止？
    private boolean istop;

    public boolean isFstop() {
        return fstop;
    }

    public void setFstop(boolean fstop) {
        this.fstop = fstop;
    }

    public boolean isIstop() {
        return istop;
    }

    public void setIstop(boolean istop) {
        this.istop = istop;
    }

    public Integer getFjobId() {
        return fjobId;
    }

    public void setFjobId(Integer fjobId) {
        this.fjobId = fjobId;
    }

    public Integer getFjobType() {
        return fjobType;
    }

    public void setFjobType(Integer fjobType) {
        this.fjobType = fjobType;
    }

    public String getFcrontab() {
        return fcrontab;
    }

    public void setFcrontab(String fcrontab) {
        this.fcrontab = fcrontab;
    }

    public Integer getIjobId() {
        return ijobId;
    }

    public void setIjobId(Integer ijobId) {
        this.ijobId = ijobId;
    }

    public Integer getIjobType() {
        return ijobType;
    }

    public void setIjobType(Integer ijobType) {
        this.ijobType = ijobType;
    }

    public String getIcrontab() {
        return icrontab;
    }

    public void setIcrontab(String icrontab) {
        this.icrontab = icrontab;
    }
}
