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
package com.qlangtech.tis.exec.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.exec.ActionInvocation;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.IExecuteInterceptor;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.RunningStatus;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.order.center.RemoteBuildCenterUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DumpInterceptor implements IExecuteInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DumpInterceptor.class);

    public static final String NAME = "dump";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ExecuteResult intercept(ActionInvocation invocation) throws Exception {
        log.info("component:" + NAME + " start execute");
        IExecChainContext execConetxt = invocation.getContext();
        final String ps = execConetxt.getPartitionTimestamp();
        // ▼▼▼▼ dump task
        final IRemoteJobTrigger dumpTask = RemoteBuildCenterUtils.remoteJobTriggerFactory.createDumpJob(execConetxt.getIndexName(), ps, new TaskContext());
        RunningStatus runningStatus = startDump(dumpTask);
        if (!runningStatus.isSuccess()) {
            log.error("Dump job is faild");
            ExecuteResult faild = ExecuteResult.createFaild();
            return faild;
        }
        // ▲▲▲▲
        log.info("dump phrase success");
        return invocation.invoke();
    }

    // /**
    // * @param execConetxt
    // * @param t
    // * @throws Exception
    // * @throws IOException
    // * @throws FileNotFoundException
    // */
    // private void deleteHdfsHistoryFile(IExecChainContext execConetxt,
    // HiveRemoveHistoryDataTask t) throws Exception, IOException,
    // FileNotFoundException {
    // t.deleteMetadata(execConetxt);
    // t.deleteHdfsFile(execConetxt, false/* isBuildFile */);
    // // 索引数据: /user/admin/search4totalpay/all/0/output/20160104003306
    // t.deleteHdfsFile(execConetxt, true/* isBuildFile */);
    // }
    /**
     * 开始执行dump
     *
     * @param startTime
     * @param dumpTask
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    private // final Date startTime,
    RunningStatus startDump(final IRemoteJobTrigger dumpTask) throws InterruptedException, ExecutionException, TimeoutException {
        final Date startTime = new Date();
        Future<RunningStatus> dumpResult = null;
        try {
            dumpResult = RemoteBuildCenterUtils.taskPool.submit(new Callable<RunningStatus>() {

                @Override
                public RunningStatus call() throws Exception {
                    dumpTask.submitJob();
                    RunningStatus runStatus = null;
                    while (true) {
                        runStatus = dumpTask.getRunningStatus();
                        if (runStatus.isComplete()) {
                            log.info("dump complete");
                            break;
                        }
                        log.info("execute dump,exec past:" + (System.currentTimeMillis() - startTime.getTime()) / 1000 + "s");
                        Thread.sleep(3000);
                    }
                    return runStatus;
                }
            });
            return dumpResult.get(8, TimeUnit.HOURS);
        } catch (TimeoutException e) {
            try {
                dumpResult.cancel(true);
            } catch (Throwable e1) {
            }
            throw e;
        }
    // return runningStatus;
    }

    public static void main(String[] arg) {
        List<Long> list = new ArrayList<Long>();
        list.add(0l);
        list.add(241124l);
        list.add(999l);
        Collections.sort(list, new Comparator<Long>() {

            @Override
            public int compare(Long o1, Long o2) {
                return (int) (o2 - o1);
            }
        });
        for (Long l : list) {
            System.out.println(l);
        }
    }
}
