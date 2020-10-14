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
package com.qlangtech.tis.runtime.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Maps;
import com.opensymphony.xwork2.ModelDriven;
import com.qlangtech.tis.coredefine.biz.FCoreRequest;
import com.qlangtech.tis.coredefine.module.action.CoreAction;
import com.qlangtech.tis.coredefine.module.control.SelectableServer;
import com.qlangtech.tis.fullbuild.indexbuild.LuceneVersion;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.*;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.manage.common.ibatis.BooleanYorNConvertCallback;
import com.qlangtech.tis.manage.servlet.DownloadServlet;
import com.qlangtech.tis.manage.servlet.LoadSolrCoreConfigByAppNameServlet;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.offline.module.manager.impl.OfflineManager;
import com.qlangtech.tis.openapi.impl.AppKey;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.DelegateControl4JavaBeanMsgHandler;
import com.qlangtech.tis.runtime.pojo.ResSynManager;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.DocCollection;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 添加应用
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-4-1
 */
public class AddAppAction extends SchemaAction implements ModelDriven<Application> {

  private static final long serialVersionUID = 1L;

  // private ITerminatorTriggerBizDalDAOFacade triggerContext;
  private OfflineManager offlineManager;

  public static final int FIRST_GROUP_INDEX = 0;

  private static final String FIELD_PROJECT_NAME = "projectName";

  private static final String FIELD_WORKFLOW = "workflow";

  private static final String FIELD_Recept = "recept";

  private static final String FIELD_DptId = "dptId";

  private final Application app = new Application();

  @Override
  public Application getModel() {
    return app;
  }

  /**
   * 创建业务流程中所需要的系统表信息，在這個方法中全部取得
   *
   * @param context
   * @throws Exception
   */
  @Func(PermissionConstant.APP_ADD)
  public void doGetCreateAppMasterData(Context context) throws Exception {
    Map<String, Object> masterData = Maps.newHashMap();
    masterData.put("bizlinelist", this.getBizLineList());
    final List<Option> verList = new ArrayList<>();
    for (LuceneVersion v : LuceneVersion.values()) {
      verList.add(new Option(v.getKey(), v.getKey()));
    }
    masterData.put("tplenum", verList);
    masterData.put("usableWorkflow", this.offlineManager.getUsableWorkflow());
    this.setBizResult(context, masterData);
  }

  @Func(value = PermissionConstant.APP_ADD, sideEffect = false)
  public void doGetSelectableNodeList(Context context) throws Exception {
    SelectableServer.CoreNode[] nodes = SelectableServer.getCoreNodeInfo(this.getRequest(), this, false, false);
    this.setBizResult(context, nodes);
  }

  /**
   * 创建索引，不在数据库中添加记录<br>
   * 当一个索引创建之后又被删除了，又需要重新创建就需要需执行该流程了
   *
   * @param context
   * @throws Exception
   */
  @Func(PermissionConstant.APP_ADD)
  public void doCreateCollection(Context context) throws Exception {
    CreateIndexConfirmModel confiemModel = parseJsonPost(CreateIndexConfirmModel.class);
    confiemModel.setTplAppId(getTemplateApp().getAppId());
    SchemaResult schemaResult = this.parseSchema(context, confiemModel);
    // if (!createNewApp(context, confiemModel.getAppform(), -1, /* publishSnapshotId */
    // null, /* schemaContent */
    // true).isSuccess()) {
    // //只作表单校验 表单校验不通过
    // return;
    // }
    this.createCollection(context, confiemModel, schemaResult, (ctx, app, publishSnapshotId, schemaContent) -> {
      CreateSnapshotResult result = new CreateSnapshotResult();
      result.setSuccess(true);
      // final Integer publishSnapshotId = ;
      Application a = getApplicationDAO().selectByName(app.getProjectName());
      if (a == null) {
        throw new IllegalStateException("appname:" + app.getProjectName() + " relevant app can not be find in DB");
      }
      result.setNewSnapshotId(getPublishSnapshotId(this.getServerGroupDAO(), a));
      return result;
    });
  }

