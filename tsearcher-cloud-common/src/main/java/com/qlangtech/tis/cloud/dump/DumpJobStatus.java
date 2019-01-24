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
 * @description
 * @version 1.0.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DumpJobStatus implements Writable, Cloneable {

    // 业务Core名 serviceName-group
    private String coreName;

    // 任务提交人
    private String userName;

    // 时间点
    private String timepoint;

    // 完成百分比
    private float dumpProgressPercent;

    // 执行条数
    protected long excuteCount;

    // 开始时间
    protected long startTime;

    // 结束时间
    protected long endTime;

    // 全部Dump数目
    protected long alldumpCount;

    // dump种类，远程还是本地
    protected String dumpType;

    private static final String DEFAULT_MSG = "SUC";

    // 错误信息
    protected String failureInfo = DEFAULT_MSG;

    private int runState;

    /**
     * @return the dumpType
     */
    public String getDumpType() {
        return dumpType;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the timepoint
     */
    public String getTimepoint() {
        return timepoint;
    }

    /**
     * @param timepoint the timepoint to set
     */
    public void setTimepoint(String timepoint) {
        this.timepoint = timepoint;
    }

    /**
     * @param failureInfo the failureInfo to set
     */
    public void setFailureInfo(String failureInfo) {
        this.failureInfo = failureInfo;
    }

    /**
     * @param dumpType the dumpType to set
     */
    public void setDumpType(String dumpType) {
        this.dumpType = dumpType;
    }

    /**
     * @return the runstates
     */
    public static String[] getRunstates() {
        return runStates;
    }

    /**
     * @return the excuteCount
     */
    public long getExcuteCount() {
        return excuteCount;
    }

    /**
     * @param excuteCount the excuteCount to set
     */
    public void setExcuteCount(long excuteCount) {
        this.excuteCount = excuteCount;
    }

    /**
     * @return the coreName
     */
    public String getCoreName() {
        return coreName;
    }

    /**
     * @param coreName the coreName to set
     */
    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the alldumpCount
     */
    public long getAlldumpCount() {
        return alldumpCount;
    }

    /**
     * @param alldumpCount the alldumpCount to set
     */
    public void setAlldumpCount(long alldumpCount) {
        this.alldumpCount = alldumpCount;
    }

    /**
     * @param runState the runState to set
     */
    public void setRunState(int runState) {
        this.runState = runState;
    }

    private DumpJobId jobid;

    /**
     * @param jobid the jobid to set
     */
    public void setDumpJobID(DumpJobId jobid) {
        this.jobid = jobid;
    }

    public static final int RUNNING = 1;

    public static final int FAILED = 2;

    public static final int SUCCEEDED = 3;

    public static final int PREP = 4;

    public static final int KILLED = 5;

    private static final String UNKNOWN = "UNKNOWN";

    private static final String[] runStates = { UNKNOWN, "RUNNING", "SUCCEEDED", "FAILED", "PREP", "KILLED" };

    public static String getJobRunState(int state) {
        if (state < 1 || state >= runStates.length) {
            return UNKNOWN;
        }
        return runStates[state];
    }

    public synchronized float getDumpProgressPercent() {
        return dumpProgressPercent;
    }

    public synchronized void setDumpProgressPercent(float percent) {
        this.dumpProgressPercent = percent;
    }

    public DumpJobId getDumpJobID() {
        return jobid;
    }

    public synchronized int getRunState() {
        return runState;
    }

    public synchronized String getFailureInfo() {
        return this.failureInfo;
    }

    @Override
    public synchronized void readFields(DataInput in) throws IOException {
        coreName = Text.readString(in);
        userName = Text.readString(in);
        timepoint = Text.readString(in);
        dumpProgressPercent = in.readFloat();
        excuteCount = in.readLong();
        startTime = in.readLong();
        endTime = in.readLong();
        runState = in.readInt();
        alldumpCount = in.readLong();
        dumpType = Text.readString(in);
        failureInfo = Text.readString(in);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Text.writeString(out, coreName);
        Text.writeString(out, userName);
        Text.writeString(out, timepoint);
        out.writeFloat(dumpProgressPercent);
        out.writeLong(excuteCount);
        out.writeLong(startTime);
        out.writeLong(endTime);
        out.writeInt(runState);
        out.writeLong(alldumpCount);
        Text.writeString(out, dumpType);
        Text.writeString(out, failureInfo);
    }
}
