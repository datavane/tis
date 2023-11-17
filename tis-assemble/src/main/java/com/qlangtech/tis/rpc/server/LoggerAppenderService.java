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

package com.qlangtech.tis.rpc.server;

import com.qlangtech.tis.flume.TisIncrLoggerSink;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.rpc.grpc.log.appender.LogAppenderGrpc;
import com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent;
import com.qlangtech.tis.rpc.grpc.log.common.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/17
 */
public class LoggerAppenderService extends LogAppenderGrpc.LogAppenderImplBase {

    private static final Logger logger = LoggerFactory.getLogger(TisIncrLoggerSink.class);
    private static final Map<String, Logger> loggers = new HashMap<String, Logger>();
    public Logger getLogger(String name) {
        Logger logger = loggers.get(name);
        if (logger == null) {
            logger = LoggerFactory.getLogger(name);
            loggers.put(name, logger);
        }
        return logger;
    }
    @Override
    public void append(LoggingEvent request, StreamObserver<Empty> responseObserver) {
      //  super.append(request, responseObserver);

        Map<String, String> headers = request.getHeadersMap();

        String execGroup = headers.get("incr_exec_group");
        String application = headers.get("application");
        String host = headers.get("host");
        MDC.put("application", application);
        MDC.put("group", execGroup);
        MDC.put("host", host);
        MDC.put(JobParams.KEY_COLLECTION, headers.get(JobParams.KEY_COLLECTION));
        Object taskid = headers.get(JobParams.KEY_TASK_ID);
        if (taskid != null) {
            MDC.put(JobParams.KEY_TASK_ID, String.valueOf(taskid));
        }
        String logtype = headers.get("logtype");
        if (StringUtils.isEmpty(logtype)) {
            logger.info(request.getBody());
        } else {
            getLogger(logtype).info(request.getBody());
        }


        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
