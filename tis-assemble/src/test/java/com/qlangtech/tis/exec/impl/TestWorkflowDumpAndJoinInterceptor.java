package com.qlangtech.tis.exec.impl;

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

import com.google.common.collect.Maps;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.exec.ExecChainContextUtils;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.fullbuild.indexbuild.RemoteTaskTriggers;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.taskflow.AdapterTask;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.sql.parser.TabPartitions;
import com.qlangtech.tis.test.TISTestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-02-06 11:09
 **/
public class TestWorkflowDumpAndJoinInterceptor extends TISTestCase {

    @Test
    public void testExecute() throws Exception {
        System.setProperty(DataxUtils.EXEC_TIMESTAMP, String.valueOf(TimeFormat.getCurrentTimeStamp()));
        // String wfName = "tttt71";
        String wfName = "texx";

        int taskId = 999;
        final boolean isDryRun = false;


        IDataxProcessor processor = DataxProcessor.load(null, StoreResourceType.DataFlow, wfName);
        PhaseStatusCollection.initialTaskPhase(taskId);
        WorkflowDumpAndJoinInterceptor interceptor = new WorkflowDumpAndJoinInterceptor();

        IExecChainContext execContext = this.mock("execContext", IExecChainContext.class);

        ArgumentCaptor<RemoteTaskTriggers> taskTriggersCapture = ArgumentCaptor.forClass(RemoteTaskTriggers.class);
        Mockito.doNothing().when(execContext).setTskTriggers(taskTriggersCapture.capture());

        Mockito.when(execContext.getTskTriggers()).thenAnswer(inv -> taskTriggersCapture.getValue());


        // Dry Run
        Mockito.when(execContext.isDryRun()).thenReturn(isDryRun);

        Mockito.when(execContext.getExecutePhaseRange())
                .thenReturn(new ExecutePhaseRange(FullbuildPhase.FullDump, FullbuildPhase.JOIN));
        Mockito.when(execContext.getProcessor()).thenReturn(processor);

        Map<IDumpTable, ITabPartition> tabPs = Maps.newHashMap();
        Mockito.when(execContext.getAttribute(ExecChainContextUtils.PARTITION_DATA_PARAMS)).thenReturn(new TabPartitions(tabPs));
        Mockito.when(execContext.getAttribute(AdapterTask.KEY_TASK_WORK_STATUS)).thenReturn(new HashMap<String, Boolean>());


       // execContext.rebindLoggingMDCParams();

        Mockito.when(execContext.getSpecifiedLocalLoggerPath()).thenReturn(null);
        Mockito.when(execContext.isDisableGrpcRemoteServerConnect()).thenReturn(true);


        Mockito.when(execContext.getAttribute(Mockito.eq(DataXJobSubmit.KEY_DATAX_READERS), Mockito.any()))
                .thenReturn(processor.getReaders(null));

        //   execContext.getWorkflowName()
        Mockito.when(execContext.hasIndexName()).thenReturn(false);
        Mockito.when(execContext.getZkClient()).thenReturn(ITISCoordinator.create());
        Mockito.when(execContext.getTaskId()).thenReturn(taskId);
        Mockito.when(execContext.getWorkflowName()).thenReturn(wfName);
        Mockito.when(execContext.getPartitionTimestampWithMillis()).thenReturn(TimeFormat.getCurrentTimeStamp());
//        Mockito.when(execContext.loadPhaseStatusFromLatest())
//                .thenReturn(TrackableExecuteInterceptor.getTaskPhaseReference(taskId));


        ExecuteResult result = interceptor.execute(execContext);
        Assert.assertTrue(result.isSuccess());

        Mockito.verify(execContext, Mockito.times(2)).getSpecifiedLocalLoggerPath();
        Mockito.verify(execContext, Mockito.times(2)).isDisableGrpcRemoteServerConnect();
        Mockito.verify(execContext).getWorkflowName();
    }

}
