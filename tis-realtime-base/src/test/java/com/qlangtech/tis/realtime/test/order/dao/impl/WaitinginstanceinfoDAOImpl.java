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
import com.qlangtech.tis.realtime.test.order.dao.IWaitinginstanceinfoDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Waitinginstanceinfo;
import com.qlangtech.tis.realtime.test.order.pojo.WaitinginstanceinfoCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WaitinginstanceinfoDAOImpl extends BasicDAO<Waitinginstanceinfo, WaitinginstanceinfoCriteria> implements IWaitinginstanceinfoDAO {

    public WaitinginstanceinfoDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "waitinginstanceinfo";
    }

    public int countByExample(WaitinginstanceinfoCriteria example) {
        Integer count = (Integer) this.count("waitinginstanceinfo.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(WaitinginstanceinfoCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("waitinginstanceinfo.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(WaitinginstanceinfoCriteria criteria) {
        return this.deleteRecords("waitinginstanceinfo.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String waitinginstanceId) {
        Waitinginstanceinfo key = new Waitinginstanceinfo();
        key.setWaitinginstanceId(waitinginstanceId);
        return this.deleteRecords("waitinginstanceinfo.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Waitinginstanceinfo record) {
        this.insert("waitinginstanceinfo.ibatorgenerated_insert", record);
    }

    public void insertSelective(Waitinginstanceinfo record) {
        this.insert("waitinginstanceinfo.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<Waitinginstanceinfo> selectByExampleWithBLOBs(WaitinginstanceinfoCriteria example) {
        List<Waitinginstanceinfo> list = this.list("waitinginstanceinfo.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<Waitinginstanceinfo> selectByExampleWithoutBLOBs(WaitinginstanceinfoCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(WaitinginstanceinfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.WaitinginstanceinfoCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("waitinginstanceinfo.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Waitinginstanceinfo> selectByExampleWithoutBLOBs(WaitinginstanceinfoCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Waitinginstanceinfo> list = this.list("waitinginstanceinfo.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Waitinginstanceinfo selectByPrimaryKey(String waitinginstanceId) {
        Waitinginstanceinfo key = new Waitinginstanceinfo();
        key.setWaitinginstanceId(waitinginstanceId);
        Waitinginstanceinfo record = (Waitinginstanceinfo) this.load("waitinginstanceinfo.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Waitinginstanceinfo record, WaitinginstanceinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("waitinginstanceinfo.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(Waitinginstanceinfo record, WaitinginstanceinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("waitinginstanceinfo.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(Waitinginstanceinfo record, WaitinginstanceinfoCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("waitinginstanceinfo.ibatorgenerated_updateByExample", parms);
    }

    public Waitinginstanceinfo loadFromWriteDB(String waitinginstanceId) {
        Waitinginstanceinfo key = new Waitinginstanceinfo();
        key.setWaitinginstanceId(waitinginstanceId);
        Waitinginstanceinfo record = (Waitinginstanceinfo) this.loadFromWriterDB("waitinginstanceinfo.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends WaitinginstanceinfoCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, WaitinginstanceinfoCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
