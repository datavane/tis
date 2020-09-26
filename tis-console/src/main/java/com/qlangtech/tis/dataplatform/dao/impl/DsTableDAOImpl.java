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
package com.qlangtech.tis.dataplatform.dao.impl;

import java.util.List;
import com.qlangtech.tis.dataplatform.dao.IDsTableDAO;
import com.qlangtech.tis.dataplatform.pojo.DsTable;
import com.qlangtech.tis.dataplatform.pojo.DsTableCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DsTableDAOImpl extends BasicDAO<DsTable, DsTableCriteria> implements IDsTableDAO {

    public DsTableDAOImpl() {
        super();
    }

    public int countByExample(DsTableCriteria example) {
        Integer count = (Integer) this.count("ds_table.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(DsTableCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("ds_table.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(DsTableCriteria criteria) {
        return this.deleteRecords("ds_table.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long tabId) {
        DsTable key = new DsTable();
        key.setTabId(tabId);
        return this.deleteRecords("ds_table.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(DsTable record) {
        Object newKey = this.insert("ds_table.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(DsTable record) {
        Object newKey = this.insert("ds_table.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    @SuppressWarnings("unchecked")
    public List<DsTable> selectByExampleWithBLOBs(DsTableCriteria example) {
        List<DsTable> list = this.list("ds_table.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<DsTable> selectByExampleWithoutBLOBs(DsTableCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<DsTable> selectByExampleWithoutBLOBs(DsTableCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<DsTable> list = this.list("ds_table.ibatorgenerated_selectByExample", example);
        return list;
    }

    public DsTable selectByPrimaryKey(Long tabId) {
        DsTable key = new DsTable();
        key.setTabId(tabId);
        DsTable record = (DsTable) this.load("ds_table.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(DsTable record, DsTableCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("ds_table.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(DsTable record, DsTableCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("ds_table.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(DsTable record, DsTableCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("ds_table.ibatorgenerated_updateByExample", parms);
    }

    public DsTable loadFromWriteDB(Long tabId) {
        DsTable key = new DsTable();
        key.setTabId(tabId);
        DsTable record = (DsTable) this.loadFromWriterDB("ds_table.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends DsTableCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, DsTableCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }

    @Override
    public String getEntityName() {
        return "ds_table";
    }
}
