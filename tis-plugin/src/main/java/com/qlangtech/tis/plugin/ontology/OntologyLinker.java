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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.aiagent.llm.ITISJsonSchema;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.MultiStepsSupportHost;
import com.qlangtech.tis.extension.MultiStepsSupportHostDescriptor;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.manage.common.OptionWithEndType;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.linker.LinkResources;
import com.qlangtech.tis.plugin.ontology.impl.linker.RelationshipType;
import com.qlangtech.tis.plugin.ontology.impl.linker.RelationshipTypeBackingObjectType;
import com.qlangtech.tis.plugin.ontology.impl.linker.RelationshipTypeJoinTableDataset;
import com.qlangtech.tis.plugin.ontology.impl.linker.RelationshipTypeObjectTypeForeignKeys;
import com.qlangtech.tis.plugin.ontology.impl.linker.RelationshipTypeSetter;
import com.qlangtech.tis.util.DescriptorsJSONForAIPrompt;
import com.qlangtech.tis.util.DescriptorsMeta;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.qlangtech.tis.plugin.ontology.OntologyValueType.KEY_START_PERSISTENCE;

/**
 * 本体对象连接器
 * <a href="https://www.palantir.com/docs/foundry/object-link-types/create-link-type/">...</a>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/14
 * @see RelationshipTypeSetter
 * @see com.qlangtech.tis.plugin.ontology.impl.linker.LinkResources
 * @see RelationshipTypeObjectTypeForeignKeys
 * @see RelationshipTypeJoinTableDataset
 * @see RelationshipTypeBackingObjectType
 */
