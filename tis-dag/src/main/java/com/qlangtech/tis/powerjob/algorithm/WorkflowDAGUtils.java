package com.qlangtech.tis.powerjob.algorithm;

import com.qlangtech.tis.powerjob.model.PEWorkflowDAG;
import com.qlangtech.tis.powerjob.model.WorkflowNodeType;
import com.qlangtech.tis.powerjob.model.InstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Workflow DAG 算法工具类
 * 提供 DAG 验证、转换、就绪节点计算等核心算法
 * <p>
 * 参考 PowerJob 项目设计
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class WorkflowDAGUtils {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDAGUtils.class);

    /**
     * 校验 DAG 合法性
     * - 检查节点是否为空
     * - 检查节点 ID 是否重复
     * - 检测环路（DFS）
     * - 检测孤立环
     *
     * @param dag DAG 定义
     * @return 是否合法
     */
    public static boolean valid(PEWorkflowDAG dag) {
        if (dag == null || dag.getNodes() == null || dag.getNodes().isEmpty()) {
            logger.warn("DAG is empty");
            return false;
        }

        // 检查节点 ID 是否重复
        Set<Long> nodeIds = new HashSet<>();
        for (PEWorkflowDAG.Node node : dag.getNodes()) {
            if (node.getNodeId() == null) {
                logger.warn("Node ID cannot be null");
                return false;
            }
            if (!nodeIds.add(node.getNodeId())) {
                logger.warn("Duplicate node ID: {}", node.getNodeId());
                return false;
            }
        }

        // 转换为引用表示法
        WorkflowDAG workflowDAG = convert(dag);

        // 检测环路
        Set<Long> visitedNodes = new HashSet<>();
        for (WorkflowDAG.Node root : workflowDAG.getRoots()) {
            if (invalidPath(root, new HashSet<>(), visitedNodes)) {
                logger.warn("DAG contains cycle");
                return false;
            }
        }

        // 检测孤立环（没有根节点可达的环）
        if (visitedNodes.size() < dag.getNodes().size()) {
            logger.warn("DAG contains isolated cycle");
            return false;
        }

        return true;
    }

    /**
     * 点线表示法转引用表示法
     *
     * @param peWorkflowDAG 点线表示法
     * @return 引用表示法
     */
    public static WorkflowDAG convert(PEWorkflowDAG peWorkflowDAG) {
        WorkflowDAG workflowDAG = new WorkflowDAG();

        // 1. 创建所有节点
        Map<Long, WorkflowDAG.Node> nodeMap = new HashMap<>();
        for (PEWorkflowDAG.Node peNode : peWorkflowDAG.getNodes()) {
            WorkflowDAG.Node node = new WorkflowDAG.Node(peNode.getNodeId(), peNode);
            nodeMap.put(peNode.getNodeId(), node);
        }
        workflowDAG.setNodeMap(nodeMap);

        // 2. 建立双向引用关系
        if (peWorkflowDAG.getEdges() != null) {
            for (PEWorkflowDAG.Edge edge : peWorkflowDAG.getEdges()) {
                WorkflowDAG.Node fromNode = nodeMap.get(edge.getFrom());
                WorkflowDAG.Node toNode = nodeMap.get(edge.getTo());

                if (fromNode != null && toNode != null) {
                    // 建立后继关系
                    fromNode.getSuccessors().add(toNode);
                    fromNode.getSuccessorEdgeMap().put(toNode, edge);

                    // 建立依赖关系
                    toNode.getDependencies().add(fromNode);
                    toNode.getDependenceEdgeMap().put(fromNode, edge);
                }
            }
        }

        // 3. 识别根节点（入度为 0）
        List<WorkflowDAG.Node> roots =
                nodeMap.values().stream().filter(node -> node.getDependencies().isEmpty()).collect(Collectors.toList());
        workflowDAG.setRoots(roots);

        return workflowDAG;
    }

    /**
     * 获取就绪节点（核心算法）
     * - 找出所有前置依赖已完成的节点
     * - 自动跳过禁用节点
     * - 支持失败节点跳过（skipWhenFailed）
     *
     * @param dag DAG 定义
     * @return 就绪节点列表
     */
    public static List<PEWorkflowDAG.Node> listReadyNodes(PEWorkflowDAG dag) {
        if (dag == null || dag.getNodes() == null) {
            return Collections.emptyList();
        }

        // 1. 构建依赖关系图（只考虑启用的边）
        Map<Long, Set<Long>> dependencyMap = new HashMap<>();
        if (dag.getEdges() != null) {
            for (PEWorkflowDAG.Edge edge : dag.getEdges()) {
                if (edge.getEnable() != null && edge.getEnable()) {
                    dependencyMap.computeIfAbsent(edge.getTo(), k -> new HashSet<>()).add(edge.getFrom());
                }
            }
        }

        // 2. 构建节点映射
        Map<Long, PEWorkflowDAG.Node> nodeMap =
                dag.getNodes().stream().collect(Collectors.toMap(PEWorkflowDAG.Node::getNodeId, node -> node));

        // 3. 遍历所有节点，找出就绪节点
        // 禁用节点在 initializeDAGRuntime 中已被标记为 SUCCEED，不会处于 WAITING 状态，无需额外处理
        List<PEWorkflowDAG.Node> readyNodes = new ArrayList<>();

        for (PEWorkflowDAG.Node node : dag.getNodes()) {
            // 只处理 WAITING 状态的节点（其他状态都跳过）
            if (node.getStatus() != InstanceStatus.WAITING) {
                continue;
            }

            // 检查是否就绪
            if (!isReadyNode(node.getNodeId(), nodeMap, dependencyMap)) {
                continue;
            }

            readyNodes.add(node);
        }

        return readyNodes;
    }

    /**
     * 获取所有根节点
     *
     * @param dag DAG 定义
     * @return 根节点列表
     */
    public static List<PEWorkflowDAG.Node> listRoots(PEWorkflowDAG dag) {
        WorkflowDAG workflowDAG = convert(dag);
        return workflowDAG.getRoots().stream().map(WorkflowDAG.Node::getHolder).collect(Collectors.toList());
    }

    /**
     * 检测环路（DFS）
     *
     * @param root         当前节点
     * @param pathIds      当前路径上的节点 ID
     * @param visitedNodes 所有访问过的节点
     * @return 是否存在环路
     */
    private static boolean invalidPath(WorkflowDAG.Node root, Set<Long> pathIds, Set<Long> visitedNodes) {
        // 递归出口：出现之前的节点则代表有环
        if (pathIds.contains(root.getNodeId())) {
            return true;
        }

        visitedNodes.add(root.getNodeId());

        // 出现无后继者节点，则说明该路径成功
        if (root.getSuccessors().isEmpty()) {
            return false;
        }

        pathIds.add(root.getNodeId());

        for (WorkflowDAG.Node node : root.getSuccessors()) {
            // 每个分支使用独立的路径集合
            Set<Long> branchPathIds = new HashSet<>(pathIds);
            if (invalidPath(node, branchPathIds, visitedNodes)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查节点是否就绪
     *
     * @param nodeId        节点 ID
     * @param nodeMap       节点映射
     * @param dependencyMap 依赖关系映射
     * @return 是否就绪
     */
    private static boolean isReadyNode(Long nodeId, Map<Long, PEWorkflowDAG.Node> nodeMap,
                                       Map<Long, Set<Long>> dependencyMap) {
        Set<Long> dependencies = dependencyMap.get(nodeId);

        // 没有依赖，直接就绪
        if (dependencies == null || dependencies.isEmpty()) {
            return true;
        }

        // 检查所有依赖是否已完成
        for (Long depNodeId : dependencies) {
            PEWorkflowDAG.Node depNode = nodeMap.get(depNodeId);
            if (depNode == null) {
                continue;
            }

            // 依赖节点未完成
            if (!isCompletedStatus(depNode.getStatus())) {
                return false;
            }

            // 依赖节点失败且不允许跳过
            if (depNode.getStatus() != null
                    && depNode.getStatus() == InstanceStatus.FAILED
                    && (depNode.getSkipWhenFailed() == null || !depNode.getSkipWhenFailed())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断节点状态是否已完成
     *
     * @param status 节点状态
     * @return 是否已完成
     */
    private static boolean isCompletedStatus(InstanceStatus status) {
        return InstanceStatus.SUCCEED == Objects.requireNonNull(status, "status can not be null")  //
                || status == InstanceStatus.FAILED  //
                || status == InstanceStatus.CANCELED  //
                || status == InstanceStatus.STOPPED;
    }
}
