package com.qlangtech.tis.powerjob.algorithm;

import com.qlangtech.tis.powerjob.model.PEWorkflowDAG;

import java.util.*;

/**
 * Workflow DAG 运行时模型（引用表示法）
 * 节点直接持有引用，便于图算法操作
 *
 * 参考 PowerJob 项目设计
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class WorkflowDAG {

    /**
     * 所有根节点（入度为 0）
     */
    private List<Node> roots;

    /**
     * nodeId -> Node 映射
     */
    private Map<Long, Node> nodeMap;

    public WorkflowDAG() {
        this.roots = new ArrayList<>();
        this.nodeMap = new HashMap<>();
    }

    /**
     * DAG 节点（引用表示法）
     */
    public static class Node {

        /**
         * 节点 ID
         */
        private Long nodeId;

        /**
         * 持有原始 Node 数据
         */
        private PEWorkflowDAG.Node holder;

        /**
         * 上游依赖节点（直接引用）
         */
        private List<Node> dependencies;

        /**
         * 下游后继节点（直接引用）
         */
        private List<Node> successors;

        /**
         * 依赖边映射
         */
        private Map<Node, PEWorkflowDAG.Edge> dependenceEdgeMap;

        /**
         * 后继边映射
         */
        private Map<Node, PEWorkflowDAG.Edge> successorEdgeMap;

        public Node(Long nodeId, PEWorkflowDAG.Node holder) {
            this.nodeId = nodeId;
            this.holder = holder;
            this.dependencies = new ArrayList<>();
            this.successors = new ArrayList<>();
            this.dependenceEdgeMap = new HashMap<>();
            this.successorEdgeMap = new HashMap<>();
        }

        // Getters and Setters

        public Long getNodeId() {
            return nodeId;
        }

        public void setNodeId(Long nodeId) {
            this.nodeId = nodeId;
        }

        public PEWorkflowDAG.Node getHolder() {
            return holder;
        }

        public void setHolder(PEWorkflowDAG.Node holder) {
            this.holder = holder;
        }

        public List<Node> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<Node> dependencies) {
            this.dependencies = dependencies;
        }

        public List<Node> getSuccessors() {
            return successors;
        }

        public void setSuccessors(List<Node> successors) {
            this.successors = successors;
        }

        public Map<Node, PEWorkflowDAG.Edge> getDependenceEdgeMap() {
            return dependenceEdgeMap;
        }

        public void setDependenceEdgeMap(Map<Node, PEWorkflowDAG.Edge> dependenceEdgeMap) {
            this.dependenceEdgeMap = dependenceEdgeMap;
        }

        public Map<Node, PEWorkflowDAG.Edge> getSuccessorEdgeMap() {
            return successorEdgeMap;
        }

        public void setSuccessorEdgeMap(Map<Node, PEWorkflowDAG.Edge> successorEdgeMap) {
            this.successorEdgeMap = successorEdgeMap;
        }
    }

    // Getters and Setters

    public List<Node> getRoots() {
        return roots;
    }

    public void setRoots(List<Node> roots) {
        this.roots = roots;
    }

    public Map<Long, Node> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(Map<Long, Node> nodeMap) {
        this.nodeMap = nodeMap;
    }
}
