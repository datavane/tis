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
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.rate.IncrRateParam;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO.RateControllerType;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO.KEY_RATE_LIMIT_PER_SECOND;

/**
 * 控制限流参数
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-07-07 16:17
 **/
public class RateLimitParam extends IncrRateParam {

    @FormField(ordinal = 0, type = FormFieldType.INT_NUMBER, validate = {Validator.require, Validator.integer})
    public Integer recordsPerSecond;

    @Override
    public RateControllerType getControllerType() {
        return RateControllerType.RateLimit;
    }

    @Override
    public Map<String, Object> getPayloadParams() {
        return Collections.singletonMap(KEY_RATE_LIMIT_PER_SECOND, Objects.requireNonNull(recordsPerSecond));
    }

    @TISExtension
    public static class DefaultDesc extends Descriptor<IncrRateParam> {
        public DefaultDesc() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "RateLimit";
        }
    }
}
