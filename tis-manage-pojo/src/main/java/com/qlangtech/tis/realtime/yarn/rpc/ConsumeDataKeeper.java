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
package com.qlangtech.tis.realtime.yarn.rpc;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ConsumeDataKeeper {

    private final long accumulation;

    private final long createTime;

    private static final long SecInMilli = 1000;

    public ConsumeDataKeeper(long accumulation, long createTime) {
        this.accumulation = accumulation;
        this.createTime = createTime;
    }

    public long getAccumulation() {
        return accumulation;
    }

    public long getCreateTime() {
        return createTime;
    }

    public static long getCurrentTimeInSec() {
        return System.currentTimeMillis() / SecInMilli;
    }
}
