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
package com.qlangtech.tis.aiagent.plan;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务步骤定义
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class TaskStep {
    
    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        SKIPPED
    }

    public enum StepType {
        PLUGIN_INSTALL,
        PLUGIN_CREATE,
        USER_INPUT,
        EXECUTE_BATCH,
        EXECUTE_INCR,
        SELECT_TABLES
    }

    private String stepId;
    private String name;
    private String description;
    private StepType type;
    private Status status = Status.PENDING;
    private String pluginImpl;
    private JSONObject pluginConfig;
    private List<TaskStep> subSteps;
    private String errorMessage;
    private long startTime;
    private long endTime;
    private boolean requireUserConfirm;

    public TaskStep() {
        this.subSteps = new ArrayList<>();
    }

    public TaskStep(String name, StepType type) {
        this();
        this.name = name;
        this.type = type;
    }

    public void addSubStep(TaskStep subStep) {
        subSteps.add(subStep);
    }

    public void markAsStarted() {
        this.status = Status.IN_PROGRESS;
        this.startTime = System.currentTimeMillis();
    }

    public void markAsCompleted() {
        this.status = Status.COMPLETED;
        this.endTime = System.currentTimeMillis();
    }

    public void markAsFailed(String errorMessage) {
        this.status = Status.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = System.currentTimeMillis();
    }

    public void markAsSkipped() {
        this.status = Status.SKIPPED;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StepType getType() {
        return type;
    }

    public void setType(StepType type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPluginImpl() {
        return pluginImpl;
    }

    public void setPluginImpl(String pluginImpl) {
        this.pluginImpl = pluginImpl;
    }

    public JSONObject getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(JSONObject pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    public List<TaskStep> getSubSteps() {
        return subSteps;
    }

    public void setSubSteps(List<TaskStep> subSteps) {
        this.subSteps = subSteps;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isRequireUserConfirm() {
        return requireUserConfirm;
    }

    public void setRequireUserConfirm(boolean requireUserConfirm) {
        this.requireUserConfirm = requireUserConfirm;
    }

    public long getExecutionTime() {
        if (startTime > 0 && endTime > 0) {
            return endTime - startTime;
        }
        return 0;
    }
}