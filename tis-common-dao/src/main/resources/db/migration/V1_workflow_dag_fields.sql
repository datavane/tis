-- TIS DAG 统一集群架构改造 - 数据库迁移脚本
-- 扩展 workflow 表，添加 DAG 定义路径和定时调度配置字段
--
-- @author 百岁(baisui@qlangtech.com)
-- @date 2026-01-29

-- 扩展 workflow 表
ALTER TABLE work_flow
ADD COLUMN dag_spec_path VARCHAR(256) COMMENT 'DAG拓扑结构文件路径,相对于${TIS_HOME}/workflow/',
ADD COLUMN schedule_cron VARCHAR(64) COMMENT '定时调度Cron表达式',
ADD COLUMN enable_schedule TINYINT(1) DEFAULT 0 COMMENT '是否启用定时调度';

-- 添加索引
CREATE INDEX idx_workflow_schedule ON work_flow(enable_schedule, schedule_cron);