  /**
   * 高级添加,会在数据库中添加对象记录
   *
   * @param context
   */
  @Func(PermissionConstant.APP_ADD)
  public void doAdvanceAddApp(Context context) throws Exception {
    CreateIndexConfirmModel confiemModel = parseJsonPost(CreateIndexConfirmModel.class);
    confiemModel.setTplAppId(getTemplateApp().getAppId());
    SchemaResult schemaResult = this.parseSchema(context, confiemModel);
    if (!createNewApp(context, confiemModel.getAppform(), -1, /**
       * publishSnapshotId
       */
      null, /* schemaContent */
      true).isSuccess()) {
      // 只作表单校验 表单校验不通过
      return;
    }
    this.createCollection(context, confiemModel, schemaResult, (ctx, app, publishSnapshotId, schemaContent) -> {
      return this.createNewApp(ctx, app, publishSnapshotId, schemaContent);
    });
  }

  private void createCollection(Context context, CreateIndexConfirmModel confiemModel, SchemaResult schemaResult, ICreateNewApp appCreator) throws Exception {
    ExtendApp extApp = confiemModel.getAppform();
    appendPrefix(extApp);
    String workflow = confiemModel.getAppform().getWorkflow();
    if (StringUtils.isBlank(workflow)) {
      this.addErrorMessage(context, "缺少全量数据流信息");
      return;
    }
    final String[] candidateNodeIps = confiemModel.getCoreNodeCandidate();
    if (candidateNodeIps == null || candidateNodeIps.length < 1) {
      // throw new IllegalStateException();
      this.addErrorMessage(context, "请选择引擎节点");
      return;
    }
    // }
    if (!schemaResult.success) {
      return;
    }
    Application app = new Application();
    app.setAppId(confiemModel.getTplAppId());
    Integer publishSnapshotId = getPublishSnapshotId(this.getServerGroupDAO(), app);
    IUser loginUser = this.getUser();
    byte[] content = schemaResult.content;
    SelectableServer.ServerNodeTopology coreNode = confiemModel.getCoreNode();
    final int gourpCount = coreNode.getShardCount();
    int repliation = coreNode.getReplicaCount();
    // 由于是在日常环境中，默认就是设置为 1*1
    FCoreRequest request = new FCoreRequest(CoreAction.createIps(context, extApp.getProjectName(), candidateNodeIps), gourpCount);
    for (String ip : candidateNodeIps) {
      request.addNodeIps(gourpCount - 1, ip);
    }
    request.setValid(true);
    CreateSnapshotResult createResult = appCreator.createNewApp(context, extApp, publishSnapshotId, content);
    if (!createResult.isSuccess()) {
      return;
    }
    /**
     * *************************************************************************************
     * 因为这里数据库的事物还没有提交，需要先将schema配置信息保存到缓存中去以便solrcore节点能获取到
     * **************************************************************************************
     */
    final AppKey appKey = new AppKey(extApp.getProjectName(), /* appName ========== */
      (short) 0, /* groupIndex */
      RunEnvironment.getSysRuntime(), false);
    appKey.setTargetSnapshotId((long) createResult.getNewId());
    appKey.setFromCache(false);
    LoadSolrCoreConfigByAppNameServlet.getSnapshotDomain(ConfigFileReader.getConfigList(), appKey, this);
    CoreAction.createCollection(this, context, gourpCount, repliation, request, createResult.getNewId());
  }

  interface ICreateNewApp {

    public CreateSnapshotResult createNewApp(Context context, ExtendApp app, int publishSnapshotId, byte[] schemaContent) throws Exception;
  }

