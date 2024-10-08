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

package com.tis.hadoop.rpc;

import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.realtime.yarn.rpc.PingResult;
import com.tis.hadoop.rpc.StatusRpcClientFactory.AssembleSvcCompsite;
import io.grpc.StatusRuntimeException;
import junit.framework.TestCase;

import static com.tis.hadoop.rpc.StatusRpcClientFactory.AssembleSvcCompsite.MOCK_PRC;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-04-15 14:08
 **/
public class TestStatusRpcClientFactory extends TestCase {
    public void testLoadPhaseStatusFromLatest() throws Exception {
        RpcServiceReference ref = StatusRpcClientFactory.getService(ITISCoordinator.create());


        // PhaseStatusCollection statusCollection = svc.statReceiveSvc.loadPhaseStatusFromLatest(18);
        // Assert.assertNotNull("statusCollection can not be null", statusCollection);
        while (true) {
            try {
                //  AssembleSvcCompsite svc = ref.get();
                PingResult ping = ref.ping();

//                if (MOCK_PRC == svc) {
//                    System.out.println(" mock ping");
//                } else {
                System.out.println("success ping:" + ping.getValue());
                //}
            } catch (StatusRuntimeException e) {
                System.out.println("faild ping:" + e.getMessage());
                ref.reConnect();
            }

            Thread.sleep(2000);
        }
        //  Thread.sleep(999999);
    }
}
