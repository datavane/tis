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

import com.qlangtech.tis.extension.Describable;

import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-12-27 12:12
 **/
public class PropAssistFieldTriple<T extends Describable,FIELD> {

    private final String fieldName;
    private final FIELD field;
    private final Function<T, Object> propGetter;

    private PropAssistFieldTriple(String fieldName, FIELD field, Function<T, Object> propGetter) {
        this.fieldName = fieldName;
        this.field = field;
        this.propGetter = propGetter;
    }

    public static <T extends Describable,FIELD> PropAssistFieldTriple<T,FIELD> of(String fieldName, FIELD field, Function<T, Object> propGetter) {
        return new PropAssistFieldTriple<>(fieldName, field, propGetter);
    }

    public String getFieldName() {
        return fieldName;
    }

    public FIELD getField() {
        return field;
    }

    public Function<T, Object> getPropGetter() {
        return propGetter;
    }
}
