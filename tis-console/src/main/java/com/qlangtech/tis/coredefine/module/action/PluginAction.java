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
package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.*;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.extension.model.UpdateCenter;
import com.qlangtech.tis.extension.model.UpdateSite;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.install.InstallState;
import com.qlangtech.tis.install.InstallUtil;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.offline.module.manager.impl.OfflineManager;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.util.*;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
@InterceptorRefs({@InterceptorRef("tisStack")})
public class PluginAction extends BasicModule {
  private static final Logger logger = LoggerFactory.getLogger(PluginAction.class);
  private OfflineManager offlineManager;

  static {

    PluginItems.addPluginItemsSaveObserver((new PluginItems.PluginItemsSaveObserver() {
      // 通知Assemble节点更新pluginStore的缓存
      @Override
      public void afterSaved(PluginItems.PluginItemsSaveEvent event) {
        final String extendPoint = event.heteroEnum.getExtensionPoint().getName();
        // @see "com.qlangtech.tis.fullbuild.servlet.TaskStatusServlet"
        notifyPluginUpdate2AssembleNode(DescriptorsJSON.KEY_EXTEND_POINT + "=" + extendPoint, "pluginStore");
      }
    }));
  }

  private static void notifyPluginUpdate2AssembleNode(String applyParams, String targetResource) {
    try {
      URL url = new URL(Config.getAssembleHttpHost() + "/task_status?" + applyParams);
      HttpUtils.get(url, new ConfigFileContext.StreamProcess<Void>() {
        @Override
        public Void p(int status, InputStream stream, Map<String, List<String>> headerFields) {
          logger.info("has apply clean " + targetResource + " cache by " + applyParams);
          return null;
        }
      });
    } catch (Exception e) {
      logger.warn("apply clean " + targetResource + " cache faild " + e.getMessage());
    }
  }

  /**
   * 取得字段的帮助信息
   *
   * @param context
   */
  public void doGetPluginFieldHelp(Context context) {
    String pluginImpl = this.getString("impl");
    String fieldName = this.getString("field");
    if (StringUtils.isEmpty(pluginImpl)) {
      throw new IllegalArgumentException("param 'impl' can not be null");
    }
    if (StringUtils.isEmpty(fieldName)) {
      throw new IllegalArgumentException("param 'field' can not be null");
    }
    Descriptor targetDesc = TIS.get().getDescriptor(pluginImpl);

    PropertyType fieldProp = (PropertyType) targetDesc.getPropertyType(fieldName);

    PluginExtraProps.Props props = fieldProp.extraProp;
    if (!props.isAsynHelp()) {
      throw new IllegalStateException("plugin:" + pluginImpl + ",field:" + fieldName + " is not support async help content fecthing");
    }
    this.setBizResult(context, props.getAsynHelp());
  }

  /**
   * 取得安装进度状态
   *
   * @param context
   */
  public void doGetUpdateCenterStatus(Context context) {
    UpdateCenter updateCenter = TIS.get().getUpdateCenter();
//    List<JSONObject> jobStats = Lists.newArrayList();
//    JSONObject stat = null;
//    for (UpdateCenter.UpdateCenterJob job : updateCenter.getJobs()) {
//      stat = new JSONObject();
//      stat.put("id", job.id);
//      if (job instanceof UpdateCenter.InstallationJob) {
//        UpdateCenter.InstallationJob installJob = (UpdateCenter.InstallationJob) job;
//        stat.put("name", installJob.getDisplayName());
//      }
//      jobStats.add(stat);
//    }
    List<UpdateCenter.UpdateCenterJob> jobs = updateCenter.getJobs();

    Collections.sort(jobs, (a, b) -> {
      // 保证最新的安装job排列在最上面
      return b.id - a.id;
    });
    setBizResult(context, jobs);
  }

