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
package com.qlangtech.tis.dataplatform.dao.impl;

import java.util.List;
import com.qlangtech.tis.dataplatform.dao.IDsDatasourceDAO;
import com.qlangtech.tis.dataplatform.pojo.DsDatasource;
import com.qlangtech.tis.dataplatform.pojo.DsDatasourceCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DsDatasourceDAOImpl extends BasicDAO<DsDatasource, DsDatasourceCriteria> implements IDsDatasourceDAO {

    public DsDatasourceDAOImpl() {
        super();
    }

    public int countByExample(DsDatasourceCriteria example) {
        Integer count = (Integer) this.count("ds_datasource.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(DsDatasourceCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("ds_datasource.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(DsDatasourceCriteria criteria) {
        return this.deleteRecords("ds_datasource.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer dsId) {
        DsDatasource key = new DsDatasource();
        key.setDsId(dsId);
        return this.deleteRecords("ds_datasource.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(DsDatasource record) {
        Object newKey = this.insert("ds_datasource.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(DsDatasource record) {
        Object newKey = this.insert("ds_datasource.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<DsDatasource> selectByExample(DsDatasourceCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<DsDatasource> selectByExample(DsDatasourceCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<DsDatasource> list = this.list("ds_datasource.ibatorgenerated_selectByExample", example);
        return list;
    }

    public DsDatasource selectByPrimaryKey(Integer dsId) {
        DsDatasource key = new DsDatasource();
        key.setDsId(dsId);
        DsDatasource record = (DsDatasource) this.load("ds_datasource.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(DsDatasource record, DsDatasourceCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("ds_datasource.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(DsDatasource record, DsDatasourceCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("ds_datasource.ibatorgenerated_updateByExample", parms);
    }

    public DsDatasource loadFromWriteDB(Integer dsId) {
        DsDatasource key = new DsDatasource();
        key.setDsId(dsId);
        DsDatasource record = (DsDatasource) this.loadFromWriterDB("ds_datasource.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends DsDatasourceCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, DsDatasourceCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }

    @Override
    public String getEntityName() {
        return "DsDatasource";
    }
}
