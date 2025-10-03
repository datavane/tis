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

import com.google.common.collect.ImmutableMap;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 任务执行计划
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class TaskPlan {

  public final Map<Class<? extends Describable>, DescribableImpl> readerExtendPoints;


  public final Map<Class<? extends Describable>, DescribableImpl> writerExtendPoints;


  /**
   * 校验每个扩展点都找到对应的实现插件
   */
  public void checkDescribableImplHasSet() {
    for (DescribableImpl dImpl : readerExtendPoints.values()) {
      if (CollectionUtils.isEmpty(dImpl.getImpls())) {
        throw new IllegalStateException(dImpl.getExtendPoint().getName() + " relevant plugin impl can not be null");
      }
    }

    for (DescribableImpl dImpl : writerExtendPoints.values()) {
      if (CollectionUtils.isEmpty(dImpl.getImpls())) {
        throw new IllegalStateException(dImpl.getExtendPoint().getName() + " relevant plugin impl can not be null");
      }
    }
  }

  private String planId;
  private IEndTypeGetter.EndType sourceType;
  private IEndTypeGetter.EndType targetType;
  private List<TaskStep> steps;
  private String userInput;
  private long createTime;

  public TaskPlan() {
    this.steps = new ArrayList<>();
    this.createTime = System.currentTimeMillis();

    ImmutableMap.Builder<Class<? extends Describable>, DescribableImpl> mapBuilder = new ImmutableMap.Builder<>();
    mapBuilder.put(DataxReader.class, new DescribableImpl(DataxReader.class));
    mapBuilder.put(MQListenerFactory.class, new DescribableImpl(MQListenerFactory.class));

    this.readerExtendPoints = mapBuilder.build();

    mapBuilder = new ImmutableMap.Builder<>();
    mapBuilder.put(DataxWriter.class, new DescribableImpl(DataxWriter.class));
    mapBuilder.put(TISSinkFactory.class, new DescribableImpl(TISSinkFactory.class));

    this.writerExtendPoints = mapBuilder.build();

//      = new ImmutableMap.Builder.
//    {
//      new DescribableImpl(com.qlangtech.tis.datax.impl.DataxReader.class)
//        // , new DescribableImpl(com.qlangtech.tis.plugin.ds.DataSourceFactory.class)
//        , new DescribableImpl(com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory.class)
//    } ;
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

  public IEndTypeGetter.EndType getSourceType() {
    return sourceType;
  }

  public void setSourceType(IEndTypeGetter.EndType sourceType) {
    this.sourceType = sourceType;
  }

  public IEndTypeGetter.EndType getTargetType() {
    return targetType;
  }

  public void setTargetType(IEndTypeGetter.EndType targetType) {
    this.targetType = targetType;
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
}
