/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.datax;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.job.DataXJobWorker;
import com.qlangtech.tis.extension.ExtensionList;
import com.qlangtech.tis.extension.TISExtensible;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import com.tis.hadoop.rpc.RpcServiceReference;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-27 17:03
 **/
@TISExtensible
public abstract class DataXJobSubmit {

    public static Callable<DataXJobSubmit> mockGetter;

    public static DataXJobSubmit.InstanceType getDataXTriggerType() {
        DataXJobWorker jobWorker = DataXJobWorker.getJobWorker(DataXJobWorker.K8S_DATAX_INSTANCE_NAME);
        boolean dataXWorkerServiceOnDuty = jobWorker != null && jobWorker.inService();//.isDataXWorkerServiceOnDuty();
        return dataXWorkerServiceOnDuty ? DataXJobSubmit.InstanceType.DISTRIBUTE : DataXJobSubmit.InstanceType.LOCAL;
    }

    public static Optional<DataXJobSubmit> getDataXJobSubmit(DataXJobSubmit.InstanceType expectDataXJobSumit) {
        try {
            if (mockGetter != null) {
                return Optional.ofNullable(mockGetter.call());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ExtensionList<DataXJobSubmit> jobSumits = TIS.get().getExtensionList(DataXJobSubmit.class);
        Optional<DataXJobSubmit> jobSubmit = jobSumits.stream()
                .filter((jsubmit) -> (expectDataXJobSumit) == jsubmit.getType()).findFirst();
        return jobSubmit;
    }

    public enum InstanceType {
        DISTRIBUTE("distribute"), LOCAL("local");
        public final String literia;

        public static InstanceType parse(String val) {
            for (InstanceType t : InstanceType.values()) {
                if (t.literia.equals(val)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("value:" + val + " is not illegal");
        }

        private InstanceType(String val) {
            this.literia = val;
        }
    }


    public abstract InstanceType getType();


    protected CuratorDataXTaskMessage getDataXJobDTO(IJoinTaskContext taskContext, String dataXfileName) {
        CuratorDataXTaskMessage msg = new CuratorDataXTaskMessage();
        msg.setDataXName(taskContext.getIndexName());
        msg.setJobId(taskContext.getTaskId());
        msg.setJobName(dataXfileName);
        PhaseStatusCollection preTaskStatus = taskContext.loadPhaseStatusFromLatest(taskContext.getIndexName());
        DumpPhaseStatus.TableDumpStatus dataXJob = null;
        if (preTaskStatus != null
                && (dataXJob = preTaskStatus.getDumpPhase().getTable(dataXfileName)) != null
                && dataXJob.getAllRows() > 0
        ) {
            msg.setAllRowsApproximately(dataXJob.getReadRows());
        } else {
            msg.setAllRowsApproximately(1000000);
        }
        return msg;
    }

    /**
     * 创建dataX任务
     *
     * @param taskContext
     * @param dataXfileName
     * @return
     */
    public abstract IRemoteJobTrigger createDataXJob(IJoinTaskContext taskContext
            , RpcServiceReference statusRpc, IDataxProcessor dataxProcessor, String dataXfileName);


}
