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

import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 启动执行剧本
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-23 14:53
 **/
public interface ILaunchingOrchestrate<T> {

    public List<ExecuteStep<T>> getExecuteSteps();

    public default ExecuteSteps createExecuteSteps(Object owner) {
        return new ExecuteSteps(owner, this.getExecuteSteps().stream().collect(Collectors.toList()));
    }

    public class ExecuteSteps {

        private final List<ExecuteStep> executeSteps;

        public List<ExecuteStep> getExecuteSteps() {
            return this.executeSteps;
        }

        public ExecuteSteps(Object owner, List<ExecuteStep> executeSteps) {
            this.executeSteps = executeSteps;
            if (CollectionUtils.isEmpty(this.executeSteps)) {
                throw new IllegalStateException("executeSteps can not be empty,owner:"
                        + Objects.requireNonNull(owner, "owner can not be null").getClass().getName());
            }
        }
    }

    public class ExecuteStep<T> extends SubJobMilestone {
        private final JobResName<T> subJob;

        public ExecuteStep(JobResName<T> resName, String describe) {
            this(resName, describe, false, false);
        }

        public ExecuteStep(JobResName<T> name, String describe, boolean complete, boolean success) {
            super(name.getName(), describe, complete, success);
            this.subJob = name;
        }

        public JobResName<T> getSubJob() {
            return this.subJob;
        }

        public ExecuteStep<T> copy(SubJobMilestone subJobStone) {
            ExecuteStep<T> step = new ExecuteStep<T>(subJob
                    , this.getDescribe(), subJobStone.isComplete(), subJobStone.isSuccess());
            return step;
        }

        public ExecuteStep<T> copy() {
            return new ExecuteStep<T>(subJob, this.getDescribe(), this.isComplete(), this.isSuccess());
        }
    }

}
