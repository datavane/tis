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

package com.qlangtech.tis.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qlangtech.tis.aiagent.llm.JsonSchema;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.extension.AIPromptEnhance;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.ValidateRule;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.qlangtech.tis.aiagent.llm.JsonSchema.SCHEMA_VALUE_DEFAULT;
import static com.qlangtech.tis.extension.Descriptor.KEY_ENUM_PROP;
import static com.qlangtech.tis.extension.Descriptor.KEY_primaryVal;
import static com.qlangtech.tis.extension.util.PluginExtraProps.KEY_ENUM_FILTER;
import static com.qlangtech.tis.extension.util.PluginExtraProps.Props.KEY_ASYNC_HELP;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_IMPL;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_VALS;

/**
 * 生成的json描述信息提供给大模型使用
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/16
 */
public class DescriptorsJSONForAIPrompt<T extends Describable<T>> extends DescriptorsJSON<T,
        DescriptorsJSONForAIPrompt.AISchemaAttrVal> {
    /**
     * 进行过程中构建的组件依赖
     */
    private Map<Class<? extends Descriptor>, DescribableImpl> descFieldsRegister = Maps.newHashMap();

    public static class AISchemaAttrVal extends AttrVal {
        private final String fieldKey;
        private final PropertyType propertyType;

        public AISchemaAttrVal(String fieldKey, PropertyType propertyType) {
            this.fieldKey = Objects.requireNonNull(fieldKey, "fieldKey can not be null");
            this.propertyType = Objects.requireNonNull(propertyType, "propertyType can not be null");
        }

        public String getFieldKey() {
            return this.fieldKey;
        }

        public String getHelp() {

            @SuppressWarnings("unchecked") AIPromptEnhance promptEnhance =
                    (AIPromptEnhance) propertyType.fieldClazz.getAnnotation(AIPromptEnhance.class);
            String helpContent = null;
            if (propertyType.extraProp != null) {
                helpContent = propertyType.extraProp.getHelpContent();
            }
            if (promptEnhance != null) {
                return StringUtils.trimToEmpty(helpContent) + "\n" + promptEnhance.prompt();
            }
            return helpContent;
        }

        @Override
        public void put(String key, Object val) {

        }

        @Override
        public void putDescriptors(DescriptorsJSON des2Json) {
            //  DescriptorsMeta descriptorsJSON = des2Json.getDescriptorsJSON();
        }
    }

    public static class AISchemaDescriptorsMeta extends DescriptorsMeta {

        public final Map<String /* concrete plugin implement class */, JsonSchema> descSchemaRegister =
                Maps.newHashMap();

        public AISchemaDescriptorsMeta(boolean rootDesc) {
            super(rootDesc);
        }

        /**
         *
         * @param id       插件的实现类className值
         * @param descJson
         * @param desc
         */
        @Override
        public void addDesc(String id, JSONObject descJson, Object desc) {
            super.addDesc(id, descJson, desc);
            Descriptor descriptor = (Descriptor) desc;
            JsonSchema.Builder schemaBuilder //
                    = JsonSchema.Builder.create(descriptor.getDisplayName(), Optional.empty());
            schemaBuilder.addProperty(PLUGIN_EXTENSION_IMPL, JsonSchema.FieldType.String //
                            , "concrete plugin implement class") //
                    .setConst(descriptor.getId());


            schemaBuilder.addObjectProperty(PLUGIN_EXTENSION_VALS, (inner) -> {
                addProps2Builder(descJson, inner);
            });

            this.descSchemaRegister.put(id, schemaBuilder.build());
        }

        /**
         * @param descJson
         * @param inner
         * @see #createFormPropertyTypes  param is return from descJson
         */
        public static void addProps2Builder(JSONObject descJson, JsonSchema.Builder inner) {
            final JSONArray attrs = Objects.requireNonNull(descJson.getJSONArray(KEY_SCHEMA_FIELDS_ATTRS),
                    KEY_SCHEMA_FIELDS_ATTRS + " relevant attrs can not be null");
            attrs.forEach((attr) -> {
                AISchemaAttrVal attrVal = (AISchemaAttrVal) attr;
                PropertyType pt = attrVal.propertyType;
                if (pt.isDescribable()) {
                    inner.addOneOfProperty(attrVal, pt.getApplicableDescriptors(), true);
                } else {
                    FormFieldType fieldType = pt.formField.type();
                    Validator[] validators = pt.formField.validate();
                    // attrVal.propertyType.extraProp.getHelpContent()
                    //  JsonSchema.AddedProperty addedProperty = //


                    //  inner.addObjectProperty(attrVal.fieldKey, (i) -> {

                    JsonSchema.FieldType schemaFieldType =
                            (pt.fieldClazz == boolean.class || pt.fieldClazz == Boolean.class) ?
                                    JsonSchema.FieldType.Boolean : fieldType.schemaFieldType;

                    String placeholder = pt.extraProp.getPlaceholder();
                    StringBuilder helpContent =
                            new StringBuilder(StringUtils.trimToEmpty(pt.extraProp.getHelpContent()));
                    if (StringUtils.isNotEmpty(placeholder)) {
                        helpContent.append(!helpContent.isEmpty() ? "," : StringUtils.EMPTY).append("例子:").append(placeholder);
                    }
                    AIPromptEnhance promptEnhance = pt.f.getAnnotation(AIPromptEnhance.class);
                    if (promptEnhance != null) {
                        helpContent.append("\n").append(promptEnhance.prompt());
                    }
                    Object dft = pt.dftVal();
                    if (dft != null) {
                        // 但 Qwen 的 structured output 功能目前（截至 2026 年）并不会自动读取并应用 default 值。它只保证输出结构符合
                        // schema（字段存在、类型正确、enum/pattern 合规），但不会主动填充默认值。
                        // 所以需要在提示词中说明默认值
                        helpContent.append("\n `").append(SCHEMA_VALUE_DEFAULT).append("`:").append(JsonUtil.toString(dft));
                    }
                    //                            if (pt.isIdentity()) {
                    //                                /**
                    //                                 * 1. 没有抽取到对应值： 输出的`_primaryVal`属性对应的值不要自动生成（切记）
                    //                                 * 2.  抽取到对应的值：输出的`_primaryVal`属性值必须严格匹配正则式：
                    //                                 `[A-Z\\da-z_]+`
                    //                                 *       ，如有非法字符须进行**合理替换**以符合正则式，例如：识别得到“mysql-mysql-2
                    //                                 *       ”不符合正则式规范，**必须**进行**合理替换**变成“mysql_mysql_2”
                    //                                 */
                    //                                helpContent.append("\n没有抽取到对应值：
                    //                                输出的`_primaryVal`属性对应的值不要自动生成（切记）");
                    //                            }
                    JsonSchema.AddedProperty addedProperty =  //
                            inner.addProperty(attrVal.fieldKey, schemaFieldType, helpContent.toString(),
                                    pt.isInputRequired());

                    if (dft != null) {
                        addedProperty.setDefault(dft);

                    }
                    List<Option> enumPropOptions = attrVal.propertyType.getEnumPropOptions(false);
                    if (CollectionUtils.isNotEmpty(enumPropOptions)) {
                        addedProperty.setValEnums(enumPropOptions.stream().map(Option::getValue).toArray(Object[]::new));
                    }
                    allValidator:
                    for (Validator validator : validators) {
                        for (ValidateRule rule : validator.rules) {
                            if (rule.pattern != null) {
                                addedProperty.setPattern(rule.pattern);
                                break allValidator;
                            }
                        }
                    }
                    //  });

                }
            });
        }
    }


    public static Pair<DescriptorsMeta, DescriptorsJSONForAIPrompt> desc(DescribableImpl pluginImpl) {
        DescriptorsJSONForAIPrompt aiPrompt = new DescriptorsJSONForAIPrompt(pluginImpl.getImplDesc(), pluginImpl);
        return Pair.of(aiPrompt.getDescriptorsJSON(), aiPrompt);
    }


    public DescriptorsJSONForAIPrompt(Descriptor<T> descriptor, DescribableImpl pluginImpl) {
        super(descriptor);
        this.descFieldsRegister.put(descriptor.getClass(), pluginImpl);
    }

    public DescriptorsJSONForAIPrompt(Collection<Descriptor<T>> collection, boolean rootDesc) {
        super(collection, rootDesc);
    }

    public Map<Class<? extends Descriptor>, DescribableImpl> getFieldDescRegister() {
        return this.descFieldsRegister;
    }

    @Override
    protected JSONObject processExtraProps(PropertyType propertyType, JSONObject extraProps) {
        //        JSONObject extra = new JSONObject(extraProps);
        //        // 噪音，没有用
        //        extra.remove(KEY_ENUM_FILTER);
        //        if (propertyType.isDescribable()) {
        //            extra.remove(KEY_ENUM_PROP);
        //        }
        //        extra.remove(KEY_ENUM_FILTER);
        //        extra.remove(KEY_ASYNC_HELP);

        return extraProps;
    }

    @Override
    protected Pair<JSONObject, Descriptor> //
    createFormPropertyTypes(Optional<SubFormFilter> subFormFilter, Descriptor<?> dd) {
        Pair<JSONObject, Descriptor> pair = createPluginFormPropertyTypes(dd, subFormFilter, true);
        return pair;
    }


    @Override
    protected DescriptorsMeta createDescriptorsMeta() {
        AISchemaDescriptorsMeta descriptorsMeta = new AISchemaDescriptorsMeta(this.rootDesc);
        return descriptorsMeta;
    }

    @Override
    protected DescriptorsJSON<T, DescriptorsJSONForAIPrompt.AISchemaAttrVal> createInnerDescrible(List<?
            extends Descriptor> descriptors) {
        return new DescriptorsJSONForAIPrompt(descriptors, false);
    }

    @Override
    protected AISchemaAttrVal createAttrVal(String key, PropertyType val) {
        //  attrVal;
        // fieldAnnot = val.getFormField();
        JSONObject attrVal = new JSONObject();
        attrVal.put("key", key);
        if (val.isDescribable()) {
            attrVal.put("describable", true);
        }
        if (val.isIdentity()) {
            // 是否是主键
            attrVal.put("pk", true);
        }

        attrVal.put("type", val.typeIdentity());
        if (val.isInputRequired()) {
            attrVal.put("required", true);
        }
        // return attrVal;
        return new AISchemaAttrVal(key, val);
    }

    @Override
    protected void setContainAdvanceField(JSONObject desJson, boolean containAdvanceField) {
        // 包含高级字段
        //  desJson.put("containAdvance", containAdvanceField);
    }

    @Override
    protected JSONObject getFieldExtraProps(PropertyType val) {
        PluginExtraProps.Props extraProp = val.extraProp;
        if (extraProp != null && extraProp.isAsynHelp()) {
            JSONObject props = new JSONObject(val.getExtraProps());
            props.put(PluginExtraProps.Props.KEY_HELP, Objects.requireNonNull(extraProp.asynHelp(),
                    "asynHelp can " + "not" + " be null").getContentForAI().toString());
            return props;
        }
        return val.getExtraProps();
    }
}
