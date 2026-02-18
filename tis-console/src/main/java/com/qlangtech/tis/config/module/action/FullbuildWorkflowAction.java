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
package com.qlangtech.tis.config.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.exec.AbstractExecContext;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.impl.DataXPipelineExecContext;
import com.qlangtech.tis.exec.impl.WorkflowExecContext;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.CreateNewTaskResult;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.offline.module.action.OfflineDatasourceAction;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.plugin.rate.IncrRateController;
import com.qlangtech.tis.powerjob.model.PEWorkflowDAG;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import com.qlangtech.tis.realtime.yarn.rpc.SynResTarget;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.workflow.pojo.DagNodeExecution;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;
import com.qlangtech.tis.workflow.pojo.WorkflowDAGFileManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年9月30日
 */
public class FullbuildWorkflowAction extends BasicModule {
  private static final Logger logger = LoggerFactory.getLogger(FullbuildWorkflowAction.class);
  private static final String TIS_WORK_FLOW_CHANNEL = "tisWorkflowChannel";
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * description: table有效时间
   */
  private static final long VALID_TIME = 4 * 60 * 60 * 1000;


  /**
   *
   * @param context
   */
  public void doHasRunningWorkflowInstance(Context context) {
    DataXName dataXName = HttpUtils.createDataXName(this);
    WorkFlowBuildHistoryCriteria criteria = new WorkFlowBuildHistoryCriteria();
    criteria.createCriteria() //
      .andAppNameEqualTo(dataXName.getPipelineName()) //
      .andStateEqualTo((byte) ExecResult.DOING.getValue());
    this.setBizResult(context //
      , this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO().countByExample(criteria) > 0);
  }

  /**
   * 接收Powerjob发送过来的初始化触发任务，主要目标在TIS中进行必要的初始化工作
   *
   * @param context
   * @see com.qlangtech.tis.coredefine.module.action.DataxAction#doTriggerFullbuildTask(Context)
   * @see OfflineDatasourceAction#doExecuteWorkflow(Context)
   */
  public void doInitializeTriggerTask(Context context) {
    // 校验参数必须有
    //this.getLong(DataxUtils.POWERJOB_WORKFLOW_INSTANCE_ID);
    Rundata rundata = this.getRundata();
    if (this.getBoolean(TIS_WORK_FLOW_CHANNEL)) {
      rundata.forwardTo("offline", "offline_datasource_action", "execute_workflow");
    } else {
      rundata.forwardTo("coredefine", "datax_action", "trigger_fullbuild_task");
    }
  }

  /**
   * @param context
   */
  public void doGetRateController(Context context) {
    DataXName dataXPipeline = DataXName.createDataXPipeline(this.getString(IncrRateControllerCfgDTO.KEY_PIPELINE));
    Long lastModified = this.getLong(IncrRateControllerCfgDTO.KEY_LAST_MODIFIED);
    IncrRateController rateController = IncrRateController.getRateController(dataXPipeline);

    if (rateController == null) {
      return;
    }
    if (lastModified != null && lastModified >= Objects.requireNonNull(rateController.lastModified,
      "lastModified " + "can" + " not be null")) {
      return;
    }
    IncrRateControllerCfgDTO controllerCfgDTO = rateController.createIncrRateControllerCfgDTO();

    this.setBizResult(context, controllerCfgDTO);
  }

