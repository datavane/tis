/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.datax.job;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.coredefine.module.action.RcHpaStatus;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.coredefine.module.action.impl.RcDeployment;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.datax.job.DataXJobWorker.K8SWorkerCptType;
import com.qlangtech.tis.datax.job.DefaultSSERunnable.SubJobLog;
import com.qlangtech.tis.datax.job.DefaultSSERunnable.k8SLaunching;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteStep;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteSteps;
import com.qlangtech.tis.datax.job.SSERunnable.SSEEventType;
import com.qlangtech.tis.plugin.incr.WatchPodLog;
import com.qlangtech.tis.trigger.jst.ILogListener;
import com.qlangtech.tis.trigger.socket.InfoType;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-30 10:01
 **/
public class TestServerLaunchToken extends TestCase {

    public void testAppendLaunchingLine() throws Exception {
        File launchTokenParentDir = new File(FileUtils.getTempDirectory(), "launch");
        TargetResName workerType = new TargetResName("res");
        K8SWorkerCptType workerCptType = K8SWorkerCptType.Server;
        ServerLaunchToken launchToken = ServerLaunchToken.create(launchTokenParentDir, workerType, false, workerCptType);
        Set<String> testLines = Sets.newHashSet();
//        for (int i = 0; i < 10; i++) {
//
//            JSONObject m = SubJobLog.createSubJobLog(InfoType.INFO, TimeFormat.getCurrentTimeStamp(), "line_" + i);
//            testLines.add(SSEEventType.TASK_LOG.getEventType() + SSERunnable.splitChar + JsonUtil.toString(m, false));
//        }

        ExecutorService executors = Executors.newCachedThreadPool();

        CountDownLatch countDown = new CountDownLatch(1);
        final int lineCount = 10;
        executors.execute(() -> {

            String line = null;
            for (int i = 0; i < lineCount; i++) {
                line = "line_" + i;
                testLines.add(line);
                JSONObject m = SubJobLog.createSubJobLog(InfoType.INFO, TimeFormat.getCurrentTimeStamp(), line);

                launchToken.appendLaunchingLine(SSEEventType.TASK_LOG.getEventType()
                        + SSERunnable.splitChar + JsonUtil.toString(m, false));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            countDown.countDown();
        });
        List<ExecuteStep> executeSteps = Lists.newArrayList();
        executeSteps.add(new ExecuteStep(new DefaultSubJobResName("subJobRes"), "desc info"));
        TestDataXJobWorker jobWorker = new TestDataXJobWorker();
        StringWriter writer = new StringWriter();

        DefaultSSERunnable sseRunnable = new DefaultSSERunnable(new PrintWriter(writer), new ExecuteSteps(jobWorker, executeSteps), () -> {

        });

        k8SLaunching k8SLaunching = sseRunnable.hasLaunchingToken(executeSteps, launchToken);

        /**==============================
         * IMPORT
         ==============================*/
        launchToken.addObserver(k8SLaunching);
        //  k8SLaunching.getLogs()

        for (SubJobLog subJobLog : k8SLaunching.getLogs()) {
            //public void writeMessage(InfoType logLevel, long timestamp, String msg)
            sseRunnable.writeHistoryLog(subJobLog);
        }

        countDown.await();
        String writeLines = writer.toString();
        Assert.assertEquals(lineCount, testLines.size());
        for (String line : testLines) {
            Assert.assertTrue(StringUtils.indexOf(writeLines, line) > -1);
        }
    }

    private static class DefaultSubJobResName extends SubJobResName<Object> {
        public DefaultSubJobResName(String name) {
            super(name, (o) -> {
            });
        }

        @Override
        protected String getResourceType() {
            return null;
        }
    }

    private static class TestDataXJobWorker extends DataXJobWorker {
        @Override
        public void relaunch() {

        }

        @Override
        public void relaunch(String podName) {

        }

        @Override
        public List<RcDeployment> getRCDeployments() {
            return null;
        }

        @Override
        public RcHpaStatus getHpaStatus() {
            return null;
        }

        @Override
        public WatchPodLog listPodAndWatchLog(String podName, ILogListener listener) {
            return null;
        }

        @Override
        public void remove() {

        }

        @Override
        protected Optional<JSONObject> launchService(SSERunnable launchProcess) {
            return null;
        }
    }
}
