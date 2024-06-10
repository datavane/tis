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

package com.qlangtech.tis.plugin.datax.transformer;

import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.datax.transformer.impl.CopyValUDF;
import com.qlangtech.tis.plugin.ds.DataType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 为一条记录Record定义的 Transformer 转化规则
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-05-07 12:35
 **/
public class RecordTransformerRules implements Describable<RecordTransformerRules>, IdentityName {

    @FormField(ordinal = 0, identity = true, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String name;

    @FormField(ordinal = 1, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
    public List<RecordTransformer> rules = Lists.newArrayList();

    @Override
    public String identityValue() {
        return this.name;
    }

    public static List<RecordTransformer> getRules() {
//        RecordTransformer t = new RecordTransformer();
//
//        t.setType(DataType.createVarChar(32));
//        t.setTarget(StringUtils.EMPTY);
////        CopyValUDF cpUdf = new CopyValUDF();
////        cpUdf.from = "name";
//        t.setUdf(null);
        return Lists.newArrayList();
    }


    @TISExtension
    public static class DefaultDescriptor extends Descriptor<RecordTransformerRules> {
        public DefaultDescriptor() {
            super();
        }
    }

}
