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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.qlangtech.tis.manage.common.TisActionMapper;

/*
 * 拦截系统异常，以控制页面友好
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisExceptionInterceptor extends MethodFilterInterceptor {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TisExceptionInterceptor.class);

    @Override
    protected String doIntercept(ActionInvocation invocation) throws Exception {
        HttpServletResponse response = ServletActionContext.getResponse();
        Boolean tagInvocation = (Boolean) ServletActionContext.getRequest().getAttribute(StrutsStatics.STRUTS_ACTION_TAG_INVOCATION);
        if (tagInvocation != null && tagInvocation) {
            return invocation.invoke();
        }
        final ActionMapping mapping = ServletActionContext.getActionMapping();
        if (mapping == null) {
            throw new IllegalStateException("actionMapping can not be null");
        }
        try {
            return invocation.invoke();
        } catch (Exception e) {
            if (TisActionMapper.REQUEST_EXTENDSION_AJAX.equals(mapping.getExtension())) {
                logger.error(e.getMessage(), e);
                List<String> empty = Collections.emptyList();
                List<String> errors = new ArrayList<String>();
                errors.add("抱歉！服务端发生异常，请联系系统管理员");
                errors.add(ExceptionUtils.getRootCauseMessage(e));
                AjaxValve.writeInfo2Client(response, errors, empty);
                return Action.NONE;
            } else {
                throw e;
            }
        }
    }
}
