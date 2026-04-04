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

package com.qlangtech.tis.mcp.tools.pipeline;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.execute.impl.BasicStepExecutor;
import com.qlangtech.tis.aiagent.execute.impl.IPrimaryValRewrite;
import com.qlangtech.tis.aiagent.execute.impl.PluginInstanceCreateExecutor;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.manage.common.AppAndRuntime;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.mcp.MCPTaskPlan;
import com.qlangtech.tis.mcp.McpSession;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.PluginStepExecutor;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescriptorsJSONForAIPrompt;
import com.qlangtech.tis.util.DescriptorsMeta;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.qlangtech.tis.mcp.TISHttpMcpServer.parseAttrValMap;
import static com.qlangtech.tis.plugin.IdentityName.createNewPrimaryFieldValue;

/**
 * 创建TIS 端到端的数据管道
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/29
 * @see PluginInstanceCreateExecutor
 */
public class CreatePipelinePrepareTool extends McpTool {

  private static final String KEY_DEFAULT_DATAX_PROCESS_IMPL = "com.qlangtech.tis.plugin.datax.DefaultDataxProcessor";
  private static final String KEY_SOURCE_END = "source_end";
  private static final String KEY_TARGET_END = "target_end";
  /**
   * Session cache key for the pipeline process instance
   */
  public static final McpSession.SessionKey<Describable> KEY_SESSION_PIPELINE_PROCESS = new McpSession.SessionKey<>( "pipeline_process");
  private final TISJsonSchema dataXProcessorSchema;
  private static final String END_TYPES_DESC;
  private final DescribableImpl pluginImpl;

  static {
    AtomicInteger index = new AtomicInteger();
    END_TYPES_DESC =
      IEndTypeGetter.EndType.getDataEnds().stream()
        .map((end) -> index.incrementAndGet() + ". " + end.getVal() + ":" + end.getDesc().orElseThrow())
        .collect(Collectors.joining("\n"));
  }

  public CreatePipelinePrepareTool(TISHttpMcpServer mcpServer) {
    super("create_pipeline_prepare",
      "准备创建端到端的数据同步通道，例如：MySQL同步到Doris的批量数据同步通道。`"
        + KEY_SOURCE_END + "`与`" + KEY_TARGET_END + "`参数说明：\n" + END_TYPES_DESC
      , mcpServer);
    this.pluginImpl = new DescribableImpl(DataxProcessor.class, Optional.empty());
    pluginImpl.addImpl(KEY_DEFAULT_DATAX_PROCESS_IMPL);
    Pair<DescriptorsMeta, DescriptorsJSONForAIPrompt> desc = DescriptorsJSONForAIPrompt.desc(pluginImpl);
    for (Map.Entry<String, TISJsonSchema> entry
      : ((DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta) desc.getKey()).descSchemaRegister.entrySet()) {
      dataXProcessorSchema = entry.getValue();
      return;
    }

    throw new IllegalStateException("dataXProcessorSchema has not been initialize");
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    Object[] dataEnds =
      IEndTypeGetter.EndType.getDataEnds().stream().map(IEndTypeGetter.EndType::getVal).toArray(String[]::new);
    builder.addProperty(KEY_SOURCE_END, TISJsonSchema.FieldType.String, "源端数据端类型")
      .setValEnums(dataEnds);
    builder.addProperty(KEY_TARGET_END, TISJsonSchema.FieldType.String, "目标端数据端类型")
      .setValEnums(dataEnds);

    builder.addObjectProperty(IncrRateControllerCfgDTO.KEY_PIPELINE, dataXProcessorSchema);

    return builder.build();
  }

  @Override
  protected TISJsonSchema getOutputStream() {
    return tisBizOutputSchema;
  }

  @Override
  public ExecuteResult execHandle(McpSyncServerExchange exchange, RequestArguments arguments) throws Exception {
    //exchange.createMessage()

    MCPTaskPlan plan = new MCPTaskPlan(this.mcpServer);
    Context context = plan.getRuntimeContext(true);
    AgentContext agentContext = createAgentContext(exchange);

    PluginStepExecutor pluginStepExecutor =
      new PluginStepExecutor(parseAttrValMap(new RequestArguments(arguments.get(IncrRateControllerCfgDTO.KEY_PIPELINE))));
    IEndTypeGetter.EndType sourceEnd = IEndTypeGetter.EndType.parse(arguments.get(KEY_SOURCE_END));
    IEndTypeGetter.EndType targetEnd = IEndTypeGetter.EndType.parse(arguments.get(KEY_TARGET_END));


    /**
     * support for DefaultDataxProcessor$DescriptorImpl.getManipulateStore()
     */
    AppAndRuntime.setAppAndRuntime(new AppAndRuntime(Collections.emptyMap()));


    /**
     * dataXProcessor vals
     */
    AttrValMap pluginVals = pluginStepExecutor.createPluginInstance(plan, agentContext
      , new UserPrompt("正在生成管道主体配置...", StringUtils.EMPTY) //
      , Optional.empty() //
      , pluginImpl, HeteroEnum.APP_SOURCE, new IPrimaryValRewrite() {
        IFieldErrorHandler.BasicPipelineValidator pipelineRules =
          ((IControlMsgHandler) plan.getControlMsgHandler()).getPipelineValidator(IFieldErrorHandler.BizLogic.VALIDATE_APP_NAME_DUPLICATE);

        @Override
        public IdentityName newCreate(PropertyType pp) {
          if (!pp.isIdentity()) {
            throw new IllegalStateException("property " + pp.propertyName() + " must identity field");
          }
          final String prefix = sourceEnd + "_to_";
          return (createNewPrimaryFieldValue(prefix + targetEnd,
            pipelineRules.getExistEntities(Optional.of(prefix))));
        }

        @Override
        public boolean isDuplicateInExistEntities(PropertyType pk, String identityFieldVal) {
          // 查找是否存在已经有的
          List<IdentityName> exist = pipelineRules.getExistEntities(Optional.of(identityFieldVal));
          return exist.stream().anyMatch((id) -> StringUtils.equals(id.identityValue(), identityFieldVal));
        }
      });
    IdentityName primaryFieldVal = IdentityName.create(pluginVals.getPrimaryFieldVal());

    agentContext.sendMessage("创建名称为：'" + primaryFieldVal.identityValue() + "'的数据通道");

    PartialSettedPluginContext pluginCtx = BasicStepExecutor.createPluginContext(plan,
      DataXName.createDataXPipeline(primaryFieldVal.identityValue()));
    UploadPluginMeta processMeta = UploadPluginMeta.appnameMeta(pluginCtx, primaryFieldVal.identityValue());

    Describable process = pluginStepExecutor.createPluginAndStore(HeteroEnum.APP_SOURCE, plan, agentContext, context,
      pluginCtx, processMeta,
      pluginVals);

    AjaxValve.ActionExecResult successResult = AjaxValve.ActionExecResult.create(context);
    if (successResult.isSuccess()) {
      McpSession mcpSession = mcpServer.getSession(exchange);
      // Save the process instance to the MCP session cache, so subsequent tools can retrieve it
      mcpSession.put(KEY_SESSION_PIPELINE_PROCESS, process);
    }
    return ExecuteResult.createSuccess(successResult);
  }
}
