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
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.util.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 在增量构建流程中针对 SelectedTab 属性进行扩展
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-07-30 11:53
 * @see com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory
 **/
public abstract class IncrSelectedTabExtend implements Describable<IncrSelectedTabExtend>, IdentityName {

    public static final String HETERO_ENUM_IDENTITY = "incrSourceSelectedExtend";

    @FormField(identity = true, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String tabName;

    /**
     * 是否是对 Source 的扩展？
     *
     * @return
     */
    public abstract boolean isSource();

    public static IncrTabExtendSuit getIncrTabExtendSuit(UploadPluginMeta pluginMeta) {

        IPluginContext pluginContext = pluginMeta.getPluginContext();
        DataxWriter dataxWriter = null;
        for (DataxWriter w : HeteroEnum.DATAX_WRITER.getPlugins(pluginMeta.getPluginContext(), pluginMeta)) {
            dataxWriter = w;
            break;
        }
        if (dataxWriter == null) {
            throw new IllegalStateException("appname:" + pluginContext.getCollectionName()
                    + " relevant dataXWriter can not be null");
        }
//        Descriptor<SelectedTab> rewriterSelectTabDesc = TIS.get().getDescriptor(SelectedTab.class);
//        Descriptor<DataxWriter> writerDesc = dataxWriter.getDescriptor();
//        if (writerDesc instanceof DataxWriter.IRewriteSuFormProperties) {
//            rewriterSelectTabDesc = ((DataxWriter.IRewriteSuFormProperties) writerDesc).getRewriterSelectTabDescriptor();
//        }

        //===================================================
        Optional<Descriptor<IncrSelectedTabExtend>> sourceExtendDesc
                = MQListenerFactory.getIncrSourceSelectedTabExtendDescriptor(pluginMeta.getDataXName());
        Optional<Descriptor<IncrSelectedTabExtend>> sinkExtendDesc
                = TISSinkFactory.getIncrSinkSelectedTabExtendDescriptor(pluginMeta.getDataXName());
        if (!sourceExtendDesc.isPresent() && !sinkExtendDesc.isPresent()) {
            throw new IllegalStateException("neither selectedTableSourceExtendDesc nor selectedTabSinkExtendDesc is present");
        }
        return new IncrTabExtendSuit(sourceExtendDesc, sinkExtendDesc);
    }

    public static class IncrTabExtendSuit {
        final Optional<Descriptor<IncrSelectedTabExtend>> sourceExtendDesc;
        final Optional<Descriptor<IncrSelectedTabExtend>> sinkExtendDesc;
        //final Descriptor<SelectedTab> rewriterSelectTabDesc;

        public IncrTabExtendSuit(Optional<Descriptor<IncrSelectedTabExtend>> sourceExtendDesc
                , Optional<Descriptor<IncrSelectedTabExtend>> sinkExtendDesc) {
            if (sourceExtendDesc == null) {
                throw new IllegalArgumentException("param sourceExtendDesc can not be null");
            }
            if (sinkExtendDesc == null) {
                throw new IllegalArgumentException("param sinkExtendDesc can not be null");
            }
            this.sourceExtendDesc = sourceExtendDesc;
            this.sinkExtendDesc = sinkExtendDesc;
//            this.rewriterSelectTabDesc = rewriterSelectTabDesc;
        }

        public List<Descriptor> getDescriptors() {
            List<Descriptor> descs = Lists.newArrayList();
            //  descs.add(this.rewriterSelectTabDesc);
            if (sourceExtendDesc.isPresent()) {
                descs.add(sourceExtendDesc.get());
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


    public static Map<String, SelectedTab> getTabExtend(UploadPluginMeta uploadPluginMeta, PluginStore.PluginsUpdateListener... updateListener) {
        PluginStore<IncrSelectedTabExtend> sourceExtendStore = (PluginStore<IncrSelectedTabExtend>) INCR_SELECTED_TAB_EXTEND
                .getPluginStore(uploadPluginMeta.getPluginContext(), uploadPluginMeta);
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
            if (ext.isSource()) {
                tab.setIncrSourceProps(ext);
            } else {
                tab.setIncrSinkProps(ext);
            }
        });
//        sinkExtendStore.getPlugins().forEach((sinkExt) -> {
//            result.get(sinkExt.tabName).setIncrSinkProps(sinkExt);
//        });

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
    public static final HeteroEnum<IncrSelectedTabExtend> INCR_SELECTED_TAB_EXTEND = new HeteroEnum<IncrSelectedTabExtend>(//
            IncrSelectedTabExtend.class, //
            HETERO_ENUM_IDENTITY, //
            "Incr Source Selected Extend", //
            Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            final String dataxName = pluginMeta.getDataXName();// (pluginMeta.getExtraParam(DataxUtils.DATAX_NAME));
            return IncrSelectedTabExtend.getPluginStore(pluginContext, dataxName);
        }
    };

//    @TISExtension
//    public static final HeteroEnum<IncrSelectedTabExtend> INCR_SINK_SELECTED_TAB_EXTEND = new HeteroEnum<IncrSelectedTabExtend>(//
//            IncrSelectedTabExtend.class, //
//            HETERO_SINK_ENUM_IDENTITY, //
//            "Incr Selected Extend", //
//            Selectable.Multi, true) {
//        @Override
//        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
//            final String dataxName = pluginMeta.getDataXName();// (pluginMeta.getExtraParam(DataxUtils.DATAX_NAME));
//            return IncrSelectedTabExtend.getPluginStore(pluginContext, false, dataxName);
//        }
//    };

    public static KeyedPluginStore<IncrSelectedTabExtend> getPluginStore(IPluginContext pluginContext, String appname) {
        KeyedPluginStore.AppKey key = new KeyedPluginStore.AppKey(pluginContext, false
                , appname, IncrSelectedTabExtend.class);
        return pluginStore.get(key);
    }

    private static final transient Memoizer<KeyedPluginStore.AppKey, KeyedPluginStore<IncrSelectedTabExtend>> pluginStore
            = new Memoizer<KeyedPluginStore.AppKey, KeyedPluginStore<IncrSelectedTabExtend>>() {
        @Override
        public KeyedPluginStore<IncrSelectedTabExtend> compute(KeyedPluginStore.AppKey key) {
            return new KeyedPluginStore(key);
        }
    };


    @Override
    public Descriptor<IncrSelectedTabExtend> getDescriptor() {
        Descriptor<IncrSelectedTabExtend> desc = TIS.get().getDescriptor(this.getClass());
        if (!BaseDescriptor.class.isAssignableFrom(desc.getClass())) {
            throw new IllegalStateException("desc class:" + desc.getClass() + " must be extend from " + BaseDescriptor.class.getName());
        }
        return desc;
    }

    protected static abstract class BaseDescriptor extends Descriptor<IncrSelectedTabExtend> {


        @Override
        public PluginFormProperties getPluginFormPropertyTypes(Optional<IPropertyType.SubFormFilter> subFormFilter) {
//            IPropertyType.SubFormFilter filter = null;
//            if (!subFormFilter.isPresent()) {
//                throw new IllegalStateException("subFormFilter must be present");
//            }
//            filter = subFormFilter.get();
//
//            if (filter.subformDetailView) {
//                return new RootFormProperties(filterFieldProp(this)) {
//                    @Override
//                    public JSON getInstancePropsJson(Object instance) {
//                        if (!(instance instanceof IncrSelectedTabExtend)) {
//                            throw new IllegalStateException("instance must be type of "
//                                    + IncrSelectedTabExtend.class.getName() + " but now is " + instance.getClass().getName());
//                        }
//                        return super.getInstancePropsJson(instance);
//                    }
//                };
//            } else {
//                Descriptor parentDesc = filter.getTargetDescriptor();
//                SuFormProperties subProps = (SuFormProperties) parentDesc.getSubPluginFormPropertyTypes(filter.subFieldName);
            return super.getPluginFormPropertyTypes(Optional.empty()); //new IncrSourceExtendSelected(filter.uploadPluginMeta, subProps.subFormField, this);
            //}

        }
    }
}