  /**
   * 取得已经安装的插件
   *
   * @param context
   */
  public void doGetInstalledPlugins(Context context) {
    List<String> extendpoint = getExtendpointParam();
    PluginManager pluginManager = TIS.get().getPluginManager();
    JSONArray response = new JSONArray();
    JSONObject pluginInfo = null;
    UpdateSite.Plugin info = null;
    for (PluginWrapper plugin : pluginManager.getPlugins()) {

      pluginInfo = new JSONObject();
      pluginInfo.put("installed", true);
      info = plugin.getInfo();
      if (info != null) {
        // pluginInfo.put("meta", info);
        pluginInfo.put("releaseTimestamp", info.releaseTimestamp);
        pluginInfo.put("excerpt", info.excerpt);
      }

      if (CollectionUtils.isNotEmpty(extendpoint)) {
        if (info == null) {
          continue;
        }

        if (!CollectionUtils.containsAny(info.extendPoints.keySet(), extendpoint)) {
          continue;
        }
        pluginInfo.put("extendPoints", info.extendPoints);
      }

      pluginInfo.put("name", plugin.getShortName());
      pluginInfo.put("version", plugin.getVersion());
      pluginInfo.put("title", plugin.getDisplayName());
      pluginInfo.put("active", plugin.isActive());
      pluginInfo.put("enabled", plugin.isEnabled());
      // pluginInfo.put("bundled", plugin.isBundled);
      pluginInfo.put("deleted", plugin.isDeleted());
      pluginInfo.put("downgradable", plugin.isDowngradable());
      pluginInfo.put("website", plugin.getUrl());
      List<PluginWrapper.Dependency> dependencies = plugin.getDependencies();
      if (dependencies != null && !dependencies.isEmpty()) {
        Option o = null;
        List<Option> dependencyMap = Lists.newArrayList();
        for (PluginWrapper.Dependency dependency : dependencies) {
          o = new Option(dependency.shortName, dependency.version);
          dependencyMap.add(o);
        }
        pluginInfo.put("dependencies", dependencyMap);
      } else {
        pluginInfo.put("dependencies", Collections.emptyList());
      }
      response.add(pluginInfo);
    }
    this.setBizResult(context, response);
  }

  /**
   * 安装插件
   *
   * @param context
   */
  public void doInstallPlugins(Context context) {
    JSONArray pluginsInstall = this.parseJsonArrayPost();
    if (pluginsInstall.size() < 1) {
      this.addErrorMessage(context, "请选择需要安装的插件");
      return;
    }
    long start = System.currentTimeMillis();
    boolean dynamicLoad = true;
    UUID correlationId = UUID.randomUUID();
    UpdateCenter updateCenter = TIS.get().getUpdateCenter();
    List<Future<UpdateCenter.UpdateCenterJob>> installJobs = new ArrayList<>();
    JSONObject willInstall = null;
    String pluginName = null;
    UpdateSite.Plugin plugin = null;
    List<PluginWrapper> batch = new ArrayList<>();
    for (int i = 0; i < pluginsInstall.size(); i++) {
      willInstall = pluginsInstall.getJSONObject(i);
      pluginName = willInstall.getString("name");
      if (StringUtils.isEmpty(pluginName)) {
        throw new IllegalStateException("plugin name can not empty");
      }
      plugin = updateCenter.getPlugin(pluginName);
      Future<UpdateCenter.UpdateCenterJob> installJob = plugin.deploy(dynamicLoad, correlationId, batch);
      installJobs.add(installJob);
    }
    if (dynamicLoad) {
      installJobs.add(updateCenter.addJob(updateCenter.new CompleteBatchJob(batch, start, correlationId)));
    }

    final TIS tis = TIS.get();

    //TODO: 每个安装流程都要进来
    if (true || !tis.getInstallState().isSetupComplete()) {
      tis.setInstallState(InstallState.INITIAL_PLUGINS_INSTALLING);
      updateCenter.persistInstallStatus();
      new Thread() {
        @Override
        public void run() {
          boolean failures = false;
          INSTALLING:
          while (true) {
            try {
              updateCenter.persistInstallStatus();
              Thread.sleep(500);
              failures = false;
              for (Future<UpdateCenter.UpdateCenterJob> jobFuture : installJobs) {
                if (!jobFuture.isDone() && !jobFuture.isCancelled()) {
                  continue INSTALLING;
                }
                UpdateCenter.UpdateCenterJob job = jobFuture.get();
                if (job instanceof UpdateCenter.InstallationJob && ((UpdateCenter.InstallationJob) job).status instanceof UpdateCenter.DownloadJob.Failure) {
                  failures = true;
                }
              }
            } catch (Exception e) {
              logger.warn("Unexpected error while waiting for initial plugin set to install.", e);
            }
            break;
          }
          updateCenter.persistInstallStatus();
          if (!failures) {

            InstallUtil.proceedToNextStateFrom(InstallState.INITIAL_PLUGINS_INSTALLING);
            // 为了让Assemble等节点的uberClassLoader重新加载一次，需要主动向Assemble等节点发送一个指令
            notifyPluginUpdate2AssembleNode(TIS.KEY_ACTION_CLEAN_TIS + "=true", "TIS");
          }
        }
      }.start();
    }
  }

