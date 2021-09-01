/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.order.center;

import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.RunningStatus;
import com.qlangtech.tis.fullbuild.taskflow.TestParamContext;
import com.qlangtech.tis.test.TISTestCase;
import com.tis.hadoop.rpc.RpcServiceReference;
import junit.framework.Assert;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-03 20:55
 **/
public class TestIndexSwapTaskflowLauncherWithDataXTrigger extends TISTestCase {

    private static final String DATAX_NAME = "baisuitestTestcase";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        DataXJobSubmit.mockGetter = () -> new MockDataXJobSubmit();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        DataXJobSubmit.mockGetter = null;
    }

    public void testDataXProcessTrigger() throws Exception {
        IndexSwapTaskflowLauncher taskflowLauncher = new IndexSwapTaskflowLauncher();
        DefaultChainContext chainContext = createRangeChainContext(FullbuildPhase.FullDump, FullbuildPhase.FullDump);
        ExecuteResult executeResult = taskflowLauncher.startWork(chainContext);
        assertTrue(executeResult.isSuccess());
    }


    public static DefaultChainContext createRangeChainContext(FullbuildPhase start, FullbuildPhase end) throws Exception {
        TestParamContext params = new TestParamContext();

        params.set(IFullBuildContext.KEY_APP_NAME, DATAX_NAME);

        params.set(IExecChainContext.COMPONENT_START, start.getName());
        params.set(IExecChainContext.COMPONENT_END, end.getName());
        final DefaultChainContext chainContext = new DefaultChainContext(params);

        ExecutePhaseRange range = chainContext.getExecutePhaseRange();
        Assert.assertEquals(start, range.getStart());
        Assert.assertEquals(end, range.getEnd());

        chainContext.setAttribute(IExecChainContext.KEY_TASK_ID, TestIndexSwapTaskflowLauncher.TASK_ID);

        chainContext.setMdcParamContext(() -> {
        });
        return chainContext;
    }

    private static class MockDataXJobSubmit extends DataXJobSubmit {
        @Override
        public InstanceType getType() {
            return InstanceType.LOCAL;
        }

        @Override
        public IRemoteJobTrigger createDataXJob(IJoinTaskContext taskContext
                , RpcServiceReference statusRpc, IDataxProcessor dataxProcessor, String dataXfileName) {
            return new IRemoteJobTrigger() {
                @Override
                public void submitJob() {

                }

                @Override
                public RunningStatus getRunningStatus() {
                    return new RunningStatus(1, true, true);
                }
            };
        }
    }

}
