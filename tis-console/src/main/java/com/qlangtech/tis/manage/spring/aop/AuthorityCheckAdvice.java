/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.spring.aop;

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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Method;

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
    protected String doIntercept(ActionInvocation invocation) throws Exception {
        BasicModule action = (BasicModule) invocation.getAction();
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
//        if (!user.hasGrantAuthority(func.value())) {
//            log.warn("loginUser username:" + user.getName() + " userid:" + user.getId() + " has not grant authority on func:" + func.value());
//            rundata.forwardTo("runtime", "hasnopermission.vm");
//            return BasicModule.key_FORWARD;
//        }
        return invocation.invoke();
    }
}
