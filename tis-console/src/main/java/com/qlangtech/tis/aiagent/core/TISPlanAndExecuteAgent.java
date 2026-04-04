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
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.aiagent.plan.AgentTaskIntention;
import com.qlangtech.tis.aiagent.plan.PlanGenerator;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.aiagent.template.TaskTemplateRegistry;
import com.qlangtech.tis.lang.PayloadLink;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.qlangtech.tis.aiagent.plan.PlanGenerator.KEY_EXECUTE_BATCH;
import static com.qlangtech.tis.aiagent.plan.PlanGenerator.KEY_EXECUTE_INCR;
import static com.qlangtech.tis.aiagent.plan.PlanGenerator.KEY_EXECUTE_OPTION_CONFIG;
import static com.qlangtech.tis.aiagent.plan.PlanGenerator.KEY_EXTRACT_INFO;
import static com.qlangtech.tis.aiagent.plan.PlanGenerator.KEY_INTENTION;
import static com.qlangtech.tis.aiagent.plan.PlanGenerator.KEY_SOURCE;
import static com.qlangtech.tis.aiagent.plan.PlanGenerator.KEY_TARGET;
import static com.qlangtech.tis.aiagent.plan.PlanGenerator.KEY_TYPE;
import static com.qlangtech.tis.datax.impl.DataxReader.SUB_PROP_FIELD_NAME;

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
  private IControlMsgHandler controlMsgHandler;


  public TISPlanAndExecuteAgent(AgentContext context, LLMProvider llmProvider, IControlMsgHandler controlMsgHandler) {
    this.context = context;

    // 初始化LLM Provider
    this.llmProvider = Objects.requireNonNull(llmProvider, "llmProvider can not be null");
    this.controlMsgHandler = controlMsgHandler;
    // 初始化组件
    this.planGenerator = new PlanGenerator(llmProvider, this.controlMsgHandler);
    this.templateRegistry = new TaskTemplateRegistry();

  }

  /**
   * 执行用户任务
   */
  public void execute(String userInput) {
    // try {
    //context.sendMessage("您好！我正在分析您的需求...");

    // 1. 解析用户输入，生成执行计划
    TaskPlan plan = generatePlan(userInput);

    if (plan == null || plan.getSteps().isEmpty()) {
      context.sendError("抱歉，我无法理解您的需求，请重新描述。");
      return;
    }

    context.sendMessage(String.format("我已经理解您的需求：从%s同步到%s。现在开始执行...", plan.getSourceEnd().getType(),
      plan.getTargetEnd().getType()));

    // 2. 执行任务计划
    executePlan(plan);


  }

  /**
   * 生成任务计划
   */
  TaskPlan generatePlan(String userInput) {
    try {
      // 使用LLM分析用户输入
      String systemPrompt = buildSystemPrompt();
      String prompt = buildUserPrompt(userInput);
      prompt += "";

      LLMProvider.LLMResponse response = llmProvider.chatJson(Objects.requireNonNull(context,
        "context can not be " + "null"), new UserPrompt("您好！我正在分析您的需求...", prompt),
        Collections.singletonList(systemPrompt), getPlanSchema());

      if (!response.isSuccess()) {
        throw new IllegalStateException("LLM call failed: " + response.getErrorMessage());
      }

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
      throw new RuntimeException(e);
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
          context.requestUserInput(String.format("是否执行：%s？请输入yes或no", step.getName()), "confirm_" + step.getStepId());

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
        // context.sendError(String.format("步骤执行异常：%s - %s", step.getName(), e.getMessage()));
        final String errMsgTpl = "步骤执行异常：%s - %s";
        TisException tisException = null;
        if ((tisException = ExceptionUtils.throwableOfThrowable(e, TisException.class)) != null) {
          Optional<PayloadLink> payloadLink = tisException.getPayloadLink();
          PayloadLink[] links =
            Objects.requireNonNull(payloadLink, "payloadLink can not be null").map((l) -> new PayloadLink[]{l}).orElse(new PayloadLink[0]);
          context.sendError(String.format(errMsgTpl, step.getName(), tisException.getMessage()), links);
        } else {
          context.sendError(String.format(errMsgTpl, step.getName(), e.getMessage()));
        }


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

    // try {
    return step.execute(plan, step, context);
    //    } catch (Exception e) {
    //      //  logger.error("Step execution exception: " + step.getName(), e);
    //      // return false;
    //      throw new IllegalStateException("Step execution exception: " + step.getName(), e);
    //    }
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
   * 构建系统提示词
   */
  private String buildSystemPrompt() {

    Set<IEndTypeGetter.EndType> dataEnds = IEndTypeGetter.EndType.getDataEnds();
    String supportedDataEnds = dataEnds.stream().map((end) -> {
      Optional<String> desc = end.getDesc();
      StringBuilder endDesc = new StringBuilder(String.valueOf(end));
      desc.ifPresent((d) -> {
        endDesc.append("(").append(d).append(")");
      });
      return endDesc.toString();
    }).collect(Collectors.joining("，"));

    return "你是TIS数据集成平台的智能助手。你的任务是帮助用户创建数据同步管道。\n" + "TIS支持多种数据源，枚举端类型为：" + supportedDataEnds + "，" +
      "请根据用户的描述，识别源端和目标端的类型。现在智能平台接受用户提交任务，需要识别任务意图对应输出json结果中的'" + KEY_INTENTION + "'字段，他是一个枚举类型，支持的值为：" + Arrays.stream(AgentTaskIntention.values()).map(String::valueOf).collect(Collectors.joining(",")) + "\n\n重要：用户请求的分析结果必须严格按照用户请求中 'response_format' 参数所指定的 JSON Schema 结构进行输出。不得添加、删除或修改任何字段名称。输出的 JSON 必须与 Schema 完全匹配。";
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
  private TISJsonSchema getPlanSchema() {

    //    String schemaExample =
    //      "{\n" + "  \"" + KEY_INTENTION + "\":\"string类型\",\n" + "  \"" + KEY_SOURCE + "\": {\"" + KEY_TYPE +
    //        "\":\"string,值必须为系统提示词中枚举到的端类型关键词，大小写必须一致\",\"" + KEY_EXTRACT_INFO + "\":\"类型为string" +
    //        "，从用户提供的数据通道任务描述信息中抽取源端相关的描述信息\",\"" //
    //      + SUB_PROP_FIELD_NAME //
    //      + "\":\"类型为string，从用户提供的数据通道任务描述信息中抽取源端相关的信息（如：‘除AA、BB表以外的所有表’，‘前缀为user的表’，‘AA，BB’）,如不能抽取得到则设置为空字符串\"} ,
    //      \n" //
    //      + "  \"" //
    //      + KEY_TARGET + "\": {\"" + KEY_TYPE + "\":\"string," //
    //      + "值必须为系统提示词中枚举到的端类型关键词，大小写必须一致\",\"" //
    //      + KEY_EXTRACT_INFO + "\":\"类型为string，从用户提供的数据通道任务描述信息中抽取目标端相关的描述信息\"} ,"  //
    //      + "\n" + "  \"" + KEY_EXECUTE_OPTION_CONFIG + "\": {\n" + "    \""  //
    //      + KEY_EXECUTE_BATCH + "\": \"类型为boolean" + "，表明数据管道创建完成之后是否立即触发全量数据同步，默认为false\",\n"  //
    //      + "    \"" + KEY_EXECUTE_INCR + "\": \"类型为boolean" + "，表明数据管道创建完成后是否立即启动增量事实同步，默认为false\"\n" + "  }\n" +
    //      "}";


    TISJsonSchema.Builder builder = TISJsonSchema.Builder.create("CreatePipelineRequest", Optional.empty());
    builder.addProperty(KEY_INTENTION, TISJsonSchema.FieldType.String, "是一个枚举类型").setValEnums(AgentTaskIntention.CreatePipeline, AgentTaskIntention.Other);

    builder.addObjectProperty(KEY_SOURCE, (innerBuilder) -> {
      innerBuilder.addProperty(KEY_TYPE, TISJsonSchema.FieldType.String, "值必须为系统提示词中枚举到的端类型关键词，大小写必须一致");
      innerBuilder.addProperty(KEY_EXTRACT_INFO, TISJsonSchema.FieldType.String, "从用户提供的数据通道任务描述信息中抽取源端相关的描述信息(不需要结构化数据，如：json或xml)");
      innerBuilder.addProperty(SUB_PROP_FIELD_NAME, TISJsonSchema.FieldType.String,
        "从用户提供的数据通道任务描述信息中抽取源端相关的信息（如：‘除AA" + "、BB表以外的所有表’，‘前缀为user" + "的表’，‘AA，BB’）,如不能抽取得到则设置为空字符串");
      //  innerBuilder.setRequiredFields(KEY_TYPE, KEY_EXTRACT_INFO, SUB_PROP_FIELD_NAME);
    });
    builder.addObjectProperty(KEY_TARGET, (inner) -> {
      inner.addProperty(KEY_TYPE, TISJsonSchema.FieldType.String, "值必须为系统提示词中枚举到的端类型关键词，大小写必须一致");
      inner.addProperty(KEY_EXTRACT_INFO, TISJsonSchema.FieldType.String, "从用户提供的数据通道任务描述信息中抽取目标端相关的描述信息(不需要结构化数据，如：json或xml)");
      //inner.setRequiredFields(KEY_TYPE, KEY_EXTRACT_INFO);
    });

    builder.addObjectProperty(KEY_EXECUTE_OPTION_CONFIG, (inner) -> {
      inner.addProperty(KEY_EXECUTE_INCR, TISJsonSchema.FieldType.Boolean, "表明数据管道创建完成后是否立即启动增量实时同步").setDefault(false);
      inner.addProperty(KEY_EXECUTE_BATCH, TISJsonSchema.FieldType.Boolean, "表明数据管道创建完成后是否立即启动批量同步").setDefault(false);
      // inner.setRequiredFields(KEY_EXECUTE_INCR,KEY_EXECUTE_BATCH);
    });
    return builder.build();


    //    //    JSONObject schema = new JSONObject();
    //    //    schema.put("name", );
    //    //    JSONObject schemaProperties = new JSONObject();
    //    //    schemaProperties.put(SCHEMA_TYPE, "object");
    //    JSONObject properties = new JSONObject();
    //
    //    JSONObject property = new JSONObject();
    //    property.put(SCHEMA_TYPE, "string");
    //    property.put(SCHEMA_DESC,
    //      "是一个枚举类型，支持的值为：" + Arrays.stream(AgentTaskIntention.values()).map(String::valueOf).collect(Collectors
    //      .joining(
    //        ",")));
    //    properties.put(KEY_INTENTION, property);
    //
    //    property = new JSONObject();
    //    property.put(SCHEMA_TYPE, "object");
    //    property.put() JSONArray requriredProps = new JSONArray();
    //    requriredProps.add(KEY_TYPE);
    //    requriredProps.add(KEY_EXTRACT_INFO);
    //    requriredProps.add(SUB_PROP_FIELD_NAME);
    //    property.put(SCHEMA_RERUIRED, requriredProps);
    //    // property.put(SCHEMA_DESC,"是一个枚举类型，支持的值为：" + Arrays.stream(AgentTaskIntention.values()).map(String::valueOf)
    //    // .collect(Collectors.joining(",")));
    //    properties.put(KEY_INTENTION, property);
    //
    //
    //    schemaProperties.put("properties", properties);
    //    schema.put("schema", schemaProperties);
    //
    //    return JsonSchema.create(schema);
  }
}
