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
package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.koubei.web.tag.pager.Pager;
import com.opensymphony.xwork2.ActionContext;
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Describable.IRefreshable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.Descriptor.ParseDescribable;
import com.qlangtech.tis.extension.Descriptor.SelectOption;
import com.qlangtech.tis.extension.IDescribableManipulate;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.PluginFormProperties.IVisitor;
import com.qlangtech.tis.extension.PluginManager;
import com.qlangtech.tis.extension.PluginWrapper;
import com.qlangtech.tis.extension.PluginWrapper.Dependency;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.extension.impl.SuFormProperties.SuFormGetterContext;
import com.qlangtech.tis.extension.model.UpdateCenter;
import com.qlangtech.tis.extension.model.UpdateCenter.DownloadJob;
import com.qlangtech.tis.extension.model.UpdateCenter.DownloadJob.Failure;
import com.qlangtech.tis.extension.model.UpdateCenter.InstallationJob;
import com.qlangtech.tis.extension.model.UpdateCenter.UpdateCenterJob;
import com.qlangtech.tis.extension.model.UpdateSite;
import com.qlangtech.tis.extension.model.UpdateSite.Plugin;
import com.qlangtech.tis.extension.util.PluginExtraProps.Props;
import com.qlangtech.tis.extension.util.TextFile;
import com.qlangtech.tis.install.InstallState;
import com.qlangtech.tis.install.InstallUtil;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.maven.plugins.tpi.ICoord;
import com.qlangtech.tis.maven.plugins.tpi.PluginClassifier;
import com.qlangtech.tis.offline.module.manager.impl.OfflineManager;
import com.qlangtech.tis.plugin.IEndTypeGetter.EndType;
import com.qlangtech.tis.plugin.IEndTypeGetter.Icon;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityDesc;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.misc.BasicRundata;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DescribableJSON;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.HeteroList;
import com.qlangtech.tis.util.IItemsSaveResult;
import com.qlangtech.tis.util.IPluginItemsProcessor;
import com.qlangtech.tis.util.IPluginWithStore;
import com.qlangtech.tis.util.IUploadPluginMeta;
import com.qlangtech.tis.util.ItemsSaveResult;
import com.qlangtech.tis.util.PluginItems;
import com.qlangtech.tis.util.PluginItems.PluginItemsSaveEvent;
import com.qlangtech.tis.util.PluginItems.PluginItemsSaveObserver;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.dispatcher.HttpParameters;
import org.apache.struts2.dispatcher.Parameter.File;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.qlangtech.tis.util.UploadPluginMeta.KEY_REQUIRE;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
@InterceptorRefs({@InterceptorRef("tisStack")})
public class PluginAction extends BasicModule {
  private static final Logger logger = LoggerFactory.getLogger(PluginAction.class);
 // private OfflineManager offlineManager;

  static {

    PluginItems.addPluginItemsSaveObserver((new PluginItemsSaveObserver() {
      // 通知Assemble节点更新pluginStore的缓存
      @Override
      public void afterSaved(PluginItemsSaveEvent event) {
        final String extendPoint = event.heteroEnum.getExtensionPoint().getName();
        // @see "com.qlangtech.tis.fullbuild.servlet.TaskStatusServlet"
        notifyPluginUpdate2AssembleNode(DescriptorsJSON.KEY_EXTEND_POINT + "=" + extendPoint, "pluginStore");
      }
    }));
  }

  private static void notifyPluginUpdate2AssembleNode(String applyParams, String targetResource) {
//    if (TisAppLaunch.isTestMock()) {
//      logger.info("skip apply clean " + targetResource + " cache by " + applyParams);
//      return;
//    }
//    long start = System.currentTimeMillis();
//    try {
//
//      URL url = new URL(Config.getAssembleHttpHost() + "/task_status?" + applyParams);
//      HttpUtils.get(url, new ConfigFileContext.StreamProcess<Void>() {
//        @Override
//        public Void p(int status, InputStream stream, Map<String, List<String>> headerFields) {
//          logger.info("has apply clean " + targetResource + " cache by " + applyParams);
//
//          return null;
//        }
//      });
//    } catch (Exception e) {
//      logger.warn("apply clean " + targetResource + ",consume:" + (System.currentTimeMillis() - start) + "ms, cache " + "faild " + e.getMessage());
//    }
  }

