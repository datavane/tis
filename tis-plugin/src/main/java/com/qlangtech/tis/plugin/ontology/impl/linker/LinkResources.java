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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.datax.transformer.PluginLiteria;
import com.qlangtech.tis.plugin.ontology.Ontology;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.plugin.ontology.OntologyUtils;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/3
 */
public abstract class LinkResources extends OneStepOfMultiSteps implements PluginLiteria {

    @Override
    protected final Class<? extends OneStepOfMultiSteps.BasicDesc> getBasicDescClass() {
        return BasicLinkResourceDesc.class;
    }

    /**
     * 提取连接可使用的唯一名称，用于鉴别连接的唯一名称，防止重复创建连接
     *
     * @return
     */
    public abstract IdentityName getLinkIdentityName();

    public abstract ObjectLinkerPair getLinks();

    public record ObjectLinkerPair(ObjectLinkInfo left, ObjectLinkInfo right) {

        public Stream<LinkResources.ObjectLinkInfo> stream() {
            return Stream.of(left, right);
        }

        public boolean isConnectMatch(OntologyPluginMeta meta) {
            if (this.left.isSourceConnect(meta)) {
                return true;
            }
            return this.right.isTargetConnect(meta);
        }

        public OntologyObjectType getOtherEnd(OntologyPluginMeta meta) {
            if (this.left.isSourceConnect(meta)) {
                return Ontology.loadObjectTypeDetail(meta.getDomain(), this.right.target);
            }
            if (this.right.isTargetConnect(meta)) {
                return Ontology.loadObjectTypeDetail(meta.getDomain(), this.left.source);
            }

            throw new IllegalStateException(this.toString() + " can not match objType:" + meta.getObjectType());
        }

        @Override
        public String toString() {
            return "{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

    /**
     * join 的节点 left 作为 source 端，right 作为 target 端；同时携带连接列与 cardinality，便于 ChatBI/SQL 生成阶段直接使用。
     */
    public record ObjectLinkInfo(String source, String sourceField,
                                 String target, String targetField,
                                 Cardinality cardinality) {
        public ObjectLinkInfo(String source, String sourceField, String target, String targetField,
                              Cardinality cardinality) {
            this.source = Objects.requireNonNull(source);
            this.sourceField = sourceField;
            this.target = Objects.requireNonNull(target);
            this.targetField = targetField;
            this.cardinality = cardinality;
        }

        public boolean contain(String objType) {
            return StringUtils.equals(objType, this.source) || StringUtils.equals(objType, this.target);
        }

        public boolean isSourceConnect(OntologyPluginMeta meta //OntologyObjectType objType, OntologyProperty fromProp
        ) {
            String objType = meta.getObjectType();
            // String ontologyPropName = meta.getObjectTypeProperty();
            return this.source.equals(objType);// && this.sourceField.equals(ontologyPropName);
        }

        public boolean isTargetConnect(OntologyPluginMeta meta //OntologyObjectType objType, OntologyProperty fromProp
        ) {
            String objType = meta.getObjectType();
            // String ontologyPropName = meta.getObjectTypeProperty();
            return this.target.equals(objType);// && this.sourceField.equals(ontologyPropName);
        }

        @Override
        public String toString() {
            return "LinkInfo{" +
                    "source='" + source + '\'' +
                    ", sourceField='" + sourceField + '\'' +
                    ", target='" + target + '\'' +
                    ", targetField='" + targetField + '\'' +
                    ", cardinality=" + cardinality +
                    '}';
        }
    }

    // @TISExtension
    public static abstract class BasicLinkResourceDesc extends OneStepOfMultiSteps.BasicDesc implements DescriptorUseableShortComment {

        public abstract RelationshipType getRelationShipType();

        @Override
        public Step getStep() {
            return Step.Step2;
        }

        @Override
        public Optional<BasicDesc> nextPluginDesc(OneStepOfMultiSteps current) {
            return Optional.empty();
        }

        @Override
        protected final boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            LinkResources linkResource = postFormVals.newInstance();
            return OntologyUtils.validateIdentityDuplicate(msgHandler, context, Optional.empty(),
                    Ontology.OntologyEnum.Linker, linkResource.getLinkIdentityName().identityValue());
        }

        @Override
        public boolean isFinalStep() {
            return true;
        }

        @Override
        public final String shortComment() {
            return getStepDescription();
        }

        @Override
        public final String getStepDescription() {
            return getRelationShipType().getName();
        }
    }
}