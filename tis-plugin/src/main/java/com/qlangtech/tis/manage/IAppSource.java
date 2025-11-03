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
package com.qlangtech.tis.manage;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Describable.IRefreshable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.StoreResourceTypeGetter;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Optional;

/**
 * 索引实例Srouce， 支持单表、dataflow
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-31 11:16
 */
@Public
public interface IAppSource extends Describable<IAppSource>, StoreResourceTypeGetter, IdentityName {

    static <T extends IAppSource> KeyedPluginStore<T> getPluginStore(IPluginContext context, String appName) {
        return getPluginStore(context, StoreResourceType.DataApp, appName);
    }

    static <T extends IAppSource> KeyedPluginStore<T> getPluginStore(
            IPluginContext context, StoreResourceType resType, String appName) {
        return (KeyedPluginStore<T>) TIS.appSourcePluginStore.get(createAppSourceKey(context, resType, appName));
    }

    static KeyedPluginStore.AppKey createAppSourceKey(IPluginContext context, String appName) {
        return new KeyedPluginStore.AppKey(context, StoreResourceType.DataApp, appName, IAppSource.class);
    }

    static KeyedPluginStore.AppKey createAppSourceKey(IPluginContext context, StoreResourceType resType, String appName) {
        if (StringUtils.isEmpty(appName)) {
            throw new IllegalArgumentException("param appName can not be empty");
        }
        return new KeyedPluginStore.AppKey(context, resType, appName, IAppSource.class);
    }

    static void cleanPluginStoreCache(IPluginContext context, DataXName appName) {
        TIS.appSourcePluginStore.clear(createAppSourceKey(context, appName.getType(), appName.getPipelineName()));
    }

    static void cleanAppSourcePluginStoreCache(IPluginContext context, DataXName appName) {
        IAppSource.cleanPluginStoreCache(context, appName);
        DataxReader.cleanPluginStoreCache(context, false, appName.getPipelineName());
        DataxWriter.cleanPluginStoreCache(context, appName);
        RecordTransformerRules.cleanPluginStoreCache(context, appName);
    }

    static <T extends IAppSource> Optional<T> loadNullable(IPluginContext context, DataXName appName) {
        return loadNullable(context, appName.getType(), appName.getPipelineName());
    }

    static <T extends IAppSource> Optional<T> loadNullable(
            IPluginContext context, StoreResourceType resType, String appName) {
        KeyedPluginStore<T> pluginStore = getPluginStore(context, resType, appName);
        IAppSource appSource = pluginStore.getPlugin();
        return (Optional<T>) Optional.ofNullable(appSource);
    }

    static <T extends IAppSource> T load(String appName) {
        return load(null, DataXName.createDataXPipeline(appName));
    }


    /**
     * save
     *
     * @param appname
     * @param appSource
     */
    static void save(IPluginContext pluginContext, String appname, IAppSource appSource) {
        KeyedPluginStore<IAppSource> pluginStore = getPluginStore(pluginContext, appname);
        Optional<Context> context = Optional.empty();
        pluginStore.setPlugins(pluginContext, context, Collections.singletonList(new Descriptor.ParseDescribable(appSource)));
    }


    /**
     * load
     *
     * @param appName
     * @return
     */
    static <T extends IAppSource> T load(IPluginContext pluginContext, DataXName appName) {
        Optional<IAppSource> iAppSource = loadNullable(pluginContext, appName);
        if (!iAppSource.isPresent()) {
            throw new IllegalStateException("appName:" + appName + " relevant appSource can not be null");
        }
        return (T) iAppSource.get();
    }

    default Descriptor<IAppSource> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }

    default DataXName getDataXName() {
        return new DataXName(this.identityValue(), this.getResType());
    }

    /**
     * 拷贝一份新的实例
     *
     * @param newIdentityVal
     */
    default void copy(String newIdentityVal) {
        throw new UnsupportedOperationException();
    }

    /**
     * DefaultDataXProcessor中需要调用PluginStore 的writeLastModifyTimeStamp()，在客户主动更新了create table DDL之后,所以需要事先将pluginStore实例注入
     *
     * @param pluginStore
     */
    void setPluginStore(PluginStore<IAppSource> pluginStore);
}

