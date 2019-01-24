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
package com.qlangtech.tis.realtime.yarn.rpc;

import org.apache.hadoop.io.WritableUtils;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MasterJob implements org.apache.hadoop.io.Writable {

    private JobType jobType;

    private boolean stop;

    private String indexName;

    private String uuid;

    private long createTime;

    // 构造函数用于反序列化，不能删除！！！！
    public MasterJob() {
    }

    public MasterJob(JobType jobType, String indexName, String uuid) {
        super();
        this.jobType = jobType;
        this.indexName = indexName;
        this.uuid = uuid;
        this.createTime = ConsumeDataKeeper.getCurrentTimeInSec();
    }

    public String getIndexName() {
        return indexName;
    }

    public JobType getJobType() {
        return this.jobType;
    }

    public String getUUID() {
        return uuid;
    }

    public long getCreateTime() {
        return createTime;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(jobType.getValue());
        out.writeBoolean(this.isStop());
        WritableUtils.writeString(out, indexName);
        WritableUtils.writeString(out, uuid);
        out.writeLong(createTime);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.jobType = JobType.parseJobType(in.readInt());
        this.setStop(in.readBoolean());
        this.indexName = WritableUtils.readString(in);
        this.uuid = WritableUtils.readString(in);
        this.createTime = in.readLong();
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
