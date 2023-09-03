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

package com.qlangtech.tis.extension.impl;

import com.qlangtech.tis.manage.common.Option;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/8/31
 */
public enum EnumFieldMode {
    MULTIPLE("multiple", (dftVal, fieldDesc) -> {
        if (!isValOfOptionList(dftVal, fieldDesc)) {
            throw new IllegalStateException(fieldDesc + " val " + dftVal.getClass() + " " + "must be " + "type" + " " + "of " + List.class.getName());
        }
        return dftVal;
    }) //
    , DEFAULT("default", (dftVal, fieldDesc) -> {
        if (isValOfOptionList(dftVal, fieldDesc)) {
            // 如果是List类型，则只需要将list的第一个option的值赋值给它就行
            dftVal = getFirstVal(dftVal);
        }
        if (dftVal != null && !(dftVal instanceof String)) {
            throw new IllegalStateException(fieldDesc + " ,default " + "val:" + dftVal + " " + " must" + " be " +
                    "type of String ,but now is " + dftVal.getClass());
        }
        return dftVal;
    });
    public final String val;
    private final BiFunction<Object/**inputVal*/, String /**field desc**/, Object/**output Val*/> enumDftValProcess;

    public static EnumFieldMode parse(String val) {
        for (EnumFieldMode mode : EnumFieldMode.values()) {
            if (mode.val.equals(val)) {
                return mode;
            }
        }
        return EnumFieldMode.DEFAULT;
    }

    private EnumFieldMode(String val, BiFunction<Object, String, Object> enumDftValProcess) {
        this.val = val;
        this.enumDftValProcess = enumDftValProcess;
    }

    /**
     * 处理 field 的默认值，需要保证返回的multi类型时候需要返回的默认值是List类型，‘default’则要返回的是String类型
     *
     * @param targetClass
     * @param formField
     * @return
     */
    public Function<Object, Object> createDefaultValProcess(Class<?> targetClass, Field formField) {
        String fieldDesc =
                "default val of owner class:" + targetClass.getName() + " with " + "field:" + formField.getName();
        return (dftVal) -> {
            if (dftVal == null) {
                throw new IllegalStateException(fieldDesc + " can not be null");
            }
            return this.enumDftValProcess.apply(dftVal, fieldDesc);
        };
    }

    private static boolean isValOfOptionList(Object val, String fieldDesc) {
        Class valClass = val.getClass();
        if (List.class.isAssignableFrom(valClass)) {
            for (Object o : ((List) val)) {
                if (!(o.getClass() == String.class)) {
                    throw new IllegalStateException(fieldDesc + ",opt" + " " + "element " + o.getClass() + " " +
                            "must be type of " + String.class);
                }
            }
            return true;
        }
        return false;
    }

    private static Object getFirstVal(Object val) {
        for (Object vv : (List<?>) val) {
            return String.valueOf(vv);

        }
        return null;
    }
}
