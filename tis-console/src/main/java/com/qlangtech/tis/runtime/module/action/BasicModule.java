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
package com.qlangtech.tis.runtime.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import com.koubei.web.tag.pager.LinkBuilder;
import com.koubei.web.tag.pager.Pager;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.coredefine.module.action.CoreAction;
import com.qlangtech.tis.coredefine.module.action.ProcessModel;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.job.DataXJobWorker;
import com.qlangtech.tis.datax.job.SSERunnable;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.indexbuild.LuceneVersion;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.lang.TisException.ErrorCode;
import com.qlangtech.tis.manage.biz.dal.dao.IAppTriggerJobRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IBizFuncAuthorityDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IClusterSnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncRoleRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IGroupInfoDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRoleDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerGroupDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotViewDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUploadResourceDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.CheckAppDomainExistValve;
import com.qlangtech.tis.manage.common.CreateNewTaskResult;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.manage.common.Module;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.RunContextGetter;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.common.UserUtils;
import com.qlangtech.tis.manage.common.apps.AppsFetcher;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.manage.common.apps.IDepartmentGetter;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.misc.BasicRundata;
import com.qlangtech.tis.runtime.module.misc.DefaultMessageHandler;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.DelegateControl4JavaBeanMsgHandler;
import com.qlangtech.tis.runtime.pojo.ServerGroupAdapter;
import com.qlangtech.tis.sql.parser.er.ERRules;
import com.qlangtech.tis.sql.parser.er.IERRulesGetter;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.IUploadPluginMeta;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.workflow.dao.IWorkFlowBuildHistoryDAO;
import com.qlangtech.tis.workflow.dao.IWorkFlowDAO;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年4月18日下午7:58:02
 */
public abstract class BasicModule extends ActionSupport implements RunContext, IControlMsgHandler, IPluginContext, IERRulesGetter {

  public static final long serialVersionUID = 1L;
  public static final String KEY_PLUGIN = "plugin";

  private static final Logger logger = LoggerFactory.getLogger("executeaction");

  // public static final int PAGE_SIZE = 30;
  private final Context context = new MockContext();
  protected IClusterSnapshotDAO clusterSnapshotDAO;
  protected IWorkflowDAOFacade wfDAOFacade;
  private IERRulesGetter erRulesGetter;

  @Override
  public Optional<ERRules> getErRules(String dfName) {
    Objects.requireNonNull(erRulesGetter, "erRulesGetter can not be null");
    return erRulesGetter.getErRules(dfName);
  }

  protected List<UploadPluginMeta> getPluginMeta() {
    return getPluginMeta(true);
  }

  protected List<UploadPluginMeta> getPluginMeta(boolean validatePluginEmpty) {
    final boolean useCache = Boolean.parseBoolean(this.getString("use_cache", "true"));
    // return UploadPluginMeta.parse(this, this.getStringArray("plugin"), useCache);
    return parsePluginMeta(this.getStringArray(KEY_PLUGIN), useCache, validatePluginEmpty)
      .stream().map((meta) -> (UploadPluginMeta) meta).collect(Collectors.toList());
  }

  @Override
  public List<IUploadPluginMeta> parsePluginMeta(String[] plugins, boolean useCache) {
    return parsePluginMeta(plugins, useCache, true);
  }

  public List<IUploadPluginMeta> parsePluginMeta(String[] plugins, boolean useCache, boolean validatePluginEmpty) {
    return UploadPluginMeta.parse(this, plugins, useCache, validatePluginEmpty);
  }


  @Override
  public String execute() throws Exception {
    try {
      IPluginContext.setPluginContext(this);
      this.getRequest().getSession();
      CheckAppDomainExistValve.getAppDomain(this);
      // 解析这个方法 event_submit_do_buildjob_by_server
      Method executeMethod = getExecuteMethod();
      logger.info(this.getClass().getName() + ":" + executeMethod.getName());
      executeMethod.invoke(this, context);
      return getReturnCode();
    } finally {
      IPluginContext.pluginContextThreadLocal.remove();
    }
  }

  /**
   * 处理恢复TIS异常
   *
   * @param context
   */
  public final void doExceptionRestore(Context context) {
    ErrorCode errorCode = TisException.parse(this.getString("errorCode"));
    errorCode.execForward(this.getRundata());
  }

  public IClusterSnapshotDAO getClusterSnapshotDAO() {
    return clusterSnapshotDAO;
  }

  private ServerGroupAdapter groupConfig;

  public ServerGroupAdapter getConfigGroup0() {
    if (groupConfig == null) {
      groupConfig = this.getServerGroup0();
    }
    return groupConfig;
  }

  /**
   * 该应用下的第0组配置
   *
   * @return
   */
  protected ServerGroupAdapter getServerGroup0() {
    final AppDomainInfo domain = CheckAppDomainExistValve.getAppDomain(this);
    return CoreAction.getServerGroup0(domain, this);
  }

  public WorkFlow loadDF(Integer wfId) {
    WorkFlow workFlow = this.getWorkflowDAOFacade().getWorkFlowDAO().loadFromWriteDB(wfId);
    return Objects.requireNonNull(workFlow, "wfId:" + wfId + " relevant wf can not be null");
  }

