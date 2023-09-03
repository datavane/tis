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
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.*;
import com.qlangtech.tis.plugin.*;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.util.*;
import com.qlangtech.tis.datax.impl.DataxReader;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    public static IncrTabExtendSuit getIncrTabExtendSuit(UploadPluginMeta pluginMeta) {

        IPluginContext pluginContext = pluginMeta.getPluginContext();
        DataxWriter dataxWriter = null;
        for (DataxWriter w : HeteroEnum.DATAX_WRITER.getPlugins(pluginMeta.getPluginContext(), pluginMeta)) {
            dataxWriter = w;
            break;
        }
        if (dataxWriter == null) {
            throw new IllegalStateException("appname:" + pluginContext.getCollectionName() + " relevant dataXWriter " + "can not be null");
        }
        //===================================================

        Optional<Descriptor<SelectedTabExtend>> sourceBatchExtendDesc =
                DataxReader.getBatchSourceSelectedTabExtendDescriptor(pluginMeta);

        Optional<Descriptor<SelectedTabExtend>> sourceExtendDesc =
                MQListenerFactory.getIncrSourceSelectedTabExtendDescriptor(pluginMeta.getDataXName());
        Optional<Descriptor<SelectedTabExtend>> sinkExtendDesc =
                TISSinkFactory.getIncrSinkSelectedTabExtendDescriptor(pluginMeta.getDataXName());
        if (!sourceExtendDesc.isPresent() && !sinkExtendDesc.isPresent()) {
            throw new IllegalStateException("neither selectedTableSourceExtendDesc nor selectedTabSinkExtendDesc is " + "present");
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

            List<Descriptor> descs = Lists.newArrayList();
            if (this.sourceBatchExtendDesc.isPresent()) {
                descs.add(sourceBatchExtendDesc.get());
            }
            if (sourceIncrExtendDesc.isPresent()) {
                descs.add(sourceIncrExtendDesc.get());
            }
            if (sinkExtendDesc.isPresent()) {
                descs.add(sinkExtendDesc.get());
            }

            return descs;
        }

        public List<Descriptor> getDescriptorsWithAppendDesc(Descriptor desc) {
            List<Descriptor> descs = getDescriptors();
            descs.add(desc);
            return descs;
        }
    }


    public static Map<String, SelectedTab> getTabExtend(UploadPluginMeta uploadPluginMeta,
                                                        PluginStore.PluginsUpdateListener... updateListener) {
        PluginStore<SelectedTabExtend> sourceExtendStore =
                (PluginStore<SelectedTabExtend>) INCR_SELECTED_TAB_EXTEND.getPluginStore(uploadPluginMeta.getPluginContext(), uploadPluginMeta);
        for (PluginStore.PluginsUpdateListener listener : updateListener) {
            sourceExtendStore.addPluginsUpdateListener(listener);
        }
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

        sourceExtendStore.getPlugins().forEach((ext) -> {

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

    @TISExtension
    public static final HeteroEnum<SelectedTabExtend> INCR_SELECTED_TAB_EXTEND = new HeteroEnum<SelectedTabExtend>(//
            SelectedTabExtend.class, //
            HETERO_ENUM_IDENTITY, //
            "Incr Source Selected Extend", //
            Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            final String dataxName = pluginMeta.getDataXName();// (pluginMeta.getExtraParam(DataxUtils.DATAX_NAME));
            return SelectedTabExtend.getPluginStore(pluginContext, dataxName);
        }
    };


    public static KeyedPluginStore<SelectedTabExtend> getPluginStore(IPluginContext pluginContext, String appname) {
        KeyedPluginStore.AppKey key = new KeyedPluginStore.AppKey(pluginContext, StoreResourceType.parse(false),
                appname, SelectedTabExtend.class);
        return pluginStore.get(key);
    }

    private static final transient Memoizer<KeyedPluginStore.AppKey, KeyedPluginStore<SelectedTabExtend>> pluginStore = new Memoizer<KeyedPluginStore.AppKey, KeyedPluginStore<SelectedTabExtend>>() {
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
        public PluginFormProperties getPluginFormPropertyTypes(Optional<IPropertyType.SubFormFilter> subFormFilter) {
            return super.getPluginFormPropertyTypes(Optional.empty());
        }
    }
}
