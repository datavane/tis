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
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.datax.job.SSERunnable;
import com.qlangtech.tis.extension.util.PluginExtraProps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent执行上下文，管理会话状态和SSE通信
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class AgentContext {

  public static final String KEY_REQUEST_ID = "requestId";

  public static String getSelectionKey(String requestId) {
    return "user_selection_" + requestId;
  }

  private final String sessionId;
  private final SSEEventWriter sseWriter;
  private final Map<String, ISessionData> sessionData;
  private final AtomicLong tokenCount;
  private volatile boolean cancelled = false;

  // 用于等待用户选择的同步对象集合，每个requestId对应一个锁对象
  private final Map<String, Object> selectionLocks = new ConcurrentHashMap<>();

  public AgentContext(String sessionId, SSEEventWriter sseWriter) {
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
  public void requestUserSelection(String requestId, String prompt, JSONObject options, List<PluginExtraProps.CandidatePlugin> candidatePlugins) {
    if (!cancelled && sseWriter != null) {
      JSONObject data = new JSONObject();
      data.put("type", "selection_request");
      data.put(KEY_REQUEST_ID, requestId);
      data.put("prompt", prompt);
      data.put("options", options);

      /**
       * 向缓存中写入初始数据
       */
      this.setSessionData(getSelectionKey(requestId), SelectionOptions.createUnSelectedOptions(candidatePlugins));
      sendSSEEvent(SSERunnable.SSEEventType.AI_AGNET_SELECTION_REQUEST, data);
    }
  }

  /**
   * 通知用户选择已提交
   * 当用户在前端提交选择时，调用此方法唤醒等待的线程
   *
   * @param requestId 请求标识符
   * @see AgentContext#waitForUserSelection
   */
  public void notifyUserSelectionSubmitted(String requestId) {
    Object lock = selectionLocks.get(requestId);
    if (lock != null) {
      synchronized (lock) {
        lock.notifyAll(); // 唤醒等待的线程
      }
    }
  }

  /**
   * 等待用户选择结果
   *
   * @param requestId 请求标识符
   * @return 用户选择的索引，如果超时或取消返回-1
   */
  public SelectionOptions waitForUserSelection(String requestId) {
    String selectionKey = getSelectionKey(requestId);

    // 为每个requestId创建一个专用的锁对象
    Object lock = selectionLocks.computeIfAbsent(requestId, k -> new Object());

    try {
      synchronized (lock) {
        long maxWaitMillis = 300000; // 5分钟超时
        long startTime = System.currentTimeMillis();

        while (!cancelled) {
          // 检查是否已有用户选择结果
          SelectionOptions selection = getSessionData(selectionKey);
          if (selection != null && selection.hasSelectedOpt()) {
            sessionData.remove(selectionKey);
            return selection;
          }

          // 计算剩余等待时间
          long elapsedTime = System.currentTimeMillis() - startTime;
          long remainingTime = maxWaitMillis - elapsedTime;

          if (remainingTime <= 0) {
            // 超时
            break;
          }

          try {
            // 等待notify信号或超时
            lock.wait(remainingTime);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
          }
        }
      }
    } finally {
      // 清理锁对象，避免内存泄漏
      selectionLocks.remove(requestId);
    }

    return null;
  }

  private void sendSSEEvent(SSERunnable.SSEEventType event, JSONObject data) {
    this.sseWriter.writeSSEEvent(event, data);
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

    // 通知所有等待的选择请求
    for (Object lock : selectionLocks.values()) {
      synchronized (lock) {
        lock.notifyAll();
      }
    }
    selectionLocks.clear();
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionData(String key, ISessionData value) {
    sessionData.put(key, value);
  }


  public <T extends ISessionData> T getSessionData(String selectedKey) {
    return (T) Objects.requireNonNull(sessionData.get(selectedKey)
      , "key:" + selectedKey + " relevant instance can not be null");
  }

  public long getTokenCount() {
    return tokenCount.get();
  }
}
