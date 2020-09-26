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
import com.qlangtech.tis.realtime.test.order.dao.IGlobalcodeorderDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Globalcodeorder;
import com.qlangtech.tis.realtime.test.order.pojo.GlobalcodeorderCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class GlobalcodeorderDAOImpl extends BasicDAO<Globalcodeorder, GlobalcodeorderCriteria> implements IGlobalcodeorderDAO {

    public GlobalcodeorderDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "globalcodeorder";
    }

    public int countByExample(GlobalcodeorderCriteria example) {
        Integer count = (Integer) this.count("globalcodeorder.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(GlobalcodeorderCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("globalcodeorder.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(GlobalcodeorderCriteria criteria) {
        return this.deleteRecords("globalcodeorder.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String globalCode) {
        Globalcodeorder key = new Globalcodeorder();
        key.setGlobalCode(globalCode);
        return this.deleteRecords("globalcodeorder.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Globalcodeorder record) {
        this.insert("globalcodeorder.ibatorgenerated_insert", record);
    }

    public void insertSelective(Globalcodeorder record) {
        this.insert("globalcodeorder.ibatorgenerated_insertSelective", record);
    }

    public List<Globalcodeorder> selectByExample(GlobalcodeorderCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(GlobalcodeorderCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.GlobalcodeorderCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("globalcodeorder.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Globalcodeorder> selectByExample(GlobalcodeorderCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Globalcodeorder> list = this.list("globalcodeorder.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Globalcodeorder selectByPrimaryKey(String globalCode) {
        Globalcodeorder key = new Globalcodeorder();
        key.setGlobalCode(globalCode);
        Globalcodeorder record = (Globalcodeorder) this.load("globalcodeorder.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Globalcodeorder record, GlobalcodeorderCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("globalcodeorder.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Globalcodeorder record, GlobalcodeorderCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("globalcodeorder.ibatorgenerated_updateByExample", parms);
    }

    public Globalcodeorder loadFromWriteDB(String globalCode) {
        Globalcodeorder key = new Globalcodeorder();
        key.setGlobalCode(globalCode);
        Globalcodeorder record = (Globalcodeorder) this.loadFromWriterDB("globalcodeorder.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends GlobalcodeorderCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, GlobalcodeorderCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