  /**
   * 校验添加新索引第一步提交的form表单
   *
   * @param context
   */
  @Func(value = PermissionConstant.APP_ADD, sideEffect = false)
  public void doValidateAppForm(Context context) throws Exception {
    this.errorsPageShow(context);
    ExtendApp app = this.parseJsonPost(ExtendApp.class);
    this.appendPrefix(app);
    this.createNewApp(context, app, -1, /* publishSnapshotId */
      null, /* schemaContent */
      true);
  }

  private void appendPrefix(ExtendApp app) {
    if (StringUtils.isNotBlank(app.getProjectName())) {
      app.setProjectName("search4" + app.getProjectName());
    }
  }

  private static CreateSnapshotResult createNewSnapshot(Context context, final SnapshotDomain domain, PropteryGetter fileGetter, byte[] uploadContent, BasicModule module, String memo, Long userId, String userName) {
    CreateSnapshotResult createResult = new CreateSnapshotResult();
    final String md5 = ConfigFileReader.md5file(uploadContent);
    // 创建一条资源记录
    try {
      Integer newResId = createNewResource(context, uploadContent, md5, fileGetter, module);
      final Snapshot snapshot = fileGetter.createNewSnapshot(newResId, domain.getSnapshot());
      snapshot.setMemo(memo);
      createResult.setNewSnapshotId(createNewSnapshot(snapshot, memo, module, userId, userName));
      snapshot.setSnId(createResult.getNewId());
      context.put("snapshot", snapshot);
    } catch (SchemaFileInvalidException e) {
      return createResult;
    }
    createResult.setSuccess(true);
    return createResult;
  }

  /**
   * 仅仅创建 Application相关的數據表
   *
   * @param context
   * @param app
   */
  private CreateSnapshotResult createNewApp(Context context, ExtendApp app, int publishSnapshotId, byte[] schemaContent) throws Exception {
    return this.createNewApp(context, app, publishSnapshotId, schemaContent, false);
  }

  private CreateSnapshotResult createNewApp(Context context, ExtendApp app, int publishSnapshotId, byte[] schemaContent, boolean justValidate) throws Exception {
    IControlMsgHandler handler = new DelegateControl4JavaBeanMsgHandler(this, app);
    Map<String, Validator.FieldValidators> validateRule = //
      Validator.fieldsValidator(//
        FIELD_PROJECT_NAME, new Validator.FieldValidators(Validator.require) {
        }, //
        new Validator.IFieldValidator() {

          @Override
          public boolean validate(IFieldErrorHandler msgHandler, Context context, String fieldKey, String fieldData) {
            if (!isAppNameValid(msgHandler, context, app)) {
              return false;
            }
            ApplicationCriteria criteria = new ApplicationCriteria();
            criteria.createCriteria().andProjectNameEqualTo(app.getProjectName());
            if (getApplicationDAO().countByExample(criteria) > 0) {
              msgHandler.addFieldError(context, FIELD_PROJECT_NAME, "已经有同名(‘" + app.getProjectName() + "’)索引存在");
              return false;
            }
            return true;
          }
        }, //
        FIELD_WORKFLOW, new Validator.FieldValidators(Validator.require) {
        }, //
        FIELD_Recept, new Validator.FieldValidators(Validator.require) {
        }, //
        FIELD_DptId, new Validator.FieldValidators(Validator.require) {
        });
    CreateSnapshotResult result = new CreateSnapshotResult();
    result.setSuccess(true);
    if (!Validator.validate(handler, context, validateRule)) {
      return result.setSuccess(false);
    }
    app.setDptName(getDepartment(this, app.getDptId()).getFullName());
    app.setCreateTime(new Date());
    app.setIsAutoDeploy(true);
    if (!justValidate) {
      result = createApplication(app, publishSnapshotId, /* publishSnapshotId */
        schemaContent, context, this);
      addActionMessage(context, "已经成功创建索引[" + app.getProjectName() + "]");
    }
    return result;
  }

