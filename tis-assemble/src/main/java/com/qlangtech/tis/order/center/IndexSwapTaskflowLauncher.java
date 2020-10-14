/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.order.center;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.*;
import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.exec.impl.DummyIndexMetaData;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.flume.FlumeApplication;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.*;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.realtime.transfer.IOnsListenerStatus;
import com.qlangtech.tis.realtime.utils.NetUtils;
import com.qlangtech.tis.realtime.yarn.rpc.impl.MasterListenerStatus;
import com.qlangtech.tis.rpc.server.FullBuildStatCollectorServer;
import com.qlangtech.tis.rpc.server.IncrStatusServer;
import com.qlangtech.tis.rpc.server.IncrStatusUmbilicalProtocolImpl;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import com.qlangtech.tis.solrj.util.ZkUtils;
import com.qlangtech.tis.trigger.zk.AbstractWatcher;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年11月5日 下午6:57:19
 */
public class IndexSwapTaskflowLauncher implements Daemon, ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(IndexSwapTaskflowLauncher.class);

    // private static FileSystem fileSystem;
    public static final String KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER = "IndexSwapTaskflowLauncher";

    public static IndexSwapTaskflowLauncher getIndexSwapTaskflowLauncher(ServletContext context) {
        IndexSwapTaskflowLauncher result = (IndexSwapTaskflowLauncher) context.getAttribute(KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER);
        if (result == null) {
            throw new IllegalStateException("IndexSwapTaskflowLauncher can not be null in servletContext");
        }
        return result;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    // private static final String indexName = "search4totalpay";
    // private JettyTISRunner jetty;
    // private HdfsRealTimeTerminatorBean totalpayBean;
    // private IRemoteIncrControl remoteIncrControl;
    private TisZkClient zkClient;

    ZkStateReader zkStateReader;

    public void setZkClient(TisZkClient zkClient) {
        this.zkClient = zkClient;
    }

    public void setZkStateReader(ZkStateReader zkStateReader) {
        this.zkStateReader = zkStateReader;
    }

    // private List<String> indexs;
    private Collection<IOnsListenerStatus> incrChannels;

    // private TriggerLogServer logServer;
    public Collection<IOnsListenerStatus> getIncrChannels() {
        return incrChannels;
    }

    // public List<String> getIndexs() {
    // return this.indexs;
    // }
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AbstractTisCloudSolrClient.initHashcodeRouter();
        // 构建各阶段持久化
        BasicPhaseStatus.statusWriter = new BasicPhaseStatus.IFlush2Local() {

            @Override
            public void write(File localFile, BasicPhaseStatus status) throws Exception {
                XmlFile xmlFile = new XmlFile(localFile);
                xmlFile.write(status, Collections.emptySet());
            }

            @Override
            public BasicPhaseStatus loadPhase(File localFile) throws Exception {
                XmlFile xmlFile = new XmlFile(localFile);
                return (BasicPhaseStatus) xmlFile.read();
            }
        };
        try {
            this.afterPropertiesSet();
            this.incrChannels = initIncrTransferStateCollect();
            FlumeApplication.startFlume();
            sce.getServletContext().setAttribute(KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @Override
    public void afterPropertiesSet() throws Exception {
        try {
            this.setZkClient(new TisZkClient(Config.getZKHost(), 60000));
        } catch (Exception e) {
            throw new RuntimeException("ZKHost:" + Config.getZKHost(), e);
        }
        // 当初始集群初始化的时候assemble先与solr启动时不执行createClusterZkNodes会出错
        ZkController.createClusterZkNodes(this.zkClient.getZK());
        ZkStateReader zkStateReader = new ZkStateReader(zkClient.getZK());
        zkStateReader.createClusterStateWatchersAndUpdate();
        this.setZkStateReader(zkStateReader);
    }

    private IncrStatusServer incrStatusServer;

    public IncrStatusServer getIncrStatusUmbilicalProtocol() {
        if (incrStatusServer == null) {
            throw new IllegalStateException("incrStatusUmbilicalProtocolServer can not be null");
        }
        return this.incrStatusServer;
    }

    // 发布增量集群任务收集器
    private Collection<IOnsListenerStatus> initIncrTransferStateCollect() throws Exception {
        // this.incrStatusUmbilicalProtocolServer = new IncrStatusUmbilicalProtocolImpl();
        final int exportPort = NetUtils.getFreeSocketPort();
        incrStatusServer = new IncrStatusServer(exportPort);
        incrStatusServer.addService(IncrStatusUmbilicalProtocolImpl.getInstance());
        incrStatusServer.addService(FullBuildStatCollectorServer.getInstance());
        incrStatusServer.start();
        final List<IOnsListenerStatus> result = new ArrayList<>();
        Collection<IOnsListenerStatus> incrChannels = getAllTransferChannel(result);
        zkClient.addOnReconnect(() -> {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
            }
            getAllTransferChannel(result);
        });
        // "/tis/incr-transfer-group/incr-state-collect"
        ZkUtils.registerAddress2ZK(// "/tis/incr-transfer-group/incr-state-collect"
                this.zkClient, // "/tis/incr-transfer-group/incr-state-collect"
                ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PATH, exportPort);
        IncrStatusUmbilicalProtocolImpl.getInstance().startLogging();
        return incrChannels;
    }

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
        try {
            this.indexNames = zkClient.getChildren("/collections", new AbstractWatcher() {

                @Override
                protected void process(Watcher watcher) throws KeeperException, InterruptedException {
                    Thread.sleep(3000);
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
            listenerStatus = new MasterListenerStatus(indexName, IncrStatusUmbilicalProtocolImpl.getInstance());
            result.add(listenerStatus);
        }
        return result;
    }

    // private ITISFileSystemFactory indexBuildFileSystem;

    /**
     * 由servlet接收到命令之后触发
     *
     * @param execContext
     * @throws Exception
     */
    @SuppressWarnings("all")
    public ExecuteResult startWork(DefaultChainContext chainContext) throws Exception {
        chainContext.rebindLoggingMDCParams();
        ActionInvocation invoke = null;
        ExecutePhaseRange range = chainContext.getExecutePhaseRange();
        // String start = chainContext.getString(IFullBuildContext.COMPONENT_START);
        // String end = chainContext.getString(IFullBuildContext.COMPONENT_END);
        logger.info("start component:" + range.getStart() + ",end component:" + range.getEnd());
        chainContext.setZkClient(zkClient);
        chainContext.setZkStateReader(zkStateReader);
        Objects.requireNonNull(chainContext.getIndexBuildFileSystem(), "IndexBuildFileSystem of chainContext can not be null");
        Objects.requireNonNull(chainContext.getFlatTableBuilder(), "FlatTableBuilder of chainContext can not be null");
        Objects.requireNonNull(chainContext.getTableDumpFactory(), "tableDumpFactory of chainContext can not be null");
        chainContext.setIndexMetaData(createIndexMetaData(chainContext));
        invoke = AbstractActionInvocation.createExecChain(chainContext);
        ExecuteResult execResult = invoke.invoke();
        if (!execResult.isSuccess()) {
            logger.warn(execResult.getMessage());
            SendSMSUtils.send("[ERR]fulbud:" + chainContext.getIndexName() + " falid," + execResult.getMessage(), SendSMSUtils.BAISUI_PHONE);
        }
        return execResult;
    }

    private IIndexMetaData createIndexMetaData(DefaultChainContext chainContext) throws Exception {
        if (!chainContext.hasIndexName()) {
            return new DummyIndexMetaData();
        }
        SnapshotDomain domain = HttpConfigFileReader.getResource(chainContext.getIndexName(), RunEnvironment.getSysRuntime(), ConfigFileReader.FILE_SCHEMA);
        return SolrFieldsParser.parse(() -> {
            return ConfigFileReader.FILE_SCHEMA.getContent(domain);
        });
        // SolrFieldsParser solrFieldsParser = new SolrFieldsParser();
        // SolrFieldsParser.ParseResult schemaParseResult;
        // try (ByteArrayInputStream reader = new ByteArrayInputStream(ConfigFileReader.FILE_SCHEMA.getContent(domain))) {
        // schemaParseResult = solrFieldsParser.parseSchema(reader, false);
        // }
        //
        // return new IIndexMetaData() {
        //
        // @Override
        // public SolrFieldsParser.ParseResult getSchemaParseResult() {
        // return schemaParseResult;
        // }
        //
        // @Override
        // public IIndexBuildLifeCycleHook getIndexBuildLifeCycleHook() {
        // return AdapterIndexBuildLifeCycleHook.create(schemaParseResult);
        // }
        //
        // @Override
        // public LuceneVersion getLuceneVersion() {
        //
        // return LuceneVersion.LUCENE_7;
        // //                Version ver = getTISLuceneVersion(domain);
        // //                if (ver.equals(Version.LUCENE_7_6_0)) {
        // //                    return LuceneVersion.LUCENE_7;
        // //                }
        // //
        // //                throw new IllegalStateException("illegal version ver:" + ver);
        // }
        // };
    }

    // private Version getTISLuceneVersion(SnapshotDomain domain) {
    // try {
    // // =getLuceneVersion===============================================================
    // Version luceneVersion = Version.LUCENE_7_6_0;
    // byte[] solrConfigContent = ConfigFileReader.FILE_SOLOR.getContent(domain);
    // ByteArrayInputStream solrReader = new ByteArrayInputStream(solrConfigContent);
    // try {
    // Document document = solrConfigDocumentbuilder.parse(solrReader);
    // Node luceneMatchVersionNode = (Node) SolrFieldsParser.createXPath()
    // .evaluate("config/luceneMatchVersion", document, XPathConstants.NODE);
    // if (luceneMatchVersionNode != null) {
    // luceneVersion = Version.parse(luceneMatchVersionNode.getTextContent());
    // }
    // } finally {
    // IOUtils.closeQuietly(solrReader);
    // }
    // logger.info("luceneMatchVersionNode:{}", luceneVersion);
    // // ================================================================================
    // return luceneVersion;
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // /**
    // * 开始执行dump
    // *
    // * @param startTime
    // * @param dumpTask
    // * @return
    // * @throws InterruptedException
    // * @throws ExecutionException
    // * @throws TimeoutException
    // */
    // private RunningStatus startDump(final Date startTime,
    // final IRemoteJobTrigger dumpTask) throws InterruptedException,
    // ExecutionException, TimeoutException {
    // Future<RunningStatus> dumpResult = RemoteBuildCenterUtils.taskPool
    // .submit(new Callable<RunningStatus>() {
    // @Override
    // public RunningStatus call() throws Exception {
    // dumpTask.submitJob();
    // RunningStatus runStatus = null;
    // while (true) {
    // runStatus = dumpTask.getRunningStatus();
    // if (runStatus.isComplete()) {
    // logger.info("dump complete");
    // break;
    // }
    // logger.info("execute dump,exec past:"
    // + (System.currentTimeMillis() - startTime
    // .getTime()) / 1000 + "s");
    // Thread.sleep(3000);
    // }
    // return runStatus;
    // }
    // });
    // return dumpResult.get(5, TimeUnit.HOURS);
    // // return runningStatus;
    // }
    // /**
    // * @param jmxConns
    // * @throws IOException
    // */
    // protected void resumeIncrFlow(List<JMXConnector> jmxConns)
    // throws IOException {
    // for (JMXConnector c : jmxConns) {
    // try {
    // this.remoteIncrControl.resumeIncrFlow(c);
    // c.close();
    // } catch (Exception e) {
    // 
    // }
    // }
    // }
    // @Override
    // public void setApplicationContext(ApplicationContext applicationContext)
    // throws BeansException {
    // 
    // applicationContext.getBeanNamesForType(com.taobao.terminator.hdfs.client.bean.HdfsRealTimeTerminatorBean.class);
    // }
    // /**
    // * @return
    // * @throws TerminatorZKException
    // * @throws Exception
    // */
    // private List<JMXConnector> pauseIncrFlow() throws Exception {
    // List<JMXConnector> jmxConns = new ArrayList<JMXConnector>();
    // try {
    // final String incrNodeParent = "/tis/incr_transfer/" + indexName;
    // if (!this.zkClient.exists(incrNodeParent, true)) {
    // return jmxConns;
    // }
    // List<String> incrNodes = this.zkClient.getChildren(incrNodeParent,
    // null, true);
    // for (String incrNode : incrNodes) {
    // jmxConns.add(DefaultRemoteIncrControl.createConnector(
    // new String(this.zkClient.getData(incrNodeParent + "/"
    // + incrNode, null, new Stat(), true)),
    // INCR_NODE_PORT));
    // }
    // for (JMXConnector c : jmxConns) {
    // this.remoteIncrControl.pauseIncrFlow(c);
    // }
    // } catch (Exception e) {
    // logger.warn(e.getMessage(), e);
    // }
    // return jmxConns;
    // }
    // /**
    // * @param indexName
    // * @param zkClient
    // * @throws UnknownHostException
    // * @throws KeeperException
    // * @throws InterruptedException
    // */
    // private static void registerMyself(final String indexName, TisZkClient
    // zkClient, int port)
    // throws UnknownHostException, KeeperException, InterruptedException {
    // final String lockpath = "/tis-lock/dumpindex/" + indexName;
    // try {
    // 
    // ZkUtils.guaranteeExist(zkClient.getZK(), lockpath);
    // String ip = Inet4Address.getLocalHost().getHostAddress();
    // zkClient.create(lockpath + "/dumper", (ip + ":" + port).getBytes(),
    // CreateMode.EPHEMERAL_SEQUENTIAL, true);
    // } catch (Exception e) {
    // throw KeeperException.create(Code.BADARGUMENTS, lockpath + "/dumper");
    // }
    // }
    // private static final ExecutorService executorService = Executors
    // .newCachedThreadPool();
    // /**
    // * 触发索引build
    // *
    // * @param indexName
    // * @param timepoint
    // * @param groupSize
    // * @param sourcePath
    // * @throws Exception
    // */
    // private boolean triggerIndexBuildJob(String indexName,
    // final String timepoint, int groupSize,
    // HdfsSourcePathCreator hdfsSourcePathCreator) throws Exception {
    // 
    // ImportDataProcessInfo processinfo = new ImportDataProcessInfo(999);
    // processinfo.setTimepoint(timepoint);
    // processinfo.setIndexName(indexName);
    // 
    // processinfo.setHdfsSourcePathCreator(hdfsSourcePathCreator);
    // 
    // // processinfo.setHdfsSourcePathCreator(new HdfsSourcePathCreator() {
    // // @Override
    // // public String build(String group) {
    // // // return
    // // // "/user/hive/db/totalpay_summary/pt=20151014210509/pmod="
    // // // + group;
    // //
    // // return String.format(sourcePath, timepoint, group);
    // // }
    // // });
    // 
    // final DistributeLog log = new DistributeLog() {
    // @Override
    // public void addLog(ImportDataProcessInfo state, String msg) {
    // logger.info(msg);
    // }
    // 
    // @Override
    // public void addLog(ImportDataProcessInfo state, InfoType level,
    // String msg) {
    // logger.info("level:" + level + "," + msg);
    // }
    // };
    // 
    // ExecutorCompletionService<BuildResult> completionService = new
    // ExecutorCompletionService<BuildResult>(
    // executorService);
    // 
    // for (int grouIndex = 0; grouIndex < groupSize; grouIndex++) {
    // RemoteIndexBuildJob indexBuildJob = createRemoteIndexBuildJob(
    // processinfo, grouIndex);
    // indexBuildJob.setLog(log);
    // completionService.submit(indexBuildJob);
    // }
    // Future<BuildResult> result = null;
    // BuildResult buildResult = null;
    // for (int grouIndex = 0; grouIndex < groupSize; grouIndex++) {
    // result = completionService.poll(4, TimeUnit.HOURS);
    // if (result == null) {
    // continue;
    // }
    // buildResult = result.get();
    // if (!buildResult.isSuccess()) {
    // logger.error("sourpath:" + buildResult.getHdfsSourcePath()
    // + " build faild.");
    // // build失败
    // return false;
    // }
    // logger.info("indexsize:" + buildResult.getIndexSize());
    // logger.info("group:" + buildResult.getGroup());
    // }
    // 
    // // 成功
    // return true;
    // }
    // @Override
    // public void setApplicationContext(ApplicationContext applicationContext)
    // throws BeansException {
    // this.beanContext = applicationContext;
    // }
    // private FileSystem getDistributeFileSystem() {
    // fileSystem = TISHdfsUtils.getFileSystem();
    // if (fileSystem == null) {
    // synchronized (IndexSwapTaskflowLauncher.class) {
    // if (fileSystem == null) {
    // TSearcherConfigFetcher configFetcher = TSearcherConfigFetcher.get();
    // Configuration configuration = new Configuration();
    // FileSystem fileSys = null;
    // if (StringUtils.isEmpty(configFetcher.getHdfsAddress())) {
    // throw new IllegalStateException("hdfsHost can not be null");
    // }
    // logger.info("hdfsAddress:" + configFetcher.getHdfsAddress());
    // try {
    // configuration.set("fs.default.name", configFetcher.getHdfsAddress());
    // 
    // configuration.addResource("core-site.xml");
    // configuration.addResource("mapred-site.xml");
    // 
    // fileSys = FileSystem.get(configuration);
    // 
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // fileSystem = fileSys;
    // 
    // }
    // }
    // }
    // return fileSystem;
    // }
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

    /**
     * @param taskid
     * @return
     * @throws Exception
     */
    public static PhaseStatusCollection loadPhaseStatusFromLocal(int taskid) {
        PhaseStatusCollection result = null;
        FullbuildPhase[] phases = FullbuildPhase.values();
        try {
            File localFile = null;
            BasicPhaseStatus phaseStatus;
            for (FullbuildPhase phase : phases) {
                localFile = BasicPhaseStatus.getFullBuildPhaseLocalFile(taskid, phase);
                if (!localFile.exists()) {
                    return result;
                }
                if (result == null) {
                    result = new PhaseStatusCollection(taskid, ExecutePhaseRange.fullRange());
                }
                phaseStatus = BasicPhaseStatus.statusWriter.loadPhase(localFile);
                switch (phase) {
                    case FullDump:
                        result.setDumpPhase((DumpPhaseStatus) phaseStatus);
                        break;
                    case JOIN:
                        result.setJoinPhase((JoinPhaseStatus) phaseStatus);
                        break;
                    case BUILD:
                        result.setBuildPhase((BuildPhaseStatus) phaseStatus);
                        break;
                    case IndexBackFlow:
                        result.setIndexBackFlowPhaseStatus((IndexBackFlowPhaseStatus) phaseStatus);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("taskid:" + taskid, e);
        }
        return result;
    }
}