public class OntologyLinker extends Ontology implements IdentityName, MultiStepsSupportHost,
        IPluginStore.ManipuldateProcessor, IPluginStore.BeforePluginSaved {

    public static final String KEY_LINK_TYPES = "link-types";
    private OneStepOfMultiSteps[] stepsPlugin;

    public static List<OntologyLinker> loadAll(String ontologyName) {
        if (StringUtils.isEmpty(ontologyName)) {
            throw new IllegalArgumentException("param ontologyName can not be null");
        }
        return OntologyEnum.Linker.loadAll(OntologyPluginMeta.create(OntologyEnum.Linker, ontologyName));
    }

    public static OntologyLinker load(String ontologyName, String linkerIdName) {
        if (StringUtils.isEmpty(ontologyName)) {
            throw new IllegalArgumentException("param ontologyName can not be null");
        }
        if (StringUtils.isEmpty(linkerIdName)) {
            throw new IllegalArgumentException("param linkerIdName can not be null");
        }
        return OntologyEnum.Linker.load(OntologyPluginMeta.create(OntologyEnum.Linker, ontologyName).setPluginIdVal(linkerIdName));
    }

    @FormField(identity = true, ordinal = 0, validate = {Validator.require})
    public String name;

    public JSONObject serializeJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", this.name);
        obj.put("type", this.getLinkTypeEnd().getVal());
        obj.put("createTime", this.getCreate());
        return obj;
    }

    public IEndTypeGetter.EndType getLinkTypeEnd() {
        return getRelationTypeSetterStep().getRelationshipType().getEndType();
    }

    public boolean isConnectMatch(OntologyPluginMeta meta) {

        // OntologyPluginMeta meta = OntologyPluginMeta.createPluginMeta(pluginContext.getContext());
        LinkResources.ObjectLinkerPair links = getLinkResourcesStep().getLinks();
        return links.isConnectMatch(meta);


    }

    public LinkResources getLinkResourcesStep() {
        if (stepsPlugin.length != 2) {
            throw new IllegalStateException("stepsPlugin.length:" + stepsPlugin.length + " length must 2");
        }
        return (LinkResources) stepsPlugin[1];
    }

    /**
     *
     * @return
     */
    public OntologyObjectType getTargetLinkerEnd(OntologyPluginMeta meta) {
        LinkResources.ObjectLinkerPair links = getLinkResourcesStep().getLinks();
        return links.getOtherEnd(meta);
    }


    private RelationshipTypeSetter getRelationTypeSetterStep() {
        if (stepsPlugin.length != 2) {
            throw new IllegalStateException("stepsPlugin.length:" + stepsPlugin.length + " length must 2");
        }
        return (RelationshipTypeSetter) stepsPlugin[0];
    }

    @Override
    public String identityValue() {
        return this.name;
    }

    @Override
    public void setSteps(OneStepOfMultiSteps[] stepsPlugin) {
        this.stepsPlugin = Objects.requireNonNull(stepsPlugin, "stepsPlugin can not be null");
    }

    @Override
    public OneStepOfMultiSteps[] getMultiStepsSavedItems() {
        return this.stepsPlugin;
    }

    @Override
    public void beforeSaved(IPluginContext pluginContext, Optional<Context> context) {
        for (OneStepOfMultiSteps step : getMultiStepsSavedItems()) {
            if (step instanceof LinkResources linkResources) {
                this.name = linkResources.getLinkIdentityName().identityValue();
                return;
            }
        }
        throw new IllegalStateException("have not find any LinkResources");
    }

    @Override
    public void manipuldateProcess(IPluginContext pluginContext, UploadPluginMeta pluginMeta,
                                   Optional<Context> context) {
        // 进行持久化
        IPluginStore<OntologyLinker> valTypeStore =
                OntologyEnum.Linker.getPluginStore(
                        OntologyPluginMeta.createPluginMeta( //
                                pluginMeta.putExtraParams(KEY_START_PERSISTENCE, Boolean.TRUE.toString())
                                        .putExtraParams(IdentityName.PLUGIN_IDENTITY_NAME,
                                                Objects.requireNonNull(this.identityValue(), "id can not be null")))).unsaveCast();

        //        = ONTOLOGY_VALUE_TYPE.getPluginStore(pluginContext,
        //                pluginMeta.putExtraParams(KEY_START_PERSISTENCE,
        //                                Boolean.TRUE.toString())
        //                        .putExtraParams(IdentityName.PLUGIN_IDENTITY_NAME,
        //                                this.getMeta().name));

        valTypeStore.setPlugins(pluginContext, context,
                Collections.singletonList(new Descriptor.ParseDescribable<>(this)));
    }

    @TISExtension
    public static class DefaultDesc extends Ontology.BasicDesc implements MultiStepsSupportHostDescriptor<OntologyLinker> {
        public DefaultDesc() {
            super();
        }

        @Override
        public List<List<ITISJsonSchema>> generateMultiStepsSchemaForAIPrompt() {

            List<List<ITISJsonSchema>> result = Lists.newArrayList();
            for (RelationshipType relationType : RelationshipType.values()) {

                List<ITISJsonSchema> oneOfSteps = Lists.newArrayList();
                DescriptorsJSONForAIPrompt<?> inner //
                        =
                        new DescriptorsJSONForAIPrompt<>(Collections.singletonList(new RelationshipTypeSetter.Desc())
                                , false,
                                (builder, descriptor) -> {
                                },
                                (attr, addedProp) -> {
                                    if (StringUtils.equals(attr.getFieldKey(),
                                            RelationshipTypeSetter.KEY_RELATIONSHIP_TYPE)) {
                                        addedProp.setConst(relationType.getToken());
                                        // skip
                                        return true;
                                    }
                                    return false;
                                });

                DescriptorsMeta innerMeta = inner.getDescriptorsJSON();
                oneOfSteps.add(innerMeta.getFirstPluginJsonSchema());


                inner = new DescriptorsJSONForAIPrompt<>(Collections.singletonList(relationType.getLinkResourceDesc())
                        , false);
                innerMeta = inner.getDescriptorsJSON();
                oneOfSteps.add(innerMeta.getFirstPluginJsonSchema());

                result.add(oneOfSteps);
            }

            return result;
        }

        @Override
        protected OntologyEnum getOntologyType() {
            return OntologyEnum.Linker;
        }

        @Override
        public EndType getEndType() {
            return EndType.OntologyLink;
        }

        @Override
        public String getDisplayName() {
            return "Link Type";
        }

        @Override
        public Class<OntologyLinker> getHostClass() {
            return OntologyLinker.class;
        }

        @Override
        public List<OneStepOfMultiSteps.BasicDesc> getStepDescriptionList() {
            return List.of(new RelationshipTypeSetter.Desc(), new RelationshipTypeObjectTypeForeignKeys.DefDesc());
        }

        @Override
        public void appendExternalProps(JSONObject multiStepsCfg) {

        }


        @Override
        public String shortComment() {
            return "定义本体对象类型间的关联关系（Link Type）";
        }
    }
}