  /**
   * 取得当前可以被安装的插件
   *
   * @param context
   */
  public void doGetAvailablePlugins(Context context) {

    List<String> extendpoint = getExtendpointParam();
    Pager pager = this.createPager();
    pager.setTotalCount(Integer.MAX_VALUE);
    List<UpdateSite.Plugin> availables = TIS.get().getUpdateCenter().getAvailables();
    if (CollectionUtils.isNotEmpty(extendpoint)) {
      availables = availables.stream().filter((plugin) -> {
        return CollectionUtils.containsAny(plugin.extendPoints.keySet(), extendpoint);
        // return plugin.extendPoints.containsKey(extendpoint.get());
      }).collect(Collectors.toList());
    }

    this.setBizResult(context, new PaginationResult(pager, availables));
  }

  private List<String> getExtendpointParam() {
    return Arrays.asList(this.getStringArray("extendpoint"));
//    return Optional.ofNullable(this.getString("extendpoint"));
  }

  /**
   * @param context
   * @throws Exception
   */
  public void doSwitchExtensionPointShow(Context context) throws Exception {
    boolean open = this.getBoolean("switch");
    TIS tis = TIS.get();
    tis.saveComponent(tis.loadGlobalComponent().setShowExtensionDetail(open));
  }

  public void doGetExtensionPointShow(Context context) throws Exception {
    TIS tis = TIS.get();
    this.setBizResult(context, tis.loadGlobalComponent().isShowExtensionDetail());
  }

  /**
   * @param context
   */
  public void doGetDescriptor(Context context) {
    String displayName = this.getString("name");
    if (StringUtils.isEmpty(displayName)) {
      throw new IllegalArgumentException("request param 'impl' can not be null");
    }
    IPluginEnum hetero = HeteroEnum.of(this.getString("hetero"));
    List<Descriptor<Describable>> descriptors = hetero.descriptors();
    for (Descriptor desc : descriptors) {
      if (StringUtils.equals(desc.getDisplayName(), displayName)) {
        this.setBizResult(context, new DescriptorsJSON(desc).getDescriptorsJSON());
        return;
      }
    }

    throw new IllegalStateException("displayName:" + displayName + " relevant Descriptor can not be null");
  }

  /**
   * @param context
   */
  public void doGetDescsByExtendpoint(Context context) throws Exception {
    List<String> extendpoints = this.getExtendpointParam();
    if (CollectionUtils.isEmpty(extendpoints)) {
      throw new IllegalArgumentException("extendpoints can not be null");
    }

    for (String extend : extendpoints) {
      this.setBizResult(context
        , new DescriptorsJSON(TIS.get().getDescriptorList((Class<Describable>) Class.forName(extend))).getDescriptorsJSON());
      return;
    }

    throw new IllegalArgumentException("extendpoints can not be null");
  }

