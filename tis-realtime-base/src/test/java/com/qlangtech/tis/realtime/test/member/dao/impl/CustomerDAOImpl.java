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
package com.qlangtech.tis.realtime.test.member.dao.impl;

import com.qlangtech.tis.ibatis.BasicDAO;
import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.member.dao.ICustomerDAO;
import com.qlangtech.tis.realtime.test.member.pojo.Customer;
import com.qlangtech.tis.realtime.test.member.pojo.CustomerCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class CustomerDAOImpl extends BasicDAO<Customer, CustomerCriteria> implements ICustomerDAO {

    public CustomerDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "customer";
    }

    public int countByExample(CustomerCriteria example) {
        Integer count = (Integer) this.count("customer.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(CustomerCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("customer.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(CustomerCriteria criteria) {
        return this.deleteRecords("customer.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        Customer key = new Customer();
        key.setId(id);
        return this.deleteRecords("customer.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Customer record) {
        this.insert("customer.ibatorgenerated_insert", record);
    }

    public void insertSelective(Customer record) {
        this.insert("customer.ibatorgenerated_insertSelective", record);
    }

    public List<Customer> selectByExample(CustomerCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(CustomerCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.member.pojo.CustomerCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("customer.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Customer> selectByExample(CustomerCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Customer> list = this.list("customer.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Customer selectByPrimaryKey(String id) {
        Customer key = new Customer();
        key.setId(id);
        Customer record = (Customer) this.load("customer.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Customer record, CustomerCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("customer.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Customer record, CustomerCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("customer.ibatorgenerated_updateByExample", parms);
    }

    public Customer loadFromWriteDB(String id) {
        Customer key = new Customer();
        key.setId(id);
        Customer record = (Customer) this.loadFromWriterDB("customer.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends CustomerCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, CustomerCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
