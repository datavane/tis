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
import com.qlangtech.tis.aiagent.execute.impl.PluginDownloadAndInstallExecutor;
import com.qlangtech.tis.aiagent.execute.impl.PluginInstanceCreateExecutor;
import com.qlangtech.tis.aiagent.llm.DeepSeekProvider;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.plan.PlanGenerator;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.aiagent.template.TaskTemplateRegistry;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TIS Plan-And-Execute Agent主控制器
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class TISPlanAndExecuteAgent {
  private static final Logger logger = LoggerFactory.getLogger(TISPlanAndExecuteAgent.class);

  private final AgentContext context;
  private final LLMProvider llmProvider;
  private final PlanGenerator planGenerator;
  private final TaskTemplateRegistry templateRegistry;
  private final Map<TaskStep.StepType, StepExecutor> executors;

  public TISPlanAndExecuteAgent(AgentContext context) {
    this.context = context;

    // 初始化LLM Provider
    LLMProvider.LLMConfig llmConfig = new LLMProvider.LLMConfig();
    llmConfig.setApiKey("sk-2e8c31e1864c48b7b7ccc0f0ac83936e");
    llmConfig.setMaxTokens(4000);
    llmConfig.setDefaultTemperature(0.7);
    this.llmProvider = new DeepSeekProvider(llmConfig);

    // 初始化组件
    this.planGenerator = new PlanGenerator(llmProvider);
    this.templateRegistry = new TaskTemplateRegistry();
    this.executors = new HashMap<>();
    initExecutors();
  }

  /**
   * 执行用户任务
   */
  public void execute(String userInput) {
    try {
      context.sendMessage("您好！我正在分析您的需求...");

      // 1. 解析用户输入，生成执行计划
      TaskPlan plan = generatePlan(userInput);

      if (plan == null || plan.getSteps().isEmpty()) {
        context.sendError("抱歉，我无法理解您的需求，请重新描述。");
        return;
      }

      context.sendMessage(String.format("我已经理解您的需求：从%s同步到%s。现在开始执行...",
        plan.getSourceType(), plan.getTargetType()));

      // 2. 执行任务计划
      executePlan(plan);

    } catch (Exception e) {
      logger.error("Task execution failed", e);
      context.sendError("执行任务时发生错误：" + e.getMessage());
    }
  }

  /**
   * 生成任务计划
   */
  private TaskPlan generatePlan(String userInput) {
    try {
      // 使用LLM分析用户输入
      String systemPrompt = buildSystemPrompt();
      String prompt = buildUserPrompt(userInput);

      LLMProvider.LLMResponse response = llmProvider.chatJson(prompt, systemPrompt, getPlanSchema());

      if (!response.isSuccess()) {
        logger.error("LLM call failed: {}", response.getErrorMessage());
        return null;
      }

      // 更新Token使用情况
      context.updateTokenUsage(response.getTotalTokens());

      // 解析LLM返回的计划
      JSONObject planJson = response.getJsonContent();
      if (planJson == null) {
        logger.error("Failed to parse LLM response plan");
        return null;
      }

      // 生成详细的执行计划
      TaskPlan plan = planGenerator.generatePlan(userInput, planJson);
      plan.setPlanId(UUID.randomUUID().toString());

      return plan;

    } catch (Exception e) {
      logger.error("Failed to generate execution plan", e);
      return null;
    }
  }

  /**
   * 执行任务计划
   */
  private void executePlan(TaskPlan plan) {
    int totalSteps = plan.getTotalSteps();
    int currentStep = 0;

    for (TaskStep step : plan.getSteps()) {
      if (context.isCancelled()) {
        context.sendMessage("任务已被取消");
        break;
      }

      currentStep++;
      context.sendProgress(step.getName(), currentStep, totalSteps);
      context.sendMessage(String.format("[%d/%d] %s", currentStep, totalSteps, step.getName()));

      try {
        // 标记步骤开始
        step.markAsStarted();

        // 检查是否需要用户确认
        if (step.isRequireUserConfirm()) {
          context.requestUserInput(
            String.format("是否执行：%s？请输入yes或no", step.getName()),
            "confirm_" + step.getStepId()
          );

          // 等待用户响应（实际实现需要异步处理）
          String userResponse = waitForUserInput("confirm_" + step.getStepId(), 60);
          if (!"yes".equalsIgnoreCase(userResponse)) {
            step.markAsSkipped();
            context.sendMessage(String.format("跳过步骤：%s", step.getName()));
            continue;
          }
        }

        // 执行步骤
        boolean success = executeStep(plan, step);

        if (success) {
          step.markAsCompleted();
          context.sendMessage(String.format("✓ %s 完成", step.getName()));
        } else {
          step.markAsFailed("执行失败");
          context.sendError(String.format("✗ %s 失败", step.getName()));

          // 询问是否继续
          context.requestUserInput("步骤执行失败，是否继续？", "continue_after_error");
          String continueResponse = waitForUserInput("continue_after_error", 60);
          if (!"yes".equalsIgnoreCase(continueResponse)) {
            break;
          }
        }

      } catch (Exception e) {
        logger.error("Step execution failed: " + step.getName(), e);
        step.markAsFailed(e.getMessage());
        context.sendError(String.format("步骤执行异常：%s - %s", step.getName(), e.getMessage()));
        break;
      }
    }

    // 发送执行总结
    int completedSteps = plan.getCompletedSteps();
    if (completedSteps == totalSteps) {
      context.sendMessage("🎉 任务执行完成！所有步骤都已成功执行。");
    } else {
      context.sendMessage(String.format("任务部分完成。执行了 %d/%d 个步骤。", completedSteps, totalSteps));
    }
  }

  /**
   * 执行单个步骤
   */
  private boolean executeStep(TaskPlan plan, TaskStep step) {
    StepExecutor executor = executors.get(step.getType());
    if (executor == null) {
      logger.warn("Step executor not found: {}", step.getType());
      return false;
    }

    try {
      return executor.execute(plan, step, context);
    } catch (Exception e) {
    //  logger.error("Step execution exception: " + step.getName(), e);
      // return false;
      throw new IllegalStateException("Step execution exception: " + step.getName(), e);
    }
  }

  /**
   * 等待用户输入（简化实现，实际需要异步处理）
   */
  private String waitForUserInput(String fieldId, int timeoutSeconds) {
    // TODO: 实际实现需要通过WebSocket或其他机制异步获取用户输入
    // 这里是简化版本
    try {
      Thread.sleep(1000); // 模拟等待
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return "yes"; // 默认返回yes继续执行
  }

  /**
   * 初始化步骤执行器
   */
  private void initExecutors() {
    // TODO: 注册各类型步骤的执行器
    executors.put(TaskStep.StepType.PLUGIN_INSTALL, new PluginDownloadAndInstallExecutor());
    executors.put(TaskStep.StepType.PLUGIN_CREATE, new PluginInstanceCreateExecutor());
    // executors.put(TaskStep.StepType.EXECUTE_BATCH, new BatchExecutor());
    // executors.put(TaskStep.StepType.EXECUTE_INCR, new IncrExecutor());
  }

  /**
   * 构建系统提示词
   */
  private String buildSystemPrompt() {

    Set<IEndTypeGetter.EndType> dataEnds = IEndTypeGetter.EndType.getDataEnds();
    String supportedDataEnds
      = dataEnds.stream().map((end) -> String.valueOf(end)).collect(Collectors.joining("，"));

    return "你是TIS数据集成平台的智能助手。你的任务是帮助用户创建数据同步管道。\n" +
      "TIS支持多种数据源，包括" + supportedDataEnds + "等各种类型的数据端，" +
      "请根据用户的描述，识别源端和目标端的数据类型。";
  }

  /**
   * 构建用户提示词
   */
  private String buildUserPrompt(String userInput) {
    return String.format("用户需求：%s\n\n请分析上述需求，识别数据同步的源端和目标端类型，并提取配置参数。", userInput);
  }

  /**
   * 获取计划JSON Schema
   */
  private String getPlanSchema() {
    return "{\n" +
      "  \"source_type\": \"string\",\n" +
      "  \"target_type\": \"string\",\n" +
      "  \"options\": {\n" +
      "    \"execute_batch\": \"类型为boolean，表明数据管道创建完成之后是否立即触发全量数据同步\",\n" +
      "    \"enable_incr\": \"类型为boolean，表明数据管道创建完成后是否立即启动增量事实同步\"\n" +
      "  }\n" +
      "}";


//        return "{\n" +
//               "  \"source_type\": \"string\",\n" +
//               "  \"target_type\": \"string\",\n" +
//               "  \"source_config\": {\n" +
//               "    \"host\": \"string\",\n" +
//               "    \"port\": \"number\",\n" +
//               "    \"username\": \"string\",\n" +
//               "    \"password\": \"string\",\n" +
//               "    \"database\": \"string\"\n" +
//               "  },\n" +
//               "  \"target_config\": {\n" +
//               "    \"host\": \"string\",\n" +
//               "    \"database\": \"string\"\n" +
//               "  },\n" +
//               "  \"options\": {\n" +
//               "    \"execute_batch\": \"boolean\",\n" +
//               "    \"enable_incr\": \"boolean\"\n" +
//               "  }\n" +
//               "}";
  }
}