  /**
   * 插件运行环境是否和数据源相关
   *
   * @return
   */
  @Override
  public boolean isDataSourceAware() {
    return false;
  }

  @Override
  public void addDb(Descriptor.ParseDescribable<DataSourceFactory> dbDesc, String dbName, Context context, boolean shallUpdateDB) {
    throw new UnsupportedOperationException(this.getClass().getName());
  }


  @Override
  public boolean isCollectionAware() {

    AppDomainInfo appDoamin = this.getAppDomain();
//    if (appDoamin.getAppType() == AppType.DataXPipe) {
//
//    }
    return !(appDoamin instanceof AppDomainInfo.EnvironmentAppDomainInfo);
  }


  @Override
  public final String getExecId() {
    String execId = this.getRequest().getHeader(KeyedPluginStore.KEY_EXEC_ID);
    if (StringUtils.isBlank(execId)) {
      throw new IllegalStateException("execId shall present in Request Header");
    }
    return execId;
  }

  public DataXName getCollectionName() {
    String collection = this.getAppDomain().getAppName();

    if (StringUtils.isNotEmpty(collection)
      && DataXJobWorker.notMatchK8SDataXAndFlinkCluster(new TargetResName(collection))) {

      List<UploadPluginMeta> metas = getPluginMeta(false);
      for (UploadPluginMeta pm : metas) {
        // 在url 参数上设置的DataX名称优先级高
        DataXName dataX = pm.getDataXName(false);
        if (dataX != null) {
          return dataX;
        }
      }
    }

    if (StringUtils.isBlank(collection)) {
      throw new IllegalStateException("param collection can not be null");
    }

    final String processModel = this.getString(StoreResourceType.KEY_PROCESS_MODEL);
    if (StringUtils.isNotEmpty(processModel)) {
      ProcessModel process = ProcessModel.parse(processModel);
      return new DataXName(collection, process.resType);
    }

    return DataXName.createDataXPipeline(collection);
  }

  @Override
  public String getRequestHeader(String key) {
    return this.getRequest().getHeader(key);
  }

  @Override
  public final void executeBizLogic(BizLogic logicType, Context context, Object param) throws Exception {
    switch (logicType) {
      case CREATE_DATA_PIPELINE: {
        String dataxName = (String) param;
        if (StringUtils.isEmpty(dataxName)) {
          throw new IllegalArgumentException("param dataXName can not be null");
        }
        this.createPipeline(context, dataxName);
        return;
      }
      default:
        throw new IllegalStateException("illegal logicType:" + logicType);
    }
  }

  @Override
  public boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value) {
    switch (logicType) {
      case VALIDATE_APP_NAME_DUPLICATE:
        AppNameDuplicateValidator nameDuplicateValidator = new AppNameDuplicateValidator(this.getApplicationDAO());
        return nameDuplicateValidator.validate(this, context, fieldName, value);
      case VALIDATE_WORKFLOW_NAME_DUPLICATE:

        DataFlowDuplicateValidator wfValidator = new DataFlowDuplicateValidator(this.wfDAOFacade.getWorkFlowDAO());
        return wfValidator.validate(this, context, fieldName, value);
//      case DB_NAME_DUPLICATE:
//        DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
//        dbCriteria.createCriteria().andNameEqualTo(value);
//        int existDBsCount = this.getWorkflowDAOFacade().getDatasourceDbDAO().countByExample(dbCriteria);
//        if (existDBsCount > 0) {
//          this.addFieldError(context, fieldName, IdentityName.MSG_ERROR_NAME_DUPLICATE);
//          return false;
//        }
//        return true;
      default:
        throw new IllegalStateException("illegal logicType:" + logicType);
    }
  }

  @SuppressWarnings("all")
  protected <T> T getObj(String key) {
    return (T) this.getContext().get(key);
  }

  public Method getExecuteMethod() throws NoSuchMethodException {
    final String mehtodName = parseMehtodName();
    Method executeMethod = this.getClass().getMethod(mehtodName, Context.class);
    return executeMethod;
  }

//  /**
//   * @return
//   */
//  public DocCollection getIndex() {
//    String index = this.getAppDomain().getAppName();
//    if (StringUtils.isEmpty(index)) {
//      throw new IllegalStateException("index name can not be null");
//    }
//    ClusterState.CollectionRef ref = this.getZkStateReader().getClusterState().getCollectionRef(index);
//    return ref.get();
//  }


  protected boolean isIndexExist() {
    AppDomainInfo app = this.getAppDomain();
    String collection = app.getAppName();
    if (StringUtils.isEmpty(collection)) {
      throw new IllegalStateException("param 'collection' can not be null");
    }
    this.getApplicationDAO().updateLastProcessTime(collection);
//    if (app.getAppType() == AppType.SolrIndex) {
//      return this.getSolrZkClient().exists("/collections/" + collection + "/state.json", true);
//    }
    return true;
  }

  public static final String key_FORWARD = "forward";

  public static ActionContext getActionContext() {
    return ActionContext.getContext();
  }

  protected String getReturnCode() {

    // ActionContext actionCtx = ActionContext.getContext();
    //ActionProxy proxy = actionCtx.getActionInvocation().getProxy();
    // 并且只有screen中的 模块可以设置forward
    if (this.getRequest().getAttribute(TERMINATOR_FORWARD) != null) {
      return key_FORWARD;
    }
    final String moduleName = this.getClass().getSimpleName();
//    ActionContext actionContext = ServletActionContext.getActionContext(this.getRequest());
//    ActionMapping mapping = ServletActionContext.getActionMapping();
//    if (mapping == null) {
//      return moduleName;
//    }

//    if (org.apache.commons.lang3.StringUtils.endsWith(proxy.getNamespace(), TisActionMapper.ACTION_TOKEN)) {
//      return NONE;
//    }

//    if ("action".equalsIgnoreCase(mapping.getExtension())) {
    // return NONE;
//    }
//    // 当前是否是action执行
//    if (isActionSubmit(mapping)) {
//      // moduleName + "_action";
//      return key_FORWARD;
//    }
    //  return moduleName + (StringUtils.equals("ajax", mapping.getExtension()) ? "_ajax" : StringUtils.EMPTY);

    return moduleName + "_ajax";// : StringUtils.EMPTY);
  }

