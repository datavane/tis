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

import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.plugin.alert.AlertChannel;
import com.qlangtech.tis.plugin.alert.AlertTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, FlinkJobState> lastJobStates = new ConcurrentHashMap<>();

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
            List<FlinkJobInfo> runningJobs = getAllRunningFlinkJobs();

            for (FlinkJobInfo jobInfo : runningJobs) {
                checkJobStatus(jobInfo);
            }

            logger.debug("Flink jobs status check completed");
        } catch (Exception e) {
            logger.error("Error during Flink jobs monitoring", e);
        }
    }

    /**
     * 检查单个Job的状态
     */
    private void checkJobStatus(FlinkJobInfo jobInfo) {
        String jobName = jobInfo.getJobName();
        FlinkJobState currentState = jobInfo.getState();

        // 记录开始时间
        if (!jobStartTimes.containsKey(jobName) && currentState == FlinkJobState.RUNNING) {
            jobStartTimes.put(jobName, new Date());
        }

        // 获取上一次的状态
        FlinkJobState lastState = lastJobStates.get(jobName);

        // 判断是否需要报警
        if (shouldAlert(lastState, currentState)) {
            logger.info("Job [{}] state changed from [{}] to [{}], triggering alert",
                       jobName, lastState, currentState);
            doAlert(jobInfo);
        }

        // 更新状态
        lastJobStates.put(jobName, currentState);

        // 如果Job已完成或失败,清理开始时间记录
        if (currentState == FlinkJobState.FINISHED ||
            currentState == FlinkJobState.FAILED ||
            currentState == FlinkJobState.CANCELED) {
            jobStartTimes.remove(jobName);
        }
    }

    /**
     * 判断是否需要报警
     * 只有当状态从RUNNING变为FAILED、LOST、CANCELED时才报警
     *
     * @param lastState 上一次状态
     * @param currentState 当前状态
     * @return 是否需要报警
     */
    private boolean shouldAlert(FlinkJobState lastState, FlinkJobState currentState) {
        if (lastState == null) {
            // 第一次检测到该Job,不报警
            return false;
        }

        // 只有从RUNNING状态变为FAILED、LOST、CANCELED时才报警
        if (lastState == FlinkJobState.RUNNING) {
            return currentState == FlinkJobState.FAILED
                || currentState == FlinkJobState.LOST
                || currentState == FlinkJobState.CANCELED;
        }

        return false;
    }

    /**
     * 执行报警
     * 构建AlertTemplate并通过所有配置的AlertChannel发送报警
     */
    private void doAlert(FlinkJobInfo jobInfo) {
        try {
            // 构建AlertTemplate
            Date startTime = jobStartTimes.get(jobInfo.getJobName());
            Date endTime = new Date();

            AlertTemplate alertTemplate = AlertTemplate.builder()
                .title("TIS Flink Job 告警")
                .subject(String.format("Flink Job [%s] 状态异常", jobInfo.getJobName()))
                .jobName(jobInfo.getJobName())
                .status(jobInfo.getState().getDisplayName())
                .type(1)  // 1-任务状态
                .startTime(startTime)
                .endTime(endTime)
                .duration(startTime, endTime)
                .link(jobInfo.getWebUILink())
                .restart(false, 0)
                .build();

            // 获取所有配置的报警渠道
            List<AlertChannel> alertChannels = getConfiguredAlertChannels();

            if (alertChannels.isEmpty()) {
                logger.warn("No alert channels configured, skip sending alert for job [{}]",
                           jobInfo.getJobName());
                return;
            }

            // 通过每个报警渠道发送报警
            for (AlertChannel channel : alertChannels) {
                try {
                    logger.info("Sending alert via channel [{}] for job [{}]",
                               channel.identityValue(), jobInfo.getJobName());
                    channel.send(alertTemplate);
                } catch (Exception e) {
                    logger.error("Failed to send alert via channel [{}] for job [{}]",
                                channel.identityValue(), jobInfo.getJobName(), e);
                }
            }

        } catch (Exception e) {
            logger.error("Error during alert execution for job [{}]", jobInfo.getJobName(), e);
        }
    }

    /**
     * 获取所有配置的报警渠道
     */
    private List<AlertChannel> getConfiguredAlertChannels() {
        return ParamsConfig.getItems(AlertChannel.KEY_CATEGORY);
    }

    /**
     * 获取所有运行中的Flink Job
     * TODO: 这里需要根据TIS实际的API实现来获取Flink Job列表
     * 目前返回空列表作为占位实现
     */
    private List<FlinkJobInfo> getAllRunningFlinkJobs() {
        // TODO: 调用TIS的API获取所有Flink Job
        // 可能的实现方式:
        // 1. 通过DataX实例管理器获取所有运行中的DataX任务
        // 2. 过滤出使用Flink引擎的任务
        // 3. 获取每个任务的Flink Job信息

        return List.of(); // 暂时返回空列表
    }

    /**
     * Flink Job信息类
     * 封装单个Flink Job的状态信息
     */
    public static class FlinkJobInfo {
        private String jobName;
        private FlinkJobState state;
        private String webUILink;

        public FlinkJobInfo(String jobName, FlinkJobState state, String webUILink) {
            this.jobName = jobName;
            this.state = state;
            this.webUILink = webUILink;
        }

        public String getJobName() {
            return jobName;
        }

        public FlinkJobState getState() {
            return state;
        }

        public String getWebUILink() {
            return webUILink;
        }
    }
}
