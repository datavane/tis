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
import com.qlangtech.tis.realtime.test.order.dao.IPaydetailDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Paydetail;
import com.qlangtech.tis.realtime.test.order.pojo.PaydetailCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PaydetailDAOImpl extends BasicDAO<Paydetail, PaydetailCriteria> implements IPaydetailDAO {

    public PaydetailDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "paydetail";
    }

    public int countByExample(PaydetailCriteria example) {
        Integer count = (Integer) this.count("paydetail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(PaydetailCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("paydetail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(PaydetailCriteria criteria) {
        return this.deleteRecords("paydetail.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String paydetailId) {
        Paydetail key = new Paydetail();
        key.setPaydetailId(paydetailId);
        return this.deleteRecords("paydetail.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Paydetail record) {
        this.insert("paydetail.ibatorgenerated_insert", record);
    }

    public void insertSelective(Paydetail record) {
        this.insert("paydetail.ibatorgenerated_insertSelective", record);
    }

    public List<Paydetail> selectByExample(PaydetailCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(PaydetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.PaydetailCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("paydetail.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Paydetail> selectByExample(PaydetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Paydetail> list = this.list("paydetail.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Paydetail selectByPrimaryKey(String paydetailId) {
        Paydetail key = new Paydetail();
        key.setPaydetailId(paydetailId);
        Paydetail record = (Paydetail) this.load("paydetail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Paydetail record, PaydetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("paydetail.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Paydetail record, PaydetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("paydetail.ibatorgenerated_updateByExample", parms);
    }

    public Paydetail loadFromWriteDB(String paydetailId) {
        Paydetail key = new Paydetail();
        key.setPaydetailId(paydetailId);
        Paydetail record = (Paydetail) this.loadFromWriterDB("paydetail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends PaydetailCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, PaydetailCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
