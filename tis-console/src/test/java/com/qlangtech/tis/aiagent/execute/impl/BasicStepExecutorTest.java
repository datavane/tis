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

import com.google.common.collect.Lists;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;

/**
 * BasicStepExecutor 单元测试
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/12/4
 */
public class BasicStepExecutorTest extends TestCase {

  /**
   * 创建用于测试的 BasicStepExecutor 实例
   */
  private BasicStepExecutor createTestExecutor() {
    return new BasicStepExecutor() {
      @Override
      public boolean execute(TaskPlan plan, TaskStep step, AgentContext context) {
        return false;
      }

      @Override
      public ValidationResult validate(TaskStep step) {
        return ValidationResult.success();
      }

      @Override
      public TaskStep.StepType getSupportedType() {
        return TaskStep.StepType.PLUGIN_CREATE;
      }
    };
  }

  private IPluginContext pluginContext = new PartialSettedPluginContext().setLoginUser(() -> "admin");
  LLMProvider llmProvider = LLMProvider.load(pluginContext, "default");
  /**
   * 测试完全匹配场景
   * 用户提供的表名与数据源中的表名完全匹配
   */
  @Test
  public void testExtractTargetTableInfo_ExactMatch() {
    BasicStepExecutor executor = createTestExecutor();
    IAgentContext context = IAgentContext.createNull();
    String extraTableInfo = "aa,bb,dd";
    List<String> existTables = Lists.newArrayList("aa", "bb", "cc");


    BasicStepExecutor.ExtractTargetTableInfoResult result = executor.extractTargetTableInfo(context, extraTableInfo,
      existTables, llmProvider);

    Assert.assertNotNull("result should not be null", result);
    Assert.assertNotNull("targetTables should not be null", result.getTargetTables());
    Assert.assertNotNull("lostTables should not be null", result.getLostTables());

    // 验证目标表包含 aa 和 bb
    Assert.assertTrue("targetTables should contain 'aa'", result.getTargetTables().contains("aa"));
    Assert.assertTrue("targetTables should contain 'bb'", result.getTargetTables().contains("bb"));

    // 验证丢失的表包含 dd
    Assert.assertTrue("lostTables should contain 'dd'", result.getLostTables().contains("dd"));

    // 验证大小
    Assert.assertEquals("targetTables size should be 2", 2, result.getTargetTables().size());
    Assert.assertEquals("lostTables size should be 1", 1, result.getLostTables().size());
  }

  /**
   * 测试排除模式
   * 用户要求排除某些表(例如"除AA以外的所有表")
   */
  @Test
  public void testExtractTargetTableInfo_ExcludePattern() {
    BasicStepExecutor executor = createTestExecutor();
    IAgentContext context = IAgentContext.createNull();
    String extraTableInfo = "除AA以外的所有表";
    List<String> existTables = Lists.newArrayList("aa", "bb", "cc");


    BasicStepExecutor.ExtractTargetTableInfoResult result = executor.extractTargetTableInfo(context, extraTableInfo,
      existTables, llmProvider);

    Assert.assertNotNull("result should not be null", result);
    Assert.assertNotNull("targetTables should not be null", result.getTargetTables());
    Assert.assertNotNull("lostTables should not be null", result.getLostTables());

    // 验证目标表包含 bb 和 cc,不包含 aa
    Assert.assertTrue("targetTables should contain 'bb'", result.getTargetTables().contains("bb"));
    Assert.assertTrue("targetTables should contain 'cc'", result.getTargetTables().contains("cc"));
    Assert.assertFalse("targetTables should not contain 'aa'", result.getTargetTables().contains("aa"));

    // 验证没有丢失的表
    Assert.assertTrue("lostTables should be empty", result.getLostTables().isEmpty());

    // 验证大小
    Assert.assertEquals("targetTables size should be 2", 2, result.getTargetTables().size());
  }

