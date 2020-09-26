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
import com.qlangtech.tis.realtime.test.order.dao.ITakeoutOrderExtraDAO;
import com.qlangtech.tis.realtime.test.order.pojo.TakeoutOrderExtra;
import com.qlangtech.tis.realtime.test.order.pojo.TakeoutOrderExtraCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TakeoutOrderExtraDAOImpl extends BasicDAO<TakeoutOrderExtra, TakeoutOrderExtraCriteria> implements ITakeoutOrderExtraDAO {

    public TakeoutOrderExtraDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "takeout_order_extra";
    }

    public int countByExample(TakeoutOrderExtraCriteria example) {
        Integer count = (Integer) this.count("takeout_order_extra.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(TakeoutOrderExtraCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("takeout_order_extra.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(TakeoutOrderExtraCriteria criteria) {
        return this.deleteRecords("takeout_order_extra.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String orderId) {
        TakeoutOrderExtra key = new TakeoutOrderExtra();
        key.setOrderId(orderId);
        return this.deleteRecords("takeout_order_extra.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(TakeoutOrderExtra record) {
        this.insert("takeout_order_extra.ibatorgenerated_insert", record);
    }

    public void insertSelective(TakeoutOrderExtra record) {
        this.insert("takeout_order_extra.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<TakeoutOrderExtra> selectByExampleWithBLOBs(TakeoutOrderExtraCriteria example) {
        List<TakeoutOrderExtra> list = this.list("takeout_order_extra.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<TakeoutOrderExtra> selectByExampleWithoutBLOBs(TakeoutOrderExtraCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(TakeoutOrderExtraCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.TakeoutOrderExtraCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("takeout_order_extra.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<TakeoutOrderExtra> selectByExampleWithoutBLOBs(TakeoutOrderExtraCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<TakeoutOrderExtra> list = this.list("takeout_order_extra.ibatorgenerated_selectByExample", example);
        return list;
    }

    public TakeoutOrderExtra selectByPrimaryKey(String orderId) {
        TakeoutOrderExtra key = new TakeoutOrderExtra();
        key.setOrderId(orderId);
        TakeoutOrderExtra record = (TakeoutOrderExtra) this.load("takeout_order_extra.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(TakeoutOrderExtra record, TakeoutOrderExtraCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("takeout_order_extra.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(TakeoutOrderExtra record, TakeoutOrderExtraCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("takeout_order_extra.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(TakeoutOrderExtra record, TakeoutOrderExtraCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("takeout_order_extra.ibatorgenerated_updateByExample", parms);
    }

    public TakeoutOrderExtra loadFromWriteDB(String orderId) {
        TakeoutOrderExtra key = new TakeoutOrderExtra();
        key.setOrderId(orderId);
        TakeoutOrderExtra record = (TakeoutOrderExtra) this.loadFromWriterDB("takeout_order_extra.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends TakeoutOrderExtraCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, TakeoutOrderExtraCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
