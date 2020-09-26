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
package com.qlangtech.tis.collectinfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import com.qlangtech.tis.collectinfo.ReplicaStatisCount.ReplicaNode;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年1月27日 下午5:20:58
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
