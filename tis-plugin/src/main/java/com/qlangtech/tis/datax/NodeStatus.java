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

package com.qlangtech.tis.datax;

import com.qlangtech.tis.workflow.pojo.DagNodeExecution;

import java.io.Serializable;
import java.util.Date;

/**
 * 节点状态
 * 用于 WorkflowRuntimeStatus 中描述单个节点的状态
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class NodeStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点 ID
     */
    private Long nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型：TASK/CONTROL
     */
    private String nodeType;

    /**
     * 节点状态：WAITING/RUNNING/SUCCEED/FAILED/CANCELED
     */
    private String status;

    /**
     * 执行结果
     */
    private String result;

    /**
     * 开始时间（毫秒时间戳）
     */
    private Long startTime;

    /**
     * 完成时间（毫秒时间戳）
     */
    private Long finishedTime;

    /**
     * Worker 地址
     */
    private String workerAddress;

    /**
     * 重试次数
     */
    private Integer retryTimes;

    public NodeStatus() {
    }

    public NodeStatus(Long nodeId, String nodeName, String status) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.status = status;
    }

    public DagNodeExecution convert() {
        NodeStatus nodeStaus = this;
        DagNodeExecution exec = new DagNodeExecution();
        exec.setNodeId(nodeStaus.getNodeId());
        exec.setNodeName(nodeStaus.getNodeName());
        exec.setNodeType(nodeStaus.getNodeType());
        exec.setStatus(nodeStaus.getStatus());
        exec.setResult(nodeStaus.getResult());
        if (nodeStaus.getStartTime() != null) {
            exec.setStartTime(new Date(nodeStaus.getStartTime()));
        }
        if (nodeStaus.getFinishedTime() != null) {
            exec.setFinishedTime(new Date(nodeStaus.getFinishedTime()));
        }
        exec.setWorkerAddress(nodeStaus.getWorkerAddress());
        exec.setRetryTimes(nodeStaus.getRetryTimes());
        return exec;
    }

    // Getters and Setters

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(Long finishedTime) {
        this.finishedTime = finishedTime;
    }

    public String getWorkerAddress() {
        return workerAddress;
    }

    public void setWorkerAddress(String workerAddress) {
        this.workerAddress = workerAddress;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    @Override
    public String toString() {
        return "NodeStatus{" + "nodeId=" + nodeId + ", nodeName='" + nodeName + '\'' + ", status='" + status + '\'' + ", workerAddress='" + workerAddress + '\'' + '}';
    }
}
