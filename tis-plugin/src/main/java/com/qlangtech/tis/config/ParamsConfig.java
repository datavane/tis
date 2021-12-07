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
package com.qlangtech.tis.config;

import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.util.HeteroEnum;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class ParamsConfig implements Describable<ParamsConfig>, IdentityName {

    public static List<ParamsConfig> getItems() {
        IPluginStore pluginStore = TIS.getPluginStore(ParamsConfig.class);
        return pluginStore.getPlugins();
    }

    public abstract <INSTANCE> INSTANCE createConfigInstance();

    public static <T extends IdentityName> T getItem(String identityName, Class<T> type) {
        if (StringUtils.isEmpty(identityName)) {
            throw new IllegalArgumentException("param identityName can not be empty");
        }
        List<T> items = getItems(type);
        for (T i : items) {
            if (StringUtils.equals(i.identityValue(), identityName)) {
                return i;
            }
        }
        throw new IllegalStateException("Name:" + identityName + ",type:" + type.getName() + " can not find relevant config in["
                + items.stream().map((r) -> r.identityValue()).collect(Collectors.joining(",")) + "]");
    }

    // 取得所有的配置项
    public static <T> List<T> getItems(Class<T> type) {
        List<ParamsConfig> items = getItems();
        return items.stream().filter((r) -> type.isAssignableFrom(r.getClass())).map((r) -> (T) r).collect(Collectors.toList());
    }

    @Override
    @JSONField(serialize = false)
    public final Descriptor<ParamsConfig> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }

    // public static DescriptorExtensionList<ParamsConfig, Descriptor<ParamsConfig>> all() {
    // DescriptorExtensionList<ParamsConfig, Descriptor<ParamsConfig>> descriptorList
    // = TIS.get().getDescriptorList(ParamsConfig.class);
    // return descriptorList;
    // }
    public static List<Descriptor<ParamsConfig>> all(Class<?> type) {
        List<Descriptor<ParamsConfig>> desc = HeteroEnum.PARAMS_CONFIG.descriptors();
        return desc.stream().filter((r) -> type.isAssignableFrom(r.getT())).collect(Collectors.toList());
    }
}
