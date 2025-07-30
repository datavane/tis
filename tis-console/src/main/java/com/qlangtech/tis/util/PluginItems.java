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

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opensymphony.xwork2.ActionContext;
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.coredefine.module.action.PluginItemsParser;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.Descriptor.FormVaildateType;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.extension.util.GroovyShellUtil;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.offline.module.action.OfflineDatasourceAction;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IPluginStoreSave;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.SetPluginsResult;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.utils.DBsGetter;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-02-10 12:24
 */
public class PluginItems implements IPluginItemsProcessor {
  private final IPluginEnum heteroEnum;
  private final UploadPluginMeta pluginMeta;
  private final IPluginContext pluginContext;
  private final Context context;
  // private PropValRewrite propValRewrite;

  public List<AttrValMap> items;

  private static final Set<Descriptor> dbUpdateEventObservers = Sets.newHashSet();

  private static final PluginItemsSaveObservable observable = new PluginItemsSaveObservable();

  public static void addPluginItemsSaveObserver(PluginItemsSaveObserver obsv) {
    observable.addObserver(obsv);
  }


  public PluginItems(IPluginContext pluginContext, Context context, UploadPluginMeta pluginMeta) {
    this.heteroEnum = pluginMeta.getHeteroEnum();
    this.pluginMeta = pluginMeta;
    this.pluginContext = pluginMeta.isDisableBizSet() ? new AdapterPluginContext(pluginContext) {
      @Override
      public void setBizResult(Context context, Object result) {
        //super.setBizResult(context, result);
      }
    } : pluginContext;
    this.context = context;
  }


  /**
   * 校验提交的表单
   *
   * @param module
   * @param context
   * @param pluginIndex
   * @param verify
   * @return
   */
  public PluginItemsParser validate(IPluginContext module, Context context, int pluginIndex, boolean verify) {
    List<Descriptor.PluginValidateResult> items = Lists.newArrayList();
    PluginItemsParser parseResult = new PluginItemsParser(items);
    parseResult.pluginItems = this;
    Descriptor.PluginValidateResult validateResult = null;

    AttrValMap attrValMap = null;

    try {

      for (int itemIndex = 0; itemIndex < this.items.size(); itemIndex++) {
        attrValMap = this.items.get(itemIndex);
        try {
          AttrValMap.setCurrentRootPluginValidator(attrValMap.descriptor);
          Descriptor.PluginValidateResult.setValidateItemPos(context, pluginIndex, itemIndex);

          if (!(validateResult = attrValMap.validate((IControlMsgHandler) module
            , context, FormVaildateType.create(verify), Optional.empty())).isValid()) {
            parseResult.faild = true;
          } else {
            validateResult.setDescriptor(attrValMap.descriptor);
            items.add(validateResult);
          }
        } finally {
          AttrValMap.removeCurrentRootPluginValidator();
        }
      }
    } finally {

    }
    return parseResult;
  }

  public static class DefaultDBsGetter extends DBsGetter {
    @Override
    public List<IdentityName> getExistDbs(String... extendClass) {
      return loadExistDbs(false, extendClass);
    }
  }

