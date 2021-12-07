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

package com.qlangtech.tis.plugin;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.extension.Descriptor;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-12-07 17:48
 **/
public class ParamsConfigPluginStore extends PluginStore<ParamsConfig> {
    private static final String CONTEXT_PARAMS_CFG = "params-cfg";

    public ParamsConfigPluginStore() {
        super(ParamsConfig.class, Descriptor.getConfigFile(CONTEXT_PARAMS_CFG));
    }


    @Override
    public List<ParamsConfig> getPlugins() {

//        this.getTargetFile()
//
//        return TIS.getPluginStore(CONTEXT_PARAMS_CFG, resName.getName(), ParamsConfig.class);
        // return super.getPlugins();
        return null;
    }
}
