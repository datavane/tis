package com.qlangtech.tis.workflow.dao;

import com.qlangtech.tis.workflow.pojo.DagNodeExecution;
import com.qlangtech.tis.workflow.pojo.DagNodeExecutionCriteria;

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

    DagNodeExecution loadFromWriteDB(Long id);
}
