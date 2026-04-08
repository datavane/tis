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
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.mcp.McpAgentContext;
import com.qlangtech.tis.mcp.McpTool;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.web.start.TisAppLaunch;
import io.modelcontextprotocol.server.McpSyncServerExchange;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Optional;

import static com.qlangtech.tis.config.module.action.CollectionAction.KEY_QUERY_LIMIT;

/**
 * 获取指定任务的执行日志
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/7
 */
public class GetTaskLogTool extends McpTool {

  private static final String KEY_LOGGER_LEVEL = "logLevel";
  private static final String KEY_LINES = "lines";

  public GetTaskLogTool(TISHttpMcpServer mcpServer) {
    super("get_task_log", "获取指定任务的执行日志，支持按日志级别过滤。排查同步失败原因时使用", mcpServer);
  }

  @Override
  protected TISJsonSchema getInputSchema(TISJsonSchema.Builder builder) {
    builder.addProperty(JobParams.KEY_TASK_ID, TISJsonSchema.FieldType.Integer, "任务ID，可通过 get_pipeline_exec_history "
      + "获取");
    builder.addProperty(KEY_LOGGER_LEVEL, TISJsonSchema.FieldType.String,
      "日志级别过滤：ALL/ERROR/WARN，默认ERROR", false).setValEnums("ALL", "ERROR", "WARN");
    builder.addProperty(KEY_QUERY_LIMIT, TISJsonSchema.FieldType.Integer,
      "返回最后几行日志，默认50", false);
    return super.getInputSchema(builder);
  }

  @Override
  protected TISJsonSchema getOutputStream(TISJsonSchema.Builder builder) {
    builder.addProperty(JobParams.KEY_TASK_ID, TISJsonSchema.FieldType.Integer, "任务ID");
    builder.addProperty(KEY_LOGGER_LEVEL, TISJsonSchema.FieldType.String, "日志级别");
    builder.addProperty(KEY_LINES, TISJsonSchema.FieldType.Array, "日志行列表")
      .setItems(TISJsonSchema.FieldType.String);
    return super.getOutputStream(builder);
  }

  @Override
  public ExecuteResult execHandle(McpAgentContext agentContext, McpSyncServerExchange exchange,
                                  RequestArguments arguments) throws Exception {
    Integer taskId = arguments.get(JobParams.KEY_TASK_ID);
    if (taskId == null) {
      throw new IllegalStateException("taskId can not be null");
    }

    String level = arguments.get(KEY_LOGGER_LEVEL);
    if (level == null || level.isEmpty()) {
      level = "ERROR";
    }
    level = level.toUpperCase();

    Integer limit = arguments.get(KEY_QUERY_LIMIT);
    if (limit == null || limit <= 0) {
      limit = 50;
    }

    String taskLogFileName = "full-" + taskId + ".log";
    File logFile = new File(TisAppLaunch.getAssebleTaskDir(), taskLogFileName);

    if (!logFile.exists()) {
      return ExecuteResult.createFaild().setMessage("日志文件不存在: " + logFile.getAbsolutePath());
    }

    // Read and filter log lines, keep last N lines
    LinkedList<String> tailLines = new LinkedList<>();
    boolean filterAll = "ALL".equals(level);

    try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (filterAll || line.contains(level)) {
          tailLines.add(line);
          if (tailLines.size() > limit) {
            tailLines.removeFirst();
          }
        }
      }
    }

    JSONObject result = new JSONObject(true);
    result.put(JobParams.KEY_TASK_ID, taskId);
    result.put(KEY_LOGGER_LEVEL, level);
    JSONArray linesArr = new JSONArray();
    linesArr.addAll(tailLines);
    result.put(KEY_LINES, linesArr);
    return ExecuteResult.createSuccess(result);
  }
}
