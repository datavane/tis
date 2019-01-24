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
import com.qlangtech.tis.manage.biz.dal.dao.IServerDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Server;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
