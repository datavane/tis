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
import com.qlangtech.tis.realtime.test.order.dao.IOrderTagDAO;
import com.qlangtech.tis.realtime.test.order.pojo.OrderTag;
import com.qlangtech.tis.realtime.test.order.pojo.OrderTagCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class OrderTagDAOImpl extends BasicDAO<OrderTag, OrderTagCriteria> implements IOrderTagDAO {

    public OrderTagDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "order_tag";
    }

    public int countByExample(OrderTagCriteria example) {
        Integer count = (Integer) this.count("order_tag.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(OrderTagCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("order_tag.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(OrderTagCriteria criteria) {
        return this.deleteRecords("order_tag.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        OrderTag key = new OrderTag();
        key.setId(id);
        return this.deleteRecords("order_tag.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(OrderTag record) {
        this.insert("order_tag.ibatorgenerated_insert", record);
    }

    public void insertSelective(OrderTag record) {
        this.insert("order_tag.ibatorgenerated_insertSelective", record);
    }

    public List<OrderTag> selectByExample(OrderTagCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(OrderTagCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.OrderTagCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("order_tag.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<OrderTag> selectByExample(OrderTagCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<OrderTag> list = this.list("order_tag.ibatorgenerated_selectByExample", example);
        return list;
    }

    public OrderTag selectByPrimaryKey(Long id) {
        OrderTag key = new OrderTag();
        key.setId(id);
        OrderTag record = (OrderTag) this.load("order_tag.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(OrderTag record, OrderTagCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_tag.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(OrderTag record, OrderTagCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_tag.ibatorgenerated_updateByExample", parms);
    }

    public OrderTag loadFromWriteDB(Long id) {
        OrderTag key = new OrderTag();
        key.setId(id);
        OrderTag record = (OrderTag) this.loadFromWriterDB("order_tag.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends OrderTagCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, OrderTagCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
