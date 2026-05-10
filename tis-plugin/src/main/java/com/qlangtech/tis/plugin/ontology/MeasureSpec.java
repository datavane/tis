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
package com.qlangtech.tis.plugin.ontology;

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

/**
 * Measure（度量）列的元信息：默认聚合方式 + 单位 + 精度。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/9
 */
public class MeasureSpec implements Describable<MeasureSpec> {

    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String agg;

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {})
    public String unit;

    @FormField(ordinal = 2, type = FormFieldType.INT_NUMBER, validate = {Validator.integer})
    public Integer precision;

    public AggregationFunc parseAgg() {
        return AggregationFunc.valueOf(this.agg);
    }

    @TISExtension
    public static class DftDesc extends Descriptor<MeasureSpec> {
        public DftDesc() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "Measure Spec";
        }
    }
}