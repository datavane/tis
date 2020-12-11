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

import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationExtendDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationExtend;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationExtendCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ApplicationExtendDAOImpl extends BasicDAO<ApplicationExtend, ApplicationExtendCriteria> implements IApplicationExtendDAO {

    public ApplicationExtendDAOImpl() {
        super();
    }

    public int countByExample(ApplicationExtendCriteria example) {
        Integer count = this.count("application_extend.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ApplicationExtendCriteria example) {
        Integer count = this.countFromWriterDB("application_extend.ibatorgenerated_countByExample", example);
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
        ApplicationExtend record = this.load("application_extend.ibatorgenerated_selectByPrimaryKey", key);
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
        ApplicationExtend record = this.loadFromWriterDB("application_extend.ibatorgenerated_selectByPrimaryKey", key);
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

    /* (non-Javadoc)
	 * @see com.taobao.terminator.manage.common.OperationLogger#getEntityName()
	 */
    @Override
    public String getEntityName() {
        // TODO Auto-generated method stub
        return "applicatoin_extend";
    }
}
