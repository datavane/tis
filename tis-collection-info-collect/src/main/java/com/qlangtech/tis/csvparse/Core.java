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

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-8
 */
public class Core {

    /**
     * solrCoreName
     */
    private final String name;

    private int historyQueryCount;

    private float historyQueryConsumeTime;

    private long historyIndexCount;

    public long getHistoryIndexCount() {
        return historyIndexCount;
    }

    public void setHistoryIndexCount(Long historyIndexCount) {
        this.historyIndexCount = historyIndexCount;
    }

    public int getHistoryQueryCount() {
        return historyQueryCount;
    }

    public float getHistoryQueryConsumeTime() {
        return historyQueryConsumeTime;
    }

    public void setHistoryQueryConsumeTime(float historyQueryConsumeTime) {
        this.historyQueryConsumeTime = historyQueryConsumeTime;
    }

    public void setHistoryQueryCount(int historyQueryCount) {
        this.historyQueryCount = historyQueryCount;
    }

    public Core(String name) {
        super();
        this.name = name;
    }

    // public int getQueryTimelatestWeekAverage() {
    // return queryTimelatestWeekAverage;
    // }
    // 
    // public void setQueryTimelatestWeekAverage(int queryTimelatestWeekAverage)
    // {
    // this.queryTimelatestWeekAverage = queryTimelatestWeekAverage;
    // }
    private float historyCupLoad;

    public float getHistoryCupLoad() {
        return historyCupLoad;
    }

    public void setHistoryCupLoad(float historyCupLoad) {
        this.historyCupLoad = historyCupLoad;
    }

    public String getName() {
        return name;
    }

    public float getAverageLoad() {
        float sumload = 0;
        float serverCount = 0;
        for (IPStatsInfo info : ipDimeStatsInfo.values()) {
            sumload += info.getMaxLoad5Min();
            serverCount++;
        }
        return sumload / serverCount;
    }

    // private int queryCountFromPhrase2;
    // private float queryConsumeTimeFromPhrase2;
    // <ip,IPStatsInfo>
    private final Map<String, IPStatsInfo> ipDimeStatsInfo = new HashMap<String, IPStatsInfo>();

    public void add(IPStatsInfo info) {
        ipDimeStatsInfo.put(info.ipAddress, info);
    }

    public IPStatsInfo getIPStatsInfo(String ip) {
        return ipDimeStatsInfo.get(ip);
    }

    public Collection<IPStatsInfo> getIpDimeStatsInfo() {
        return Collections.unmodifiableCollection(ipDimeStatsInfo.values());
    }

    public String getIpDesc() {
        StringBuffer buffer = new StringBuffer("[");
        for (IPStatsInfo info : ipDimeStatsInfo.values()) {
            buffer.append(info.getIpAddress()).append(",");
        }
        buffer.append("]");
        return buffer.toString();
    }

    public String getIndexVolume() {
        long volume = 0;
        for (IPStatsInfo info : ipDimeStatsInfo.values()) {
            volume += info.getIndexSize();
        }
        return formatVolume(volume);
    }

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.0");

    public static String formatVolume(long volume) {
        if ((volume / 1024) < 1) {
            return String.valueOf(volume) + "K";
        }
        if (volume > (1024 * 1024)) {
            return decimalFormat.format(new Float(volume) / (1024l * 1024l)) + "G";
        }
        return String.valueOf((volume / 1024)) + "M";
    }

    public int getServerSum() {
        return ipDimeStatsInfo.keySet().size();
    }

    // public int getQueryCountFromPhrase2() {
    // return queryCountFromPhrase2;
    // }
    // 
    // public void setQueryCountFromPhrase2(int queryCountFromPhrase2) {
    // this.queryCountFromPhrase2 = queryCountFromPhrase2;
    // }
    // public float getQueryConsumeTimeFromPhrase2() {
    // return queryConsumeTimeFromPhrase2;
    // }
    // public void setQueryConsumeTimeFromPhrase2(float
    // queryConsumeTimeFromPhrase2) {
    // this.queryConsumeTimeFromPhrase2 = queryConsumeTimeFromPhrase2;
    // }
    // 
    // public float getQueryConsumeTimeFromPhrase2() {
    // return this.queryConsumeTimeFromPhrase2;
    // }
    public float getAverageQueryTime() {
        float consumeTimeSum = 0;
        for (IPStatsInfo info : ipDimeStatsInfo.values()) {
            for (CoreGroup group : info.getGroupSet()) {
                consumeTimeSum += group.getQueryConsumeTime();
            }
        }
        return consumeTimeSum / getQueryCount();
    }

    private Set<CoreGroup> getAllGroup() {
        final Set<CoreGroup> groupSet = new HashSet<CoreGroup>();
        for (IPStatsInfo info : ipDimeStatsInfo.values()) {
            groupSet.addAll(info.getGroupSet());
        }
        return groupSet;
    }

    public long getQueryCount() {
        long queryCount = 0;
        for (IPStatsInfo info : ipDimeStatsInfo.values()) {
            for (CoreGroup group : info.getGroupSet()) {
                queryCount += group.getQueryCount();
            }
        }
        return queryCount;
    }

    public long getIndexCount() {
        long indexCount = 0;
        for (CoreGroup group : getAllGroup()) {
            indexCount += group.getIndexNum();
        }
        return indexCount;
    }
}
