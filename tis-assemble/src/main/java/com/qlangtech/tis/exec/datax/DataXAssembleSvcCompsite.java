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

import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.rpc.grpc.log.ILoggerAppenderClient;
import com.tis.hadoop.rpc.StatusRpcClientFactory;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-04 09:19
 **/
public class DataXAssembleSvcCompsite extends StatusRpcClientFactory.AssembleSvcCompsite {

    public DataXAssembleSvcCompsite(IncrStatusUmbilicalProtocol statReceiveSvc) {
        super(statReceiveSvc, new StatusRpcClientFactory.MockLogReporter(), ILoggerAppenderClient.createMock());
    }

    @Override
    public void close() {
    }

    @Override
    public StatusRpcClientFactory.AssembleSvcCompsite unwrap() {
        return this;
    }
}
