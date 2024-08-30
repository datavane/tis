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

import com.qlangtech.tis.rpc.grpc.log.appender.LogAppenderGrpc;
import com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent;
import com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent.Level;
import io.grpc.ManagedChannel;

import java.util.Map;
import java.util.Objects;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/17
 */
public class DefaultLoggerAppenderClient implements ILoggerAppenderClient {
    final LogAppenderGrpc.LogAppenderBlockingStub logAppenderBlockingStub;

    public DefaultLoggerAppenderClient(ManagedChannel channel) {
        this.logAppenderBlockingStub = LogAppenderGrpc.newBlockingStub(channel);
    }

    @Override
    public void append(Map<String, String> headers, LogLevel level, String message) {

        LoggingEvent.Builder evtBuilder = LoggingEvent.newBuilder();
        switch (level) {
            case INFO: {
                evtBuilder.setLevel(Level.INFO);
                break;
            }
            case ERROR: {
                evtBuilder.setLevel(Level.ERROR);
                break;
            }
            case WARNING: {
                evtBuilder.setLevel(Level.WARNING);
                break;
            }
            default:
                throw new IllegalStateException("illegal level type:" + level);
        }


        evtBuilder.setBody(message);
        evtBuilder.putAllHeaders(headers);
        this.logAppenderBlockingStub.append(Objects.requireNonNull(evtBuilder.build(), "param event can not be null"));
    }

//    @Override
//    public void append(LoggingEvent event) {
//        this.logAppenderBlockingStub.append(Objects.requireNonNull(event, "param event can not be null"));
//    }
}
