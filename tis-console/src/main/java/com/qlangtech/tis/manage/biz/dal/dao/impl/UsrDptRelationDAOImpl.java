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

import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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

    @SuppressWarnings("unchecked")
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
