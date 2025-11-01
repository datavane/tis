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
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * TISPlanAndExecuteAgent集成测试 - 不使用Mock框架
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @create 2025/9/17
 */
public class ITTISPlanAndExecuteAgent extends EasyMockSupport {

  private StringWriter outputWriter;
  private SSEEventWriter printWriter;
  private TestAgentContext testContext;
  private MockLLMProvider mockLLMProvider;
  private IControlMsgHandler controlMsgHandler;

  @Before
  public void setUp() {
    outputWriter = new StringWriter();
    printWriter = new SSEEventWriter(new PrintWriter(outputWriter));
    testContext = new TestAgentContext("test-session", printWriter);
    mockLLMProvider = new MockLLMProvider();
    controlMsgHandler = createMock(IControlMsgHandler.class);
  }

  /**
   * 测试完整的任务执行流程
   */
  @Test
  public void testCompleteTaskFlow() throws Exception {
    // 创建Agent，使用测试配置
    TISPlanAndExecuteAgent agent = new TestableAgent(testContext, mockLLMProvider, controlMsgHandler);

    // 准备用户输入
    String userInput = "创建MySQL到Paimon的数据同步管道，MySQL地址192.168.1.100，用户root，密码123456，数据库test";

    // 执行任务
    agent.execute(userInput);

    // 等待异步操作完成
    Thread.sleep(1000);

    // 验证输出
    String output = outputWriter.toString();
    assertTrue("Should contain greeting", output.contains("您好"));
    assertTrue("Should contain understanding", output.contains("我已经理解您的需求"));
    assertTrue("Should show progress", output.contains("[1/"));
    assertTrue("Should complete", output.contains("任务") && output.contains("完成"));

    // 验证消息记录
    List<String> messages = testContext.getMessages();
    assertFalse("Should have messages", messages.isEmpty());

    // 验证进度记录
    List<TestAgentContext.ProgressInfo> progressList = testContext.getProgressList();
    assertFalse("Should have progress", progressList.isEmpty());
  }

  /**
   * 测试任务取消功能
   */
  @Test
  public void testTaskCancellation() throws Exception {
    // 创建可取消的context
    CancellableAgentContext cancellableContext = new CancellableAgentContext("cancel-test", printWriter);
    TISPlanAndExecuteAgent agent = new TestableAgent(cancellableContext, mockLLMProvider, controlMsgHandler);

    // 启动任务
    Thread taskThread = new Thread(() -> {
      agent.execute("创建长时间运行的同步任务");
    });
    taskThread.start();

    // 等待任务开始
    Thread.sleep(500);

    // 取消任务
    cancellableContext.cancel();

    // 等待任务结束
    taskThread.join(3000);

    // 验证任务被取消
    String output = outputWriter.toString();
    assertTrue("Should contain cancellation message", output.contains("任务已被取消"));
  }

  /**
   * 测试错误恢复机制
   */
  @Test
  public void testErrorRecovery() throws Exception {
    // 创建会失败的LLM Provider
    FailingLLMProvider failingProvider = new FailingLLMProvider();
    TISPlanAndExecuteAgent agent = new TestableAgent(testContext, failingProvider, controlMsgHandler);

    // 执行任务
    agent.execute("测试错误恢复");

    // 验证错误处理
    List<String> errors = testContext.getErrors();
    assertFalse("Should have errors", errors.isEmpty());
    assertTrue("Should contain error message",
      errors.stream().anyMatch(e -> e.contains("无法理解您的需求")));
  }

