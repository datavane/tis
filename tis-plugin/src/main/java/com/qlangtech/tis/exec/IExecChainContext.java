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
package com.qlangtech.tis.exec;

import com.google.common.collect.Lists;
import com.qlangtech.tis.ajax.AjaxResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.coredefine.module.action.TriggerBuildResult;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.indexbuild.RemoteTaskTriggers;
import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.CreateNewTaskResult;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月15日 上午11:48:16
 */
public interface IExecChainContext extends IJoinTaskContext {

    Logger logger = LoggerFactory.getLogger(IExecChainContext.class);
    MessageFormat WORKFLOW_CONFIG_URL_POST_FORMAT
            = new MessageFormat(Config.getConfigRepositoryHost()
            + "/config/config.ajax?action={0}&event_submit_{1}=true");

    static Integer createNewTask(IExecChainContext chainContext) {
        return createNewTask(chainContext, TriggerType.MANUAL);
    }

    /**
     * 创建新的task
     *
     * @param chainContext
     * @return taskid
     */
    static Integer createNewTask(IExecChainContext chainContext, TriggerType triggerType) {
        Integer workflowId = chainContext.getWorkflowId();
        NewTaskParam newTaskParam = new NewTaskParam();
        ExecutePhaseRange executeRanage = chainContext.getExecutePhaseRange();
        if (executeRanage == null) {
            throw new IllegalStateException("executeRanage can not be null");
        }
        if (chainContext.hasIndexName() || executeRanage.getEnd().bigThan(FullbuildPhase.JOIN)) {
            String indexname = chainContext.getIndexName();
            newTaskParam.setAppname(indexname);
        }
        String histroyTaskId = chainContext.getString(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID);
        if (StringUtils.isNotBlank(histroyTaskId)) {
            newTaskParam.setHistoryTaskId(Integer.parseInt(histroyTaskId));
        }
        newTaskParam.setWorkflowid(workflowId);
        newTaskParam.setExecuteRanage(executeRanage);

        newTaskParam.setTriggerType(triggerType);
        /**=============================================
         * 提交task请求
         =============================================*/
        Integer taskid = createNewTask(newTaskParam);
        logger.info("create new taskid:" + taskid);
        chainContext.setAttribute(JobCommon.KEY_TASK_ID, taskid);
        return taskid;
    }

    /**
     * 开始执行一個新的任務, 只是创建一个taskid而已
     *
     * @param newTaskParam taskid
     * @return
     */
    static Integer createNewTask(NewTaskParam newTaskParam) {
        String url = WORKFLOW_CONFIG_URL_POST_FORMAT
                .format(new Object[]{"fullbuild_workflow_action", "do_create_new_task"});
        AjaxResult<CreateNewTaskResult> result = HttpUtils.soapRemote(url, newTaskParam.params(), CreateNewTaskResult.class);
        if (!result.isSuccess()) {
            throw new IllegalStateException("error:" + String.join(",", result.getErrormsg()));
        }
        return result.getBizresult().getTaskid();
    }

    /**
     * 创建一个新的同步任务
     *
     * @param triggerNewTaskParam
     * @return
     */
    static Integer triggerNewTask(TriggerNewTaskParam triggerNewTaskParam) {
        String url = WORKFLOW_CONFIG_URL_POST_FORMAT
                .format(new Object[]{"fullbuild_workflow_action", "do_initialize_trigger_task"});
        AjaxResult<CreateNewTaskResult> result = HttpUtils.soapRemote(url, triggerNewTaskParam.params(), CreateNewTaskResult.class);
        if (!result.isSuccess()) {
            throw new IllegalStateException("error:" + String.join(",", result.getErrormsg()));
        }
        return result.getBizresult().getTaskid();
    }


    IDataxProcessor getProcessor();

    public void addAsynSubJob(AsynSubJob jobName);

    public List<AsynSubJob> getAsynSubJobs();

    public boolean containAsynJob();

    void setTskTriggers(RemoteTaskTriggers tskTriggers);

    public RemoteTaskTriggers getTskTriggers();

    /**
     * 取消当前正在运行的任务
     */
    void cancelTask();

//    TableDumpFactory getTableDumpFactory();

    class AsynSubJob {
        public final String jobName;

        public AsynSubJob(String jobName) {
            this.jobName = jobName;
        }
    }

//    <T extends IBasicAppSource> T getAppSource();

    ITISCoordinator getZkClient();


    /**
     * 全量構建流程ID
     *
     * @return
     */
    Integer getWorkflowId();

    String getWorkflowName();

    ITISFileSystem getIndexBuildFileSystem();

//    TableDumpFactory getTableDumpFactory();
//
//    IndexBuilderTriggerFactory getIndexBuilderFactory();

    void rebindLoggingMDCParams();

    class TriggerNewTaskParam {
        private final Long powerJobWorkflowInstanceId;
        private final String appname;


        public TriggerNewTaskParam(Long powerJobWorkflowInstanceId, String appname) {
            this.powerJobWorkflowInstanceId = Objects.requireNonNull(powerJobWorkflowInstanceId);
            this.appname = Objects.requireNonNull(appname, "appname can not be null");
        }

        public List<HttpUtils.PostParam> params() {
            return Lists.newArrayList(
                    new HttpUtils.PostParam(DataxUtils.POWERJOB_WORKFLOW_INSTANCE_ID, powerJobWorkflowInstanceId)
                    , new HttpUtils.PostParam(TriggerBuildResult.KEY_APPNAME, appname)
            );
        }
    }

    class NewTaskParam {

        private Integer workflowid;

        private TriggerType triggerType;

        private String appname;

        // 历史任务ID
        private Integer historyTaskId;

        public void setHistoryTaskId(Integer historyTaskId) {
            this.historyTaskId = historyTaskId;
        }

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

        public ExecutePhaseRange getExecuteRanage() {
            return executeRanage;
        }

        public void setExecuteRanage(ExecutePhaseRange executeRanage) {
            this.executeRanage = executeRanage;
        }

        public List<HttpUtils.PostParam> params() {
            if (executeRanage == null) {
                throw new IllegalStateException("executeRanage can not be null");
            }
            List<HttpUtils.PostParam> params = Lists.newArrayList( //
                    new HttpUtils.PostParam(IFullBuildContext.KEY_WORKFLOW_ID, workflowid)
                    , new HttpUtils.PostParam(IFullBuildContext.KEY_TRIGGER_TYPE, triggerType.getValue())
                    , new HttpUtils.PostParam(COMPONENT_START, executeRanage.getStart().getValue())
                    , new HttpUtils.PostParam(COMPONENT_END, executeRanage.getEnd().getValue()));
            if (!executeRanage.contains(FullbuildPhase.FullDump)) {
                if (historyTaskId == null) {
                    throw new IllegalStateException("param historyTaskId can not be null");
                }
                params.add(new HttpUtils.PostParam(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID, historyTaskId));
            }
            if (StringUtils.isNotBlank(appname)) {
                // result.append("&").append(IFullBuildContext.KEY_APP_NAME).append("=").append(appname);
                params.add(new HttpUtils.PostParam(IFullBuildContext.KEY_APP_NAME, appname));
            }
            return params;
        }
    }
}
