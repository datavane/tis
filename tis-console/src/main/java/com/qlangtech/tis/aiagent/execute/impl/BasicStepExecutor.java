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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.aiagent.core.PluginPropsComplement;
import com.qlangtech.tis.aiagent.core.RequestKey;
import com.qlangtech.tis.aiagent.core.SelectionOptions;
import com.qlangtech.tis.aiagent.execute.StepExecutor;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.coredefine.module.action.PluginAction;
import com.qlangtech.tis.coredefine.module.action.PluginFilter;
import com.qlangtech.tis.coredefine.module.action.PluginWillInstall;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.extension.model.UpdateCenter;
import com.qlangtech.tis.extension.model.UpdateSite;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.extension.util.TextFile;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.offline.DbScope;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.credentials.ParamsConfigPluginStore;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.runtime.module.misc.FormVaildateType;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.ListDetailedItemsErrors;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescribableJSON;
import com.qlangtech.tis.util.DescriptorsJSONForAIPromote;
import com.qlangtech.tis.util.DescriptorsJSONResult;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.ItemsSaveResult;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.PluginItems;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.util.impl.AttrVals;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.qlangtech.tis.aiagent.execute.impl.PluginEqualResult.notEqual;
import static com.qlangtech.tis.extension.Descriptor.KEY_DESC_VAL;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_VALS;
import static com.qlangtech.tis.util.AttrValMap.parseDescribableMap;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/3
 */
public abstract class BasicStepExecutor implements StepExecutor {

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
  protected AttrValMap createPluginInstance(TaskPlan plan, AgentContext context, UserPrompt userInput //
    , Optional<IEndTypeGetter.EndType> endType, DescribableImpl pluginImpl //
    , IPluginEnum heteroEnum, IPrimaryValRewrite primaryValRewrite) throws Exception {
    Pair<DescriptorsJSONResult, DescriptorsJSONForAIPromote> desc = DescriptorsJSONForAIPromote.desc(pluginImpl);

    DescriptorsJSONForAIPromote forAIPromote = desc.getValue();
    DescribableImpl propImplInfo = null;
    Descriptor implDesc = null;
    Map<Class<? extends Descriptor>, DescribableImpl> fieldDescRegister = forAIPromote.getFieldDescRegister();

    Map<String, IdentityName> propsImplRefsVals = null;

    for (Map.Entry<Class<? extends Descriptor>, DescribableImpl> entry : fieldDescRegister.entrySet()) {
      propImplInfo = entry.getValue();
      implDesc = propImplInfo.getImplDesc();
      propsImplRefsVals = setPropsImplRefsVals(plan, context, userInput, endType, implDesc);
      break;
    }
    Objects.requireNonNull(propsImplRefsVals, "propsImplRefsVals can not be null");
    LLMProvider llmProvider = plan.getLLMProvider();

    for (Map.Entry<String, JSONObject> entry : desc.getLeft().getDescriptorsResult().entrySet()) {
      // 需要遍历他的所有属性如果有需要创建的属性插件需要先创建
      JSONObject pluginPostBody = extractUserInput2Json(context, userInput, endType,
              Objects.requireNonNull(entry.getValue()), llmProvider);
      AttrValMap attrValMap = parseDescribableMap(Optional.empty(), pluginPostBody);

      if (attrValMap.descriptor.getIdentityField(false) != null) {
        PropertyType pk = attrValMap.descriptor.getIdentityField();
        if (attrValMap.isPrimaryFieldEmpty() || primaryValRewrite.isDuplicateInExistEntities(pk,
                Objects.requireNonNull(attrValMap.getPrimaryFieldVal(), "PrimaryFieldVal can not be empty"))) {
          // 1. 没有主键的情况下，由agent自主生成主键值
          // 2. 识别到用户提交的主键的情况下，需要判断是否和已经有的主键列表冲突，如果冲突也需要重新生成
          IdentityName primaryFieldVal = primaryValRewrite.newCreate(pk);
          if (primaryFieldVal != null) {
            attrValMap.getAttrVals().setPrimaryVal(pk.propertyName(), primaryFieldVal.identityValue());
          }
        }
      }

      for (Map.Entry<String, IdentityName> refProp : propsImplRefsVals.entrySet()) {
        final String propName = refProp.getKey();
        IdentityName refPropVal = refProp.getValue();
        attrValMap.getAttrVals().setPrimaryVal(propName, Objects.requireNonNull(refPropVal,
                "refPropVal can not be " + "null").identityValue());
      }


      return attrValMap;
    }

    throw new IllegalStateException("can not create AttrValMap , desc.getLeft().getDescriptorsResult() size:" + desc.getLeft().getDescriptorsResult().size());
  }

