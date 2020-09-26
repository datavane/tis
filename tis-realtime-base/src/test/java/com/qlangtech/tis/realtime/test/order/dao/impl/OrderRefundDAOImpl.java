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
import com.qlangtech.tis.realtime.test.order.dao.IOrderRefundDAO;
import com.qlangtech.tis.realtime.test.order.pojo.OrderRefund;
import com.qlangtech.tis.realtime.test.order.pojo.OrderRefundCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class OrderRefundDAOImpl extends BasicDAO<OrderRefund, OrderRefundCriteria> implements IOrderRefundDAO {

    public OrderRefundDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "order_refund";
    }

    public int countByExample(OrderRefundCriteria example) {
        Integer count = (Integer) this.count("order_refund.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(OrderRefundCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("order_refund.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(OrderRefundCriteria criteria) {
        return this.deleteRecords("order_refund.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        OrderRefund key = new OrderRefund();
        key.setId(id);
        return this.deleteRecords("order_refund.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(OrderRefund record) {
        this.insert("order_refund.ibatorgenerated_insert", record);
    }

    public void insertSelective(OrderRefund record) {
        this.insert("order_refund.ibatorgenerated_insertSelective", record);
    }

    public List<OrderRefund> selectByExample(OrderRefundCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(OrderRefundCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.OrderRefundCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("order_refund.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<OrderRefund> selectByExample(OrderRefundCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<OrderRefund> list = this.list("order_refund.ibatorgenerated_selectByExample", example);
        return list;
    }

    public OrderRefund selectByPrimaryKey(String id) {
        OrderRefund key = new OrderRefund();
        key.setId(id);
        OrderRefund record = (OrderRefund) this.load("order_refund.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(OrderRefund record, OrderRefundCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_refund.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(OrderRefund record, OrderRefundCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_refund.ibatorgenerated_updateByExample", parms);
    }

    public OrderRefund loadFromWriteDB(String id) {
        OrderRefund key = new OrderRefund();
        key.setId(id);
        OrderRefund record = (OrderRefund) this.loadFromWriterDB("order_refund.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends OrderRefundCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, OrderRefundCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
