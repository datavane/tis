/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.spring.aop;

import java.lang.reflect.Method;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;
import org.springframework.beans.factory.annotation.Autowired;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContextGetter;
import com.qlangtech.tis.manage.common.UserUtils;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.action.BasicModule.Rundata;
import com.qlangtech.tis.runtime.module.action.LoginAction;
import com.qlangtech.tis.runtime.module.screen.Login;

/*
 * 权限校验
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AuthorityCheckAdvice extends MethodFilterInterceptor {

    private static final long serialVersionUID = 1L;

    private DelegateAdminUserService authService;

    private static final Log log = LogFactory.getLog(AuthorityCheckAdvice.class);

    // private HttpServletRequest request;
    private RunContextGetter daoContextGetter;

    @Autowired
    public final void setRunContextGetter(RunContextGetter daoContextGetter) {
        this.daoContextGetter = daoContextGetter;
    }

    @Override
    protected String doIntercept(ActionInvocation invocation) throws Exception {
        BasicModule action = (BasicModule) invocation.getAction();
        Boolean tagInvocation = (Boolean) ServletActionContext.getRequest().getAttribute(StrutsStatics.STRUTS_ACTION_TAG_INVOCATION);
        if (tagInvocation != null && tagInvocation) {
            return invocation.invoke();
        }
        ActionProxy proxy = invocation.getProxy();
        String namespace = proxy.getNamespace();
        final Method method = action.getExecuteMethod();
        Func func = method.getAnnotation(Func.class);
        final Rundata rundata = BasicModule.getRundataInstance();
        // }
        final IUser user = UserUtils.getUser(ServletActionContext.getRequest(), daoContextGetter.get());
        if (!user.hasLogin() && !(action instanceof Login || action instanceof LoginAction || StringUtils.startsWith(namespace, "/config"))) {
            rundata.redirectTo("/runtime/login.htm");
            return Action.NONE;
        }
        action.setAuthtoken(user);
        if (func == null) {
            log.debug("target:" + proxy.getActionName() + ",method:" + method.getName() + " has not set FUNC");
            return invocation.invoke();
        }
        if (!user.hasGrantAuthority(func.value())) {
            log.warn("loginUser username:" + user.getName() + " userid:" + user.getId() + " has not grant authority on func:" + func.value());
            rundata.forwardTo("runtime", "hasnopermission.vm");
            return BasicModule.key_FORWARD;
        }
        return invocation.invoke();
    }

    public DelegateAdminUserService getAuthService() {
        return authService;
    }

    // 
    // public HttpServletRequest getRequest() {
    // return request;
    // }
    // 
    // @Autowired
    // public void setRequest(HttpServletRequest request) {
    // this.request = request;
    // }
    public void setAuthService(DelegateAdminUserService authService) {
        this.authService = authService;
    }
}