  /**
   * plugin form 的子表单的某条详细记录被点击
   *
   * @param context
   * @throws Exception
   */
  public void doSubformDetailedClick(Context context) throws Exception {
    List<UploadPluginMeta> pluginsMeta = getPluginMeta();
    //String targetMethod = this.getString("targetMethod");
    // String[] params = StringUtils.split(this.getString("params"), ",");
    List<Describable> plugins = null;
    Map<String, String> execContext = Maps.newHashMap();
    execContext.put("id", this.getString("id"));

    IPluginEnum heteroEnum = null;
    for (UploadPluginMeta meta : pluginsMeta) {
      heteroEnum = meta.getHeteroEnum();
      plugins = heteroEnum.getPlugins(this, meta);
      for (Describable p : plugins) {

        PluginFormProperties pluginFormPropertyTypes = p.getDescriptor().getPluginFormPropertyTypes(meta.getSubFormFilter());
        pluginFormPropertyTypes.accept(new DescriptorsJSON.SubFormFieldVisitor() {
          @Override
          protected void visitSubForm(JSONObject behaviorMeta, SuFormProperties props) {
            JSONObject fieldDataGetterMeta = null;
            JSONArray params = null;
            JSONObject onClickFillData = behaviorMeta.getJSONObject("onClickFillData");
            Objects.requireNonNull(onClickFillData, "onClickFillData can not be null");
            Map<String, Object> fillFieldsData = Maps.newHashMap();
            for (String fillField : onClickFillData.keySet()) {
              fieldDataGetterMeta = onClickFillData.getJSONObject(fillField);
              Objects.requireNonNull(fieldDataGetterMeta, "fillField:" + fillField + " relevant behavier meta can not be null");
              String targetMethod = fieldDataGetterMeta.getString("method");
              params = fieldDataGetterMeta.getJSONArray("params");
              Objects.requireNonNull(params, "params can not be null");
              Class<?>[] paramClass = new Class<?>[params.size()];
              String[] paramsVals = new String[params.size()];
              for (int index = 0; index < params.size(); index++) {
                paramClass[index] = String.class;
                paramsVals[index] = Objects.requireNonNull(execContext.get(params.getString(index))
                  , "param:" + params.getString(index) + " can not be null in context");
              }
              Method method = ReflectionUtils.findMethod(p.getClass(), targetMethod, paramClass);
              Objects.requireNonNull(method, "target method '" + targetMethod + "' of " + p.getClass() + " can not be null");
              fillFieldsData.put(fillField, ReflectionUtils.invokeMethod(method, p, paramsVals));
            }
            // params 必须全为spring类型的
            setBizResult(context, fillFieldsData);
          }
        });


        return;
      }
    }
    throw new IllegalStateException("have not set plugin meta");
  }

  public void doGetPluginConfigInfo(Context context) throws Exception {

    HeteroList<?> hList = null;
    List<UploadPluginMeta> plugins = getPluginMeta();

    if (plugins == null || plugins.size() < 1) {
      throw new IllegalArgumentException("param plugin is not illegal");
    }
    com.alibaba.fastjson.JSONObject pluginDetail = new com.alibaba.fastjson.JSONObject();
    com.alibaba.fastjson.JSONArray hlist = new com.alibaba.fastjson.JSONArray();
    pluginDetail.put("showExtensionPoint", TIS.get().loadGlobalComponent().isShowExtensionDetail());
    for (UploadPluginMeta pmeta : plugins) {
      hList = pmeta.getHeteroList(this);
      hlist.add(hList.toJSON());
    }
    pluginDetail.put("plugins", hlist);
    this.setBizResult(context, pluginDetail);
  }


