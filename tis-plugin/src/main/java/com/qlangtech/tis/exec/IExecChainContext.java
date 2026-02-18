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

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.ajax.AjaxResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.coredefine.module.action.DistributeJobTriggerBuildResult;
import com.qlangtech.tis.coredefine.module.action.TriggerBuildResult;
import com.qlangtech.tis.datax.DataXJobSubmit.InstanceType;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.ISpecifiedLocalLogger;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.indexbuild.RemoteTaskTriggers;
import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.CreateNewTaskResult;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import com.qlangtech.tis.plugin.PluginAndCfgsSnapshot;
import com.qlangtech.tis.plugin.PluginAndCfgsSnapshotUtils;
import com.qlangtech.tis.workflow.pojo.DagNodeExecution;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.qlangtech.tis.manage.common.HttpUtils.KEY_FULLBUILD_WORKFLOW_ACTION;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月15日 上午11:48:16
 */
public interface IExecChainContext extends IJoinTaskContext, ISpecifiedLocalLogger {

    String KEY_HISTORY_TASK = "task";
    String KEY_NODE_EXEC = "nodeExec";

    Logger logger = LoggerFactory.getLogger(IExecChainContext.class);
    MessageFormat WORKFLOW_CONFIG_URL_POST_FORMAT = new MessageFormat(Config.getConfigRepositoryHost() + "/config" +
            "/config.ajax?action={0}&event_submit_{1}=true");


    static CreateNewTaskResult createNewTask(IExecChainContext chainContext, File dagSpecPath) {
        return createNewTask(chainContext, TriggerType.MANUAL, dagSpecPath);
    }

    /**
     * 创建新的task
     *
     * @param chainContext
     * @return taskid
     */
    static CreateNewTaskResult createNewTask(IExecChainContext chainContext, TriggerType triggerType,
                                             File dagSpecPath) {
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
        newTaskParam.setDagSpecPath(Objects.requireNonNull(dagSpecPath, "dagSpecPath can not be null"));

        newTaskParam.setTriggerType(triggerType);
        /**=============================================
         * 提交task请求
         =============================================*/
        CreateNewTaskResult createResult = createNewTask(newTaskParam);
        logger.info("create new taskid:" + createResult.getTaskid());
        chainContext.setAttribute(JobCommon.KEY_TASK_ID, createResult.getTaskid());
        chainContext.setAttribute(JobCommon.KEY_PREVIOUS_TASK_ID, createResult.getPreTaskId());
        return createResult;
    }

    /**
     * 开始执行一個新的任務, 只是创建一个taskid而已
     *
     * @param newTaskParam taskid
     * @return
     */
    static CreateNewTaskResult createNewTask(NewTaskParam newTaskParam) {
        String url = WORKFLOW_CONFIG_URL_POST_FORMAT.format(new Object[]{"fullbuild_workflow_action",
                "do_create_new_task"});
        AjaxResult<CreateNewTaskResult> result = HttpUtils.soapRemote(url, newTaskParam.params(),
                CreateNewTaskResult.class);
        if (!result.isSuccess()) {
            throw new IllegalStateException("error:" + String.join(",", result.getErrormsg()));
        }
        return result.getBizresult();
    }

    /**
     * 是否有正在运行的实例
     *
     * @param dataXName
     * @return
     */
    public static Boolean hasRunningWorkflowInstance(DataXName dataXName) {
        List<HttpUtils.PostParam> params = HttpUtils.dataXToParams(dataXName);
        AjaxResult<Boolean> result //
                =
                HttpUtils.soapRemote(WORKFLOW_CONFIG_URL_POST_FORMAT.format(new Object[]{KEY_FULLBUILD_WORKFLOW_ACTION, "do_has_running_workflow_instance"}) //
                , params //
                , Boolean.class);
        if (!result.isSuccess()) {
            throw new IllegalStateException("error:" + String.join(",", result.getErrormsg()));
        }
        return result.getBizresult();
    }

