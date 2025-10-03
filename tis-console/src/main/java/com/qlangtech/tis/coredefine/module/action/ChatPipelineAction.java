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
package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.core.TISPlanAndExecuteAgent;
import com.qlangtech.tis.aiagent.template.TaskTemplateRegistry;
import com.qlangtech.tis.datax.job.SSERunnable;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chat Pipeline Action控制器
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class ChatPipelineAction extends BasicModule {
  private static final Logger logger = LoggerFactory.getLogger(ChatPipelineAction.class);

  // 会话管理
  private static final ConcurrentHashMap<String, ChatSession> sessions = new ConcurrentHashMap<>();

  // 异步执行线程池
  private static final ExecutorService executorService = Executors.newCachedThreadPool();

  /**
   * 获取任务模板列表
   */
  @Func(value = PermissionConstant.AI_AGENT, sideEffect = false)
  public void doGetTemplates(Context context) {
    TaskTemplateRegistry registry = new TaskTemplateRegistry();
    JSONArray templates = new JSONArray();

    for (TaskTemplateRegistry.TaskTemplate template : registry.getAllTemplates()) {
      JSONObject templateJson = new JSONObject();
      templateJson.put("id", template.getId());
      templateJson.put("name", template.getName());
      templateJson.put("description", template.getDescription());
      templateJson.put("sampleText", template.getSampleText());
      templates.add(templateJson);
    }

    this.setBizResult(context, templates);
  }

  /**
   * 创建新的聊天会话
   */
  @Func(value = PermissionConstant.AI_AGENT, sideEffect = false)
  public void doCreateSession(Context context) {
    String sessionId = UUID.randomUUID().toString();
    ChatSession session = new ChatSession(sessionId);
    sessions.put(sessionId, session);

    JSONObject result = new JSONObject();
    result.put("sessionId", sessionId);
    result.put("createTime", System.currentTimeMillis());

    this.setBizResult(context, result);
  }

  /**
   * 获取会话历史
   */
  @Func(value = PermissionConstant.AI_AGENT, sideEffect = false)
  public void doGetSessionHistory(Context context) {
    String sessionId = this.getString("sessionId");
    ChatSession session = sessions.get(sessionId);

    if (session == null) {
      this.addErrorMessage(context, "会话不存在");
      return;
    }

    JSONArray history = new JSONArray();
    for (ChatMessage message : session.getMessages()) {
      JSONObject msg = new JSONObject();
      msg.put("role", message.getRole());
      msg.put("content", message.getContent());
      msg.put("timestamp", message.getTimestamp());
      history.add(msg);
    }

    this.setBizResult(context, history);
  }

  /**
   * SSE聊天接口 - 使用AsyncContext确保连接正确管理
   */
  @Func(value = PermissionConstant.AI_AGENT, sideEffect = true)
  public void doChat(Context context) throws IOException {
    String sessionId = this.getString("sessionId");
    String userInput = this.getString("input");

    if (sessionId == null || sessionId.isEmpty()) {
      sessionId = UUID.randomUUID().toString();
    }

    ChatSession session = sessions.computeIfAbsent(sessionId, ChatSession::new);
    session.addMessage("user", userInput);

    // 获取原始的Servlet请求和响应对象
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    // 设置SSE响应头
//    response.setContentType("text/event-stream");
//    response.setCharacterEncoding("UTF-8");
//    response.setHeader("Cache-Control", "no-cache");
//    response.setHeader("Connection", "keep-alive");
//    response.setHeader("X-Accel-Buffering", "no"); // 禁用Nginx缓冲

    // 启动异步处理 - 这是关键！
    AsyncContext asyncContext = request.startAsync(request, response);

    // 设置超时时间（10分钟）
    asyncContext.setTimeout(600000);
    SSERunnable.SSEEventWriter writer = this.getEventStreamWriter();
    // PrintWriter writer = response.getWriter();

    // 发送会话ID
    JSONObject sessionInfo = new JSONObject();
    sessionInfo.put("sessionId", sessionId);
//    writer.write("event: session\n");
//    writer.write("data: " + sessionInfo.toJSONString() + "\n\n");
//    writer.flush();

    writer.writeSSEEvent(SSERunnable.SSEEventType.AI_AGNET_SESSION, sessionInfo.toJSONString());

    // 创建Agent上下文
    AgentContext agentContext = new AgentContext(sessionId, writer);

    // 保存AgentContext到session中，供submitSelection使用
    session.setAgentContext(agentContext);

    // 异步执行Agent任务
    String finalSessionId = sessionId;
    executorService.execute(() -> {
      try {
        TISPlanAndExecuteAgent agent = new TISPlanAndExecuteAgent(agentContext);
        agent.execute(userInput);

        // 保存助手回复到会话历史
        session.addMessage("assistant", "任务执行完成");

      } catch (Exception e) {
        logger.error("Agent execution failed", e);

//          writer.write("event: error\n");
//          writer.write("data: \n\n");
//          writer.flush();
        writer.writeSSEEvent(
          SSERunnable.SSEEventType.AI_AGNET_ERROR, "{\"error\":\"" + e.getMessage() + "\"}");

      } finally {
        try {
          writer.writeSSEEvent(SSERunnable.SSEEventType.AI_AGNET_DONE, "{\"finished\":true}");

        } finally {
          // 完成异步处理 - 确保连接正确关闭
          asyncContext.complete();
        }
      }
    });

    // 主线程直接返回，不会关闭连接
  }

  /**
   * 用户响应输入（响应Agent的输入请求）
   */
  @Func(value = PermissionConstant.AI_AGENT, sideEffect = true)
  public void doUserResponse(Context context) {
    String sessionId = this.getString("sessionId");
    String fieldId = this.getString("fieldId");
    String userResponse = this.getString("response");

    ChatSession session = sessions.get(sessionId);
    if (session == null) {
      this.addErrorMessage(context, "会话不存在");
      return;
    }

    // 保存用户响应
    session.addUserResponse(fieldId, userResponse);

    JSONObject result = new JSONObject();
    result.put("success", true);
    this.setBizResult(context, result);
  }

  /**
   * 提交用户选择（响应Agent的选择请求）
   */
  @Func(value = PermissionConstant.AI_AGENT, sideEffect = true)
  public void doSubmitSelection(Context context) {
    String sessionId = this.getString("sessionId");
    String requestId = this.getString("requestId");
    Integer selectedIndex = this.getInt("selectedIndex");

    if (sessionId == null || requestId == null || selectedIndex == null) {
      this.addErrorMessage(context, "参数不完整");
      return;
    }

    ChatSession session = sessions.get(sessionId);
    if (session == null) {
      this.addErrorMessage(context, "会话不存在");
      return;
    }

    // 获取AgentContext
    AgentContext agentContext = session.getAgentContext();
    if (agentContext == null) {
      this.addErrorMessage(context, "会话上下文不存在");
      return;
    }

    // 保存用户选择到sessionData中，waitForUserSelection会读取这个值
    String selectionKey = "user_selection_" + requestId;
    agentContext.setSessionData(selectionKey, selectedIndex);

    logger.info("User selection submitted for session={}, requestId={}, selectedIndex={}",
      sessionId, requestId, selectedIndex);

    JSONObject result = new JSONObject();
    result.put("success", true);
    this.setBizResult(context, result);
  }

  /**
   * 清除会话
   */
  @Func(value = PermissionConstant.AI_AGENT, sideEffect = true)
  public void doClearSession(Context context) {
    String sessionId = this.getString("sessionId");
    sessions.remove(sessionId);

    JSONObject result = new JSONObject();
    result.put("success", true);
    this.setBizResult(context, result);
  }

  /**
   * 聊天会话
   */
  private static class ChatSession {
    private final String sessionId;
    private final java.util.List<ChatMessage> messages;
    private final java.util.Map<String, String> userResponses;
    private final long createTime;
    private AgentContext agentContext;

    public ChatSession(String sessionId) {
      this.sessionId = sessionId;
      this.messages = new java.util.ArrayList<>();
      this.userResponses = new ConcurrentHashMap<>();
      this.createTime = System.currentTimeMillis();
    }

    public void addMessage(String role, String content) {
      messages.add(new ChatMessage(role, content));
    }

    public void addUserResponse(String fieldId, String response) {
      userResponses.put(fieldId, response);
    }

    public String getUserResponse(String fieldId) {
      return userResponses.get(fieldId);
    }

    public java.util.List<ChatMessage> getMessages() {
      return messages;
    }

    public String getSessionId() {
      return sessionId;
    }

    public AgentContext getAgentContext() {
      return agentContext;
    }

    public void setAgentContext(AgentContext agentContext) {
      this.agentContext = agentContext;
    }
  }

  /**
   * 聊天消息
   */
  private static class ChatMessage {
    private final String role;
    private final String content;
    private final long timestamp;

    public ChatMessage(String role, String content) {
      this.role = role;
      this.content = content;
      this.timestamp = System.currentTimeMillis();
    }

    public String getRole() {
      return role;
    }

    public String getContent() {
      return content;
    }

    public long getTimestamp() {
      return timestamp;
    }
  }
}
