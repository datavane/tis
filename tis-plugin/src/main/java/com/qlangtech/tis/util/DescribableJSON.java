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
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.plugin.IdentityName;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DescribableJSON<T extends Describable<T>> {

    private final T instance;

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

        DescriptorsJSON.setDescInfo(pluginFormPropertyTypes.accept(new PluginFormProperties.IVisitor() {
            @Override
            public Descriptor visit(RootFormProperties props) {
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


        item.put(AttrValMap.PLUGIN_EXTENSION_VALS, vals);
        if (instance instanceof IdentityName) {
            item.put(IdentityName.PLUGIN_IDENTITY_NAME, ((IdentityName) instance).identityValue());
        }

        return item;
    }

    /**
     * 获取用于提交表单的属性值映射
     * 将当前描述对象转换为 AttrValMap 格式，便于表单提交和验证
     * 这是 getItemJson() 的逆向操作，用于将插件实例转换为可提交的格式
     *
     * @return AttrValMap 对象，包含插件的属性值映射
     * @throws Exception 如果转换过程中发生错误
     */
    public AttrValMap getPostAttribute() throws Exception {
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

        // 使用 AttrValMap 的静态方法解析
        return AttrValMap.parseDescribableMap(Optional.empty(), postJson);
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
                        newNestedObj.put(AttrValMap.PLUGIN_EXTENSION_IMPL, nestedObj.getString(AttrValMap.PLUGIN_EXTENSION_IMPL));
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
