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
package com.qlangtech.tis.hdfs.client.bean;

import java.io.IOException;
import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.common.config.IServiceConfig;
import com.qlangtech.tis.common.zk.OnReconnect;
import com.qlangtech.tis.common.zk.TerminatorZkClient;
import com.qlangtech.tis.dump.hive.BindHiveTableTool;
import com.qlangtech.tis.dump.hive.HiveRemoveHistoryDataTask;
import com.qlangtech.tis.exception.DataImportHDFSException;
import com.qlangtech.tis.exception.TerminatorInitException;
import com.qlangtech.tis.hdfs.client.bean.searcher.BasicTerminatorSearcher;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherDumpContextImpl;
import com.qlangtech.tis.hdfs.client.data.HDFSProvider;
import com.qlangtech.tis.hdfs.client.data.MultiThreadHDFSDataProvider;
import com.qlangtech.tis.hdfs.client.router.GroupRouter;
import com.qlangtech.tis.hdfs.client.service.ImportGroupConfig;
import com.qlangtech.tis.hdfs.client.service.ImportGroupServiceSupport;
import com.qlangtech.tis.hdfs.client.status.SolrCoreStatusHolder;
import com.qlangtech.tis.hdfs.util.Assert;
import com.qlangtech.tis.hdfs.util.Constants;
import com.qlangtech.tis.trigger.service.TriggerJobService;
import com.qlangtech.tis.trigger.socket.IWorkflowFeedback;
import com.qlangtech.tis.trigger.util.TriggerParam;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicTerminatorClient implements ImportGroupServiceSupport, TriggerJobService {

	public static final ExecutorService threadPool = java.util.concurrent.Executors.newCachedThreadPool();

	// private Context context;
	public static final String DEFAULT_SERVLET_CONTEXT = "terminator-search";

	public static final int DEFAULT_ZK_TIMEOUT = 300000;

	private static final Logger logger = LoggerFactory.getLogger(BasicTerminatorClient.class);

	private BasicTerminatorSearcher searcher;

	// private final String CLIENT_ZK_PATH = "/terminator/dump-controller/";
	// protected VisualGroupInfo visualGroupInfo= null;
	// private boolean visualGroupMode = false;
	private TSearcherDumpContextImpl dumpContext;

	private IWorkflowFeedback workflowFeedback = new MockWorkflowFeedback();

	@SuppressWarnings("all")
	private HDFSProvider fullHdfsProvider;

	@SuppressWarnings("all")
	private HDFSProvider incrHdfsProvider;

	// private int zkTimeout = DEFAULT_ZK_TIMEOUT;
	private String servletContextName = DEFAULT_SERVLET_CONTEXT;

	private SolrCoreStatusHolder hostStatusHolder;

	private Integer hsfTryInteger;

	private String hsfTryCountString = "7";

	private String shardKey = "id";

	// 可以默认不连接trigger服务器
	private boolean shallConnectTriggerServer = true;

	private long lastRegisterTime = 0;

	private ImportGroupConfig importGroupConfig;

	private boolean canConnectToZK = false;

	// 初始化是已经执行的状态
	private AtomicBoolean fullDumpExecuted = new AtomicBoolean(true);

	private AtomicBoolean incrDumpExecuted = new AtomicBoolean(true);

	public BasicTerminatorClient() {
		super();
	}

	@Override
	public boolean triggerFullImportJob(Long taskId, Long jobId) {
		return false;
	}

	@Override
	public boolean triggerIncrImportJob(Long taskId, Long jobId) {
		return false;
	}

	@SuppressWarnings("all")
	public HDFSProvider getFullHdfsProvider() {
		return fullHdfsProvider;
	}

	@SuppressWarnings("all")
	public void setFullHdfsProvider(HDFSProvider fullHdfsProvider) {
		this.fullHdfsProvider = fullHdfsProvider;
	}

	@SuppressWarnings("all")
	public HDFSProvider getIncrHdfsProvider() {
		return incrHdfsProvider;
	}

	@SuppressWarnings("all")
	public void setIncrHdfsProvider(HDFSProvider incrHdfsProvider) {
		this.incrHdfsProvider = incrHdfsProvider;
	}

	public String getFSName() {
		return dumpContext.getFSName();
	}

	public IWorkflowFeedback getWorkflowFeedback() {
		return workflowFeedback;
	}

	public void setWorkflowFeedback(IWorkflowFeedback workflowFeedback) {
		this.workflowFeedback = workflowFeedback;
	}

	public BasicTerminatorSearcher getSearcher() {
		return searcher;
	}

	public void setSearcher(BasicTerminatorSearcher searcher) {
		this.searcher = searcher;
	}

	public final IServiceConfig getServiceConfig() {
		return searcher.getServiceConfig();
	}

	public final GroupRouter getGroupRouter() {
		return searcher.getGroupRouter();
	}

	public final TerminatorZkClient getZkClient() {
		return this.searcher.getZkClient();
	}

	/**
	 */
	public void init() throws TerminatorInitException {
		// 创建查询对象
		// =
		Assert.assertNotNull("searcher can not be null", this.searcher);
		// createTerminatorSearcher();
		checkRequestProperty();
		// 发布客户端socket服务
		// 发布客户端
		// publishRemoteTriggerJobService();
		initImportGroupConfig();
		publishTriggerJobService();
		registerTriggerLogService();
		initMasterZK();
	}

	public void initMasterZK() {

	}

	public String getServletContextName() {
		return servletContextName;
	}

	// public TerminatorZkClient zkClient = null;
	// private String zkAddress;
	public void setServletContextName(String servletContextName) {
		this.servletContextName = servletContextName;
	}

	public SolrCoreStatusHolder getHostStatusHolder() {
		return hostStatusHolder;
	}

	public void setHostStatusHolder(SolrCoreStatusHolder hostStatusHolder) {
		this.hostStatusHolder = hostStatusHolder;
	}

	protected boolean isSingleGroup() {
		return getServiceConfig().getGroupSize() == 1;
	}

	public void registerTriggerLogService() {
	}

	public void close() {
		FileSystem fileSystem = dumpContext.getDistributeFileSystem();
		try {
			if (fileSystem != null)
				fileSystem.close();
		} catch (IOException e) {
			logger.warn("[注意]关闭HDFS文件系统失败", e);
		}
	}

	public String getServiceName() {
		return dumpContext.getServiceName();
	}

	// @Override
	// public void createFileSystem() {
	// fileSystem = HDFSClusterManager.getInstance().createFileSystem();
	// }
	// protected ServiceConfig serviceConfig = null;
	// public void setServiceConfig(ServiceConfig serviceConfig) {
	// this.serviceConfig = serviceConfig;
	// }
	public void subscribeTerminatorOuterService(String version) {
	}

	// @Override
	public void copyToLocalFile(String src, String dst) throws TerminatorInitException {
		final FileSystem fileSystem = this.dumpContext.getDistributeFileSystem();
		if (fileSystem == null) {
			throw new TerminatorInitException(">>>文件系统没初始化或者初始化失败<<");
		}
		Path srcPath = new Path(src);
		Path dstPath = new Path(dst);
		try {
			fileSystem.copyToLocalFile(srcPath, dstPath);
		} catch (IOException e) {
			throw new TerminatorInitException(">>>>>从HDFS集群下载文件到本地出错<<<<<", e);
		}
	}

	public void setHsfTryInteger(Integer hsfTryInteger) {
		this.hsfTryInteger = hsfTryInteger;
	}

	public void setHsfTryCountString(String hsfTryCountString) {
		this.hsfTryCountString = hsfTryCountString;
	}

	public boolean isCanConnectToZK() {
		return canConnectToZK;
	}

	public void setCanConnectToZK(boolean canConnectToZK) {
		this.canConnectToZK = canConnectToZK;
	}

	public final void setHsfTryCount(String hsfTryCountString) {
		this.hsfTryCountString = hsfTryCountString;
	}

	public String getShardKey() {
		return shardKey;
	}

	public void setShardKey(String shardKey) {
		this.shardKey = shardKey;
	}

	public boolean isShallConnectTriggerServer() {
		return shallConnectTriggerServer;
	}

	public void setShallConnectTriggerServer(boolean shallConnectTriggerServer) {
		this.shallConnectTriggerServer = shallConnectTriggerServer;
	}

	// baisui response for
	// @Override
	public void initServiceDataToHdfsTask() throws TerminatorInitException {
		// 每次 执行必须间隔 10秒
		if (System.currentTimeMillis() < lastRegisterTime + (1000 * 10)) {
			logger.warn("too frequent to execute the initServiceDataToHdfsTask method");
			return;
		}
		lastRegisterTime = System.currentTimeMillis();
		if (!isShallConnectTriggerServer()) {
			logger.warn("【注意】ShallConnectTriggerServer is false");
			return;
		}
		if (this.getFullHdfsProvider() == null && this.getIncrHdfsProvider() == null) {
			logger.warn("【注意】没有配置HdfsProvider 那么该Bean将只会提供检索服务");
			return;
		}
		// 服务启动抢锁是否成功？
		// 如果该应用抢到dump锁的话就不会启动 想dump 任务中心注册了
		logger.warn("start to register job:" + this.getServiceName());
		// 注册定时任务
		Assert.assertNotNull("this.zkClient can not be null", getZkClient());
		// try {
		//
		// final Watcher watcher = new Watcher() {
		// @Override
		// public void process(WatchedEvent event) {
		//
		// if (EventType.NodeDataChanged == event.getType()
		// || EventType.NodeCreated == event.getType()) {
		// logger.warn("start execute the method
		// initServiceDataToHdfsTask,event:"
		// + event);
		// // initServiceDataToHdfsTask();
		// try {
		// registerDumpJob(this);
		// } catch (TriggerJobNotValidException e) {
		// throw new RuntimeException(e);
		// }
		//
		// } else {
		// // getTriggerServerAddress(this);
		// if (EventType.NodeDeleted == event.getType()) {
		// try {
		// getZkClient().exists(TRIGGER_SERVER, this);
		// logger.warn("continue to watch the node,event:"
		// + event);
		// } catch (Exception e) {
		// throw new TerminatorInitException(e);
		// }
		// } else {
		// logger.warn("ignor the event:" + event);
		// }
		// }
		// }
		// };
		//
		// while (true) {
		// try {
		// registerDumpJob(watcher);
		// break;
		// } catch (TriggerJobNotValidException e) {
		// throw e;
		// } catch (Exception e) {
		// logger.warn(e.getMessage(), e);
		// try {
		// // 等待10秒继续尝试连接
		// Thread.sleep(10000);
		// } catch (InterruptedException e1) {
		//
		// }
		// }
		// }
		//
		// } catch (TriggerJobNotValidException e) {
		// e.printStackTrace();
		// logger.warn(e);
		// }
	}

	public String getOuterDataId() {
		return getServiceName() + "-outerservice";
	}

	public String getOuterGroupId() {
		return "Terminator-outer";
	}

	public String getOuterDataId(String groupNum) {
		return getServiceName() + "-" + groupNum + "-outerservice";
	}

	private void initImportGroupConfig() {
		// /////////////////////////////////////////////////
		try {
			this.importGroupConfig = new ImportGroupConfig(this.dumpContext.getServiceName(),
					this.dumpContext.getZkClient(), this);
		} catch (Exception e) {
			throw new TerminatorInitException("Load ImportGroupConfig Object From ZK Have Error: ", e);
		}
		try {
			ImportGroupConfig.backUp2LocalFS(importGroupConfig);
		} catch (IOException e) {
		}
		dumpContext.getZkClient().setOnReconnect(new OnReconnect() {

			public void onReconnect(TerminatorZkClient zkClient) {
				try {
					importGroupConfig.initConfig();
				} catch (Exception e) {
					logger.warn("[" + dumpContext.getServiceName() + "]和ZK断开后重新连接ZK，并重新初始化importGroupConfig出现错误:", e);
				}
			}

			@Override
			public String getReconnectName() {
				return "ImportGroupConfigEvent";
			}
		});
	}

	// public void setServiceName(String serviceName) {
	// this.serviceName = serviceName;
	// }
	// start 百岁 add 20120628
	// private Long fullDumpJobid;
	// private Long incrDumpJobid;
	// private String triggerServerAddress;
	public void publishTriggerJobService() {
		// 百岁修改20121210 将应用变成一个纯粹的查询业务端
		if (!this.isShallConnectTriggerServer()) {
			logger.warn("this have any dump logic ,so shall not gain the dump lock");
			return;
		}
	}

	public void checkRequestProperty() throws TerminatorInitException {
		if (getServiceName() == null) {
			throw new TerminatorInitException("【注意】请在配置文件中设置serviceName");
		}
		// }
		if (hsfTryCountString != null) {
			try {
				hsfTryInteger = new Integer(hsfTryCountString);
				if (hsfTryInteger <= 0) {
					throw new Exception();
				}
			} catch (Exception e) {
				throw new TerminatorInitException("【注意】配置的hsf服务重试次数不为正整数");
			}
		}
	}

	protected final boolean isFullDumpExecuted() {
		return fullDumpExecuted.get();
	}

	protected final boolean isIncrDumpExecuted() {
		return incrDumpExecuted.get();
	}

	public final void fullDumpExecuted() {
		fullDumpExecuted.getAndSet(true);
	}

	public final void unexecutedFullDump() {
		fullDumpExecuted.getAndSet(false);
	}

	public final void incrDumpExecuted() {
		incrDumpExecuted.getAndSet(true);
	}

	public final void unexecutedIncrDump() {
		incrDumpExecuted.getAndSet(false);
	}

	// protected Map<String, ClientDumpHDFSService> dumpHdfsMap = new
	// ConcurrentHashMap<String, ClientDumpHDFSService>();
	// baisui
	@SuppressWarnings("all")
	public // @Override
	void executeDumpTask(boolean isIncr, IWorkflowFeedback feedback, // DumpJobHook
																		// jobhook,
			final int groupNum, boolean force, TriggerParamProcess triggerParamProcess, Date startTime,
			Connection hiveConnection) {
		// JobExecutionContext
		// if (this.isImportJobSuspend() && !force) {
		// logger.warn((isIncr ? "【增量】" : "【全量】") + "任务被暂停<<<<<<");
		// return;
		// }
		TSearcherDumpContextImpl dumpContext = this.getDumpContext();
		final Map result = new HashMap();
		logger.warn((isIncr ? "【增量】" : "【全量】") + "将执行<<<<<<");
		try {
			// 如果阀值锁为空，可能是ZK出现问题，那么不做控制，
			// 如果阀值锁不为空，并且获取到导入HDFS的资源,也就是此时HDFS还没有到任务阀值，那么能马上进行任务操作
			// if (lock == null || (lock != null &&
			// lock.tryLock(isIncr))) {
			// 使用isBusy是为了不让有线程一直被挂住，直接忽视调这次任务调用
			// Busy肯定不起作用，所以importServiceData方法为同步控制
			MultiThreadHDFSDataProvider.setDumpLaunchTime(result, startTime);
			HDFSProvider dataProvider = isIncr ? this.getIncrHdfsProvider() : this.getFullHdfsProvider();
			Assert.assertNotNull(isIncr ? "incr" : "full" + " dump hdfs provider can not be null", dataProvider);
			if (!dataProvider.isBusy().get()) {
				HiveRemoveHistoryDataTask historyDataClearTask = new HiveRemoveHistoryDataTask(
						dumpContext.getServiceName(), dumpContext.getCurrentUserName(),
						dumpContext.getDistributeFileSystem());
				// FileSystem fileSystem, Connection hiveConnection)
				try {
					// 清理历史文件
					historyDataClearTask.deleteHdfsHistoryFile(hiveConnection);
					// logger.warn((isIncr ? "【增量】" : "【全量】") + "任务正在执行<<<<<<");
					dataProvider.importServiceData(isIncr, feedback, result, groupNum);
					// 导入完成,需要绑定hive表
					BindHiveTableTool.bindHiveTables(dumpContext.getDistributeFileSystem(),
							Collections.singleton(dumpContext.getServiceName()), dumpContext.getCurrentUserName(),
							MultiThreadHDFSDataProvider.getDumpLaunchTimestamp(result));
				} catch (Exception e) {
					throw new DataImportHDFSException(e);
				}
				logger.warn((isIncr ? "【增量】" : "【全量】") + "任务执行结束<<<<<<");
			} else {
				// 当前任务虽然拿到HDFS集群的执行任务的锁可以执行任务，但是此时客户端的
				// 数据提供者正在执行任务，没有空闲，所以赶紧释放锁，让其他客户能拿到锁资源。
				// 其实针对目前的应用情况而言，只有一个增量和一个全量，其实并没有增量/全量Job同时竞争一个dataProvider情况
				// lock.unLock(isIncr);
				logger.warn(">>>>>>>>>>此DataProvider正在执行HDFS" + (isIncr ? "【增量】" : "【全量】")
						+ "导入任务,这次任务调用将被忽略！！！<<<<<<<<<<");
			}
		} catch (DataImportHDFSException e) {
			e.printStackTrace();
			// 百岁添加20121024
			result.put(Constants.SHALL_DO_JOB_HOOK, "false");
			logger.warn("定时任务执行源数据导入HDFS出错" + e.getMessage(), e);
			// logger.warn(">>>>>>>【注意】当前任务导入失败，忽略此次任务调用<<<<<<");
		} finally {
			if (isIncr) {
				this.incrDumpExecuted();
			} else {
				this.fullDumpExecuted();
			}
		}
	}

	public TSearcherDumpContextImpl getDumpContext() {
		return dumpContext;
	}

	public void setDumpContext(TSearcherDumpContextImpl dumpContext) {
		this.dumpContext = dumpContext;
	}

	public interface TriggerParamProcess {

		void callback(TriggerParam param);
	}
	// public void initContext() {
	// context = new Context();
	// context.setDataProcessor(this.getDataProcessor());
	// context.setDistributeFileSystem(fileSystem);
	// // context.setGroupNameSet(this.importGroupConfig.getGroupNameSet());
	// context.setGroupRoute(getGroupRouter());
	// context.setIncrDataProvider(this.getIncrDataProvider());
	// context.setFullDataProvider(this.getFullDataProvider());
	// context.setServiceName(serviceName);
	// context.setZkClient(this.getZkClient());
	// context.setConfiguration(configuration);
	// context.setlocalTimeFilePath(localTimeFilePath);
	// context.setFSName(fsName);
	// context.setShardKey(shardKey);
	// context.setWriteHdfsThreadCount(writeHdfsThreadCount);
	// if (fileTimeProvider == null && this.isShallInitializeHdfs()) {
	// try {
	// if (localTimeFilePath != null)
	// fileTimeProvider = new FileTimeProvider(localTimeFilePath);// 本地存储
	// else
	// fileTimeProvider = new FileTimeProvider(context,
	// currentUserName);// HDFS存储
	// } catch (TimeManageException e) {
	// logger.error("[错误]生成时间记录生成器出现错误，将不能正常启动", e);
	// throw new RuntimeException("[错误]生成时间记录生成器出现错误，将不能正常启动", e);
	// }
	// }
	// context.setTimeProvider(fileTimeProvider);
	// }
	/*
	 * TSearcherContext impl
	 * start================================================
	 * ================================
	 */
	// @Override
	// public Configuration getConfiguration() {
	// return this.configuration;
	// }
	/*
	 * TSearcherContext impl
	 * end==================================================
	 * ==============================
	 */
}
