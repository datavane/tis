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

import java.util.List;

/**
 * 基于 Sampling (createMessage) 的收货地址收集工具。
 * 通过 LLM 向用户询问收货地址和手机号码，以 JSON 结构化形式返回。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/3
 */
public class RequireUserAddressTool extends McpTool {

  public RequireUserAddressTool(TISHttpMcpServer mcpServer) {
    super("require_user_address", "需要用户提交一个收货地址（基于Sampling）", mcpServer);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    builder.addProperty("address", TISJsonSchema.FieldType.String, "接收到的用户物流地址");
    builder.addProperty("mobile", TISJsonSchema.FieldType.String, "接收到的用户电话号码");
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpSyncServerExchange exchange, RequestArguments arguments) throws Exception {
    McpSchema.CreateMessageRequest createMessageRequest = McpSchema.CreateMessageRequest.builder()
      .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
        new McpSchema.TextContent("请提供您的快递收货地址和手机电话号码"))))
      .systemPrompt("你是一个收货信息收集助手。请向用户询问并收集以下信息：1. 快递收货地址 2. 手机电话号码。"
        + "收集完成后，请严格以如下JSON格式返回，不要包含其他内容：{\"address\":\"用户的收货地址\",\"mobile\":\"用户的手机号码\"}")
      .maxTokens(500)
      .build();

    McpSchema.CreateMessageResult message = exchange.createMessage(createMessageRequest);
    McpSchema.Content content = message.content();

    JSONObject result = new JSONObject();
    result.put("address", getAddress(content));
    result.put("mobile", getMobile(content));
    return ExecuteResult.createSuccess(result);
  }

  private String getAddress(McpSchema.Content content) {
    JSONObject json = parseContentAsJson(content);
    return json != null ? json.getString("address") : null;
  }

  private String getMobile(McpSchema.Content content) {
    JSONObject json = parseContentAsJson(content);
    return json != null ? json.getString("mobile") : null;
  }

  private JSONObject parseContentAsJson(McpSchema.Content content) {
    if (content instanceof McpSchema.TextContent textContent) {
      return JSONObject.parseObject(textContent.text());
    }
    return null;
  }
}
