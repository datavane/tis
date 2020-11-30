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
package com.qlangtech.tis.runtime.module.misc;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-12-24
 */
public class DefaultMessageHandler extends DefaultFieldErrorHandler implements IMessageHandler {

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

//    @SuppressWarnings("unchecked")
//    public boolean hasErrors(Context context) {
//
//        return context.hasErrors();
//    }

    @Override
    public void errorsPageShow(Context context) {
        context.put(ACTION_ERROR_PAGE_SHOW, true);
    }

    /**
     * 添加错误信息
     *
     * @param context
     * @param msg
     */
    @SuppressWarnings("all")
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
