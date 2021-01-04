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

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IPluginStoreSave;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.PostedDSProp;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-02-10 12:24
 */
public class PluginItems {

    private final HeteroEnum heteroEnum;
    private final UploadPluginMeta pluginMeta;
    private final IPluginContext pluginContext;

    public List<AttrValMap> items;

    private static final PluginItemsSaveObservable observable = new PluginItemsSaveObservable();

    public static void addPluginItemsSaveObserver(PluginItemsSaveObserver obsv) {
        observable.addObserver(obsv);
    }

    public PluginItems(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
        this.heteroEnum = pluginMeta.getHeteroEnum();
        this.pluginMeta = pluginMeta;
        this.pluginContext = pluginContext;
    }

    public void save(Context context) {
        if (items == null) {
            throw new IllegalStateException("prop items can not be null");
        }
        Descriptor.ParseDescribable describable = null;
        AttrValMap attrValMap = null;
        List<Descriptor.ParseDescribable<?>> dlist = Lists.newArrayList();
        List<Describable> describableList = Lists.newArrayList();
        for (int i = 0; i < this.items.size(); i++) {
            attrValMap = this.items.get(i);
            describable = attrValMap.createDescribable();
            dlist.add(describable);
            describableList.add(describable.instance);
        }
        IPluginStoreSave<?> store = null;
        if (this.pluginContext.isCollectionAware()) {
            store = TIS.getPluginStore(this.pluginContext, this.pluginContext.getCollectionName(), heteroEnum.extensionPoint);
        } else if (this.pluginContext.isDataSourceAware()) {

            store = new IPluginStoreSave<DataSourceFactory>() {
                @Override
                public boolean setPlugins(Optional<Context> context, List<Descriptor.ParseDescribable<DataSourceFactory>> dlist, boolean update) {
                    for (Descriptor.ParseDescribable<DataSourceFactory> plugin : dlist) {

                        PostedDSProp dbExtraProps = PostedDSProp.parse(pluginMeta);
                        dbExtraProps.setDbname(plugin.instance.identityValue());

                        boolean success = TIS.getDataBasePluginStore(pluginContext, dbExtraProps)
                                .setPlugins(context, Collections.singletonList(plugin), dbExtraProps.isUpdate());
                        if (!success) {
                            return false;
                        }
                    }
                    return true;
                }
            };


        } else {
            store = TIS.getPluginStore(heteroEnum.extensionPoint);
        }
        //dlist
        if (!store.setPlugins(Optional.of(context), convert(dlist))) {
            return;
        }
        observable.notifyObservers(new PluginItemsSaveEvent(this.pluginContext, this.heteroEnum, describableList));
    }

    private <T extends Describable> List<Descriptor.ParseDescribable<T>> convert(List<Descriptor.ParseDescribable<?>> dlist) {
        return dlist.stream().map((r) -> (Descriptor.ParseDescribable<T>) r).collect(Collectors.toList());
    }

    public static class PluginItemsSaveObservable extends Observable {

        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }
    }

    public abstract static class PluginItemsSaveObserver implements Observer {

        @Override
        public final void update(Observable o, Object arg) {
            this.afterSaved((PluginItemsSaveEvent) arg);
        }

        public abstract void afterSaved(PluginItemsSaveEvent event);
    }

    public static class PluginItemsSaveEvent {

        public final IPluginContext collectionName;

        public final HeteroEnum heteroEnum;

        public final List<Describable> dlist;

        public PluginItemsSaveEvent(IPluginContext collectionName, HeteroEnum heteroEnum, List<Describable> dlist) {
            this.collectionName = collectionName;
            this.heteroEnum = heteroEnum;
            this.dlist = dlist;
        }
    }
}
