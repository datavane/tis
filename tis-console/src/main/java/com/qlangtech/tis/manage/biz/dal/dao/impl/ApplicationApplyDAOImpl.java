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

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationApplyDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApply;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApplyCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ApplicationApplyDAOImpl extends BasicDAO<ApplicationApply, ApplicationApplyCriteria> implements IApplicationApplyDAO {

    @Override
    public String getEntityName() {
        return "application_apply";
    }

    public ApplicationApplyDAOImpl() {
        super();
    }

    public int countByExample(ApplicationApplyCriteria example) {
        Integer count = (Integer) this.count("application_apply.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ApplicationApplyCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("application_apply.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ApplicationApplyCriteria criteria) {
        return this.deleteRecords("application_apply.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer appId) {
        ApplicationApply key = new ApplicationApply();
        key.setAppId(appId);
        return this.deleteRecords("application_apply.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(ApplicationApply record) {
        Object newKey = this.insert("application_apply.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(ApplicationApply record) {
        Object newKey = this.insert("application_apply.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<ApplicationApply> selectByExample(ApplicationApplyCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<ApplicationApply> selectByExample(ApplicationApplyCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<ApplicationApply> list = this.list("application_apply.ibatorgenerated_selectByExample", example);
        return list;
    }

    public ApplicationApply selectByPrimaryKey(Integer appId) {
        ApplicationApply key = new ApplicationApply();
        key.setAppId(appId);
        ApplicationApply record = (ApplicationApply) this.load("application_apply.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(ApplicationApply record, ApplicationApplyCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("application_apply.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(ApplicationApply record, ApplicationApplyCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("application_apply.ibatorgenerated_updateByExample", parms);
    }

    public ApplicationApply loadFromWriteDB(Integer appId) {
        ApplicationApply key = new ApplicationApply();
        key.setAppId(appId);
        ApplicationApply record = (ApplicationApply) this.loadFromWriterDB("application_apply.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends ApplicationApplyCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ApplicationApplyCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
