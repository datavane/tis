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
import com.qlangtech.tis.realtime.test.order.dao.IWaitingordercridDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Waitingordercrid;
import com.qlangtech.tis.realtime.test.order.pojo.WaitingordercridCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WaitingordercridDAOImpl extends BasicDAO<Waitingordercrid, WaitingordercridCriteria> implements IWaitingordercridDAO {

    public WaitingordercridDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "waitingordercrid";
    }

    public int countByExample(WaitingordercridCriteria example) {
        Integer count = (Integer) this.count("waitingordercrid.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(WaitingordercridCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("waitingordercrid.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(WaitingordercridCriteria criteria) {
        return this.deleteRecords("waitingordercrid.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String waitingorderId) {
        Waitingordercrid key = new Waitingordercrid();
        key.setWaitingorderId(waitingorderId);
        return this.deleteRecords("waitingordercrid.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Waitingordercrid record) {
        this.insert("waitingordercrid.ibatorgenerated_insert", record);
    }

    public void insertSelective(Waitingordercrid record) {
        this.insert("waitingordercrid.ibatorgenerated_insertSelective", record);
    }

    public List<Waitingordercrid> selectByExample(WaitingordercridCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(WaitingordercridCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.WaitingordercridCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("waitingordercrid.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Waitingordercrid> selectByExample(WaitingordercridCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Waitingordercrid> list = this.list("waitingordercrid.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Waitingordercrid selectByPrimaryKey(String waitingorderId) {
        Waitingordercrid key = new Waitingordercrid();
        key.setWaitingorderId(waitingorderId);
        Waitingordercrid record = (Waitingordercrid) this.load("waitingordercrid.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Waitingordercrid record, WaitingordercridCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("waitingordercrid.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Waitingordercrid record, WaitingordercridCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("waitingordercrid.ibatorgenerated_updateByExample", parms);
    }

    public Waitingordercrid loadFromWriteDB(String waitingorderId) {
        Waitingordercrid key = new Waitingordercrid();
        key.setWaitingorderId(waitingorderId);
        Waitingordercrid record = (Waitingordercrid) this.loadFromWriterDB("waitingordercrid.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends WaitingordercridCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, WaitingordercridCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
