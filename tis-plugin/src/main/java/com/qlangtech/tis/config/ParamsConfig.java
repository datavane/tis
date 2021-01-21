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
package com.qlangtech.tis.config;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
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
        PluginStore pluginStore = TIS.getPluginStore(ParamsConfig.class);
        return pluginStore.getPlugins();
    }

    public abstract <INSTANCE> INSTANCE createConfigInstance();

    public static <T extends IdentityName> T getItem(String identityName, Class<T> type) {
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
