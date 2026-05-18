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

import com.google.common.collect.Lists;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.IMultiElement;
import com.qlangtech.tis.plugin.ontology.OntologyLinker;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.util.IPluginContext;

import java.util.List;

/**
 * 对 {@link OntologyLinker} 的轻量引用——只保存 link type 名字。
 * Derived property 的多跳链路通过持有多个 ref 实现。
 * <p>
 * Note:目前前端先使用一跳的方式实现，以后有需要再实现多跳
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/10
 * @see MeasureRole
 * @see OntologyLinkerRefCreatorFactory
 */
public class OntologyLinkerRef implements IMultiElement {

    public static final String KEY_DERIVED_LINKER_NAME = "derivedLinkerName";
    static final String  KEY_LINK_TYPE_NAME = "linkerName";

    // @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String linkerName;

    public String getLinkerName() {
        return linkerName;
    }

    public void setLinkerName(String linkerName) {
        this.linkerName = linkerName;
    }

    public OntologyLinker resolve(String ontologyName) {
        for (OntologyLinker linker : OntologyLinker.loadAll(ontologyName)) {
            if (linker.identityValue().equals(this.linkerName)) {
                return linker;
            }
        }
        throw new IllegalStateException("linker not found: " + this.linkerName);
    }

    public static List<OntologyLinker> getLinkerOpts() {

        IPluginContext pluginContext = IPluginContext.getThreadLocalInstance();
        OntologyPluginMeta meta = OntologyPluginMeta.createPluginMeta(pluginContext.getContext());
//        meta.getObjectType();
//        meta.getObjectTypeProperty();
        List<OntologyLinker> result = Lists.newArrayList();
        for (OntologyLinker linker : OntologyLinker.loadAll(meta.getDomain())) {
            if (linker.isConnectMatch(meta)) {
                result.add(linker);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return this.linkerName;
    }
}