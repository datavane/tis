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
package com.qlangtech.tis.manage.common;

import com.opensymphony.xwork2.ActionContext;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * 校验用户当前 应用是否选择了
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class CheckAppDomainExistValve {
  static {
    AppAndRuntime.newAppAndRuntimeConsumer = (appRuntime) -> {
      if (appRuntime.getAppName() != null) {
        removeAppDomain();
      }
    };
  }

  @Autowired
  private HttpServletRequest request;

  public static AppDomainInfo getAppDomain(RunContext context) {
    HttpServletRequest request = ServletActionContext.getRequest();
    return getAppDomain(request, context);
  }

  public static void removeAppDomain() {
    if (ActionContext.getContext() != null) {
      HttpServletRequest request = ServletActionContext.getRequest();
      // request.setAttribute(ActionTool.REQUEST_DOMAIN_KEY, Objects.requireNonNull(domain, "domain can not be null"));
      request.removeAttribute(ActionTool.REQUEST_DOMAIN_KEY);
    }
  }

  public static AppDomainInfo getAppDomain(HttpServletRequest request, RunContext context) {
    AppDomainInfo domain = (AppDomainInfo) request.getAttribute(ActionTool.REQUEST_DOMAIN_KEY);
    if (domain != null) {
      return domain;
    }

    AppDomainInfo appDomain = null;
    AppAndRuntime environment = AppAndRuntime.getAppAndRuntime();
    if (environment == null) {
      domain = AppDomainInfo.createAppNotAware(DefaultFilter.getRuntime());
      request.setAttribute(ActionTool.REQUEST_DOMAIN_KEY, domain);
      return domain;
    }
    try {
      if ((environment.getAppName()) == null) {
        // 只选择了环境 参数
        appDomain = AppDomainInfo.createAppNotAware(environment.getRuntime());
      } else {
        DataXName dataXName = environment.getAppName();
        appDomain = queryApplication(request, context, dataXName.getPipelineName(), environment.getRuntime());
        if (appDomain == null) {
          Application app = new Application();
          app.setProjectName(dataXName.getPipelineName());
          appDomain = new AppDomainInfo(0, 0, environment.getRuntime(), app);
        }
      }
    } catch (Exception e) {
      // return new NullAppDomainInfo(context.getApplicationDAO());
      throw new IllegalStateException(e);
    }
    if (appDomain == null) {
      appDomain = CheckAppDomainExistValve.createNull();
    }
    request.setAttribute(ActionTool.REQUEST_DOMAIN_KEY, appDomain);
    return appDomain;
  }

  public static AppDomainInfo queryApplication(HttpServletRequest request, RunContext context, final String appname, RunEnvironment runtime) {
    if (true) {
      ApplicationCriteria query = new ApplicationCriteria();
      query.createCriteria().andProjectNameEqualTo(appname);
      AppDomainInfo appDomain = null;
      for (Application app : context.getApplicationDAO().selectByExample(query)) {
        // Integer bizid, Integer appid, RunEnvironment runEnvironment, Application app
        appDomain = new AppDomainInfo(app.getDptId(), app.getAppId(), // getRuntime(match),
          runtime, app);
        break;
      }
      return appDomain;
    }
    return null;
  }

  private static final AppDomainInfo NULL = new NullAppDomainInfo(null);

  public static AppDomainInfo createNull() {
    return NULL;
  }

  private static class NullAppDomainInfo extends AppDomainInfo implements Nullable {

    // Integer bizid, Integer appid, RunEnvironment runEnvironment, Application app
    private NullAppDomainInfo(IApplicationDAO applicationDAO) {
      super(Integer.MAX_VALUE, Integer.MAX_VALUE, RunEnvironment.DAILY, null);
    }

    @Override
    protected void judgeNull(Application application) {
    }

    @Override
    public Integer getAppid() {
      return 0;
    }

    @Override
    public String getAppName() {
      return StringUtils.EMPTY;
    }

    @Override
    public Integer getDptid() {
      return -1;
    }

    @Override
    public RunEnvironment getRunEnvironment() {
      return RunEnvironment.DAILY;
    }
  }
}
