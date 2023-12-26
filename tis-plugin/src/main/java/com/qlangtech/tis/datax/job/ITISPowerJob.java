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

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;

import java.util.Objects;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/9
 */
public interface ITISPowerJob {

    static Long getPowerJobWorkflowInstanceId(WorkFlowBuildHistory wfBuildHistory, boolean validate) {
        JSONObject wfHistory = JSONObject.parseObject(
                Objects.requireNonNull(wfBuildHistory, "param wfBuildHistory can not be null").getAsynSubTaskStatus());
        if (validate) {
            Objects.requireNonNull(wfHistory, "taskId:" + wfBuildHistory.getId()
                    + ",relevant getAsynSubTaskStatus:" + wfBuildHistory.getAsynSubTaskStatus());
        } else {
            if (wfHistory == null) {
                return null;
            }
        }
        Long powerJobWorkflowInstanceId = wfHistory.getLong(ITISPowerJob.KEY_POWERJOB_WORKFLOW_INSTANCE_ID);
        if (validate) {
            Objects.requireNonNull(powerJobWorkflowInstanceId, "key:"
                    + ITISPowerJob.KEY_POWERJOB_WORKFLOW_INSTANCE_ID + " relevant property can not be find in json:" + JsonUtil.toString(wfHistory));

        }
        return powerJobWorkflowInstanceId;
    }


    String KEY_POWERJOB_WORKFLOW_INSTANCE_ID = "pwoerjob_instance_id";

    public <PowerJobClient> PowerJobClient getPowerJobClient();

//    /**
//     * 注册新app
//     *
//     * @param appName
//     * @param password
//     */
//    public void registerPowerJobApp(String powerjobDomain, String appName, String password);
}
