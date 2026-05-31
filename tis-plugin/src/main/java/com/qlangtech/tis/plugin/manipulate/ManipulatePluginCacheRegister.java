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

package com.qlangtech.tis.plugin.manipulate;

import com.google.common.collect.Maps;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 通用的 Manipulate 插件内存缓存注册表。
 * DataX 和 Ontology 各自持有独立实例，互不干扰。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/29
 * @see com.qlangtech.tis.datax.DefaultDataXProcessorManipulate
 * @see com.qlangtech.tis.plugin.ontology.OntologyDomainManipulate
 */
public class ManipulatePluginCacheRegister<T extends BasicManipuldateProcessor<T>> {

    private final ConcurrentMap<String, TemplateManipulateStore<T>> register = Maps.newConcurrentMap();

    private final Function<String, IPluginStore<T>> pluginStoreLoader;

    public ManipulatePluginCacheRegister(Function<String, IPluginStore<T>> pluginStoreLoader) {
        this.pluginStoreLoader = pluginStoreLoader;
    }

    public TemplateManipulateStore<T> getOrLoad(String key, boolean forceFresh) {
        return register.compute(key, (k, old) -> {
            if (forceFresh || old == null) {
                TemplateManipulateStore<T> store = createStore();
                IPluginStore<T> pluginStore = pluginStoreLoader.apply(k);
                if (forceFresh) {
                    pluginStore.cleanPlugins();
                }
                for (T manipulate : pluginStore.getPlugins()) {
                    store.replace(manipulate);
                }
                return store;
            }
            return old;
        });
    }

    /** 子类可覆盖以返回特化的 store 实例 */
    protected TemplateManipulateStore<T> createStore() {
        return new TemplateManipulateStore<>();
    }

    public ConcurrentMap<String, TemplateManipulateStore<T>> getRegistry() {
        return register;
    }

    /**
     * 通用内存缓存 store，按 identity 存放 manipulate 实例。
     */
    public static class TemplateManipulateStore<T extends BasicManipuldateProcessor<T>> {
        private final Map<IdentityName, T> manipuldateStore = Maps.newHashMap();

        public Collection<T> getManipulates() {
            return manipuldateStore.values();
        }

        public <S extends T> S getManipuldate(IdentityName id, Class<S> clazz) {
            return clazz.cast(manipuldateStore.get(id));
        }

        public void replace(T item) {
            manipuldateStore.put(IdentityName.create(item), item);
        }

        public void remove(IdentityName id) {
            manipuldateStore.remove(IdentityName.create(id));
        }
    }
}
