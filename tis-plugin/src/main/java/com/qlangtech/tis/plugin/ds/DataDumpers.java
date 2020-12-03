/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.plugin.ds;

import java.util.Iterator;

/**
 * Abstract the dataSource modal
 *
 * @author: baisui 百岁
 * @create: 2020-11-24 10:40
 **/
public class DataDumpers {
    public final int splitCount;
    public final Iterator<IDataSourceDumper> dumpers;

    public DataDumpers(int splitCount, Iterator<IDataSourceDumper> dumpers) {
        this.splitCount = splitCount;
        this.dumpers = dumpers;
    }
}
