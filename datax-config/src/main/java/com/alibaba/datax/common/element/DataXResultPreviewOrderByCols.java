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
 * @create: 2024-07-28 22:43
 **/
public class DataXResultPreviewOrderByCols {

    private final boolean first;

    public DataXResultPreviewOrderByCols(boolean first) {
        this.first = first;
    }

    private List<OffsetColVal> offsetCols = new ArrayList<>();

    public List<OffsetColVal> getOffsetCols() {
        return this.offsetCols;
    }

    public void addOffsetColVal(OffsetColVal colVal) {
        this.offsetCols.add(colVal);
    }

    /**
     * 是否是第一页
     *
     * @param nextPakge
     * @return
     */
    public String createWhereAndOrderByStatment(boolean nextPakge) {

        if (offsetCols.size() < 1) {
            throw new IllegalStateException("offsetCols size can not small than 1");
        }
        StringBuilder buffer = new StringBuilder();

        boolean firstProcessed = false;
        if (!this.first) {
            buffer.append(" WHERE ");
            // WHERE
            for (OffsetColVal colVal : offsetCols) {
                if (firstProcessed) {
                    buffer.append(" AND ");
                }
                buffer.append(colVal.colKey).append(nextPakge ? ">" : "<").append(colVal.getLiteriaVal());

                firstProcessed = true;
            }
        }
        buffer.append("\n");
        // ORDER BY
        buffer.append("ORDER BY ");
        firstProcessed = false;
        for (OffsetColVal colVal : offsetCols) {
            if (firstProcessed) {
                buffer.append(",");
            }
            buffer.append(colVal.colKey).append(" ").append(nextPakge ? "ASC" : "DESC");
        }

        return buffer.toString();
    }


    /**
     * 用于卡定DataX数据预览，翻页的偏移指针
     */
    public static class OffsetColVal {
        private final String colKey;
        private final String val;
        private final Boolean isNumericJdbcType;

        private String getLiteriaVal() {
            if (this.isNumericJdbcType == null) {
                throw new IllegalStateException("property isNumericJdbcType can not be null");
            }
            if (this.val == null) {
                throw new IllegalStateException("property val can not be null");
            }
            return isNumericJdbcType ? this.val : ("'" + this.val + "'");
        }

        public OffsetColVal(String colKey, String val, Boolean isNumericJdbcType) {
            this.colKey = colKey;
            this.val = val;
            this.isNumericJdbcType = isNumericJdbcType;
        }

        public String getColKey() {
            return this.colKey;
        }

        public String getVal() {
            return val;
        }

        public Boolean isNumericJdbcType() {
            return isNumericJdbcType;
        }
    }
}