  private static Integer createNewResource(Context context, final byte[] uploadContent, final String md5, PropteryGetter fileGetter, BasicModule module) throws SchemaFileInvalidException {
    UploadResource resource = getUploadResource(context, uploadContent, md5, fileGetter, module);
    return module.getUploadResourceDAO().insert(resource);
  }

  private static UploadResource getUploadResource(Context context, byte[] uploadContent, String md5, PropteryGetter fileGetter, BasicModule module) throws SchemaFileInvalidException {
    UploadResource resource = new UploadResource();
    resource.setContent(uploadContent);
    resource.setCreateTime(new Date());
    resource.setResourceType(fileGetter.getFileName());
    resource.setMd5Code(md5);
    ConfigFileValidateResult validateResult = fileGetter.validate(resource);
    // 校验文件格式是否正确，通用用DTD来校验
    if (!validateResult.isValid()) {
      module.addErrorMessage(context, ResSynManager.ERROR_MSG_SCHEMA_TITLE);
      module.addErrorMessage(context, validateResult.getValidateResult());
      throw new SchemaFileInvalidException(validateResult.getValidateResult());
    }
    return resource;
  }

  private static // BasicModule module
  Integer createNewSnapshot(// BasicModule module
                            final Snapshot snapshot, // BasicModule module
                            final String memo, // BasicModule module
                            RunContext runContext, // BasicModule module
                            Long userid, String userName) {
    Integer newId;
    snapshot.setSnId(null);
    snapshot.setUpdateTime(new Date());
    snapshot.setCreateTime(new Date());
    // snapshot.setCreateUserName();
    try {
      snapshot.setCreateUserId(userid);
    } catch (Throwable e) {
      snapshot.setCreateUserId(0l);
    }
    snapshot.setCreateUserName(userName);
    if (StringUtils.isNotEmpty(memo)) {
      snapshot.setMemo(memo);
    }
    // 插入一条新纪录
    newId = runContext.getSnapshotDAO().insert(snapshot);
    if (newId == null) {
      throw new IllegalArgumentException(" have not create a new snapshot id");
    }
    return newId;
  }

  /**
   * 删除一条记录
   *
   * @param context
   */
  @Func(PermissionConstant.APP_DELETE)
  public void doDelete(Context context) {
    Integer appid = this.getInt("appid");
    Assert.assertNotNull("appid can not be null", appid);
    // 需要判断在solr cluster中是否存在
    DocCollection collection = this.getZkStateReader().getClusterState().getCollectionOrNull(this.getAppDomain().getAppName());
    if (collection != null) {
      this.addErrorMessage(context, "集群中存在索引实例“" + collection.getName() + "”，请先联系管理员将该实例删除");
      return;
    }
    ServerGroupCriteria criteria = new ServerGroupCriteria();
    criteria.createCriteria().andAppIdEqualTo(appid);
    // group 表删除
    this.getServerGroupDAO().deleteByExample(criteria);
    AppTriggerJobRelationCriteria acriteria = new AppTriggerJobRelationCriteria();
    acriteria.createCriteria().andAppIdEqualTo(appid);
    // 触发表刪除
    this.getAppTriggerJobRelationDAO().deleteByExample(acriteria);
    this.getApplicationDAO().deleteByPrimaryKey(appid);
    this.addActionMessage(context, "索引实例“" + this.getAppDomain().getAppName() + "”该应用被成功删除");
  }

