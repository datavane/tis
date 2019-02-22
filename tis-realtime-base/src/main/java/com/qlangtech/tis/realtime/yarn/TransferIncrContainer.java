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

import static com.qlangtech.tis.realtime.transfer.MonitorSysTagMarker.addMetric;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.json.JSONArray;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.manage.common.GCJmxUtils;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.realtime.TisIncrLauncher;
import com.qlangtech.tis.realtime.transfer.BasicIncrStatusReport;
import com.qlangtech.tis.realtime.transfer.BasicONSListener;
import com.qlangtech.tis.realtime.transfer.BasicTransferTool;
import com.qlangtech.tis.realtime.transfer.CounterType;
import com.qlangtech.tis.realtime.transfer.IIncreaseCounter;
import com.qlangtech.tis.realtime.transfer.IOnsListenerStatus;
import com.qlangtech.tis.realtime.transfer.MonitorSysTagMarker;
import com.qlangtech.tis.realtime.transfer.TableSingleDataIndexStatus;
import com.qlangtech.tis.realtime.transfer.TransferStatusMBean;
import com.qlangtech.tis.realtime.utils.NetUtils;
import com.qlangtech.tis.realtime.yarn.rpc.ConsumeDataKeeper;
import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.realtime.yarn.rpc.JobType;
import com.qlangtech.tis.realtime.yarn.rpc.LaunchReportInfo;
import com.qlangtech.tis.realtime.yarn.rpc.MasterJob;
import com.qlangtech.tis.realtime.yarn.rpc.UpdateCounterMap;
import com.qlangtech.tis.solrj.util.ZkUtils;
import com.qlangtech.tis.trigger.zk.AbstractWatcher;

