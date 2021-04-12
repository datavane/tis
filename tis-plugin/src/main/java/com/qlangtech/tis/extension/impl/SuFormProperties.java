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
import com.alibaba.fastjson.JSONArray;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 13:22
 */
public class SuFormProperties extends PluginFormProperties implements IPropertyType {
    public final Map<String, /*** fieldname */PropertyType> fieldsType;
    private final Field subFormField;

    public SuFormProperties(Field subFormField, Map<String, PropertyType> fieldsType) {
        Objects.requireNonNull(fieldsType, "fieldsType can not be null");
        this.fieldsType = fieldsType;
        this.subFormField = subFormField;
    }

    @Override
    public Set<Map.Entry<String, PropertyType>> getKVTuples() {
        return fieldsType.entrySet();
    }

    @Override
    public JSON getInstancePropsJson(Object instance) {
        Class<?> declarFieldClass = subFormField.getType();
        if (!Collection.class.isAssignableFrom(declarFieldClass)) {
            // 现在表单只支持1对n 关系的子表单，因为1对1就没有必要有子表单了
            throw new UnsupportedOperationException("sub form field:" + subFormField.getName() + " just support one2multi relationship,declarFieldClass:" + declarFieldClass.getName());
        }

        JSONArray vals = new JSONArray();

        try {
            Object o = subFormField.get(instance);
            Collection<?> itItems = null;
            if (o != null) {
                itItems = (Collection<?>) o;
                for (Object i : itItems) {
                    vals.add((new RootFormProperties(fieldsType)).getInstancePropsJson(i));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return vals;
    }
}
