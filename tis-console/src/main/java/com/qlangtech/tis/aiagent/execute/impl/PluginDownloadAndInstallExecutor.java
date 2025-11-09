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

package com.qlangtech.tis.aiagent.execute.impl;

import com.google.common.collect.Sets;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/18
 */
public class PluginDownloadAndInstallExecutor extends BasicStepExecutor {
  @Override
  public boolean execute(TaskPlan plan, TaskStep step, AgentContext context) {

    this.checkInstallPlugin(context, Sets.newHashSet(
        Pair.of(plan.getSourceEnd().getType(), plan.readerExtendPoints.values())
        , Pair.of(plan.getTargetEnd().getType(), plan.writerExtendPoints.values()))
      , (pluginsInstall) -> {
        plan.checkDescribableImplHasSet();
      });

    return true;
  }


  /**
   * 执行完毕之后需要校验
   *
   * @param step 要验证的步骤
   * @return
   */
  @Override
  public ValidationResult validate(TaskStep step) {
    return ValidationResult.success();
  }

  @Override
  public TaskStep.StepType getSupportedType() {
    return TaskStep.StepType.PLUGIN_INSTALL;
  }
}
