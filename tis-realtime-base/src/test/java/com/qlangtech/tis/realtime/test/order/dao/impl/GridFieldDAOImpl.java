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
import com.qlangtech.tis.realtime.test.order.dao.IGridFieldDAO;
import com.qlangtech.tis.realtime.test.order.pojo.GridField;
import com.qlangtech.tis.realtime.test.order.pojo.GridFieldCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class GridFieldDAOImpl extends BasicDAO<GridField, GridFieldCriteria> implements IGridFieldDAO {

    public GridFieldDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "grid_field";
    }

    public int countByExample(GridFieldCriteria example) {
        Integer count = (Integer) this.count("grid_field.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(GridFieldCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("grid_field.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(GridFieldCriteria criteria) {
        return this.deleteRecords("grid_field.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        GridField key = new GridField();
        key.setId(id);
        return this.deleteRecords("grid_field.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(GridField record) {
        this.insert("grid_field.ibatorgenerated_insert", record);
    }

    public void insertSelective(GridField record) {
        this.insert("grid_field.ibatorgenerated_insertSelective", record);
    }

    public List<GridField> selectByExample(GridFieldCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(GridFieldCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.GridFieldCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("grid_field.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<GridField> selectByExample(GridFieldCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<GridField> list = this.list("grid_field.ibatorgenerated_selectByExample", example);
        return list;
    }

    public GridField selectByPrimaryKey(Long id) {
        GridField key = new GridField();
        key.setId(id);
        GridField record = (GridField) this.load("grid_field.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(GridField record, GridFieldCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("grid_field.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(GridField record, GridFieldCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("grid_field.ibatorgenerated_updateByExample", parms);
    }

    public GridField loadFromWriteDB(Long id) {
        GridField key = new GridField();
        key.setId(id);
        GridField record = (GridField) this.loadFromWriterDB("grid_field.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends GridFieldCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, GridFieldCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
