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
package com.qlangtech.tis.exec.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.URL;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.cloud.dump.DumpJobStatus;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import com.qlangtech.tis.manage.common.UISVersion;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.trigger.jst.AbstractIndexBuildJob;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;
import com.qlangtech.tis.yarn.common.YarnConstant;

/* 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RemoteYarnIndexBuildJob extends AbstractIndexBuildJob {

	// private static final Logger logger =
	// LoggerFactory.getLogger(RemoteYarnIndexBuildJob.class);
	private static final Logger logger = LoggerFactory.getLogger(RemoteYarnIndexBuildJob.class);

	/**
	 * @param processInfo
	 * @param group
	 *            第几组
	 * @param userName
	 */
	public RemoteYarnIndexBuildJob(ImportDataProcessInfo processInfo, int group, String userName) {
		super(processInfo, group, userName);
	}

	@Override
	protected BuildResult buildSliceIndex(String coreName, String timePoint, DumpJobStatus statuss, String outPath,
			String serviceName) throws Exception {
		TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
		RunEnvironment runtime = config.getRuntime();
		final List<Path> libs = getDependencyLibsPath();

		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(getYarnConfig());
		yarnClient.start();
		YarnClientApplication app = yarnClient.createApplication();
		ApplicationSubmissionContext submissionContext = app.getApplicationSubmissionContext();
		submissionContext.setMaxAppAttempts(100);
		submissionContext.setKeepContainersAcrossApplicationAttempts(false);
		final ApplicationId appid = submissionContext.getApplicationId();
		submissionContext.setApplicationName(coreName + "-indexbuild");
		ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);
		// 可以设置javaHome 留给以后扩展
		final String JAVA_HOME = "";// "/usr/lib/java/jdk1.8.0_91";

		String javaCommand = StringUtils.isEmpty(JAVA_HOME) ? "java" : (JAVA_HOME + "/bin/java ");

		final int memoryConsume = 500;
		amContainer.setCommands(
				Collections.singletonList(javaCommand + getMemorySpec(memoryConsume) + getRemoteDebugParam(runtime)
						+ " -Druntime=" + runtime.getKeyName() + " com.qlangtech.tis.build.yarn.BuildNodeMaster "
						+ createLauncherParam() + " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" + " 2>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"));
		
