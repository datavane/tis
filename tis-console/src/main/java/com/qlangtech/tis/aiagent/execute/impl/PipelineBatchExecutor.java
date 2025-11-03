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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.core.RequestKey;
import com.qlangtech.tis.aiagent.core.SelectionOptions;
import com.qlangtech.tis.aiagent.execute.StepExecutor;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.coredefine.module.action.TriggerBuildResult;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.util.PartialSettedPluginContext;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.qlangtech.tis.aiagent.execute.impl.PluginInstanceCreateExecutor.createPluginContext;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/4
 */
public class PipelineBatchExecutor implements StepExecutor {
  @Override
  public boolean execute(TaskPlan plan, TaskStep step, AgentContext context) {

    TaskPlan.SourceDataEndCfg sourceEnd = plan.getSourceEnd();
    TaskPlan.DataEndCfg targetEnd = plan.getTargetEnd();
    if (!Objects.requireNonNull(sourceEnd.getEndTypeMeta()
      , "sourceEnd:" + sourceEnd.getType() + " relevant EndTypeMeta can not be null").isSupportBatch()) {
      context.sendMessage("源端‘" + sourceEnd.getType() + "’类型不支持批量数据同步，因此跳过该步骤");
      return true;
    }
    if (!Objects.requireNonNull(targetEnd.getEndTypeMeta()
      , "targetEnd:" + targetEnd.getType() + " relevant EndTypeMeta can not be null").isSupportBatch()) {
      context.sendMessage("目标端‘" + targetEnd.getType() + "’类型不支持批量数据同步，因此跳过该步骤");
      return true;
    }
    boolean executeBatch = sourceEnd.isExecuteBatch();
    if (!executeBatch) {
      RequestKey requestId = RequestKey.create();
      List<PluginExtraProps.CandidatePlugin> opts
        = Lists.newArrayList(
        new NormalSelectionOption("是的，立即触发")
        , new NormalSelectionOption("不，先不触发，等等再说"));

      final String prompt = "请选择是否立即触发全量历史数据同步？";
      context.requestUserSelection(requestId, prompt, Optional.empty(), opts);

      /************************************************************************
       * 等待客户端发送的选择信息
       ************************************************************************/
      SelectionOptions selectedIndex = context.waitForUserPost(requestId, (selOpts) -> {
        return (selOpts != null && selOpts.hasSelectedOpt());
      });
      executeBatch = (selectedIndex.getSelectedIndex() == 0);
    }

    if (!executeBatch) {
      context.sendMessage("您选择了不触发全量历史数据同步，现在跳过该步骤。");
      return true;
    }

    context.sendMessage("现在开始触发全量历史数据同步。");
    DataXJobSubmit.InstanceType triggerType = DataXJobSubmit.getDataXTriggerType();
    Optional<DataXJobSubmit> dataXJobSubmit = DataXJobSubmit.getDataXJobSubmit(false, triggerType);

    if (!dataXJobSubmit.isPresent()) {
      throw new IllegalStateException("triggerType:" + triggerType + " have not been installed,please install it ahead");
    }
    DataXName dataXName = sourceEnd.getProcessor().getDataXName();
    PartialSettedPluginContext pluginContext = createPluginContext(plan, dataXName);
    Context runtimeContext = plan.getRuntimeContext(true);
    TriggerBuildResult triggerResult = dataXJobSubmit.get().triggerJob(pluginContext
      , runtimeContext, dataXName, Optional.empty(), Optional.empty());
    if (triggerResult.success) {
      context.sendMessage("已经成功触发'" + String.valueOf(dataXName) + "'全量历史数据同步执行。"
        , new AgentContext.ManagerLink("查看任务执行状态", "/x/" + String.valueOf(dataXName) + "/app_build_history/" + triggerResult.getTaskid()));
    } else {
      context.sendError("触发'" + String.valueOf(dataXName) + "'全量历史数据同步失败。");
    }
    return true;
  }

  private static class NormalSelectionOption extends PluginExtraProps.CandidatePlugin {
    public NormalSelectionOption(String displayName) {
      super(displayName, Optional.empty(), null);
    }

    @Override
    public void setExtraProps(Optional<IEndTypeGetter.EndType> endType, JSONObject option) {
      option.put(KEY_DISABLE_PLUGIN_INSTALL, true);
      option.put(KEY_INSTALLED, true);
    }
  }

  @Override
  public ValidationResult validate(TaskStep step) {
    return ValidationResult.success();
  }

  @Override
  public TaskStep.StepType getSupportedType() {
    return TaskStep.StepType.EXECUTE_BATCH;
  }
}
