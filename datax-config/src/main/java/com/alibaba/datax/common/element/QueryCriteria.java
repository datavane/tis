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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-28 22:39
 **/
public class QueryCriteria {
    private List<OffsetColVal> pagerOffsetCursor;

//    public String createWhereAndOrderByStatment() {
//        return Objects.requireNonNull(pagerOffsetPointCols, "pagerOffsetPointCols can not be null")
//                .createWhereAndOrderByStatment(this.nextPakge);
//    }

    /**
     * example:
     * <pre>
     * {"nextPage":true
     *  ,"offsetPointer":[{"val":"000012184fb5165f014fb51722460038","numeric":false,"key":"pay_id"}]}
     * </pre>
     *
     * @param jsonPostContent
     * @return
     */
    public static QueryCriteria createCriteria(int pageSize, JSONObject jsonPostContent) {
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setNextPakge(true);
        queryCriteria.setPageSize(pageSize);
        if (queryCriteria.getPageSize() < 1) {
            throw new IllegalStateException("page size can not small than 1");
        }

        JSONArray offsetPointer = null;
        if (jsonPostContent != null) {
            queryCriteria.setNextPakge(jsonPostContent.getBooleanValue("nextPage"));
            offsetPointer = jsonPostContent.getJSONArray("offsetPointer");
        }

        if (offsetPointer != null) {
            List<OffsetColVal> pagerOffsetCursor = OffsetColVal.deserializePreviewCursor(offsetPointer);
            queryCriteria.setPagerOffsetCursor(pagerOffsetCursor);
        }
        return queryCriteria;
    }

    public List<OffsetColVal> getPagerOffsetCursor() {


        return this.pagerOffsetCursor;
    }

    public void setPagerOffsetCursor(List<OffsetColVal> pagerOffsetCursor) {
        this.pagerOffsetCursor = pagerOffsetCursor;
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
