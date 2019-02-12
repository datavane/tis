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
package com.qlangtech.tis.fullbuild.indexbuild.impl;

// import com.taobao.terminator.trigger.jst.ImportDataProcessInfo.LuceneVersion;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.common.LuceneVersion;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTriggerFactory;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.pubhook.common.ConfigConstant;
import com.qlangtech.tis.trigger.feedback.DistributeLog;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;
import com.taobao.terminator.build.job.JobConf;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Hadoop020RemoteJobTriggerFactory implements IRemoteJobTriggerFactory {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultRemoteJobTrigger.class);

	public Hadoop020RemoteJobTriggerFactory() {
		super();
	}

	public static void main(String[] args) throws Exception {
	}

	@Override
	public IRemoteJobTrigger createBuildJob(DistributeLog log, String timePoint, String indexName, String username,
			String groupNum, ImportDataProcessInfo state, TaskContext context) throws Exception {
		final String coreName = state.getIndexName() + '-' + groupNum;
		TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
		JobConf jobConf = new JobConf(false);
		jobConf.set("indexing.sourcefsname", config.getHdfsAddress());
		jobConf.set("indexing.buildtabletitleitems", state.getBuildTableTitleItems());
		String outPath = ImportDataProcessInfo.createIndexDir(username, timePoint, groupNum, indexName, false);
		jobConf.set("indexing.outputpath", outPath);
		String hdfsSourcePath = state.getHdfsSourcePath() == null
				? ImportDataProcessInfo.createIndexDir(username, timePoint, groupNum, indexName, true)
				: state.getHdfsSourcePath().build(groupNum);
		jobConf.set("indexing.sourcepath", hdfsSourcePath);
		jobConf.set("indexing." + ConfigConstant.FILE_CORE_PROPERTIES,
				"/user/" + username + "/" + coreName + "/core/" + ConfigConstant.FILE_CORE_PROPERTIES);
		String schemaPath = "/user/" + username + "/" + coreName + "/schema/schema.xml";
		jobConf.set("indexing.schemapath", schemaPath);
		jobConf.set("indexing.servicename", indexName);
		jobConf.set("indexing.corename", coreName);
		jobConf.setInt("indexing.maxNumSegments", 1);
		jobConf.set("indexing.username", username);
		jobConf.set("indexing.incrtime", state.getTimepoint());
		jobConf.set("indexing.groupnum", groupNum);
		if (StringUtils.isNotBlank(state.getHdfsdelimiter())) {
			jobConf.set("indexing.delimiter", state.getHdfsdelimiter());
		}
		jobConf.set("job.name", coreName + "-indexBuildJob");
		jobConf.setAtLeastMemoryMb(300);
		jobConf.setAtLeastSpaceMb(1024);
		String indexBuilder = StringUtils.defaultIfEmpty(state.getIndexBuilder(),
				"com.qlangtech.tis.indexbuilder.map.HdfsIndexBuilder");
		jobConf.set("task.map.class", "com.qlangtech.tis.indexbuilder.map.HdfsIndexGetConfig," + indexBuilder
				+ ",com.qlangtech.tis.indexbuilder.map.HdfsIndexDeliver");
		jobConf.set("task.jar.transfer", "false,false,false");
		String appcontext = "/user/" + username + "/" + coreName + "/app/applicationContext.xml";
		jobConf.set("indexing.configpath", appcontext);
		if (// RunEnvironment.isOnlineMode()
		state.getLuceneVersion() == LuceneVersion.LUCENE_7) // &&
		// UISVersion.isDataCenterCollection(state.getIndexName())
		{
			LOG.info("collection:" + state.getIndexName() + " use solr6.0");
			jobConf.set("job.jarfile", "indexbuilder7.6,indexbuilder7.6,indexbuilder7.6");
		} else {
			LOG.info("collection:" + state.getIndexName() + " use solr5.3");
			jobConf.set("job.jarfile", "indexbuilder5.3,indexbuilder5.3,indexbuilder5.3");
		}
		if (state.getDumpCount() != null) {
			jobConf.set("indexing.recordlimit", String.valueOf(state.getDumpCount()));
			log.addLog(state, "has set record limit:" + String.valueOf(state.getDumpCount()));
		}
		jobConf.set("indexing.sourcetype", "HDFS");
		jobConf.set("job.priority", "HIGH");
		LOG.warn("Excute Remote Dump Job Status: Sbumit");
		return createRemoteJob(jobConf);
	}

	/**
	 * @param jobConf
	 * @return
	 */
	protected IRemoteJobTrigger createRemoteJob(JobConf jobConf) {
		IRemoteJobTrigger remoteJobTrigger = new DefaultRemoteJobTrigger(jobConf);
		return remoteJobTrigger;
	}

	@Override
	public IRemoteJobTrigger createDumpJob(String indexName, String starttime, TaskContext context) throws Exception {
		JobConf jobConf = new JobConf(false);
		if (StringUtils.isEmpty(starttime)) {
			throw new IllegalArgumentException("starttime can not be null");
		}
		jobConf.set("job.name", indexName + "-FullDumpJob");
		jobConf.set("dumpstarttime", starttime);
		jobConf.set("job.jarfile", "tsearcher-dumpcenter,tsearcher-dumpcenter,tsearcher-dumpcenter");
		final String execTaskClass = "com.dfire.tis.order.dump.biz." + indexName + ".TableDumpTask";
		jobConf.set("task.map.class", "com.taobao.terminator.build.task.impl.MockTaskMap," + execTaskClass
				+ ",com.taobao.terminator.build.task.impl.MockTaskMap");
		LOG.info("execTaskClass:" + execTaskClass);
		jobConf.set("task.jar.transfer", "false,false,false");
		return createRemoteJob(jobConf);
	}
	// public void updateStatus(DumpJobStatus status, JobStatus.State state) {
	// if (state == state.FAILED) {
	// status.setRunState(status.FAILED);
	// }
	// if (state == state.KILLED) {
	// status.setRunState(status.KILLED);
	// }
	// if (state == state.PREP) {
	// status.setRunState(status.PREP);
	// }
	// if (state == state.RUNNING) {
	// status.setRunState(status.RUNNING);
	// }
	// if (state == state.SUCCEEDED) {
	// status.setRunState(status.SUCCEEDED);
	// }
	// if (state == state.WAITING) {
	// status.setRunState(status.SUCCEEDED);
	// }
	// }
}
