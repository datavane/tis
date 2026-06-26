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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.ontology.OntologyDomain;
import com.qlangtech.tis.plugin.ontology.OntologyDomainManipulate;
import com.qlangtech.tis.plugin.ontology.chatbi.ChatBIConstants;
import com.qlangtech.tis.plugin.ontology.chatbi.ChatBIResult;
import com.qlangtech.tis.plugin.ontology.chatbi.ChatBIService;
import com.qlangtech.tis.plugin.ontology.chatbi.QueryResult;
import com.qlangtech.tis.plugin.ontology.chatbi.TraceStep;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ChatBI MCP Tool：通过自然语言问数功能。
 * <p>
 * 说明：使用 MCP 的 progressNotification 实时推送执行步骤（TraceStep），
 * 最终结果中也包含完整的 trace 数组供客户端回溯和调试。
 * <pre>
 *   使用示例
 *
 *   MCP 客户端调用示例：
 *   {
 *     "tool": "chat_bi",
 *     "arguments": {
 *       "nlq": "最近一个月销售额最高的前10个产品是什么？"
 *     }
 *   }
 *
 *   执行过程中会实时收到 progressNotification：
 *   {
 *     "method": "notifications/progress",
 *     "params": {
 *       "progressToken": "chatbi-1719388800000",
 *       "progress": 120,
 *       "message": "GraphRAG retrieval completed",
 *       "_meta": {
 *         "traceStep": {
 *           "step": "retrieve",
 *           "ok": true,
 *           "message": "GraphRAG retrieval completed",
 *           "millis": 120,
 *           "data": {...}
 *         }
 *       }
 *     }
 *   }
 *
 *   最终返回结果示例：
 *   {
 *     "success": true,
 *     "bizresult": {
 *       "sql": "SELECT product_name, SUM(sales_amount) as total_sales FROM orders WHERE ...",
 *       "data": {
 *         "columns": ["product_name", "total_sales"],
 *         "rows": [{"product_name": "Product A", "total_sales": 10000}, ...],
 *         "rowCount": 10,
 *         "truncated": false,
 *         "actualRows": 10
 *       },
 *       "trace": [
 *         {"step": "retrieve", "ok": true, "message": "GraphRAG retrieval completed", "millis": 120, "data": {...}},
 *         {"step": "llm", "ok": true, "message": "LLM invoked", "millis": 3500, "data": {...}},
 *         ...
 *       ],
 *       "reqId": "20260625183000-abc123def456"
 *     }
 *   }
 * </pre>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/6/25
 */
public class ChatBITool extends McpTool {
  private static final Logger logger = LoggerFactory.getLogger(ChatBITool.class);

  //private static final String KEY_ONTOLOGY_DOMAIN = "domain";
  public static final String KEY_NLQ = "nlq";

