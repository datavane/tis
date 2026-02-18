/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.datax;

import com.qlangtech.tis.powerjob.model.InstanceStatus;
import com.qlangtech.tis.powerjob.model.PEWorkflowDAG;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作流运行时状态响应消息
 * DAGMonitorActor 返回给查询者
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class WorkflowRuntimeStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工作流实例 ID
     */
    private Integer instanceId;

    /**
     * 工作流状态：WAITING/RUNNING/SUCCEED/FAILED/STOPPED
     */
    private String status;

    /**
     * 所有节点的状态
     */
    private List<NodeStatus> nodes;

    /**
     * DAG 边信息（节点间的连线关系）
     */
    private List<PEWorkflowDAG.Edge> edges;

    /**
     * 开始时间（毫秒时间戳）
     */
    private Long startTime;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updateTime;

    private int totalNodes;
    private int waitingNodes;
    private int runningNodes;
    private int succeedNodes;
    private int failedNodes;

    public WorkflowRuntimeStatus() {
        this.nodes = new ArrayList<>();
    }

    public WorkflowRuntimeStatus(Integer instanceId, String status) {
        this.instanceId = instanceId;
        this.status = status;
        this.nodes = new ArrayList<>();
    }

    // Getters and Setters

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<NodeStatus> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeStatus> nodes) {
        this.nodes = nodes;
    }

    public List<PEWorkflowDAG.Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<PEWorkflowDAG.Edge> edges) {
        this.edges = edges;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }

    public int getWaitingNodes() {
        return waitingNodes;
    }

    public void setWaitingNodes(int waitingNodes) {
        this.waitingNodes = waitingNodes;
    }

    public int getRunningNodes() {
        return runningNodes;
    }

    public void setRunningNodes(int runningNodes) {
        this.runningNodes = runningNodes;
    }

    public int getSucceedNodes() {
        return succeedNodes;
    }

    public void setSucceedNodes(int succeedNodes) {
        this.succeedNodes = succeedNodes;
    }

    public int getFailedNodes() {
        return failedNodes;
    }

    public void setFailedNodes(int failedNodes) {
        this.failedNodes = failedNodes;
    }

    /**
     * @see PEWorkflowDAG
     */
    public void appendDAG(PEWorkflowDAG dag) {
        if (dag == null || dag.getNodes() == null) {
            return;
        }
        int waiting = 0, running = 0, succeed = 0, failed = 0;

        for (PEWorkflowDAG.Node node : dag.getNodes()) {
            NodeStatus ns = new NodeStatus();
            ns.setNodeId(node.getNodeId());
            ns.setNodeName(node.getNodeName());
            ns.setNodeType(node.getNodeType() != null ? node.getNodeType().name() : null);
            ns.setStatus(node.getStatus() != null ? node.getStatus().name() : null);
            ns.setResult(node.getResult());
            this.getNodes().add(ns);

            InstanceStatus nodeStatus = node.getStatus();
            if (nodeStatus != null) {
                switch (nodeStatus) {
                    case WAITING:
                    case QUEUED:
                        waiting++;
                        break;
                    case RUNNING:
                        running++;
                        break;
                    case SUCCEED:
                        succeed++;
                        break;
                    case FAILED:
                        failed++;
                        break;
                    default:
                        break;
                }
            }
        }

        this.setTotalNodes(dag.getNodes().size());
        this.setWaitingNodes(waiting);
        this.setRunningNodes(running);
        this.setSucceedNodes(succeed);
        this.setFailedNodes(failed);
        this.setEdges(dag.getEdges());
    }

    @Override
    public String toString() {
        return "WorkflowRuntimeStatus{" +
                "instanceId=" + instanceId +
                ", status='" + status + '\'' +
                ", nodes=" + nodes.size() +
                ", startTime=" + startTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
