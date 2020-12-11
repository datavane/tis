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
import com.qlangtech.tis.manage.biz.dal.dao.IGroupInfoDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.GroupInfo;
import com.qlangtech.tis.manage.biz.dal.pojo.GroupInfoCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class GroupInfoDAOImpl extends BasicDAO<GroupInfo, GroupInfoCriteria> implements IGroupInfoDAO {

    @Override
    public String getEntityName() {
        return "group_info";
    }

    public GroupInfoDAOImpl() {
        super();
    }

    public int countByExample(GroupInfoCriteria example) {
        Integer count = this.count("group_info.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(GroupInfoCriteria example) {
        Integer count = this.countFromWriterDB("group_info.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(GroupInfoCriteria criteria) {
        return this.deleteRecords("group_info.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer gid) {
        GroupInfo key = new GroupInfo();
        key.setGid(gid);
        return this.deleteRecords("group_info.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(GroupInfo record) {
        this.insert("group_info.ibatorgenerated_insert", record);
    }

    public void insertSelective(GroupInfo record) {
        this.insert("group_info.ibatorgenerated_insertSelective", record);
    }

    public List<GroupInfo> selectByExample(GroupInfoCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<GroupInfo> selectByExample(GroupInfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<GroupInfo> list = this.list("group_info.ibatorgenerated_selectByExample", example);
        return list;
    }

    public GroupInfo selectByPrimaryKey(Integer gid) {
        GroupInfo key = new GroupInfo();
        key.setGid(gid);
        GroupInfo record = this.load("group_info.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(GroupInfo record, GroupInfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("group_info.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(GroupInfo record, GroupInfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("group_info.ibatorgenerated_updateByExample", parms);
    }

    public GroupInfo loadFromWriteDB(Integer gid) {
        GroupInfo key = new GroupInfo();
        key.setGid(gid);
        GroupInfo record = this.loadFromWriterDB("group_info.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends GroupInfoCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, GroupInfoCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
