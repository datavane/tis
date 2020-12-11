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
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
        Integer count = this.count("department.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(DepartmentCriteria example) {
        Integer count = this.countFromWriterDB("department.ibatorgenerated_countByExample", example);
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
        Department record = this.load("department.ibatorgenerated_selectByPrimaryKey", key);
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
        Department record = this.loadFromWriterDB("department.ibatorgenerated_selectByPrimaryKey", key);
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
