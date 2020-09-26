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
import com.qlangtech.tis.realtime.test.order.dao.IRefundPayItemDAO;
import com.qlangtech.tis.realtime.test.order.pojo.RefundPayItem;
import com.qlangtech.tis.realtime.test.order.pojo.RefundPayItemCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class RefundPayItemDAOImpl extends BasicDAO<RefundPayItem, RefundPayItemCriteria> implements IRefundPayItemDAO {

    public RefundPayItemDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "refund_pay_item";
    }

    public int countByExample(RefundPayItemCriteria example) {
        Integer count = (Integer) this.count("refund_pay_item.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(RefundPayItemCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("refund_pay_item.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(RefundPayItemCriteria criteria) {
        return this.deleteRecords("refund_pay_item.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        RefundPayItem key = new RefundPayItem();
        key.setId(id);
        return this.deleteRecords("refund_pay_item.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(RefundPayItem record) {
        this.insert("refund_pay_item.ibatorgenerated_insert", record);
    }

    public void insertSelective(RefundPayItem record) {
        this.insert("refund_pay_item.ibatorgenerated_insertSelective", record);
    }

    public List<RefundPayItem> selectByExample(RefundPayItemCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(RefundPayItemCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.RefundPayItemCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("refund_pay_item.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<RefundPayItem> selectByExample(RefundPayItemCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<RefundPayItem> list = this.list("refund_pay_item.ibatorgenerated_selectByExample", example);
        return list;
    }

    public RefundPayItem selectByPrimaryKey(String id) {
        RefundPayItem key = new RefundPayItem();
        key.setId(id);
        RefundPayItem record = (RefundPayItem) this.load("refund_pay_item.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(RefundPayItem record, RefundPayItemCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("refund_pay_item.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(RefundPayItem record, RefundPayItemCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("refund_pay_item.ibatorgenerated_updateByExample", parms);
    }

    public RefundPayItem loadFromWriteDB(String id) {
        RefundPayItem key = new RefundPayItem();
        key.setId(id);
        RefundPayItem record = (RefundPayItem) this.loadFromWriterDB("refund_pay_item.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends RefundPayItemCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, RefundPayItemCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
