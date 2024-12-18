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

package com.qlangtech.tis.plugin.datax;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.SetPluginsResult;
import com.qlangtech.tis.plugin.StoreResourceType;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Memoizer;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.MapUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 在增量构建流程中针对 SelectedTab 属性进行扩展
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-07-30 11:53
 * @see com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory
 **/
public abstract class SelectedTabExtend implements Describable<SelectedTabExtend>, IdentityName {

    public static final String HETERO_ENUM_IDENTITY = "incrSourceSelectedExtend";

    @FormField(identity = true, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String tabName;

    /**
     * 是否是对 Source 的扩展？
     *
     * @return
     */
    public abstract ExtendType getExtendType();

    public enum ExtendType {
        INCR_SOURCE, INCR_SINK, BATCH_SOURCE;
    }

    public static IncrTabExtendSuit getIncrTabExtendSuit(boolean incrExtend, UploadPluginMeta pluginMeta) {

        IPluginContext pluginContext = pluginMeta.getPluginContext();
        if (incrExtend) {
            DataxWriter dataxWriter = null;
            for (DataxWriter w : HeteroEnum.DATAX_WRITER.getPlugins(pluginMeta.getPluginContext(), pluginMeta)) {
                dataxWriter = w;
                break;
            }
            if (dataxWriter == null) {
                throw new IllegalStateException("appname:" + pluginContext.getCollectionName() + " relevant " +
                        "dataXWriter " + "can not be null");
            }
        }

        //===================================================
        Optional<Descriptor<SelectedTabExtend>> sourceBatchExtendDesc = Optional.empty();
        Optional<Descriptor<SelectedTabExtend>> sourceExtendDesc = Optional.empty();
        Optional<Descriptor<SelectedTabExtend>> sinkExtendDesc = Optional.empty();

        if (incrExtend) {
            sourceExtendDesc = MQListenerFactory.getIncrSourceSelectedTabExtendDescriptor(pluginMeta.getDataXName());
            sinkExtendDesc = TISSinkFactory.getIncrSinkSelectedTabExtendDescriptor(pluginMeta.getDataXName());
            if (!sourceExtendDesc.isPresent() && !sinkExtendDesc.isPresent()) {
                throw new IllegalStateException("neither selectedTableSourceExtendDesc nor selectedTabSinkExtendDesc "
                        + "is " + "present");
            }
        } else {
            sourceBatchExtendDesc = DataxReader.getBatchSourceSelectedTabExtendDescriptor(pluginMeta);
        }
        return new IncrTabExtendSuit(sourceBatchExtendDesc, sourceExtendDesc, sinkExtendDesc);
    }

    public static class IncrTabExtendSuit {
        final Optional<Descriptor<SelectedTabExtend>> sourceIncrExtendDesc;
        final Optional<Descriptor<SelectedTabExtend>> sinkExtendDesc;
        final Optional<Descriptor<SelectedTabExtend>> sourceBatchExtendDesc;

        public IncrTabExtendSuit(Optional<Descriptor<SelectedTabExtend>> sourceBatchExtendDesc,
                                 Optional<Descriptor<SelectedTabExtend>> sourceExtendDesc,
                                 Optional<Descriptor<SelectedTabExtend>> sinkExtendDesc) {
            if (sourceExtendDesc == null) {
                throw new IllegalArgumentException("param sourceExtendDesc can not be null");
            }
            if (sinkExtendDesc == null) {
                throw new IllegalArgumentException("param sinkExtendDesc can not be null");
            }
            this.sourceIncrExtendDesc = sourceExtendDesc;
            this.sinkExtendDesc = sinkExtendDesc;
            this.sourceBatchExtendDesc = Objects.requireNonNull(sourceBatchExtendDesc,
                    "sourceBatchExtendDesc can " + "not be null");
        }

        public List<Descriptor> getDescriptors() {


            List<Descriptor> descs = Lists.newArrayList(sourceBatchExtendDesc, sourceIncrExtendDesc, sinkExtendDesc) //
                    .stream().filter((optDesc) -> optDesc.isPresent()).map((optDesc) -> optDesc.get()).collect(Collectors.toList());

            return descs;
        }

        public List<Descriptor> getDescriptorsWithAppendDesc(Descriptor desc) {
            List<Descriptor> descs = getDescriptors();
            descs.add(desc);
            return descs;
        }
    }

    public static Map<String, SelectedTab> getTabExtend(IPluginContext pluginContext, String appname,
                                                        PluginStore.PluginsUpdateListener... updateListener) {
        UploadPluginMeta extMeta = UploadPluginMeta.appnameMeta(pluginContext, appname);
        return getTabExtend(extMeta, updateListener);
    }

    public static void clearTabExtend(IPluginContext pluginContext, String appname) {
        UploadPluginMeta extMeta = UploadPluginMeta.appnameMeta(pluginContext, appname);
        getTabExtendPluginStore(extMeta).forEach((pluginStore) -> {
            pluginStore.cleanPlugins();
        });
    }

    private static Stream<PluginStore<SelectedTabExtend>> getTabExtendPluginStore(UploadPluginMeta uploadPluginMeta,
                                                                                  PluginStore.PluginsUpdateListener... updateListener) {
        PluginStore<SelectedTabExtend> incrExtendStore =
                (PluginStore<SelectedTabExtend>) INCR_SELECTED_TAB_EXTEND.getPluginStore(uploadPluginMeta.getPluginContext(), uploadPluginMeta);

        PluginStore<SelectedTabExtend> batchExtendStore =
                (PluginStore<SelectedTabExtend>) BATCH_SOURCE_SELECTED_TAB_EXTEND.getPluginStore(uploadPluginMeta.getPluginContext(), uploadPluginMeta);

        for (PluginStore.PluginsUpdateListener listener : updateListener) {
            incrExtendStore.addPluginsUpdateListener(listener);
            //  batchExtendStore.addPluginsUpdateListener(listener);
        }

        Stream<PluginStore<SelectedTabExtend>> stream = Lists.newArrayList(incrExtendStore, batchExtendStore).stream();
        return stream;
    }


    public static Map<String, SelectedTab> getTabExtend(UploadPluginMeta uploadPluginMeta,
                                                        PluginStore.PluginsUpdateListener... updateListener) {
//        PluginStore<SelectedTabExtend> incrExtendStore =
//                (PluginStore<SelectedTabExtend>) INCR_SELECTED_TAB_EXTEND.getPluginStore(uploadPluginMeta.getPluginContext(), uploadPluginMeta);
//
//        PluginStore<SelectedTabExtend> batchExtendStore =
//                (PluginStore<SelectedTabExtend>) BATCH_SOURCE_SELECTED_TAB_EXTEND.getPluginStore(uploadPluginMeta.getPluginContext(), uploadPluginMeta);
//
//        for (PluginStore.PluginsUpdateListener listener : updateListener) {
//            incrExtendStore.addPluginsUpdateListener(listener);
//            //  batchExtendStore.addPluginsUpdateListener(listener);
//        }
        Memoizer<String, SelectedTab> result = new Memoizer<String, SelectedTab>() {
            @Override
            public SelectedTab compute(String key) {
                try {
                    SelectedTab tabExtend = new SelectedTab();
                    tabExtend.name = key;
                    return tabExtend;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        //  getTabExtendPluginStore(uploadPluginMeta,updateListener).flatMap()

        //  Lists.newArrayList(incrExtendStore, batchExtendStore) //
        getTabExtendPluginStore(uploadPluginMeta, updateListener)
                .flatMap((store) -> store.getPlugins().stream()).forEach((ext) -> {
                    SelectedTab tab = result.get(ext.tabName);
                    switch (ext.getExtendType()) {
                        case INCR_SINK:
                            tab.setIncrSinkProps(ext);
                            break;
                        case INCR_SOURCE:
                            tab.setIncrSourceProps(ext);
                            break;
                        case BATCH_SOURCE:
                            tab.setSourceProps(ext);
                            break;
                        default:
                            throw new IllegalStateException("illegl extendType:" + ext.getExtendType());
                    }
                });

        return result.snapshot();
    }

    @Override
    public String identityValue() {
        return this.tabName;
    }

    public void setName(String name) {
        this.tabName = name;
    }

    public static IPluginStore wrapSubFormStore(IPluginContext pluginContext, String dataxName,
                                                KeyedPluginStore<SelectedTab> subFormStore) {

        final KeyedPluginStore<SelectedTabExtend> tabExtendStore =
                SelectedTabExtend.getBatchPluginStore(pluginContext, dataxName);
        return new IPluginStore() {

            @Override
            public XmlFile getTargetFile() {
                return subFormStore.getTargetFile();
            }

            @Override
            public void copyConfigFromRemote() {
                subFormStore.copyConfigFromRemote();
                tabExtendStore.copyConfigFromRemote();
            }

            @Override
            public long getWriteLastModifyTimeStamp() {
                return subFormStore.getWriteLastModifyTimeStamp();
            }

            @Override
            public SetPluginsResult setPlugins(IPluginContext pluginContext, Optional optional, List dlist,
                                               boolean update) {

                List<Descriptor.ParseDescribable> plugins = (List<Descriptor.ParseDescribable>) dlist;


                Map<String, SelectedTabExtend> selectedExtendTabs = Maps.newHashMap();
                // 需要将selected tab 和 extendTab 分流
                Map<String, SelectedTab> selectedTabs = Maps.newHashMap();

                for (Descriptor.ParseDescribable plugin : plugins) {

                    for (Object p : plugin.getSubFormInstances()) {
                        if (p instanceof SelectedTabExtend) {
                            SelectedTabExtend stExt = (SelectedTabExtend) p;
                            if (stExt.getExtendType() != ExtendType.BATCH_SOURCE) {
                                throw new IllegalStateException("tabExtend type must be :" //
                                        + ExtendType.BATCH_SOURCE + " but now is:" + stExt.getExtendType());
                            }
                            selectedExtendTabs.put(stExt.tabName, stExt);
                            //  selectedExtendTabs.add(stExt);
                        } else {
                            SelectedTab tab = (SelectedTab) p;
                            selectedTabs.put(tab.getName(), tab);
                        }
                    }
                }


                if (MapUtils.isNotEmpty(selectedExtendTabs)) {

                    for (Map.Entry<String, SelectedTabExtend> entry : selectedExtendTabs.entrySet()) {
                        selectedTabs.get(entry.getKey()).setSourceProps(entry.getValue());
                    }
                    tabExtendStore.setPlugins(pluginContext, optional,
                            Collections.singletonList(new Descriptor.ParseDescribable(Lists.newArrayList(selectedExtendTabs.values()))), update);
                }
                if (MapUtils.isEmpty(selectedTabs)) {
                    throw new IllegalStateException("selectedTabs can not be empty");
                }
                return subFormStore.setPlugins(pluginContext, optional,
                        Collections.singletonList(new Descriptor.ParseDescribable(Lists.newArrayList(selectedTabs.values()))), update);
            }

            @Override
            public Describable getPlugin() {
                return subFormStore.getPlugin();
            }

            @Override
            public List getPlugins() {
                return subFormStore.getPlugins();
            }

            @Override
            public void cleanPlugins() {
                subFormStore.cleanPlugins();
                tabExtendStore.cleanPlugins();
            }

            @Override
            public List<Descriptor> allDescriptor() {
                return subFormStore.allDescriptor().stream().map((desc) -> desc).collect(Collectors.toList());
            }

            @Override
            public Describable find(String name, boolean throwNotFoundErr) {
                return subFormStore.find(name, throwNotFoundErr);
            }
        };
    }

    @TISExtension
    public static final HeteroEnum<SelectedTabExtend> INCR_SELECTED_TAB_EXTEND //
            = createTabExtendHetero((pluginContext, dataxName) -> {
        return SelectedTabExtend.getIncrPluginStore(pluginContext, dataxName);
    });
    //            new HeteroEnum<SelectedTabExtend>(//
    //            SelectedTabExtend.class, //
    //            HETERO_ENUM_IDENTITY, //
    //            "Incr Source Selected Extend", //
    //            Selectable.Multi, true) {
    //        @Override
    //        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
    //            final String dataxName = pluginMeta.getDataXName();// (pluginMeta.getExtraParam(DataxUtils
    //            .DATAX_NAME));
    //            return SelectedTabExtend.getIncrPluginStore(pluginContext, dataxName);
    //        }
    //    };

    private static HeteroEnum<SelectedTabExtend> createTabExtendHetero(
            BiFunction<IPluginContext, String, IPluginStore> storeCreator) {
        return new HeteroEnum<SelectedTabExtend>(//
                SelectedTabExtend.class, //
                HETERO_ENUM_IDENTITY, //
                "Incr Source Selected Extend", //
                Selectable.Multi, true) {
            @Override
            public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
                final String dataxName = pluginMeta.getDataXName();
                return storeCreator.apply(pluginContext, dataxName);

            }
        };
    }

    @TISExtension
    public static final HeteroEnum<SelectedTabExtend> BATCH_SOURCE_SELECTED_TAB_EXTEND =
            createTabExtendHetero((pluginContext, dataxName) -> {
                return SelectedTabExtend.getBatchPluginStore(pluginContext, dataxName);
            });
    //            new HeteroEnum<SelectedTabExtend>(//
    //            SelectedTabExtend.class, //
    //            HETERO_ENUM_IDENTITY, //
    //            "Incr Source Selected Extend", //
    //            Selectable.Multi, true) {
    //        @Override
    //        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
    //            final String dataxName = pluginMeta.getDataXName();// (pluginMeta.getExtraParam(DataxUtils
    //            .DATAX_NAME));
    //            return SelectedTabExtend.getIncrPluginStore(pluginContext, dataxName);
    //        }
    //    };


    public static KeyedPluginStore<SelectedTabExtend> getIncrPluginStore(IPluginContext pluginContext, String appname) {

        KeyedPluginStore.PluginClassCategory pluginCategory =
                new KeyedPluginStore.PluginClassCategory(SelectedTabExtend.class, "_incr");

        KeyedPluginStore.AppKey key = new KeyedPluginStore.AppKey(pluginContext, StoreResourceType.parse(false),
                appname, pluginCategory);
        return pluginStore.get(key);
    }

    public static KeyedPluginStore<SelectedTabExtend> getBatchPluginStore( //
                                                                           IPluginContext pluginContext,
                                                                           String appname) {
        KeyedPluginStore.AppKey key = new KeyedPluginStore.AppKey(pluginContext, StoreResourceType.parse(false),
                appname, new KeyedPluginStore.PluginClassCategory(SelectedTabExtend.class, "_batch"));
        return pluginStore.get(key);
    }

    private static final transient Memoizer<KeyedPluginStore.AppKey, KeyedPluginStore<SelectedTabExtend>> pluginStore  //
            = new Memoizer<KeyedPluginStore.AppKey, KeyedPluginStore<SelectedTabExtend>>() {
        @Override
        public KeyedPluginStore<SelectedTabExtend> compute(KeyedPluginStore.AppKey key) {
            return new KeyedPluginStore(key);
        }
    };


    @Override
    public Descriptor<SelectedTabExtend> getDescriptor() {
        Descriptor<SelectedTabExtend> desc = TIS.get().getDescriptor(this.getClass());
        if (!BaseDescriptor.class.isAssignableFrom(desc.getClass())) {
            throw new IllegalStateException("desc class:" + desc.getClass() + " must be extend from " + BaseDescriptor.class.getName());
        }
        return desc;
    }

    protected static abstract class BaseDescriptor extends Descriptor<SelectedTabExtend> {
        @Override
        public PluginFormProperties getPluginFormPropertyTypes(Optional<SubFormFilter> subFormFilter) {
            return super.getPluginFormPropertyTypes(Optional.empty());
        }
    }
}
