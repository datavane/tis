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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.Server;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.lucene.util.Version;
import org.apache.solr.common.cloud.OnReconnect;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.common.LuceneVersion;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.exec.AbstractActionInvocation;
import com.qlangtech.tis.exec.ActionInvocation;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.exec.lifecycle.hook.IIndexBuildLifeCycleHook;
import com.qlangtech.tis.exec.lifecycle.hook.impl.AdapterIndexBuildLifeCycleHook;
import com.qlangtech.tis.flume.FlumeApplication;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.SendSMSUtils;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.realtime.transfer.IOnsListenerStatus;
import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.realtime.yarn.rpc.impl.IncrStatusUmbilicalProtocolImpl;
import com.qlangtech.tis.realtime.yarn.rpc.impl.MasterListenerStatus;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import com.qlangtech.tis.solrj.util.ZkUtils;
import com.qlangtech.tis.spring.LauncherResourceUtils;
import com.qlangtech.tis.spring.LauncherResourceUtils.AppLauncherResource;
import com.qlangtech.tis.trigger.jst.LogCollector;
import com.qlangtech.tis.trigger.netty.TriggerLogServer;
import com.qlangtech.tis.trigger.zk.AbstractWatcher;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexSwapTaskflowLauncher implements // ApplicationContextAware,
		Daemon, ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(IndexSwapTaskflowLauncher.class);

	private static final DocumentBuilder solrConfigDocumentbuilder;

	static {
		try {
			DocumentBuilderFactory solrConfigBuilderFactory = DocumentBuilderFactory.newInstance();
			solrConfigBuilderFactory.setValidating(false);
			solrConfigDocumentbuilder = solrConfigBuilderFactory.newDocumentBuilder();
			solrConfigDocumentbuilder.setEntityResolver(new EntityResolver() {

				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					InputSource source = new InputSource();
					source.setCharacterStream(new StringReader(""));
					return source;
				}
			});
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	// private static FileSystem fileSystem;
	public static final String KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER = "IndexSwapTaskflowLauncher";

	public static IndexSwapTaskflowLauncher getIndexSwapTaskflowLauncher(ServletContext context) {
		IndexSwapTaskflowLauncher result = (IndexSwapTaskflowLauncher) context
				.getAttribute(KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER);
		if (result == null) {
			throw new IllegalStateException("IndexSwapTaskflowLauncher can not be null in servletContext");
		}
		return result;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

	private TisZkClient zkClient;

	ZkStateReader zkStateReader;

	private List<String> indexs;

	private Collection<IOnsListenerStatus> incrChannels;

	private TriggerLogServer logServer;

	public Collection<IOnsListenerStatus> getIncrChannels() {
		return incrChannels;
	}

	public List<String> getIndexs() {
		return this.indexs;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		AbstractTisCloudSolrClient.initHashcodeRouter();
		try {
			afterPropertiesSet();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		sce.getServletContext().setAttribute(KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER, this);
	}

	// @Override
	public void afterPropertiesSet() throws Exception {
		this.zkClient = new TisZkClient(TSearcherConfigFetcher.get().getZkAddress(), 60000);
		this.zkStateReader = new ZkStateReader(zkClient.getZK());
		this.zkStateReader.createClusterStateWatchersAndUpdate();
		this.incrChannels = initIncrTransferStateCollect();
		// final int port = TriggerJobManage.JETTY_TRIGGER_SERVER_PORT;
		// 分布式环境中日志收集器
		this.logServer = new TriggerLogServer(LogCollector.LOG_COLLECTOR_SOCKET_PORT_56789);
		// logger.info("export trigger port:" + port);
		Set<String> /* collection name */
		includes = Collections.emptySet();
		AppLauncherResource launcherResource = LauncherResourceUtils.getAppResource(includes,
				"classpath*:com/qlangtech/tis/assemble/search4*/join.xml");
		indexs = Collections.unmodifiableList(launcherResource.getIndexNames());
		FlumeApplication.startFlume();
		logger.info("success start flume service");
	}

	private IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtocolServer;

	public IncrStatusUmbilicalProtocolImpl getIncrStatusUmbilicalProtocol() {
		if (incrStatusUmbilicalProtocolServer == null) {
			throw new IllegalStateException("incrStatusUmbilicalProtocolServer can not be null");
		}
		return this.incrStatusUmbilicalProtocolServer;
	}

	// 发布增量集群任务收集器
	private Collection<IOnsListenerStatus> initIncrTransferStateCollect() throws Exception {
		final List<IOnsListenerStatus> result = new ArrayList<>();
		if (!AssembleConfig.shallRobTheAssembleRoleLock()) {
			logger.info("disable.assemble.role is true ,so will not gain the zk lock");
			return result;
		}
		logger.info("start assemble control/(incr logging collect)");
		this.incrStatusUmbilicalProtocolServer = new IncrStatusUmbilicalProtocolImpl();
		final int exportPort = NetUtils.getFreeSocketPort();
		// getConfig();
		YarnConfiguration conf = new YarnConfiguration();
		// 状态收集服务端
		Server server = new RPC.Builder(conf).setProtocol(IncrStatusUmbilicalProtocol.class)
				.setInstance(incrStatusUmbilicalProtocolServer).setBindAddress("0.0.0.0").setPort(exportPort)
				.setNumHandlers(2).setVerbose(false).build();
		server.start();
		logger.info("incr log collection server started,server port:" + exportPort);
		// this.incrStatusaddress = server.getListenerAddress();
		Collection<IOnsListenerStatus> incrChannels = getAllTransferChannel(result);
		zkClient.addOnReconnect(new OnReconnect() {

			@Override
			public void command() {
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
				}
				getAllTransferChannel(result);
			}
		});
		String zkpath = "/tis/incr-transfer-group/incr-state-collect";
		String registIp = ZkUtils.registerAddress2ZK(this.zkClient, zkpath, exportPort);
		logger.info("register local ip :" + registIp + ",zkpath:" + zkpath);
		this.incrStatusUmbilicalProtocolServer.startLogging();
		return incrChannels;
	}

	/**
	 * 需要启动去抢夺assemble的锁吗？
	 *
	 * @return
	 * @throws IOException
	 */
	// public static boolean shallRobTheAssembleRoleLock() throws IOException {
	// String dataDir = System.getProperty("data.dir");
	// if (StringUtils.isEmpty(dataDir)) {
	// throw new IllegalStateException("sys prop 'data.dir' is null");
	// }
	// File f = new File(dataDir, "assemble.properties");
	// if (f.exists()) {
	// try (InputStream input = FileUtils.openInputStream(f)) {
	// Properties prop = new Properties();
	// prop.load(input);
	// if ("true".equalsIgnoreCase(prop.getProperty("disable.assemble.role"))) {
	// return false;
	// }
	// }
	// }
	//
	// return true;
	// }
	private List<String> indexNames;

	public List<String> getIndexNames() {
		List<String> result = null;
		try {
			int retry = 0;
			while ((result = indexNames) == null && (retry++) < 5) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (result == null) {
			throw new IllegalStateException("index name can not be null");
		}
		return result;
	}

	public List<IOnsListenerStatus> getAllTransferChannel(final List<IOnsListenerStatus> result) {
		// List<String> indexNames;
		try {
			this.indexNames = zkClient.getChildren("/collections", new AbstractWatcher() {

				@Override
				protected void process(Watcher watcher) throws KeeperException, InterruptedException {
					Thread.sleep(60000);
					getAllTransferChannel(result);
				}
			}, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Set<String> exist = new HashSet<String>();
		String collectionName = null;
		Iterator<IOnsListenerStatus> it = result.iterator();
		while (it.hasNext()) {
			collectionName = it.next().getCollectionName();
			if (!indexNames.contains(collectionName)) {
				it.remove();
			}
			exist.add(collectionName);
		}
		MasterListenerStatus listenerStatus = null;
		for (String indexName : indexNames) {
			if (exist.contains(indexName)) {
				continue;
			}
			listenerStatus = new MasterListenerStatus(indexName, this.incrStatusUmbilicalProtocolServer);
			result.add(listenerStatus);
		}
		return result;
	}

	public static final String COMPONENT_START = "component.start";

	public static final String COMPONENT_END = "component.end";

	/**
	 * 由servlet接收到命令之后触发
	 *
	 * @param execContext
	 * @throws Exception
	 */
	@SuppressWarnings("all")
	public void startWork(IParamContext execContext) throws Exception {
		ActionInvocation invoke = null;
		DefaultChainContext chainContext = new DefaultChainContext(execContext);
		String start = chainContext.getString(COMPONENT_START);
		String end = chainContext.getString(COMPONENT_END);
		logger.info("start component:" + start + ",end component:" + end);
		chainContext.setFileSystem(TISHdfsUtils.getFileSystem());
		chainContext.setZkClient(zkClient);
		chainContext.setZkStateReader(zkStateReader);
		TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
		SnapshotDomain domain = HttpConfigFileReader.getResource(config.getTisConsoleHostAddress(),
				chainContext.getIndexName(), config.getRuntime(), ConfigFileReader.FILE_SCHEMA,
				ConfigFileReader.FILE_SOLOR);
		SolrFieldsParser solrFieldsParser = new SolrFieldsParser();
		ParseResult schemaParseResult;
		try (ByteArrayInputStream reader = new ByteArrayInputStream(ConfigFileReader.FILE_SCHEMA.getContent(domain))) {
			schemaParseResult = solrFieldsParser.parseSchema(reader, false);
		}
		chainContext.setIndexMetaData(new IIndexMetaData() {

			@Override
			public ParseResult getSchemaParseResult() {
				return schemaParseResult;
			}

			@Override
			public IIndexBuildLifeCycleHook getIndexBuildLifeCycleHook() {
				return AdapterIndexBuildLifeCycleHook.create(schemaParseResult);
			}

			@Override
			public LuceneVersion getLuceneVersion() {
				Version ver = getTISLuceneVersion(domain);
				if (ver.equals(Version.LUCENE_7_6_0)) {
					return LuceneVersion.LUCENE_7;
				}

				throw new IllegalStateException("illegal version ver:" + ver);
			}
		});
		invoke = AbstractActionInvocation.createExecChain(chainContext);
		ExecuteResult execResult = invoke.invoke();
		if (!execResult.isSuccess()) {
			logger.warn(execResult.getMessage());
			SendSMSUtils.send("[ERR]fulbud:" + chainContext.getIndexName() + " falid," + execResult.getMessage(),
					SendSMSUtils.BAISUI_PHONE);
		}
		// InputStream configFileStream = null;
		//
		// final String user = System.getProperty("user.name");
		//
		// final SimpleDateFormat dataFormat = new SimpleDateFormat(
		// "yyyyMMddHHmmss");
		// final Date startTime = new Date();
		// final String ps = dataFormat.format(startTime);
		// logger.info("ps:" + ps + ", user:" + user + ",indexname:" +
		// indexName);
		//
		// // ▼▼▼▼ dump task
		// final IRemoteJobTrigger dumpTask =
		// RemoteBuildCenterUtils.remoteJobTriggerFactory
		// .createDumpJob(ps, new TaskContext());
		// RunningStatus runningStatus = startDump(startTime, dumpTask);
		// if (!runningStatus.isSuccess()) {
		// logger.error("Dump job is faild");
		// return;
		// }
		// // ▲▲▲▲
		// logger.info("dump phrase success");
		//
		// StringBuffer tabs = new StringBuffer();
		// for (String tab : TABELS) {
		// tabs.append(tab).append(",");
		// }
		// // ▼▼▼▼ bind hive table task
		// logger.info("start hive table bind,tabs:" + tabs.toString());
		// // Dump 任务结束,开始绑定hive partition
		// HiveTableBuilder hiveTableBuilder = new HiveTableBuilder(ps, user);
		// hiveTableBuilder.setHiveDbHeler(hiveDbHeler);
		// hiveTableBuilder.setFileSystem(this.getDistributeFileSystem());
		// hiveTableBuilder.bindHiveTables(new HashSet<String>(Arrays
		// .asList(TABELS)));
		// // ▲▲▲▲
		//
		// // ▼▼▼▼ 执行打宽表
		// TaskConfigParser parse = new TaskConfigParser();
		// HiveTaskFactory hiveTaskFactory = new HiveTaskFactory();
		// parse.setTaskFactory(hiveTaskFactory);
		//
		// try {
		// configFileStream = Thread.currentThread().getContextClassLoader()
		// .getResourceAsStream("orderinfo_join.xml");
		// parse.startJoinSubTables(configFileStream, ps);
		//
		// } finally {
		// IOUtils.closeQuietly(configFileStream);
		// }
		// // ▲▲▲▲
		//
		// // ▼▼▼▼ 触发索引构建
		// final HdfsSourcePathCreator pathCreator = new HdfsSourcePathCreator()
		// {
		// String hdfsPath = "/user/hive/db/totalpay_summary/pt=%s/pmod=%s";
		//
		// @Override
		// public String build(String group) {
		// return String.format(hdfsPath, ps, group);
		// }
		// };
		// final int groupSize = getGroupSize(indexName, pathCreator);
		// if (!triggerIndexBuildJob(indexName, ps, groupSize, pathCreator)) {
		// logger.info("index build faild,ps:" + ps + ",groupsize:"
		// + groupSize);
		// return;
		// }
		// // ▲▲▲▲
		//
		// // ▼▼▼▼ 索引回流
		// List<JMXConnector> jmxConns = pauseIncrFlow();
		// DocCollection collection = zkStateReader.getClusterState()
		// .getCollection(indexName);
		// if (collection == null) {
		// throw new IllegalStateException("indexName:" + indexName
		// + " collection can not be null in solr cluster");
		// }
		// try {
		// // << 回流索引 开始>>
		// int taskid = 1233 + (int) (Math.random() * 10000);
		// indexBackflowManager.startSwapClusterIndex(collection,
		// Long.parseLong(ps), user, taskid);
		// logger.info("all node feedback successful,ps:" + ps);
		// } finally {
		// resumeIncrFlow(jmxConns);
		// }
		// // ▲▲▲▲
	}

	@SuppressWarnings("all")
	private Version getTISLuceneVersion(SnapshotDomain domain) {
		try {
			// =getLuceneVersion===============================================================
			Version luceneVersion = Version.LUCENE_7_6_0;
			byte[] solrConfigContent = ConfigFileReader.FILE_SOLOR.getContent(domain);
			ByteArrayInputStream solrReader = new ByteArrayInputStream(solrConfigContent);
			try {
				Document document = solrConfigDocumentbuilder.parse(solrReader);
				Node luceneMatchVersionNode = (Node) SolrFieldsParser.createXPath()
						.evaluate("config/luceneMatchVersion", document, XPathConstants.NODE);
				if (luceneMatchVersionNode != null) {
					luceneVersion = Version.parse(luceneMatchVersionNode.getTextContent());
				}
			} finally {
				IOUtils.closeQuietly(solrReader);
			}
			logger.info("luceneMatchVersionNode:{}", luceneVersion);
			// ================================================================================
			return luceneVersion;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ///daemon/////////////////===========================================
	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
	}

	@Override
	public void start() throws Exception {
		afterPropertiesSet();
		logger.info("index Swap Task ready");
	}

	public static void main(String[] arg) throws Exception {
		IndexSwapTaskflowLauncher launcher = new IndexSwapTaskflowLauncher();
		launcher.start();
		synchronized (launcher) {
			launcher.wait();
		}
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public void destroy() {
	}
}
