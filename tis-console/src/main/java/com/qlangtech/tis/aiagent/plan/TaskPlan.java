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
package com.qlangtech.tis.aiagent.plan;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.google.common.collect.ImmutableMap;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 任务执行计划
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class TaskPlan {

  public final Map<Class<? extends Describable>, DescribableImpl> readerExtendPoints;


  public final Map<Class<? extends Describable>, DescribableImpl> writerExtendPoints;
  public final DescribableImpl processorExtendPoints;
  private MockContext runtimeContext;

  public Context getRuntimeContext(boolean createNew) {

    if (createNew) {
      DefaultContext ctx = new DefaultContext();
      setPluginMeta(ctx);
      return ctx;
    } else {
      if (runtimeContext == null) {
        runtimeContext = new MockContext();
        setPluginMeta(runtimeContext);
      }
      return this.runtimeContext;
    }
  }

  private static void setPluginMeta(Context ctx) {
    ctx.put(UploadPluginMeta.KEY_PLUGIN_META, UploadPluginMeta.create(HeteroEnum.APP_SOURCE)
      .putExtraParams(DBIdentity.KEY_UPDATE, Boolean.FALSE.toString()));
  }

  /**
   * 校验每个扩展点都找到对应的实现插件
   */
  public void checkDescribableImplHasSet() {
    for (DescribableImpl dImpl : readerExtendPoints.values()) {
      if (CollectionUtils.isEmpty(dImpl.getImpls())) {
        throw new IllegalStateException(dImpl.getExtendPoint().getSimpleName()
          + "：" + dImpl.getEndType().map(String::valueOf).orElse(StringUtils.EMPTY) + " relevant plugin impl can not be null");
      }
    }

    for (DescribableImpl dImpl : writerExtendPoints.values()) {
      if (CollectionUtils.isEmpty(dImpl.getImpls())) {
        throw new IllegalStateException(dImpl.getExtendPoint().getSimpleName()
          + "：" + dImpl.getEndType().map(String::valueOf).orElse(StringUtils.EMPTY) + " relevant plugin impl can not be null");
      }
    }
  }

  private String planId;
  private final SourceDataEndCfg sourceEnd;
  private final DataEndCfg targetEnd;
  private List<TaskStep> steps;
  private String userInput;
  private long createTime;
  private final LLMProvider llmProvider;
  private final IControlMsgHandler controlMsgHandler;

  public TaskPlan(SourceDataEndCfg sourceEnd, DataEndCfg targetEnd, LLMProvider llmProvider, IControlMsgHandler controlMsgHandler) {
    this.steps = new ArrayList<>();
    this.createTime = System.currentTimeMillis();
    this.llmProvider = Objects.requireNonNull(llmProvider, "llmProvider can not be null");
    this.sourceEnd = Objects.requireNonNull(sourceEnd, "sourceEnd can not be null");
    this.targetEnd = Objects.requireNonNull(targetEnd, "targetEnd can not be null");
    this.controlMsgHandler = Objects.requireNonNull(controlMsgHandler, "controlMsgHandler can not be null");


    this.processorExtendPoints = new DescribableImpl(IAppSource.class, Optional.empty())
      .addImpl("com.qlangtech.tis.plugin.datax.DefaultDataxProcessor");

    ImmutableMap.Builder<Class<? extends Describable>, DescribableImpl> mapBuilder = new ImmutableMap.Builder<>();
    mapBuilder.put(DataxReader.class, new DescribableImpl(DataxReader.class, Optional.of(sourceEnd.getType())));
    mapBuilder.put(MQListenerFactory.class, new DescribableImpl(MQListenerFactory.class, Optional.of(sourceEnd.getType())));

    this.readerExtendPoints = mapBuilder.build();

    mapBuilder = new ImmutableMap.Builder<>();
    mapBuilder.put(DataxWriter.class, new DescribableImpl(DataxWriter.class, Optional.of(targetEnd.getType())));
    mapBuilder.put(TISSinkFactory.class, new DescribableImpl(TISSinkFactory.class, Optional.of(targetEnd.getType())));

    this.writerExtendPoints = mapBuilder.build();
  }

  public IControlMsgHandler getControlMsgHandler() {
    return Objects.requireNonNull(this.controlMsgHandler, "controlMsgHandler can not be null");
  }

  public LLMProvider getLLMProvider() {
    return this.llmProvider;
  }

  public void addStep(TaskStep step) {
    steps.add(step);
  }

  public String getPlanId() {
    return planId;
  }

  public void setPlanId(String planId) {
    this.planId = planId;
  }

  public SourceDataEndCfg getSourceEnd() {
    return this.sourceEnd;
  }


  public DataEndCfg getTargetEnd() {
    return targetEnd;
  }


  public List<TaskStep> getSteps() {
    return steps;
  }

  public void setSteps(List<TaskStep> steps) {
    this.steps = steps;
  }

  public String getUserInput() {
    return userInput;
  }

  public void setUserInput(String userInput) {
    this.userInput = userInput;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  public int getTotalSteps() {
    return steps != null ? steps.size() : 0;
  }

  public int getCompletedSteps() {
    return (int) steps.stream()
      .filter(step -> step.getStatus() == TaskStep.Status.COMPLETED)
      .count();
  }

  /**
   * 数据端解析内容
   */
  public static class DataEndCfg {
    private final IEndTypeGetter.EndType type;

    public DataEndCfg(IEndTypeGetter.EndType type) {
      this.type = type;
    }

    /**
     * 端配置描述信息，一段自然语言
     */
    private String relevantDesc;

    public IEndTypeGetter.EndType getType() {
      return type;
    }


    public String getRelevantDesc() {
      return relevantDesc;
    }

    public void setRelevantDesc(String relevantDesc) {
      this.relevantDesc = relevantDesc;
    }
  }

  public static class SourceDataEndCfg extends DataEndCfg {
    private List<String> selectedTabs;

    public SourceDataEndCfg(IEndTypeGetter.EndType type) {
      super(type);
    }

    public List<String> getSelectedTabs() {
      return this.selectedTabs;
    }

    public void setSelectedTabs(List<String> selectedTabs) {
      this.selectedTabs = selectedTabs;
    }
  }

}
