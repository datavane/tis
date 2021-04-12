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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.plugin.IdentityName;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//import org.json.JSONObject;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DescribableJSON<T extends Describable<T>> {

    private final T instance;

    private final Descriptor<T> descriptor;

    public DescribableJSON(T instance, Descriptor<T> descriptor) {
        this.instance = instance;
        this.descriptor = descriptor;
    }

    public DescribableJSON(T instance) {
        this(instance, instance.getDescriptor());
    }

    public JSONObject getItemJson() throws Exception {
        return this.getItemJson(Optional.empty());
    }

    public JSONObject getItemJson(Optional<IPropertyType.SubFormFilter> subFormFilter) throws Exception {

        JSONObject item = new JSONObject();
        item.put("impl", descriptor.getId());
        item.put(DescriptorsJSON.KEY_DISPLAY_NAME, descriptor.getDisplayName());

        PluginFormProperties pluginFormPropertyTypes = descriptor.getPluginFormPropertyTypes(subFormFilter);

        JSON vals = pluginFormPropertyTypes.getInstancePropsJson(this.instance);
        item.put("vals", vals);
        if (instance instanceof IdentityName) {
            item.put("identityName", ((IdentityName) instance).identityValue());
        }

        return item;
    }

//    private static JSONObject getInstancePropsJsonObject(Object instance, Set<Map.Entry<String, PropertyType>> pluginFormPropertyTypes) throws Exception {
//        JSONObject vals = new JSONObject();
//        try {
//            Object o = null;
//            for (Map.Entry<String, PropertyType> entry : pluginFormPropertyTypes) {
//                // o = instance.getClass().getField(entry.getKey()).get(instance);
//                // instance.getClass().getField(entry.getKey()).get(instance);
//                o = entry.getValue().getVal(instance);
//                if (o == null) {
//                    continue;
//                }
//                if (entry.getValue().isDescribable()) {
//                    DescribableJSON djson = new DescribableJSON((Describable) o);
//                    vals.put(entry.getKey(), djson.getItemJson());
//                } else {
//                    vals.put(entry.getKey(), o);
//                }
//            }
//        } catch (NoSuchFieldException e) {
//            throw new RuntimeException("fetchKeys:" + pluginFormPropertyTypes.stream().map((entry) -> entry.getKey()).collect(Collectors.joining(","))
//                    + "，hasKeys:" + Arrays.stream(instance.getClass().getFields()).map((r) -> r.getName()).collect(Collectors.joining(",")), e);
//        }
//        return vals;
//    }
}
