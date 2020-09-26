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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRdsTableDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsTable;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsTableCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class RdsTableDAOImpl extends BasicDAO<RdsTable, RdsTableCriteria> implements IRdsTableDAO {

    public RdsTableDAOImpl() {
        super();
    }

    public int countByExample(RdsTableCriteria example) {
        Integer count = (Integer) this.count("rds_table.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(RdsTableCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("rds_table.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(RdsTableCriteria criteria) {
        return this.deleteRecords("rds_table.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        RdsTable key = new RdsTable();
        key.setId(id);
        return this.deleteRecords("rds_table.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(RdsTable record) {
        Object newKey = this.insert("rds_table.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(RdsTable record) {
        Object newKey = this.insert("rds_table.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<RdsTable> selectByExample(RdsTableCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<RdsTable> selectByExample(RdsTableCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<RdsTable> list = this.list("rds_table.ibatorgenerated_selectByExample", example);
        return list;
    }

    public RdsTable selectByPrimaryKey(Long id) {
        RdsTable key = new RdsTable();
        key.setId(id);
        RdsTable record = (RdsTable) this.load("rds_table.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(RdsTable record, RdsTableCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("rds_table.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(RdsTable record, RdsTableCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("rds_table.ibatorgenerated_updateByExample", parms);
    }

    public RdsTable loadFromWriteDB(Long id) {
        RdsTable key = new RdsTable();
        key.setId(id);
        RdsTable record = (RdsTable) this.loadFromWriterDB("rds_table.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends RdsTableCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, RdsTableCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }

    @Override
    public String getEntityName() {
        return "rds_table";
    }
}
