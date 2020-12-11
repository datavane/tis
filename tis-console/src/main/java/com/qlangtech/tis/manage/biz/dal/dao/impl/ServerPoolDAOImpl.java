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
import com.qlangtech.tis.manage.biz.dal.dao.IServerPoolDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerPool;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerPoolCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ServerPoolDAOImpl extends BasicDAO<ServerPool, ServerPoolCriteria> implements IServerPoolDAO {

    @Override
    public String getEntityName() {
        return "server_pool";
    }

    public ServerPoolDAOImpl() {
        super();
    }

    public int countByExample(ServerPoolCriteria example) {
        Integer count = this.count("server_pool.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ServerPoolCriteria example) {
        Integer count = this.countFromWriterDB("server_pool.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ServerPoolCriteria criteria) {
        return this.deleteRecords("server_pool.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer spId) {
        ServerPool key = new ServerPool();
        key.setSpId(spId);
        return this.deleteRecords("server_pool.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(ServerPool record) {
        this.insert("server_pool.ibatorgenerated_insert", record);
    }

    public Integer insertSelective(ServerPool record) {
        return (Integer) this.insert("server_pool.ibatorgenerated_insertSelective", record);
    }

    public List<ServerPool> selectByExample(ServerPoolCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<ServerPool> selectByExample(ServerPoolCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<ServerPool> list = this.list("server_pool.ibatorgenerated_selectByExample", example);
        return list;
    }

    public ServerPool selectByPrimaryKey(Integer spId) {
        ServerPool key = new ServerPool();
        key.setSpId(spId);
        ServerPool record = this.load("server_pool.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(ServerPool record, ServerPoolCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("server_pool.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(ServerPool record, ServerPoolCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("server_pool.ibatorgenerated_updateByExample", parms);
    }

    public ServerPool loadFromWriteDB(Integer spId) {
        ServerPool key = new ServerPool();
        key.setSpId(spId);
        ServerPool record = this.loadFromWriterDB("server_pool.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends ServerPoolCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ServerPoolCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
