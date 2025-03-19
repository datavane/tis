/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.dao;

import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.common.CreateNewTaskResult;
import com.qlangtech.tis.realtime.yarn.rpc.SynResTarget;
import com.qlangtech.tis.workflow.dao.IWorkFlowBuildHistoryDAO;
import com.qlangtech.tis.workflow.dao.IWorkFlowDAO;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria.Criteria;
import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;
import java.util.Objects;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/9
 */
public interface ICommonDAOContext {
    public IApplicationDAO getApplicationDAO();

    public IWorkflowDAOFacade getWorkflowDAOFacade();

    public default WorkFlowBuildHistory getLatestSuccessWorkflowHistory(SynResTarget resTarget) {
        Objects.requireNonNull(resTarget, "param resTarget can not be null");
//        if (!resTarget.isPipeline()) {
//            throw new NotImplementedException("resTarget:" + resTarget.getName() + " tranform workflow type has not been implemented");
//        }
        WorkFlowBuildHistoryCriteria historyCriteria = new WorkFlowBuildHistoryCriteria();
        historyCriteria.setOrderByClause("id desc");
        Criteria criteria = historyCriteria.createCriteria().andStateEqualTo((byte) ExecResult.SUCCESS.getValue());
        if (resTarget.isPipeline()) {
            criteria.andAppNameEqualTo(resTarget.getName());
        } else {
            // workflow
            Integer workflowId = resTarget.getWorkflowId();
            if (workflowId == null) {
                WorkFlowCriteria wfCriteria = new WorkFlowCriteria();
                wfCriteria.createCriteria().andNameEqualTo(resTarget.getName());
                List<WorkFlow> workFlows = getWorkflowDAOFacade().getWorkFlowDAO().selectByExample(wfCriteria, 1, 1);
                boolean hasSetWfId = false;
                for (WorkFlow wf : workFlows) {
                    // criteria.andWorkFlowIdEqualTo(wf.getId());
                    workflowId = wf.getId();
                    hasSetWfId = true;
                }
                if (!hasSetWfId) {
                    throw new IllegalStateException("has not set workflow Id workFlows.size:" + workFlows.size() + ",workflowName:" + resTarget.getName());
                }
            }
            criteria.andWorkFlowIdEqualTo(workflowId);
        }

        List<WorkFlowBuildHistory> histories
                = this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO().selectByExample(historyCriteria, 1, 1);

        for (WorkFlowBuildHistory buildHistory : histories) {
            return buildHistory;
        }
        return null;
    }

    /**
     * reference: IExecChainContext.createNewTask(
     * chainContext, workflowInstanceIdOpt.isPresent() ? TriggerType.CRONTAB : TriggerType.MANUAL);
     *
     * @param chainContext
     * @param triggerType
     * @return
     */
    public CreateNewTaskResult createNewDataXTask(IExecChainContext chainContext, TriggerType triggerType);

    default IWorkFlowBuildHistoryDAO getTaskBuildHistoryDAO() {
        return this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO();
    }

    default IWorkFlowDAO getWorkFlowDAO() {
        return this.getWorkflowDAOFacade().getWorkFlowDAO();
    }
}
