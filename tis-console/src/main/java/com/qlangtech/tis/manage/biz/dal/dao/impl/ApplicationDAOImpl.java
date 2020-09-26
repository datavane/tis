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

import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ApplicationDAOImpl extends BasicDAO<Application, ApplicationCriteria> implements IApplicationDAO {

    @Override
    public String getEntityName() {
        return "application";
    }

    public ApplicationDAOImpl() {
        super();
    }

    @Override
    public Application selectByName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("param name can not be null");
        }
        Application key = new Application();
        key.setProjectName(name);
        return (Application) this.load("application.ibatorgenerated_selectByName", key);
    // return record;
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

    @Override
    public int updateLastProcessTime(String appname) {
        if (StringUtils.isBlank(appname)) {
            throw new IllegalArgumentException("param appId can not be null");
        }
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.createCriteria().andProjectNameEqualTo(appname);
        return this.updateRecords("application.ibatorgenerated_updateLastProcessTime", criteria);
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
