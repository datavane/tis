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

import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class UsrDptRelationDAOImpl extends BasicDAO<UsrDptRelation, UsrDptRelationCriteria> implements IUsrDptRelationDAO {

    public UsrDptRelationDAOImpl() {
        super();
    }

    @Override
    public List<TriggerCrontab> selectAppDumpJob(UsrDptRelationCriteria criteria) {
        return this.listAnonymity("usr_dpt_relation.ibatorgenerated_select_out_join_app_trigger_job_relation_ByExample", criteria);
    }

    @Override
    public String getEntityName() {
        return "usr_dpt_relation";
    }

    public int countByExample(UsrDptRelationCriteria example) {
        Integer count = (Integer) this.count("usr_dpt_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(UsrDptRelationCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("usr_dpt_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(UsrDptRelationCriteria criteria) {
        return this.deleteRecords("usr_dpt_relation.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String usrId) {
        UsrDptRelation key = new UsrDptRelation();
        key.setUsrId(usrId);
        return this.deleteRecords("usr_dpt_relation.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(UsrDptRelation record) {
        this.insert("usr_dpt_relation.ibatorgenerated_insert", record);
    }

    public void insertSelective(UsrDptRelation record) {
        this.insert("usr_dpt_relation.ibatorgenerated_insertSelective", record);
    }

    public List<UsrDptRelation> selectByExample(UsrDptRelationCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public List<UsrDptRelation> selectByExample(UsrDptRelationCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<UsrDptRelation> list = this.list("usr_dpt_relation.ibatorgenerated_selectByExample", example);
        return list;
    }

    public UsrDptRelation selectByPrimaryKey(String usrId) {
        UsrDptRelation key = new UsrDptRelation();
        key.setUsrId(usrId);
        UsrDptRelation record = (UsrDptRelation) this.load("usr_dpt_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(UsrDptRelation record, UsrDptRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("usr_dpt_relation.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(UsrDptRelation record, UsrDptRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("usr_dpt_relation.ibatorgenerated_updateByExample", parms);
    }

    public UsrDptRelation loadFromWriteDB(String usrId) {
        UsrDptRelation key = new UsrDptRelation();
        key.setUsrId(usrId);
        UsrDptRelation record = (UsrDptRelation) this.loadFromWriterDB("usr_dpt_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends UsrDptRelationCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, UsrDptRelationCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
