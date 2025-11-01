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
import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.BasicActionTestCase;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.coredefine.module.action.ChatPipelineAction;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.datax.job.SSERunnable;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.HeteroList;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.qlangtech.tis.aiagent.core.AgentContext.KEY_REQUEST_ID;
import static com.qlangtech.tis.aiagent.core.AgentContext.KEY_VALIDATE_PLUGIN_ATTR_VALS;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/18
 */
public class TestRealTISPlanAndExecuteAgent extends BasicActionTestCase {

  private static boolean initialized = false;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (!initialized) {
      CenterResource.setNotFetchFromCenterRepository();
      initialized = true;
    }
  }

  public void testExecuteSuccess() throws Exception {
    request.setParameter("emethod", "chat");
    request.setParameter("action", "chat_pipeline_action");
    Pair<ActionProxy, ChatPipelineAction> proxy = getProxy("/coredefine/corenodemanage.ajax");
    Assert.assertNotNull(proxy);
    AtomicReference<AgentContext> agentContextRef = new AtomicReference<>();

    try (SSEEventWriter printWriter = new SSEEventWriter(
      new PrintWriter(new OutputStreamWriter(System.out, TisUTF8.get()))
      , (event, data) -> {
      AgentContext agentContext = Objects.requireNonNull(agentContextRef.get(), "AgentContext instance can not be null");
      final RequestKey requestId = RequestKey.create(data.getString(KEY_REQUEST_ID));
      if (event == SSERunnable.SSEEventType.AI_AGNET_SELECTION_REQUEST) {

        Assert.assertNotNull("requestId can not be null", requestId);
        // final String selectionKey = AgentContext.getSelectionKey(requestId);
        SelectionOptions selection = agentContext.getSessionData(requestId);
        Assert.assertNotNull("selection can not be null", selection);

        Assert.assertNotNull("candidatePlugins size must be 2,mysql-v5,mysql-v8"
          , selection.getCandidatePlugins().size());

        agentContext.setSessionData(requestId, new SelectionOptions(0, selection.getCandidatePlugins()));
        agentContext.notifyUserSelectionSubmitted(requestId);
      } else if (event == SSERunnable.SSEEventType.AI_AGNET_PLUGIN) {
        // requestId = RequestKey.create(data.getString(KEY_REQUEST_ID));

        JSONObject jsonObject = data.getJSONObject(KEY_VALIDATE_PLUGIN_ATTR_VALS);

        // List<AttrValMap> attrVals = AttrValMap.describableAttrValMapList(jsonObject.getJSONArray(HeteroList.KEY_ITEMS), Optional.empty());
        // Assert.assertTrue(CollectionUtils.isNotEmpty(attrVals));
        PluginPropsComplement pluginPropsComplement = agentContext.getSessionData(requestId);// new PluginPropsComplement();

        // for (AttrValMap valMap : attrVals) {
        pluginPropsComplement.setPluginValMap(pluginPropsComplement.getUnComplementValMap());
        //}

        agentContext.setSessionData(requestId, pluginPropsComplement);
        agentContext.notifyUserSelectionSubmitted(requestId);
      }

    })) {
      AgentContext agentContext = new AgentContext(String.valueOf(UUID.randomUUID()), printWriter);
      agentContextRef.set(agentContext);


      LLMProvider llmProvider = getLlmProvider();

      TISPlanAndExecuteAgent executeAgent = new TISPlanAndExecuteAgent(agentContext, llmProvider, proxy.getRight());

      String taskDesc = "创建MySQL到Doris的数据同步管道，MySQL源端：host=192.168.1.10, port=3306, user=admin, password=pass123, database=orders。Doris目标端：host=192.168.1.20, port=9030, user=root, password=doris123。";

      TaskPlan taskPlan = executeAgent.generatePlan(taskDesc);
      TaskPlan.DataEndCfg sourceEnd = taskPlan.getSourceEnd();
      TaskPlan.DataEndCfg targetEnd = taskPlan.getTargetEnd();
      Assert.assertEquals(sourceEnd.getType(), IEndTypeGetter.EndType.MySQL);
      Assert.assertEquals(targetEnd.getType(), IEndTypeGetter.EndType.Doris);
      Assert.assertTrue(StringUtils.isNotEmpty(sourceEnd.getRelevantDesc()));
      Assert.assertTrue(StringUtils.isNotEmpty(targetEnd.getRelevantDesc()));
//      ActionContext.getContext().getServletContext();
//      ServletActionContext.getActionContext(this.request).withServletContext(this.servletContext);
      executeAgent.execute(taskDesc);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }


  }

  @NotNull
  public static LLMProvider getLlmProvider() {
    PartialSettedPluginContext context = new PartialSettedPluginContext();
    return LLMProvider.load(context.setLoginUser(() -> "admin"), "default");
  }
}
