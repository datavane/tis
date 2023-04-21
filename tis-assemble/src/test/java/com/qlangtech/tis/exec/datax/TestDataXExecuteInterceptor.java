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

package com.qlangtech.tis.exec.datax;

import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.IDataXBatchPost;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.impl.TrackableExecuteInterceptor;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskTrigger;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.order.center.TestIndexSwapTaskflowLauncherWithDataXTrigger;
import com.qlangtech.tis.plugin.PluginStubUtils;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.util.Collections;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-03 14:56
 **/
public class TestDataXExecuteInterceptor extends BasicDataXExecuteInterceptor {


    BatchPostDataXWriter dataxWriter;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.clearMocks();
        System.clearProperty(Config.KEY_DATA_DIR);
        Config.setTestDataDir();
        PluginStubUtils.setTISField();
        dataxCfgDir = new File(Config.getDataDir(), "/cfg_repo/tis_plugin_config/ap/" + AP_NAME + "/dataxCfg");
        FileUtils.forceMkdir(dataxCfgDir);

        mockGenerateCfgs(dataxCfgDir);


        mockDataXReader();
        dataxWriter = new BatchPostDataXWriter(Collections.singletonList(dataCfgFileName));
        DataxWriter.dataxWriterGetter = (name) -> {
            // DataxWriter dataxWriter = mock(name + "DataXWriter", DataxWriter.class);
            return dataxWriter;
        };
    }




    @After
    protected void tearDown() throws Exception {
        //   super.tearDown();
        /**
         * 开始校验
         */
        dataxWriter.verify();

    }

    public void testExecute() throws Exception {


        IRemoteTaskTrigger jobTrigger
                = mock(dataCfgTaskName + "_" + IRemoteTaskTrigger.class.getSimpleName(), IRemoteTaskTrigger.class);
        //

        final String preExecuteTaskName = IDataXBatchPost.getPreExecuteTaskName(new TestSelectedTab(tableName));
        EasyMock.expect(jobTrigger.getTaskName()).andReturn(dataCfgFileName).anyTimes();
        EasyMock.expect(jobTrigger.isAsyn()).andReturn(false).anyTimes();
        jobTrigger.run();
        //   RunningStatus runningStatus = RunningStatus.SUCCESS;
        // EasyMock.expect(jobTrigger.getRunningStatus()).andReturn(runningStatus);

        /**
         * 开始执行
         */
        executeJobTrigger(jobTrigger, true);


        PhaseStatusCollection taskPhaseRef = TrackableExecuteInterceptor.getTaskPhaseReference(testTaskId);
        Assert.assertNotNull(taskPhaseRef);

        DumpPhaseStatus dumpStatus = taskPhaseRef.getDumpPhase();
        Assert.assertNotNull(dumpStatus);

        DumpPhaseStatus.TableDumpStatus tableDumpStatus = dumpStatus.getTable(dataCfgFileName);
        Assert.assertNotNull(tableName + "must exist", tableDumpStatus);
        Assert.assertTrue(tableDumpStatus.isComplete());
        Assert.assertTrue(tableDumpStatus.isSuccess());
        Assert.assertFalse(tableDumpStatus.isWaiting());
        Assert.assertFalse(tableDumpStatus.isFaild());
        Assert.assertEquals(dataCfgFileName, tableDumpStatus.getName());

        DumpPhaseStatus.TableDumpStatus preExecuteStatus = dumpStatus.getTable(preExecuteTaskName);
        Assert.assertNotNull(preExecuteTaskName + "must exist", preExecuteStatus);

        Assert.assertTrue(preExecuteStatus.isComplete());
        Assert.assertTrue(preExecuteStatus.isSuccess());
        Assert.assertFalse(preExecuteStatus.isWaiting());
        Assert.assertFalse(preExecuteStatus.isFaild());
        Assert.assertEquals(preExecuteTaskName, preExecuteStatus.getName());


        JoinPhaseStatus joinStatus = taskPhaseRef.getJoinPhase();
        Assert.assertNotNull(joinStatus);
        JoinPhaseStatus.JoinTaskStatus tableProcess = joinStatus.getTaskStatus(tableName);
        Assert.assertNotNull(tableProcess);

        Assert.assertTrue(tableProcess.isComplete());
        Assert.assertTrue(tableProcess.isSuccess());
        Assert.assertFalse(tableProcess.isWaiting());
        Assert.assertFalse(tableProcess.isFaild());

        Assert.assertEquals(tableName, tableProcess.getName());


    }

    public void testExecuteWithExcpetionWhenSubmitJob() throws Exception {
        IRemoteTaskTrigger jobTrigger = mockDataXExecTaskTrigger();
        //  RunningStatus runningStatus = RunningStatus.SUCCESS;
        // EasyMock.expect(jobTrigger.getRunningStatus()).andReturn(runningStatus);

        try {
            executeJobTrigger(jobTrigger, false);

        } catch (Exception e) {
            fail("shall not throw an exception");
        }
    }

    public void testExecuteWithGetRunningStatusFaild() throws Exception {
        IRemoteTaskTrigger jobTrigger = mockDataXExecTaskTrigger();
        // EasyMock.expectLastCall().andThrow(new RuntimeException("throw a exception"));
        // RunningStatus runningStatus = RunningStatus.FAILD;
        // EasyMock.expect(jobTrigger.getRunningStatus()).andReturn(runningStatus).anyTimes();


        executeJobTrigger(jobTrigger, false);

    }



    private void executeJobTrigger(IRemoteTaskTrigger jobTrigger, boolean finalSuccess) throws Exception {

        TrackableExecuteInterceptor.initialTaskPhase(testTaskId);

        DataXJobSubmit.mockGetter = () -> new TestIndexSwapTaskflowLauncherWithDataXTrigger.MockDataXJobSubmit(jobTrigger);

        DataXExecuteInterceptor executeInterceptor = new DataXExecuteInterceptor();

        MockDataxProcessor dataxProcessor = new MockDataxProcessor();

        IExecChainContext execChainContext = mockExecChainContext(dataxProcessor);

        this.replay();
        /**
         * ================================================================
         * 开始执行
         * ================================================================
         */
        ExecuteResult executeResult = executeInterceptor.execute(execChainContext);


        executeInterceptor.getPhaseStatus(execChainContext, FullbuildPhase.FullDump);

        assertEquals("execute must be " + (finalSuccess ? "success" : "faild"), finalSuccess, executeResult.isSuccess());
        this.verifyAll();
    }


}
