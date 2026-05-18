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
import com.qlangtech.tis.plugin.annotation.Validator;

/**
 * Object type foreign keys
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/3
 */
public class RelationshipTypeObjectTypeForeignKeys extends LinkResources {
    /**
     * 事实表（1 or n）
     */
    @FormField(ordinal = 0, validate = {Validator.require})
    public LinkReference left;

    /**
     * 维表（1）
     */
    @FormField(ordinal = 2, validate = {Validator.require})
    public LinkReference right;

    @Override
    public IdentityName getLinkIdentityName() {
        return IdentityName.create(left.getObjectType() + "_join_" + right.getObjectType());
    }

    @Override
    public ObjectLinkerPair getLinks() {
        return new ObjectLinkerPair(
                new ObjectLinkInfo(
                        left.getObjectType(), left.targetField,
                        right.getObjectType(), right.targetField,
                        Cardinality.MANY_ONE),
                new ObjectLinkInfo(
                        left.getObjectType(), left.targetField,
                        right.getObjectType(), right.targetField,
                        Cardinality.MANY_ONE));
    }

    @TISExtension
    public static class DefDesc extends BasicLinkResourceDesc {
        public DefDesc() {
            super();
        }

        @Override
        public String getStepDescription() {
            return "Object type foreign keys";
        }
    }
}