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
import com.qlangtech.tis.manage.biz.dal.dao.IGlobalAppResourceDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.GlobalAppResource;
import com.qlangtech.tis.manage.biz.dal.pojo.GlobalAppResourceCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
        Integer count = this.count("global_app_resource.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(GlobalAppResourceCriteria example) {
        Integer count = this.countFromWriterDB("global_app_resource.ibatorgenerated_countByExample", example);
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
        GlobalAppResource record = this.load("global_app_resource.ibatorgenerated_selectByPrimaryKey", key);
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
        GlobalAppResource record = this.loadFromWriterDB("global_app_resource.ibatorgenerated_selectByPrimaryKey", key);
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
