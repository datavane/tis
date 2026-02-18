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

import com.google.common.collect.Sets;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.coredefine.module.action.TriggerBuildResult;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.impl.TrackableExecuteInterceptor;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.job.common.JobCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * DataX 执行器
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-27 15:42
 **/
public class DataXExecuteInterceptor extends TrackableExecuteInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(DataXExecuteInterceptor.class);


    @Override
    protected ExecuteResult execute(IExecChainContext execChainContext) throws Exception {

        IDataxProcessor appSource = execChainContext.getProcessor();

        DataXJobSubmit.InstanceType expectDataXJobSumit = getDataXTriggerType();
        Optional<DataXJobSubmit> jobSubmit = DataXJobSubmit.getDataXJobSubmit(execChainContext, expectDataXJobSumit);
        // 如果分布式worker ready的话
        if (jobSubmit.isEmpty()) {
            throw new IllegalStateException("can not find expect jobSubmit by type:" + expectDataXJobSumit);
        }
        DataXName pipelineName = new DataXName(appSource.identityValue(), appSource.getResType());

        DataXJobSubmit submit = jobSubmit.get();
        // PhaseStatusCollection lastHistory = execChainContext.loadPhaseStatusFromLatest();

        TriggerBuildResult triggerBuildResult = submit.triggerJob(execChainContext, pipelineName
                //     , Optional.ofNullable(lastHistory)
        );
        execChainContext.setAttribute(JobCommon.KEY_TASK_ID, Objects.requireNonNull(triggerBuildResult.taskid,
                "taskid can not be null"));

        return ExecuteResult.createSuccess();
    }


    private DataXJobSubmit.InstanceType getDataXTriggerType() {
        return DataXJobSubmit.getDataXTriggerType();
    }


    @Override
    public Set<FullbuildPhase> getPhase() {
        return Sets.newHashSet(FullbuildPhase.FullDump, FullbuildPhase.JOIN);
    }

}
