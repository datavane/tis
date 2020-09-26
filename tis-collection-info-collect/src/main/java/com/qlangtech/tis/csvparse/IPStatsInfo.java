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
package com.qlangtech.tis.csvparse;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-8
 */
public class IPStatsInfo {

    final String ipAddress;

    private String hostName;

    private final String coreName;

    public IPStatsInfo(String ipAddress, String coreName) {
        super();
        this.ipAddress = StringUtils.trimToEmpty(ipAddress);
        this.coreName = coreName;
    }

    private final Set<CoreGroup> groupSet = new HashSet<CoreGroup>();

    public Set<CoreGroup> getGroupSet() {
        return groupSet;
    }

    /**
     * @return
     */
    public long getIndexSize() {
        long result = 0;
        for (CoreGroup group : groupSet) {
            result += group.getIndexSize();
        }
        return result;
    }

    public void addGroup(CoreGroup group) {
        groupSet.add(group);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     */
    private float maxLoad5Min;

    public float getMaxLoad5Min() {
        return maxLoad5Min;
    }

    public void setMaxLoad5Min(float maxLoad5Min) {
        this.maxLoad5Min = maxLoad5Min;
    }

    private Date date;

    private long used;

    private long available;

    private String uedPercent;

    public String getIpAddress() {
        return ipAddress;
    }

    public String getCoreName() {
        return coreName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getAvailable() {
        return available;
    }

    public void setAvailable(long available) {
        this.available = available;
    }

    public String getUedPercent() {
        return uedPercent;
    }

    public void setUedPercent(String uedPercent) {
        this.uedPercent = uedPercent;
    }
}
