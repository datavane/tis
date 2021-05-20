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
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.plugin.IPluginStoreSave;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import org.apache.commons.lang3.StringUtils;

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

  public List<Describable> save(Context context) {
    if (items == null) {
      throw new IllegalStateException("prop items can not be null");
    }
    Descriptor.ParseDescribable describable = null;
    AttrValMap attrValMap = null;
    List<Descriptor.ParseDescribable<?>> dlist = Lists.newArrayList();
    List<Describable> describableList = Lists.newArrayList();
    for (int i = 0; i < this.items.size(); i++) {
      attrValMap = this.items.get(i);
      describable = attrValMap.createDescribable(pluginContext);
      dlist.add(describable);
      describableList.add((Describable) describable.instance);
    }
    IPluginStoreSave<?> store = null;
    if (this.pluginContext.isCollectionAware()) {
      store = TIS.getPluginStore(this.pluginContext.getCollectionName(), heteroEnum.extensionPoint);
    } else if (heteroEnum == HeteroEnum.APP_SOURCE) {

      for (Descriptor.ParseDescribable<?> d : dlist) {
        if (d.instance instanceof IdentityName) {
          store = IAppSource.getPluginStore(((IdentityName) d.instance).identityValue());
          break;
        }
      }

      Objects.requireNonNull(store, "plugin type:" + heteroEnum.name() + " can not find relevant Store");

    } else if (this.pluginContext.isDataSourceAware()) {

      store = new IPluginStoreSave<DataSourceFactory>() {
        @Override
        public boolean setPlugins(IPluginContext pluginContext, Optional<Context> context
          , List<Descriptor.ParseDescribable<DataSourceFactory>> dlist, boolean update) {
          for (Descriptor.ParseDescribable<DataSourceFactory> plugin : dlist) {

            PostedDSProp dbExtraProps = PostedDSProp.parse(pluginMeta);
            dbExtraProps.setDbname(plugin.instance.identityValue());

            boolean success = TIS.getDataBasePluginStore(dbExtraProps)
              .setPlugins(pluginContext, context, Collections.singletonList(plugin), dbExtraProps.isUpdate());
            if (!success) {
              return false;
            }
          }
          return true;
        }
      };
    } else if (heteroEnum == HeteroEnum.DATAX_WRITER || heteroEnum == HeteroEnum.DATAX_READER) {
      final String dataxName = pluginMeta.getExtraParam(DataxUtils.DATAX_NAME);
      if (StringUtils.isEmpty(dataxName)) {
        throw new IllegalArgumentException("plugin extra param " + DataxUtils.DATAX_NAME + " can not be null");
      }
//      if ((heteroEnum == HeteroEnum.DATAX_READER)) {
//        for (Descriptor.ParseDescribable<?> dataXReader : dlist) {
//          DataSourceMeta sourceMeta = (DataSourceMeta) dataXReader.instance;
//          pluginContext.setBizResult(context, sourceMeta.getTablesInDB());
//        }
//      }
      store = (heteroEnum == HeteroEnum.DATAX_READER) ? DataxReader.getPluginStore(dataxName) : DataxWriter.getPluginStore(dataxName);

    } else {
      store = TIS.getPluginStore(heteroEnum.extensionPoint);
    }
    //dlist
    if (!store.setPlugins(pluginContext, Optional.of(context), convert(dlist))) {
      return Collections.emptyList();
    }
    observable.notifyObservers(new
      PluginItemsSaveEvent(this.pluginContext, this.heteroEnum, describableList));
    return describableList;
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
