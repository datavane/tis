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

import java.util.Map;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/17
 */
public interface ILoggerAppenderClient {

    public static ILoggerAppenderClient createMock() {
        return new ILoggerAppenderClient() {
            @Override
            public void append(Map<String, String> headers, LogLevel level, String body) {

            }

//            @Override
//            public void append(LoggingEvent event) {
//
//            }
        };
//        final String target = ZkUtils.getFirstChildValue(ITISCoordinator.create(), ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PATH
//                , true);
//
//        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
//
//        final LogAppenderGrpc.LogAppenderBlockingStub logAppenderBlockingStub = LogAppenderGrpc.newBlockingStub(channel);
//
//        return new ILoggerAppenderClient() {
//            @Override
//            public void append(LoggingEvent event) {
//                logAppenderBlockingStub.append(Objects.requireNonNull(event));
//            }
//        };
    }

    //    map<string /* key */, string> headers = 1;
//    string body = 2;
//    Level level = 3;
//    INFO = 0;
//    WARNING = 1;
//    ERROR = 2;
    void append(Map<String, String> headers, LogLevel level, String body);

    public enum LogLevel {
        INFO, WARNING, ERROR
    }

    //public void append(LoggingEvent event);
}
