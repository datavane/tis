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
import com.qlangtech.tis.manage.biz.dal.dao.IZookeeperServerDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ZookeeperServer;
import com.qlangtech.tis.manage.biz.dal.pojo.ZookeeperServerCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
