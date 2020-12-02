/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.annotation.FormFieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DescriptorsJSON<T extends Describable<T>> {

    public static final String KEY_DISPLAY_NAME = "displayName";

    private final List<Descriptor<T>> descriptors;

    public DescriptorsJSON(List<Descriptor<T>> descriptors) {
        this.descriptors = descriptors;
        descriptors.stream().findFirst();
    }

    public JSONObject getDescriptorsJSON() {
        JSONObject des;
        JSONArray attrs;
        String key;
        Descriptor.PropertyType val;
        JSONObject extraProps = null;
        // FormField fieldAnnot;
        JSONObject attrVal;
        JSONObject descriptors = new JSONObject();
        Map<String, Object> extractProps;
        for (Descriptor<T> d : this.descriptors) {
            des = new JSONObject();
            des.put(KEY_DISPLAY_NAME, d.getDisplayName());
            des.put("extendPoint", d.getT().getName());
            des.put("impl", d.getId());
            des.put("veriflable", d.overWriteValidateMethod);

            extractProps = d.getExtractProps();
            if (!extractProps.isEmpty()) {
                des.put("extractProps", extractProps);
            }

            attrs = new JSONArray();
            ArrayList<Map.Entry<String, Descriptor.PropertyType>> entries = Lists.newArrayList(d.getPropertyTypes().entrySet());
            entries.sort(((o1, o2) -> o1.getValue().ordinal() - o2.getValue().ordinal()));
            for (Map.Entry<String, Descriptor.PropertyType> pp : entries) {
                key = pp.getKey();
                val = pp.getValue();
                // fieldAnnot = val.getFormField();
                attrVal = new JSONObject();
                attrVal.put("key", key);
                attrVal.put("describable", val.isDescribable());
                attrVal.put("type", val.typeIdentity());
                attrVal.put("required", val.isInputRequired());
                attrVal.put("ord", val.ordinal());

                extraProps = val.getExtraProps();
                if (extraProps != null) {
                    // 额外属性
                    attrVal.put("eprops", extraProps);
                }

                ISelectOptionsGetter optionsCreator = null;
                if (val.typeIdentity() == FormFieldType.SELECTABLE.getIdentity()) {
                    if (!(d instanceof ISelectOptionsGetter)) {
                        throw new IllegalStateException("descriptor:" + d.getClass()
                                + " has a selectable field:" + key + " descriptor must be an instance of 'ISelectOptionsGetter'");
                    }
                    optionsCreator = d;
                    List<Descriptor.SelectOption> selectOptions = optionsCreator.getSelectOptions(key);
                    attrVal.put("options", selectOptions);
                }
                if (val.isDescribable()) {
                    DescriptorsJSON des2Json = new DescriptorsJSON(val.getApplicableDescriptors());
                    attrVal.put("descriptors", des2Json.getDescriptorsJSON());
                }
                // attrs.put(attrVal);
                attrs.add(attrVal);
            }
            // 对象拥有的属性
            des.put("attrs", attrs);
            // processor.process(attrs.keySet(), d);
            descriptors.put(d.getId(), des);
        }
        return descriptors;
    }
}
