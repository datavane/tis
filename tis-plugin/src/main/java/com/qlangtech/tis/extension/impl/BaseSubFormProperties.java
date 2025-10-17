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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescriptorsJSON;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-07-30 18:44
 **/
public abstract class BaseSubFormProperties extends PluginFormProperties implements IPropertyType {

    public final Field subFormField;
    public Class instClazz;
    public final Descriptor subFormFieldsDescriptor;

    @Override
    public boolean isCollectionType() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param subFormField
     * @param subFormFieldsDescriptor Example: SelectedTable.DefaultDescriptor
     */
    public BaseSubFormProperties(Field subFormField, Class instClazz, Descriptor subFormFieldsDescriptor) {
        this.subFormField = subFormField;
        this.instClazz = instClazz;
        this.subFormFieldsDescriptor = subFormFieldsDescriptor;
    }

    @Override
    public final boolean isIdentity() {
        return false;
    }

    @Override
    public String propertyName() {
        return this.subFormField.getName();
    }

    @Override
    public final Descriptor getDescriptor() {
        return this.subFormFieldsDescriptor;
    }

    public abstract Descriptor getParentPluginDesc();

    public abstract Class<? extends Describable> getParentPluginClass();

    public abstract PropertyType getPropertyType(String fieldName);
    //PropertyType propertyType = props.fieldsType.get(descField.field)

    public String getSubFormFieldName() {
        return this.subFormField.getName();
    }

    public RootFormProperties convertRootFormProps() {
        return new RootFormProperties(this.subFormFieldsDescriptor, this.getKVTuples().stream().collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
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

        return (createSubFormVals(getSubFormPropVal(instance)));

        //        } catch (IllegalAccessException e) {
        //            throw new RuntimeException(e);
        //        }
    }

    public abstract DescriptorsJSON.IPropGetter getSubFormIdListGetter(SubFormFilter filter);

    public Collection<IdentityName> getSubFormPropVal(Object instance) {
        Class<?> fieldType = subFormField.getType();
        if (!Collection.class.isAssignableFrom(fieldType)) {
            // 现在表单只支持1对n 关系的子表单，因为1对1就没有必要有子表单了
            throw new UnsupportedOperationException("sub form field:" + subFormField.getName() + " just support " +
                    "one2multi relationship,declarFieldClass:" + fieldType.getName());
        }

        try {
            Object o = subFormField.get(instance);
            Collection<IdentityName> subItems = (o == null) ? Collections.emptyList() : (Collection<IdentityName>) o;
            // 在pipeline创建阶段，当用户先选择 mysql-> doris 类型的同步，选择完表，然后又回退到端类型选择页面，重新选择了 mysql-> mysql，再进入下一步选择页面节点机会出错
            boolean containNotEqualClassForItem = false;
            for (IdentityName itme : subItems) {
                if (itme.getClass() != this.instClazz) {
                    containNotEqualClassForItem = true;
                }
            }
            return containNotEqualClassForItem
                    ? subItems.stream().filter((subitem) -> subitem.getClass() == instClazz).collect(Collectors.toList())
                    : subItems;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T newSubDetailed() {
        try {
            return (T) this.instClazz.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final <T> T accept(IVisitor visitor) {
        return visitor.visit(this);
    }

    protected abstract Map<String, SelectedTab> getSelectedTabs(Collection<IdentityName> subFormFieldInstance);

    protected abstract void addSubItems(SelectedTab ext, JSONArray pair) throws Exception;

    public final JSONObject createSubFormVals(Collection<IdentityName> subFormFieldInstance) {
        Map<String, SelectedTab> tabExtends = getSelectedTabs(subFormFieldInstance);
        JSONObject vals = null;
        try {
            SelectedTab ext = null;
            vals = new JSONObject();
            JSONArray pair = null;
            if (subFormFieldInstance != null) {
                for (IdentityName subItem : subFormFieldInstance) {
                    ext = tabExtends.get(subItem.identityValue());
                    if (ext == null) {
                        continue;
                    }
                    pair = new JSONArray();
                    addSubItems(ext, pair);

                    vals.put(subItem.identityValue(), pair);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return vals;
    }


    public final <T> T visitAllSubDetailed(
            Context context,
            AttrValMap.IAttrVals formData, ISubDetailedProcess<T> subDetailedProcess) {
        String subFormId = null;
        //JSONObject subformData = null;
        AttrValMap attrVals = null;
        try {
            for (Map.Entry<String /*tableName*/, JSONArray> entry : formData.asSubFormDetails().entrySet()) {
                subFormId = entry.getKey();

                //  subform = Maps.newHashMap();
                for (Object o : entry.getValue()) {
                    attrVals = AttrValMap.parseDescribableMap(Optional.empty(), (JSONObject) o);
                    // KEY_VALIDATE_ITEM_SUBITEM_DETAILED_PK_VAL
                    if (context != null) {
                        context.put(DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_SUBITEM_DETAILED_PK_VAL, subFormId);
                    }

                    T result = subDetailedProcess.process(subFormId, attrVals);
                    if (result != null) {
                        return result;
                    }
                }

            }
        } finally {
            if (context != null) {
                context.remove(DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_SUBITEM_DETAILED_PK_VAL);
            }
        }
        return null;
    }

    public interface ISubDetailedProcess<T> {
        T process(String subFormId, AttrValMap attrVals);
    }
}
