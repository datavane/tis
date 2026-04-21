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
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

/**
 * 本体对象连接器
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/14
 */
public class OntologyLinker implements Describable<OntologyLinker>, IdentityName {

    public static final String KEY_LINK_TYPES = "link-types";

    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.identity_strict})
    public String name;

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String sourceType;

    @FormField(ordinal = 2, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String targetType;

    @FormField(ordinal = 3, type = FormFieldType.TEXTAREA)
    public String description;

    @Override
    public String identityValue() {
        return this.name;
    }

    @TISExtension
    public static class DefaultDesc extends Descriptor<OntologyLinker> {
        public DefaultDesc() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "OntologyLinker";
        }
    }
}
