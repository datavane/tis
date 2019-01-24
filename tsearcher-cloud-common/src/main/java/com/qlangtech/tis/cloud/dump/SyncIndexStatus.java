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
package com.qlangtech.tis.cloud.dump;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/*
 * The Class SyncIndexStatus.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SyncIndexStatus implements Writable, Cloneable {

    private String host;

    private long lastSuccess;

    private long lastFail;

    public SyncIndexStatus() {
    }

    public SyncIndexStatus(String host) {
        super();
        this.host = host;
    }

    public long getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(long lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    public long getLastFail() {
        return lastFail;
    }

    public void setLastFail(long lastFail) {
        this.lastFail = lastFail;
    }

    public String getHost() {
        return this.host;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        host = Text.readString(in);
        lastSuccess = in.readLong();
        lastFail = in.readLong();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Text.writeString(out, host);
        out.writeLong(lastSuccess);
        out.writeLong(lastFail);
    }

    @Override
    public String toString() {
        return "SyncIndexStatus [host=" + host + ", lastSuccess=" + lastSuccess + ", lastFail=" + lastFail + "]";
    }
}
