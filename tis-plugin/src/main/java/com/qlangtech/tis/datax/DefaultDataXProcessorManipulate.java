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

package com.qlangtech.tis.datax;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IPluginStore.ManipuldateProcessor;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.StoreResourceType;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对 DefaultDataXProcessor的控制操作，可以实现克隆等操作
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-10 20:56
 **/
public abstract class DefaultDataXProcessorManipulate implements Describable<DefaultDataXProcessorManipulate>, ManipuldateProcessor {

    private static IPluginStore<DefaultDataXProcessorManipulate> getPluginStore(
            IPluginContext context, String appName) {
        if (StringUtils.isEmpty(appName)) {
            throw new IllegalArgumentException("param appName can not be empty");
        }
        KeyedPluginStore.AppKey appKey = new KeyedPluginStore.AppKey(context, StoreResourceType.DataApp, appName, DefaultDataXProcessorManipulate.class);
        IPluginStore<DefaultDataXProcessorManipulate> pluginStore = TIS.getPluginStore(appKey);
        return pluginStore;
    }

    public static <T extends DefaultDataXProcessorManipulate> Pair<List<T>, IPluginStore<DefaultDataXProcessorManipulate>>
    loadPlugins(IPluginContext context, final Class<T> clazz, String appName) {
        IPluginStore<DefaultDataXProcessorManipulate> store = getPluginStore(context, appName);
        List<T> result = store.getPlugins().stream()
                .filter((p) -> clazz.isAssignableFrom(p.getClass()))
                .map((p) -> (T) p)
                .collect(Collectors.toList());
        return Pair.of(result, store);
    }

    @Override
    public final Descriptor<DefaultDataXProcessorManipulate> getDescriptor() {
        Descriptor<DefaultDataXProcessorManipulate> desc = Describable.super.getDescriptor();
        if (!(desc instanceof DefaultDataXProcessorManipulate.BasicDesc)) {
            throw new IllegalStateException("descriptor:"
                    + desc.getClass().getName() + " must extend from " + DefaultDataXProcessorManipulate.BasicDesc.class.getSimpleName());
        }
        return desc;
    }

    protected static class BasicDesc extends Descriptor<DefaultDataXProcessorManipulate> {
        public BasicDesc() {
            super();
        }
    }

}
