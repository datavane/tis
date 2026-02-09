///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.qlangtech.tis.config.module.action;
//
//import akka.actor.ActorRef;
//import akka.pattern.Patterns;
//import akka.util.Timeout;
//import com.alibaba.citrus.turbine.Context;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.qlangtech.tis.dag.actor.message.*;
//import com.qlangtech.tis.dag.validator.DAGValidator;
//import com.qlangtech.tis.datax.DataXName;
//import com.qlangtech.tis.manage.PermissionConstant;
//import com.qlangtech.tis.manage.TISActorSystemHolder;
//import com.qlangtech.tis.manage.spring.aop.Func;
//import com.qlangtech.tis.powerjob.model.PEWorkflowDAG;
//import com.qlangtech.tis.runtime.module.action.BasicModule;
//import com.qlangtech.tis.workflow.dao.IWorkFlowBuildHistoryDAO;
//import com.qlangtech.tis.workflow.dao.IWorkFlowDAO;
//import com.qlangtech.tis.workflow.pojo.WorkFlow;
//import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
//import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;
//import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import scala.concurrent.Await;
//import scala.concurrent.Future;
//import scala.concurrent.duration.Duration;
//
//import javax.servlet.ServletContext;
//import java.io.File;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * 工作流管理 Action
// * 处理工作流 CRUD 操作和执行控制
// *
// * @author 百岁（baisui@qlangtech.com）
// * @date 2026-01-30
// */
//public class WorkflowAction extends BasicModule {
//
//    private static final Logger logger = LoggerFactory.getLogger(WorkflowAction.class);
//
//    /**
//     * 工作流定义文件存储根目录
//     */
//    private static final String WORKFLOW_DIR = System.getProperty("TIS_HOME", "/opt/tis") + "/workflow";
//
//    /**
//     * Actor 消息超时时间
//     */
//    private static final Timeout ASK_TIMEOUT = Timeout.apply(30, TimeUnit.SECONDS);
//
//    /**
//     * 保存工作流
//     * 验证 DAG、保存文件、保存数据库
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = true)
//    public void doSaveWorkflow(Context context) {
//        try {
//            // 1. 获取请求参数
//            Integer workflowId = this.getInt("id");
//            String workflowName = this.getString("name");
//            String dagJson = this.getString("dagJson");
//            String scheduleCron = this.getString("scheduleCron");
//            Boolean enableSchedule = this.getBoolean("enableSchedule");
//
//            // 2. 参数验证
//            if (StringUtils.isEmpty(workflowName)) {
//                throw new IllegalArgumentException("workflow name cannot be empty");
//            }
//            if (StringUtils.isEmpty(dagJson)) {
//                throw new IllegalArgumentException("DAG JSON cannot be empty");
//            }
//
//            // 3. 验证 DAG 定义
//            PEWorkflowDAG dag = JSON.parseObject(dagJson, PEWorkflowDAG.class);
//            DAGValidator.ValidationResult validationResult = DAGValidator.validate(dag);
//            if (!validationResult.isValid()) {
//                this.addErrorMessage(context, "DAG validation failed: " + String.join(", ", validationResult.getErrors()));
//                return;
//            }
//
//            // 4. 保存或更新数据库记录
//            IWorkFlowDAO workflowDAO = this.getWorkflowDAOFacade().getWorkFlowDAO();
//            WorkFlow workflow;
//
//            if (workflowId != null) {
//                // 更新现有工作流
//                workflow = workflowDAO.selectByPrimaryKey(workflowId);
//                if (workflow == null) {
//                    throw new IllegalStateException("workflow not found: " + workflowId);
//                }
//            } else {
//                // 创建新工作流
//                workflow = new WorkFlow();
//                workflow.setName(workflowName);
//                workflow.setCreateTime(new Date());
//            }
//
//            // 设置 DAG 文件路径
//            String dagSpecPath = workflowName + ".json";
//            workflow.setDagSpecPath(dagSpecPath);
//            workflow.setOpTime(new Date());
//
//            // 设置调度配置
//            if (StringUtils.isNotEmpty(scheduleCron)) {
//                workflow.setScheduleCron(scheduleCron);
//                workflow.setEnableSchedule(enableSchedule != null ? enableSchedule : false);
//            }
//
//            if (workflowId != null) {
//                workflowDAO.updateByExampleSelective(workflow, new WorkFlowCriteria());
//            } else {
//                workflowId = workflowDAO.insertSelective(workflow);
//                workflow.setId(workflowId);
//            }
//
//            // 5. 保存 DAG 定义文件
//            File workflowDir = new File(WORKFLOW_DIR);
//            if (!workflowDir.exists()) {
//                workflowDir.mkdirs();
//            }
//            File dagFile = new File(workflowDir, dagSpecPath);
//            FileUtils.writeStringToFile(dagFile, dagJson, StandardCharsets.UTF_8);
//
//            logger.info("Workflow saved successfully: id={}, name={}, dagPath={}",
//                workflowId, workflowName, dagSpecPath);
//
//            // 6. 返回成功响应
//            JSONObject result = new JSONObject();
//            result.put("success", true);
//            result.put("workflowId", workflowId);
//            result.put("message", "Workflow saved successfully");
//            this.setBizResult(context, result);
//
//        } catch (Exception e) {
//            logger.error("Failed to save workflow", e);
//            this.addErrorMessage(context, "Failed to save workflow: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 加载工作流
//     * 返回 DAG 定义和元数据
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE)
//    public void doLoadWorkflow(Context context) {
//        try {
//            // 1. 获取工作流 ID
//            Integer workflowId = this.getInt("id");
//            if (workflowId == null) {
//                throw new IllegalArgumentException("workflow id cannot be null");
//            }
//
//            // 2. 查询数据库
//            IWorkFlowDAO workflowDAO = this.getWorkflowDAOFacade().getWorkFlowDAO();
//            WorkFlow workflow = workflowDAO.selectByPrimaryKey(workflowId);
//            if (workflow == null) {
//                throw new IllegalStateException("workflow not found: " + workflowId);
//            }
//
//            // 3. 读取 DAG 定义文件
//            String dagSpecPath = workflow.getDagSpecPath();
//            if (StringUtils.isEmpty(dagSpecPath)) {
//                throw new IllegalStateException("DAG spec path is empty for workflow: " + workflowId);
//            }
//
//            File dagFile = new File(WORKFLOW_DIR, dagSpecPath);
//            if (!dagFile.exists()) {
//                throw new IllegalStateException("DAG file not found: " + dagFile.getAbsolutePath());
//            }
//
//            String dagJson = FileUtils.readFileToString(dagFile, StandardCharsets.UTF_8);
//
//            // 4. 构建响应
//            JSONObject result = new JSONObject();
//            result.put("success", true);
//            result.put("workflow", workflow);
//            result.put("dagJson", dagJson);
//            this.setBizResult(context, result);
//
//        } catch (Exception e) {
//            logger.error("Failed to load workflow", e);
//            this.addErrorMessage(context, "Failed to load workflow: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 删除工作流
//     * 删除文件、删除数据库记录、移除调度任务
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = true)
//    public void doDeleteWorkflow(Context context) {
//        try {
//            // 1. 获取工作流 ID
//            Integer workflowId = this.getInt("id");
//            if (workflowId == null) {
//                throw new IllegalArgumentException("workflow id cannot be null");
//            }
//
//            // 2. 查询工作流
//            IWorkFlowDAO workflowDAO = this.getWorkflowDAOFacade().getWorkFlowDAO();
//            WorkFlow workflow = workflowDAO.selectByPrimaryKey(workflowId);
//            if (workflow == null) {
//                throw new IllegalStateException("workflow not found: " + workflowId);
//            }
//
//            // 3. 删除 DAG 定义文件
//            String dagSpecPath = workflow.getDagSpecPath();
//            if (StringUtils.isNotEmpty(dagSpecPath)) {
//                File dagFile = new File(WORKFLOW_DIR, dagSpecPath);
//                if (dagFile.exists()) {
//                    dagFile.delete();
//                    logger.info("Deleted DAG file: {}", dagFile.getAbsolutePath());
//                }
//            }
//
//            // 4. 删除数据库记录
//            workflowDAO.deleteByPrimaryKey(workflowId);
//
//            // 5. TODO: 移除调度任务（需要调度器支持）
//            // 这部分需要在定时调度器实现后添加
//
//            logger.info("Workflow deleted successfully: id={}, name={}", workflowId, workflow.getName());
//
//            // 6. 返回成功响应
//            JSONObject result = new JSONObject();
//            result.put("success", true);
//            result.put("message", "Workflow deleted successfully");
//            this.setBizResult(context, result);
//
//        } catch (Exception e) {
//            logger.error("Failed to delete workflow", e);
//            this.addErrorMessage(context, "Failed to delete workflow: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 手动触发工作流
//     * 创建实例、发送 StartWorkflow 消息
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = true)
//    public void doTriggerWorkflow(Context context) {
//        try {
//            // 1. 获取参数
//            Integer workflowId = this.getInt("id");
//            String initParamsJson = this.getString("initParams");
//          DataXName dataXName = DataXName.createDataXPipeline("");
//            if (workflowId == null) {
//                throw new IllegalArgumentException("workflow id cannot be null");
//            }
//
//            // 2. 查询工作流
//            IWorkFlowDAO workflowDAO = this.getWorkflowDAOFacade().getWorkFlowDAO();
//            WorkFlow workflow = workflowDAO.selectByPrimaryKey(workflowId);
//            if (workflow == null) {
//                throw new IllegalStateException("workflow not found: " + workflowId);
//            }
//
//            // 3. 创建工作流实例
//            IWorkFlowBuildHistoryDAO historyDAO = this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO();
//            WorkFlowBuildHistory instance = new WorkFlowBuildHistory();
//            instance.setWorkFlowId(workflowId);
//            instance.setCreateTime(new Date());
//            instance.setStartTime(new Date());
//            instance.setInstanceStatus("WAITING");
//
//            Integer instanceId = historyDAO.insertSelective(instance);
//            instance.setId(instanceId);
//
//            // 4. 解析初始化参数
//            Map<String, String> initParams = new HashMap<>();
//            if (StringUtils.isNotEmpty(initParamsJson)) {
//                JSONObject paramsObj = JSON.parseObject(initParamsJson);
//                for (String key : paramsObj.keySet()) {
//                    initParams.put(key, paramsObj.getString(key));
//                }
//            }
//
//            // 5. 发送 StartWorkflow 消息到 WorkflowInstance Sharding Region
//            ServletContext servletContext = this.getRequest().getServletContext();
//            ActorRef workflowRegion = TISActorSystemHolder.getWorkflowInstanceRegion(servletContext);
//            if (workflowRegion == null) {
//                throw new IllegalStateException("WorkflowInstance Sharding Region not initialized");
//            }
//
//            StartWorkflow startMsg = new StartWorkflow(instanceId, dataXName);
//            workflowRegion.tell(startMsg, ActorRef.noSender());
//
//            logger.info("Workflow triggered successfully: workflowId={}, instanceId={}", workflowId, instanceId);
//
//            // 6. 返回成功响应
//            JSONObject result = new JSONObject();
//            result.put("success", true);
//            result.put("instanceId", instanceId);
//            result.put("message", "Workflow triggered successfully");
//            this.setBizResult(context, result);
//
//        } catch (Exception e) {
//            logger.error("Failed to trigger workflow", e);
//            this.addErrorMessage(context, "Failed to trigger workflow: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 取消工作流
//     * 发送 CancelWorkflow 消息
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE, sideEffect = true)
//    public void doCancelWorkflow(Context context) {
//        try {
//            // 1. 获取参数
//            Integer instanceId = this.getInt("instanceId");
//            String reason = this.getString("reason");
//
//            if (instanceId == null) {
//                throw new IllegalArgumentException("instance id cannot be null");
//            }
//
//            // 2. 发送 CancelWorkflow 消息
//            ServletContext servletContext = this.getRequest().getServletContext();
//            ActorRef workflowRegion = TISActorSystemHolder.getWorkflowInstanceRegion(servletContext);
//            if (workflowRegion == null) {
//                throw new IllegalStateException("WorkflowInstance Sharding Region not initialized");
//            }
//
//            CancelWorkflow cancelMsg = new CancelWorkflow(instanceId, reason);
//            workflowRegion.tell(cancelMsg, ActorRef.noSender());
//
//            logger.info("Workflow cancel requested: instanceId={}, reason={}", instanceId, reason);
//
//            // 3. 返回成功响应
//            JSONObject result = new JSONObject();
//            result.put("success", true);
//            result.put("message", "Workflow cancel requested");
//            this.setBizResult(context, result);
//
//        } catch (Exception e) {
//            logger.error("Failed to cancel workflow", e);
//            this.addErrorMessage(context, "Failed to cancel workflow: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 查询工作流状态
//     * 调用 DAGMonitorActor
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE)
//    public void doQueryWorkflowStatus(Context context) {
//        try {
//            // 1. 获取实例 ID
//            Integer instanceId = this.getInt("instanceId");
//            if (instanceId == null) {
//                throw new IllegalArgumentException("instance id cannot be null");
//            }
//
//            // 2. 发送查询消息到 DAGMonitorActor
//            ServletContext servletContext = this.getRequest().getServletContext();
//            ActorRef dagMonitor = TISActorSystemHolder.getDagMonitorActor(servletContext);
//            if (dagMonitor == null) {
//                throw new IllegalStateException("DAGMonitorActor not initialized");
//            }
//
//            QueryWorkflowStatus queryMsg = new QueryWorkflowStatus(instanceId);
//            Future<Object> future = Patterns.ask(dagMonitor, queryMsg, ASK_TIMEOUT);
//            Object response = Await.result(future, Duration.create(30, TimeUnit.SECONDS));
//
//            // 3. 处理响应
//            if (response instanceof WorkflowRuntimeStatus) {
//                WorkflowRuntimeStatus status = (WorkflowRuntimeStatus) response;
//                JSONObject result = new JSONObject();
//                result.put("success", true);
//                result.put("status", status);
//                this.setBizResult(context, result);
//            } else {
//                throw new IllegalStateException("unexpected response type: " + response.getClass().getName());
//            }
//
//        } catch (Exception e) {
//            logger.error("Failed to query workflow status", e);
//            this.addErrorMessage(context, "Failed to query workflow status: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 查询等待队列
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE)
//    public void doQueryWaitingQueue(Context context) {
//        try {
//            ServletContext servletContext = this.getRequest().getServletContext();
//            ActorRef dagMonitor = TISActorSystemHolder.getDagMonitorActor(servletContext);
//            if (dagMonitor == null) {
//                throw new IllegalStateException("DAGMonitorActor not initialized");
//            }
//
//            QueryWaitingQueue queryMsg = new QueryWaitingQueue();
//            Future<Object> future = Patterns.ask(dagMonitor, queryMsg, ASK_TIMEOUT);
//            Object response = Await.result(future, Duration.create(30, TimeUnit.SECONDS));
//
//            JSONObject result = new JSONObject();
//            result.put("success", true);
//            result.put("queue", response);
//            this.setBizResult(context, result);
//
//        } catch (Exception e) {
//            logger.error("Failed to query waiting queue", e);
//            this.addErrorMessage(context, "Failed to query waiting queue: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 查询执行队列
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE)
//    public void doQueryRunningQueue(Context context) {
//        try {
//            ServletContext servletContext = this.getRequest().getServletContext();
//            ActorRef dagMonitor = TISActorSystemHolder.getDagMonitorActor(servletContext);
//            if (dagMonitor == null) {
//                throw new IllegalStateException("DAGMonitorActor not initialized");
//            }
//
//            QueryRunningQueue queryMsg = new QueryRunningQueue();
//            Future<Object> future = Patterns.ask(dagMonitor, queryMsg, ASK_TIMEOUT);
//            Object response = Await.result(future, Duration.create(30, TimeUnit.SECONDS));
//
//            JSONObject result = new JSONObject();
//            result.put("success", true);
//            result.put("queue", response);
//            this.setBizResult(context, result);
//
//        } catch (Exception e) {
//            logger.error("Failed to query running queue", e);
//            this.addErrorMessage(context, "Failed to query running queue: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 查询工作流历史
//     * 支持分页和过滤
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE)
//    public void doQueryWorkflowHistory(Context context) {
//        try {
//            // 1. 获取查询参数
//            Integer workflowId = this.getInt("workflowId");
//            Integer page = this.getInt("page", 1);
//            Integer pageSize = this.getInt("pageSize", 20);
//            String status = this.getString("status");
//
//            // 2. 构建查询条件
//            WorkFlowBuildHistoryCriteria criteria = new WorkFlowBuildHistoryCriteria();
//            WorkFlowBuildHistoryCriteria.Criteria c = criteria.createCriteria();
//
//            if (workflowId != null) {
//                c.andWorkFlowIdEqualTo(workflowId);
//            }
//
//            if (StringUtils.isNotEmpty(status)) {
//                // c.andInstanceStatusEqualTo(status); // 需要 Criteria 支持这个字段
//            }
//
//            criteria.setOrderByClause("id DESC");
//
//            // 3. 查询数据
//            IWorkFlowBuildHistoryDAO historyDAO = this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO();
//            int total = historyDAO.countByExample(criteria);
//            List<WorkFlowBuildHistory> histories = historyDAO.selectByExample(criteria, page, pageSize);
//
//            // 4. 返回结果
//            JSONObject result = new JSONObject();
//            result.put("success", true);
//            result.put("total", total);
//            result.put("page", page);
//            result.put("pageSize", pageSize);
//            result.put("histories", histories);
//            this.setBizResult(context, result);
//
//        } catch (Exception e) {
//            logger.error("Failed to query workflow history", e);
//            this.addErrorMessage(context, "Failed to query workflow history: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 查询节点执行详情
//     *
//     * @param context Turbine Context
//     */
//    @Func(value = PermissionConstant.DATAFLOW_MANAGE)
//    public void doQueryNodeExecutionDetails(Context context) {
//        try {
//            // 1. 获取参数
//            Integer instanceId = this.getInt("instanceId");
//            String nodeId = this.getString("nodeId");
//
//            if (instanceId == null) {
//                throw new IllegalArgumentException("instance id cannot be null");
//            }
//
//            // 2. 查询节点执行详情
//            // TODO: 需要实现 IDAGNodeExecutionDAO 的查询方法
//            // IDAGNodeExecutionDAO nodeDAO = ...;
//            // DAGNodeExecution nodeExecution = nodeDAO.selectByInstanceAndNode(instanceId, nodeId);
//
//            // 3. 返回结果
//            JSONObject result = new JSONObject();
//            result.put("success", true);
//            result.put("message", "Node execution details query not yet implemented");
//            // result.put("nodeExecution", nodeExecution);
//            this.setBizResult(context, result);
//
//        } catch (Exception e) {
//            logger.error("Failed to query node execution details", e);
//            this.addErrorMessage(context, "Failed to query node execution details: " + e.getMessage());
//        }
//    }
//}
