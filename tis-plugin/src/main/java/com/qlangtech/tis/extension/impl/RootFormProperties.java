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
package com.qlangtech.tis.extension.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.util.DescribableJSON;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-12 11:05
 */
public class RootFormProperties extends PluginFormProperties {
    private final Map<String, /*** fieldname*/PropertyType> propertiesType;

    public RootFormProperties(Map<String, PropertyType> propertiesType) {
        this.propertiesType = propertiesType;
    }

    @Override
    public Set<Map.Entry<String, PropertyType>> getKVTuples() {
        return this.propertiesType.entrySet();
    }

    @Override
    public JSON getInstancePropsJson(Object instance) {
        JSONObject vals = new JSONObject();
        try {
            Object o = null;
            for (Map.Entry<String, PropertyType> entry : propertiesType.entrySet()) {
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
        } catch (Exception e) {
            throw new RuntimeException("fetchKeys:" + propertiesType.keySet().stream().collect(Collectors.joining(","))
                    + "，hasKeys:" + Arrays.stream(instance.getClass().getFields()).map((r) -> r.getName()).collect(Collectors.joining(",")), e);
        }
        return vals;
    }


}
