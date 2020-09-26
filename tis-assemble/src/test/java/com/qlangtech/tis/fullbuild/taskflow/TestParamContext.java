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
package com.qlangtech.tis.fullbuild.taskflow;

import java.util.Map;
import com.qlangtech.tis.order.center.IParamContext;
import com.google.common.collect.Maps;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestParamContext implements IParamContext {

    private final Map<String, String> params = Maps.newHashMap();

    public void set(String key, String value) {
        this.params.put(key, value);
    }

    @Override
    public String getPartitionTimestamp() {
        return null;
    }

    @Override
    public String getString(String key) {
        return params.get(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(params.get(key));
    }

    @Override
    public int getInt(String key) {
        return Integer.parseInt(params.get(key));
    }

    @Override
    public long getLong(String key) {
        return Long.parseLong(params.get(key));
    }
}
