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
package com.qlangtech.tis.csvparse;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
