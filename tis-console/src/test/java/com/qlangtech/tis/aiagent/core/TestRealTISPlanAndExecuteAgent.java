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

import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.datax.job.SSERunnable;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.common.UserProfile;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.llm.DeepSeekProvider;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.qlangtech.tis.aiagent.core.AgentContext.KEY_REQUEST_ID;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/18
 */
public class TestRealTISPlanAndExecuteAgent {

  @BeforeClass
  public static void initBefore() {
    CenterResource.setNotFetchFromCenterRepository();
  }

  @Test
  public void testExecuteSuccess() throws Exception {


    AtomicReference<AgentContext> agentContextRef = new AtomicReference<>();

    try (SSEEventWriter printWriter = new SSEEventWriter(
      new PrintWriter(new OutputStreamWriter(System.out, TisUTF8.get()))
      , (event, data) -> {

      if (event == SSERunnable.SSEEventType.AI_AGNET_SELECTION_REQUEST) {
        String requestId = data.getString(KEY_REQUEST_ID);
        Assert.assertNotNull("requestId can not be null", requestId);
        AgentContext agentContext = Objects.requireNonNull(agentContextRef.get(), "AgentContext instance can not be null");

        final String selectionKey = AgentContext.getSelectionKey(requestId);

        SelectionOptions selection = agentContext.getSessionData(selectionKey);
        Assert.assertNotNull("selection can not be null", selection);

        Assert.assertNotNull("candidatePlugins size must be 2,mysql-v5,mysql-v8"
          , selection.getCandidatePlugins().size());

        agentContext.setSessionData(selectionKey, new SelectionOptions(0, selection.getCandidatePlugins()));
        agentContext.notifyUserSelectionSubmitted(requestId);
      }

    })) {
      AgentContext agentContext = new AgentContext(String.valueOf(UUID.randomUUID()), printWriter);
      agentContextRef.set(agentContext);

      PartialSettedPluginContext context = new PartialSettedPluginContext();
      LLMProvider llmProvider = LLMProvider.load(context.setLoginUser(() -> "admin"), "default");

      TISPlanAndExecuteAgent executeAgent = new TISPlanAndExecuteAgent(agentContext, llmProvider);

      String taskDesc = "我需要创建一个数据同步管道，从MySQL 同步到 Paimon 数据库，MySql 数据源，用户名为baisui，密码为123456，主机地址为192.168.28.200，端口为3306，数据库名称为order2\n" +
        "Paimon端的Hive配置为，db地址：192.168.28.200，db名称：default。同步管道创建完成自动触发历史数据同步，并开启增量同步，谢谢";

      TaskPlan taskPlan = executeAgent.generatePlan(taskDesc);
      TaskPlan.DataEndCfg sourceEnd = taskPlan.getSourceEnd();
      TaskPlan.DataEndCfg targetEnd = taskPlan.getTargetEnd();
      Assert.assertEquals(sourceEnd.getType(), IEndTypeGetter.EndType.MySQL);
      Assert.assertEquals(targetEnd.getType(), IEndTypeGetter.EndType.Paimon);
      Assert.assertTrue(StringUtils.isNotEmpty(sourceEnd.getRelevantDesc()));
      Assert.assertTrue(StringUtils.isNotEmpty(targetEnd.getRelevantDesc()));

      executeAgent.execute(taskDesc);
    }


  }
}
