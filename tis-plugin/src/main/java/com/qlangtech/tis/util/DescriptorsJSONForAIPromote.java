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

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.qlangtech.tis.extension.Descriptor.KEY_ENUM_PROP;
import static com.qlangtech.tis.extension.util.PluginExtraProps.KEY_ENUM_FILTER;
import static com.qlangtech.tis.extension.util.PluginExtraProps.Props.KEY_ASYNC_HELP;

/**
 * 生成的json描述信息提供给大模型使用
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/16
 */
public class DescriptorsJSONForAIPromote<T extends Describable<T>> extends DescriptorsJSON<T> {


    public static Pair<DescriptorsJSONResult, DescriptorsJSONForAIPromote> desc(DescribableImpl pluginImpl) {
        DescriptorsJSONForAIPromote aiPromote = new DescriptorsJSONForAIPromote(pluginImpl.getImplDesc(), pluginImpl);
        return Pair.of(aiPromote.getDescriptorsJSON(), aiPromote);
    }

    /**
     * 进行过程中构建的组件依赖
     */
    private Map<Class<? extends Descriptor>, DescribableImpl> descFieldsRegister = Maps.newHashMap();

    public DescriptorsJSONForAIPromote(Descriptor<T> descriptor, DescribableImpl pluginImpl) {
        super(descriptor);
        this.descFieldsRegister.put(descriptor.getClass(), pluginImpl);
    }

    public DescriptorsJSONForAIPromote(Collection<Descriptor<T>> collection, boolean rootDesc) {
        super(collection, rootDesc);
    }

    public Map<Class<? extends Descriptor>, DescribableImpl> getFieldDescRegister() {
        return this.descFieldsRegister;
    }

    @Override
    protected JSONObject processExtraProps(PropertyType propertyType, JSONObject extraProps) {
        JSONObject extra = new JSONObject(extraProps);
        // 噪音，没有用
        extra.remove(KEY_ENUM_FILTER);
        if (propertyType.isDescribable()) {
            extra.remove(KEY_ENUM_PROP);
        }
        extra.remove(KEY_ENUM_FILTER);
        extra.remove(KEY_ASYNC_HELP);

        return extra;
    }

    @Override
    protected Pair<JSONObject, Descriptor> //
    createFormPropertyTypes(Optional<SubFormFilter> subFormFilter, Descriptor<?> dd) {
        Pair<JSONObject, Descriptor> pair = createPluginFormPropertyTypes(dd, subFormFilter, true);
        return pair;
    }

    @Override
    public DescriptorsJSONResult getDescriptorsJSON() {
        return super.getDescriptorsJSON();
    }

    @Override
    protected DescriptorsJSON<T> createInnerDescrible(PropertyType val) {
        return new DescriptorsJSONForAIPromote(val.getApplicableDescriptors(), false);
    }

    @Override
    protected JSONObject createAttrVal(String key, PropertyType val) {
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
        return attrVal;
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
            props.put(PluginExtraProps.Props.KEY_HELP, Objects.requireNonNull(extraProp.asynHelp(), "asynHelp can not"
                    + " be null").getContentForAI());
            return props;
        }
        return val.getExtraProps();
    }
}
