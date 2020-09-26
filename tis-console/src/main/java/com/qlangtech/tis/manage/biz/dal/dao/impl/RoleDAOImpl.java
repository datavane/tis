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
import com.qlangtech.tis.manage.biz.dal.dao.IRoleDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Role;
import com.qlangtech.tis.manage.biz.dal.pojo.RoleCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class RoleDAOImpl extends BasicDAO<Role, RoleCriteria> implements IRoleDAO {

    @Override
    public String getEntityName() {
        return "role";
    }

    public RoleDAOImpl() {
        super();
    }

    public int countByExample(RoleCriteria example) {
        Integer count = (Integer) this.count("role.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(RoleCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("role.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(RoleCriteria criteria) {
        return this.deleteRecords("role.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer rId) {
        Role key = new Role();
        key.setrId(rId);
        return this.deleteRecords("role.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(Role record) {
        Object newKey = this.insert("role.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(Role record) {
        Object newKey = this.insert("role.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<Role> selectByExample(RoleCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<Role> selectByExample(RoleCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Role> list = this.list("role.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Role selectByPrimaryKey(Integer rId) {
        Role key = new Role();
        key.setrId(rId);
        Role record = (Role) this.load("role.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Role record, RoleCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("role.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Role record, RoleCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("role.ibatorgenerated_updateByExample", parms);
    }

    public Role loadFromWriteDB(Integer rId) {
        Role key = new Role();
        key.setrId(rId);
        Role record = (Role) this.loadFromWriterDB("role.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends RoleCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, RoleCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
