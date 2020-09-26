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
import com.qlangtech.tis.realtime.test.order.dao.IInstancedetailDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Instancedetail;
import com.qlangtech.tis.realtime.test.order.pojo.InstancedetailCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class InstancedetailDAOImpl extends BasicDAO<Instancedetail, InstancedetailCriteria> implements IInstancedetailDAO {

    public InstancedetailDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "instancedetail";
    }

    public int countByExample(InstancedetailCriteria example) {
        Integer count = (Integer) this.count("instancedetail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(InstancedetailCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("instancedetail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(InstancedetailCriteria criteria) {
        return this.deleteRecords("instancedetail.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String instanceId) {
        Instancedetail key = new Instancedetail();
        key.setInstanceId(instanceId);
        return this.deleteRecords("instancedetail.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Instancedetail record) {
        this.insert("instancedetail.ibatorgenerated_insert", record);
    }

    public void insertSelective(Instancedetail record) {
        this.insert("instancedetail.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<Instancedetail> selectByExampleWithBLOBs(InstancedetailCriteria example) {
        List<Instancedetail> list = this.list("instancedetail.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<Instancedetail> selectByExampleWithoutBLOBs(InstancedetailCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(InstancedetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.InstancedetailCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("instancedetail.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Instancedetail> selectByExampleWithoutBLOBs(InstancedetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Instancedetail> list = this.list("instancedetail.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Instancedetail selectByPrimaryKey(String instanceId) {
        Instancedetail key = new Instancedetail();
        key.setInstanceId(instanceId);
        Instancedetail record = (Instancedetail) this.load("instancedetail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Instancedetail record, InstancedetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("instancedetail.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(Instancedetail record, InstancedetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("instancedetail.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(Instancedetail record, InstancedetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("instancedetail.ibatorgenerated_updateByExample", parms);
    }

    public Instancedetail loadFromWriteDB(String instanceId) {
        Instancedetail key = new Instancedetail();
        key.setInstanceId(instanceId);
        Instancedetail record = (Instancedetail) this.loadFromWriterDB("instancedetail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends InstancedetailCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, InstancedetailCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
