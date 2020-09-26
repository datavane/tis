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
package com.alibaba.citrus.turbine.impl;

import com.alibaba.citrus.turbine.Context;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DefaultContext implements Context {

    final Map<String, Object> contextMap = new HashMap<String, Object>();

    public Map<String, Object> getContextMap() {
        return contextMap;
    }

    public DefaultContext() {
    }

    public Map<String, Object> getContextValue() {
        return this.contextMap;
    }

    @Override
    public boolean containsKey(String key) {
        return contextMap.containsKey(key);
    }

    @Override
    public Object get(String key) {
        return contextMap.get(key);
    }

    @Override
    public Set<String> keySet() {
        return contextMap.keySet();
    }

    @Override
    public void put(String key, Object value) {
        contextMap.put(key, value);
    }

    @Override
    public void remove(String key) {
    }
}
