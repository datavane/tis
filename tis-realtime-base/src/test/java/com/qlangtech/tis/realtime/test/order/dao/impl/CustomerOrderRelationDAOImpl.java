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
import com.qlangtech.tis.realtime.test.order.dao.ICustomerOrderRelationDAO;
import com.qlangtech.tis.realtime.test.order.pojo.CustomerOrderRelation;
import com.qlangtech.tis.realtime.test.order.pojo.CustomerOrderRelationCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class CustomerOrderRelationDAOImpl extends BasicDAO<CustomerOrderRelation, CustomerOrderRelationCriteria> implements ICustomerOrderRelationDAO {

    public CustomerOrderRelationDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "customer_order_relation";
    }

    public int countByExample(CustomerOrderRelationCriteria example) {
        Integer count = (Integer) this.count("customer_order_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(CustomerOrderRelationCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("customer_order_relation.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(CustomerOrderRelationCriteria criteria) {
        return this.deleteRecords("customer_order_relation.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String customerregisterId, String waitingorderId) {
        CustomerOrderRelation key = new CustomerOrderRelation();
        key.setCustomerregisterId(customerregisterId);
        key.setWaitingorderId(waitingorderId);
        return this.deleteRecords("customer_order_relation.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(CustomerOrderRelation record) {
        this.insert("customer_order_relation.ibatorgenerated_insert", record);
    }

    public void insertSelective(CustomerOrderRelation record) {
        this.insert("customer_order_relation.ibatorgenerated_insertSelective", record);
    }

    public List<CustomerOrderRelation> selectByExample(CustomerOrderRelationCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(CustomerOrderRelationCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.CustomerOrderRelationCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("customer_order_relation.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<CustomerOrderRelation> selectByExample(CustomerOrderRelationCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<CustomerOrderRelation> list = this.list("customer_order_relation.ibatorgenerated_selectByExample", example);
        return list;
    }

    public CustomerOrderRelation selectByPrimaryKey(String customerregisterId, String waitingorderId) {
        CustomerOrderRelation key = new CustomerOrderRelation();
        key.setCustomerregisterId(customerregisterId);
        key.setWaitingorderId(waitingorderId);
        CustomerOrderRelation record = (CustomerOrderRelation) this.load("customer_order_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(CustomerOrderRelation record, CustomerOrderRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("customer_order_relation.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(CustomerOrderRelation record, CustomerOrderRelationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("customer_order_relation.ibatorgenerated_updateByExample", parms);
    }

    public CustomerOrderRelation loadFromWriteDB(String customerregisterId, String waitingorderId) {
        CustomerOrderRelation key = new CustomerOrderRelation();
        key.setCustomerregisterId(customerregisterId);
        key.setWaitingorderId(waitingorderId);
        CustomerOrderRelation record = (CustomerOrderRelation) this.loadFromWriterDB("customer_order_relation.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends CustomerOrderRelationCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, CustomerOrderRelationCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
