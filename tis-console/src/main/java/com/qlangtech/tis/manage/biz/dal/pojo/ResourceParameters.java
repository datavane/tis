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
public class ResourceParameters implements Serializable {

    private Long rpId;

    private String keyName;

    private String dailyValue;

    private String readyValue;

    private String onlineValue;

    public String value;

    public void setValue(String value) {
        this.value = value;
    }

    private Date gmtCreate;

    private Date gmtUpdate;

    private static final long serialVersionUID = 1L;

    public Long getRpId() {
        return rpId;
    }

    public void setRpId(Long rpId) {
        this.rpId = rpId;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName == null ? null : keyName.trim();
    }

    public String getDailyValue() {
        return dailyValue;
    }

    public void setDailyValue(String dailyValue) {
        this.dailyValue = dailyValue == null ? null : dailyValue.trim();
    }

    public String getReadyValue() {
        return readyValue;
    }

    public void setReadyValue(String readyValue) {
        this.readyValue = readyValue == null ? null : readyValue.trim();
    }

    public String getOnlineValue() {
        return onlineValue;
    }

    public void setOnlineValue(String onlineValue) {
        this.onlineValue = onlineValue == null ? null : onlineValue.trim();
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtUpdate() {
        return gmtUpdate;
    }

    public void setGmtUpdate(Date gmtUpdate) {
        this.gmtUpdate = gmtUpdate;
    }
}
