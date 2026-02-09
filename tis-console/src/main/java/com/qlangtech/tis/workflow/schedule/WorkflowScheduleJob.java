package com.qlangtech.tis.workflow.schedule;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工作流定时调度 Job
 * 实现 Quartz Job 接口，负责定时触发工作流执行
 *
 * 核心职责：
 * 1. 从 JobDataMap 中获取工作流 ID
 * 2. 检查工作流是否已在运行（跳过逻辑）
 * 3. 创建工作流实例
 * 4. 发送 StartWorkflow 消息到 DAGSchedulerActor
 * 5. 异常处理和日志记录
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class WorkflowScheduleJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowScheduleJob.class);

    /**
     * JobDataMap 中的 key
     */
    public static final String KEY_WORKFLOW_ID = "workflowId";
    public static final String KEY_WORKFLOW_NAME = "workflowName";
    public static final String KEY_ACTOR_SYSTEM = "actorSystem";
    public static final String KEY_WORKFLOW_DAO = "workflowDAO";
    public static final String KEY_BUILD_HISTORY_DAO = "buildHistoryDAO";

    /**
     * 执行定时任务
     *
     * 实现步骤：
     * 1. 从 JobDataMap 获取工作流信息和依赖
     * 2. 查询工作流是否已在运行
     * 3. 如果已在运行，跳过本次执行
     * 4. 如果未运行，创建新实例并触发执行
     * 5. 发送 StartWorkflow 消息到 DAGSchedulerActor
     *
     * @param context Job 执行上下文
     * @throws JobExecutionException 执行异常
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        // 获取工作流信息
        Integer workflowId = dataMap.getInt(KEY_WORKFLOW_ID);
        String workflowName = dataMap.getString(KEY_WORKFLOW_NAME);

        logger.info("Executing scheduled workflow: id={}, name={}", workflowId, workflowName);

        try {
            // TODO: 实现定时触发逻辑
            // 1. 获取依赖
            // TISActorSystem actorSystem = (TISActorSystem) dataMap.get(KEY_ACTOR_SYSTEM);
            // IWorkFlowDAO workflowDAO = (IWorkFlowDAO) dataMap.get(KEY_WORKFLOW_DAO);
            // IWorkFlowBuildHistoryDAO buildHistoryDAO = (IWorkFlowBuildHistoryDAO) dataMap.get(KEY_BUILD_HISTORY_DAO);

            // 2. 检查工作流是否已在运行
            // List<WorkFlowBuildHistory> runningInstances = buildHistoryDAO.selectRunningInstances(workflowId);
            // if (!runningInstances.isEmpty()) {
            //     logger.info("Workflow is already running, skip this execution: workflowId={}, instanceId={}",
            //             workflowId, runningInstances.get(0).getId());
            //     return;
            // }

            // 3. 加载工作流定义
            // WorkFlow workflow = workflowDAO.selectByPrimaryKey(workflowId);
            // if (workflow == null) {
            //     logger.error("Workflow not found: workflowId={}", workflowId);
            //     return;
            // }

            // 4. 创建工作流实例
            // WorkFlowBuildHistory instance = new WorkFlowBuildHistory();
            // instance.setWorkFlowId(workflowId);
            // instance.setCreateTime(new Date());
            // instance.setOpTime(new Date());
            // instance.setInstanceStatus(InstanceStatus.WAITING.getDesc());
            // instance.setTriggerType("SCHEDULE"); // 定时触发
            // buildHistoryDAO.insertSelective(instance);

            // 5. 发送 StartWorkflow 消息
            // ActorRef dagScheduler = actorSystem.getDagSchedulerActor();
            // StartWorkflow startMsg = new StartWorkflow(
            //     instance.getId(),
            //     workflowId,
            //     workflow.getDagSpecPath()
            // );
            // dagScheduler.tell(startMsg, ActorRef.noSender());

            logger.info("Workflow scheduled successfully: workflowId={}, instanceId={}", workflowId, "TODO");

        } catch (Exception e) {
            logger.error("Failed to execute scheduled workflow: workflowId={}, name={}",
                    workflowId, workflowName, e);
            throw new JobExecutionException("Failed to execute scheduled workflow", e);
        }
    }
}
