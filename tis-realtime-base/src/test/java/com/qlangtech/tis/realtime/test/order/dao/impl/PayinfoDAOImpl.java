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
import com.qlangtech.tis.realtime.test.order.dao.IPayinfoDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Payinfo;
import com.qlangtech.tis.realtime.test.order.pojo.PayinfoCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PayinfoDAOImpl extends BasicDAO<Payinfo, PayinfoCriteria> implements IPayinfoDAO {

    public PayinfoDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "payinfo";
    }

    public int countByExample(PayinfoCriteria example) {
        Integer count = (Integer) this.count("payinfo.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(PayinfoCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("payinfo.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(PayinfoCriteria criteria) {
        return this.deleteRecords("payinfo.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String payId) {
        Payinfo key = new Payinfo();
        key.setPayId(payId);
        return this.deleteRecords("payinfo.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Payinfo record) {
        this.insert("payinfo.ibatorgenerated_insert", record);
    }

    public void insertSelective(Payinfo record) {
        this.insert("payinfo.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<Payinfo> selectByExampleWithBLOBs(PayinfoCriteria example) {
        List<Payinfo> list = this.list("payinfo.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<Payinfo> selectByExampleWithoutBLOBs(PayinfoCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(PayinfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.PayinfoCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("payinfo.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Payinfo> selectByExampleWithoutBLOBs(PayinfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Payinfo> list = this.list("payinfo.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Payinfo selectByPrimaryKey(String payId) {
        Payinfo key = new Payinfo();
        key.setPayId(payId);
        Payinfo record = (Payinfo) this.load("payinfo.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Payinfo record, PayinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("payinfo.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(Payinfo record, PayinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("payinfo.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(Payinfo record, PayinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("payinfo.ibatorgenerated_updateByExample", parms);
    }

    public Payinfo loadFromWriteDB(String payId) {
        Payinfo key = new Payinfo();
        key.setPayId(payId);
        Payinfo record = (Payinfo) this.loadFromWriterDB("payinfo.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends PayinfoCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, PayinfoCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