  /**
   * @param app
   * @param tplPublishSnapshotId 模板ID
   * @param schemaContent
   * @param context
   * @param module
   * @param
   * @return
   * @throws Exception
   */
  public static CreateSnapshotResult createApplication(Application app, Integer tplPublishSnapshotId, byte[] schemaContent, Context context, BasicModule module) throws Exception {
    final Integer newAppid = module.getApplicationDAO().insertSelective(app);
    IUser loginUser = module.getUser();
    CreateSnapshotResult snapshotResult = new CreateSnapshotResult();
    snapshotResult.setNewAppId(newAppid);
    snapshotResult.setSuccess(true);
    if (schemaContent != null) {
      SnapshotDomain domain = module.getSnapshotViewDAO().getView(tplPublishSnapshotId);
      domain.getSnapshot().setAppId(newAppid);
      snapshotResult = createNewSnapshot(// Long.parseLong(loginUser.getId())
        context, // Long.parseLong(loginUser.getId())
        domain, // Long.parseLong(loginUser.getId())
        ConfigFileReader.FILE_SCHEMA, // Long.parseLong(loginUser.getId())
        schemaContent, // Long.parseLong(loginUser.getId())
        module, // Long.parseLong(loginUser.getId())
        StringUtils.EMPTY, -1l, loginUser.getName());
      snapshotResult.setNewAppId(newAppid);
      if (!snapshotResult.isSuccess()) {
        return snapshotResult;
      }
    }
    int offset = (int) (Math.random() * 10);
    // TriggerAction.createJob(newAppid, context, "0 0 " + offset + " * * ?", JobConstant.JOB_TYPE_FULL_DUMP, module, triggerContext);
    // TriggerAction.createJob(newAppid, context, "0 0/10 * * * ?", JobConstant.JOB_INCREASE_DUMP, module, triggerContext);
    // 创建默认组和服务器
    GroupAction.createGroup(RunEnvironment.DAILY, FIRST_GROUP_INDEX, newAppid, snapshotResult.getNewId(), module.getServerGroupDAO());
    GroupAction.createGroup(RunEnvironment.ONLINE, FIRST_GROUP_INDEX, newAppid, snapshotResult.getNewId(), module.getServerGroupDAO());
    return snapshotResult;
  }

  public static class IpsValidateResult {

    private final Map<String, String> ipsValidateResult = new HashMap<String, String>();

    public String put(String key, String value) {
      return ipsValidateResult.put(key, value);
    }

    private boolean success = true;

    public boolean isSuccess() {
      return success;
    }

    public boolean isValidate(String ip) {
      Matcher matcher = pattern.matcher(StringUtils.trimToEmpty(ipsValidateResult.get(ip)));
      return matcher.matches();
      // return StringUtils.isNumeric(StringUtils.substringAfter(
      // , "version:"));
    }

    public String getMsg(String ip) {
      return ipsValidateResult.get(ip);
    }
  }

  private static final Pattern pattern = Pattern.compile("version:\\d+");

  public static class AddReplic {

    private String appname;

    private int groupCount;

    private Integer replica;

    private RunEnvironment runtime;

    private List<String> servers;

    public AddReplic(String appname, int groupCount, Integer replica, RunEnvironment runtime, List<String> servers) {
      super();
      this.appname = appname;
      this.groupCount = groupCount;
      this.replica = replica;
      this.runtime = runtime;
      this.servers = servers;
    }

    // public void setClientProtocol(CoreManagerClient manageClient) {
    //
    // }
    public void publishNewCore() {
    }

    public Map<String, Set<String>> createPublishNewCoreParam() {
      return Collections.emptyMap();
    }
  }

  public static Department getDepartment(RunContext runcontext, Integer aliDptId) {
    // Department department = null;
    DepartmentCriteria criteria = new DepartmentCriteria();
    criteria.createCriteria().andDptIdEqualTo(aliDptId);
    for (Department dpt : runcontext.getDepartmentDAO().selectByExample(criteria)) {
      return dpt;
    }
    throw new IllegalArgumentException("aliDptId:" + aliDptId + " can not find any department obj");
  }

