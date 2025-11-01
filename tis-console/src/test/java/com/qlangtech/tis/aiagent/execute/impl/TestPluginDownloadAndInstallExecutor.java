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

import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.execute.StepExecutor;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DescriptorsJSONForAIPromote;
import com.qlangtech.tis.util.DescriptorsJSONResult;
import org.easymock.EasyMockSupport;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * PluginDownloadExecutor单元测试 - 基础功能测试
 * 注：由于execute方法依赖静态方法TIS.get()等，完整测试需要集成测试环境
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @create 2025/9/19
 */
public class TestPluginDownloadAndInstallExecutor extends EasyMockSupport {

  private PluginDownloadAndInstallExecutor executor;
  private TaskPlan mockPlan;
  private TaskStep mockStep;
  private AgentContext mockContext;
  private IMocksControl control;
  private LLMProvider mockLLMProvider;
  private IControlMsgHandler mockMsgHandler;

  @Before
  public void setUp() {
    executor = new PluginDownloadAndInstallExecutor();
    control = createControl();

    // 创建mock对象
    mockPlan = control.createMock(TaskPlan.class);
    mockStep = control.createMock(TaskStep.class);
    mockContext = control.createMock(AgentContext.class);

    mockLLMProvider = control.createMock(LLMProvider.class);
    mockMsgHandler = control.createMock(IControlMsgHandler.class);
  }

  /**
   * 测试validate方法
   */
  @Test
  public void testValidate() {
    TaskStep step = new TaskStep("test", TaskStep.StepType.PLUGIN_INSTALL);

    StepExecutor.ValidationResult result = executor.validate(step);

    assertNotNull(result);
    assertTrue(result.isValid());
    assertNull(result.getErrorMessage());
  }

  @Test
  public void testExecute() {

    DescriptorsJSONResult desc = DescriptorsJSONForAIPromote.desc("com.qlangtech.tis.plugin.paimon.datax.DataxPaimonWriter");
    System.out.println(JsonUtil.toString(desc.getDescriptorsResult(), true));
//    for (Map.Entry<String, JSONObject> entry : desc.getDescriptorsResult().entrySet()) {
//
//      break;
//    }

    CenterResource.setNotFetchFromCenterRepository();
    TaskPlan taskPlan = new TaskPlan(
      new TaskPlan.SourceDataEndCfg(IEndTypeGetter.EndType.MySQL)
      , new TaskPlan.DataEndCfg(IEndTypeGetter.EndType.Paimon), mockLLMProvider, this.mockMsgHandler);

    executor.execute(taskPlan, mockStep, mockContext);
  }

  /**
   * 测试getSupportedType方法
   */
  @Test
  public void testGetSupportedType() {
    TaskStep.StepType supportedType = executor.getSupportedType();

    assertEquals(TaskStep.StepType.PLUGIN_INSTALL, supportedType);
  }

  /**
   * 测试validate方法 - 使用mock对象
   */
  @Test
  public void testValidateWithMock() {
    // 设置期望
    // validate方法不需要从step读取任何属性，所以不设置期望

    // 重放mock
    control.replay();

    // 执行测试
    StepExecutor.ValidationResult result = executor.validate(mockStep);

    // 验证
    assertNotNull(result);
    assertTrue(result.isValid());
    assertNull(result.getErrorMessage());

    control.verify();
  }

  /**
   * 测试ValidationResult成功工厂方法
   */
  @Test
  public void testValidationResultSuccess() {
    StepExecutor.ValidationResult result =
      StepExecutor.ValidationResult.success();

    assertTrue(result.isValid());
    assertNull(result.getErrorMessage());
  }

  /**
   * 测试ValidationResult失败工厂方法
   */
  @Test
  public void testValidationResultFailure() {
    String errorMessage = "Test error message";
    StepExecutor.ValidationResult result =
      StepExecutor.ValidationResult.failure(errorMessage);

    assertFalse(result.isValid());
    assertEquals(errorMessage, result.getErrorMessage());
  }
}
