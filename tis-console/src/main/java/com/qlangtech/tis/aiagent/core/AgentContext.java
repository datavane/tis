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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.datax.job.SSERunnable;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent执行上下文，管理会话状态和SSE通信
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class AgentContext {
  private final String sessionId;
  private final SSERunnable.SSEEventWriter sseWriter;
  private final Map<String, Object> sessionData;
  private final AtomicLong tokenCount;
  private volatile boolean cancelled = false;

  public AgentContext(String sessionId, SSERunnable.SSEEventWriter sseWriter) {
    this.sessionId = sessionId;
    this.sseWriter = sseWriter;
    this.sessionData = new HashMap<>();
    this.tokenCount = new AtomicLong(0);
  }

  /**
   * 发送文本消息到客户端
   */
  public void sendMessage(String message) {
    if (!cancelled && sseWriter != null) {
      JSONObject data = new JSONObject();
      data.put("type", "text");
      data.put("content", message);
      sendSSEEvent(SSERunnable.SSEEventType.AI_AGNET_MESSAGE, data.toJSONString());
    }
  }

  /**
   * 发送JSON数据到客户端（用于插件配置）
   */
  public void sendPluginConfig(String pluginImpl, JSONObject config) {
    if (!cancelled && sseWriter != null) {
      JSONObject data = new JSONObject();
      data.put("type", "plugin");
      data.put("impl", pluginImpl);
      data.put("config", config);
      sendSSEEvent(SSERunnable.SSEEventType.AI_AGNET_PLUGIN, data.toJSONString());
    }
  }

  /**
   * 发送进度更新
   */
  public void sendProgress(String taskName, int current, int total) {
    if (!cancelled && sseWriter != null) {
      JSONObject data = new JSONObject();
      data.put("type", "progress");
      data.put("task", taskName);
      data.put("current", current);
      data.put("total", total);
      sendSSEEvent(SSERunnable.SSEEventType.AI_AGNET_PROGRESS, data.toJSONString());
    }
  }

  /**
   * 发送Token使用情况
   */
  public void updateTokenUsage(long tokens) {
    tokenCount.addAndGet(tokens);
    if (!cancelled && sseWriter != null) {
      JSONObject data = new JSONObject();
      data.put("type", "token");
      data.put("count", tokenCount.get());
      sendSSEEvent(SSERunnable.SSEEventType.AI_AGNET_TOKEN, data.toJSONString());
    }
  }

  /**
   * 发送错误信息
   */
  public void sendError(String error) {
    if (!cancelled && sseWriter != null) {
      JSONObject data = new JSONObject();
      data.put("type", "error");
      data.put("message", error);
      sendSSEEvent(SSERunnable.SSEEventType.AI_AGNET_ERROR, data.toJSONString());
    }
  }

  /**
   * 请求用户输入
   */
  public void requestUserInput(String prompt, String field) {
    if (!cancelled && sseWriter != null) {
      JSONObject data = new JSONObject();
      data.put("type", "input_request");
      data.put("prompt", prompt);
      data.put("field", field);
      sendSSEEvent(SSERunnable.SSEEventType.AI_AGENT_INPUT_REQUEST, data.toJSONString());
    }
  }

  /**
   * 请求用户从候选项中选择
   *
   * @param requestId 请求标识符，用于后续匹配用户的选择结果
   * @param prompt    提示信息
   * @param options   候选项列表
   */
  public void requestUserSelection(String requestId, String prompt, JSONObject options) {
    if (!cancelled && sseWriter != null) {
      JSONObject data = new JSONObject();
      data.put("type", "selection_request");
      data.put("requestId", requestId);
      data.put("prompt", prompt);
      data.put("options", options);
      sendSSEEvent(SSERunnable.SSEEventType.AI_AGNET_SELECTION_REQUEST, data.toJSONString());
    }
  }

  /**
   * 等待用户选择结果
   *
   * @param requestId 请求标识符
   * @return 用户选择的索引，如果超时或取消返回-1
   */
  public int waitForUserSelection(String requestId) {
    String selectionKey = "user_selection_" + requestId;
    int maxWaitSeconds = 300;
    int waitCount = 0;

    while (!cancelled && waitCount < maxWaitSeconds * 10) {
      Object selection = getSessionData(selectionKey);
      if (selection != null) {
        sessionData.remove(selectionKey);
        return (Integer) selection;
      }

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return -1;
      }
      waitCount++;
    }

    return -1;
  }

  private void sendSSEEvent(SSERunnable.SSEEventType event, String data) {
//    synchronized (sseWriter) {
//      sseWriter.write("event: " + event.getEventType() + "\n");
//      sseWriter.write("data: " + data + "\n\n");
//      sseWriter.flush();
//    }
    this.sseWriter.writeSSEEvent(event, data);
  }

  public void cancel() {
    this.cancelled = true;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionData(String key, Object value) {
    sessionData.put(key, value);
  }

  public Object getSessionData(String key) {
    return sessionData.get(key);
  }

  public long getTokenCount() {
    return tokenCount.get();
  }
}
