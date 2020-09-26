/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IAppTriggerJobRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelationCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.terminator.manage.common.BasicDAO#getTableName()
	 */
    @Override
    public String getEntityName() {
        return "app_trigger_job_relation";
    }
}
