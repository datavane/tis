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

import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.workflow.dao.IWorkFlowBuildHistoryDAO;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WorkFlowBuildHistoryDAOImpl extends BasicDAO<WorkFlowBuildHistory, WorkFlowBuildHistoryCriteria> implements IWorkFlowBuildHistoryDAO {

    public WorkFlowBuildHistoryDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "work_flow_build_history";
    }

    public int countByExample(WorkFlowBuildHistoryCriteria example) {
        Integer count = (Integer) this.count("work_flow_build_history.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(WorkFlowBuildHistoryCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("work_flow_build_history.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(WorkFlowBuildHistoryCriteria criteria) {
        return this.deleteRecords("work_flow_build_history.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer id) {
        WorkFlowBuildHistory key = new WorkFlowBuildHistory();
        key.setId(id);
        return this.deleteRecords("work_flow_build_history.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(WorkFlowBuildHistory record) {
        Object newKey = this.insert("work_flow_build_history.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(WorkFlowBuildHistory record) {
        Object newKey = this.insert("work_flow_build_history.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<WorkFlowBuildHistory> selectByExample(WorkFlowBuildHistoryCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<WorkFlowBuildHistory> selectByExample(WorkFlowBuildHistoryCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<WorkFlowBuildHistory> list = this.list("work_flow_build_history.ibatorgenerated_selectByExample", example);
        return list;
    }

    public WorkFlowBuildHistory selectByPrimaryKey(Integer id) {
        WorkFlowBuildHistory key = new WorkFlowBuildHistory();
        key.setId(id);
        WorkFlowBuildHistory record = (WorkFlowBuildHistory) this.load("work_flow_build_history.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(WorkFlowBuildHistory record, WorkFlowBuildHistoryCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("work_flow_build_history.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(WorkFlowBuildHistory record, WorkFlowBuildHistoryCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("work_flow_build_history.ibatorgenerated_updateByExample", parms);
    }

    public WorkFlowBuildHistory loadFromWriteDB(Integer id) {
        WorkFlowBuildHistory key = new WorkFlowBuildHistory();
        key.setId(id);
        WorkFlowBuildHistory record = (WorkFlowBuildHistory) this.loadFromWriterDB("work_flow_build_history.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    /**
     * 从写库加载工作流实例（加行锁）
     *
     * 实现说明：
     * 1. 调用 MyBatis 映射文件中的 loadFromWriteDBWithLock 方法
     * 2. 该方法执行 SELECT ... FOR UPDATE 语句，对记录加行锁
     * 3. 用于并发控制，防止多个线程同时修改同一个工作流实例
     *
     * 伪代码：
     * WorkFlowBuildHistory key = new WorkFlowBuildHistory();
     * key.setId(id);
     * return (WorkFlowBuildHistory) this.loadFromWriterDB("work_flow_build_history.loadFromWriteDBWithLock", key);
     */
    @Override
    public WorkFlowBuildHistory loadFromWriteDBWithLock(Integer id) {
        // TODO: 实现从写库加载并加行锁
        // 参考 loadFromWriteDB() 方法，但使用 loadFromWriteDBWithLock 映射
        return null;
    }

    /**
     * 查询卡住的工作流实例
     *
     * 实现说明：
     * 1. 调用 MyBatis 映射文件中的 selectStuckInstances 方法
     * 2. 查询状态为 RUNNING 且超过指定时间未更新的实例
     * 3. 用于故障恢复，识别长时间运行的异常实例
     *
     * 伪代码：
     * Map<String, Object> params = new HashMap<>();
     * params.put("timeoutMinutes", timeoutMinutes);
     * return this.list("work_flow_build_history.selectStuckInstances", params);
     */
    @Override
    public List<WorkFlowBuildHistory> selectStuckInstances(int timeoutMinutes) {
        // TODO: 实现查询卡住的工作流实例
        // 传入 timeoutMinutes 参数，调用 MyBatis 映射
        return List.of();
    }

    /**
     * 选择性更新工作流实例
     *
     * 实现说明：
     * 1. 调用 MyBatis 映射文件中的 updateByPrimaryKeySelective 方法
     * 2. 只更新 record 中非 null 的字段
     * 3. 自动更新 op_time 为当前时间
     *
     * 伪代码：
     * return this.updateRecords("work_flow_build_history.updateByPrimaryKeySelective", record);
     */
    @Override
    public int updateByPrimaryKeySelective(WorkFlowBuildHistory record) {
        // TODO: 实现选择性更新
        // 参考 updateByExampleSelective() 方法，但使用主键更新
        return 0;
    }

  private static class UpdateByExampleParms extends WorkFlowBuildHistoryCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, WorkFlowBuildHistoryCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
