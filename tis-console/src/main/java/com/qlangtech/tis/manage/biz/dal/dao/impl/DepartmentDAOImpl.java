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
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DepartmentDAOImpl extends BasicDAO<Department, DepartmentCriteria> implements IDepartmentDAO {

    @Override
    public String getEntityName() {
        return "department";
    }

    @Override
    public List<Department> selectByInnerJoinWithExtraDptUsrRelation(String userid) {
        return this.listAnonymity("department.ibatorgenerated_join_with_extra_dpt_usr_relation", userid);
    }

    public DepartmentDAOImpl() {
        super();
    }

    public int countByExample(DepartmentCriteria example) {
        Integer count = (Integer) this.count("department.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(DepartmentCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("department.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(DepartmentCriteria criteria) {
        return this.deleteRecords("department.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer dptId) {
        Department key = new Department();
        key.setDptId(dptId);
        return this.deleteRecords("department.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(Department record) {
        Object newKey = this.insert("department.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(Department record) {
        Object newKey = this.insert("department.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<Department> selectByExample(DepartmentCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<Department> selectByExample(DepartmentCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Department> list = this.list("department.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Department selectByPrimaryKey(Integer dptId) {
        Department key = new Department();
        key.setDptId(dptId);
        Department record = (Department) this.load("department.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Department record, DepartmentCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("department.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Department record, DepartmentCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("department.ibatorgenerated_updateByExample", parms);
    }

    public Department loadFromWriteDB(Integer dptId) {
        Department key = new Department();
        key.setDptId(dptId);
        Department record = (Department) this.loadFromWriterDB("department.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends DepartmentCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, DepartmentCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
