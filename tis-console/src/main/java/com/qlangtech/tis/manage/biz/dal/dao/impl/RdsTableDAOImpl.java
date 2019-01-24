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
import com.qlangtech.tis.manage.biz.dal.dao.IRdsTableDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsTable;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsTableCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
