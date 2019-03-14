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
import com.qlangtech.tis.manage.biz.dal.dao.IGroupInfoDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.GroupInfo;
import com.qlangtech.tis.manage.biz.dal.pojo.GroupInfoCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
        Integer count = (Integer) this.count("group_info.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(GroupInfoCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("group_info.ibatorgenerated_countByExample", example);
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

    @SuppressWarnings("all")
    public List<GroupInfo> selectByExample(GroupInfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<GroupInfo> list = this.list("group_info.ibatorgenerated_selectByExample", example);
        return list;
    }

    public GroupInfo selectByPrimaryKey(Integer gid) {
        GroupInfo key = new GroupInfo();
        key.setGid(gid);
        GroupInfo record = (GroupInfo) this.load("group_info.ibatorgenerated_selectByPrimaryKey", key);
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
        GroupInfo record = (GroupInfo) this.loadFromWriterDB("group_info.ibatorgenerated_selectByPrimaryKey", key);
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
