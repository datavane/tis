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
package com.qlangtech.tis.plugin.ontology.impl.glossary;

import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

/**
 * 自定义指标 SQL 表达式 —— ChatBI 把它直接拼到 SELECT 中。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/9
 */
public class GlossaryTargetMetricExpr extends GlossaryTarget {

    @FormField(ordinal = 0, type = FormFieldType.TEXTAREA, validate = {Validator.require})
    public String sql;

    @Override
    public String getTargetLiteral() {
        return sql;
    }

    @TISExtension
    public static class DftDesc extends BasicDesc {
        public DftDesc() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "Metric Expression";
        }

        @Override
        public EndType getEndType() {
            return EndType.OntologyMetric;
        }

        @Override
        public String shortComment() {
            return "术语绑定自定义SQL指标表达式";
        }
    }
}