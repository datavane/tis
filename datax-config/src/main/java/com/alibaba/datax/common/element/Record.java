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

/**
 * Created by jingxing on 14-8-24.
 */
public interface Record extends ColumnAwareRecord<Object> {

//    /**
//     * 用于data rows preview
//     */
//    ThreadLocal<List<Record>> threadLocalRows
//            = new ThreadLocal<List<Record>>() {
//        @Override
//        protected List<Record> initialValue() {
//            return new ArrayList<>();
//        }
//    };

    public void addColumn(Column column);

    public void setColumn(int i, final Column column);

    public Column getColumn(int i);

    public String toString();

    public int getColumnNumber();

    public int getByteSize();

    public int getMemorySize();

}
