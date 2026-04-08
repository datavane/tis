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
package com.qlangtech.tis.manage;

import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.mcp.TISHttpMcpServer;
import com.qlangtech.tis.util.HeteroEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * TIS Console 初始化监听器
 * 负责 TIS 系统启动和关闭时的初始化工作
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-23 09:49
 */
public class ConsoleInitilizeListener implements ServletContextListener {

  private static final Logger logger = LoggerFactory.getLogger(ConsoleInitilizeListener.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    logger.info("TIS Console initializing...");


    // 1. 设置 CenterResource 配置
    CenterResource.setNotFetchFromCenterRepository();

    // 2. 将 Spring ApplicationContext 注入到 BasicServlet，
    //    避免后续在非 HTTP 请求线程（如 MCP tool handler）中
    //    通过 DefaultFilter ThreadLocal 获取 ServletContext 失败
    WebApplicationContext springCtx =
      WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
    BasicServlet.setApplicationContext(springCtx);


    IPluginEnum.registeExistItemsGettor(HeteroEnum.DATASOURCE, (desc) -> {
      return com.qlangtech.tis.util.PluginItems.getExistDbs(desc.getDisplayName());
    });

    // 3. 初始化 MCP Server
    TISHttpMcpServer.McpProviderResult mcpResult = TISHttpMcpServer.getMcpProvider();

    jakarta.servlet.ServletRegistration.Dynamic mcpServlet =
      sce.getServletContext().addServlet("tis-mcp", mcpResult.getServletProvider());
    mcpServlet.setAsyncSupported(true);
    mcpServlet.addMapping("/mcp/*");

    // 4. 初始化 TIS Actor System
    try {
      // initializeTISActorSystem(sce);
    } catch (Exception e) {
      logger.error("Failed to initialize TIS Actor System", e);
      // 不抛出异常，允许 TIS 继续启动（Actor System 是可选功能）
    }

    logger.info("TIS Console initialized successfully");
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    logger.info("TIS Console shutting down...");

    // 优雅关闭 TIS Actor System
    //    if (tisActorSystem != null) {
    //      try {
    //        tisActorSystem.shutdown();
    //      } catch (Exception e) {
    //        logger.error("Failed to shutdown TIS Actor System", e);
    //      }
    //    }

    logger.info("TIS Console shutdown completed");
  }


}