  /**
   * datax中显示已由数据源使用 <br>
   * must call form Descriptor
   *
   * @param extendClass
   * @return
   */
  private static List<IdentityName> loadExistDbs(boolean listen2SaveEvent, String... extendClass) {
    final ActionContext actionContext = ActionContext.getContext();
    if (extendClass == null || extendClass.length < 1 || actionContext == null) {
      throw new IllegalArgumentException("param extendClass can not be null");
    }

    Descriptor descriptor = GroovyShellUtil.descriptorThreadLocal.get();
    if (listen2SaveEvent && dbUpdateEventObservers.add(Objects.requireNonNull(descriptor, "descriptor can not be null"))) {
      // 当有数据源更新时需要将descriptor的属性重新更新一下
      addPluginItemsSaveObserver(new PluginItemsSaveObserver() {
        @Override
        public void afterSaved(PluginItemsSaveEvent event) {
          if (event.heteroEnum == HeteroEnum.DATASOURCE) {
            descriptor.cleanPropertyTypes();
          }
        }
      });
    }


    IWorkflowDAOFacade wfFacade = BasicServlet.getBeanByType(actionContext.getServletContext(), IWorkflowDAOFacade.class);
    Objects.requireNonNull(wfFacade, "wfFacade can not be null");
    DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
    List<String> extendClazzs = Lists.newArrayList(); // Lists.newArrayList(extendClass).stre;
    for (String type : extendClass) {
      extendClazzs.add(StringUtils.lowerCase(type));
    }
    dbCriteria.createCriteria().andExtendClassIn(extendClazzs);
    List<com.qlangtech.tis.workflow.pojo.DatasourceDb> dbs = wfFacade.getDatasourceDbDAO().selectByExample(dbCriteria);
    List<Descriptor<DataSourceFactory>> dsDescs = HeteroEnum.DATASOURCE.descriptors();
    return dbs.stream().map((db) -> new DBIdentity(db, dsDescs)).collect(Collectors.toList());
    // return dbs.stream().map((db) -> new Option(db.getName(), db.getName())).collect(Collectors.toList());
  }

  private static class DBIdentity implements IdentityName {
    private final com.qlangtech.tis.workflow.pojo.DatasourceDb db;
    private final List<Descriptor<DataSourceFactory>> dsDescs;

    public DBIdentity(DatasourceDb db, List<Descriptor<DataSourceFactory>> dsDescs) {
      this.db = db;
      this.dsDescs = dsDescs;
    }

    @Override
    public String identityValue() {
      return db.getName();
    }

    @Override
    public Class<?> getDescribleClass() {
      for (Descriptor<DataSourceFactory> desc : dsDescs) {
        if (desc.getDisplayName().equalsIgnoreCase(db.getExtendClass())) {
          return desc.clazz;
        }
      }
      throw new IllegalStateException("can not find '" + db.getExtendClass() + "' in " //
        + dsDescs.stream().map((d) -> d.getDisplayName()).collect(Collectors.joining(",")));
    }
  }

  /**
   * datax中显示已由数据源使用 <br>
   * must call form Descriptor
   *
   * @param extendClass
   * @return
   */
  public static List<Option> getExistDbs(String... extendClass) {
    if (OfflineDatasourceAction.existDbs != null) {
      return OfflineDatasourceAction.existDbs;
    }
    return loadExistDbs(true, extendClass).stream().map((db) -> new Option(db.identityValue(), db.identityValue())).collect(Collectors.toList());
  }

