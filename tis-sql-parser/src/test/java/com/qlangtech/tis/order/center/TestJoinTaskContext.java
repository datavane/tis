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
package com.qlangtech.tis.order.center;

import com.google.common.collect.Maps;
import com.qlangtech.tis.fullbuild.taskflow.IFlatTableBuilder;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-20 12:10
 */
public class TestJoinTaskContext implements IJoinTaskContext {

    @Override
    public IFlatTableBuilder getFlatTableBuilder() {
        return null;
    }

    @Override
    public int getTaskId() {
        return 0;
    }

    private Map<String, Object> attrs = Maps.newHashMap();

    @Override
    public <T> T getAttribute(String key) {
        return (T) attrs.get(key);
    }

    @Override
    public void setAttribute(String key, Object v) {
        attrs.put(key, v);
    }

    @Override
    public String getString(String key) {
        return null;
    }

    @Override
    public boolean getBoolean(String key) {
        return false;
    }

    @Override
    public int getInt(String key) {
        return 0;
    }

    @Override
    public long getLong(String key) {
        return 0;
    }

    @Override
    public String getPartitionTimestamp() {
        return null;
    }

    @Override
    public String getIndexName() {
        return null;
    }

    @Override
    public boolean hasIndexName() {
        return false;
    }

    @Override
    public int getIndexShardCount() {
        return 0;
    }
}
