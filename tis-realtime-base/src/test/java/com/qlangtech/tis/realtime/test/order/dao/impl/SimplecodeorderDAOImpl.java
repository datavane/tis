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
import com.qlangtech.tis.realtime.test.order.dao.ISimplecodeorderDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Simplecodeorder;
import com.qlangtech.tis.realtime.test.order.pojo.SimplecodeorderCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class SimplecodeorderDAOImpl extends BasicDAO<Simplecodeorder, SimplecodeorderCriteria> implements ISimplecodeorderDAO {

    public SimplecodeorderDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "simplecodeorder";
    }

    public int countByExample(SimplecodeorderCriteria example) {
        Integer count = (Integer) this.count("simplecodeorder.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(SimplecodeorderCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("simplecodeorder.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(SimplecodeorderCriteria criteria) {
        return this.deleteRecords("simplecodeorder.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long simpleCode) {
        Simplecodeorder key = new Simplecodeorder();
        key.setSimpleCode(simpleCode);
        return this.deleteRecords("simplecodeorder.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Simplecodeorder record) {
        this.insert("simplecodeorder.ibatorgenerated_insert", record);
    }

    public void insertSelective(Simplecodeorder record) {
        this.insert("simplecodeorder.ibatorgenerated_insertSelective", record);
    }

    public List<Simplecodeorder> selectByExample(SimplecodeorderCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(SimplecodeorderCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.SimplecodeorderCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("simplecodeorder.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Simplecodeorder> selectByExample(SimplecodeorderCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Simplecodeorder> list = this.list("simplecodeorder.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Simplecodeorder selectByPrimaryKey(Long simpleCode) {
        Simplecodeorder key = new Simplecodeorder();
        key.setSimpleCode(simpleCode);
        Simplecodeorder record = (Simplecodeorder) this.load("simplecodeorder.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Simplecodeorder record, SimplecodeorderCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("simplecodeorder.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Simplecodeorder record, SimplecodeorderCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("simplecodeorder.ibatorgenerated_updateByExample", parms);
    }

    public Simplecodeorder loadFromWriteDB(Long simpleCode) {
        Simplecodeorder key = new Simplecodeorder();
        key.setSimpleCode(simpleCode);
        Simplecodeorder record = (Simplecodeorder) this.loadFromWriterDB("simplecodeorder.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends SimplecodeorderCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, SimplecodeorderCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
