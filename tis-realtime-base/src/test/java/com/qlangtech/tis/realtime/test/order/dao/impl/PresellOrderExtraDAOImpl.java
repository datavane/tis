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
import com.qlangtech.tis.realtime.test.order.dao.IPresellOrderExtraDAO;
import com.qlangtech.tis.realtime.test.order.pojo.PresellOrderExtra;
import com.qlangtech.tis.realtime.test.order.pojo.PresellOrderExtraCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PresellOrderExtraDAOImpl extends BasicDAO<PresellOrderExtra, PresellOrderExtraCriteria> implements IPresellOrderExtraDAO {

    public PresellOrderExtraDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "presell_order_extra";
    }

    public int countByExample(PresellOrderExtraCriteria example) {
        Integer count = (Integer) this.count("presell_order_extra.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(PresellOrderExtraCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("presell_order_extra.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(PresellOrderExtraCriteria criteria) {
        return this.deleteRecords("presell_order_extra.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String orderId) {
        PresellOrderExtra key = new PresellOrderExtra();
        key.setOrderId(orderId);
        return this.deleteRecords("presell_order_extra.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(PresellOrderExtra record) {
        this.insert("presell_order_extra.ibatorgenerated_insert", record);
    }

    public void insertSelective(PresellOrderExtra record) {
        this.insert("presell_order_extra.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<PresellOrderExtra> selectByExampleWithBLOBs(PresellOrderExtraCriteria example) {
        List<PresellOrderExtra> list = this.list("presell_order_extra.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<PresellOrderExtra> selectByExampleWithoutBLOBs(PresellOrderExtraCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(PresellOrderExtraCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.PresellOrderExtraCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("presell_order_extra.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<PresellOrderExtra> selectByExampleWithoutBLOBs(PresellOrderExtraCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<PresellOrderExtra> list = this.list("presell_order_extra.ibatorgenerated_selectByExample", example);
        return list;
    }

    public PresellOrderExtra selectByPrimaryKey(String orderId) {
        PresellOrderExtra key = new PresellOrderExtra();
        key.setOrderId(orderId);
        PresellOrderExtra record = (PresellOrderExtra) this.load("presell_order_extra.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(PresellOrderExtra record, PresellOrderExtraCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("presell_order_extra.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(PresellOrderExtra record, PresellOrderExtraCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("presell_order_extra.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(PresellOrderExtra record, PresellOrderExtraCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("presell_order_extra.ibatorgenerated_updateByExample", parms);
    }

    public PresellOrderExtra loadFromWriteDB(String orderId) {
        PresellOrderExtra key = new PresellOrderExtra();
        key.setOrderId(orderId);
        PresellOrderExtra record = (PresellOrderExtra) this.loadFromWriterDB("presell_order_extra.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends PresellOrderExtraCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, PresellOrderExtraCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
