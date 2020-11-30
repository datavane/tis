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
package com.qlangtech.tis.util;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.offline.FileSystemFactory;
import com.qlangtech.tis.offline.FlatTableBuilder;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.plugin.incr.IncrStreamFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 表明一种插件的类型
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public enum HeteroEnum {

    FLAT_TABLE_BUILDER(// 
            FlatTableBuilder.class, //
            "flat_table_builder", "宽表构建", Selectable.Single),
    // ////////////////////////////////////////////////////////
    INDEX_BUILD_CONTAINER(// 
            IndexBuilderTriggerFactory.class, //
            "index_build_container", // },
            "索引构建容器", Selectable.Single),
    // ////////////////////////////////////////////////////////
    DS_DUMP(// 
            TableDumpFactory.class, //
            "ds_dump", // },
            "数据导出", Selectable.Single),
    // ////////////////////////////////////////////////////////
    FS(// 
            FileSystemFactory.class, //
            "fs", "存储"),
    // ////////////////////////////////////////////////////////
    MQ(// 
            MQListenerFactory.class, //
            "mq", "MQ消息监听"),
    // ////////////////////////////////////////////////////////
    PARAMS_CONFIG(// 
            ParamsConfig.class, //
            "params-cfg", // },//
            "基础配置", Selectable.Multi),
    // ////////////////////////////////////////////////////////
    INCR_K8S_CONFIG(//
            IncrStreamFactory.class, //
            "incr-config", // },
            "增量配置", Selectable.Single),
    DATASOURCE(//
            DataSourceFactory.class, //
            "datasource", //
            "数据源", //
            Selectable.Single);

    public final String caption;

    private final String identity;

    public final Class<? extends Describable> extensionPoint;

    // public final IDescriptorsGetter descriptorsGetter;
    // private final IItemGetter itemGetter;
    public final Selectable selectable;

    private <// IDescriptorsGetter descriptorsGetter,
            T extends Describable<T>> HeteroEnum(// IDescriptorsGetter descriptorsGetter,
                                                 Class<T> extensionPoint, // IDescriptorsGetter descriptorsGetter,
                                                 String identity, String caption, Selectable selectable) {
        this.extensionPoint = extensionPoint;
        this.caption = caption;
        // this.descriptorsGetter = descriptorsGetter;
        // this.itemGetter = itemGetter;
        this.identity = identity;
        this.selectable = selectable;
    }

    private <// , IDescriptorsGetter descriptorsGetter//, ISaveable saveable
            T extends Describable<T>> HeteroEnum(// , IDescriptorsGetter descriptorsGetter//, ISaveable saveable
                                                 Class<T> extensionPoint, // , IDescriptorsGetter descriptorsGetter//, ISaveable saveable
                                                 String identity, String caption) {
        this(extensionPoint, identity, caption, Selectable.Multi);
    }

    public <T> T getPlugin() {
        if (this.selectable != Selectable.Single) {
            throw new IllegalStateException(this.extensionPoint + " selectable is:" + this.selectable);
        }
        PluginStore store = TIS.getPluginStore(this.extensionPoint);
        return (T) store.getPlugin();
    }

    public <T> List<T> getPlugins(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
        PluginStore store = null;
        if (pluginContext.isCollectionAware()) {
            store = TIS.getPluginStore(pluginContext, pluginContext.getCollectionName(), this.extensionPoint);
        } else if (pluginContext.isDataSourceAware()) {

            PostedDSProp dsProp = PostedDSProp.parse(pluginMeta);
            if (StringUtils.isEmpty(dsProp.getDbname())) {
                return Collections.emptyList();
            }
            store = TIS.getDataBasePluginStore(pluginContext, dsProp);
        } else {
            store = TIS.getPluginStore(this.extensionPoint);
        }
        return store.getPlugins();
    }

    public <T extends Describable<T>> List<Descriptor<T>> descriptors() {
        PluginStore pluginStore = TIS.getPluginStore(this.extensionPoint);
        return pluginStore.allDescriptor();
    }

    public static HeteroEnum of(String identity) {
        for (HeteroEnum he : HeteroEnum.values()) {
            if (StringUtils.equals(he.identity, identity)) {
                return he;
            }
        }
        throw new IllegalStateException("identity:" + identity + " is illegal");
    }
}
