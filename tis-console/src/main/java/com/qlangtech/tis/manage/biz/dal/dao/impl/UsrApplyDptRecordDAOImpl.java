/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrApplyDptRecordDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrApplyDptRecord;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrApplyDptRecordCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UsrApplyDptRecordDAOImpl extends BasicDAO<UsrApplyDptRecord, UsrApplyDptRecordCriteria> implements IUsrApplyDptRecordDAO {

    public UsrApplyDptRecordDAOImpl() {
        super();
    }

    public int countByExample(UsrApplyDptRecordCriteria example) {
        Integer count = (Integer) this.count("usr_apply_dpt_record.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(UsrApplyDptRecordCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("usr_apply_dpt_record.ibatorgenerated_countByExample", example);
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
        UsrApplyDptRecord record = (UsrApplyDptRecord) this.load("usr_apply_dpt_record.ibatorgenerated_selectByPrimaryKey", key);
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
        UsrApplyDptRecord record = (UsrApplyDptRecord) this.loadFromWriterDB("usr_apply_dpt_record.ibatorgenerated_selectByPrimaryKey", key);
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

 
    @Override
    public String getEntityName() {
        // TODO Auto-generated method stub
        return "usr_apply_dpt_record";
    }
}
