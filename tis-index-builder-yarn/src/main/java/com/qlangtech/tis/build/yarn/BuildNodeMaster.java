package com.qlangtech.tis.build.yarn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.build.log.AppnameAwareFlumeLogstashV1Appender;
import com.qlangtech.tis.build.task.TaskReturn;
import com.qlangtech.tis.build.task.TaskReturn.ReturnCode;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.map.HdfsIndexBuilder;
import com.qlangtech.tis.indexbuilder.map.HdfsIndexGetConfig;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import com.qlangtech.tis.yarn.common.YarnConstant;

/**
 * build索引的master节点类
 * 
 * @author 百岁
 *
 * @date 2016年3月31日
 */
public class BuildNodeMaster implements AMRMClientAsync.CallbackHandler {

	public static final String ENVIRONMENT_EXEC_INDEXS = "exec_indexs";

	private static final Logger logger = LoggerFactory.getLogger(BuildNodeMaster.class);

	boolean isShutdown = false;

	private AMRMClientAsync<ContainerRequest> rmClient;

	public static void main(String[] args) throws Exception {
		CommandLine commandLine = parseCommandLine(args);

		HdfsIndexBuilder.setMdcAppName(commandLine.getOptionValue(IndexBuildParam.INDEXING_SERVICE_NAME));

		logger.info("param:##############################");
		for (String key : IndexBuildParam.getAllFieldName()) {
			logger.info(key + ":" + commandLine.getOptionValue(key));
		}
		logger.info("param:##############################");
		// TSearcherConfigFetcher
		// .setConfigCenterHost(/* runtime
		// */RunEnvironment.getSysEnvironment().getKeyName());
		BuildNodeMaster master = new BuildNodeMaster();
		master.run(commandLine);
	}

	public static CommandLine parseCommandLine(String[] args) {
		return DeployIndexBuildJars.processCommandLineArgs("index-build", getClientOptions(), args);
	}

	private HdfsIndexBuilder indexBuilder = null;
	private TaskContext taskContext = null;

	/**
	 * 取得任务执行百分比，只能近似正確
	 */
	@Override
	public float getProgress() {
		if (indexBuilder == null || taskContext == null) {
			return 0;
		}
		final long allRowCount = taskContext.getAllRowCount();
		long indexMakeCounter = taskContext.getIndexMakerComplete();
		// logger.info("complete:" + indexMakeCounter + ",all:" + allRowCount);
		float mainProgress = (float) (((double) indexMakeCounter) / allRowCount);
		return (float) (((mainProgress > 1.0) ? 1.0 : mainProgress));
		// + ((indexBuilder.getMergeOver() ? 0.02f : 0.0f));
	}

	@SuppressWarnings("all")
	private static Option[] getClientOptions() {

		List<String> fields = IndexBuildParam.getAllFieldName();
		List<Option> opts = new ArrayList<>();
		for (String f : fields) {
			opts.add(OptionBuilder.withArgName(f).hasArg().isRequired(false).withDescription(f).create(f));
		}
		return opts.toArray(new Option[fields.size()]);
	}

	private YarnConfiguration conf;

	private Configuration getConfiguration() {
		return this.conf;
	}

	public void run(CommandLine commandLine) throws Exception {

		HdfsIndexBuilder.setMdcAppName(commandLine.getOptionValue(IndexBuildParam.INDEXING_SERVICE_NAME));

		this.conf = new YarnConfiguration();
		final File yarnSite = new File(YarnConstant.PATH_YARN_SITE);
		if (!yarnSite.exists()) {
			throw new IllegalStateException("yarnSite file is not exist:" + yarnSite.getAbsolutePath());
		}

		conf.addResource(FileUtils.openInputStream(yarnSite));

		rmClient = AMRMClientAsync.createAMRMClientAsync(1000, this);
		rmClient.init(getConfiguration());
		rmClient.start();
		rmClient.registerApplicationMaster("", 0, "");
		logger.info("have register master");

		try {
			/* 执行索引build start */
			HdfsIndexGetConfig configJob = new HdfsIndexGetConfig();
			this.indexBuilder = new HdfsIndexBuilder();
			this.taskContext = TaskContext.create(commandLine);

			TaskReturn result = configJob.map(taskContext);
			if (result.getReturnCode() == ReturnCode.FAILURE) {
				masterShutdown(FinalApplicationStatus.FAILED, result.getMsg());
				return;
			}
			result = indexBuilder.map(taskContext);
			if (result.getReturnCode() == ReturnCode.FAILURE) {
				masterShutdown(FinalApplicationStatus.FAILED, result.getMsg());
				return;
			}

			/* 执行索引build end */
			masterShutdown(FinalApplicationStatus.SUCCEEDED, StringUtils.EMPTY);
		} catch (Throwable e) {
			// logger.error(e.getMessage(), e);
			logger.error(e.getMessage(), e);
			masterShutdown(FinalApplicationStatus.FAILED, ExceptionUtils.getRootCauseMessage(e));
		} finally {
			AppnameAwareFlumeLogstashV1Appender.closeAllFlume();
		}
	}

	// public static void setMdcAppName(String appname) {
	// MDC.put(AppnameAwareFlumeLogstashV1Appender.KEY_COLLECTION, appname);
	// }

	protected void masterShutdown(FinalApplicationStatus appStatus, String msg) {
		String m = "build master application shutdown.";
		System.out.println(m);

		if (appStatus == FinalApplicationStatus.FAILED) {
			logger.error(m + ",status:" + appStatus + ",msg:" + msg);
		} else {
			logger.info(m + ",status:" + appStatus + ",msg:" + msg);
		}
		try {
			rmClient.unregisterApplicationMaster(appStatus
			// FinalApplicationStatus.SUCCEEDED
					, msg, "");
		} catch (Exception exc) {
			// safe to ignore ... this usually fails anyway
		}
	}

	// public synchronized boolean doneWithContainers() {
	// // return isShutdown || numContainersToWaitFor <= 0;
	// return true;
	// }

	// public static String getRemoteDebugStr() {
	// return "-Xrunjdwp:transport=dt_socket,address=45687,suspend=n,server=y";
	// }

	// public static void setEnvironment(Map<String, String> environment,
	// ContainerLaunchContext ctx,
	// YarnConfiguration conf, boolean includeHadoopJars) throws IOException {
	//
	// Apps.addToEnvironment(environment, Environment.CLASSPATH.name(),
	// Environment.PWD.$() + File.separator + "*",
	// File.pathSeparator);
	// if (includeHadoopJars) {
	//
	// for (String c :
	// conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
	// YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
	// Apps.addToEnvironment(environment, Environment.CLASSPATH.name(),
	// c.trim(), File.pathSeparator);
	// }
	// }
	//
	// ctx.setEnvironment(environment);
	// System.out.println("classpath:" +
	// environment.get(Environment.CLASSPATH.name()));
	// }

	public void onShutdownRequest() {
		System.out.println("onShutdownRequest");
		this.isShutdown = true;
	}

	public void onNodesUpdated(List<NodeReport> updatedNodes) {
		System.out.println("onNodesUpdated");
	}

	public void onError(Throwable e) {
		e.printStackTrace();
	}

	/* 由于这里没有slaver所以这里两个方法不会触发 */
	@Override
	public void onContainersCompleted(List<ContainerStatus> statuses) {
	}

	@Override
	public void onContainersAllocated(List<Container> containers) {
	}
	/* 由于这里没有slaver所以这里两个方法不会触发 end */
}
