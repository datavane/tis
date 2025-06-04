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

package com.qlangtech.tis.extension.util;

import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-12-27 12:12
 **/
public class PropAssistFieldTriple<FIELD> {

    private final String fieldName;
    private final FIELD field;
    private final PropValFilter propValFilter;

    /**
     * @param fieldName
     * @param field
     * @param propValFilter 设置目标端对象最终，会进行一次值的转换（可以和目标端类型相一致）
     */
    private PropAssistFieldTriple(String fieldName, FIELD field, PropValFilter propValFilter) {
        this.fieldName = fieldName;
        this.field = field;
        this.propValFilter = propValFilter;
    }

    public static <FIELD> PropAssistFieldTriple<FIELD> of(String fieldName, FIELD field, PropValFilter propValFilter) {
        return new PropAssistFieldTriple<>(fieldName, field, propValFilter);
    }

    public String getFieldName() {
        return fieldName;
    }

    public FIELD getField() {
        return field;
    }

    public PropValFilter getPropValFilter() {
        return propValFilter;
    }
}
