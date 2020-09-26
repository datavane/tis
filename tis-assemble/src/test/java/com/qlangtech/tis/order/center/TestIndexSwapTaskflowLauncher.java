/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.order.center;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.ExecChainContextUtils;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.exec.impl.TrackableExecuteInterceptor;
import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fs.TISFSDataOutputStream;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.fullbuild.indexbuild.RunningStatus;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.fullbuild.phasestatus.IProcessDetailStatus;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildSharedPhaseStatus;
import com.qlangtech.tis.fullbuild.taskflow.TestParamContext;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.offline.FileSystemFactory;
import com.qlangtech.tis.offline.FlatTableBuilder;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.plugin.ComponentMeta;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.rpc.server.IncrStatusUmbilicalProtocolImpl;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta.SqlDataFlowTopology;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年8月22日
 */
public class TestIndexSwapTaskflowLauncher extends TestCase {

    private static final int TASK_ID = 253;

    private static final String WF_ID = "45";

    private static final String TAB_TOTALPYINFO = "order.totalpayinfo";

    static {
        CenterResource.setNotFetchFromCenterRepository();
        HttpUtils.addMockGlobalParametersConfig();
        try {
            File tmpDir = new File("/tmp/tis");
            FileUtils.forceMkdir(tmpDir);
            Config.setDataDir(tmpDir.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ComponentMeta dumpAndIndexBuilderComponent = TIS.getDumpAndIndexBuilderComponent();
        dumpAndIndexBuilderComponent.synchronizePluginsFromRemoteRepository();
    }

    public void testZkHostGetter() {
        assertTrue("getZKHost can not null", StringUtils.isNotBlank(Config.getZKHost()));
        assertTrue("getTisHost can not null", StringUtils.isNotBlank(Config.getConfigRepositoryHost()));
        assertTrue("getAssembleHost can not null", StringUtils.isNotBlank(Config.getAssembleHost()));
    }

    public void setUp() throws Exception {
        clearMocks();
    }

    public void testIndexBuilder() throws Exception {
        String pt = "20200616170903";
        final DefaultChainContext chainContext = createRangeChainContext(FullbuildPhase.BUILD, FullbuildPhase.IndexBackFlow, pt);
        startFullBuild(chainContext, new FullBuildStrategy());
    }

    public void testFullDumpUntilIndexBuilder() throws Exception {
        final DefaultChainContext chainContext = createRangeChainContext(FullbuildPhase.FullDump, FullbuildPhase.BUILD);
        startFullBuild(chainContext, new FullBuildStrategy());
    }

    public void testFullDumpUntilIndexBuilderTotalpayFaild() throws Exception {
        final Set<String> dumpFaildTabs = Sets.newHashSet(TAB_TOTALPYINFO);
        final DefaultChainContext chainContext = createRangeChainContext(FullbuildPhase.FullDump, FullbuildPhase.BUILD);
        startFullBuild(chainContext, new FullBuildStrategy() {

            @Override
            public Set<String> getTableDumpFaild() {
                return dumpFaildTabs;
            }
        });
    }

    public void startFullBuild(DefaultChainContext chainContext, FullBuildStrategy strategy) throws Exception {
        final String partitionTimestamp = chainContext.getPartitionTimestamp();
        assertNotNull("has create partitionTimestamp", partitionTimestamp);
        final MockFlatTableBuilder flatTableBuilder = new MockFlatTableBuilder();
        TableDumpFactory tableDumpFactory = mock("tableDumpFactory", TableDumpFactory.class);
        // FlatTableBuilder flatTableBuilder
        // = EasyMock.createMock("flatTableBuilder", FlatTableBuilder.class);
        FileSystemFactory indexBuilderFileSystemFactory = mock("indexBuildFileSystem", FileSystemFactory.class);
        IndexBuilderTriggerFactory indexBuilderTriggerFactory = mock("indexBuilderTriggerFactory", IndexBuilderTriggerFactory.class);
        String fsRoot = "/user/admin";
        ITISFileSystem fileSystem = mock("tisFileSystem", ITISFileSystem.class);
        // EasyMock.expect(fileSystem.getName()).andReturn("easymock").anyTimes();
        EasyMock.expect(indexBuilderFileSystemFactory.getFileSystem()).andReturn(fileSystem).anyTimes();
        EasyMock.expect(indexBuilderTriggerFactory.getFsFactory()).andReturn(indexBuilderFileSystemFactory);
        SnapshotDomain domain = HttpConfigFileReader.getResource(SEARCH_APP_NAME, 0, RunEnvironment.getSysRuntime(), ConfigFileReader.FILE_SCHEMA, ConfigFileReader.FILE_SOLR);
        ImportDataProcessInfo processInfo = new ImportDataProcessInfo(TASK_ID, indexBuilderFileSystemFactory);
        if (!strategy.errorTest()) {
            for (int groupNum = 0; groupNum < shardCount; groupNum++) {
                IRemoteJobTrigger builderTrigger = this.mock("indexbuild_" + groupNum, IRemoteJobTrigger.class);
                builderTrigger.submitJob();
                RunningStatus runStatus = RunningStatus.SUCCESS;
                EasyMock.expect(builderTrigger.getRunningStatus()).andReturn(runStatus);
                EasyMock.expect(indexBuilderTriggerFactory.createBuildJob(partitionTimestamp, SEARCH_APP_NAME, String.valueOf(groupNum), processInfo)).andReturn(builderTrigger);
                expectSolrMetaOutput(fsRoot, "schema", ConfigFileReader.FILE_SCHEMA, fileSystem, domain, groupNum);
                expectSolrMetaOutput(fsRoot, "solrconfig", ConfigFileReader.FILE_SOLR, fileSystem, domain, groupNum);
            }
        }
        EasyMock.expect(indexBuilderFileSystemFactory.getRootDir()).andReturn(fsRoot).anyTimes();
        if (!strategy.errorTest()) {
            EasyMock.expect(tableDumpFactory.getJoinTableStorePath(EntityName.parse("tis.totalpay_summary"))).andReturn("xxxx");
        }
        ExecutePhaseRange execRange = chainContext.getExecutePhaseRange();
        if (execRange.contains(FullbuildPhase.FullDump)) {
            expectCreateSingleTableDumpJob("shop.mall_shop", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("order.takeout_order_extra", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("order.orderdetail", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("order.instancedetail", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("order.specialfee", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("order.payinfo", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("member.card", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("order.order_bill", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob(TAB_TOTALPYINFO, partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("member.customer", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("cardcenter.ent_expense_order", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("cardcenter.ent_expense", partitionTimestamp, tableDumpFactory, strategy);
            expectCreateSingleTableDumpJob("order.servicebillinfo", partitionTimestamp, tableDumpFactory, strategy);
        }
        if (strategy.errorTest()) {
            IncrStatusUmbilicalProtocolImpl.ExecHook execHook = mock("incrStatusUmbilicalProtocolImplExecHook", IncrStatusUmbilicalProtocolImpl.ExecHook.class);
            for (String faildTab : strategy.getTableDumpFaild()) {
                execHook.reportDumpTableStatusError(TASK_ID, faildTab);
            }
            IncrStatusUmbilicalProtocolImpl.execHook = execHook;
        }
        replay();
        chainContext.setTableDumpFactory(tableDumpFactory);
        chainContext.setFlatTableBuilderPlugin(flatTableBuilder);
        // chainContext.setIndexBuildFileSystem(indexBuilderFileSystemFactory);
        chainContext.setIndexBuilderTriggerFactory(indexBuilderTriggerFactory);
        IndexSwapTaskflowLauncher taskflowLauncher = new IndexSwapTaskflowLauncher();
        taskflowLauncher.afterPropertiesSet();
        try {
            taskflowLauncher.startWork(chainContext);
            if (strategy.errorTest()) {
                fail();
            }
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Exception e) {
        }
        verifyAll();
        if (execRange.contains(FullbuildPhase.BUILD)) {
            PhaseStatusCollection phaseStatusCollection = TrackableExecuteInterceptor.taskPhaseReference.get(TASK_ID);
            assertNotNull("phaseStatusCollection can not be null", phaseStatusCollection);
            BuildPhaseStatus buildPhase = phaseStatusCollection.getBuildPhase();
            assertNotNull("buildPhase can not be null", buildPhase);
            IProcessDetailStatus<BuildSharedPhaseStatus> processStatus = buildPhase.getProcessStatus();
            processStatus.getProcessPercent();
            Collection<BuildSharedPhaseStatus> details = processStatus.getDetails();
            assertEquals(shardCount, details.size());
            for (int i = 0; i < shardCount; i++) {
                String sharedName = SEARCH_APP_NAME + "-" + i;
                BuildSharedPhaseStatus buildSharedPhaseStatus = buildPhase.getBuildSharedPhaseStatus(sharedName);
                assertNotNull("buildSharedPhaseStatus-" + sharedName, buildSharedPhaseStatus);
                assertTrue("buildSharedPhaseStatus.isComplete()", buildSharedPhaseStatus.isComplete());
                assertFalse("buildSharedPhaseStatus.isWaiting()", buildSharedPhaseStatus.isWaiting());
            }
        }
    }

    private class FullBuildStrategy {

        boolean errorTest() {
            return getTableDumpFaild().size() > 0;
        }

        public Set<String> getTableDumpFaild() {
            return Collections.emptySet();
        }

        public IRemoteJobTrigger createRemoteJobTrigger(EntityName table) {
            boolean dumpFaild = getTableDumpFaild().contains(table.getFullName());
            return new MockRemoteJobTrigger(!dumpFaild);
        }
    }

    private void expectSolrMetaOutput(String fsRoot, String fileName, PropteryGetter getter, ITISFileSystem fileSystem, SnapshotDomain domain, int groupNum) throws IOException {
        IPath path = mock("group" + groupNum + "configschema", IPath.class);
        String p = fsRoot + "/" + SEARCH_APP_NAME + "-" + groupNum + "/config/" + fileName + ".xml";
        // System.out.println(p);
        EasyMock.expect(fileSystem.getPath(p)).andReturn(path).anyTimes();
        TISFSDataOutputStream schemaOutput = mock("group" + groupNum + "_config" + fileName + "OutputStream", TISFSDataOutputStream.class);
        EasyMock.expect(fileSystem.create(path, true)).andReturn(schemaOutput);
        // IOUtils.write(ConfigFileReader.FILE_SCHEMA.getContent(domain), schemaOutput);
        IOUtils.write(getter.getContent(domain), schemaOutput);
        schemaOutput.close();
    }

    private void clearMocks() {
        mocks.clear();
    }

    private void verifyAll() {
        mocks.forEach((r) -> {
            EasyMock.verify(r);
        });
    }

    private static List<Object> mocks = Lists.newArrayList();

    public <T> T mock(String name, Class<?> toMock) {
        Object mock = EasyMock.createMock(name, toMock);
        mocks.add(mock);
        return (T) mock;
    }

    public void replay() {
        mocks.forEach((r) -> {
            EasyMock.replay(r);
        });
    }

    private void expectCreateSingleTableDumpJob(String tableName, String partitionTimestamp, TableDumpFactory tableDumpFactory, FullBuildStrategy strategy) {
        EntityName table = EntityName.parse(tableName);
        TaskContext taskContext = TaskContext.create();
        EasyMock.expect(tableDumpFactory.createSingleTableDumpJob(table, partitionTimestamp, taskContext)).andReturn(strategy.createRemoteJobTrigger(table));
    }

    /**
     * 执行dump和Joiner
     *
     * @throws Exception
     */
    public void testFullDumpAndJoiner() throws Exception {
        // System.out.println();
        assertFalse(TIS.initialized);
        // final TisZkClient zkClient = new
        // TisZkClient(TSearcherConfigFetcher.get().getZkAddress(), 60000);
        // ZkStateReader zkStateReader = new ZkStateReader(zkClient.getZK());
        // zkStateReader.createClusterStateWatchersAndUpdate();
        final DefaultChainContext chainContext = createDumpAndJoinChainContext();
        IndexSwapTaskflowLauncher taskflowLauncher = new IndexSwapTaskflowLauncher();
        taskflowLauncher.afterPropertiesSet();
        taskflowLauncher.startWork(chainContext);
    }

    static final int shardCount = 8;

    static final String SEARCH_APP_NAME = "search4totalpay";

    public static DefaultChainContext createRangeChainContext(FullbuildPhase start, FullbuildPhase end, String... pts) throws Exception {
        TestParamContext params = new TestParamContext();
        params.set(IFullBuildContext.KEY_APP_SHARD_COUNT, String.valueOf(shardCount));
        params.set(IFullBuildContext.KEY_APP_NAME, SEARCH_APP_NAME);
        params.set(IFullBuildContext.KEY_WORKFLOW_NAME, "totalpay");
        params.set(IFullBuildContext.KEY_WORKFLOW_ID, WF_ID);
        params.set(IExecChainContext.COMPONENT_START, start.getName());
        params.set(IExecChainContext.COMPONENT_END, end.getName());
        final DefaultChainContext chainContext = new DefaultChainContext(params);
        ExecutePhaseRange range = chainContext.getExecutePhaseRange();
        Assert.assertEquals(start, range.getStart());
        Assert.assertEquals(end, range.getEnd());
        Map<EntityName, ITabPartition> dateParams = Maps.newHashMap();
        chainContext.setAttribute(ExecChainContextUtils.PARTITION_DATA_PARAMS, dateParams);
        chainContext.setAttribute(IExecChainContext.KEY_TASK_ID, TASK_ID);
        chainContext.setTopology(SqlTaskNodeMeta.getSqlDataFlowTopology(chainContext.getWorkflowName()));
        final PluginStore<IndexBuilderTriggerFactory> buildTriggerFactory = TIS.getPluginStore(IndexBuilderTriggerFactory.class);
        assertNotNull(buildTriggerFactory.getPlugin());
        // chainContext.setIndexBuildFileSystem(buildTriggerFactory.getPlugin().getFsFactory());
        if (pts.length > 0) {
            chainContext.setPs(pts[0]);
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String pt = format.format(new Date());
            chainContext.setPs(pt);
        }
        PluginStore<FlatTableBuilder> pStore = TIS.getPluginStore(FlatTableBuilder.class);
        assertNotNull(pStore.getPlugin());
        chainContext.setFlatTableBuilderPlugin(pStore.getPlugin());
        chainContext.setMdcParamContext(() -> {
        });
        return chainContext;
    }

    public static DefaultChainContext createDumpAndJoinChainContext() throws Exception {
        return createRangeChainContext(FullbuildPhase.FullDump, FullbuildPhase.JOIN);
    }

    // b3eb0-636c-535e-044a-0d8083d6036b
    static final Pattern idpattern = Pattern.compile("[a-z0-9]+-[a-z0-9]+-[a-z0-9]+-[a-z0-9]+-[a-z0-9]+");

    public void testIDpattern() {
        Matcher matcher = idpattern.matcher("a3eb0-636c-535e-044a-0d8083d6036b");
        Assert.assertTrue(matcher.matches());
    }

    /**
     * 从远端取
     *
     * @throws Exception
     */
    public void testGetSqlDataFlowTopologyFromConsole() throws Exception {
    // SqlDataFlowTopology topology = RunEnvironment.getSysRuntime(ion.getWorkflowDetail(Integer.parseInt(WF_ID));
    // valiateTopology(topology);
    }

    // 从本地取
    public void testGetSqlDataFlowTopology() throws Exception {
        SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology("totalpay");
        valiateTopology(topology);
    // @SuppressWarnings("all")
    // public static SqlDataFlowTopology getSqlDataFlowTopology(String topologyName)
    // throws Exception {
    }

    protected void valiateTopology(SqlDataFlowTopology topology) {
        Assert.assertNotNull(topology);
        for (DependencyNode node : topology.getDumpNodes()) {
            Assert.assertTrue(StringUtils.isNotBlank(node.getDbName()));
            Assert.assertTrue(StringUtils.isNotBlank(node.getName()));
        }
        String spec = topology.getDAGSessionSpec();
        System.out.println(spec);
        Matcher matcher = null;
        String[] groups = StringUtils.split(spec, " ");
        String[] tuple = null;
        String[] ids = null;
        for (String g : groups) {
            tuple = g.split("->");
            for (String t : tuple) {
                if (StringUtils.isEmpty(t)) {
                    continue;
                }
                ids = t.split(",");
                for (String id : ids) {
                    matcher = idpattern.matcher(id);
                    Assert.assertTrue(t, matcher.matches());
                }
            }
        }
    }
}
