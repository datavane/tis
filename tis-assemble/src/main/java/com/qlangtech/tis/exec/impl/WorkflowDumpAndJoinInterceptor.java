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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus;
import com.qlangtech.tis.fullbuild.taskflow.DataflowTask;
import com.qlangtech.tis.fullbuild.taskflow.IFlatTableBuilder;
import com.qlangtech.tis.fullbuild.taskflow.TISReactor;
import com.qlangtech.tis.fullbuild.taskflow.TISReactor.TaskAndMilestone;
import com.qlangtech.tis.fullbuild.taskflow.TemplateContext;
import com.qlangtech.tis.fullbuild.workflow.SingleTableDump;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta.SqlDataFlowTopology;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import org.apache.commons.lang.StringUtils;
import org.jvnet.hudson.reactor.ReactorListener;
import org.jvnet.hudson.reactor.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 工作流dump流程
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年9月2日
 */
public class WorkflowDumpAndJoinInterceptor extends TrackableExecuteInterceptor {

    // 超时时间
    public static final int TIME_OUT_HOURS = 8;
    private static final Logger logger = LoggerFactory.getLogger("fullbuild");

    @Override
    protected ExecuteResult execute(IExecChainContext execChainContext) throws Exception {
        TisZkClient zkClient = execChainContext.getZkClient();
        // 执行工作流数据结构
        SqlDataFlowTopology topology = execChainContext.getAttribute(IFullBuildContext.KEY_WORKFLOW_ID);
        Map<String, TaskAndMilestone> /*** taskid*/
                taskMap = Maps.newHashMap();
        // 取得workflowdump需要依赖的表
        Collection<DependencyNode> tables = topology.getDumpNodes();
        StringBuffer dumps = new StringBuffer("dependency table:\n");
        dumps.append("\t\t=======================\n");
        for (DependencyNode t : tables) {
            dumps.append("\t\t").append(t.getDbName()).append(".").append(t.getName())
                    .append("[").append(t.getTabid()).append(",").append("] \n");
        }
        dumps.append("\t\t=======================\n");
        logger.info(dumps.toString());
        // 将所有的表的状态先初始化出来
        DumpPhaseStatus dumpPhaseStatus = getPhaseStatus(execChainContext, FullbuildPhase.FullDump);
        SingleTableDump tabDump = null;
        for (DependencyNode dump : topology.getDumpNodes()) {
            tabDump = new SingleTableDump(dump, false, /* isHasValidTableDump */
                    "tableDump.getPt()", zkClient, execChainContext, dumpPhaseStatus);
            taskMap.put(dump.getId(), new TaskAndMilestone(tabDump));
        }
        final ExecuteResult[] faildResult = new ExecuteResult[1];
        if (topology.isSingleTableModel()) {
            executeDAG(execChainContext, topology, taskMap, faildResult);
        } else {
            TemplateContext tplContext = new TemplateContext(execChainContext);
            JoinPhaseStatus joinPhaseStatus = this.getPhaseStatus(execChainContext, FullbuildPhase.JOIN);

            final IFlatTableBuilder flatTableBuilder = execChainContext.getFlatTableBuilder();
            final SqlTaskNodeMeta fNode = topology.getFinalNode();
            flatTableBuilder.startTask((context) -> {
                DataflowTask process = null;
                for (SqlTaskNodeMeta pnode : topology.getNodeMetas()) {
                    /**
                     * ***********************************
                     * 构建宽表构建任务节点
                     * ************************************
                     */
                    process = flatTableBuilder.createTask(pnode, StringUtils.equals(fNode.getId(), pnode.getId())
                            , tplContext, context, execChainContext.getTableDumpFactory(), joinPhaseStatus.getTaskStatus(pnode.getExportName()));
                    taskMap.put(pnode.getId(), new TaskAndMilestone(process));
                }
                executeDAG(execChainContext, topology, taskMap, faildResult);
            });
        }


        final List<Map<String, String>> summary = new ArrayList<>();
        if (faildResult[0] != null) {
            return faildResult[0];
        } else {
            return ExecuteResult.createSuccess().setMessage(JSON.toJSONString(summary, true));
        }
    }

    private void executeDAG(IExecChainContext execChainContext, SqlDataFlowTopology topology, Map<String, TaskAndMilestone> taskMap, ExecuteResult[] faildResult) {
        try {
            TISReactor reactor = new TISReactor(execChainContext, taskMap);
            String dagSessionSpec = topology.getDAGSessionSpec();
            logger.info("dagSessionSpec:" + dagSessionSpec);
            // 执行DAG地调度
            reactor.execute(executorService, reactor.buildSession(dagSessionSpec), new ReactorListener() {

                @Override
                public void onTaskCompleted(Task t) {
                    // dumpPhaseStatus.isComplete();
                    // joinPhaseStatus.isComplete();
                }

                @Override
                public void onTaskFailed(Task t, Throwable err, boolean fatal) {
                    logger.error(t.getDisplayName(), err);
                    faildResult[0] = ExecuteResult.createFaild().setMessage("status.runningStatus.isComplete():" + err.getMessage());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<FullbuildPhase> getPhase() {
        // Collections.singleton(FullbuildPhase.FullDump);
        return Sets.newHashSet(FullbuildPhase.FullDump, FullbuildPhase.JOIN);
    }
}
