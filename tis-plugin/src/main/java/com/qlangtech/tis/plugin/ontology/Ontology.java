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
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.storegetter.BaiscAssistStoreGetter;
import com.qlangtech.tis.plugin.ontology.impl.storegetter.IAssistStoreGetter;
import com.qlangtech.tis.plugin.ontology.impl.storegetter.OntologyObjectTypeStoreGetter;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TIS 本体
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/25
 */
public abstract class Ontology implements Describable<Ontology> {
    public static final String KEY_ONTOLOGY = "ontology-type";
    /**
     * 创建或者修改时间
     */
    private transient long create;
    @TISExtension
    public static final HeteroEnum<Ontology> ONTOLOGY = new HeteroEnum<>(//
            Ontology.class, //
            "ontology", // },
            "本体", Selectable.Single, false) {
        @SuppressWarnings("all")
        @Override
        public IPluginStore<Ontology> getPluginStore(IPluginContext pluginContext,
                                                     UploadPluginMeta meta) {

            OntologyEnum ontologyEnum = OntologyEnum.parse(meta);

            OntologyPluginMeta pluginMeta = OntologyPluginMeta.createPluginMeta(meta);
            return ontologyEnum.getPluginStore(pluginMeta);
        }
    };

    public enum OntologyEnum {

        ObjectType(OntologyObjectType.KEY_OBJECT_TYPE, new OntologyObjectTypeStoreGetter()),
        ValueType(OntologyValueType.KEY_VALUE_TYPE //
                , new BaiscAssistStoreGetter<OntologyValueType>() {
            @Override
            protected File getAssistRootDir(String ontologyName) {
                return OntologyDomain.getValueTypeDir(ontologyName);
            }
        }),
        Linker(OntologyLinker.KEY_LINK_TYPES //
                , new BaiscAssistStoreGetter<OntologyLinker>() {
            @Override
            protected File getAssistRootDir(String ontologyName) {
                return OntologyDomain.getLinkTypeDir(ontologyName);
            }
        }),
        SharedProperty(OntologySharedProperty.KEY_SHARED_PROPERTY,
                new BaiscAssistStoreGetter<OntologySharedProperty>() {
                    @Override
                    public IPluginStore<OntologySharedProperty> getPluginStore(OntologyPluginMeta pluginMeta) {
                        return super.getPluginStore(pluginMeta.setPersistence());
                    }

                    @Override
                    protected File getAssistRootDir(String ontologyName) {
                        return OntologyDomain.getSharedPropsDir(ontologyName);
                    }
                }),
        Glossary(OntologyGlossary.KEY_GLOSSARY,
                new BaiscAssistStoreGetter<OntologyGlossary>() {
                    @Override
                    protected File getAssistRootDir(String ontologyName) {
                        return OntologyDomain.getGlossaryDir(ontologyName);
                    }
                });

        public static OntologyEnum parse(UploadPluginMeta meta) {
            String ontology = meta.getExtraParam(KEY_ONTOLOGY);
            if (StringUtils.isEmpty(ontology)) {
                throw new IllegalArgumentException("illegal param ontology can not be empty");
            }
            for (OntologyEnum type : OntologyEnum.values()) {
                if (ontology.equalsIgnoreCase(type.typeIdentity)) {
                    return type;
                }
            }
            throw new IllegalStateException("can not find ontology:" + ontology + " relevant type");
        }

        private final String typeIdentity;
        private final IAssistStoreGetter<?> storeKeyGetter;

        private OntologyEnum(String typeIdentity, IAssistStoreGetter<?> storeKeyGetter) {
            this.typeIdentity = typeIdentity;
            this.storeKeyGetter = storeKeyGetter;
        }

        public String getTypeIdentity() {
            return typeIdentity;
        }

        @SuppressWarnings("all")
        public IPluginStore<Ontology> getPluginStore(OntologyPluginMeta meta) {
            return (IPluginStore<Ontology>) this.storeKeyGetter.getPluginStore(meta);
        }

        @SuppressWarnings("all")
        public <T extends Ontology> List<T> loadAll(OntologyPluginMeta meta) {
            return (List<T>) this.storeKeyGetter.loadAll(meta.getDomain());
        }

    }

    public long getCreate() {
        return this.create;
    }

    @SuppressWarnings("all")
    public <T extends Ontology> T setCreate(long create) {
        this.create = create;
        return (T) this;
    }

    @Override
    public Descriptor<Ontology> getDescriptor() {
        Descriptor<Ontology> desc = Objects.requireNonNull(Describable.super.getDescriptor(),
                this.getClass().getName() + " relevant desc can not be null");
        if (!(desc instanceof BasicDesc)) {
            throw new IllegalStateException("desc:" + desc.getClass().getName() + " must type of " + BasicDesc.class.getName());
        }
        return desc;
    }

    protected static abstract class BasicDesc extends Descriptor<Ontology> implements IEndTypeGetter {
        public BasicDesc() {
            super();
        }

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = super.getExtractProps();
            eprops.put(KEY_ONTOLOGY, getOntologyType().getTypeIdentity());
            return eprops;
        }

        protected abstract OntologyEnum getOntologyType();
    }

}