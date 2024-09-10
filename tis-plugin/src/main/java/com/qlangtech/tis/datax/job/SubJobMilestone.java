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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteStep;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-25 12:54
 **/
public class SubJobMilestone {
    public static SubJobMilestone readSubJobMilestoneJson(String o) {
        return JSON.parseObject(o, SubJobMilestone.class);
    }

    private String name;
    private String describe;
    private boolean complete;
    private boolean success;

    public SubJobMilestone() {
    }

    public SubJobMilestone(String name, String describe, boolean complete, boolean success) {
        this.name = name;
        this.describe = describe;
        this.complete = complete;
        this.success = success;
    }

    public static JSONObject createMilestoneJson(String subJobName, Optional<String> describe, boolean complete, Boolean success) {
        JSONObject step;
        step = new JSONObject();
        step.put("name", subJobName);
        step.put("describe", describe.orElse(subJobName));
        step.put("complete", complete);
        if (complete) {
            step.put("success", Objects.requireNonNull(success, "success can not be null"));
        }
        return step;
    }


    public static JSONArray createSubJobJSONArray(List<ExecuteStep> executeSteps) {
        JSONArray steps = new JSONArray();
        JSONObject step = null;

        for (ExecuteStep s : executeSteps) {
            step = SubJobMilestone.createMilestoneJson(s.getName()
                    , Optional.ofNullable(s.getDescribe())
                    , s.isComplete()
                    , s.isSuccess());
            steps.add(step);
        }
        return steps;
    }

    public static List<ExecuteStep> readSubJobJSONArray( //
                                                         List<ExecuteStep> example, Function<String, SubJobMilestone> executeSubJobFinder) {
        List<ExecuteStep> result = Lists.newArrayList();
        SubJobMilestone subJobStone = null;
        for (ExecuteStep e : example) {
            subJobStone = executeSubJobFinder.apply(e.getName());
            if (subJobStone != null) {
                result.add(e.copy(subJobStone));
            } else {
                result.add(e.copy());
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isFaild() {
        return this.complete && !this.success;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
