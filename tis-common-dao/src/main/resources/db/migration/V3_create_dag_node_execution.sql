-- TIS DAG 统一集群架构改造 - 数据库迁移脚本
-- 创建 dag_node_execution 表，记录 DAG 节点执行详情
--
-- @author 百岁(baisui@qlangtech.com)
-- @date 2026-01-29

-- 创建 dag_node_execution 表
CREATE TABLE dag_node_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    workflow_instance_id INT NOT NULL COMMENT '关联workflow_build_history.id',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(32) NOT NULL COMMENT '节点类型:TASK/CONTROL',
    task_name VARCHAR(256) COMMENT '关联的任务名称',
    status VARCHAR(32) COMMENT '节点状态:WAITING/RUNNING/SUCCEED/FAILED/CANCELED',
    result TEXT COMMENT '节点执行结果',
    start_time DATETIME COMMENT '开始时间',
    finished_time DATETIME COMMENT '完成时间',
    skip_when_failed TINYINT(1) DEFAULT 0 COMMENT '失败时是否跳过',
    enable TINYINT(1) DEFAULT 1 COMMENT '节点是否启用',
    retry_times INT DEFAULT 0 COMMENT '重试次数',
    worker_address VARCHAR(128) COMMENT '执行节点地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_wf_instance(workflow_instance_id),
    INDEX idx_node_id(node_id),
    INDEX idx_status(status),
    INDEX idx_worker(worker_address),
    INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DAG节点执行详情表';
