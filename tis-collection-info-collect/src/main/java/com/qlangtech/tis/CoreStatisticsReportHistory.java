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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.qlangtech.tis.collectinfo.api.ICoreStatistics;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年9月11日上午11:47:16
 */
public class CoreStatisticsReportHistory {

    private Integer allCoreCount;

    private final ConcurrentHashMap<Integer, ICoreStatistics> /* appid */
    statis;

    /**
     * @param statis
     */
    public CoreStatisticsReportHistory() {
        super();
        this.statis = new ConcurrentHashMap<>();
    }

    public Set<java.util.Map.Entry<Integer, ICoreStatistics>> entrySet() {
        return statis.entrySet();
    }

    public Set<java.util.Map.Entry<Integer, ICoreStatistics>> entrySetWithOutValidate() {
        return statis.entrySet();
    }

    /**
     */
    private void checkConsist() {
    // int i = 0;
    // while (true) {
    // final int mapSize = this.size();
    // if (allCoreCount != null && (mapSize + 2) >= allCoreCount) {
    // break;
    // }
    // 
    // if (i++ > 5) {
    // throw new IllegalStateException("mapsize:" + mapSize
    // + ",allCoreCount:" + allCoreCount);
    // }
    // 
    // try {
    // Thread.sleep(3000);
    // } catch (InterruptedException e) {
    // 
    // }
    // }
    }

    public ICoreStatistics get(Integer key) {
        checkConsist();
        return statis.get(key);
    }

    public void put(Integer key, ICoreStatistics value) {
        this.statis.put(key, value);
    }

    public void putIfAbsent(Integer key, ICoreStatistics value) {
        this.statis.putIfAbsent(key, value);
    }

    public Integer getAllCoreCount() {
        return allCoreCount;
    }

    public void setAllCoreCount(Integer allCoreCount) {
        this.allCoreCount = allCoreCount;
    }

    public void clear() {
        this.statis.clear();
        allCoreCount = null;
    }
}
