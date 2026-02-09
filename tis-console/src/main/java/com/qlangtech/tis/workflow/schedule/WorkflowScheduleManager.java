package com.qlangtech.tis.workflow.schedule;

//import com.qlangtech.tis.dag.TISActorSystem;
import com.qlangtech.tis.workflow.dao.IWorkFlowBuildHistoryDAO;
import com.qlangtech.tis.workflow.dao.IWorkFlowDAO;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 工作流定时调度管理器
 * 负责管理工作流定时调度任务的生命周期
 *
 * 核心职责：
 * 1. 注册定时调度任务
 * 2. 移除定时调度任务
 * 3. 更新 Cron 表达式
 * 4. 启动时加载所有启用定时调度的工作流
 * 5. 跳过已在运行的工作流
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class WorkflowScheduleManager {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowScheduleManager.class);

    /**
     * Quartz 调度器配置
     */
    private final QuartzConfig quartzConfig;

    /**
     * Actor System
     */
  //  private final TISActorSystem actorSystem;

    /**
     * DAO 依赖
     */
    private final IWorkFlowDAO workflowDAO;
    private final IWorkFlowBuildHistoryDAO buildHistoryDAO;

    public WorkflowScheduleManager(QuartzConfig quartzConfig,
                                 //  TISActorSystem actorSystem,
                                   IWorkFlowDAO workflowDAO,
                                   IWorkFlowBuildHistoryDAO buildHistoryDAO) {
        this.quartzConfig = quartzConfig;
      //  this.actorSystem = actorSystem;
        this.workflowDAO = workflowDAO;
        this.buildHistoryDAO = buildHistoryDAO;
    }

    /**
     * 初始化调度管理器
     * 启动时加载所有启用定时调度的工作流
     *
     * 实现步骤：
     * 1. 初始化 Quartz Scheduler
     * 2. 查询所有启用定时调度的工作流
     * 3. 为每个工作流注册调度任务
     * 4. 记录加载结果
     */
    public void initialize() {
        logger.info("Initializing WorkflowScheduleManager...");

        try {
            // TODO: 实现初始化逻辑
            // 1. 初始化 Quartz Scheduler
            // quartzConfig.initialize();

            // 2. 查询所有启用定时调度的工作流
            // List<WorkFlow> scheduledWorkflows = workflowDAO.selectScheduledWorkflows();
            // logger.info("Found {} scheduled workflows", scheduledWorkflows.size());

            // 3. 为每个工作流注册调度任务
            // int successCount = 0;
            // for (WorkFlow workflow : scheduledWorkflows) {
            //     try {
            //         registerSchedule(workflow.getId(), workflow.getName(), workflow.getScheduleCron());
            //         successCount++;
            //     } catch (Exception e) {
            //         logger.error("Failed to register schedule for workflow: id={}, name={}",
            //                 workflow.getId(), workflow.getName(), e);
            //     }
            // }

            // logger.info("WorkflowScheduleManager initialized: {}/{} workflows registered",
            //         successCount, scheduledWorkflows.size());

            logger.info("WorkflowScheduleManager initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize WorkflowScheduleManager", e);
            throw new IllegalStateException("Failed to initialize WorkflowScheduleManager", e);
        }
    }

    /**
     * 注册定时调度任务
     *
     * 实现步骤：
     * 1. 验证 Cron 表达式
     * 2. 创建 JobDetail
     * 3. 创建 CronTrigger
     * 4. 将 Job 和 Trigger 注册到 Scheduler
     * 5. 记录日志
     *
     * @param workflowId   工作流 ID
     * @param workflowName 工作流名称
     * @param cronExpr     Cron 表达式
     * @throws SchedulerException 调度异常
     */
    public void registerSchedule(Integer workflowId, String workflowName, String cronExpr)
            throws SchedulerException {
        logger.info("Registering schedule: workflowId={}, name={}, cron={}",
                workflowId, workflowName, cronExpr);

        try {
            // TODO: 实现注册逻辑
            // 1. 验证 Cron 表达式
            // if (!CronExpression.isValidExpression(cronExpr)) {
            //     throw new IllegalArgumentException("Invalid cron expression: " + cronExpr);
            // }

            // 2. 创建 JobDetail
            // JobKey jobKey = new JobKey("workflow-" + workflowId, "TIS-Workflow");
            // JobDetail jobDetail = JobBuilder.newJob(WorkflowScheduleJob.class)
            //         .withIdentity(jobKey)
            //         .withDescription("Scheduled workflow: " + workflowName)
            //         .usingJobData(WorkflowScheduleJob.KEY_WORKFLOW_ID, workflowId)
            //         .usingJobData(WorkflowScheduleJob.KEY_WORKFLOW_NAME, workflowName)
            //         .build();

            // 3. 将依赖放入 JobDataMap
            // JobDataMap dataMap = jobDetail.getJobDataMap();
            // dataMap.put(WorkflowScheduleJob.KEY_ACTOR_SYSTEM, actorSystem);
            // dataMap.put(WorkflowScheduleJob.KEY_WORKFLOW_DAO, workflowDAO);
            // dataMap.put(WorkflowScheduleJob.KEY_BUILD_HISTORY_DAO, buildHistoryDAO);

            // 4. 创建 CronTrigger
            // TriggerKey triggerKey = new TriggerKey("workflow-trigger-" + workflowId, "TIS-Workflow");
            // CronTrigger trigger = TriggerBuilder.newTrigger()
            //         .withIdentity(triggerKey)
            //         .withSchedule(CronScheduleBuilder.cronSchedule(cronExpr))
            //         .build();

            // 5. 注册到 Scheduler
            // Scheduler scheduler = quartzConfig.getScheduler();
            // scheduler.scheduleJob(jobDetail, trigger);

            logger.info("Schedule registered successfully: workflowId={}", workflowId);

        } catch (Exception e) {
            logger.error("Failed to register schedule: workflowId={}, name={}",
                    workflowId, workflowName, e);
            throw new SchedulerException("Failed to register schedule", e);
        }
    }

    /**
     * 移除定时调度任务
     *
     * 实现步骤：
     * 1. 构造 JobKey
     * 2. 从 Scheduler 中删除 Job
     * 3. 记录日志
     *
     * @param workflowId 工作流 ID
     * @throws SchedulerException 调度异常
     */
    public void unregisterSchedule(Integer workflowId) throws SchedulerException {
        logger.info("Unregistering schedule: workflowId={}", workflowId);

        try {
            // TODO: 实现移除逻辑
            // 1. 构造 JobKey
            // JobKey jobKey = new JobKey("workflow-" + workflowId, "TIS-Workflow");

            // 2. 从 Scheduler 中删除 Job
            // Scheduler scheduler = quartzConfig.getScheduler();
            // boolean deleted = scheduler.deleteJob(jobKey);

            // if (deleted) {
            //     logger.info("Schedule unregistered successfully: workflowId={}", workflowId);
            // } else {
            //     logger.warn("Schedule not found: workflowId={}", workflowId);
            // }

            logger.info("Schedule unregistered: workflowId={}", workflowId);

        } catch (Exception e) {
            logger.error("Failed to unregister schedule: workflowId={}", workflowId, e);
            throw new SchedulerException("Failed to unregister schedule", e);
        }
    }

    /**
     * 更新 Cron 表达式
     *
     * 实现步骤：
     * 1. 移除旧的调度任务
     * 2. 注册新的调度任务
     * 3. 记录日志
     *
     * @param workflowId   工作流 ID
     * @param workflowName 工作流名称
     * @param newCronExpr  新的 Cron 表达式
     * @throws SchedulerException 调度异常
     */
    public void updateSchedule(Integer workflowId, String workflowName, String newCronExpr)
            throws SchedulerException {
        logger.info("Updating schedule: workflowId={}, name={}, newCron={}",
                workflowId, workflowName, newCronExpr);

        try {
            // TODO: 实现更新逻辑
            // 方式一：先删除再注册
            // unregisterSchedule(workflowId);
            // registerSchedule(workflowId, workflowName, newCronExpr);

            // 方式二：使用 rescheduleJob（更高效）
            // TriggerKey triggerKey = new TriggerKey("workflow-trigger-" + workflowId, "TIS-Workflow");
            // CronTrigger newTrigger = TriggerBuilder.newTrigger()
            //         .withIdentity(triggerKey)
            //         .withSchedule(CronScheduleBuilder.cronSchedule(newCronExpr))
            //         .build();
            //
            // Scheduler scheduler = quartzConfig.getScheduler();
            // scheduler.rescheduleJob(triggerKey, newTrigger);

            logger.info("Schedule updated successfully: workflowId={}", workflowId);

        } catch (Exception e) {
            logger.error("Failed to update schedule: workflowId={}, name={}",
                    workflowId, workflowName, e);
            throw new SchedulerException("Failed to update schedule", e);
        }
    }

    /**
     * 检查工作流是否已在运行
     * 用于跳过已在运行的工作流
     *
     * @param workflowId 工作流 ID
     * @return true 如果工作流已在运行
     */
    public boolean isWorkflowRunning(Integer workflowId) {
        try {
            // TODO: 实现检查逻辑
            // List<WorkFlowBuildHistory> runningInstances = buildHistoryDAO.selectRunningInstances(workflowId);
            // return !runningInstances.isEmpty();

            return false;

        } catch (Exception e) {
            logger.error("Failed to check workflow running status: workflowId={}", workflowId, e);
            return false;
        }
    }

    /**
     * 获取工作流的下次执行时间
     *
     * @param workflowId 工作流 ID
     * @return 下次执行时间（毫秒时间戳），如果未找到则返回 null
     */
    public Long getNextFireTime(Integer workflowId) {
        try {
            // TODO: 实现获取下次执行时间
            // JobKey jobKey = new JobKey("workflow-" + workflowId, "TIS-Workflow");
            // Scheduler scheduler = quartzConfig.getScheduler();
            //
            // List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            // if (!triggers.isEmpty()) {
            //     Date nextFireTime = triggers.get(0).getNextFireTime();
            //     return nextFireTime != null ? nextFireTime.getTime() : null;
            // }

            return null;

        } catch (Exception e) {
            logger.error("Failed to get next fire time: workflowId={}", workflowId, e);
            return null;
        }
    }

    /**
     * 优雅关闭调度管理器
     */
    public void shutdown() {
        logger.info("Shutting down WorkflowScheduleManager...");

        try {
            // TODO: 实现优雅关闭
            // quartzConfig.shutdown();

            logger.info("WorkflowScheduleManager shutdown completed");

        } catch (Exception e) {
            logger.error("Failed to shutdown WorkflowScheduleManager", e);
        }
    }

    /**
     * 暂停工作流调度
     *
     * @param workflowId 工作流 ID
     * @throws SchedulerException 调度异常
     */
    public void pauseSchedule(Integer workflowId) throws SchedulerException {
        logger.info("Pausing schedule: workflowId={}", workflowId);

        try {
            // TODO: 实现暂停逻辑
            // JobKey jobKey = new JobKey("workflow-" + workflowId, "TIS-Workflow");
            // Scheduler scheduler = quartzConfig.getScheduler();
            // scheduler.pauseJob(jobKey);

            logger.info("Schedule paused: workflowId={}", workflowId);

        } catch (Exception e) {
            logger.error("Failed to pause schedule: workflowId={}", workflowId, e);
            throw new SchedulerException("Failed to pause schedule", e);
        }
    }

    /**
     * 恢复工作流调度
     *
     * @param workflowId 工作流 ID
     * @throws SchedulerException 调度异常
     */
    public void resumeSchedule(Integer workflowId) throws SchedulerException {
        logger.info("Resuming schedule: workflowId={}", workflowId);

        try {
            // TODO: 实现恢复逻辑
            // JobKey jobKey = new JobKey("workflow-" + workflowId, "TIS-Workflow");
            // Scheduler scheduler = quartzConfig.getScheduler();
            // scheduler.resumeJob(jobKey);

            logger.info("Schedule resumed: workflowId={}", workflowId);

        } catch (Exception e) {
            logger.error("Failed to resume schedule: workflowId={}", workflowId, e);
            throw new SchedulerException("Failed to resume schedule", e);
        }
    }
}
