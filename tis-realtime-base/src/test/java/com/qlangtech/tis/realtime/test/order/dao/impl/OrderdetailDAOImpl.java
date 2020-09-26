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
import com.qlangtech.tis.realtime.test.order.dao.IOrderdetailDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Orderdetail;
import com.qlangtech.tis.realtime.test.order.pojo.OrderdetailCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class OrderdetailDAOImpl extends BasicDAO<Orderdetail, OrderdetailCriteria> implements IOrderdetailDAO {

    public OrderdetailDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "orderdetail";
    }

    public int countByExample(OrderdetailCriteria example) {
        Integer count = (Integer) this.count("orderdetail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(OrderdetailCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("orderdetail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(OrderdetailCriteria criteria) {
        return this.deleteRecords("orderdetail.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String orderId) {
        Orderdetail key = new Orderdetail();
        key.setOrderId(orderId);
        return this.deleteRecords("orderdetail.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Orderdetail record) {
        this.insert("orderdetail.ibatorgenerated_insert", record);
    }

    public void insertSelective(Orderdetail record) {
        this.insert("orderdetail.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<Orderdetail> selectByExampleWithBLOBs(OrderdetailCriteria example) {
        List<Orderdetail> list = this.list("orderdetail.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<Orderdetail> selectByExampleWithoutBLOBs(OrderdetailCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(OrderdetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.OrderdetailCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("orderdetail.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Orderdetail> selectByExampleWithoutBLOBs(OrderdetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Orderdetail> list = this.list("orderdetail.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Orderdetail selectByPrimaryKey(String orderId) {
        Orderdetail key = new Orderdetail();
        key.setOrderId(orderId);
        Orderdetail record = (Orderdetail) this.load("orderdetail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Orderdetail record, OrderdetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("orderdetail.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(Orderdetail record, OrderdetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("orderdetail.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(Orderdetail record, OrderdetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("orderdetail.ibatorgenerated_updateByExample", parms);
    }

    public Orderdetail loadFromWriteDB(String orderId) {
        Orderdetail key = new Orderdetail();
        key.setOrderId(orderId);
        Orderdetail record = (Orderdetail) this.loadFromWriterDB("orderdetail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends OrderdetailCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, OrderdetailCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
