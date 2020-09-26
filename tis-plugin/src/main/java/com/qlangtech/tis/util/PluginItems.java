/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.util;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.PluginStore;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-02-10 12:24
 */
public class PluginItems {

    private final HeteroEnum heteroEnum;

    private final ICollectionName collectionName;

    public List<AttrValMap> items;

    private static final PluginItemsSaveObservable observable = new PluginItemsSaveObservable();

    public static void addPluginItemsSaveObserver(PluginItemsSaveObserver obsv) {
        observable.addObserver(obsv);
    }

    public PluginItems(ICollectionName collectionName, HeteroEnum heteroEnum) {
        this.heteroEnum = heteroEnum;
        this.collectionName = collectionName;
    }

    public void save() {
        if (items == null) {
            throw new IllegalStateException("prop items can not be null");
        }
        Descriptor.ParseDescribable describable = null;
        AttrValMap attrValMap = null;
        List<Descriptor.ParseDescribable> dlist = Lists.newArrayList();
        List<Describable> describableList = Lists.newArrayList();
        for (int i = 0; i < this.items.size(); i++) {
            attrValMap = this.items.get(i);
            describable = attrValMap.createDescribable();
            dlist.add(describable);
            describableList.add(describable.instance);
        }
        PluginStore store = null;
        if (this.collectionName.isAware()) {
            store = TIS.getPluginStore(this.collectionName.getCollectionName(), heteroEnum.extensionPoint);
        } else {
            store = TIS.getPluginStore(heteroEnum.extensionPoint);
        }
        store.setPlugins(dlist);
        observable.notifyObservers(new PluginItemsSaveEvent(this.collectionName, this.heteroEnum, describableList));
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

        public final ICollectionName collectionName;

        public final HeteroEnum heteroEnum;

        public final List<Describable> dlist;

        public PluginItemsSaveEvent(ICollectionName collectionName, HeteroEnum heteroEnum, List<Describable> dlist) {
            this.collectionName = collectionName;
            this.heteroEnum = heteroEnum;
            this.dlist = dlist;
        }
    }
}
