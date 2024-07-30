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

import java.util.ArrayList;
import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-22 16:56
 **/
public class ThreadLocalRows {
    private final List<Record> rows = new ArrayList<>();

    private QueryCriteria query;
    private DataXResultPreviewOrderByCols orderByCols;

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
