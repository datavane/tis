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
package com.qlangtech.tis.order.center;

import static org.apache.commons.io.FileUtils.ONE_KB_BI;
import static org.apache.commons.io.FileUtils.ONE_MB_BI;
import static org.apache.solr.common.cloud.ZkStateReader.BASE_URL_PROP;
import static org.apache.solr.common.cloud.ZkStateReader.CORE_NAME_PROP;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.params.CommonAdminParams;
import org.apache.solr.common.params.CoreAdminParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.HttpUtils.ProcessResponse;
import com.qlangtech.tis.trigger.jst.AbstractIndexBuildJob.BuildResult;

/*
 * 索引回流控制器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexBackflowManager {

	private static final Logger logger = LoggerFactory.getLogger(IndexBackflowManager.class);

	int phrase = 0;

	private static final Logger log = LoggerFactory.getLogger(IndexBackflowManager.class);

	public static final long STATUS_GET_INTERVAL = 5000;

	private static final ConcurrentHashMap<String, ReentrantLock> /*
																	 * nodename,
																	 * example:
																	 * 10.1.5.
																	 * 19
																	 */
	nodeLockMap = new ConcurrentHashMap<>();

	private final ExecutorService backFlowExecutor;

	private final DocCollection collection;

	private Map<String, ConcurrentLinkedQueue<BuildResult>> /*
															 * nodename,example:
															 * 10.1.5.19
															 */
	nodeBackflowLock;

	private final Map<String, List<Replica>> /* shardName,example:shard1 */
	shardMap;

	private final int nodeSize;

	// private static final Pattern NODE_PATTERN = Pattern
	// .compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):\\d+_solr");
	private final CountDownLatch replicaCountDown;

	private boolean executeSuccess = true;

	public boolean isExecuteSuccess() {
		return this.executeSuccess;
	}

	public void await() {
		try {
			replicaCountDown.await(10, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Replica> getReplicByShard(int shard) {
		final String shardName = "shard" + (shard + 1);
		List<Replica> replics = shardMap.get(shardName);
		if (replics == null || replics.size() < 1) {
			throw new IllegalStateException(shardName + " relevant replics is null");
		}
		return replics;
	}

	public IndexBackflowManager(final DocCollection collection) {
		this.collection = collection;
		final Map<String, List<Replica>> /* shardName,example:shard1 */
		shardMap = new HashMap<>();
		final Map<String, ConcurrentLinkedQueue<BuildResult>> /*
																 * nodename,
																 * example:10.1.
																 * 5.19
																 */
		nodeBackflowLock = new HashMap<>();
		List<Replica> nodeReplics = null;
		ConcurrentLinkedQueue<BuildResult> lock = null;
		int replicaCount = 0;
		for (Slice slice : collection.getSlices()) {
			nodeReplics = shardMap.get(slice.getName());
			if (nodeReplics == null) {
				nodeReplics = new ArrayList<>();
				shardMap.put(slice.getName(), nodeReplics);
			}
			for (Replica replica : slice.getReplicas()) {
				replicaCount++;
				lock = nodeBackflowLock.get(replica.getNodeName());
				if (lock == null) {
					lock = new ConcurrentLinkedQueue<BuildResult>();
					nodeBackflowLock.put(replica.getNodeName(), lock);
				}
				nodeReplics.add(replica);
			}
		}
		this.replicaCountDown = new CountDownLatch(replicaCount);
		this.nodeSize = nodeBackflowLock.keySet().size();
		this.backFlowExecutor = Executors.newFixedThreadPool(nodeSize);
		this.nodeBackflowLock = Collections.unmodifiableMap(nodeBackflowLock);
		this.shardMap = Collections.unmodifiableMap(shardMap);
	}

	/**
	 * 遍历所有的副本
	 *
	 * @param action
	 */
	public void vistAllReplica(Consumer<Replica> action) {
		shardMap.values().forEach((a) -> {
			a.forEach(action);
		});
	}

	/**
	 * group size
	 *
	 * @return
	 */
	public int getGroupSize() {
		return this.shardMap.keySet().size();
	}

	public void addBackFlowTask(BuildResult buildResult) {
		ConcurrentLinkedQueue<BuildResult> backflowQueue = this.nodeBackflowLock.get(buildResult.getNodeName());
		if (backflowQueue == null) {
			throw new IllegalStateException(
					"node:" + buildResult.getNodeName() + " relevant backflow queue can not be null");
		}
		backflowQueue.offer(buildResult);
	}

	public void startSwapClusterIndex(String userName, final int taskid) throws Exception {
		for (final Map.Entry<String, ConcurrentLinkedQueue<BuildResult>> /*
																			 * nodename
																			 * ,
																			 * example
																			 * :
																			 * 10
																			 * .
																			 * 1
																			 * .
																			 * 5
																			 * .
																			 * 19
																			 */
		entry : nodeBackflowLock.entrySet()) {
			this.backFlowExecutor.execute(() -> {
				MDC.put("app", collection.getName());
				while (true) {
					BuildResult buildResult = entry.getValue().poll();
					if (buildResult == null) {
						if (replicaCountDown.getCount() < 1) {
							// 说明任务已经全部结束需要退出了
							return;
						}
						try {
							Thread.sleep(3000);
						} catch (Throwable e) {
						}
						continue;
					}
					ReentrantLock lock = nodeLockMap.get(entry.getKey());
					if (lock == null) {
						lock = new ReentrantLock();
						ReentrantLock tmp = nodeLockMap.putIfAbsent(entry.getKey(), lock);
						if (tmp != null) {
							lock = tmp;
						}
					}
					try {
						log.info("node:" + entry.getKey() + " gain the lock");
						lock.lockInterruptibly();
						BackflowResult backflowResult = triggerIndexBackflow(buildResult,
								Long.parseLong(buildResult.getTimepoint()), userName, taskid);
						if (backflowResult.isSuccess()) {
							this.replicaCountDown.countDown();
						} else {
							shortCircuit();
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						shortCircuit();
						throw new RuntimeException(e);
					} finally {
						lock.unlock();
					}
				}
			});
		}
	}

	public void shortCircuit() {
		this.executeSuccess = false;
		// 说明有一组回流失败了，需要短路任务执行
		while (this.replicaCountDown.getCount() > 0) {
			this.replicaCountDown.countDown();
		}
	}

	/**
	 * 触发索引回流
	 *
	 * @param replica
	 * @param timestamp
	 * @param userName
	 * @param taskid
	 * @return
	 * @throws Exception
	 */
	private BackflowResult triggerIndexBackflow(BuildResult buildResult, long timestamp, String userName,
			final int taskid) throws Exception {
		final Replica replica = buildResult.getReplica();
		final long coreReloadSleepTime = buildResult.getCoreReloadSleepTime();
		final String requestId = taskid + "_p_" + (phrase++);
		if (replica == null) {
			throw new IllegalArgumentException("replica can not be null");
		}
		log.info("start " + replica.getCoreUrl() + " index back,size:"
				+ FileUtils.byteCountToDisplaySize(buildResult.getIndexSize()) + ",coreReloadSleepTime:"
				+ coreReloadSleepTime);
		URL url = new URL(
				replica.getStr(BASE_URL_PROP) + "/admin/cores?action=CREATEALIAS&execaction=swapindexfile&core="
						+ replica.getStr(CORE_NAME_PROP) + "&property.hdfs_timestamp=" + timestamp
						+ "&property.hdfs_user=" + userName + "&" + CommonAdminParams.ASYNC + "=" + requestId
						+ "&property.core_reload_sleep_time=" + (coreReloadSleepTime > 0 ? coreReloadSleepTime : 0));
		log.debug("apply swap index url :" + url);
		BackflowResult result = HttpUtils.processContent(url, new StreamProcess<BackflowResult>() {

			@Override
			public BackflowResult p(int status, InputStream stream, String md5) {
				BackflowResult result = new BackflowResult();
				try {

					if (!HttpUtils.processResponse(stream, (err) -> {
						result.setSTATUS(BackflowResult.FAILED);
						result.setMsg(err);
					}).success) {
						return result;
					}
					// log.info(IOUtils.toString(stream,
					// Charset.forName("utf8")));
					URL url = new URL(replica.getStr(BASE_URL_PROP) + "/admin/cores?action=requeststatus&wt=json&"
							+ CoreAdminParams.REQUESTID + "=" + requestId);
					log.info("check url :" + url);
					return getCallbackResult(replica, url);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				return result;
			}
		}, 5);
		return result;
	}

	// 将近两个小时
	private static final int MAX_RETRY = 3000;

	/**
	 * 轮询是否回流完成
	 *
	 * @param replica
	 * @param url
	 * @return
	 */
	private BackflowResult getCallbackResult(Replica replica, URL url) {
		int applyCount = 0;
		BackflowResult callbackResult = null;
		IndexflowbackStatus preStatus = null;
		while (applyCount++ < MAX_RETRY) {
			callbackResult = HttpUtils.processContent(url, new StreamProcess<BackflowResult>() {

				@SuppressWarnings("all")
				@Override
				public BackflowResult p(int status, InputStream stream, String md5) {
					BackflowResult callbackResult = new BackflowResult();
					// try {
					ProcessResponse result = null;

					result = HttpUtils.processResponse(stream, (err) -> {
						callbackResult.msg = err;
					});

					logger.info("respBody:\n" + result.respBody);

					if (!result.success) {
						callbackResult.setSTATUS(BackflowResult.FAILED);
						return callbackResult;
					}

					Map<String, Object> resultMap = (Map<String, Object>) result.result;
					callbackResult.setSTATUS(String.valueOf(resultMap.get("STATUS")));
					// String body = IOUtils.toString(stream,
					// Charset.forName("utf8"));
					//
					// callbackResult = (BackflowResult) JSON.parseObject(body,
					// BackflowResult.class);
					// callbackResult.setResponseBody(body);
					// } catch (IOException e) {
					// throw new RuntimeException(e);
					// }
					return callbackResult;
				}
			}, 10);
			if (callbackResult.isFaild()) {
				log.error(replica.getCoreUrl() + ",index back faild:" + callbackResult.getMsg() + "\n body:"
						+ callbackResult.getResponseBody());
				return callbackResult;
			}
			if (!callbackResult.isSuccess()) {
				try {
					Thread.sleep(STATUS_GET_INTERVAL);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				log.info("waitting index flowback " + replica.getStr(CORE_NAME_PROP) + ","
						+ callbackResult.getCopyStatus(preStatus) + "retry count:" + applyCount + ",remain:"
						+ this.replicaCountDown.getCount() + "nodes");
				preStatus = callbackResult.getIndexflowback_status();
				continue;
			}
			return callbackResult;
		}
		if (applyCount >= MAX_RETRY) {
			log.error(replica.getStr(CORE_NAME_PROP) + " index back faild:exceed the max retry count " + MAX_RETRY);
		}
		return callbackResult;
	}

	public static class BackflowResult {

		public static String RUNNING = "running";

		public static String COMPLETED = "completed";

		public static String FAILED = "failed";

		public static String NOT_FOUND = "notfound";

		private boolean result = false;

		private String responseBody;

		private String msg;

		private String STATUS;

		private String trace;

		private IndexflowbackStatus indexflowback_status = new IndexflowbackStatus();

		public IndexflowbackStatus getIndexflowback_status() {
			return indexflowback_status;
		}

		public void setIndexflowback_status(IndexflowbackStatus indexflowback_status) {
			this.indexflowback_status = indexflowback_status;
		}

		/**
		 * 打印索引回流進度狀態，1.已经读到的，2.总共有的字节，3.百分比，4.读取速率（每秒）
		 *
		 * @param preStatus
		 * @return
		 */
		public String getCopyStatus(IndexflowbackStatus preStatus) {
			if (indexflowback_status.all < 1) {
				return StringUtils.EMPTY;
			}
			StringBuffer summary = new StringBuffer(
					byteCountToDisplaySize(BigInteger.valueOf(indexflowback_status.readed)));
			summary.append("/");
			summary.append(byteCountToDisplaySize(BigInteger.valueOf(indexflowback_status.all))).append("(");
			summary.append((int) ((((double) indexflowback_status.readed) / indexflowback_status.all) * 100));
			summary.append("%");
			if (preStatus != null) {
				summary.append(",speed:");
				summary.append(byteCountToDisplaySize(BigInteger
						.valueOf((indexflowback_status.readed - preStatus.readed) * 1000 / STATUS_GET_INTERVAL)));
				summary.append("/s");
			}
			return summary.append(")").toString();
		}

		private static String byteCountToDisplaySize(final BigInteger size) {
			String displaySize;
			if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
				displaySize = String.valueOf(size.divide(ONE_MB_BI)) + "MB";
			} else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
				displaySize = String.valueOf(size.divide(ONE_KB_BI)) + "KB";
			} else {
				displaySize = String.valueOf(size) + " bytes";
			}
			return displaySize;
		}

		public boolean isFaild() {
			if (FAILED.equalsIgnoreCase(STATUS) || NOT_FOUND.equalsIgnoreCase(STATUS)) {
				return true;
			}
			return false;
		}

		public String getResponseBody() {
			return responseBody;
		}

		public void setResponseBody(String responseBody) {
			this.responseBody = responseBody;
		}

		public String getTrace() {
			return trace;
		}

		public void setTrace(String trace) {
			this.trace = trace;
		}

		public boolean isSuccess() {
			return COMPLETED.equalsIgnoreCase(STATUS);
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getSTATUS() {
			return STATUS;
		}

		public void setSTATUS(String sTATUS) {
			STATUS = sTATUS;
		}

		@Override
		public String toString() {
			return "status:" + getSTATUS() + ",msg:" + getMsg();
		}
	}

	public static class IndexflowbackStatus {

		private long all;

		private long readed;

		public long getAll() {
			return all;
		}

		public void setAll(long all) {
			this.all = all;
		}

		public long getReaded() {
			return readed;
		}

		public void setReaded(long readed) {
			this.readed = readed;
		}
	}

	public static void main(String[] args) throws Exception {
		// IndexBackflowManager backflow = new IndexBackflowManager();
		// BackflowResult result = backflow.getCallbackResult(null, new URL(
		// "http://10.1.7.42:8983/solr/admin/cores?action=requeststatus&requestid=123&wt=json"));
		//
		// System.out.println(result.getMsg() + " " + result.getSTATUS() +
		// result.isFaild() + " "
		// + result.isSuccess());
	}
}
