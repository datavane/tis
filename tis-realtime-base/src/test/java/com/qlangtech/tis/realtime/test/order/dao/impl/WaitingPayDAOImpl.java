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
import com.qlangtech.tis.realtime.test.order.dao.IWaitingPayDAO;
import com.qlangtech.tis.realtime.test.order.pojo.WaitingPay;
import com.qlangtech.tis.realtime.test.order.pojo.WaitingPayCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WaitingPayDAOImpl extends BasicDAO<WaitingPay, WaitingPayCriteria> implements IWaitingPayDAO {

    public WaitingPayDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "waiting_pay";
    }

    public int countByExample(WaitingPayCriteria example) {
        Integer count = (Integer) this.count("waiting_pay.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(WaitingPayCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("waiting_pay.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(WaitingPayCriteria criteria) {
        return this.deleteRecords("waiting_pay.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        WaitingPay key = new WaitingPay();
        key.setId(id);
        return this.deleteRecords("waiting_pay.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(WaitingPay record) {
        this.insert("waiting_pay.ibatorgenerated_insert", record);
    }

    public void insertSelective(WaitingPay record) {
        this.insert("waiting_pay.ibatorgenerated_insertSelective", record);
    }

    public List<WaitingPay> selectByExample(WaitingPayCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(WaitingPayCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.WaitingPayCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("waiting_pay.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<WaitingPay> selectByExample(WaitingPayCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<WaitingPay> list = this.list("waiting_pay.ibatorgenerated_selectByExample", example);
        return list;
    }

    public WaitingPay selectByPrimaryKey(String id) {
        WaitingPay key = new WaitingPay();
        key.setId(id);
        WaitingPay record = (WaitingPay) this.load("waiting_pay.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(WaitingPay record, WaitingPayCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("waiting_pay.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(WaitingPay record, WaitingPayCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("waiting_pay.ibatorgenerated_updateByExample", parms);
    }

    public WaitingPay loadFromWriteDB(String id) {
        WaitingPay key = new WaitingPay();
        key.setId(id);
        WaitingPay record = (WaitingPay) this.loadFromWriterDB("waiting_pay.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends WaitingPayCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, WaitingPayCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
