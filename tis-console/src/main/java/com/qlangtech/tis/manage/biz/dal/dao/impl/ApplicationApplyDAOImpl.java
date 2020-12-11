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
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationApplyDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApply;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApplyCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
        Integer count = this.count("application_apply.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ApplicationApplyCriteria example) {
        Integer count = this.countFromWriterDB("application_apply.ibatorgenerated_countByExample", example);
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
        ApplicationApply record = this.load("application_apply.ibatorgenerated_selectByPrimaryKey", key);
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
        ApplicationApply record = this.loadFromWriterDB("application_apply.ibatorgenerated_selectByPrimaryKey", key);
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
