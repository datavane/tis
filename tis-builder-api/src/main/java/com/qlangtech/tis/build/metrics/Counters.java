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
package com.qlangtech.tis.build.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class Counters {

    public enum Counter {

        // 
        MAP_ALL_RECORDS,
        // 
        MAP_INPUT_RECORDS,
        // 
        MAP_SKIPPED_RECORDS,
        // 
        MAP_INPUT_BYTES,
        // 
        MAP_OUTPUT_RECORDS,
        // 
        MAP_OUTPUT_BYTES,
        DOCMAKE_COMPLETE,
        DOCMAKE_QUEUE_PUT_TIME,
        INDEXMAKE_COMPLETE,
        MERGE_COMPLETE,
        DOCMAKE_FAIL,
        INDEXMAKE_FAIL
    }

    private final Map<Counter, AtomicLong> counters = new HashMap<>();

    public void incrCounter(Counter counter, int val) {
        AtomicLong aval = getCounter(counter);
        aval.addAndGet(val);
    }

    public void setCounterValue(Counter type, long val) {
        AtomicLong aval = getCounter(type);
        aval.set(val);
    }

    public AtomicLong getCounter(Counter type) {
        AtomicLong aval = counters.get(type);
        if (aval == null) {
            synchronized (counters) {
                aval = counters.get(type);
                if (aval == null) {
                    aval = new AtomicLong();
                    counters.put(type, aval);
                }
            }
        }
        return aval;
    }
}