  private static class IconsDefs {
    final JSONArray iconsDefs;
    final String checkToken;

    public IconsDefs(JSONArray iconsDefs) {
      this.iconsDefs = iconsDefs;
      this.checkToken = DigestUtils.md5Hex(JsonUtil.toString(iconsDefs));
    }

    public JSONObject getIcons(String verToken) {
      JSONObject result = new JSONObject();
      String oldCheckToken = this.checkToken;
      result.put("verToken", oldCheckToken);
      result.put("iconsDefs", oldCheckToken.equals(verToken) ? new JSONArray() : iconsDefs);
      return result;
    }

//    public void add(JSONObject icon) {
//      iconsDefs.add(icon);
//    }
  }

  private static IconsDefs iconsDefsWithCheckSum;

  /**
   * 取得端类型相对应的插件列表
   *
   * @param context
   * @throws Exception
   */
  public void doGetEndtypeDescs(Context context) throws Exception {

  }

  public void doGetEndtypeIcons(Context context) throws Exception {

    if (iconsDefsWithCheckSum == null) {
      JSONArray iconsDefs = new JSONArray();
      JSONObject icon = null;
      Icon i = null;

      for (EndType type : EndType.values()) {

        i = type.getIcon();
        if (i == null) {
          continue;
        }
        // boolean isRef = (i instanceof IconReference);

        icon = new JSONObject();
        icon.put("name", type.getVal());
        icon.put("theme", "fill");
        i.setRes(icon, true);
        iconsDefs.add(icon);


        icon = new JSONObject();
        icon.put("name", type.getVal());
        icon.put("theme", "outline");
        // icon.put("icon", i.outlineType());
        if (i.setRes(icon, false)) {
        }
        iconsDefs.add(icon);
      }

      iconsDefsWithCheckSum = new IconsDefs(iconsDefs);
    }

    String verToken = this.getString("vertoken");

    this.setBizResult(context, iconsDefsWithCheckSum.getIcons(verToken));
  }

  /**
   * 取得 plugin的manipuldate plugin item
   *
   * @param context
   * @throws Exception
   */
  public void doGetManipuldatePlugin(Context context) throws Exception {

    String id = this.getString(IdentityName.PLUGIN_IDENTITY_NAME);
    String pluginImpl = this.getString(DescriptorsJSON.KEY_IMPL);
    Descriptor targetPlugin = Objects.requireNonNull(
      TIS.get().getDescriptor(pluginImpl), "pluginImpl:" + pluginImpl + " relevant descriptor can not be null");
    if (!(targetPlugin instanceof IDescribableManipulate)) {
      throw new IllegalStateException("targetPlugin:" + targetPlugin.getClass().getName() + " is not type of " + IDescribableManipulate.class.getSimpleName());
    }
    IDescribableManipulate describableManipulate = ((IDescribableManipulate) targetPlugin);
    Optional<IPluginStore> manipulateStore = describableManipulate.getManipulateStore();
    if (!manipulateStore.isPresent()) {
      throw new IllegalStateException("manipulateStore must be present");
    }
    IPluginStore pluginStore = manipulateStore.get();
    List<Describable> plugins = pluginStore.getPlugins();
    for (Describable man : plugins) {
      if (!(man instanceof IdentityName)) {
        continue;
      }
      DescribableJSON pluginJSON = null;
      if (StringUtils.equals(id, ((IdentityName) man).identityValue())) {
        pluginJSON = new DescribableJSON(man);
        Map<String, Object> manipuldateItem = Maps.newHashMap();
        manipuldateItem.put("item", pluginJSON.getItemJson());
        manipuldateItem.put("desc", (new DescriptorsJSON<>(pluginJSON.descriptor)).getDescriptorsJSON());
        this.setBizResult(context, manipuldateItem);
        return;
      }
    }
    throw new IllegalStateException("target " + IdentityName.PLUGIN_IDENTITY_NAME + ":" + id + " relevant item can not be found");

  }

