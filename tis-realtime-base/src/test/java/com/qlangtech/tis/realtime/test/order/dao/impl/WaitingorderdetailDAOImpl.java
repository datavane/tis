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
import com.qlangtech.tis.realtime.test.order.dao.IWaitingorderdetailDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Waitingorderdetail;
import com.qlangtech.tis.realtime.test.order.pojo.WaitingorderdetailCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WaitingorderdetailDAOImpl extends BasicDAO<Waitingorderdetail, WaitingorderdetailCriteria> implements IWaitingorderdetailDAO {

    public WaitingorderdetailDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "waitingorderdetail";
    }

    public int countByExample(WaitingorderdetailCriteria example) {
        Integer count = (Integer) this.count("waitingorderdetail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(WaitingorderdetailCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("waitingorderdetail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(WaitingorderdetailCriteria criteria) {
        return this.deleteRecords("waitingorderdetail.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String waitingorderId) {
        Waitingorderdetail key = new Waitingorderdetail();
        key.setWaitingorderId(waitingorderId);
        return this.deleteRecords("waitingorderdetail.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Waitingorderdetail record) {
        this.insert("waitingorderdetail.ibatorgenerated_insert", record);
    }

    public void insertSelective(Waitingorderdetail record) {
        this.insert("waitingorderdetail.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<Waitingorderdetail> selectByExampleWithBLOBs(WaitingorderdetailCriteria example) {
        List<Waitingorderdetail> list = this.list("waitingorderdetail.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<Waitingorderdetail> selectByExampleWithoutBLOBs(WaitingorderdetailCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(WaitingorderdetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.WaitingorderdetailCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("waitingorderdetail.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Waitingorderdetail> selectByExampleWithoutBLOBs(WaitingorderdetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Waitingorderdetail> list = this.list("waitingorderdetail.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Waitingorderdetail selectByPrimaryKey(String waitingorderId) {
        Waitingorderdetail key = new Waitingorderdetail();
        key.setWaitingorderId(waitingorderId);
        Waitingorderdetail record = (Waitingorderdetail) this.load("waitingorderdetail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Waitingorderdetail record, WaitingorderdetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("waitingorderdetail.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(Waitingorderdetail record, WaitingorderdetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("waitingorderdetail.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(Waitingorderdetail record, WaitingorderdetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("waitingorderdetail.ibatorgenerated_updateByExample", parms);
    }

    public Waitingorderdetail loadFromWriteDB(String waitingorderId) {
        Waitingorderdetail key = new Waitingorderdetail();
        key.setWaitingorderId(waitingorderId);
        Waitingorderdetail record = (Waitingorderdetail) this.loadFromWriterDB("waitingorderdetail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends WaitingorderdetailCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, WaitingorderdetailCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
