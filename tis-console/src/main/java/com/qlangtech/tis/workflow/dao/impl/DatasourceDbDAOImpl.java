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
package com.qlangtech.tis.workflow.dao.impl;

import com.qlangtech.tis.workflow.dao.IDatasourceDbDAO;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DatasourceDbDAOImpl extends BasicDAO<DatasourceDb, DatasourceDbCriteria> implements IDatasourceDbDAO {

    public DatasourceDbDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "datasource_db";
    }

    public int countByExample(DatasourceDbCriteria example) {
        Integer count = (Integer) this.count("datasource_db.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(DatasourceDbCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("datasource_db.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(DatasourceDbCriteria criteria) {
        return this.deleteRecords("datasource_db.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer id) {
        DatasourceDb key = new DatasourceDb();
        key.setId(id);
        return this.deleteRecords("datasource_db.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(DatasourceDb record) {
        Object newKey = this.insert("datasource_db.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(DatasourceDb record) {
        Object newKey = this.insert("datasource_db.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<DatasourceDb> selectByExample(DatasourceDbCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<DatasourceDb> selectByExample(DatasourceDbCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<DatasourceDb> list = this.list("datasource_db.ibatorgenerated_selectByExample", example);
        return list;
    }

    public List<DatasourceDb> minSelectByExample(DatasourceDbCriteria criteria) {
        return this.minSelectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<DatasourceDb> minSelectByExample(DatasourceDbCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<DatasourceDb> list = this.list("datasource_db.ibatorgenerated_minSelectByExample", example);
        return list;
    }

    public DatasourceDb selectByPrimaryKey(Integer id) {
        DatasourceDb key = new DatasourceDb();
        key.setId(id);
        DatasourceDb record = (DatasourceDb) this.load("datasource_db.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(DatasourceDb record, DatasourceDbCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("datasource_db.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(DatasourceDb record, DatasourceDbCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("datasource_db.ibatorgenerated_updateByExample", parms);
    }

    public DatasourceDb loadFromWriteDB(Integer id) {
        DatasourceDb key = new DatasourceDb();
        key.setId(id);
        DatasourceDb record = (DatasourceDb) this.loadFromWriterDB("datasource_db.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends DatasourceDbCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, DatasourceDbCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
