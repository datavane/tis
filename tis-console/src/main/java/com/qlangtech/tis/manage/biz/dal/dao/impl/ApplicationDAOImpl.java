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
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ApplicationDAOImpl extends BasicDAO<Application, ApplicationCriteria> implements IApplicationDAO {

    @Override
    public String getEntityName() {
        return "application";
    }

    public ApplicationDAOImpl() {
        super();
    }

    public int countByExample(ApplicationCriteria example) {
        Integer count = (Integer) this.count("application.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ApplicationCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("application.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ApplicationCriteria criteria) {
        return this.deleteRecords("application.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer appId) {
        Application key = new Application();
        key.setAppId(appId);
        return this.deleteRecords("application.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(Application record) {
        Object newKey = this.insert("application.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(Application record) {
        Object newKey = this.insert("application.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<Application> selectByExample(ApplicationCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<Application> selectByExample(ApplicationCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Application> list = this.list("application.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Application selectByPrimaryKey(Integer appId) {
        Application key = new Application();
        key.setAppId(appId);
        Application record = (Application) this.load("application.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Application record, ApplicationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("application.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Application record, ApplicationCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("application.ibatorgenerated_updateByExample", parms);
    }

    public Application loadFromWriteDB(Integer appId) {
        Application key = new Application();
        key.setAppId(appId);
        Application record = (Application) this.loadFromWriterDB("application.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends ApplicationCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ApplicationCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
