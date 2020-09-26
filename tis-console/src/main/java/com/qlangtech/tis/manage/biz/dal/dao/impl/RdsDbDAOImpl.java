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
import com.qlangtech.tis.manage.biz.dal.dao.IRdsDbDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsDb;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsDbCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class RdsDbDAOImpl extends BasicDAO<RdsDb, RdsDbCriteria> implements IRdsDbDAO {

    public RdsDbDAOImpl() {
        super();
    }

    public int countByExample(RdsDbCriteria example) {
        Integer count = (Integer) this.count("rds_db.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(RdsDbCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("rds_db.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(RdsDbCriteria criteria) {
        return this.deleteRecords("rds_db.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        RdsDb key = new RdsDb();
        key.setId(id);
        return this.deleteRecords("rds_db.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(RdsDb record) {
        Object newKey = this.insert("rds_db.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(RdsDb record) {
        Object newKey = this.insert("rds_db.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<RdsDb> selectByExample(RdsDbCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<RdsDb> selectByExample(RdsDbCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<RdsDb> list = this.list("rds_db.ibatorgenerated_selectByExample", example);
        return list;
    }

    public RdsDb selectByPrimaryKey(Long id) {
        RdsDb key = new RdsDb();
        key.setId(id);
        RdsDb record = (RdsDb) this.load("rds_db.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(RdsDb record, RdsDbCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("rds_db.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(RdsDb record, RdsDbCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("rds_db.ibatorgenerated_updateByExample", parms);
    }

    public RdsDb loadFromWriteDB(Long id) {
        RdsDb key = new RdsDb();
        key.setId(id);
        RdsDb record = (RdsDb) this.loadFromWriterDB("rds_db.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends RdsDbCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, RdsDbCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }

    /* (non-Javadoc)
	 * @see com.taobao.terminator.manage.common.OperationLogger#getEntityName()
	 */
    @Override
    public String getEntityName() {
        return "rds_db";
    }
}
