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
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.core.PluginPropsComplement;
import com.qlangtech.tis.aiagent.core.SelectionOptions;
import com.qlangtech.tis.aiagent.core.SessionKey;
import com.qlangtech.tis.aiagent.execute.StepExecutor;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.AppAndRuntime;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.offline.DbScope;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.credentials.ParamsConfigPluginStore;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.runtime.module.misc.DefaultMessageHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescriptorsJSONForAIPromote;
import com.qlangtech.tis.util.DescriptorsJSONResult;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.util.impl.AttrVals;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.qlangtech.tis.extension.util.PluginExtraProps.CandidatePlugin.createNewPrimaryFieldValue;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_VALS;
import static com.qlangtech.tis.util.AttrValMap.parseDescribableMap;

/**
 * 创建插件实例
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/18
 */
public class PluginInstanceCreateExecutor implements StepExecutor {
  @Override
  public boolean execute(TaskPlan plan, TaskStep step, AgentContext context) {
    try {
      // DataxProcessor.
      PartialSettedPluginContext pluginCtx = createPluginContext();
      Context ctx = new DefaultContext();
      /**
       * support for DefaultDataxProcessor$DescriptorImpl.getManipulateStore()
       */
      AppAndRuntime.setAppAndRuntime(new AppAndRuntime());
      AttrValMap processValMap
        = createPluginInstance(plan, context
        , plan.getUserInput(), Optional.empty()
        , plan.processorExtendPoints, HeteroEnum.APP_SOURCE, (propType) -> {
          if (!propType.isIdentity()) {
            throw new IllegalStateException(propType.propertyName() + " must be primary key");
          }
          String pipelineName = plan.getSourceEnd().getType() + "_to_" + plan.getTargetEnd().getType();
          // List<IAppSource> plugins = HeteroEnum.APP_SOURCE.getPlugins(pluginCtx, UploadPluginMeta.create(HeteroEnum.APP_SOURCE));
          IFieldErrorHandler.BasicPipelineValidator pipelineRules
            = plan.getControlMsgHandler().getPipelineValidator(IFieldErrorHandler.BizLogic.VALIDATE_APP_NAME_DUPLICATE);
          return createNewPrimaryFieldValue(pipelineName, pipelineRules.getExistEntities());
        });

      ctx.put(UploadPluginMeta.KEY_PLUGIN_META, UploadPluginMeta.create(HeteroEnum.APP_SOURCE)
        .putExtraParams(DBIdentity.KEY_UPDATE, Boolean.TRUE.toString()));
      processValMap = validateAttrValMap(ctx, context, HeteroEnum.APP_SOURCE, processValMap);
      final String primaryFieldVal = String.valueOf(processValMap.getAttrVals()
        .getPrimaryVal(processValMap.descriptor.getIdentityField().propertyName()));
      Descriptor.ParseDescribable newPlugin = processValMap.createDescribable(pluginCtx, ctx);
      IPluginStore pluginStore = HeteroEnum.APP_SOURCE.getPluginStore(pluginCtx, UploadPluginMeta.appnameMeta(pluginCtx, primaryFieldVal));
      pluginStore.setPlugins(pluginCtx, Optional.empty(), Collections.singletonList(newPlugin));


      TaskPlan.DataEndCfg endCfg = null;
      DescribableImpl dataXReaderImpl = plan.readerExtendPoints.get(DataxReader.class);
      endCfg = plan.getSourceEnd();
      createPluginInstance(plan, context, endCfg.getRelevantDesc() //
        , Optional.of(endCfg.getType()) //
        , dataXReaderImpl, HeteroEnum.DATAX_READER, (pt) -> {
          return primaryFieldVal;
        });


      endCfg = plan.getTargetEnd();
      DescribableImpl dataXWriterImpl = plan.writerExtendPoints.get(DataxWriter.class);
      createPluginInstance(plan, context, endCfg.getRelevantDesc(), Optional.of(endCfg.getType())
        , dataXWriterImpl, HeteroEnum.DATAX_WRITER //
        , (pt) -> {
          return primaryFieldVal;
        });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return true;
  }

  /**
   *
   * @param plan
   * @param context
   * @param userInput
   * @param endType
   * @param pluginImpl
   * @param heteroEnum
   * @param primaryValRewrite 主键值生成器
   * @return
   * @throws Exception
   */
  private AttrValMap createPluginInstance(TaskPlan plan, AgentContext context, String userInput //
    , Optional<IEndTypeGetter.EndType> endType, DescribableImpl pluginImpl //
    , IPluginEnum heteroEnum, Function<IPropertyType, String> primaryValRewrite) throws Exception {
    Pair<DescriptorsJSONResult, DescriptorsJSONForAIPromote> desc = DescriptorsJSONForAIPromote.desc(pluginImpl);

    DescriptorsJSONForAIPromote forAIPromote = desc.getValue();
    DescribableImpl propImplInfo = null;
    Descriptor implDesc = null;
    Map<String, PluginExtraProps.FieldRefCreateor> propsImplRefs = null;
    Map<Class<? extends Descriptor>, DescribableImpl> fieldDescRegister = forAIPromote.getFieldDescRegister();
    Descriptor installedPluginDescriptor = null;
    final Map<String, IdentityName> propsImplRefsVals = Maps.newHashMap();

    for (Map.Entry<Class<? extends Descriptor>, DescribableImpl> entry : fieldDescRegister.entrySet()) {
      propImplInfo = entry.getValue();
      implDesc = propImplInfo.getImplDesc();
      propsImplRefs = implDesc.getPropsImplRefs();
      PluginExtraProps.FieldRefCreateor refCreateor = null;

      fieldValCreate:
      for (Map.Entry<String /**fieldName*/, PluginExtraProps.FieldRefCreateor> e : propsImplRefs.entrySet()) {
        refCreateor = e.getValue();

        Object dftVal = null;
        if ((dftVal = refCreateor.getDftValue()) != null) {
          // 有默认值直接设置，进入下一个字段
          propsImplRefsVals.put(e.getKey(), IdentityName.create(String.valueOf(dftVal)));
          continue fieldValCreate;
        }

        List<PluginExtraProps.CandidatePlugin> candidatePlugins = refCreateor.getCandidatePlugins();
        // 已经存在的opts，需要根据用户提交的信息内容判断是否可以选择已经有的opts
        if (CollectionUtils.isEmpty(candidatePlugins)
          && refCreateor.getAssistType() == PluginExtraProps.RouterAssistType.hyperlink) {
          // 说明是类似 DefaultDataxProcessor的dptId这样的属性，那必须要有一个默认值
          List<Option> valOpts = refCreateor.getValOptions();
          for (Option opt : valOpts) {
            propsImplRefsVals.put(e.getKey(), IdentityName.create(String.valueOf(opt.getValue())));
            continue fieldValCreate;
          }
          throw new IllegalStateException("impl:" + implDesc.getId() + " of prop " + e.getKey() + " relevant opt vals can nto be empty");
        }

        List<Option> existOpts = refCreateor.getValOptions();
        if (CollectionUtils.isEmpty(candidatePlugins)) {
          throw new IllegalStateException(e.getKey() + "，candidatePlugins can not be empty");
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
        AttrValMap pluginVals = createInnerPluginInstance(plan, context, userInput, refCreateor
          , Objects.requireNonNull(candidatePlugin
            , "candidatePlugin can not be null for field:" + e.getKey()));

        // 遍历已经存在的所有实例
        IPluginEnum hetero = candidatePlugin.getHetero();
        for (Option option : existOpts) {
          Describable plugin = (Describable) hetero.findPlugin(
            candidatePlugin, IdentityName.create(String.valueOf(option.getValue())));
          if (plugin == null) {
            continue;
          }
          // plugin 是否和 pluginVals 相等？
          if (!StringUtils.equals(plugin.getClass().getName(), pluginVals.descriptor.clazz.getName())) {
            continue;
          }

          if (isPluginEqual(plugin, pluginVals.getAttrVals())) {
            if ((plugin instanceof IdentityName)) {
              throw new IllegalStateException("plugin:"
                + plugin.getClass().getName() + " must be type of " + IdentityName.class.getSimpleName());
            }
            propsImplRefsVals.put(e.getKey(), ((IdentityName) plugin));
            continue fieldValCreate;
          }
        }

        Context ctx = new DefaultContext();
        PartialSettedPluginContext msgHandler = createPluginContext();

        /**
         * 生成新的主键
         */
        final String primaryFieldKey = pluginVals.descriptor.getIdentityField().propertyName();
//        pluginVals.getAttrVals().setPrimaryVal(
//          primaryFieldKey, candidatePlugin.createNewPrimaryFieldValue(existOpts));

        Descriptor.ParseDescribable plugin = pluginVals.createDescribable(msgHandler, ctx);
        // 需要持久化
        IPluginEnum pluginEnum = candidatePlugin.getHetero();
        IdentityName pluginRef = (IdentityName) plugin.getInstance();
        IPluginStore pluginStore = null;
        IPluginContext pluginCtx = null;
        switch (refCreateor.getAssistType()) {
          case paramCfg:
            pluginStore = pluginEnum.getPluginStore(
              null, ParamsConfigPluginStore.createParamsConfig(pluginEnum, candidatePlugin));
          case dbQuickManager:
            pluginCtx = IPluginContext.namedContext(new DataXName(pluginRef.identityValue(), StoreResourceType.DataBase))
              .setTargetRuntimeContext((IPluginContext) plan.getControlMsgHandler());
            pluginStore
              = pluginEnum.getPluginStore(
              pluginCtx, PostedDSProp.createPluginMeta(DBIdentity.parseId(pluginRef.identityValue()), false)
                .putExtraParams(DBIdentity.KEY_TYPE, DbScope.DETAILED.getToken()));
            break;
          default:
            throw new IllegalStateException("illegal assistType:" + refCreateor.getAssistType()
              + "for field:" + primaryFieldKey + " of plugin:" + pluginVals.descriptor.getId());
        }

        /**
         * 持久化保存
         */
        Objects.requireNonNull(pluginStore, "pluginStore can not be null")
          .setPlugins(pluginCtx, Optional.of(plan.getRuntimeContext()), Collections.singletonList(plugin));

        propsImplRefsVals.put(e.getKey(), pluginRef);
      }
    }


    LLMProvider llmProvider = plan.getLLMProvider();

    // pluginImpl.getImplDesc();
    Objects.requireNonNull(propsImplRefsVals, "propsImplRefsVals can not be null");
    final Map<String, PluginExtraProps.FieldRefCreateor> propsImplRefsCopy
      = Objects.requireNonNull(propsImplRefs, "propsImplRefs can not be null");
    for (Map.Entry<String, JSONObject> entry : desc.getLeft().getDescriptorsResult().entrySet()) {
      // 需要遍历他的所有属性如果有需要创建的属性插件需要先创建
      JSONObject pluginPostBody
        = extractUserInput2Json(userInput, endType, Objects.requireNonNull(entry.getValue()), llmProvider);
      final AttrValMap[] attrValMap = new AttrValMap[1];
      attrValMap[0] = parseDescribableMap(
        Optional.empty(), pluginPostBody, ((propType, val) -> {

          // 需要判断 是否有可用的已经存在的插件实例可用，
          // 如果没有：则需要创建
          // 如果有：需要便利已经存在的插件确认是否是相同的
//          if (propType.isIdentity()) {
//
//          }
          IdentityName refPropVal = propsImplRefsVals.get(propType.propertyName());
          if (refPropVal == null) {
            if (propsImplRefsCopy.get(propType.propertyName()) != null) {
              throw new IllegalStateException("field:" + propType.propertyName() + " relevant refPropVal can not be null");
            } else if (val != null) {
              if (propType.isIdentity() && val instanceof String && StringUtils.isEmpty((String) val)) {
                String primaryFieldVal = primaryValRewrite.apply(propType);
                Objects.requireNonNull(attrValMap[0], "attrValMap")
                  .getAttrVals().setPrimaryVal(propType.propertyName(), primaryFieldVal);
                return primaryFieldVal;
              } else {
                return val;
              }
            }
          } else {
            if (val == null) {
              return refPropVal.identityValue();
            }
          }


          return val;
        }));
      return attrValMap[0];
    }

    throw new IllegalStateException("can not create AttrValMap , desc.getLeft().getDescriptorsResult() size:"
      + desc.getLeft().getDescriptorsResult().size());
  }

  private boolean isPluginEqual(Describable plugin, AttrVals pluginVals) throws Exception {
    // 空值检查
    Descriptor desc = Objects.requireNonNull(plugin, "plugin can not be null").getDescriptor();
    PluginFormProperties propertyTypes = desc.getPluginFormPropertyTypes();

    return propertyTypes.accept(new PluginFormProperties.IVisitor() {
      @Override
      public Boolean visit(RootFormProperties props) {
        try {
          PropertyType pt = null;
          String fieldName = null;
          JSONObject describle = null;
          for (Map.Entry<String, PropertyType> entry : props.getSortedUseableProperties()) {
            pt = entry.getValue();
            fieldName = entry.getKey();
            Object exist = null;
            if (pt.isIdentity()) {
              continue;
            }
            exist = pt.getFrontendOutput(plugin);

            if (pt.isDescribable()) {
              // 检查 pluginVals 中是否存在该字段

              describle = pluginVals.getAttrVal(fieldName);
              if (describle == null) {
                throw new IllegalStateException("fieldName:" + fieldName + " relevant describle can not be null");
              }

              Object vals = describle.get(PLUGIN_EXTENSION_VALS);
              if (vals == null) {
                // 如果 exist 也是 null，则认为相等
                if (exist == null) {
                  continue;
                }
                return false;
              }

              if (!isPluginEqual((Describable) exist, Descriptor.parseAttrValMap(vals))) {
                return false;
              }

            } else {
              try {
                Object primaryVal = pluginVals.getPrimaryVal(fieldName);
                if (!StringUtils.equals(String.valueOf(exist), String.valueOf(primaryVal))) {
                  return false;
                }
              } catch (Exception e) {
                // 如果获取属性时出现异常，可能是字段不存在，检查现有值是否为 null
                if (exist != null) {
                  return false;
                }
              }
            }
          }
          return true;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Void visit(BaseSubFormProperties props) {
        // 对于子表单属性，暂时不支持比较
        throw new UnsupportedOperationException();
      }
    });
  }

  /**
   * 通过userInput 和 descriptorJson 生成提交的json内容
   *
   * @param userInput
   * @param endType
   * @param descriptorJson
   * @param llmProvider
   * @return
   */
  public JSONObject extractUserInput2Json(String userInput
    , Optional<IEndTypeGetter.EndType> endType, JSONObject descriptorJson, LLMProvider llmProvider) {
    String prompt = "用户输入内容：" + userInput + "\n 参照json结构说明，如下：\n" + JsonUtil.toString(descriptorJson, true);
    String systemPrompt = "你是TIS数据集成平台的智能助手。你的任务是帮助用户创建数据同步管道。\n" +
      endType.map((end) -> "当前处理的数据端是针对：" + String.valueOf(end)).orElse(StringUtils.EMPTY) + "\n" +
      "现在需要通过用户提交的内容，结合提系统提供的json结构说明，解析出结构化的json作为输出内容";

    JSONObject pluginPostBody = null;
    final String jsonSchema = "\n\n请严格按照系统提示中输出json的Schema格式返回结果";
    try {
      LLMProvider.LLMResponse llmResponse = llmProvider.chatJson(prompt
        , Lists.newArrayList(systemPrompt
          , IOUtils.toString(PluginInstanceCreateExecutor.class.getResourceAsStream("describle_plugin_json_create_deamo.md"), TisUTF8.get())), jsonSchema);
      return pluginPostBody = llmResponse.getJsonContent();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 通过用户提交的内容生成plugin封装，并且进行校验，直到校验成功之后返回封装实例
   *
   * @param plan
   * @param context
   * @param userInput
   * @param candidate
   * @return
   * @throws Exception
   */
  private AttrValMap createInnerPluginInstance(TaskPlan plan, AgentContext context, String userInput
    , PluginExtraProps.FieldRefCreateor refCreateor //
    , PluginExtraProps.CandidatePlugin candidate) throws Exception {
    Descriptor installedPluginDescriptor = candidate.getInstalledPluginDescriptor();
    AttrValMap valMap = createPluginInstance(plan, context, userInput, Optional.empty()
      , new DescribableImpl(candidate.getHetero().getExtensionPoint(), Optional.empty())
        .setDescriptor(
          Objects.requireNonNull(installedPluginDescriptor
            , "candidate:" + candidate + ",installedPluginDescriptor can not be null")), candidate.getHetero()
      , (propType) -> {
        if (!propType.isIdentity()) {
          throw new IllegalStateException("propType:" + propType.propertyName() + " must be primary field");
        }
        List<Option> existOpts = refCreateor.getValOptions();
        return candidate.createNewPrimaryFieldValue(existOpts);
      });
//    Context ctx = new DefaultContext();
    return validateAttrValMap(plan.getRuntimeContext(), context, candidate.getHetero(), valMap);
  }

  private AttrValMap validateAttrValMap(Context ctx, AgentContext context, IPluginEnum pluginEnum, AttrValMap valMap) throws Exception {
    /**
     * 需要对valMap进行校验
     */
    // Context ctx = new DefaultContext(); //
    Descriptor.FormVaildateType verify = Descriptor.FormVaildateType.create(true);
    Descriptor.FormVaildateType validate = Descriptor.FormVaildateType.create(false);
    Descriptor.PluginValidateResult.setValidateItemPos(ctx, 0, 0);

    PartialSettedPluginContext msgHandler = createPluginContext();

    if (!valMap.validate(msgHandler, ctx, verify, Optional.empty()).isValid()
      || !valMap.validate(msgHandler, ctx, validate, Optional.empty()).isValid()) {

      final SessionKey requestId = SessionKey.create();
      //
      /***
       * 此处需要用户将不足的属性补足
       ***/
      context.sendPluginConfig(requestId, pluginEnum, valMap.descriptor.getId(), valMap);

      /***
       * 等到用户选择
       ***/
      PluginPropsComplement pluginProps
        = context.waitForUserSelection(requestId, (pp) -> {
        return pp != null && pp.getPluginValMap() != null;
      });
      return Objects.requireNonNull(pluginProps, "validate pluginProps can not be null").getPluginValMap();
    }

    return valMap;
  }

  private static PartialSettedPluginContext createPluginContext() {
    DefaultMessageHandler messageHandler = new DefaultMessageHandler();
    PartialSettedPluginContext msgHandler = IPluginContext.namedContext("test")
      .setMessageAndFieldErrorHandler(messageHandler, messageHandler);
    return msgHandler;
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
    , Map.Entry<String, PluginExtraProps.FieldRefCreateor> e //
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

    SessionKey requestId = SessionKey.create();// "plugin_select_" + System.currentTimeMillis();

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
    SelectionOptions selectedIndex = context.waitForUserSelection(requestId, (selOpts) -> {
      return (selOpts != null && selOpts.hasSelectedOpt());
    });
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
