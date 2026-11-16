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
package com.qlangtech.tis.plugin.annotation;

import com.qlangtech.tis.extension.impl.EnumFieldMode;
import com.qlangtech.tis.plugin.IEndTypeGetter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * 标在枚举类型上，声明它作 {@code @FormField} 字段类型时的表单呈现：
 * 显示模式见 {@link #mode()}（{@code PropertyType.getEnumFieldMode()} 解析），
 * 选项过滤见 {@link #enumItemPredicate()}（{@code PropertyType.resolveEnumProp()} 解析）。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EnumFormFieldType {

    EnumFieldMode mode() default EnumFieldMode.DEFAULT;

    /**
     * 枚举常量过滤，默认 {@link EnumFormFieldTypeFilter#ALL} 不做过滤。
     */
    EnumFormFieldTypeFilter enumItemPredicate() default EnumFormFieldTypeFilter.ALL;

    /**
     * 内置过滤器作用在具体枚举类型上（如 {@link EnumFormFieldTypeFilter#ExcludeDataEndType} 只认
     * {@link IEndTypeGetter.EndType}），标错枚举类型在编译期发现不了，
     * 要等到表单 descriptor 构建（GET）生成选项时才抛 {@link IllegalStateException}。
     */
    enum EnumFormFieldTypeFilter {
        /** 不过滤，全部枚举常量都进选项 */
        ALL((e) -> true) //
        /**
         * 排除 {@link IEndTypeGetter.EndTypeCategory#Data} 与 {@link IEndTypeGetter.EndTypeCategory#DataType}
         * 类别的端类型，供图标、辅助组件类选择场景使用
         */
        , ExcludeDataEndType((en) -> {
            if (!(en instanceof IEndTypeGetter.EndType endType)) {
                throw new IllegalStateException("param type must be " + IEndTypeGetter.EndType.class
                        + " but now is " + en.getClass().getName());
            }
            return endType.category != IEndTypeGetter.EndTypeCategory.Data
                    && endType.category != IEndTypeGetter.EndTypeCategory.DataType;
        });

        public final Predicate<Enum<?>> filter;

        private EnumFormFieldTypeFilter(Predicate<Enum<?>> filter) {
            this.filter = filter;
        }
    }
}
