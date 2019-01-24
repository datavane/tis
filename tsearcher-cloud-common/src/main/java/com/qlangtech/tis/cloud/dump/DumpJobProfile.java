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
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobID;

/*
 * @description
 * @version 1.0.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DumpJobProfile implements Writable {

    String user;

    final DumpJobId jobid;

    String jobFile;

    String url;

    String name;

    String queueName;

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the jobFile
     */
    public String getJobFile() {
        return jobFile;
    }

    /**
     * @param jobFile the jobFile to set
     */
    public void setJobFile(String jobFile) {
        this.jobFile = jobFile;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the queueName
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * @param queueName the queueName to set
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * @return the jobid
     */
    public DumpJobId getJobid() {
        return jobid;
    }

    public DumpJobId getJobID() {
        return jobid;
    }

    public DumpJobProfile(String user, DumpJobId jobid, String jobFile, String url, String name, String queueName) {
        this.user = user;
        this.jobid = jobid;
        this.jobFile = jobFile;
        this.url = url;
        this.name = name;
        this.queueName = queueName;
    }

    public String getJobName() {
        return name;
    }

    public void setJobName(String jobName) {
        this.name = jobName;
    }

    public DumpJobProfile(DumpJobId jobid) {
        this.jobid = jobid;
    }

    /*
	 * (non-Javadoc)
	 * @see org.apache.hadoop.io.Writable#readFields(java.io.DataInput)
	 */
    @Override
    public void readFields(DataInput in) throws IOException {
    // TODO Auto-generated method stub
    }

    /*
	 * (non-Javadoc)
	 * @see org.apache.hadoop.io.Writable#write(java.io.DataOutput)
	 */
    @Override
    public void write(DataOutput out) throws IOException {
    // TODO Auto-generated method stub
    }
}
