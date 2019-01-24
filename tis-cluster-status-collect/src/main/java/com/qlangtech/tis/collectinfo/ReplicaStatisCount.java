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
package com.qlangtech.tis.collectinfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import com.qlangtech.tis.collectinfo.ReplicaStatisCount.ReplicaNode;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ReplicaStatisCount extends HashMap<ReplicaNode, AtomicLong> {

    private static final long serialVersionUID = 1L;

    private long count;

    public void add(ReplicaNode replica, Long add) {
        if (add == null) {
            return;
        }
        this.count += add;
        AtomicLong count = this.get(replica);
        if (count == null) {
            this.put(replica, new AtomicLong(add));
            return;
        }
        count.addAndGet(add);
    }

    public long getCount() {
        return count;
    }

    /**
     * 取得前后两次取样之间的增量值
     *
     * @param newReport
     * @return
     */
    public long getIncreasement(ReplicaStatisCount newStatisCount) {
        long result = 0;
        AtomicLong preReplicValue = null;
        long increase = 0;
        for (Map.Entry<ReplicaNode, AtomicLong> entry : newStatisCount.entrySet()) {
            preReplicValue = this.get(entry.getKey());
            if (preReplicValue == null || (increase = (entry.getValue().get() - preReplicValue.get())) < 0) {
                result += entry.getValue().get();
            } else {
                result += increase;
            }
        }
        return result;
    }

    public static class ReplicaNode {

        private final Integer groupIndex;

        private final String host;

        public String getHost() {
            return host;
        }

        public ReplicaNode(Integer groupIndex, String host) {
            super();
            this.groupIndex = groupIndex;
            this.host = host;
        }

        @Override
        public int hashCode() {
            return (host + groupIndex).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.hashCode() == obj.hashCode();
        }
    }
}
