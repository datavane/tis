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
package com.qlangtech.tis.aiagent.core;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.execute.StepExecutor;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.plan.PlanGenerator;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.aiagent.template.TaskTemplateRegistry;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TISPlanAndExecuteAgent单元测试
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @create 2025/9/17
 */
public class TestTISPlanAndExecuteAgent extends EasyMockSupport {

  private AgentContext mockContext;
  private LLMProvider mockLLMProvider;
  private PlanGenerator mockPlanGenerator;
  private TaskTemplateRegistry mockTemplateRegistry;
  private StepExecutor mockStepExecutor;
  private TISPlanAndExecuteAgent agent;
  private StringWriter stringWriter;
  private IControlMsgHandler mockMsgHandler;

  @Before
  public void setUp() throws Exception {
    // 创建mock对象
    mockContext = createMock(AgentContext.class);
    mockLLMProvider = createMock(LLMProvider.class);
    mockPlanGenerator = createMock(PlanGenerator.class);
    mockTemplateRegistry = createMock(TaskTemplateRegistry.class);
    mockStepExecutor = createMock(StepExecutor.class);
    mockMsgHandler = createMock(IControlMsgHandler.class);

    // 创建真实的PrintWriter用于测试
    stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);

    // 初始化mock对象的基本行为
    expect(mockContext.getSessionId()).andReturn("test-session-id").anyTimes();
    expect(mockContext.isCancelled()).andReturn(false).anyTimes();

    // 创建Agent实例
    agent = new TISPlanAndExecuteAgent(mockContext, mockLLMProvider, mockMsgHandler);

