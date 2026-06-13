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
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.coredefine.module.action.PluginItemsParser;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.extension.util.GroovyShellUtil;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.OptionWithEndType;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.offline.module.action.OfflineDatasourceAction;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IPluginStoreSave;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.SetPluginsResult;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.plugin.ontology.Ontology;
import com.qlangtech.tis.plugin.ontology.OntologyDomain;
import com.qlangtech.tis.plugin.ontology.OntologyProperty;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.runtime.module.misc.FormVaildateType;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.utils.DBsGetter;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.qlangtech.tis.util.UploadPluginMeta.KEY_SKIP_PLUGINS_SAVE;

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

      @Override
      public BasicPipelineValidator getPipelineValidator(BizLogic logicType) {
        throw new UnsupportedOperationException();
      }
    } : pluginContext;
    this.context = context;
  }

  public final List<AttrValMap> getItems() {
    if (CollectionUtils.isEmpty(this.items)) {
      throw new IllegalStateException("items can not be empty");
    }
    return items;
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
  public PluginItemsParser validate(IPluginContext module, Context context, int pluginIndex, FormVaildateType verify) {
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

          if (!(validateResult = attrValMap.validate((IControlMsgHandler) module, context,
            Objects.requireNonNull(verify, "verify can not be null") //
            , Optional.empty())).isValid()) {
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
    public List<DBIdentity> getExistDbs(String... extendClass) {
      return loadExistDbs(false, extendClass);
    }
  }

  private static final String KEY_ALL_TYPE = "all";

  /**
   * datax中显示已由数据源使用 <br>
   * must call form Descriptor
   *
   * @param extendClass
   * @return
   */
  private static List<DBIdentity> loadExistDbs(boolean listen2SaveEvent, String... extendClass) {
    // final ActionContext actionContext = BasicServlet.getActionContext();
    if (extendClass == null || extendClass.length < 1) {
      throw new IllegalArgumentException("param extendClass can not be null");
    }
    Descriptor descriptor = GroovyShellUtil.descriptorThreadLocal.get();
    if (listen2SaveEvent && dbUpdateEventObservers.add(Objects.requireNonNull(descriptor, "descriptor can not be " +
      "null"))) {
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

    IWorkflowDAOFacade wfFacade = BasicServlet.getBeanByType(IWorkflowDAOFacade.class);
    Objects.requireNonNull(wfFacade, "wfFacade can not be null");
    DatasourceDbCriteria dbCriteria = createDatasourceDbCriteria(extendClass);
    List<com.qlangtech.tis.workflow.pojo.DatasourceDb> dbs = wfFacade.getDatasourceDbDAO().selectByExample(dbCriteria);
    List<Descriptor<DataSourceFactory>> dsDescs = HeteroEnum.DATASOURCE.descriptors();
    //    Map<String, Descriptor<DataSourceFactory>> descs =
    //      dsDescs.stream().collect(Collectors.toMap(Descriptor::getDisplayName, (desc) -> desc));
    return dbs.stream() //
      .filter((db) -> StringUtils.isNotEmpty(db.getExtendClass())) //
      .map((db) -> new DBIdentity(db, dsDescs))
      //.filter((db) -> db.getDesc() != null)
      .toList();
    // return dbs.stream().map((db) -> new Option(db.getName(), db.getName())).collect(Collectors.toList());
  }

  private static DatasourceDbCriteria createDatasourceDbCriteria(String[] extendClass) {
    DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
    List<String> extendClazzs = Lists.newArrayList(); // Lists.newArrayList(extendClass).stre;
    for (String type : extendClass) {
      if (KEY_ALL_TYPE.equalsIgnoreCase(type)) {
        dbCriteria.createCriteria().andExtendClassNotNull();
        return dbCriteria;
      } else {
        extendClazzs.add(StringUtils.lowerCase(type));
      }
    }
    dbCriteria.createCriteria().andExtendClassIn(extendClazzs);
    return dbCriteria;
  }

  public static class DBIdentity implements IdentityName, IEndTypeGetter {
    private final com.qlangtech.tis.workflow.pojo.DatasourceDb db;
    private final List<Descriptor<DataSourceFactory>> dsDescs;
    private Descriptor _descriptor;

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
      return getDesc().clazz;
    }

    private Descriptor getDesc() {
      if (_descriptor == null) {
        //        Descriptor<DataSourceFactory> d = null;
        //        if ((d = ) != null) {
        //          return _descriptor = d;
        //        }

        //return _descriptor = dsDescs.get(db.getExtendClass());
        for (Descriptor<DataSourceFactory> desc : dsDescs) {
          if (desc.getDisplayName().equalsIgnoreCase(db.getExtendClass())) {
            return _descriptor = desc;
          }
        }
        throw new IllegalStateException("can not find '" + db.getExtendClass() + "' in " //
          + dsDescs.stream().map(Descriptor::getDisplayName).collect(Collectors.joining(",")));
      }
      return _descriptor;
    }

    @Override
    public EndType getEndType() {
      Descriptor desc = null;
      if ((desc = getDesc()) instanceof IEndTypeGetter) {
        return ((IEndTypeGetter) desc).getEndType();
      }
      return null;
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

    //    Function<DBIdentity, Option> optMapper =
    //      (extendClass.length == 1 && KEY_ALL_TYPE.equalsIgnoreCase(extendClass[0])) //
    //        ? (dbId) -> {
    //        return new OptionWithEndType(dbId.identityValue(), dbId.identityValue(), dbId.getEndType());
    //      } : (dbId) -> {
    //        return new Option(dbId.identityValue(), dbId.identityValue());
    //      };

    Function<DBIdentity, Option> optMapper =
      //      (extendClass.length == 1 && KEY_ALL_TYPE.equalsIgnoreCase(extendClass[0])) //
      //        ? (dbId) -> {
      //        return new OptionWithEndType(dbId.identityValue(), dbId.identityValue(), dbId.getEndType());
      //      } :
      (dbId) -> {
        return new Option(dbId.identityValue(), dbId.identityValue());
      };

    return loadExistDbs(true, extendClass).stream()
      .map(optMapper)
      .collect(Collectors.toList());
  }

  private IPluginStoreSave<?> getStore(List<Descriptor.ParseDescribable<?>> dlist) {

    if (this.pluginMeta.getBoolean(KEY_SKIP_PLUGINS_SAVE)) {
      return IPluginStoreSave.noneSave;
    }

    boolean stepPlugin = true;
    for (Descriptor.ParseDescribable<?> d : dlist) {
      if (!(d.getInstance() instanceof OneStepOfMultiSteps)) {
        stepPlugin = false;
      }
    }
    if (stepPlugin) {
      // 如果在流程插件保存过程不需要持久化
      return IPluginStore.noSaveStore(pluginMeta);
    }

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
              pluginMeta.putExtraParams(com.qlangtech.tis.plugin.ds.DBIdentity.KEY_DB_NAME,
                ((IdentityName) plugin.getInstance()).identityValue());
            }
            return PostedDSProp.parse(pluginMeta);
          }

          throw new IllegalStateException("has not set：" + com.qlangtech.tis.plugin.ds.DBIdentity.KEY_DB_NAME);
        }

        @Override
        public SetPluginsResult setPlugins(IPluginContext pluginContext, Optional<Context> context,
                                           List<Descriptor.ParseDescribable<DataSourceFactory>> dlist, boolean update) {
          SetPluginsResult finalResult = new SetPluginsResult(true, false);
          for (Descriptor.ParseDescribable<DataSourceFactory> plugin : dlist) {
            //            if (StringUtils.isEmpty(pluginMeta.getExtraParam(PostedDSProp.KEY_DB_NAME))) {
            //              pluginMeta.putExtraParams(PostedDSProp.KEY_DB_NAME, ((IdentityName) plugin.getInstance())
            //              .identityValue());
            //            }

            SetPluginsResult result = pluginStore.setPlugins(pluginContext, context,
              Collections.singletonList(plugin), dbExtraProps.isUpdate());
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

      store = HeteroEnum.getDataXReaderAndWriterStore(this.pluginContext, this.heteroEnum == HeteroEnum.DATAX_READER,
        this.pluginMeta, pluginMeta.getSubFormFilter());

    } else if (heteroEnum == HeteroEnum.uploadCustomizedTPI) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.PARAMS_CONFIG || heteroEnum == HeteroEnum.PARAMS_CONFIG_USER_ISOLATION) {
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
      //    } else if (heteroEnum == HeteroEnum.appJobWorkerTplReWriter) {
      //      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.noStore) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == HeteroEnum.TRANSFORMER_RULES) {
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == OntologyDomain.ONTOLOGY_DOMAIN) {
      for (Descriptor.ParseDescribable<?> plugin : dlist) {
        if (StringUtils.isEmpty(pluginMeta.getExtraParam(OntologyDomain.NAME_ONTOLOGY_DOMAIN))) {
          pluginMeta.putExtraParams(OntologyDomain.NAME_ONTOLOGY_DOMAIN,
            ((IdentityName) plugin.getInstance()).identityValue());
        }
        break;
      }
      store = heteroEnum.getPluginStore(this.pluginContext, pluginMeta);
    } else if (heteroEnum == Ontology.ONTOLOGY || heteroEnum == OntologyProperty.ONTOLOGY_PROPERTY) {

      for (Descriptor.ParseDescribable<?> plugin : dlist) {
        if (plugin.getInstance() instanceof IdentityName idInstant) {
          //          if (idInstant instanceof IPluginStore.BeforePluginSaved beforeSave) {
          //            beforeSave.beforeSaved(this.pluginContext, Optional.empty());
          //          }
          OntologyPluginMeta meta = OntologyPluginMeta.createPluginMeta(pluginMeta);
          if (StringUtils.isEmpty(meta.getPluginIdVal())) {
            //

            if (StringUtils.isNotEmpty(meta.getDelegate().getExtraParam(Ontology.KEY_ONTOLOGY))
              && Ontology.OntologyEnum.ontologyEnumsSet.contains(meta.getOntologyType())
            ) {
              meta.setPluginIdVal(idInstant.identityValue());
            } else {
              // ontology propertyType
              if (StringUtils.isEmpty(meta.getObjectType())) {
                throw new IllegalStateException("meta.getObjectType() can not be empty, meta:" + meta.getDelegate());
              }
              if (StringUtils.isEmpty(meta.getObjectTypeProperty(false))) {
                meta.setObjectTypeProperty(idInstant.identityValue());
              }
              // 由于最终是更新objectType实例，所以需要执行以下
              meta.setPluginIdVal(meta.getObjectType());
            }

          }
          break;
        }
      }

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
      return convert(this.appendHistorical).stream().flatMap((d) -> d.getSubFormInstances().stream()).map((d) -> (T) d).collect(Collectors.toList());
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
    notifyNewPluginSaved(store.describableList, result.cfgChanged);
    return new ItemsSaveResult(store.describableList, result);
  }

  private void notifyNewPluginSaved(List<Describable> describableList, boolean cfgChanged) {
    observable.notifyObservers(new PluginItemsSaveEvent(this.pluginContext, this.heteroEnum, describableList,
      cfgChanged));
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

    public PluginItemsSaveEvent(IPluginContext collectionName, IPluginEnum heteroEnum, List<Describable> dlist,
                                boolean cfgChanged) {
      this.collectionName = collectionName;
      this.heteroEnum = heteroEnum;
      this.dlist = dlist;
      this.cfgChanged = cfgChanged;
    }
  }
}
