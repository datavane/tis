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
package com.qlangtech.tis.aiagent.execute;

import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;

/**
 * 步骤执行器接口
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public interface StepExecutor {

  /**
   * 执行任务步骤
   *
   * @param step    要执行的步骤
   * @param context Agent上下文
   * @return 执行是否成功
   */
  boolean execute(TaskPlan plan, TaskStep step, AgentContext context);

  /**
   * 验证步骤配置是否完整
   *
   * @param step 要验证的步骤
   * @return 验证结果
   */
  ValidationResult validate(TaskStep step);

  /**
   * 获取执行器支持的步骤类型
   */
  TaskStep.StepType getSupportedType();

  /**
   * 验证结果
   */
  class ValidationResult {
    private boolean valid;
    private String errorMessage;
    private String[] missingFields;

    public ValidationResult(boolean valid) {
      this.valid = valid;
    }

    public ValidationResult(boolean valid, String errorMessage) {
      this.valid = valid;
      this.errorMessage = errorMessage;
    }

    public boolean isValid() {
      return valid;
    }

    public void setValid(boolean valid) {
      this.valid = valid;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }

    public String[] getMissingFields() {
      return missingFields;
    }

    public void setMissingFields(String[] missingFields) {
      this.missingFields = missingFields;
    }

    public static ValidationResult success() {
      return new ValidationResult(true);
    }

    public static ValidationResult failure(String message) {
      return new ValidationResult(false, message);
    }
  }
}
