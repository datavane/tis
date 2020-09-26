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
import com.qlangtech.tis.realtime.test.order.dao.ITotalpayinfoDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Totalpayinfo;
import com.qlangtech.tis.realtime.test.order.pojo.TotalpayinfoCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TotalpayinfoDAOImpl extends BasicDAO<Totalpayinfo, TotalpayinfoCriteria> implements ITotalpayinfoDAO {

    public TotalpayinfoDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "totalpayinfo";
    }

    public int countByExample(TotalpayinfoCriteria example) {
        Integer count = (Integer) this.count("totalpayinfo.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(TotalpayinfoCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("totalpayinfo.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(TotalpayinfoCriteria criteria) {
        return this.deleteRecords("totalpayinfo.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String totalpayId) {
        Totalpayinfo key = new Totalpayinfo();
        key.setTotalpayId(totalpayId);
        return this.deleteRecords("totalpayinfo.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Totalpayinfo record) {
        this.insert("totalpayinfo.ibatorgenerated_insert", record);
    }

    public void insertSelective(Totalpayinfo record) {
        this.insert("totalpayinfo.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<Totalpayinfo> selectByExampleWithBLOBs(TotalpayinfoCriteria example) {
        List<Totalpayinfo> list = this.list("totalpayinfo.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<Totalpayinfo> selectByExampleWithoutBLOBs(TotalpayinfoCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(TotalpayinfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.TotalpayinfoCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("totalpayinfo.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Totalpayinfo> selectByExampleWithoutBLOBs(TotalpayinfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Totalpayinfo> list = this.list("totalpayinfo.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Totalpayinfo selectByPrimaryKey(String totalpayId) {
        Totalpayinfo key = new Totalpayinfo();
        key.setTotalpayId(totalpayId);
        Totalpayinfo record = (Totalpayinfo) this.load("totalpayinfo.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Totalpayinfo record, TotalpayinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("totalpayinfo.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(Totalpayinfo record, TotalpayinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("totalpayinfo.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(Totalpayinfo record, TotalpayinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("totalpayinfo.ibatorgenerated_updateByExample", parms);
    }

    public Totalpayinfo loadFromWriteDB(String totalpayId) {
        Totalpayinfo key = new Totalpayinfo();
        key.setTotalpayId(totalpayId);
        Totalpayinfo record = (Totalpayinfo) this.loadFromWriterDB("totalpayinfo.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends TotalpayinfoCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, TotalpayinfoCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
