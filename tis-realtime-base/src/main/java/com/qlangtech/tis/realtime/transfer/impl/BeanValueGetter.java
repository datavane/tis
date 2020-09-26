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
package com.qlangtech.tis.realtime.transfer.impl;

import com.qlangtech.tis.realtime.transfer.IRowValueGetter;
import com.qlangtech.tis.realtime.transfer.UnderlineUtils;
import org.apache.commons.beanutils.BeanUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class BeanValueGetter implements IRowValueGetter {

    private final Object target;

    public BeanValueGetter(Object target) {
        super();
        this.target = target;
    }

    /* 列是有下划线的
     */
    @Override
    public String getColumn(String key) {
        try {
            return BeanUtils.getProperty(target, UnderlineUtils.removeUnderline(key).toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
