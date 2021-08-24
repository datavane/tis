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

package com.qlangtech.tis.exec.datax;

import com.qlangtech.tis.datax.IDataxGlobalCfg;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.RunningStatus;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.PluginStubUtils;
import com.qlangtech.tis.test.TISTestCase;
import com.tis.hadoop.rpc.RpcServiceReference;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-03 14:56
 **/
public class TestDataXExecuteInterceptor extends TISTestCase {
    private static final String AP_NAME = "testDataxProcessor";
    private static File dataxCfgDir;
    private static final String dataCfgFileName = "customer_order_relation_1.json";


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearMocks();
        System.clearProperty(Config.KEY_DATA_DIR);
        Config.setTestDataDir();
        PluginStubUtils.setTISField();
        dataxCfgDir = new File(Config.getDataDir(), "/cfg_repo/tis_plugin_config/ap/" + AP_NAME + "/dataxCfg");
        FileUtils.forceMkdir(dataxCfgDir);

        try (InputStream res = TestDataXExecuteInterceptor.class.getResourceAsStream(dataCfgFileName)) {
            Objects.requireNonNull(res, dataCfgFileName + " can not be null");
            FileUtils.copyInputStreamToFile(res, new File(dataxCfgDir, dataCfgFileName));
        }
    }

    public void testExecute() throws Exception {


        IRemoteJobTrigger jobTrigger = mock("remoteJobTrigger", IRemoteJobTrigger.class);
        //
        EasyMock.expect(jobTrigger.isAsyn()).andReturn(false);
        jobTrigger.submitJob();
        RunningStatus runningStatus = RunningStatus.SUCCESS;
        EasyMock.expect(jobTrigger.getRunningStatus()).andReturn(runningStatus);


        DataXExecuteInterceptor executeInterceptor = new DataXExecuteInterceptor() {
            @Override
            protected IRemoteJobTrigger createDataXJob(IExecChainContext execChainContext, RpcServiceReference statusRpc, DataxProcessor appSource, String fileName) {
                assertEquals(dataCfgFileName, fileName);
                return jobTrigger;
            }
        };

        IExecChainContext execChainContext = mock("execChainContext", IExecChainContext.class);

        MockDataxProcessor dataxProcessor = new MockDataxProcessor();

        EasyMock.expect(execChainContext.getAppSource()).andReturn(dataxProcessor);

        this.replay();
        ExecuteResult executeResult = executeInterceptor.execute(execChainContext);

        assertTrue(executeResult.isSuccess());
        this.verifyAll();
    }


    private static class MockDataxProcessor extends DataxProcessor {
        @Override
        public Application buildApp() {
            return null;
        }

        @Override
        public String identityValue() {
            return AP_NAME;
        }


        @Override
        public IDataxGlobalCfg getDataXGlobalCfg() {
            return null;
        }
    }

}
