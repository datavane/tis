/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.workflow.dao.impl;

import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.workflow.dao.IDAGNodeExecutionDAO;
import com.qlangtech.tis.workflow.pojo.DagNodeExecution;
import com.qlangtech.tis.workflow.pojo.DagNodeExecutionCriteria;

import java.util.List;

/**
 * DAG 节点执行记录 DAO 实现类
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class DAGNodeExecutionDAOImpl extends BasicDAO<DagNodeExecution, Object> implements IDAGNodeExecutionDAO {

  public final String getEntityName() {
    return "dag_node_execution";

  }

  public int countByExample(DagNodeExecutionCriteria example) {
    Integer count = (Integer)  this.count("dag_node_execution.ibatorgenerated_countByExample",example);
    return count;
  }

  public int countFromWriteDB(DagNodeExecutionCriteria example) {
    Integer count = (Integer)  this.countFromWriterDB("dag_node_execution.ibatorgenerated_countByExample",example);
    return count;
  }

  public int deleteByExample(DagNodeExecutionCriteria criteria) {
    return  this.deleteRecords("dag_node_execution.ibatorgenerated_deleteByExample", criteria);

  }

  public int deleteByPrimaryKey(Long id) {
    DagNodeExecution key = new DagNodeExecution();
    key.setId(id);
    return  this.deleteRecords("dag_node_execution.ibatorgenerated_deleteByPrimaryKey", key);

  }

  @Override
  public Integer insert(DagNodeExecution record) {
    Object newKey = this.insert("dag_node_execution.ibatorgenerated_insert", record);
    return (Integer) newKey;
  }

  @Override
  public Integer insertSelective(DagNodeExecution record) {
    Object newKey = this.insert("dag_node_execution.ibatorgenerated_insertSelective", record);
    return (Integer) newKey;
  }

  @SuppressWarnings("unchecked")
  public List<DagNodeExecution> selectByExampleWithBLOBs(DagNodeExecutionCriteria example) {
    List<DagNodeExecution> list = this.list("dag_node_execution.ibatorgenerated_selectByExampleWithBLOBs", example);
    return list;
  }

  public List<DagNodeExecution> selectByExampleWithoutBLOBs(DagNodeExecutionCriteria criteria) {
    return this.selectByExampleWithoutBLOBs(criteria,1,100);
  }

  @SuppressWarnings("all")
  public final List<RowMap> selectColsByExample(DagNodeExecutionCriteria example, int page, int pageSize) {
    example.setPage(page);
    example.setPageSize(pageSize);
    if(example.isTargetColsEmpty()){
      throw new IllegalStateException("criteria com.qlangtech.tis.workflow.pojo.DagNodeExecutionCriteria target Cols can not be empty ");
    }
    return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("dag_node_execution.ibatorgenerated_selectTargetColsByExample", example);
  }

  @SuppressWarnings("unchecked")
  public List<DagNodeExecution> selectByExampleWithoutBLOBs(DagNodeExecutionCriteria example, int page, int pageSize) {
    example.setPage(page);
    example.setPageSize(pageSize);
    List<DagNodeExecution> list = this.list("dag_node_execution.ibatorgenerated_selectByExample", example);
    return list;
  }

  public DagNodeExecution selectByPrimaryKey(Long id) {
    DagNodeExecution key = new DagNodeExecution();
    key.setId(id);
    DagNodeExecution record = (DagNodeExecution) this.load("dag_node_execution.ibatorgenerated_selectByPrimaryKey", key);
    return record;
  }

  public int updateByExampleSelective(DagNodeExecution record, DagNodeExecutionCriteria example) {
    UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
    return this.updateRecords("dag_node_execution.ibatorgenerated_updateByExampleSelective", parms);
  }

  public int updateByExampleWithBLOBs(DagNodeExecution record, DagNodeExecutionCriteria example) {
    UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
    int rows = this.updateRecords("dag_node_execution.ibatorgenerated_updateByExampleWithBLOBs", parms);
    return rows;
  }

  public int updateByExampleWithoutBLOBs(DagNodeExecution record, DagNodeExecutionCriteria example) {
    UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
    return this.updateRecords("dag_node_execution.ibatorgenerated_updateByExample", parms);
  }

  public DagNodeExecution loadFromWriteDB(Long id) {
    DagNodeExecution key = new DagNodeExecution();
    key.setId(id);
    DagNodeExecution record = (DagNodeExecution) this.loadFromWriterDB("dag_node_execution.ibatorgenerated_selectByPrimaryKey",key);
    return record;
  }

  private static class UpdateByExampleParms extends DagNodeExecutionCriteria {
    private Object record;

    public UpdateByExampleParms(Object record, DagNodeExecutionCriteria example) {
      super(example);
      this.record = record;
    }

    public Object getRecord() {
      return record;
    }
  }
}
