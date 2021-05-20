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
package com.qlangtech.tis.manage;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.plugin.KeyedPluginStore;

import java.util.Collections;
import java.util.Optional;

/**
 * 索引实例Srouce， 支持单表、dataflow
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-31 11:16
 */
public interface IAppSource extends Describable<IAppSource> {

    static <T extends IAppSource> KeyedPluginStore<T> getPluginStore(String appName) {
        return (KeyedPluginStore<T>) new KeyedPluginStore(new AppKey(appName));
    }

    static <T extends IAppSource> Optional<T> loadNullable(String appName) {
        KeyedPluginStore<T> pluginStore = getPluginStore(appName);
        IAppSource appSource = pluginStore.getPlugin();
        return (Optional<T>) Optional.ofNullable(appSource);
    }

    /**
     * save
     *
     * @param appname
     * @param appSource
     */
    static void save(String appname, IAppSource appSource) {
        KeyedPluginStore<IAppSource> pluginStore = getPluginStore(appname);
        Optional<Context> context = Optional.empty();
        pluginStore.setPlugins(null, context, Collections.singletonList(new Descriptor.ParseDescribable(appSource)));
    }

    /**
     * load
     *
     * @param appName
     * @return
     */
    static <T extends IAppSource> T load(String appName) {
        Optional<IAppSource> iAppSource = loadNullable(appName);
        if (!iAppSource.isPresent()) {
            throw new IllegalStateException("appName:" + appName + " relevant appSource can not be null");
        }
        return (T) iAppSource.get();
    }

    default Descriptor<IAppSource> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }


    class AppKey extends KeyedPluginStore.Key<IAppSource> {
        public AppKey(String collection) {
            super(IFullBuildContext.NAME_APP_DIR, collection, IAppSource.class);
        }
    }
}

