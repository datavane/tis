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
package com.qlangtech.tis.util;

import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.datax.job.DataXJobWorker;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.ExtensionList;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.offline.*;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ParamsConfigPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.plugin.incr.IncrStreamFactory;
import com.qlangtech.tis.plugin.k8s.K8sImage;
import com.qlangtech.tis.plugin.solr.config.QueryParserFactory;
import com.qlangtech.tis.plugin.solr.config.SearchComponentFactory;
import com.qlangtech.tis.plugin.solr.config.TISTransformerFactory;
import com.qlangtech.tis.plugin.solr.schema.FieldTypeFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

//import com.qlangtech.tis.plugin.incr.IncrStreamFactory;

/**
 * 表明一种插件的类型
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class HeteroEnum<T extends Describable<T>> implements IPluginEnum<T> {

    @TISExtension
    public final static HeteroEnum<FlatTableBuilder> FLAT_TABLE_BUILDER = new HeteroEnum(//
            FlatTableBuilder.class, //
            "flat_table_builder", "宽表构建", Selectable.Single);
    // ////////////////////////////////////////////////////////
    @TISExtension
    public static final HeteroEnum<IndexBuilderTriggerFactory> INDEX_BUILD_CONTAINER = new HeteroEnum<IndexBuilderTriggerFactory>(//
            IndexBuilderTriggerFactory.class, //
            "index_build_container", // },
            "索引构建容器", Selectable.Single);
    // ////////////////////////////////////////////////////////
    @TISExtension
    public static final HeteroEnum<TableDumpFactory> DS_DUMP = new HeteroEnum<TableDumpFactory>(//
            TableDumpFactory.class, //
            "ds_dump", // },
            "数据导出", Selectable.Single);
    // ////////////////////////////////////////////////////////
    @TISExtension
    public static final HeteroEnum<FileSystemFactory> FS = new HeteroEnum<FileSystemFactory>(//
            FileSystemFactory.class, //
            "fs", "存储");
    // ////////////////////////////////////////////////////////
    @TISExtension
    public static final HeteroEnum<MQListenerFactory> MQ = new HeteroEnum<MQListenerFactory>(//
            MQListenerFactory.class, //
            "mq", "Source Factory", Selectable.Multi, true);
    // ////////////////////////////////////////////////////////
    @TISExtension
    public static final HeteroEnum<ParamsConfig> PARAMS_CONFIG = new HeteroEnum<ParamsConfig>(//
            ParamsConfig.class, //
            "params-cfg", // },//
            "基础配置", Selectable.Multi, false);
    // ////////////////////////////////////////////////////////
    @TISExtension
    public static final HeteroEnum<K8sImage> K8S_IMAGES = new HeteroEnum<K8sImage>(//
            K8sImage.class, //
            "k8s-images", // },//
            "K8S-Images", Selectable.Multi, false);
    // ////////////////////////////////////////////////////////

    @TISExtension
    public static final HeteroEnum<DataXJobWorker> DATAX_WORKER = new HeteroEnum<DataXJobWorker>(//
            DataXJobWorker.class, //
            "datax-worker", // },//
            "DataX Worker", Selectable.Single, true);
    // ////////////////////////////////////////////////////////

    @TISExtension
    public static final HeteroEnum<IncrStreamFactory> INCR_STREAM_CONFIG
            = new HeteroEnum<>(//
            IncrStreamFactory.class, //
            "incr-config", // },
            "增量引擎配置", Selectable.Single, true);

    @TISExtension
    public static final HeteroEnum<DataSourceFactory> DATASOURCE = new HeteroEnum<DataSourceFactory>(//
            DataSourceFactory.class, //
            "datasource", //
            "数据源", //
            Selectable.Single);
    //    @TISExtension
    public static final HeteroEnum<FieldTypeFactory> SOLR_FIELD_TYPE = new HeteroEnum<FieldTypeFactory>(//
            FieldTypeFactory.class, //
            "field-type", //
            "字段类型", //
            Selectable.Multi);
    @TISExtension
    public static final HeteroEnum<QueryParserFactory> SOLR_QP = new HeteroEnum<QueryParserFactory>(//
            QueryParserFactory.class, //
            "qp", //
            "QueryParser", //
            Selectable.Multi);
    @TISExtension
    public static final HeteroEnum<SearchComponentFactory> SOLR_SEARCH_COMPONENT = new HeteroEnum<SearchComponentFactory>(//
            SearchComponentFactory.class, //
            "searchComponent", //
            "SearchComponent", //
            Selectable.Multi);
    @TISExtension
    public static final HeteroEnum<TISTransformerFactory> SOLR_TRANSFORMER = new HeteroEnum<TISTransformerFactory>(//
            TISTransformerFactory.class, //
            "transformer", //
            "Transformer", //
            Selectable.Multi);
    @TISExtension
    public static final HeteroEnum<DataxReader> DATAX_READER = new HeteroEnum<DataxReader>(//
            DataxReader.class, //
            "dataxReader", //
            "DataX Reader", //
            Selectable.Multi, true);
    @TISExtension
    public static final HeteroEnum<DataxWriter> DATAX_WRITER = new HeteroEnum<DataxWriter>(//
            DataxWriter.class, //
            "dataxWriter", //
            "DataX Writer", //
            Selectable.Multi, true);
    @TISExtension
    public static final HeteroEnum<IAppSource> APP_SOURCE = new HeteroEnum<IAppSource>(//
            IAppSource.class, //
            "appSource", //
            "App Source", //
            Selectable.Multi, true);

    public final String caption;

    public final String identity;

    public final Class<? extends Describable> extensionPoint;

    // public final IDescriptorsGetter descriptorsGetter;
    // private final IItemGetter itemGetter;
    public final Selectable selectable;
    private final boolean appNameAware;

    public HeteroEnum(
            Class<T> extensionPoint,
            String identity, String caption, Selectable selectable) {
        this(extensionPoint, identity, caption, selectable, false);
    }

    @Override
    public boolean isAppNameAware() {
        return this.appNameAware;
    }

    public HeteroEnum(
            Class<T> extensionPoint,
            String identity, String caption, Selectable selectable, boolean appNameAware) {
        this.extensionPoint = extensionPoint;
        this.caption = caption;
        this.identity = identity;
        this.selectable = selectable;
        this.appNameAware = appNameAware;
    }

    /**
     * 判断实例是否是应该名称唯一的
     *
     * @return
     */
    public boolean isIdentityUnique() {
        return IdentityName.class.isAssignableFrom(this.extensionPoint);
    }


    HeteroEnum(
            Class<T> extensionPoint, String identity, String caption) {
        this(extensionPoint, identity, caption, Selectable.Multi);
    }

    public <T> T getPlugin() {
        if (this.selectable != Selectable.Single) {
            throw new IllegalStateException(this.extensionPoint + " selectable is:" + this.selectable);
        }
        IPluginStore store = TIS.getPluginStore(this.extensionPoint);
        return (T) store.getPlugin();
    }

    /**
     * ref: PluginItems.save()
     *
     * @param pluginContext
     * @param pluginMeta
     * @param
     * @return
     */
    public List<T> getPlugins(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
        IPluginStore store = getPluginStore(pluginContext, pluginMeta);
        if (store == null) {
            return Collections.emptyList();
        }
//        if (this == HeteroEnum.APP_SOURCE) {
//            final String dataxName = (pluginMeta.getExtraParam(DataxUtils.DATAX_NAME));
//            if (StringUtils.isEmpty(dataxName)) {
//                throw new IllegalArgumentException("plugin extra param 'DataxUtils.DATAX_NAME'" + DataxUtils.DATAX_NAME + " can not be null");
//            }
//            store = com.qlangtech.tis.manage.IAppSource.getPluginStore(pluginContext, dataxName);
//        } else if (this == HeteroEnum.DATAX_WRITER || this == HeteroEnum.DATAX_READER) {
//            final String dataxName = pluginMeta.getExtraParam(DataxUtils.DATAX_NAME);
//            if (StringUtils.isEmpty(dataxName)) {
//                throw new IllegalArgumentException("plugin extra param 'DataxUtils.DATAX_NAME': '" + DataxUtils.DATAX_NAME + "' can not be null");
//            }
//            store = (this == HeteroEnum.DATAX_READER) ? DataxReader.getPluginStore(pluginContext, dataxName) : DataxWriter.getPluginStore(pluginContext, dataxName);
//        } else if (pluginContext.isCollectionAware()) {
//            store = TIS.getPluginStore(pluginContext.getCollectionName(), this.extensionPoint);
//        } else if (pluginContext.isDataSourceAware()) {
//            PostedDSProp dsProp = PostedDSProp.parse(pluginMeta);
//            if (StringUtils.isEmpty(dsProp.getDbname())) {
//                return Collections.emptyList();
//            }
//            store = TIS.getDataBasePluginStore(dsProp);
//        } else {
//            store = TIS.getPluginStore(this.extensionPoint);
//        }
        //Objects.requireNonNull(store, "plugin store can not be null");
        List<T> plugins = store.getPlugins();
        if (pluginMeta != null && StringUtils.isNotEmpty(pluginMeta.getTargetPluginDesc())) {
            return plugins.stream()
                    .filter((p) -> StringUtils.equals(p.getDescriptor().getDisplayName(), pluginMeta.getTargetPluginDesc()))
                    .collect(Collectors.toList());
        }

        return plugins;
    }

    @Override
    public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
        IPluginStore store = null;
        if (this == HeteroEnum.APP_SOURCE) {
            final String dataxName = (pluginMeta.getExtraParam(DataxUtils.DATAX_NAME));
            if (StringUtils.isEmpty(dataxName)) {
                throw new IllegalArgumentException("plugin extra param 'DataxUtils.DATAX_NAME'" + DataxUtils.DATAX_NAME + " can not be null");
            }
            store = com.qlangtech.tis.manage.IAppSource.getPluginStore(pluginContext, dataxName);
        } else if (this == HeteroEnum.DATAX_WRITER || this == HeteroEnum.DATAX_READER) {
            final String dataxName = pluginMeta.getExtraParam(DataxUtils.DATAX_NAME);
            if (StringUtils.isEmpty(dataxName)) {
                throw new IllegalArgumentException("plugin extra param 'DataxUtils.DATAX_NAME': '" + DataxUtils.DATAX_NAME + "' can not be null");
            }
            store = (this == HeteroEnum.DATAX_READER) ? DataxReader.getPluginStore(pluginContext, dataxName) : DataxWriter.getPluginStore(pluginContext, dataxName);
        } else if (this == PARAMS_CONFIG) {
            return new ParamsConfigPluginStore();
        } else if (pluginContext.isDataSourceAware()) {
            PostedDSProp dsProp = PostedDSProp.parse(pluginMeta);
            if (StringUtils.isEmpty(dsProp.getDbname())) {
                return null; //Collections.emptyList();
            }
            store = TIS.getDataBasePluginStore(dsProp);
        } else {
            if (this.isAppNameAware()) {
                if (!pluginContext.isCollectionAware()) {
                    throw new IllegalStateException(this.getExtensionPoint().getName() + " must be collection aware");
                }
                store = TIS.getPluginStore(pluginContext.getCollectionName(), this.extensionPoint);
            } else {
                store = TIS.getPluginStore(this.extensionPoint);
            }
        }
        Objects.requireNonNull(store, "plugin store can not be null");
        return store;
    }

    public <T extends Describable<T>> List<Descriptor<T>> descriptors() {
        IPluginStore pluginStore = TIS.getPluginStore(this.extensionPoint);
        return pluginStore.allDescriptor();
    }

    public static <T extends Describable<T>> IPluginEnum<T> of(String identity) {

        ExtensionList<IPluginEnum> pluginEnums = TIS.get().getExtensionList(IPluginEnum.class);

        for (IPluginEnum he : pluginEnums) {
            if (StringUtils.equals(he.getIdentity(), identity)) {
                return he;
            }
        }
        throw new IllegalStateException("identity:" + identity + " is illegal,exist:"
                + pluginEnums.stream().map((h) -> "'" + h.getIdentity() + "'").collect(Collectors.joining(",")));
    }


    @Override
    public Class getExtensionPoint() {
        return this.extensionPoint;
    }

    @Override
    public String getIdentity() {
        return this.identity;
    }

    @Override
    public String getCaption() {
        return this.caption;
    }

    @Override
    public Selectable getSelectable() {
        return this.selectable;
    }
}
