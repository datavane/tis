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
package com.qlangtech.tis.plugin.ontology.impl.role;

import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.PropertyRoleType;
import com.qlangtech.tis.plugin.ontology.SemanticRole;
import com.qlangtech.tis.plugin.ontology.impl.aggregation.AggregationKind;

import java.util.List;

/**
 * 度量（Measure）角色。承载 derived property 配置——多跳 link 链路 + 聚合方式 + 单位/精度。
 * 设计参考 Palantir Foundry derived properties。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/10
 */
public class MeasureRole extends PropertyRoleType {

    /**
     * Linked objects 链路，最多 3 跳。
     */
    @FormField(ordinal = 0, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
    public List<OntologyLinkerRef> linkers;

    /**
     * 在链路末端 objectType 上施加的聚合方式。
     */
    @FormField(ordinal = 1, validate = {Validator.require})
    public AggregationKind aggregation;

    @FormField(ordinal = 2, type = FormFieldType.INPUTTEXT, validate = {})
    public String unit;

    @FormField(ordinal = 3, type = FormFieldType.INT_NUMBER, validate = {Validator.integer})
    public Integer precision;

    public List<OntologyLinkerRef> getLinkers() {
        return linkers;
    }

    public AggregationKind getAggregation() {
        return aggregation;
    }

    public String getUnit() {
        return unit;
    }

    public Integer getPrecision() {
        return precision;
    }

    @Override
    public SemanticRole kind() {
        return SemanticRole.Measure;
    }

    @TISExtension
    public static class DftDesc extends BasicDesc {
        @Override
        public String getDisplayName() {
            return SemanticRole.Measure.getLabel();
        }
    }
}