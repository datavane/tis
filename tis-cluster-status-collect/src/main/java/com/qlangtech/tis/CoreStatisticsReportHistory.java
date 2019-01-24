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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.qlangtech.tis.collectinfo.api.ICoreStatistics;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