  /**
   * 保存blugin配置
   *
   * @param context
   */
  public void doSavePluginConfig(Context context) throws Exception {
    if (this.getBoolean("errors_page_show")) {
      this.errorsPageShow(context);
    }
    List<UploadPluginMeta> plugins = getPluginMeta();
    JSONArray pluginArray = parseJsonArrayPost();

    UploadPluginMeta pluginMeta = null;
    // JSONObject itemObj = null;
    boolean faild = false;
    List<PluginItems> categoryPlugins = Lists.newArrayList();
    // 是否进行业务逻辑校验？当正式提交表单时候不进行业务逻辑校验，用户可能先添加一个不存在的数据库配置
    final boolean verify = this.getBoolean("verify");
    PluginItemsParser pluginItemsParser = null;
    for (int pluginIndex = 0; pluginIndex < plugins.size(); pluginIndex++) {
      // items = Lists.newArrayList();
      pluginMeta = plugins.get(pluginIndex);
      // subFormFilter = pluginMeta.getSubFormFilter();
      JSONArray itemsArray = pluginArray.getJSONArray(pluginIndex);
      // hEnum = pluginMeta.getHeteroEnum();
      pluginItemsParser = parsePluginItems(this, pluginMeta, context, pluginIndex, itemsArray, verify);
      if (pluginItemsParser.faild) {
        faild = true;
      }
      categoryPlugins.add(pluginItemsParser.pluginItems);
    }
    if (this.hasErrors(context) || verify) {
      return;
    }
    if (faild) {
      // 判断提交的plugin表单是否有错误？错误则退出
      this.addErrorMessage(context, "提交表单内容有错误");
      return;
    }

    List<Describable> describables = Lists.newArrayList();

    for (PluginItems pi : categoryPlugins) {
      describables.addAll(pi.save(context));
    }
    addActionMessage(context, "配置保存成功");
    // 成功保存的主键信息返回给客户端
    if (context.get(IMessageHandler.ACTION_BIZ_RESULT) == null) {
      this.setBizResult(context, describables.stream()
        .filter((d) -> d instanceof IdentityName)
        .map((d) -> ((IdentityName) d).identityValue()).collect(Collectors.toList()));
    }
  }


  public static PluginItemsParser parsePluginItems(BasicModule module, UploadPluginMeta pluginMeta
    , Context context, int pluginIndex, JSONArray itemsArray, boolean verify) {
    context.put(UploadPluginMeta.KEY_PLUGIN_META, pluginMeta);
    PluginItemsParser parseResult = new PluginItemsParser();
    List<Descriptor.PluginValidateResult> items = Lists.newArrayList();
    Optional<IPropertyType.SubFormFilter> subFormFilter = pluginMeta.getSubFormFilter();
    Descriptor.PluginValidateResult validateResult = null;
    IPluginEnum hEnum = pluginMeta.getHeteroEnum();
    //context.put(KEY_VALIDATE_PLUGIN_INDEX, new Integer(pluginIndex));
    PluginItems pluginItems = new PluginItems(module, pluginMeta);
    List<AttrValMap> describableAttrValMapList = AttrValMap.describableAttrValMapList(module, itemsArray, subFormFilter);
    if (pluginMeta.isRequired() && describableAttrValMapList.size() < 1) {
      module.addErrorMessage(context, "请设置'" + hEnum.getCaption() + "'表单内容");
    }


    pluginItems.items = describableAttrValMapList;
    parseResult.pluginItems = pluginItems;
    //categoryPlugins.add(pluginItems);
    AttrValMap attrValMap = null;


    for (int itemIndex = 0; itemIndex < describableAttrValMapList.size(); itemIndex++) {
      attrValMap = describableAttrValMapList.get(itemIndex);
      Descriptor.PluginValidateResult.setValidateItemPos(context, pluginIndex, itemIndex);
      if (!(validateResult = attrValMap.validate(context, verify)).isValid()) {
        parseResult.faild = true;
      } else {
        validateResult.setDescriptor(attrValMap.descriptor);
        items.add(validateResult);
      }
    }


    /**===============================================
     * 校验Item字段的identity字段不能重复，不然就报错
     ===============================================*/
    Map<String, Descriptor.PluginValidateResult> identityUniqueMap = Maps.newHashMap();

    Descriptor.PluginValidateResult previous = null;
    if (!parseResult.faild && hEnum.isIdentityUnique()
      && hEnum.getSelectable() == Selectable.Multi
      && (items.size() > 1 || pluginMeta.isAppend())) {

      if (pluginMeta.isAppend()) {
        List<IdentityName> plugins = hEnum.getPlugins(module, pluginMeta);
        for (IdentityName p : plugins) {
          Descriptor.PluginValidateResult r = new Descriptor.PluginValidateResult(new Descriptor.PostFormVals(Collections.emptyMap()), 0, 0);
          r.setDescriptor(((Describable) p).getDescriptor());
          identityUniqueMap.put(p.identityValue(), r);
        }
      }

      for (Descriptor.PluginValidateResult i : items) {
        if ((previous = identityUniqueMap.put(i.getIdentityFieldValue(), i)) != null) {
          previous.addIdentityFieldValueDuplicateError(module, context);
          i.addIdentityFieldValueDuplicateError(module, context);
          return parseResult;
        }
      }
    }
    return parseResult;
  }

