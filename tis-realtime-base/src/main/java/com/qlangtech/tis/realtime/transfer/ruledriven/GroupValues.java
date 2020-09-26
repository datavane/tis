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
package com.qlangtech.tis.realtime.transfer.ruledriven;

import com.google.common.collect.Lists;
import com.qlangtech.tis.realtime.transfer.IRowValueGetter;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年6月25日
 */
public class GroupValues {

    public final List<IRowValueGetter> vals = Lists.newArrayList();

    public final MediaData data = new MediaData();

    public GroupValues(IRowValueGetter val) {
        this.addVal(val);
    }

    public GroupValues() {
    }

    public void addVal(IRowValueGetter val) {
        this.vals.add(val);
    }

    public void putMediaData(String key, Object val) {
        this.data.put(key, val);
    }

    public Object getMediaProp(String key) {
        return data.get(key);
    }
}
