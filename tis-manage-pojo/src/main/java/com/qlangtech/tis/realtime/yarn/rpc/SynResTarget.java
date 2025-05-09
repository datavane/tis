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

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-04-15 10:51
 **/
public class SynResTarget {
    private final String name;
    /**
     * dataX pipeline OR transform workflow
     */
    private final boolean pipeline;
    private Integer workflowId;

    public static SynResTarget pipeline(String name) {
        return new SynResTarget(name, true);
    }

    /**
     * transform workflow 处理管道
     *
     * @param name
     * @return
     */
    public static SynResTarget transform(String name) {
        return transform(null, name);
    }

    public static SynResTarget transform(Integer workflowId, String name) {
        SynResTarget wfResTarget = new SynResTarget(name, false);
        wfResTarget.setWorkflowId(workflowId);
        return wfResTarget;
    }

    private SynResTarget(String name, boolean pipeline) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("param name can not be empty");
        }
        this.name = name;
        this.pipeline = pipeline;
    }

    public Integer getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }

    public String getName() {
        return name;
    }

    public boolean isPipeline() {
        return pipeline;
    }
}
