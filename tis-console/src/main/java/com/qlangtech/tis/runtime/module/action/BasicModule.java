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
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.fullbuild.indexbuild.LuceneVersion;
import com.qlangtech.tis.manage.biz.dal.dao.*;
import com.qlangtech.tis.manage.biz.dal.pojo.*;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.apps.AppsFetcher;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.misc.DefaultMessageHandler;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.workflow.dao.IComDfireTisWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.ClusterState;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.TISZkStateReader;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年4月18日下午7:58:02
 */
public abstract class BasicModule extends ActionSupport implements RunContext, IControlMsgHandler, IPluginContext {

  public static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger("executeaction");

  // public static final int PAGE_SIZE = 30;
  private final Context context = new MockContext();

  protected static WorkFlow getAppBindedWorkFlow(BasicModule module) {
    Integer wfid = module.getAppDomain().getApp().getWorkFlowId();
    WorkFlow dataflow = module.getWorkflowDAOFacade().getWorkFlowDAO().selectByPrimaryKey(wfid);
    if (dataflow == null) {
      throw new IllegalStateException("wfid relevant dataflow can not be null");
    }
    return dataflow;
  }

  @Override
  public String execute() throws Exception {
    this.getRequest().getSession();
    CheckAppDomainExistValve.getAppDomain(this);
    // 解析这个方法 event_submit_do_buildjob_by_server
    Method executeMethod = getExecuteMethod();
    logger.info(this.getClass().getName() + ":" + executeMethod.getName());
    executeMethod.invoke(this, context);
    return getReturnCode();
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

  /**
   * description: 添加一个 数据源库 date: 2:30 PM 4/28/2017
   */
  @Override
  public final void addDb(String dbName, Context context) {
    // update db
    //String dbName = db.getDbName();
    DatasourceDb datasourceDb = new DatasourceDb();
    datasourceDb.setName(dbName);
    datasourceDb.setSyncOnline(new Byte("0"));
    datasourceDb.setCreateTime(new Date());
    datasourceDb.setOpTime(new Date());
    DatasourceDbCriteria criteria = new DatasourceDbCriteria();
    criteria.createCriteria().andNameEqualTo(dbName);
    int exist = this.getWorkflowDAOFacade().getDatasourceDbDAO().countByExample(criteria);
    if (exist > 0) {
      this.addErrorMessage(context, "已经有了同名(" + dbName + ")的数据库");
      return;
    }
    /**
     * 校验数据库连接是否正常
     */
//        if (!testDbConnection(db, action, context).valid) {
//            return;
//        }
    int dbId = this.getWorkflowDAOFacade().getDatasourceDbDAO().insertSelective(datasourceDb);
    datasourceDb.setId(dbId);
    // GitUtils.$().createDatabase(db, "add db " + db.getDbName());
    // this.addActionMessage(context, "数据库添加成功");
    this.setBizResult(context, datasourceDb);
  }


  @Override
  public boolean isCollectionAware() {
    return !(this.getAppDomain() instanceof AppDomainInfo.EnvironmentAppDomainInfo);
  }

  public String getCollectionName() {
    String collection = this.getAppDomain().getAppName();
    if (StringUtils.isBlank(collection)) {
      throw new IllegalStateException("param collection can not be null");
    }
    return collection;
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

  /**
   * @return
   */
  protected DocCollection getIndex() {
    String index = this.getAppDomain().getAppName();
    if (StringUtils.isEmpty(index)) {
      throw new IllegalStateException("index name can not be null");
    }
    ClusterState.CollectionRef ref = this.getZkStateReader().getClusterState().getCollectionRef(index);
    return ref.get();
  }

  /**
   * @throws KeeperException
   * @throws InterruptedException
   */
  protected boolean isIndexExist() throws KeeperException, InterruptedException {
    String collection = this.getAppDomain().getAppName();
    if (StringUtils.isEmpty(collection)) {
      throw new IllegalStateException("param 'collection' can not be null");
    }
    this.getApplicationDAO().updateLastProcessTime(collection);
    return this.getSolrZkClient().exists("/collections/" + collection + "/state.json", true);
  }

  public static final String key_FORWARD = "forward";

  public static ActionContext getActionContext() {
    return ActionContext.getContext();
  }

  private String getReturnCode() {
    // 并且只有screen中的 模块可以设置forward
    if (isScreenApply() && this.getRequest().getAttribute(TERMINATOR_FORWARD) != null) {
      return key_FORWARD;
    }
    final String moduleName = this.getClass().getSimpleName();
    ActionMapping mapping = ServletActionContext.getActionMapping();
    if (mapping == null) {
      return moduleName;
    }
    if ("action".equalsIgnoreCase(mapping.getExtension())) {
      return NONE;
    }
    // 当前是否是action执行
    if (isActionSubmit(mapping)) {
      // moduleName + "_action";
      return key_FORWARD;
    }
    return moduleName + (StringUtils.equals("ajax", mapping.getExtension()) ? "_ajax" : StringUtils.EMPTY);
  }

  public static boolean isScreenApply() {
    return "screen".equals(StringUtils.substringAfter(getActionProxy().getNamespace(), "#"));
  }

  public static final boolean isActionSubmit(ActionMapping mapping) {
    return "action".equals(StringUtils.substringAfter(getActionProxy().getNamespace(), "#")) && !(StringUtils.equals("ajax", mapping.getExtension()));
  }

  public static final String TERMINATOR_FORWARD = "terminatorForward";

  private static final Pattern COMPONENT_PATTERN = Pattern.compile("/[^/|^#]+");

  public static final String Layout_template = "layout_template";

  @Override
  public TisZkClient getSolrZkClient() {
    return getDaoContext().getSolrZkClient();
  }

  private static Rundata createRundata() {
    return new Rundata() {

      @Override
      public HttpServletRequest getRequest() {
        return ServletActionContext.getRequest();
      }

      public void forwardTo(String target) {
        // 设置跳转到的地方可以是 action 或者 vm
        // getRequest().setAttribute(TERMINATOR_FORWARD,
        // new Forward(null, target));
        forwardTo(null, target);
        return;
      }

      @Override
      public void forwardTo(String namespace, String target) {
        getRequest().setAttribute(TERMINATOR_FORWARD, new Forward(namespace, target));
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

  public static interface Rundata {

    public HttpServletRequest getRequest();

    public void forwardTo(String target);

    public void forwardTo(String namespace, String target);

    public void setLayout(String layout);

    public void redirectTo(String target);
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

  // private static final String DEFAULT_MEHTO = "execute";
  public static String parseMehtodName() {
    // if ("get".equalsIgnoreCase(this.getRequest().getMethod())) {
    // return DEFAULT_MEHTO;
    // }
    HttpServletRequest request = ServletActionContext.getRequest();
    // String namespace = getActionProxy().getNamespace();
    // if (StringUtils.indexOf(namespace, "#action") < 0) {
    // return ActionConfig.DEFAULT_METHOD;
    // }
    // 判断参数的emethod参数
    final String execMethod = request.getParameter("emethod");
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
    // boolean underline = false;
    //
    // StringBuffer result = new StringBuffer();
    // for (int i = 0; i < pc.length; i++) {
    // if ('_' != pc[i]) {
    // result.append(underline ? Character.toUpperCase(pc[i]) : pc[i]);
    // underline = false;
    // } else {
    // underline = true;
    // }
    // }
    // return result.toString();
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

  // public static void main(String[] args) {
  //
  // final long current = System.currentTimeMillis();
  //
  // for (int i = 0; i < 100000; i++) {
  // normalizeExecuteMethod("event_submit_do_buildjob_by_server");
  // }
  // System.out.println(System.currentTimeMillis() - current);
  // }
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

  // public IPermissionService getPermissionService() {
  // return new IPermissionService() {
  //
  // @Override
  // public boolean hasAuthority(TUser user, String funcKey) {
  //
  // return true;
  // }
  // };
  // }
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

  // /**
  // * 将对象以json的格式写到客户端
  // *
  // * @param o
  // * @throws IOException
  // */
  // protected void writeJson2Response(Object o) throws IOException {
  // getResponse().setContentType("json;charset=UTF-8");
  // JsonUtil.copy2writer(o, getResponse().getWriter());
  // }
  @Override
  public TISZkStateReader getZkStateReader() {
    return this.getDaoContext().getZkStateReader();
  }

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
    // page = (page == null) ? 1 : page;
    return page;
  }

  protected StringBuffer getPagerUrl() {
    return new StringBuffer();
  }

  // /**
  // * 重定向到某个页面 如果是action forward的话需要使用驼峰式的路径，类似：tis_app_noble_bind
  // *
  // * @param to
  // */
  // protected void forward(String to) {
  // getRundataInstance().forwardTo(to);
  // }
  // protected void forward(Class<? extends BasicScreen> to) {
  // getRundataInstance().forwardTo(TisActionMapper.addUnderline(to.getSimpleName()).toString());
  // }
  // private static final MockTurbineRunDataInternal rundata = new
  // MockTurbineRunDataInternal();
  //
  // protected final TurbineRunDataInternal getRundata() {
  // // return (TurbineRunDataInternal) TurbineUtil
  // // .getTurbineRunData(getRequest());
  // return rundata;
  // }
  // @Override
  public IGlobalAppResourceDAO getGlobalAppResourceDAO() {
    return this.getDaoContext().getGlobalAppResourceDAO();
  }

  // @Autowired
  // private FormService formService;
  // @Override
  public IServerJoinGroupDAO getServerJoinGroupDAO() {
    return this.getDaoContext().getServerJoinGroupDAO();
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

  // protected void disableDomainView(Context context) {
  // context.put("shallNotAppDomain", true);
  // }
  protected void disableNavigationBar(Context context) {
    context.put("shallNotNavigationBar", true);
  }

  // @Autowired
  // private FormService formService;
  //
  // // 取得到表单服务
  // public FormService getFormService() {
  // return formService;
  // }
  public static String getEncode() {
    return TisUTF8.getName();
  }

  // public Group getGroup(String name) {
  // return this.actionTool.getGroup(name);
  // }
  // private static final Group mockGroup = new GroupMock();
  //
  // public Group getDefaultGroup() {
  // return this.actionTool.getDefaultGroup();
  // }
  // @Autowired
  // private URIBrokerService uriService;
  //
  // public URIBrokerService getUriService() {
  // return uriService;
  // }
  // protected String getFieldValue(String field) {
  // return this.actionTool.getFieldValue(field);
  // }
  // protected void setFieldValue(String field, Object value) {
  // this.actionTool.setFieldValue(field, value);
  // }
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
    // UserUtils.getUser(this.getRequest(), this);
    return this.authtoken;
  }

  public String getUserId() {
    return UserUtils.getUser(this.getRequest(), this).getId();
  }

  // private HttpServletRequest request;
  // @Autowired
  // private HttpServletResponse response;
  protected void setErrorMsgInvisiable(Context context) {
    context.put("errorMsgInvisiable", true);
  }

  /**
   * 取得业务线集合
   *
   * @return
   */
  public List<Option> getBizLineList() {
    // BizDomainCriteria query = new BizDomainCriteria();
    // query.createCriteria().andNotDelete();
    // List<BizDomain> result =
    // this.getBizDomainDAO().selectByExample(query);
    List<Option> answer = new ArrayList<Option>();
    // this.getRequest(), false, this.getUser(), this
    for (Department domain : getAppsFetcher(this.getRequest(), this.isMaxMatch(), this.getUser(), this).getDepartmentBelongs(this)) {
      // answer.add(new Option(domain.getFullName(), String.valueOf(domain
      // .getDptId())));
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
    // AppsFetcher fetcher = this.getAppsFetcher();
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

  // public Integer getInt(Context context, String key) {
  // return actionTool.getInt( key);
  //
  // }
  // public String getString(Context context, String key) {
  // // GroupInstanceHelper group = getGroupHelper(context);
  // return this.getString(context, key);
  // }
  // /**
  // * @param context
  // * @return
  // */
  // protected GroupInstanceHelper getGroupHelper(Context context) {
  // FormTool formTool = (FormTool) context.get("form");
  // GroupInstanceHelper group = formTool.get(this.getGroupName())
  // .getDefaultInstance();
  // return group;
  // }
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
      if (ignoreError) {
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

  protected final String[] getStringArray(String key) {
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

  public void setServerPoolDAO(IServerPoolDAO serverPoolDAO) {
    this.serverPoolDAO = serverPoolDAO;
  }

  @Autowired
  private IServerPoolDAO serverPoolDAO;

  @Autowired
  private IZookeeperServerDAO zookeeperServerDAO;

  private RunContextGetter daoContextGetter;

  // private AdminUserService authService;
  // @Autowired
  // private AuthProvider authProvider;
  //
  // public AuthProvider getAuthProvider() {
  // return authProvider;
  // }
  //
  // public void setAuthProvider(AuthProvider authProvider) {
  // this.authProvider = authProvider;
  // }
  // @Autowired
  // public void setAuthService(AdminUserService authService) {
  // // this.authService = new DelegateAdminUserService(authService, this
  // // .getRequest());// ;
  // this.authService = authService;
  // }
  @Autowired
  public final void setRunContextGetter(RunContextGetter daoContextGetter) {
    this.daoContextGetter = daoContextGetter;
  }

  protected IZookeeperServerDAO getZookeeperServerDAO() {
    return this.zookeeperServerDAO;
  }

  // // @Override
  // public DelegateAdminUserService getAuthService() {
  // return (DelegateAdminUserService) this.getDaoContext().getAuthService();
  // }
  @Override
  public IResourceParametersDAO getResourceParametersDAO() {
    return getDaoContext().getResourceParametersDAO();
  }

  protected IServerPoolDAO getServerPoolDAO() {
    return serverPoolDAO;
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

  public IAppPackageDAO getAppPackageDAO() {
    return getDaoContext().getAppPackageDAO();
  }

  // public IBizDomainDAO getBizDomainDAO() {
  // return getDaoContext().getBizDomainDAO();
  // }
  public IGroupInfoDAO getGroupInfoDAO() {
    return getDaoContext().getGroupInfoDAO();
  }

  // private IServerDAO getServerDAO() {
  // return getDaoContext().getServerDAO();
  // }
  // @Override
  public IDepartmentDAO getDepartmentDAO() {
    return getDaoContext().getDepartmentDAO();
  }

  // @Override
  public IUsrDptRelationDAO getUsrDptRelationDAO() {
    return getDaoContext().getUsrDptRelationDAO();
  }

  // @Override
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

  public IRdsDbDAO getRdsDbDAO() {
    return getDaoContext().getRdsDbDAO();
  }

  public IRdsTableDAO getRdsTableDAO() {
    return getDaoContext().getRdsTableDAO();
  }

  public IApplicationExtendDAO getApplicationExtendDAO() {
    return getDaoContext().getApplicationExtendDAO();
  }

  @Override
  public IUsrApplyDptRecordDAO getUsrApplyDptRecordDAO() {
    return getDaoContext().getUsrApplyDptRecordDAO();
  }

  @Override
  public IUsrDptExtraRelationDAO getUsrDptExtraRelationDAO() {
    return getDaoContext().getUsrDptExtraRelationDAO();
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

  @Override
  public IComDfireTisWorkflowDAOFacade getWorkflowDAOFacade() {
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
  public void setBizResult(Context context, Object result) {
    messageHandler.setBizResult(context, result);
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

  protected Application getTemplateApp() {
    // LuceneVersion.parse(this.getString("luceneversion"));
    LuceneVersion version = LuceneVersion.LUCENE_7;
    Application tplApp = this.getApplicationDAO().selectByName(version.getTemplateIndexName());
    if (tplApp == null) {
      throw new IllegalStateException("tpl version:" + version + ", index:" + version.getTemplateIndexName() + ", relevant app can not be null");
    }
    return tplApp;
  }

  /**
   * @return the daoContext
   */
  protected RunContext getDaoContext() {
    Assert.assertNotNull("daoContextGetter can not be null", daoContextGetter);
    return daoContextGetter.get();
  }

  public boolean isAppNameAware() {
    return true;
  }

  public static class Forward {

    private String namespace;

    private final String action;

    public Forward(String namespace, String action) {
      super();
      if (StringUtils.isNotBlank(namespace)) {
        this.namespace = (StringUtils.startsWith(namespace, "/") ? StringUtils.EMPTY : "/") + namespace;
      }
      this.action = action;
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
  // private void setResponse(HttpServletResponse response) {
  // this.response = response;
  // }
}
