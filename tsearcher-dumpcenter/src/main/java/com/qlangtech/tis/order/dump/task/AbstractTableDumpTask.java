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
package com.qlangtech.tis.order.dump.task;

import java.io.Reader;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.build.task.TaskReturn;
import com.qlangtech.tis.build.task.TaskReturn.ReturnCode;
import com.qlangtech.tis.common.config.IServiceConfig;
import com.qlangtech.tis.dump.hive.HiveDBUtils;
import com.qlangtech.tis.dump.hive.HiveRemoveHistoryDataTask;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.hdfs.client.bean.BasicTerminatorClient;
import com.qlangtech.tis.hdfs.client.bean.BasicTerminatorClient.TriggerParamProcess;
import com.qlangtech.tis.hdfs.client.bean.HdfsRealTimeTerminatorBean;
import com.qlangtech.tis.hdfs.client.bean.MockWorkflowFeedback;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherDumpContextImpl;
import com.qlangtech.tis.order.dump.task.DataSourceRegister.DBRegister;
import com.qlangtech.tis.trigger.socket.IWorkflowFeedback;
import com.qlangtech.tis.trigger.util.TriggerParam;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractTableDumpTask implements TaskMapper {

	private static final Logger logger = LoggerFactory.getLogger(AbstractTableDumpTask.class);

	private static final Pattern DB_HOST_ENUM = Pattern.compile("db\\.(.+?)\\.enum");

	public ClassPathXmlApplicationContext springContext;

	public AbstractTableDumpTask() {
		super();
	}

	protected abstract Reader getDataSourceConfigStream();

	protected final List<DBLinkMetaData> parseDbLinkMetaData(TaskContext context) {
		final List<DBLinkMetaData> dbMetaList = new ArrayList<DBLinkMetaData>();
		Reader input = null;
		try {
			input = getDataSourceConfigStream();
			if (input == null) {
				Log.warn("config.properties is not config in classpath");
				return dbMetaList;
			}
			Properties prop = new Properties();
			prop.load(input);
			Matcher matcher = null;
			String dbDefinitionId = null;
			DBLinkMetaData dbLinkMetaData = null;
			String portKey = null;
			for (Object key : prop.keySet()) {
				matcher = DB_HOST_ENUM.matcher(String.valueOf(key));
				if (matcher.matches()) {
					dbDefinitionId = matcher.group(1);
					dbLinkMetaData = new DBLinkMetaData();
					dbLinkMetaData.setDbDefineId(dbDefinitionId);
					dbLinkMetaData.setHostEmnu(prop.getProperty(matcher.group(0)));
					dbLinkMetaData.setUserName(prop.getProperty("db." + dbDefinitionId + ".username"));
					dbLinkMetaData.setDbName(prop.getProperty("db." + dbDefinitionId + ".dbname", dbDefinitionId));
					dbLinkMetaData.setPassword(prop.getProperty("db." + dbDefinitionId + ".password"));
					dbLinkMetaData.setDisabled(
							Boolean.parseBoolean(prop.getProperty("db." + dbDefinitionId + ".disabled", "false")));
					portKey = "db." + dbDefinitionId + ".port";
					int port = 3306;
					try {
						port = Integer.parseInt(prop.getProperty(portKey));
					} catch (Throwable e) {
					}
					dbLinkMetaData.setPort(port);
					if (!dbLinkMetaData.isDisabled()) {
						logger.info("dbLinkMetaData:" + dbLinkMetaData.toString());
						dbMetaList.add(dbLinkMetaData);
					} else {
						logger.info("dbLinkMetaData is disabled:" + dbLinkMetaData.toString());
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(input);
		}
		return dbMetaList;
	}

	// protected abstract List<DBLinkMetaData> parseDbLinkMetaData(
	// TaskContext context);
	protected abstract Collection<HdfsRealTimeTerminatorBean> getDumpBeans(TaskContext context) throws Exception;

	/**
	 * 在容器中注册额外的bean,比如大量的datasource
	 *
	 * @param factory
	 */
	protected abstract void registerExtraBeanDefinition(DefaultListableBeanFactory factory);

	// @Override
	@SuppressWarnings("all")
	public TaskReturn map(TaskContext context) {
		Map<String, DumpResult> /* 索引名称 */
		dumpResultMap = new HashMap<>();
		final Connection hiveConnection = HiveDBUtils.getInstance().createConnection();
		logger.info("static initialize start");
		initialSpringContext(context);
		logger.info("static initialize success");
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			final Date startTime = dateFormat.parse(context.getUserParam("dumpstarttime"));
			logger.info("dump startTime:" + startTime);
			// leader 选举代码
			// https://git-wip-us.apache.org/repos/asf?p=curator.git;a=blob;f=curator-examples/src/main/java/leader/LeaderSelectorExample.java;h=85f0598a62537952f072db6bdb5c16f049bab38f;hb=HEAD
			final IWorkflowFeedback workflowFeedback = new MockWorkflowFeedback();
			ExecutorCompletionService<DumpResult> executeService = new ExecutorCompletionService(
					BasicTerminatorClient.threadPool);
			int dumpJobCount = 0;
			AtomicBoolean joinTableClear = new AtomicBoolean(false);
			// 取得所有的dump bean 以表为单位
			Collection<HdfsRealTimeTerminatorBean> dumpbeans = getDumpBeans(context);
			if (dumpbeans.isEmpty()) {
				throw new IllegalStateException("dumpbeans list size can not small than 1");
			}
			for (final HdfsRealTimeTerminatorBean dumpBean : dumpbeans) {
				deleteHdfsHistoryFile(context, hiveConnection, joinTableClear, dumpBean);
				executeService.submit(new Callable<DumpResult>() {

					@Override
					public DumpResult call() throws Exception {
						DumpResult dumpResult = new DumpResult();
						try {
							dumpBean.executeDumpTask(false, workflowFeedback,
									dumpBean.getServiceConfig().getGroupSize(), true, new TriggerParamProcess() {

										@Override
										public void callback(TriggerParam param) {
											return;
										}
									}, startTime, hiveConnection);
							// dump 执行完成
							dumpResult.serviceConfig = dumpBean.getServiceConfig();
						} catch (Exception e) {
							dumpResult.error = e;
						}
						return dumpResult;
					}
				});
				dumpJobCount++;
			}
			DumpResult dumpResult = null;
			for (int i = 0; i < dumpJobCount; i++) {
				dumpResult = executeService.take().get();
				if (!dumpResult.isSuccess()) {
					if (dumpResult.error != null) {
						throw dumpResult.error;
					} else {
						logger.info("dump job error");
						return new TaskReturn(ReturnCode.FAILURE, "dump job error");
					}
				}
				logger.info("dump job:" + dumpResult.serviceConfig.getServiceName() + " complete!!!");
				dumpResultMap.put(dumpResult.serviceConfig.getServiceName(), dumpResult);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new TaskReturn(ReturnCode.FAILURE, "all table dump faild");
		} finally {
			try {
				hiveConnection.close();
			} catch (Exception e) {
			}
		}
		TaskReturn taskResult;
		try {
			JSONObject dumpResultDesc = createDumpResultDesc(dumpResultMap);
			taskResult = new TaskReturn(ReturnCode.SUCCESS, dumpResultDesc.toString(1));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return taskResult;
	}

	public void initialSpringContext(TaskContext context) {
		final List<DBLinkMetaData> dbMetaList = parseDbLinkMetaData(context);
		springContext = new ClassPathXmlApplicationContext("dump-app-context.xml", this.getClass()) {

			protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
				DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
				// DataSourceRegister.setApplicationContext(factory,
				// dbMetaList);
				SpringDBRegister dbRegister = new SpringDBRegister(dbMetaList, factory);
				dbRegister.setApplicationContext();
				registerExtraBeanDefinition(factory);
				super.prepareBeanFactory(beanFactory);
			}
		};
	}

	private static class SpringDBRegister extends DBRegister {

		private final DefaultListableBeanFactory factory;

		public SpringDBRegister(List<DBLinkMetaData> dbMetaList, DefaultListableBeanFactory factory) {
			super(dbMetaList);
			this.factory = factory;
		}

		@Override
		protected void createDefinition(final String dbDefinitionId, String driverClassName, String jdbcUrl,
				String userName, String password) {
			BeanDefinitionBuilder define = BeanDefinitionBuilder.genericBeanDefinition(BasicDataSource.class);
			define.setLazyInit(true);
			define.addPropertyValue("driverClassName", driverClassName);
			define.addPropertyValue("url", jdbcUrl);
			define.addPropertyValue("username", userName);
			define.addPropertyValue("password", password);
			define.addPropertyValue("validationQuery", "select 1");
			define.setDestroyMethodName("close");
			logger.info("create dbbean:" + dbDefinitionId + ",jdbc url:" + jdbcUrl);
			factory.registerBeanDefinition(dbDefinitionId, define.getBeanDefinition());
		}
	}

	public static class DBLinkMetaData {

		private String hostEmnu;

		private String userName;

		private String password;

		private String dbName;

		private int port;

		private boolean disabled = false;

		public boolean isDisabled() {
			return disabled;
		}

		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}

		private String dbDefineId;

		@Override
		public String toString() {
			return JSON.toJSONString(this);
		}

		public final int getPort() {
			return port;
		}

		public final void setPort(int port) {
			this.port = port;
		}

		void validate() {
			if (StringUtils.isBlank(hostEmnu)) {
				throw new IllegalStateException("dbDefineId:" + dbDefineId + ",hostEnum can not be null");
			}
			if (StringUtils.isBlank(userName)) {
				throw new IllegalStateException("dbDefineId:" + dbDefineId + ",userName can not be null");
			}
			if (StringUtils.isBlank(password)) {
				throw new IllegalStateException("dbDefineId:" + dbDefineId + ",password can not be null");
			}
			if (StringUtils.isBlank(dbName)) {
				throw new IllegalStateException("dbDefineId:" + dbDefineId + ",dbName can not be null");
			}
			if (StringUtils.isBlank(dbDefineId)) {
				throw new IllegalStateException("dbDefineId:" + dbDefineId + ",dbDefineId can not be null");
			}
		}

		public String getDbDefineId() {
			return dbDefineId;
		}

		public void setDbDefineId(String dbDefineId) {
			this.dbDefineId = dbDefineId;
		}

		public String getHostEmnu() {
			return hostEmnu;
		}

		public void setHostEmnu(String hostEmnu) {
			this.hostEmnu = hostEmnu;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getDbName() {
			return dbName;
		}

		public void setDbName(String dbName) {
			this.dbName = dbName;
		}
	}

	/**
	 * 清理历史数据
	 *
	 * @param context
	 * @param hiveConnection
	 * @param joinTableClear
	 * @param dumpBean
	 * @throws Exception
	 */
	private void deleteHdfsHistoryFile(TaskContext context, final Connection hiveConnection,
			AtomicBoolean joinTableClear, final HdfsRealTimeTerminatorBean dumpBean) throws Exception {
		if (joinTableClear.compareAndSet(false, true)) {
			// 只做一次
			TSearcherDumpContextImpl dumpContext = dumpBean.getDumpContext();
			// search4xxxx-FullDumpJob
			String indexName = StringUtils.substringBeforeLast(context.getUserParam("job.name"), "-");
			HiveRemoveHistoryDataTask historyDataClearTask = new HiveRemoveHistoryDataTask(indexName,
					dumpContext.getCurrentUserName(), dumpContext.getDistributeFileSystem());
			historyDataClearTask.deleteHdfsHistoryFile(hiveConnection);
		}
	}

	/**
	 * @param dumpResultMap
	 * @return
	 */
	private JSONObject createDumpResultDesc(Map<String, DumpResult> dumpResultMap) {
		try {
			JSONObject dumpResultDesc = new JSONObject();
			JSONArray importTabs = new JSONArray();
			for (String indexName : dumpResultMap.keySet()) {
				importTabs.put(indexName);
			}
			dumpResultDesc.put("tabs", importTabs);
			return dumpResultDesc;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static class DumpResult {

		private IServiceConfig serviceConfig;

		private Exception error;

		boolean isSuccess() {
			return serviceConfig != null && error == null;
		}
	}
}
