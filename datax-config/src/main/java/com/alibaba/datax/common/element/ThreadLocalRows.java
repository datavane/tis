/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.datax.common.element;

import com.alibaba.datax.common.element.DataXResultPreviewOrderByCols.OffsetColVal;
import com.alibaba.datax.common.util.ISelectedTabMeta;
import com.qlangtech.tis.plugin.ds.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-22 16:56
 **/
public class ThreadLocalRows {
    private final List<Record> rows = new ArrayList<>();

    private QueryCriteria query;
    private DataXResultPreviewOrderByCols orderByCols;

    public PreviewRecords createPreviewRecords(ISelectedTabMeta tab) {
        final int pageSize = query.getPageSize();
        List<OffsetColVal> headerCursor = null;
        List<OffsetColVal> tailerCursor = null;
        Map<String, DataType> typeMap
                = tab.getCols().stream().collect(Collectors.toMap((col) -> col.getName(), (col) -> col.getType()));
        for (Record record : rows) {
            // 设置header
            headerCursor = createCursor(tab, typeMap, record);
            break;
        }

        for (int index = rows.size() - 1; index >= 0; index--) {
            // 设置tailer
            if (index >= pageSize - 1) {
                Record tailerRecord = rows.get(index);
                tailerCursor = createCursor(tab, typeMap, tailerRecord);
            }
            break;
        }

        return new PreviewRecords(this.rows, headerCursor, tailerCursor);
    }

    private List<OffsetColVal> createCursor(ISelectedTabMeta tab, Map<String, DataType> typeMap, Record record) {
        List<OffsetColVal> cursor = new ArrayList<>();
        for (String pk : tab.getPrimaryKeys()) {
            cursor.add(new OffsetColVal(pk, record.getString(pk, true), isNumericJdbcType(typeMap, pk)));
        }
        return cursor;
    }

    private boolean isNumericJdbcType(Map<String, DataType> typeMap, String colKey) {
        switch (Objects.requireNonNull(typeMap.get(colKey)
                , "colKey:" + colKey + " relevant dataType can not be null").getCollapse()) {
            case INT:
            case Long:
            case Double:
            case Boolean:
                return true;
            default:
                return false;
        }
    }

    public DataXResultPreviewOrderByCols getPagerOffsetPointCols() {
        return this.orderByCols;
    }

    public void setPagerOffsetPointCols(DataXResultPreviewOrderByCols pagerOffsetPointCols) {
        this.orderByCols = pagerOffsetPointCols;
    }

    public QueryCriteria getQuery() {
        return query;
    }

    public void setQuery(QueryCriteria query) {
        this.query = query;
    }

    public void addRecord(Record record) {
        this.rows.add(record);
    }

    public List<Record> getRows() {
        return this.rows;
    }
}