  /**
   * assemble 节点接收到来自console节点的触发任务，开始执行需要创建一个new的workflowbuildhistory记录
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
  public void doCreateNewTask(Context context) {

    final TriggerType triggerType = TriggerType.parse(this.getInt(IFullBuildContext.KEY_TRIGGER_TYPE));
    // Application app = null;
    // appname 可以为空
    String appname = this.getString(IFullBuildContext.KEY_APP_NAME);
    Integer workflowId = this.getInt(IFullBuildContext.KEY_WORKFLOW_ID, null, false);
    File dagSpecPath = new File(this.getString(IFullBuildContext.KEY_DAG_SPEC_PATH));
    if (!dagSpecPath.exists()) {
      throw new IllegalStateException("specPath:" + dagSpecPath.getAbsolutePath() + " must exist");
    }
    SynResTarget resTarget = null;
    AbstractExecContext execContext = null;
    if (StringUtils.isEmpty(appname)) {
      if (workflowId == null) {
        throw new IllegalStateException("workflowId can not be null");
      }
      resTarget = SynResTarget.transform(workflowId, "transformer");
      execContext = new WorkflowExecContext(workflowId, 0l);
    } else {
      resTarget = SynResTarget.pipeline(appname);
      execContext = new DataXPipelineExecContext(appname, 0l);
    }

    WorkFlowBuildHistory latestWorkflowHistory = this.getDaoContext().getLatestSuccessWorkflowHistory(resTarget);
    execContext.setExecutePhaseRange( //
      new ExecutePhaseRange(FullbuildPhase.parse(getInt(IParamContext.COMPONENT_START,
        FullbuildPhase.FullDump.getValue())), FullbuildPhase.parse(getInt(IParamContext.COMPONENT_END,
        FullbuildPhase.JOIN.getValue()))));


    CreateNewTaskResult newTaskResult = this.createNewDataXTask(execContext, triggerType, dagSpecPath,
      Optional.of(latestWorkflowHistory));
    // 生成一个新的taskid
    this.setBizResult(context, newTaskResult);
  }

  /**
   * @param context
   * @see IExecChainContext#loadWorkFlowBuildHistory(Integer)
   */
  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
  public void doLoadTask(Context context) {
    Integer taskId = this.getInt(JobParams.KEY_TASK_ID);
    WorkFlowBuildHistory buildHistory = getHistoryDAO().loadFromWriteDB(taskId);
    if (buildHistory == null) {
      throw new IllegalStateException("taskId:" + taskId + " relevant buildHistory can not be null");
    }
    this.setBizResult(context, buildHistory);
  }

  /**
   * 更新执行记录
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
  public void doUpdateTask(Context context) {
    JSONObject postBody = this.parseJsonPost();
    WorkFlowBuildHistory buildHistory = postBody.getObject(IExecChainContext.KEY_HISTORY_TASK,
      WorkFlowBuildHistory.class);
    if (buildHistory == null) {
      throw new IllegalStateException("buildHistory can not be null");
    }
    this.setBizResult(context, getHistoryDAO().updateByPrimaryKeySelective(buildHistory));
  }

  /**
   * 更新执行记录
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
  public void doInsertNodeExec(Context context) {
    JSONObject postBody = this.parseJsonPost();
    DagNodeExecution nodeExec = postBody.getObject(IExecChainContext.KEY_NODE_EXEC, DagNodeExecution.class);
    if (nodeExec == null) {
      throw new IllegalStateException("nodeExec can not be null");
    }
    //    if (nodeExec.getId() == null) {
    //      throw new IllegalStateException("nodeExec id can not be null");
    //    }
    this.setBizResult(context //
      , this.getWorkflowDAOFacade().getDagNodeExecutionDAO().insertSelective(nodeExec));
  }


  //  /**
  //   * 取得最近一次成功执行的workflowhistory
  //   *
  //   * @param context
  //   */
  //  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
  //  public void doGetLatestSuccessWorkflow(Context context) {
  //    try {
  //      String appName = this.getString(IFullBuildContext.KEY_APP_NAME);
  //      if (StringUtils.isEmpty(appName)) {
  //        throw new IllegalArgumentException("param appName can not be null");
  //      }
  //
  //
  //      WorkFlowBuildHistory latestSuccessWorkflowHistory = this.getLatestSuccessWorkflowHistory(SynResTarget
  //      .pipeline(appName));
  //      if (latestSuccessWorkflowHistory != null) {
  //        this.setBizResult(context, latestSuccessWorkflowHistory);
  //        return;
  //      }
  //      this.addErrorMessage(context, "can not find build history by appname:" + appName);
  //    } finally {
  //    }
  //  }

  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)

  public void doGetWf(Context context) {
    Integer taskId = this.getInt(JobCommon.KEY_TASK_ID);
    this.setBizResult(context, this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO().loadFromWriteDB(taskId));
  }

  /**
   * 执行阶段结束
   * do_task_complete
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
  public void doTaskComplete(Context context) {
    JSONObject postContent = this.getJSONPostContent();
    Integer taskid = postContent.getInteger(JobCommon.KEY_TASK_ID);
    // 执行结果
    ExecResult execResult = ExecResult.parse(postContent.getInteger(IParamContext.KEY_EXEC_RESULT));
    PEWorkflowDAG workflowDAG = postContent.getObject(PEWorkflowDAG.KEY_DAG, PEWorkflowDAG.class);
    //String[] asynJobsName = this.getStringArray(IParamContext.KEY_ASYN_JOB_NAME);
    WorkFlowBuildHistory buildHistory = updateWfHistory(taskid, execResult);
    if (StringUtils.isEmpty(buildHistory.getAppName())) {
      throw new IllegalStateException("pipeline name can not be null");
    }
    IDataxProcessor processor //
      = DataxProcessor.load(null, DataXName.createDataXPipeline(buildHistory.getAppName()));
    WorkflowDAGFileManager.saveTaskDAGStatus(processor, taskid, workflowDAG);

  }

  /**
   * 接收异步执行任务执行状态
   *
   * @param context
   */
