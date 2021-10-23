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

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月9日
 */
public class MasterJob {

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

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getIndexName() {
        return indexName;
    }

    public JobType getJobType() {
        return this.jobType;
    }

    public boolean isCollectionIncrProcessCommand(String indexName) {
        return (this.getJobType() == JobType.IndexJobRunning) && StringUtils.equals(indexName, this.getIndexName());
    }

    public String getUUID() {
        return uuid;
    }

    public long getCreateTime() {
        return createTime;
    }

    // @Override
    // public void write(DataOutput out) throws IOException {
    // out.writeInt(jobType.getValue());
    // out.writeBoolean(this.isStop());
    // WritableUtils.writeString(out, indexName);
    // WritableUtils.writeString(out, uuid);
    // out.writeLong(createTime);
    // }
    //
    // @Override
    // public void readFields(DataInput in) throws IOException {
    // this.jobType = JobType.parseJobType(in.readInt());
    // this.setStop(in.readBoolean());
    // this.indexName = WritableUtils.readString(in);
    // this.uuid = WritableUtils.readString(in);
    // this.createTime = in.readLong();
    // }
    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
