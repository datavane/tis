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

package com.qlangtech.tis.aiagent.llm;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.MultiStepsSupportHost;
import com.qlangtech.tis.extension.MultiStepsSupportHostDescriptor;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.util.MultiItemsViewType;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.ds.BasicMultiSelectSingleValElementCreatorFactory;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.plugin.ds.ViewContent;
import com.qlangtech.tis.util.AttrValMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.qlangtech.tis.extension.Descriptor.KEY_DESC_VAL;
import static com.qlangtech.tis.extension.Descriptor.KEY_ENUM_PROP;
import static com.qlangtech.tis.extension.Descriptor.KEY_primaryVal;
import static com.qlangtech.tis.plugin.ds.ElementCreatorFactory.PROPERTY_TUPLES_KEY;

/**
 * 将 LLM 生成的扁平 JSON 还原为 TIS 后端 {@link AttrValMap#parseDescribableMap} 期望的深层格式。
 *
 * <h3>转换规则</h3>
 * <ol>
 *   <li>根级 {@code impl} 字段保持不变</li>
 *   <li>遍历 {@code vals} 中的每个字段，通过 {@link Descriptor#getPropertyTypes()} 获取字段的 PropertyType</li>
 *   <li><b>非 describable 字段</b>：{@code "port": 3306} → {@code "port": { "_primaryVal": 3306 }}</li>
 *   <li><b>describable 字段（oneOf）</b>：从扁平值中取 {@code id} 字段值，匹配对应 Descriptor，
 *       还原为 {@code { "descVal": { "impl": "...", "vals": { ... } } }}</li>
 * </ol>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/3/25
 * @see com.qlangtech.tis.util.AttrValMap#parseDescribableMap
 */
@SuppressWarnings("all")
public class FlatJsonToTisConverter {

    /**
     * 将 LLM 生成的扁平 JSON 还原为 TIS 后端期望的深层格式
     *
     * @param flatJson 扁平 JSON，结构为 { "impl": "...", "vals": { fieldKey: value, ... } }
     * @return TIS 格式 JSON，结构为 { "impl": "...", "vals": { fieldKey: { "_primaryVal": value }, ... } }
     */
    public static JSONObject convert(JSONObject flatJson) {
        String impl = Objects.requireNonNull(flatJson.getString(AttrValMap.PLUGIN_EXTENSION_IMPL),
                "flatJson must contain '" + AttrValMap.PLUGIN_EXTENSION_IMPL + "' field");
        Descriptor descriptor = Objects.requireNonNull(TIS.get().getDescriptor(impl),
                "impl:" + impl + " can not find relevant Descriptor");

        JSONObject result = new JSONObject();
        result.put(AttrValMap.PLUGIN_EXTENSION_IMPL, impl);
        JSONObject flatVals = flatJson.getJSONObject(AttrValMap.PLUGIN_EXTENSION_VALS);

        if (descriptor instanceof MultiStepsSupportHostDescriptor) {
            JSONObject stepVals = new JSONObject();
            for (int i = 0; i < OneStepOfMultiSteps.stepsArray.length; i++) {
                OneStepOfMultiSteps.Step step = OneStepOfMultiSteps.stepsArray[i];
                JSONObject stepContent = flatVals.getJSONObject(step.name());
                if (stepContent == null) {
                    break;
                }
                stepVals.put(step.name(), convert(stepContent));
            }
            result.put(AttrValMap.PLUGIN_EXTENSION_VALS, stepVals);
        } else {

            if (flatVals != null) {
                result.put(AttrValMap.PLUGIN_EXTENSION_VALS, convertVals(flatVals, descriptor));
            }
        }


        return result;
    }

