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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Akka Actor System status DTO for monitoring dashboard
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/2/13
 */
public class ActorSystemStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private String systemName;
    private String address;
    private String hostname;
    private int port;
    private long startTime;
    private long uptime;
    private boolean initialized;
    private boolean running;
    private List<ClusterMemberInfo> clusterMembers = new ArrayList<>();
    private Map<String, Integer> actorCounts = new HashMap<>();
    private List<ActiveWorkflowInfo> activeWorkflows = new ArrayList<>();
    private List<ActiveWorkerInfo> activeWorkers = new ArrayList<>();

    // Getters and Setters

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public List<ClusterMemberInfo> getClusterMembers() {
        return clusterMembers;
    }

    public void setClusterMembers(List<ClusterMemberInfo> clusterMembers) {
        this.clusterMembers = clusterMembers;
    }

    public Map<String, Integer> getActorCounts() {
        return actorCounts;
    }

    public void setActorCounts(Map<String, Integer> actorCounts) {
        this.actorCounts = actorCounts;
    }

    public List<ActiveWorkflowInfo> getActiveWorkflows() {
        return activeWorkflows;
    }

    public void setActiveWorkflows(List<ActiveWorkflowInfo> activeWorkflows) {
        this.activeWorkflows = activeWorkflows;
    }

    public List<ActiveWorkerInfo> getActiveWorkers() {
        return activeWorkers;
    }

    public void setActiveWorkers(List<ActiveWorkerInfo> activeWorkers) {
        this.activeWorkers = activeWorkers;
    }

    /**
     * Cluster member info
     */
    public static class ClusterMemberInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String address;
        private String roles;
        private String status;
        private long upSince;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getRoles() {
            return roles;
        }

        public void setRoles(String roles) {
            this.roles = roles;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getUpSince() {
            return upSince;
        }

        public void setUpSince(long upSince) {
            this.upSince = upSince;
        }
    }

    /**
     * Active workflow instance info
     */
    public static class ActiveWorkflowInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer taskId;
        private long startTime;
        private String status;
        private int nodeCount;
        private int runningNodes;

        public Integer getTaskId() {
            return taskId;
        }

        public void setTaskId(Integer taskId) {
            this.taskId = taskId;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getNodeCount() {
            return nodeCount;
        }

        public void setNodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
        }

        public int getRunningNodes() {
            return runningNodes;
        }

        public void setRunningNodes(int runningNodes) {
            this.runningNodes = runningNodes;
        }
    }

    /**
     * Active worker info
     */
    public static class ActiveWorkerInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String actorPath;
        private Integer taskId;
        private Long nodeId;
        private long startTime;

        public String getActorPath() {
            return actorPath;
        }

        public void setActorPath(String actorPath) {
            this.actorPath = actorPath;
        }

        public Integer getTaskId() {
            return taskId;
        }

        public void setTaskId(Integer taskId) {
            this.taskId = taskId;
        }

        public Long getNodeId() {
            return nodeId;
        }

        public void setNodeId(Long nodeId) {
            this.nodeId = nodeId;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }
    }
}
