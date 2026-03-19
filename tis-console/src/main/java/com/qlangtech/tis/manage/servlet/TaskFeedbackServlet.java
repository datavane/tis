/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.manage.servlet;

import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServletFactory;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 根据傳入的TaskID，实时获取远端實時日志
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014-4-2
 */
public class TaskFeedbackServlet extends JettyWebSocketServlet {

  private static final Logger logger = LoggerFactory.getLogger(TaskFeedbackServlet.class);

  private static final long serialVersionUID = 1L;

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  public void configure(JettyWebSocketServletFactory factory) {
    // set a 10 second timeout
    factory.setIdleTimeout(java.time.Duration.ofMillis(240000));
    factory.setCreator((req, rep) -> new FullAssembleLogSocket());
  }

  @WebSocket
  public static class FullAssembleLogSocket {

    long taskid;
    private Session session;

    private final ScheduledExecutorService falconSendScheduler = Executors.newScheduledThreadPool(1);

    @OnWebSocketOpen
    public void onOpen(Session sess) {
      this.session = sess;
      taskid = Long.parseLong(getParameter(JobCommon.KEY_TASK_ID));
      logger.info("start a new log fetch tasklog status taskid:" + taskid);
      // new  LogCollectorClient();
      falconSendScheduler.scheduleAtFixedRate(() -> {
          try {
            URL url = new URL(Config.getAssembleHost() + "/task_status?" + JobCommon.KEY_TASK_ID + "=" + taskid);
            // server side:TaskStatusServlet
            JSONObject result = HttpUtils.processContent(url, new StreamProcess<JSONObject>() {
              @Override
              public JSONObject p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                JSONTokener tokener = new JSONTokener(new InputStreamReader(stream, Charset.forName("utf8")));
                return new JSONObject(tokener);
              }
            });
            boolean success = result.getBoolean("success");
            if (success) {
              // 向客戶端傳輸數據
              session.sendText(String.valueOf(result.get("status")), Callback.NOOP);
            }
          } catch (Throwable e) {
            logger.error(e.getMessage(), e);
          }
        }, 5, /* 5秒之后开始下发数据 */
        2, TimeUnit.SECONDS);
    }

    private String getParameter(String key) {
      for (String v : this.session.getUpgradeRequest().getParameterMap().get(key)) {
        return v;
      }
      throw new IllegalArgumentException("key:" + key + " relevant val is not exist in request");
    }

    // @Override
    // public void onOpen(final Connection connection) {
    // this._connection = connection;
    //
    // }
    @OnWebSocketClose
    public void onClose(Session sess, int statusCode, String reason) {
      logger.info("close tasklog status monitor taskid:" + taskid);
      this.falconSendScheduler.shutdownNow();
    }
  }
}
