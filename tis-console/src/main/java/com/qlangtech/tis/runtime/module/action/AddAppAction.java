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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.DocCollection;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.turbine.Context;
import com.opensymphony.xwork2.ModelDriven;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.manage.common.ibatis.BooleanYorNConvertCallback;
import com.qlangtech.tis.manage.servlet.DownloadServlet;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.trigger.biz.dal.dao.ITerminatorTriggerBizDalDAOFacade;
import com.qlangtech.tis.trigger.biz.dal.dao.JobConstant;
import com.qlangtech.tis.trigger.module.action.TriggerAction;
import junit.framework.Assert;

/*
 * 添加应用
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AddAppAction extends BasicModule implements ModelDriven<Application> {

	private static final long serialVersionUID = 1L;

	private ITerminatorTriggerBizDalDAOFacade triggerContext;

	public static final int FIRST_GROUP_INDEX = 0;

	private final Application app = new Application();

	@Override
	public Application getModel() {
		return app;
	}

	@Func(PermissionConstant.APP_ADD)
	public // @FormGroup("addapp") Application app,
	void doAddApp(// Navigator nav,
			Context context) {
		Assert.assertNotNull("param app can not be null", app);
		if (!isAppNameValid(this, context, app)) {
			return;
		}
		Integer dptid = this.getInt("dptId");
		app.setDptId(dptid);
		app.setDptName(getDepartment(this, dptid).getFullName());
		app.setCreateTime(new Date());

		app.setNobleAppId(0);
		app.setNobleAppName("");
		ApplicationCriteria criteria = new ApplicationCriteria();
		criteria.createCriteria().andProjectNameEqualTo(app.getProjectName());
		if (this.getApplicationDAO().countByExample(criteria) > 0) {
			this.addErrorMessage(context, "系统中已经有同名（“" + app.getProjectName() + "”）应用存在");
			return;
		}
		createApplication(app, context, this, triggerContext);
		addActionMessage(context, "已经成功创建应用[" + app.getProjectName() + "]");
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
		DocCollection collection = this.getZkStateReader().getClusterState()
				.getCollectionOrNull(this.getAppDomain().getAppName());
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
	 * @param context
	 * @param basicModule
	 * @param triggerContexts
	 * @return
	 */
	public static Integer createApplication(Application app, Context context, BasicModule basicModule,
			ITerminatorTriggerBizDalDAOFacade triggerContext) {
		final Integer newid = basicModule.getApplicationDAO().insertSelective(app);
		// 创建默认trigger触发器
		// if (app.getIsAutoDeploy()) {
		// 创建定时任务
		int offset = (int) (Math.random() * 10);
		TriggerAction.createJob(newid, context, "0 0 " + offset + " * * ?", JobConstant.JOB_TYPE_FULL_DUMP, basicModule,
				triggerContext);
		TriggerAction.createJob(newid, context, "0 0/10 * * * ?", JobConstant.JOB_INCREASE_DUMP, basicModule,
				triggerContext);
		GroupAction.createGroup(context, RunEnvironment.getSysRuntime(), FIRST_GROUP_INDEX, newid, basicModule);
		return newid;
	}

	@Func(PermissionConstant.APP_ADD)
	public void doBatchAddApp(Context context) {
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

		public AddReplic(String appname, int groupCount, Integer replica, RunEnvironment runtime,
				List<String> servers) {
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
		final ServerGroup group = DownloadServlet.getServerGroup(fromAppId,
				(new Integer(FIRST_GROUP_INDEX)).shortValue(), this.getAppDomain().getRunEnvironment().getId(),
				getServerGroupDAO());
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

		this.addActionMessage(context,
				"拷贝源应用“" + fromApp.getProjectName() + "”已经成功复制到目标应用“" + destinationApp.getProjectName() + "”");
	}

	/**
	 * 更新应用
	 *
	 * @param form
	 * @param nav
	 * @param context
	 */
	@Func(PermissionConstant.APP_UPDATE)
	public // @FormGroup("appupdate") Application form,
	void doUpdate(// Navigator nav,
			Context context) {
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

	@Autowired
	public void setTisTriggerBizDalDaoFacade(ITerminatorTriggerBizDalDAOFacade triggerDaoContext) {
		this.triggerContext = triggerDaoContext;
	}

	private static final Pattern APPNAME_PATTERN = Pattern.compile("search4[a-zA-Z0-9_]+");

	public static void main(String[] arg) throws Exception {
		System.out.println("search4realjhsItemtest");
		Matcher m = APPNAME_PATTERN.matcher("search4realj_hsItemtest");
		System.out.println(m.matches());
	}

	public static boolean isAppNameValid(BasicModule module, Context context, Application form) {
		if (!StringUtils.startsWith(form.getProjectName(), "search4")) {
			module.addErrorMessage(context, "应用名称必须以“search4”作为前缀");
			return false;
		}
		Matcher m = APPNAME_PATTERN.matcher(form.getProjectName());
		if (!m.matches()) {
			module.addErrorMessage(context, "应用名称必用小写字母或大写字母数字组成");
			return false;
		}
		return true;
	}

	private boolean validateAppForm(Context context, Application app) {
		if (StringUtils.isBlank(app.getProjectName())) {
			this.addErrorMessage(context, "应用名称不能为空");
			return false;
		}
		if (StringUtils.isBlank(app.getRecept())) {
			this.addErrorMessage(context, "应用接口人不能为空");
			return false;
		}
		return true;
	}

	@SuppressWarnings("all")
	public void doCopyFromOtherIndex(Context context) throws Exception {
		final String colonFrom = this.getString("appname");
		if (StringUtils.startsWith(colonFrom, "search4")) {
			throw new IllegalArgumentException("colonFrom:" + colonFrom + " is not start with 'search4'");
		}
		BasicDAO<Application, ApplicationCriteria> basicDao = (BasicDAO<Application, ApplicationCriteria>) this
				.getApplicationDAO();
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
		String insertSql = "insert into application(project_name,recept,manager,create_time,update_time"
				+ ", is_auto_deploy, dpt_id,  dpt_name ) "
				+ "select  concat('search4', 'N', SUBSTRING(project_name,8)) as project_name "
				+ ",recept,manager,create_time,update_time "
				+ ",'Y' as is_auto_deploy, 8 as dpt_id, '淘宝网-产品技术部-综合业务平台-互动业务平台-终搜' as dpt_name "
				+ "from application where app_id = ?";
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
		statement = conn.prepareStatement("insert into"
				+ "snapshot(create_time,update_time,app_id,res_schema_id ,res_solr_id ,res_jar_id ,res_core_prop_id ,res_ds_id ,res_application_id ,pre_sn_id)"
				+ " select create_time,update_time,? as app_id,res_schema_id ,res_solr_id ,res_jar_id ,res_core_prop_id ,res_ds_id ,res_application_id ,pre_sn_id"
				+ "   from snapshot "
				+ " where sn_id in (select publish_snapshot_id from server_group where publish_snapshot_id is not null and app_id = ?)");
		statement.setInt(1, newAppId);
		statement.setInt(2, oldAppId);
		statement.execute();
		statement.close();
		conn.close();
	}
}
