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

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.datax.IncrSourceSelectedTabExtend;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.Memoizer;
import com.qlangtech.tis.util.UploadPluginMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-07-30 18:59
 * @see com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory
 **/
public class IncrSourceExtendSelected extends BaseSubFormProperties {
    private final UploadPluginMeta uploadPluginMeta;

    public IncrSourceExtendSelected(UploadPluginMeta uploadPluginMeta, Field subFormField, Class instClazz, Descriptor subFormFieldsDescriptor) {
        super(subFormField, instClazz, subFormFieldsDescriptor);
        this.uploadPluginMeta = uploadPluginMeta;
    }

    @Override
    public PropertyType getPropertyType(String fieldName) {
        return (PropertyType) subFormFieldsDescriptor.getPropertyType(fieldName);
    }

    @Override
    public boolean atLeastOne() {
        return true;
    }

    @Override
    public DescriptorsJSON.IPropGetter getSubFormIdListGetter() {
        return (filter) -> {
            IPluginStore<?> readerSubFieldStore
                    = HeteroEnum.getDataXReaderAndWriterStore(filter.uploadPluginMeta.getPluginContext(), true, filter.uploadPluginMeta, Optional.of(filter));
            List<?> plugins = readerSubFieldStore.getPlugins();
            return plugins.stream().map((p) -> ((IdentityName) p).identityValue()).collect(Collectors.toList());
        };
    }

    @Override
    public <T> T visitAllSubDetailed(Map<String, JSONObject> formData, SuFormProperties.ISubDetailedProcess<T> subDetailedProcess) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject createSubFormVals(Collection<IdentityName> subFormFieldInstance) {
        //  throw new UnsupportedOperationException(subFormFieldInstance.stream().map((i) -> i.identityValue()).collect(Collectors.joining(",")));
        // IncrSourceSelectedTabExtend.INCR_SELECTED_TAB_EXTEND.
        // IncrSourceSelectedTabExtend.INCR_SELECTED_TAB_EXTEND.
        Memoizer<String, IncrSourceSelectedTabExtend> tabExtends = getTabExtend(this.uploadPluginMeta, subFormFieldsDescriptor);
        JSONObject vals = null;
        try {

            IncrSourceSelectedTabExtend ext = null;
            vals = new JSONObject();
            RootFormProperties props = (new RootFormProperties(getPropertyType()));
            if (subFormFieldInstance != null) {
                for (IdentityName subItem : subFormFieldInstance) {
                    ext = Objects.requireNonNull(tabExtends.get(subItem.identityValue())
                            , "table:" + subItem.identityValue() + " relevant tab ext can not be null");
                    vals.put(subItem.identityValue(), props.getInstancePropsJson(ext));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return vals;
    }

    public static Memoizer<String, IncrSourceSelectedTabExtend> getTabExtend(UploadPluginMeta uploadPluginMeta, Descriptor subFormFieldsDescriptor) {
        IPluginStore<IncrSourceSelectedTabExtend> tabExtendStore = IncrSourceSelectedTabExtend.INCR_SELECTED_TAB_EXTEND
                .getPluginStore(uploadPluginMeta.getPluginContext(), uploadPluginMeta);

        Map<String, IncrSourceSelectedTabExtend> exist = tabExtendStore.getPlugins().stream().collect(Collectors.toMap((t) -> t.identityValue(), (t) -> t));
        Memoizer<String, IncrSourceSelectedTabExtend> result = new Memoizer<String, IncrSourceSelectedTabExtend>() {
            @Override
            public IncrSourceSelectedTabExtend compute(String key) {
                try {
                    IncrSourceSelectedTabExtend tabExtend = exist.get(key);
                    if (tabExtend == null) {
                        tabExtend = (IncrSourceSelectedTabExtend) subFormFieldsDescriptor.clazz.newInstance();
                        tabExtend.setName(key);
                    }
                    return tabExtend;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        return result;

    }

    @Override
    public Set<Map.Entry<String, PropertyType>> getKVTuples() {
        return getPropertyType().entrySet();
    }

    private Map<String, PropertyType> props = null;

    public Map<String, PropertyType> getPropertyType() {
        if (props == null) {
            props = Descriptor.filterFieldProp(subFormFieldsDescriptor);
        }
        return props;
    }
}
