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
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.TISExtensible;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.CompanionPluginFactory;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.qlangtech.tis.extension.Descriptor.KEY_EPROPS;
import static com.qlangtech.tis.extension.Descriptor.KEY_OPTIONS;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 * @see DescriptorsJSONForAIPrompt for AI prompt
 */
public abstract class DescriptorsJSON<T extends Describable<T>, ATTR_VAL extends DescriptorsJSON.AttrVal> {

    //public static final int FORM_START_LEVEL = 1;

    public static final String KEY_DISPLAY_NAME = "displayName";
    public static final String KEY_EXTEND_POINT = "extendPoint";
    public static final String KEY_IMPL = AttrValMap.PLUGIN_EXTENSION_IMPL;// "impl";
    public static final String KEY_IMPL_URL = "implUrl";
    public static final String KEY_ADVANCE = "advance";

    public static final String KEY_SCHEMA_FIELDS_ATTRS = "attrs";

    private final Collection<Descriptor<T>> descriptors;
    /**
     * 由于describe 可以嵌套，此标志位可以判断 是否是根元素
     */
    protected final boolean rootDesc;


    public static DescriptorsMeta desc(String requestDescId) {
        return new DefaultDescriptorsJSON(TIS.get().getDescriptor(requestDescId)).getDescriptorsJSON();
    }

    public static DescriptorsMeta desc(Descriptor desc) {
        return new DefaultDescriptorsJSON(desc).getDescriptorsJSON();
    }

    public DescriptorsJSON(Collection<Descriptor<T>> descriptors) {
        this(descriptors, true);
    }

    public DescriptorsJSON(Descriptor<T> descriptor) {
        this(descriptor, true);
    }

    public DescriptorsJSON(Collection<Descriptor<T>> descriptors, boolean rootDesc) {
        this.descriptors = descriptors;
        this.rootDesc = rootDesc;
    }

