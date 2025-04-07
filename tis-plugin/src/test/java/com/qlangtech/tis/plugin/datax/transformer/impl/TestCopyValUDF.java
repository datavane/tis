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

import com.alibaba.datax.common.element.ColumnAwareRecord;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.datax.transformer.InParamer;
import com.qlangtech.tis.plugin.datax.transformer.OutputParameter;
import com.qlangtech.tis.plugin.datax.transformer.UDFDefinition;
import com.qlangtech.tis.plugin.datax.transformer.UDFDesc;
import com.qlangtech.tis.plugin.datax.transformer.jdbcprop.TargetColType;
import com.qlangtech.tis.plugin.ds.CMeta;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于使用虚拟列
 */
public class TestCopyValUDF extends UDFDefinition {

    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    public String from;
    @FormField(ordinal = 2, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
    public TargetColType to;

    @Override
    public List<UDFDesc> getLiteria() {
        List<UDFDesc> literia = Lists.newArrayList(new UDFDesc("from", this.from));
        literia.add(new UDFDesc("to", to.getLiteria()));
        return literia;
    }

    @Override
    public List<InParamer> inParameters() {
        return Collections.singletonList(InParamer.create(this.from));
    }

    public static List<IdentityName> colsCandidate() {
        List<CMeta> colsCandidate = SelectedTab.getColsCandidate();
        return colsCandidate.stream().collect(Collectors.toList());
    }



    public static List<TargetColType> getCols() {
        return Lists.newArrayList();
    }

    @Override
    public List<OutputParameter> outParameters() {
        return Collections.singletonList(TargetColType.create(this.to));
    }

    @Override
    public void evaluate(ColumnAwareRecord record) {

        record.setColumn(this.to.getName(), record.getColumn(this.from));
    }


    @TISExtension
    public static final class DefaultDescriptor extends Descriptor<UDFDefinition> {
        public DefaultDescriptor() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "TestCopyVal";
        }
    }
}
