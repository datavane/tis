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
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.lang.PayloadLink;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.plugin.IDataXEndTypeGetter;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
    ctx.put(UploadPluginMeta.KEY_PLUGIN_META,
      UploadPluginMeta.create(HeteroEnum.APP_SOURCE).putExtraParams(DBIdentity.KEY_UPDATE, Boolean.FALSE.toString()));
  }

  /**
   * 校验每个扩展点都找到对应的实现插件
   */
  public void checkDescribableImplHasSet() {
    checkDescribableImplHasSet(readerExtendPoints);
    checkDescribableImplHasSet(writerExtendPoints);
  }

  private void checkDescribableImplHasSet(Map<Class<? extends Describable>, DescribableImpl> extendPoints) {
    for (DescribableImpl dImpl : extendPoints.values()) {
      if (!dImpl.isIncrStreamEndType() && CollectionUtils.isEmpty(dImpl.getImpls())) {
        // 批量必须要有端类型实现插件，增量可以没有
        throw TisException.create(dImpl.getExtendPoint().getSimpleName() + "：" + dImpl.getEndType().map(String::valueOf).orElse(StringUtils.EMPTY) + "，请确认对应的端目前是否支持").setPayloadLink(new PayloadLink("TIS支持的端类型", "https://tis.pub/docs/plugin/source-sink/"));
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

  public TaskPlan(SourceDataEndCfg sourceEnd, DataEndCfg targetEnd, LLMProvider llmProvider,
                  IControlMsgHandler controlMsgHandler) {
    this.steps = new ArrayList<>();
    this.createTime = System.currentTimeMillis();
    this.llmProvider = Objects.requireNonNull(llmProvider, "llmProvider can not be null");
    this.sourceEnd = Objects.requireNonNull(sourceEnd, "sourceEnd can not be null");
    this.targetEnd = Objects.requireNonNull(targetEnd, "targetEnd can not be null");
    this.controlMsgHandler = Objects.requireNonNull(controlMsgHandler, "controlMsgHandler can not be null");


    this.processorExtendPoints = new DescribableImpl(IAppSource.class, Optional.empty()).addImpl("com.qlangtech.tis"
      + ".plugin.datax.DefaultDataxProcessor");

    ImmutableMap.Builder<Class<? extends Describable>, DescribableImpl> mapBuilder = new ImmutableMap.Builder<>();
    mapBuilder.put(DataxReader.class, new DescribableImpl(DataxReader.class, Optional.of(sourceEnd.getType())));
    mapBuilder.put(MQListenerFactory.class, new DescribableImpl(MQListenerFactory.class,
      Optional.of(sourceEnd.getType())));

    this.readerExtendPoints = mapBuilder.build();

    mapBuilder = new ImmutableMap.Builder<>();
    mapBuilder.put(DataxWriter.class, new DescribableImpl(DataxWriter.class, Optional.of(targetEnd.getType())));
    mapBuilder.put(TISSinkFactory.class, new DescribableImpl(TISSinkFactory.class, Optional.of(targetEnd.getType())));

    this.writerExtendPoints = mapBuilder.build();
  }

  public <T> T getControlMsgHandler() {
    return (T) Objects.requireNonNull(this.controlMsgHandler, "controlMsgHandler can not be null");
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
    return (int) steps.stream().filter(step -> step.getStatus() == TaskStep.Status.COMPLETED).count();
  }

  /**
   * 数据端解析内容
   */
  public static class DataEndCfg {
    private final IEndTypeGetter.EndType type;
    private IDataXEndTypeGetter endTypeMeta;

    public DataEndCfg(IEndTypeGetter.EndType type) {
      this.type = type;
    }

    public IDataXEndTypeGetter getEndTypeMeta() {
      return endTypeMeta;
    }

    public void setEndTypeMeta(IDataXEndTypeGetter endTypeMeta) {
      this.endTypeMeta = endTypeMeta;
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
    private String extraSelectedTabInfo;
    /**
     * 对应管道的profile
     */
    private IAppSource processor;
    /**
     * 是否触发全量历史数据同步
     */
    private boolean executeBatch;

    /**
     * 是否启动增量实时数据同步
     */
    private boolean executeIncr;

    public SourceDataEndCfg(IEndTypeGetter.EndType type) {
      super(type);
    }

    public String getExtraSelectedTabInfo() {
      return this.extraSelectedTabInfo;
    }

    public void setExtraSelectedTabInfo(String info) {
      this.extraSelectedTabInfo = info;
    }

    public void setExecuteBatch(boolean val) {
      this.executeBatch = val;
    }

    public void setExecuteIncr(boolean val) {
      this.executeIncr = val;
    }

    public boolean isExecuteBatch() {
      return executeBatch;
    }

    public boolean isExecuteIncr() {
      return this.executeIncr;
    }


    public IAppSource getProcessor() {
      return this.processor;
    }

    public void setProcessor(IAppSource processor) {
      this.processor = processor;
    }

//    public List<String> getSelectedTabs() {
//      return null;
//    }
  }

}
