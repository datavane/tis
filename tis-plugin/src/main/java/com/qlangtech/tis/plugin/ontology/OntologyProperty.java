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
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.SetPluginsResult;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.IMultiElement;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.IUploadPluginMeta;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
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

public abstract class OntologyProperty implements Describable<OntologyProperty>, IdentityName, IMultiElement {

    public static final String KEY_ONTOLOGY_PROP = "ontology-property";
    public static final String FIELD_NULLABLE = "nullable";
    public static final String FIELD_NAME = "name";

    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.db_col_name})
    public String name;

    @FormField(ordinal = 1, type = FormFieldType.TEXTAREA, validate = {Validator.none_blank})
    public String description;

    public abstract OntologyPropertyTypeRef getPropertyTypeRef();

    /**
     * 物理表达式（可选）：用于描述从物理存储格式到查询可用格式的转换。
     * 使用 {col} 作为列名占位符。
     * 示例：REPLACE(TRIM({col}), '$', '') - 清洗货币符号
     */
    @FormField(ordinal = 999, advance = true, type = FormFieldType.TEXTAREA, validate = {})
    public String physicalExpression;

    //    /**
    //     * @see OntologyType
    //     */
    //    @JSONField(serialize = false)
    //    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    //    public int type;


    @FormField(ordinal = 3, type = FormFieldType.ENUM, validate = {Validator.require})
    public Boolean pk;

    @FormField(ordinal = 4, type = FormFieldType.ENUM, validate = {Validator.require})
    public Boolean nullable;


    @TISExtension
    public static final HeteroEnum<OntologyProperty> ONTOLOGY_PROPERTY = new HeteroEnum<>(//
            OntologyProperty.class, //
            KEY_ONTOLOGY_PROP, // },
            "本体对象属性", Selectable.Single, false) {
        @SuppressWarnings("all")
        @Override
        public IPluginStore<OntologyProperty> getPluginStore(IPluginContext pluginContext,
                                                             UploadPluginMeta pluginMeta) {

            OntologyPluginMeta ontologyMeta = new OntologyPluginMeta(pluginMeta);
            //            if (StringUtils.isEmpty(propName)) {
            //                throw new IllegalStateException("param propName can not be null");
            //            }
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

                        final boolean deleteProcess =
                                pluginContext.getJSONPostContent().getBooleanValue(IUploadPluginMeta.KEY_JSON_MANIPULATE_BOOL_DELETE_PROCESS);
                        for (Descriptor.ParseDescribable<OntologyProperty> plugin : dlist) {
                            OntologyProperty property = plugin.getInstance();
                            if (deleteProcess) {
                                // 删除操作
                                t.getMiddle().deleteCol(Objects.requireNonNull(property, "property can not be null"));
                            } else if (ontologyMeta.isCreate()) {
                                // 添加操作
                                t.getMiddle().addCol(Objects.requireNonNull(property, "property can not be null"));
                            } else {
                                // 更新操作
                                ontologyProp.copy(property);
                            }
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
                    final String propName = ontologyMeta.getObjectTypeProperty();
                    IPluginStore<OntologyObjectType> pluginStore =
                            Ontology.OntologyEnum.ObjectType.getPluginStore(ontologyMeta).unsaveCast();
                    OntologyObjectType objectType = Ontology.loadObjectTypeDetail(ontologyMeta.getDomain(),
                            ontologyMeta.getObjectType());// pluginStore.getPlugin();
                    if (ontologyMeta.isCreate()) {
                        propConsumer.accept(Triple.of(pluginStore, objectType, null));
                        return null;
                    }
                    //  Ontology.ONTOLOGY.getPluginStore(pluginContext, pluginMeta).unsaveCast();

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


    @Override
    public String identityValue() {
        return this.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    public Boolean isPk() {
        return pk;
    }

    public Boolean isNullable() {
        return nullable;
    }

    public String getDescription() {
        return description;
    }

    public String getPhysicalExpression() {
        return physicalExpression;
    }

    /**
     * 判断是否需要物理层转换
     */
    public boolean needsPhysicalTransform() {
        return org.apache.commons.lang3.StringUtils.isNotBlank(physicalExpression);
    }

    /**
     * 应用物理转换（替换 {col} 占位符）
     *
     * @param columnRef 列引用（如 "p.Product_Price" 或 "Product_Price"）
     * @return 转换后的表达式，如果没有物理表达式则返回原列引用
     */
    public String applyPhysicalTransform(String columnRef) {
        if (!needsPhysicalTransform()) {
            return columnRef;
        }
        return physicalExpression.replace("{col}", columnRef);
    }

    public abstract OntologyType parseOntologyType();


    public String getType() {
        return parseOntologyType().getLiteria();
    }

    public String getTypeEnd() {
        return this.parseOntologyType().getEndType().getVal();
    }

    public void setPk(boolean val) {
        this.pk = val;
    }

    /**
     * 将属性挂接到已经创建的sharedProperty上
     *
     * @param sharedProp
     */
    public abstract void reference2SharedProp(OntologySharedProperty sharedProp);


    public abstract void reference2ValueType(OntologyValueType valueType);


}
