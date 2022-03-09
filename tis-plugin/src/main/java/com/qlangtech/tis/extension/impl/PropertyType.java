/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.extension.impl;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.jvnet.tiger_types.Types;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 12:07
 */
public class PropertyType implements IPropertyType {
    private static final ConvertUtilsBean convertUtils = new ConvertUtilsBean();

    public final Class clazz;

    public final Type type;

    private volatile Class itemType;

    public final String displayName;

    public final FormField formField;

    private final Field f;

    private Boolean inputRequired;

    public PluginExtraProps.Props extraProp;

    PropertyType(Field f, Class clazz, Type type, String displayName, FormField formField) {
        this.f = f;
        this.clazz = clazz;
        this.type = type;
        this.displayName = displayName;
        if (formField == null) {
            throw new IllegalStateException("param formField can not be null");
        }
        this.formField = formField;
    }


    /**
     * 是否是主键
     *
     * @return
     */
    public boolean isIdentity() {
        return this.formField.identity();
    }

    public JSONObject getExtraProps() {
        if (this.extraProp == null) {
            return null;
        }
        return this.extraProp.getProps();
    }

    public void setExtraProp(PluginExtraProps.Props extraProp) {
        this.extraProp = extraProp;
    }

    public String dftVal() {
        return this.extraProp.getDftVal();
    }

    public int ordinal() {
        return formField.ordinal();
    }

    public int typeIdentity() {
        return formField.type().getIdentity();
    }


    public Validator[] getValidator() {
        return formField.validate();
    }

    public boolean isInputRequired() {
        if (inputRequired == null) {
            inputRequired = false;
            Validator[] validators = this.formField.validate();
            for (Validator v : validators) {
                if (v == Validator.require) {
                    return inputRequired = true;
                }
            }
        }
        return inputRequired;
    }

    public PropertyType(Field f, FormField formField) {
        this(f, f.getType(), f.getGenericType(), f.getName(), formField);
    }

    // PropertyType(Method getter) {
    // this(getter.getReturnType(), getter.getGenericReturnType(), getter.toString());
    // }
    public Enum[] getEnumConstants() {
        return (Enum[]) clazz.getEnumConstants();
    }

    /**
     * If the property is a collection/array type, what is an item type?
     */
    public Class getItemType() {
        if (itemType == null)
            itemType = computeItemType();
        return itemType;
    }

    // 取得实例的值
    public Object getVal(Object instance) {
        try {
            return this.f.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setVal(Object instance, Object val) {
        Object prop = null;
        try {
            prop = convertUtils.convert(val, this.clazz);
            this.f.set(instance, prop);
        } catch (Throwable e) {
            throw new RuntimeException("\ntarget instance:" + instance.getClass() + "\nfield:" + this.f + (prop == null ? StringUtils.EMPTY : "\nprop class:" + prop.getClass()), e);
        }
    }

    private Class computeItemType() {
        if (clazz.isArray()) {
            return clazz.getComponentType();
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            Type col = Types.getBaseClass(type, Collection.class);
            if (col instanceof ParameterizedType) {
                return Types.erasure(Types.getTypeArgument(col, 0));
            } else {
                return Object.class;
            }
        }
        return null;
    }

    /**
     * Returns {@link Descriptor} whose 'clazz' is the same as {@link #getItemType() the item type}.
     */
    public Descriptor getItemTypeDescriptor() {
        return TIS.get().getDescriptor(getItemType());
    }

    public boolean isDescribable() {
        // }
        return Describable.class.isAssignableFrom(clazz);
    }

    public Descriptor getItemTypeDescriptorOrDie() {
        Class it = getItemType();
        if (it == null) {
            throw new AssertionError(clazz + " is not an array/collection type in " + displayName + ". See https://wiki.jenkins-ci.org/display/JENKINS/My+class+is+missing+descriptor");
        }
        Descriptor d = TIS.get().getDescriptor(it);
        if (d == null)
            throw new AssertionError(it + " is missing its descriptor in " + displayName + ". See https://wiki.jenkins-ci.org/display/JENKINS/My+class+is+missing+descriptor");
        return d;
    }

    /**
     * Returns all the descriptors that produce types assignable to the property type.
     */
    public List<? extends Descriptor> getApplicableDescriptors() {
        return TIS.get().getDescriptorList(clazz);
    }

    /**
     * Returns all the descriptors that produce types assignable to the item type for a collection property.
     */
    public List<? extends Descriptor> getApplicableItemDescriptors() {
        Class itemType = getItemType();
        if (itemType == null) {
            return null;
        }
        return TIS.get().getDescriptorList(itemType);
    }
}
