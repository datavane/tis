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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.datax.transformer.TargetColumn;
import com.qlangtech.tis.plugin.datax.transformer.UDFDesc;
import com.qlangtech.tis.plugin.ds.ContextParamConfig;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-09 13:44
 **/
public class VirtualTargetColumn extends TargetColumn {

    @FormField(ordinal = 1, identity = true, type = FormFieldType.INPUTTEXT, validate = {Validator.require //, Validator.db_col_name
    })
    public String name;

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String identityValue() {
        return this.getName();
    }

    @Override
    public List<UDFDesc> getLiteria() {
        return Collections.singletonList(new UDFDesc("col", this.name));
    }

    @TISExtension
    public static class VirtualTargetColumnDesc extends Descriptor<TargetColumn> {
        public VirtualTargetColumnDesc() {
            super();
        }

        public boolean validateName(IFieldErrorHandler msgHandler, Context context, String fieldName, String value) {

            if (StringUtils.startsWith(value, ContextParamConfig.CONTEXT_BINDED_KEY_PREFIX)) {
                value = StringUtils.substring(value, 1);
            }

            return Validator.db_col_name.getFieldValidator().validate(msgHandler, context, fieldName, value);
        }

        @Override
        public String getDisplayName() {
            return "Virtual Column";
        }
    }

}
