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

package com.qlangtech.tis.plugin.ontology.impl.linker;

import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.plugin.ontology.OntologyProperty;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.util.IPluginContext;

import java.util.List;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/3
 */
public class RelationshipTypeBackingObjectType extends LinkResources {

    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String leftObjectType;

    @FormField(ordinal = 2, type = FormFieldType.ENUM, validate = {Validator.require})
    public String rightObjectType;

    @FormField(ordinal = 3, validate = {Validator.require})
    public LinkReference joinObjectType;

    @Override
    public IdentityName getLinkIdentityName() {
        return IdentityName.create((leftObjectType) + "_join_" + (rightObjectType) +
                "_with_" + joinObjectType.getObjectType());
    }

    @Override
    public List<ObjectLinkInfo> getLinks() {
        if (!(joinObjectType instanceof JoinReference jr)) {
            throw new IllegalStateException(
                    "joinObjectType must be type of " + JoinReference.class.getName()
                            + " but actual is " + joinObjectType.getClass().getName());
        }
        return List.of(
                new ObjectLinkInfo(
                        leftObjectType, inferPk(leftObjectType),
                        jr.getObjectType(), jr.targetField,
                        Cardinality.ONE_MANY),
                new ObjectLinkInfo(
                        jr.getObjectType(), jr.rightTargetField,
                        rightObjectType, inferPk(rightObjectType),
                        Cardinality.ONE_MANY));
    }

    private static String inferPk(String otName) {
        IPluginContext pluginContext = IPluginContext.getThreadLocalInstance();
        OntologyPluginMeta meta = OntologyPluginMeta.createPluginMeta(pluginContext.getContext());
        OntologyObjectType ot = OntologyObjectType.loadDetail(meta.getDomain(), otName);
        for (OntologyProperty col : ot.getCols()) {
            if (Boolean.TRUE.equals(col.pk)) {
                return col.getName();
            }
        }
        throw new IllegalStateException("object-type '" + otName + "' has no PK column");
    }

    @TISExtension
    public static class DefDesc extends BasicLinkResourceDesc {
        public DefDesc() {
            super();
        }

        @Override
        public String getStepDescription() {
            return "Backing object type";
        }
    }
}