//  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
//  public void doFeedbackAsynTaskStatus(Context context) {
//    Integer taskid = this.getInt(JobCommon.KEY_TASK_ID);
//    String jobName = this.getString(IParamContext.KEY_ASYN_JOB_NAME);
//    boolean execSuccess = this.getBoolean(IParamContext.KEY_ASYN_JOB_SUCCESS);
//
//    // this.updateAsynTaskState(taskid, jobName, execSuccess, 0);
//    this.setBizResult(context, new CreateNewTaskResult(taskid, null));
//  }

  //public static int MAX_CAS_RETRY_COUNT = 5;

  //  private void updateAsynTaskState(Integer taskid, String jobName, boolean execSuccess, int tryCount) {
  //    validateMaxCasRetryCount(taskid, tryCount);
  //    final WorkFlowBuildHistory history = getBuildHistory(taskid);
  //
  //    if (ExecResult.ASYN_DOING != ExecResult.parse(history.getState())) {
  //      updateAsynTaskState(taskid, jobName, execSuccess, ++tryCount);
  //      return;
  //    }
  //
  //    JSONObject status = JSON.parseObject(history.getAsynSubTaskStatus());
  //    JSONObject tskStat = status.getJSONObject(jobName);
  //    if (tskStat == null) {
  //      throw new IllegalStateException("jobName:" + jobName + " relevant status is not in history,now exist keys:"
  //      + status.keySet().stream().collect(Collectors.joining(",")));
  //    }
  //    tskStat.put(IParamContext.KEY_ASYN_JOB_COMPLETE, true);
  //    tskStat.put(IParamContext.KEY_ASYN_JOB_SUCCESS, execSuccess);
  //    status.put(jobName, tskStat);
  //    boolean[] allComplete = new boolean[]{true};
  //    boolean[] faild = new boolean[]{false};
  //    status.forEach((key, val) -> {
  //      JSONObject s = (JSONObject) val;
  //      if (s.getBoolean(IParamContext.KEY_ASYN_JOB_COMPLETE)) {
  //        if (!s.getBoolean(IParamContext.KEY_ASYN_JOB_SUCCESS)) {
  //          faild[0] = true;
  //        }
  //      } else {
  //        allComplete[0] = false;
  //      }
  //    });
  //
  //    WorkFlowBuildHistory updateHistory = new WorkFlowBuildHistory();
  //    updateHistory.setAsynSubTaskStatus(status.toJSONString());
  //    updateHistory.setLastVer(history.getLastVer() + 1);
  //    ExecResult execResult = null;
  //    if (faild[0]) {
  //      // 有任务失败了
  //      execResult = ExecResult.FAILD;
  //    } else if (allComplete[0]) {
  //      execResult = ExecResult.SUCCESS;
  //    }
  //
  //    if (execResult != null) {
  //      updateHistory.setState((byte) execResult.getValue());
  //      updateHistory.setEndTime(new Date());
  //    }
  //    WorkFlowBuildHistoryCriteria hq = new WorkFlowBuildHistoryCriteria();
  //    hq.createCriteria().andIdEqualTo(taskid).andLastVerEqualTo(history.getLastVer());
  //
  //    if (getHistoryDAO().updateByExampleSelective(updateHistory, hq) < 1) {
  //
  //      //  System.out.println("old lastVer:" + history.getLastVer() + ",new UpdateVersion:" + updateHistory
  //      .getLastVer
  //      //  ());
  //      updateAsynTaskState(taskid, jobName, execSuccess, ++tryCount);
  //    }
  //  }

  //  private void validateMaxCasRetryCount(Integer taskid, int tryCount) {
  //    try {
  //      if (tryCount > 0) {
  //        Thread.sleep(200);
  //      }
  //    } catch (Throwable e) {
  //    }
  //    if (tryCount > MAX_CAS_RETRY_COUNT) {
  //      throw new IllegalStateException("taskId:" + taskid + " exceed max try count " + MAX_CAS_RETRY_COUNT);
  //    }
  //  }

  /**
   * CAS更新，尝试4次
   *
   * @param taskid
   * @param execResult
   */
  private WorkFlowBuildHistory updateWfHistory(Integer taskid, final ExecResult execResult) {
    // validateMaxCasRetryCount(taskid, tryCount);

    WorkFlowBuildHistory history = getBuildHistory(taskid);
    WorkFlowBuildHistoryCriteria hq = new WorkFlowBuildHistoryCriteria();
    WorkFlowBuildHistoryCriteria.Criteria criteria = hq.createCriteria().andIdEqualTo(taskid);
    criteria.andLastVerEqualTo(history.getLastVer());
    WorkFlowBuildHistory upHistory = new WorkFlowBuildHistory();
    upHistory.setLastVer(history.getLastVer() + 1);

    //JSONObject jobState = null;
    //    if (asynJobsName != null && asynJobsName.length > 0) {
    //      JSONObject asynSubTaskStatus = new JSONObject();
    //      for (String jobName : asynJobsName) {
    //        jobState = new JSONObject();
    //        jobState.put(IParamContext.KEY_ASYN_JOB_COMPLETE, false);
    //        jobState.put(IParamContext.KEY_ASYN_JOB_SUCCESS, false);
    //        asynSubTaskStatus.put(jobName, jobState);
    //      }
    //      upHistory.setState((byte) ExecResult.ASYN_DOING.getValue());
    //      upHistory.setAsynSubTaskStatus(asynSubTaskStatus.toJSONString());
    //    } else {
    upHistory.setState((byte) execResult.getValue());
    upHistory.setEndTime(new Date());
    //}

    if (getHistoryDAO().updateByExampleSelective(upHistory, hq) < 1) {
      // updateWfHistory(taskid, execResult, asynJobsName, ++tryCount);
      throw new IllegalStateException("have not successful update workflow history");
    }

    history.setLastVer(upHistory.getLastVer());
    history.setState(upHistory.getState());
    history.setEndTime(upHistory.getEndTime());

    return history;
  }

  private WorkFlowBuildHistory getBuildHistory(Integer taskid) {
    WorkFlowBuildHistory history = this.getHistoryDAO().loadFromWriteDB(taskid);
    if (history == null) {
      throw new IllegalStateException("taskid:" + taskid + " relevant WorkFlowBuildHistory obj can not be null");
    }
    return history;
  }


  //  private DatasourceTable getTable(String tabName) {
  //    DatasourceTableCriteria query = new DatasourceTableCriteria();
  //    query.createCriteria().andNameEqualTo(tabName);
  //    List<DatasourceTable> tabList = this.getWorkflowDAOFacade().getDatasourceTableDAO().selectByExample(query);
  //    return tabList.stream().findFirst().get();
  //  }

  //  public static GitUtils.GitBranchInfo getBranch(WorkFlow workFlow) {
  //    RunEnvironment runtime = RunEnvironment.getSysRuntime();
  //    if (runtime == RunEnvironment.ONLINE) {
  //      return GitBranchInfo.$(GitUtils.GitBranch.MASTER);
  //    } else {
  //      // : GitBranchInfo.$(workFlow.getName());
  //      return GitBranchInfo.$(GitUtils.GitBranch.DEVELOP);
  //    }
  //  }

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
