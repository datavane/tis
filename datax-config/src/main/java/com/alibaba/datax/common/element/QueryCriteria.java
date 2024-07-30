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

import java.util.Map;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-28 22:39
 **/
public class QueryCriteria {
    private Map<String,String> pagerOffsetPointCols;

//    public String createWhereAndOrderByStatment() {
//        return Objects.requireNonNull(pagerOffsetPointCols, "pagerOffsetPointCols can not be null")
//                .createWhereAndOrderByStatment(this.nextPakge);
//    }

    public Map<String, String> getPagerOffsetPointCols() {
        return this.pagerOffsetPointCols;
    }

    public void setPagerOffsetPointCols(Map<String, String> pagerOffsetPointCols) {
        this.pagerOffsetPointCols = pagerOffsetPointCols;
    }

    /**
     * next or previous pager
     */
    private boolean nextPakge;

    private int pageSize;

    public boolean isNextPakge() {
        return this.nextPakge;
    }

    public int getPageSize() {
        if (this.pageSize < 1) {
            throw new IllegalStateException("property pageSize can not small than 1");
        }
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setNextPakge(boolean nextPakge) {
        this.nextPakge = nextPakge;
    }

//    public DataXResultPreviewOrderByCols getPagerOffsetPointCols() {
//        return this.pagerOffsetPointCols;
//    }
//
//    public void setPagerOffsetPointCols(DataXResultPreviewOrderByCols pagerOffsetPointCols) {
//        this.pagerOffsetPointCols = pagerOffsetPointCols;
//    }
}
