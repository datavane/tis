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

package com.qlangtech.tis.mcp.tools;

import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.execute.impl.BasicStepExecutor;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.coredefine.module.action.PluginWillInstall;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.model.UpdateCenter;
import com.qlangtech.tis.extension.model.UpdateSite;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.util.DescriptorsJSONForAIPrompt;
import com.qlangtech.tis.util.DescriptorsMeta;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_IMPL;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/27
 * @see ListPluginTypesTool
 */
public class GetPluginSchemaTool extends McpTool {
  public static final String KEY_OUT_PLUGIN_SCHEMA = "pluginSchema";
  public static final String TOOL_NAME_GET_SCHEMA = "get_plugin_schema";

  public GetPluginSchemaTool(TISHttpMcpServer mcpServer) {
    super(TOOL_NAME_GET_SCHEMA,
      //      "获取插件定义的schema，调用端获得schema之后，可以调用`" //
      //        + CreatePluginInstanceTool.TOOL_NAME_CREATE_PLUGIN_INSTANCE + "`创建TIS插件实例"
      "获取插件的JSON Schema定义（表单模板）。返回的schema描述了插件需要哪些字段、字段类型、是否必填等信息。注意：返回的是schema定义，不是实例数据。你需要根据schema和用户输入生成实例数据"
      ,
      mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    //    TISJsonSchema.Builder builder //
    //      = TISJsonSchema.Builder.create(this.toolName + "_input_schema", Optional.empty());
    builder.addProperty(PLUGIN_EXTENSION_IMPL, TISJsonSchema.FieldType.String, "TIS插件的具体实现类");
    //    builder.addProperty(DescriptorsJSON.KEY_EXTEND_POINT, TISJsonSchema.FieldType.String,
    //      "插件扩展点，使用tool " + ListPluginTypesTool.TOOL_NAME + " 输出key:" + KEY_AVAILABLE_PLUGINS + "." +
    //      DescriptorsJSON.KEY_EXTEND_POINT + "对应的值");
    return builder.build();
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    //    TISJsonSchema.Builder builder
    //      = TISJsonSchema.Builder.create(this.toolName + "_output_schema", Optional.empty());
    builder.addObjectProperty(KEY_OUT_PLUGIN_SCHEMA, TISJsonSchema.Builder::enableAdditionalProps);

    return builder.build();
  }

  @SuppressWarnings("all")
  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {

    String impl = arguments.get(PLUGIN_EXTENSION_IMPL);

    //    Class<? extends Describable> extendPoint =
    //      (Class<? extends Describable>) Class.forName(arguments.get(DescriptorsJSON.KEY_EXTEND_POINT));
    //    DescribableImpl pluginImpl = new DescribableImpl(extendPoint, Optional.empty());
    //    pluginImpl.addImpl(impl);

    Descriptor descriptor = TIS.get().getDescriptor(impl);

    if (descriptor == null) {
      // 插件还没有安装，需要安装
      UpdateCenter updateCenter = TIS.get().getUpdateCenter();

      Set<PluginWillInstall> pluginWillInstalls
        = parsePluginWillInstalls(impl, updateCenter.getAvailablePlugins());
      if (pluginWillInstalls.isEmpty()) {
        throw new IllegalStateException(impl + " relevant pluginWillInstalls can not be empty");
      }
      //  AgentContext agentContext = createAgentContext(exchange);
      // exchange.progressNotification();
      BasicStepExecutor.installPlugin(agentContext, pluginWillInstalls, updateCenter, exchange);
      descriptor = TIS.get().getDescriptor(impl);
      if (descriptor == null) {
        throw new IllegalStateException(impl + " relevant tpi plugin have not been install successful");
      }
    }
    DescribableImpl pluginImpl = new DescribableImpl(descriptor.clazz, Optional.empty());
    pluginImpl.addImpl(impl);
    Pair<DescriptorsMeta, DescriptorsJSONForAIPrompt> desc = DescriptorsJSONForAIPrompt.desc(pluginImpl);
    for (Map.Entry<String, Pair<TISJsonSchema, Descriptor>> entry
      : ((DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta) desc.getKey()).descSchemaRegister.entrySet()) {
      TISJsonSchema pluginImplSchema = entry.getValue().getKey();
      return ExecuteResult.createSuccess(Map.of(KEY_OUT_PLUGIN_SCHEMA,pluginImplSchema.schema()));
    }
    throw new IllegalStateException("can not find jsonSchema for:" + String.join(",", pluginImpl.getImpls()));


  }

  private Set<PluginWillInstall> parsePluginWillInstalls(String implClazz,
                                                         List<UpdateSite.Plugin> availables) {
    Set<PluginWillInstall> pluginsInstall = Sets.newHashSet();
    for (UpdateSite.Plugin plugin : availables) {
      if (MapUtils.isNotEmpty(plugin.extendPoints)) {
        for (Map.Entry<String, List<String>> entry : plugin.extendPoints.entrySet()) {
          if (entry.getValue().contains(implClazz) && plugin.getInstalled() == null) {
            // 扩展实现的插件还没有安装，需要进行安装
            pluginsInstall.add(new PluginWillInstall(plugin.getDisplayName()));
          }
        }
      }
    }

    return pluginsInstall;
  }

}
