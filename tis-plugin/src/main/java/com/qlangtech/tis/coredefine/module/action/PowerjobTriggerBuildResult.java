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
import com.qlangtech.tis.plugin.PluginAndCfgsSnapshotUtils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/12/11
 */
public class PowerjobTriggerBuildResult extends TriggerBuildResult {
    private String pluginCfgsMetas;

    public PowerjobTriggerBuildResult() {
    }

    public PowerjobTriggerBuildResult(boolean success, AtomicReference<JSONObject> instanceParamsRef) {
        super(success);
        JSONObject instanceParams = Objects.requireNonNull(instanceParamsRef).get();
        if (instanceParams != null) {
            pluginCfgsMetas = instanceParams.getString(PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS);
        }
    }

    public String getPluginCfgsMetas() {
        return pluginCfgsMetas;
    }

    public void setPluginCfgsMetas(String pluginCfgsMetas) {
        this.pluginCfgsMetas = pluginCfgsMetas;
    }
}
