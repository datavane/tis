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
import com.qlangtech.tis.manage.biz.dal.dao.IFuncRoleRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelationCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class FuncRoleRelationDAOImpl extends BasicDAO<FuncRoleRelation, FuncRoleRelationCriteria> implements IFuncRoleRelationDAO {

    public FuncRoleRelationDAOImpl() {
        super();
    }

    @Override
    public String getEntityName() {
        return "funcRoleRelation";
    }

    public int countByExample(FuncRoleRelationCriteria example) {
        Integer count = (Integer) this.count("func_role_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    @SuppressWarnings("unchecked")
    public List<String> selectFuncListByUsrid(String usrid) {
        return (List<String>) this.getSqlMapClientTemplate().queryForList("func_role_relation.ibatorgenerated_select_func_key_ByUsrid", usrid);
    }

    public int countFromWriteDB(FuncRoleRelationCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("func_role_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(FuncRoleRelationCriteria criteria) {
        return this.deleteRecords("func_role_relation.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer id) {
        FuncRoleRelation key = new FuncRoleRelation();
        key.setId(id);
        return this.deleteRecords("func_role_relation.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(FuncRoleRelation record) {
        Object newKey = this.insert("func_role_relation.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(FuncRoleRelation record) {
        Object newKey = this.insert("func_role_relation.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<FuncRoleRelation> selectByExample(FuncRoleRelationCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<FuncRoleRelation> selectByExample(FuncRoleRelationCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<FuncRoleRelation> list = this.list("func_role_relation.ibatorgenerated_selectByExample", example);
        return list;
    }

    public FuncRoleRelation selectByPrimaryKey(Integer id) {
        FuncRoleRelation key = new FuncRoleRelation();
        key.setId(id);
        FuncRoleRelation record = (FuncRoleRelation) this.load("func_role_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(FuncRoleRelation record, FuncRoleRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("func_role_relation.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(FuncRoleRelation record, FuncRoleRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("func_role_relation.ibatorgenerated_updateByExample", parms);
    }

    public FuncRoleRelation loadFromWriteDB(Integer id) {
        FuncRoleRelation key = new FuncRoleRelation();
        key.setId(id);
        FuncRoleRelation record = (FuncRoleRelation) this.loadFromWriterDB("func_role_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends FuncRoleRelationCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, FuncRoleRelationCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
