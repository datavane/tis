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

import com.qlangtech.tis.extension.util.ClassLoaderReflectionToolkit;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.web.start.TisApp;
import com.qlangtech.tis.web.start.TisAppLaunch;
import com.qlangtech.tis.web.start.TisSubModule;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import org.eclipse.jetty.ee11.servlet.ServletHolder;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-05 19:53
 * @see ClassLoaderReflectionToolkit 启动时候要加：--add-opens java.base/java.lang=ALL-UNNAMED
 */
public class ConsoleStart {
  static {
    // System.setProperty(Config.KEY_LOG_DIR, "/opt/logs/tis");
    System.setProperty(Config.SYSTEM_KEY_LOGBACK_PATH_KEY, "logback-console.xml");
  }

  public static void main(String[] args) throws Exception {
    TisAppLaunch.setTest(true);
    CenterResource.setNotFetchFromCenterRepository();


    TisApp app = new TisApp(TisSubModule.TIS_CONSOLE, (context) -> {
//      HttpServletStreamableServerTransportProvider mcpProvider = TISHttpMcpServer.getMcpProvider();
//      context.addServlet(new ServletHolder(mcpProvider), "/mcp/*");
      context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
      context.setInitParameter("org.eclipse.jetty.servlet.Default.welcomeServlets", "true");
    });

    System.out.println("start");
    app.start(args);
  }
}
