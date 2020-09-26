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
import com.qlangtech.tis.realtime.test.order.dao.IOrderSnapshotDAO;
import com.qlangtech.tis.realtime.test.order.pojo.OrderSnapshot;
import com.qlangtech.tis.realtime.test.order.pojo.OrderSnapshotCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class OrderSnapshotDAOImpl extends BasicDAO<OrderSnapshot, OrderSnapshotCriteria> implements IOrderSnapshotDAO {

    public OrderSnapshotDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "order_snapshot";
    }

    public int countByExample(OrderSnapshotCriteria example) {
        Integer count = (Integer) this.count("order_snapshot.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(OrderSnapshotCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("order_snapshot.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(OrderSnapshotCriteria criteria) {
        return this.deleteRecords("order_snapshot.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String snapshotId) {
        OrderSnapshot key = new OrderSnapshot();
        key.setSnapshotId(snapshotId);
        return this.deleteRecords("order_snapshot.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(OrderSnapshot record) {
        this.insert("order_snapshot.ibatorgenerated_insert", record);
    }

    public void insertSelective(OrderSnapshot record) {
        this.insert("order_snapshot.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<OrderSnapshot> selectByExampleWithBLOBs(OrderSnapshotCriteria example) {
        List<OrderSnapshot> list = this.list("order_snapshot.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<OrderSnapshot> selectByExampleWithoutBLOBs(OrderSnapshotCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(OrderSnapshotCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.OrderSnapshotCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("order_snapshot.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<OrderSnapshot> selectByExampleWithoutBLOBs(OrderSnapshotCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<OrderSnapshot> list = this.list("order_snapshot.ibatorgenerated_selectByExample", example);
        return list;
    }

    public OrderSnapshot selectByPrimaryKey(String snapshotId) {
        OrderSnapshot key = new OrderSnapshot();
        key.setSnapshotId(snapshotId);
        OrderSnapshot record = (OrderSnapshot) this.load("order_snapshot.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(OrderSnapshot record, OrderSnapshotCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_snapshot.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(OrderSnapshot record, OrderSnapshotCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("order_snapshot.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(OrderSnapshot record, OrderSnapshotCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_snapshot.ibatorgenerated_updateByExample", parms);
    }

    public OrderSnapshot loadFromWriteDB(String snapshotId) {
        OrderSnapshot key = new OrderSnapshot();
        key.setSnapshotId(snapshotId);
        OrderSnapshot record = (OrderSnapshot) this.loadFromWriterDB("order_snapshot.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends OrderSnapshotCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, OrderSnapshotCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