  /**
   * 从其他应用拷贝配置文件
   *
   * @param context
   */
  @Func(PermissionConstant.CONFIG_UPLOAD)
  public void doCopyConfigFromOtherApp(Context context) {
    // 拷贝源
    Integer fromAppId = this.getInt("hiddenAppnamesuggest");
    if (fromAppId == null) {
      fromAppId = this.getInt("combAppid");
    }
    Assert.assertNotNull("fromAppId can not be null", fromAppId);
    Application fromApp = this.getApplicationDAO().loadFromWriteDB(fromAppId);
    if (fromApp == null) {
      this.addErrorMessage(context, "拷贝源应用已经删除，请重新选择");
      return;
    }
    // 拷贝目的地
    Integer toAppId = this.getInt("toAppId");
    Assert.assertNotNull("toAppId can not be null", toAppId);
    Application destinationApp = this.getApplicationDAO().loadFromWriteDB(toAppId);
    if (destinationApp == null) {
      this.addErrorMessage(context, "拷贝目标应用已经删除");
      return;
    }
    final ServerGroup group = DownloadServlet.getServerGroup(fromAppId, (new Integer(FIRST_GROUP_INDEX)).shortValue(), this.getAppDomain().getRunEnvironment().getId(), getServerGroupDAO());
    if (group == null) {
      this.addErrorMessage(context, "拷贝目标应用还没有定义组");
      return;
    }
    if (group.getPublishSnapshotId() == null) {
      this.addErrorMessage(context, "拷贝目标应用还没有设定配置文件版本");
      return;
    }
    // 开始插入配置文件
    // 先插入snapshot
    Snapshot newSnapshto = this.getSnapshotDAO().loadFromWriteDB(group.getPublishSnapshotId());
    newSnapshto.setCreateTime(new Date());
    newSnapshto.setUpdateTime(new Date());
    newSnapshto.setPreSnId(newSnapshto.getSnId());
    newSnapshto.setSnId(null);
    newSnapshto.setAppId(toAppId);
    newSnapshto.setMemo("从应用“" + fromApp.getProjectName() + "” 拷贝而来");
    try {
      newSnapshto.setCreateUserId(Long.parseLong(this.getUserId()));
    } catch (Throwable e) {
    }
    newSnapshto.setCreateUserName(this.getLoginUserName());
    // final Integer newsnapshotId =
    this.getSnapshotDAO().insertSelective(newSnapshto);
    // 更新目标组目标组
    // ServerGroup destinatGroup = DownloadServlet.getServerGroup(toAppId,
    // (new Integer(FIRST_GROUP_INDEX)).shortValue(),
    // RunEnvironment.DAILY.getId(), getServerGroupDAO());
    // if (destinatGroup == null) {
    // // 插入组
    // destinatGroup = getServerGroupDAO().loadFromWriteDB(
    // GroupAction.createGroup(context, RunEnvironment.DAILY,
    // FIRST_GROUP_INDEX, toAppId, this));
    // }
    // Assert.assertNotNull("destinatGroup can not be null", destinatGroup);
    // ServerGroup updateGroup = new ServerGroup();
    // updateGroup.setPublishSnapshotId(newsnapshotId);
    // ServerGroupCriteria groupQuery = new ServerGroupCriteria();
    // groupQuery.createCriteria().andGidEqualTo(destinatGroup.getGid());
    // getServerGroupDAO().updateByExampleSelective(updateGroup,
    // groupQuery);
    this.addActionMessage(context, "拷贝源应用“" + fromApp.getProjectName() + "”已经成功复制到目标应用“" + destinationApp.getProjectName() + "”");
  }

  // @Func(PermissionConstant.APP_SERVER_GROUP_SET)
  // public void doUpdateYuntipath(// @FormGroup("appupdate") Application
  // form,
  // // Navigator nav,
  // Context context) {
  // Integer groupid = this.getInt("groupid");
  // Assert.assertNotNull(groupid);
  //
  // ServerGroup serverGroup = new ServerGroup();
  // if (!setYuntiPath(serverGroup, context)) {
  // return;
  // }
  //
  // ServerGroupCriteria groupCriteria = new ServerGroupCriteria();
  // groupCriteria.createCriteria().andGidEqualTo(groupid);
  // this.getServerGroupDAO().updateByExampleSelective(serverGroup,
  // groupCriteria);
  //
  // AppDomainInfo appdomain = this.getAppDomain();
  // // this.addActionMessage(context, msg)
  // this.addActionMessage(context,
  // "应用:" + appdomain.getAppName() + " 环境："
  // + appdomain.getRunEnvironment().getDescribe()
  // + "的云梯路径已经成功更新成功：" + serverGroup.getYuntiPath());
  // }