  private IPluginStoreSave<?> getStore(List<Descriptor.ParseDescribable<?>> dlist) {
    IPluginStoreSave<?> store = null;
    if (heteroEnum == HeteroEnum.APP_SOURCE) {

      for (Descriptor.ParseDescribable<?> d : dlist) {
        Object inst = d.getInstance();
        if (inst instanceof IdentityName) {
          StoreResourceType resType = ((IAppSource) inst).getResType();
          store = IAppSource.getPluginStore(pluginContext, resType, ((IdentityName) d.getInstance()).identityValue());
          break;
        }
      }

      Objects.requireNonNull(store, "plugin type:" + heteroEnum.getIdentity() + " can not find relevant Store");

    } else if (heteroEnum == HeteroEnum.DATASOURCE || this.pluginContext.isDataSourceAware()) {

      store = new IPluginStoreSave<DataSourceFactory>() {

        PostedDSProp dbExtraProps = createPostedDSProp((pluginMeta));
        DataSourceFactoryPluginStore pluginStore = TIS.getDataSourceFactoryPluginStore(dbExtraProps);

        @Override
        public XmlFile getTargetFile() {
          return pluginStore.getTargetFile();
        }

        private PostedDSProp createPostedDSProp(UploadPluginMeta pluginMeta) {
          for (Descriptor.ParseDescribable<?> plugin : dlist) {
            if (StringUtils.isEmpty(pluginMeta.getExtraParam(com.qlangtech.tis.plugin.ds.DBIdentity.KEY_DB_NAME))) {
              pluginMeta.putExtraParams(com.qlangtech.tis.plugin.ds.DBIdentity.KEY_DB_NAME, ((IdentityName) plugin.getInstance()).identityValue());
            }
            return PostedDSProp.parse(pluginMeta);
          }

          throw new IllegalStateException("has not set：" + com.qlangtech.tis.plugin.ds.DBIdentity.KEY_DB_NAME);
        }

        @Override
        public SetPluginsResult setPlugins(IPluginContext pluginContext, Optional<Context> context, List<Descriptor.ParseDescribable<DataSourceFactory>> dlist, boolean update) {
          SetPluginsResult finalResult = new SetPluginsResult(true, false);
          for (Descriptor.ParseDescribable<DataSourceFactory> plugin : dlist) {
//            if (StringUtils.isEmpty(pluginMeta.getExtraParam(PostedDSProp.KEY_DB_NAME))) {
//              pluginMeta.putExtraParams(PostedDSProp.KEY_DB_NAME, ((IdentityName) plugin.getInstance()).identityValue());
//            }

            SetPluginsResult result = pluginStore.setPlugins(pluginContext, context, Collections.singletonList(plugin), dbExtraProps.isUpdate());
            if (!result.success) {
              return result;
            }
            if (result.cfgChanged) {
              finalResult.cfgChanged = true;
            }
          }
          return finalResult;
        }
      };
    } else if (heteroEnum == HeteroEnum.DATAX_WRITER || heteroEnum == HeteroEnum.DATAX_READER) {

      store = HeteroEnum.getDataXReaderAndWriterStore(this.pluginContext
        , this.heteroEnum == HeteroEnum.DATAX_READER, this.pluginMeta, pluginMeta.getSubFormFilter());

    } else if (heteroEnum == HeteroEnum.uploadCustomizedTPI) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.PARAMS_CONFIG) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.K8S_DEFAULT_IMAGES) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.K8S_FLINK_IMAGES) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.K8S_POWERJOB_IMAGES) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.K8S_SESSION_WORKER) {
      boolean hasSetDataXId = false;
      for (Descriptor.ParseDescribable<?> plugin : dlist) {
        pluginMeta.putExtraParams(StoreResourceType.DATAX_NAME, ((IdentityName) plugin.getInstance()).identityValue());
        hasSetDataXId = true;
        break;
      }
      if (!hasSetDataXId) {
        throw new IllegalStateException("has not set " + StoreResourceType.DATAX_NAME);
      }
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.DATAX_WORKER) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.appJobWorkerTplReWriter) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.noStore) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.TRANSFORMER_RULES) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else {
      if (heteroEnum.isAppNameAware()) {
        if (!this.pluginContext.isCollectionAware()) {
          throw new IllegalStateException(heteroEnum.getExtensionPoint().getName() + " must be collection aware");
        }
        store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
      } else {
        store = TIS.getPluginStore(heteroEnum.getExtensionPoint());
      }
    }
    return Objects.requireNonNull(store, "store can not be null");
  }

  public PluginWithStore getStorePlugins() {
    return new PluginWithStore();
  }


  public class PluginWithStore implements IPluginWithStore {
    final List<Describable> describableList = Lists.newArrayList();
    final IPluginStoreSave<?> store;
    private final List<Descriptor.ParseDescribable<?>> appendHistorical;

    SetPluginsResult setPlugins(IPluginContext pluginContext, Optional<Context> context) {
      return store.setPlugins(pluginContext, context, convert(this.appendHistorical));
    }

    /**
     * 列表所有的 describle 实例
     *
     * @return
     */
    public <T> List<T> listPlugins() {
      return convert(this.appendHistorical).stream()
        .flatMap((d) -> d.getSubFormInstances().stream()).map((d) -> (T) d).collect(Collectors.toList());
    }

    public PluginWithStore() {

      this.appendHistorical = getPlugins(describableList);
      this.store = getStore(appendHistorical);
    }

    @Override
    public void afterVerified() {
      for (Descriptor.ParseDescribable d : this.appendHistorical) {
        d.getSubFormInstances().forEach((plugin) -> {
          if (plugin instanceof IPluginStore.AfterPluginVerified) {
            ((IPluginStore.AfterPluginVerified) plugin).afterVerified(this.store);
          }
        });
      }
    }
  }


  public ItemsSaveResult save(Context context) {
    Objects.requireNonNull(this.pluginContext, "pluginContext can not be null");
    if (items == null) {
      throw new IllegalStateException("prop items can not be null");
    }

    PluginWithStore store = getStorePlugins();

    // store
    //dlist
    SetPluginsResult result = store.setPlugins(pluginContext, Optional.of(context));
    if (!result.success) {
      return new ItemsSaveResult(Collections.emptyList(), result);
    }
    observable.notifyObservers(new PluginItemsSaveEvent(this.pluginContext, this.heteroEnum, store.describableList, result.cfgChanged));
    return new ItemsSaveResult(store.describableList, result);
  }

  private List<Descriptor.ParseDescribable<?>> getPlugins(List<Describable> describableList) {
    AttrValMap attrValMap = null;
    Descriptor.ParseDescribable describable;
    List<Descriptor.ParseDescribable<?>> dlist = Lists.newArrayList();
    if (this.pluginMeta.isAppend()) {
      IPluginStore pluginStore = heteroEnum.getPluginStore(this.pluginContext, this.pluginMeta);
      if (pluginStore != null) {
        List<Describable> plugins = pluginStore.getPlugins();
        boolean firstSkip = false;
        for (Describable p : plugins) {
          if (!firstSkip) {
            firstSkip = true;
            Descriptor.ParseDescribable describablesWithMeta = PluginStore.getDescribablesWithMeta(pluginStore, p);
            dlist.add(describablesWithMeta);
          } else {
            dlist.add(new Descriptor.ParseDescribable(p));
          }
        }
      }
    }
    for (int i = 0; i < this.items.size(); i++) {
      attrValMap = this.items.get(i);
      /**====================================================
       * 将客户端POST数据包装
       ======================================================*/

      describable = attrValMap.createDescribable((IControlMsgHandler) pluginContext, this.context);
      dlist.add(describable);
      if (!describable.subFormFields) {
        describableList.add((Describable) describable.getInstance());
      }
    }
    return dlist;
  }

  private <T extends Describable> List<Descriptor.ParseDescribable<T>> convert(List<Descriptor.ParseDescribable<?>> dlist) {
    return dlist.stream().map((r) -> (Descriptor.ParseDescribable<T>) r).collect(Collectors.toList());
  }

//  @Override
//  public String cerateOrGetNotebook(IControlMsgHandler msgHandler, Context context) throws Exception {
//
//    for (AttrValMap vals : this.items) {
//      return vals.createOrGetNotebook(msgHandler, context);
//    }
//
//    throw new IllegalStateException("items size:" + this.items.size());
//  }

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
      PluginItemsSaveEvent evt = (PluginItemsSaveEvent) arg;
      if (evt.cfgChanged) {
        this.afterSaved(evt);
      }
    }

    public abstract void afterSaved(PluginItemsSaveEvent event);
  }

  public static class PluginItemsSaveEvent {

    public final IPluginContext collectionName;

    public final IPluginEnum heteroEnum;

    public final List<Describable> dlist;

    public final boolean cfgChanged;

    public PluginItemsSaveEvent(IPluginContext collectionName, IPluginEnum heteroEnum, List<Describable> dlist, boolean cfgChanged) {
      this.collectionName = collectionName;
      this.heteroEnum = heteroEnum;
      this.dlist = dlist;
      this.cfgChanged = cfgChanged;
    }
  }
}
