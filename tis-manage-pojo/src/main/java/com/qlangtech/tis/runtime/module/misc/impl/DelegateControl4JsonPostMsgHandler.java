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
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.lang.StringUtils;

/**
 * 支持json post处理
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-24 13:48
 */
public class DelegateControl4JsonPostMsgHandler extends BasicDelegateMsgHandler {

    private final JSONObject postData;

    public DelegateControl4JsonPostMsgHandler(IControlMsgHandler delegate, JSONObject postData) {
        super(delegate);
        this.postData = postData;
    }

    @Override
    public String getString(String key) {
        return postData.getString(key);
    }

    @Override
    public String getString(String key, String dftVal) {
        return StringUtils.defaultIfBlank(postData.getString(key), dftVal);
    }

    @Override
    public boolean getBoolean(String key) {
        return postData.getBoolean(key);
    }
}
