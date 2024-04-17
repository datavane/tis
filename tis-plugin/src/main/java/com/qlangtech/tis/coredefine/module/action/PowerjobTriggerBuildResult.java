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

package com.qlangtech.tis.coredefine.module.action;


import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.plugin.PluginAndCfgsSnapshotUtils;

import java.util.Objects;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/12/11
 */
public class PowerjobTriggerBuildResult extends TriggerBuildResult {
    private String pluginCfgsMetas;
    /**
     * 用于Crontab任务传递的参数
     */
    private String javaMemorySpec;
    /**
     * 前一次执行的taskId，初次执行时为空
     */
    private Integer previousTaskId;

    public PowerjobTriggerBuildResult() {
    }

    public PowerjobTriggerBuildResult(boolean success, JSONObject instanceParams) {
        super(success);
        // JSONObject instanceParams = Objects.requireNonNull(instanceParamsRef).get();
        pluginCfgsMetas = Objects.requireNonNull(instanceParams, "instanceParams can not be null") //
                .getString(PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS);
        this.javaMemorySpec = instanceParams.getString(JobParams.KEY_JAVA_MEMORY_SPEC);
        this.previousTaskId = instanceParams.getInteger(JobParams.KEY_PREVIOUS_TASK_ID);
    }

    public Integer getPreviousTaskId() {
        return previousTaskId;
    }

    public void setPreviousTaskId(Integer previousTaskId) {
        this.previousTaskId = previousTaskId;
    }

    public String getJavaMemorySpec() {
        return javaMemorySpec;
    }

    public void setJavaMemorySpec(String javaMemorySpec) {
        this.javaMemorySpec = javaMemorySpec;
    }

    public String getPluginCfgsMetas() {
        return pluginCfgsMetas;
    }

    public void setPluginCfgsMetas(String pluginCfgsMetas) {
        this.pluginCfgsMetas = pluginCfgsMetas;
    }
}
