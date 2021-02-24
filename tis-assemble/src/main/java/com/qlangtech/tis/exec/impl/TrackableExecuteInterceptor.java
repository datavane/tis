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
package com.qlangtech.tis.exec.impl;

import com.google.common.collect.Lists;
import com.qlangtech.tis.ajax.AjaxResult;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.exec.*;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BasicPhaseStatus;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.HttpUtils.PostParam;
import com.qlangtech.tis.order.center.IParamContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 执行进度可跟踪的执行器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月23日
 */
public abstract class TrackableExecuteInterceptor implements IExecuteInterceptor, ITaskPhaseInfo {

    private static final Logger log = LoggerFactory.getLogger(TrackableExecuteInterceptor.class);

    public static final MessageFormat WORKFLOW_CONFIG_URL_FORMAT = new MessageFormat(Config.getConfigRepositoryHost() + "/config/config.ajax?action={0}&event_submit_{1}=true&handler={2}" + "{3}");

    public static final MessageFormat WORKFLOW_CONFIG_URL_POST_FORMAT = new MessageFormat(Config.getConfigRepositoryHost() + "/config/config.ajax?action={0}&event_submit_{1}=true");

    public static final Map<Integer, PhaseStatusCollection> /**
     * taskid
     */
    taskPhaseReference = new HashMap<>();

    protected static final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 标记当前任务的ID
     *
     * @return
     */
    @Override
    @SuppressWarnings("all")
    public <T extends BasicPhaseStatus<?>> T getPhaseStatus(IExecChainContext execContext, FullbuildPhase phase) {
        PhaseStatusCollection phaseStatusCollection = taskPhaseReference.get(execContext.getTaskId());
        switch(phase) {
            case FullDump:
                return (T) phaseStatusCollection.getDumpPhase();
            case JOIN:
                return (T) phaseStatusCollection.getJoinPhase();
            case BUILD:
                return (T) phaseStatusCollection.getBuildPhase();
            case IndexBackFlow:
                return (T) phaseStatusCollection.getIndexBackFlowPhaseStatus();
            default:
                throw new IllegalStateException(phase + " is illegal has not any match status");
        }
    }

    @Override
    public final ExecuteResult intercept(ActionInvocation invocation) throws Exception {
        IExecChainContext execChainContext = invocation.getContext();
        int taskid = execChainContext.getTaskId();
        log.info("phase:" + FullbuildPhase.desc(this.getPhase()) + " start ,taskid:" + taskid);
        // 开始执行一个新的phase需要通知console
        // final int phaseId = createNewPhase(taskid, FullbuildPhase.getFirst(this.getPhase()));
        ExecuteResult result = null;
        try {
            result = this.execute(execChainContext);
            if (!result.isSuccess()) {
                log.error("taskid:" + taskid + ",phase:" + FullbuildPhase.desc(this.getPhase()) + " faild,reason:" + result.getMessage());
            }
        // createCompletePhase(taskid, phaseId, result.isSuccess() ? ExecResult.SUCCESS : ExecResult.FAILD, result.getMessage());
        } catch (Exception e) {
            // }
            throw e;
        }
        if (result.isSuccess()) {
            return invocation.invoke();
        } else {
            log.error("full build job is failed");
            // StringUtils.EMPTY);
            return result;
        }
    }

    /**
     * 执行
     *
     * @param execChainContext
     * @return
     * @throws Exception
     */
    protected abstract ExecuteResult execute(IExecChainContext execChainContext) throws Exception;

    // /**
    // * 创建节点完成
    // *
    // * @param taskid
    // * @return newphaseid
    // */
    public static void createTaskComplete(int taskid, ExecResult execResult) {
        if (execResult == null) {
            throw new IllegalArgumentException("param execResult can not be null");
        }
        String url = WorkflowDumpAndJoinInterceptor.WORKFLOW_CONFIG_URL_FORMAT.format(new Object[] { "fullbuild_workflow_action", "do_task_complete", StringUtils.EMPTY, /* advance_query_result */
        StringUtils.EMPTY });
        // 
        List<PostParam> params = Lists.newArrayList(// 
        new PostParam("execresult", String.valueOf(execResult.getValue())), // 
        new PostParam(IParamContext.KEY_TASK_ID, String.valueOf(taskid)));
        HttpUtils.soapRemote(url, params, CreateNewTaskResult.class);
    }

