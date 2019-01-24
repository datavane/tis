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
import com.qlangtech.tis.manage.biz.dal.dao.IRdsDbDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsDb;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsDbCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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

  
    @Override
    public String getEntityName() {
        return "rds_db";
    }
}
