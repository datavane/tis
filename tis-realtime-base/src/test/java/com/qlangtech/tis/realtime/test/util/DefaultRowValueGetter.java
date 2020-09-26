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
package com.qlangtech.tis.realtime.test.util;

import com.qlangtech.tis.realtime.transfer.IRowValueGetter;
import com.qlangtech.tis.realtime.transfer.impl.AbstractRowValueGetter;
import com.qlangtech.tis.wangjubao.jingwei.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DefaultRowValueGetter extends AbstractRowValueGetter implements IRowValueGetter {

    public UpdatePropsCollector updateProps;

    public DefaultRowValueGetter(Table tableProcessor) {
        super(tableProcessor);
    }

    public boolean isUpdatePropsCollect() {
        return updateProps.collect;
    }

    public void startCollectUpdateProp() {
        updateProps.collect = true;
        updateProps.vals.vals.clear();
    }

    public DefaultRowValueGetter stopCollectUpdateProp() {
        updateProps.collect = false;
        return updateProps.vals;
    }

    public final Map<String, String> vals = new HashMap<>();

    public void put(String key, String val) {
        val = this.processRawVal(key, val);
        this.vals.put(key, val);
        UpdatePropsCollector updatePropsCollector = updateProps;
        if (updatePropsCollector != null && updatePropsCollector.collect) {
            updatePropsCollector.vals.put(key, val);
        }
    }

    @Override
    public String getColumn(String key) {
        return this.vals.get(key);
    }

    public static class UpdatePropsCollector {

        // 是否收集
        public boolean collect = false;

        public final DefaultRowValueGetter vals;

        public UpdatePropsCollector(Table tableProcessor) {
            this.vals = new DefaultRowValueGetter(tableProcessor);
        }
    }
}
