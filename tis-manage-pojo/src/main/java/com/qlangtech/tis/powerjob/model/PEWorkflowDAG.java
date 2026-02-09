package com.qlangtech.tis.powerjob.model;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.datax.LifeCycleHook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * PowerJob Workflow DAG 数据模型（点线表示法）
 * 可序列化的 DAG 定义，用于持久化和网络传输
 * <p>
 * 参考 PowerJob 项目设计
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class PEWorkflowDAG implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点列表
     */
    private List<Node> nodes;

    /**
     * 边列表
     */
    private List<Edge> edges;

    public PEWorkflowDAG() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    /**
     * DAG 节点定义
     */
    public static class Node implements Serializable {

        private static final long serialVersionUID = 1L;
        private LifeCycleHook execRole;
        /**
         * 节点唯一 ID
         */
        private Long nodeId;

        /**
         * 节点类型：1=任务节点(TASK), 2=控制节点(CONTROL)
         */
        private WorkflowNodeType nodeType;

        /**
         * 关联的任务 ID（对应 TIS 的 DataflowTask）
         */
        // private Long jobId;

        /**
         * 节点名称
         */
        private String nodeName;

        /**
         * 运行时实例 ID
         */
        @JSONField(serialize = false)
        private Long instanceId;

        /**
         * 节点参数（JSON 格式）
         */
        private JSONObject nodeParams;
        // private transient JSONObject _nodeParams;

        /**
         * 节点状态：1=WAITING, 2=RUNNING, 3=SUCCEED, 4=FAILED, 5=CANCELED
         */
        @JSONField(serialize = false)
        private InstanceStatus status;

        /**
         * 执行结果
         */
        @JSONField(serialize = false)
        private String result;

        /**
         * 是否启用（默认 true）
         */
        private Boolean enable = true;

        /**
         * 失败时是否跳过（默认 false）
         */
        private Boolean skipWhenFailed = false;

        /**
         * 开始时间
         */
        @JSONField(serialize = false)
        private String startTime;

        /**
         * 完成时间
         */
        @JSONField(serialize = false)
        private String finishedTime;

        // Getters and Setters

        public Long getNodeId() {
            return nodeId;
        }

        public void setNodeId(Long nodeId) {
            this.nodeId = nodeId;
        }

        public WorkflowNodeType getNodeType() {
            return nodeType;
        }

        public void setNodeType(WorkflowNodeType nodeType) {
            this.nodeType = nodeType;
        }

        public LifeCycleHook getExecRole() {
            return execRole;
        }

        public void setExecRole(LifeCycleHook execRole) {
            this.execRole = execRole;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public Long getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(Long instanceId) {
            this.instanceId = instanceId;
        }

        public JSONObject getNodeParams() {
            return nodeParams;
        }

        public void setNodeParams(JSONObject nodeParams) {
            this.nodeParams = nodeParams;
        }

        public <T> T getNodeParam(String propKey) {
            if (this.getNodeParams() == null) {
                return null;
            }
            Object val = this.getNodeParams().get(propKey);
            if (val == null) {
                throw new IllegalStateException("propKey:" + propKey + " relevant val can not be null");
            }
            @SuppressWarnings("all") T resultVal = (T) val;
            return resultVal;
        }

        public InstanceStatus getStatus() {
            return status;
        }

        public void setStatus(InstanceStatus status) {
            this.status = status;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public Boolean getEnable() {
            return enable;
        }

        public void setEnable(Boolean enable) {
            this.enable = enable;
        }

        public Boolean getSkipWhenFailed() {
            return skipWhenFailed;
        }

        public void setSkipWhenFailed(Boolean skipWhenFailed) {
            this.skipWhenFailed = skipWhenFailed;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getFinishedTime() {
            return finishedTime;
        }

        public void setFinishedTime(String finishedTime) {
            this.finishedTime = finishedTime;
        }


        @Override
        public String toString() {
            return "(" + nodeId + ", '" + nodeName + '\'' + ')';
        }
    }

    /**
     * DAG 边定义
     */
    public static class Edge implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 起始节点 ID
         */
        private Long from;

        /**
         * 目标节点 ID
         */
        private Long to;

        /**
         * 边属性（用于条件分支）
         */
        private String property;

        /**
         * 边是否启用（默认 true）
         */
        private Boolean enable = true;

        // Getters and Setters

        public Long getFrom() {
            return from;
        }

        public void setFrom(Long from) {
            this.from = from;
        }

        public Long getTo() {
            return to;
        }

        public void setTo(Long to) {
            this.to = to;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public Boolean getEnable() {
            return enable;
        }

        public void setEnable(Boolean enable) {
            this.enable = enable;
        }

        @Override
        public String toString() {
            return "(" + "from=" + from + ", to=" + to + ')';
        }
    }

    // Getters and Setters

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }
}
