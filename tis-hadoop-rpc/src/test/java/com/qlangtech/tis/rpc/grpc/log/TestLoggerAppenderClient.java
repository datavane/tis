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

package com.qlangtech.tis.rpc.grpc.log;

import com.google.common.collect.Maps;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.rpc.grpc.log.ILoggerAppenderClient.LogLevel;
import com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent;
import com.tis.hadoop.rpc.RpcServiceReference;
import com.tis.hadoop.rpc.StatusRpcClientFactory;
import junit.framework.TestCase;

import java.util.Map;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/21
 */
public class TestLoggerAppenderClient extends TestCase {

    public void testAppendEvent() throws Exception {
        RpcServiceReference rpc = StatusRpcClientFactory.getService(ITISCoordinator.create());

        // StatusRpcClientFactory.AssembleSvcCompsite svc = rpc.get();


//        for (int i = 0; i < 100; i++) {
//           // LoggingEvent.Builder evtBuilder = LoggingEvent.newBuilder();
////            evtBuilder.setLevel(LoggingEvent.Level.INFO);
////            evtBuilder.setBody();
//            Map<String, String> headers = Maps.newHashMap();
//            headers.put(JobParams.KEY_TASK_ID, "123");
//            headers.put(JobParams.KEY_COLLECTION, "unknow");
//            headers.put("logtype", "fullbuild");
//          //  evtBuilder.putAllHeaders(headers);
//            rpc.appendLog(headers , LogLevel.INFO,"hello log event index:" + i);
//            System.out.println("send index:" + i);
//            Thread.sleep(1000);
//        }

    }
}