  /**
   * 测试前缀模式
   * 用户要求以某个前缀开头的所有表
   */
  @Test
  public void testExtractTargetTableInfo_PrefixPattern() {
    BasicStepExecutor executor = createTestExecutor();
    IAgentContext context = IAgentContext.createNull();
    String extraTableInfo = "以aa作为前缀的所有表";
    List<String> existTables = Lists.newArrayList("aa1", "aa2", "bb", "cc");


    BasicStepExecutor.ExtractTargetTableInfoResult result = executor.extractTargetTableInfo(context, extraTableInfo,
      existTables, llmProvider);

    Assert.assertNotNull("result should not be null", result);
    Assert.assertNotNull("targetTables should not be null", result.getTargetTables());
    Assert.assertNotNull("lostTables should not be null", result.getLostTables());

    // 验证目标表包含 aa1 和 aa2
    Assert.assertTrue("targetTables should contain 'aa1'", result.getTargetTables().contains("aa1"));
    Assert.assertTrue("targetTables should contain 'aa2'", result.getTargetTables().contains("aa2"));

    // 验证不包含其他表
    Assert.assertFalse("targetTables should not contain 'bb'", result.getTargetTables().contains("bb"));
    Assert.assertFalse("targetTables should not contain 'cc'", result.getTargetTables().contains("cc"));

    // 验证没有丢失的表
    Assert.assertTrue("lostTables should be empty", result.getLostTables().isEmpty());

    // 验证大小
    Assert.assertEquals("targetTables size should be 2", 2, result.getTargetTables().size());
  }

  /**
   * 测试完全匹配场景
   * 用户提供的所有表都能在数据源中找到
   */
  @Test
  public void testExtractTargetTableInfo_AllMatch() {
    BasicStepExecutor executor = createTestExecutor();
    IAgentContext context = IAgentContext.createNull();
    String extraTableInfo = "table1,table2";
    List<String> existTables = Lists.newArrayList("table1", "table2", "table3");


    BasicStepExecutor.ExtractTargetTableInfoResult result = executor.extractTargetTableInfo(context, extraTableInfo,
      existTables, llmProvider);

    Assert.assertNotNull("result should not be null", result);
    Assert.assertNotNull("targetTables should not be null", result.getTargetTables());
    Assert.assertNotNull("lostTables should not be null", result.getLostTables());

    // 验证目标表包含 table1 和 table2
    Assert.assertTrue("targetTables should contain 'table1'", result.getTargetTables().contains("table1"));
    Assert.assertTrue("targetTables should contain 'table2'", result.getTargetTables().contains("table2"));

    // 验证没有丢失的表
    Assert.assertTrue("lostTables should be empty", result.getLostTables().isEmpty());

    // 验证大小
    Assert.assertEquals("targetTables size should be 2", 2, result.getTargetTables().size());
    Assert.assertEquals("lostTables size should be 0", 0, result.getLostTables().size());
  }

  /**
   * 测试空输入抛出异常
   * extraTableInfo 参数为空,应抛出异常
   */
  @Test
  public void testExtractTargetTableInfo_EmptyInput_ThrowException() {
    BasicStepExecutor executor = createTestExecutor();
    IAgentContext context = IAgentContext.createNull();
    String extraTableInfo = "";
    List<String> existTables = Lists.newArrayList("table1", "table2");


    try {
      executor.extractTargetTableInfo(context, extraTableInfo, existTables, llmProvider);
      fail("should throw IllegalArgumentException when extraTableInfo is empty");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("param userInput can not be empty", e.getMessage());
    }
  }

  /**
   * 测试 null 输入抛出异常
   * extraTableInfo 参数为 null,应抛出异常
   */
  @Test
  public void testExtractTargetTableInfo_NullInput_ThrowException() {
    BasicStepExecutor executor = createTestExecutor();
    IAgentContext context = IAgentContext.createNull();
    String extraTableInfo = null;
    List<String> existTables = Lists.newArrayList("table1", "table2");


    try {
      executor.extractTargetTableInfo(context, extraTableInfo, existTables, llmProvider);
      fail("should throw IllegalArgumentException when extraTableInfo is null");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("param userInput can not be empty", e.getMessage());
    }
  }
}
