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

import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildSharedPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus.TableDumpStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus.JoinTaskStatus;
import com.qlangtech.tis.realtime.yarn.rpc.LaunchReportInfo;
import com.qlangtech.tis.realtime.yarn.rpc.MasterJob;
import com.qlangtech.tis.realtime.yarn.rpc.PingResult;
import com.qlangtech.tis.realtime.yarn.rpc.UpdateCounterMap;
import com.qlangtech.tis.rpc.grpc.log.ILoggerAppenderClient.LogLevel;
import com.qlangtech.tis.trigger.jst.ILogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-03 11:16
 */
public class RpcServiceReference implements IPartialGrpcServiceFacade {
    private final AtomicReference<ITISRpcService> ref;
    private final Runnable connect;
    private final ITISRpcService mockSvc;
    private static final Logger logger = LoggerFactory.getLogger(RpcServiceReference.class);

    public RpcServiceReference(AtomicReference<ITISRpcService> ref, ITISRpcService mockSvc, Runnable connect) {
        this.ref = ref;
        this.mockSvc = mockSvc;
        this.connect = connect;
    }

    public AtomicReference<ITISRpcService> getRef() {
        return ref;
    }

    @Override
    public void close() {
        this.get().close();
    }

    /**
     * when throw an error of  io.grpc.StatusRuntimeException,then shall execute reConnect()
     * // wait 点在 @see StatusRpcClientFactory.startConnect2RPC() ：
     */
    public void reConnect() {
        synchronized (connect) {
            //  this.connect.run();
            // 唤醒 Runnable，连接继续
            this.connect.notifyAll();
        }
    }

    /**
     * default instance is type of AssembleSvcCompsite
     *
     * @param <T>
     * @return
     */
    private <T extends ITISRpcService> T get() {
        T t = (T) ref.get();
        return t;
    }

    public void reportDumpJobStatus(boolean faild, boolean complete, boolean waiting, Integer taskId, String jobName,
                                    int readRows, int allRows) {
        RpcServiceReference svc = this;
        DumpPhaseStatus.TableDumpStatus dumpStatus = new DumpPhaseStatus.TableDumpStatus(jobName,
                Objects.requireNonNull(taskId, "taskId can not be null"));
        dumpStatus.setFaild(faild);
        dumpStatus.setComplete(complete);
        dumpStatus.setWaiting(waiting);
        dumpStatus.setReadRows(readRows);
        dumpStatus.setAllRows(allRows);
        svc.reportDumpTableStatus(dumpStatus);
    }

    @Override
    public <STREAM_OBSERVER> STREAM_OBSERVER registerMonitorEvent(ILogListener logListener) {
        //        try {
        //            return this.get().registerMonitorEvent(logListener);
        //        } catch (GrpcConnectionException e) {
        //            this.reConnect();
        //            return mockSvc.registerMonitorEvent(logListener);
        //        }
        return invokeRpc((svc) -> {
            return svc.registerMonitorEvent(logListener);
        });
    }

    private <T> T invokeRpc(Function<ITISRpcService, T> func) {
        try {
            return func.apply(Objects.requireNonNull(this.get(),
                    "instance of " + ITISRpcService.class.getSimpleName() + " can not be null"));
        } catch (GrpcConnectionException e) {
            logger.warn(e.getMessage(), e);
            this.reConnect();
            return func.apply(this.mockSvc); //mockSvc.registerMonitorEvent(logListener);
        }
    }

    @Override
    public <PHASE_STATUS_COLLECTION> Iterator<PHASE_STATUS_COLLECTION> buildPhraseStatus(Integer taskid) {
        return invokeRpc((svc) -> {
            return svc.buildPhraseStatus(taskid);
        });
    }
    //IPartialGrpcServiceFacade impl below

    @Override
    public PingResult ping() {
        return invokeRpc((svc) -> {
            return svc.ping();
        });
    }

    @Override
    public MasterJob reportStatus(UpdateCounterMap upateCounter) {
        return invokeRpc((svc) -> {
            return svc.reportStatus(upateCounter);
        });
    }

    @Override
    public void nodeLaunchReport(LaunchReportInfo launchReportInfo) {
        invokeRpc((svc) -> {
            svc.nodeLaunchReport(launchReportInfo);
            return null;
        });
    }

    @Override
    public void reportDumpTableStatus(TableDumpStatus tableDumpStatus) {
        invokeRpc((svc) -> {
            svc.reportDumpTableStatus(tableDumpStatus);
            return null;
        });
    }

    @Override
    public void reportJoinStatus(Integer taskId, JoinTaskStatus joinTaskStatus) {
        invokeRpc((svc) -> {
            svc.reportJoinStatus(taskId, joinTaskStatus);
            return null;
        });
    }

    @Override
    public void reportBuildIndexStatus(BuildSharedPhaseStatus buildStatus) {
        invokeRpc((svc) -> {
            svc.reportBuildIndexStatus(buildStatus);
            return null;
        });
    }

    @Override
    public void initSynJob(PhaseStatusCollection buildStatus) {
        invokeRpc((svc) -> {
            svc.initSynJob(buildStatus);
            return null;
        });
    }

    @Override
    public PhaseStatusCollection loadPhaseStatusFromLatest(Integer taskId) {
        return invokeRpc((svc) -> {
            return svc.loadPhaseStatusFromLatest(taskId);
        });
    }

    @Override
    public void appendLog(LogLevel level, Integer taskId, Optional<String> appName, String message) {
        invokeRpc((svc) -> {
            svc.appendLog(level, taskId, appName, message);
            return null;
        });
    }

    @Override
    public void append(Map<String, String> headers, LogLevel level, String body) {
        invokeRpc((svc) -> {
            svc.append(headers, level, body);
            return null;
        });
    }
    //    @Override
    //    public void append(Map<String, String> headers, LogLevel level, String body) {
    //        get().append(headers, level, body);
    //    }
}
