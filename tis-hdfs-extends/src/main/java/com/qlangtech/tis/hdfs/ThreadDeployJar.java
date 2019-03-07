
package com.qlangtech.tis.hdfs;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.fs.Path;

import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.yarn.common.YarnConstant;

/**
 * @author 百岁
 *
 * @date 2016年6月17日
 */
public class ThreadDeployJar implements Runnable {

	private final CommandLine commandLine;

	public ThreadDeployJar(CommandLine commandLine) {
		super();
		this.commandLine = commandLine;
	}

	@Override
	public void run() {
		try {
			RunEnvironment runtime = RunEnvironment.getEnum(commandLine.getOptionValue(RunEnvironment.KEY_RUNTIME));
			// TSearcherConfigFetcher.setConfigCenterHost(runtime.getKeyName());
			System.setProperty(RunEnvironment.KEY_RUNTIME, runtime.getKeyName());
			String localJarDir = commandLine.getOptionValue(YarnConstant.PARAM_OPTION_LOCAL_JAR_DIR);

			final Path dest = new Path(
					YarnConstant.HDFS_GROUP_LIB_DIR + YarnConstant.INDEX_BUILD_JAR_DIR + '/' + runtime.getKeyName());
			TISHdfsUtils.copyLibs2Hdfs(localJarDir, dest);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
