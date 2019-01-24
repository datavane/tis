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

/*
 * Created by Qinjiu(Qinjiu@2dfire.com) on 2017/2/8.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class OnsListenerStatusKeeper {

    private int bufferQueueRemainingCapacity;

    private int bufferQueueUsedSize;

    private int consumeErrorCount;

    private int ignoreRowsCount;

    private String uuid;

    private long tis30sAvgRT;

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