    /**
     * 从TIS console中加载一个已经创建的history workflow记录
     *
     * @param taskId
     * @return
     */
    public static WorkFlowBuildHistory loadWorkFlowBuildHistory(Integer taskId) {

        HttpUtils.PostParam taskIdParam = new HttpUtils.PostParam(JobParams.KEY_TASK_ID, taskId);
        AjaxResult<WorkFlowBuildHistory> result //
                =
                HttpUtils.soapRemote(WORKFLOW_CONFIG_URL_POST_FORMAT.format(new Object[]{KEY_FULLBUILD_WORKFLOW_ACTION, "do_load_task"}), Collections.singletonList(taskIdParam), WorkFlowBuildHistory.class);
        if (!result.isSuccess()) {
            throw new IllegalStateException("error:" + String.join(",", result.getErrormsg()));
        }
        return result.getBizresult();
    }

    /**
     * 更新一条历史执行记录
     *
     * @param history
     * @return
     */
    static Integer updateWorkFlowBuildHistory(WorkFlowBuildHistory history) {
        if (history.getId() == null) {
            throw new IllegalArgumentException("history.getId() can not be null");
        }
        HttpUtils.PostParam taskHistoryParam = new HttpUtils.PostParam(KEY_HISTORY_TASK, history);
        AjaxResult<Integer> result //
                =
                HttpUtils.postJSON(WORKFLOW_CONFIG_URL_POST_FORMAT.format(new Object[]{KEY_FULLBUILD_WORKFLOW_ACTION,
                        "do_update_task"}), Collections.singletonList(taskHistoryParam), Integer.class, true);
        if (!result.isSuccess()) {
            throw new IllegalStateException("error:" + String.join(",", result.getErrormsg()));
        }
        return result.getBizresult();
    }


    /**
     * 创建一条DAG Node exec执行记录
     *
     * @param nodeExec
     * @return
     */
    static Integer insertDAGNodeExecution(DagNodeExecution nodeExec) {
        //        if (nodeExec.getId() == null) {
        //            throw new IllegalArgumentException("record.getId() can not be null");
        //        }
        HttpUtils.PostParam nodeExecParam = new HttpUtils.PostParam(KEY_NODE_EXEC, nodeExec);
        AjaxResult<Integer> result //
                =
                HttpUtils.postJSON(WORKFLOW_CONFIG_URL_POST_FORMAT.format(new Object[]{KEY_FULLBUILD_WORKFLOW_ACTION,
                        "do_insert_node_exec"}), Collections.singletonList(nodeExecParam), Integer.class, true);
        if (!result.isSuccess()) {
            throw new IllegalStateException("error:" + String.join(",", result.getErrormsg()));
        }
        return result.getBizresult();
    }


    /**
     * 创建一个新的同步任务
     *
     * @param triggerNewTaskParam
     * @return
     */
    static DistributeJobTriggerBuildResult triggerNewTask(TriggerNewTaskParam triggerNewTaskParam) {
        String url = WORKFLOW_CONFIG_URL_POST_FORMAT.format(new Object[]{KEY_FULLBUILD_WORKFLOW_ACTION,
                "do_initialize_trigger_task"});

        AjaxResult<DistributeJobTriggerBuildResult> result = HttpUtils.soapRemote(url, triggerNewTaskParam.params(),
                DistributeJobTriggerBuildResult.class);
        if (!result.isSuccess()) {
            throw new IllegalStateException("error:" + String.join(",", result.getErrormsg()));
        }
        return result.getBizresult();
    }


    static JSONObject createInstanceParams( //
                                            Integer tisTaskId, IDataxProcessor processor, boolean dryRun,
                                            Optional<String> pluginCfgsMetas) {

        JSONObject instanceParams = new JSONObject();
        instanceParams.put(JobParams.KEY_TASK_ID, tisTaskId);
        instanceParams.put(JobParams.KEY_COLLECTION, processor.identityValue());
        instanceParams.put(DataxUtils.EXEC_TIMESTAMP, TimeFormat.getCurrentTimeStamp());
        instanceParams.put(StoreResourceType.KEY_STORE_RESOURCE_TYPE, processor.getResType().getType());
        instanceParams.put(IFullBuildContext.DRY_RUN, dryRun);

        instanceParams.put(PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS, pluginCfgsMetas.orElseGet(() -> {
            return manifestOfDataX(processor);
        }));
        return instanceParams;
    }

