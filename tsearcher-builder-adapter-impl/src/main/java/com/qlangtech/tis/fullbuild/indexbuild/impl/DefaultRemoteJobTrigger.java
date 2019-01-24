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
package com.qlangtech.tis.fullbuild.indexbuild.impl;

import java.io.IOException;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.RunningStatus;
import com.taobao.terminator.build.client.JobClient;
import com.taobao.terminator.build.job.JobConf;
import com.taobao.terminator.build.job.RunningJob;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public final class DefaultRemoteJobTrigger implements IRemoteJobTrigger {

    private final JobConf jobConf;

    private RunningJob rj;

    public static final String KEY_TASK_JAR_TRANSFER = "task.jar.transfer";

    /**
     * @param jobClient
     */
    public DefaultRemoteJobTrigger(JobConf jobConf) {
        super();
        if (jobConf.getStrings(KEY_TASK_JAR_TRANSFER) == null) {
            throw new IllegalStateException("has not set parameter:" + KEY_TASK_JAR_TRANSFER);
        }
        this.jobConf = jobConf;
    }

    @Override
    public void submitJob() {
        try {
            JobConf conf = new JobConf(false);
            TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
            conf.set("jobtracker.rpcserver", config.getJobRpcserver());
            conf.set("jobtracker.transserver", config.getJobTransserver());
            // 远程连接客户端端创建
            JobClient jobClient = new JobClient(conf);
            rj = jobClient.summitJob(jobConf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取得运行状态
     */
    public RunningStatus getRunningStatus() {
        try {
            if (rj == null) {
                throw new IllegalStateException("running job can not be null");
            }
            return new RunningStatus(rj.progress(), rj.isComplete(), rj.isSuccessful());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
