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
import com.qlangtech.tis.manage.common.OptionWithEndType;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.linker.LinkReference;

import java.util.List;

import static com.qlangtech.tis.plugin.ontology.impl.linker.LinkReference.KEY_TARGET_FIELD;

/**
 * 指向某个 ObjectType.Property。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/9
 * @see LinkReference
 */
public class GlossaryTargetProperty extends GlossaryTarget {

    //    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    //    public String objectType;
    //
    //    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    //    public String propertyName;
    //    @FormField(ordinal = 0, validate = {Validator.require})
    //    public LinkReference reference;


    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String objectType;


    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    public String targetField;

    @Override
    public final String getTargetLiteral() {
        return objectType + "." + targetField;
    }

    @TISExtension
    public static class DftDesc extends BasicDesc {
        public DftDesc() {
            super();
            this.valueChangePipe(LinkReference.KEY_OBJECT_TYPE, LinkReference.KEY_TARGET_FIELD)
                    .render((pluginMeta, param) -> {
                        // JSONObject result = new JSONObject();
                        OntologyPluginMeta meta = OntologyPluginMeta.createPluginMeta(pluginMeta);

                        OntologyObjectType objectType = OntologyObjectType.loadDetail(meta.getDomain(),
                                param.getString(LinkReference.KEY_OBJECT_TYPE));

                        return objectType.getColOpts();
                    });
        }

        @Override
        public String getDisplayName() {
            return "Property Reference";
        }

        @Override
        public EndType getEndType() {
            return EndType.OntologyProperty;
        }

        @Override
        public String shortComment() {
            return "术语指向对象类型的属性字段";
        }
    }
}