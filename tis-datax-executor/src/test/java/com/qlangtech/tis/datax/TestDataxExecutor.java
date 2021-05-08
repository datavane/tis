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

import com.alibaba.datax.core.util.container.JarLoader;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.test.TISTestCase;
import com.tis.hadoop.rpc.ITISRpcService;
import com.tis.hadoop.rpc.RpcServiceReference;
import com.tis.hadoop.rpc.StatusRpcClient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-20 14:12
 */
public class TestDataxExecutor extends TISTestCase implements IExecutorContext{



    public void testDataxJobLaunch() throws Exception {


        //TisZkClient zkClient = new TisZkClient(Config.getZKHost(), 60000);

        AtomicReference<ITISRpcService> ref = new AtomicReference<>();
        ref.set(StatusRpcClient.AssembleSvcCompsite.MOCK_PRC);
        RpcServiceReference statusRpc = new RpcServiceReference(ref);

        //RpcServiceReference statusRpc = StatusRpcClient.getService(zkClient);

        final JarLoader uberClassLoader = new JarLoader(new String[]{"."}) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                return TIS.get().getPluginManager().uberClassLoader.findClass(name);
            }
        };

        DataxExecutor executor = new DataxExecutor(statusRpc, uberClassLoader);
        final String jobName = "customer_order_relation_1.json";
        Path path = Paths.get("/opt/data/tis/cfg_repo/tis_plugin_config/ap/baisuitest/dataxCfg/" + jobName);
// tring dataxName, Integer jobId, String jobName, String jobPath
        Integer jobId = 1;
        executor.startWork(dataXName, jobId, jobName, path.toString());
    }
}
