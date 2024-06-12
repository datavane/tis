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

package com.qlangtech.tis.plugin.datax.transformer.impl;

import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.datax.transformer.UDFDefinition;
import com.qlangtech.tis.plugin.datax.transformer.UDFDesc;
import com.qlangtech.tis.plugin.datax.transformer.jdbcprop.TargetColType;

import java.util.List;

/**
 * 将某列JSON内容拆解成多个字段
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-11 14:38
 **/
public class JSONSplitterUDF extends AbstractFromColumnUDFDefinition {

    @FormField(ordinal = 2, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
    public List<TargetColType> to;

    @Override
    public List<UDFDesc> getLiteria() {
        List<UDFDesc> result = Lists.newArrayList(super.getLiteria());

        List<UDFDesc> toDesc = Lists.newArrayList();
        this.to.forEach((colType) -> {
            toDesc.addAll(colType.getLiteria());
        });
        result.add(new UDFDesc("to", toDesc));
        return result;
    }

    public static List<TargetColType> getCols() {
        return Lists.newArrayList();
    }

    @TISExtension
    public static final class DefaultDescriptor extends Descriptor<UDFDefinition> {
        public DefaultDescriptor() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "JSON Splitter";
        }
    }

}
