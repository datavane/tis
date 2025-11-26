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

package com.qlangtech.tis.alert;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.config.flink.JobManagerAddress;
import com.qlangtech.tis.coredefine.module.action.IFlinkIncrJobStatus;
import com.qlangtech.tis.coredefine.module.action.IndexIncrStatus;
import com.qlangtech.tis.coredefine.module.action.impl.FlinkJobDeploymentDetails;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.DefaultDataXProcessorManipulate;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.lang.ErrorValue;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.alert.AlertChannel;
import com.qlangtech.tis.plugin.alert.AlertTemplate;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.qlangtech.tis.config.flink.IFlinkClusterConfig.KEY_JOB_MANAGER_ADDRESS;
import static com.qlangtech.tis.coredefine.module.action.CoreAction.getIndexIncrStatus;

/**
 * Flink Job 监控器
 * 参照StreamPark的FlinkAppHttpWatcher实现
 * 定期检查Flink Job状态,并在状态变化时触发报警
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/16
 */
//@Component
public class FlinkJobsMonitor implements InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(FlinkJobsMonitor.class);

  /**
   * 记录上一次的Job状态,用于判断状态是否变化
   * Key: JobName, Value: FlinkJobState
   */
  private final Map<String, IFlinkIncrJobStatus.State> lastJobStates = new ConcurrentHashMap<>();

  /**
   * 记录Job的开始时间
   * Key: JobName, Value: StartTime
   */
  private final Map<String, Date> jobStartTimes = new ConcurrentHashMap<>();

  @Override
  public void afterPropertiesSet() throws Exception {
    logger.info("FlinkJobsMonitor initialized successfully");
  }

  /**
   * 定时任务:每5秒执行一次
   * 使用@Scheduled注解实现定时调度
   */
  // @Scheduled(fixedDelay = 5000, initialDelay = 10000)
  public void executeTask() {
    try {
      logger.debug("Starting Flink jobs status check...");

      // 获取所有运行中的Flink Job
      List<FlinkJobDeploymentDetails> runningJobs = getAllRunningFlinkJobs();

      for (FlinkJobDeploymentDetails jobInfo : runningJobs) {
        checkJobStatus(jobInfo);
      }

      logger.debug("Flink jobs status check completed");
    } catch (Exception e) {
      TisException.ErrMsg errMsg = TisException.getErrMsg(e);
      ErrorValue errCode = errMsg.getErrCode();
      if (errCode != null) {
        if (errCode.getCode() == TisException.ErrorCode.FLINK_INSTANCE_LOSS_OF_CONTACT) {
          this.doAlert(createNoneDeployment(errCode));
        }
      } else {
        logger.error("Error during Flink jobs monitoring", e);
      }

    }
  }

  private static FlinkJobDeploymentDetails createNoneDeployment(ErrorValue errCode) {
    return FlinkJobDeploymentDetails.noneState(
      DataXName.createDataXPipeline(errCode.getPayload(IFullBuildContext.KEY_APP_NAME))
      , () -> Optional.ofNullable((JobManagerAddress) errCode.getPayload(KEY_JOB_MANAGER_ADDRESS))
        .orElseGet(() -> new JobManagerAddress("127.0.0.1", 8081)), new NoneFlinkIncrJobStatus());
  }

  private static class NoneFlinkIncrJobStatus implements IFlinkIncrJobStatus<Object> {
    @Override
    public State getState() {
      return State.DISAPPEAR;
    }

    @Override
    public Object createNewJob(Object o) {
      return null;
    }

    @Override
    public Object getLaunchJobID() {
      return null;
    }

    @Override
    public void relaunch(Object o) {

    }

    @Override
    public void addSavePoint(String savepointDirectory, State state) {

    }

    @Override
    public void discardSavepoint(String savepointDirectory) {

    }

    @Override
    public void stop(String savepointDirectory) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public Optional<FlinkSavepoint> containSavepoint(String path) {
      return Optional.empty();
    }

    @Override
    public void setState(State state) {

    }

    @Override
    public List<FlinkSavepoint> getSavepointPaths() {
      return List.of();
    }
  }

  /**
   * 检查单个Job的状态
   */
  private void checkJobStatus(FlinkJobDeploymentDetails jobInfo) {
    String jobName = jobInfo.getJobName();
    IFlinkIncrJobStatus.State currentState = jobInfo.getIncrJobStatus().getState();

    // 记录开始时间
    if (!jobStartTimes.containsKey(jobName) && currentState == IFlinkIncrJobStatus.State.RUNNING) {
      jobStartTimes.put(jobName, new Date());
    }

    // 获取上一次的状态
    IFlinkIncrJobStatus.State lastState = lastJobStates.get(jobName);

    // 判断是否需要报警
    if (shouldAlert(lastState, currentState)) {
      logger.info("Job [{}] state changed from [{}] to [{}], triggering alert",
        jobName, lastState, currentState);
      doAlert(jobInfo);
    }

    // 更新状态
    lastJobStates.put(jobName, currentState);

    // 如果Job已完成或失败,清理开始时间记录
    if (currentState != IFlinkIncrJobStatus.State.RUNNING) {
      jobStartTimes.remove(jobName);
    }
  }

  /**
   * 判断是否需要报警
   * 只有当状态从RUNNING变为FAILED、LOST、CANCELED时才报警
   *
   * @param lastState    上一次状态
   * @param currentState 当前状态
   * @return 是否需要报警
   */
  private boolean shouldAlert(IFlinkIncrJobStatus.State lastState, IFlinkIncrJobStatus.State currentState) {
    if (lastState == null) {
      // 第一次检测到该Job,不报警
      return false;
    }

    // 只有从RUNNING状态变为FAILED、LOST、CANCELED时才报警
    if (lastState == IFlinkIncrJobStatus.State.RUNNING) {
      return currentState == IFlinkIncrJobStatus.State.FAILED
        || currentState == IFlinkIncrJobStatus.State.DISAPPEAR
        || currentState == IFlinkIncrJobStatus.State.NONE;
    }

    return false;
  }

  /**
   * 执行报警
   * 构建AlertTemplate并通过所有配置的AlertChannel发送报警
   */
  private void doAlert(FlinkJobDeploymentDetails jobInfo) {
    try {
      // 构建AlertTemplate
      Date startTime = jobStartTimes.get(jobInfo.getJobName());
      Date endTime = new Date();

      AlertTemplate alertTemplate = AlertTemplate.builder()
        .title("TIS Flink Job 告警")
        .subject(String.format("Flink Job [%s] 状态异常", jobInfo.getJobName()))
        .jobName(jobInfo.getJobName())
        .status(String.valueOf(jobInfo.getIncrJobStatus().getState()))
        .type(1)  // 1-任务状态
        .startTime(startTime)
        .endTime(endTime)
        .duration(startTime, endTime)
        .link(jobInfo.getJobManagerUrl())
        .restart(false, 0)
        .build();

      Pair<List<AlertChannel>, DefaultDataXProcessorManipulate.MonitorForEventsManager>
        pair = this.getPipelineAlertChannels(jobInfo.getJobName());
      // 获取所有配置的报警渠道
      List<AlertChannel> alertChannels = pair.getKey();

      if (alertChannels.isEmpty()) {
        logger.warn("No alert channels configured, skip sending alert for job [{}]",
          jobInfo.getJobName());
        return;
      }

      boolean hasSuccess = false;
      // 通过每个报警渠道发送报警
      for (AlertChannel channel : alertChannels) {
        try {
          logger.info("Sending alert via channel [{}] for job [{}]",
            channel.identityValue(), jobInfo.getJobName());
          channel.send(alertTemplate);
          hasSuccess = true;
        } catch (Exception e) {
          logger.error("Failed to send alert via channel [{}] for job [{}]",
            channel.identityValue(), jobInfo.getJobName(), e);
        }
      }
      if (hasSuccess) {
        // 用于前端展示
        pair.getValue().addSendCount();
      }
    } catch (Exception e) {
      logger.error("Error during alert execution for job [{}]", jobInfo.getJobName(), e);
    }
  }

  /**
   * 获取所有配置的报警渠道
   */
  private Pair<List<AlertChannel>, DefaultDataXProcessorManipulate.MonitorForEventsManager> getPipelineAlertChannels(String pipelineName) {
    DefaultDataXProcessorManipulate.DataXProcessorTemplateManipulateStore
      manipulateStore = DefaultDataXProcessorManipulate.getManipulateStore(pipelineName);
    DefaultDataXProcessorManipulate.MonitorForEventsManager monitorManager
      = manipulateStore.getAlertManager();
    if (monitorManager == null) {
      return Pair.of(Collections.emptyList(), null);
    }
    if (!monitorManager.isActivate()) {
      return Pair.of(Collections.emptyList(), monitorManager);
    }
    return Pair.of(monitorManager.getAlertChannels(), monitorManager);
  }

  public static List<String> loadExistRunningIncrPipeline() {
    File appDir = new File(TIS.pluginCfgRoot, StoreResourceType.DataApp.getType());
    String[] subDirs = null;
    if (!appDir.exists() || (subDirs = appDir.list()) == null) {
      return Collections.emptyList();
    }
    List<String> runningIncrPipelines = Lists.newArrayList();
    for (String subDir : subDirs) {
      if (new File(appDir, subDir + File.separator + IFlinkIncrJobStatus.KEY_INCR_JOB_LOG).exists()) {
        runningIncrPipelines.add(subDir);
      }
    }
    return runningIncrPipelines;
  }

  public static void main(String[] args) {
    System.out.println(String.join(" ,", loadExistRunningIncrPipeline()));
  }

  /**
   * 获取所有运行中的Flink Job
   * TODO: 这里需要根据TIS实际的API实现来获取Flink Job列表
   * 目前返回空列表作为占位实现
   */
  private List<FlinkJobDeploymentDetails> getAllRunningFlinkJobs() throws Exception {
    // TODO: 调用TIS的API获取所有Flink Job
    // 可能的实现方式:
    // 1. 通过DataX实例管理器获取所有运行中的DataX任务
    // 2. 过滤出使用Flink引擎的任务
    // 3. 获取每个任务的Flink Job信息

    List<FlinkJobDeploymentDetails> result = Lists.newArrayList();
    for (String pipelineName : loadExistRunningIncrPipeline()) {
      IndexIncrStatus incrStatus = new IndexIncrStatus();
      IndexIncrStatus indexIncrStatus
        = getIndexIncrStatus(IControlMsgHandler.namedContext(pipelineName), incrStatus, false);

      if (indexIncrStatus.getFlinkJobDetail() == null) {
        ErrorValue errCode = ErrorValue.create(
          TisException.ErrorCode.FLINK_INSTANCE_LOSS_OF_CONTACT, IFullBuildContext.KEY_APP_NAME, pipelineName);
        indexIncrStatus.setFlinkJobDetail(createNoneDeployment(errCode));
      }

      result.add(Objects.requireNonNull(
        indexIncrStatus.getFlinkJobDetail(), "pipelineName:" + pipelineName + " relevant FlinkJobDetail can not be null"));
    }

    return result; // 暂时返回空列表
  }

  /**
   * Flink Job信息类
   * 封装单个Flink Job的状态信息
   */
//  public static class FlinkJobInfo {
//    private String jobName;
//    private IFlinkIncrJobStatus.State state;
//    private String webUILink;
//
//    public FlinkJobInfo(String jobName, IFlinkIncrJobStatus.State state, String webUILink) {
//      this.jobName = jobName;
//      this.state = state;
//      this.webUILink = webUILink;
//    }
//
//    public String getJobName() {
//      return jobName;
//    }
//
//    public IFlinkIncrJobStatus.State getState() {
//      return state;
//    }
//
//    public String getWebUILink() {
//      return webUILink;
//    }
//  }
}
