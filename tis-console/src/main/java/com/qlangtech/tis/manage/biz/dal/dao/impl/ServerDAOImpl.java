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
import com.qlangtech.tis.manage.biz.dal.dao.IServerDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Server;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ServerDAOImpl extends BasicDAO<Server, ServerCriteria> implements IServerDAO {

    @Override
    public String getEntityName() {
        return "server";
    }

    public ServerDAOImpl() {
        super();
    }

    public int countByExample(ServerCriteria example) {
        Integer count = (Integer) this.count("server.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ServerCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("server.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ServerCriteria criteria) {
        return this.deleteRecords("server.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer sid) {
        Server key = new Server();
        key.setSid(sid);
        return this.deleteRecords("server.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(Server record) {
        return (Integer) this.insert("server.ibatorgenerated_insert", record);
    }

    public Integer insertSelective(Server record) {
        return (Integer) this.insert("server.ibatorgenerated_insertSelective", record);
    }

    public List<Server> selectByExample(ServerCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<Server> selectByExample(ServerCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Server> list = this.list("server.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Server selectByPrimaryKey(Integer sid) {
        Server key = new Server();
        key.setSid(sid);
        Server record = (Server) this.load("server.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Server record, ServerCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("server.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Server record, ServerCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("server.ibatorgenerated_updateByExample", parms);
    }

    public Server loadFromWriteDB(Integer sid) {
        Server key = new Server();
        key.setSid(sid);
        Server record = (Server) this.loadFromWriterDB("server.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends ServerCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ServerCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
