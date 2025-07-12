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

package com.qlangtech.tis.realtime.yarn.rpc;

import java.util.Map;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-07-09 14:38
 **/
public class IncrRateControllerCfgDTO {

    public static final String KEY_RATE_LIMIT_PER_SECOND = "rateLimitPerSecond";
    public static final String KEY_PIPELINE = "pipeline";
    public static final String KEY_LAST_MODIFIED = "lastModified";

    private Boolean pause;

    private Long lastModified;

    private RateControllerType controllerType;

    private Map<String, Object> payloadParams;

    public Boolean getPause() {
        return pause;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public RateControllerType getControllerType() {
        return controllerType;
    }

    public void setControllerType(RateControllerType controllerType) {
        this.controllerType = controllerType;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public Map<String, Object> getPayloadParams() {
        return payloadParams;
    }

    public void setPayloadParams(Map<String, Object> payloadParams) {
        this.payloadParams = payloadParams;
    }

    public void setPause(Boolean pause) {
        this.pause = pause;
    }

    public enum RateControllerType {
        FloodDischargeRate,
        NoLimitParam,
        RateLimit,
        // 没有开启流控 DataGeneratorSource 中产生一条未不需要处理的消息
        SkipProcess
    }
}
