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

package com.qlangtech.tis.manage.common;

import com.google.common.collect.Lists;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.ITaskPhaseInfo;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.RemoteTaskTriggers;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus;
import com.qlangtech.tis.fullbuild.taskflow.DumpTask;
import com.qlangtech.tis.fullbuild.taskflow.JoinTask;
import com.qlangtech.tis.fullbuild.taskflow.TaskAndMilestone;
import com.qlangtech.tis.powerjob.IDAGSessionSpec;
import com.qlangtech.tis.sql.parser.DAGSessionSpec;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-03 14:57
 **/
public class DagTaskUtils {


    public static List<IRemoteTaskTrigger> createTasks(IExecChainContext execChainContext, ITaskPhaseInfo phaseStatus
            , IDAGSessionSpec dagSessionSpec, RemoteTaskTriggers tskTriggers) {
        List<IRemoteTaskTrigger> triggers = Lists.newArrayList();
        for (IRemoteTaskTrigger trigger : tskTriggers.getDumpPhaseTasks()) {
            triggers.add(addDumpTask(execChainContext, phaseStatus, (DAGSessionSpec) dagSessionSpec, trigger));
        }

        for (IRemoteTaskTrigger trigger : tskTriggers.getJoinPhaseTasks()) {
            triggers.add(addJoinTask(execChainContext, phaseStatus, (DAGSessionSpec) dagSessionSpec, trigger));
        }
        return triggers;
    }

    private static IRemoteTaskTrigger addDumpTask(IExecChainContext execChainContext
            , ITaskPhaseInfo phaseStatus, DAGSessionSpec dagSessionSpec
            , IRemoteTaskTrigger jobTrigger) {
        // triggers.add(jobTrigger);
        DumpPhaseStatus dumpStatus = phaseStatus.getPhaseStatus(execChainContext, FullbuildPhase.FullDump);
        dagSessionSpec.put(jobTrigger.getTaskName()
                , new TaskAndMilestone(DumpTask.createDumpTask(jobTrigger, dumpStatus.getTable(jobTrigger.getTaskName()))));
        return jobTrigger;
    }

    private static IRemoteTaskTrigger addJoinTask(IExecChainContext execChainContext, ITaskPhaseInfo phaseStatus
            , DAGSessionSpec dagSessionSpec
            , IRemoteTaskTrigger postTaskTrigger) {
        JoinPhaseStatus joinStatus = phaseStatus.getPhaseStatus(execChainContext, FullbuildPhase.JOIN);
        //triggers.add(postTaskTrigger);
        JoinPhaseStatus.JoinTaskStatus taskStatus = joinStatus.getTaskStatus(postTaskTrigger.getTaskName());
        taskStatus.setWaiting(true);
        dagSessionSpec.put(postTaskTrigger.getTaskName()
                , new TaskAndMilestone(JoinTask.createJoinTask(postTaskTrigger, taskStatus)));
        return postTaskTrigger;
    }

}
