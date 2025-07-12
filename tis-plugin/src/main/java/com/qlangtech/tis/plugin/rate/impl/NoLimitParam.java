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

package com.qlangtech.tis.plugin.rate.impl;

import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.rate.IncrRateParam;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO.RateControllerType;

/**
 * 流量不作任何限制
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-07-09 10:46
 **/
public class NoLimitParam extends IncrRateParam {

    @Override
    public RateControllerType getControllerType() {
        return RateControllerType.NoLimitParam;
    }

    @TISExtension
    public static class DefaultDesc extends Descriptor<IncrRateParam> {
        public DefaultDesc() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "NoLimit";
        }
    }
}
