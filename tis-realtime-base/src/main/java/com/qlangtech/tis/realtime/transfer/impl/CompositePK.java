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

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class CompositePK extends DefaultPk {

    private final Map<String, String> routerVals;

    public CompositePK(Object primaryVal, String... kvs) {
        super(String.valueOf(Objects.requireNonNull(primaryVal, "primaryVal can not be null")));
        if (kvs.length < 1) {
            routerVals = null;
        } else {
            routerVals = Maps.newHashMap();
        }
        for (int i = 0; i < kvs.length; i = i + 2) {
            routerVals.put(kvs[i], kvs[i + 1]);
        }
    }

    public String getRouterVal(String routerKey) {
        String routerVal = this.routerVals.get(routerKey);
        if ((routerVal) == null) {
            throw new IllegalStateException("routerKey:" + routerKey + " relevantVal can not be null");
        }
        return routerVal;
    }
}