  public static class PluginItemsParser {
    public boolean faild = false;
    public PluginItems pluginItems;
  }

  private List<UploadPluginMeta> getPluginMeta() {
    return UploadPluginMeta.parse(this.getStringArray("plugin"));
  }

  /**
   * 是否是和数据源相关的流程处理
   *
   * @return
   */
  @Override
  public boolean isDataSourceAware() {
    //return super.isDataSourceAware();
    List<UploadPluginMeta> pluginMeta = getPluginMeta();
    return pluginMeta.size() == 1 && pluginMeta.stream().findFirst().get().getHeteroEnum() == HeteroEnum.DATASOURCE;
  }

  /**
   * description: 添加一个 数据源库 date: 2:30 PM 4/28/2017
   */
  @Override
  public final void addDb(Descriptor.ParseDescribable<DataSourceFactory> dbDesc, String dbName, Context context, boolean shallUpdateDB) {
    createDatabase(this, dbDesc, dbName, context, shallUpdateDB, this.offlineManager);
  }

  public static DatasourceDb createDatabase(BasicModule module, Descriptor.ParseDescribable<DataSourceFactory> dbDesc, String dbName, Context context
    , boolean shallUpdateDB, OfflineManager offlineManager) {
    DatasourceDb datasourceDb = null;
    if (shallUpdateDB) {
      datasourceDb = new DatasourceDb();
      datasourceDb.setName(dbName);
      datasourceDb.setSyncOnline(new Byte("0"));
      datasourceDb.setCreateTime(new Date());
      datasourceDb.setOpTime(new Date());
      datasourceDb.setExtendClass(StringUtils.lowerCase(dbDesc.instance.getDescriptor().getDisplayName()));

      DatasourceDbCriteria criteria = new DatasourceDbCriteria();
      criteria.createCriteria().andNameEqualTo(dbName);
      int exist = module.getWorkflowDAOFacade().getDatasourceDbDAO().countByExample(criteria);
      if (exist > 0) {
        module.addErrorMessage(context, "已经有了同名(" + dbName + ")的数据库");
        return null;
      }
      /**
       * 校验数据库连接是否正常
       */
      int dbId = module.getWorkflowDAOFacade().getDatasourceDbDAO().insertSelective(datasourceDb);
      datasourceDb.setId(dbId);
      //module.setBizResult(context, datasourceDb);
    } else {
      // 更新状态
      DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
      dbCriteria.createCriteria().andNameEqualTo(dbName);
      for (DatasourceDb db : module.getWorkflowDAOFacade().getDatasourceDbDAO().selectByExample(dbCriteria)) {
        datasourceDb = db;
        break;
      }
      Objects.requireNonNull(datasourceDb, "dbName:" + dbName + " relevant datasourceDb can not be null");
    }

    module.setBizResult(context, offlineManager.getDbConfig(module, datasourceDb));
    return datasourceDb;
  }


  @Autowired
  public void setOfflineManager(OfflineManager offlineManager) {
    this.offlineManager = offlineManager;
  }

}
