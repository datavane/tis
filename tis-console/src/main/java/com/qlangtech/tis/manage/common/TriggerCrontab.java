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
package com.qlangtech.tis.manage.common;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-1-25
 */
public class TriggerCrontab {

    private String appName;

    private Integer appId;

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

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }
}
