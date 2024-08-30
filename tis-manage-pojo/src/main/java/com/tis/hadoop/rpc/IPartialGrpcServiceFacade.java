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

import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.rpc.grpc.log.ILoggerAppenderClient.LogLevel;
import com.qlangtech.tis.trigger.jst.ILogListener;

import java.util.Map;
import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-08-29 21:30
 **/
public interface IPartialGrpcServiceFacade extends IncrStatusUmbilicalProtocol {
    void appendLog(LogLevel level, Integer taskId, Optional<String> appName, String message);

    void append(Map<String, String> headers, LogLevel level, String body);

    void close();

    /**
     * StreamObserver<PMonotorTarget>
     *
     * @param logListener
     * @param <STREAM_OBSERVER>
     * @return
     */
    <STREAM_OBSERVER> STREAM_OBSERVER registerMonitorEvent(ILogListener logListener);

    /**
     * @param taskid
     * @return com.qlangtech.tis.rpc.grpc.log.stream.PPhaseStatusCollection
     */
    public <PHASE_STATUS_COLLECTION> java.util.Iterator<PHASE_STATUS_COLLECTION> buildPhraseStatus(Integer taskid);
}