  /**
   * 通过之前缓存在服务端的NotebookEntry实例对象打开notebook
   *
   * @param context
   * @throws Exception
   */
//  public void doGetOrCreateNotebook(Context context) throws Exception {
//    DataxProcessor dataxProcessor = IAppSource.load(this, this.getAppDomain().getAppName());
//    String pluginIdVal = this.getString("pluginIdVal");
//    if (StringUtils.isEmpty(pluginIdVal)) {
//      throw new IllegalArgumentException("param pluginIdVal can not be null");
//    }
//    Map<String, INotebookable.NotebookEntry> notebooks = dataxProcessor.scanNotebook();
//    INotebookable.NotebookEntry notebookEntry = notebooks.get(pluginIdVal);
//    Objects.requireNonNull(notebookEntry, "pluginId:" + pluginIdVal + " relevant notebookEntry can not be null");
//    this.setBizResult(context, notebookEntry.createOrGetNotebook());
//  }

  /**
   * @param context
   */
//  public void doScanNotebooks(Context context) throws Exception {
//    String dataxName = this.getAppDomain().getAppName();
//
//    DataxProcessor dataxProcessor = IAppSource.load(this, dataxName);
//    Map<String, INotebookable.NotebookEntry> notebooks = dataxProcessor.scanNotebook();
//    String pluginIdVal = null;
//    DescriptorsJSON descJson = null;
//    List<Descriptor> descs = Lists.newArrayList();
//    INotebookable.NotebookEntry note = null;
//    List<Map<String, Object>> notebookProps = Lists.newArrayList();
//    Map<String, Object> props = null;
//    for (Map.Entry<String, INotebookable.NotebookEntry> entry : notebooks.entrySet()) {
//      pluginIdVal = entry.getKey();
//      note = entry.getValue();
//
//      props = new HashMap<>(note.getDescriptor().getExtractProps());
//      props.put("pluginId", pluginIdVal);
//      props.put("displayName", note.getDescriptor().getDisplayName());
//      notebookProps.add(props);
//    }
//    this.setBizResult(context, notebookProps);
//  }


  /**
   * 为表单中提交临时文件
   *
   * @param context
   */
  @Func(value = PermissionConstant.CONFIG_UPLOAD, sideEffect = false)
  public void doUploadFile(Context context) {
    final String inputName = "file";
    final String fileNameName = inputName + "FileName";
    ActionContext ac = ActionContext.getContext();
    HttpParameters parameters = ac.getParameters();

    File file = (File) parameters.get(inputName);
    UploadedFile[] uploades = (UploadedFile[]) file.getObject();
    for (UploadedFile f : uploades) {
      java.io.File tmpFile = new java.io.File(f.getAbsolutePath());
      java.io.File renameTo = new java.io.File(tmpFile.getParentFile(), f.getName() + "_tmp");
      tmpFile.renameTo(renameTo);
      this.setBizResult(context, Collections.singletonMap(inputName, renameTo.getAbsolutePath()));
      return;
    }

    throw new IllegalStateException(" have not receive any upload file,inputName:" + inputName);
  }

  /**
   * @param context
   */
  public void doCreateOrGetNotebook(Context context) {

  }

  /**
   * 刷新多选字段内容
   *
   * @param context
   */
  public void doGetFreshEnumField(Context context) {
    DescriptorField descField = parseDescField();
    List<SelectOption> options = null;
    if (descField.getFieldPropType().typeIdentity() == FormFieldType.SELECTABLE.getIdentity()) {
      options = DescriptorsJSON.getSelectOptions(descField.getTargetDesc(), descField.getFieldPropType(),
        descField.field);
      this.setBizResult(context, options);
    } else if (descField.getFieldPropType().typeIdentity() == FormFieldType.ENUM.getIdentity()) {
      this.setBizResult(context, descField.getFieldPropType().getExtraProps().getJSONArray(Descriptor.KEY_ENUM_PROP));
    }
  }

  private static class DescriptorField {
    final String pluginImpl;
    final String field;

    public DescriptorField(String pluginImpl, String field) {
      this.pluginImpl = pluginImpl;
      this.field = field;
    }

    Descriptor getTargetDesc() {
      return TIS.get().getDescriptor(this.pluginImpl);
    }

