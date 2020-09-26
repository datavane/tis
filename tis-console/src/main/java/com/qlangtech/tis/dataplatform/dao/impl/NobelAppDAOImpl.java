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
package com.qlangtech.tis.dataplatform.dao.impl;

import com.qlangtech.tis.dataplatform.dao.INobelAppDAO;
import com.qlangtech.tis.dataplatform.pojo.NobelApp;
import com.qlangtech.tis.dataplatform.pojo.NobelAppCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class NobelAppDAOImpl extends BasicDAO<NobelApp, NobelAppCriteria> implements INobelAppDAO {

    @Override
    public String getEntityName() {
        return "nobel_app";
    }

    public NobelAppDAOImpl() {
        super();
    }

    public int countByExample(NobelAppCriteria example) {
        Integer count = (Integer) this.count("nobel_app.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(NobelAppCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("nobel_app.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(NobelAppCriteria criteria) {
        return this.deleteRecords("nobel_app.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        NobelApp key = new NobelApp();
        key.setId(id);
        return this.deleteRecords("nobel_app.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(NobelApp record) {
        Object newKey = this.insert("nobel_app.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(NobelApp record) {
        Object newKey = this.insert("nobel_app.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<NobelApp> selectByExample(NobelAppCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<NobelApp> selectByExample(NobelAppCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<NobelApp> list = this.list("nobel_app.ibatorgenerated_selectByExample", example);
        return list;
    }

    public NobelApp selectByPrimaryKey(Long id) {
        NobelApp key = new NobelApp();
        key.setId(id);
        NobelApp record = (NobelApp) this.load("nobel_app.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(NobelApp record, NobelAppCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("nobel_app.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(NobelApp record, NobelAppCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("nobel_app.ibatorgenerated_updateByExample", parms);
    }

    public NobelApp loadFromWriteDB(Long id) {
        NobelApp key = new NobelApp();
        key.setId(id);
        NobelApp record = (NobelApp) this.loadFromWriterDB("nobel_app.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends NobelAppCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, NobelAppCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
