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
import com.google.common.collect.Lists;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.lang.PayloadLink;
import com.qlangtech.tis.aiagent.core.RequestKey;
import com.qlangtech.tis.aiagent.core.TableSelectApplySessionData;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.coredefine.module.action.DataxAction;
import com.qlangtech.tis.coredefine.module.action.ProcessModel;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.AppAndRuntime;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.plugin.IDataXEndTypeGetter;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.TableNotFoundException;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.qlangtech.tis.datax.StoreResourceType.DATAX_NAME;
import static com.qlangtech.tis.datax.impl.ESTableAlias.MAX_READER_TABLE_SELECT_COUNT;
import static com.qlangtech.tis.extension.SubFormFilter.PLUGIN_META_SUB_FORM_FIELD;
import static com.qlangtech.tis.extension.util.PluginExtraProps.CandidatePlugin.createNewPrimaryFieldValue;
import static com.qlangtech.tis.util.UploadPluginMeta.KEY_REQUIRE;
import static com.qlangtech.tis.util.UploadPluginMeta.PLUGIN_META_TARGET_DESCRIPTOR_IMPLEMENTION;
import static com.qlangtech.tis.util.UploadPluginMeta.PLUGIN_META_TARGET_DESCRIPTOR_NAME;

/**
 * 创建插件实例
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/18
 */
public class PluginInstanceCreateExecutor extends BasicStepExecutor {
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
            = ((IControlMsgHandler) plan.getControlMsgHandler()).getPipelineValidator(IFieldErrorHandler.BizLogic.VALIDATE_APP_NAME_DUPLICATE);

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

      //==========================================


      //=====================================================
      IPrimaryValRewrite primaryValRewrite = (pkField) -> ((IdentityName) process);
      TaskPlan.DataEndCfg endCfg = plan.getSourceEnd();
      DescribableImpl dataXReaderImpl = plan.readerExtendPoints.get(DataxReader.class);
      DescribableImpl dataXWriterImpl = plan.writerExtendPoints.get(DataxWriter.class);
      // 保存元数据
      ProcessModel.CreateDatax.createProcessMeta(pluginCtx, primaryFieldVal.identityValue()
        , (DataxReader.BaseDataxReaderDescriptor) dataXReaderImpl.getImplDesc()
        , (DataxWriter.BaseDataxWriterDescriptor) dataXWriterImpl.getImplDesc());

      DataxReader dataXReader = createPluginAndStore(HeteroEnum.DATAX_READER, plan, Optional.of(endCfg.getType())
        , dataXReaderImpl
        , new UserPrompt("正在生成源端" + endCfg.getType() + "主体配置...", endCfg.getRelevantDesc())
        , context, primaryValRewrite, ctx, pluginCtx, processMeta);
      endCfg.setEndTypeMeta((IDataXEndTypeGetter) dataXReaderImpl.getImplDesc());


      TaskPlan.SourceDataEndCfg sourceEnd = plan.getSourceEnd();
      sourceEnd.setProcessor((IAppSource) process);

