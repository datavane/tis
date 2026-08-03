package com.qlangtech.tis.workflow.dao;

import com.qlangtech.tis.workflow.pojo.DagNodeExecution;
import com.qlangtech.tis.workflow.pojo.DagNodeExecutionCriteria;
//import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DAG 节点执行记录 DAO 接口
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public interface IDAGNodeExecutionDAO {

    int countByExample(DagNodeExecutionCriteria example);

    int countFromWriteDB(DagNodeExecutionCriteria example);

    int deleteByExample(DagNodeExecutionCriteria criteria);

    int deleteByPrimaryKey(Long id);

    Integer insert(DagNodeExecution record);

    Integer insertSelective(DagNodeExecution record);

    List<DagNodeExecution> selectByExampleWithBLOBs(DagNodeExecutionCriteria example);

    List<DagNodeExecution> selectByExampleWithoutBLOBs(DagNodeExecutionCriteria criteria);


    List<DagNodeExecution> selectByExampleWithoutBLOBs(DagNodeExecutionCriteria example, int page, int pageSize);

    DagNodeExecution selectByPrimaryKey(Long id);

    int updateByExampleSelective(DagNodeExecution record, DagNodeExecutionCriteria example);

    int updateByExampleWithBLOBs(DagNodeExecution record, DagNodeExecutionCriteria example);

    int updateByExampleWithoutBLOBs(DagNodeExecution record, DagNodeExecutionCriteria example);

    /**
     * Worker 实际开始执行任务时，将节点执行记录由 WAITING 更新为 RUNNING，
     * 并记录实际开始时间与真实的 worker 地址。
     * 按 (workflowInstanceId, nodeId) 定位记录。
     *
     * @param record 携带 workflowInstanceId、nodeId、status、startTime、workerAddress
     * @return 更新的记录数
     */
    int updateStatusOnStarted(DagNodeExecution record);

    /**
     * 节点执行到达终态（SUCCEED/FAILED/STOPPED）时更新状态、执行结果与完成时间。
     * 按 (workflowInstanceId, nodeId) 定位记录。
     *
     * @param record 携带 workflowInstanceId、nodeId、status、result、finishedTime
     * @return 更新的记录数
     */
    int updateStatusOnCompleted(DagNodeExecution record);

    DagNodeExecution loadFromWriteDB(Long id);

    /**
     * 查询最近完成的任务
     * 用于监控面板展示已完成任务队列
     *
     * @param limit 返回数量限制
     * @param timeWindowMillis 时间窗口（毫秒），例如 3600000 表示最近1小时
     * @return 完成的任务列表，按完成时间倒序
     */
    List<DagNodeExecution> selectRecentlyCompletedTasks(
            //@Param("limit")
            int limit,
            //@Param("timeWindowMillis")
            long timeWindowMillis
    );
}
