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

package com.qlangtech.tis.datax;

import com.alibaba.datax.common.util.Configuration;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.PluginStubUtils;
import com.qlangtech.tis.test.TISTestCase;
import com.tis.hadoop.rpc.ITISRpcService;
import com.tis.hadoop.rpc.RpcServiceReference;
import com.tis.hadoop.rpc.StatusRpcClient;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-03 16:25
 **/
public abstract class BasicDataXExecutorTestCase extends TISTestCase implements IExecutorContext {
    protected DataxExecutor executor;
    protected RpcServiceReference statusRpc;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.clearProperty(Config.DEFAULT_DATA_DIR);
        setDataDir();
        PluginStubUtils.setTISField();
        AtomicReference<ITISRpcService> ref = new AtomicReference<>();
        ref.set(StatusRpcClient.AssembleSvcCompsite.MOCK_PRC);
        statusRpc = new RpcServiceReference(ref);

        executor = createExecutor();
    }

    protected void setDataDir() {
        Config.setDataDir(Config.DEFAULT_DATA_DIR);
    }

    protected DataxExecutor createExecutor() {
        return new DataxExecutor(statusRpc, DataXJobSubmit.InstanceType.LOCAL, 300) {
            @Override
            protected void startEngine(Configuration configuration, Integer jobId, String jobName) {
                //  make skip the ex
            }
        };
    }

}
