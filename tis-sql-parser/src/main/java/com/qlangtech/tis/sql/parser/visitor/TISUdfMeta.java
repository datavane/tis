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
package com.qlangtech.tis.sql.parser.visitor;

import java.lang.reflect.Method;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年6月2日
 */
public class TISUdfMeta {

    // 是否是聚合函数
    private final boolean aggregateFunc;

    // 数字类型函数计算，比如sum,avg之类的
    private final boolean numeric;

    private Method method;

    public TISUdfMeta(boolean aggregateFunc, Method method) {
        this(aggregateFunc, false, method);
    }

    public TISUdfMeta(boolean aggregateFunc, boolean numeric, Method method) {
        super();
        this.aggregateFunc = aggregateFunc;
        this.numeric = numeric;
        this.method = method;
    }

    public boolean isAggregateFunc() {
        return this.aggregateFunc;
    }

    public boolean isNumeric() {
        return this.numeric;
    }
}
