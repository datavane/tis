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

import java.util.List;

import org.apache.solr.common.cloud.DocCollection;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.exec.ActionInvocation;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.IExecuteInterceptor;
import com.qlangtech.tis.order.center.IndexBackflowManager;
import com.qlangtech.tis.trigger.jst.AbstractIndexBuildJob.BuildResult;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo.HDFSRootDir;
import com.qlangtech.tis.trigger.jst.impl.RemoteIndexBuildJob;
import com.tis.zookeeper.ZkPathUtils;

/*
 * 索引回流处理流程
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexBackFlowInterceptor implements IExecuteInterceptor {

	// private static final int INCR_NODE_PORT = 9998;
	private static final Logger logger = LoggerFactory.getLogger(IndexBackFlowInterceptor.class);

	public static final String NAME = "indexBackflow";

	/**
	 */
	public IndexBackFlowInterceptor() {
		super();
		// this.remoteIncrControl = new DefaultRemoteIncrControl();
	}

	@Override
	public ExecuteResult intercept(ActionInvocation invocation) throws Exception {
		logger.info("component:" + NAME + " start execute");
		IExecChainContext context = invocation.getContext();
		TisZkClient zookeeper = context.getZkClient();
		// 在回流索引的時候需要写一个标记位到zk中,这样监控在发起抱紧之前判断是否在回流索引,如回流索引的话直接退出了
		final String zkBackIndexSignalPath = createZkSignalToken(context, zookeeper);
		final String user = context.getContextUserName();
		try {
			// << 回流索引 开始>>
			int taskid = 1233 + (int) (Math.random() * 10000);
			IndexBackflowManager indexBackFlowQueue = null;
			if (IndexBuildInterceptor.isPropagateFromIndexBuild(context)) {
				indexBackFlowQueue = IndexBuildInterceptor.getIndeBackFlowQueue(context);
			} else {
				DocCollection collection = context.getZkStateReader().getClusterState()
						.getCollection(context.getIndexName());
				IndexBackflowManager indexBackFlowQueueTmp = new IndexBackflowManager(collection);
				ImportDataProcessInfo state = new ImportDataProcessInfo(taskid);
				state.setTimepoint(context.getPartitionTimestamp());
				state.setIndexName(context.getIndexName());
				indexBackFlowQueueTmp.vistAllReplica((replic) -> {
					BuildResult buildResult = new BuildResult(replic, state);
					buildResult.setSuccess(true);
					buildResult.setReplica(replic);
					// 取得当前文件夹的size
					String hdfsPath = state.getIndexBuildOutputPath(
							new HDFSRootDir(TSearcherConfigFetcher.get().getHDFSRootDir()),
							buildResult.getGroupIndex());
					buildResult.setIndexSize(
							RemoteIndexBuildJob.getSizeHdfsDir(context.getDistributeFileSystem(), hdfsPath));
					indexBackFlowQueueTmp.addBackFlowTask(buildResult);
				});
				indexBackFlowQueue = indexBackFlowQueueTmp;
			}
			indexBackFlowQueue.startSwapClusterIndex(user, taskid);
			indexBackFlowQueue.await();
			if (!indexBackFlowQueue.isExecuteSuccess()) {
				// 失败了
				ExecuteResult faild = ExecuteResult.createFaild();
				faild.setMessage("indexBackFlowQueue.isExecuteSuccess() is false");
				return faild;
			}
			logger.info("all node feedback successful,ps:" + context.getPartitionTimestamp());
		} finally {
			removeZkSignal(zookeeper, zkBackIndexSignalPath);
			// 重新开启增量执行
			// resumeIncrFlow(jmxConns, context.getIndexName());
		}
		return invocation.invoke();
	}

	protected String createZkSignalToken(IExecChainContext context, TisZkClient zookeeper)
			throws KeeperException, InterruptedException {
		final String zkBackIndexSignalPath = ZkPathUtils.getIndexBackflowSignalPath(context.getIndexName());
		if (!zookeeper.exists(zkBackIndexSignalPath, true)) {
			zookeeper.create(zkBackIndexSignalPath, "".getBytes(), CreateMode.PERSISTENT, true);
		}
		zookeeper.create(zkBackIndexSignalPath + "/" + ZkPathUtils.INDEX_BACKFLOW_SIGNAL_PATH_SEQNODE_NAME,
				String.valueOf(System.currentTimeMillis()).getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL, true);
		return zkBackIndexSignalPath;
	}

	private void removeZkSignal(TisZkClient zookeeper, final String zkBackIndexSignalPath)
			throws KeeperException, InterruptedException {
		try {
			List<String> children = zookeeper.getChildren(zkBackIndexSignalPath, null, true);
			Stat stat = new Stat();
			for (String c : children) {
				zookeeper.getData(zkBackIndexSignalPath + "/" + c, null, stat, true);
				zookeeper.delete(zkBackIndexSignalPath + "/" + c, stat.getVersion(), true);
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}
}
