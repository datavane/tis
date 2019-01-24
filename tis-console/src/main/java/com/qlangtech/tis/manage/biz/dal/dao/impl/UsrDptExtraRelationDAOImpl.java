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
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptExtraRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptExtraRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptExtraRelationCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UsrDptExtraRelationDAOImpl extends BasicDAO<UsrDptExtraRelation, UsrDptExtraRelationCriteria> implements IUsrDptExtraRelationDAO {

    @Override
    public String getEntityName() {
        return "usr_dpt_extra_relation";
    }

    public UsrDptExtraRelationDAOImpl() {
        super();
    }

    public int countByExample(UsrDptExtraRelationCriteria example) {
        Integer count = (Integer) this.count("usr_dpt_extra_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(UsrDptExtraRelationCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("usr_dpt_extra_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(UsrDptExtraRelationCriteria criteria) {
        return this.deleteRecords("usr_dpt_extra_relation.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        UsrDptExtraRelation key = new UsrDptExtraRelation();
        key.setId(id);
        return this.deleteRecords("usr_dpt_extra_relation.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(UsrDptExtraRelation record) {
        Object newKey = this.insert("usr_dpt_extra_relation.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(UsrDptExtraRelation record) {
        Object newKey = this.insert("usr_dpt_extra_relation.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<UsrDptExtraRelation> selectByExample(UsrDptExtraRelationCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<UsrDptExtraRelation> selectByExample(UsrDptExtraRelationCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<UsrDptExtraRelation> list = this.list("usr_dpt_extra_relation.ibatorgenerated_selectByExample", example);
        return list;
    }

    public UsrDptExtraRelation selectByPrimaryKey(Long id) {
        UsrDptExtraRelation key = new UsrDptExtraRelation();
        key.setId(id);
        UsrDptExtraRelation record = (UsrDptExtraRelation) this.load("usr_dpt_extra_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(UsrDptExtraRelation record, UsrDptExtraRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("usr_dpt_extra_relation.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(UsrDptExtraRelation record, UsrDptExtraRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("usr_dpt_extra_relation.ibatorgenerated_updateByExample", parms);
    }

    public UsrDptExtraRelation loadFromWriteDB(Long id) {
        UsrDptExtraRelation key = new UsrDptExtraRelation();
        key.setId(id);
        UsrDptExtraRelation record = (UsrDptExtraRelation) this.loadFromWriterDB("usr_dpt_extra_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends UsrDptExtraRelationCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, UsrDptExtraRelationCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
