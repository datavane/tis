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
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.datax.transformer.PluginLiteria;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.storegetter.BaiscAssistStoreGetter;
import com.qlangtech.tis.plugin.ontology.impl.storegetter.IAssistStoreGetter;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * TIS 本体
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/25
 */
@SuppressWarnings("all")
public abstract class Ontology implements Describable<Ontology>, IdentityName, IPluginStore.BeforePluginSaved,
        PluginLiteria {
    public static final String KEY_ONTOLOGY = "ontology-type";
    public static final String KEY_CREATE_TIME = "createTime";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TYPE = "type";

    /**
     * 创建或者修改时间
     */
    private long create;
    @TISExtension
    public static final HeteroEnum<Ontology> ONTOLOGY = new HeteroEnum<>(//
            Ontology.class, //
            "ontology", // },
            "本体", Selectable.Single, false) {
        @SuppressWarnings("all")
        @Override
        public IPluginStore<Ontology> getPluginStore(IPluginContext pluginContext,
                                                     UploadPluginMeta meta) {
            OntologyPluginMeta pluginMeta = OntologyPluginMeta.createPluginMeta(meta);
            OntologyEnum ontologyEnum = pluginMeta.getOntologyType();
            return ontologyEnum.getPluginStore(pluginMeta);
        }
    };

    public OntologyEnum ontologyType() {
        return ((BasicDesc) this.getDescriptor()).getOntologyType();
    }

    public IEndTypeGetter.EndType getEndType() {
        return this.ontologyType().endType;
    }

    /**
     * 删除一个ontology object type
     *
     * @param domain
     * @param objTypeName
     */
    public static void delete(OntologyEnum ontologyEnum, String domain, IdentityName objTypeName) {


        File storeFile = new File(
                ontologyEnum.getAssistRootDir(domain),
                objTypeName.identityValue() + XmlFile.KEY_XML_DOT_EXTENSION);
        try {
            FileUtils.forceDelete(storeFile);
        } catch (IOException e) {
            throw new RuntimeException("delete objectType file:" + storeFile.getAbsolutePath(), e);
        }
    }

    public static List<OntologyLinker> loadAllLinkers(String ontologyName) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(ontologyName)) {
            throw new IllegalArgumentException("param ontologyName can not be null");
        }
        return OntologyEnum.Linker.loadAll(OntologyPluginMeta.create(OntologyEnum.Linker, ontologyName));
    }

    public static List<OntologyGlossary> loadAllGlossary(String ontologyName) {

        return OntologyEnum.Glossary.loadAll(OntologyPluginMeta.create(Ontology.OntologyEnum.Glossary, ontologyName));
    }

    public static List<OntologySharedProperty> loadAllSharedProperties(String ontologyDomainName) {
        return loadAllSharedProperties(OntologyPluginMeta.create(OntologyEnum.SharedProperty, ontologyDomainName));
    }

    /**
     * 加载某一个本体域中的Object Type
     *
     * @param meta
     * @return
     */
    public static List<OntologySharedProperty> loadAllSharedProperties(OntologyPluginMeta meta) {
        return OntologyEnum.SharedProperty.loadAll(meta);
        // throw new NotImplementedException();
        // return Collections.emptyList();
    }

    public static OntologyObjectType loadObjectTypeDetail(String ontologyName, String objType) {
        return OntologyEnum.ObjectType.load(OntologyPluginMeta.create(OntologyEnum.ObjectType, ontologyName).setPluginIdVal(objType));
    }

    /**
     * 加载某一个本体域中的Object Type
     *
     * @param ontologyName
     * @return
     */
    public static List<OntologyValueType> loadAllValueTypes(String ontologyName) {
        if (StringUtils.isEmpty(ontologyName)) {
            throw new IllegalArgumentException("param ontologyName can not be empty");
        }
        return OntologyEnum.ValueType.loadAll(OntologyPluginMeta.create(OntologyEnum.ValueType, ontologyName));
        //  return objectTypes;
    }

    // ================================================================
    //  Neo4j 图谱统计（由 tis-ontology-plugin 运行时注册）
    // ================================================================

    /**
     * 各类节点/关系数量及最后同步时间。
     */
    public record OntologyGraphStats(
            long objectTypeCount,
            long propertyCount,
            long linkerCount,
            long glossaryCount,
            long sharedPropertyCount,
            long valueTypeCount,
            long lastSyncAt   // Neo4j timestamp() 毫秒，0 表示无数据
    ) {
    }

    @FunctionalInterface
    public interface GraphStatsProvider {
        OntologyGraphStats getStats(String domain);
    }

    private static volatile GraphStatsProvider _graphStatsProvider;

    /**
     * tis-ontology-plugin 初始化时注册（在 OntologyNeo4jSyncService.getInstance() 中调用）。
     */
    public static void registerGraphStatsProvider(GraphStatsProvider p) {
        _graphStatsProvider = p;
    }

    /**
     * 查询 domain 下的 Neo4j 统计；Neo4j 未启动时返回 empty。
     */
    public static Optional<OntologyGraphStats> queryGraphStats(String domain) {
        OntologyDomainManipulate.getManipulateStore(domain, false) //
                .getManipulates().forEach(OntologyDomainManipulate::initialize);
        return Optional.of(Objects.requireNonNull(_graphStatsProvider, "_graphStatsProvider can not be null").getStats(domain));
    }

    // ================================================================

    public enum OntologyEnum {

        ObjectType(OntologyObjectType.KEY_OBJECT_TYPE //
                , IEndTypeGetter.EndType.OntologyObjectType,
                new BaiscAssistStoreGetter<OntologyObjectType>() {
                    @Override
                    public File getAssistRootDir(String ontologyName) {
                        return OntologyDomain.getObjectTypeDir(ontologyName);
                        //  return objectTypeDir;
                    }
                }),
        ValueType(OntologyValueType.KEY_VALUE_TYPE //
                ,
                IEndTypeGetter.EndType.OntologyValueType
                , new BaiscAssistStoreGetter<OntologyValueType>() {
            @Override
            public File getAssistRootDir(String ontologyName) {
                return OntologyDomain.getValueTypeDir(ontologyName);
            }
        }),
        Linker(OntologyLinker.KEY_LINK_TYPES //
                , IEndTypeGetter.EndType.OntologyLink
                , new BaiscAssistStoreGetter<OntologyLinker>() {
            @Override
            public File getAssistRootDir(String ontologyName) {
                return OntologyDomain.getLinkTypeDir(ontologyName);
            }
        }),
        SharedProperty(OntologySharedProperty.KEY_SHARED_PROPERTY,
                IEndTypeGetter.EndType.Shared,
                new BaiscAssistStoreGetter<OntologySharedProperty>() {
                    @Override
                    public IPluginStore<OntologySharedProperty> getPluginStore(OntologyPluginMeta pluginMeta) {
                        return super.getPluginStore(pluginMeta.setPersistence());
                    }

                    @Override
                    public File getAssistRootDir(String ontologyName) {
                        return OntologyDomain.getSharedPropsDir(ontologyName);
                    }
                }),
        Glossary(OntologyGlossary.KEY_GLOSSARY
                , IEndTypeGetter.EndType.OntologyGlossary
                , new BaiscAssistStoreGetter<OntologyGlossary>() {
            @Override
            public IPluginStore<OntologyGlossary> getPluginStore(OntologyPluginMeta pluginMeta) {
                return super.getPluginStore(pluginMeta.setPersistence());
            }

            @Override
            public File getAssistRootDir(String ontologyName) {
                return OntologyDomain.getGlossaryDir(ontologyName);
            }
        });

        public final static Set<OntologyEnum> ontologyEnumsSet = Set.of(OntologyEnum.values());

        public final String typeIdentity;
        private final IAssistStoreGetter<?> storeKeyGetter;
        public final IEndTypeGetter.EndType endType;

        public static OntologyEnum parse(IEndTypeGetter.EndType endType) {
            for (OntologyEnum e : OntologyEnum.values()) {
                if (e.endType == endType) {
                    return e;
                }
            }
            throw new IllegalStateException("endType:" + endType + " is illegal");
        }

        private OntologyEnum(String typeIdentity, IEndTypeGetter.EndType endType,
                             IAssistStoreGetter<?> storeKeyGetter) {
            this.typeIdentity = typeIdentity;
            this.storeKeyGetter = storeKeyGetter;
            this.endType = endType;
        }

        public File getAssistRootDir(String ontologyName) {
            if (StringUtils.isEmpty(ontologyName)) {
                throw new IllegalArgumentException("param ontologyName can not be null");
            }
            return storeKeyGetter.getAssistRootDir(ontologyName);
        }

        public String getTypeIdentity() {
            return typeIdentity;
        }

        @SuppressWarnings("all")
        public IPluginStore<Ontology> getPluginStore(OntologyPluginMeta meta) {
            return (IPluginStore<Ontology>) this.storeKeyGetter.getPluginStore(meta);
        }

        /**
         * 保存本体资源
         *
         * @param pluginContext
         * @param ontologyDomain
         * @param ontologyObj
         */
        public void save(IPluginContext pluginContext, String ontologyDomain, Ontology ontologyObj) {
            OntologyPluginMeta pluginMeta = OntologyPluginMeta.create(this, ontologyDomain);
            pluginMeta.setPluginIdVal(ontologyObj.identityValue()).setPersistence();

            IPluginStore<Ontology> valTypeStore = this.getPluginStore(pluginMeta);

            //        = ONTOLOGY_VALUE_TYPE.getPluginStore(pluginContext,
            //                pluginMeta.putExtraParams(KEY_START_PERSISTENCE,
            //                                Boolean.TRUE.toString())
            //                        .putExtraParams(IdentityName.PLUGIN_IDENTITY_NAME,
            //                                this.getMeta().name));

            valTypeStore.setPlugins(pluginContext, Optional.of(pluginContext.getContext()),
                    Collections.singletonList(new Descriptor.ParseDescribable<>(ontologyObj)));
        }

        @SuppressWarnings("all")
        public <T extends Ontology> List<T> loadAll(OntologyPluginMeta meta) {
            return (List<T>) this.storeKeyGetter.loadAll(meta.getDomain());
        }

        /**
         * 加载详细
         *
         * @param meta
         * @param <T>
         * @return
         */
        @SuppressWarnings("all")
        public <T extends Ontology> T load(OntologyPluginMeta meta) {
            if (StringUtils.isEmpty(meta.getPluginIdVal())) {
                throw new IllegalArgumentException("meta.getPluginIdVal() can not be empty");
            }
            return (T) this.storeKeyGetter.load(meta.getDomain(), meta.getPluginIdVal());
        }

    }

    @Override
    public final void beforeSaved(IPluginContext pluginContext, Optional<Context> context) {
        this.create = System.currentTimeMillis();
    }

    public long getCreate() {
        return this.create;
    }

    //    @SuppressWarnings("all")
    //    public <T extends Ontology> T setCreate(long create) {
    //        this.create = create;
    //        return (T) this;
    //    }

    @Override
    public Descriptor<Ontology> getDescriptor() {
        Descriptor<Ontology> desc = Objects.requireNonNull(Describable.super.getDescriptor(),
                this.getClass().getName() + " relevant desc can not be null");
        if (!(desc instanceof BasicDesc)) {
            throw new IllegalStateException("desc:" + desc.getClass().getName() + " must type of " + BasicDesc.class.getName());
        }
        return desc;
    }

    public static abstract class BasicDesc extends Descriptor<Ontology> implements IEndTypeGetter,
            DescriptorUseableShortComment {
        public BasicDesc() {
            super();
        }

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = super.getExtractProps();
            eprops.put(KEY_ONTOLOGY, getOntologyType().getTypeIdentity());
            return eprops;
        }

        @Override
        public final EndType getEndType() {
            return getOntologyType().endType;
        }

        public abstract OntologyEnum getOntologyType();
    }

}