    PropertyType getFieldPropType() {
      return (PropertyType) Objects.requireNonNull(getTargetDesc()
        , "impl:" + this.pluginImpl + " relevant desc can not be null").getPropertyType(this.field);
    }
  }

  private DescriptorField parseDescField() {
    String pluginImpl = this.getString("impl");
    String fieldName = this.getString("field");
    if (StringUtils.isEmpty(pluginImpl)) {
      throw new IllegalArgumentException("param 'impl' can not be null");
    }
    if (StringUtils.isEmpty(fieldName)) {
      throw new IllegalArgumentException("param 'field' can not be null");
    }
    return new DescriptorField(pluginImpl, fieldName);
  }

  /**
   * 取得字段的帮助信息
   *
   * @param context
   */
  public void doGetPluginFieldHelp(Context context) {
    DescriptorField descField = parseDescField();
    Props props = descField.getFieldPropType().extraProp;
    if (!props.isAsynHelp()) {
      throw new IllegalStateException("plugin:" + descField.pluginImpl + ",field:" + descField.field + " is not " +
        "support async help content fecthing");
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
    List<UpdateCenterJob> jobs = updateCenter.getJobs();
    Collections.sort(jobs, (a, b) -> {
      // 保证最新的安装job排列在最上面
      return b.id - a.id;
    });
    jobs.forEach((job) -> {
      if (job instanceof DownloadJob) {
        ((DownloadJob) job).status.setUsed();
      }
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
    Plugin info = null;
    PluginFilter pluginFilter = PluginFilter.create(this);//  new PluginFilter(this.getExtendpointParam());
    for (PluginWrapper plugin : pluginManager.getPlugins()) {


      pluginInfo = new JSONObject();
      pluginInfo.put("installed", true);
      info = plugin.getInfo();
      if (info != null) {
        // pluginInfo.put("meta", info);
        pluginInfo.put("releaseTimestamp", info.releaseTimestamp);
        pluginInfo.put("excerpt", info.excerpt);
        pluginInfo.put("endTypeIcons", info.getEndTypeIcons());
      }

      if (CollectionUtils.isNotEmpty(extendpoint)) {
        if (info == null) {
          continue;
        }
        //        if (!CollectionUtils.containsAny(info.extendPoints.keySet(), extendpoint)) {
        //          continue;
        //        }
        pluginInfo.put("extendPoints", info.extendPoints);
      }

      if (pluginFilter.filter(Optional.of(plugin), info)) {
        continue;
      }

      Optional<PluginClassifier> classifier = plugin.getClassifier();
      if (classifier.isPresent()) {
        pluginInfo.put(PluginManager.PACAKGE_CLASSIFIER, classifier.get().getClassifier());
      }

      pluginInfo.put("name", plugin.getShortName());
      pluginInfo.put("version", plugin.getVersion());
      pluginInfo.put("title", plugin.getDisplayName());
      pluginInfo.put("active", plugin.isActive());
      pluginInfo.put("enabled", plugin.isEnabled());
      // pluginInfo.put("bundled", plugin.isBundled);
      pluginInfo.put("deleted", plugin.isDeleted());
      pluginInfo.put("downgradable", plugin.isDowngradable());
      pluginInfo.put(ICoord.KEY_PLUGIN_VIP, plugin.manifest.isCommunityVIP());

      pluginInfo.put("website", plugin.getUrl());
      List<Dependency> dependencies = plugin.getDependencies();
      if (dependencies != null && !dependencies.isEmpty()) {
        Option o = null;
        List<Option> dependencyMap = Lists.newArrayList();
        for (Dependency dependency : dependencies) {
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
    JSONArray willBeInstall = this.parseJsonArrayPost();
    if (willBeInstall.size() < 1) {
      this.addErrorMessage(context, "请选择需要安装的插件");
      return;
    }

    List<PluginWillInstall> pluginsInstall = PluginWillInstall.parse(willBeInstall);
    for (PluginWillInstall willInstall : pluginsInstall) {
      if (willInstall.isMultiClassifier() && !willInstall.isPresentPluginClassifier()) {
        this.addFieldError(context, willInstall.getName(), "请选择安装的版本");
        //  continue;
      }
    }

    if (this.hasErrors(context)) {
      return;
    }


    PluginWillInstall.installPlugins(pluginsInstall);
  }

  /**
   * 重新加载updateSite元数据信息
   *
   * @throws Exception
   */
  public void doReloadUpdateSiteMeta(Context context) throws Exception {
    UpdateCenter center = TIS.get().getUpdateCenter();
    for (UpdateSite usite : center.getSiteList()) {
      TextFile textFile = usite.getDataLoadFaildFile();
      if (textFile.exists()) {
        usite.updateDirectly().get();
      }
    }
    this.doGetAvailablePlugins(context);
  }

  /**
   * 取得当前可以被安装的插件
   *
   * @param context
   */
  public void doGetAvailablePlugins(Context context) throws Exception {


    //List<String> extendpoint = getExtendpointParam();
    Pager pager = this.createPager();
    pager.setTotalCount(Integer.MAX_VALUE);
    UpdateCenter center = TIS.get().getUpdateCenter();
    List<Plugin> availables = center.getAvailables();
    if (CollectionUtils.isEmpty(availables)) {
      for (UpdateSite usite : center.getSiteList()) {
        TextFile textFile = usite.getDataLoadFaildFile();
        if (textFile.exists()) {

          Map<String, Object> err = Maps.newHashMap();
          err.put("updateSiteLoadErr", true);
          err.put(IMessageHandler.ACTION_ERROR_MSG, textFile.read());
          this.setBizResult(context, new PaginationResult(pager, availables, err));
          return;
        }
      }
    }

    PluginFilter filter = PluginFilter.create(this);// new PluginFilter(this);
    availables = availables.stream().filter((plugin) -> {
      return !(filter.filter(Optional.empty(), plugin));
    }).collect(Collectors.toList());


    //    if (CollectionUtils.isNotEmpty(extendpoint)) {
    //
    //      Predicate<UpdateSite.Plugin> endTypeMatcher = getEndTypeMatcher();
    //
    //      availables = availables.stream().filter((plugin) -> {
    //        return CollectionUtils.containsAny(plugin.extendPoints.keySet(), extendpoint);
    //      }).filter(endTypeMatcher).collect(Collectors.toList());
    //    }
    //
    //    if (CollectionUtils.isNotEmpty(this.getQueryPluginParam())) {
    //      availables = availables.stream().filter((plugin) -> {
    //        return !filterPlugin(plugin.title, plugin.excerpt);
    //      }).collect(Collectors.toList());
    //    }

    this.setBizResult(context, new PaginationResult(pager, availables));
  }


  public List<String> getExtendpointParam() {
    return Arrays.asList(this.getStringArray("extendpoint"));
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
    this.errorsPageShow(context);
    DescriptorsGetter result = getDescriptorsGetter();

    for (Descriptor desc : result.descriptors) {
      if (StringUtils.equals(desc.getDisplayName(), result.displayName)) {
        this.setBizResult(context, new DescriptorsJSON(desc).getDescriptorsJSON());
        return;
      }
    }
    this.setBizResult(context, Collections.singletonMap("notFoundExtension", result.hetero.getExtensionPoint().getName()));
    this.addErrorMessage(context, "displayName:" + result.displayName + " relevant Descriptor can not be null");

  }

  private DescriptorsGetter getDescriptorsGetter() {
    final String displayName = this.getString("name");
    if (StringUtils.isEmpty(displayName)) {
      throw new IllegalArgumentException("request param 'impl' can not be null");
    }
    IPluginEnum hetero = HeteroEnum.of(this.getString("hetero"));
    List<Descriptor<Describable>> descriptors = null;
    DescriptorsGetter result = new DescriptorsGetter(displayName, hetero);
    String[] plugins = this.getStringArray(KEY_PLUGIN);
    if (plugins != null && plugins.length > 0) {
      List<UploadPluginMeta> pluginMetas = this.getPluginMeta();
      for (UploadPluginMeta meta : pluginMetas) {
        IPluginStore pluginStore = hetero.getPluginStore(this, meta);
        descriptors = pluginStore.allDescriptor();
        result.setItems(pluginStore.getPlugins());
        result.setPluginMeta(meta);
        break;
      }
    } else {
      descriptors = hetero.descriptors();
    }
    result.descriptors = descriptors;
    return result;
  }

  private static class DescriptorsGetter {
    public final String displayName;
    public final IPluginEnum hetero;
    public List<Descriptor<Describable>> descriptors;

    private List<Describable> items = Collections.emptyList();

    private UploadPluginMeta pluginMeta;


    public DescriptorsGetter(String displayName, IPluginEnum hetero) {
      this.displayName = displayName;
      this.hetero = hetero;
    }

    public UploadPluginMeta getPluginMeta() {
      return pluginMeta;
    }

    public void setPluginMeta(UploadPluginMeta pluginMeta) {
      this.pluginMeta = pluginMeta;
    }

    public List<Describable> getItems() {
      return this.items;
    }

    public void setItems(List<Describable> items) {
      this.items = items;
    }
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
      this.setBizResult(context,
        new DescriptorsJSON(TIS.get().getDescriptorList((Class<Describable>) Class.forName(extend))).getDescriptorsJSON());
      return;
    }

    throw new IllegalArgumentException("extendpoints can not be null");
  }

  /**
   * 取Descriptor 某 field（必须为Describle类型）的 descriptpr列表
   *
   * @param context
   * @throws Exception
   */
  public void doGetDescsByFieldOfDesc(Context context) throws Exception {
    String extImpl = this.getString("extImpl");
    String field = this.getString("field");
    Descriptor desc = TIS.get().getDescriptor(extImpl);

    PluginFormProperties props = desc.getPluginFormPropertyTypes();

    List<? extends Descriptor> descs = props.accept(new IVisitor() {
      @Override
      public List<? extends Descriptor> visit(RootFormProperties props) {

        PropertyType descProp = Objects.requireNonNull(props.propertiesType.get(field), "field:" + field + " relevant"
          + " propDesc can not be null");
        if (descProp.isDescribable()) {
          return descProp.getApplicableDescriptors();
        }
        throw new IllegalStateException("can not find any desc impls for field:" + field + " in extImpl:" + extImpl);
      }
    });

    this.setBizResult(context, new DescriptorsJSON(descs).getDescriptorsJSON());
  }

  /**
   * 取得一个插件的一条记录
   *
   * @param context
   * @throws Exception
   */
  public void doGetDescrible(Context context) throws Exception {

    DescriptorsGetter descriptorsGetter = getDescriptorsGetter();

    HeteroList hlist = new HeteroList(descriptorsGetter.getPluginMeta());
    if (CollectionUtils.isEmpty(descriptorsGetter.descriptors)) {
      throw new IllegalStateException("descriptors can not be empty");
    }
    hlist.setDescriptors(descriptorsGetter.descriptors);
    hlist.setExtensionPoint(descriptorsGetter.hetero.getExtensionPoint());
    hlist.setSelectable(Selectable.Single);
    hlist.setCaption(org.apache.commons.lang.StringUtils.EMPTY);

    hlist.setItems(descriptorsGetter.getItems());

    this.setBizResult(context, hlist.toJSON());
  }

  /**
   * 取得Descs列表
   *
   * @param context
   * @throws Exception
   */
  public void doGetDescs(Context context) throws Exception {
    String[] descsImpl = this.getStringArray("desc");
    if (descsImpl == null || descsImpl.length < 1) {
      throw new IllegalStateException("argument desc can not be empty");
    }
    JSONObject pluginDetail = new JSONObject();
    JSONArray hlistArray = new JSONArray();
    HeteroList hList = null;
    // List<Descriptor> descs = new ArrayList<>();
    Descriptor desc = null;
    for (String extImpl : descsImpl) {
      desc = TIS.get().getDescriptor(extImpl);
      if (desc == null) {
        throw new IllegalStateException("extImpl:" + extImpl + " relevant desc can not be null");
      }
      hList = new HeteroList(UploadPluginMeta.parse("test_plugin:" + KEY_REQUIRE));
      hList.setDescriptors(Collections.singletonList(desc));
      hList.setSelectable(Selectable.Single);
      hList.setCaption(desc.clazz.getSimpleName());
      hList.setExtensionPoint(desc.getT());

      hlistArray.add(hList.toJSON());
    }
    pluginDetail.put("plugins", hlistArray);
    this.setBizResult(context, pluginDetail);
  }

  /**
   * plugin form 的子表单的某条详细记录被点击
   *
   * @param context
   * @throws Exception
   */
  public void doSubformDetailedClick(Context context) throws Exception {

    List<UploadPluginMeta> pluginsMeta = getPluginMeta();
    Pair<List<DataxReader>, IPluginStore<DataxReader>> plugins = null;

    // IPluginEnum heteroEnum = null;
    HeteroList<?> hList = null;

    for (UploadPluginMeta meta : pluginsMeta) {

      // heteroEnum = meta.getHeteroEnum();
      plugins = meta.getDataxReaders(this);// heteroEnum.getPlugins(this, meta);
      for (DataxReader plugin : plugins.getKey()) {

        SuFormProperties.setSuFormGetterContext(plugin, plugins.getRight(), meta,
          this.getString(SuFormGetterContext.FIELD_SUBFORM_ID));

        hList = meta.getHeteroList(this);

        this.setBizResult(context, hList.toJSON());
        return;
      }
      throw new IllegalStateException("have not set plugin,meta:" + String.valueOf(meta));
    }
    throw new IllegalStateException("have not set plugin meta");
  }

  /**
   * @param context
   * @throws Exception
   */
  public void doGetPluginConfigInfo(Context context) throws Exception {

    HeteroList<?> hetero = null;
    //  List<HeteroList<?>> heteros = Lists.newArrayList();
    List<UploadPluginMeta> plugins = getPluginMeta();

    if (plugins == null || plugins.size() < 1) {
      throw new IllegalArgumentException("param plugin is not illegal");
    }
    JSONObject pluginDetail = new JSONObject();
    JSONArray hlist = new JSONArray();
    pluginDetail.put("showExtensionPoint", TIS.get().loadGlobalComponent().isShowExtensionDetail());
    for (UploadPluginMeta pmeta : plugins) {

      hetero = this.createHeteroList(pmeta);
      if (!pmeta.isUseCache()) {
        hetero.getItems().forEach((p) -> {
          if (p instanceof Describable.IRefreshable) {
            ((IRefreshable) p).refresh();
          }
        });
      }
      //heteros.add(hetero);
      hlist.add(hetero.toJSON());
    }

    pluginDetail.put("plugins", hlist);
    this.setBizResult(context, pluginDetail);
  }

  private HeteroList<?> createHeteroList(UploadPluginMeta pmeta) {
    return pmeta.getHeteroList(this);
  }

  /**
   * 保存plugin配置
   *
   * @param context
   */
  public void doSavePluginConfig(Context context) throws Exception {
    if (this.getBoolean("errors_page_show")) {
      this.errorsPageShow(context);
    }
    List<UploadPluginMeta> plugins = getPluginMeta();
    JSONObject postData = this.parseJsonPost();
    String[] forwardParams = getActionForwardParam(postData);

    JSONArray pluginArray = Objects.requireNonNull(
      postData.getJSONArray("items"), "json prop items can not be null");
    UploadPluginMeta pluginMeta = null;

    boolean faild = false;
    List<IPluginItemsProcessor> categoryPlugins = Lists.newArrayList();
    // final boolean processNotebook = this.getBoolean("getNotebook");
    // 是否进行业务逻辑校验？当正式提交表单时候不进行业务逻辑校验，用户可能先添加一个不存在的数据库配置
    final boolean verify = this.getBoolean("verify");
    Pair<Boolean, IPluginItemsProcessor> pluginItemsParser = null;
    for (int pluginIndex = 0; pluginIndex < plugins.size(); pluginIndex++) {

      pluginMeta = plugins.get(pluginIndex);
      JSONArray itemsArray = pluginArray.getJSONArray(pluginIndex);

      pluginItemsParser = getPluginItems(pluginMeta, context, pluginIndex, itemsArray, verify, ((propType, val) -> val));
      if (pluginItemsParser.getKey()) {
        faild = true;
      }
      categoryPlugins.add(pluginItemsParser.getValue());
    }

    if (verify && !this.hasErrors(context)) {
      for (IPluginItemsProcessor pi : categoryPlugins) {
        IPluginWithStore storePlugins = pi.getStorePlugins();
        storePlugins.afterVerified();
      }
    }

    if (this.hasErrors(context) || (verify)) {
      return;
    }
    if (faild) {
      // 判断提交的plugin表单是否有错误？错误则退出
      this.addErrorMessage(context, "提交表单内容有错误");
      return;
    }


    List<IItemsSaveResult> describables = Lists.newArrayList();

    for (IPluginItemsProcessor pi : categoryPlugins) {
      describables.add(pi.save(context));
    }

    if (forwardParams != null) {
      this.getRequest().setAttribute(ItemsSaveResult.KEY_ITEMS_SAVE_RESULT, describables);
      // getRundata().forwardTo(forwardParams[0], forwardParams[1], forwardParams[2]);

      BasicRundata.forward(getRundata(), forwardParams);
      return;
    }

    addActionMessage(context, "配置保存成功");
    // 成功保存的主键信息返回给客户端
    if (context.get(IMessageHandler.ACTION_BIZ_RESULT) == null) {
      this.setBizResult(context,
        describables.stream()
          .flatMap((itemSaveResult) -> itemSaveResult.getIdentityStream()).map((d) -> {
            if (d instanceof IdentityDesc) {
              return ((IdentityDesc) d).describePlugin();
            } else {
              return (d).identityValue();
            }
          }).collect(Collectors.toList()));
    }
  }

  public static List<ItemsSaveResult> getItemsSaveResultInRequest(HttpServletRequest request) {
    return (List<ItemsSaveResult>) request.getAttribute(ItemsSaveResult.KEY_ITEMS_SAVE_RESULT);
  }

  protected String[] getActionForwardParam(JSONObject postData) {
    String serverForward = postData.getString("serverForward");
    String[] forwardParams = null;
    if (StringUtils.isNotEmpty(serverForward)) {
      forwardParams = StringUtils.split(serverForward, ":");
      if (forwardParams.length != 3) {
        throw new IllegalArgumentException("illegal forward param:" + serverForward);
      }
    }
    return forwardParams;
  }

  /**
   * @param pluginMeta
   * @param context
   * @param pluginIndex
   * @param itemsArray
   * @param verify
   * @return Boolean: is faild?, IPluginItemsProcessor
   * @see com.qlangtech.tis.runtime.module.misc.IPostContent impl of
   */
  @Override
  public final Pair<Boolean, IPluginItemsProcessor> getPluginItems(IUploadPluginMeta pluginMeta, Context context,
                                                                   int pluginIndex, JSONArray itemsArray, boolean verify, PropValRewrite propValRewrite) {
    PluginItemsParser pluginItemsParser
      = PluginItemsParser.parsePluginItems(this, (UploadPluginMeta) pluginMeta, context, pluginIndex, itemsArray, verify, propValRewrite);
    return Pair.of(pluginItemsParser.faild, pluginItemsParser.pluginItems);

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
  public final void addDb(ParseDescribable<DataSourceFactory> dbDesc, String dbName, Context context,
                          boolean shallUpdateDB) {
    createDatabase(this, dbDesc, dbName, context, shallUpdateDB, this.offlineManager);
  }

  public static DatasourceDb createDatabase(BasicModule module, ParseDescribable<DataSourceFactory> dbDesc
    , String dbName, Context context, boolean shallUpdateDB, OfflineManager offlineManager) {
    if (StringUtils.isEmpty(dbName)) {
      throw new IllegalArgumentException("param dbName can not be empty");
    }
    DatasourceDb datasourceDb = null;
    if (shallUpdateDB) {
      datasourceDb = new DatasourceDb();
      datasourceDb.setName(dbName);
      datasourceDb.setSyncOnline(new Byte("0"));
      datasourceDb.setCreateTime(new Date());
      datasourceDb.setOpTime(new Date());
      Describable plugin = dbDesc.getInstance();
      datasourceDb.setExtendClass(StringUtils.lowerCase(plugin.getDescriptor().getDisplayName()));

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


//  @Autowired
//  public void setOfflineManager(OfflineManager offlineManager) {
//    this.offlineManager = offlineManager;
//  }

}
