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

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.result.StrutsResultSupport;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.alibaba.citrus.turbine.Context;
import com.opensymphony.xwork2.ActionInvocation;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.pubhook.common.JsonUtil;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.misc.MessageHandler;
import junit.framework.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AjaxValve extends StrutsResultSupport {

    private static final long serialVersionUID = -3835145419233595896L;

    public static final String BIZ_RESULT = "biz_result";

    public static final String QUERY_RESULT = "query_result";

    public static final String EXEC_NULL = "exec_null";

    @Override
    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        Context context = new MockContext();
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        final String resultHandler = request.getParameter("resulthandler");
        if (EXEC_NULL.equals(resultHandler)) {
            return;
        }
        if (StringUtils.isEmpty(resultHandler)) {
            writeExecuteResult(context, response);
        } else if ("advance_query_result".equals(resultHandler)) {
            Object result = context.get(QUERY_RESULT);
            Assert.assertNotNull("result can not be null", result);
            writeJson(response, new StringBuffer(result.toString()));
        } else {
            Object result = context.get(BIZ_RESULT);
            Assert.assertNotNull("result can not be null", result);
            writeJson(response, new StringBuffer(JsonUtil.toString(result)));
        }
    }

    @SuppressWarnings("unchecked")
    private void writeExecuteResult(Context context, HttpServletResponse response) throws IOException {
        List<String> errorMsgList = (List<String>) context.get(BasicModule.ACTION_ERROR_MSG);
        List<String> msgList = (List<String>) context.get(BasicModule.ACTION_MSG);
        Object bizResult = context.get(MessageHandler.ACTION_BIZ_RESULT);
        writeInfo2Client(response, errorMsgList, msgList, bizResult);
    }

    public static void writeInfo2Client(HttpServletResponse response, List<String> errorMsgList, List<String> msgList) throws IOException {
        writeInfo2Client(response, errorMsgList, msgList, null);
    }

    /**
     * @param response
     * @param errorMsgList
     * @param msgList
     * @param extendVal
     *            业务系统出了 errors 和msgs之外还要传其他的值
     * @throws IOException
     */
    public static void writeInfo2Client(HttpServletResponse response, List<String> errorMsgList, List<String> msgList, Object extendVal) throws IOException {
        try {
            StringBuffer result = new StringBuffer();
            result.append("{\n");
            result.append(" \"success\":").append(errorMsgList == null || errorMsgList.isEmpty());
            JSONArray errors = new JSONArray();
            if (errorMsgList != null) {
                for (String msg : errorMsgList) {
                    errors.put(msg);
                }
            }
            result.append(",\n \"errormsg\":").append(errors.toString(1));
            JSONArray msgs = new JSONArray();
            if (msgList != null) {
                for (String msg : msgList) {
                    msgs.put(msg);
                }
            }
            result.append(",\n \"msg\":").append(msgs.toString(1));
            if (extendVal != null) {
                result.append(",\n \"bizresult\":");
                if (extendVal instanceof JSONObject) {
                    result.append(((JSONObject) extendVal).toString(1));
                } else if (extendVal instanceof JSONArray) {
                    result.append(((JSONArray) extendVal).toString(1));
                } else {
                    result.append(com.alibaba.fastjson.JSON.toJSONString(extendVal, true));
                }
            }
            result.append("\n}");
            writeJson(response, result);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static void writeJson(HttpServletResponse response, StringBuffer execResult) throws IOException {
        response.setContentType("text/json;charset=UTF-8");
        response.getWriter().write(execResult.toString());
    }
}
