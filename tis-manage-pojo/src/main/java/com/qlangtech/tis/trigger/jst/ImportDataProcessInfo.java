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

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.qlangtech.tis.common.LuceneVersion;

/*
 * 共享区处理完成信息之后需要向弹内发送直接结果，以执行后续流程
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ImportDataProcessInfo implements Serializable {

	public static final String KEY_DELIMITER = "split_char";

	public static final String DELIMITER_001 = "char001";

	public static final String DELIMITER_TAB = "tab";

	private static final long serialVersionUID = 1L;

	private final Integer taskId;

	private String indexName;

	private String indexBuilder;

	// 远程任务是否要启动暂停，等到debug连接，该参数在测试时候使用
	private boolean remoteDebugSuspend = false;

	// 编译索引使用的Lucene版本
	private LuceneVersion luceneVersion;

	private String timepoint;

	private String buildTableTitleItems;

	private String hdfsdelimiter;

	private Integer sessionNum;

	// 如果使用的是hive数据源地址直接是由hive 之后之后的结果路径指定的
	private HdfsSourcePathCreator hdfsSourcePathCreator;

	// exec type dump,create ,update
	private String execType;

	// 根据数据量预估出分多少组
	private int estimateGroupNum;

	/**
	 * 导入数据条数
	 */
	private Long dumpCount;

	public ImportDataProcessInfo(Integer taskId) {
		super();
		this.taskId = taskId;
	}

	private ConcurrentHashMap<Integer, AtomicInteger> sharedReplicBackflowOrder = new ConcurrentHashMap<>();

	/**
	 * 当同一个shared的两个副本同步回流的时候
	 * ，由于两个副本会同时切全量，导致客戶端在短時間內不可用，所以在Assemble節點上触发回流的时候根据先后顺序，<br>
	 * 当第二个副本回流的时候需要让引擎在回流的时候sleep一下， 这样在索引回流的时候可以做到至少有一个副本可用<br>
	 * 2017/12/27
	 *
	 * @param group
	 * @return
	 */
	public boolean indexBackFlowShallSleep(int group) {
		AtomicInteger order = sharedReplicBackflowOrder.get(group);
		if (order == null) {
			order = new AtomicInteger();
			AtomicInteger tmp = sharedReplicBackflowOrder.putIfAbsent(group, order);
			if (tmp != null) {
				order = tmp;
			}
		}
		return order.getAndIncrement() == 1;
	}

	public static String createIndexDir(HDFSRootDir rootDir, String timePoint, String groupNum, String serviceName,
			boolean isSourceDir) {
		return rootDir.path + "/" + serviceName + "/all/" + groupNum + (!isSourceDir ? "/output" : StringUtils.EMPTY)
				+ "/" + timePoint;
	}

	public static class HDFSRootDir {
		private final String path;

		public HDFSRootDir(String path) {
			super();
			this.path = path;
		}

		public String getPath() {
			return path;
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	public String getIndexBuilder() {
		return indexBuilder;
	}

	public void setIndexBuilder(String indexBuilder) {
		this.indexBuilder = indexBuilder;
	}

	public LuceneVersion getLuceneVersion() {
		return luceneVersion;
	}

	public void setLuceneVersion(LuceneVersion luceneVersion) {
		this.luceneVersion = luceneVersion;
	}

	public String getIndexBuildOutputPath(HDFSRootDir root, int groupIndex) {
		return createIndexDir(root, this.timepoint, String.valueOf(groupIndex), this.getIndexName(), false);
	}

	public String getHdfsdelimiter() {
		return hdfsdelimiter;
	}

	public void setHdfsdelimiter(String hdfsdelimiter) {
		this.hdfsdelimiter = hdfsdelimiter;
	}

	public HdfsSourcePathCreator getHdfsSourcePath() {
		return hdfsSourcePathCreator;
	}

	public void setHdfsSourcePathCreator(HdfsSourcePathCreator hdfsSourcePath) {
		this.hdfsSourcePathCreator = hdfsSourcePath;
	}

	public String getBuildTableTitleItems() {
		return buildTableTitleItems;
	}

	public void setBuildTableTitleItems(String buildTableTitleItems) {
		this.buildTableTitleItems = buildTableTitleItems;
	}

	public int getEstimateGroupNum() {
		return estimateGroupNum;
	}

	public void setEstimateGroupNum(int estimateGroupNum) {
		this.estimateGroupNum = estimateGroupNum;
	}

	public String getExecType() {
		return execType;
	}

	public void setExecType(String execType) {
		this.execType = execType;
	}

	public Long getDumpCount() {
		return dumpCount;
	}

	public void setDumpCount(Long maxDumpCount) {
		this.dumpCount = maxDumpCount;
	}

	public Integer getSessionNum() {
		return sessionNum;
	}

	public void setSessionNum(Integer sessionNum) {
		this.sessionNum = sessionNum;
	}

	public String getTimepoint() {
		return timepoint;
	}

	public void setTimepoint(String timepoint) {
		this.timepoint = timepoint;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public boolean isRemoteDebugSuspend() {
		return this.remoteDebugSuspend;
	}

	public void setRemoteDebugSuspend(boolean remoteDebugSuspend) {
		this.remoteDebugSuspend = remoteDebugSuspend;
	}

	public static abstract class HdfsSourcePathCreator {
		public final String name;

		public HdfsSourcePathCreator(String name) {
			this.name = name;
		}

		//source hdfs path
		public abstract String build(String group);

		public int getGroupSize(FileSystem hdfs) throws Exception {

			int groupIndex = 0;
			while (true) {
				if (!hdfs.exists(new Path(build(String.valueOf(groupIndex++))))) {
					break;
				}
			}
			return groupIndex - 1;
		}
	}
}
