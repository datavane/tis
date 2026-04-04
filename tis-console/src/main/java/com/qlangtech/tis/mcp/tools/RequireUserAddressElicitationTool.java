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

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 Elicitation (createElicitation) 的收货地址收集工具。
 * 通过 JSON Schema 定义表单字段，客户端直接返回结构化数据，无需 LLM 解析。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/3
 */
public class RequireUserAddressElicitationTool extends McpTool {

  public RequireUserAddressElicitationTool(TISHttpMcpServer mcpServer) {
    super("require_user_address_elicitation", "需要用户提交一个收货地址（基于Elicitation）", mcpServer);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    builder.addProperty("address", TISJsonSchema.FieldType.String, "接收到的用户物流地址");
    builder.addProperty("mobile", TISJsonSchema.FieldType.String, "接收到的用户电话号码");
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpSyncServerExchange exchange, RequestArguments arguments) throws Exception {
    // 构建 JSON Schema，定义 address 和 mobile 两个必填字段
    Map<String, Object> requestedSchema = new HashMap<>();
    requestedSchema.put("type", "object");
    requestedSchema.put("required", List.of("address", "mobile"));

    Map<String, Object> properties = new HashMap<>();
    properties.put("address", Map.of("type", "string", "description", "快递收货地址"));
    properties.put("mobile", Map.of("type", "string", "description", "手机电话号码"));
    requestedSchema.put("properties", properties);

    McpSchema.ElicitRequest elicitRequest = McpSchema.ElicitRequest.builder()
      .message("请提供您的快递收货地址和手机电话号码")
      .requestedSchema(requestedSchema)
      .build();

    McpSchema.ElicitResult elicitResult = exchange.createElicitation(elicitRequest);

    if (elicitResult.action() == McpSchema.ElicitResult.Action.DECLINE) {
      return ExecuteResult.createFaild().setMessage("用户拒绝了提供收货地址信息");
    }
    if (elicitResult.action() == McpSchema.ElicitResult.Action.CANCEL) {
      return ExecuteResult.createFaild().setMessage("用户取消了收货地址信息的填写");
    }

    Map<String, Object> content = elicitResult.content();
    JSONObject result = new JSONObject();
    result.put("address", content.get("address"));
    result.put("mobile", content.get("mobile"));
    return ExecuteResult.createSuccess(result);
  }
}