      TableSelectApplySessionData selectApplySessionData = null;
      if (CollectionUtils.isNotEmpty(sourceEnd.getSelectedTabs())) {

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

        Assert.assertTrue("subFormFilter must be present"
          , selectedTabsMeta.getSubFormFilter().isPresent());


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
          selectApplySessionData = sendTableSelectApply(context, "用户提交的表：" + notFoundTabs.stream().map((tab) -> "'" + tab + "'").collect(Collectors.joining(","))
            + "在源库中没有识别到, 请重新设置", primaryFieldVal, dataXReaderImpl);
        } else if (CollectionUtils.isEmpty(tabs)) {
          selectApplySessionData = sendTableSelectApply(context, "用户提交的表：" + sourceEnd.getSelectedTabs().stream().map((tab) -> "'" + tab + "'").collect(Collectors.joining(","))
            + "在源库中没有识别到, 请重新设置", primaryFieldVal, dataXReaderImpl);
        } else {

          if (sourceEnd.getSelectedTabs().size() != tabs.size()) {
            selectApplySessionData = sendTableSelectApply(context, "用户提交的表：" + sourceEnd.getSelectedTabs().stream().map((tab) -> "'" + tab + "'").collect(Collectors.joining(","))
              + "与源库中识别到的表：" + tabs.stream().map((tab) -> "'" + tab + "'").collect(Collectors.joining(",")) + "有出入,请设置", primaryFieldVal, dataXReaderImpl);
          } else {
            List<Descriptor.ParseDescribable> selectedTabs = Lists.newArrayList();
            selectedTabs.add(new Descriptor.ParseDescribable(tabs));
            if (CollectionUtils.isEmpty(selectedTabs)) {
              throw new IllegalStateException("selectedTabs can not be null");
            }
            IPluginStore tabsStore = HeteroEnum.getDataXReaderAndWriterStore(pluginCtx, true, selectedTabsMeta, selectedTabsMeta.getSubFormFilter());
            tabsStore.setPlugins(pluginCtx, Optional.of(ctx), selectedTabs);
            selectApplySessionData = new TableSelectApplySessionData();
            selectApplySessionData.setTableSelectConfirm(true);
            selectApplySessionData.setTableSelected(sourceEnd.getSelectedTabs());
          }
        }
      } else {
        selectApplySessionData = sendTableSelectApply(context, "请选择源库中需要同步的表", primaryFieldVal, dataXReaderImpl);
      }

      if (selectApplySessionData != null) {
        AtomicInteger count = new AtomicInteger();
        List<String> targetTabs = selectApplySessionData.getSelectedTabs();
        final int maxShow = 5;
        context.sendMessage("已经识别到导入表："
          + targetTabs.stream().filter((tab) -> count.incrementAndGet() < maxShow).collect(Collectors.joining(","))
          + ((count.get() > maxShow) ? "...等" : StringUtils.EMPTY) + "，共" + targetTabs.size() + "张表");
      }

      //=====================================================
      endCfg = plan.getTargetEnd();


      createPluginAndStore(HeteroEnum.DATAX_WRITER, plan, Optional.of(endCfg.getType())
        , dataXWriterImpl
        , new UserPrompt("正在生成目标端" + endCfg.getType() + "主体配置...", endCfg.getRelevantDesc())
        , context, primaryValRewrite, ctx, pluginCtx, processMeta);
      endCfg.setEndTypeMeta((IDataXEndTypeGetter) dataXWriterImpl.getImplDesc());

      // TableAlias.saveTableMapper(this, dataxName, tableMaps);

      /*******************************************************
       * 创建管道
       *******************************************************/
      ((RunContext) plan.getControlMsgHandler())
        .createPipeline(plan.getRuntimeContext(false), primaryFieldVal.identityValue());

      // 创建datax配置和SQL脚本
      DataxAction.generateDataXCfgs(pluginCtx, ctx, StoreResourceType.DataApp, primaryFieldVal.identityValue(), false);

      // /x/mysql_to_doris_3/manage
      // context.sendMessage();
      context.sendMessage("数据管道：`" + primaryFieldVal.identityValue() + "`已经创建成功"
        , new PayloadLink("管理", "/x/" + primaryFieldVal.identityValue() + "/manage"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return true;
  }

  private static TableSelectApplySessionData sendTableSelectApply(
    AgentContext context, String reasonDetail, IdentityName primaryFieldVal, DescribableImpl dataXReaderImpl) throws Exception {
    // 当用户没有说明需要同步哪些表，需要询问用户，让用户辅助输入需要同步的表
    final RequestKey requestId = RequestKey.create();

    context.sendTableSelectApply(requestId
      , reasonDetail, primaryFieldVal, dataXReaderImpl);

    TableSelectApplySessionData selectionSessionData
      = context.waitForUserPost(requestId, TableSelectApplySessionData::isTableSelectConfirm);
    return selectionSessionData;
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


  @Override
  public ValidationResult validate(TaskStep step) {
    return ValidationResult.success();
  }

  @Override
  public TaskStep.StepType getSupportedType() {
    return TaskStep.StepType.PLUGIN_CREATE;
  }
}
