/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IAppPackageDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.AppPackage;
import com.qlangtech.tis.manage.biz.dal.pojo.AppPackageCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
        Integer count = this.count("app_package.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(AppPackageCriteria example) {
        Integer count = this.countFromWriterDB("app_package.ibatorgenerated_countByExample", example);
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
        AppPackage record = this.load("app_package.ibatorgenerated_selectByPrimaryKey", key);
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
        AppPackage record = this.loadFromWriterDB("app_package.ibatorgenerated_selectByPrimaryKey", key);
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
