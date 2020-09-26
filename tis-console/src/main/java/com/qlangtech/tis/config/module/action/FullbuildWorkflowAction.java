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
package com.qlangtech.tis.config.module.action;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.git.GitUtils;
import com.qlangtech.tis.git.GitUtils.GitBranchInfo;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.workflow.dao.IWorkFlowBuildHistoryDAO;
import com.qlangtech.tis.workflow.pojo.*;
import org.apache.commons.lang.StringUtils;
import java.util.Date;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年9月30日
 */
public class FullbuildWorkflowAction extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;

    /**
     * description: table有效时间
     */
    private static final long VALID_TIME = 4 * 60 * 60 * 1000;

    /**
     * assemble 节点接收到来自console节点的触发任务，开始执行需要创建一个new的workflowbuildhistory记录
     *
     * @param context
     */
    @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
    public void doCreateNewTask(Context context) {
        Integer worflowid = this.getInt(IFullBuildContext.KEY_WORKFLOW_ID);
        final TriggerType triggerType = TriggerType.parse(this.getInt(IFullBuildContext.KEY_TRIGGER_TYPE));
        Application app = null;
        // appname 可以为空
        String appname = this.getString(IFullBuildContext.KEY_APP_NAME);
        if (StringUtils.isNotBlank(appname)) {
            app = this.getApplicationDAO().selectByName(appname);
            if (app == null) {
                throw new IllegalStateException("appname:" + appname + " relevant app pojo is not exist");
            }
        }
        WorkFlowBuildHistory task = new WorkFlowBuildHistory();
        task.setCreateTime(new Date());
        task.setStartTime(new Date());
        task.setWorkFlowId(worflowid);
        task.setTriggerType(triggerType.getValue());
        task.setState((byte) ExecResult.DOING.getValue());
        // Integer buildHistoryId = null;
        // 从什么阶段开始执行
        FullbuildPhase fromPhase = FullbuildPhase.parse(getInt(IParamContext.COMPONENT_START, FullbuildPhase.FullDump.getValue()));
        FullbuildPhase endPhase = FullbuildPhase.parse(getInt(IParamContext.COMPONENT_END, FullbuildPhase.IndexBackFlow.getValue()));
        if (app == null) {
            if (endPhase.bigThan(FullbuildPhase.JOIN)) {
                endPhase = FullbuildPhase.JOIN;
            }
        }
        if (fromPhase.getValue() > FullbuildPhase.FullDump.getValue()) {
            // 如果是从非第一步开始执行的话，需要客户端提供依赖的history记录id
            // buildHistoryId = this.getInt("buildHistoryId");
            // if (buildHistoryId == null) {
            // throw new IllegalArgumentException("fromPhase:" + fromPhase + " must provide
            // a buildHistoryId param");
            // }
            task.setHistoryId(this.getInt(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID));
        }
        // 说明只有workflow的流程和索引没有关系，所以不可能执行到索引build阶段去
        // task.setEndPhase((app == null) ? FullbuildPhase.JOIN.getValue() : FullbuildPhase.IndexBackFlow.getValue());
        task.setEndPhase(endPhase.getValue());
        task.setStartPhase(fromPhase.getValue());
        if (app != null) {
            task.setAppId(app.getAppId());
            task.setAppName(app.getProjectName());
        }
        // 生成一个新的taskid
        this.setBizResult(context, new CreateNewTaskResult(getHistoryDAO().insertSelective(task), app));
    }

    // public void doPhaseStart(Context context) {
    // Integer taskid = this.getInt("taskid");
    // if (taskid == null) {
    // throw new IllegalArgumentException("taskid can not be null");
    // }
    // FullbuildPhase taskPhase = FullbuildPhase.parse(this.getInt("taskphase"));
    // WorkFlowBuildPhase phase = new WorkFlowBuildPhase();
    // phase.setCreateTime(new Date());
    // phase.setOpTime(new Date());
    // phase.setPhase(taskPhase.getValue());
    // phase.setWorkFlowBuildHistoryId(taskid);
    // this.setBizResult(context, getPhaseDAO().insertSelective(phase));
    // }
    /**
     * 执行阶段结束
     *
     * @param context
     */
    @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
    public void doTaskComplete(Context context) {
        // Integer phaseid = this.getInt("phaseid");
        Integer taskid = this.getInt("taskid");
        // 执行结果
        // final String phaseinfo = this.getString("phaseinfo");
        // WorkFlowBuildPhase phase = new WorkFlowBuildPhase();
        // phase.setOpTime(new Date());
        // if (StringUtils.isNotBlank(phaseinfo)) {
        // phase.setPhaseInfo(phaseinfo);
        // }
        ExecResult execResult = ExecResult.parse(this.getInt("execresult"));
        // phase.setResult(execResult.getValue());
        // WorkFlowBuildPhaseCriteria query = new WorkFlowBuildPhaseCriteria();
        // query.createCriteria().andIdEqualTo(phaseid);
        // getPhaseDAO().updateByExampleSelective(phase, query);
        // WorkFlowBuildPhase phaseInfo = this.getPhaseDAO().loadFromWriteDB(phaseid);
        // workflow history 表更新
        WorkFlowBuildHistory history = this.getHistoryDAO().loadFromWriteDB(taskid);
        if (history == null) {
            throw new IllegalStateException("taskid:" + taskid + " relevant WorkFlowBuildHistory obj can not be null");
        }
        WorkFlowBuildHistoryCriteria hq = new WorkFlowBuildHistoryCriteria();
        hq.createCriteria().andIdEqualTo(taskid);
        history = new WorkFlowBuildHistory();
        history.setEndTime(new Date());
        history.setState((byte) execResult.getValue());
        getHistoryDAO().updateByExampleSelective(history, hq);
    }

    protected IWorkFlowBuildHistoryDAO getHistoryDAO() {
        return this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO();
    }

    public static class CreateNewTaskResult {

        private final int taskid;

        private final Application app;

        public CreateNewTaskResult(int taskid, Application app) {
            super();
            this.taskid = taskid;
            this.app = app;
        }

        public int getTaskid() {
            return taskid;
        }

        public Application getApp() {
            return app;
        }
    }

    private DatasourceTable getTable(String tabName) {
        DatasourceTableCriteria query = new DatasourceTableCriteria();
        query.createCriteria().andNameEqualTo(tabName);
        List<DatasourceTable> tabList = this.getWorkflowDAOFacade().getDatasourceTableDAO().selectByExample(query);
        return tabList.stream().findFirst().get();
    }

    public static GitUtils.GitBranchInfo getBranch(WorkFlow workFlow) {
        RunEnvironment runtime = RunEnvironment.getSysRuntime();
        if (runtime == RunEnvironment.ONLINE) {
            return GitBranchInfo.$(GitUtils.GitBranch.MASTER);
        } else {
            // : GitBranchInfo.$(workFlow.getName());
            return GitBranchInfo.$(GitUtils.GitBranch.DEVELOP);
        }
    }

    public static class ValidTableDump {

        boolean hasValidTableDump;

        String pt = "";

        public boolean isHasValidTableDump() {
            return hasValidTableDump;
        }

        public void setHasValidTableDump(boolean hasValidTableDump) {
            this.hasValidTableDump = hasValidTableDump;
        }

        public String getPt() {
            return pt;
        }

        public void setPt(String pt) {
            this.pt = pt;
        }
    }
}
