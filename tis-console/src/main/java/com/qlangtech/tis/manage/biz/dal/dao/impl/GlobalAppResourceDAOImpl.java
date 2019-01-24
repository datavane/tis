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
import com.qlangtech.tis.manage.biz.dal.dao.IGlobalAppResourceDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.GlobalAppResource;
import com.qlangtech.tis.manage.biz.dal.pojo.GlobalAppResourceCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GlobalAppResourceDAOImpl extends BasicDAO<GlobalAppResource, GlobalAppResourceCriteria> implements IGlobalAppResourceDAO {

    @Override
    public String getEntityName() {
        return "alobal_app_resource";
    }

    public GlobalAppResourceDAOImpl() {
        super();
    }

    public int countByExample(GlobalAppResourceCriteria example) {
        Integer count = (Integer) this.count("global_app_resource.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(GlobalAppResourceCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("global_app_resource.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(GlobalAppResourceCriteria criteria) {
        return this.deleteRecords("global_app_resource.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long appResId) {
        GlobalAppResource key = new GlobalAppResource();
        key.setAppResId(appResId);
        return this.deleteRecords("global_app_resource.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(GlobalAppResource record) {
        Object newKey = this.insert("global_app_resource.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(GlobalAppResource record) {
        Object newKey = this.insert("global_app_resource.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<GlobalAppResource> selectByExample(GlobalAppResourceCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<GlobalAppResource> selectByExample(GlobalAppResourceCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<GlobalAppResource> list = this.list("global_app_resource.ibatorgenerated_selectByExample", example);
        return list;
    }

    public GlobalAppResource selectByPrimaryKey(Long appResId) {
        GlobalAppResource key = new GlobalAppResource();
        key.setAppResId(appResId);
        GlobalAppResource record = (GlobalAppResource) this.load("global_app_resource.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(GlobalAppResource record, GlobalAppResourceCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("global_app_resource.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(GlobalAppResource record, GlobalAppResourceCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("global_app_resource.ibatorgenerated_updateByExample", parms);
    }

    public GlobalAppResource loadFromWriteDB(Long appResId) {
        GlobalAppResource key = new GlobalAppResource();
        key.setAppResId(appResId);
        GlobalAppResource record = (GlobalAppResource) this.loadFromWriterDB("global_app_resource.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends GlobalAppResourceCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, GlobalAppResourceCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
