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
import com.qlangtech.tis.plugin.ds.DataType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-08-06 16:55
 **/
public class PreviewRecords {

    private final List<Record> pageRows;
    private final List<OffsetColVal> headerCursor;
    private final List<OffsetColVal> tailerCursor;




    PreviewRecords(List<Record> pageRows, List<OffsetColVal> headerCursor, List<OffsetColVal> tailerCursor) {
        this.pageRows = pageRows;
        this.headerCursor = headerCursor;
        this.tailerCursor = tailerCursor;
    }

    public List<Record> getPageRows() {
        return pageRows;
    }

    public List<OffsetColVal> getHeaderCursor() {
        return headerCursor;
    }

    public List<OffsetColVal> getTailerCursor() {
        return tailerCursor;
    }
}