  public ChatBITool(TISHttpMcpServer mcpServer) {
    super("chat_bi",
      "提供通过自然语言问数功能，根据用户的自然语言问句生成并执行 SQL 查询。" +
        "\n\n执行过程中会通过 progressNotification 实时推送执行步骤（TraceStep），" +
        "客户端可通过 progressToken 关联同一次查询的所有通知。" +
        "通知的 meta.traceStep 结构与最终返回的 trace 数组元素相同。",
      mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    //    builder.addProperty(KEY_ONTOLOGY_DOMAIN, TISJsonSchema.FieldType.String,
    //      "本体域名称，如为空，如用户没有明确说明不要设置该参数", false);
    builder.addProperty(KEY_NLQ, TISJsonSchema.FieldType.String,
      "自然语言问句，例如：'最近一个月销售额最高的前10个产品是什么？'", true);
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream() {
    // 定义 QueryResult 的 Schema
    TISJsonSchema.Builder queryResultBuilder = TISJsonSchema.Builder.create("QueryResult", java.util.Optional.empty());
    queryResultBuilder.addProperty(ChatBIConstants.FIELD_COLUMNS, TISJsonSchema.FieldType.Array, "列名列表", false)
      .setItems(TISJsonSchema.FieldType.String);
    queryResultBuilder.addProperty(ChatBIConstants.FIELD_ROWS, TISJsonSchema.FieldType.Array, "查询结果行数据", false)
      .setItems(TISJsonSchema.FieldType.Object);
    queryResultBuilder.addProperty(ChatBIConstants.FIELD_ROW_COUNT, TISJsonSchema.FieldType.Integer, "返回的行数", false);
    queryResultBuilder.addProperty(ChatBIConstants.FIELD_TRUNCATED, TISJsonSchema.FieldType.Boolean,
      "结果是否被截断（超过最大行数限制）", false);
    queryResultBuilder.addProperty(ChatBIConstants.FIELD_ACTUAL_ROWS, TISJsonSchema.FieldType.Integer, "实际查询到的总行数",
      false);

    // 定义 TraceStep 的 Schema
    TISJsonSchema.Builder traceStepBuilder = TISJsonSchema.Builder.create("TraceStep", java.util.Optional.empty());
    traceStepBuilder.addProperty(ChatBIConstants.FIELD_STEP, TISJsonSchema.FieldType.String,
      "执行步骤名称：retrieve(检索)/prompt(提示词)/llm(大模型调用)/extract(提取SQL)/validate(验证)/execute(执行)", false);
    traceStepBuilder.addProperty(ChatBIConstants.FIELD_OK, TISJsonSchema.FieldType.Boolean, "该步骤是否成功", false);
    traceStepBuilder.addProperty(ChatBIConstants.FIELD_MESSAGE, TISJsonSchema.FieldType.String, "步骤描述信息", false);
    traceStepBuilder.addProperty(ChatBIConstants.FIELD_MILLIS, TISJsonSchema.FieldType.Integer, "该步骤耗时（毫秒）", false);
    traceStepBuilder.addProperty(ChatBIConstants.FIELD_DATA, TISJsonSchema.FieldType.Object,
      "步骤相关数据，包含 model/tokens/sql/issues 等字段，具体内容取决于步骤类型", false);

    // 定义 bizresult 的详细结构
    TISJsonSchema.Builder bizResultBuilder = TISJsonSchema.Builder.create("ChatBIResult", java.util.Optional.empty());
    bizResultBuilder.addProperty(ChatBIConstants.FIELD_SQL, TISJsonSchema.FieldType.String, "生成的 SQL 语句", false);
    bizResultBuilder.addProperty(ChatBIConstants.FIELD_DATA, TISJsonSchema.FieldType.Object, "SQL 查询结果数据", false)
      .setItems(queryResultBuilder.build().schema());
    bizResultBuilder.addProperty(ChatBIConstants.FIELD_TRACE, TISJsonSchema.FieldType.Array,
        "完整的执行步骤轨迹，包含每个阶段的详细信息和耗时。" +
          "\n\n实时通知格式：执行过程中会通过 progressNotification 推送每个 TraceStep，" +
          "通知结构为 {progressToken, progress, message, meta: {traceStep: <TraceStep对象>}}。" +
          "其中 meta.traceStep 的结构与本数组元素完全相同（包含 step/ok/message/millis/data 字段）。" +
          "\n\nprogress 字段表示累计耗时（毫秒），progressToken 格式为 'chatbi-{timestamp}'。", false)
      .setItems(traceStepBuilder.build().schema());
    bizResultBuilder.addProperty(ChatBIConstants.FIELD_ERROR, TISJsonSchema.FieldType.String, "错误信息（仅在执行失败时存在）", false);
    bizResultBuilder.addProperty(ChatBIConstants.FIELD_REQ_ID, TISJsonSchema.FieldType.String,
      "请求唯一标识符，格式为 yyyyMMddHHmmss-{uuid32}，可用于追踪和调试", false);

    //    // 将详细的 bizresult schema 添加到基础 schema 中
    //    builder.addProperty(IAjaxResult.KEY_BIZRESULT, TISJsonSchema.FieldType.Object,
    //        "ChatBI 业务结果，包含生成的 SQL、查询结果数据、执行轨迹等完整信息", false)
    //      .setItems(bizResultBuilder.build().schema());
    return createOutputSchema("ChatBIResult", (bizProperty) -> {
      bizProperty.addDescription("ChatBI 业务结果，包含生成的 SQL、查询结果数据、执行轨迹等完整信息");
      bizProperty.setItems(bizResultBuilder.build().schema());
    });
  }


  @Override
  public ExecuteResult execHandle(McpAgentContext mcpAgentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    // 1. 获取参数
    // String domain = arguments.get(KEY_ONTOLOGY_DOMAIN);
    String nlq = arguments.get(KEY_NLQ);

    if (StringUtils.isEmpty(nlq)) {
      mcpAgentContext.addActionError("自然语言问句（nlq）不能为空");
      return ExecuteResult.createSuccess(mcpAgentContext.getActionExecResult());
    }

    // 2. 确定目标域（如果为空，查找默认域）
    String targetDomain = determineDomain();

    // 3. 获取 ChatBIService 实例
    ChatBIService chatBIService = getChatBIService(targetDomain);

    // 4. 生成 progressToken（用于客户端关联多个 progress notification）
    String progressToken = "chatbi-" + System.currentTimeMillis();

    // 5. 收集 TraceStep 用于最终返回（同时也实时推送）
    List<TraceStep> steps = Collections.synchronizedList(new ArrayList<>());
    long cumulativeMillis = 0; // 累计耗时

    // 6. 调用 ask 方法，使用回调实时推送 TraceStep
    ChatBIResult result = chatBIService.ask(targetDomain, nlq, true, (TraceStep step) -> {
      // 收集到列表中
      steps.add(step);

      // 实时推送给 MCP 客户端（通过 progressNotification）
      try {
        // 构造 TraceStep JSON
        JSONObject stepJson = new JSONObject();
        stepJson.put(ChatBIConstants.FIELD_STEP, step.step());
        stepJson.put(ChatBIConstants.FIELD_OK, step.ok());
        stepJson.put(ChatBIConstants.FIELD_MESSAGE, step.message());
        stepJson.put(ChatBIConstants.FIELD_MILLIS, step.millis());
        if (step.data() != null) {
          stepJson.put(ChatBIConstants.FIELD_DATA, step.data());
        }

        // 将 TraceStep 数据放在 meta 中
        Map<String, Object> meta = new HashMap<>();
        meta.put("traceStep", stepJson);

        // 计算累计进度（使用 synchronized 块确保线程安全）
        long currentProgress;
        synchronized (steps) {
          currentProgress = steps.stream().mapToLong(TraceStep::millis).sum();
        }

        // 创建并发送 progress notification
        io.modelcontextprotocol.spec.McpSchema.ProgressNotification notification =
          new io.modelcontextprotocol.spec.McpSchema.ProgressNotification(
            progressToken,              // progressToken - 用于关联同一次查询的所有通知
            (double) currentProgress,   // progress - 累计耗时（毫秒）
            null,                       // total - 总耗时未知
            step.message(),             // message - 步骤描述
            meta                        // meta - 包含完整 TraceStep 数据
          );

        exchange.progressNotification(notification);

        logger.debug("Sent progress notification for step: {} ({}ms)", step.step(), step.millis());
      } catch (Exception e) {
        // 推送失败不应影响主流程
        logger.warn("Failed to send progress notification for step: " + step.step(), e);
      }
    });

    // 7. 构造返回结果
    if (result.isSuccess()) {
      // 成功
      JSONObject bizResult = new JSONObject();
      bizResult.put(ChatBIConstants.FIELD_SQL, result.sql());
      bizResult.put(ChatBIConstants.FIELD_DATA, convertQueryResult(result.data()));
      bizResult.put(ChatBIConstants.FIELD_TRACE, convertTraceSteps(steps));
      bizResult.put(ChatBIConstants.FIELD_REQ_ID, result.reqId());

      mcpAgentContext.getTaskPlan().getControlMsgHandler()
        .setBizResult(mcpAgentContext.getRuntimeContext(), bizResult);
      mcpAgentContext.addActionMessage("成功生成并执行 SQL：" + result.sql());
    } else {
      // 失败
      JSONObject bizResult = new JSONObject();
      bizResult.put(ChatBIConstants.FIELD_ERROR, result.error());
      bizResult.put(ChatBIConstants.FIELD_TRACE, convertTraceSteps(steps));
      bizResult.put(ChatBIConstants.FIELD_REQ_ID, result.reqId());
      if (result.sql() != null) {
        bizResult.put(ChatBIConstants.FIELD_SQL, result.sql());
      }

      mcpAgentContext.getTaskPlan().getControlMsgHandler()
        .setBizResult(mcpAgentContext.getRuntimeContext(), bizResult);
      mcpAgentContext.addActionError("ChatBI 执行失败：" + result.error());
    }

    return ExecuteResult.createSuccess(mcpAgentContext.getActionExecResult());
  }

  /**
   * 确定目标本体域名称。
   * 如果 domain 为空，则查找默认域；否则使用指定的域。
   */
  private String determineDomain() {
    // if (StringUtils.isEmpty(domain)) {
    List<Pair<OntologyDomain, IPluginStore<OntologyDomain>>> domainList = OntologyDomain.getDoaminList();
    final int domainSize = domainList.size();
    for (Pair<OntologyDomain, IPluginStore<OntologyDomain>> p : domainList) {
      if (domainSize == 1 || p.getKey().defaultDomain) {
        return p.getKey().name;
      }
    }
    throw new IllegalStateException("can not find default ontology domain in: " //
      + domainList.stream().map((p) -> p.getKey().name).collect(Collectors.joining(",")));

    // return domain;
  }

  /**
   * 获取指定域的 ChatBIService 实例。
   */
  private ChatBIService getChatBIService(String targetDomain) {
    if (StringUtils.isEmpty(targetDomain)) {
      throw new IllegalStateException("target domain can not be null");
    }

    Collection<OntologyDomainManipulate> manipulates = OntologyDomain.getDomainManiplidateList(targetDomain);
    for (OntologyDomainManipulate m : manipulates) {
      if (m instanceof ChatBIService service) {
        return service;
      }
    }

    throw new IllegalStateException("domain '" + targetDomain + "' does not have ChatBI enabled. "
      + "Please enable ChatBI for this domain first.");
  }

  /**
   * 将 QueryResult 转换为 JSONObject。
   */
  private JSONObject convertQueryResult(QueryResult queryResult) {
    if (queryResult == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put(ChatBIConstants.FIELD_COLUMNS, queryResult.columns());
    json.put(ChatBIConstants.FIELD_ROWS, queryResult.rows());
    json.put(ChatBIConstants.FIELD_ROW_COUNT, queryResult.rowCount());
    json.put(ChatBIConstants.FIELD_TRUNCATED, queryResult.truncated());
    json.put(ChatBIConstants.FIELD_ACTUAL_ROWS, queryResult.actualRows());
    return json;
  }

  /**
   * 将 TraceStep 列表转换为 JSONArray。
   */
  private JSONArray convertTraceSteps(List<TraceStep> steps) {
    JSONArray array = new JSONArray();
    for (TraceStep step : steps) {
      JSONObject json = new JSONObject();
      json.put(ChatBIConstants.FIELD_STEP, step.step());
      json.put(ChatBIConstants.FIELD_OK, step.ok());
      json.put(ChatBIConstants.FIELD_MESSAGE, step.message());
      json.put(ChatBIConstants.FIELD_MILLIS, step.millis());
      if (step.data() != null) {
        json.put(ChatBIConstants.FIELD_DATA, step.data());
      }
      array.add(json);
    }
    return array;
  }
}
