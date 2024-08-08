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

package com.qlangtech.tis.datax.preview;

import com.alibaba.datax.common.element.DataXResultPreviewOrderByCols;
import com.alibaba.datax.common.element.DataXResultPreviewOrderByCols.OffsetColVal;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-25 09:22
 **/
public class PreviewRowsData {
    private final PreviewHeaderCol[] header;
    private final List<String[]> rows;

    private final List<DataXResultPreviewOrderByCols.OffsetColVal> headerCursor;
    private final List<DataXResultPreviewOrderByCols.OffsetColVal> tailerCursor;

    public PreviewRowsData(PreviewHeaderCol[] header, List<String[]> rows
            , List<DataXResultPreviewOrderByCols.OffsetColVal> headerCursor
            , List<DataXResultPreviewOrderByCols.OffsetColVal> tailerCursor) {
        this.header = header;
        this.rows = rows;
        this.headerCursor = headerCursor;
        this.tailerCursor = tailerCursor;
    }

    public List<OffsetColVal> getHeaderCursor() {
        return headerCursor;
    }

    public List<OffsetColVal> getTailerCursor() {
        return tailerCursor;
    }

    public PreviewHeaderCol[] getHeader() {
        return this.header;
    }

    public List<String[]> getRows() {
        return this.rows;
    }
}
