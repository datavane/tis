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
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.glossary.GlossaryTarget;
import com.qlangtech.tis.plugin.ontology.impl.synonyms.SynonymsElement;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 业务术语 / 同义词字典。ChatBI NL→SQL 链路通过它把业务名词桥接到本体（OT/Property/SQL 表达式）。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/9
 */
public abstract class OntologyGlossary extends Ontology implements IPluginStore.ManipuldateProcessor {

    public static final String KEY_GLOSSARY = "ontology-glossary";

    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT,
            validate = {Validator.require, Validator.identity_strict})
    public String term;

    /**
     * 同义词列表（持久化为 List&lt;String&gt;）。
     *
     * // @see SynonymsElementCreatorFactory
     */
    @FormField(ordinal = 1, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
    public List<SynonymsElement> synonyms;

    @FormField(ordinal = 2, type = FormFieldType.TEXTAREA, validate = {Validator.require})
    public String description;

    @FormField(ordinal = 3, validate = {Validator.require})
    public GlossaryTarget target;

    public List<SynonymsElement> getSynonyms() {
        return CollectionUtils.isEmpty(synonyms) ? new ArrayList<>() : synonyms;
    }

    @Override
    public void manipuldateProcess(IPluginContext pluginContext, UploadPluginMeta pluginMeta,
                                   Optional<Context> context) {

    }

    public static OntologyGlossary load(OntologyPluginMeta meta, String idVal) {
        meta.setPluginIdVal(idVal);
        IPluginStore<OntologyGlossary> pluginStore =
                OntologyEnum.Glossary.getPluginStore(meta).unsaveCast();
        return Objects.requireNonNull(pluginStore.getPlugin(),
                "idVal:" + idVal + " relevant glossary can not be null");
    }

    @Override
    public String identityValue() {
        return this.term;
    }



}