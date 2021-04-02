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
package com.qlangtech.tis.exec;

import com.google.common.collect.Maps;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.impl.*;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.impl.DataFlowAppSource;
import com.qlangtech.tis.sql.parser.er.IPrimaryTabFinder;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年9月6日下午5:03:00
 */
public class AbstractActionInvocation implements ActionInvocation {

    // 由数据中心触发的直接進入索引build階段
    public static final String COMMAND_KEY_DIRECTBUILD = "directbuild";

    private static final Logger logger = LoggerFactory.getLogger(AbstractActionInvocation.class);

    private static final IExecuteInterceptor[] directBuild = new IExecuteInterceptor[]{ // /////
            new IndexBuildWithHdfsPathInterceptor(), new IndexBackFlowInterceptor()};

    // 工作流執行方式
    public static final IExecuteInterceptor[] workflowBuild = new IExecuteInterceptor[]{ // new WorkflowTableJoinInterceptor(),
            new WorkflowDumpAndJoinInterceptor(), new WorkflowIndexBuildInterceptor(), new IndexBackFlowInterceptor()};

    /**
     * 创建执行链
     *
     * @param chainContext
     * @return
     */
    public static ActionInvocation createExecChain(IExecChainContext chainContext) throws Exception {
        IExecuteInterceptor[] ints = null;
        if (chainContext.getWorkflowId() != null) {
            ints = workflowBuild;
            IAppSource appSource = DataFlowAppSource.load(chainContext.getIndexName());
            EntityName targetEntity = appSource.getTargetEntity();

//            Integer workflowId = chainContext.getWorkflowId();
//            SqlDataFlowTopology workflowDetail = chainContext.getTopology();
//            Objects.requireNonNull(workflowDetail, "workflowDetail can not be null");
//            EntityName targetEntity = null;
//            if (workflowDetail.isSingleTableModel()) {
//                DependencyNode dumpNode = workflowDetail.getDumpNodes().get(0);
//                targetEntity = dumpNode.parseEntityName();
//            } else {
//                SqlTaskNodeMeta finalN = workflowDetail.getFinalNode();
//                targetEntity = EntityName.parse(finalN.getExportName());
//            }
            chainContext.setAttribute(IExecChainContext.KEY_BUILD_TARGET_TABLE_NAME, targetEntity);

            Integer taskid = chainContext.getTaskId();
            TrackableExecuteInterceptor.taskPhaseReference.put(taskid, new PhaseStatusCollection(taskid, ExecutePhaseRange.fullRange()));
            //chainContext.setAttribute(IFullBuildContext.KEY_WORKFLOW_ID, workflowDetail);

            IPrimaryTabFinder pTabFinder = appSource.getPrimaryTabFinder();

            chainContext.setAttribute(IFullBuildContext.KEY_ER_RULES, pTabFinder);
        } else {
            if ("true".equalsIgnoreCase(chainContext.getString(COMMAND_KEY_DIRECTBUILD))) {
                ints = directBuild;
            } else {
                // ints = fullints;
                throw new UnsupportedOperationException();
            }
        }
        return createInvocation(chainContext, ints);
    }


    public static ActionInvocation createInvocation(IExecChainContext chainContext, IExecuteInterceptor[] ints) {
        final ComponentOrders componentOrders = new ComponentOrders();
        AbstractActionInvocation preInvocation = new AbstractActionInvocation();
        preInvocation.setContext(chainContext);
        preInvocation.setComponentOrders(componentOrders);
        AbstractActionInvocation invocation = null;
        for (int i = (ints.length - 1); i >= 0; i--) {
            for (FullbuildPhase phase : ints[i].getPhase()) {
                componentOrders.put(phase, i);
            }
            invocation = new AbstractActionInvocation();
            invocation.setComponentOrders(componentOrders);
            invocation.setContext(chainContext);
            invocation.setInterceptor(ints[i]);
            invocation.setSuccessor(preInvocation);
            preInvocation = invocation;
        }
        logger.info("component description:");
        for (Map.Entry<FullbuildPhase, Integer> componentEntry : componentOrders.entrySet()) {
            logger.info(componentEntry.getKey() + ":" + componentEntry.getValue());
        }
        logger.info("description end");
        return preInvocation;
    }

    private ComponentOrders componentOrders;

    public static class ComponentOrders {

        private final Map<FullbuildPhase, Integer> orders;

        public ComponentOrders() {
            super();
            this.orders = Maps.newHashMap();
        }

        public Integer get(FullbuildPhase key) {
            Integer index = this.orders.get(key);
            if (index == null) {
                throw new IllegalStateException("key:" + key + " can not find relevant map keys[" + orders.keySet().stream().map((r) -> r.getName()).collect(Collectors.joining(",")) + "]");
            }
            return index;
        }

        public Set<Entry<FullbuildPhase, Integer>> entrySet() {
            return orders.entrySet();
        }

        public Integer put(FullbuildPhase key, Integer value) {
            return orders.put(key, value);
        }
    }

    @Override
    public ExecuteResult invoke() throws Exception {
        if (componentOrders == null) {
            throw new IllegalStateException("componentOrders can not be null");
        }
        ExecutePhaseRange phaseRange = chainContext.getExecutePhaseRange();
        // String start = chainContext.getString(IFullBuildContext.COMPONENT_START);
        // String end = chainContext.getString(IFullBuildContext.COMPONENT_END);
        int startIndex = Integer.MIN_VALUE;
        int endIndex = Integer.MAX_VALUE;
        startIndex = componentOrders.get(phaseRange.getStart());
        endIndex = componentOrders.get(phaseRange.getEnd());
        if (interceptor == null) {
            return ExecuteResult.SUCCESS;
        } else {
            int current;
            try {
                current = componentOrders.get(FullbuildPhase.getFirst(interceptor.getPhase()));
            } catch (Throwable e) {
                throw new IllegalStateException("component:" + FullbuildPhase.desc(interceptor.getPhase()) + " can not find value in componentOrders," + componentOrders.toString());
            }
            if (current >= startIndex && current <= endIndex) {
                logger.info("execute " + FullbuildPhase.desc(interceptor.getPhase()) + ":" + current + "[" + startIndex + "," + endIndex + "]");
                return interceptor.intercept(successor);
            } else {
                // 直接跳过
                return successor.invoke();
            }
        }
    }

    public ComponentOrders getComponentOrders() {
        return componentOrders;
    }

    public void setComponentOrders(ComponentOrders componentOrders) {
        this.componentOrders = componentOrders;
    }


    @Override
    public IExecChainContext getContext() {
        return this.chainContext;
    }

    private IExecChainContext chainContext;

    private IExecuteInterceptor interceptor;

    private ActionInvocation successor;

    public void setSuccessor(ActionInvocation successor) {
        this.successor = successor;
    }

    public IExecuteInterceptor getSuccessor() {
        return interceptor;
    }

    public void setInterceptor(IExecuteInterceptor successor) {
        this.interceptor = successor;
    }

    public void setContext(IExecChainContext action) {
        this.chainContext = action;
    }


}