  /**
   * 更新应用
   */
  @Func(PermissionConstant.APP_UPDATE)
  public // Navigator nav,
  void doUpdate(Context context) {
    Application form = new Application();
    context.put("app", form);
    Integer bizid = this.getInt("bizid");
    final Integer appid = this.getInt("appid");
    Assert.assertNotNull(bizid);
    Assert.assertNotNull(appid);
    form.setAppId(appid);
    DepartmentCriteria criteria = new DepartmentCriteria();
    criteria.createCriteria().andDptIdEqualTo(bizid).andLeafEqualTo(BooleanYorNConvertCallback.YES);
    List<Department> depatment = this.getDepartmentDAO().selectByExample(criteria);
    Assert.assertTrue("dptid:" + bizid + " depatment can not be null ", depatment.size() == 1);
    for (Department d : depatment) {
      form.setDptId(d.getDptId());
      form.setDptName(d.getFullName());
    }
    form.setProjectName(this.getString("projectName"));
    form.setRecept(this.getString("recept"));
    if (!isAppNameValid(this, context, form)) {
      return;
    }
    // 是否使用自动部署新方案
    form.setIsAutoDeploy("true".equalsIgnoreCase(this.getString("isautodeploy")));
    if (!validateAppForm(context, form)) {
      return;
    }
    IAppsFetcher fetcher = getAppsFetcher();
    fetcher.update(form, new CriteriaSetter() {

      @Override
      public void set(Criteria criteria) {
        criteria.andAppIdEqualTo(appid);
      }
    });
    this.addActionMessage(context, "已经成功更新应用[" + form.getProjectName() + "]");
  }

  // private boolean setYuntiPath(IYuntiPath yuntipath, Context context) {
  // if ("true".equals(this.getString("yunti"))) {
  // String yuntiPath = this.getString("yuntiPath");
  // if (StringUtils.isEmpty(yuntiPath)) {
  // this.addErrorMessage(context, "请填写云梯路径");
  // return false;
  // }
  //
  // String yuntiToken = this.getString("yuntiToken");
  // yuntipath.setYuntiPath(
  // YuntiPathInfo.createYuntiPathInfo(yuntiPath, yuntiToken));
  // } else {
  // // 设置为空字符串
  // yuntipath.setYuntiPath(StringUtils.EMPTY);
  // }
  //
  // return true;
  // }
  // @Autowired
  // public void setTerminatorTriggerBizDalDaoFacade(ITerminatorTriggerBizDalDAOFacade triggerDaoContext) {
  // this.triggerContext = triggerDaoContext;
  // }
  private static final Pattern APPNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

  public static void main(String[] arg) throws Exception {
    System.out.println("search4realjhsItemtest");
    Matcher m = APPNAME_PATTERN.matcher("search4realj_hsItemtest");
    System.out.println(m.matches());
  }

  public static boolean isAppNameValid(IFieldErrorHandler msgHandler, Context context, Application form) {
    Matcher m = APPNAME_PATTERN.matcher(form.getProjectName());
    if (!m.matches()) {
      msgHandler.addFieldError(context, FIELD_PROJECT_NAME, "必须用小写字母或大写字母数字组成");
      return false;
    }
    return true;
  }

