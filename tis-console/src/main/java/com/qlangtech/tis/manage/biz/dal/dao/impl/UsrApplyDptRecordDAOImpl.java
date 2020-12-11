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
import com.qlangtech.tis.manage.biz.dal.dao.IUsrApplyDptRecordDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrApplyDptRecord;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrApplyDptRecordCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class UsrApplyDptRecordDAOImpl extends BasicDAO<UsrApplyDptRecord, UsrApplyDptRecordCriteria> implements IUsrApplyDptRecordDAO {

    public UsrApplyDptRecordDAOImpl() {
        super();
    }

    public int countByExample(UsrApplyDptRecordCriteria example) {
        Integer count = this.count("usr_apply_dpt_record.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(UsrApplyDptRecordCriteria example) {
        Integer count = this.countFromWriterDB("usr_apply_dpt_record.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(UsrApplyDptRecordCriteria criteria) {
        return this.deleteRecords("usr_apply_dpt_record.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        UsrApplyDptRecord key = new UsrApplyDptRecord();
        key.setId(id);
        return this.deleteRecords("usr_apply_dpt_record.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(UsrApplyDptRecord record) {
        Object newKey = this.insert("usr_apply_dpt_record.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(UsrApplyDptRecord record) {
        Object newKey = this.insert("usr_apply_dpt_record.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<UsrApplyDptRecord> selectByExample(UsrApplyDptRecordCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<UsrApplyDptRecord> selectByExample(UsrApplyDptRecordCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<UsrApplyDptRecord> list = this.list("usr_apply_dpt_record.ibatorgenerated_selectByExample", example);
        return list;
    }

    public UsrApplyDptRecord selectByPrimaryKey(Long id) {
        UsrApplyDptRecord key = new UsrApplyDptRecord();
        key.setId(id);
        UsrApplyDptRecord record = this.load("usr_apply_dpt_record.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(UsrApplyDptRecord record, UsrApplyDptRecordCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("usr_apply_dpt_record.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(UsrApplyDptRecord record, UsrApplyDptRecordCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("usr_apply_dpt_record.ibatorgenerated_updateByExample", parms);
    }

    public UsrApplyDptRecord loadFromWriteDB(Long id) {
        UsrApplyDptRecord key = new UsrApplyDptRecord();
        key.setId(id);
        UsrApplyDptRecord record = this.loadFromWriterDB("usr_apply_dpt_record.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends UsrApplyDptRecordCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, UsrApplyDptRecordCriteria example) {
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
        // TODO Auto-generated method stub
        return "usr_apply_dpt_record";
    }
}
