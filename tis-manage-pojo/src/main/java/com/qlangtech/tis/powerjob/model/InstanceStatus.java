package com.qlangtech.tis.powerjob.model;

import org.apache.commons.lang.StringUtils;

/**
 * Workflow 实例状态枚举
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public enum InstanceStatus {

    /**
     * 等待调度
     */
    WAITING(1, "WAITING"),

    /**
     * 运行中
     */
    RUNNING(2, "RUNNING"),

    /**
     * 执行成功
     */
    SUCCEED(3, "SUCCEED"),

    /**
     * 执行失败
     */
    FAILED(4, "FAILED"),

    /**
     * 已取消
     */
    CANCELED(5, "CANCELED"),

    /**
     * 人工停止
     */
    STOPPED(6, "STOPPED"),

    /**
     * 已加入队列等待执行
     */
    QUEUED(7, "QUEUED");

    private final int v;
    private final String desc;

    InstanceStatus(int v, String desc) {
        this.v = v;
        this.desc = desc;
    }

    public int getV() {
        return v;
    }

    public String getDesc() {
        return desc;
    }

    public static InstanceStatus of(String desc) {
        for (InstanceStatus status : values()) {
            if (StringUtils.equals(status.desc, desc)) {
                return status;
            }
        }
        throw new IllegalArgumentException("invalid InstanceStatus value: " + desc);
    }

    public static InstanceStatus of(int v) {
        for (InstanceStatus status : values()) {
            if (status.v == v) {
                return status;
            }
        }
        throw new IllegalArgumentException("invalid InstanceStatus value: " + v);
    }
}
