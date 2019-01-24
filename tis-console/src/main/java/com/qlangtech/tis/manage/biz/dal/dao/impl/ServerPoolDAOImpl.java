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
import com.qlangtech.tis.manage.biz.dal.dao.IServerPoolDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerPool;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerPoolCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
        Integer count = (Integer) this.count("server_pool.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ServerPoolCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("server_pool.ibatorgenerated_countByExample", example);
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
        ServerPool record = (ServerPool) this.load("server_pool.ibatorgenerated_selectByPrimaryKey", key);
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
        ServerPool record = (ServerPool) this.loadFromWriterDB("server_pool.ibatorgenerated_selectByPrimaryKey", key);
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
