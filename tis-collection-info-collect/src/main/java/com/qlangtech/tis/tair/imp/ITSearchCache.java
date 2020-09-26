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
package com.qlangtech.tis.tair.imp;

import java.io.Serializable;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年9月4日下午3:25:52
 */
public interface ITSearchCache {

    public abstract boolean put(Serializable key, Serializable obj);

    /**
     * @param key
     * @param obj
     * @param expir 单位：秒
     * @return
     */
    public abstract boolean put(Serializable key, Serializable obj, int expir);

    public abstract boolean invalid(Serializable key);

    @SuppressWarnings("all")
    public <T> T getObj(Serializable key);
}
