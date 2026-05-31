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
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IDescribableManipulate;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.alert.AlertChannel;
import com.qlangtech.tis.plugin.ds.manipulate.ManipulateItemsProcessor;
import com.qlangtech.tis.plugin.manipulate.BasicManipuldateProcessor;
import com.qlangtech.tis.plugin.manipulate.ManipulatePluginCacheRegister;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 针对 DefaultDataXProcessor的控制操作，可以实现克隆等操作
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-10 20:56
 * @see com.qlangtech.tis.plugin.ontology.OntologyDomainManipulate
 **/
public abstract class DefaultDataXProcessorManipulate
        extends BasicManipuldateProcessor<DefaultDataXProcessorManipulate> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDataXProcessorManipulate.class);

    private static ManipulatePluginCacheRegister<DefaultDataXProcessorManipulate> cacheRegister;

    /**
     * Get the in-memory manipulate registry for all loaded pipelines.
     * Used by DAGSchedulerActor to discover pipelines with cron schedules.
     *
     * @return unmodifiable view of the manipulate registry
     */
    @SuppressWarnings("unchecked")
    public static ConcurrentMap<String, AbstractTemplateManipulateStore> getManipulateRegistry() {
        ensureCacheRegister();
        return (ConcurrentMap<String, AbstractTemplateManipulateStore>) (ConcurrentMap<?, ?>) cacheRegister.getRegistry();
    }

    private static void ensureCacheRegister() {
        if (cacheRegister == null) {
            cacheRegister = new ManipulatePluginCacheRegister<DefaultDataXProcessorManipulate>(
                    pipe -> getPluginStore(null, DataXName.createDataXPipeline(pipe))) {
                @Override
                protected AbstractTemplateManipulateStore createStore() {
                    return new AbstractTemplateManipulateStore();
                }
            };

            File appDir = new File(TIS.pluginCfgRoot, StoreResourceType.DataApp.getType());
            String[] subDirs = null;
            if (!appDir.exists() || (subDirs = appDir.list()) == null) {
                return;
            }

            List<String> target = Lists.newArrayList();
            for (String pipe : subDirs) {
                DataXName dataXName = DataXName.createDataXPipeline(pipe);
                if (getStoreKey(null, dataXName).getStoreXmlFile().exists()) {
                    target.add(pipe);
                    getManipulateStore(dataXName, false);
                }
            }

            logger.info("scan subDirs count:{},target " + DefaultDataXProcessorManipulate.class.getSimpleName() +
                    ":{}", subDirs.length, String.join(",", target));
        }
    }

    /**
     * @param dataXName
     * @param forceFresh 强制从文件系统中获取最新的
     */
    public static AbstractTemplateManipulateStore getManipulateStore(DataXName dataXName,
                                                                     final boolean forceFresh) {
        if (dataXName == null) {
            throw new IllegalArgumentException("param pipelineName can not be empty");
        }
        ensureCacheRegister();
        return (AbstractTemplateManipulateStore) cacheRegister.getOrLoad(dataXName.getPipelineName(), forceFresh);
    }

    public static final class AbstractTemplateManipulateStore
            extends ManipulatePluginCacheRegister.TemplateManipulateStore<DefaultDataXProcessorManipulate> {

        public DefaultDataXProcessorManipulate.MonitorForEventsManager getAlertManager() {
            DefaultDataXProcessorManipulate manipuldate =
                    this.getManipuldate(IdentityName.create(DefaultDataXProcessorManipulate.MonitorForEventsManager.KEY_ALERT), DefaultDataXProcessorManipulate.class);
            return (DefaultDataXProcessorManipulate.MonitorForEventsManager) manipuldate;
        }
    }

    public interface MonitorForEventsManager extends IManipulateStatus {
        String KEY_ALERT = "alert";

        public boolean isActivate();

        public void addSendCount();

        public List<AlertChannel> getAlertChannels();
    }


    public static IPluginStore<DefaultDataXProcessorManipulate> getPluginStore(IPluginContext context,
                                                                               DataXName appName) {
        if (appName == null) {
            throw new IllegalArgumentException("param appName can not be empty");
        }
        KeyedPluginStore.AppKey appKey = getStoreKey(context, appName);

        IPluginStore<DefaultDataXProcessorManipulate> pluginStore = TIS.getPluginStore(appKey);

        return pluginStore;
    }

    private static KeyedPluginStore.AppKey getStoreKey(IPluginContext context, DataXName appName) {
        return new KeyedPluginStore.AppKey(context, appName.getType(), appName.getPipelineName(),
                DefaultDataXProcessorManipulate.class);
    }

    @Override
    protected final IPluginStore<DefaultDataXProcessorManipulate> loadPluginStore(IPluginContext pluginContext,
                                                                                  ManipulateItemsProcessor itemsProcessor) {
        DataXName pipelineName = itemsProcessor.getOriginIdentityId()
                .orElseThrow(() -> new IllegalStateException("originId can not be null"));
        return getPluginStore(pluginContext, pipelineName);
    }


    @Override
    protected void afterManipuldateProcess(IPluginContext pluginContext, Optional<Context> context,
                                           ManipulateItemsProcessor itemsProcessor) {
        DataXName pipelineName = itemsProcessor.getOriginIdentityId().orElse(null);
        if (pipelineName == null) {
            return;
        }
        AbstractTemplateManipulateStore memStore = getManipulateStore(pipelineName, false);
        if (itemsProcessor.isDeleteProcess()) {
            memStore.remove(this);
        } else {
            memStore.replace(this);
        }
    }

    public static class ProcessorManipulateManager<T extends DefaultDataXProcessorManipulate> {
        private final Class<T> targetClazz;
        private final IPluginStore<DefaultDataXProcessorManipulate> store;
        private final List<? extends DefaultDataXProcessorManipulate> plugins;
        private final IDescribableManipulate.IManipulateStorable storable;
        private final DataXName pipelineName;


        public ProcessorManipulateManager(DataXName pipelineName, Class<T> targetClazz,
                                          IPluginStore<DefaultDataXProcessorManipulate> store, List<?
                        extends DefaultDataXProcessorManipulate> plugins,
                                          IDescribableManipulate.IManipulateStorable storable) {
            this.targetClazz = targetClazz;
            this.store = store;
            this.plugins = plugins;
            this.storable = storable;
            this.pipelineName = Objects.requireNonNull(pipelineName, "pipelineName can not be null");
        }

        public List<T> getTargetInstancePlugin() {
            return plugins.stream().filter((p) -> targetClazz.isAssignableFrom(p.getClass())).map(targetClazz::cast).collect(Collectors.toList());
        }

        /**
         * 更新或者插入
         *
         * @param beReplace
         */
        public void replace(IPluginContext pluginContext, Optional<Context> context,
                            DefaultDataXProcessorManipulate beReplace) {
            if (!storable.isManipulateStorable()) {
                throw new UnsupportedOperationException();
            }
            List<Descriptor.ParseDescribable<DefaultDataXProcessorManipulate>> dlist = getPluginsExclude(beReplace);
            dlist.add(new Descriptor.ParseDescribable<>(beReplace));
            store.setPlugins(pluginContext, context, dlist, true);
            AbstractTemplateManipulateStore manipulateStore = getManipulateStore();

            manipulateStore.replace(beReplace);

        }

        private final AbstractTemplateManipulateStore getManipulateStore() {
            return DefaultDataXProcessorManipulate.getManipulateStore(this.pipelineName, false);
            //            return processorManipulateRegister.computeIfAbsent(
            //                    this.pipelineName.getPipelineName(), (pipe) -> new
            //                    DataXProcessorTemplateManipulateStore());
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
            AbstractTemplateManipulateStore manipulateStore = getManipulateStore();
            manipulateStore.remove(id);
        }

        private List<Descriptor.ParseDescribable<DefaultDataXProcessorManipulate>> getPluginsExclude(IdentityName id) {
            List<Descriptor.ParseDescribable<DefaultDataXProcessorManipulate>> dlist =
                    plugins.stream().filter((p) -> !StringUtils.equals(Objects.requireNonNull(id,
                            "param id can not " + "be null").identityValue(), p.identityValue())).map((p) -> new Descriptor.ParseDescribable<DefaultDataXProcessorManipulate>(p)).collect(Collectors.toList());
            return dlist;
        }
    }

    public static <T extends DefaultDataXProcessorManipulate> ProcessorManipulateManager<T> loadPlugins(IPluginContext context, final Class<T> clazz, DataXName pipelineName, IDescribableManipulate.IManipulateStorable storable) {
        IPluginStore<DefaultDataXProcessorManipulate> store = getPluginStore(context, pipelineName);
        List<? extends DefaultDataXProcessorManipulate> result =
                store.getPlugins().stream().map((p) -> p).collect(Collectors.toList());
        return new ProcessorManipulateManager<>(pipelineName, clazz, store, result, storable);// Pair.of(result, store);
    }

    protected static class BasicDesc extends BasicManipuldateProcessor.BasicDesc<DefaultDataXProcessorManipulate>
            implements IEndTypeGetter {
        public BasicDesc() {
            super();
        }
    }

}
