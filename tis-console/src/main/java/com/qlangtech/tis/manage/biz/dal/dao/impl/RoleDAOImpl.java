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
import com.qlangtech.tis.manage.biz.dal.dao.IRoleDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Role;
import com.qlangtech.tis.manage.biz.dal.pojo.RoleCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
