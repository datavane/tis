/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Created by work on 15-1-12.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
