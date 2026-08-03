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
  //  private List<ActiveWorkflowInfo> activeWorkflows = new ArrayList<>();
    private List<ActiveWorkerInfo> activeWorkers = new ArrayList<>();
    private ActorTopology actorTopology = new ActorTopology();
    // private int maxInstancesPerNode = DataXJobSubmitParams.DEFAULT_MAX_INSTANCES_PER_NODE;

    // New fields for task queue visualization
    private List<QueuedTask> waitingQueue = new ArrayList<>();
    private List<RunningTask> runningQueue = new ArrayList<>();
   // private List<CompletedTask> completedTasks = new ArrayList<>();
    private Integer maxConcurrentTasks;

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

//    public List<ActiveWorkflowInfo> getActiveWorkflows() {
//        return activeWorkflows;
//    }
//
//    public void setActiveWorkflows(List<ActiveWorkflowInfo> activeWorkflows) {
//        this.activeWorkflows = activeWorkflows;
//    }

    public List<ActiveWorkerInfo> getActiveWorkers() {
        return activeWorkers;
    }

    public void setActiveWorkers(List<ActiveWorkerInfo> activeWorkers) {
        this.activeWorkers = activeWorkers;
    }

    public int getMaxInstancesPerNode() {
        return DataXJobSubmitParams.getDftIfEmpty().maxInstancesPerNode;
    }

    //    public void setMaxInstancesPerNode(int maxInstancesPerNode) {
    //        this.maxInstancesPerNode = maxInstancesPerNode;
    //    }

    public ActorTopology getActorTopology() {
        return actorTopology;
    }

    public void setActorTopology(ActorTopology actorTopology) {
        this.actorTopology = actorTopology;
    }

    public List<QueuedTask> getWaitingQueue() {
        return waitingQueue;
    }

    public void setWaitingQueue(List<QueuedTask> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }

    public List<RunningTask> getRunningQueue() {
        return runningQueue;
    }

    public void setRunningQueue(List<RunningTask> runningQueue) {
        this.runningQueue = runningQueue;
    }

//    public List<CompletedTask> getCompletedTasks() {
//        return completedTasks;
//    }
//
//    public void setCompletedTasks(List<CompletedTask> completedTasks) {
//        this.completedTasks = completedTasks;
//    }

    public Integer getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

    public void setMaxConcurrentTasks(Integer maxConcurrentTasks) {
        this.maxConcurrentTasks = maxConcurrentTasks;
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
        /**
         * Full cluster address of the node where this worker runs,
         * e.g. akka://TIS-DAG-System@192.168.28.189:2551
         */
        private String workerAddress;

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

        public String getWorkerAddress() {
            return workerAddress;
        }

        public void setWorkerAddress(String workerAddress) {
            this.workerAddress = workerAddress;
        }
    }

    /**
     * Actor topology information - structure and relationships of actors
     */
    public static class ActorTopology implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<ActorNode> nodes = new ArrayList<>();
        private List<ActorRelation> relations = new ArrayList<>();

        public List<ActorNode> getNodes() {
            return nodes;
        }

        public void setNodes(List<ActorNode> nodes) {
            this.nodes = nodes;
        }

        public List<ActorRelation> getRelations() {
            return relations;
        }

        public void setRelations(List<ActorRelation> relations) {
            this.relations = relations;
        }
    }

    /**
     * Actor node - represents a single actor type in the system
     */
    public static class ActorNode implements Serializable {
        private static final long serialVersionUID = 1L;

        private String id;              // Actor type identifier (e.g., "WorkflowInstanceActor")
        private String name;            // Display name (e.g., "工作流实例Actor")
        private String type;            // Actor category: "stateful", "router", "worker", "monitor", "cluster"
        private String description;     // Role description
        private Integer count;          // Current instance count
        private String path;            // Actor path (e.g., "/user/workflow-instance-region")
        private boolean clickable;      // Whether this actor has a detail page

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isClickable() {
            return clickable;
        }

        public void setClickable(boolean clickable) {
            this.clickable = clickable;
        }
    }

    /**
     * Actor relation - represents message flow between actors
     */
    public static class ActorRelation implements Serializable {
        private static final long serialVersionUID = 1L;

        private String from;            // Source actor ID
        private String to;              // Target actor ID
        private String messageType;     // Message type (e.g., "StartWorkflow", "DispatchTask")
        private String description;     // Relation description

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Queued task info - represents a task waiting to be executed
     */
    public static class QueuedTask implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long nodeId;
        private String nodeName;
        private Integer taskId;
        private long queuedTime;

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

        public Integer getTaskId() {
            return taskId;
        }

        public void setTaskId(Integer taskId) {
            this.taskId = taskId;
        }

        public long getQueuedTime() {
            return queuedTime;
        }

        public void setQueuedTime(long queuedTime) {
            this.queuedTime = queuedTime;
        }
    }

    /**
     * Running task info - represents a task currently being executed
     */
    public static class RunningTask implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long nodeId;
        private String nodeName;
        private Integer taskId;
        private long startTime;
        private String workerAddress;

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

        public String getWorkerAddress() {
            return workerAddress;
        }

        public void setWorkerAddress(String workerAddress) {
            this.workerAddress = workerAddress;
        }
    }

    /**
     * Completed task info - represents a recently completed task
     */
    public static class CompletedTask implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long nodeId;
        private String nodeName;
        private Integer taskId;
        private long startTime;
        private long endTime;
        private String status;

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

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
