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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.collect.Maps;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.EnumFieldMode;
import com.qlangtech.tis.extension.impl.MultiStepsHostPluginFormProperties;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.OptionWithEndType;
import com.qlangtech.tis.plugin.IdentityName;
import org.apache.commons.collections.MapUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static com.qlangtech.tis.extension.Descriptor.KEY_ENUM_PROP;
import static com.qlangtech.tis.util.DescriptorsJSON.KEY_EXTRACT_PROPS;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
@JSONType(serializer = DescribableJSONSerializer.class)
public class DescribableJSON<T extends Describable<T>> {

    public final T instance;

    public final Descriptor<T> descriptor;

    public DescribableJSON(T instance, Descriptor<T> descriptor) {
        this.instance = Objects.requireNonNull(instance, "param instance can not be null");
        this.descriptor = Objects.requireNonNull(descriptor,
                "param descriptor can not be null,plugin type:" + instance.getClass());
    }

    public DescribableJSON(T instance) {
        this(Objects.requireNonNull(instance, "param instance can not be null"), instance.getDescriptor());
    }

    public JSONObject getItemJson() throws Exception {
        return this.getItemJson(Optional.empty());
    }


    public JSONObject getItemJson(Optional<SubFormFilter> subFormFilter) throws Exception {
        PluginFormProperties pluginFormPropertyTypes = descriptor.getPluginFormPropertyTypes(subFormFilter);
        return getItemJson(pluginFormPropertyTypes);
    }

    public JSONObject getItemJson(PluginFormProperties pluginFormPropertyTypes) throws Exception {

        JSONObject item = new JSONObject();
        ConcurrentMap<String /*field name*/, Enum<?>> modelAssistVals = Maps.newConcurrentMap();
        DescriptorsJSON.setDescInfo(pluginFormPropertyTypes.accept(new PluginFormProperties.IVisitor() {
            @Override
            public Descriptor visitMultiStepsHost(MultiStepsHostPluginFormProperties props) {
                return descriptor;
            }

            @Override
            public Descriptor visit(RootFormProperties props) {
                for (Map.Entry<String, IPropertyType> entry : props.propertiesType.entrySet()) {
                    if (!(entry.getValue() instanceof PropertyType pp)
                            || pp.getEnumFieldMode() != EnumFieldMode.MODEL_ASSIST) {
                        continue;
                    }
                    // 只把「当前值」那一条枚举描述下发：MODEL_ASSIST 存在的意义就是不给首屏塞全量枚举
                    // （全量下发会让每个 item 膨胀上百KB）。用 instanceof Enum 同时挡掉两种坏值：
                    // 值为 null（未选值）和 SELECTABLE 模式下配置了 model_assist 的 String 值（强转会 CCE）
                    if (pp.getVal(false, instance) instanceof Enum<?> enVal) {
                        modelAssistVals.put(entry.getKey(), enVal);
                    }
                }

                return descriptor;
            }

            @Override
            public Descriptor visit(BaseSubFormProperties props) {

                //                if(descriptor instanceof CompanionPluginFactory){
                //                    ((CompanionPluginFactory)descriptor).getCompaonPlugin()
                //                }

                return props.subFormFieldsDescriptor;
            }
        }), false, item);


        final JSON vals = pluginFormPropertyTypes.getInstancePropsJson(this.instance);

        if (MapUtils.isNotEmpty(modelAssistVals)) {
            // 每个 model_assist 字段只带一条（当前值的）选项描述，供前端行内直接渲染 label/icon/comment，
            // 不需要为了显示已选值再向服务端请求一次全量枚举
            JSONObject enumDesc = new JSONObject();
            modelAssistVals.forEach((fieldName, enVal) -> {
                enumDesc.put(fieldName, Option.toJson(OptionWithEndType.createEnumOptions(new Object[]{enVal},
                        (e) -> true)));
            });
            Map<String, Object> extractProps = Maps.newHashMap();
            extractProps.put(KEY_ENUM_PROP, enumDesc);
            item.put(KEY_EXTRACT_PROPS, extractProps);
        }

        item.put(AttrValMap.PLUGIN_EXTENSION_VALS, vals);
        if (instance instanceof IdentityName) {
            item.put(IdentityName.PLUGIN_IDENTITY_NAME, ((IdentityName) instance).identityValue());
        }

        return item;
    }