    /**
     *
     * @param descriptor
     * @param rootDesc   由于describe 可以嵌套，此标志位可以判断 是否是根元素
     */
    public DescriptorsJSON(Descriptor<T> descriptor, boolean rootDesc) {
        this(Collections.singletonList(descriptor), rootDesc);
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static abstract class SubFormFieldVisitor implements PluginFormProperties.IVisitor {

        final Optional<SubFormFilter> subFormFilter;

        public SubFormFieldVisitor(Optional<SubFormFilter> subFormFilter) {
            this.subFormFilter = subFormFilter;
        }

    }

    public DescriptorsMeta getDescriptorsJSON() {
        return getDescriptorsJSON(Optional.empty());
    }


    /**
     * @param descriptor
     * @param subFormFilter
     * @param forAIPromote
     * @return
     */
    public static Pair<JSONObject, Descriptor> createPluginFormPropertyTypes(Descriptor<?> descriptor,
                                                                             Optional<SubFormFilter> subFormFilter,
                                                                             boolean forAIPromote) {
        PluginFormProperties pluginFormPropertyTypes = descriptor.getPluginFormPropertyTypes(subFormFilter);

        JSONObject desJson = new JSONObject();
        // des.put("formLevel", formLevel);
        final Descriptor desc = pluginFormPropertyTypes.accept(new SubFormFieldVisitor(subFormFilter) {
            @Override
            public Descriptor visit(RootFormProperties props) {
                return descriptor;
            }

            @Override
            public Descriptor visit(BaseSubFormProperties props) {
                JSONObject subForm = new JSONObject();
                subForm.put("fieldName", props.getSubFormFieldName());
                if (subFormFilter.isPresent()) {

                    SubFormFilter filter = subFormFilter.get();
                    if (!filter.subformDetailView) {
                        desJson.put("subForm", true);
                        subForm.put("idList", props.getSubFormIdListGetter(filter).build(filter));
                    }
                }
                desJson.put("subFormMeta", subForm);
                return props.subFormFieldsDescriptor;
            }
        });

        desJson.put(KEY_EXTEND_POINT, desc.getT().getName());

        setDescInfo(desc, forAIPromote, desJson);

        if (IdentityName.class.isAssignableFrom(desc.clazz)) {
            desJson.put("pkField", desc.getIdentityField().displayName);
        }

        if (!forAIPromote) {
            desJson.put("veriflable", desc.overWriteValidateMethod);
            Map<String, Object> extractProps = desc.getExtractProps();
            desJson.put("extractProps", extractProps);
        }

        return Pair.of(desJson, desc);
    }


    public DescriptorsMeta getDescriptorsJSON(Optional<SubFormFilter> subFormFilter) {
        JSONArray attrs;
        String key;
        PropertyType val;


        AttrVal attrVal = null;
        DescriptorsMeta descriptorsMeta = this.createDescriptorsMeta();
        // Map<String, Object> extractProps;

        List<Descriptor<?>> acceptDescs = getAcceptDescs(subFormFilter);

        for (Descriptor<?> dd : acceptDescs) {
            try {

                PluginFormProperties pluginFormPropertyTypes = dd.getPluginFormPropertyTypes(subFormFilter);

                Pair<JSONObject, Descriptor> pair = createFormPropertyTypes(subFormFilter, dd);

                final JSONObject desJson = pair.getKey();
                Descriptor desc = pair.getValue();
                attrs = new JSONArray();

                List<Entry<String, PropertyType>> entries = pluginFormPropertyTypes.getSortedUseableProperties();

                boolean containAdvanceField = false;
                for (Map.Entry<String, PropertyType> pp : entries) {
                    key = pp.getKey();
                    val = pp.getValue();
                    JSONObject extraProps = getFieldExtraProps(val);

                    if (extraProps != null && extraProps.getBooleanValue(PluginExtraProps.KEY_DISABLE)) {
                        continue;
                    }

                    if (val.advance()) {
                        containAdvanceField = true;
                    }

                    attrVal = createAttrVal(key, val);

                    if (extraProps != null) {
                        // 额外属性
                        final JSONObject ep = processExtraProps(val, extraProps);
                        //this.processExtraProps(dd, val, val);
                        JSONObject n = val.multiSelectablePropProcess((vt) -> {
                            JSONObject clone = (JSONObject) ep.clone();
                            clone.put(PluginExtraProps.Props.KEY_VIEW_TYPE, vt.getViewTypeToken());
                            return clone;
                        });
                        val.appendExternalProp(ep);
                        attrVal.put(KEY_EPROPS, n != null ? n : ep);
                    }

                    if (val.typeIdentity() == FormFieldType.SELECTABLE.getIdentity()) {
                        attrVal.put(KEY_OPTIONS, getSelectOptions(desc, val, key));
                    }
                    if (val.isDescribable()) {
                        DescriptorsJSON des2Json = createInnerDescrible(val);

                        attrVal.putDescriptors(des2Json);
                        Annotation extensible = val.fieldClazz.getAnnotation(TISExtensible.class);
                        // 可以运行时添加插件
                        attrVal.put("extensible", (extensible != null));
                        attrVal.put(KEY_EXTEND_POINT, val.fieldClazz.getName());
                    }

                    attrs.add(attrVal);
                }
                // 对象拥有的属性
                desJson.put(KEY_SCHEMA_FIELDS_ATTRS, attrs);
                setContainAdvanceField(desJson, containAdvanceField);
                descriptorsMeta.addDesc(desc.getId(), desJson, dd);
            } finally {

            }
        }
        return descriptorsMeta;
    }

    protected DescriptorsMeta createDescriptorsMeta() {
        DescriptorsMeta descriptorsMeta = new DescriptorsMeta(this.rootDesc);
        return descriptorsMeta;
    }

    protected void setContainAdvanceField(JSONObject desJson, boolean containAdvanceField) {
        // 包含高级字段
        desJson.put("containAdvance", containAdvanceField);
    }

    protected abstract ATTR_VAL createAttrVal(String key, PropertyType val);


    protected JSONObject processExtraProps(PropertyType propertyType, JSONObject extraProps) {
        return extraProps;
    }


    protected JSONObject getFieldExtraProps(PropertyType val) {

        return val.getExtraProps();
    }

    protected Pair<JSONObject, Descriptor> createFormPropertyTypes( //
                                                                    Optional<SubFormFilter> subFormFilter,
                                                                    Descriptor<?> dd) {
        Pair<JSONObject, Descriptor> pair = createPluginFormPropertyTypes(dd, subFormFilter, false);
        return pair;
    }

    protected abstract DescriptorsJSON<T, ATTR_VAL> createInnerDescrible(PropertyType val);
    //    {
    //        return new DescriptorsJSON(val.getApplicableDescriptors(), false);
    //    }

    private List<Descriptor<?>> getAcceptDescs(Optional<SubFormFilter> subFormFilter) {
        PluginFormProperties pluginFormPropertyTypes = null;
        List<Descriptor<?>> acceptDescs = Lists.newArrayList(this.descriptors);
        for (Descriptor<T> dd : this.descriptors) {
            pluginFormPropertyTypes = dd.getPluginFormPropertyTypes(subFormFilter);
            pluginFormPropertyTypes.accept(new PluginFormProperties.IVisitor() {
                @Override
                public Void visit(BaseSubFormProperties props) {
                    if (dd instanceof CompanionPluginFactory) {
                        acceptDescs.add(((CompanionPluginFactory) dd).getCompanionDescriptor());
                    }
                    return null;
                }
            });
        }
        return acceptDescs;
    }

    /**
     *
     * @param d
     * @param forAIPromote 是否是为了生成大模型Promote用
     * @param des
     */
    public static void setDescInfo(Descriptor d, boolean forAIPromote, Map<String, Object> des) {
        des.put(KEY_DISPLAY_NAME, d.getDisplayName());
        des.put(KEY_IMPL, d.getId());
        if (!forAIPromote) {
            des.put(KEY_IMPL_URL,
                    Config.TIS_PUB_PLUGINS_DOC_URL + StringUtils.remove(StringUtils.lowerCase(d.clazz.getName()), "."));
        }
    }

    public static List<Descriptor.SelectOption> getSelectOptions(Descriptor descriptor, PropertyType propType,
                                                                 String fieldKey) {
      //   optionsCreator = null;
        if (propType.typeIdentity() != FormFieldType.SELECTABLE.getIdentity()) {
            throw new IllegalStateException("propType must be:" + FormFieldType.SELECTABLE + " but now is:" + propType.typeIdentity());
        }
        if (!(descriptor instanceof ISelectOptionsGetter)) {
            throw new IllegalStateException("descriptor:" + descriptor.getClass() + " has a selectable field:" + fieldKey + " descriptor must be an instance of 'ISelectOptionsGetter'");
        }
        ISelectOptionsGetter  optionsCreator = descriptor;
        List<Descriptor.SelectOption> selectOptions = optionsCreator.getSelectOptions(fieldKey);

        return selectOptions;
    }

    public interface IPropGetter {
        public Object build(SubFormFilter filter);
    }

    public static abstract class AttrVal {

        public abstract void put(String key, Object val);

        public abstract void putDescriptors(DescriptorsJSON des2Json);
    }
}
