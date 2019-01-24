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
package com.qlangtech.tis.realtime.transfer;

import com.qlangtech.tis.realtime.yarn.rpc.ConsumeDataKeeper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TableMultiDataIndexStatus extends OnsListenerStatusKeeper {

    // 单位s
    private static final int TAB_DATA_KEYYPER_EXPIRE_TIME = 30;

    private HashMap<String, LinkedList<ConsumeDataKeeper>> /* 保存了一个时间段 */
    tableConsumeData;

    private String fromAddress;

    private long updateTime;

    // 20s过期时间，收不到report过来的数据就删除该索引对应的uuid
    private static final long EXPIRE_TIME = 20;

    public TableMultiDataIndexStatus() {
        tableConsumeData = new HashMap<>();
    }

    public void put(String tableName, ConsumeDataKeeper consumeDataKeeper) {
        LinkedList<ConsumeDataKeeper> consumeDataKeeperList = tableConsumeData.computeIfAbsent(tableName, k -> new LinkedList<>());
        long createTime = consumeDataKeeper.getCreateTime();
        if (consumeDataKeeperList.size() > 0 && consumeDataKeeperList.getLast().getCreateTime() > createTime) {
            createTime = consumeDataKeeperList.getLast().getCreateTime();
        } else {
            consumeDataKeeperList.add(consumeDataKeeper);
        }
        // 剔除过期的数据
        Iterator<ConsumeDataKeeper> it = consumeDataKeeperList.iterator();
        while (it.hasNext()) {
            ConsumeDataKeeper dataKeeper = it.next();
            if (dataKeeper.getCreateTime() < createTime - TAB_DATA_KEYYPER_EXPIRE_TIME) {
                it.remove();
            } else {
                break;
            }
        }
    // // 剔除过期的数据
    // while (consumeDataKeeperList.size() > 0) {
    // if (consumeDataKeeperList.peek().getCreateTime() < createTime -
    // TAB_DATA_KEYYPER_EXPIRE_TIME) {
    // consumeDataKeeperList.pop();
    // } else {
    // break;
    // }
    // }
    }

    public LinkedList<ConsumeDataKeeper> getConsumeDataKeepList(String tableName) {
        return tableConsumeData.get(tableName);
    }

    /**
     * 取得最后一个更新时间(秒),用于监控上的
     *
     * @return
     */
    public long getLastUpdateSec() {
        long max = 0;
        ConsumeDataKeeper last = null;
        for (LinkedList<ConsumeDataKeeper> stateQueue : tableConsumeData.values()) {
            last = stateQueue.getLast();
            if (max < last.getCreateTime()) {
                max = last.getCreateTime();
            }
        }
        return max;
    }

    public Set<String> getTableNames() {
        return tableConsumeData.keySet();
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isExpire(long currentTime) {
        return (currentTime - EXPIRE_TIME) > updateTime;
    }
}
