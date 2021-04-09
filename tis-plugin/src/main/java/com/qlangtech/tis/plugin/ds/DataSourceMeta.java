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

import java.util.List;

/**
 * 数据源meta信息获取
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 15:51
 */
public interface DataSourceMeta {
    /**
     * Get all the tables in dataBase
     *
     * @return
     */
    default List<String> getTablesInDB() throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Get table column metaData list
     *
     * @param table
     * @return
     */
    default List<ColumnMetaData> getTableMetadata(String table) {
        throw new UnsupportedOperationException();
    }
}