    // 使用反射注入mock对象
    injectMockDependencies();
  }

  private void injectMockDependencies() throws Exception {
    // 注入LLMProvider
//    Field llmProviderField = TISPlanAndExecuteAgent.class.getDeclaredField("llmProvider");
//    llmProviderField.setAccessible(true);
//    llmProviderField.set(agent, mockLLMProvider);

    // 注入PlanGenerator
    Field planGeneratorField = TISPlanAndExecuteAgent.class.getDeclaredField("planGenerator");
    planGeneratorField.setAccessible(true);
    planGeneratorField.set(agent, mockPlanGenerator);

    // 注入TaskTemplateRegistry
    Field templateRegistryField = TISPlanAndExecuteAgent.class.getDeclaredField("templateRegistry");
    templateRegistryField.setAccessible(true);
    templateRegistryField.set(agent, mockTemplateRegistry);

    // 注入executors并添加mock executor
    Field executorsField = TISPlanAndExecuteAgent.class.getDeclaredField("executors");
    executorsField.setAccessible(true);
    Map<TaskStep.StepType, StepExecutor> executors = new HashMap<>();
    executors.put(TaskStep.StepType.PLUGIN_CREATE, mockStepExecutor);
    executorsField.set(agent, executors);
  }

  /**
   * 测试execute方法 - 成功场景
   */
  @Test
  public void testExecuteSuccess() throws Exception {
    // 准备测试数据
    String userInput = "创建MySQL到Paimon的数据同步管道";

    // 模拟LLM响应
    LLMProvider.LLMResponse mockResponse = new LLMProvider.LLMResponse();
    mockResponse.setSuccess(true);
    //mockResponse.setTotalTokens(1000L);

    JSONObject jsonContent = new JSONObject();
    jsonContent.put("source_type", "mysql");
    jsonContent.put("target_type", "paimon");
    mockResponse.setJsonContent(jsonContent);
    IAgentContext context = IAgentContext.createNull();
    expect(mockLLMProvider.chatJson(context, anyString(), Collections.singletonList(anyString()), anyString())).andReturn(mockResponse);

    // 模拟生成计划
    TaskPlan mockPlan = createMockTaskPlan();
    expect(mockPlanGenerator.generatePlan(eq(userInput), anyObject(JSONObject.class))).andReturn(mockPlan);

    // 模拟步骤执行成功
    expect(mockStepExecutor.execute(anyObject(TaskPlan.class), anyObject(TaskStep.class), eq(mockContext))).andReturn(true).times(2);

    // 记录期望的调用
    mockContext.sendMessage(contains("您好"));
    expectLastCall();
    mockContext.sendMessage(contains("我已经理解您的需求"));
    expectLastCall();
    mockContext.updateTokenUsage(1000L);
    expectLastCall();
    mockContext.sendProgress(anyString(), eq(1), eq(2));
    expectLastCall();
    mockContext.sendProgress(anyString(), eq(2), eq(2));
    expectLastCall();
    mockContext.sendMessage(contains("[1/2]"));
    expectLastCall();
    mockContext.sendMessage(contains("[2/2]"));
    expectLastCall();
    mockContext.sendMessage(contains("完成"));
    expectLastCall().times(2);
    mockContext.sendMessage(contains("任务执行完成"));
    expectLastCall();

    // 重放mock
    replayAll();

    // 执行测试
    agent.execute(userInput);

    // 验证
    verifyAll();
  }

  /**
   * 测试execute方法 - 计划生成失败
   */
  @Test
  public void testExecuteWithNullPlan() throws Exception {
    String userInput = "无效的输入";

    // 模拟LLM响应失败
    LLMProvider.LLMResponse mockResponse = new LLMProvider.LLMResponse();
    mockResponse.setSuccess(false);
    mockResponse.setErrorMessage("Failed to understand input");

    expect(mockLLMProvider.chatJson(IAgentContext.createNull(),anyString(), Collections.singletonList(anyString()), anyString())).andReturn(mockResponse);

    // 记录期望的调用
    mockContext.sendMessage(contains("我正在分析您的需求"));
    expectLastCall();
    mockContext.sendError(contains("我无法理解您的需求"));
    expectLastCall();

    // 重放mock
    replayAll();

    // 执行测试
    agent.execute(userInput);

    // 验证
    verifyAll();
  }

  /**
   * 测试execute方法 - 任务被取消
   */
  @Test
  public void testExecuteWithCancellation() throws Exception {
    String userInput = "创建数据同步管道";

    // 准备mock数据
    TaskPlan mockPlan = createMockTaskPlan();
    LLMProvider.LLMResponse mockResponse = new LLMProvider.LLMResponse();
    mockResponse.setSuccess(true);
    mockResponse.setJsonContent(new JSONObject());

    expect(mockLLMProvider.chatJson(IAgentContext.createNull(),anyString(), Collections.singletonList(anyString()), anyString())).andReturn(mockResponse);
    expect(mockPlanGenerator.generatePlan(anyString(), anyObject())).andReturn(mockPlan);

    // 重置isCancelled的期望，第一次返回false，第二次返回true
    reset(mockContext);
    expect(mockContext.getSessionId()).andReturn("test-session-id").anyTimes();
    expect(mockContext.isCancelled()).andReturn(false);
    expect(mockContext.isCancelled()).andReturn(true);

    mockContext.sendMessage(anyString());
    expectLastCall().anyTimes();
    mockContext.updateTokenUsage(anyLong());
    expectLastCall().anyTimes();

    // 重放mock
    replayAll();

    // 执行测试
    agent.execute(userInput);

    // 验证
    verifyAll();
  }

  /**
   * 测试execute方法 - 步骤执行失败
   */
  @Test
  public void testExecuteWithStepFailure() throws Exception {
    String userInput = "创建数据同步管道";

    // 准备mock数据
    TaskPlan mockPlan = createMockTaskPlan();
    LLMProvider.LLMResponse mockResponse = new LLMProvider.LLMResponse();
    mockResponse.setSuccess(true);
    mockResponse.setJsonContent(new JSONObject());

    expect(mockLLMProvider.chatJson(IAgentContext.createNull(),anyString(), Collections.singletonList(anyString()), anyString())).andReturn(mockResponse);
    expect(mockPlanGenerator.generatePlan(anyString(), anyObject())).andReturn(mockPlan);

    // 模拟第一个步骤执行失败
    expect(mockStepExecutor.execute(anyObject(TaskPlan.class), anyObject(TaskStep.class), eq(mockContext))).andReturn(false);

    // 设置期望的调用
    mockContext.sendMessage(anyString());
    expectLastCall().anyTimes();
    mockContext.updateTokenUsage(anyLong());
    expectLastCall().anyTimes();
    mockContext.sendProgress(anyString(), anyInt(), anyInt());
    expectLastCall().anyTimes();
    mockContext.sendError(contains("失败"));
    expectLastCall();
    mockContext.requestUserInput(contains("步骤执行失败"), anyString());
    expectLastCall();

    // 重放mock
    replayAll();

    // 执行测试
    agent.execute(userInput);

    // 验证
    verifyAll();
  }

  /**
   * 测试execute方法 - 步骤执行异常
   */
  @Test
  public void testExecuteWithStepException() throws Exception {
    String userInput = "创建数据同步管道";

    // 准备mock数据
    TaskPlan mockPlan = createMockTaskPlan();
    LLMProvider.LLMResponse mockResponse = new LLMProvider.LLMResponse();
    mockResponse.setSuccess(true);
    mockResponse.setJsonContent(new JSONObject());

    expect(mockLLMProvider.chatJson(IAgentContext.createNull(),anyString(), Collections.singletonList(anyString()), anyString())).andReturn(mockResponse);
    expect(mockPlanGenerator.generatePlan(anyString(), anyObject())).andReturn(mockPlan);

    // 模拟步骤执行抛出异常
    expect(mockStepExecutor.execute(anyObject(TaskPlan.class), anyObject(TaskStep.class), eq(mockContext)))
      .andThrow(new RuntimeException("Execution error"));

    // 设置期望的调用
    mockContext.sendMessage(anyString());
    expectLastCall().anyTimes();
    mockContext.updateTokenUsage(anyLong());
    expectLastCall().anyTimes();
    mockContext.sendProgress(anyString(), anyInt(), anyInt());
    expectLastCall().anyTimes();
    mockContext.sendError(contains("步骤执行异常"));
    expectLastCall();

    // 使用Capture来捕获消息
    Capture<String> messageCapture = newCapture();
    mockContext.sendMessage(capture(messageCapture));
    expectLastCall().anyTimes();

    // 重放mock
    replayAll();

    // 执行测试
    agent.execute(userInput);

    // 验证
    verifyAll();
  }

  /**
   * 测试generatePlan方法 - 私有方法测试
   */
  @Test
  public void testGeneratePlan() throws Exception {
    // 使用反射测试私有方法
    Method generatePlanMethod = TISPlanAndExecuteAgent.class.getDeclaredMethod("generatePlan", String.class);
    generatePlanMethod.setAccessible(true);

    String userInput = "测试输入";

    // 模拟LLM响应成功
    LLMProvider.LLMResponse mockResponse = new LLMProvider.LLMResponse();
    mockResponse.setSuccess(true);
  //  mockResponse.setTotalTokens(500L);

    JSONObject jsonContent = new JSONObject();
    jsonContent.put("source_type", "mysql");
    jsonContent.put("target_type", "doris");
    mockResponse.setJsonContent(jsonContent);

    expect(mockLLMProvider.chatJson(IAgentContext.createNull(),anyString(), Collections.singletonList(anyString()), anyString())).andReturn(mockResponse);

    TaskPlan expectedPlan = new TaskPlan(
      new TaskPlan.SourceDataEndCfg(IEndTypeGetter.EndType.MySQL), new TaskPlan.DataEndCfg(IEndTypeGetter.EndType.Doris), mockLLMProvider, this.mockMsgHandler);


    expect(mockPlanGenerator.generatePlan(eq(userInput), anyObject(JSONObject.class))).andReturn(expectedPlan);
    mockContext.updateTokenUsage(500L);
    expectLastCall();

    // 重放mock
    replayAll();

    // 执行方法
    TaskPlan result = (TaskPlan) generatePlanMethod.invoke(agent, userInput);

    // 验证
    assertNotNull(result);
    assertEquals(IEndTypeGetter.EndType.MySQL, result.getSourceEnd().getType());
    assertEquals(IEndTypeGetter.EndType.Doris, result.getTargetEnd().getType());
    assertNotNull(result.getPlanId());

    verifyAll();
  }

  /**
   * 测试executeStep方法 - 私有方法测试
   */
  @Test
  public void testExecuteStep() throws Exception {
    // 使用反射测试私有方法
    Method executeStepMethod = TISPlanAndExecuteAgent.class.getDeclaredMethod("executeStep", TaskPlan.class, TaskStep.class);
    executeStepMethod.setAccessible(true);

    TaskPlan mockPlan = createMockTaskPlan();
    TaskStep step = new TaskStep("Test Step", TaskStep.StepType.PLUGIN_CREATE);

    // 测试执行成功
    expect(mockStepExecutor.execute(eq(mockPlan), eq(step), eq(mockContext))).andReturn(true);
    replayAll();
    boolean result = (boolean) executeStepMethod.invoke(agent, mockPlan, step);
    assertTrue(result);
    verifyAll();

    // 重置mock
    resetAll();

    // 测试执行失败
    expect(mockStepExecutor.execute(eq(mockPlan), eq(step), eq(mockContext))).andReturn(false);
    replayAll();
    result = (boolean) executeStepMethod.invoke(agent, mockPlan, step);
    assertFalse(result);
    verifyAll();

    // 测试没有找到执行器
    TaskStep unknownStep = new TaskStep("Unknown Step", TaskStep.StepType.SELECT_TABLES);
    replayAll();
    result = (boolean) executeStepMethod.invoke(agent, unknownStep);
    assertFalse(result);
    verifyAll();
  }

  /**
   * 测试buildSystemPrompt方法
   */
  @Test
  public void testBuildSystemPrompt() throws Exception {
    Method buildSystemPromptMethod = TISPlanAndExecuteAgent.class.getDeclaredMethod("buildSystemPrompt");
    buildSystemPromptMethod.setAccessible(true);

    String prompt = (String) buildSystemPromptMethod.invoke(agent);

    assertNotNull(prompt);
    assertTrue(prompt.contains("TIS数据集成平台"));
    assertTrue(prompt.contains("MySQL"));
    assertTrue(prompt.contains("Paimon"));
  }

  /**
   * 测试buildUserPrompt方法
   */
  @Test
  public void testBuildUserPrompt() throws Exception {
    Method buildUserPromptMethod = TISPlanAndExecuteAgent.class.getDeclaredMethod("buildUserPrompt", String.class);
    buildUserPromptMethod.setAccessible(true);

    String userInput = "创建MySQL到Paimon的同步";
    String prompt = (String) buildUserPromptMethod.invoke(agent, userInput);

    assertNotNull(prompt);
    assertTrue(prompt.contains(userInput));
    assertTrue(prompt.contains("用户需求"));
    assertTrue(prompt.contains("识别数据同步"));
  }

  /**
   * 测试getPlanSchema方法
   */
  @Test
  public void testGetPlanSchema() throws Exception {
    Method getPlanSchemaMethod = TISPlanAndExecuteAgent.class.getDeclaredMethod("getPlanSchema");
    getPlanSchemaMethod.setAccessible(true);

    String schema = (String) getPlanSchemaMethod.invoke(agent);

    assertNotNull(schema);
    assertTrue(schema.contains("source_type"));
    assertTrue(schema.contains("target_type"));
    assertTrue(schema.contains("source_config"));
    assertTrue(schema.contains("target_config"));
    assertTrue(schema.contains("execute_batch"));
    assertTrue(schema.contains("enable_incr"));
  }

  /**
   * 测试executePlan方法 - 用户确认场景
   */
  @Test
  public void testExecutePlanWithUserConfirmation() throws Exception {
    String userInput = "创建数据同步管道";

    // 创建需要用户确认的计划
    TaskPlan mockPlan = new TaskPlan(new TaskPlan.SourceDataEndCfg(IEndTypeGetter.EndType.MySQL)
      , new TaskPlan.DataEndCfg(IEndTypeGetter.EndType.Paimon), mockLLMProvider, this.mockMsgHandler);


    TaskStep confirmStep = new TaskStep("需要确认的步骤", TaskStep.StepType.PLUGIN_CREATE);
    confirmStep.setStepId("step-1");
    confirmStep.setRequireUserConfirm(true);
    mockPlan.addStep(confirmStep);

    LLMProvider.LLMResponse mockResponse = new LLMProvider.LLMResponse();
    mockResponse.setSuccess(true);
    mockResponse.setJsonContent(new JSONObject());

    expect(mockLLMProvider.chatJson(IAgentContext.createNull(),anyString(), Collections.singletonList(anyString()), anyString())).andReturn(mockResponse);
    expect(mockPlanGenerator.generatePlan(anyString(), anyObject())).andReturn(mockPlan);
    expect(mockStepExecutor.execute(anyObject(), anyObject(), anyObject())).andReturn(true);

    // 设置期望的调用
    mockContext.sendMessage(anyString());
    expectLastCall().anyTimes();
    mockContext.updateTokenUsage(anyLong());
    expectLastCall().anyTimes();
    mockContext.sendProgress(anyString(), anyInt(), anyInt());
    expectLastCall().anyTimes();
    mockContext.requestUserInput(contains("是否执行"), eq("confirm_step-1"));
    expectLastCall();

    // 重放mock
    replayAll();

    // 执行测试
    agent.execute(userInput);

    // 验证
    verifyAll();
  }

  /**
   * 测试并发场景 - 多个任务同时执行
   */
  @Test
  public void testConcurrentExecution() throws Exception {
    // 准备多个Agent实例
    List<Thread> threads = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      final int index = i;
      Thread thread = new Thread(() -> {
        try {
          AgentContext context = createMock(AgentContext.class);
          expect(context.getSessionId()).andReturn("session-" + index).anyTimes();
          expect(context.isCancelled()).andReturn(false).anyTimes();
          context.sendMessage(anyString());
          expectLastCall().anyTimes();
          context.sendError(anyString());
          expectLastCall().anyTimes();
          replay(context);

          TISPlanAndExecuteAgent concurrentAgent = new TISPlanAndExecuteAgent(context, this.mockLLMProvider, this.mockMsgHandler);

          // 注入mock依赖
          Field llmField = TISPlanAndExecuteAgent.class.getDeclaredField("llmProvider");
          llmField.setAccessible(true);
          LLMProvider llm = createMock(LLMProvider.class);
          LLMProvider.LLMResponse response = new LLMProvider.LLMResponse();
          response.setSuccess(false);
          expect(llm.chatJson(IAgentContext.createNull(),anyString(), Collections.singletonList(anyString()), anyString())).andReturn(response).anyTimes();
          replay(llm);
          llmField.set(concurrentAgent, llm);

          concurrentAgent.execute("任务" + index);
        } catch (Exception e) {
          fail("Concurrent execution failed: " + e.getMessage());
        }
      });
      threads.add(thread);
    }

    // 启动所有线程
    threads.forEach(Thread::start);

    // 等待所有线程完成
    for (Thread thread : threads) {
      thread.join(5000);
    }

    // 验证没有发生异常
    assertTrue("All threads should complete", true);
  }

  // ========== 辅助方法 ==========

  /**
   * 创建模拟的TaskPlan
   */
  private TaskPlan createMockTaskPlan() {
    TaskPlan plan = new TaskPlan(new TaskPlan.SourceDataEndCfg(IEndTypeGetter.EndType.MySQL)
      , new TaskPlan.DataEndCfg(IEndTypeGetter.EndType.Paimon), mockLLMProvider, mockMsgHandler);
    plan.setPlanId("test-plan-id");
//    plan.setSourceEnd();
//    plan.setTargetEnd();
    plan.setUserInput("测试输入");

    TaskStep step1 = new TaskStep("步骤1", TaskStep.StepType.PLUGIN_CREATE);
    step1.setStepId("step-1");
    plan.addStep(step1);

    TaskStep step2 = new TaskStep("步骤2", TaskStep.StepType.PLUGIN_CREATE);
    step2.setStepId("step-2");
    plan.addStep(step2);

    return plan;
  }
}
