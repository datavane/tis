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
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationExtendDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationExtend;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationExtendCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ApplicationExtendDAOImpl extends BasicDAO<ApplicationExtend, ApplicationExtendCriteria> implements IApplicationExtendDAO {

    public ApplicationExtendDAOImpl() {
        super();
    }

    public int countByExample(ApplicationExtendCriteria example) {
        Integer count = (Integer) this.count("application_extend.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ApplicationExtendCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("application_extend.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ApplicationExtendCriteria criteria) {
        return this.deleteRecords("application_extend.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        ApplicationExtend key = new ApplicationExtend();
        key.setId(id);
        return this.deleteRecords("application_extend.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(ApplicationExtend record) {
        Object newKey = this.insert("application_extend.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(ApplicationExtend record) {
        Object newKey = this.insert("application_extend.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<ApplicationExtend> selectByExample(ApplicationExtendCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<ApplicationExtend> selectByExample(ApplicationExtendCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<ApplicationExtend> list = this.list("application_extend.ibatorgenerated_selectByExample", example);
        return list;
    }

    public ApplicationExtend selectByPrimaryKey(Long id) {
        ApplicationExtend key = new ApplicationExtend();
        key.setId(id);
        ApplicationExtend record = (ApplicationExtend) this.load("application_extend.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(ApplicationExtend record, ApplicationExtendCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("application_extend.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(ApplicationExtend record, ApplicationExtendCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("application_extend.ibatorgenerated_updateByExample", parms);
    }

    public ApplicationExtend loadFromWriteDB(Long id) {
        ApplicationExtend key = new ApplicationExtend();
        key.setId(id);
        ApplicationExtend record = (ApplicationExtend) this.loadFromWriterDB("application_extend.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends ApplicationExtendCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ApplicationExtendCriteria example) {
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
        return "applicatoin_extend";
    }
}