//  public static boolean isScreenApply() {
//    return "screen".equals(StringUtils.substringAfter(getActionProxy().getNamespace(), "#"));
//  }

  public static final boolean isActionSubmit(ActionMapping mapping) {
    return "action".equals(StringUtils.substringAfter(getActionProxy().getNamespace(), "#"))
      && !(StringUtils.equals("ajax", mapping.getExtension()));
  }

  public static final String TERMINATOR_FORWARD = "terminatorForward";

  private static final Pattern COMPONENT_PATTERN = Pattern.compile("/[^/|^#]+");

  public static final String Layout_template = "layout_template";

  @Override
  public ITISCoordinator getSolrZkClient() {
    return getDaoContext().getSolrZkClient();
  }

  private static Rundata createRundata() {
    return new Rundata() {
      @Override
      public String getStringParam(String key) {
        return getRequest().getParameter(key);
      }

      @Override
      public HttpServletRequest getRequest() {
        return ServletActionContext.getRequest();
      }

//      public void forwardTo(String target) {
//        // 设置跳转到的地方可以是 action 或者 vm
//        // getRequest().setAttribute(TERMINATOR_FORWARD,
//        // new Forward(null, target));
//        forwardTo(null, target);
//        return;
//      }

      @Override
      public void forwardTo(String namespace, String target, String method) {
        getRequest().setAttribute(TERMINATOR_FORWARD, new Forward(namespace, target, method));
        return;
      }

      public void setLayout(String layout) {
        if (StringUtils.isBlank(layout)) {
          throw new IllegalArgumentException("param layout can not be null");
        }
        getRequest().setAttribute("layout_template", layout);
        //
        // final String namespace =
        // getActionContext().getActionInvocation().getProxy().getNamespace();
        // Matcher matcher = COMPONENT_PATTERN.matcher(namespace);
        //
        // if (matcher.find()) {
        // getRequest().setAttribute("layout_template",
        // matcher.group() + "/templates/layout/" + layout + ".vm");
        // } else {
        // throw new IllegalArgumentException(
        // "namespace:" + namespace + " can not match pattern " +
        // COMPONENT_PATTERN);
        // }
        //
      }

      public void redirectTo(String target) {
        try {
          getResponse().sendRedirect(target);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public Rundata getRundata() {
    return getRundataInstance();
  }

  public static Rundata getRundataInstance() {
    if (rundata == null) {
      synchronized (BasicModule.class) {
        if (rundata == null) {
          rundata = createRundata();
        }
      }
    }
    return rundata;
  }

  public void doBigPipe(ServletOutputStream ouputStream) throws Exception {
  }

  private static Rundata rundata;

  @Autowired
  public void setClusterSnapshotDAO(IClusterSnapshotDAO clusterSnapshotDAO) {
    this.clusterSnapshotDAO = clusterSnapshotDAO;
  }

  @Autowired
  public void setWfDaoFacade(IWorkflowDAOFacade facade) {
    this.wfDAOFacade = facade;
  }

  protected void createPipeline(Context context, String dataxName) throws Exception {
    DataxProcessor dataxProcessor = (DataxProcessor) DataxProcessor.load(null, StoreResourceType.DataApp, dataxName);
    Application app = dataxProcessor.buildApp();

    SchemaAction.CreateAppResult createAppResult
      = this.createNewApp(context, app, dataxProcessor, false, (newAppId) -> {
      SchemaAction.CreateAppResult appResult = new SchemaAction.CreateAppResult();
      appResult.setSuccess(true);
      appResult.setNewAppId(newAppId);
      return appResult;
    });
  }

  public static class DataFlowDuplicateValidator implements Validator.IFieldValidator {
    private final IWorkFlowDAO workFlowDAO;

    public DataFlowDuplicateValidator(IWorkFlowDAO workFlowDAO) {
      this.workFlowDAO = workFlowDAO;
    }


    @Override
    public boolean validate(IFieldErrorHandler msgHandler, Context context, String fieldKey, String fieldData) {

//      Application app = new Application();
//      app.setProjectName(fieldData);
//      if (!AddAppAction.isAppNameValid(msgHandler, context, fieldKey, app)) {
//        return false;
//      }
      WorkFlowCriteria criteria = new WorkFlowCriteria();
      criteria.createCriteria().andNameEqualTo(fieldData);
      if (workFlowDAO.countByExample(criteria) > 0) {
        msgHandler.addFieldError(context, fieldKey, "已经有同名实例(‘" + fieldData + "’)存在");
        return false;
      }

      return true;
    }
  }

  public static class AppNameDuplicateValidator implements Validator.IFieldValidator {
    //private final Application app;
    private final IApplicationDAO appDAO;

    public AppNameDuplicateValidator(IApplicationDAO appDAO) {
      // this.app = app;
      this.appDAO = appDAO;
    }

    @Override
    public boolean validate(IFieldErrorHandler msgHandler, Context context, String fieldKey, String fieldData) {
      Application app = new Application();
      app.setProjectName(fieldData);
      if (!AddAppAction.isAppNameValid(msgHandler, context, fieldKey, app)) {
        return false;
      }
      ApplicationCriteria criteria = new ApplicationCriteria();
      criteria.createCriteria().andProjectNameEqualTo(app.getProjectName());
      if (appDAO.countByExample(criteria) > 0) {
        msgHandler.addFieldError(context, fieldKey, "已经有同名实例(‘" + app.getProjectName() + "’)存在");
        return false;
      }
      return true;
    }
  }

  /**
   * 创建 新的应用
   *
   * @param context
   * @param app
   * @param justValidate
   * @param afterAppCreate
   * @param validateParam
   * @return
   * @throws Exception
   */
  protected SchemaAction.CreateAppResult createNewApp(Context context, Application app, DataxProcessor dataxProcessor
    , boolean justValidate, IAfterApplicationCreate afterAppCreate, Object... validateParam) throws Exception {
    IControlMsgHandler handler = new DelegateControl4JavaBeanMsgHandler(this, app);
    Map<String, Validator.FieldValidators> validateRule = //
      Validator.fieldsValidator(//
        SchemaAction.FIELD_PROJECT_NAME, new Validator.FieldValidators(Validator.require) {
        }, new AppNameDuplicateValidator(this.getApplicationDAO())//
        , //
        SchemaAction.FIELD_Recept, new Validator.FieldValidators(Validator.require) {
        }, //
        SchemaAction.FIELD_DptId, new Validator.FieldValidators(Validator.require) {
        });
    Validator.addValidateRule(validateRule, validateParam);
    SchemaAction.CreateAppResult result = new SchemaAction.CreateAppResult();
    result.setSuccess(true);
    if (!Validator.validate(handler, context, validateRule)) {
      return result.setSuccess(false);
    }

    app.setDptName(AddAppAction.getDepartment(this, app.getDptId()).getFullName());
    app.setCreateTime(new Date());
    app.setIsAutoDeploy(true);
    if (!justValidate) {
      result = AddAppAction.createApplication(app, context, this, afterAppCreate);

//      Optional<DataXJobSubmit> dataXJobSubmit //
//        = DataXJobSubmit.getDataXJobSubmit(false, DataXJobSubmit.getDataXTriggerType());
//      if (!dataXJobSubmit.isPresent()) {
//        throw new IllegalStateException("dataXJobSubmit must be present");
//      }
      // 这里可以在pwoerjob 中创建workflow任务
      // dataXJobSubmit.get().createJob(this, context, dataxProcessor);

      DataXJobSubmit.getPowerJobSubmit().ifPresent((submit) -> {
        submit.createJob(this, context, dataxProcessor);
      });

      addActionMessage(context, "已经成功创建实例[" + app.getProjectName() + "]");
    }
    return result;
  }

  public interface Rundata extends BasicRundata {

    public HttpServletRequest getRequest();


    // public Context getContext();
  }

  private static final String MANAGE_TOOL = "manageTool";

  // private final PageControl pageControl = new PageControl();
  // public PageControl getControl() {
  // return this.pageControl;
  // }

  /**
   * 页面取得工具Util
   *
   * @return
   */
  public ManageUtils getManageTool() {
    ManageUtils result = null;
    if ((result = (ManageUtils) this.getRequest().getAttribute(MANAGE_TOOL)) == null) {
      result = new ManageUtils();
      result.setRequest(this.getRequest());
      result.setDaoContext(this);
      this.getRequest().setAttribute(MANAGE_TOOL, result);
    }
    return result;
  }

  private static final Module manageModule = new Module("/runtime");

  private static final Module coredefineModule = new Module("/coredefine");

  private static final Module trigger = new Module("/trigger");

  public Module getCoredefine() {
    return coredefineModule;
  }

  public Module getManageModule() {
    return manageModule;
  }

  public Module getTrigger() {
    return trigger;
  }

  public static final String KEY_MEHTO = "emethod";

  public static String parseMehtodName() {
    HttpServletRequest request = ServletActionContext.getRequest();

    String forwardMethod = (String) request.getAttribute(KEY_MEHTO);
    if (forwardMethod != null) {
      //request.removeAttribute(KEY_MEHTO);
      return normalizeExecuteMethod("event_submit_do_" + forwardMethod);
    }

    // 判断参数的emethod参数
    final String execMethod = request.getParameter(KEY_MEHTO);
    if (StringUtils.isNotBlank(execMethod)) {
      return normalizeExecuteMethod("event_submit_do_" + execMethod);
    }
    Enumeration<?> params = request.getParameterNames();
    String param = null;
    while (params.hasMoreElements()) {
      if (StringUtils.startsWith(param = String.valueOf(params.nextElement()), "event_submit_")) {
        return normalizeExecuteMethod(param);
      }
    }
    return ActionConfig.DEFAULT_METHOD;
  }

  private static ActionProxy getActionProxy() {
    return ActionContext.getContext().getActionInvocation().getProxy();
  }

  /**
   * 将 event_submit_do_buildjob_by_server 解析成 doBuildjobByServer
   *
   * @param param
   * @return
   */
  private static String normalizeExecuteMethod(String param) {
    char[] pc = Arrays.copyOfRange(param.toCharArray(), 13, param.length());
    return trimUnderline(pc).toString();
  }

  public static StringBuffer trimUnderline(char[] pc) {
    boolean underline = false;
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < pc.length; i++) {
      if ('_' != pc[i]) {
        result.append(underline ? Character.toUpperCase(pc[i]) : pc[i]);
        underline = false;
      } else {
        underline = true;
      }
    }
    return result;
  }

  protected Snapshot createSnapshot() {
    return createSnapshot(this);
  }

  public static IAppsFetcher getAppsFetcher(HttpServletRequest request, boolean maxMatch, IUser user, RunContext context) {
    if (maxMatch) {
      return AppsFetcher.create(user, context, true);
    }
    return UserUtils.getAppsFetcher(request, context);
  }

  public IAppsFetcher getAppsFetcher() {
    return getAppsFetcher(this.getRequest(), false, this.getUser(), this);
  }


  public static final Snapshot createSnapshot(final BasicModule basicModule) {
    return createSnapshot(basicModule, new SnapshotSetter() {
      @Override
      public void set(Snapshot snapshot) {
        snapshot.setAppId(basicModule.getAppDomain().getAppid());
      }
    });
  }

  public static final Snapshot createSnapshot(BasicModule basicModule, SnapshotSetter snapshotSetter) {
    Snapshot snapshot = new Snapshot();
    try {
      snapshot.setCreateUserId(Long.parseLong(basicModule.getUserId()));
    } catch (Throwable e) {
      snapshot.setCreateUserId(0l);
    }
    snapshot.setCreateUserName(basicModule.getLoginUserName());
    snapshot.setCreateTime(new Date());
    snapshot.setPreSnId(-1);
    snapshot.setSnId(null);
    snapshotSetter.set(snapshot);
    return snapshot;
  }

  public static interface SnapshotSetter {

    public void set(Snapshot snapshot);
  }

  protected final void writeJson(StringBuffer execResult) throws IOException {
    getResponse().setContentType("text/json;charset=UTF-8");
    getResponse().getWriter().write(execResult.toString());
    getResponse().flushBuffer();
  }

//  @Override
//  public TISZkStateReader getZkStateReader() {
//    return this.getDaoContext().getZkStateReader();
//  }

  public static UsrDptRelation getUserDepartment(BasicModule basicModule) {
    UsrDptRelationCriteria query = new UsrDptRelationCriteria();
    query.createCriteria().andUsrIdEqualTo(basicModule.getUserId());
    List<UsrDptRelation> usrDptRelation = basicModule.getUsrDptRelationDAO().selectByExample(query);
    for (UsrDptRelation depart : usrDptRelation) {
      return depart;
    }
    return null;
  }

  protected static final int PAGE_SIZE = 10;

  protected Pager createPager() {
    Pager pager = Pager.register("page", new LinkBuilder() {

      @Override
      public StringBuffer getPagerUrl() {
        return BasicModule.this.getPagerUrl();
      }

      @Override
      public final StringBuffer getPageUrl(int page) {
        StringBuffer url = new StringBuffer(this.getPagerUrl());
        if (url.toString().indexOf("?") >= 0) {
          url.append("&");
        } else {
          url.append("?");
        }
        url.append("page=").append(page);
        return url;
      }
    }, this.getRequest());
    pager.setSchema("k1");
    pager.setRowsPerPage(PAGE_SIZE);
    pager.setCurPage(this.getPage());
    return pager;
  }

  protected Integer getPage() {
    Integer page = this.getInt("page", 1);
    return page;
  }

  protected StringBuffer getPagerUrl() {
    return new StringBuffer();
  }


  public static final String REQUEST_DOMAIN_KEY = BasicModule.class.getName() + "domain";

  protected void enableChangeDomain(Context context) {
    context.put("domain_change_enable", true);
  }

  /**
   * 是否可以切换应用应用域
   *
   * @return
   */
  public boolean isEnableDomainView() {
    return true;
  }

  protected void disableNavigationBar(Context context) {
    context.put("shallNotNavigationBar", true);
  }


  public static String getEncode() {
    return TisUTF8.getName();
  }

  /**
   * 表单的组
   */
  private final String groupName;

  public String getGroupName() {
    return groupName;
  }

  public BasicModule(String groupName) {
    this.groupName = groupName;
  }

  public BasicModule() {
    this(StringUtils.EMPTY);
  }

  private IUser authtoken;

  public void setAuthtoken(IUser authtoken) {
    this.authtoken = authtoken;
  }

  /**
   * 取得当前登录的用户的姓名
   *
   * @return
   */
  public String getLoginUserName() {
    return this.authtoken.getName();
  }

  public IUser getUser() {
    return this.authtoken;
  }

  public String getUserId() {
    return UserUtils.getUser(this.getRequest(), this).getId();
  }


  protected void setErrorMsgInvisiable(Context context) {
    context.put("errorMsgInvisiable", true);
  }

  public List<Option> getBizLineList() {
    IAppsFetcher appsFetcher = getAppsFetcher(this.getRequest(), this.isMaxMatch(), this.getUser(), this);
    return getDptList(this, appsFetcher);
  }

  /**
   * 取得业务线集合
   *
   * @return
   */
  public static List<Option> getDptList(RunContext runContext, IDepartmentGetter departmentGetter) {

    List<Option> answer = new ArrayList<Option>();

    for (Department domain : departmentGetter.getDepartmentBelongs(runContext)) {

      answer.add(new Option(domain.getFullName(), String.valueOf(domain.getDptId())));
    }
    Collections.sort(answer, new Comparator<Option>() {

      @Override
      public int compare(Option o1, Option o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return answer;
  }

  private boolean maxMatch;

  public final boolean isMaxMatch() {
    return maxMatch;
  }

  public final void setMaxMatch(boolean maxMatch) {
    this.maxMatch = maxMatch;
  }

  /**
   * 取得某业务线下应用
   *
   * @param dptid
   * @return
   */
  protected List<Option> getAppList(final Integer dptid) {
    List<Option> answer = new ArrayList<Option>();
    ApplicationCriteria query = new ApplicationCriteria();
    query.createCriteria().andDptIdEqualTo(dptid);
    for (Application app : this.getApplicationDAO().selectByExample(query)) {
      answer.add(new Option(app.getProjectName(), String.valueOf(app.getAppId())));
    }
    return answer;
  }

  public void doGetAppList() {
  }


  protected Integer getValue(Integer value1, Integer value2) {
    return (value1 != null) ? value1 : value2;
  }

  public final Integer getInt(String key) {
    return getInt(key, null, true);
  }

  public final Integer getInt(String key, Integer defaultVal) {
    return getInt(key, defaultVal, false);
  }

  public final Integer getInt(String key, Integer defaultVal, boolean notIgnoreError) {
    final String val = getRequest().getParameter(key);
    try {
      return Integer.parseInt(val);
    } catch (Throwable e) {
      if (notIgnoreError) {
        throw new RuntimeException("key:" + key + " val:[" + val + "] relevant value is ERROR", e);
      }
      return defaultVal;
    }
  }

  protected final Integer[] getIntAry(String key) {
    String[] params = this.getRequest().getParameterValues(key);
    if (params == null) {
      return new Integer[0];
    }
    Integer[] result = new Integer[params.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = Integer.parseInt(params[i]);
    }
    return result;
  }

  protected final Short getShort(String key) {
    try {
      return Short.parseShort(getRequest().getParameter(key));
    } catch (Throwable e) {
      return null;
    }
  }

  protected final Long getLong(String key) {
    // }
    return getLong(key, null, false);
  }

  protected final Long getLong(String key, Long dftVal) {
    return getLong(key, dftVal, true);
  }

  protected final Long getLong(String key, Long dft, boolean ignoreError) {
    final String val = getRequest().getParameter(key);
    try {
      return Long.parseLong(val);
    } catch (Throwable e) {
      if (!ignoreError) {
        throw new RuntimeException("key:" + key + " val:[" + val + "] relevant value is ERROR", e);
      }
      return dft;
    }
  }

  @Override
  public String getString(String key, String dftVal) {
    return StringUtils.defaultIfBlank(this.getString(key), dftVal);
  }

  @Override
  public final String getString(String key) {

    // Assert.assertNotNull("the request can not be null", getRequest());
    Objects.requireNonNull(getRequest(), "the request can not be null");
    return getRequest().getParameter(key);
  }

  public final String[] getStringArray(String key) {
    Assert.assertNotNull("the request can not be null", getRequest());
    return getRequest().getParameterValues(key);
  }

  public final boolean getBoolean(String key) {
    return Boolean.parseBoolean(getString(key));
  }

  public HttpServletRequest getRequest() {
    // return this.request;
    return ServletActionContext.getRequest();
  }

  protected void setRequestAttribue(String key, Object value) {
    this.getRequest().setAttribute(key, value);
  }

  protected static final HttpServletResponse getResponse() {
    return ServletActionContext.getResponse();
  }


  private RunContextGetter daoContextGetter;

  @Autowired
  public final void setRunContextGetter(RunContextGetter daoContextGetter) {
    this.daoContextGetter = daoContextGetter;
  }

  @Autowired
  public void setErRulesGetter(IERRulesGetter erRulesGetter) {
    this.erRulesGetter = erRulesGetter;
  }

  @Override
  public IResourceParametersDAO getResourceParametersDAO() {
    return getDaoContext().getResourceParametersDAO();
  }


  public IApplicationDAO getApplicationDAO() {
    return getDaoContext().getApplicationDAO();
  }

  protected Application getApplication() {
    String indexName = this.getString("indexname");
    if (!StringUtils.startsWith(indexName, "search4")) {
      throw new IllegalArgumentException("indexName:" + indexName);
    }
    ApplicationCriteria criteria = new ApplicationCriteria();
    criteria.createCriteria().andProjectNameEqualTo(indexName);
    List<Application> apps = this.getApplicationDAO().selectByExample(criteria);
    for (Application app : apps) {
      return app;
    }
    throw new IllegalStateException("can not find app:" + indexName + " in db");
  }

  public IGroupInfoDAO getGroupInfoDAO() {
    return getDaoContext().getGroupInfoDAO();
  }

  public IDepartmentDAO getDepartmentDAO() {
    return getDaoContext().getDepartmentDAO();
  }

  public IUsrDptRelationDAO getUsrDptRelationDAO() {
    return getDaoContext().getUsrDptRelationDAO();
  }

  public final ISnapshotViewDAO getSnapshotViewDAO() {
    return getDaoContext().getSnapshotViewDAO();
  }

  public IServerGroupDAO getServerGroupDAO() {
    return getDaoContext().getServerGroupDAO();
  }

  // @Override
  public IAppTriggerJobRelationDAO getAppTriggerJobRelationDAO() {
    return getDaoContext().getAppTriggerJobRelationDAO();
  }

  // @Override
  public IBizFuncAuthorityDAO getBizFuncAuthorityDAO() {
    return getDaoContext().getBizFuncAuthorityDAO();
  }

  public ISnapshotDAO getSnapshotDAO() {
    return getDaoContext().getSnapshotDAO();
  }

  public IUploadResourceDAO getUploadResourceDAO() {
    return getDaoContext().getUploadResourceDAO();
  }


  @Override
  public IFuncDAO getFuncDAO() {
    return getDaoContext().getFuncDAO();
  }

  @Override
  public IFuncRoleRelationDAO getFuncRoleRelationDAO() {
    return getDaoContext().getFuncRoleRelationDAO();
  }

  @Override
  public IRoleDAO getRoleDAO() {
    return getDaoContext().getRoleDAO();
  }

  protected final IWorkFlowBuildHistoryDAO getHistoryDAO() {
    return this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO();
  }

  @Override
  public IWorkflowDAOFacade getWorkflowDAOFacade() {
    return getDaoContext().getWorkflowDAOFacade();
  }

  private final DefaultMessageHandler messageHandler = new DefaultMessageHandler();

  public void addActionMessage(final Context context, String msg) {
    messageHandler.addActionMessage(context, msg);
  }

  @Override
  public void addFieldError(Context context, String fieldName, String msg, Object... params) {
    messageHandler.addFieldError(context, fieldName, msg, params);
  }

  @Override
  public void setBizResult(Context context, Object result, boolean overwriteable) {
    messageHandler.setBizResult(context, result, overwriteable);
  }

  protected boolean hasErrors(Context context) {
    return context.hasErrors(); //messageHandler.hasErrors(context);
  }

  /**
   * 控制错误信息在页面上显示
   *
   * @param context
   */
  @Override
  public void errorsPageShow(Context context) {
    messageHandler.errorsPageShow(context);
  }

  /**
   * 添加错误信息
   *
   * @param context
   * @param msg
   */
  public void addErrorMessage(final Context context, String msg) {
    messageHandler.addErrorMessage(context, msg);
  }

  protected DefaultContext createMockContext() {
    return new DefaultContext();
  }

  public AppDomainInfo getAppDomain() {
    return CheckAppDomainExistValve.getAppDomain(this);
  }

  protected RunEnvironment getCurrentRuntime() {
    return getAppDomain().getRunEnvironment();
  }

  protected ServerGroup getAppServerGroup() {
    AppDomainInfo appdomain = this.getAppDomain();
    if (StringUtils.isEmpty(appdomain.getAppName())) {
      throw new IllegalStateException("app name can not be null");
    }
    ServerGroup group = this.getServerGroupDAO().load(appdomain.getAppName(), (short) 0, appdomain.getRunEnvironment().getId());
    return group;
  }

  /**
   * 解析http post 上传输的json文本内容
   *
   * @param clazz
   * @return
   */
  protected <T> T parseJsonPost(Class<T> clazz) {
    try {
      try (ServletInputStream input = this.getRequest().getInputStream()) {
        return JSON.parseObject(IOUtils.toString(input, getEncode()), clazz, Feature.IgnoreNotMatch);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JSONObject getJSONPostContent() {
    return this.parseJsonPost();
  }

  protected JSONObject parseJsonPost() {
    try {
      return JSON.parseObject(IOUtils.toString(this.getRequest().getInputStream(), getEncode()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected JSONArray parseJsonArrayPost() {
    try {
      return JSONArray.parseArray(IOUtils.toString(this.getRequest().getInputStream(), TisUTF8.getName()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static Application getTemplateApp(BasicModule module) {
    // LuceneVersion.parse(this.getString("luceneversion"));
    LuceneVersion version = LuceneVersion.LUCENE_7;
    Application tplApp = module.getApplicationDAO().selectByName(version.getTemplateIndexName());
    if (tplApp == null) {
      throw new IllegalStateException("tpl version:" + version + ", index:" + version.getTemplateIndexName() + ", relevant app can not be null");
    }
    return tplApp;
  }

  /**
   * @return the daoContext
   */

  public RunContext getDaoContext() {
    Assert.assertNotNull("daoContextGetter can not be null", daoContextGetter);
    return daoContextGetter.get();
  }

  public boolean isAppNameAware() {
    return true;
  }

  public static class Forward {

    private String namespace;

    private final String action;
    public final String method;

    public Forward(String namespace, String action, String method) {
      super();
      if (StringUtils.isNotBlank(namespace)) {
        this.namespace = (StringUtils.startsWith(namespace, "/") ? StringUtils.EMPTY : "/") + namespace;
      }
      this.action = action;
      this.method = method;
    }

    public String getNamespace() {
      return namespace;
    }

    public String getAction() {
      return action;
    }
  }

  protected Context getContext() {
    return context;
  }

  public static class PaginationResult {

    @JSONField(serialize = false)
    private final Pager pager;

    private final List<?> rows;

    private Object[] payload = new Object[0];

    /**
     * @param pager
     * @param rows
     * @param payload 可以附带一些参数
     */
    public PaginationResult(Pager pager, List<?> rows, Object... payload) {
      super();
      this.pager = pager;
      this.rows = rows;
      this.payload = payload;
    }

    public Object[] getPayload() {
      return this.payload;
    }

    public int getTotalPage() {
      return this.pager.getTotalPage();
    }

    public int getCurPage() {
      return this.pager.getCurPage();
    }

    public int getTotalCount() {
      return this.pager.getTotalCount();
    }

    public int getPageSize() {
      return this.pager.getRowsPerPage();
    }

    public List<?> getRows() {
      return rows;
    }
  }

  @Override
  public CreateNewTaskResult createNewDataXTask(IExecChainContext chainContext, TriggerType triggerType) {
    // final TriggerType triggerType = TriggerType.parse(this.getInt(IFullBuildContext.KEY_TRIGGER_TYPE));
    Application app = null;
    ExecutePhaseRange executeRanage = chainContext.getExecutePhaseRange();
    ;
    // appname 可以为空
    // String appname = this.getString(IFullBuildContext.KEY_APP_NAME);
    // Integer workflowId = this.getInt(IFullBuildContext.KEY_WORKFLOW_ID, null, false);


    if (chainContext.hasIndexName()) {
      String appname = chainContext.getIndexName();
      app = this.getApplicationDAO().selectByName(appname);
      if (app == null) {
        throw new IllegalStateException("appname:" + appname + " relevant app pojo is not exist");
      }
    }
    Integer workflowId = chainContext.getWorkflowId();
    WorkFlowBuildHistory task = new WorkFlowBuildHistory();
    task.setCreateTime(new Date());
    task.setStartTime(new Date());
    task.setWorkFlowId(workflowId);
    task.setTriggerType(triggerType.getValue());
    task.setState((byte) ExecResult.DOING.getValue());
    // Integer buildHistoryId = null;
    // 从什么阶段开始执行
    FullbuildPhase fromPhase = executeRanage.getStart();// FullbuildPhase.parse(getInt(IParamContext.COMPONENT_START, FullbuildPhase.FullDump.getValue()));
    FullbuildPhase endPhase = executeRanage.getEnd();// FullbuildPhase.parse(getInt(IParamContext.COMPONENT_END, FullbuildPhase.IndexBackFlow.getValue()));
    if (app == null) {
      if (endPhase.bigThan(FullbuildPhase.JOIN)) {
        endPhase = FullbuildPhase.JOIN;
      }
    }
    if (fromPhase.getValue() > FullbuildPhase.FullDump.getValue()) {
      // 如果是从非第一步开始执行的话，需要客户端提供依赖的history记录id
      task.setHistoryId(this.getInt(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID));
    }
    // 说明只有workflow的流程和索引没有关系，所以不可能执行到索引build阶段去
    // task.setEndPhase((app == null) ? FullbuildPhase.JOIN.getValue() : FullbuildPhase.IndexBackFlow.getValue());
    task.setEndPhase(endPhase.getValue());
    task.setStartPhase(fromPhase.getValue());
    if (app != null) {
      task.setAppId(app.getAppId());
      task.setAppName(app.getProjectName());
    }
    // 生成一个新的taskid
    return new CreateNewTaskResult(getHistoryDAO().insertSelective(task), app);
  }

  @Override
  public final SSERunnable.SSEEventWriter getEventStreamWriter() {
    try {
      HttpServletResponse response = ServletActionContext.getResponse();
      response.setContentType("text/event-stream");
      response.setCharacterEncoding(TisUTF8.getName());
      response.setHeader("Cache-Control", "no-cache");
      response.setHeader("Connection", "keep-alive");
      response.setHeader("X-Accel-Buffering", "no"); // 禁用Nginx缓冲
      return new SSERunnable.SSEEventWriter(response.getWriter());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
