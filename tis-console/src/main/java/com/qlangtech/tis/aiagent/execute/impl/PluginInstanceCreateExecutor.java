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
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.aiagent.core.PluginPropsComplement;
import com.qlangtech.tis.aiagent.core.RequestKey;
import com.qlangtech.tis.aiagent.core.SelectionOptions;
import com.qlangtech.tis.aiagent.core.TableSelectApplySessionData;
import com.qlangtech.tis.aiagent.execute.StepExecutor;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.coredefine.module.action.DataxAction;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.TableAlias;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.AppAndRuntime;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.offline.DbScope;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.credentials.ParamsConfigPluginStore;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.plugin.ds.TableNotFoundException;
import com.qlangtech.tis.runtime.module.misc.FormVaildateType;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescriptorsJSONForAIPromote;
import com.qlangtech.tis.util.DescriptorsJSONResult;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.ItemsSaveResult;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.PluginItems;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.util.impl.AttrVals;
import groovyjarjarantlr4.v4.parse.v3TreeGrammarException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.qlangtech.tis.datax.StoreResourceType.DATAX_NAME;
import static com.qlangtech.tis.datax.impl.ESTableAlias.MAX_READER_TABLE_SELECT_COUNT;
import static com.qlangtech.tis.extension.Descriptor.KEY_DESC_VAL;
import static com.qlangtech.tis.extension.SubFormFilter.PLUGIN_META_SUB_FORM_FIELD;
import static com.qlangtech.tis.extension.util.PluginExtraProps.CandidatePlugin.createNewPrimaryFieldValue;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_VALS;
import static com.qlangtech.tis.util.AttrValMap.parseDescribableMap;
import static com.qlangtech.tis.util.UploadPluginMeta.KEY_REQUIRE;
import static com.qlangtech.tis.util.UploadPluginMeta.PLUGIN_META_TARGET_DESCRIPTOR_IMPLEMENTION;
import static com.qlangtech.tis.util.UploadPluginMeta.PLUGIN_META_TARGET_DESCRIPTOR_NAME;

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

      Context ctx = plan.getRuntimeContext(false);

      /**
       * support for DefaultDataxProcessor$DescriptorImpl.getManipulateStore()
       */
      AppAndRuntime.setAppAndRuntime(new AppAndRuntime());

