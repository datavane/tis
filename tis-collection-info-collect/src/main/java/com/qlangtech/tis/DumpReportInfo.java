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
package com.qlangtech.tis;

import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年2月15日
 */
public class DumpReportInfo {

    private String hostname;

    private Date lastSucTriggerTime;

    public DumpReportInfo(String hostname, Date lastSucTriggerTime) {
        this.hostname = hostname;
        this.lastSucTriggerTime = lastSucTriggerTime;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Date getLastSucTriggerTime() {
        return lastSucTriggerTime;
    }

    public void setLastSucTriggerTime(Date lastSucTriggerTime) {
        this.lastSucTriggerTime = lastSucTriggerTime;
    }
}
