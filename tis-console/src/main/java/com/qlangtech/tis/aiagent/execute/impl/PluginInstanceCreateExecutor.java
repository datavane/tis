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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.core.SelectionOptions;
import com.qlangtech.tis.aiagent.execute.StepExecutor;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DescriptorsJSONForAIPromote;
import com.qlangtech.tis.util.DescriptorsJSONResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    TaskPlan.DataEndCfg endCfg = null;
    DescribableImpl dataXReaderImpl = plan.readerExtendPoints.get(DataxReader.class);
    endCfg = plan.getSourceEnd();
    createPluginInstance(context, endCfg.getRelevantDesc(), Optional.of(endCfg.getType()), dataXReaderImpl);


    endCfg = plan.getTargetEnd();
    DescribableImpl dataXWriterImpl = plan.writerExtendPoints.get(DataxWriter.class);
    createPluginInstance(context, endCfg.getRelevantDesc(), Optional.of(endCfg.getType()), dataXWriterImpl);
    return true;
  }

  private void createPluginInstance(AgentContext context, String userInput //
    , Optional<IEndTypeGetter.EndType> endType, DescribableImpl pluginImpl) {
    Pair<DescriptorsJSONResult, DescriptorsJSONForAIPromote> desc = DescriptorsJSONForAIPromote.desc(pluginImpl);

    DescriptorsJSONForAIPromote forAIPromote = desc.getValue();
    DescribableImpl propImplInfo = null;
    Descriptor implDesc = null;
    Map<String, Descriptor.PropsImplRefs> propsImplRefs = null;
    Map<Class<? extends Descriptor>, DescribableImpl> fieldDescRegister = forAIPromote.getFieldDescRegister();
    Descriptor installedPluginDescriptor = null;
    for (Map.Entry<Class<? extends Descriptor>, DescribableImpl> entry : fieldDescRegister.entrySet()) {
      propImplInfo = entry.getValue();
      implDesc = propImplInfo.getImplDesc();
      propsImplRefs = implDesc.getPropsImplRefs();

      for (Map.Entry<String /**fieldName*/, Descriptor.PropsImplRefs> e : propsImplRefs.entrySet()) {
        List<PluginExtraProps.CandidatePlugin> candidatePlugins = e.getValue().getCandidatePlugins();
        // 已经存在的opts，需要根据用户提交的信息内容判断是否可以选择已经有的opts
        List<Option> existOpts = e.getValue().getValOptions();
        if (CollectionUtils.isEmpty(candidatePlugins)) {
          throw new IllegalStateException("candidatePlugins can not be empty");
        }
        PluginExtraProps.CandidatePlugin candidatePlugin = null;
        if (candidatePlugins.size() < 2) {
          for (PluginExtraProps.CandidatePlugin candidate : candidatePlugins) {
            installedPluginDescriptor = candidate.getInstalledPluginDescriptor();
            if (installedPluginDescriptor == null) {
              // 需要先安装插件，然后再实例化
              // 由于插件还没有安装需要安装上
              candidatePlugin = this.selectTargetPluginDescriptor(context, e, endType, candidatePlugins);
            } else {
              candidatePlugin = candidate;
            }
          }
        } else {
          candidatePlugin = this.selectTargetPluginDescriptor(context, e, endType, candidatePlugins);
        }

        // 开始实例化插件
        createPluginInstance(context, userInput
          , Objects.requireNonNull(candidatePlugin
            , "candidatePlugin can not be null for field:" + e.getKey())
          , candidatePlugin.getInstalledPluginDescriptor());
      }
    }


    pluginImpl.getImplDesc();

    for (Map.Entry<String, JSONObject> entry : desc.getLeft().getDescriptorsResult().entrySet()) {

      // 需要遍历他的所有属性如果有需要创建的属性插件需要先创建

      JsonUtil.toString(entry.getValue(), true);


    }
  }

  private void createPluginInstance(AgentContext context, String userInput
    , PluginExtraProps.CandidatePlugin candidate, Descriptor installedPluginDescriptor) {
    createPluginInstance(context, userInput, Optional.empty()
      , new DescribableImpl(candidate.getHetero().getExtensionPoint(), Optional.empty())
        .setDescriptor(
          Objects.requireNonNull(installedPluginDescriptor
            , "candidate:" + candidate + ",installedPluginDescriptor can not be null")));
  }

  /**
   * 选择&安装目标插件
   *
   * @param context
   * @param e
   * @param candidatePlugins
   * @return
   */
  private PluginExtraProps.CandidatePlugin selectTargetPluginDescriptor(AgentContext context
    , Map.Entry<String, Descriptor.PropsImplRefs> e //
    , Optional<IEndTypeGetter.EndType> endType, List<PluginExtraProps.CandidatePlugin> candidatePlugins) {

    /**
     * 1. 需要用户去确认使用哪种插件类型
     * 2. 查看每种插件是否已经安装，如果没有安装需要先安装插件
     */
    PluginExtraProps.CandidatePlugin tagetPlugin = selectPlugin(context, e.getKey(), endType, candidatePlugins);

    Descriptor installedPluginDescriptor = tagetPlugin.getInstalledPluginDescriptor();
    if (installedPluginDescriptor == null) {
      // 开始需要安装插件
      throw new IllegalStateException(tagetPlugin.getDisplayName() + " plugin must have been installed");
    }
    return tagetPlugin;
  }

  /**
   * 请求用户选择插件，通过SSE发送候选项到前端，等待用户选择
   *
   * @param context          Agent上下文
   * @param fieldName        字段名称
   * @param candidatePlugins 候选插件列表
   * @return 用户选择的插件，如果取消或超时返回null
   */
  private PluginExtraProps.CandidatePlugin selectPlugin(
    AgentContext context,
    String fieldName,
    Optional<IEndTypeGetter.EndType> endType,
    List<PluginExtraProps.CandidatePlugin> candidatePlugins) {

    if (CollectionUtils.isEmpty(candidatePlugins)) {
      throw new IllegalArgumentException("candidatePlugins can not be empty");
    }

    String requestId = "plugin_select_" + System.currentTimeMillis();

    JSONObject optionsData = new JSONObject();
    optionsData.put("fieldName", fieldName);

    JSONArray optionsArray = PluginExtraProps.CandidatePlugin.convertOptionsArray(endType, candidatePlugins);

    optionsData.put("candidates", optionsArray);

    String prompt = String.format("请选择 %s 字段的插件实现", fieldName);
    context.sendMessage(prompt);
    /************************************************************************
     * 向客户端发送需要用户确认的请求
     ************************************************************************/
    context.requestUserSelection(requestId, prompt, optionsData, candidatePlugins);

    /************************************************************************
     * 等待客户端发送的选择信息
     ************************************************************************/
    SelectionOptions selectedIndex = context.waitForUserSelection(requestId);
    if (selectedIndex != null) {
      PluginExtraProps.CandidatePlugin selected = selectedIndex.getSelectPluginDesc();
//    if (selectedIndex >= 0 && selectedIndex < candidatePlugins.size()) {
//      PluginExtraProps.CandidatePlugin selected = candidatePlugins.get(selectedIndex);
//      if (selected.getInstalledPluginDescriptor(true) == null) {
//        throw new IllegalStateException(selected.getDisplayName()
//          + " of extendpoint:" + selected.getHetero().getExtensionPoint().getName() + " have not been installed");
//      }

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