  /**
   * 测试并发任务执行
   */
  @Test
  public void testConcurrentTasks() throws Exception {
    int taskCount = 10;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completeLatch = new CountDownLatch(taskCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // 创建并启动多个任务
    for (int i = 0; i < taskCount; i++) {
      final int taskId = i;
      new Thread(() -> {
        try {
          // 等待所有线程准备好
          startLatch.await();

          StringWriter writer = new StringWriter();
          SSEEventWriter pw = new SSEEventWriter(new PrintWriter(writer));
          TestAgentContext ctx = new TestAgentContext("task-" + taskId, pw);
          TISPlanAndExecuteAgent agent = new TestableAgent(ctx, mockLLMProvider, controlMsgHandler);

          // 执行任务
          agent.execute("任务 " + taskId);

          // 检查结果
          if (writer.toString().contains("完成")) {
            successCount.incrementAndGet();
          } else {
            failureCount.incrementAndGet();
          }
        } catch (Exception e) {
          failureCount.incrementAndGet();
        } finally {
          completeLatch.countDown();
        }
      }).start();
    }

    // 同时启动所有任务
    startLatch.countDown();

    // 等待所有任务完成
    assertTrue("All tasks should complete within timeout",
      completeLatch.await(10, TimeUnit.SECONDS));

    // 验证结果
    assertEquals("All tasks should complete", taskCount, successCount.get() + failureCount.get());
    assertTrue("Most tasks should succeed", successCount.get() > taskCount * 0.8);
  }

  // ========== 测试辅助类 ==========

  /**
   * 可测试的Agent，允许注入mock依赖
   */
  private static class TestableAgent extends TISPlanAndExecuteAgent {
    public TestableAgent(AgentContext context, LLMProvider llmProvider, IControlMsgHandler controlMsgHandler) {
      super(context, llmProvider, controlMsgHandler);
      try {
        // 使用反射注入LLMProvider
//        java.lang.reflect.Field field = TISPlanAndExecuteAgent.class.getDeclaredField("llmProvider");
//        field.setAccessible(true);
//        field.set(this, llmProvider);

        // 注入测试用的StepExecutor
        java.lang.reflect.Field executorsField = TISPlanAndExecuteAgent.class.getDeclaredField("executors");
        executorsField.setAccessible(true);
        java.util.Map<TaskStep.StepType, StepExecutor> executors =
          (java.util.Map<TaskStep.StepType, StepExecutor>) executorsField.get(this);

        executors.put(TaskStep.StepType.PLUGIN_CREATE, new MockStepExecutor());
        executors.put(TaskStep.StepType.PLUGIN_INSTALL, new MockStepExecutor());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * 测试用的AgentContext
   */
  private static class TestAgentContext extends AgentContext {
    private final List<String> messages = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<ProgressInfo> progressList = new ArrayList<>();

    public TestAgentContext(String sessionId, SSEEventWriter sseWriter) {
      super(sessionId, sseWriter);
    }

    @Override
    public void sendMessage(String message) {
      messages.add(message);
      super.sendMessage(message);
    }

    @Override
    public void sendError(String error) {
      errors.add(error);
      super.sendError(error);
    }

    @Override
    public void sendProgress(String taskName, int current, int total) {
      progressList.add(new ProgressInfo(taskName, current, total));
      super.sendProgress(taskName, current, total);
    }

    public List<String> getMessages() {
      return messages;
    }

    public List<String> getErrors() {
      return errors;
    }

    public List<ProgressInfo> getProgressList() {
      return progressList;
    }

    static class ProgressInfo {
      final String taskName;
      final int current;
      final int total;

      ProgressInfo(String taskName, int current, int total) {
        this.taskName = taskName;
        this.current = current;
        this.total = total;
      }
    }
  }

  /**
   * 可取消的AgentContext
   */
  private static class CancellableAgentContext extends TestAgentContext {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public CancellableAgentContext(String sessionId, SSEEventWriter sseWriter) {
      super(sessionId, sseWriter);
    }

    @Override
    public void cancel() {
      cancelled.set(true);
      super.cancel();
    }

    @Override
    public boolean isCancelled() {
      return cancelled.get();
    }
  }

  /**
   * Mock的LLMProvider
   */
  public static class MockLLMProvider extends LLMProvider {
    @Override
    public LLMResponse chat(IAgentContext context, String prompt, List<String> systemPrompt) {
      LLMResponse response = new LLMResponse();
      response.setSuccess(true);
      response.setContent("Mock response");
     // response.setTotalTokens(100);
      return response;
    }

    @Override
    public LLMResponse chatJson(IAgentContext context, String prompt, List<String> systemPrompt, String jsonSchema) {
      LLMResponse response = new LLMResponse();
      response.setSuccess(true);

      JSONObject json = new JSONObject();
      json.put("source_type", "mysql");
      json.put("target_type", "paimon");

      JSONObject sourceConfig = new JSONObject();
      sourceConfig.put("host", "192.168.1.100");
      sourceConfig.put("port", 3306);
      sourceConfig.put("username", "root");
      sourceConfig.put("password", "123456");
      sourceConfig.put("database", "test");
      json.put("source_config", sourceConfig);

      response.setJsonContent(json);
     // response.setTotalTokens(200);
      return response;
    }

    @Override
    public String getProviderName() {
      return "MockProvider";
    }

    @Override
    public boolean isAvailable() {
      return true;
    }


    @Override
    public LLMProvider createConfigInstance() {
      return this;
    }

    @Override
    public String identityValue() {
      return "";
    }
  }

  /**
   * 会失败的LLMProvider
   */
  private static class FailingLLMProvider extends LLMProvider {
    @Override
    public LLMResponse chat(IAgentContext context, String prompt, List<String> systemPrompt) {
      LLMResponse response = new LLMResponse();
      response.setSuccess(false);
      response.setErrorMessage("Simulated failure");
      return response;
    }

    @Override
    public LLMResponse chatJson(IAgentContext context, String prompt, List<String> systemPrompt, String jsonSchema) {
      return chat(context, prompt, systemPrompt);
    }

    @Override
    public String getProviderName() {
      return "FailingProvider";
    }

    @Override
    public boolean isAvailable() {
      return false;
    }


    @Override
    public FailingLLMProvider createConfigInstance() {
      return this;
    }

    @Override
    public String identityValue() {
      return "";
    }
  }

  /**
   * Mock的StepExecutor
   */
  private static class MockStepExecutor implements StepExecutor {
    @Override
    public boolean execute(TaskPlan plan, TaskStep step, AgentContext context) {
      // 模拟执行
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return true;
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
}