    /**
     * 递归转换 vals 内的属性
     */
    private static JSONObject convertVals(JSONObject flatVals, Descriptor descriptor) {
        JSONObject result = new JSONObject();
        Map<String, IPropertyType> propertyTypes = descriptor.getPropertyTypes();

        for (Map.Entry<String, Object> entry : flatVals.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            PropertyType pt = (PropertyType) propertyTypes.get(fieldName);

            if (pt != null && pt.isDescribable()) {
                // oneOf 字段：需要还原 descVal 包装
                if (!(value instanceof Map<?, ?>)) {
                    throw new IllegalStateException("fieldName:" + fieldName + " type error,value:" + String.valueOf(value));
                }
                Map<String, Object> flatOneOf = (Map<String, Object>) value;
                result.put(fieldName, convertDescribableField(flatOneOf, pt));
            } else {
                // 普通字段：添加 _primaryVal 包装
                JSONObject wrapped = new JSONObject();
                if (pt.formField.type() == FormFieldType.MULTI_SELECTABLE) {
                    MultiItemsViewType multiItemsViewType = pt.getMultiItemsViewType();
                    if (multiItemsViewType.getViewContent() != ViewContent.MultiSelectSingleVal) {
                        // 如果是非MultiSelectSingleVal 类型的话，AI 大模型端处理会非常难处理，所以，这里只处理MultiSelectSingleVal
                        throw new IllegalStateException("property " + pt.propertyName() //
                                + " viewType must be " + ViewContent.MultiSelectSingleVal + " but now is " + multiItemsViewType.getViewContent());
                    }
                    //
                    JSONObject enumProp = new JSONObject();
                    JSONObject mclosProp = new JSONObject();
                    JSONArray mclos = new JSONArray();
                    JSONObject elmt = null;
                    if (value instanceof List multiSingleVals) {
                        if (CollectionUtils.isEmpty(multiSingleVals)) {
                            throw new IllegalStateException("fieldName:" + fieldName + " of " //
                                    + pt.f.getDeclaringClass().getName() + " relevant multiSingleVals can not be "
                                    + "empty");
                        }
                        for (Object val : multiSingleVals) {
                            elmt = new JSONObject();
                            elmt.put(BasicMultiSelectSingleValElementCreatorFactory.KEY_ENUM_VAL,
                                    String.valueOf(Objects.requireNonNull(val)));
                            mclos.add(elmt);
                        }
                    } else {
                        throw new IllegalStateException("value type must be " + List.class.getSimpleName() + " but "
                                + "now is " + value.getClass().getName());
                    }
                    mclosProp.put(PROPERTY_TUPLES_KEY, mclos);
                    enumProp.put(KEY_ENUM_PROP, mclosProp);
                    wrapped.put(ElementCreatorFactory.PROPERTY_EXTERNAL_PROPERTIES, enumProp);
                } else {
                    wrapped.put(KEY_primaryVal, value);

                }
                result.put(fieldName, wrapped);
            }
        }
        return result;
    }

    /**
     * 还原 describable（oneOf）字段
     * <pre>
     * 输入：{ "id": "off", "host": "192.168.28.200" }
     * 输出：{ "descVal": { "impl": "...NoneSplitTableStrategy", "vals": { "host": { "_primaryVal": "..." } } } }
     * </pre>
     */
    private static JSONObject convertDescribableField(Map<String, Object> flatOneOf, PropertyType pt) {
        Object impl = Objects.requireNonNull(flatOneOf.get(TISJsonSchema.SCHEMA_PLUGIN_DESCRIPTOR_ID),
                "oneOf field must contain '" + TISJsonSchema.SCHEMA_PLUGIN_DESCRIPTOR_ID + "' field");

        // 通过 id（displayName）找到对应的 Descriptor
        List<? extends Descriptor> applicableDescriptors = pt.applicableDescriptors(false);
        Descriptor matchedDesc = null;
        for (Descriptor desc :applicableDescriptors) {
            if (desc.clazz.getName().equals(impl) || impl.equals(desc.getDisplayName())) {
                matchedDesc = desc;
                break;
            }
        }

        if (matchedDesc == null) {
            throw new IllegalStateException("can not find Descriptor with displayName '" + impl + "' for oneOf field,"
                    + "in:"
                    + applicableDescriptors.stream() //
                    .map((desc) -> desc.getDisplayName() + ":" + desc.clazz.getName()).collect(Collectors.joining(",")));
        }

        JSONObject descVal = new JSONObject();
        descVal.put(AttrValMap.PLUGIN_EXTENSION_IMPL, matchedDesc.getId());

        // 除 id 外的字段递归转换为 vals
        JSONObject innerVals = new JSONObject();
        Map<String, IPropertyType> innerPts = matchedDesc.getPropertyTypes();

        for (Map.Entry<String, Object> entry : flatOneOf.entrySet()) {
            if (TISJsonSchema.SCHEMA_PLUGIN_DESCRIPTOR_ID.equals(entry.getKey())) {
                continue;
            }
            PropertyType innerPt = (PropertyType) innerPts.get(entry.getKey());
            if (innerPt != null && innerPt.isDescribable()) {
                // 递归处理：子 Descriptor 中如果还有 describable 字段
                innerVals.put(entry.getKey(), convertDescribableField((JSONObject) entry.getValue(), innerPt));
            } else {
                JSONObject wrapped = new JSONObject();
                wrapped.put(KEY_primaryVal, entry.getValue());
                innerVals.put(entry.getKey(), wrapped);
            }
        }
        descVal.put(AttrValMap.PLUGIN_EXTENSION_VALS, innerVals);

        JSONObject result = new JSONObject();
        result.put(KEY_DESC_VAL, descVal);
        return result;
    }
}