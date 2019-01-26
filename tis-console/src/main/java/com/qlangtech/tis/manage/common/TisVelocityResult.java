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
package com.qlangtech.tis.manage.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.result.VelocityResult;
import com.opensymphony.xwork2.ActionInvocation;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisVelocityResult extends VelocityResult {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(ActionInvocation invocation) throws Exception {
        final String lastFinalLocation = conditionalParse(this.getLocation(), invocation);
        if (!BasicModule.isScreenApply()) {
            doExecute(lastFinalLocation, invocation);
            return;
        }
        setPlaceholder(invocation, lastFinalLocation);
        expressLayout(invocation);
    }

    public void setPlaceholder(ActionInvocation invocation, final String lastFinalLocation) {
        invocation.getInvocationContext().put("screen_placeholder", lastFinalLocation);
    }

    public void expressLayout(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        final String overrideLayoutTemplate = (String) request.getAttribute(BasicModule.Layout_template);
        if (overrideLayoutTemplate == null) {
            doExecute("/runtime/templates/layout/default.vm", invocation);
        } else {
            doExecute(overrideLayoutTemplate, invocation);
        }
        // 是否要执行bigpipe
        BasicModule basicAction = (BasicModule) invocation.getAction();
        HttpServletResponse response = ServletActionContext.getResponse();
        basicAction.doBigPipe(response.getOutputStream());
    }
}
