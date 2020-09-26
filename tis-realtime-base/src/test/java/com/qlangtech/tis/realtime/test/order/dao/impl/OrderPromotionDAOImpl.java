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
import com.qlangtech.tis.realtime.test.order.dao.IOrderPromotionDAO;
import com.qlangtech.tis.realtime.test.order.pojo.OrderPromotion;
import com.qlangtech.tis.realtime.test.order.pojo.OrderPromotionCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class OrderPromotionDAOImpl extends BasicDAO<OrderPromotion, OrderPromotionCriteria> implements IOrderPromotionDAO {

    public OrderPromotionDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "order_promotion";
    }

    public int countByExample(OrderPromotionCriteria example) {
        Integer count = (Integer) this.count("order_promotion.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(OrderPromotionCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("order_promotion.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(OrderPromotionCriteria criteria) {
        return this.deleteRecords("order_promotion.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        OrderPromotion key = new OrderPromotion();
        key.setId(id);
        return this.deleteRecords("order_promotion.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(OrderPromotion record) {
        this.insert("order_promotion.ibatorgenerated_insert", record);
    }

    public void insertSelective(OrderPromotion record) {
        this.insert("order_promotion.ibatorgenerated_insertSelective", record);
    }

    public List<OrderPromotion> selectByExample(OrderPromotionCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(OrderPromotionCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.OrderPromotionCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("order_promotion.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<OrderPromotion> selectByExample(OrderPromotionCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<OrderPromotion> list = this.list("order_promotion.ibatorgenerated_selectByExample", example);
        return list;
    }

    public OrderPromotion selectByPrimaryKey(String id) {
        OrderPromotion key = new OrderPromotion();
        key.setId(id);
        OrderPromotion record = (OrderPromotion) this.load("order_promotion.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(OrderPromotion record, OrderPromotionCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_promotion.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(OrderPromotion record, OrderPromotionCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("order_promotion.ibatorgenerated_updateByExample", parms);
    }

    public OrderPromotion loadFromWriteDB(String id) {
        OrderPromotion key = new OrderPromotion();
        key.setId(id);
        OrderPromotion record = (OrderPromotion) this.loadFromWriterDB("order_promotion.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends OrderPromotionCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, OrderPromotionCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
