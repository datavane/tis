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
package com.qlangtech.tis.runtime.module.misc;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.citrus.turbine.Context;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultMessageHandler implements MessageHandler {

    // private static final long serialVersionUID = 1L;
    @SuppressWarnings("unchecked")
    public void addActionMessage(final Context context, String msg) {
        List<String> msgList = (List<String>) context.get(ACTION_MSG);
        if (msgList == null) {
            msgList = new ArrayList<String>();
        }
        msgList.add(msg);
        context.put(ACTION_MSG, msgList);
    }

    @Override
    public void setBizResult(Context context, Object result) {
        context.put(ACTION_BIZ_RESULT, result);
    }

    @SuppressWarnings("unchecked")
    public boolean hasErrors(Context context) {
        return context.get(ACTION_ERROR_MSG) != null && !((List<String>) context.get(ACTION_ERROR_MSG)).isEmpty();
    }

    /**
     * 添加错误信息
     *
     * @param context
     * @param msg
     */
    @SuppressWarnings("unchecked")
    public void addErrorMessage(final Context context, String msg) {
        List<String> msgList = (List<String>) context.get(ACTION_ERROR_MSG);
        if (msgList == null) {
            msgList = new ArrayList<String>();
            context.put(ACTION_ERROR_MSG, msgList);
        }
        msgList.add(msg);
        context.put(ACTION_MSG, new ArrayList<String>());
    }
}
