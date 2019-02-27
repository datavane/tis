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
package com.qlangtech.tis.realtime.yarn;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
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
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.realtime.TisIncrLauncher;

/*
 * 该类的启动类是：com.qlangtech.tis.hdfs.TisIncrLauncher(在terminator-job-trigger-common工程中)
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TransferStart implements Runnable {

	public static final String JAVA_HOME_8 = "/usr/lib/jvm/java-1.8.0-oracle.x86_64";

	// 使用数据中心的机器
	public static final String PATH_YARN_SITE = "/etc/hive/conf.cloudera.hive/yarn-site.xml";

	// 使用自己搭建的网络
	// public static final String PATH_YARN_SITE =
	// "/usr/share/hadoop-2.6.0/etc/hadoop/yarn-site.xml";
	private static final MessageFormat HDFS_GROUP_LIB_DIR_FORMAT = new MessageFormat("/user/admin/{0}");

	private static final Logger logger = LoggerFactory.getLogger(TransferStart.class);

	private final CommandLine commandLine;

	public TransferStart(CommandLine commandLine) {
		super();
		if (commandLine == null) {
			throw new IllegalArgumentException("param commandLine can not be null");
		}
		this.commandLine = commandLine;
	}

	private static String getIncrGroupName(CommandLine commandLine) {
		final String incrGroupName = commandLine.getOptionValue(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP);
		Objects.requireNonNull(incrGroupName,
				"param " + TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP + " can not be null");
		return incrGroupName;
	}

	@Override
	public void run() {
		try {
			RunEnvironment runtime = RunEnvironment.getEnum(commandLine.getOptionValue(RunEnvironment.KEY_RUNTIME));
			RunEnvironment.setSysRuntime(runtime);
			// System.setProperty(RunEnvironment.KEY_RUNTIME,
			// runtime.getKeyName());
			final String incrGroupName = getIncrGroupName(commandLine);
			// TSearcherConfigFetcher.setConfigCenterHost(runtime.getKeyName());
			List<Path> libs = getLibPaths(incrGroupName, commandLine, runtime);
			YarnConfiguration conf = new YarnConfiguration();
			conf.addResource(FileUtils.openInputStream(new File(PATH_YARN_SITE)));
			YarnClient yarnClient = YarnClient.createYarnClient();
			yarnClient.init(conf);
			yarnClient.start();
			YarnClientApplication app = yarnClient.createApplication();
			ApplicationSubmissionContext submissionContext = app.getApplicationSubmissionContext();
			submissionContext.setMaxAppAttempts(100);
			submissionContext.setKeepContainersAcrossApplicationAttempts(false);
			final ApplicationId appid = submissionContext.getApplicationId();
			submissionContext.setApplicationName(incrGroupName + "-incr");
			ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);
			System.out.println("runtime:" + runtime);
			final int masterMemorySpec = 512;
			amContainer.setCommands(Collections.singletonList(JAVA_HOME_8 + "/bin/java " + " -Druntime="
					+ runtime.getKeyName() + " " + getRunningMemorySpec(masterMemorySpec)
					+ " com.qlangtech.tis.realtime.yarn.TransferNodeMaster " + incrGroupName + " 1>"
					+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" + " 2>"
					+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"));
			setJarsLibs(amContainer, libs);
			Map<String, String> environment = new HashMap<String, String>();
			TransferNodeMaster.setEnvironment(environment, amContainer, conf, true);
			// setClasspath(amContainer, conf);
			submissionContext.setAMContainerSpec(amContainer);
			Resource capability = Records.newRecord(Resource.class);
			capability.setMemory(masterMemorySpec);
			capability.setVirtualCores(1);
			submissionContext.setResource(capability);
			submissionContext.setQueue(commandLine.getOptionValue(TisIncrLauncher.PARAM_OPTION_LOCAL_QUEUE));
			Priority p = Records.newRecord(Priority.class);
			p.setPriority(1000);
			submissionContext.setPriority(p);
			yarnClient.submitApplication(submissionContext);
			logger.info("success to submit");
			ApplicationReport appReport = yarnClient.getApplicationReport(appid);
			logger.info("get app report");
			YarnApplicationState appState = appReport.getYarnApplicationState();
			while (appState != YarnApplicationState.RUNNING && appState != YarnApplicationState.KILLED
					&& appState != YarnApplicationState.FAILED) {
				System.out.println("waitting app launch ,current:" + appState);
				Thread.sleep(2000);
				appReport = yarnClient.getApplicationReport(appid);
				appState = appReport.getYarnApplicationState();
			}
			System.out.println("\n \n app (" + appid + ") is " + appState + "\n" + appReport.getDiagnostics() + "\n\n");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getRunningMemorySpec(int masterMemorySpec) {
		int memory = (int) (masterMemorySpec * (0.85));
		return " -Xms" + memory + "m -Xmx" + memory + "m ";
	}

	protected static List<Path> getLibPaths(String incrGroupName, CommandLine commandLine, RunEnvironment runtime) {
		Path libRootPath = null;
		try {
			String localJarDir = null;
			if (commandLine != null) {
				localJarDir = commandLine.getOptionValue(TisIncrLauncher.PARAM_OPTION_LOCAL_JAR_DIR);
			}
			libRootPath = getLibRootPath(incrGroupName, runtime);
			List<Path> libs = null;
			if (StringUtils.isNotBlank(localJarDir)) {
				libs = copyLibs2Hdfs(localJarDir, libRootPath);
			} else {
				libs = new ArrayList<Path>();
				for (FileStatus s : getFileSystem().listStatus(libRootPath)) {
					libs.add(s.getPath());
				}
			}
			if (libs.size() < 1) {
				throw new IllegalStateException("libs size can not small than 1");
			}
			return libs;
		} catch (Exception e) {
			throw new RuntimeException("libpath:" + libRootPath, e);
		}
	}

	private static Path getLibRootPath(String incrGroupName, RunEnvironment runtime) {
		return new Path(HDFS_GROUP_LIB_DIR_FORMAT.format(new Object[] { incrGroupName }) + "/" + runtime.getKeyName());
	}

	private static List<Path> copyLibs2Hdfs(String localJarDir, Path libRootPath) throws Exception {
		List<Path> libs = new ArrayList<Path>();
		if (StringUtils.isBlank(localJarDir)) {
			throw new IllegalArgumentException("param localJarDir can not be null");
		}
		FileSystem fs = getFileSystem();
		// getLibRootPath(commandLine,runtime);//
		final Path path = libRootPath;
		// new Path(HDFS_GROUP_LIB_DIR + "/" +
		// runtime.getKeyName());
		fs.delete(path, true);
		File dir = new File(localJarDir);
		String[] childs = null;
		if (!dir.isDirectory() || (childs = dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return StringUtils.endsWith(name, ".jar");
			}
		})).length < 1) {
			throw new IllegalStateException("dir:" + dir.getAbsolutePath() + " has not find any jars");
		}
		URI source = null;
		Path dest = null;
		for (String f : childs) {
			source = (new File(dir, f)).toURI();
			dest = new Path(path, f);
			fs.copyFromLocalFile(new Path(source), dest);
			libs.add(dest);
			logger.info("local:" + source + " have been copy to hdfs");
		}
		return libs;
	}

	public static void setJarsLibs(ContainerLaunchContext amContainer, List<Path> libs) {
		Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();
		for (Path l : libs) {
			localResources.put(l.getName(), setupAppJar(l));
		}
		amContainer.setLocalResources(localResources);
	}

	private static LocalResource setupAppJar(Path jarPath) {
		try {
			LocalResource localResource = Records.newRecord(LocalResource.class);
			FileStatus jarStat = getFileSystem().getFileStatus(jarPath);
			URL resURI = ConverterUtils.getYarnUrlFromPath(jarStat.getPath());
			logger.info("resurl:" + resURI);
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

	// private static FileSystem fileSystem;
	private static FileSystem getFileSystem() {
		// }
		return TISHdfsUtils.getFileSystem();
	}
}
