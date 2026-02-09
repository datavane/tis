-- TIS DAG 统一集群架构改造 - 数据库迁移脚本
-- 扩展 workflow_build_history 表，添加 DAG 运行时状态和工作流上下文字段
--
-- @author 百岁(baisui@qlangtech.com)
-- @date 2026-01-29

-- 扩展 workflow_build_history 表
ALTER TABLE work_flow_build_history
ADD COLUMN dag_runtime TEXT COMMENT 'DAG运行时状态JSON,包含所有节点执行状态',
ADD COLUMN wf_context TEXT COMMENT '工作流上下文,节点间数据共享',
ADD COLUMN instance_status VARCHAR(32) COMMENT '实例状态:WAITING/RUNNING/SUCCEED/FAILED/STOPPED';

-- 添加索引
CREATE INDEX idx_wfbh_instance_status ON work_flow_build_history(instance_status);
CREATE INDEX idx_wfbh_create_time ON work_flow_build_history(create_time);
