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

import com.qlangtech.tis.dataplatform.dao.INobelAppDAO;
import com.qlangtech.tis.dataplatform.pojo.NobelApp;
import com.qlangtech.tis.dataplatform.pojo.NobelAppCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NobelAppDAOImpl extends BasicDAO<NobelApp, NobelAppCriteria> implements INobelAppDAO {

    @Override
    public String getEntityName() {
        return "nobel_app";
    }

    public NobelAppDAOImpl() {
        super();
    }

    public int countByExample(NobelAppCriteria example) {
        Integer count = (Integer) this.count("nobel_app.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(NobelAppCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("nobel_app.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(NobelAppCriteria criteria) {
        return this.deleteRecords("nobel_app.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        NobelApp key = new NobelApp();
        key.setId(id);
        return this.deleteRecords("nobel_app.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(NobelApp record) {
        Object newKey = this.insert("nobel_app.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(NobelApp record) {
        Object newKey = this.insert("nobel_app.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<NobelApp> selectByExample(NobelAppCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<NobelApp> selectByExample(NobelAppCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<NobelApp> list = this.list("nobel_app.ibatorgenerated_selectByExample", example);
        return list;
    }

    public NobelApp selectByPrimaryKey(Long id) {
        NobelApp key = new NobelApp();
        key.setId(id);
        NobelApp record = (NobelApp) this.load("nobel_app.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(NobelApp record, NobelAppCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("nobel_app.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(NobelApp record, NobelAppCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("nobel_app.ibatorgenerated_updateByExample", parms);
    }

    public NobelApp loadFromWriteDB(Long id) {
        NobelApp key = new NobelApp();
        key.setId(id);
        NobelApp record = (NobelApp) this.loadFromWriterDB("nobel_app.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends NobelAppCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, NobelAppCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
