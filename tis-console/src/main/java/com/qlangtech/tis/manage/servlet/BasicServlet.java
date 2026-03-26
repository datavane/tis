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
package com.qlangtech.tis.manage.servlet;

import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.RunContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.struts2.ActionContext;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-19
 */
public class BasicServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  protected RunContext getContext() {
    return getBeanByType(RunContext.class);
  }

  public static ActionContext getActionContext() {
    final ActionContext actionContext = ActionContext.getContext();
    if (actionContext == null) {
      throw new IllegalStateException("ActionContext can not be null");
    }
    if (actionContext.getServletContext() == null) {
      throw new IllegalStateException("ServletContext can not be null");
    }
    return actionContext;
  }


  public static void autowireBeanProperties(Object existingBean) {
    getSpringApplicationContext().getAutowireCapableBeanFactory().autowireBeanProperties(existingBean,
      AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
  }

  private static ApplicationContext applicationContext;

  private static ApplicationContext getSpringApplicationContext() {
    if (applicationContext == null) {
      ServletContext servletContext = getActionContext().getServletContext();
      applicationContext = (ApplicationContext)
        Objects.requireNonNull(servletContext, "servletContext can not be null")
          .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
      Objects.requireNonNull(applicationContext
        , "applicationContext can not be null in servletContent by key:"
          + WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }
    return applicationContext;
  }

  public static void setApplicationContext(ApplicationContext context) {
    applicationContext =Objects.requireNonNull( context,"context can not be null");
  }

  public static <T> T getBeanByType(Class<T> clazz) {

    ApplicationContext applicationContext = getSpringApplicationContext();
    for (Object context : applicationContext.getBeansOfType(clazz).values()) {
      return clazz.cast(context);
    }
    throw new IllegalStateException("can no t find:" + clazz);
  }

  protected void wirteXml2Client(HttpServletResponse response, Object o) throws IOException {
    response.setContentType(DownloadResource.XML_CONTENT_TYPE);
    HttpConfigFileReader.xstream.toXML(o, response.getWriter());
  }

  protected void include(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
    RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(path);
    dispatcher.include(request, response);
  }
}
