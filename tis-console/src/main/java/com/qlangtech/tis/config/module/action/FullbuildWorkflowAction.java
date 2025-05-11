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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.exec.AbstractExecContext;
import com.qlangtech.tis.exec.impl.DataXPipelineExecContext;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.impl.WorkflowExecContext;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.CreateNewTaskResult;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.offline.module.action.OfflineDatasourceAction;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.realtime.yarn.rpc.SynResTarget;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.stream.Collectors;

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
   * 接收Powerjob发送过来的初始化触发任务，主要目标在TIS中进行必要的初始化工作
   *
   * @param context
   * @see com.qlangtech.tis.coredefine.module.action.DataxAction
   * @see OfflineDatasourceAction
   */
  public void doInitializeTriggerTask(Context context) {
    // 校验参数必须有
    this.getLong(DataxUtils.POWERJOB_WORKFLOW_INSTANCE_ID);
    Rundata rundata = this.getRundata();
    if (this.getBoolean(TIS_WORK_FLOW_CHANNEL)) {
      rundata.forwardTo("offline", "offline_datasource_action", "execute_workflow");
    } else {
      rundata.forwardTo("coredefine", "datax_action", "trigger_fullbuild_task");
    }
  }

  /**
   * assemble 节点接收到来自console节点的触发任务，开始执行需要创建一个new的workflowbuildhistory记录
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
  public void doCreateNewTask(Context context) {

    final TriggerType triggerType = TriggerType.parse(this.getInt(IFullBuildContext.KEY_TRIGGER_TYPE));
    Application app = null;
    // appname 可以为空
    String appname = this.getString(IFullBuildContext.KEY_APP_NAME);
    Integer workflowId = this.getInt(IFullBuildContext.KEY_WORKFLOW_ID, null, false);

    AbstractExecContext execContext = null;
    if (StringUtils.isEmpty(appname)) {
      if (workflowId == null) {
        throw new IllegalStateException("workflowId can not be null");
      }
      execContext = new WorkflowExecContext(workflowId, 0l);
    } else {
      execContext = new DataXPipelineExecContext(appname, 0l);
    }

    // execContext.setWorkflowId(workflowId);

    execContext.setExecutePhaseRange(new ExecutePhaseRange(
      FullbuildPhase.parse(getInt(IParamContext.COMPONENT_START, FullbuildPhase.FullDump.getValue()))
      , FullbuildPhase.parse(getInt(IParamContext.COMPONENT_END, FullbuildPhase.IndexBackFlow.getValue()))));
    CreateNewTaskResult newTaskResult = this.createNewDataXTask(execContext, triggerType);
    // 生成一个新的taskid
    this.setBizResult(context, newTaskResult);
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
//      WorkFlowBuildHistory latestSuccessWorkflowHistory = this.getLatestSuccessWorkflowHistory(SynResTarget.pipeline(appName));
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
    Integer taskid = this.getInt(JobCommon.KEY_TASK_ID);
    // 执行结果
    ExecResult execResult = ExecResult.parse(this.getInt(IParamContext.KEY_EXEC_RESULT));
    String[] asynJobsName = this.getStringArray(IParamContext.KEY_ASYN_JOB_NAME);

    updateWfHistory(taskid, execResult, asynJobsName, 0);
  }

  /**
   * 接收异步执行任务执行状态
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = false)
  public void doFeedbackAsynTaskStatus(Context context) {
    Integer taskid = this.getInt(JobCommon.KEY_TASK_ID);
    String jobName = this.getString(IParamContext.KEY_ASYN_JOB_NAME);
    boolean execSuccess = this.getBoolean(IParamContext.KEY_ASYN_JOB_SUCCESS);

    this.updateAsynTaskState(taskid, jobName, execSuccess, 0);
    this.setBizResult(context, new CreateNewTaskResult(taskid, null));
  }

  public static int MAX_CAS_RETRY_COUNT = 5;

  private void updateAsynTaskState(Integer taskid, String jobName, boolean execSuccess, int tryCount) {
    validateMaxCasRetryCount(taskid, tryCount);
    final WorkFlowBuildHistory history = getBuildHistory(taskid);

    if (ExecResult.ASYN_DOING != ExecResult.parse(history.getState())) {
      updateAsynTaskState(taskid, jobName, execSuccess, ++tryCount);
      return;
    }

    JSONObject status = JSON.parseObject(history.getAsynSubTaskStatus());
    JSONObject tskStat = status.getJSONObject(jobName);
    if (tskStat == null) {
      throw new IllegalStateException("jobName:" + jobName
        + " relevant status is not in history,now exist keys:"
        + status.keySet().stream().collect(Collectors.joining(",")));
    }
    tskStat.put(IParamContext.KEY_ASYN_JOB_COMPLETE, true);
    tskStat.put(IParamContext.KEY_ASYN_JOB_SUCCESS, execSuccess);
    status.put(jobName, tskStat);
    boolean[] allComplete = new boolean[]{true};
    boolean[] faild = new boolean[]{false};
    status.forEach((key, val) -> {
      JSONObject s = (JSONObject) val;
      if (s.getBoolean(IParamContext.KEY_ASYN_JOB_COMPLETE)) {
        if (!s.getBoolean(IParamContext.KEY_ASYN_JOB_SUCCESS)) {
          faild[0] = true;
        }
      } else {
        allComplete[0] = false;
      }
    });

    WorkFlowBuildHistory updateHistory = new WorkFlowBuildHistory();
    updateHistory.setAsynSubTaskStatus(status.toJSONString());
    updateHistory.setLastVer(history.getLastVer() + 1);
    ExecResult execResult = null;
    if (faild[0]) {
      // 有任务失败了
      execResult = ExecResult.FAILD;
    } else if (allComplete[0]) {
      execResult = ExecResult.SUCCESS;
    }

    if (execResult != null) {
      updateHistory.setState((byte) execResult.getValue());
      updateHistory.setEndTime(new Date());
    }
    WorkFlowBuildHistoryCriteria hq = new WorkFlowBuildHistoryCriteria();
    hq.createCriteria().andIdEqualTo(taskid).andLastVerEqualTo(history.getLastVer());

    if (getHistoryDAO().updateByExampleSelective(updateHistory, hq) < 1) {

      //  System.out.println("old lastVer:" + history.getLastVer() + ",new UpdateVersion:" + updateHistory.getLastVer());
      updateAsynTaskState(taskid, jobName, execSuccess, ++tryCount);
    }
  }

  private void validateMaxCasRetryCount(Integer taskid, int tryCount) {
    try {
      if (tryCount > 0) {
        Thread.sleep(200);
      }
    } catch (Throwable e) {
    }
    if (tryCount > MAX_CAS_RETRY_COUNT) {
      throw new IllegalStateException("taskId:" + taskid + " exceed max try count " + MAX_CAS_RETRY_COUNT);
    }
  }

  /**
   * CAS更新，尝试4次
   *
   * @param taskid
   * @param execResult
   * @param asynJobsName
   * @param tryCount
   */
  private void updateWfHistory(Integer taskid, final ExecResult execResult, String[] asynJobsName, int tryCount) {
    validateMaxCasRetryCount(taskid, tryCount);

    WorkFlowBuildHistory history = getBuildHistory(taskid);
    WorkFlowBuildHistoryCriteria hq = new WorkFlowBuildHistoryCriteria();
    WorkFlowBuildHistoryCriteria.Criteria criteria = hq.createCriteria().andIdEqualTo(taskid);
    criteria.andLastVerEqualTo(history.getLastVer());
    WorkFlowBuildHistory upHistory = new WorkFlowBuildHistory();
    upHistory.setLastVer(history.getLastVer() + 1);

    JSONObject jobState = null;
    if (asynJobsName != null && asynJobsName.length > 0) {
      JSONObject asynSubTaskStatus = new JSONObject();
      for (String jobName : asynJobsName) {
        jobState = new JSONObject();
        jobState.put(IParamContext.KEY_ASYN_JOB_COMPLETE, false);
        jobState.put(IParamContext.KEY_ASYN_JOB_SUCCESS, false);
        asynSubTaskStatus.put(jobName, jobState);
      }
      upHistory.setState((byte) ExecResult.ASYN_DOING.getValue());
      upHistory.setAsynSubTaskStatus(asynSubTaskStatus.toJSONString());
    } else {
      upHistory.setState((byte) execResult.getValue());
      upHistory.setEndTime(new Date());
    }

    if (getHistoryDAO().updateByExampleSelective(upHistory, hq) < 1) {
      updateWfHistory(taskid, execResult, asynJobsName, ++tryCount);
    }
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