//		amContainer.setCommands(
//				Collections.singletonList(javaCommand + getMemorySpec(memoryConsume) + getRemoteDebugParam(runtime)
//						+ " -Druntime=" + runtime.getKeyName() + " com.qlangtech.tis.build.yarn.Main "
//						+ createLauncherParam() + " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" + " 2>"
//						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"));
		
		setJarsLibs(amContainer, libs);
		/* 运行依賴的環境變量 */
		Map<String, String> environment = new HashMap<String, String>();
		setEnvironment(environment, amContainer, true);
		submissionContext.setAMContainerSpec(amContainer);
		// 使用4核10G的节点，原则上越大越好
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(memoryConsume);
		capability.setVirtualCores(3);
		// submissionContext.setNodeLabelExpression(nodeLabelExpression);
		submissionContext.setResource(capability);
		submissionContext.setQueue("default");
		Priority p = Records.newRecord(Priority.class);
		p.setPriority(1000);
		submissionContext.setPriority(p);
		yarnClient.submitApplication(submissionContext);
		logger.info("success to submit");
		ApplicationReport appReport = yarnClient.getApplicationReport(appid);
		logger.info("get app report,appid:" + appid);
		YarnApplicationState appState = appReport.getYarnApplicationState();
		FinalApplicationStatus finalStatus = appReport.getFinalApplicationStatus();
		while (appState != YarnApplicationState.RUNNING && appState != YarnApplicationState.KILLED
				&& appState != YarnApplicationState.FAILED && appState != YarnApplicationState.FINISHED) {
			logger.info("waitting:" + coreName + " ,build task wait launch,current:" + appState);
			Thread.sleep(2000);
			appReport = yarnClient.getApplicationReport(appid);
			appState = appReport.getYarnApplicationState();
			finalStatus = appReport.getFinalApplicationStatus();
		}
		while (appState == YarnApplicationState.RUNNING) {
			logger.info("slice:" + coreName + " ,progress:" + appReport.getProgress());
			Thread.sleep(2000);
			appReport = yarnClient.getApplicationReport(appid);
			appState = appReport.getYarnApplicationState();
			finalStatus = appReport.getFinalApplicationStatus();
		}
		BuildResult result = new BuildResult(Integer.parseInt(groupNum), this.state);
		result.setSuccess(true);
		if (appState == YarnApplicationState.KILLED || appState == YarnApplicationState.FAILED
				|| finalStatus != FinalApplicationStatus.SUCCEEDED) {
			logger.error("slice:" + coreName + " ,build result:" + appState + "\n finalStatus:" + finalStatus
					+ "\ndiagnostics:" + appReport.getDiagnostics());
			result.setSuccess(false);
		} else {
			logger.info("core:" + coreName + " app (" + appid + ") is " + appState);
		}
		return result;
	}

	/**
	 * 取得执行build依赖的lib包路径<br>
	 * 事先需要先將jar包存放到本地目录中，执行时如果返现本地没有‘deploy_token’ 标记文件则需要将本地的lib包上传到远端
	 * 
	 * @return
	 */
	private List<Path> getDependencyLibsPath() throws Exception {
		TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
		RunEnvironment runtime = config.getRuntime();

		final Path dest = new Path(
				YarnConstant.HDFS_GROUP_LIB_DIR + YarnConstant.INDEX_BUILD_JAR_DIR + '/' + runtime.getKeyName());
		final File dataDir = new File(System.getProperty("data.dir"));
		if (!dataDir.exists()) {
			throw new IllegalStateException("data.dir has not been defined");
		}
		File deployToken = new File(dataDir, YarnConstant.LOCAL_JAR_DIR_PATH + File.separator + "deploy_token");
		List<Path> libs = null;
		synchronized (RemoteYarnIndexBuildJob.class) {
			if (deployToken.exists()) {
				// 之前的jar包已经存在
				libs = TISHdfsUtils.getLibPaths(StringUtils.EMPTY, dest);
			} else {
				// 重新上传indexbuild所需要的jar包
				libs = TISHdfsUtils.getLibPaths((new File(dataDir, YarnConstant.LOCAL_JAR_DIR_PATH)).getAbsolutePath(),
						dest);
				FileUtils.touch(deployToken);
			}
		}
		return libs;
	}

	private YarnConfiguration getYarnConfig() throws IOException {
		YarnConfiguration conf = new YarnConfiguration();
		conf.set("hadoop.job.ugi", "search");
		final File f = new File(YarnConstant.PATH_YARN_SITE);
		if (!f.exists()) {
			throw new IllegalStateException("yarn-site.xml is not exist:" + YarnConstant.PATH_YARN_SITE);
		}
		InputStream yarnsiteStream = FileUtils.openInputStream(f);
		conf.addResource(yarnsiteStream);
		return conf;
	}

	protected String getMemorySpec(int memoryConsume) {
		final int javaMemory = (int) (memoryConsume * 0.8);
		return " -Xms" + javaMemory + "m -Xmx" + javaMemory + "m";
	}

	protected String getRemoteDebugParam(RunEnvironment runtime) {
		return (runtime == RunEnvironment.DAILY) ? " -Xrunjdwp:transport=dt_socket,address=9890,suspend=n,server=y "
				: StringUtils.EMPTY;
	}

	private StringBuffer createLauncherParam() {
		final String username = "admin";
		final String coreName = state.getIndexName() + '-' + groupNum;
		TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
		JobConf jobConf = new JobConf();
		// 设置记录条数
		if (state.getDumpCount() != null) {
			jobConf.set(IndexBuildParam.INDEXING_ROW_COUNT, String.valueOf(state.getDumpCount()));
		}
		jobConf.set(IndexBuildParam.INDEXING_SOURCE_FS_NAME, config.getHdfsAddress());
		jobConf.set(IndexBuildParam.INDEXING_BUILD_TABLE_TITLE_ITEMS, state.getBuildTableTitleItems());
		String outPath = ImportDataProcessInfo.createIndexDir(username, state.getTimepoint(), groupNum,
				state.getIndexName(), false);
		jobConf.set(IndexBuildParam.INDEXING_OUTPUT_PATH, outPath);
		String hdfsSourcePath = state.getHdfsSourcePath() == null ? ImportDataProcessInfo.createIndexDir(username,
				state.getTimepoint(), groupNum, state.getIndexName(), true) : state.getHdfsSourcePath().build(groupNum);
		jobConf.set(IndexBuildParam.INDEXING_SOURCE_PATH, hdfsSourcePath);
		final String schemaPath = "/user/" + username + "/" + coreName + "/config/"
				+ ConfigFileReader.FILE_SCHEMA.getFileName();
		final String solrConifgPath = "/user/" + username + "/" + coreName + "/config/"
				+ ConfigFileReader.FILE_SOLOR.getFileName();
		jobConf.set(IndexBuildParam.INDEXING_SCHEMA_PATH, schemaPath);
		jobConf.set(IndexBuildParam.INDEXING_SOLRCONFIG_PATH, solrConifgPath);
		// "indexing.servicename"
		jobConf.set(IndexBuildParam.INDEXING_SERVICE_NAME, state.getIndexName());
		// "indexing.corename"
		jobConf.set(IndexBuildParam.INDEXING_CORE_NAME, coreName);
		// "indexing.maxNumSegments"
		jobConf.set(IndexBuildParam.INDEXING_MAX_NUM_SEGMENTS, String.valueOf(1));
		// "indexing.username"
		jobConf.set(IndexBuildParam.INDEXING_USER_NAME, username);
		// "indexing.incrtime"
		jobConf.set(IndexBuildParam.INDEXING_INCR_TIME, state.getTimepoint());
		// "indexing.groupnum"
		jobConf.set(IndexBuildParam.INDEXING_GROUP_NUM, groupNum);
		if (StringUtils.isNotBlank(state.getHdfsdelimiter())) {
			// "indexing.delimiter"
			jobConf.set(IndexBuildParam.INDEXING_DELIMITER, state.getHdfsdelimiter());
		}
		// "job.solrversion"
		jobConf.set(IndexBuildParam.INDEXING_SOLR_VERSION, UISVersion.SOLR_VERSION_7);
		jobConf.set(IndexBuildParam.INDEXING_SOURCE_TYPE, "HDFS");
		return jobConf.paramSerialize();
	}

	private static class JobConf {

		private Map<String, String> params = new HashMap<>();

		void set(String key, String value) {
			this.params.put(key, value);
		}

		public StringBuffer paramSerialize() {
			StringBuffer buffer = new StringBuffer();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (StringUtils.isBlank(entry.getValue())) {
					continue;
				}
				buffer.append(" -").append(entry.getKey()).append(" ").append(entry.getValue());
			}
			logger.info("main(String[] args),param:" + buffer.toString());
			return buffer;
		}
	}

	public static void start(String[] args) throws Exception {
	}

	private static void setEnvironment(Map<String, String> environment, ContainerLaunchContext ctx,
			boolean includeHadoopJars) throws IOException {
		Apps.addToEnvironment(environment, Environment.CLASSPATH.name(), Environment.PWD.$() + File.separator + "*",
				File.pathSeparator);
		// if (includeHadoopJars) {
		// Apps.addToEnvironment(environment, Environment.CLASSPATH.name(),
		// "/opt/cloudera/parcels/CDH/lib/hadoop-yarn/*", File.pathSeparator);
		// Apps.addToEnvironment(environment, Environment.CLASSPATH.name(),
		// "/opt/cloudera/parcels/CDH/lib/hadoop-hdfs/*", File.pathSeparator);
		// Apps.addToEnvironment(environment, Environment.CLASSPATH.name() //
		// , "/opt/cloudera/parcels/CDH/lib/hadoop/*", File.pathSeparator);
		// Apps.addToEnvironment(environment, Environment.CLASSPATH.name(),
		// "/opt/cloudera/parcels/CDH/lib/hadoop/lib/*", File.pathSeparator);
		// Apps.addToEnvironment(environment, Environment.CLASSPATH.name(),
		// "/opt/cloudera/parcels/CDH/lib/hadoop-mapreduce/*",
		// File.pathSeparator);
		// }
		ctx.setEnvironment(environment);
		logger.info("classpath:" + environment.get(Environment.CLASSPATH.name()));
	}

	private static void setJarsLibs(ContainerLaunchContext amContainer, List<Path> libs) {
		Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();
		for (Path l : libs) {
			localResources.put(l.getName(), setupAppJar(l));
		}
		amContainer.setLocalResources(localResources);
	}

	private static LocalResource setupAppJar(Path jarPath) {
		try {
			LocalResource localResource = Records.newRecord(LocalResource.class);
			FileStatus jarStat = TISHdfsUtils.getFileSystem().getFileStatus(jarPath);
			URL resURI = ConverterUtils.getYarnUrlFromPath(jarStat.getPath());
			localResource.setResource(resURI);
			localResource.setSize(jarStat.getLen());
			localResource.setTimestamp(jarStat.getModificationTime());
			localResource.setType(LocalResourceType.FILE);
			localResource.setVisibility(LocalResourceVisibility.APPLICATION);
			return localResource;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
