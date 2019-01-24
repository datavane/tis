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
package com.qlangtech.tis.trigger.jst.impl;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTriggerFactory;
import com.qlangtech.tis.fullbuild.indexbuild.RunningStatus;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.cloud.dump.DumpJobStatus;
import com.qlangtech.tis.trigger.jst.AbstractIndexBuildJob;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RemoteIndexBuildJob extends AbstractIndexBuildJob {

    private final IRemoteJobTriggerFactory remoteJobTriggerFactory;

    private final ExecutorService taskPool;

    /**
     * @param processInfo
     * @param group
     *            任务一共有多少组
     * @param remoteJobTriggerFactory
     * @param taskPool
     * @param userName
     */
    public RemoteIndexBuildJob(ImportDataProcessInfo processInfo, int group, IRemoteJobTriggerFactory remoteJobTriggerFactory, ExecutorService taskPool, String userName) {
        super(// taskPool,
        processInfo, // taskPool,
        group, userName);
        this.remoteJobTriggerFactory = remoteJobTriggerFactory;
        this.taskPool = taskPool;
    }

    /**
     * 编译单组分片
     */
    protected BuildResult buildSliceIndex(final String coreName, final String timePoint, final DumpJobStatus status, final String outPath, String serviceName) throws Exception, IOException, InterruptedException {
        Future<BuildResult> fresult = this.taskPool.submit(new Callable<BuildResult>() {

            @Override
            public BuildResult call() throws Exception {
                final IRemoteJobTrigger remoteJobTrigger = remoteJobTriggerFactory.createBuildJob(log, timePoint, serviceName, userName, groupNum, state, new TaskContext());
                remoteJobTrigger.submitJob();
                log.addLog(state, "group:" + coreName + " add submit index build task to build center");
                status.setTimepoint(timePoint);
                status.setUserName(userName);
                status.setRunState(DumpJobStatus.RUNNING);
                // JobStatus s = null;
                int tryCount = 0;
                RunningStatus runStatus = null;
                do {
                    runStatus = remoteJobTrigger.getRunningStatus();
                    log.addLog(state, "trycount" + (tryCount++) + " wait index building,progress:" + runStatus.progress());
                    Thread.sleep(3000);
                } while (!runStatus.isComplete());
                if (StringUtils.isEmpty(outPath)) {
                    throw new IllegalStateException("outPath can not be empty");
                }
                try {
                    BuildResult buildResult = new BuildResult(Integer.parseInt(groupNum), state);
                    buildResult.setSuccess(runStatus.isSuccess());
                    if (runStatus.isSuccess()) {
                        // ContentSummary summary = fileSystem.getContentSummary(new Path(outPath));
                        // 设置目录下所有文件占用的size
                        buildResult.setIndexSize(getSizeHdfsDir(fileSystem, outPath));
                    }
                    return buildResult;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        // 最多6小时
        return fresult.get(7, TimeUnit.HOURS);
    }

    public static long getSizeHdfsDir(FileSystem fs, String path) {
        try {
            ContentSummary summary = fileSystem.getContentSummary(new Path(path));
            return summary.getLength();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
