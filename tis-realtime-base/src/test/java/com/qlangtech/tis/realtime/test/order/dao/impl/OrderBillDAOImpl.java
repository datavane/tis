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
import com.qlangtech.tis.realtime.test.order.dao.IOrderBillDAO;
import com.qlangtech.tis.realtime.test.order.pojo.OrderBill;
import com.qlangtech.tis.realtime.test.order.pojo.OrderBillCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class OrderBillDAOImpl extends BasicDAO<OrderBill, OrderBillCriteria> implements IOrderBillDAO {

    public OrderBillDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "order_bill";
    }

    public int countByExample(OrderBillCriteria example) {
        Integer count = (Integer) this.count("order_bill.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(OrderBillCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("order_bill.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(OrderBillCriteria criteria) {
        return this.deleteRecords("order_bill.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        OrderBill key = new OrderBill();
        key.setId(id);
        return this.deleteRecords("order_bill.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(OrderBill record) {
        this.insert("order_bill.ibatorgenerated_insert", record);
    }

    public void insertSelective(OrderBill record) {
        this.insert("order_bill.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<OrderBill> selectByExampleWithBLOBs(OrderBillCriteria example) {
        List<OrderBill> list = this.list("order_bill.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<OrderBill> selectByExampleWithoutBLOBs(OrderBillCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(OrderBillCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.OrderBillCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("order_bill.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<OrderBill> selectByExampleWithoutBLOBs(OrderBillCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<OrderBill> list = this.list("order_bill.ibatorgenerated_selectByExample", example);
        return list;
    }

    public OrderBill selectByPrimaryKey(String id) {
        OrderBill key = new OrderBill();
        key.setId(id);
        OrderBill record = (OrderBill) this.load("order_bill.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(OrderBill record, OrderBillCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_bill.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(OrderBill record, OrderBillCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("order_bill.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(OrderBill record, OrderBillCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_bill.ibatorgenerated_updateByExample", parms);
    }

    public OrderBill loadFromWriteDB(String id) {
        OrderBill key = new OrderBill();
        key.setId(id);
        OrderBill record = (OrderBill) this.loadFromWriterDB("order_bill.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends OrderBillCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, OrderBillCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
