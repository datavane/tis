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
package com.qlangtech.tis.realtime.test.order.dao.impl;

import com.qlangtech.tis.ibatis.BasicDAO;
import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.order.dao.IUserDAO;
import com.qlangtech.tis.realtime.test.order.pojo.User;
import com.qlangtech.tis.realtime.test.order.pojo.UserCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class UserDAOImpl extends BasicDAO<User, UserCriteria> implements IUserDAO {

    public UserDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "user";
    }

    public int countByExample(UserCriteria example) {
        Integer count = (Integer) this.count("user.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(UserCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("user.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(UserCriteria criteria) {
        return this.deleteRecords("user.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        User key = new User();
        key.setId(id);
        return this.deleteRecords("user.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(User record) {
        this.insert("user.ibatorgenerated_insert", record);
    }

    public void insertSelective(User record) {
        this.insert("user.ibatorgenerated_insertSelective", record);
    }

    public List<User> selectByExample(UserCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(UserCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.UserCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("user.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<User> selectByExample(UserCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<User> list = this.list("user.ibatorgenerated_selectByExample", example);
        return list;
    }

    public User selectByPrimaryKey(String id) {
        User key = new User();
        key.setId(id);
        User record = (User) this.load("user.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(User record, UserCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("user.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(User record, UserCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("user.ibatorgenerated_updateByExample", parms);
    }

    public User loadFromWriteDB(String id) {
        User key = new User();
        key.setId(id);
        User record = (User) this.loadFromWriterDB("user.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends UserCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, UserCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
