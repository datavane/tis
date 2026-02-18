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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.ITaskPhaseInfo;
import com.qlangtech.tis.fullbuild.indexbuild.RemoteTaskTriggers;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;

import com.qlangtech.tis.manage.ISolrAppSource;
import com.qlangtech.tis.manage.impl.DataFlowAppSource;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.ds.DefaultTab;
import com.qlangtech.tis.powerjob.IDAGSessionSpec;
import com.qlangtech.tis.sql.parser.DAGSessionSpec;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import com.tis.hadoop.rpc.RpcServiceReference;
import org.jvnet.hudson.reactor.Task;

import java.util.*;

/**
 * 工作流dump流程
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年9月2日
 */
public class WorkflowDumpAndJoinInterceptor extends TrackableExecuteInterceptor {

    // 超时时间
    public static final int TIME_OUT_HOURS = 8;


    @Override
    protected ExecuteResult execute(IExecChainContext execChainContext) throws Exception {
        // TisZkClient zkClient = execChainContext.getZkClient();

        WorkFlow wf = new WorkFlow();
        wf.setId(null);
        wf.setName(execChainContext.getWorkflowName());
        ;

        IDataxProcessor dataxProc = execChainContext.getProcessor();// DataxProcessor.load(null, StoreResourceType.DataFlow, wf.getName());
        IDataxWriter writer = dataxProc.getWriter(null, true);
        DataFlowAppSource appRule = new DataFlowAppSource(wf, writer);
        //Map<String, TISReactor.TaskAndMilestone> taskMap = Maps.newHashMap();
        RpcServiceReference dataXExecReporter = getDataXExecReporter();
        DataXJobSubmit.InstanceType triggerType = DataXJobSubmit.getDataXTriggerType();
        Optional<DataXJobSubmit> jobSubmit = DataXJobSubmit.getDataXJobSubmit(execChainContext, triggerType);
        if (!jobSubmit.isPresent()) {
            throw new IllegalStateException("jobSumit can not be empty,triggerType:" + triggerType);
        }

        DataXJobSubmit submit = jobSubmit.get();
       // final DataXAssembleSvcCompsite svcCompsite = dataXExecReporter.get();

        final DataXCfgGenerator.GenerateCfgs cfgFileNames
                = dataxProc.getDataxCfgFileNames(null, Optional.empty());

        final ExecuteResult faildResult = appRule.getProcessDataResults(execChainContext, new ISolrAppSource.ISingleTableDumpFactory() {
                    @Override
                    public void createSingleTableDump(RemoteTaskTriggers tskTrigger, DependencyNode dump, boolean hasValidTableDump, String pt
                            , ITISCoordinator zkClient, IExecChainContext execChainContext, DumpPhaseStatus dumpPhaseStatus, ITaskPhaseInfo taskPhaseInfo
                            , IDAGSessionSpec dagSessionSpec) {

                        DAGSessionSpec.buildTaskTriggers(
                                execChainContext, dataxProc, submit, dataXExecReporter, new DefaultTab(dump.getName()), dump.getId(), dagSessionSpec, cfgFileNames);

                    }
                },
                new ISolrAppSource.IDataProcessFeedback() {
                    @Override
                    public PhaseStatusCollection getPhaseStatusSet(IExecChainContext execContext) {
                        return PhaseStatusCollection.getTaskPhaseReference(execContext.getTaskId());
                    }

                    @Override
                    public void reportDumpTableStatusError(IExecChainContext execContext, DumpPhaseStatus dumpPhase, Task task) {

//                        TISReactor.TaskImpl tsk = (TISReactor.TaskImpl) task;
//                        DumpPhaseStatus.TableDumpStatus dumpStatus = dumpPhase.getTable(tsk.getIdentityName());
//                        dataXExecReporter.reportDumpJobStatus(true, true
//                                , false, dumpPhase.getTaskId(), tsk.getDisplayName(), dumpStatus.getReadRows(), dumpStatus.getAllRows());
                    }
                }, this
        );

        if (faildResult != null) {
            return faildResult;
        } else {
            final List<Map<String, String>> summary = new ArrayList<>();
            return ExecuteResult.createSuccess().setMessage(JSON.toJSONString(summary, true));
        }
    }

    @Override
    public Set<FullbuildPhase> getPhase() {
        return Sets.newHashSet(FullbuildPhase.FullDump, FullbuildPhase.JOIN);
    }
}
