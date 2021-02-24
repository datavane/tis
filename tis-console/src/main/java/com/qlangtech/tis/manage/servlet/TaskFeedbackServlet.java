/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.servlet;

import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.order.center.IParamContext;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
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
public class TaskFeedbackServlet extends WebSocketServlet {

  private static final Logger logger = LoggerFactory.getLogger(TaskFeedbackServlet.class);

  private static final long serialVersionUID = 1L;

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    // set a 10 second timeout
    factory.getPolicy().setIdleTimeout(240000);
    factory.getPolicy().setAsyncWriteTimeout(-1);
    factory.register(FullAssembleLogSocket.class);
  }

  public static class FullAssembleLogSocket extends WebSocketAdapter {

    long taskid;

    private final ScheduledExecutorService falconSendScheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onWebSocketConnect(Session sess) {
      super.onWebSocketConnect(sess);
      taskid = Long.parseLong(getParameter(IParamContext.KEY_TASK_ID));
      logger.info("start a new log fetch tasklog status taskid:" + taskid);
      // new  LogCollectorClient();
      falconSendScheduler.scheduleAtFixedRate(() -> {
          try {
            URL url = new URL(Config.getAssembleHost() + "/task_status?" + IParamContext.KEY_TASK_ID + "=" + taskid);
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
              getRemote().sendString(String.valueOf(result.get("status")));
            }
          } catch (Throwable e) {
            logger.error(e.getMessage(), e);
          }
        }, 5, /* 5秒之后开始下发数据 */
        2, TimeUnit.SECONDS);
    }

    private String getParameter(String key) {
      for (String v : this.getSession().getUpgradeRequest().getParameterMap().get(key)) {
        return v;
      }
      throw new IllegalArgumentException("key:" + key + " relevant val is not exist in request");
    }

    // @Override
    // public void onOpen(final Connection connection) {
    // this._connection = connection;
    //
    // }
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
      super.onWebSocketClose(statusCode, reason);
      logger.info("close tasklog status monitor taskid:" + taskid);
      this.falconSendScheduler.shutdownNow();
    }
  }
}