  private static final String SYSTEM_ASSIST_ROLE = "您是TIS数据集成平台的智能助手。";

  /**
   * 通过userInput 和 descriptorJson 生成提交的json内容
   *
   * @param userInput
   * @param endType
   * @param descriptorJson
   * @param llmProvider
   * @return
   */
  public JSONObject extractUserInput2Json(IAgentContext context, UserPrompt userInput,
                                          Optional<IEndTypeGetter.EndType> endType, JSONObject descriptorJson,
                                          LLMProvider llmProvider) {
    String prompt = "用户输入内容：" + userInput.getPrompt() + "\n 参照json结构说明，如下：\n" + JsonUtil.toString(descriptorJson, true);
    final String systemPrompt =
            SYSTEM_ASSIST_ROLE + "你的任务是帮助用户创建数据同步管道。\n" + endType.map((end) -> "当前处理的数据端是针对：" + String.valueOf(end)).orElse(StringUtils.EMPTY) + "\n" + "现在需要通过用户提交的内容，结合提系统提供的json结构说明，解析出结构化的json作为输出内容";

    JSONObject pluginPostBody = null;
    final String jsonSchema = "\n\n请严格按照系统提示中输出json的Schema格式返回结果";
    try (InputStream sysPromote = PluginInstanceCreateExecutor.class.getResourceAsStream(
            "describle_plugin_json_create_deamo.md")) {
      LLMProvider.LLMResponse llmResponse = llmProvider.chatJson(context, userInput.setNewPrompt(prompt),
              Lists.newArrayList(systemPrompt, IOUtils.toString(Objects.requireNonNull(sysPromote,
                      "sysPromote can " + "not" + " be " + "null"), TisUTF8.get())), jsonSchema);
      return pluginPostBody = llmResponse.getJsonContent();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class ExtractTargetTableInfoResult {
    //      "targetTables":["table1","table2"]
    //        "lostTables":["table3","table4"]
    // 目标表
    private final List<String> targetTables;
    // 缺失的表
    private final List<String> lostTables;

    public ExtractTargetTableInfoResult(List<String> targetTables, List<String> lostTables) {
      this.targetTables = targetTables;
      this.lostTables = lostTables;
    }

    public List<String> getTargetTables() {
      return targetTables;
    }

    public List<String> getLostTables() {
      return lostTables;
    }
  }

  public ExtractTargetTableInfoResult //
  extractTargetTableInfo(IAgentContext context, String extraTableInfo, List<String> extisTables,
                         LLMProvider llmProvider) {
    if (StringUtils.isEmpty(extraTableInfo)) {
      throw new IllegalArgumentException("param userInput can not be empty");
    }


    try (InputStream prompt = PluginInstanceCreateExecutor.class.getResourceAsStream(
            "describe_table_extraction_prompt.md")) {

      String promptTpl = IOUtils.toString(Objects.requireNonNull(prompt, "prompt can not be null"), TisUTF8.get());

      promptTpl = StringUtils.replace(promptTpl, "${extraTableInfo}", extraTableInfo);
      promptTpl = StringUtils.replace(promptTpl, "${existTables}", String.join(",", extisTables));

      UserPrompt userPrompt = new UserPrompt("解析源端目标表列表", promptTpl);
      LLMProvider.LLMResponse llmResponse = llmProvider.chatJson(context, userPrompt,
              Lists.newArrayList(SYSTEM_ASSIST_ROLE), null);
      JSONObject json = llmResponse.getJsonContent();

      JSONArray targetTables = json.getJSONArray("targetTables");
      JSONArray lostTables = json.getJSONArray("lostTables");

      return new ExtractTargetTableInfoResult( //
        targetTables.toJavaList(String.class) //
        , lostTables.toJavaList(String.class));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }


  /**
   * 生成依赖应用，例如MysqlDataXReader中依赖的 mysqlDataSource
   *
   * @param plan
   * @param context
   * @param userInput
   * @param endType
   * @param implDesc
   * @return
   * @throws Exception
   */
  private Map<String, IdentityName> setPropsImplRefsVals(TaskPlan plan, AgentContext context, UserPrompt userInput,
                                                         Optional<IEndTypeGetter.EndType> endType,
                                                         Descriptor implDesc) throws Exception {
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
      if (CollectionUtils.isEmpty(candidatePlugins) && refCreateor.getAssistType() == PluginExtraProps.RouterAssistType.hyperlink) {
        // 说明是类似 DefaultDataxProcessor的dptId这样的属性，那必须要有一个默认值
        List<Option> valOpts = refCreateor.getValOptions();
        for (Option opt : valOpts) {
          propsImplRefsVals.put(e.getKey(), IdentityName.create(String.valueOf(opt.getValue())));
          continue fieldValCreate;
        }
        throw new IllegalStateException("impl:" + implDesc.getId() + " of prop " + e.getKey() + " relevant opt vals " + "can nto be empty");
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

      // 开始实例化插件，内部有校验
      AttrValMap pluginVals = createInnerPluginInstance(plan //
        , context, userInput.setAbstract("解析'" + candidatePlugin.getTargetItemDesc() + "'插件内容"), refCreateor,
              Objects.requireNonNull(candidatePlugin, "candidatePlugin can not be null for field:" + e.getKey()));

      /**
       * 找历史记录中是否有相同的实例，避免重复创建相同的实例
       */
      List<Option> existOpts = refCreateor.getValOptions();
      if (!pluginVals.isPrimaryFieldEmpty()) {
        /**
         * 这种情况是用户明确在提交的文本中说明需要使用的对象实例名称，例如,以下文本中明确说明‘shop’数据库实例
         * <pre>
         *   我有一个本地文件需要导入到mysql数据库中，文件路径为：/opt/misc/dfs/totalpayinfo__47a61b2a_7e96_409d_8769_0143423bebba.csv，文件格式为csv，mysql库使用名称为‘shop’的数据库源作为目标端导入对象
         * </pre>
         */
        IdentityName findId = IdentityName.create(pluginVals.getPrimaryFieldVal());
        if (existOpts.stream().anyMatch((opt) -> opt.equalWithId(findId))) {
          // 创建的实例对应的对象实例已经存在，不用再创建，重用就行了
          context.sendMessage("找到既存" + candidatePlugin.getDisplayName() + "类型的实例：'" + findId.identityValue() +
                  "'，直接复用它就行");
          propsImplRefsVals.put(e.getKey(), findId);
          continue fieldValCreate;
        }
      }

      pluginVals = this.validateAttrValMap(plan, context, candidatePlugin.getHetero(), pluginVals, Optional.empty());

      Optional<Describable> existPlugin = this.findExistPlugin(candidatePlugin, pluginVals, existOpts);
      if (existPlugin.isPresent()) {
        IdentityName foundExist = (IdentityName) existPlugin.get();
        context.sendMessage("找到既存" + candidatePlugin.getDisplayName() + "类型的实例：'" + foundExist.identityValue() +
                "'与您提交内容一致，直接复用它就行");
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
          pItems = new PluginItems(pluginCtx, ctx, ParamsConfigPluginStore.createParamsConfig(pluginEnum,
                  candidatePlugin));
          break;
        }
        case dbQuickManager: {
          pluginCtx = IPluginContext.namedContext(new DataXName(pluginRef.identityValue(),
                  StoreResourceType.DataBase)).setTargetRuntimeContext((IPluginContext) plan.getControlMsgHandler());
          pItems = new PluginItems(pluginCtx, ctx,
                  PostedDSProp.createPluginMeta(DBIdentity.parseId(pluginRef.identityValue()), false).putExtraParams(DBIdentity.KEY_TYPE, DbScope.DETAILED.getToken()));
          break;
        }
        default: {
          final String primaryFieldKey = pluginVals.descriptor.getIdentityField().propertyName();
          throw new IllegalStateException("illegal assistType:" + refCreateor.getAssistType() + "for field:" + primaryFieldKey + " of plugin:" + pluginVals.descriptor.getId());
        }
      }
      pItems.items = Collections.singletonList(pluginVals);
      context.sendMessage("创建" + candidatePlugin.getDisplayName() + "类型实例：'" + pluginRef.identityValue() + "'");
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

  private <OPTION extends IdentityName> Optional<Describable> findExistPlugin(PluginExtraProps.CandidatePlugin candidatePlugin, AttrValMap pluginVals, List<OPTION> existOpts) throws Exception {
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

      PluginEqualResult equalResult = null;
      if ((equalResult = isPluginEqual(plugin, pluginVals.getAttrVals())).isEqual()) {
        if (!(plugin instanceof IdentityName)) {
          throw new IllegalStateException("plugin:" + plugin.getClass().getName() + " must be type of " + IdentityName.class.getSimpleName());
        }
        return Optional.of(plugin);
      }
      // equalResult.printUnEqualStack();
    }

    return Optional.empty();
  }

  PluginEqualResult isPluginEqual(Describable plugin, AttrVals pluginVals) throws Exception {
    // 空值检查
    Descriptor desc = Objects.requireNonNull(plugin, "plugin can not be null").getDescriptor();
    PluginFormProperties propertyTypes = desc.getPluginFormPropertyTypes();

    return Objects.requireNonNull(propertyTypes, "propertyTypes can not be null").accept(new PluginFormProperties.IVisitor() {
      @Override
      public PluginEqualResult visit(RootFormProperties props) {
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
                return notEqual("fieldName:" + fieldName + " relevant describle is null");
              }

              Object vals = Objects.requireNonNull(describle.getJSONObject(KEY_DESC_VAL),
                      "key:" + KEY_DESC_VAL + " " + "relevant json can not be null").get(PLUGIN_EXTENSION_VALS);
              if (vals == null) {
                // 如果 exist 也是 null，则认为相等
                if (exist == null) {
                  continue;
                }
                return notEqual("fieldName:" + fieldName + " vals is null but exist vals is not null");
              }

              if (exist == null) {
                return notEqual("fieldName:" + fieldName + " relevant exist vals is  null");
              }

              PluginEqualResult compareResult = null;
              if (!(compareResult = isPluginEqual((Describable) exist, AttrVals.parseAttrValMap(vals))).equal) {
                return notEqual("desc field:" + fieldName + "," + compareResult.unEqualLogger).setStack(compareResult.stack);
              }

            } else {
              Object primaryVal = pluginVals.getPrimaryVal(fieldName);
              if (primaryVal == null && exist == null) {
                continue;
              }
              if (primaryVal == null ^ exist == null) {
                return notEqual("fieldName:" + fieldName + ",primaryVal(" + primaryVal + ") == null ^ exist(\"" + exist + "\") == null");
              }
              if (!StringUtils.equals(String.valueOf(exist), String.valueOf(primaryVal))) {
                return notEqual("fieldName:" + fieldName + ",exist(" + exist + ") != " + primaryVal);
              }
            }
          }
          return new PluginEqualResult(true, null);
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
   * 通过用户提交的内容生成plugin封装，并且进行校验，直到校验成功之后返回封装实例
   *
   * @param plan
   * @param context
   * @param userInput
   * @param candidate
   * @return
   * @throws Exception
   */
  private AttrValMap createInnerPluginInstance(TaskPlan plan, AgentContext context, UserPrompt userInput,
                                               PluginExtraProps.FieldRefCreateor refCreateor //
    , PluginExtraProps.CandidatePlugin candidate) throws Exception {
    Descriptor installedPluginDescriptor = candidate.getInstalledPluginDescriptor();
    AttrValMap valMap = createPluginInstance(plan, context, userInput, Optional.empty(),
            new DescribableImpl(candidate.getHetero().getExtensionPoint() //
        , Optional.empty()).setDescriptor( //
        Objects.requireNonNull(installedPluginDescriptor, "candidate:" + candidate + ",installedPluginDescriptor " +
                "can not be null"))  //
      , candidate.getHetero() //
      , (propType) -> {
        if (!propType.isIdentity()) {
          throw new IllegalStateException("propType:" + propType.propertyName() + " must be primary field");
        }
        List<Option> existOpts = refCreateor.getValOptions();
        return candidate.createNewPrimaryFieldValue(existOpts);
      });
    return valMap;
    // return validateAttrValMap(plan, context, candidate.getHetero(), valMap, Optional.empty());
  }

  /**
   * 选择&安装目标插件
   *
   * @param context
   * @param e
   * @param candidatePlugins
   * @return
   */
  private PluginExtraProps.CandidatePlugin selectTargetPluginDescriptor(AgentContext context, Map.Entry<String,
                                                                                PluginExtraProps.FieldRefCreateor> e //
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
  private PluginExtraProps.CandidatePlugin selectPlugin(AgentContext context, String fieldName,
                                                        Optional<IEndTypeGetter.EndType> endType,
                                                        List<PluginExtraProps.CandidatePlugin> candidatePlugins) {

    if (CollectionUtils.isEmpty(candidatePlugins)) {
      throw new IllegalArgumentException("candidatePlugins can not be empty");
    }

    RequestKey requestId = RequestKey.create();// "plugin_select_" + System.currentTimeMillis();


    String prompt = String.format("请选择 %s 字段的插件实现版本", fieldName);
    //    context.sendMessage(prompt);
    /************************************************************************
     * 向客户端发送需要用户确认的请求
     ************************************************************************/
    context.requestUserSelection(requestId, prompt, endType, candidatePlugins);

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
      //          + " of extendpoint:" + selected.getHetero().getExtensionPoint().getName() + " have not been
      //          installed");
      //      }

      context.sendMessage(String.format("已选择: %s", selected.getDisplayName()));
      return selected;
    }

    context.sendError("用户选择超时或取消");
    return null;
  }

  protected AttrValMap validateAttrValMap(TaskPlan plan, AgentContext context, IPluginEnum pluginEnum,
                                          AttrValMap valMap, Optional<DataXName> pipelineName) throws Exception {
    /**
     * 需要对valMap进行校验
     */
    final Context ctx = plan.getRuntimeContext(true);
    FormVaildateType verify = FormVaildateType.create(true);
    FormVaildateType validate = FormVaildateType.create(false);

    AttrValMap.setCurrentRootPluginValidator(valMap.descriptor);
    Descriptor.PluginValidateResult.setValidateItemPos(ctx, 0, 0);

    PartialSettedPluginContext msgHandler = createPluginContext(plan, pipelineName.orElse(null));

    boolean validateFaild = false;
    try {
      if (!valMap.validate(msgHandler, ctx, verify, Optional.empty()).isValid() //
        || !valMap.validate(msgHandler, ctx, validate, Optional.empty()).isValid()) {
        // error
        validateFaild = true;
      }
    } catch (Exception e) {
      validateFaild = true;
      TisException expt = null;
      if ((expt = ExceptionUtils.throwableOfType(e, TisException.class)) != null) {
        msgHandler.addErrorMessage(ctx, expt.getMessage());
      } else {
        throw new RuntimeException(e);
      }
    }

    if (validateFaild) {
      AjaxValve.ActionExecResult validateResult = AjaxValve.ActionExecResult.create(ctx);

      ListDetailedItemsErrors itemErrors = validateResult.getItemErrors();
      if (itemErrors != null) {
        // 处理Flink 本地环境还没有部署，需要部署Flink 实例的流程
        Map<String, /*** fieldname*/IPropertyType> propertyTypes = valMap.descriptor.getPropertyTypes();
        AttrVals attrVals = valMap.getAttrVals();
        for (DefaultFieldErrorHandler.FieldError fieldError : itemErrors.fieldsErrorList) {
          PropertyType pt = (PropertyType) Objects.requireNonNull( //
            propertyTypes.get(fieldError.getFieldName()), "prop" + ":" + fieldError.getFieldName() + " relevant " +
                          "PropertyType can not be null");


          String pluginImpl = PropertyType.getPluginImpl(attrVals.getAttrVal(fieldError.getFieldName()));


          if (pt.isDescribable()) {
            Descriptor fieldDesc = TIS.get().getDescriptor(pluginImpl);
            Optional<DescribableJSON<ParamsConfig>> aiAssistSupport = fieldDesc.getAIAssistSupport();
            if (aiAssistSupport.isPresent()) {
              RequestKey requestId = RequestKey.create();

              DescribableJSON<ParamsConfig> aiAssistDescribableJSON = aiAssistSupport.get();
              context.sendPluginConfig(requestId, AjaxValve.ActionExecResult.create(plan.getRuntimeContext(true)),
                      HeteroEnum.PARAMS_CONFIG, aiAssistDescribableJSON.descriptor.getId(),
                      aiAssistDescribableJSON.getPostAttribute());
              /***
               * 等到用户选择
               ***/
              PluginPropsComplement pluginProps = context.waitForUserPost(requestId, (pp) -> {
                return pp != null && pp.getPluginValMap() != null;
              });
            }

            //            RequestKey requestId, AjaxValve.ActionExecResult validateResult
            //    , IPluginEnum pluginEnum, String pluginImpl, AttrValMap valMap


          }
        }
      }


      final RequestKey requestId = RequestKey.create();
      /***
       * 此处需要用户将不足的属性补足
       ***/
      context.sendPluginConfig(requestId, validateResult, pluginEnum, valMap.descriptor.getId(), valMap);

      /***
       * 等到用户选择
       ***/
      PluginPropsComplement pluginProps = context.waitForUserPost(requestId, (pp) -> {
        return pp != null && pp.getPluginValMap() != null;
      });
      return Objects.requireNonNull(pluginProps, "validate pluginProps can not be null").getPluginValMap();
    }
    return valMap;
  }

  protected <PLUGIN extends Describable> PLUGIN  //
  createPluginAndStore( //
                        HeteroEnum hetero, TaskPlan plan, AgentContext context, Context ctx,
                        PartialSettedPluginContext pluginCtx, UploadPluginMeta pluginMetaMeta, AttrValMap pluginVals) throws Exception {

    /**
     * 先进行校验
     */
    pluginVals = validateAttrValMap(plan, context, hetero, pluginVals,
            Optional.ofNullable(pluginMetaMeta.getDataXName(false)));
    Descriptor.ParseDescribable newPlugin = pluginVals.createDescribable(pluginCtx, ctx);
    IPluginStore pluginStore = hetero.getPluginStore(pluginCtx, pluginMetaMeta);
    pluginStore.setPlugins(pluginCtx, Optional.empty(), Collections.singletonList(newPlugin));
    return (PLUGIN) newPlugin.getInstance();
  }

  public static PartialSettedPluginContext createPluginContext(TaskPlan plan, @Nullable DataXName name) {
    PartialSettedPluginContext pluginContext = null;
    if (name != null) {
      pluginContext = IPluginContext.namedContext(name);
    } else {
      pluginContext = new PartialSettedPluginContext();
    }
    return pluginContext.setTargetRuntimeContext((IPluginContext) plan.getControlMsgHandler());
  }

  protected void checkInstallPlugin(AgentContext context, Set<Pair<IEndTypeGetter.EndType,
          Collection<DescribableImpl>>> installImpls) {
    checkInstallPlugin(context, installImpls, (beforeInstall) -> {
    });
  }

  /**
   * 识别endtype和扩展点对应插件实现，并且检查对应的插件是否已经安装，如果没有安装就将插件进行安装
   *
   * @param installImpls
   * @param beforeInstall
   */
  protected void checkInstallPlugin(AgentContext context, Set<Pair<IEndTypeGetter.EndType,
          Collection<DescribableImpl>>> installImpls, Consumer<Set<PluginWillInstall>> beforeInstall) {
    UpdateCenter center = TIS.get().getUpdateCenter();
    final List<UpdateSite.Plugin> availables = center.getPlugins(UpdateSite::getAllPlugins);
    try {
      if (CollectionUtils.isEmpty(availables)) {
        for (UpdateSite usite : center.getSiteList()) {
          TextFile textFile = usite.getDataLoadFaildFile();
          if (textFile.exists()) {
            throw TisException.create(textFile.read());
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Set<PluginWillInstall> pluginsInstall = Sets.newHashSet();
    for (Pair<IEndTypeGetter.EndType, Collection<DescribableImpl>> pair : installImpls) {
      pluginsInstall.addAll(parsePluginWillInstalls(pair.getKey(), pair.getRight(), availables));
    }
    beforeInstall.accept(pluginsInstall);

    Boolean success = false;
    try {
      if (CollectionUtils.isNotEmpty(pluginsInstall)) {
        // 开始安装
        Future<Boolean> installResult = PluginWillInstall.installPlugins(new ArrayList<>(pluginsInstall));
        // 等待安装完毕
        final ExecutorService waitInstallComplete = Executors.newSingleThreadExecutor();
        final RequestKey requestKey = RequestKey.create();
        final AtomicBoolean beWait = new AtomicBoolean(true);
        waitInstallComplete.execute(() -> {
          while (beWait.get()) {

            context.sendPluginInstallStatus(requestKey, PluginAction.getInstallJobs(center), false);
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        });
        success = installResult.get();
        context.sendPluginInstallStatus(requestKey, PluginAction.getInstallJobs(center), true);
        beWait.set(false);
        waitInstallComplete.shutdown();
        if (!success) {
          throw TisException.create("install plugins:" + pluginsInstall.stream().map(PluginWillInstall::getName).collect(Collectors.joining(",")) + " faild");
        }
      }
    } catch (TisException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Set<PluginWillInstall> parsePluginWillInstalls(IEndTypeGetter.EndType endType,
                                                         Collection<DescribableImpl> extendPoints,
                                                         List<UpdateSite.Plugin> availables) {
    if (endType == null) {
      throw new IllegalArgumentException("param endType can not be null");
    }
    Set<PluginWillInstall> pluginsInstall = Sets.newHashSet();

    PluginFilter filter = PluginFilter.create(endType.getVal(), extendPoints);

    List<UpdateSite.Plugin> filterAvailables = availables.stream().filter((plugin) -> {
      return !(filter.filter(Optional.empty(), plugin));
    }).collect(Collectors.toList());

    List<String> impls = null;

    outter:
    for (DescribableImpl ep : extendPoints) {
      for (UpdateSite.Plugin plugin : filterAvailables) {
        impls = plugin.extendPoints.get(ep.getExtendPointClassName());
        if (CollectionUtils.isNotEmpty(impls)) {
          for (String pluginImpl : impls) {
            ep.addImpl(pluginImpl);
          }
          if (plugin.getInstalled() == null) {
            // 扩展实现的插件还没有安装，需要进行安装
            pluginsInstall.add(new PluginWillInstall(plugin.getDisplayName()));
          }
        }
      }
    }
    return pluginsInstall;
  }

}
