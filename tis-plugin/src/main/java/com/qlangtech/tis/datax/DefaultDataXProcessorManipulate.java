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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IDescribableManipulate;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IPluginStore.ManipuldateProcessor;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.ds.manipulate.ManipulateItemsProcessor;
import com.qlangtech.tis.plugin.ds.manipulate.ManipuldateUtils;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 针对 DefaultDataXProcessor的控制操作，可以实现克隆等操作
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-10 20:56
 **/
public abstract class DefaultDataXProcessorManipulate implements Describable<DefaultDataXProcessorManipulate>, ManipuldateProcessor, IdentityName {

    public static IPluginStore<DefaultDataXProcessorManipulate> getPluginStore(
            IPluginContext context, DataXName appName) {
        if (appName == null) {
            throw new IllegalArgumentException("param appName can not be empty");
        }
        KeyedPluginStore.AppKey appKey = new KeyedPluginStore.AppKey(context, appName.getType(), appName.getPipelineName(), DefaultDataXProcessorManipulate.class);
        IPluginStore<DefaultDataXProcessorManipulate> pluginStore = TIS.getPluginStore(appKey);
        return pluginStore;
    }

    @Override
    public final void manipuldateProcess(IPluginContext pluginContext, Optional<Context> context) {
        synchronized (DefaultDataXProcessorManipulate.class) {
            /**
             * 校验
             */
            ManipulateItemsProcessor itemsProcessor
                    = ManipuldateUtils.instance(pluginContext, context.get(), null
                    , (meta) -> {
                    });
            if (org.apache.commons.lang.StringUtils.isEmpty(itemsProcessor.getOriginIdentityId())) {
                throw new IllegalStateException("originId can not be null");
            }
            if (itemsProcessor == null) {
                return;
            }
            final BasicDesc desc = (BasicDesc) this.getDescriptor();
            ProcessorManipulateManager<? extends DefaultDataXProcessorManipulate>
                    store = DefaultDataXProcessorManipulate.loadPlugins(pluginContext
                    , this.getClass(), DataXName.createDataXPipeline(itemsProcessor.getOriginIdentityId()), desc);
            /**
             * 是否需要删除
             */
            if (itemsProcessor.isDeleteProcess()) {
                // 只删除TIS本地端配置，dolphinscheduler端不进行任何操作
                // store.setPlugins(pluginContext, context, Collections.emptyList());
                store.delete(pluginContext, context, this);
                return;
            }


            if (desc.isManipulateStorable() && !itemsProcessor.isUpdateProcess()) {
                List<? extends DefaultDataXProcessorManipulate> existPlugins = store.getTargetInstancePlugin();
                // 添加操作
                if (CollectionUtils.isNotEmpty(existPlugins)) {
                    for (DefaultDataXProcessorManipulate i : existPlugins) {
                        pluginContext.addErrorMessage(context.get(), "实例'" + i.identityValue() + "'已经配置，不能再创建新实例");
                    }
                    return;
                }
            }

            afterManipuldateProcess(pluginContext, context, itemsProcessor);

            if (desc.isManipulateStorable()) {
                /**
                 *2. 并且将实例持久化在app管道下，当DS端触发会调用 DolphinschedulerDistributedSPIDataXJobSubmit.createPayload()方法获取DS端的WorkflowDAG拓扑视图
                 */
                store.replace(pluginContext, context, this);
            }

        }
    }

    protected abstract void afterManipuldateProcess(IPluginContext pluginContext, Optional<Context> context, ManipulateItemsProcessor itemsProcessor);

    public static class ProcessorManipulateManager<T extends DefaultDataXProcessorManipulate> {
        private final Class<T> targetClazz;
        private final IPluginStore<DefaultDataXProcessorManipulate> store;
        private final List<? extends DefaultDataXProcessorManipulate> plugins;
        private final IDescribableManipulate.IManipulateStorable storable;

        public ProcessorManipulateManager(Class<T> targetClazz
                , IPluginStore<DefaultDataXProcessorManipulate> store
                , List<? extends DefaultDataXProcessorManipulate> plugins, IDescribableManipulate.IManipulateStorable storable) {
            this.targetClazz = targetClazz;
            this.store = store;
            this.plugins = plugins;
            this.storable = storable;
        }

        public List<T> getTargetInstancePlugin() {
            return plugins.stream()
                    .filter((p) -> targetClazz.isAssignableFrom(p.getClass()))
                    .map(targetClazz::cast).collect(Collectors.toList());
        }

        /**
         * 更新或者插入
         *
         * @param beReplace
         */
        public void replace(IPluginContext pluginContext, Optional<Context> context, DefaultDataXProcessorManipulate beReplace) {
            if (!storable.isManipulateStorable()) {
                throw new UnsupportedOperationException();
            }
            List<Descriptor.ParseDescribable<DefaultDataXProcessorManipulate>> dlist = getPluginsExclude(beReplace);
            dlist.add(new Descriptor.ParseDescribable<>(beReplace));
            store.setPlugins(pluginContext, context, dlist, true);
        }

        /**
         * 删除一个Manipulate实例
         *
         * @param pluginContext
         * @param context
         * @param id
         */
        public void delete(IPluginContext pluginContext, Optional<Context> context, IdentityName id) {
            if (!storable.isManipulateStorable()) {
                throw new UnsupportedOperationException();
            }
            List<Descriptor.ParseDescribable<DefaultDataXProcessorManipulate>> dlist = getPluginsExclude(id);
            store.setPlugins(pluginContext, context, dlist, true);
        }

        private List<Descriptor.ParseDescribable<DefaultDataXProcessorManipulate>> getPluginsExclude(IdentityName id) {
            List<Descriptor.ParseDescribable<DefaultDataXProcessorManipulate>> dlist
                    = plugins.stream()
                    .filter((p) ->
                            !StringUtils.equals(Objects.requireNonNull(id, "param id can not be null").identityValue(), p.identityValue()))
                    .map((p) ->
                            new Descriptor.ParseDescribable<DefaultDataXProcessorManipulate>(p)).collect(Collectors.toList());
            return dlist;
        }
    }

    public static <T extends DefaultDataXProcessorManipulate> ProcessorManipulateManager<T>
    loadPlugins(IPluginContext context, final Class<T> clazz, DataXName appName, IDescribableManipulate.IManipulateStorable storable) {
        IPluginStore<DefaultDataXProcessorManipulate> store = getPluginStore(context, appName);
        List<? extends DefaultDataXProcessorManipulate> result = store.getPlugins().stream()
                // .filter((p) -> clazz.isAssignableFrom(p.getClass()))
                .map((p) -> p)
                .collect(Collectors.toList());
        return new ProcessorManipulateManager<>(clazz, store, result, storable);// Pair.of(result, store);
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

    protected static class BasicDesc extends Descriptor<DefaultDataXProcessorManipulate> implements IEndTypeGetter, IDescribableManipulate.IManipulateStorable {
        public BasicDesc() {
            super();
        }
    }

}
