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
package com.qlangtech.tis.trigger.jst;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.ZkStateReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.cloud.dump.DumpJobId;
import com.qlangtech.tis.cloud.dump.DumpJobStatus;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.exception.IndexDumpFatalException;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.hdfs.util.Constants;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.common.TISCollectionUtils;
import com.qlangtech.tis.manage.common.TISCollectionUtils.TisCoreName;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.trigger.feedback.DistributeLog;
import com.qlangtech.tis.trigger.jst.AbstractIndexBuildJob.BuildResult;
import com.qlangtech.tis.trigger.jst.impl.RemoteIndexBuildJob;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractIndexBuildJob implements Callable<BuildResult> {
	// public static final Pattern PATTERN_CORE =
	// Pattern.compile("search4(.+?)_shard(\\d+?)_replica_n(\\d+?)");
	protected final String userName;

	protected DistributeLog log;

	private static final Logger logger = LoggerFactory.getLogger(RemoteIndexBuildJob.class);

	protected static final FileSystem fileSystem;

	protected final String groupNum;

	static int jobid = 0;

	static {
		fileSystem = createFileSystem(TSearcherConfigFetcher.get().getHdfsAddress());
	}

	public AbstractIndexBuildJob(// ,
			ImportDataProcessInfo processInfo, // ,
			int group, // ExecutorService taskPool,
			String userName) {
		// System.getProperty("user.name");
		this.userName = userName;
		this.state = processInfo;
		if (StringUtils.isEmpty(processInfo.getTimepoint())) {
			throw new IllegalArgumentException("processInfo.getTimepoint() can not be null");
		}
		this.groupNum = String.valueOf(group);
		// this.taskPool = taskPool;
	}

	public DistributeLog getLog() {
		return log;
	}

	public void setLog(DistributeLog log) {
		this.log = log;
	}

	public String getUserName() {
		return userName;
	}

	public String getGroupNum() {
		return groupNum;
	}

	public static void main(String[] arg) throws Exception {
	}

	// private static final Log LOG =
	// LogFactory.getLog(RemoteIndexBuildJob.class);
	private final TSearcherConfigFetcher config = TSearcherConfigFetcher.get();

	public static interface SCallback {

		public void execute(ImportDataProcessInfo state, BuildResult buildResult);
	}

	protected final ImportDataProcessInfo state;

	public BuildResult call() throws Exception {
		try {
			return startBuildIndex();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 执行单组build任務
	 *
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("all")
	public final BuildResult startBuildIndex() throws Exception {
		final String coreName = state.getIndexName() + '-' + groupNum;
		final String timePoint = state.getTimepoint();
		final DumpJobStatus status = new DumpJobStatus();
		status.setUserName(userName);
		status.setTimepoint(state.getTimepoint());
		status.setDumpType("remote");
		DumpJobId dumpJobId = new DumpJobId("jtIdentifier", jobid++);
		status.setDumpJobID(dumpJobId);
		status.setCoreName(coreName);
		// RunEnvironment.getEnum(config.getRunEnvironment());
		RunEnvironment runtime = config.getRuntime();
		long now = System.currentTimeMillis();
		final String outPath = state.getIndexBuildOutputPath(this.userName, Integer.parseInt(this.groupNum));
		logger.info("build out path:" + outPath);
		SnapshotDomain domain = HttpConfigFileReader.getResource(config.getTerminatorConsoleHostAddress(),
				state.getIndexName(), 0, runtime, ConfigFileReader.FILE_SCHEMA, ConfigFileReader.FILE_SOLOR);
		if (domain == null) {
			throw new IllegalStateException(
					"index:" + state.getIndexName() + ",runtime:" + runtime + " have not prepare for confg");
		}
		writeResource2Hdfs(coreName, domain, ConfigFileReader.FILE_SCHEMA, "config");
		writeResource2Hdfs(coreName, domain, ConfigFileReader.FILE_SOLOR, "config");
		// writeResource2Hdfs(coreName, domain,
		// ConfigFileReader.FILE_CORE_PROPERTIES, "config");
		// TODO 为了兼容老的索引先加上，到时候要删除掉的
		writeResource2Hdfs(coreName, domain, ConfigFileReader.FILE_SCHEMA, Constants.SCHEMA);
		//writeResource2Hdfs(coreName, domain, ConfigFileReader.FILE_APPLICATION, "app");
		// writeResource2Hdfs(coreName, domain,
		// ConfigFileReader.FILE_CORE_PROPERTIES, "core");
		// TODO 为了兼容老的索引先加上，到时候要删除掉的 end
		logger.info("Excute  RemoteDumpJob: Sbumit Remote Job .....  ");
		status.setStartTime(now);
		// String[] core = this.coreName.split("-");
		String serviceName = state.getIndexName();
		// ///////////////////////////////////////////
		logger.info("Excute Remote Dump Job Status: Sbumit  ");
		return buildSliceIndex(coreName, timePoint, status, outPath, serviceName);
	}

	protected abstract BuildResult buildSliceIndex(final String coreName, final String timePoint,
			final DumpJobStatus status, final String outPath, String serviceName)
			throws Exception, IOException, InterruptedException;

	public static class BuildResult {

		public static BuildResult createFaild() {
			BuildResult buildResult = new BuildResult(Integer.MAX_VALUE, new ImportDataProcessInfo(0));
			return buildResult.setSuccess(false);
		}

		public static BuildResult clone(BuildResult from) {
			BuildResult buildResult = new BuildResult(from.groupIndex, from.processInfo);
			buildResult.setSuccess(true).setIndexSize(from.indexSize);
			return buildResult;
		}

		private Replica replica;

		public Replica getReplica() {
			return replica;
		}

		public final String getNodeName() {
			return this.replica.getNodeName();
		}

		// 执行索引回流时需要sleep的时间
		public long getCoreReloadSleepTime() {
			boolean shallSleep = processInfo.indexBackFlowShallSleep(groupIndex);
			if (shallSleep) {
				return (long) (Math.log10(1 + this.getIndexSize()) * 12000);
			} else {
				return -1l;
			}
		}

		public static void main(String[] args) {
			System.out.println((long) (Math.log10(1 + 319l * 1024 * 1024) * 12000));
		}

		public BuildResult setReplica(Replica replica) {
			this.replica = replica;
			return this;
		}

		// private final RunningJob rj;
		private boolean success;

		private final ImportDataProcessInfo processInfo;

		public String getTimepoint() {
			return this.processInfo.getTimepoint();
		}

		public boolean isSuccess() {
			return success;
		}

		public BuildResult setSuccess(boolean success) {
			this.success = success;
			return this;
		}

		private final int groupIndex;

		// 索引磁盘容量
		private long indexSize;

		public long getIndexSize() {
			return indexSize;
		}

		public void setIndexSize(long indexSize) {
			this.indexSize = indexSize;
		}

		public int getGroupIndex() {
			return groupIndex;
		}

		public BuildResult(Replica replica, ImportDataProcessInfo processInfo) {
			super();
			String coreName = replica.getStr(ZkStateReader.CORE_NAME_PROP);
			// Matcher matcher = TisCoreAdminHandler..matcher(coreName);
			// if (!matcher.matches()) {
			// throw new IllegalStateException("coreName:" + coreName + " is not
			// match the pattern:" + PATTERN_CORE);
			// }

			TisCoreName tiscoreName = TISCollectionUtils.parse(coreName);

			this.groupIndex = tiscoreName.getSharedNo() - 1;
			this.processInfo = processInfo;
		}

		public BuildResult(int group, ImportDataProcessInfo processInfo) {
			super();
			this.groupIndex = group;
			this.processInfo = processInfo;
		}

		public String getHdfsSourcePath() {
			return this.processInfo.getHdfsSourcePath().build(String.valueOf(groupIndex));
		}
	}

	public static FileSystem createFileSystem(String hdfsHost) {
		return TISHdfsUtils.getFileSystem();
	}

	/**
	 * @param config
	 * @param coreName
	 * @param domain
	 * @return
	 * @throws IndexDumpFatalException
	 */
	private void writeResource2Hdfs(String coreName, SnapshotDomain domain, PropteryGetter getter, String subdir)
			throws IndexDumpFatalException {
		Path dst = new Path(config.getHdfsAddress() + Path.SEPARATOR + Constants.USER + Path.SEPARATOR + userName
				+ Path.SEPARATOR + coreName + Path.SEPARATOR + subdir + Path.SEPARATOR + getter.getFileName());
		OutputStream dstoutput = null;
		try {
			dstoutput = fileSystem.create(dst, true);
			IOUtils.write(getter.getContent(domain), dstoutput);
		} catch (IOException e1) {
			throw new IndexDumpFatalException("[ERROR] Submit Service Core  Schema.xml to HDFS Failure !!!!", e1);
		} finally {
			IOUtils.closeQuietly(dstoutput);
		}
	}
}