    // public static Integer createNewPhase(int taskid, FullbuildPhase phase) {
    // String url = WorkflowDumpAndJoinInterceptor.WORKFLOW_CONFIG_URL_POST_FORMAT
    // .format(new Object[]{"fullbuild_workflow_action", "do_phase_start", "", /* advance_query_result */
    // "" /* extra params */
    // });
    // List<PostParam> params = Lists.newArrayList();
    // params.add(new PostParam("taskid", String.valueOf(taskid)));
    // params.add(new PostParam("taskphase", String.valueOf(phase.getValue())));
    // return HttpUtils.soapRemote(url, params, Integer.class).getBizresult();
    // }
    /**
     * 开始执行一個新的任務
     *
     * @param newTaskParam taskid
     * @return
     */
    public static Integer createNewTask(NewTaskParam newTaskParam) {
        String url = WorkflowDumpAndJoinInterceptor.WORKFLOW_CONFIG_URL_POST_FORMAT.format(new Object[] { "fullbuild_workflow_action", "do_create_new_task" });
        AjaxResult<CreateNewTaskResult> result = HttpUtils.soapRemote((url), newTaskParam.params(), CreateNewTaskResult.class);
        return result.getBizresult().getTaskid();
    }

    /**
     * 创建新的Task执行结果
     */
    public static class IntegerAjaxResult extends AjaxResult<Integer> {
    }

    public static class CreateNewTaskResult {

        private int taskid;

        private Application app;

        public CreateNewTaskResult() {
            super();
        }

        public int getTaskid() {
            return taskid;
        }

        public void setTaskid(int taskid) {
            this.taskid = taskid;
        }

        public void setApp(Application app) {
            this.app = app;
        }

        public Application getApp() {
            return app;
        }
    }

    public static class NewTaskParam {

        private Integer workflowid;

        private TriggerType triggerType;

        private String appname;

        // 历史任务ID
        private Integer historyTaskId;

        public void setHistoryTaskId(Integer historyTaskId) {
            this.historyTaskId = historyTaskId;
        }

        // private FullbuildPhase fromPhase;
        // private FullbuildPhase toPhase;
        private ExecutePhaseRange executeRanage;

        public Integer getWorkflowid() {
            return workflowid;
        }

        public void setWorkflowid(Integer workflowid) {
            this.workflowid = workflowid;
        }

        public TriggerType getTriggerType() {
            return triggerType;
        }

        public void setTriggerType(TriggerType triggerType) {
            this.triggerType = triggerType;
        }

        public String getAppname() {
            return appname;
        }

        public void setAppname(String appname) {
            this.appname = appname;
        }

        // public Integer getBuildHistoryId() {
        // return buildHistoryId;
        // }
        // 
        // public void setBuildHistoryId(Integer buildHistoryId) {
        // this.buildHistoryId = buildHistoryId;
        // }
        // 历史taskid
        // private Integer buildHistoryId;
        public ExecutePhaseRange getExecuteRanage() {
            return executeRanage;
        }

        public void setExecuteRanage(ExecutePhaseRange executeRanage) {
            this.executeRanage = executeRanage;
        }

        private List<PostParam> params() {
            List<PostParam> params = Lists.newArrayList(new PostParam(IFullBuildContext.KEY_WORKFLOW_ID, workflowid), new PostParam(IFullBuildContext.KEY_TRIGGER_TYPE, triggerType.getValue()), new PostParam(IParamContext.COMPONENT_START, executeRanage.getStart().getValue()), new PostParam(IParamContext.COMPONENT_END, executeRanage.getEnd().getValue()));
            if (!executeRanage.contains(FullbuildPhase.FullDump)) {
                if (historyTaskId == null) {
                    throw new IllegalStateException("param historyTaskId can not be null");
                }
                params.add(new PostParam(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID, historyTaskId));
            }
            if (StringUtils.isNotBlank(appname)) {
                // result.append("&").append(IFullBuildContext.KEY_APP_NAME).append("=").append(appname);
                params.add(new PostParam(IFullBuildContext.KEY_APP_NAME, appname));
            }
            return params;
        }
    }
}
