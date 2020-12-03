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
import java.util.List;
import java.util.Map;

/**
 * @author: baisui 百岁
 * @create: 2020-11-24 16:24
 */
public interface IDataSourceDumper {
    void closeResource();
   // void openResource();

    int getRowSize();

    List<ColumnMetaData> getMetaData();

    /**
     * start to dump data to target fileSystem
     */
    public Iterator<Map<String, String>> startDump();

    String getDbHost();
}
