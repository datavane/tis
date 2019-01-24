/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.trigger.biz.dal.dao.impl;

import java.util.List;
import com.qlangtech.tis.trigger.biz.dal.dao.ITaskExecLogDAO;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskExecLog;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskExecLogCriteria;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TaskExecLogDAOImpl extends BasicDAO<TaskExecLog, TaskExecLogCriteria> implements ITaskExecLogDAO {

    @Override
    public String getTableName() {
        return "task_exec_log";
    }

    public TaskExecLogDAOImpl() {
        super();
    }

    public int countByExample(TaskExecLogCriteria example) {
        Integer count = (Integer) this.count("task_exec_log.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(TaskExecLogCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("task_exec_log.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(TaskExecLogCriteria criteria) {
        return this.deleteRecords("task_exec_log.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long execLogId) {
        TaskExecLog key = new TaskExecLog();
        key.setExecLogId(execLogId);
        return this.deleteRecords("task_exec_log.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(TaskExecLog record) {
        Object newKey = this.insert("task_exec_log.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(TaskExecLog record) {
        Object newKey = this.insert("task_exec_log.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    @SuppressWarnings("unchecked")
    public List<TaskExecLog> selectByExampleWithBLOBs(TaskExecLogCriteria example) {
        List<TaskExecLog> list = this.list("task_exec_log.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<TaskExecLog> selectByExampleWithoutBLOBs(TaskExecLogCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<TaskExecLog> selectByExampleWithoutBLOBs(TaskExecLogCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<TaskExecLog> list = this.list("task_exec_log.ibatorgenerated_selectByExample", example);
        return list;
    }

    public TaskExecLog selectByPrimaryKey(Long execLogId) {
        TaskExecLog key = new TaskExecLog();
        key.setExecLogId(execLogId);
        TaskExecLog record = (TaskExecLog) this.load("task_exec_log.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(TaskExecLog record, TaskExecLogCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("task_exec_log.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(TaskExecLog record, TaskExecLogCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("task_exec_log.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(TaskExecLog record, TaskExecLogCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("task_exec_log.ibatorgenerated_updateByExample", parms);
    }

    public TaskExecLog loadFromWriteDB(Long execLogId) {
        TaskExecLog key = new TaskExecLog();
        key.setExecLogId(execLogId);
        TaskExecLog record = (TaskExecLog) this.loadFromWriterDB("task_exec_log.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends TaskExecLogCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, TaskExecLogCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
