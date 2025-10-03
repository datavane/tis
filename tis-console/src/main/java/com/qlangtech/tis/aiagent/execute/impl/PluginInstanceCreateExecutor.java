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

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.execute.StepExecutor;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DescriptorsJSONForAIPromote;
import com.qlangtech.tis.util.DescriptorsJSONResult;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 创建插件实例
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/18
 */
public class PluginInstanceCreateExecutor implements StepExecutor {
  @Override
  public boolean execute(TaskPlan plan, TaskStep step, AgentContext context) {

    DescribableImpl dataXReaderImpl = plan.readerExtendPoints.get(DataxReader.class);
    createPluginInstance(context, plan, Optional.of(plan.getSourceType()), dataXReaderImpl);


    DescribableImpl dataXWriterImpl = plan.writerExtendPoints.get(DataxWriter.class);

    return false;
  }

  private void createPluginInstance(AgentContext context, TaskPlan taskPlan, Optional<IEndTypeGetter.EndType> endType, DescribableImpl dataXReaderImpl) {
    Pair<DescriptorsJSONResult, DescriptorsJSONForAIPromote> desc = DescriptorsJSONForAIPromote.desc(dataXReaderImpl);

    DescriptorsJSONForAIPromote forAIPromote = desc.getValue();
    DescribableImpl propImplInfo = null;
    Descriptor implDesc = null;
    Map<String, Descriptor.PropsImplRefs> propsImplRefs = null;
    Map<Class<? extends Descriptor>, DescribableImpl> fieldDescRegister = forAIPromote.getFieldDescRegister();
    Descriptor installedPluginDescriptor = null;
    for (Map.Entry<Class<? extends Descriptor>, DescribableImpl> entry : fieldDescRegister.entrySet()) {
      propImplInfo = entry.getValue();
      implDesc = propImplInfo.getImplDesc(endType);
      propsImplRefs = implDesc.getPropsImplRefs();

      for (Map.Entry<String /**fieldName*/, Descriptor.PropsImplRefs> e : propsImplRefs.entrySet()) {
        List<PluginExtraProps.CandidatePlugin> candidatePlugins = e.getValue().getCandidatePlugins();
        if (candidatePlugins.size() < 2) {
          for (PluginExtraProps.CandidatePlugin candidate : candidatePlugins) {
            installedPluginDescriptor = candidate.getInstalledPluginDescriptor();
            if (installedPluginDescriptor != null) {
              // 开始实例化插件
              createPluginInstance(context, taskPlan, Optional.empty()
                , new DescribableImpl(candidate.getHetero().getExtensionPoint()).setDescriptor(installedPluginDescriptor));
            } else {
              // 需要先安装插件，然后再实例化
            }
          }
        } else {
          /**
           * 1. 需要用户去确认使用哪种插件类型
           * 2. 查看每种插件是否已经安装，如果没有安装需要先安装插件
           */
          PluginExtraProps.CandidatePlugin tagetPlugin = selectPlugin(context, e.getKey(), candidatePlugins);

          installedPluginDescriptor = tagetPlugin.getInstalledPluginDescriptor();
          if (installedPluginDescriptor == null) {
            // 开始实例化插件
          }


        }
      }

    }

    for (Map.Entry<String, JSONObject> entry : desc.getLeft().getDescriptorsResult().entrySet()) {

      // 需要遍历他的所有属性如果有需要创建的属性插件需要先创建

      JsonUtil.toString(entry.getValue(), true);
    }
  }

  /**
   * 请求用户选择插件，通过SSE发送候选项到前端，等待用户选择
   *
   * @param context Agent上下文
   * @param fieldName 字段名称
   * @param candidatePlugins 候选插件列表
   * @return 用户选择的插件，如果取消或超时返回null
   */
  private PluginExtraProps.CandidatePlugin selectPlugin(
      AgentContext context,
      String fieldName,
      List<PluginExtraProps.CandidatePlugin> candidatePlugins) {

    if (candidatePlugins == null || candidatePlugins.isEmpty()) {
      return null;
    }

//    if (candidatePlugins.size() == 1) {
//      return candidatePlugins.get(0);
//    }

    String requestId = "plugin_select_" + System.currentTimeMillis();

    JSONObject optionsData = new JSONObject();
    optionsData.put("fieldName", fieldName);
    com.alibaba.fastjson.JSONArray optionsArray = new com.alibaba.fastjson.JSONArray();

    for (int i = 0; i < candidatePlugins.size(); i++) {
      PluginExtraProps.CandidatePlugin candidate = candidatePlugins.get(i);
      JSONObject option = new JSONObject();
      option.put("index", i);
      option.put("name", candidate.getDisplayName());
      option.put("description", candidate.getDisplayName());
      option.put("installed", candidate.getInstalledPluginDescriptor() != null);

      Descriptor<?> installedDesc = candidate.getInstalledPluginDescriptor();
      if (installedDesc != null) {
        option.put("version", installedDesc.getId());
      }

      optionsArray.add(option);
    }

    optionsData.put("candidates", optionsArray);

    String prompt = String.format("请选择 %s 字段的插件实现", fieldName);
    context.sendMessage(prompt);
    context.requestUserSelection(requestId, prompt, optionsData);

    int selectedIndex = context.waitForUserSelection(requestId);

    if (selectedIndex >= 0 && selectedIndex < candidatePlugins.size()) {
      PluginExtraProps.CandidatePlugin selected = candidatePlugins.get(selectedIndex);
      context.sendMessage(String.format("已选择: %s", selected.getDisplayName()));
      return selected;
    }

    context.sendError("用户选择超时或取消");
    return null;
  }

  @Override
  public ValidationResult validate(TaskStep step) {
    return ValidationResult.success();
  }

  @Override
  public TaskStep.StepType getSupportedType() {
    return TaskStep.StepType.PLUGIN_CREATE;
  }
}
