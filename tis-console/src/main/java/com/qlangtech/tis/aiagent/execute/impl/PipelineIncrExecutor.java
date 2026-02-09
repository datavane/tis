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
import com.google.common.collect.Sets;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.core.RequestKey;
import com.qlangtech.tis.aiagent.core.SelectionOptions;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.job.SSERunnable;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.lang.PayloadLink;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.incr.IncrStreamFactory;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.qlangtech.tis.coredefine.module.action.CoreAction.startDeployIncrSyncChannal;

/**
 * 管道实时增量开启
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/4
 */
public class PipelineIncrExecutor extends BasicStepExecutor {
  @Override
  public boolean execute(TaskPlan plan, TaskStep step, AgentContext context) {
    TaskPlan.SourceDataEndCfg sourceEnd = plan.getSourceEnd();
    TaskPlan.DataEndCfg targetEnd = plan.getTargetEnd();
    IAppSource processor = sourceEnd.getProcessor();
    DataXName pipelineName =sourceEnd.getDataXName();
    if (!Objects.requireNonNull(sourceEnd.getEndTypeMeta(), "sourceEnd:" + sourceEnd.getType() + " relevant "
      + "EndTypeMeta can not be null").isSupportIncr()) {
      context.sendMessage("源端‘" + sourceEnd.getType() + "’ 类型不支持增量实时数据同步，因此跳过该步骤");
      return true;
    }
    if (!Objects.requireNonNull(targetEnd.getEndTypeMeta(), "targetEnd:" + targetEnd.getType() + " relevant "
      + "EndTypeMeta can not be null").isSupportIncr()) {
      context.sendMessage("目标端‘" + targetEnd.getType() + "’ 类型不支持增量实时数据同步，因此跳过该步骤");
      return true;
    }

    boolean executeIncr = sourceEnd.isExecuteIncr();
    if (!executeIncr) {
      // 询问用户是否开通
      RequestKey requestId = RequestKey.create();
      List<PluginExtraProps.CandidatePlugin> opts = Lists.newArrayList(new NormalSelectionOption("是的，现在开始吧"),
        new NormalSelectionOption("不需要，等等再说"));

      final String prompt = "请选择是否开启增量实时同步通道执行？";
      context.requestUserSelection(requestId, prompt, Optional.empty(), opts);

      /************************************************************************
       * 等待客户端发送的选择信息
       ************************************************************************/
      SelectionOptions selectedIndex = context.waitForUserPost(requestId, (selOpts) -> {
        return (selOpts != null && selOpts.hasSelectedOpt());
      });
      executeIncr = (selectedIndex.getSelectedIndex() == 0);
    }

    if (!executeIncr) {
      context.sendMessage("您选择了不开启增量实时数据同步执行，现在跳过该步骤。");
      return true;
    }

    try {
      Optional<IEndTypeGetter.EndType> flinkType = Optional.of(IEndTypeGetter.EndType.Flink);
      DescribableImpl streamFactoryImpl = new DescribableImpl(IncrStreamFactory.class, flinkType);
      IEndTypeGetter.EndType incrSourceType = plan.getSourceEnd().getType();
      IEndTypeGetter.EndType incrSinkType = plan.getTargetEnd().getType();
      DescribableImpl incrSourceImpl = new DescribableImpl(MQListenerFactory.class, Optional.of(incrSourceType));
      DescribableImpl incrSinkImpl = new DescribableImpl(TISSinkFactory.class, Optional.of(incrSinkType));
      // .addImpl("com.qlangtech.plugins.incr.flink.launch.TISFlinkCDCStreamFactory");

      this.checkInstallPlugin(context, Sets.newHashSet(Pair.of(IEndTypeGetter.EndType.Flink,
        Collections.singletonList(streamFactoryImpl)), Pair.of(incrSourceType,
        Collections.singletonList(incrSourceImpl)), Pair.of(incrSinkType, Collections.singletonList(incrSinkImpl))));

      /**
       * 生成 IncrStreamFactory
       */
      AttrValMap pluginVals = createPluginInstance(plan, context,
        new UserPrompt("正在生成" + IEndTypeGetter.EndType.Flink + "实时管道主体配置...", plan.getUserInput()) //
        , flinkType //
        , streamFactoryImpl, HeteroEnum.INCR_STREAM_CONFIG, new IPrimaryValRewrite() {
          @Override
          public IdentityName newCreate(PropertyType pp) {
            throw new IllegalStateException("primary key is not support,fieldName:" + pp.propertyName());
          }
        });

      PartialSettedPluginContext pluginCtx = createPluginContext(plan, pipelineName);
      UploadPluginMeta processMeta = UploadPluginMeta.appnameMeta(pluginCtx, processor.identityValue());
      Context ctx = plan.getRuntimeContext(true);

      IncrStreamFactory streamFactory = createPluginAndStore(HeteroEnum.INCR_STREAM_CONFIG, plan, context, ctx,
        pluginCtx, processMeta, pluginVals);


      /**
       * MQListenerFactory
       */

      pluginVals = createPluginInstance(plan, context, new UserPrompt("正在生成" + incrSourceType + "源端实时增量实例主体配置...",
          plan.getUserInput()) //
        , Optional.of(incrSourceType) //
        , incrSourceImpl, HeteroEnum.MQ, new IPrimaryValRewrite() {
          @Override
          public IdentityName newCreate(PropertyType pp) {
            throw new IllegalStateException("primary key is not support,fieldName:" + pp.propertyName());
          }
        });
      ctx = plan.getRuntimeContext(true);
      createPluginAndStore(HeteroEnum.MQ, plan, context, ctx, pluginCtx, processMeta, pluginVals);
      /**
       * TISSinkFactory
       */
      pluginVals = createPluginInstance(plan, context, new UserPrompt("正在生成" + incrSinkType + "目标端实时增量实例主体配置...",
          plan.getUserInput()) //
        , Optional.of(incrSinkType) //
        , incrSinkImpl, TISSinkFactory.sinkFactory, new IPrimaryValRewrite() {
          @Override
          public IdentityName newCreate(PropertyType pp) {
            throw new IllegalStateException("primary key is not support,fieldName:" + pp.propertyName());
          }
        });
      ctx = plan.getRuntimeContext(true);
      createPluginAndStore(TISSinkFactory.sinkFactory, plan, context, ctx, pluginCtx, processMeta, pluginVals);

      // 执行发布
      JSONObject launchData = new JSONObject();
      launchData.put(StoreResourceType.DATAX_NAME, pipelineName.getPipelineName());
      context.getSseWriter().writeSSEEvent(SSERunnable.SSEEventType.AI_AGNET_OPEN_LAUNCHING_PROCESS, launchData);
      ctx = plan.getRuntimeContext(true);
      startDeployIncrSyncChannal(context.getSseWriter(), plan.getControlMsgHandler(), ctx, streamFactory, pipelineName);

      context.sendMessage("已经成功部署实时增量实例：" + String.valueOf(pipelineName), new PayloadLink("查看",
        "/x/" + pipelineName.getPipelineName() + "/incr_build"));

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return true;
  }

  @Override
  public ValidationResult validate(TaskStep step) {
    return ValidationResult.success();
  }

  @Override
  public TaskStep.StepType getSupportedType() {
    return TaskStep.StepType.EXECUTE_INCR;
  }
}
