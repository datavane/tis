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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IAppTriggerJobRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelationCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppTriggerJobRelationDAOImpl extends BasicDAO<AppTriggerJobRelation, AppTriggerJobRelationCriteria> implements IAppTriggerJobRelationDAO {

    public AppTriggerJobRelationDAOImpl() {
        super();
    }

    public int countByExample(AppTriggerJobRelationCriteria example) {
        Integer count = (Integer) this.count("app_trigger_job_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(AppTriggerJobRelationCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("app_trigger_job_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(AppTriggerJobRelationCriteria criteria) {
        return this.deleteRecords("app_trigger_job_relation.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long atId) {
        AppTriggerJobRelation key = new AppTriggerJobRelation();
        key.setAtId(atId);
        return this.deleteRecords("app_trigger_job_relation.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(AppTriggerJobRelation record) {
        Object newKey = this.insert("app_trigger_job_relation.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(AppTriggerJobRelation record) {
        Object newKey = this.insert("app_trigger_job_relation.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<AppTriggerJobRelation> selectByExample(AppTriggerJobRelationCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<AppTriggerJobRelation> selectByExample(AppTriggerJobRelationCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<AppTriggerJobRelation> list = this.list("app_trigger_job_relation.ibatorgenerated_selectByExample", example);
        return list;
    }

    public AppTriggerJobRelation selectByPrimaryKey(Long atId) {
        AppTriggerJobRelation key = new AppTriggerJobRelation();
        key.setAtId(atId);
        AppTriggerJobRelation record = (AppTriggerJobRelation) this.load("app_trigger_job_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(AppTriggerJobRelation record, AppTriggerJobRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("app_trigger_job_relation.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(AppTriggerJobRelation record, AppTriggerJobRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("app_trigger_job_relation.ibatorgenerated_updateByExample", parms);
    }

    public AppTriggerJobRelation loadFromWriteDB(Long atId) {
        AppTriggerJobRelation key = new AppTriggerJobRelation();
        key.setAtId(atId);
        AppTriggerJobRelation record = (AppTriggerJobRelation) this.loadFromWriterDB("app_trigger_job_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends AppTriggerJobRelationCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, AppTriggerJobRelationCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }


    @Override
    public String getEntityName() {
        return "app_trigger_job_relation";
    }
}
