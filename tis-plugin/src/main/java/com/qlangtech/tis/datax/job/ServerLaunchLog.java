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

package com.qlangtech.tis.datax.job;

import com.qlangtech.tis.config.k8s.IReplicaScalaLog;
import com.qlangtech.tis.datax.job.DefaultSSERunnable.SubJobLog;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteStep;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-01-03 08:28
 **/
public class ServerLaunchLog implements IReplicaScalaLog {
    private final boolean launchingTokenExist;
    private List<SubJobMilestone> milestones;
    private SubJobLog[] logs;
    private List<ExecuteStep> executeSteps;

    public ServerLaunchLog(boolean launchingTokenExist) {
        this.launchingTokenExist = launchingTokenExist;
    }

    public boolean isFaild() {
        for (SubJobMilestone subJobMilestone : getMilestones()) {
            if (subJobMilestone.isFaild()) {
                return true;
            }
        }
        return false;
    }

    public void setMilestones(List<SubJobMilestone> milestones) {
        this.milestones = milestones;
    }

    public void setExecuteSteps(List<ExecuteStep> executeSteps) {
        this.executeSteps = executeSteps;
    }

    public void setLogs(SubJobLog[] logs) {
        int size = 0;
        for (int i = 0; i < logs.length; i++) {
            if (logs[i] != null) {
                size++;
            }
        }

        SubJobLog[] newLogs = new SubJobLog[size];
        int index = 0;
        for (int i = 0; i < logs.length; i++) {
            if (logs[i] != null) {
                newLogs[index++] = logs[i];
            }
        }
        this.logs = newLogs;
    }

    public boolean isLaunchingTokenExist() {
        return launchingTokenExist;
    }

    public List<SubJobMilestone> getMilestones() {
        return milestones;
    }

    public SubJobLog[] getLogs() {
        return logs;
    }

    public List<ExecuteStep> getExecuteSteps() {
        return executeSteps;
    }
}
