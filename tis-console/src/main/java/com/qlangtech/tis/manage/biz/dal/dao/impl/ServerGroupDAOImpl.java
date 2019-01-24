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
import com.qlangtech.tis.manage.biz.dal.dao.IServerGroupDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ServerGroupDAOImpl extends BasicDAO<ServerGroup, ServerGroupCriteria> implements IServerGroupDAO {

    @Override
    public String getEntityName() {
        return "server_group";
    }

    public ServerGroupDAOImpl() {
        super();
    }

    public int countByExample(ServerGroupCriteria example) {
        Integer count = (Integer) this.count("server_group.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ServerGroupCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("server_group.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ServerGroupCriteria criteria) {
        return this.deleteRecords("server_group.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer gid) {
        ServerGroup key = new ServerGroup();
        key.setGid(gid);
        return this.deleteRecords("server_group.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public int insert(ServerGroup record) {
        return (Integer) this.insert("server_group.ibatorgenerated_insert", record);
    }

    public Integer insertSelective(ServerGroup record) {
        return (Integer) this.insert("server_group.ibatorgenerated_insertSelective", record);
    }

    public List<ServerGroup> selectByExample(ServerGroupCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<ServerGroup> selectByExample(ServerGroupCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<ServerGroup> list = this.list("server_group.ibatorgenerated_selectByExample", example);
        return list;
    }

    public ServerGroup selectByPrimaryKey(Integer gid) {
        ServerGroup key = new ServerGroup();
        key.setGid(gid);
        ServerGroup record = (ServerGroup) this.load("server_group.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(ServerGroup record, ServerGroupCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("server_group.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(ServerGroup record, ServerGroupCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("server_group.ibatorgenerated_updateByExample", parms);
    }

    public ServerGroup loadFromWriteDB(Integer gid) {
        ServerGroup key = new ServerGroup();
        key.setGid(gid);
        ServerGroup record = (ServerGroup) this.loadFromWriterDB("server_group.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    /**
     * 百岁添加20120502
     */
    public ServerGroup load(String appName, Short groupIndex, Short runtime) {
        ServerGroup key = new ServerGroup();
        key.setAppName(appName);
        key.setGroupIndex(groupIndex);
        key.setRuntEnvironment(runtime);
        return (ServerGroup) this.loadFromWriterDB("server_group.ibatorgenerated_getBy_appName_groupIndex_runtime", key);
    }

    private static class UpdateByExampleParms extends ServerGroupCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ServerGroupCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
