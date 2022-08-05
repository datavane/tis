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

package com.qlangtech.tis.extension.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.util.DescriptorsJSON;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-07-30 18:44
 **/
public abstract class BaseSubFormProperties extends PluginFormProperties implements IPropertyType {

    public final Field subFormField;
    public Class instClazz;
    public final Descriptor subFormFieldsDescriptor;


    /**
     * @param subFormField
     * @param subFormFieldsDescriptor Example: SelectedTable.DefaultDescriptor
     */
    public BaseSubFormProperties(Field subFormField, Class instClazz, Descriptor subFormFieldsDescriptor) {
        this.subFormField = subFormField;
        this.instClazz = instClazz;
        this.subFormFieldsDescriptor = subFormFieldsDescriptor;
    }

    public abstract PropertyType getPropertyType(String fieldName);
    //PropertyType propertyType = props.fieldsType.get(descField.field)

    public String getSubFormFieldName() {
        return this.subFormField.getName();
    }

    /**
     * 至少选一个
     *
     * @return
     */
    public abstract boolean atLeastOne();

    @Override
    public JSON getInstancePropsJson(Object instance) {
//        Class<?> fieldType = subFormField.getType();
//        if (!Collection.class.isAssignableFrom(fieldType)) {
//            // 现在表单只支持1对n 关系的子表单，因为1对1就没有必要有子表单了
//            throw new UnsupportedOperationException("sub form field:" + subFormField.getName()
//                    + " just support one2multi relationship,declarFieldClass:" + fieldType.getName());
//        }
//        getSubFormPropVal(instance);
//        try {
//            Object o = subFormField.get(instance);

        return createSubFormVals(getSubFormPropVal(instance));

//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
    }

    public abstract DescriptorsJSON.IPropGetter getSubFormIdListGetter();

    public Collection<IdentityName> getSubFormPropVal(Object instance) {
        Class<?> fieldType = subFormField.getType();
        if (!Collection.class.isAssignableFrom(fieldType)) {
            // 现在表单只支持1对n 关系的子表单，因为1对1就没有必要有子表单了
            throw new UnsupportedOperationException("sub form field:" + subFormField.getName()
                    + " just support one2multi relationship,declarFieldClass:" + fieldType.getName());
        }

        try {
            Object o = subFormField.get(instance);
            return (o == null) ? Collections.emptyList() : (Collection<IdentityName>) o;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T newSubDetailed() {
        try {
            // Class<?> aClass = desClazz this.subFormFieldsAnnotation.desClazz();
            return (T) this.instClazz.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final <T> T accept(IVisitor visitor) {
        return visitor.visit(this);
    }

    public abstract JSONObject createSubFormVals(Collection<IdentityName> subFormFieldInstance);

//    public abstract <T> T visitAllSubDetailed(
//            Map<String, /*** attr key */JSONObject> formData
//            , SuFormProperties.ISubDetailedProcess<T> subDetailedProcess);

    public final <T> T visitAllSubDetailed(Map<String, /*** attr key */JSONObject> formData, ISubDetailedProcess<T> subDetailedProcess) {
        String subFormId = null;
        JSONObject subformData = null;
        Map<String, JSONObject> subform = null;
        for (Map.Entry<String, JSONObject> entry : formData.entrySet()) {
            subFormId = entry.getKey();
            subformData = entry.getValue();
            subform = Maps.newHashMap();
            for (String fieldName : subformData.keySet()) {
                subform.put(fieldName, subformData.getJSONObject(fieldName));
            }
            T result = subDetailedProcess.process(subFormId, subform);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public interface ISubDetailedProcess<T> {
        T process(String subFormId, Map<String, JSONObject> subform);
    }
}
