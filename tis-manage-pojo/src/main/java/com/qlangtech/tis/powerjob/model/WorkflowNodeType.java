package com.qlangtech.tis.powerjob.model;

/**
 * Workflow 节点类型枚举
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public enum WorkflowNodeType {

    /**
     * 任务节点 - 执行具体的数据处理任务
     */
    TASK(1, "TASK"),

    /**
     * 控制节点 - 用于条件分支和流程控制
     */
    CONTROL(2, "CONTROL");

    private final int v;
    private final String desc;

    WorkflowNodeType(int v, String desc) {
        this.v = v;
        this.desc = desc;
    }

    public int getV() {
        return v;
    }

    public String getDesc() {
        return desc;
    }

    public static WorkflowNodeType of(int v) {
        for (WorkflowNodeType type : values()) {
            if (type.v == v) {
                return type;
            }
        }
        throw new IllegalArgumentException("invalid WorkflowNodeType value: " + v);
    }
}
