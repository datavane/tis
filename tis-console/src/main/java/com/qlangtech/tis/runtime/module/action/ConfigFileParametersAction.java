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
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.coredefine.module.action.CoreAction;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParametersCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.UploadJarAction.ConfigContentGetter;
import com.qlangtech.tis.solrj.util.ZkUtils;
import com.qlangtech.tis.trigger.biz.dal.dao.ITriggerBizDalDAOFacade;

import junit.framework.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ConfigFileParametersAction extends BasicModule {

	private static final long serialVersionUID = 1L;

	private IResourceParametersDAO resourceParametersDAO;
	private static final int DEPARTMENT_DEFAULT_ID = 2;
	private static final int DEPARTMENT_ROOT_ID = 1;
	private static final int APPLICATION_DEFAULT_ID = 1;

	public static final String APP_NAME_TEMPLATE = "search4template";

	private static final Pattern PATTERN_KEY_NAME = Pattern.compile("[\\w|_]+");

	private static final String IP_PATTERN = "((25[0-5]|2[0-4]\\d|[1]{1}\\d{1}\\d{1}|[1-9]{1}\\d{1}|\\d{1})($|(?!\\.$)\\.)){4}";

	private static final Pattern PATTERN_IP = Pattern.compile("^" + IP_PATTERN + "$");

	private static final Pattern PATTERN_ZK_ADDRESS = Pattern.compile("([^/]+)(/.+)$");
	// private static final Pattern PATTERN_IP_WITH_PORT = Pattern.compile("^" +
	// IP_PATTERN + "\\:" + "$");

	public static void main(String[] args) {

		Matcher matcher = PATTERN_ZK_ADDRESS.matcher("10.1.21.202:2181,10.1.21.201:2181,10.1.21.200:2181/tis/7_6cloud");

		if (matcher.matches()) {
			System.out.println("match");
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
		} else {
			System.out.println("not match");
		}

	}

	private ITriggerBizDalDAOFacade triggerContext;

	public static GlobalParam[] globalParams = new GlobalParam[] { //
			new GlobalParam(TSearcherConfigFetcher.CONFIG_ZKADDRESS, "Solr集群zookeeper地址" //
					, new ParamValiate() {
						@Override
						public boolean validate(Context ctx, BasicModule module, Option p) {

							if (!super.validate(ctx, module, p)) {
								return false;
							}
							Matcher matcher = PATTERN_ZK_ADDRESS.matcher(p.getValue());
							if (!matcher.matches()) {
								module.addErrorMessage(ctx, "ZK地址不符合规范:" + PATTERN_ZK_ADDRESS);
								return false;
							}

							final String zkServer = matcher.group(1);
							String zkSubDir = StringUtils.trimToEmpty(matcher.group(2));
							if (StringUtils.endsWith(zkSubDir, "/")) {
								zkSubDir = StringUtils.substring(zkSubDir, 0, zkSubDir.length() - 1);
								p.setValue(StringUtils.substring(p.getValue(), 0, p.getValue().length() - 1));
							}

							ZooKeeper zk = null;
							try {
								zk = new ZooKeeper(zkServer, 50000, null);
								zk.getChildren("/", false);

								ZkUtils.guaranteeExist(zk, zkSubDir + "/tis");
								ZkUtils.guaranteeExist(zk, zkSubDir + "/tis-lock/dumpindex");
								ZkUtils.guaranteeExist(zk, zkSubDir + "/configs/" + CoreAction.DEFAULT_SOLR_CONFIG);

							} catch (Throwable e) {
								module.addErrorMessage(ctx, p.getName() + "填写的地址不能连接Zookeeper主机");
								return false;
							} finally {
								try {
									zk.close();
								} catch (Throwable e) {

								}
							}

							return true;
						}
					}, new ParamProcess(), true /* userinput */), //
			new GlobalParam(TSearcherConfigFetcher.CONFIG_HDFS_ADDRESS, "全量构建分布式文件系统地址" //
					, new ParamValiate() {
						@Override
						public boolean validate(Context ctx, BasicModule module, Option p) {
							if (!super.validate(ctx, module, p)) {
								return false;
							}
							String prefix = "hdfs://";
							if (!StringUtils.startsWith(p.getValue(), prefix)) {
								module.addErrorMessage(ctx, p.getName() + "应以" + prefix + "作为前缀");
								return false;
							}
							return true;
						}
					}, new ParamProcess() {
						@Override
						void process(ConfigFileParametersAction m, GlobalParam p, Option option) {
							this.insertDefault(m, p.getName(), "hdfs://cluster-cdh", p.getDesc());
						}
					}), //
			new GlobalParam(TSearcherConfigFetcher.CONFIG_TIS_HDFS_ROOT_DIR, "分布式文件系统根路径" //
					, new ParamValiate() {
						@Override
						public boolean validate(Context ctx, BasicModule module, Option p) {
							if (!super.validate(ctx, module, p)) {
								return false;
							}
							String prefix = "/";
							if (!StringUtils.startsWith(p.getValue(), prefix)) {
								module.addErrorMessage(ctx, p.getName() + "应以" + prefix + "作为前缀");
								return false;
							}
							return true;
						}
					}, new ParamProcess()), //
			new GlobalParam(TSearcherConfigFetcher.CONFIG_terminator_host_address, "TIS中控节点Host地址" //
					, new ParamValiate() {
						@Override
						public boolean validate(Context ctx, BasicModule module, Option p) {
							if (!super.validate(ctx, module, p)) {
								return false;
							}
							if (!validateIP(ctx, module, p)) {
								return false;
							}
							return true;
						}
					}, new ParamProcess() {
						@Override
						public void process(ConfigFileParametersAction m, GlobalParam p, Option option) {
							this.insertDefault(m, p.getName(), Config.getTisRepository(), p.getDesc());
						}
					}), //
			new GlobalParam(TSearcherConfigFetcher.TIS_ASSEMBLE_HOST, "TIS全量控制、日志收集节点" //
					, new ParamValiate() {
						@Override
						public boolean validate(Context ctx, BasicModule module, Option p) {
							if (!super.validate(ctx, module, p)) {
								return false;
							}
							if (!validateIP(ctx, module, p)) {
								return false;
							}
							return true;
						}
					}, new ParamProcess() {
						@Override
						public void process(ConfigFileParametersAction m, GlobalParam p, Option option) {
							this.insertDefault(m //
							, p.getName() //
							, "http://" + Config.getAssembleHostAddress() + ":8080" //
							, p.getDesc());

							this.insertDefault(m //
							, TSearcherConfigFetcher.LOG_SOURCE_ADDRESS //
							, Config.getAssembleHostAddress() //
							, "结构化日志收集地址");

							this.insertDefault(m //
							, TSearcherConfigFetcher.CONFIG_LOG_FLUME_AGENT_ADDRESS //
							, Config.getAssembleHostAddress() + ":41414" //
							, "全量、增量flume日志收集地址");
						}
					}), //
//			new GlobalParam(TSearcherConfigFetcher.jobtracker_rpcserver, "TIS任务中心入口地址" //
//					, new ParamValiate() {
//						@Override
//						public boolean validate(Context ctx, BasicModule module, Option p) {
//							if (!super.validate(ctx, module, p)) {
//								return false;
//							}
//							if (!validateIP(ctx, module, p)) {
//								return false;
//							}
//							return true;
//						}
//					}, new ParamProcess() {
//						@Override
//						public void process(ConfigFileParametersAction m, GlobalParam p, Option option) {
//
//							// this.insertDefault(m, p.getName() //
//							// , Config.getYarnResourceManagerHost() + ":8848"
//							// //
//							// , p.getDesc());
//							//
//							// this.insertDefault(m //
//							// , TSearcherConfigFetcher.jobtracker_transserver
//							// //
//							// , Config.getYarnResourceManagerHost() + ":8849"
//							// //
//							// , p.getDesc());
//							//
//							// this.insertDefault(m //
//							// , TSearcherConfigFetcher.INDEX_BUILD_CENTER_HOST
//							// //
//							// , "http://" + Config.getYarnResourceManagerHost()
//							// + ":9999/jobtracker.jsp" //
//							// , p.getDesc() + " URL");
//
//						}
//					}) //
//			, 
			new GlobalParam(TSearcherConfigFetcher.HIVE_HOST, "TIS 全量构建HIVE入口地址,格式:'10.1.127.105:10000'" //
					, new ParamValiate() {
						@Override
						public boolean validate(Context ctx, BasicModule module, Option p) {
							if (!super.validate(ctx, module, p)) {
								return false;
							}
							return true;
						}
					}, new ParamProcess() {
						@Override
						public void process(ConfigFileParametersAction m, GlobalParam p, Option option) {
							this.insertDefault(m, p.getName() //
							, option.getValue() //
							, "TIS 全量构建HIVE入口地址");
						}
					}, true /* user input */) //

			// , new GlobalParam("mq_statistics_host", "TIS实时日志状态收集节点地址" //
			// , new ParamValiate() {
			// @Override
			// public boolean validate(Context ctx, BasicModule module,
			// GlobalParam p) {
			// if (!super.validate(ctx, module, p)) {
			// return false;
			// }
			// if (!validateIP(ctx, module, p)) {
			// return false;
			// }
			// return true;
			// }
			// }, new ParamProcess() {
			// @Override
			// public void process(ConfigFileParametersAction m, GlobalParam p)
			// {
			//
			// }
			// })
			// , new GlobalParam("max_db_dump_thread_count", "数据库Dump最大线程数" //
			// , new ParamValiate() {
			// @Override
			// public boolean validate(Context ctx, BasicModule module,
			// GlobalParam p) {
			//
			// return true;
			// }
			// }, new ParamProcess() {
			// @Override
			// public boolean process(GlobalParam p) {
			// return false;
			// }
			// })
	};

	/**
	 * 初始化系统参数
	 * 
	 * @param context
	 */
	public void doInitParameter(Context context) throws Exception {

		boolean hasError = false;
		Map<String, Option> paramMap = new HashMap<>();
		for (GlobalParam p : globalParams) {
			Option pp = new Option(p.getName(), this.getString(p.getName()));
			paramMap.put(p.getName(), pp);
			if (p.isUserInput() && !p.validate(context, this, pp)) {
				hasError = true;
			}
		}

		context.put("options", paramMap);

		if (hasError) {
			return;
		}

		// 添加一个系统管理员
		this.getUsrDptRelationDAO().addAdminUser();

		this.initializeDepartment();

		this.getApplicationDAO().deleteByPrimaryKey(APPLICATION_DEFAULT_ID);
		SnapshotCriteria snapshotQuery = new SnapshotCriteria();
		snapshotQuery.createCriteria().andAppidEqualTo(APPLICATION_DEFAULT_ID);
		this.getSnapshotDAO().deleteByExample(snapshotQuery);

		ServerGroupCriteria serverGroupQuery = new ServerGroupCriteria();
		serverGroupQuery.createCriteria().andAppIdEqualTo(APPLICATION_DEFAULT_ID);
		this.getServerGroupDAO().deleteByExample(serverGroupQuery);

		// 添加初始化模板配置
		Application app = new Application();
		app.setAppId(APPLICATION_DEFAULT_ID);
		app.setProjectName(APP_NAME_TEMPLATE);
		app.setDptId(DEPARTMENT_DEFAULT_ID);
		app.setDptName("default");
		app.setIsDeleted("N");
		app.setManager("admin");
		app.setUpdateTime(new Date());
		app.setCreateTime(new Date());
		app.setRecept("admin");

		// final Integer newid =
		this.getApplicationDAO().insertSelective(app);

		// int newAppid = AddAppAction.createApplication(app, context, this,
		// this.triggerContext);
		app.setAppId(APPLICATION_DEFAULT_ID);
		this.initializeSchemaConfig(context, app);

		ResourceParametersCriteria pquery = new ResourceParametersCriteria();
		pquery.createCriteria().andKeyNameIsNotNull();
		this.getResourceParametersDAO().deleteByExample(pquery);
		for (GlobalParam p : globalParams) {
			p.process(this, p, paramMap.get(p.getName()));
		}

		this.addActionMessage(context, "初始化系统参数完成");
	}

	void initializeSchemaConfig(Context context, Application app) throws IOException {
		Snapshot snap = new Snapshot();
		snap.setCreateTime(new Date());
		snap.setCreateUserId(9999l);
		snap.setCreateUserName("admin");
		snap.setUpdateTime(new Date());

		snap.setAppId(app.getAppId());
		try (InputStream schemainput = this.getClass().getResourceAsStream("/solrtpl/schema.xml.tpl")) {
			ConfigContentGetter schema = new ConfigContentGetter(ConfigFileReader.FILE_SCHEMA,
					IOUtils.toString(schemainput, getEncode()));
			snap = UploadJarAction.processFormItem(this.getDaoContext(), schema, snap);
		}
		try (InputStream solrconfigInput = this.getClass().getResourceAsStream("/solrtpl/solrconfig.xml.tpl")) {
			ConfigContentGetter solrConfig = new ConfigContentGetter(ConfigFileReader.FILE_SOLOR,
					IOUtils.toString(solrconfigInput, getEncode()));
			snap = UploadJarAction.processFormItem(this.getDaoContext(), solrConfig, snap);
		}
		snap.setPreSnId(-1);
		Integer snapshotId = this.getSnapshotDAO().insertSelective(snap);

		GroupAction.createGroup(context, RunEnvironment.getSysRuntime(), AddAppAction.FIRST_GROUP_INDEX, app.getAppId(),
				snapshotId, this);
	}

	void initializeDepartment() {

		this.getDepartmentDAO().deleteByPrimaryKey(DEPARTMENT_DEFAULT_ID);
		this.getDepartmentDAO().deleteByPrimaryKey(DEPARTMENT_ROOT_ID);

		// 初始化部门
		Department dpt = new Department();
		dpt.setDptId(1);
		dpt.setLeaf(false);
		dpt.setGmtCreate(new Date());
		dpt.setGmtModified(new Date());
		dpt.setName("tis");
		dpt.setFullName("/tis");
		dpt.setParentId(-1);
		this.getDepartmentDAO().insertSelective(dpt);

		dpt = new Department();
		dpt.setDptId(DEPARTMENT_DEFAULT_ID);
		dpt.setLeaf(true);
		dpt.setGmtCreate(new Date());
		dpt.setGmtModified(new Date());
		dpt.setName("default");
		dpt.setFullName("/tis/default");
		dpt.setParentId(1);
		this.getDepartmentDAO().insertSelective(dpt);
	}

	public static class GlobalParam // extends Option
	{

		private final String desc;

		private ParamValiate validator;
		private ParamProcess process;

		private String name;
		private final boolean userInput;

		public GlobalParam(String name, String desc, ParamValiate validator, ParamProcess process, boolean userInput) {
			// super(name, null);
			this.name = name;
			this.desc = desc;
			this.validator = validator;
			this.process = process;
			this.userInput = userInput;
		}

		public GlobalParam(String name, String desc, ParamValiate validator, ParamProcess process) {
			this(name, desc, validator, process, false /* userInput */);
		}

		public boolean validate(Context ctx, BasicModule module, Option p) {
			return validator.validate(ctx, module, p);
		}

		public boolean isUserInput() {
			return this.userInput;
		}

		public String getName() {
			return this.name;
		}

		public void process(ConfigFileParametersAction m, GlobalParam p, Option option) {
			this.process.process(m, p, option);
		}

		public String getDesc() {
			return desc;
		}
	}

	static class ParamValiate {
		boolean validate(Context ctx, BasicModule module, Option p) {
			if (StringUtils.isEmpty(p.getValue())) {
				module.addErrorMessage(ctx, "请填写'" + p.getName() + "'");
				return false;
			}
			return true;
		}

		protected boolean validateIP(Context ctx, BasicModule module, Option p) {
			Matcher m = PATTERN_IP.matcher(p.getValue());
			if (!m.matches()) {
				module.addErrorMessage(ctx, p.getName() + "不符合IP规范");
				return false;
			}
			return true;
		}
	}

	static class ParamProcess {
		void process(ConfigFileParametersAction m, GlobalParam p, Option option) {
			this.insertDefault(m, p.getName(), option.getValue(), p.getDesc());
		}

		protected void insertDefault(ConfigFileParametersAction m, String keyname, String val, String desc) {
			ResourceParameters param = new ResourceParameters();
			param.setGmtCreate(new Date());
			param.setGmtUpdate(new Date());
			param.setDailyValue(val);
			// param.setReadyValue();
			param.setOnlineValue(val);
			param.setKeyName(keyname);
			param.setDesc(desc);
			m.getParametersDAO().insertSelective(param);
		}
	}

	/**
	 * 设置参数值
	 *
	 * @param context
	 */
	@Func(PermissionConstant.GLOBAL_PARAMETER_SET)
	public void doSetParameter(Context context) {
		// RunEnvironment runtime =
		// RunEnvironment.getEnum(this.getString("runtime"));
		Long rpid = this.getLong("rpid");
		Assert.assertNotNull(rpid);
		// Assert.assertNotNull(runtime);
		ResourceParameters param = new ResourceParameters();
		ResourceParametersCriteria criteria = new ResourceParametersCriteria();
		criteria.createCriteria().andRpIdEqualTo(rpid);
		// Assert.assertNotNull(runtime);
		String keyvalue = this.getString("keyvalue");
		String keyDesc = this.getString("desc");

		param.setDailyValue(keyvalue);
		param.setOnlineValue(keyvalue);
		param.setDesc(keyDesc);
		context.put("biz", param);

		if (!validateKeyDesc(context, keyDesc)) {
			return;
		}

		if (StringUtils.isBlank(keyvalue)) {
			this.addErrorMessage(context, "键值不能为空");
			return;
		}

		this.resourceParametersDAO.updateByExampleSelective(param, criteria);
		this.addActionMessage(context, "已经成功更新");
	}

	boolean validateKeyDesc(Context context, String keyDesc) {
		if (StringUtils.length(StringUtils.trimToEmpty(keyDesc)) < 4) {
			this.addErrorMessage(context, "'描述'内容不能少于四个字符");
			return false;
		}
		return true;
	}

	@Func(PermissionConstant.GLOBAL_PARAMETER_ADD)
	public void doAddParameter(Context context) {
		ResourceParameters param = new ResourceParameters();
		param.setKeyName(this.getString("keyname"));
		param.setDesc(this.getString("desc"));
		param.setOnlineValue(this.getString("keyvalue"));
		param.setDailyValue(this.getString("keyvalue"));
		// Option option = new Option(this.getString("keyname"),
		// this.getString("keyvalue"));
		context.put("biz", param);

		if (StringUtils.isBlank(param.getKeyName())) {
			this.addErrorMessage(context, "键名称不能为空");
			return;
		}

		if (!validateKeyDesc(context, param.getDesc())) {
			return;
		}

		// String keyName = this.getString("keyname");
		if (StringUtils.isBlank(param.getKeyName())) {
			this.addErrorMessage(context, "键名称不能为空");
			return;
		}
		if (!PATTERN_KEY_NAME.matcher(param.getKeyName()).matches()) {
			this.addErrorMessage(context, "键名键值必须由字母和数字和下划线组成");
			return;
		}
		ResourceParametersCriteria criteria = new ResourceParametersCriteria();
		criteria.createCriteria().andKeyNameEqualTo(param.getKeyName());
		if (resourceParametersDAO.countByExample(criteria) > 0) {
			this.addErrorMessage(context, "该键值名" + param.getKeyName() + "系统中已经存在");
			return;
		}

		final String keyValue = StringUtils.trimToNull(param.getKeyName());
		// ResourceParameters param = new ResourceParameters();
		param.setGmtCreate(new Date());
		param.setGmtUpdate(new Date());
		param.setDailyValue(keyValue);
		param.setReadyValue(keyValue);
		param.setOnlineValue(keyValue);
		// param.setKeyName(option.getName());
		resourceParametersDAO.insertSelective(param);
		this.addActionMessage(context, "成功添加配置全局变量：" + param.getKeyName());
	}

	public IResourceParametersDAO getParametersDAO() {
		return this.resourceParametersDAO;
	}

	@Autowired
	public void setResourceParametersDAO(IResourceParametersDAO resourceParametersDAO) {
		this.resourceParametersDAO = resourceParametersDAO;
	}

	@Autowired
	public void setTisTriggerBizDalDaoFacade(ITriggerBizDalDAOFacade triggerDaoContext) {
		this.triggerContext = triggerDaoContext;
	}
}
