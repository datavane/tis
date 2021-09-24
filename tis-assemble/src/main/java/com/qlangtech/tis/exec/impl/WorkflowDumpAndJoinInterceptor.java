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
package com.qlangtech.tis.exec.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.taskflow.DataflowTask;
import com.qlangtech.tis.fullbuild.workflow.SingleTableDump;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.ISolrAppSource;
import com.qlangtech.tis.manage.impl.DataFlowAppSource;
import com.qlangtech.tis.rpc.server.IncrStatusUmbilicalProtocolImpl;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import org.jvnet.hudson.reactor.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工作流dump流程
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年9月2日
 */
public class WorkflowDumpAndJoinInterceptor extends TrackableExecuteInterceptor {

    // 超时时间
    public static final int TIME_OUT_HOURS = 8;


    @Override
    protected ExecuteResult execute(IExecChainContext execChainContext) throws Exception {
       // TisZkClient zkClient = execChainContext.getZkClient();

        ISolrAppSource appRule = execChainContext.getAppSource();//  DataFlowAppSource.load(execChainContext.getIndexName());

        //  execChainContext.getZkClient()
//        IExecChainContext execChainContext, TisZkClient zkClient
//                , DataFlowAppSource.ISingleTableDumpFactory singleTableDumpFactory, IAppSource.IDataProcessFeedback
//        dataProcessFeedback, ITaskPhaseInfo taskPhaseInfo

        final ExecuteResult faildResult = appRule.getProcessDataResults(execChainContext, new ISolrAppSource.ISingleTableDumpFactory() {
                    @Override
                    public DataflowTask createSingleTableDump(DependencyNode dump, boolean hasValidTableDump, String pt
                            , ITISCoordinator zkClient, IExecChainContext execChainContext, DumpPhaseStatus dumpPhaseStatus) {
                        return new SingleTableDump(dump, hasValidTableDump, /* isHasValidTableDump */
                                pt, zkClient, execChainContext, dumpPhaseStatus);

                    }
                },
                new ISolrAppSource.IDataProcessFeedback() {
                    @Override
                    public PhaseStatusCollection getPhaseStatusSet(IExecChainContext execContext) {
                        return TrackableExecuteInterceptor.taskPhaseReference.get(execContext.getTaskId());
                    }

                    @Override
                    public void reportDumpTableStatusError(IExecChainContext execContext, Task task) {
                        IncrStatusUmbilicalProtocolImpl statReceiver = IncrStatusUmbilicalProtocolImpl.getInstance();
                        statReceiver.reportDumpTableStatusError(execChainContext.getTaskId(), task.getDisplayName());
                       // statReceiver.reportDumpTableStatus();
                    }
                }, this
        );

        //   final ExecuteResult[] faildResult = getProcessDataResults(execChainContext, zkClient, this);
        final List<Map<String, String>> summary = new ArrayList<>();
        if (faildResult != null) {
            return faildResult;
        } else {
            return ExecuteResult.createSuccess().setMessage(JSON.toJSONString(summary, true));
        }
    }

//    private ExecuteResult[] getProcessDataResults(IExecChainContext execChainContext, TisZkClient zkClient, ITaskPhaseInfo taskPhaseInfo) throws Exception {
//        // 执行工作流数据结构
//        SqlDataFlowTopology topology = execChainContext.getAttribute(IFullBuildContext.KEY_WORKFLOW_ID);
//        Map<String, TaskAndMilestone> /*** taskid*/
//                taskMap = Maps.newHashMap();
//        // 取得workflowdump需要依赖的表
//        Collection<DependencyNode> tables = topology.getDumpNodes();
//        StringBuffer dumps = new StringBuffer("dependency table:\n");
//        dumps.append("\t\t=======================\n");
//        for (DependencyNode t : tables) {
//            dumps.append("\t\t").append(t.getDbName()).append(".").append(t.getName())
//                    .append("[").append(t.getTabid()).append(",").append("] \n");
//        }
//        dumps.append("\t\t=======================\n");
//        logger.info(dumps.toString());
//        // 将所有的表的状态先初始化出来
//        DumpPhaseStatus dumpPhaseStatus = getPhaseStatus(execChainContext, FullbuildPhase.FullDump);
//        SingleTableDump tabDump = null;
//        for (DependencyNode dump : topology.getDumpNodes()) {
//            tabDump = new SingleTableDump(dump, false, /* isHasValidTableDump */
//                    "tableDump.getPt()", zkClient, execChainContext, dumpPhaseStatus);
//            taskMap.put(dump.getId(), new TaskAndMilestone(tabDump));
//        }
//        final ExecuteResult[] faildResult = new ExecuteResult[1];
//        if (topology.isSingleTableModel()) {
//            executeDAG(execChainContext, topology, taskMap, faildResult);
//        } else {
//            TemplateContext tplContext = new TemplateContext(execChainContext);
//            JoinPhaseStatus joinPhaseStatus = taskPhaseInfo.getPhaseStatus(execChainContext, FullbuildPhase.JOIN);
//            PluginStore<FlatTableBuilder> pluginStore = TIS.getPluginStore(FlatTableBuilder.class);
//            Objects.requireNonNull(pluginStore.getPlugin(), "flatTableBuilder can not be null");
//            // chainContext.setFlatTableBuilderPlugin(pluginStore.getPlugin());
//            final IFlatTableBuilder flatTableBuilder = pluginStore.getPlugin();// execChainContext.getFlatTableBuilder();
//            final SqlTaskNodeMeta fNode = topology.getFinalNode();
//            flatTableBuilder.startTask((context) -> {
//                DataflowTask process = null;
//                for (SqlTaskNodeMeta pnode : topology.getNodeMetas()) {
//                    /**
//                     * ***********************************
//                     * 构建宽表构建任务节点
//                     * ************************************
//                     */
//                    process = flatTableBuilder.createTask(pnode, StringUtils.equals(fNode.getId(), pnode.getId())
//                            , tplContext, context, execChainContext.getTableDumpFactory(), joinPhaseStatus.getTaskStatus(pnode.getExportName()));
//                    taskMap.put(pnode.getId(), new TaskAndMilestone(process));
//                }
//                executeDAG(execChainContext, topology, taskMap, faildResult);
//            });
//        }
//        return faildResult;
//    }

//    private void executeDAG(IExecChainContext execChainContext, SqlDataFlowTopology topology, Map<String, TaskAndMilestone> taskMap, ExecuteResult[] faildResult) {
//        try {
//            TISReactor reactor = new TISReactor(execChainContext, taskMap);
//            String dagSessionSpec = topology.getDAGSessionSpec();
//            logger.info("dagSessionSpec:" + dagSessionSpec);
//
//            //  final PrintWriter w = new PrintWriter(sw, true);
//            ReactorListener listener = new ReactorListener() {
//                // TODO: Does it really needs handlers to be synchronized?
////                @Override
////                public synchronized void onTaskStarted(Task t) {
////            //        w.println("Started " + t.getDisplayName());
////                }
//
//                @Override
//                public synchronized void onTaskCompleted(Task t) {
//                    //   w.println("Ended " + t.getDisplayName());
//                    processTaskResult(execChainContext, (TISReactor.TaskImpl) t, new ITaskResultProcessor() {
//                        @Override
//                        public void process(DumpPhaseStatus dumpPhase, TISReactor.TaskImpl task) {
//                        }
//
//                        @Override
//                        public void process(JoinPhaseStatus joinPhase, TISReactor.TaskImpl task) {
//                        }
//                    });
//                }
//
//                @Override
//                public synchronized void onTaskFailed(Task t, Throwable err, boolean fatal) {
//                    // w.println("Failed " + t.getDisplayName() + " with " + err);
//                    processTaskResult(execChainContext, (TISReactor.TaskImpl) t, new ITaskResultProcessor() {
//
//                        @Override
//                        public void process(DumpPhaseStatus dumpPhase, TISReactor.TaskImpl task) {
//                            IncrStatusUmbilicalProtocolImpl statReceiver = IncrStatusUmbilicalProtocolImpl.getInstance();
//                            statReceiver.reportDumpTableStatusError(execChainContext.getTaskId(), task.getIdentityName());
//                        }
//
//                        @Override
//                        public void process(JoinPhaseStatus joinPhase, TISReactor.TaskImpl task) {
//                            JoinPhaseStatus.JoinTaskStatus stat = joinPhase.getTaskStatus(task.getIdentityName());
//                            // statReceiver.reportBuildIndexStatErr(execContext.getTaskId(),task.getIdentityName());
//                            stat.setWaiting(false);
//                            stat.setFaild(true);
//                            stat.setComplete(true);
//                        }
//                    });
//                }
////
////                @Override
////                public synchronized void onAttained(Milestone milestone) {
////                    w.println("Attained " + milestone);
////                }
//            };
//
//
//            // 执行DAG地调度
//            reactor.execute(executorService, reactor.buildSession(dagSessionSpec), listener, new ReactorListener() {
//
//                @Override
//                public void onTaskCompleted(Task t) {
//                    // dumpPhaseStatus.isComplete();
//                    // joinPhaseStatus.isComplete();
//                }
//
//                @Override
//                public void onTaskFailed(Task t, Throwable err, boolean fatal) {
//                    logger.error(t.getDisplayName(), err);
//                    faildResult[0] = ExecuteResult.createFaild().setMessage("status.runningStatus.isComplete():" + err.getMessage());
//                }
//            });
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }


