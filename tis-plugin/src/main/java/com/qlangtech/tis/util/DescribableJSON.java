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

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IdentityName;

import java.util.Arrays;
import java.util.Map;
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
        JSONObject vals;
        JSONObject item = new JSONObject();
        item.put("impl", descriptor.getId());
        item.put(DescriptorsJSON.KEY_DISPLAY_NAME, descriptor.getDisplayName());
        vals = new JSONObject();
        // Set<String> keys = descPropsMap.get(descriptor.getT());
        try {
            Object o = null;
            for (Map.Entry<String, Descriptor.PropertyType> entry : descriptor.getPropertyTypes().entrySet()) {
                // o = instance.getClass().getField(entry.getKey()).get(instance);
                // instance.getClass().getField(entry.getKey()).get(instance);
                o = entry.getValue().getVal(instance);
                if (o == null) {
                    continue;
                }
                if (entry.getValue().isDescribable()) {
                    DescribableJSON djson = new DescribableJSON((Describable) o);
                    vals.put(entry.getKey(), djson.getItemJson());
                } else {
                    vals.put(entry.getKey(), o);
                }
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("fetchKeys:" + descriptor.getPropertyTypes().keySet().stream().collect(Collectors.joining(","))
                    + "，hasKeys:" + Arrays.stream(this.instance.getClass().getFields()).map((r) -> r.getName()).collect(Collectors.joining(",")), e);
        }
        item.put("vals", vals);
        if (instance instanceof IdentityName) {
            item.put("identityName", ((IdentityName) instance).identityValue());
        }


        return item;
    }
}
