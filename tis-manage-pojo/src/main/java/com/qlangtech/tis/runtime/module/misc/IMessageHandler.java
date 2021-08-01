/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.runtime.module.misc;

import com.alibaba.citrus.turbine.Context;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-12-24
 */
public interface IMessageHandler {

    String ACTION_MSG = "action_msg";

    String ACTION_BIZ_RESULT = "action_biz_result";

    String ACTION_ERROR_MSG = "action_error_msg";

    /**
     * 错误信息是否是显示在页面上，而不是消息提示框中
     */
    String ACTION_ERROR_PAGE_SHOW = "action_error_page_show";
    String TSEARCH_PACKAGE = "com.qlangtech.tis";

    void errorsPageShow(final Context context);

    void addActionMessage(final Context context, String msg);

    void setBizResult(final Context context, Object result);

    /**
     * 添加错误信息
     *
     * @param context
     * @param msg
     */
    void addErrorMessage(final Context context, String msg);

    default String getRequestHeader(String key) {
        throw new UnsupportedOperationException();
    }
}
