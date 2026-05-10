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

import java.util.List;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/3
 */
public class RelationshipTypeJoinTableDataset extends LinkResources {

    @FormField(ordinal = 0, validate = {Validator.require})
    public LinkReference left;

    @FormField(ordinal = 2, validate = {Validator.require})
    public LinkReference right;

    @FormField(ordinal = 3, validate = {Validator.require})
    public LinkReference join;

    public static List<LinkReference.DftDesc> linkRefDescFilter(List<LinkReference.DftDesc> descs, boolean isJoin) {
        return descs.stream().filter((desc) -> {
            if (isJoin) {
                return desc instanceof JoinReference.JoinDesc;
            } else {
                return desc != null;
            }
        }).toList();
    }

    @Override
    public List<ObjectLinkInfo> getLinks() {
        return List.of(
                new ObjectLinkInfo(
                        left.getObjectType(), left.targetField,
                        join.getObjectType(), join.targetField,
                        Cardinality.ONE_MANY),
                new ObjectLinkInfo(
                        join.getObjectType(), join.targetField,
                        right.getObjectType(), right.targetField,
                        Cardinality.ONE_MANY));
    }

    @Override
    public IdentityName getLinkIdentityName() {
        return IdentityName.create(left.getObjectType() + "_join_" + right.getObjectType() + "_with_" + join.getObjectType());
    }

    @TISExtension
    public static class DefDesc extends BasicLinkResourceDesc {
        public DefDesc() {
            super();
        }

        @Override
        public String getStepDescription() {
            return "Join table dataset";
        }
    }
}