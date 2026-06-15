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
package com.qlangtech.tis.plugin.ontology.chatbi;

import java.util.List;
import java.util.Map;

/**
 * SQL 执行结果。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/6/2
 */
public class QueryResult {

    private final List<String> columns;
    private final List<Map<String, Object>> rows;
    private final int rowCount;
    private final boolean truncated;
    private final int actualRows;

    public QueryResult(List<String> columns, List<Map<String, Object>> rows, int rowCount,
                       boolean truncated, int actualRows) {
        this.columns = columns;
        this.rows = rows;
        this.rowCount = rowCount;
        this.truncated = truncated;
        this.actualRows = actualRows;
    }

    public QueryResult(List<String> columns, List<Map<String, Object>> rows, int rowCount) {
        this(columns, rows, rowCount, false, rowCount);
    }

    public List<String> columns() {
        return columns;
    }

    public List<Map<String, Object>> rows() {
        return rows;
    }

    public int rowCount() {
        return rowCount;
    }

    public boolean truncated() {
        return truncated;
    }

    public int actualRows() {
        return actualRows;
    }

    public static QueryResult empty() {
        return new QueryResult(List.of(), List.of(), 0, false, 0);
    }
}
