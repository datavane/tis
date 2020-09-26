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
package com.qlangtech.tis.fullbuild.phasestatus.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 单Groupbuild状态
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public class BuildSharedPhaseStatus extends AbstractChildProcessStatus {

    private static final String NAME = "build";

    private long allBuildSize;

    private long buildReaded;

    private Integer taskid;

    // 分组名称
    private String sharedName;

    // @Override
    // public void write(DataOutput out) throws IOException {
    // WritableUtils.writeString(out, this.sharedName);
    // out.writeLong(this.allBuildSize);
    // out.writeLong(this.buildReaded);
    // out.writeInt(this.taskid);
    // out.writeBoolean(this.isFaild());
    // out.writeBoolean(this.isComplete());
    // out.writeBoolean(this.isWaiting());
    // }
    // @Override
    // public void readFields(DataInput in) throws IOException {
    // this.sharedName = WritableUtils.readString(in);
    // this.allBuildSize = in.readLong();
    // this.buildReaded = in.readLong();
    // this.taskid = in.readInt();
    // this.setFaild(in.readBoolean());
    // this.setComplete(in.readBoolean());
    // this.setWaiting(in.readBoolean());
    // }
    public String getSharedName() {
        return this.sharedName;
    }

    public void setSharedName(String sharedName) {
        this.sharedName = sharedName;
    }

    public Integer getTaskid() {
        return taskid;
    }

    public void setTaskid(Integer taskid) {
        this.taskid = taskid;
    }

    @Override
    public String getAll() {
        return FileUtils.byteCountToDisplaySize(this.allBuildSize);
    }

    @Override
    public String getProcessed() {
        return FileUtils.byteCountToDisplaySize(this.buildReaded);
    }

    @Override
    public int getPercent() {
        return (int) ((this.buildReaded * 1f / this.allBuildSize) * 100);
    }

    @Override
    public final String getName() {
        if (StringUtils.isEmpty(getSharedName())) {
            return StringUtils.EMPTY;
        } else {
            return getSharedName() + "-" + NAME;
        }
    }

    public void setAllBuildSize(long allBuildSize) {
        this.allBuildSize = allBuildSize;
    }

    public void setBuildReaded(long buildReaded) {
        this.buildReaded = buildReaded;
    }

    public long getAllBuildSize() {
        return this.allBuildSize;
    }

    public long getBuildReaded() {
        return this.buildReaded;
    }
}
