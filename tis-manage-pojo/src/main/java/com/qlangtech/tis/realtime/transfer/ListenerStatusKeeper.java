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
package com.qlangtech.tis.realtime.transfer;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class ListenerStatusKeeper {

    private int bufferQueueRemainingCapacity;

    private int bufferQueueUsedSize;

    private int consumeErrorCount;

    private int ignoreRowsCount;

    private String uuid;

    // 增量任务是否暂停
    private boolean incrProcessPaused;

    private long tis30sAvgRT;

    public boolean isIncrProcessPaused() {
        return incrProcessPaused;
    }

    public void setIncrProcessPaused(boolean incrProcessPaused) {
        this.incrProcessPaused = incrProcessPaused;
    }

    public int getBufferQueueRemainingCapacity() {
        return bufferQueueRemainingCapacity;
    }

    public void setBufferQueueRemainingCapacity(int bufferQueueRemainingCapacity) {
        this.bufferQueueRemainingCapacity = bufferQueueRemainingCapacity;
    }

    public int getBufferQueueUsedSize() {
        return bufferQueueUsedSize;
    }

    public void setBufferQueueUsedSize(int bufferQueueUsedSize) {
        this.bufferQueueUsedSize = bufferQueueUsedSize;
    }

    public int getConsumeErrorCount() {
        return consumeErrorCount;
    }

    public void setConsumeErrorCount(int consumeErrorCount) {
        this.consumeErrorCount = consumeErrorCount;
    }

    public int getIgnoreRowsCount() {
        return ignoreRowsCount;
    }

    public void setIgnoreRowsCount(int ignoreRowsCount) {
        this.ignoreRowsCount = ignoreRowsCount;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public long getTis30sAvgRT() {
        return tis30sAvgRT;
    }

    public void setTis30sAvgRT(long tis30sAvgRT) {
        this.tis30sAvgRT = tis30sAvgRT;
    }
}
