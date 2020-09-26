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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by work on 15-1-12.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ExportReportStatus {

    private String serviceName;

    private Date shouldTriggerTime;

    private Map<Integer, List<DumpReportInfo>> hostsStatus = new HashMap<Integer, List<DumpReportInfo>>();

    public ExportReportStatus(String serviceName) {
        this.serviceName = serviceName;
    }

    public ExportReportStatus(String serviceName, Date shouldTriggerTime, Map<Integer, List<DumpReportInfo>> hostsStatus) {
        this.serviceName = serviceName;
        this.shouldTriggerTime = shouldTriggerTime;
        this.hostsStatus = hostsStatus;
    }

    public int getHostsSize() {
        int size = 0;
        for (Map.Entry<Integer, List<DumpReportInfo>> entry : hostsStatus.entrySet()) {
            if (entry.getKey() != null) {
                size += entry.getValue().size();
            }
        }
        return size;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Date getShouldTriggerTime() {
        return shouldTriggerTime;
    }

    public void setShouldTriggerTime(Date shouldTriggerTime) {
        this.shouldTriggerTime = shouldTriggerTime;
    }

    public Map<Integer, List<DumpReportInfo>> getHostsStatus() {
        return hostsStatus;
    }

    public void setHostsStatus(Map<Integer, List<DumpReportInfo>> hostsStatus) {
        this.hostsStatus = hostsStatus;
    }
}
