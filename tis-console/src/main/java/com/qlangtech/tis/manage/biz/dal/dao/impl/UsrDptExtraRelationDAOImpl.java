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
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptExtraRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptExtraRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptExtraRelationCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
        Integer count = this.count("usr_dpt_extra_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(UsrDptExtraRelationCriteria example) {
        Integer count = this.countFromWriterDB("usr_dpt_extra_relation.ibatorgenerated_countByExample", example);
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
        UsrDptExtraRelation record = this.load("usr_dpt_extra_relation.ibatorgenerated_selectByPrimaryKey", key);
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
        UsrDptExtraRelation record = this.loadFromWriterDB("usr_dpt_extra_relation.ibatorgenerated_selectByPrimaryKey", key);
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