    public static String manifestOfDataX(IDataxProcessor processor) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 将数据通道的依赖插件以及配置信息添加到instanceParams中
            PluginAndCfgsSnapshotUtils.writeManifest2OutputStream(outputStream,
                    PluginAndCfgsSnapshot.createDataBatchJobManifestCfgAttrs(processor));
            final Base64 base64 = new Base64();
            return base64.encodeAsString(outputStream.toByteArray());
            // instanceParams.put(PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS, base64.encodeAsString
            // (outputStream.toByteArray()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    default DataXName getDataXName() {
        return this.getProcessor().getDataXName();
    }


    IDataxProcessor getProcessor();

    //    public void addAsynSubJob(AsynSubJob jobName);
    //
    //    public List<AsynSubJob> getAsynSubJobs();
    //
    //    public boolean containAsynJob();

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

    ITISCoordinator getZkClient();


    /**
     * 全量構建流程ID
     *
     * @return
     */
    Integer getWorkflowId();

    String getWorkflowName();

    ITISFileSystem getIndexBuildFileSystem();

    //    void rebindLoggingMDCParams();

    class TriggerNewTaskParam {
        // private final Long powerJobWorkflowInstanceId;
        private final DataXName dataXName;
        private final InstanceType instanceTriggerType;

        /**
         * // @param powerJobWorkflowInstanceId
         *
         * @param dataXName 可能是dataX pipeline 名称，也可能是 tis DataFlow名称
         */
        public TriggerNewTaskParam(
                //  Long powerJobWorkflowInstanceId
                InstanceType instanceTriggerType, DataXName dataXName) {
            //this.powerJobWorkflowInstanceId = Objects.requireNonNull(powerJobWorkflowInstanceId);
            this.instanceTriggerType = Objects.requireNonNull(instanceTriggerType,
                    "param instanceTriggerType can " + "not" + " be null");
            this.dataXName = Objects.requireNonNull(dataXName, "dataXName can not be null");
        }

        public List<HttpUtils.PostParam> params() {
            List<HttpUtils.PostParam> postParams = HttpUtils.dataXToParams(this.dataXName);
            postParams.add(new HttpUtils.PostParam(InstanceType.KEY_TYPE, this.instanceTriggerType.literia));
            return postParams;
        }
    }

    class NewTaskParam {

        private Integer workflowid;

        private TriggerType triggerType;

        private String appname;

        // 历史任务ID
        private Integer historyTaskId;

        private File dagSpecPath;

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

        public File getDagSpecPath() {
            return dagSpecPath;
        }

        public void setDagSpecPath(File dagSpecPath) {
            this.dagSpecPath = dagSpecPath;
        }

        public List<HttpUtils.PostParam> params() {
            if (executeRanage == null) {
                throw new IllegalStateException("executeRanage can not be null");
            }

            List<HttpUtils.PostParam> params = Lists.newArrayList( //
                    // new HttpUtils.PostParam(IFullBuildContext.KEY_WORKFLOW_ID, workflowid)
                    new HttpUtils.PostParam(IFullBuildContext.KEY_TRIGGER_TYPE, triggerType.getValue()),
                    new HttpUtils.PostParam(COMPONENT_START, executeRanage.getStart().getValue()),
                    new HttpUtils.PostParam(COMPONENT_END, executeRanage.getEnd().getValue()));

            params.add(new HttpUtils.PostParam( //
                    IFullBuildContext.KEY_DAG_SPEC_PATH, Objects.requireNonNull(this.dagSpecPath,
                    "dagSpecPath can " + "not be null").getAbsolutePath()));
            if (!this.dagSpecPath.exists()) {
                throw new IllegalStateException("dagSpecPath:" + dagSpecPath.getAbsolutePath() + " is not exist");
            }

            if (!executeRanage.contains(FullbuildPhase.FullDump)) {
                if (historyTaskId == null) {
                    throw new IllegalStateException("param historyTaskId can not be null");
                }
                params.add(new HttpUtils.PostParam(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID, historyTaskId));
            }
            int requireParamCount = 0;
            if (this.workflowid != null) {
                params.add(new HttpUtils.PostParam(IFullBuildContext.KEY_WORKFLOW_ID, workflowid));
                requireParamCount++;
            }
            if (StringUtils.isNotBlank(appname)) {
                // result.append("&").append(IFullBuildContext.KEY_APP_NAME).append("=").append(appname);
                params.add(new HttpUtils.PostParam(IFullBuildContext.KEY_APP_NAME, appname));
                requireParamCount++;
            }

            if (requireParamCount < 1) {
                throw new IllegalStateException("neither of param workflowid:" + workflowid + " appname:" + appname + " can be null");
            }
            return params;
        }
    }
}
