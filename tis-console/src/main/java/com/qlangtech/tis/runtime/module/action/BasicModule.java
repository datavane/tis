/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.runtime.module.action;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.TISZkStateReader;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.TisZkClient;
import com.koubei.web.tag.pager.LinkBuilder;
import com.koubei.web.tag.pager.Pager;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;
import com.qlangtech.tis.manage.biz.dal.dao.IAppPackageDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IAppTriggerJobRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationExtendDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IBizFuncAuthorityDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncRoleRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IGlobalAppResourceDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IGroupInfoDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRdsDbDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRdsTableDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRoleDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerGroupDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerJoinGroupDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerPoolDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotViewDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUploadResourceDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrApplyDptRecordDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptExtraRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IZookeeperServerDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.CheckAppDomainExistValve;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.manage.common.Module;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.RunContextGetter;
import com.qlangtech.tis.manage.common.TisActionMapper;
import com.qlangtech.tis.manage.common.UserUtils;
import com.qlangtech.tis.manage.common.apps.AppsFetcher;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.pubhook.common.JsonUtil;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.misc.DefaultMessageHandler;
import com.qlangtech.tis.runtime.module.misc.MessageHandler;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;
import junit.framework.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicModule extends ActionSupport implements RunContext, MessageHandler {

	public static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger("executeaction");

	private final Context context = new MockContext();

	@Override
	public String execute() throws Exception {
		this.getRequest().getSession();
		CheckAppDomainExistValve.getAppDomain(this);
		// 解析这个方法 event_submit_do_buildjob_by_server
		Method executeMethod = getExecuteMethod();
		logger.info(this.getClass().getName());
		executeMethod.invoke(this, context);
		return getReturnCode();
	}

	protected void setBizObjResult(Context context, Object result) {
		String json = JSON.toJSONString(result, true);
		context.put(AjaxValve.QUERY_RESULT, json);
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

	protected DocCollection getDocCollection(String collectionName) {
		return this.getZkStateReader().getClusterState().getCollection(collectionName);
	}

	/**
	 * @return
	 */
	protected DocCollection getIndex() {
		return this.getZkStateReader().getClusterState().getCollection(this.getAppDomain().getAppName());
	}

	/**
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	protected boolean isIndexExist() throws KeeperException, InterruptedException {
		return this.getSolrZkClient().exists("/collections/" + this.getAppDomain().getAppName() + "/state.json", true);
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
		return "action".equals(StringUtils.substringAfter(getActionProxy().getNamespace(), "#"))
				&& !(StringUtils.equals("ajax", mapping.getExtension()));
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
				// /runtime/templates/layout/ext_tpl.vm
				// StringUtils.split();
				//
				final String namespace = getActionContext().getActionInvocation().getProxy().getNamespace();
				Matcher matcher = COMPONENT_PATTERN.matcher(namespace);
				if (matcher.find()) {
					getRequest().setAttribute("layout_template",
							matcher.group() + "/templates/layout/" + layout + ".vm");
				} else {
					throw new IllegalArgumentException(
							"namespace:" + namespace + " can not match pattern " + COMPONENT_PATTERN);
				}
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

	private static final Module enginePlugins = new Module("/engineplugins");

	public Module getMvnPlugins() {
		return enginePlugins;
	}

	public Module getCoredefine() {
		return coredefineModule;
	}

	public Module getManageModule() {
		return manageModule;
	}

	public Module getTrigger() {
		return trigger;
	}

	private static final String DEFAULT_MEHTO = "execute";

	private String parseMehtodName() {
		// if ("get".equalsIgnoreCase(this.getRequest().getMethod())) {
		// return DEFAULT_MEHTO;
		// }
		String namespace = getActionProxy().getNamespace();
		if (StringUtils.indexOf(namespace, "#action") < 0) {
			return DEFAULT_MEHTO;
		}
		Enumeration<?> params = this.getRequest().getParameterNames();
		String param = null;
		while (params.hasMoreElements()) {
			if (StringUtils.startsWith(param = String.valueOf(params.nextElement()), "event_submit_")) {
				return normalizeExecuteMethod(param);
			}
		}
		return DEFAULT_MEHTO;
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

	public static IAppsFetcher getAppsFetcher(HttpServletRequest request, boolean maxMatch, IUser user,
			RunContext context) {
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

	/**
	 * 将对象以json的格式写到客户端
	 *
	 * @param o
	 * @throws IOException
	 */
	protected void writeJson2Response(Object o) throws IOException {
		getResponse().setContentType("json;charset=UTF-8");
		JsonUtil.copy2writer(o, getResponse().getWriter());
	}

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

	protected static final int PAGE_SIZE = 30;

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
		Integer page = this.getInt("page");
		page = (page == null) ? 1 : page;
		return page;
	}

	protected StringBuffer getPagerUrl() {
		return new StringBuffer();
	}

	/**
	 * 重定向到某个页面 如果是action forward的话需要使用驼峰式的路径，类似：tis_app_noble_bind
	 *
	 * @param to
	 */
	protected void forward(String to) {
		getRundataInstance().forwardTo(to);
	}

	protected void forward(Class<? extends BasicScreen> to) {
		getRundataInstance().forwardTo(TisActionMapper.addUnderline(to.getSimpleName()).toString());
	}

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
		return "utf8";
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
		// UserUtils.getUser(this.getRequest(),
		return this.authtoken.getName();
		// this).getName();
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
		for (Department domain : getAppsFetcher(this.getRequest(), this.isMaxMatch(), this.getUser(), this)
				.getDepartmentBelongs(this)) {
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

	/**
	 * @param bizid
	 * @param context
	 * @return
	 */
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
		try {
			return Integer.parseInt(getRequest().getParameter(key));
		} catch (Throwable e) {
			return null;
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
		try {
			return Long.parseLong(getRequest().getParameter(key));
		} catch (Throwable e) {
			return null;
		}
	}

	protected final String getString(String key) {
		Assert.assertNotNull("the request can not be null", getRequest());
		return getRequest().getParameter(key);
	}

	protected final boolean getBoolean(String key) {
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

	// // dao 层代码
	// @Autowired
	// private IApplicationDAO applicationDAO;
	// @Autowired
	// private IAppPackageDAO appPackageDAO;
	// @Autowired
	// private IBizDomainDAO bizDomainDAO;
	// @Autowired
	// private IGroupInfoDAO groupInfoDAO;
	// @Autowired
	// private IServerDAO serverDAO;
	// @Autowired
	// private IServerGroupDAO serverGroupDAO;
	// @Autowired
	// private ISnapshotDAO snapshotDAO;
	//
	// @Autowired
	// private ISnapshotViewDAO snapshotViewDAO;
	//
	// public ISnapshotViewDAO getSnapshotViewDAO() {
	// return snapshotViewDAO;
	// }
	//
	// public void setSnapshotViewDAO(ISnapshotViewDAO snapshotViewDAO) {
	// this.snapshotViewDAO = snapshotViewDAO;
	// }
	//
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

	// // 聚石塔相关
	// public IIsvDAO getIsvDAO() {
	// return getDaoContext().getIsvDAO();
	// }
	public IRdsDbDAO getRdsDbDAO() {
		return getDaoContext().getRdsDbDAO();
	}

	public IRdsTableDAO getRdsTableDAO() {
		return getDaoContext().getRdsTableDAO();
	}

	public IApplicationExtendDAO getApplicationExtendDAO() {
		return getDaoContext().getApplicationExtendDAO();
	}

	// dao 层代码 结束
	// private List<String> actionMessage = new ArrayList<String>();
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

	private final DefaultMessageHandler messageHandler = new DefaultMessageHandler();

	public void addActionMessage(final Context context, String msg) {
		messageHandler.addActionMessage(context, msg);
	}

	@Override
	public void setBizResult(Context context, Object result) {
		messageHandler.setBizResult(context, result);
	}

	protected boolean hasErrors(Context context) {
		return messageHandler.hasErrors(context);
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

	protected EContext createMockContext() {
		return new EContext();
	}

	public static class EContext implements Context {

		final Map<String, Object> contextMap = new HashMap<String, Object>();

		public Map<String, Object> getContextMap() {
			return contextMap;
		}

		public EContext() {
		}

		public Map<String, Object> getContextValue() {
			return this.contextMap;
		}

		@Override
		public boolean containsKey(String key) {
			return contextMap.containsKey(key);
		}

		@Override
		public Object get(String key) {
			return contextMap.get(key);
		}

		@Override
		public Set<String> keySet() {
			return contextMap.keySet();
		}

		@Override
		public void put(String key, Object value) {
			contextMap.put(key, value);
		}

		@Override
		public void remove(String key) {
		}
	}

	public AppDomainInfo getAppDomain() {
		return CheckAppDomainExistValve.getAppDomain(this);
	}

	protected RunEnvironment getCurrentRuntime() {
		return getAppDomain().getRunEnvironment();
	}

	protected ServerGroup getAppServerGroup() {
		AppDomainInfo appdomain = this.getAppDomain();
		ServerGroup group = this.getServerGroupDAO().load(getCollectionName(), (short) 0,
				appdomain.getRunEnvironment().getId());
		return group;
	}

	public String getCollectionName() {
		return this.getAppDomain().getAppName();
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
}
