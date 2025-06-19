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
package com.qlangtech.tis.manage.spring.aop;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.qlangtech.tis.lang.ErrorValue;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.lang.TisException.ErrorCode;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.manage.common.RunContextGetter;
import com.qlangtech.tis.manage.common.UserUtils;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.action.BasicModule.Rundata;
import com.qlangtech.tis.runtime.module.action.LoginAction;
import com.qlangtech.tis.runtime.module.action.SysInitializeAction;
import com.qlangtech.tis.runtime.module.action.UserAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 权限校验
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-15
 */
public class AuthorityCheckAdvice extends MethodFilterInterceptor {

  private static final long serialVersionUID = 1L;

  //private DelegateAdminUserService authService;

  private static final Logger log = LoggerFactory.getLogger(AuthorityCheckAdvice.class);

  // private HttpServletRequest request;
  private RunContextGetter daoContextGetter;

  @Autowired
  public final void setRunContextGetter(RunContextGetter daoContextGetter) {
    this.daoContextGetter = daoContextGetter;
  }

  @Override
  protected boolean applyInterceptor(ActionInvocation invocation) {
    return AuthorityCheckAdvice.inNotForwardProcess();
  }

  @Override
  protected String doIntercept(ActionInvocation invocation) throws Exception {
    BasicModule action = (BasicModule) invocation.getAction();
    if (!SysInitializeAction.isSysInitialized()) {
      if (!(action instanceof SysInitializeAction)) {
        // 系统还没有进行初始化
        HttpServletResponse response = ServletActionContext.getResponse();
        AjaxValve.writeInfo2Client(() -> true, response, false
          , Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
          , UserAction.createSysInfo());
        return Action.NONE;
      } else {
        // 直接进行初始化
        SysInitializeAction initAction = (SysInitializeAction) action;
        initAction.doInit(new MockContext());
        return initAction.getReturnCode();
      }
    }



    ActionProxy proxy = invocation.getProxy();
    String namespace = proxy.getNamespace();
    final Method method = action.getExecuteMethod();
    Func func = method.getAnnotation(Func.class);
    final Rundata rundata = BasicModule.getRundataInstance();
    // }
    final IUser user = UserUtils.getUser(ServletActionContext.getRequest(), daoContextGetter.get());
    if (!user.hasLogin() && !(action instanceof LoginAction || StringUtils.startsWith(namespace, "/config"))) {
      rundata.redirectTo("/runtime/login.htm");
      return Action.NONE;
    }
    action.setAuthtoken(user);
    if (func == null) {
      log.debug("target:" + proxy.getActionName() + ",method:" + method.getName() + " has not set FUNC");
      return invocation.invoke();
    }

    return invocation.invoke();
  }

  public static boolean inNotForwardProcess() {
    HttpServletRequest request = ServletActionContext.getRequest();
    return request.getAttribute(BasicModule.KEY_MEHTO) == null;
  }
}
