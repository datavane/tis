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

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-07-15 22:45
 **/
public class PipelineFlinkTaskId {
    private final String pipeline;
    private final String taskId;

    public static final PipelineFlinkTaskId parse(String key) {
        String[] split = StringUtils.split(key, "$");
        if (split.length != 2) {
            throw new IllegalArgumentException("key:" + key + " is not invalid");
        }
        return new PipelineFlinkTaskId(split[0], split[1]);
    }

    /**
     * @param pipeline
     * @param taskId
     */
    public PipelineFlinkTaskId(String pipeline, String taskId) {
        if (StringUtils.isEmpty(pipeline)) {
            throw new IllegalArgumentException("illegal param pipeline");
        }
        if (StringUtils.isEmpty(taskId)) {
            throw new IllegalArgumentException("illegal param taskId");
        }
        this.pipeline = pipeline;
        this.taskId = taskId;
    }

    public String getPipeline() {
        return this.pipeline;
    }

    public String getTaskId() {
        return this.taskId;
    }

    public String getKey() {
        return this.pipeline + "$" + this.taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineFlinkTaskId that = (PipelineFlinkTaskId) o;
        return Objects.equals(pipeline, that.pipeline) && Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pipeline, taskId);
    }
}
