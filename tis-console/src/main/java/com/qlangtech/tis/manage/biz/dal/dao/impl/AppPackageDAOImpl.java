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
import com.qlangtech.tis.manage.biz.dal.dao.IAppPackageDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.AppPackage;
import com.qlangtech.tis.manage.biz.dal.pojo.AppPackageCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppPackageDAOImpl extends BasicDAO<AppPackage, AppPackageCriteria> implements IAppPackageDAO {

    @Override
    public String getEntityName() {
        return "app_package";
    }

    public AppPackageDAOImpl() {
        super();
    }

    public int countByExample(AppPackageCriteria example) {
        Integer count = (Integer) this.count("app_package.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(AppPackageCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("app_package.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(AppPackageCriteria criteria) {
        return this.deleteRecords("app_package.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer pid) {
        AppPackage key = new AppPackage();
        key.setPid(pid);
        return this.deleteRecords("app_package.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(AppPackage record) {
        return (Integer) this.insert("app_package.ibatorgenerated_insert", record);
    }

    public Integer insertSelective(AppPackage record) {
        return (Integer) this.insert("app_package.ibatorgenerated_insertSelective", record);
    }

    public List<AppPackage> selectByExample(AppPackageCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<AppPackage> selectByExample(AppPackageCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<AppPackage> list = this.list("app_package.ibatorgenerated_selectByExample", example);
        return list;
    }

    public AppPackage selectByPrimaryKey(Integer pid) {
        AppPackage key = new AppPackage();
        key.setPid(pid);
        AppPackage record = (AppPackage) this.load("app_package.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(AppPackage record, AppPackageCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("app_package.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(AppPackage record, AppPackageCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("app_package.ibatorgenerated_updateByExample", parms);
    }

    public AppPackage loadFromWriteDB(Integer pid) {
        AppPackage key = new AppPackage();
        key.setPid(pid);
        AppPackage record = (AppPackage) this.loadFromWriterDB("app_package.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends AppPackageCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, AppPackageCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
