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
package com.qlangtech.tis.manage.common.valve;

import javax.servlet.http.HttpServletRequest;
import junit.framework.Assert;
import org.apache.struts2.ServletActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.qlangtech.tis.coredefine.module.screen.CoreDefineScreen;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.pojo.ServerGroupAdapter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ServerGroupExistInterceptor extends MethodFilterInterceptor {

    private static final long serialVersionUID = 1L;

    @Override
    protected String doIntercept(ActionInvocation invocation) throws Exception {
        // BasicScreen
        HttpServletRequest request = ServletActionContext.getRequest();
        Object process = request.getAttribute(ServerGroupExistInterceptor.class.getName());
        if (process != null) {
            return invocation.invoke();
        }
        try {
            CoreDefineScreen targetAction = null;
            if (invocation.getAction() instanceof CoreDefineScreen) {
                targetAction = (CoreDefineScreen) invocation.getAction();
            }
            Assert.assertNotNull("an unexpect target class:" + invocation.getAction().getClass().getName(), targetAction);
            final ServerGroupAdapter configGroup0 = targetAction.getConfigGroup0();
            if (configGroup0 == null) {
                BasicModule.getRundataInstance().forwardTo("coredefine", "has_not_config_resource.vm");
                return BasicModule.key_FORWARD;
            }
            return invocation.invoke();
        } finally {
            request.setAttribute(ServerGroupExistInterceptor.class.getName(), new Object());
        }
    }
}