/*
 * 增量转发节点执行内容
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TransferIncrContainer extends BasicTransferTool {

	private static final String TABLE_CONSUME_COUNT = "tableConsumeCount";

	// private static final String logFlumeAddress;
	private static final Pattern ADDRESS_PATTERN = Pattern.compile("(.+?):(\\d+)$");

	// private static final ScheduledExecutorService falconSendScheduler =
	// Executors.newScheduledThreadPool(1);
	// private TransferStatusMBean mbean;
	private static final Joiner joinerWith = Joiner.on("_").skipNulls();

	static {
		// System.out.println(
		// "===============print system environment===============");
		// for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
		// System.out.print("=");
		// System.out.print(entry.getKey());
		// System.out.print(":");
		// System.out.println(entry.getValue());
		// }
		// System.out.println(
		// "======================================================");
		String execGroup = StringUtils.defaultIfEmpty(System.getenv(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP),
				System.getProperty(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP));
		if (StringUtils.isEmpty(execGroup)) {
			throw new IllegalArgumentException(
					"param " + TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP + " can not be null");
		}
		System.setProperty(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP, execGroup);
		// TSearcherConfigFetcher.setConfigCenterHost(RunEnvironment.getSysEnvironment().getKeyName());
		// logFlumeAddress = TSearcherConfigFetcher.get().getLogFlumeAddress();
		// if (StringUtils.isBlank(logFlumeAddress)) {
		// throw new IllegalArgumentException("logFlumeAddress can not be
		// null");
		// }
		// System.out.println(TSearcherConfigFetcher.CONFIG_LOG_FLUME_AGENT_ADDRESS
		// + ":" + logFlumeAddress);
		// System.setProperty(TSearcherConfigFetcher.CONFIG_LOG_FLUME_AGENT_ADDRESS,
		// logFlumeAddress);
	}

	private final List<String> execIndexs;

	private final String hostName;

	// private static final Logger logger =
	// LoggerFactory.getLogger(TransferIncrContainer.class);
	// 反馈执行状态RPC
	private IncrStatusUmbilicalProtocol incrStatusUmbilicalProtocol;

	private String collectionNames;

	private IncrStatusReportWorker statusReportWorker;

	public TransferIncrContainer(List<String> execIndexs) {
		super();
		this.execIndexs = execIndexs;
		this.hostName = NetUtils.getHostname();
	}

	public static void main(String[] args) throws Exception {
		final String execIndexs = System.getenv(TransferNodeMaster.ENVIRONMENT_EXEC_INDEXS);
		if (StringUtils.isBlank(execIndexs)) {
			throw new IllegalArgumentException(
					"environment param:" + TransferNodeMaster.ENVIRONMENT_EXEC_INDEXS + " can not be null");
		}
		final List<String> execIndexsList = Arrays.asList(StringUtils.split(execIndexs, ","));
		TransferIncrContainer incrContainer = new TransferIncrContainer(execIndexsList);
		incrContainer.start();
	}

	public void start() throws Exception {
		this.info("execIndexs:" + Utils.list2String(execIndexs));
		this.collectionNames = joinerWith
				.join(Iterators.transform(execIndexs.iterator(), input -> StringUtils.replace(input, "search4", "s4")));
		final TisZkClient zookeeper = BasicONSListener.getTisZkClient();
		this.startService(new HashSet<>(execIndexs));
		this.info("launch_rm_success");
		final List<IOnsListenerStatus> allChannels = getAllTransferChannel();
		connect2RemoteIncrStatusServer(zookeeper, true);
		TransferStatusMBean mbean = new TransferStatusMBean(this.getIndexNames(), allChannels);
		mbean.afterPropertiesSet();
		// 向小米监控系统发送监控消息
		// falconSendScheduler.scheduleAtFixedRate(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// sendStatus2Falcon(allChannels);
		// } catch (Throwable e) {
		// e.printStackTrace();
		// SendSMSUtils.send("err send falcon:" + e.getMessage(),
		// SendSMSUtils.BAISUI_PHONE);
		// }
		// }
		// }, MonitorSysTagMarker.FalconSendTimeStep,
		// MonitorSysTagMarker.FalconSendTimeStep, TimeUnit.SECONDS);
		statusReportWorker = new IncrStatusReportWorker(allChannels, mbean.getIndexUUID());
		statusReportWorker.run();
		this.info("launch transfer node success");
		// Thread worker = new Thread(statusReportWorker,
		// "incr-status-report-worker");
		// worker.start();
	}

	// private JSONObject addMetric(long timestamp, String collection, String
	// metricName, long value,
	// CounterType counterType, IIncreaseCounter counter) {
	// MonitorSysTagMarker tagMarker = null;
	// JSONObject o = new JSONObject();
	// if (counter != null && (tagMarker = counter.getMonitorTagMarker()) !=
	// null) {
	// o.put("metric", tagMarker.getFalconMetric());
	// } else {
	// o.put("metric", metricName);
	// }
	// o.put("endpoint", hostName);
	// o.put("timestamp", timestamp);
	// o.put("value", value);
	// o.put("step", MonitorSysTagMarker.FalconSendTimeStep);
	// // COUNTER or GAUGE
	// o.put("counterType", counterType.getValue());
	// String tags = "index=" + collection;
	// String tag = null;
	// if (tagMarker != null && (tag = tagMarker.getTags(metricName)) != null) {
	// tags += ("," + tag);
	// }
	// o.put("tags", tags);
	// return o;
	// }
	/**
	 * 发送节点状态到小米Falcon
	 */
	void sendStatus2Falcon(List<IOnsListenerStatus> allChannels) throws Exception {
		long timestamp = System.currentTimeMillis() / 1000;
		// 发送数据格式：https://book.open-falcon.org/zh/usage/data-push.html
		long fullgc = GCJmxUtils.getFullGC();
		long yonggc = GCJmxUtils.getYongGC();
		JSONArray result = new JSONArray();
		MonitorSysTagMarker tagMarker = null;
		for (IOnsListenerStatus status : allChannels) {
			for (Map.Entry<String, IIncreaseCounter> entry : status.getUpdateStatic()) {
				if ((tagMarker = entry.getValue().getMonitorTagMarker()) != null
						&& tagMarker.shallCollectByMonitorSystem()) {
					result.put(addMetric(hostName, timestamp, status.getCollectionName(), entry.getKey(),
							entry.getValue().getAccumulation(), CounterType.COUNTER, entry.getValue()));
				}
			}
			result.put(addMetric(hostName, timestamp, status.getCollectionName(), "tis_buf_remain",
					status.getBufferQueueRemainingCapacity(), CounterType.GAUGE));
			result.put(addMetric(hostName, timestamp, status.getCollectionName(), "tis_consume_err",
					status.getConsumeErrorCount(), CounterType.COUNTER));
			result.put(addMetric(hostName, timestamp, status.getCollectionName(), "tis_ignore",
					status.getIgnoreRowsCount(), CounterType.COUNTER));
			// ===========================================================
		}
		result.put(addMetric(hostName, timestamp, collectionNames, "incr_fullgc", fullgc, CounterType.COUNTER));
		result.put(addMetric(hostName, timestamp, collectionNames, "incr_yonggc", yonggc, CounterType.COUNTER));
		String content = result.toString();
		// System.out.println(content);
		HttpUtils.post(new URL("http://127.0.0.1:1988/v1/push"), content.getBytes(Charset.forName("utf8")),
				new PostFormStreamProcess<Object>() {

					@Override
					public Object p(int status, InputStream stream, String md5) {
						return null;
					}
				});
	}

	private void info(String msg) {
		System.out.println(msg);
	}

	private void error(String msg, Throwable e) {
		info("err:" + msg);
		if (e != null) {
			info(ExceptionUtils.getFullStackTrace(e));
		}
	}

	// 是否需要继续把状态汇报给assemble节点
	private void setDoReport(boolean doReport) {
		if (statusReportWorker != null) {
			statusReportWorker.setDoReport(doReport);
		}
	}

	// static String[] keys = new String[] { "order", "info", "tab2", "tab3",
	// IIncreaseCounter.SOLR_CONSUME_COUNT };
	private void connect2RemoteIncrStatusServer(final TisZkClient zookeeper, boolean reConnect) throws Exception {
		connect2RemoteIncrStatusServer(zookeeper, reConnect, 0);
	}

	/**
	 * 连接日志收集节点地址
	 *
	 * @param zookeeper
	 *            zookeeper client
	 * @param reConnect
	 *            是否需要重连
	 * @throws Exception
	 *             异常
	 */
	private void connect2RemoteIncrStatusServer(final TisZkClient zookeeper, boolean reConnect, int retryCount)
			throws Exception {
		final String incrStateCollect = "/tis/incr-transfer-group/incr-state-collect";
		try {
			// 增量状态收集节点
			final String incrStateCollectAddress = ZkUtils.getFirstChildValue(zookeeper, incrStateCollect,
					new AbstractWatcher() {

						@Override
						protected void process(Watcher watcher) throws KeeperException, InterruptedException {
							try {
								connect2RemoteIncrStatusServer(zookeeper, false);
							} catch (Exception e) {
								error(e.getMessage(), e);
							}
						}
					}, reConnect);
			InetSocketAddress address;
			Matcher matcher = ADDRESS_PATTERN.matcher(incrStateCollectAddress);
			if (matcher.matches()) {
				address = new InetSocketAddress(matcher.group(1), Integer.parseInt(matcher.group(2)));
			} else {
				setDoReport(false);
				throw new IllegalStateException("incrStatusRpcServer:" + incrStateCollectAddress
						+ " is not match the pattern:" + ADDRESS_PATTERN);
			}
			this.info("status server address:" + address);
			if (this.incrStatusUmbilicalProtocol != null) {
				RPC.stopProxy(this.incrStatusUmbilicalProtocol);
				this.incrStatusUmbilicalProtocol = null;
			}
			this.incrStatusUmbilicalProtocol = RPC.getProxy(IncrStatusUmbilicalProtocol.class,
					IncrStatusUmbilicalProtocol.versionID, address, new Configuration());
			this.info("successful connect to " + address + ",pingResult:" + this.incrStatusUmbilicalProtocol.ping());
			// 向服务端注册自己监听的topic及topic下的tag信息
			this.incrStatusUmbilicalProtocol.nodeLaunchReport(new LaunchReportInfo(this.collectionFocusTopicInfo));
			setDoReport(true);
		} catch (Throwable e) {
			if (retryCount < 5) {
				this.info("warn!!! retry to connect " + e.getMessage());
				Thread.sleep(1000);
				connect2RemoteIncrStatusServer(zookeeper, reConnect, retryCount + 1);
				return;
			} else {
				this.error(e.getMessage(), e);
				setDoReport(false);
			}
		}
	}

	private class IncrStatusReportWorker extends BasicIncrStatusReport {

		private final String hostName;

		private final Map<String, String> indexUUID;

		private boolean doReport = true;

		IncrStatusReportWorker(Collection<IOnsListenerStatus> incrChannels, Map<String, String> indexUUID) {
			super(incrChannels);
			this.hostName = NetUtils.getHostname();
			this.indexUUID = indexUUID;
		}

		private void setDoReport(boolean doReport) {
			this.doReport = doReport;
		}

		@Override
		protected void processSnapshot() throws Exception {
			if (!doReport) {
				return;
			}
			UpdateCounterMap updateCounterMap = new UpdateCounterMap();
			updateCounterMap.setGcCounter(BasicONSListener.getGarbageCollectionCount());
			updateCounterMap.setFrom(hostName);
			long currentTimeInSec = ConsumeDataKeeper.getCurrentTimeInSec();
			updateCounterMap.setUpdateTime(currentTimeInSec);
			// 汇总一个节点中所有索引的增量信息
			for (IOnsListenerStatus l : incrChannels) {
				TableSingleDataIndexStatus tableUpdateCounter = new TableSingleDataIndexStatus();
				tableUpdateCounter.setBufferQueueRemainingCapacity(l.getBufferQueueRemainingCapacity());
				tableUpdateCounter.setBufferQueueUsedSize(l.getBufferQueueUsedSize());
				tableUpdateCounter.setConsumeErrorCount((int) l.getConsumeErrorCount());
				tableUpdateCounter.setIgnoreRowsCount((int) l.getIgnoreRowsCount());
				tableUpdateCounter.setUUID(this.indexUUID.get(l.getCollectionName()));
				tableUpdateCounter.setTis30sAvgRT(((BasicONSListener) l).getTis30sAvgRT());
				// 汇总一个索引中所有focus table的增量信息
				for (Map.Entry<String, IIncreaseCounter> entry : l.getUpdateStatic()) {
					// IncrCounter tableIncrCounter = new
					// IncrCounter((int)entry.getValue().getIncreasePastLast());
					// tableIncrCounter.setAccumulationCount(entry.getValue().getAccumulation());
					// tableUpdateCounter.put(entry.getKey(), tableIncrCounter);
					// 只记录一个消费总量和当前时间
					tableUpdateCounter.put(entry.getKey(), entry.getValue().getAccumulation());
				}
				tableUpdateCounter.put(TABLE_CONSUME_COUNT, ((BasicONSListener) l).getTableConsumeCount());
				updateCounterMap.addTableCounter(l.getCollectionName(), tableUpdateCounter);
			}
			IncrStatusUmbilicalProtocol remote;
			if ((remote = incrStatusUmbilicalProtocol) != null) {
				MasterJob masterJob = remote.reportStatus(updateCounterMap);
				// 接收主节点发送回来的消息
				if (masterJob != null) {
					for (IOnsListenerStatus l : incrChannels) {
						if ((masterJob.getJobType() == JobType.IndexJobRunning)
								&& StringUtils.equals(l.getCollectionName(), masterJob.getIndexName())) {
							if (masterJob.isStop()) {
								l.pauseConsume();
							} else {
								l.resumeConsume();
							}
							info("index receive a command:" + masterJob.getJobType() + ",index:"
									+ masterJob.getIndexName() + ",stop:" + masterJob.isStop());
							return;
						}
					}
					error("index receive a command can not match any listener:" + masterJob.getJobType() + ",index:"
							+ masterJob.getIndexName() + ",stop:" + masterJob.isStop(), null);
				}
			}
		}
	}
}
