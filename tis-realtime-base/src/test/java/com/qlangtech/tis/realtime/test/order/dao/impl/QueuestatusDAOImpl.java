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
import com.qlangtech.tis.realtime.test.order.dao.IQueuestatusDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Queuestatus;
import com.qlangtech.tis.realtime.test.order.pojo.QueuestatusCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class QueuestatusDAOImpl extends BasicDAO<Queuestatus, QueuestatusCriteria> implements IQueuestatusDAO {

    public QueuestatusDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "queuestatus";
    }

    public int countByExample(QueuestatusCriteria example) {
        Integer count = (Integer) this.count("queuestatus.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(QueuestatusCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("queuestatus.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(QueuestatusCriteria criteria) {
        return this.deleteRecords("queuestatus.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String entityId) {
        Queuestatus key = new Queuestatus();
        key.setEntityId(entityId);
        return this.deleteRecords("queuestatus.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Queuestatus record) {
        this.insert("queuestatus.ibatorgenerated_insert", record);
    }

    public void insertSelective(Queuestatus record) {
        this.insert("queuestatus.ibatorgenerated_insertSelective", record);
    }

    public List<Queuestatus> selectByExample(QueuestatusCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(QueuestatusCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.QueuestatusCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("queuestatus.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Queuestatus> selectByExample(QueuestatusCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Queuestatus> list = this.list("queuestatus.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Queuestatus selectByPrimaryKey(String entityId) {
        Queuestatus key = new Queuestatus();
        key.setEntityId(entityId);
        Queuestatus record = (Queuestatus) this.load("queuestatus.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Queuestatus record, QueuestatusCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("queuestatus.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Queuestatus record, QueuestatusCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("queuestatus.ibatorgenerated_updateByExample", parms);
    }

    public Queuestatus loadFromWriteDB(String entityId) {
        Queuestatus key = new Queuestatus();
        key.setEntityId(entityId);
        Queuestatus record = (Queuestatus) this.loadFromWriterDB("queuestatus.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends QueuestatusCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, QueuestatusCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
