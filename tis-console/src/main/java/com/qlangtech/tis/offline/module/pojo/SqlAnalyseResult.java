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
package com.qlangtech.tis.offline.module.pojo;

import java.util.List;

/**
 * 返回sql解析结果
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class SqlAnalyseResult {

    private final String tableName;

    private final List<ColumnMetaData> columns;

    public SqlAnalyseResult(String tableName, List<ColumnMetaData> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnMetaData> getColumns() {
        return columns;
    }
}