//    private void processTaskResult(IExecChainContext execContext, TISReactor.TaskImpl t, ITaskResultProcessor resultProcessor) {
//        TISReactor.TaskImpl task = t;
//        PhaseStatusCollection pstats = TrackableExecuteInterceptor.taskPhaseReference.get(execContext.getTaskId());
//        if (pstats != null) {
//            switch (task.getPhase()) {
//                case FullDump:
//                    // pstats.getDumpPhase()
//                    // IncrStatusUmbilicalProtocolImpl statReceiver = IncrStatusUmbilicalProtocolImpl.getInstance();
//                    // statReceiver.reportDumpTableStatusError(execContext.getTaskId(), task.getIdentityName());
//                    pstats.getDumpPhase().isComplete();
//                    resultProcessor.process(pstats.getDumpPhase(), task);
//                    return;
//                case JOIN:
//                    // JoinPhaseStatus.JoinTaskStatus stat
//                    // = pstats.getJoinPhase().getTaskStatus(task.getIdentityName());
//                    // //statReceiver.reportBuildIndexStatErr(execContext.getTaskId(),task.getIdentityName());
//                    // stat.setWaiting(false);
//                    // stat.setFaild(true);
//                    // stat.setComplete(true);
//                    pstats.getJoinPhase().isComplete();
//                    resultProcessor.process(pstats.getJoinPhase(), task);
//                    return;
//                default:
//                    throw new IllegalStateException("taskphase:" + task.getPhase() + " is illegal");
//            }
//        }
//    }


    @Override
    public Set<FullbuildPhase> getPhase() {
        return Sets.newHashSet(FullbuildPhase.FullDump, FullbuildPhase.JOIN);
    }
}
