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
import com.qlangtech.tis.trigger.biz.dal.dao.ITriggerJobDAO;
import com.qlangtech.tis.trigger.biz.dal.pojo.TriggerJob;
import com.qlangtech.tis.trigger.biz.dal.pojo.TriggerJobCriteria;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TriggerJobDAOImpl extends BasicDAO<TriggerJob, TriggerJobCriteria> implements ITriggerJobDAO {

    public TriggerJobDAOImpl() {
        super();
    }

    @Override
    public String getTableName() {
        return "trigger_job";
    }

    public int countByExample(TriggerJobCriteria example) {
        Integer count = (Integer) this.count("trigger_job.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(TriggerJobCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("trigger_job.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(TriggerJobCriteria criteria) {
        return this.deleteRecords("trigger_job.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long jobId) {
        TriggerJob key = new TriggerJob();
        key.setJobId(jobId);
        return this.deleteRecords("trigger_job.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(TriggerJob record) {
        Object newKey = this.insert("trigger_job.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(TriggerJob record) {
        Object newKey = this.insert("trigger_job.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<TriggerJob> selectByExample(TriggerJobCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<TriggerJob> selectByExample(TriggerJobCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<TriggerJob> list = this.list("trigger_job.ibatorgenerated_selectByExample", example);
        return list;
    }

    public TriggerJob selectByPrimaryKey(Long jobId) {
        TriggerJob key = new TriggerJob();
        key.setJobId(jobId);
        TriggerJob record = (TriggerJob) this.load("trigger_job.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(TriggerJob record, TriggerJobCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("trigger_job.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(TriggerJob record, TriggerJobCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("trigger_job.ibatorgenerated_updateByExample", parms);
    }

    public TriggerJob loadFromWriteDB(Long jobId) {
        TriggerJob key = new TriggerJob();
        key.setJobId(jobId);
        TriggerJob record = (TriggerJob) this.loadFromWriterDB("trigger_job.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends TriggerJobCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, TriggerJobCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
