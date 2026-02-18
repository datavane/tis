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
package com.qlangtech.tis.exec.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.qlangtech.tis.ajax.AjaxResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.*;
import com.qlangtech.tis.exec.datax.DataXAssembleSvcCompsite;
import com.qlangtech.tis.fullbuild.phasestatus.IFlush2Local;
import com.qlangtech.tis.fullbuild.phasestatus.IFlush2LocalFactory;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BasicPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.IndexBackFlowPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.order.center.IndexSwapTaskflowLauncher;
import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.realtime.yarn.rpc.impl.AdapterStatusUmbilicalProtocol;
import com.qlangtech.tis.rpc.server.IncrStatusUmbilicalProtocolImpl;
import com.tis.hadoop.rpc.ITISRpcService;
import com.tis.hadoop.rpc.RpcServiceReference;
import com.tis.hadoop.rpc.StatusRpcClientFactory.AssembleSvcCompsite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 执行进度可跟踪的执行器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月23日
 */
public abstract class TrackableExecuteInterceptor implements IExecuteInterceptor, ITaskPhaseInfo {

    private static final Logger log = LoggerFactory.getLogger(TrackableExecuteInterceptor.class);


    //    /**
    //     * @param taskid
    //     * @return
    //     * @throws Exception
    //     */
    //    private static PhaseStatusCollection loadPhaseStatusFromLocal(int taskid) {
    //        PhaseStatusCollection result = null;
    //        FullbuildPhase[] phases = FullbuildPhase.values();
    //        try {
    //            File localFile = null;
    //            BasicPhaseStatus phaseStatus;
    //            for (FullbuildPhase phase : phases) {
    //                localFile = BasicPhaseStatus.getFullBuildPhaseLocalFile(taskid, phase);
    //                if (!localFile.exists()) {
    //                    return result;
    //                }
    //                if (result == null) {
    //                    result = new PhaseStatusCollection(taskid, ExecutePhaseRange.fullRange());
    //                }
    //                IFlush2Local flush2Local =
    //                        IFlush2LocalFactory.createNew(IndexSwapTaskflowLauncher.class.getClassLoader(),
    //                        localFile).orElseThrow(() -> new IllegalStateException("flush2Local must be present"));
    //                phaseStatus = flush2Local.loadPhase(); // BasicPhaseStatus.statusWriter.loadPhase(localFile);
    //                switch (phase) {
    //                    case FullDump:
    //                        result.setDumpPhase((DumpPhaseStatus) phaseStatus);
    //                        break;
    //                    case JOIN:
    //                        result.setJoinPhase((JoinPhaseStatus) phaseStatus);
    //                        break;

    /// /                    case BUILD:
    /// /                        result.setBuildPhase((BuildPhaseStatus) phaseStatus);
    /// /                        break;
    /// /                    case IndexBackFlow:
    /// /                        result.setIndexBackFlowPhaseStatus((IndexBackFlowPhaseStatus) phaseStatus);
    //                }
    //            }
    //        } catch (Exception e) {
    //            throw new RuntimeException("taskid:" + taskid, e);
    //        }
    //        return result;
    //    }
    protected RpcServiceReference getDataXExecReporter() {
        IncrStatusUmbilicalProtocolImpl statusServer = IncrStatusUmbilicalProtocolImpl.getInstance();
        IncrStatusUmbilicalProtocol statReceiveSvc = new AdapterStatusUmbilicalProtocol() {
            @Override
            public void reportDumpTableStatus(DumpPhaseStatus.TableDumpStatus tableDumpStatus) {
                statusServer.reportDumpTableStatus(tableDumpStatus);
            }
        };
        AtomicReference<ITISRpcService> ref = new AtomicReference<>();
        ref.set(new DataXAssembleSvcCompsite(statReceiveSvc));
        return new RpcServiceReference(ref, AssembleSvcCompsite.MOCK_PRC, () -> {
        });
    }

    /**
     * 标记当前任务的ID
     *
     * @return
     */
    @Override
    @SuppressWarnings("all")
    public <T extends BasicPhaseStatus<?>> T getPhaseStatus(IExecChainContext execContext, FullbuildPhase phase) {
        PhaseStatusCollection phaseStatusCollection =
                PhaseStatusCollection.getTaskPhaseReference(execContext.getTaskId());
        Objects.requireNonNull(phaseStatusCollection, "phaseStatusCollection can not be null");
        switch (phase) {
            case FullDump:
                return (T) phaseStatusCollection.getDumpPhase();
            case JOIN:
                return (T) phaseStatusCollection.getJoinPhase();
            //                case BUILD:
            //                    return (T) phaseStatusCollection.getBuildPhase();
            //                case IndexBackFlow:
            //                    return (T) phaseStatusCollection.getIndexBackFlowPhaseStatus();
            default:
                throw new IllegalStateException(phase + " is illegal has not any match status");
        }
    }

    @Override
    public final ExecuteResult intercept(ActionInvocation invocation) throws Exception {
        IExecChainContext execChainContext = invocation.getContext();
        ExecuteResult result = null;
        try {
            result = this.execute(execChainContext);
            if (!result.isSuccess()) {
                log.error("phase:" + FullbuildPhase.desc(this.getPhase()) + " faild,reason:" + result.getMessage());
            }
        } catch (Exception e) {
            throw e;
        }
        if (result.isSuccess()) {
            return invocation.invoke();
        } else {
            log.error("full build job is failed");
            return result;
        }
    }

    /**
     * 执行
     *
     * @param execChainContext
     * @return
     * @throws Exception
     */
    protected abstract ExecuteResult execute(IExecChainContext execChainContext) throws Exception;

    /**
     * 创建新的Task执行结果
     */
    public static class IntegerAjaxResult extends AjaxResult<Integer> {
    }

    public static class CreateNewTaskResult {

        private int taskid;

        private Application app;

        public CreateNewTaskResult() {
            super();
        }

        public int getTaskid() {
            return taskid;
        }

        public void setTaskid(int taskid) {
            this.taskid = taskid;
        }

        public void setApp(Application app) {
            this.app = app;
        }

        public Application getApp() {
            return app;
        }
    }

}
