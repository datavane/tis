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
import com.qlangtech.tis.trigger.biz.dal.dao.ITaskDAO;
import com.qlangtech.tis.trigger.biz.dal.pojo.ErrorJob;
import com.qlangtech.tis.trigger.biz.dal.pojo.Task;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskCriteria;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TaskDAOImpl extends BasicDAO<Task, TaskCriteria> implements ITaskDAO {

    @Override
    public String getTableName() {
        return "task";
    }

    @SuppressWarnings("all")
    @Override
    public List<ErrorJob> getRecentExecuteJobs(String environment) {
        return (List<ErrorJob>) this.getSqlMapClientTemplate().queryForList("task.executeJobsError", environment);
    }

    public TaskDAOImpl() {
        super();
    }

    public int countByExample(TaskCriteria example) {
        Integer count = (Integer) this.count("task.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(TaskCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("task.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(TaskCriteria criteria) {
        return this.deleteRecords("task.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long taskId) {
        Task key = new Task();
        key.setTaskId(taskId);
        return this.deleteRecords("task.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(Task record) {
        Object newKey = this.insert("task.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(Task record) {
        Object newKey = this.insert("task.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<Task> selectByExample(TaskCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<Task> selectByExample(TaskCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Task> list = this.list("task.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Task selectByPrimaryKey(Long taskId) {
        Task key = new Task();
        key.setTaskId(taskId);
        Task record = (Task) this.load("task.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Task record, TaskCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("task.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Task record, TaskCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("task.ibatorgenerated_updateByExample", parms);
    }

    public Task loadFromWriteDB(Long taskId) {
        Task key = new Task();
        key.setTaskId(taskId);
        Task record = (Task) this.loadFromWriterDB("task.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends TaskCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, TaskCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