//      // final String prefix = plan.getSourceEnd().getType() + "_to_";
//      IdentityName primaryFieldVal = (createNewPrimaryFieldValue(
//        prefix + plan.getTargetEnd().getType()
//        , pipelineRules.getExistEntities(Optional.of(prefix))));

      AttrValMap pluginVals = createPluginInstance(plan, context, new UserPrompt("正在生成管道主体配置...", plan.getUserInput()) //
        , Optional.empty() //
        , plan.processorExtendPoints, HeteroEnum.APP_SOURCE, new IPrimaryValRewrite() {
          IFieldErrorHandler.BasicPipelineValidator pipelineRules
            = plan.getControlMsgHandler().getPipelineValidator(IFieldErrorHandler.BizLogic.VALIDATE_APP_NAME_DUPLICATE);

          @Override
          public IdentityName newCreate(PropertyType pp) {
            if (!pp.isIdentity()) {
              throw new IllegalStateException("property " + pp.propertyName() + " must identity field");
            }
            final String prefix = plan.getSourceEnd().getType() + "_to_";
            return (createNewPrimaryFieldValue(
              prefix + plan.getTargetEnd().getType()
              , pipelineRules.getExistEntities(Optional.of(prefix))));
          }

          @Override
          public boolean isDuplicateInExistEntities(PropertyType pk, String identityFieldVal) {
            // 查找是否存在已经有的
            List<IdentityName> exist = pipelineRules.getExistEntities(Optional.of(identityFieldVal));
            return exist.stream().anyMatch((id) -> StringUtils.equals(id.identityValue(), identityFieldVal));
          }
        });
      IdentityName primaryFieldVal = IdentityName.create(pluginVals.getPrimaryFieldVal());

      context.sendMessage("创建名称为：`" + primaryFieldVal.identityValue() + "`的数据通道");

      PartialSettedPluginContext pluginCtx = createPluginContext(
        plan, DataXName.createDataXPipeline(primaryFieldVal.identityValue()));
      UploadPluginMeta processMeta = UploadPluginMeta.appnameMeta(pluginCtx, primaryFieldVal.identityValue());

      Describable process = createPluginAndStore(HeteroEnum.APP_SOURCE, plan, context, ctx, pluginCtx, processMeta, pluginVals);

      //=====================================================
      IPrimaryValRewrite primaryValRewrite = (pkField) -> ((IdentityName) process);
      TaskPlan.DataEndCfg endCfg = plan.getSourceEnd();
      DescribableImpl dataXReaderImpl = plan.readerExtendPoints.get(DataxReader.class);

      DataxReader dataXReader = createPluginAndStore(HeteroEnum.DATAX_READER, plan, Optional.of(endCfg.getType())
        , dataXReaderImpl
        , new UserPrompt("正在生成源端" + endCfg.getType() + "主体配置...", endCfg.getRelevantDesc())
        , context, primaryValRewrite, ctx, pluginCtx, processMeta);

      /**********************************************************
       * 选择表
       **********************************************************/
      UploadPluginMeta selectedTabsMeta = UploadPluginMeta.parse(pluginCtx
        , HeteroEnum.DATAX_READER.identity + ":" + KEY_REQUIRE
          + "," + PLUGIN_META_TARGET_DESCRIPTOR_IMPLEMENTION + "_" + dataXReaderImpl.getImplDesc().getId()
          + "," + PLUGIN_META_TARGET_DESCRIPTOR_NAME + "_" + dataXReaderImpl.getImplDesc().getDisplayName()
          + "," + PLUGIN_META_SUB_FORM_FIELD + "_selectedTabs,"
          + DATAX_NAME + "_" + primaryFieldVal.identityValue()
          + "," + MAX_READER_TABLE_SELECT_COUNT + "_999", false);
      IPluginStore tabsStore = HeteroEnum.DATAX_READER.getPluginStore(pluginCtx, selectedTabsMeta);

      TaskPlan.SourceDataEndCfg sourceEnd = plan.getSourceEnd();

      List<Descriptor.ParseDescribable> selectedTabs = Lists.newArrayList();
      if (CollectionUtils.isNotEmpty(sourceEnd.getSelectedTabs())) {
        // 当用户明确说明需要同步哪些表
        /**
         *  action=plugin_action&emethod=subform_detailed_click&plugin=dataxReader:require,targetDescriptorImpl_com.qlangtech.tis.plugin.datax.DataxMySQLReader,targetDescriptorName_MySQL,subFormFieldName_selectedTabs,dataxName_mysql_to_doris_6,subformDetailIdValue_base&id=base
         *  @see com.qlangtech.tis.coredefine.module.action.PluginAction#doSubformDetailedClick
         *
         *  以下是初始化表的值<br/>
         *  emethod=get_ds_tabs_vals&action=offline_datasource_action<br>
         *  以下是提交的body内容
         *  {
         * 	"tabs": ["base"],
         * 	"name": "dataxReader",
         * 	"require": true,
         * 	"extraParam": "targetDescriptorImpl_com.qlangtech.tis.plugin.datax.DataxMySQLReader,targetDescriptorName_MySQL,subFormFieldName_selectedTabs,dataxName_mysql_to_doris_6,maxReaderTableCount_9999"
         * }
         * @see com.qlangtech.tis.offline.module.action.OfflineDatasourceAction#doGetDsTabsVals(Context)
         */
        List<ISelectedTab> tabs = null;
        List<String> notFoundTabs = Collections.emptyList();
        try {
          tabs = dataXReader.createDefaultTables(pluginCtx
            , sourceEnd.getSelectedTabs(), selectedTabsMeta, (entry) -> {
            }, false);
        } catch (Exception e) {
          int expIdx;
          // 目标表无法识别到
          if ((expIdx = ExceptionUtils.indexOfThrowable(e, TableNotFoundException.class)) > -1) {
            TableNotFoundException tabNotFoundException = ExceptionUtils.throwableOfThrowable(e, TableNotFoundException.class, expIdx);
            notFoundTabs = Collections.singletonList(tabNotFoundException.tableName);
          } else {
            throw e;
          }
        }

        if (CollectionUtils.isNotEmpty(notFoundTabs)) {
          sendTableSelectApply(context, "用户提交的表：" + notFoundTabs.stream().map((tab) -> "'" + tab + "'").collect(Collectors.joining(","))
            + "在源库中没有识别到, 请重新设置", primaryFieldVal, dataXReaderImpl);
        } else if (CollectionUtils.isEmpty(tabs)) {
          sendTableSelectApply(context, "用户提交的表：" + sourceEnd.getSelectedTabs().stream().map((tab) -> "'" + tab + "'").collect(Collectors.joining(","))
            + "在源库中没有识别到, 请重新设置", primaryFieldVal, dataXReaderImpl);
        } else {
          selectedTabs.add(new Descriptor.ParseDescribable(tabs));
          if (CollectionUtils.isEmpty(selectedTabs)) {
            throw new IllegalStateException("selectedTabs can not be null");
          }
          tabsStore.setPlugins(pluginCtx, Optional.of(ctx), selectedTabs);
          if (sourceEnd.getSelectedTabs().size() != tabs.size()) {
            sendTableSelectApply(context, "用户提交的表：" + sourceEnd.getSelectedTabs().stream().map((tab) -> "'" + tab + "'").collect(Collectors.joining(","))
              + "与源库中识别到的表：" + tabs.stream().map((tab) -> "'" + tab + "'").collect(Collectors.joining(",")) + "有出入,请设置", primaryFieldVal, dataXReaderImpl);
          }
        }
      } else {
        sendTableSelectApply(context, "请选择源库中需要同步的表", primaryFieldVal, dataXReaderImpl);
      }


      //=====================================================
      endCfg = plan.getTargetEnd();
      DescribableImpl dataXWriterImpl = plan.writerExtendPoints.get(DataxWriter.class);

      createPluginAndStore(HeteroEnum.DATAX_WRITER, plan, Optional.of(endCfg.getType())
        , dataXWriterImpl
        , new UserPrompt("正在生成目标端" + endCfg.getType() + "主体配置...", endCfg.getRelevantDesc())
        , context, primaryValRewrite, ctx, pluginCtx, processMeta);

      // TableAlias.saveTableMapper(this, dataxName, tableMaps);

      /*******************************************************
       * 创建管道
       *******************************************************/
      ((RunContext) plan.getControlMsgHandler())
        .createPipeline(plan.getRuntimeContext(false), primaryFieldVal.identityValue());

      // 创建datax配置和SQL脚本
      DataxAction.generateDataXCfgs(pluginCtx, ctx, StoreResourceType.DataApp, primaryFieldVal.identityValue(), false);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return true;
  }

  private static void sendTableSelectApply(AgentContext context, String reasonDetail, IdentityName primaryFieldVal, DescribableImpl dataXReaderImpl) throws Exception {
    // 当用户没有说明需要同步哪些表，需要询问用户，让用户辅助输入需要同步的表
    final RequestKey requestId = RequestKey.create();

    context.sendTableSelectApply(requestId
      , reasonDetail, primaryFieldVal, dataXReaderImpl);

    TableSelectApplySessionData selectionSessionData
      = context.waitForUserPost(requestId, TableSelectApplySessionData::isTableSelectConfirm);
  }

  private <PLUGIN extends Describable> PLUGIN createPluginAndStore(HeteroEnum hetero
    , TaskPlan plan, Optional<IEndTypeGetter.EndType> endType, DescribableImpl pluginImpl
    , UserPrompt userInput
    , AgentContext context, IPrimaryValRewrite primaryValRewrite
    , Context ctx, PartialSettedPluginContext pluginCtx, UploadPluginMeta pluginMetaMeta) throws Exception {
    AttrValMap pluginVals = createPluginInstance(plan, context, userInput //
      , endType //
      , pluginImpl, hetero, primaryValRewrite);
    return createPluginAndStore(hetero, plan, context, ctx, pluginCtx, pluginMetaMeta, pluginVals);
  }

  private <PLUGIN extends Describable> PLUGIN createPluginAndStore(HeteroEnum hetero
    , TaskPlan plan, AgentContext context
    , Context ctx, PartialSettedPluginContext pluginCtx, UploadPluginMeta pluginMetaMeta, AttrValMap pluginVals) throws Exception {

    /**
     * 先进行校验
     */
    pluginVals = validateAttrValMap(plan, context, hetero
      , pluginVals, Optional.ofNullable(pluginMetaMeta.getDataXName(false)));
    Descriptor.ParseDescribable newPlugin = pluginVals.createDescribable(pluginCtx, ctx);
    IPluginStore pluginStore = hetero.getPluginStore(pluginCtx, pluginMetaMeta);
    pluginStore.setPlugins(pluginCtx, Optional.empty(), Collections.singletonList(newPlugin));
    return (PLUGIN) newPlugin.getInstance();
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
  private AttrValMap createPluginInstance(TaskPlan plan, AgentContext context, UserPrompt userInput //
    , Optional<IEndTypeGetter.EndType> endType, DescribableImpl pluginImpl //
    , IPluginEnum heteroEnum, IPrimaryValRewrite primaryValRewrite) throws Exception {
    Pair<DescriptorsJSONResult, DescriptorsJSONForAIPromote> desc = DescriptorsJSONForAIPromote.desc(pluginImpl);

    DescriptorsJSONForAIPromote forAIPromote = desc.getValue();
    DescribableImpl propImplInfo = null;
    Descriptor implDesc = null;
    // Map<String, PluginExtraProps.FieldRefCreateor> propsImplRefs = null;
    Map<Class<? extends Descriptor>, DescribableImpl> fieldDescRegister = forAIPromote.getFieldDescRegister();

    Map<String, IdentityName> propsImplRefsVals = null;

    for (Map.Entry<Class<? extends Descriptor>, DescribableImpl> entry : fieldDescRegister.entrySet()) {
      propImplInfo = entry.getValue();
      implDesc = propImplInfo.getImplDesc();
      // propsImplRefs = implDesc.getPropsImplRefs();
      // PluginExtraProps.FieldRefCreateor refCreateor = null;

      propsImplRefsVals = setPropsImplRefsVals(plan, context, userInput, endType, implDesc);
      break;
    }
    Objects.requireNonNull(propsImplRefsVals, "propsImplRefsVals can not be null");
    LLMProvider llmProvider = plan.getLLMProvider();

    for (Map.Entry<String, JSONObject> entry : desc.getLeft().getDescriptorsResult().entrySet()) {
      // 需要遍历他的所有属性如果有需要创建的属性插件需要先创建
      JSONObject pluginPostBody
        = extractUserInput2Json(context, userInput, endType, Objects.requireNonNull(entry.getValue()), llmProvider);
      AttrValMap attrValMap = parseDescribableMap(Optional.empty(), pluginPostBody);

      if (attrValMap.descriptor.getIdentityField(false) != null) {
        PropertyType pk = attrValMap.descriptor.getIdentityField();
        if (attrValMap.isPrimaryFieldEmpty() || primaryValRewrite.isDuplicateInExistEntities(pk
          , Objects.requireNonNull(attrValMap.getPrimaryFieldVal(), "PrimaryFieldVal can not be empty"))) {
          // 1. 没有主键的情况下，由agent自主生成主键值
          // 2. 识别到用户提交的主键的情况下，需要判断是否和已经有的主键列表冲突，如果冲突也需要重新生成
          IdentityName primaryFieldVal = primaryValRewrite.newCreate(pk);
          if (primaryFieldVal != null) {
            attrValMap
              .getAttrVals().setPrimaryVal(pk.propertyName(), primaryFieldVal.identityValue());
          }
        }
      }

      for (Map.Entry<String, IdentityName> refProp : propsImplRefsVals.entrySet()) {
        final String propName = refProp.getKey();
        IdentityName refPropVal = refProp.getValue();
        attrValMap
          .getAttrVals().setPrimaryVal(propName
            , Objects.requireNonNull(refPropVal, "refPropVal can not be null").identityValue());
      }


      return attrValMap;
    }

    throw new IllegalStateException("can not create AttrValMap , desc.getLeft().getDescriptorsResult() size:"
      + desc.getLeft().getDescriptorsResult().size());
  }

  private Map<String, IdentityName> setPropsImplRefsVals(TaskPlan plan, AgentContext context
    , UserPrompt userInput, Optional<IEndTypeGetter.EndType> endType, Descriptor implDesc) throws Exception {
    Map<String, IdentityName> propsImplRefsVals = Maps.newHashMap();
    Map<String, PluginExtraProps.FieldRefCreateor> propsImplRefs = implDesc.getPropsImplRefs();
    Descriptor installedPluginDescriptor;
    PluginExtraProps.FieldRefCreateor refCreateor;
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

      //
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

      List<Option> existOpts = refCreateor.getValOptions();
      // 开始实例化插件
      AttrValMap pluginVals = createInnerPluginInstance(plan, context, userInput.setAbstract("解析'" + candidatePlugin.getTargetItemDesc() + "'插件内容"), refCreateor
        , Objects.requireNonNull(candidatePlugin
          , "candidatePlugin can not be null for field:" + e.getKey()));

      /**
       * 找历史记录中是否有相同的实例，避免重复创建相同的实例
       */
      Optional<Describable> existPlugin = this.findExistPlugin(candidatePlugin, pluginVals, existOpts);
      if (existPlugin.isPresent()) {
        propsImplRefsVals.put(e.getKey(), ((IdentityName) existPlugin.get()));
        continue fieldValCreate;
      }

      Context ctx = plan.getRuntimeContext(true);
      // 需要持久化
      IPluginEnum pluginEnum = candidatePlugin.getHetero();
      IdentityName pluginRef = IdentityName.create(pluginVals.getPrimaryFieldVal());
      IPluginContext pluginCtx = null;
      PluginItems pItems = null;
      switch (refCreateor.getAssistType()) {
        case paramCfg: {
          pItems = new PluginItems(pluginCtx, ctx, ParamsConfigPluginStore.createParamsConfig(pluginEnum, candidatePlugin));
          break;
        }
        case dbQuickManager: {
          pluginCtx = IPluginContext.namedContext(new DataXName(pluginRef.identityValue(), StoreResourceType.DataBase))
            .setTargetRuntimeContext((IPluginContext) plan.getControlMsgHandler());
          pItems = new PluginItems(pluginCtx, ctx, PostedDSProp.createPluginMeta(DBIdentity.parseId(pluginRef.identityValue()), false)
            .putExtraParams(DBIdentity.KEY_TYPE, DbScope.DETAILED.getToken()));
          break;
        }
        default: {
          final String primaryFieldKey = pluginVals.descriptor.getIdentityField().propertyName();
          throw new IllegalStateException("illegal assistType:" + refCreateor.getAssistType()
            + "for field:" + primaryFieldKey + " of plugin:" + pluginVals.descriptor.getId());
        }
      }
      pItems.items = Collections.singletonList(pluginVals);
      ItemsSaveResult saved = pItems.save(ctx);
      if (!saved.cfgSaveResult.success) {
        throw new IllegalStateException("identity:" + pluginRef.identityValue() + " of plugin save process faild");
      }
      /**
       * 持久化保存
       */
      propsImplRefsVals.put(e.getKey(), pluginRef);
    }
    return propsImplRefsVals;
  }

  private <OPTION extends IdentityName> Optional<Describable> findExistPlugin(
    PluginExtraProps.CandidatePlugin candidatePlugin, AttrValMap pluginVals, List<OPTION> existOpts) throws Exception {
    // 遍历已经存在的所有实例
    IPluginEnum hetero = candidatePlugin.getHetero();
    for (IdentityName option : existOpts) {
      Describable plugin = (Describable) hetero.findPlugin(candidatePlugin, option);
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
        return Optional.of(plugin);
      }
    }
    return Optional.empty();
  }

  boolean isPluginEqual(Describable plugin, AttrVals pluginVals) throws Exception {
    // 空值检查
    Descriptor desc = Objects.requireNonNull(plugin, "plugin can not be null").getDescriptor();
    PluginFormProperties propertyTypes = desc.getPluginFormPropertyTypes();

    return Objects.requireNonNull(propertyTypes, "propertyTypes can not be null")
      .accept(new PluginFormProperties.IVisitor() {
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
                  // throw new IllegalStateException("fieldName:" + fieldName + " relevant describle can not be null");
                  return false;
                }

                Object vals = Objects.requireNonNull(describle.getJSONObject(KEY_DESC_VAL)
                  , "key:" + KEY_DESC_VAL + " relevant json can not be null").get(PLUGIN_EXTENSION_VALS);
                if (vals == null) {
                  // 如果 exist 也是 null，则认为相等
                  if (exist == null) {
                    continue;
                  }
                  return false;
                }

                if (exist == null) {
                  return false;
                }

                if (!isPluginEqual((Describable) exist, AttrVals.parseAttrValMap(vals))) {
                  return false;
                }

              } else {
                Object primaryVal = pluginVals.getPrimaryVal(fieldName);
                if (primaryVal == null && exist == null) {
                  continue;
                }
                if (primaryVal == null ^ exist == null) {
                  return false;
                }
                if (!StringUtils.equals(String.valueOf(exist), String.valueOf(primaryVal))) {
                  return false;
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
  public JSONObject extractUserInput2Json(IAgentContext context, UserPrompt userInput
    , Optional<IEndTypeGetter.EndType> endType, JSONObject descriptorJson, LLMProvider llmProvider) {
    String prompt = "用户输入内容：" + userInput.getPrompt() + "\n 参照json结构说明，如下：\n" + JsonUtil.toString(descriptorJson, true);
    final String systemPrompt = "你是TIS数据集成平台的智能助手。你的任务是帮助用户创建数据同步管道。\n" +
      endType.map((end) -> "当前处理的数据端是针对：" + String.valueOf(end)).orElse(StringUtils.EMPTY) + "\n" +
      "现在需要通过用户提交的内容，结合提系统提供的json结构说明，解析出结构化的json作为输出内容";

    JSONObject pluginPostBody = null;
    final String jsonSchema = "\n\n请严格按照系统提示中输出json的Schema格式返回结果";
    try (InputStream sysPromote = PluginInstanceCreateExecutor.class.getResourceAsStream("describle_plugin_json_create_deamo.md")) {
      LLMProvider.LLMResponse llmResponse = llmProvider.chatJson(context, userInput.setNewPrompt(prompt)
        , Lists.newArrayList(systemPrompt
          , IOUtils.toString(Objects.requireNonNull(sysPromote, "sysPromote can not be null"), TisUTF8.get())), jsonSchema);
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
  private AttrValMap createInnerPluginInstance(TaskPlan plan, AgentContext context, UserPrompt userInput
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
    return validateAttrValMap(plan, context, candidate.getHetero(), valMap, Optional.empty());
  }

  private AttrValMap validateAttrValMap(TaskPlan plan, AgentContext context
    , IPluginEnum pluginEnum, AttrValMap valMap, Optional<DataXName> pipelineName) throws Exception {
    /**
     * 需要对valMap进行校验
     */
    final Context ctx = plan.getRuntimeContext(true);
    FormVaildateType verify = FormVaildateType.create(true);
    FormVaildateType validate = FormVaildateType.create(false);
    Descriptor.PluginValidateResult.setValidateItemPos(ctx, 0, 0);

    PartialSettedPluginContext msgHandler = createPluginContext(plan, pipelineName.orElse(null));

    if (!valMap.validate(msgHandler, ctx, verify, Optional.empty()).isValid()
      || !valMap.validate(msgHandler, ctx, validate, Optional.empty()).isValid()) {

      AjaxValve.ActionExecResult validateResult = new AjaxValve.ActionExecResult(ctx).invoke();

      final RequestKey requestId = RequestKey.create();
      // ctx.hasErrors();
      /***
       * 此处需要用户将不足的属性补足
       ***/
      context.sendPluginConfig(requestId, validateResult, pluginEnum, valMap.descriptor.getId(), valMap);

      /***
       * 等到用户选择
       ***/
      PluginPropsComplement pluginProps
        = context.waitForUserPost(requestId, (pp) -> {
        return pp != null && pp.getPluginValMap() != null;
      });
      return Objects.requireNonNull(pluginProps, "validate pluginProps can not be null").getPluginValMap();
    }

    return valMap;
  }

  private static PartialSettedPluginContext createPluginContext(TaskPlan plan, @Nullable DataXName name) {
    PartialSettedPluginContext pluginContext = null;
    if (name != null) {
      pluginContext = IPluginContext.namedContext(name);
    } else {
      pluginContext = new PartialSettedPluginContext();
    }
    return
      pluginContext.setTargetRuntimeContext((IPluginContext) plan.getControlMsgHandler());
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

    RequestKey requestId = RequestKey.create();// "plugin_select_" + System.currentTimeMillis();

    JSONObject optionsData = new JSONObject();
    optionsData.put("fieldName", fieldName);

    JSONArray optionsArray = PluginExtraProps.CandidatePlugin.convertOptionsArray(endType, candidatePlugins);

    optionsData.put("candidates", optionsArray);

    String prompt = String.format("请选择 %s 字段的插件实现", fieldName);
//    context.sendMessage(prompt);
    /************************************************************************
     * 向客户端发送需要用户确认的请求
     ************************************************************************/
    context.requestUserSelection(requestId, prompt, optionsData, candidatePlugins);

    /************************************************************************
     * 等待客户端发送的选择信息
     ************************************************************************/
    SelectionOptions selectedIndex = context.waitForUserPost(requestId, (selOpts) -> {
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