    public JSONObject buildPostJsonBody() {
        try {
            return getPostAttribute().getPostJsonBody();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取用于提交表单的属性值映射
     * 将当前描述对象转换为 AttrValMap 格式，便于表单提交和验证
     * 这是 getItemJson() 的逆向操作，用于将插件实例转换为可提交的格式
     *
     * @return AttrValMap 对象，包含插件的属性值映射
     * @throws Exception 如果转换过程中发生错误
     */
    public AttrValMap getPostAttribute() {
        // 使用 AttrValMap 的静态方法解析
        return AttrValMap.parseDescribableMap(Optional.empty(), getPostAttributeJson());
    }

    public JSONObject getPostAttributeJson() {
        // 获取插件的属性信息
        PluginFormProperties pluginFormPropertyTypes = descriptor.getPluginFormPropertyTypes(Optional.empty());

        // 获取实例的属性值JSON
        final JSON vals = pluginFormPropertyTypes.getInstancePropsJson(this.instance);

        // 构建用于创建 AttrValMap 的 JSONObject
        JSONObject postJson = new JSONObject();

        // 设置插件实现类信息
        postJson.put(AttrValMap.PLUGIN_EXTENSION_IMPL, descriptor.getId());

        // 转换属性值为 AttrVals 格式
        // 需要将简单值包装成带有 KEY_primaryVal 的格式
        JSONObject wrappedVals = convertToAttrVals(vals);
        postJson.put(AttrValMap.PLUGIN_EXTENSION_VALS, wrappedVals);

        return postJson;
    }

    /**
     * 递归转换属性值为 AttrVals 格式
     * 处理嵌套的 Describable 对象
     *
     * @param vals 原始属性值
     * @return 转换后的属性值（包装成带有 KEY_primaryVal 或 KEY_DESC_VAL 的格式）
     */
    private JSONObject convertToAttrVals(JSON vals) {
        if (!(vals instanceof JSONObject)) {
            throw new IllegalStateException("type of vals must be " + JSONObject.class.getSimpleName());
        }

        JSONObject valsObj = (JSONObject) vals;
        JSONObject wrappedVals = new JSONObject();

        for (Map.Entry<String, Object> entry : valsObj.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();

            JSONObject fieldVal = new JSONObject();
            if (value instanceof JSONObject) {
                JSONObject nestedObj = (JSONObject) value;

                // 检查是否是嵌套的 Describable 对象
                if (nestedObj.containsKey(AttrValMap.PLUGIN_EXTENSION_IMPL)) {
                    // 这是一个嵌套的 Describable 对象
                    // 需要递归处理其 vals 字段
                    JSONObject nestedVals = nestedObj.getJSONObject(AttrValMap.PLUGIN_EXTENSION_VALS);
                    if (nestedVals != null) {
                        // 递归转换嵌套的属性值
                        JSONObject convertedNestedVals = convertToAttrVals(nestedVals);

                        // 创建新的嵌套对象，保持原有的结构
                        JSONObject newNestedObj = new JSONObject();
                        newNestedObj.put(AttrValMap.PLUGIN_EXTENSION_IMPL,
                                nestedObj.getString(AttrValMap.PLUGIN_EXTENSION_IMPL));
                        newNestedObj.put(AttrValMap.PLUGIN_EXTENSION_VALS, convertedNestedVals);

                        // 包装成 KEY_DESC_VAL 格式
                        JSONObject descVal = new JSONObject();
                        descVal.put(Descriptor.KEY_DESC_VAL, newNestedObj);
                        fieldVal = descVal;
                    } else {
                        // 如果没有 vals，直接包装原对象
                        JSONObject descVal = new JSONObject();
                        descVal.put(Descriptor.KEY_DESC_VAL, nestedObj);
                        fieldVal = descVal;
                    }
                } else {
                    // 普通的 JSONObject，作为 primaryVal 处理
                    fieldVal.put(Descriptor.KEY_primaryVal, value);
                }
            } else {
                // 普通值，包装成 primaryVal 格式
                fieldVal.put(Descriptor.KEY_primaryVal, value);
            }
            wrappedVals.put(field, fieldVal);
        }

        return wrappedVals;
    }

}
