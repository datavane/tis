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
import com.qlangtech.tis.realtime.test.order.dao.ISpecialfeeDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Specialfee;
import com.qlangtech.tis.realtime.test.order.pojo.SpecialfeeCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class SpecialfeeDAOImpl extends BasicDAO<Specialfee, SpecialfeeCriteria> implements ISpecialfeeDAO {

    public SpecialfeeDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "specialfee";
    }

    public int countByExample(SpecialfeeCriteria example) {
        Integer count = (Integer) this.count("specialfee.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(SpecialfeeCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("specialfee.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(SpecialfeeCriteria criteria) {
        return this.deleteRecords("specialfee.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String specialfeeId) {
        Specialfee key = new Specialfee();
        key.setSpecialfeeId(specialfeeId);
        return this.deleteRecords("specialfee.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Specialfee record) {
        this.insert("specialfee.ibatorgenerated_insert", record);
    }

    public void insertSelective(Specialfee record) {
        this.insert("specialfee.ibatorgenerated_insertSelective", record);
    }

    public List<Specialfee> selectByExample(SpecialfeeCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(SpecialfeeCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.SpecialfeeCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("specialfee.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Specialfee> selectByExample(SpecialfeeCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Specialfee> list = this.list("specialfee.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Specialfee selectByPrimaryKey(String specialfeeId) {
        Specialfee key = new Specialfee();
        key.setSpecialfeeId(specialfeeId);
        Specialfee record = (Specialfee) this.load("specialfee.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Specialfee record, SpecialfeeCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("specialfee.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Specialfee record, SpecialfeeCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("specialfee.ibatorgenerated_updateByExample", parms);
    }

    public Specialfee loadFromWriteDB(String specialfeeId) {
        Specialfee key = new Specialfee();
        key.setSpecialfeeId(specialfeeId);
        Specialfee record = (Specialfee) this.loadFromWriterDB("specialfee.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends SpecialfeeCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, SpecialfeeCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
