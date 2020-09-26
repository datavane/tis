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
package com.qlangtech.tis.manage.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.result.VelocityResult;
import com.opensymphony.xwork2.ActionInvocation;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年5月11日
 */
public class TerminatorVelocityResult extends VelocityResult {

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
        String layout = request.getParameter("vmlayout");
        String overrideLayoutTemplate = (String) request.getAttribute(BasicModule.Layout_template);
        if (// && invocation.getAction() instanceof IModalDialog
        StringUtils.isEmpty(layout)) {
            overrideLayoutTemplate = "modal";
        }
        overrideLayoutTemplate = StringUtils.defaultString(layout, overrideLayoutTemplate);
        if (overrideLayoutTemplate == null) {
            doExecute("/runtime/templates/layout/blank.vm", invocation);
        } else {
            doExecute("/runtime/templates/layout/" + overrideLayoutTemplate + ".vm", invocation);
        }
        // 是否要执行bigpipe
        BasicModule basicAction = (BasicModule) invocation.getAction();
        HttpServletResponse response = ServletActionContext.getResponse();
        basicAction.doBigPipe(response.getOutputStream());
    }
}