  private boolean validateAppForm(Context context, Application app) {
    if (StringUtils.isBlank(app.getProjectName())) {
      this.addErrorMessage(context, "索引名称不能为空");
      return false;
    }
    if (StringUtils.isBlank(app.getRecept())) {
      this.addErrorMessage(context, "接口人不能为空");
      return false;
    }
    return true;
  }

  public static class ExtendApp extends Application {

    private static final long serialVersionUID = 1L;

    private List<Option> selectableDepartment;

    // // 应用选择的模板
    // private String tisTpl;
    private String workflow;

    @JSONField(serialize = false)
    @Override
    public Integer getWorkFlowId() {
      return Integer.parseInt(StringUtils.substringBefore(this.workflow, ":"));
    }

    public void setName(String name) {
      this.setProjectName(name);
    }

    // // public void setTisTpl(String val) {
    // this.tisTpl = val;
    // }
    public void setWorkflow(String val) {
      this.workflow = val;
    }

    // public String getTisTpl() {
    // return tisTpl;
    // }
    public List<Option> getSelectableDepartment() {
      return selectableDepartment;
    }

    public void setSelectableDepartment(List<Option> selectableDepartment) {
      this.selectableDepartment = selectableDepartment;
    }

    public String getWorkflow() {
      return workflow;
    }
  }

  @Autowired
  public void setOfflineManager(OfflineManager offlineManager) {
    this.offlineManager = offlineManager;
  }

  @SuppressWarnings("all")
  public void doCopyFromOtherIndex(Context context) throws Exception {
    final String colonFrom = this.getString("appname");
    if (StringUtils.startsWith(colonFrom, "search4")) {
      throw new IllegalArgumentException("colonFrom:" + colonFrom + " is not start with 'search4'");
    }
    BasicDAO<Application, ApplicationCriteria> basicDao = (BasicDAO<Application, ApplicationCriteria>) this.getApplicationDAO();
    DataSource dataSource = (DataSource) basicDao.getDataSource();
    Connection conn = dataSource.getConnection();
    PreparedStatement statement = conn.prepareStatement("select app_id from application where project_name = ?");
    statement.setString(1, colonFrom);
    Integer oldAppId = null;
    ResultSet result = statement.executeQuery();
    if (result.next()) {
      oldAppId = result.getInt(1);
    }
    result.close();
    statement.close();
    Assert.assertNotNull(oldAppId);
    String insertSql = "insert into application(project_name,recept,manager,create_time,update_time" + ", is_auto_deploy, dpt_id,  dpt_name ) " + "select  concat('search4', 'N', SUBSTRING(project_name,8)) as project_name " + ",recept,manager,create_time,update_time " + ",'Y' as is_auto_deploy, 8 as dpt_id, '淘宝网-产品技术部-综合业务平台-互动业务平台-终搜' as dpt_name " + "from application where app_id = ?";
    statement = conn.prepareStatement(insertSql);
    statement.setInt(1, oldAppId);
    Assert.assertTrue(statement.execute());
    statement.close();
    statement = conn.prepareStatement(" SELECT LAST_INSERT_ID()");
    result = statement.executeQuery();
    Integer newAppId = null;
    if (result.next()) {
      newAppId = result.getInt(1);
    }
    Assert.assertNotNull(newAppId);
    result.close();
    statement.close();
    statement = conn.prepareStatement("insert into" + "snapshot(create_time,update_time,app_id,res_schema_id ,res_solr_id ,res_jar_id ,res_core_prop_id ,res_ds_id ,res_application_id ,pre_sn_id)" + " select create_time,update_time,? as app_id,res_schema_id ,res_solr_id ,res_jar_id ,res_core_prop_id ,res_ds_id ,res_application_id ,pre_sn_id" + "   from snapshot " + " where sn_id in (select publish_snapshot_id from server_group where publish_snapshot_id is not null and app_id = ?)");
    statement.setInt(1, newAppId);
    statement.setInt(2, oldAppId);
    statement.execute();
    statement.close();
    conn.close();
  }
}
