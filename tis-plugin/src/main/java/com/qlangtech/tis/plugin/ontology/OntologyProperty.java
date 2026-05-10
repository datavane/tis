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
import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.SetPluginsResult;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.typeref.DefaultPropertyTypeRef;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/18
 */
public class OntologyProperty implements Describable<OntologyProperty>, IdentityName {

    public static final String KEY_ONTOLOGY_PROP = "ontology-property";


    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.db_col_name})
    public String name;

    @FormField(ordinal = 4, validate = {Validator.none_blank})
    public String description;

    //    /**
    //     * @see OntologyType
    //     */
    //    @JSONField(serialize = false)
    //    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    //    public int type;

    @JSONField(serialize = false)
    @FormField(ordinal = 1, validate = {Validator.require})
    public OntologyPropertyTypeRef typeRef;


    @FormField(ordinal = 3, type = FormFieldType.ENUM, validate = {Validator.require})
    public Boolean pk;

    @FormField(ordinal = 4, type = FormFieldType.ENUM, validate = {Validator.require})
    public Boolean nullable;

    /**
     * ChatBI 语义角色 —— Dimension/Measure/TimeDimension/Identifier/Unknown。
     * 取值见 {@link SemanticRole}。空时按 Unknown 处理。
     */
    @FormField(ordinal = 5, type = FormFieldType.ENUM, validate = {Validator.integer})
    public Integer role;

    /**
     * 仅当 role=Measure 时有意义：默认聚合方式 + 单位 + 精度。
     */
    @FormField(ordinal = 6, validate = {})
    public MeasureSpec measureSpec;

    public SemanticRole getSemanticRole() {
        return this.role == null ? SemanticRole.Unknown : SemanticRole.parse(this.role);
    }

    public MeasureSpec getMeasureSpec() {
        return this.measureSpec;
    }

    
    @TISExtension
    public static final HeteroEnum<OntologyProperty> ONTOLOGY_PROPERTY = new HeteroEnum<>(//
            OntologyProperty.class, //
            KEY_ONTOLOGY_PROP, // },
            "本体对象属性", Selectable.Single, false) {
        @SuppressWarnings("all")
        @Override
        public IPluginStore<OntologyProperty> getPluginStore(IPluginContext pluginContext,
                                                             UploadPluginMeta pluginMeta) {
            final String propName = pluginMeta.getExtraParam(KEY_ONTOLOGY_PROP);
            OntologyPluginMeta ontologyMeta = new OntologyPluginMeta(pluginMeta);
            if (StringUtils.isEmpty(propName)) {
                throw new IllegalStateException("param propName can not be null");
            }
            return new IPluginStore.AdapterPluginStore<>() {
                @Override
                public List<OntologyProperty> getPlugins() {
                    return Collections.singletonList(this.getPlugin());
                }

                @Override
                public SetPluginsResult setPlugins(IPluginContext pluginContext, Optional<Context> context,
                                                   List<Descriptor.ParseDescribable<OntologyProperty>> dlist,
                                                   boolean update) {
                    getOntologyProperty((t) -> {
                        OntologyProperty ontologyProp = t.getRight();
                        for (Descriptor.ParseDescribable<OntologyProperty> plugin : dlist) {
                            OntologyProperty property = plugin.getInstance();
                            ontologyProp.copy(property);
                            t.getLeft().setPlugins(pluginContext, context,
                                    Collections.singletonList(new Descriptor.ParseDescribable<>(t.getMiddle())));
                        }
                    });
                    return new SetPluginsResult(true, true);
                }

                @Override
                public OntologyProperty getPlugin() {
                    return getOntologyProperty((t) -> {
                    });
                }

                private OntologyProperty getOntologyProperty(
                        Consumer<Triple<IPluginStore<OntologyObjectType>,
                                OntologyObjectType, OntologyProperty>> propConsumer) {

                    IPluginStore<OntologyObjectType> pluginStore =
                            Ontology.OntologyEnum.ObjectType.getPluginStore(ontologyMeta).unsaveCast();
                    //  Ontology.ONTOLOGY.getPluginStore(pluginContext, pluginMeta).unsaveCast();
                    OntologyObjectType objectType = pluginStore.getPlugin();
                    for (OntologyProperty prop : objectType.getCols()) {
                        if (propName.equals(prop.name)) {
                            propConsumer.accept(Triple.of(pluginStore, objectType, prop));
                            return prop;
                        }
                    }
                    throw new IllegalStateException("colName:" + propName + " can not find relevant prop,in:"
                            + objectType.getCols().stream().map(OntologyProperty::getName).collect(Collectors.joining(",")));

                }
            };
        }
    };

    private void copy(OntologyProperty from) {

        Descriptor<OntologyProperty> descriptor = from.getDescriptor();
        descriptor.assign(this, from);

    }


    public OntologyProperty() {
    }

    public OntologyProperty(String name, boolean pk, boolean nullable, String description, OntologyType type) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.typeRef = new DefaultPropertyTypeRef(Objects.requireNonNull(type, "type must not be null").getValue());
        this.pk = pk;
        this.nullable = nullable;
        this.description = description;
    }

    @Override
    public String identityValue() {
        return this.getName();
    }

    public String getName() {
        return name;
    }

    public boolean isPk() {
        return pk;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getDescription() {
        return description;
    }

    public OntologyType parseOntologyType() {
        return this.typeRef.getOntologyType();

    }

    public String getType() {
        return parseOntologyType().getLiteria();
    }

    public String getTypeEnd() {
        return this.parseOntologyType().getEndType().getVal();
    }

    @TISExtension
    public static class DftDesc extends Descriptor<OntologyProperty> {
        public DftDesc() {
            super();
        }


    }
}
