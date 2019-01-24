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
import com.qlangtech.tis.manage.biz.dal.dao.IZookeeperServerDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ZookeeperServer;
import com.qlangtech.tis.manage.biz.dal.pojo.ZookeeperServerCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ZookeeperServerDAOImpl extends BasicDAO<ZookeeperServer, ZookeeperServerCriteria> implements IZookeeperServerDAO {

    @Override
    public String getEntityName() {
        return "zookeeper_server";
    }

    public ZookeeperServerDAOImpl() {
        super();
    }

    public int countByExample(ZookeeperServerCriteria example) {
        Integer count = (Integer) this.count("zookeeper_server.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ZookeeperServerCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("zookeeper_server.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ZookeeperServerCriteria criteria) {
        return this.deleteRecords("zookeeper_server.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer zid) {
        ZookeeperServer key = new ZookeeperServer();
        key.setZid(zid);
        return this.deleteRecords("zookeeper_server.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(ZookeeperServer record) {
        this.insert("zookeeper_server.ibatorgenerated_insert", record);
    }

    public void insertSelective(ZookeeperServer record) {
        this.insert("zookeeper_server.ibatorgenerated_insertSelective", record);
    }

    public List<ZookeeperServer> selectByExample(ZookeeperServerCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<ZookeeperServer> selectByExample(ZookeeperServerCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<ZookeeperServer> list = this.list("zookeeper_server.ibatorgenerated_selectByExample", example);
        return list;
    }

    public ZookeeperServer selectByPrimaryKey(Integer zid) {
        ZookeeperServer key = new ZookeeperServer();
        key.setZid(zid);
        ZookeeperServer record = (ZookeeperServer) this.load("zookeeper_server.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(ZookeeperServer record, ZookeeperServerCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("zookeeper_server.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(ZookeeperServer record, ZookeeperServerCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("zookeeper_server.ibatorgenerated_updateByExample", parms);
    }

    public ZookeeperServer loadFromWriteDB(Integer zid) {
        ZookeeperServer key = new ZookeeperServer();
        key.setZid(zid);
        ZookeeperServer record = (ZookeeperServer) this.loadFromWriterDB("zookeeper_server.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends ZookeeperServerCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ZookeeperServerCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
