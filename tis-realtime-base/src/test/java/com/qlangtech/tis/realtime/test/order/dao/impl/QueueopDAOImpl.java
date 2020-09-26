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
import com.qlangtech.tis.realtime.test.order.dao.IQueueopDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Queueop;
import com.qlangtech.tis.realtime.test.order.pojo.QueueopCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class QueueopDAOImpl extends BasicDAO<Queueop, QueueopCriteria> implements IQueueopDAO {

    public QueueopDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "queueop";
    }

    public int countByExample(QueueopCriteria example) {
        Integer count = (Integer) this.count("queueop.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(QueueopCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("queueop.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(QueueopCriteria criteria) {
        return this.deleteRecords("queueop.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String queueopId) {
        Queueop key = new Queueop();
        key.setQueueopId(queueopId);
        return this.deleteRecords("queueop.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Queueop record) {
        this.insert("queueop.ibatorgenerated_insert", record);
    }

    public void insertSelective(Queueop record) {
        this.insert("queueop.ibatorgenerated_insertSelective", record);
    }

    public List<Queueop> selectByExample(QueueopCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(QueueopCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.QueueopCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("queueop.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Queueop> selectByExample(QueueopCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Queueop> list = this.list("queueop.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Queueop selectByPrimaryKey(String queueopId) {
        Queueop key = new Queueop();
        key.setQueueopId(queueopId);
        Queueop record = (Queueop) this.load("queueop.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Queueop record, QueueopCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("queueop.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Queueop record, QueueopCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("queueop.ibatorgenerated_updateByExample", parms);
    }

    public Queueop loadFromWriteDB(String queueopId) {
        Queueop key = new Queueop();
        key.setQueueopId(queueopId);
        Queueop record = (Queueop) this.loadFromWriterDB("queueop.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends QueueopCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, QueueopCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
