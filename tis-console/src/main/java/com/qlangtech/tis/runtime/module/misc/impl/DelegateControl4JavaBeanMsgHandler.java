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

import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-09-11 15:27
 */
public class DelegateControl4JavaBeanMsgHandler extends BasicDelegateMsgHandler {

    private final Object bean;

    public DelegateControl4JavaBeanMsgHandler(IControlMsgHandler delegate, Object bean) {
        super(delegate);
        this.bean = bean;
    }

    @Override
    public String getString(String key) {
        try {
            return BeanUtils.getProperty(bean, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getString(String key, String dftVal) {
        String result = this.getString(key);
        return StringUtils.defaultIfEmpty(result, dftVal);
    }

    @Override
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(this.getString(key));
    }
}
