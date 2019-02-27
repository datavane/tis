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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.Records;

import com.google.common.base.Preconditions;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.realtime.TisIncrLauncher;
import com.qlangtech.tis.realtime.transfer.BasicTransferTool;
import com.qlangtech.tis.spring.LauncherResourceUtils.AppLauncherResource;

/*
 * 增量转发节点控制AM节点
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TransferNodeMaster extends BasicTransferTool implements AMRMClientAsync.CallbackHandler {

	public static final String ENVIRONMENT_EXEC_INDEXS = "exec_indexs";

	// public static final String ENVIRONMENT_INCR_STATUS_RPC_SERVER =
	// "incr_status_rpc_server";
	// 会启动两个子节点
	private final int numContainersToWaitFor;

	boolean isShutdown = false;

	private final List<ExecuteUnit> indexExecuteUnits = new ArrayList<ExecuteUnit>();

	private final Map<ContainerId, ExecuteUnit> allocatedTask = new HashMap<>();

	private final String incrExecGroupName;

	public TransferNodeMaster(String incrExecGroupName) {
		super();
		this.incrExecGroupName = incrExecGroupName;
		if (StringUtils.isBlank(incrExecGroupName)) {
			throw new IllegalArgumentException("param incrExecGroupName can not be null");
		}
		try {
			AppLauncherResource launcherResource = getLauncherResource();
			// List<String> indexNames = launcherResource.getIndexNames();
			// Collections.shuffle(indexNames);
			// master节点会监控兩個container
			// 将应用分成两组
			// System.out.println("all indexNames:" +
			// Utils.list2String(indexNames));
			// prepareExecuteIndexNames(indexNames, ODD);
			// prepareExecuteIndexNames(indexNames, EVEN);
			// this.numContainersToWaitFor = indexExecuteUnits.size();
			// 获得重新排序后的节点及其分布
			List<List<String>> lists = launcherResource.getIndexPartitionShuffle();
			for (List<String> list : lists) {
				indexExecuteUnits.add(new ExecuteUnit(list));
			}
			List<String> names = launcherResource.getIndexNames();
			if (names.isEmpty()) {
				throw new IllegalStateException("index names can not ben empty");
			}
			System.out.println("all indexNames:" + Utils.list2String(names));
			this.numContainersToWaitFor = indexExecuteUnits.size();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// private void prepareExecuteIndexNames(List<String> indexNames, int
	// oddOReven) {
	// List<String> indexList = getSubIndex(indexNames, oddOReven);
	// if (indexList.size() > 0) {
	// indexExecuteUnits.add(new ExecuteUnit(indexList));
	// }
	// }
	public static void main(String[] args) throws Exception {
		// 设置当前运行环境
		// TSearcherConfigFetcher.setConfigCenterHost(RunEnvironment.getSysEnvironment().getKeyName());
		// final String incrExecGroup = args[0];
		// Preconditions.checkNotNull(incrExecGroup, "param 'incrExecGroup' can
		// not be null");
		// System.setProperty(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP,
		// incrExecGroup);
		TransferNodeMaster master = new TransferNodeMaster("default");
		master.run();
	}

	private YarnConfiguration conf;

	private NMClient nmClient;

	// private IncrStatusUmbilicalProtocolImpl
	// incrStatusUmbilicalProtocolServer;
	// private InetSocketAddress incrStatusaddress;
	private AMRMClientAsync<ContainerRequest> rmClient;

	// private MasterLaunchONSListener masterStatusListener;
	private Configuration getConfiguration() {
		return this.conf;
	}

	public void run() throws Exception {
		// this.incrStatusUmbilicalProtocolServer = new
		// IncrStatusUmbilicalProtocolImpl();
		// getConfig();
		this.conf = new YarnConfiguration();
		//
		conf.addResource(FileUtils.openInputStream(new File(TransferStart.PATH_YARN_SITE)));
		this.nmClient = NMClient.createNMClient();
		this.nmClient.init(conf);
		this.nmClient.start();
		// this.masterStatusListener = new MasterLaunchONSListener(indexNames,
		// this.incrStatusUmbilicalProtocolServer);
		// this.masterStatusListener.start();
		// ==========================================================================
		rmClient = AMRMClientAsync.createAMRMClientAsync(1000, this);
		rmClient.init(getConfiguration());
		rmClient.start();
		rmClient.registerApplicationMaster("", 0, "");
		for (int i = 0; i < numContainersToWaitFor; i++) {
			applyNewContainer();
			System.out.println("send container request :" + i);
		}
		System.out.println("Waiting for " + numContainersToWaitFor + " containers to finish");
		synchronized (rmClient) {
			rmClient.wait();
		}
		System.out.println("master application shutdown.");
		try {
			rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
		} catch (Exception exc) {
			// safe to ignore ... this usually fails anyway
		}
	}

	/**
	 * 申请新的资源容器
	 */
	protected void applyNewContainer() {
		Priority priority = Records.newRecord(Priority.class);
		priority.setPriority(0);
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(getMemorySpec());
		capability.setVirtualCores((RunEnvironment.getSysEnvironment() == RunEnvironment.DAILY) ? 1 : 2);
		rmClient.addContainerRequest(new ContainerRequest(capability, null, null, priority));
	}

	protected int getMemorySpec() {
		return (int) (1024f * ((RunEnvironment.getSysEnvironment() == RunEnvironment.DAILY) ? 1.5f : 3f));
	}

	private static final int ODD = 1;

	private static final int EVEN = 0;

	private List<String> getSubIndex(List<String> indexNames, int oddOrEven) {
		List<String> result = new ArrayList<>();
		int index = 0;
		for (String name : indexNames) {
			if (((index++ % 2) == oddOrEven)) {
				result.add(name);
			}
		}
		return result;
	}

	public synchronized boolean doneWithContainers() {
		return isShutdown || numContainersToWaitFor <= 0;
	}

	public void onContainersCompleted(List<ContainerStatus> statuses) {
		System.out.println("onContainersCompleted execute");
		for (ContainerStatus s : statuses) {
			System.out.println(s.getContainerId().getContainerId() + ",status:" + s.getState() + ",exit_stat:"
					+ s.getExitStatus() + ",getDiagnostics:" + s.getDiagnostics());
			synchronized (this) {
				ExecuteUnit incrTask = allocatedTask.get(s.getContainerId());
				if (incrTask == null) {
					throw new IllegalStateException(
							"containerid:" + s.getContainerId() + " relevant incr task can not be null");
				}
				if (incrTask.allocated.compareAndSet(true, false)) {
					System.out.println(incrTask.getIndexNameSerialize() + " allocated status:true");
				} else {
					System.out.println(incrTask.getIndexNameSerialize() + " allocated status:false");
				}
				applyNewContainer();
			}
		}
	}

	@Override
	public synchronized void onContainersAllocated(List<Container> containers) {
		System.out.println("onContainersAllocated:" + containers.size());
		outter: for (Container container : containers) {
			ContainerId containerId = container.getId();
			ContainerLaunchContext ctx = Records.newRecord(ContainerLaunchContext.class);
			// ,RunEnvironment.getSysEnvironment()
			TransferStart.setJarsLibs(ctx,
					TransferStart.getLibPaths(this.incrExecGroupName, null, RunEnvironment.getSysEnvironment()));
			try {
				NodeId nodeId = container.getNodeId();
				System.out.println("=========containerId:" + containerId + ",node:" + nodeId.getHost());
				for (ExecuteUnit execUnit : indexExecuteUnits) {
					synchronized (execUnit) {
						if (!execUnit.allocated() && execUnit.hasIndex()) {
							Map<String, String> environment = new HashMap<String, String>();
							environment.put(ENVIRONMENT_EXEC_INDEXS, execUnit.getIndexNameSerialize());
							//environment.put(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP, incrExecGroupName);
							setEnvironment(environment, ctx, conf, true);
							ctx.setCommands(Collections.singletonList(TransferStart.JAVA_HOME_8 + "/bin/java "
									+ getRemoteDebugStr() + getRunningMemorySpec() + " -Druntime="
									+ RunEnvironment.getSysEnvironment().getKeyName() + " -DgroupName="
									+ incrExecGroupName + " com.qlangtech.tis.realtime.yarn.TransferIncrContainer" + " 1>"
									+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" + " 2>"
									+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"));
							if (execUnit.allocated.compareAndSet(false, true)) {
								allocatedTask.put(container.getId(), execUnit);
								nmClient.startContainer(container, ctx);
								System.out.println("start trigger launch event,index:"
										+ Utils.list2String(execUnit.indexNames) + ",containerId:" + container.getId());
								// waitLaunchCounter.countDown();
							}
							continue outter;
						}
					}
				}
				System.out.println("send msg to launcher the container");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected String getRunningMemorySpec() {
		int memory = (int) (getMemorySpec() * (0.85));
		return " -Xms" + memory + "m -Xmx" + memory + "m ";
	}

	public static String getRemoteDebugStr() {
		if (RunEnvironment.isOnlineMode()) {
			return StringUtils.EMPTY;
		}
		int port = 45687 + (int) (Math.random() * 10000);
		return !RunEnvironment.isOnlineMode() ? "-Xrunjdwp:transport=dt_socket,address=" + port + ",suspend=n,server=y"
				: StringUtils.EMPTY;
	}

	public static void setEnvironment(Map<String, String> environment, ContainerLaunchContext ctx,
			YarnConfiguration conf, boolean includeHadoopJars) throws IOException {
		Apps.addToEnvironment(environment, Environment.CLASSPATH.name(), Environment.PWD.$() + File.separator + "*",
				File.pathSeparator);
		if (includeHadoopJars) {
			// File.pathSeparator);
			for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
					YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
				Apps.addToEnvironment(environment, Environment.CLASSPATH.name(), c.trim(), File.pathSeparator);
			}
		}
		ctx.setEnvironment(environment);
		System.out.println("classpath:" + environment.get(Environment.CLASSPATH.name()));
	}

	public void onShutdownRequest() {
		System.out.println("onShutdownRequest");
		this.isShutdown = true;
	}

	public void onNodesUpdated(List<NodeReport> updatedNodes) {
		System.out.println("onNodesUpdated");
	}

	public float getProgress() {
		// System.out.println("getProgress");
		return 0;
	}

	public void onError(Throwable e) {
		e.printStackTrace();
	}

	private class ExecuteUnit {

		private final List<String> indexNames;

		private final AtomicBoolean allocated = new AtomicBoolean(false);

		public String getIndexNameSerialize() {
			StringBuffer result = new StringBuffer();
			int count = 0;
			for (String name : indexNames) {
				result.append(name);
				if (++count < indexNames.size()) {
					result.append(",");
				}
			}
			return result.toString();
		}

		public ExecuteUnit(List<String> indexNames) {
			this.indexNames = indexNames;
		}

		public boolean hasIndex() {
			return !indexNames.isEmpty();
		}

		public boolean allocated() {
			return allocated.get();
		}
	}
}
