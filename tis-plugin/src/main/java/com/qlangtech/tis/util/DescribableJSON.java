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
package com.qlangtech.tis.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.plugin.IdentityName;

import java.util.Objects;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DescribableJSON<T extends Describable<T>> {

    private final T instance;

    public final Descriptor<T> descriptor;

    public DescribableJSON(T instance, Descriptor<T> descriptor) {
        this.instance = Objects.requireNonNull(instance, "param instance can not be null");
        this.descriptor = Objects.requireNonNull(descriptor,
                "param descriptor can not be null,plugin type:" + instance.getClass());
    }

    public DescribableJSON(T instance) {
        this(Objects.requireNonNull(instance, "param instance can not be null"), instance.getDescriptor());
    }

    public JSONObject getItemJson() throws Exception {
        return this.getItemJson(Optional.empty());
    }

    public JSONObject getItemJson(Optional<SubFormFilter> subFormFilter) throws Exception {
        PluginFormProperties pluginFormPropertyTypes = descriptor.getPluginFormPropertyTypes(subFormFilter);
        return getItemJson(pluginFormPropertyTypes);
    }

    public JSONObject getItemJson(PluginFormProperties pluginFormPropertyTypes) throws Exception {

        JSONObject item = new JSONObject();

        DescriptorsJSON.setDescInfo(pluginFormPropertyTypes.accept(new PluginFormProperties.IVisitor() {
            @Override
            public Descriptor visit(RootFormProperties props) {
                return descriptor;
            }

            @Override
            public Descriptor visit(BaseSubFormProperties props) {

                //                if(descriptor instanceof CompanionPluginFactory){
                //                    ((CompanionPluginFactory)descriptor).getCompaonPlugin()
                //                }

                return props.subFormFieldsDescriptor;
            }
        }), false, item);


        final JSON vals = pluginFormPropertyTypes.getInstancePropsJson(this.instance);


        item.put(AttrValMap.PLUGIN_EXTENSION_VALS, vals);
        if (instance instanceof IdentityName) {
            item.put(IdentityName.PLUGIN_IDENTITY_NAME, ((IdentityName) instance).identityValue());
        }

        return item;
    }

}
