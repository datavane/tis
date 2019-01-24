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
import com.qlangtech.tis.manage.biz.dal.dao.IFuncRoleRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelationCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
