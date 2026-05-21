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
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.DefaultDataXProcessorManipulate;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.IDescribableManipulate;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.qlangtech.tis.plugin.ontology.OntologyObjectType.KEY_OBJECT_TYPE;

/**
 * 本体
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/14
 */
public final class OntologyDomain implements Describable<OntologyDomain>, IdentityName, IPluginStore.BeforePluginSaved {

    public static final String KEY_SHARED_PROPERTIES = "shared-property";

    public static final String NAME_ONTOLOGY_DOMAIN = "ontology";

    public long updateTime;

    @TISExtension
    public static final HeteroEnum<OntologyDomain> ONTOLOGY_DOMAIN = new HeteroEnum<>(//
            OntologyDomain.class, //
            "ontology-domain", // },
            "本体域", Selectable.Single, false) {
        @SuppressWarnings("all")
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            String ontology = pluginMeta.getExtraParam(NAME_ONTOLOGY_DOMAIN);
            if (StringUtils.isEmpty(ontology)) {
                throw new IllegalStateException("param ontology can not be empty");
            }
            KeyedPluginStore.Key key = getStoreKey(ontology);
            return TIS.getPluginStore(key);
        }
    };

    public static Pair<OntologyDomain, IPluginStore<OntologyDomain>> load(String domain) {
        IPluginStore<OntologyDomain> domainStore = getOntologyDomainPluginStore(domain);
        return Pair.of(domainStore.getPlugin(), domainStore);
    }

    private static IPluginStore<OntologyDomain> getOntologyDomainPluginStore(String domain) {
        if (StringUtils.isEmpty(domain)) {
            throw new IllegalArgumentException("param doamin can not be null");
        }
        IPluginStore<OntologyDomain> domainStore
                = ONTOLOGY_DOMAIN.getPluginStore(null,
                UploadPluginMeta.create(ONTOLOGY_DOMAIN).putExtraParams(NAME_ONTOLOGY_DOMAIN, domain));
        return domainStore;
    }

    @SuppressWarnings("all")
    private static KeyedPluginStore.Key getStoreKey(String ontologyName) {
        KeyedPluginStore.Key key = new KeyedPluginStore.Key(ONTOLOGY_DOMAIN.getIdentity(), ontologyName
                , ONTOLOGY_DOMAIN.extensionPoint);
        return key;
    }

    public static List<OntologyDomain> getDoaminListWithoutStore() {
        return getDoaminList().stream().map(Pair::getKey).toList();
    }

    public static List<Pair<OntologyDomain, IPluginStore<OntologyDomain>>> getDoaminList() {
        File ontologyRoot = new File(TIS.pluginCfgRoot, OntologyDomain.ONTOLOGY_DOMAIN.getIdentity());

        List<Pair<OntologyDomain, IPluginStore<OntologyDomain>>> ontologyList = new ArrayList<>();

        if (ontologyRoot.exists()) {
            for (File ontology : Objects.requireNonNull(ontologyRoot.listFiles())) {
                if (ontology.isDirectory()) {
                    Pair<OntologyDomain, IPluginStore<OntologyDomain>> load = OntologyDomain.load(ontology.getName());
                    ontologyList.add(load);
                }
            }
        }
        return ontologyList;
    }

    /**
     * 获取某个domain下的Object type集合
     */
    public static List<OntologyObjectType> getObjectTypes(String ontologyDomain) {
        if (StringUtils.isEmpty(ontologyDomain)) {
            throw new IllegalArgumentException("param ontologyDomain can not be null");
        }
        File objectTypeDir = getObjectTypeDir(ontologyDomain);
        List<OntologyObjectType> objTypes = Lists.newArrayList();
        for (String ds : Objects.requireNonNull(objectTypeDir.list())) {
            File dsDir = new File(objectTypeDir, ds);
        }
        return objTypes;
    }

    private static File getDir(String ontologyName) {
        return (getStoreKey(ontologyName).getStoreXmlFile().getFile().getParentFile());
    }

    public static File getObjectTypeDir(String ontologyName) {
        return new File(getDir(ontologyName), KEY_OBJECT_TYPE);
    }

    public static File getLinkTypeDir(String ontologyName) {
        return new File(getDir(ontologyName), OntologyLinker.KEY_LINK_TYPES);
    }

    public static File getValueTypeDir(String ontologyName) {
        return new File(getDir(ontologyName), OntologyValueType.KEY_VALUE_TYPE);
    }

    public static File getSharedPropsDir(String ontologyName) {
        return new File(getDir(ontologyName), KEY_SHARED_PROPERTIES);
    }

    public static File getGlossaryDir(String ontologyName) {
        return new File(getDir(ontologyName), OntologyGlossary.KEY_GLOSSARY);
    }

    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.identity_strict})
    public String name;

    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    public boolean defaultDomain;

    @Override
    public String identityValue() {
        return this.name;
    }

    @Override
    public void beforeSaved(IPluginContext pluginContext, Optional<Context> context) {
        this.updateTime = System.currentTimeMillis();
        if (this.defaultDomain) {
            List<Pair<OntologyDomain, IPluginStore<OntologyDomain>>> doaminList = getDoaminList();
            for (Pair<OntologyDomain, IPluginStore<OntologyDomain>> pair : doaminList) {
                OntologyDomain old = pair.getKey();
                if (!StringUtils.equals(this.name, old.name) && old.defaultDomain) {
                    old.defaultDomain = false;
                    pair.getValue().setPlugins(null, Optional.empty(),
                            Collections.singletonList(new Descriptor.ParseDescribable<>(old)));
                }
            }
        }
    }

    public OntologyDomainPojo convertPojo() {
        return new OntologyDomainPojo(this.name, new Date(this.updateTime), this.defaultDomain);
    }

    @TISExtension
    public static class DefaultDesc extends Descriptor<OntologyDomain> implements DescriptorUseableShortComment, IDescribableManipulate<OntologyDomainManipulate> {
        public DefaultDesc() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "Ontology";
        }

        @Override
        public String shortComment() {
            return "定义本体顶层命名空间（域），用于隔离不同业务场景";
        }

        @Override
        public Class<OntologyDomainManipulate> getManipulateExtendPoint() {
            return OntologyDomainManipulate.class;
        }

        @Override
        public Optional<IPluginStore<OntologyDomainManipulate>> getManipulateStore() {
            return Optional.empty();
        }
    }

    public record OntologyDomainPojo(String name, Date updateTime, boolean defaultDomain) {
    }
}