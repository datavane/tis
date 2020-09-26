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
package com.qlangtech.tis.runtime.module.misc.impl;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-09-11 15:24
 */
public abstract class BasicDelegateMsgHandler implements IControlMsgHandler {

    private final IControlMsgHandler delegate;

    public BasicDelegateMsgHandler(IControlMsgHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public final void addFieldError(Context context, String fieldName, String msg, Object... params) {
        delegate.addFieldError(context, fieldName, msg, params);
    }

    @Override
    public final void errorsPageShow(Context context) {
        delegate.errorsPageShow(context);
    }

    @Override
    public final void addActionMessage(Context context, String msg) {
        delegate.addActionMessage(context, msg);
    }

    @Override
    public final void setBizResult(Context context, Object result) {
        delegate.setBizResult(context, result);
    }

    @Override
    public final void addErrorMessage(Context context, String msg) {
        delegate.addErrorMessage(context, msg);
    }
}
